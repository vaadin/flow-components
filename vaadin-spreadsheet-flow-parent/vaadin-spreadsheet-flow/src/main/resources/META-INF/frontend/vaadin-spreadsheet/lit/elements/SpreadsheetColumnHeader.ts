/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, type TemplateResult } from 'lit';

import { columnIndexToLetters } from '../algorithms/CellRef.js';
import type { ViewportBounds } from '../algorithms/ViewportMath.js';

/**
 * Input state for the {@link SpreadsheetColumnHeader} element.
 * The Spreadsheet connector builds a fresh instance alongside the
 * cell-grid's {@code CellGridRenderState} and pushes it to the
 * header on every render cycle (slice 143).
 *
 * Coordinates use the same conventions as the cell-grid: 1-based
 * column indices, pixel offsets from the (virtual) grid origin.
 */
export interface ColumnHeaderRenderState {
  /** Currently visible columns — same shape as the cell-grid's viewport. */
  viewport: ViewportBounds;
  /**
   * Pixel x-offsets for visible columns, indexed by
   * {@code col - viewport.firstColumnIndex} — copied through
   * from the cell-grid render state.
   */
  colX: ReadonlyArray<number>;
  /** Pixel width of each visible column, same indexing as {@code colX}. */
  colWidths: ReadonlyArray<number>;
  /**
   * Height of the header strip in pixels. {@code 0} disables the
   * header (the connector still pushes a state, but render() emits
   * an empty fragment). Excel's default header is around 18-22 px;
   * the connector picks the value via {@code setHeaderRowHeight}.
   */
  headerHeight: number;
  /**
   * Total grid pixel width — used to size the header's intrinsic
   * width so it contributes to the scroll container's scroll
   * extent the same way the cell-grid does.
   */
  totalGridWidthPx: number;
  /**
   * Column groupings (outline) with pre-computed pixel positions
   * (slice 167). Pushed by the connector after computing the
   * absolute startPx/endPx from {@code colWidths}. The header
   * renders one row per level above (or below, if inversed) the
   * letter cells; each grouping shows a clickable +/- button at
   * one end of its range.
   */
  /**
   * Number of frozen columns at the left of the sheet. Those columns
   * stay on screen whatever the scroll position, so their headers have
   * to stay with them — a header that scrolls away from the cells it
   * names is worse than none.
   */
  horizontalSplitPosition?: number;
  /** Pixel x-offsets of the frozen columns, indexed by {@code col - 1}. */
  frozenColX?: ReadonlyArray<number>;
  /** Pixel widths of the frozen columns, same indexing as frozenColX. */
  frozenColWidths?: ReadonlyArray<number>;
  /**
   * Current horizontal scroll offset. The frozen strip is translated by
   * it to cancel the scroll out, the way the cell-grid pins its own
   * frozen quadrants.
   */
  scrollLeft?: number;

  colGroupings: ReadonlyArray<PositionedGrouping>;
  /**
   * Number of grouping levels (rows in the gutter). {@code 0}
   * means no grouping data — the gutter renders nothing and
   * doesn't add to the element's height.
   */
  colGroupingMax: number;
  /**
   * When {@code true}, the grouping gutter renders BELOW the
   * letter cells (and the +/- button is at the END of each
   * grouping range — the column just after the data). When
   * {@code false} (default), the gutter is ABOVE and the
   * button is at the START. Matches Excel's "summary rows
   * above/below" toggle.
   */
  colGroupingInversed: boolean;
  /**
   * Columns intersecting the active selection. Each cell whose
   * column is in this set gets the {@code header-selected} part
   * in addition to {@code column-header}, mirroring the
   * documented public styling API. Slice 173.
   */
  selectedColumns: ReadonlySet<number>;
}

/**
 * A grouping outline entry with pre-computed pixel positions.
 * Slice 167. The connector pre-computes {@code startPx} +
 * {@code endPx} + {@code buttonPx} from {@code colWidths} /
 * {@code rowHeights} so the header doesn't need access to the
 * full width array.
 */
export interface PositionedGrouping {
  /** Unique-within-the-grouping-list identifier (matches server). */
  uniqueIndex: number;
  /** Grouping level (1-based; level-1 is the outermost). */
  level: number;
  /** Pixel start of the range (start of column/row at startIndex). */
  startPx: number;
  /** Pixel end of the range (start of column/row AFTER endIndex). */
  endPx: number;
  /**
   * Pixel position of the +/- button. {@code startPx - btnSize}
   * when {@code !inversed} (button BEFORE the range), or
   * {@code endPx} when {@code inversed} (button AFTER).
   */
  buttonPx: number;
  /** Current collapse state — drives the +/- glyph. */
  collapsed: boolean;
}

/** Helper: build an empty header render state for tests + initial render. */
export function emptyColumnHeaderRenderState(): ColumnHeaderRenderState {
  return {
    viewport: {
      firstColumnIndex: 1,
      firstColumnPosition: 0,
      lastColumnIndex: 0,
      lastColumnPosition: 0,
      firstRowIndex: 1,
      firstRowPosition: 0,
      lastRowIndex: 0,
      lastRowPosition: 0
    },
    colX: [],
    colWidths: [],
    headerHeight: 0,
    totalGridWidthPx: 0,
    horizontalSplitPosition: 0,
    frozenColX: [],
    frozenColWidths: [],
    scrollLeft: 0,
    colGroupings: [],
    colGroupingMax: 0,
    colGroupingInversed: false,
    selectedColumns: new Set()
  };
}

/** Slice 167: pixel height per grouping level row in the gutter. */
export const GROUPING_LEVEL_HEIGHT_PX = 16;
/** Slice 167: pixel width of a +/- collapse button. */
export const GROUPING_BUTTON_SIZE_PX = 14;

/**
 * Internal column-header Lit element. Renders one
 * {@code .col-header-cell} per visible column with the column's
 * letter ({@code A, B, ..., Z, AA, AB, ...}) inside.
 *
 * The element is sticky-positioned (via host CSS) so it stays
 * pinned at the top of the scroll container as the user scrolls
 * down. Horizontal scroll moves the header naturally with the
 * cells, keeping column letters aligned with their columns.
 *
 * The connector creates an instance, appends it to the scroll
 * container ABOVE the cell-grid, and pushes a fresh render state
 * on every {@code applyRenderState} call.
 *
 * Properties are declared via Lit's {@code static properties} API
 * (no decorators) for the same esbuild-compatibility reason as
 * {@link SpreadsheetCellGrid} (slice 132).
 */
export class SpreadsheetColumnHeader extends LitElement {
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
  static readonly EXPORTED_PARTS = 'column-header,header-selected';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    :host {
      display: block;
      position: sticky;
      top: 0;
      z-index: 5;
      box-sizing: border-box;
      background: var(--vaadin-spreadsheet-header-bg, #f4f4f4);
      border-bottom: 1px solid var(--vaadin-spreadsheet-header-border, #c0c0c0);
      font-family: var(--vaadin-spreadsheet-header-font, inherit);
      font-size: var(--vaadin-spreadsheet-header-font-size, inherit);
    }

    /* Frozen columns are drawn in their own strip, translated to
       cancel the scroll out. It sits above the scrolling headers so a
       column sliding underneath does not show through. */
    .frozen-header-strip {
      position: absolute;
      inset: 0;
      z-index: 2;
      pointer-events: none;
    }

    .frozen-header-strip > * {
      pointer-events: auto;
    }

    .col-header-cell {
      position: absolute;
      box-sizing: border-box;
      overflow: hidden;
      text-align: center;
      border-right: 1px solid var(--vaadin-spreadsheet-header-border, #c0c0c0);
    }

    /* The active cell's headers are tinted so the cursor's row and
       column stay findable on a wide sheet. This is the tint the GWT
       client painted; the cell also carries the documented
       ::part(header-selected) for consumers who want another one. */
    .col-header-cell[part~='header-selected'] {
      background: var(--vaadin-spreadsheet-header-selected-bg, #e6edf4);
    }

    /* Resize handle on the right edge of each column header
           (slice 164). 4px wide, full-height, cursor:col-resize.
           Mousedown begins a column-resize drag. */
    .col-resize-handle {
      position: absolute;
      top: 0;
      width: 5px;
      cursor: col-resize;
      z-index: 1;
    }

    /* Slice 167 — grouping outline gutter. Sits above (or below
           if inversed) the column-letter row. Each grouping renders
           one .col-grouping-line spanning startPx..endPx + one
           .col-grouping-toggle button at the start (or end). */
    .col-grouping-line {
      position: absolute;
      height: 1px;
      background: var(--vaadin-spreadsheet-header-border, #c0c0c0);
      pointer-events: none;
    }

    .col-grouping-toggle {
      position: absolute;
      width: 14px;
      height: 14px;
      box-sizing: border-box;
      padding: 0;
      margin: 0;
      border: 1px solid var(--vaadin-spreadsheet-header-border, #888);
      background: var(--vaadin-spreadsheet-header-bg, #fff);
      color: inherit;
      font: inherit;
      line-height: 1;
      font-size: 11px;
      text-align: center;
      cursor: pointer;
      z-index: 2;
    }

    .col-grouping-toggle:hover {
      background: #e8e8e8;
    }
  `;

  declare renderState: ColumnHeaderRenderState;

  constructor() {
    super();
    this.renderState = emptyColumnHeaderRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', SpreadsheetColumnHeader.EXPORTED_PARTS);
  }

  /**
   * Wire shadow-root click delegation on first render — same
   * pattern as {@link SpreadsheetCellGrid}. Each click that
   * lands on a {@code .col-header-cell} dispatches a
   * {@code column-header-click} custom event with the column
   * coordinate + modifier-key state. Slice 148.
   */
  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
    // Slice 164: column-resize drag. mousedown on a
    // `.col-resize-handle` dispatches `column-resize-start`
    // with the col + the original clientX so the connector
    // can track the drag.
    this.shadowRoot!.addEventListener('mousedown', this.handleShadowMouseDown as EventListener);
    // Slice 166: right-click on a column header dispatches
    // `column-header-contextmenu` so the connector can
    // forward it as `columnHeaderContextMenuOpen` to the
    // server. preventDefault suppresses the browser's native
    // menu.
    this.shadowRoot!.addEventListener('contextmenu', this.handleShadowContextMenu);
    // Double-clicking a resize handle autofits the column to its content,
    // the way Excel does; the drag on the same handle resizes it by hand.
    this.shadowRoot!.addEventListener('dblclick', this.handleShadowDoubleClick as EventListener);
  }

  private readonly handleShadowContextMenu = (event: Event): void => {
    const e = event as MouseEvent;
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const cell = target.closest<HTMLElement>('.col-header-cell');
    if (!cell) {
      return;
    }
    const col = Number(cell.dataset.col);
    if (!Number.isFinite(col) || col < 1) {
      return;
    }
    e.preventDefault();
    this.dispatchEvent(
      new CustomEvent('column-header-contextmenu', {
        detail: { col, clientX: e.clientX, clientY: e.clientY },
        bubbles: true,
        composed: true
      })
    );
  };

  private readonly handleShadowDoubleClick = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const handle = target.closest<HTMLElement>('.col-resize-handle');
    if (!handle) {
      return;
    }
    const col = Number(handle.dataset.col);
    if (!Number.isFinite(col) || col < 1) {
      return;
    }
    e.preventDefault();
    this.dispatchEvent(
      new CustomEvent('column-autofit', {
        detail: { col },
        bubbles: true,
        composed: true
      })
    );
  };

  private readonly handleShadowMouseDown = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const handle = target.closest<HTMLElement>('.col-resize-handle');
    if (!handle) {
      return;
    }
    const col = Number(handle.dataset.col);
    if (!Number.isFinite(col) || col < 1) {
      return;
    }
    e.preventDefault();
    const ev = e as MouseEvent;
    this.dispatchEvent(
      new CustomEvent('column-resize-start', {
        detail: { col, clientX: ev.clientX },
        bubbles: true,
        composed: true
      })
    );
  };

  private readonly handleShadowClick = (e: Event): void => {
    const ev = e as MouseEvent;
    const target = ev.target;
    if (!(target instanceof Element)) {
      return;
    }
    // Slice 167: grouping toggle click — dispatch
    // `column-grouping-toggle` so the connector can forward
    // `groupingCollapsed` to the server. Stop here so the
    // click doesn't also bubble as a column-header click.
    const toggle = target.closest<HTMLElement>('.col-grouping-toggle');
    if (toggle) {
      const uniqueIndex = Number(toggle.dataset.uniqueIndex);
      const collapsed = toggle.dataset.collapsed === 'true';
      if (Number.isFinite(uniqueIndex)) {
        this.dispatchEvent(
          new CustomEvent('column-grouping-toggle', {
            detail: { uniqueIndex, collapsed: !collapsed },
            bubbles: true,
            composed: true
          })
        );
      }
      ev.stopPropagation();
      return;
    }
    const cell = target.closest<HTMLElement>('.col-header-cell');
    if (!cell) {
      return;
    }
    const col = Number(cell.dataset.col);
    if (!Number.isFinite(col) || col < 1) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('column-header-click', {
        detail: {
          col,
          shiftKey: ev.shiftKey,
          ctrlKey: ev.ctrlKey,
          metaKey: ev.metaKey,
          altKey: ev.altKey
        },
        bubbles: true,
        composed: true
      })
    );
  };

  render(): TemplateResult {
    const {
      viewport,
      colX,
      colWidths,
      headerHeight,
      colGroupings = [],
      colGroupingMax = 0,
      colGroupingInversed = false,
      selectedColumns = new Set<number>(),
      horizontalSplitPosition = 0,
      frozenColX = [],
      frozenColWidths = [],
      scrollLeft = 0
    } = this.renderState;

    // A header of no height still draws its grouping gutter: a
    // workbook can hide its headers and keep its column groups.
    if ((headerHeight === 0 && colGroupingMax === 0) || viewport.lastColumnIndex < viewport.firstColumnIndex) {
      return html``;
    }

    // Slice 167: grouping gutter occupies `colGroupingMax`
    // rows of `GROUPING_LEVEL_HEIGHT_PX` pixels each, either
    // ABOVE the letter row (default) or BELOW it (inversed).
    const gutterHeight = colGroupingMax * GROUPING_LEVEL_HEIGHT_PX;
    const letterTop = colGroupingInversed ? 0 : gutterHeight;
    const gutterTop = colGroupingInversed ? headerHeight : 0;

    const cells: TemplateResult[] = [];
    const handles: TemplateResult[] = [];
    const buildColumn = (col: number, left: number, width: number): void => {
      const letter = columnIndexToLetters(col);
      const style =
        `left:${left}px;top:${letterTop}px;width:${width}px;` +
        `height:${headerHeight}px;line-height:${headerHeight}px;`;
      // data-col drives the click delegation handler in
      // firstUpdated() (slice 148) — same pattern as the
      // cell-grid (slice 146).
      // Slice 173: `part="column-header"` (+ `header-selected`
      // when the column intersects the active selection)
      // mirrors the documented public styling API.
      const isSelected = selectedColumns.has(col);
      const cellPart = isSelected ? 'column-header header-selected' : 'column-header';
      cells.push(html`
        <div class="col-header-cell col${col}" part=${cellPart} style=${style} data-col=${col}> ${letter} </div>
      `);
      // Resize handle on the right edge of this column
      // (slice 164). Anchored at the column's right edge so
      // mousedown begins a resize-drag for THIS column.
      const handleStyle = `left:${left + width - 3}px;top:${letterTop}px;` + `height:${headerHeight}px;`;
      handles.push(html` <div class="col-resize-handle" style=${handleStyle} data-col=${col}></div> `);
    };

    for (let col = viewport.firstColumnIndex; col <= viewport.lastColumnIndex; col++) {
      const width = colWidths[col - viewport.firstColumnIndex] ?? 0;
      if (width === 0) {
        continue;
      }
      buildColumn(col, colX[col - viewport.firstColumnIndex] ?? 0, width);
    }

    // Frozen columns get their own strip, translated by the scroll
    // offset so it stays put while the rest slides underneath — the
    // same trick the cell-grid uses for its frozen quadrants. Built
    // after the scrolling columns so it draws over them.
    const frozenCells: TemplateResult[] = [];
    const frozenHandles: TemplateResult[] = [];
    let frozenStrip: TemplateResult | string = '';
    if (horizontalSplitPosition > 0) {
      const scrolling = cells.length;
      const scrollingHandles = handles.length;
      for (let col = 1; col <= horizontalSplitPosition; col++) {
        const width = frozenColWidths[col - 1] ?? 0;
        if (width === 0) {
          continue;
        }
        buildColumn(col, frozenColX[col - 1] ?? 0, width);
      }
      frozenCells.push(...cells.splice(scrolling));
      frozenHandles.push(...handles.splice(scrollingHandles));
      frozenStrip = html`
        <div class="frozen-header-strip" style="transform:translateX(${scrollLeft}px);">
          ${frozenCells}${frozenHandles}
        </div>
      `;
    }

    const groupingEls: TemplateResult[] = [];
    for (const g of colGroupings) {
      const levelRow = (g.level - 1) * GROUPING_LEVEL_HEIGHT_PX;
      const lineTop = gutterTop + levelRow + Math.floor(GROUPING_LEVEL_HEIGHT_PX / 2);
      const lineLeft = g.startPx;
      const lineWidth = Math.max(0, g.endPx - g.startPx);
      const lineStyle = `left:${lineLeft}px;top:${lineTop}px;width:${lineWidth}px;`;
      groupingEls.push(html` <div class="col-grouping-line" style=${lineStyle}></div> `);
      const btnLeft = g.buttonPx;
      const btnTop = gutterTop + levelRow + Math.floor((GROUPING_LEVEL_HEIGHT_PX - GROUPING_BUTTON_SIZE_PX) / 2);
      const btnStyle = `left:${btnLeft}px;top:${btnTop}px;`;
      groupingEls.push(html`
        <button
          class="col-grouping-toggle"
          style=${btnStyle}
          data-unique-index=${g.uniqueIndex}
          data-collapsed=${g.collapsed ? 'true' : 'false'}
        >
          ${g.collapsed ? '+' : '−'}
        </button>
      `);
    }
    return html`${groupingEls}${cells}${handles}${frozenStrip}`;
  }
}

customElements.define('vaadin-spreadsheet-internal-column-header', SpreadsheetColumnHeader);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-column-header': SpreadsheetColumnHeader;
  }
}
