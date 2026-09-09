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
}

/** Helper: build the empty state for tests + initial render. */
export function emptySheetTabsRenderState(): SheetTabsRenderState {
  return {
    sheetNames: [],
    activeIndex: 0
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
  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
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
      border-bottom: 2px solid var(--vaadin-spreadsheet-tabs-active-bg, #1976d2);
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

  /**
   * Wire shadow-root-level click delegation on first render —
   * same pattern as the cell-grid + headers (slices 146+148).
   * Each click that lands on a {@code .sheet-tab} dispatches
   * {@code sheet-tab-click} with the clicked index.
   */
  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
  }

  private readonly handleShadowClick = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
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

  render(): TemplateResult {
    const { sheetNames, activeIndex } = this.renderState;
    if (sheetNames.length === 0) {
      return html``;
    }
    const tabs = sheetNames.map((name, i) => {
      const oneBasedIndex = i + 1;
      const isActive = oneBasedIndex === activeIndex;
      const className = isActive ? 'sheet-tab active' : 'sheet-tab';
      // Slice 173: `part="tab"` is the documented public
      // styling part for sheet tabs; the active tab also
      // carries `tab-selected` so
      // `vaadin-spreadsheet::part(tab-selected)` works.
      const part = isActive ? 'tab tab-selected' : 'tab';
      return html` <div class=${className} part=${part} data-index=${oneBasedIndex}> ${name} </div> `;
    });
    return html`${tabs}`;
  }
}

customElements.define('vaadin-spreadsheet-internal-sheet-tabs', SpreadsheetSheetTabs);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-sheet-tabs': SpreadsheetSheetTabs;
  }
}
