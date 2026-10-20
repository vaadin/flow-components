/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, nothing, type TemplateResult } from 'lit';
import { SpreadsheetPart } from './SpreadsheetPart.js';
import { composing } from '../algorithms/KeyboardMoves.js';
import {
  headerBorderColor,
  tabButtonBg,
  tabsActiveBg,
  tabsActiveFg,
  tabsBg,
  tabsBorder,
  tabsFg,
  tabsFont,
  tabsFontSize
} from './ThemeProperties.js';

/**
 * Input state for the {@link SpreadsheetSheetTabs} element.
 * The connector builds + pushes this whenever
 * {@code setSheetNames} or {@code setSheetIndex} changes the
 * sheet list / active sheet.
 */
export interface SheetTabsRenderState {
  /** Sheet names in order (typically what {@code Workbook.getSheetNames()} returns). */
  sheetNames: ReadonlyArray<string>;
  /**
   * Currently active sheet index (1-based, matching the rest of
   * the Spreadsheet API). {@code 0} means no active sheet —
   * tabs render without any highlighted.
   */
  activeIndex: number;
  /**
   * Index of the leftmost tab currently shown, 0-based. With more
   * sheets than fit, the strip scrolls by whole tabs: the scroll buttons
   * move this, and so does a change of the active sheet whose tab is not
   * in view. The server is not told.
   */
  firstVisibleTab: number;
  /**
   * Whether a new sheet can be added. A read-only workbook still shows
   * the button — Excel does — but it does nothing.
   */
  readOnly: boolean;
  /**
   * The tab whose name the user is typing, 1-based, or 0 when none is.
   * Only the active tab is ever renamed, which is what double-clicking
   * one that is not active does first: it makes it active.
   */
  renamingIndex?: number;
  /**
   * What `Spreadsheet.setStatusLabelValue` shows at the right end of the
   * strip, or null for no label at all. An empty string still shows the
   * label, empty.
   */
  infoLabel?: string | null;
}

/** Helper: build the empty state for tests + initial render. */
export function emptySheetTabsRenderState(): SheetTabsRenderState {
  return {
    sheetNames: [],
    activeIndex: 0,
    firstVisibleTab: 0,
    readOnly: false,
    renamingIndex: 0,
    infoLabel: null
  };
}

/** How much of the strip a tab takes, its margins included. */
function tabWidth(tab: HTMLElement): number {
  const style = getComputedStyle(tab);
  return tab.getBoundingClientRect().width + parseFloat(style.marginLeft) + parseFloat(style.marginRight);
}

/**
 * Sheet-tab strip element. Renders one tab per sheet
 * name from {@link SheetTabsRenderState.sheetNames}, highlights
 * the tab at {@link SheetTabsRenderState.activeIndex}. Clicking
 * a tab dispatches a {@code sheet-tab-click} custom event with
 * the clicked index — the connector translates this into a
 * {@code sheetSelected} {@code spreadsheet-event} for the server.
 *
 * Renders empty when {@code sheetNames} is empty.
 *
 * Same no-decorator pattern as the cell-grid + column-header
 * sub-elements.
 */
export class SpreadsheetSheetTabs extends SpreadsheetPart {
  static readonly EXPORTED_PARTS =
    'tab,tab-selected,tab-rename,scroll-tabs-button,scroll-tabs-button-disabled,' +
    'scroll-tabs-to-start-button,scroll-tabs-backward-button,' +
    'scroll-tabs-forward-button,scroll-tabs-to-end-button,new-tab-button';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    /* The buttons sit before the tabs, side by side in 130px. */
    .sheet-tabsheet-options {
      display: inline-flex;
      flex: none;
      align-items: center;
      height: 28px;
    }

    /*
     * The rename input stands in for the tab's own text, so it borrows
     * the tab's typography rather than the browser's and keeps the tab
     * from jumping in size as it appears.
     */
    .sheet-tab-rename {
      width: 100%;
      min-width: 60px;
      box-sizing: border-box;
      padding: 0;
      border: 0;
      background: transparent;
      font: inherit;
      color: inherit;
      outline: none;
    }

    .sheet-tabsheet-container {
      display: inline-flex;
      align-items: flex-start;
      min-width: 0;
      overflow: hidden;
    }

    /* The scroll and add-sheet buttons. Each is an icon painted in the
       button's text colour through a mask - a bare filled box says nothing about what it does, and five of them
       in a row read as decoration. */
    .sheet-tabsheet-options > div {
      width: 26px;
      height: 26px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: ${tabButtonBg};
    }

    .sheet-tabsheet-options > div::before {
      content: '';
      width: 20px;
      height: 20px;
      background: currentColor;
      -webkit-mask-image: var(--_tab-button-icon);
      mask-image: var(--_tab-button-icon);
    }

    /* A blue of their own: the active tab's colour is the app's to change,
       and the buttons do not follow it. */
    .sheet-tabsheet-options > div:hover {
      color: #197de1;
    }

    /* Kept in the DOM so a stylesheet can reach it, but inert. */
    .sheet-tabsheet-options > div.hidden {
      color: #d6d6d6;
      cursor: default;
    }

    .scroll-tabs-beginning {
      --_tab-button-icon: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M15.707 15.707a1 1 0 01-1.414 0l-5-5a1 1 0 010-1.414l5-5a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 010 1.414zm-6 0a1 1 0 01-1.414 0l-5-5a1 1 0 010-1.414l5-5a1 1 0 011.414 1.414L5.414 10l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd"/></svg>');
    }

    .scroll-tabs-left {
      --_tab-button-icon: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>');
    }

    .scroll-tabs-right {
      --_tab-button-icon: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/></svg>');
    }

    .scroll-tabs-end {
      --_tab-button-icon: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10.293 15.707a1 1 0 010-1.414L14.586 10l-4.293-4.293a1 1 0 111.414-1.414l5 5a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0z" clip-rule="evenodd"/><path fill-rule="evenodd" d="M4.293 15.707a1 1 0 010-1.414L8.586 10 4.293 5.707a1 1 0 011.414-1.414l5 5a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0z" clip-rule="evenodd"/></svg>');
    }

    .add-new-tab {
      --_tab-button-icon: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/></svg>');
    }

    /* The strip: 28px under a one-pixel border, with the
       tabs hanging from that border. Tabs past its end are cut off; the
       buttons scroll to them. */
    :host {
      position: relative;
      display: flex;
      flex-direction: row;
      align-items: flex-start;
      box-sizing: border-box;
      height: 29px;
      background: ${tabsBg};
      border-top: 1px solid ${tabsBorder};
      font-family: ${tabsFont};
      font-size: ${tabsFontSize};
      overflow: hidden;
    }

    :host([hidden]) {
      display: none !important;
    }

    /* A tab: a folder tab hanging from the
       strip's top border, its name in light type, cut short with an
       ellipsis past 200px. */
    .sheet-tab {
      flex: none;
      box-sizing: content-box;
      height: 20px;
      min-width: 50px;
      max-width: 200px;
      margin-left: 5px;
      padding: 2px 6px;
      border: 1px solid ${tabsBorder};
      border-top: none;
      border-radius: 0 0 5px 5px;
      cursor: pointer;
      user-select: none;
      background: ${tabsBg};
      color: ${tabsFg};
      font-weight: 300;
      line-height: 20px;
      text-align: center;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    /* The active tab joins the sheet above it: white, in the accent
       colour, and as wide as its name needs. */
    .sheet-tab.active {
      max-width: none;
      background: ${tabsActiveBg};
      color: ${tabsActiveFg};
      cursor: default;
    }

    /* The status label: 200px at the right end of the strip, over the
       tabs' last stretch, which keep out from under it. */
    .sheet-tabsheet-infolabel {
      position: absolute;
      top: 0;
      right: 0;
      box-sizing: content-box;
      width: 200px;
      height: 100%;
      padding: 0 5px;
      border-left: 1px solid ${headerBorderColor};
      background: ${tabsBg};
      font-weight: bold;
      font-size: 11px;
      line-height: 29px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .sheet-tabsheet-container.beside-infolabel {
      margin-right: 206px;
    }

    /* SpreadsheetTheme.LUMO: tinted folder tabs
       in regular weight, the active one lifted off the strip, and buttons
       that light up in the primary tint. */
    :host([theme~='lumo']) {
      height: calc(var(--lumo-font-size-s) + 15px);
    }

    :host([theme~='lumo']) .sheet-tabsheet-options > div {
      cursor: var(--lumo-clickable-cursor);
    }

    :host([theme~='lumo']) .sheet-tabsheet-options > div:hover {
      color: ${tabButtonBg};
      background: var(--lumo-primary-color-10pct);
    }

    :host([theme~='lumo']) .sheet-tabsheet-options > div.hidden {
      color: var(--lumo-contrast-20pct);
    }

    :host([theme~='lumo']) .sheet-tab {
      height: calc(var(--lumo-font-size-s) + 4px);
      margin-left: var(--lumo-space-s);
      padding: var(--lumo-space-xs);
      border-radius: 0 0 var(--lumo-border-radius-m) var(--lumo-border-radius-m);
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
      font-weight: 400;
      cursor: var(--lumo-clickable-cursor);
    }

    /* Lumo's tabs do not change under the pointer. */
    :host([theme~='lumo']) .sheet-tab:hover:not(.active) {
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    }

    :host([theme~='lumo']) .sheet-tab.active {
      background-image: none;
      box-shadow: var(--lumo-box-shadow-xs);
    }

    :host([theme~='lumo']) .sheet-tabsheet-infolabel {
      background: var(--lumo-contrast-5pct);
      font-size: var(--lumo-font-size-s);
      padding: 0 var(--lumo-space-xs);
    }
  `;

  declare renderState: SheetTabsRenderState;

  /**
   * The leftmost tab that still shows the last one: from the last tab back, as many as fit beside the
   * buttons and the status label. Scrolling forward stops there and the
   * end button goes straight to it, so the strip never scrolls into empty
   * space - and with every tab in view, neither button does anything.
   * Measured after each render: only the browser knows how wide the tabs
   * came out.
   */
  private lastFirstTab = 0;

  /**
   * The sheets and the active one the strip last brought the active tab
   * into view for: it does so again only when either changes, so a strip
   * the buttons scrolled stays where they put it.
   */
  private revealedFor: { sheetNames: ReadonlyArray<string>; activeIndex: number } | null = null;

  /**
   * The tab the keyboard is on, 1-based, the only one in the Tab order; 0 for
   * the active sheet's, which it goes back to whenever that changes.
   */
  private focusedTab = 0;

  /** The active sheet the strip last drew, which resets {@link focusedTab}. */
  private drawnActiveIndex = 0;

  /** Whether the next drawing hands the focus to {@link focusedTab}. */
  private focusPending = false;

  private readonly resizeObserver = new ResizeObserver(() => {
    this.measureLastFirstTab();
    this.revealActiveTab();
  });

  constructor() {
    super();
    this.renderState = emptySheetTabsRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    this.resizeObserver.observe(this);
  }

  disconnectedCallback(): void {
    super.disconnectedCallback();
    this.resizeObserver.disconnect();
  }

  /**
   * Work out {@link lastFirstTab} from the tabs as rendered, and render
   * again when it moved. The tabs before the first visible one are not
   * rendered, so the count stops there - which only matters when every
   * rendered tab fits, and then nothing scrolls forward either way.
   */
  private measureLastFirstTab(): void {
    const root = this.shadowRoot;
    const options = root?.querySelector<HTMLElement>('.sheet-tabsheet-options');
    const tabs = Array.from(root?.querySelectorAll<HTMLElement>('.sheet-tab') ?? []);
    const first = this.renderState.firstVisibleTab ?? 0;
    let lastFirstTab = first;
    if (root && options && tabs.length > 0) {
      const label = root.querySelector<HTMLElement>('.sheet-tabsheet-infolabel');
      let room = this.offsetWidth - options.offsetWidth - (label?.offsetWidth ?? 0);
      let index = tabs.length - 1;
      room -= tabWidth(tabs[index]);
      while (index > 0 && room - tabWidth(tabs[index - 1]) > 0) {
        index--;
        room -= tabWidth(tabs[index]);
      }
      lastFirstTab = first + index;
    }
    if (lastFirstTab !== this.lastFirstTab) {
      this.lastFirstTab = lastFirstTab;
      this.requestUpdate();
    }
  }

  /**
   * Bring the active sheet's tab into view once the active sheet or the
   * sheets changed: back to it when it is before the
   * first tab shown, and forward until it shows whole when the strip cuts it
   * off. Asked of the controller, which keeps where the strip starts. A
   * strip not laid out yet has nothing to measure, and tries again once it
   * is.
   */
  private revealActiveTab(): void {
    const { sheetNames, activeIndex } = this.renderState;
    const done = this.revealedFor;
    if (done !== null && done.sheetNames === sheetNames && done.activeIndex === activeIndex) {
      return;
    }
    if (activeIndex < 1 || activeIndex > sheetNames.length) {
      return;
    }
    if (!this.reveal(activeIndex)) {
      return;
    }
    this.revealedFor = { sheetNames, activeIndex };
  }

  /**
   * Ask the controller to scroll the strip so the tab, 1-based, shows whole,
   * if it does not. False when the strip cannot measure yet.
   */
  private reveal(index: number): boolean {
    const firstVisibleTab = this.renderState.firstVisibleTab ?? 0;
    const first = this.firstShowing(index);
    if (first === null) {
      return false;
    }
    if (first !== firstVisibleTab) {
      this.dispatchEvent(
        new CustomEvent('sheet-tabs-scroll', {
          detail: { direction: 'reveal', firstVisibleTab: first },
          bubbles: true,
          composed: true
        })
      );
    }
    return true;
  }

  /**
   * The leftmost tab, 0-based, that shows the tab, 1-based, whole: the first
   * one shown when it already does, the tab itself when it is before it, and
   * otherwise the one that puts it at the right end of the strip. Null when
   * the strip is not laid out yet.
   */
  private firstShowing(index: number): number | null {
    const firstVisibleTab = this.renderState.firstVisibleTab ?? 0;
    const at = index - 1;
    if (at < firstVisibleTab) {
      return at;
    }
    const root = this.shadowRoot;
    const options = root?.querySelector<HTMLElement>('.sheet-tabsheet-options');
    if (!root || !options || this.offsetWidth === 0) {
      return null;
    }
    const label = root.querySelector<HTMLElement>('.sheet-tabsheet-infolabel');
    let room = this.offsetWidth - options.offsetWidth - (label?.offsetWidth ?? 0);
    // The tabs are rendered from the first one shown on.
    const widths = Array.from(root.querySelectorAll<HTMLElement>('.sheet-tab'))
      .slice(0, at - firstVisibleTab + 1)
      .map(tabWidth);
    if (widths.reduce((sum, width) => sum + width, 0) <= room) {
      return firstVisibleTab;
    }
    let i = widths.length - 1;
    room -= widths[i];
    while (i > 0 && room - widths[i - 1] > 0) {
      i--;
      room -= widths[i];
    }
    return firstVisibleTab + i;
  }

  /**
   * Wire shadow-root-level click delegation on first render —
   * same pattern as the cell-grid + headers.
   * Each click that lands on a {@code .sheet-tab} dispatches
   * {@code sheet-tab-click} with the clicked index.
   */
  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
    this.shadowRoot!.addEventListener('dblclick', this.handleShadowDoubleClick);
    this.shadowRoot!.addEventListener('keydown', this.handleShadowKeyDown);
  }

  /**
   * The keyboard, as the tabs pattern of WAI-ARIA has it: Left and Right
   * move the focus to the tab beside, Home and End to the first and the
   * last, scrolling the strip to show it; Enter or Space shows its sheet,
   * and F2 on the active sheet's tab renames it, as a double-click does.
   * Enter or Space on the + button adds a sheet. The keys taken are the
   * strip's, not the page's.
   */
  private readonly handleShadowKeyDown = (event: Event): void => {
    const e = event as KeyboardEvent;
    const target = e.target;
    if (!(target instanceof HTMLElement) || composing(e)) {
      return;
    }
    if (target.matches('[data-new-tab]')) {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        e.stopPropagation();
        this.dispatchEvent(new CustomEvent('sheet-tab-add', { bubbles: true, composed: true }));
      }
      return;
    }
    // The rename input keeps its keys to itself.
    if (!target.classList.contains('sheet-tab')) {
      return;
    }
    const index = Number(target.dataset.index);
    const { sheetNames, activeIndex, readOnly } = this.renderState;
    let next: number;
    switch (e.key) {
      case 'ArrowLeft':
        next = Math.max(1, index - 1);
        break;
      case 'ArrowRight':
        next = Math.min(sheetNames.length, index + 1);
        break;
      case 'Home':
        next = 1;
        break;
      case 'End':
        next = sheetNames.length;
        break;
      case 'Enter':
      case ' ':
        e.preventDefault();
        e.stopPropagation();
        this.dispatchEvent(new CustomEvent('sheet-tab-click', { detail: { index }, bubbles: true, composed: true }));
        return;
      case 'F2':
        e.preventDefault();
        e.stopPropagation();
        if (!readOnly && index === activeIndex) {
          this.dispatchEvent(
            new CustomEvent('sheet-tab-rename-start', { detail: { index }, bubbles: true, composed: true })
          );
        }
        return;
      default:
        return;
    }
    e.preventDefault();
    e.stopPropagation();
    this.focusedTab = next;
    this.focusPending = true;
    this.reveal(next);
    this.requestUpdate();
  };

  /**
   * Double-clicking a sheet tab renames it.
   *
   * Double-clicking one that is not the active sheet only makes it
   * active - the first click already asked for that, and Excel does not
   * put two gestures on one action. A read-only workbook renames
   * nothing.
   */
  private readonly handleShadowDoubleClick = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element) || this.renderState.readOnly) {
      return;
    }
    const tab = target.closest<HTMLElement>('.sheet-tab');
    if (!tab) {
      return;
    }
    const index = Number(tab.dataset.index);
    if (!Number.isFinite(index) || index < 1 || index !== this.renderState.activeIndex) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('sheet-tab-rename-start', {
        detail: { index },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * The rename inputs whose rename is over: Escape threw the name away, or
   * it was reported; see {@link commitRename}. A new rename brings a new
   * input.
   */
  private readonly endedRenames = new WeakSet<HTMLInputElement>();

  /**
   * The keys that end a rename.
   *
   * Enter and Tab keep what was typed, Escape throws it away. Every
   * other key belongs to the input, and so does the event: without
   * stopping it here, typing a name would also be typing into the
   * sheet underneath.
   */
  private readonly handleRenameKeyDown = (e: KeyboardEvent): void => {
    e.stopPropagation();
    if (composing(e)) {
      // The input method's: confirming a conversion keeps the name open.
      return;
    }
    if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault();
      this.commitRename(e.target as HTMLInputElement);
      return;
    }
    if (e.key === 'Escape') {
      e.preventDefault();
      this.endedRenames.add(e.target as HTMLInputElement);
      this.dispatchEvent(new CustomEvent('sheet-tab-rename-cancel', { bubbles: true, composed: true }));
    }
  };

  /** Clicking away keeps the name, as leaving a cell editor does. */
  private readonly handleRenameBlur = (e: FocusEvent): void => {
    this.commitRename(e.target as HTMLInputElement);
  };

  /**
   * Report a finished rename, once.
   *
   * Ending a rename takes the input out of the DOM, and an input that
   * had the focus blurs on its way out - so both Escape and Enter are
   * followed by a blur that would report the rename all over again. The
   * strip may not have been told yet that no tab is being renamed: the
   * sheet draws it with the rest of the sheet. So the input itself is
   * marked as done with.
   */
  private commitRename(input: HTMLInputElement | null): void {
    const index = this.renderState.renamingIndex ?? 0;
    if (input === null || index < 1 || this.endedRenames.has(input)) {
      return;
    }
    this.endedRenames.add(input);
    this.dispatchEvent(
      new CustomEvent('sheet-tab-rename', {
        detail: { index, name: input.value },
        bubbles: true,
        composed: true
      })
    );
  }

  /**
   * Put the caret in the rename input as soon as it appears, and see
   * whether the tabs still fit.
   */
  updated(): void {
    const input = this.shadowRoot?.querySelector<HTMLInputElement>('.sheet-tab-rename');
    if (input !== null && input !== undefined && this.shadowRoot!.activeElement !== input) {
      input.focus();
      input.select();
    }
    this.measureLastFirstTab();
    this.revealActiveTab();
    if (this.focusPending) {
      this.focusPending = false;
      this.shadowRoot?.querySelector<HTMLElement>(`.sheet-tab[data-index="${this.focusedTab}"]`)?.focus();
    }
  }

  private readonly handleShadowClick = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }

    // A disabled scroll button carries the `hidden` class; it is still
    // in the DOM so a stylesheet can style it, but it does nothing.
    // The end button carries where it goes: the strip is the one that
    // knows how many tabs fit.
    const scroll = target.closest<HTMLElement>('[data-scroll]');
    if (scroll) {
      if (!scroll.classList.contains('hidden')) {
        this.dispatchEvent(
          new CustomEvent('sheet-tabs-scroll', {
            detail: { direction: scroll.dataset.scroll, lastFirstTab: this.lastFirstTab },
            bubbles: true,
            composed: true
          })
        );
      }
      return;
    }

    if (target.closest('[data-new-tab]')) {
      this.dispatchEvent(new CustomEvent('sheet-tab-add', { bubbles: true, composed: true }));
      return;
    }

    const tab = target.closest<HTMLElement>('.sheet-tab');
    if (!tab) {
      return;
    }
    const index = Number(tab.dataset.index);
    if (!Number.isFinite(index) || index < 1) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('sheet-tab-click', {
        detail: { index },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * The strip's buttons, in their order.
   *
   * Not named `renderOptions`: LitElement assigns an instance property
   * of that name in its own constructor, which shadows a prototype
   * method and turns the call into "not a function" at render time.
   *
   * Class names and parts are both contract: the classes are what the
   * default stylesheet and the TestBench elements look for, and the
   * parts are documented for application CSS. A button that cannot do
   * anything right now swaps its `scroll-tabs-button` part for
   * `scroll-tabs-button-disabled`, so a stylesheet can grey it out.
   */
  private renderStripButtons(canScrollBack: boolean, canScrollForward: boolean): TemplateResult {
    const scrollPart = (enabled: boolean, specific: string): string =>
      `${enabled ? 'scroll-tabs-button' : 'scroll-tabs-button-disabled'} ${specific}`;
    const scrollClass = (cls: string, enabled: boolean): string => (enabled ? cls : `${cls} hidden`);
    // Buttons to assistive technology, named. The keyboard scrolls the strip
    // by moving along the tabs, so these stay out of the Tab order.
    return html`
      <div class="sheet-tabsheet-options">
        <div
          class=${scrollClass('scroll-tabs-beginning', canScrollBack)}
          part=${scrollPart(canScrollBack, 'scroll-tabs-to-start-button')}
          role="button"
          aria-label="Scroll to the first sheets"
          aria-disabled=${String(!canScrollBack)}
          data-scroll="start"
        ></div>
        <div
          class=${scrollClass('scroll-tabs-left', canScrollBack)}
          part=${scrollPart(canScrollBack, 'scroll-tabs-backward-button')}
          role="button"
          aria-label="Scroll the sheets left"
          aria-disabled=${String(!canScrollBack)}
          data-scroll="backward"
        ></div>
        <div
          class=${scrollClass('scroll-tabs-right', canScrollForward)}
          part=${scrollPart(canScrollForward, 'scroll-tabs-forward-button')}
          role="button"
          aria-label="Scroll the sheets right"
          aria-disabled=${String(!canScrollForward)}
          data-scroll="forward"
        ></div>
        <div
          class=${scrollClass('scroll-tabs-end', canScrollForward)}
          part=${scrollPart(canScrollForward, 'scroll-tabs-to-end-button')}
          role="button"
          aria-label="Scroll to the last sheets"
          aria-disabled=${String(!canScrollForward)}
          data-scroll="end"
        ></div>
        ${
          // A protected workbook takes no new sheet, and shows no button
          // for one.
          this.renderState.readOnly
            ? nothing
            : html`<div
                class="add-new-tab"
                part="new-tab-button"
                role="button"
                tabindex="0"
                aria-label="Add a sheet"
                data-new-tab
              ></div>`
        }
      </div>
    `;
  }

  render(): TemplateResult {
    const { sheetNames, activeIndex, firstVisibleTab = 0, renamingIndex = 0, infoLabel = null } = this.renderState;
    if (sheetNames.length === 0) {
      return html``;
    }
    const canScrollBack = firstVisibleTab > 0;
    const canScrollForward = firstVisibleTab < this.lastFirstTab;
    if (activeIndex !== this.drawnActiveIndex) {
      this.drawnActiveIndex = activeIndex;
      this.focusedTab = 0;
    }
    // One tab in the Tab order: the one the keyboard is on, the active
    // sheet's to begin with, or the first one shown when that is scrolled
    // away, so the strip can always be reached.
    const keyboardTab = this.focusedTab || activeIndex;
    const tabbable =
      keyboardTab > firstVisibleTab && keyboardTab <= sheetNames.length ? keyboardTab : firstVisibleTab + 1;
    const tabs = sheetNames.map((name, i) => {
      const oneBasedIndex = i + 1;
      const isActive = oneBasedIndex === activeIndex;
      const className = isActive ? 'sheet-tab active' : 'sheet-tab';
      // `part="tab"` is the documented public
      // styling part for sheet tabs; the active tab also
      // carries `tab-selected` so
      // `vaadin-spreadsheet::part(tab-selected)` works.
      const part = isActive ? 'tab tab-selected' : 'tab';
      // A tab is only as wide as the strip allows, so a long name is
      // cut off with an ellipsis. The title is how the user reads the
      // rest of it, and how a test names a tab it cannot see whole.
      // The tab being renamed shows an input in place of its name. It
      // keeps its own class and part, so a stylesheet that colours the
      // active tab still colours this one.
      const content =
        oneBasedIndex === renamingIndex
          ? html`<input
              class="sheet-tab-rename"
              part="tab-rename"
              value=${name}
              @keydown=${this.handleRenameKeyDown}
              @blur=${this.handleRenameBlur}
            />`
          : name;
      return html`
        <div
          class=${className}
          part=${part}
          role="tab"
          aria-selected=${String(isActive)}
          tabindex=${oneBasedIndex === tabbable ? '0' : '-1'}
          title=${name}
          data-index=${oneBasedIndex}
        >
          ${content}
        </div>
      `;
    });
    // The tabs scroll by whole tabs; hiding the ones before
    // firstVisibleTab is enough and needs no width arithmetic.
    const hasLabel = infoLabel !== null && infoLabel !== undefined;
    return html`
      ${this.renderStripButtons(canScrollBack, canScrollForward)}
      <div
        class=${hasLabel ? 'sheet-tabsheet-container beside-infolabel' : 'sheet-tabsheet-container'}
        role="tablist"
        aria-label="Sheets"
      >
        ${tabs.slice(firstVisibleTab)}
      </div>
      ${hasLabel ? html`<div class="sheet-tabsheet-infolabel">${infoLabel}</div>` : nothing}
    `;
  }
}

customElements.define('vaadin-spreadsheet-internal-sheet-tabs', SpreadsheetSheetTabs);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-sheet-tabs': SpreadsheetSheetTabs;
  }
}
