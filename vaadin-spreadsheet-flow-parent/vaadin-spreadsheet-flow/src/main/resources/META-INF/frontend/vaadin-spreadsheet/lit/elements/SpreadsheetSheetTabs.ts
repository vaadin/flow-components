/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, type TemplateResult } from 'lit';

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
}

/** Helper: build the empty state for tests + initial render. */
export function emptySheetTabsRenderState(): SheetTabsRenderState {
  return {
    sheetNames: [],
    activeIndex: 0,
    firstVisibleTab: 0,
    readOnly: false,
    renamingIndex: 0
  };
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
    .sheet-tabsheet-options {
      display: inline-flex;
      align-items: center;
      gap: 2px;
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
      align-items: center;
      overflow: hidden;
    }

    .sheet-tabsheet-options > div {
      width: 14px;
      height: 14px;
      cursor: pointer;
      background: var(--vaadin-spreadsheet-tab-button-bg, #808080);
    }

    /* Kept in the DOM so a stylesheet can reach it, but inert. */
    .sheet-tabsheet-options > div.hidden {
      opacity: 0.3;
      cursor: default;
    }

    :host {
      display: flex;
      flex-direction: row;
      align-items: stretch;
      box-sizing: border-box;
      background: var(--vaadin-spreadsheet-tabs-bg, #f4f4f4);
      border-top: 1px solid var(--vaadin-spreadsheet-tabs-border, #c0c0c0);
      font-family: var(--vaadin-spreadsheet-tabs-font, inherit);
      font-size: var(--vaadin-spreadsheet-tabs-font-size, inherit);
      overflow-x: auto;
      overflow-y: hidden;
      min-height: 24px;
    }

    .sheet-tab {
      padding: 4px 12px;
      border-right: 1px solid var(--vaadin-spreadsheet-tabs-border, #c0c0c0);
      cursor: pointer;
      user-select: none;
      background: var(--vaadin-spreadsheet-tabs-bg, #f4f4f4);
      color: var(--vaadin-spreadsheet-tabs-fg, #333);
      white-space: nowrap;
      display: flex;
      align-items: center;
    }

    .sheet-tab.active {
      background: var(--vaadin-spreadsheet-tabs-active-bg, #fff);
      color: var(--vaadin-spreadsheet-tabs-active-fg, #1976d2);
      border-bottom: 2px solid var(--vaadin-spreadsheet-tabs-active-fg, #1976d2);
      font-weight: 500;
    }

    .sheet-tab:hover:not(.active) {
      background: var(--vaadin-spreadsheet-tabs-hover-bg, #e8e8e8);
    }
  `;

  declare renderState: SheetTabsRenderState;

  constructor() {
    super();
    this.renderState = emptySheetTabsRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', SpreadsheetSheetTabs.EXPORTED_PARTS);
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

  /** Put the caret in the rename input as soon as it appears. */
  updated(): void {
    const input = this.shadowRoot?.querySelector<HTMLInputElement>('.sheet-tab-rename');
    if (input !== null && input !== undefined && this.shadowRoot!.activeElement !== input) {
      input.focus();
      input.select();
    }
  }

  private readonly handleShadowClick = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }

    // A disabled scroll button carries the `hidden` class; it is still
    // in the DOM so a stylesheet can style it, but it does nothing.
    const scroll = target.closest<HTMLElement>('[data-scroll]');
    if (scroll) {
      if (!scroll.classList.contains('hidden')) {
        this.dispatchEvent(
          new CustomEvent('sheet-tabs-scroll', {
            detail: { direction: scroll.dataset.scroll },
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
    const { sheetNames, activeIndex, firstVisibleTab = 0, renamingIndex = 0 } = this.renderState;
    if (sheetNames.length === 0) {
      return html``;
    }
    const canScrollBack = firstVisibleTab > 0;
    const canScrollForward = firstVisibleTab < sheetNames.length - 1;
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
    return html`
      ${this.renderStripButtons(canScrollBack, canScrollForward)}
      <div class="sheet-tabsheet-container"> ${tabs.slice(firstVisibleTab)} </div>
    `;
  }
}

customElements.define('vaadin-spreadsheet-internal-sheet-tabs', SpreadsheetSheetTabs);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-sheet-tabs': SpreadsheetSheetTabs;
  }
}
