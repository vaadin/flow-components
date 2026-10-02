/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, type TemplateResult } from 'lit';

import {
  GROUPING_BUTTON_SIZE_PX,
  type PositionedGrouping,
  ROW_GROUPING_LEVEL_PX,
  rowGroupingGutterPx
} from './SpreadsheetColumnHeader.js';
import type { ViewportBounds } from '../algorithms/ViewportMath.js';

/**
 * Input state for the {@link SpreadsheetRowHeader} element. The
 * connector builds + pushes this alongside the cell-grid's render
 * state on every render cycle (slice 144).
 *
 * Mirror of {@link ColumnHeaderRenderState}: same shape, swapped
 * column axis → row axis.
 */
export interface RowHeaderRenderState {
  /** Currently visible rows — same shape as the cell-grid's viewport. */
  viewport: ViewportBounds;
  /**
   * Pixel y-offsets for visible rows, indexed by
   * {@code row - viewport.firstRowIndex} — copied through from
   * the cell-grid render state.
   */
  rowY: ReadonlyArray<number>;
  /** Pixel height of each visible row, same indexing as {@code rowY}. */
  rowHeights: ReadonlyArray<number>;
  /**
   * Width of the header strip in pixels. {@code 0} disables the
   * header (render emits an empty fragment). Excel's typical row-
   * header width is 30-40 px (wider for large row counts so 1M+
   * fits without truncation).
   */
  headerWidth: number;
  /**
   * Total grid pixel height — used to size the row-header strip's
   * intrinsic height so it occupies the same vertical scroll
   * extent as the cell-grid.
   */
  totalGridHeightPx: number;
  /**
   * Rows intersecting the active selection. Each cell whose row
   * is in this set gets the {@code header-selected} part in
   * addition to {@code row-header}, mirroring the documented
   * public styling API. Slice 173.
   */
  selectedRows: ReadonlySet<number>;
  /**
   * Number of frozen rows at the top of the sheet. Those rows stay on
   * screen whatever the scroll position, so their headers have to stay
   * with them — a header that scrolls away from the rows it numbers is
   * worse than none.
   */
  verticalSplitPosition?: number;
  /** Pixel y-offsets of the frozen rows, indexed by {@code row - 1}. */
  frozenRowY?: ReadonlyArray<number>;
  /** Pixel heights of the frozen rows, same indexing as frozenRowY. */
  frozenRowHeights?: ReadonlyArray<number>;
  /**
   * Current vertical scroll offset, used to translate the frozen strip
   * so it cancels the scroll out.
   */
  scrollTop?: number;
  /**
   * Row groupings (outline) with pre-computed pixel positions. Mirrors the
   * column header's gutter, rotated: the gutter is a column of levels to the
   * LEFT of the row numbers (or to the right when inversed).
   */
  rowGroupings: ReadonlyArray<PositionedGrouping>;
  /** Number of grouping levels. {@code 0} renders no gutter. */
  rowGroupingMax: number;
  /**
   * When {@code true} the gutter renders to the RIGHT of the row numbers and
   * each +/- button sits at the END of its range. Excel's "summary columns to
   * the right" toggle.
   */
  rowGroupingInversed: boolean;
}

/** Helper: build an empty row-header render state. */
export function emptyRowHeaderRenderState(): RowHeaderRenderState {
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
    rowY: [],
    rowHeights: [],
    headerWidth: 0,
    totalGridHeightPx: 0,
    selectedRows: new Set(),
    verticalSplitPosition: 0,
    frozenRowY: [],
    frozenRowHeights: [],
    scrollTop: 0,
    rowGroupings: [],
    rowGroupingMax: 0,
    rowGroupingInversed: false
  };
}

/**
 * Internal row-header Lit element. Renders one
 * {@code .row-header-cell} per visible row with the row's 1-based
 * number inside.
 *
 * The element is sticky-positioned (via host CSS, {@code left:0})
 * so it stays pinned at the left edge of the scroll container as
 * the user scrolls right. Vertical scroll moves the strip
 * naturally with the cells, keeping row numbers aligned with
 * their rows.
 *
 * Same render-state-driven contract + same no-decorator pattern as
 * the cell-grid + column-header. Slice 144.
 */
export class SpreadsheetRowHeader extends LitElement {
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
  static readonly EXPORTED_PARTS = 'row-header,header-selected';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    /* The outline gutter beside the numbers, as the GWT client drew it:
       a bracket down each group - a line with a tick where the group
       starts - and a round button beside its summary row, which sits
       below the group or, inversed, above it. */
    .row-grouping-line {
      position: absolute;
      width: 8px;
      border-top: 2px solid var(--vaadin-spreadsheet-header-border-color, #c7c7c7);
      border-left: 2px solid var(--vaadin-spreadsheet-header-border-color, #c7c7c7);
      pointer-events: none;
    }

    .row-grouping-line.inversed {
      border-top: none;
      border-bottom: 2px solid var(--vaadin-spreadsheet-header-border-color, #c7c7c7);
    }

    .row-grouping-toggle {
      position: absolute;
      width: 12px;
      height: 12px;
      box-sizing: border-box;
      padding: 0;
      margin: 0;
      border: 1px solid var(--vaadin-spreadsheet-grouping-toggle-border-color, #c7c7c7);
      border-radius: 50%;
      background: var(--vaadin-spreadsheet-grouping-toggle-bg, #c7c7c7);
      color: #fff;
      font: inherit;
      font-size: 12px;
      line-height: 10px;
      text-align: center;
      cursor: pointer;
      z-index: 2;
    }

    /* Divides one outline level from the next. */
    .row-grouping-separator {
      position: absolute;
      top: 0;
      width: 0;
      border-right: 1px dotted var(--vaadin-spreadsheet-header-border-color, #c7c7c7);
      pointer-events: none;
    }

    :host {
      display: block;
      position: sticky;
      left: 0;
      z-index: 4;
      box-sizing: border-box;
      background: var(--vaadin-spreadsheet-header-bg, #fafafa);
      border-right: 1px solid var(--vaadin-spreadsheet-header-border-color, #c7c7c7);
      font-family: var(--vaadin-spreadsheet-header-font, inherit);
      font-size: var(--vaadin-spreadsheet-header-font-size, 13px);
    }

    /* Frozen rows are drawn in their own strip, translated to cancel
       the scroll out, above the scrolling headers. */
    .frozen-header-strip {
      position: absolute;
      inset: 0;
      z-index: 2;
      pointer-events: none;
    }

    .frozen-header-strip > * {
      pointer-events: auto;
    }

    .row-header-cell {
      position: absolute;
      box-sizing: border-box;
      overflow: hidden;
      text-align: center;
      border-bottom: 1px solid var(--vaadin-spreadsheet-header-border-color, #c7c7c7);
    }

    /* The active cell's headers are tinted so the cursor's row and
       column stay findable on a wide sheet. This is the tint the GWT
       client painted; the cell also carries the documented
       ::part(header-selected) for consumers who want another one. */
    .row-header-cell[part~='header-selected'] {
      background: var(--vaadin-spreadsheet-header-selected-bg, #e6edf4);
      /* The GWT client's marker along the edge that faces the cells. */
      box-shadow: inset -2px 0 0 #63b1ff;
    }

    /* Resize handle on the bottom edge of each row header
           (slice 164). 5px tall, full-width, cursor:row-resize. */
    .row-resize-handle {
      position: absolute;
      left: 0;
      height: 5px;
      cursor: row-resize;
      z-index: 1;
    }
  `;

  declare renderState: RowHeaderRenderState;

  constructor() {
    super();
    this.renderState = emptyRowHeaderRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', SpreadsheetRowHeader.EXPORTED_PARTS);
  }

  /**
   * Shadow-root click delegation — same pattern as the column
   * header (slice 148). Dispatches {@code row-header-click}
   * with the row coordinate + modifier-key state.
   */
  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
    // Slice 164: row-resize drag mousedown.
    this.shadowRoot!.addEventListener('mousedown', this.handleShadowMouseDown as EventListener);
    // Slice 166: right-click on a row header dispatches
    // `row-header-contextmenu`. preventDefault suppresses the
    // browser's native menu so only the server-supplied
    // ContextMenu is shown.
    this.shadowRoot!.addEventListener('contextmenu', this.handleShadowContextMenu);
    // Double-clicking a resize handle autofits the row to its content, the
    // way Excel does; the drag on the same handle resizes it by hand.
    this.shadowRoot!.addEventListener('dblclick', this.handleShadowDoubleClick as EventListener);
  }

  private readonly handleShadowDoubleClick = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const handle = target.closest<HTMLElement>('.row-resize-handle');
    if (!handle) {
      return;
    }
    const row = Number(handle.dataset.row);
    if (!Number.isFinite(row) || row < 1) {
      return;
    }
    e.preventDefault();
    this.dispatchEvent(
      new CustomEvent('row-autofit', {
        detail: { row },
        bubbles: true,
        composed: true
      })
    );
  };

  private readonly handleShadowContextMenu = (event: Event): void => {
    const e = event as MouseEvent;
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const cell = target.closest<HTMLElement>('.row-header-cell');
    if (!cell) {
      return;
    }
    const row = Number(cell.dataset.row);
    if (!Number.isFinite(row) || row < 1) {
      return;
    }
    e.preventDefault();
    this.dispatchEvent(
      new CustomEvent('row-header-contextmenu', {
        detail: { row, clientX: e.clientX, clientY: e.clientY },
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
    const handle = target.closest<HTMLElement>('.row-resize-handle');
    if (!handle) {
      return;
    }
    const row = Number(handle.dataset.row);
    if (!Number.isFinite(row) || row < 1) {
      return;
    }
    e.preventDefault();
    const ev = e as MouseEvent;
    this.dispatchEvent(
      new CustomEvent('row-resize-start', {
        detail: { row, clientY: ev.clientY },
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
    // Grouping toggle click — dispatch `row-grouping-toggle` so the
    // connector can forward `groupingCollapsed`. Stop here so the click is
    // not also read as a row-header click.
    const toggle = target.closest<HTMLElement>('.row-grouping-toggle');
    if (toggle) {
      const uniqueIndex = Number(toggle.dataset.uniqueIndex);
      const collapsed = toggle.dataset.collapsed === 'true';
      if (Number.isFinite(uniqueIndex)) {
        this.dispatchEvent(
          new CustomEvent('row-grouping-toggle', {
            detail: { uniqueIndex, collapsed: !collapsed },
            bubbles: true,
            composed: true
          })
        );
      }
      ev.stopPropagation();
      return;
    }
    const cell = target.closest<HTMLElement>('.row-header-cell');
    if (!cell) {
      return;
    }
    const row = Number(cell.dataset.row);
    if (!Number.isFinite(row) || row < 1) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('row-header-click', {
        detail: {
          row,
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
      rowY,
      rowHeights,
      headerWidth,
      totalGridHeightPx,
      selectedRows = new Set<number>(),
      rowGroupings = [],
      rowGroupingMax = 0,
      rowGroupingInversed = false,
      verticalSplitPosition = 0,
      frozenRowY = [],
      frozenRowHeights = [],
      scrollTop = 0
    } = this.renderState;

    // As in the column header: no width still draws the grouping
    // gutter, for a workbook that hides its headers and groups rows.
    if ((headerWidth === 0 && rowGroupingMax === 0) || viewport.lastRowIndex < viewport.firstRowIndex) {
      return html``;
    }

    // The gutter takes a fixed slice of the header's width, on the left
    // whichever side a group's summary row is on, as in the GWT client;
    // the numbers occupy the rest.
    const gutterWidth = rowGroupingGutterPx(rowGroupingMax);
    const numbersLeft = gutterWidth;
    const numbersWidth = Math.max(0, headerWidth - gutterWidth);

    const cells: TemplateResult[] = [];
    const handles: TemplateResult[] = [];
    const buildRow = (row: number, top: number, height: number): void => {
      const style =
        `left:${numbersLeft}px;top:${top}px;width:${numbersWidth}px;height:${height}px;` + `line-height:${height}px;`;
      // Slice 173: `part="row-header"` (+ `header-selected`
      // when the row intersects the active selection) mirrors
      // the documented public styling API.
      const isSelected = selectedRows.has(row);
      const cellPart = isSelected ? 'row-header header-selected' : 'row-header';
      // data-row drives the click-delegation handler in
      // firstUpdated() (slice 148).
      cells.push(html`
        <div class="row-header-cell row${row}" part=${cellPart} style=${style} data-row=${row}> ${row} </div>
      `);
      // Resize handle on the row's bottom edge (slice 164).
      const handleStyle = `top:${top + height - 3}px;left:${numbersLeft}px;width:${numbersWidth}px;`;
      handles.push(html` <div class="row-resize-handle" style=${handleStyle} data-row=${row}></div> `);
    };

    for (let row = viewport.firstRowIndex; row <= viewport.lastRowIndex; row++) {
      const height = rowHeights[row - viewport.firstRowIndex] ?? 0;
      if (height === 0) {
        continue;
      }
      buildRow(row, rowY[row - viewport.firstRowIndex] ?? 0, height);
    }

    // Frozen rows get their own strip, translated by the scroll offset
    // so it stays put while the rest slides underneath — the same trick
    // the cell-grid uses for its frozen quadrants.
    let frozenStrip: TemplateResult | string = '';
    if (verticalSplitPosition > 0) {
      const scrolling = cells.length;
      const scrollingHandles = handles.length;
      for (let row = 1; row <= verticalSplitPosition; row++) {
        const height = frozenRowHeights[row - 1] ?? 0;
        if (height === 0) {
          continue;
        }
        buildRow(row, frozenRowY[row - 1] ?? 0, height);
      }
      const frozenCells = cells.splice(scrolling);
      const frozenHandles = handles.splice(scrollingHandles);
      frozenStrip = html`
        <div class="frozen-header-strip" style="transform:translateY(${scrollTop}px);">
          ${frozenCells}${frozenHandles}
        </div>
      `;
    }

    const groupingEls: TemplateResult[] = [];
    for (let level = 1; level <= rowGroupingMax; level++) {
      const style = `left:${level * ROW_GROUPING_LEVEL_PX}px;height:${totalGridHeightPx}px;`;
      groupingEls.push(html`<div class="row-grouping-separator" style=${style}></div>`);
    }
    for (const g of rowGroupings) {
      const lineLeft = 6 + (g.level - 1) * ROW_GROUPING_LEVEL_PX;
      // A collapsed group keeps only its button, showing "+".
      if (!g.collapsed) {
        // The tick's border comes on top of the height, so the bracket
        // reaches just past the button's centre, as the GWT client's did.
        const lineStyle = `left:${lineLeft}px;top:${g.startPx}px;height:${Math.max(0, g.endPx - g.startPx)}px;`;
        const lineClass = rowGroupingInversed ? 'row-grouping-line inversed' : 'row-grouping-line';
        groupingEls.push(html`<div class=${lineClass} style=${lineStyle}></div>`);
      }
      // Centred on the bracket's line, at the summary row.
      const half = GROUPING_BUTTON_SIZE_PX / 2;
      const btnStyle = `left:${lineLeft + 1 - half}px;top:${g.buttonPx - half}px;`;
      groupingEls.push(html`
        <button
          class="row-grouping-toggle"
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

customElements.define('vaadin-spreadsheet-internal-row-header', SpreadsheetRowHeader);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-row-header': SpreadsheetRowHeader;
  }
}
