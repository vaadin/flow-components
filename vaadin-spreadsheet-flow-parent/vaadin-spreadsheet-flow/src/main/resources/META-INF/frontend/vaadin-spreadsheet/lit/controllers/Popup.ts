/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { ReactiveController } from 'lit';
import { popupPlacement } from '../algorithms/Placement.js';
import { listen } from '../dom/Listen.js';
import { animateIn, animateOut } from '../dom/OverlayAnimation.js';
import { popupOverlayRules } from '../dom/PopupOverlayStyles.js';
import { hideFromTopLayer, showInTopLayer } from '../dom/TopLayer.js';
import {
  flowVirtualChildResolver,
  namedSlot,
  popupContentSlotName,
  SlottedVirtualChildren,
  type VirtualChildResolver
} from '../dom/VirtualChildren.js';
import type { SpreadsheetCellGrid } from '../elements/SpreadsheetCellGrid.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';
import type { PopupButtonState } from '../types/wire.js';

/** What the popups need of the sheet. */
export interface PopupHost {
  /** The text of a cell, which a popup's header shows. */
  cellValue(col: number, row: number): string;
  /** The scrolling pane on screen, which a popup keeps to. */
  paneClientRect(): { left: number; top: number; bottom: number };
  /** Gives the keyboard to the sheet, where its button has gone. */
  focusSheet(): void;
  /** Draws the sheet again, its popup buttons included. */
  scheduleRender(): void;
  /** Tells the server. */
  send(event: SpreadsheetEvent): void;
}

/**
 * The popup buttons the server puts on cells - a table's filters, say - and
 * the popup one opens. A click on a button asks the server, which opens the
 * popup with its content; the popup sits by the button's cell, follows it
 * when the sheet scrolls, and closes on a click elsewhere, on Escape, or on
 * its button. Opened from the keyboard, it takes the keyboard and gives it
 * back when it closes.
 */
export class Popup implements ReactiveController {
  /** The overlay the open popup's content is shown in. */
  readonly overlay: HTMLElement;

  /** The popup buttons the server has added, one per cell. */
  private buttonStates: PopupButtonState[] = [];

  /**
   * Content of the popup a popup button opens. The server attaches it as a
   * virtual child of the popup button, so it needs placing like any other
   * hosted component; it goes in its own holder because it is shown in an
   * overlay rather than in the grid.
   */
  private readonly contents: SlottedVirtualChildren;

  /** The popup that is open, or null when none is. */
  private openPopup: { row: number; col: number; nodeId: string } | null = null;

  /**
   * The button whose press has just closed its own popup: the click that
   * follows is not to open it again.
   */
  private closedByItsButton: { row: number; col: number } | null = null;

  /** The popup whose button was pressed from the keyboard, until it opens. */
  private openedFromKeyboard: { row: number; col: number } | null = null;

  /**
   * Places the open popup again when its size changes: its content is
   * Flow's components, which render after they are attached, so the size
   * the popup opens at is seldom the one it settles at.
   */
  private readonly resizeObserver: ResizeObserver;

  constructor(
    contentHost: HTMLElement,
    resolveVirtualChild: VirtualChildResolver,
    private readonly grid: SpreadsheetCellGrid,
    private readonly host: PopupHost,
    signal: AbortSignal
  ) {
    // The popup overlay is the sheet's own element inside its shadow
    // root, one boundary from the host's light DOM, so the slot it renders
    // projects the content directly — no forwarder, hence the same element
    // for both hosts.
    this.overlay = document.createElement('div');
    this.overlay.className = 'popup-overlay';
    this.overlay.setAttribute('part', 'popup-overlay');
    this.overlay.hidden = true;
    // In the top layer while open: no edge of the sheet or of the page around it cuts it off. Absolutely
    // placed there, so in page coordinates and moving with the page. The
    // inset, margin and overflow undo the browser's popover defaults:
    // content taller than a set height shows
    // past it rather than scrolling. Drawn by the rules the overlay carries
    // - see popupOverlayRules.
    this.overlay.popover = 'manual';
    // Takes the keyboard when it opens from it without a close box.
    this.overlay.tabIndex = -1;
    this.overlay.style.cssText = 'position:absolute;inset:auto;margin:0;overflow:visible;box-sizing:border-box;';
    this.resizeObserver = new ResizeObserver(() => this.reposition());
    this.contents = new SlottedVirtualChildren(contentHost, this.overlay, resolveVirtualChild, popupContentSlotName);
    listen(grid, 'popup-button-click', this.handlePopupButtonClick, signal);
  }

  /** The popup buttons, for the sheet to draw. */
  buttons(): readonly PopupButtonState[] {
    return this.buttonStates;
  }

  /**
   * A popup (filter) button was attached to a cell.
   *
   * These arrive one at a time over the RPC lane rather than as a list
   * property, because on the server each is a real component with its
   * own lifecycle. They go in the same list the renderer already positions,
   * keyed by their cell.
   */
  addButton(state: PopupButtonState): void {
    this.buttonStates = [...this.buttonStates.filter((b) => !sameCell(b, state)), state];
    this.host.scheduleRender();
  }

  /**
   * A button was taken away; its cell is what identifies it, and the rest
   * of the state may differ.
   *
   * The server takes a button away once its cell has scrolled out of view,
   * and when the sheet changes. The popup closes with its button and tells
   * the server it has.
   */
  removeButton(state: PopupButtonState): void {
    // The button goes first, so that a popup closing with it does not give
    // the keyboard back to it.
    this.buttonStates = this.buttonStates.filter((b) => !sameCell(b, state));
    if (this.openPopup !== null && this.openPopup.row === state.row && this.openPopup.col === state.col) {
      this.dismiss(true);
    }
    this.host.scheduleRender();
  }

  /**
   * Show a popup button's content.
   *
   * Called by {@code PopupButton.openPopup()} — which the server also runs
   * when it handles the button's own click, so this is the only path that
   * ever opens a popup, whether the user clicked the button or application
   * code asked for it.
   *
   * The content arrives as a Flow node id: the server attached it with
   * {@code appendVirtualChild}, so the element exists on the client but is
   * not in the document until it is placed. `appId` names the Flow
   * application to resolve it against — this is the popup button's own
   * element, which may belong to a different application than the sheet
   * when several are on the page.
   */
  open(row: number, column: number, contentId: number | string, appId: string): void {
    const nodeId = String(contentId);
    // Opened again while it shows, the popup stays put.
    const showing = this.openPopup !== null && this.openPopup.row === row && this.openPopup.col === column;
    this.openPopup = { row, col: column, nodeId };
    // A resolver per call rather than the sheet's own: the id belongs to
    // the application the button reports, not necessarily the sheet's.
    this.contents.setResolver(flowVirtualChildResolver(appId));
    this.contents.sync([nodeId]);
    const state = this.buttonStates.find((b) => b.row === row && b.col === column);
    const parts: Node[] = [popupOverlayRules()];
    if (!state?.headerHidden) {
      parts.push(this.buildHeader(this.host.cellValue(column, row)));
    }
    parts.push(namedSlot(popupContentSlotName(nodeId)));
    this.overlay.replaceChildren(...parts);
    this.overlay.hidden = false;
    showInTopLayer(this.overlay);
    this.reposition();
    if (!showing) {
      animateIn(this.overlay);
    }
    this.resizeObserver.observe(this.overlay);
    this.host.scheduleRender();
    // Opened from the keyboard, the keyboard goes into it: to its close
    // box, or to the card itself, from where Tab reaches the content. With
    // the pointer the focus stays where it is.
    const fromKeyboard = this.openedFromKeyboard;
    this.openedFromKeyboard = null;
    if (fromKeyboard !== null && fromKeyboard.row === row && fromKeyboard.col === column) {
      const target = this.overlay.querySelector<HTMLElement>('.popup-close') ?? this.overlay;
      target.focus({ preventScroll: true });
    }
  }

  /** Whether a popup is open. */
  isOpen(): boolean {
    return this.openPopup !== null;
  }

  /**
   * Hide the open popup at the given cell.
   *
   * A no-op when the popup that is open belongs to another cell: the
   * server closes popups one by one and a stale close must not take down
   * the one the user just opened.
   */
  close(row: number, column: number): void {
    if (this.openPopup === null || this.openPopup.row !== row || this.openPopup.col !== column) {
      return;
    }
    this.hide(true);
  }

  /**
   * Take the popup down and tell the server. Used for every client-side
   * dismissal — clicking away, Escape, scrolling the anchor out of view.
   * Its content fades out with it unless asked to go first - see hide.
   */
  dismiss(releaseContentFirst = false): void {
    const closing = this.openPopup;
    if (closing === null) {
      return;
    }
    this.hide(releaseContentFirst);
    this.host.send({
      type: 'popupClose',
      data: { row: closing.row, column: closing.col }
    });
  }

  /**
   * A press somewhere on the page. A press inside the popup belongs to the
   * popup's content. A press on the button that opened it closes it, and the
   * click that follows is then not to open it
   * again. Any other press closes it.
   */
  pressed(path: EventTarget[]): void {
    // The content is a light child of the host projected into the overlay,
    // so the path reaches the overlay through the slot — except before the
    // projection settles, hence the second check.
    const insidePopup = path.includes(this.overlay) || path.some((target) => this.contents.owns(target));
    const open = this.openPopup;
    this.closedByItsButton = open !== null && this.hitOpenButton(path) ? { row: open.row, col: open.col } : null;
    if (open !== null && !insidePopup) {
      this.dismiss();
    }
  }

  /**
   * Place the overlay at its button - see {@link popupPlacement} - against the cell as it is drawn now and the
   * scrolling pane around it.
   *
   * While the cell is outside the rendered range the overlay stays where it
   * is.
   */
  reposition(): void {
    if (this.openPopup === null) {
      return;
    }
    const { row, col } = this.openPopup;
    const state = this.buttonStates.find((b) => b.row === row && b.col === col);
    const overlay = this.overlay;
    overlay.style.width = state?.popupWidth ?? '';
    overlay.style.height = state?.popupHeight ?? '';
    const cell = this.grid.cellClientRect(col, row);
    if (cell === null) {
      return;
    }
    // Measured at the page's corner, where nothing squeezes it: next to the
    // right edge of the page an overlay of no set width wraps to fit.
    overlay.style.left = '0px';
    overlay.style.top = '0px';
    const place = popupPlacement(cell, this.host.paneClientRect(), overlay.offsetWidth, overlay.offsetHeight);
    overlay.style.left = `${place.left + window.scrollX}px`;
    overlay.style.top = `${place.top + window.scrollY}px`;
  }

  /** Stop following the popup's size, as the sheet leaves the page. */
  hostDisconnected(): void {
    this.resizeObserver.disconnect();
  }

  /** Release the content and take the overlay out of the top layer for good. */
  dispose(): void {
    this.contents.clear();
    hideFromTopLayer(this.overlay);
  }

  /**
   * Popup-button click handler. Translates the cell-grid's local event into
   * a `popupButtonClick` for the server, which opens the popup.
   */
  private readonly handlePopupButtonClick = (
    e: CustomEvent<{ col: number; row: number; keyboard?: boolean }>
  ): void => {
    const { col, row, keyboard } = e.detail;
    const closed = this.closedByItsButton;
    this.closedByItsButton = null;
    if (closed !== null && closed.row === row && closed.col === col) {
      return;
    }
    this.openedFromKeyboard = keyboard === true ? { row, col } : null;
    this.host.send({
      type: 'popupButtonClick',
      data: { row, column: col }
    });
  };

  /**
   * The popup's header strip: its close box and the text of the cell the
   * button is on - a filter's column title.
   *
   * Shown unless the button asked for {@code headerHidden}, which is what
   * {@code PopupButton.setHeaderHidden(true)} sets: a popup with no header
   * has to be dismissed by clicking away from it.
   */
  private buildHeader(caption: string): HTMLElement {
    const header = document.createElement('div');
    header.className = 'popup-header';
    header.setAttribute('part', 'popup-header');
    const close = document.createElement('button');
    close.type = 'button';
    close.className = 'popup-close';
    close.setAttribute('part', 'popup-close');
    close.setAttribute('aria-label', 'Close');
    close.addEventListener('click', () => this.dismiss());
    const text = document.createElement('div');
    text.className = 'header-caption';
    text.setAttribute('part', 'popup-caption');
    text.textContent = caption;
    header.append(close, text);
    return header;
  }

  /**
   * Hide the overlay and release its content, without telling the server.
   *
   * The card fades out and then goes. What it held fades with it when the
   * user closed it; when the server did, or took the button away, the
   * content goes first and the empty card fades.
   */
  private hide(releaseContentFirst: boolean): void {
    const closing = this.openPopup;
    // The keyboard was in the popup: it goes back to the button, or to the
    // sheet when the button has gone, rather than to nowhere.
    const hadFocus = this.hasFocus();
    this.openPopup = null;
    this.resizeObserver.unobserve(this.overlay);
    if (releaseContentFirst) {
      this.contents.clear();
    }
    if (hadFocus && closing !== null) {
      const stays = this.buttonStates.some((b) => b.row === closing.row && b.col === closing.col);
      const button = stays ? this.grid.popupButton(closing.col, closing.row) : null;
      if (button !== null) {
        button.focus({ preventScroll: true });
      } else {
        this.host.focusSheet();
      }
    }
    const overlay = this.overlay;
    animateOut(overlay, () => {
      this.contents.clear();
      overlay.replaceChildren();
      overlay.hidden = true;
      hideFromTopLayer(overlay);
    });
    this.host.scheduleRender();
  }

  /** Whether the focus is in the popup: its card, or the content it shows. */
  private hasFocus(): boolean {
    let el: Element | null = document.activeElement;
    while (el?.shadowRoot?.activeElement) {
      el = el.shadowRoot.activeElement;
    }
    for (let node: Node | null = el; node !== null; ) {
      if (node === this.overlay || this.contents.owns(node)) {
        return true;
      }
      node = node.parentNode instanceof ShadowRoot ? node.parentNode.host : node.parentNode;
    }
    return false;
  }

  /** Whether an event path passes through the button of the open popup. */
  private hitOpenButton(path: EventTarget[]): boolean {
    const open = this.openPopup;
    if (open === null) {
      return false;
    }
    return path.some(
      (target) =>
        target instanceof Element &&
        target.classList.contains('popup-button') &&
        target.getAttribute('data-col') === String(open.col) &&
        target.getAttribute('data-row') === String(open.row)
    );
  }
}

/** Two popup-button states describe the same button when they share a cell. */
function sameCell(a: PopupButtonState, b: PopupButtonState): boolean {
  return a.col === b.col && a.row === b.row;
}
