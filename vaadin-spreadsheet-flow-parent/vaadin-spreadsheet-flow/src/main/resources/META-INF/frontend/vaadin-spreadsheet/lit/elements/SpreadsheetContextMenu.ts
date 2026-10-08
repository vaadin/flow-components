/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, nothing, type PropertyValues, type TemplateResult } from 'lit';
import { showInTopLayer } from '../dom/TopLayer.js';
import { animateIn, animateOut, overlayAnimationStyles } from '../dom/OverlayAnimation.js';
import { actionIconSlotName } from '../dom/VirtualChildren.js';
import type { SpreadsheetActionDetails } from '../types/wire.js';

/**
 * Input state for the {@link SpreadsheetContextMenu} element.
 *
 * The server decides what a right-click offers: the client reports where the
 * click landed, the server answers with `showActions`, and this element
 * displays whatever came back. Nothing is rendered until then.
 */
export interface ContextMenuRenderState {
  /** Whether the menu is showing. */
  open: boolean;
  /** Viewport x of the click the menu belongs to. */
  x: number;
  /** Viewport y of the click. */
  y: number;
  /** Actions the server offered for this click. */
  actions: ReadonlyArray<SpreadsheetActionDetails>;
}

/** Helper: build a closed, empty context-menu state. */
export function emptyContextMenuRenderState(): ContextMenuRenderState {
  return { open: false, x: 0, y: 0, actions: [] };
}

/**
 * Internal context-menu element, shown on right-click over a cell or a
 * row/column header.
 *
 * Selecting an item dispatches `context-menu-action` carrying the action's
 * `key` and `type`; the connector turns that into the matching server event
 * (`actionOnCurrentSelection`, `actionOnRowHeader` or `actionOnColumnHeader`).
 * Closing it from the keyboard dispatches `context-menu-dismiss`, which the
 * connector reports as `contextMenuClosed`.
 *
 * The keyboard works as the GWT client's menu bar had it: the menu takes
 * the focus when it opens, with no item in hand; Down and Up take the first
 * item and then go down or up the list and round, Enter picks the item in
 * hand, Left and Tab close the menu. The item under the pointer is the one
 * in hand.
 *
 * Built as an internal element rather than on `@vaadin/context-menu`: the Lit
 * client has no `@vaadin/*` dependency today, and adding one to the
 * Spreadsheet's dependency graph is a product decision rather than a detail of
 * this rewrite.
 *
 * No-decorator pattern matching every other internal element (slice 132+).
 */
export class SpreadsheetContextMenu extends LitElement {
  /**
   * Parts this element re-exports to the tree above it.
   *
   * `::part()` crosses exactly one shadow boundary. Every part the
   * spreadsheet documents is emitted inside one of these sub-elements,
   * which sit inside the component's own shadow root — two boundaries
   * away from an application's stylesheet. Without re-exporting them,
   * `vaadin-spreadsheet::part(cell)` matches nothing, and the whole
   * documented styling API silently does nothing.
   */
  static readonly EXPORTED_PARTS = 'context-menu,context-menu-item';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = [
    overlayAnimationStyles,
    css`
      /*
     * The sheet's origin. The menu is given the point clicked in the
     * sheet's own coordinates and measures this element to turn it back
     * into the page's. The menu itself goes in the top layer, where no
     * ancestor's transform applies: a fixed box inside a dialog overlay,
     * which is transformed while it animates, was drawn 16px off and
     * jumped a moment later.
     */
      :host {
        position: absolute;
        left: 0;
        top: 0;
        z-index: 10;
      }

      :host([hidden]) {
        display: none;
      }

      /* The menu as the GWT client drew it: a white card with a soft
       shadow, its items in 27px rows. */
      /* In the top layer while open, as the GWT client's menu was outside
       the sheet: the sheet clips what overflows it, and a menu opened near
       its bottom or right edge lost its last items. Absolutely placed
       there, so in page coordinates and moving with the page. The inset
       and margin undo the browser's popover defaults; a menu taller than
       the window scrolls. */
      .menu {
        position: absolute;
        inset: auto;
        margin: 0;
        overflow: hidden auto;
        padding: 4px;
        box-sizing: border-box;
        background: var(--vaadin-spreadsheet-contextmenu-bg, var(--_vaadin-spreadsheet-contextmenu-bg, #fff));
        border: 0;
        border-radius: 4px;
        /* The edge is the last shadow, a hairline that takes no room. */
        box-shadow: 0 4px 10px 0 rgba(0, 0, 0, 0.1), 0 3px 5px 0 rgba(0, 0, 0, 0.05),
          0 0 0 1px
            var(
              --vaadin-spreadsheet-contextmenu-border,
              var(--_vaadin-spreadsheet-contextmenu-border, rgba(0, 0, 0, 0.09))
            );
        font: inherit;
        color: #474747;
        outline: none;
      }

      /* Dropping into place as it opens and fading out as it closes, as
       the GWT client's menu did. */
      .menu.animate-in {
        animation: overlay-animate-in 120ms;
      }

      .menu.animate-out {
        animation: overlay-animate-out 120ms;
      }

      .item {
        position: relative;
        display: flex;
        align-items: center;
        gap: 5px;
        width: 100%;
        box-sizing: border-box;
        padding: 0 20px 0 10px;
        border: 0;
        border-radius: 3px;
        background: transparent;
        font: inherit;
        font-weight: 400;
        line-height: 27px;
        color: inherit;
        text-align: left;
        cursor: pointer;
        white-space: nowrap;
      }

      /* The item under the pointer on the GWT client's blue, in light type.
       A stylesheet that gives it a lighter ground through the property
       sets its text through ::part(context-menu-item). */
      .item:hover,
      .item:focus {
        background: var(
          --vaadin-spreadsheet-contextmenu-hover-bg,
          var(--_vaadin-spreadsheet-contextmenu-hover-bg, linear-gradient(to bottom, #1b87e3 2%, #166ed6 98%))
        );
        color: #ecf2f8;
        outline: none;
      }

      /* The item being pressed, darkened a little, as the GWT client did. */
      .item:active::before {
        content: '';
        position: absolute;
        inset: 0;
        background: #0957a6;
        opacity: 0.15;
        pointer-events: none;
        border-radius: inherit;
      }

      /* SpreadsheetTheme.LUMO, as the GWT client drew it: Lumo's card and
       spacing, and the item under the pointer in the primary tint. */
      :host([theme~='lumo']) .menu {
        padding: var(--lumo-space-xs);
        border-radius: var(--lumo-border-radius-s);
        color: var(--lumo-body-text-color);
        box-shadow: var(--lumo-box-shadow-s),
          0 0 0 1px
            var(
              --vaadin-spreadsheet-contextmenu-border,
              var(--_vaadin-spreadsheet-contextmenu-border, rgba(0, 0, 0, 0.09))
            );
      }

      :host([theme~='lumo']) .item {
        gap: var(--lumo-space-xs);
        padding: 0 var(--lumo-space-l) 0 var(--lumo-space-m);
        border-radius: var(--lumo-border-radius-s);
        line-height: calc(1.5 * var(--lumo-line-height-m));
        cursor: var(--lumo-clickable-cursor);
      }

      :host([theme~='lumo']) .item:hover,
      :host([theme~='lumo']) .item:focus {
        color: var(--lumo-body-text-color);
      }

      :host([theme~='lumo']) .item:active::before {
        background: var(--lumo-primary-color);
      }
    `
  ];

  declare renderState: ContextMenuRenderState;

  /** The menu as it was when it closed, drawn while it fades out. */
  private fading: ContextMenuRenderState | null = null;

  constructor() {
    super();
    this.renderState = emptyContextMenuRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', SpreadsheetContextMenu.EXPORTED_PARTS);
  }

  firstUpdated(): void {
    const root = this.shadowRoot!;
    root.addEventListener('click', (e: Event) => {
      const target = e.target;
      if (!(target instanceof Element)) {
        return;
      }
      const item = target.closest('.item');
      if (item instanceof HTMLElement) {
        this.pick(item);
      }
    });
    root.addEventListener('keydown', this.handleKeyDown as EventListener);
    // The item under the pointer is the one in hand, and none is once the
    // pointer has left the items.
    root.addEventListener('pointerover', (e: Event) => {
      const item = e.target instanceof Element ? e.target.closest('.item') : null;
      if (item instanceof HTMLElement) {
        item.focus({ preventScroll: true });
      }
    });
    root.addEventListener('pointerout', (e: Event) => {
      const to = (e as PointerEvent).relatedTarget;
      if (!(to instanceof Element && to.closest('.item')) && this.itemInHand() !== null) {
        this.panel()?.focus({ preventScroll: true });
      }
    });
  }

  willUpdate(changed: PropertyValues<this>): void {
    if (!changed.has('renderState')) {
      return;
    }
    const before = changed.get('renderState') as ContextMenuRenderState | undefined;
    if (isShowing(this.renderState)) {
      // Opened again while it fades: it stays.
      this.fading = null;
    } else if (before !== undefined && isShowing(before)) {
      this.fading = before;
    }
  }

  updated(changed: Map<string, unknown>): void {
    if (!changed.has('renderState')) {
      return;
    }
    const panel = this.panel();
    if (panel === null) {
      return;
    }
    const fading = this.fading;
    if (fading !== null) {
      animateOut(panel, () => {
        if (this.fading === fading) {
          this.fading = null;
          this.requestUpdate();
        }
      });
      return;
    }
    // Shown before it is placed: a popover that is not open takes no room
    // and cannot be measured.
    showInTopLayer(panel);
    this.place(panel);
    panel.focus({ preventScroll: true });
    // Only as it opens: offered its actions again while open, the GWT
    // client's menu stayed put.
    const before = changed.get('renderState') as ContextMenuRenderState | undefined;
    if (before === undefined || !isShowing(before)) {
      animateIn(panel);
    }
  }

  /**
   * Where the GWT client put its menu: at the point clicked, to the left of
   * it when it would cross the right edge of the window, and moved up to
   * fit when it would cross the bottom - no higher than the top, where it
   * is as tall as the window.
   */
  private place(panel: HTMLElement): void {
    const { x, y } = this.renderState;
    // The menu is placed in the sheet's space, which starts where this
    // element does.
    const origin = this.getBoundingClientRect();
    panel.style.maxHeight = '';
    const width = panel.offsetWidth;
    const height = panel.offsetHeight;
    const windowWidth = document.documentElement.clientWidth;
    const windowHeight = document.documentElement.clientHeight;
    let left = origin.left + x;
    let top = origin.top + y;
    if (left + width > windowWidth) {
      left = Math.max(0, left - width);
    }
    if (top + height > windowHeight) {
      top = Math.max(0, windowHeight - height);
    }
    if (top === 0) {
      panel.style.maxHeight = `${windowHeight}px`;
    }
    if (panel.matches(':popover-open')) {
      panel.style.left = `${left + window.scrollX}px`;
      panel.style.top = `${top + window.scrollY}px`;
    } else {
      // No top layer in this browser: placed in the sheet's own space.
      panel.style.left = `${left - origin.left}px`;
      panel.style.top = `${top - origin.top}px`;
    }
  }

  private readonly handleKeyDown = (e: KeyboardEvent): void => {
    const items = Array.from(this.shadowRoot!.querySelectorAll<HTMLElement>('.item'));
    if (items.length === 0) {
      return;
    }
    const inHand = this.itemInHand();
    const index = inHand === null ? -1 : items.indexOf(inHand);
    const take = (i: number): void => items[(i + items.length) % items.length].focus({ preventScroll: true });
    switch (e.key) {
      case 'ArrowDown':
        take(index < 0 ? 0 : index + 1);
        break;
      case 'ArrowUp':
        take(index < 0 ? 0 : index - 1);
        break;
      case 'ArrowRight':
        if (index < 0) {
          take(0);
        }
        break;
      case 'ArrowLeft':
        if (index < 0) {
          take(0);
        } else {
          this.dismiss();
        }
        break;
      case 'Enter':
        if (inHand === null) {
          take(0);
        } else {
          this.pick(inHand);
        }
        break;
      case 'Tab':
        this.dismiss();
        break;
      default:
        return;
    }
    // The keys are the menu's: none of them reaches the sheet, and Enter
    // does not click the button a second time.
    e.preventDefault();
    e.stopPropagation();
  };

  private panel(): HTMLElement | null {
    return this.shadowRoot?.querySelector<HTMLElement>('.menu') ?? null;
  }

  private itemInHand(): HTMLElement | null {
    const active = this.shadowRoot?.activeElement;
    return active instanceof HTMLElement && active.classList.contains('item') ? active : null;
  }

  private pick(item: HTMLElement): void {
    const key = item.dataset.key;
    const type = Number(item.dataset.type);
    if (key === undefined) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('context-menu-action', {
        detail: { key, type },
        bubbles: true,
        composed: true
      })
    );
  }

  private dismiss(): void {
    this.dispatchEvent(new CustomEvent('context-menu-dismiss', { bubbles: true, composed: true }));
  }

  render(): TemplateResult | typeof nothing {
    const state = isShowing(this.renderState) ? this.renderState : this.fading;
    if (state === null) {
      return nothing;
    }
    const { actions } = state;
    return html`
      <div class="menu" part="context-menu" tabindex="-1" popover="manual">
        ${actions.map(
          (action) => html`
            <button class="item" part="context-menu-item" type="button" data-key=${action.key} data-type=${action.type}>
              ${action.iconNodeId
                ? html`<slot class="item-icon" name=${actionIconSlotName(String(action.iconNodeId))}></slot>`
                : nothing}
              ${action.caption}
            </button>
          `
        )}
      </div>
    `;
  }
}

/** Whether a menu with this state is drawn. */
function isShowing(state: ContextMenuRenderState): boolean {
  return state.open && state.actions.length > 0;
}

customElements.define('vaadin-spreadsheet-internal-context-menu', SpreadsheetContextMenu);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-context-menu': SpreadsheetContextMenu;
  }
}
