/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, nothing, type TemplateResult } from 'lit';

/**
 * Input state for the {@link SpreadsheetSheetTabs} element
 * (slice 154). The connector builds + pushes this whenever
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
   * sheets than fit, the strip scrolls by whole tabs and the scroll
   * buttons move this. Mirrors the GWT client's `tabScrollIndex`, and
   * the server is told about changes so it can restore the position.
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
   * label, empty, as the GWT client did.
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
 * Sheet-tab strip element (slice 154). Renders one tab per sheet
 * name from {@link SheetTabsRenderState.sheetNames}, highlights
 * the tab at {@link SheetTabsRenderState.activeIndex}. Clicking
 * a tab dispatches a {@code sheet-tab-click} custom event with
 * the clicked index — the connector translates this into a
 * {@code sheetSelected} {@code spreadsheet-event} for the server.
 *
 * Renders empty when {@code sheetNames} is empty.
 *
 * Same no-decorator pattern as the cell-grid + column-header
 * sub-elements (slice 132+).
 */
export class SpreadsheetSheetTabs extends LitElement {
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
  static readonly EXPORTED_PARTS =
    'tab,tab-selected,tab-rename,scroll-tabs-button,scroll-tabs-button-disabled,' +
    'scroll-tabs-to-start-button,scroll-tabs-backward-button,' +
    'scroll-tabs-forward-button,scroll-tabs-to-end-button,new-tab-button';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    /* The buttons sit before the tabs, as the GWT strip had them. */
    /* The strip buttons, side by side in the GWT client's 130px. */
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
       button's text colour through a mask, as the GWT client drew them -
       a bare filled box says nothing about what it does, and five of them
       in a row read as decoration. */
    .sheet-tabsheet-options > div {
      width: 26px;
      height: 26px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--vaadin-spreadsheet-tab-button-bg, var(--_vaadin-spreadsheet-tab-button-bg, #464646));
    }

    .sheet-tabsheet-options > div::before {
      content: '';
      width: 20px;
      height: 20px;
      background: currentColor;
      -webkit-mask-image: var(--_tab-button-icon);
      mask-image: var(--_tab-button-icon);
    }

    /* The GWT client's blue, its own: the active tab's colour is the app's
       to change, and the buttons did not follow it. */
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

    /* The GWT client's strip: 28px under a one-pixel border, with the
       tabs hanging from that border. Tabs past its end are cut off; the
       buttons scroll to them. */
    :host {
      position: relative;
      display: flex;
      flex-direction: row;
      align-items: flex-start;
      box-sizing: border-box;
      height: 29px;
      background: var(--vaadin-spreadsheet-tabs-bg, var(--_vaadin-spreadsheet-tabs-bg, #fafafa));
      border-top: 1px solid var(--vaadin-spreadsheet-tabs-border, var(--_vaadin-spreadsheet-tabs-border, #c7c7c7));
      font-family: var(--vaadin-spreadsheet-tabs-font, var(--_vaadin-spreadsheet-tabs-font, Helvetica));
      font-size: var(--vaadin-spreadsheet-tabs-font-size, var(--_vaadin-spreadsheet-tabs-font-size, 14px));
      overflow: hidden;
    }

    /* A tab as the GWT client drew one: a folder tab hanging from the
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
      border: 1px solid var(--vaadin-spreadsheet-tabs-border, var(--_vaadin-spreadsheet-tabs-border, #c7c7c7));
      border-top: none;
      border-radius: 0 0 5px 5px;
      cursor: pointer;
      user-select: none;
      background: var(--vaadin-spreadsheet-tabs-bg, var(--_vaadin-spreadsheet-tabs-bg, #fafafa));
      color: var(--vaadin-spreadsheet-tabs-fg, var(--_vaadin-spreadsheet-tabs-fg, #6a6a6a));
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
      background: var(--vaadin-spreadsheet-tabs-active-bg, var(--_vaadin-spreadsheet-tabs-active-bg, #fff));
      color: var(--vaadin-spreadsheet-tabs-active-fg, var(--_vaadin-spreadsheet-tabs-active-fg, #2584e2));
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
      border-left: 1px solid
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      background: var(--vaadin-spreadsheet-tabs-bg, var(--_vaadin-spreadsheet-tabs-bg, #fafafa));
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

    /* SpreadsheetTheme.LUMO, as the GWT client drew it: tinted folder tabs
       in regular weight, the active one lifted off the strip, and buttons
       that light up in the primary tint. */
    :host([theme~='lumo']) {
      height: calc(var(--lumo-font-size-s) + 15px);
    }

    :host([theme~='lumo']) .sheet-tabsheet-options > div {
      cursor: var(--lumo-clickable-cursor);
    }

    :host([theme~='lumo']) .sheet-tabsheet-options > div:hover {
      color: var(--vaadin-spreadsheet-tab-button-bg, var(--_vaadin-spreadsheet-tab-button-bg, #464646));
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

    /* The GWT client's Lumo tabs did not change under the pointer. */
    :host([theme~='lumo']) .sheet-tab:hover:not(.active) {
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    }

    :host([theme~='lumo']) .sheet-tab.active {
      background-image: none;
      box-shadow: var(--lumo-box-shadow-xs);
    }

    :host([theme~='lumo']) .sheet-tab-rename {
      font-size: var(--lumo-font-size-s);
      color: var(--lumo-primary-text-color);
    }

    :host([theme~='lumo']) .sheet-tabsheet-infolabel {
      background: var(--lumo-contrast-5pct);
      font-size: var(--lumo-font-size-s);
      padding: 0 var(--lumo-space-xs);
    }
  `;

  declare renderState: SheetTabsRenderState;

  /**
   * The leftmost tab that still shows the last one, as the GWT client
   * worked it out: from the last tab back, as many as fit beside the
   * buttons and the status label. Scrolling forward stops there and the
   * end button goes straight to it, so the strip never scrolls into empty
   * space - and with every tab in view, neither button does anything.
   * Measured after each render: only the browser knows how wide the tabs
   * came out.
   */
  private lastFirstTab = 0;

  private readonly resizeObserver = new ResizeObserver(() => this.measureLastFirstTab());

  constructor() {
    super();
    this.renderState = emptySheetTabsRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', SpreadsheetSheetTabs.EXPORTED_PARTS);
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
    if (options && tabs.length > 0) {
      const label = root!.querySelector<HTMLElement>('.sheet-tabsheet-infolabel');
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
   * Wire shadow-root-level click delegation on first render —
   * same pattern as the cell-grid + headers (slices 146+148).
   * Each click that lands on a {@code .sheet-tab} dispatches
   * {@code sheet-tab-click} with the clicked index.
   */
  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
    this.shadowRoot!.addEventListener('dblclick', this.handleShadowDoubleClick);
  }

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
   * The keys that end a rename.
   *
   * Enter and Tab keep what was typed, Escape throws it away. Every
   * other key belongs to the input, and so does the event: without
   * stopping it here, typing a name would also be typing into the
   * sheet underneath.
   */
  private readonly handleRenameKeyDown = (e: KeyboardEvent): void => {
    e.stopPropagation();
    if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault();
      this.commitRename(e.target as HTMLInputElement);
      return;
    }
    if (e.key === 'Escape') {
      e.preventDefault();
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
   * followed by a blur that would report the rename all over again. By
   * then the strip has been told no tab is being renamed, which is the
   * signal that the rename is already over.
   */
  private commitRename(input: HTMLInputElement | null): void {
    const index = this.renderState.renamingIndex ?? 0;
    if (input === null || index < 1) {
      return;
    }
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
   * The strip's buttons, in the order the GWT client had them.
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
    return html`
      <div class="sheet-tabsheet-options">
        <div
          class=${scrollClass('scroll-tabs-beginning', canScrollBack)}
          part=${scrollPart(canScrollBack, 'scroll-tabs-to-start-button')}
          data-scroll="start"
        ></div>
        <div
          class=${scrollClass('scroll-tabs-left', canScrollBack)}
          part=${scrollPart(canScrollBack, 'scroll-tabs-backward-button')}
          data-scroll="backward"
        ></div>
        <div
          class=${scrollClass('scroll-tabs-right', canScrollForward)}
          part=${scrollPart(canScrollForward, 'scroll-tabs-forward-button')}
          data-scroll="forward"
        ></div>
        <div
          class=${scrollClass('scroll-tabs-end', canScrollForward)}
          part=${scrollPart(canScrollForward, 'scroll-tabs-to-end-button')}
          data-scroll="end"
        ></div>
        <div class="add-new-tab" part="new-tab-button" data-new-tab></div>
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
    const tabs = sheetNames.map((name, i) => {
      const oneBasedIndex = i + 1;
      const isActive = oneBasedIndex === activeIndex;
      const className = isActive ? 'sheet-tab active' : 'sheet-tab';
      // Slice 173: `part="tab"` is the documented public
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
      return html` <div class=${className} part=${part} title=${name} data-index=${oneBasedIndex}> ${content} </div> `;
    });
    // The tabs scroll by whole tabs; hiding the ones before
    // firstVisibleTab is enough and needs no width arithmetic.
    const hasLabel = infoLabel !== null && infoLabel !== undefined;
    return html`
      ${this.renderStripButtons(canScrollBack, canScrollForward)}
      <div class=${hasLabel ? 'sheet-tabsheet-container beside-infolabel' : 'sheet-tabsheet-container'}>
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
