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
import { type HeaderAxis, HeaderStrip } from './headers/HeaderStrip.js';
import { headerStripStyles } from './headers/HeaderStripStyles.js';

import {
  GROUPING_BUTTON_SIZE_PX,
  type PositionedGrouping,
  ROW_GROUPING_LEVEL_PX,
  rowGroupingGutterPx
} from './headers/Grouping.js';
import type { ViewportBounds } from '../algorithms/ViewportMath.js';
import { headerBg, headerBorderColor } from './ThemeProperties.js';

/**
 * Input state for the {@link SpreadsheetRowHeader} element. The
 * connector builds + pushes this alongside the cell-grid's render
 * state on every render cycle.
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
   * public styling API.
   */
  selectedRows: ReadonlySet<number>;
  /**
   * Number of frozen rows at the top of the sheet. Those rows stay on
   * screen whatever the scroll position, so their headers have to stay
   * with them — a header that scrolls away from the rows it numbers is
   * worse than none.
   */
  verticalSplitPosition?: number;
  /**
   * Whether columns are frozen as well. Only then does the freeze line run
   * on through the numbers; with rows frozen alone it does not, and the
   * numbers keep a pixel's gap at the freeze line that the sheet's background shows
   * through.
   */
  frozenColumns?: boolean;
  /**
   * How far the sheet is scrolled right. With rows frozen alone, the frozen
   * rows' border shows through the numbers' gap at the freeze line as the
   * sheet scrolls right, from the right - under the numbers scrolled over
   * it.
   */
  scrollLeft?: number;
  /** Pixel y-offsets of the frozen rows, indexed by {@code row - 1}. */
  frozenRowY?: ReadonlyArray<number>;
  /** Pixel heights of the frozen rows, same indexing as frozenRowY. */
  frozenRowHeights?: ReadonlyArray<number>;
  /**
   * Row groupings (outline) with pre-computed pixel positions. Mirrors the
   * column header's gutter, rotated: the gutter is a column of levels to the
   * LEFT of the row numbers (or to the right when inversed).
   */
  rowGroupings: ReadonlyArray<PositionedGrouping>;
  /** Number of grouping levels. {@code 0} renders no gutter. */
  rowGroupingMax: number;
  /**
   * Whether a group's summary row is the one above it - Excel's summary rows
   * above their detail - which puts the group's button there and turns its
   * bracket's tick to the far end. The gutter stays beside the numbers
   * either way.
   */
  rowGroupingInversed: boolean;
  /**
   * Whether the headings show, which gives the gutter its extra level for
   * the button that opens every group. Absent means they do.
   */
  rowGroupingHeadings?: boolean;
  /**
   * How far above the first row the top of the outline gutters is - the
   * letters and their outline gutter. The dotted lines between the outline
   * levels start there, so their dots fall on the same pixels, and the frozen
   * numbers are held that far down from the view's top edge.
   */
  leadingPx?: number;
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
    rowGroupings: [],
    rowGroupingMax: 0,
    rowGroupingInversed: false
  };
}

/** The numbers along the rows, for what both header strips do alike. */
const ROW_AXIS: HeaderAxis = {
  key: 'row',
  cellClass: 'row-header-cell',
  handleClass: 'row-resize-handle',
  toggleClass: 'row-grouping-toggle',
  edge: 'top',
  events: {
    click: 'row-header-click',
    contextMenu: 'row-header-contextmenu',
    resizeStart: 'row-resize-start',
    autofit: 'row-autofit',
    groupingToggle: 'row-grouping-toggle'
  }
};

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
 * the cell-grid + column-header.
 */
export class SpreadsheetRowHeader extends HeaderStrip {
  static readonly EXPORTED_PARTS = 'row-header,header-selected';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = [
    headerStripStyles,
    css`
      :host {
        left: 0;
        z-index: 4;
        flex-direction: column;
        /* Nothing in the header may lengthen the sheet's scroll area: the
           last row's resize handle reaches past its end, and so can the
           frozen strip, held in place as the sheet scrolls. Clipped down
           only: along the cells, the active number's marker and the freeze
           line run over the header's border. */
        overflow-y: clip;
        border-right: 1px solid ${headerBorderColor};
      }

      .frozen-header-strip,
      .frozen-header-under {
        height: 0;
      }

      .frozen-header-strip > .freeze-line {
        height: 1px;
      }

      .freeze-gutter-pane {
        border-right: 1px solid ${headerBorderColor};
        border-bottom: 1px solid #6a6a6a;
      }

      :host([theme~='lumo']) .freeze-gutter-pane {
        border-bottom-color: var(--lumo-contrast-50pct);
      }

      .freeze-gap {
        right: -1px;
        height: 1px;
      }

      /* What of the frozen rows' line has slid left under the numbers. */
      .freeze-gap.slid {
        background: #6a6a6a;
      }

      :host([theme~='lumo']) .freeze-gap.slid {
        background: linear-gradient(var(--lumo-contrast-50pct), var(--lumo-contrast-50pct)) var(--lumo-base-color);
      }

      /* A number, over a border of its own along
         the cells - which the active row's marker replaces, and so does an
         application's ::part(row-header) border. The pointer is an arrow
         along the row a click selects. Each number has the strip's own
         background, so a frozen row's number covers the scrolled one under
         it. The number is centred as a line of the font's own height in the
         middle of the whole row. The divider between numbers is drawn inside
         the number's box: it takes no room from the number, and the border
         along the cells runs past it to the end, the active row's marker
         too. */
      .row-header-cell {
        display: flex;
        flex-direction: column;
        justify-content: center;
        line-height: 100%;
        background: ${headerBg};
        box-shadow: inset 0 -1px 0 ${headerBorderColor};
        border-right: 1px solid ${headerBorderColor};
        cursor: e-resize;
      }

      /* A marker along the edge that faces the cells. */
      .row-header-cell[part~='header-selected'] {
        border-right: 2px solid #63b1ff;
      }

      /* Resize handle on the bottom edge of each row header, full-width. */
      .row-resize-handle {
        left: 0;
        height: 5px;
        cursor: row-resize;
      }

      /* The outline gutter beside the numbers: the brackets run down, their
         ticks across, and the summary row sits below the group or, inversed,
         above it. */
      .row-grouping-line {
        width: 8px;
      }

      .row-grouping-line.inversed {
        border-top: none;
        border-bottom: 2px solid ${headerBorderColor};
      }

      .row-grouping-gutter {
        border-right: 1px solid ${headerBorderColor};
      }

      .row-grouping-separator {
        width: 0;
        border-right: 1px dotted ${headerBorderColor};
      }

      /* Each number carries the strip's tint too in Lumo, so a frozen row's
         number covers the scrolled one under it. The active cell's row is in
         the primary tint, with its marker beside it. */
      :host([theme~='lumo']) .row-header-cell {
        background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
      }

      :host([theme~='lumo']) .row-header-cell[part~='header-selected'] {
        background-image: linear-gradient(var(--lumo-primary-color-10pct), var(--lumo-primary-color-10pct));
        border-right-color: var(--lumo-primary-color-50pct);
      }

      :host([theme~='lumo']) .row-grouping-toggle {
        font-size: var(--lumo-font-size-xs);
      }
    `
  ];

  declare renderState: RowHeaderRenderState;

  protected readonly axis = ROW_AXIS;

  constructor() {
    super();
    this.renderState = emptyRowHeaderRenderState();
  }

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
      rowGroupingHeadings = true,
      verticalSplitPosition = 0,
      frozenColumns = false,
      scrollLeft = 0,
      frozenRowY = [],
      frozenRowHeights = [],
      leadingPx = 0
    } = this.renderState;

    // As in the column header: no width still draws the grouping
    // gutter, for a workbook that hides its headers and groups rows.
    if ((headerWidth === 0 && rowGroupingMax === 0) || viewport.lastRowIndex < viewport.firstRowIndex) {
      return html``;
    }

    // The gutter takes a fixed slice of the header's width, on the left
    // whichever side a group's summary row is on; the numbers occupy the
    // rest.
    const gutterWidth = rowGroupingGutterPx(rowGroupingMax, rowGroupingHeadings);
    const numbersLeft = gutterWidth;
    const numbersWidth = Math.max(0, headerWidth - gutterWidth);

    const labels = this.labels(
      {
        first: viewport.firstRowIndex,
        last: viewport.lastRowIndex,
        starts: rowY,
        sizes: rowHeights,
        frozen: verticalSplitPosition,
        frozenStarts: frozenRowY,
        frozenSizes: frozenRowHeights
      },
      (row, top, height) => {
        // Headings hidden, the outline still drawn: no numbers at all. A number of no width still draws its border.
        if (numbersWidth <= 0) {
          return null;
        }
        const style = `left:${numbersLeft}px;top:${top}px;width:${numbersWidth}px;height:${height}px;`;
        // `part="row-header"` (+ `header-selected` when the row intersects
        // the active selection) mirrors the documented public styling API.
        // data-row is what the click delegation reads.
        const isSelected = selectedRows.has(row);
        const cellPart = isSelected ? 'row-header header-selected' : 'row-header';
        // Resize handle on the row's bottom edge.
        const handleStyle = `top:${top + height - 3}px;left:${numbersLeft}px;width:${numbersWidth}px;`;
        return {
          cell: html`
            <div class="row-header-cell row${row}" part=${cellPart} style=${style} data-row=${row}> ${row} </div>
          `,
          handle: html` <div class="row-resize-handle" style=${handleStyle} data-row=${row}></div> `
        };
      }
    );

    // Frozen rows get their own strip, held in place by the browser as the
    // sheet scrolls, as the cell-grid's frozen panes are, while the rest
    // slide underneath: as far down from the view's top edge as the column
    // letters reach.
    let frozenStrip: TemplateResult | string = '';
    let freezeGap: TemplateResult | typeof nothing = nothing;
    if (verticalSplitPosition > 0) {
      // The freeze line runs on across the outline gutter, on the pixel it
      // has to
      // itself among the cells; across the numbers too when columns are
      // frozen as well, and otherwise a gap there.
      const frozenHeight =
        (frozenRowY[verticalSplitPosition - 1] ?? 0) + (frozenRowHeights[verticalSplitPosition - 1] ?? 0);
      // In the gutter, the bottom border of a pane over the frozen rows'
      // part of it, bordered along the right as the gutter is, the two
      // borders meeting in a blended corner pixel.
      const gutterLine =
        gutterWidth > 0
          ? html`<div class="freeze-gutter-pane" style="width:${gutterWidth}px;height:${frozenHeight + 1}px;"></div>`
          : nothing;
      const numberLine =
        numbersWidth > 0 && frozenColumns
          ? html`<div class="freeze-line" style="top:${frozenHeight}px;left:${gutterWidth}px;right:-1px;"></div>`
          : nothing;
      // With rows frozen alone the corner has no border, and its pixel is
      // see-through: the sheet's background at rest, the numbers that
      // scroll under it as the sheet scrolls. So the gap lies
      // under the numbers, held in place with the frozen ones.
      // What of the frozen rows' line has slid left under the numbers lies
      // in the gap too, under the numbers.
      freezeGap =
        numbersWidth > 0 && !frozenColumns
          ? html`<div class="frozen-header-under" style="top:${leadingPx}px;">
              <div class="freeze-gap" style="top:${frozenHeight}px;left:${gutterWidth}px;"></div>
              ${scrollLeft > 0
                ? html`<div
                    class="freeze-gap slid"
                    style="top:${frozenHeight}px;left:${Math.max(gutterWidth, headerWidth - scrollLeft)}px;"
                  ></div>`
                : nothing}
            </div>`
          : nothing;
      frozenStrip = html`
        <div class="frozen-header-strip" style="top:${leadingPx}px;">
          ${labels.frozenCells}${labels.frozenHandles}${gutterLine}${numberLine}
        </div>
      `;
    }

    const groupingEls: TemplateResult[] = [];
    // The gutter is bordered off from the numbers beside it; without numbers the strip's own border does it.
    if (gutterWidth > 0 && numbersWidth > 0) {
      const style = `width:${gutterWidth}px;height:${totalGridHeightPx}px;`;
      groupingEls.push(html`<div class="row-grouping-gutter" style=${style}></div>`);
    }
    // From the top of the gutters, and an odd length: the browser lays a
    // dotted line's dots every other pixel from its start only along an
    // odd length, and along an even one doubles the first and shifts the
    // rest.
    const levels = Math.round((gutterWidth - 1) / ROW_GROUPING_LEVEL_PX);
    for (let level = 1; level < levels; level++) {
      const style =
        `left:${level * ROW_GROUPING_LEVEL_PX}px;top:${-leadingPx}px;` +
        `height:${Math.ceil(totalGridHeightPx + leadingPx) | 1}px;`;
      groupingEls.push(html`<div class="row-grouping-separator" style=${style}></div>`);
    }
    for (const g of rowGroupings) {
      const lineLeft = 6 + (g.level - 1) * ROW_GROUPING_LEVEL_PX;
      // A collapsed group keeps only its button, showing "+".
      if (!g.collapsed) {
        // The tick's border comes on top of the height, so the bracket
        // reaches just past the button's centre.
        const lineStyle = `left:${lineLeft}px;top:${g.startPx}px;height:${Math.max(0, g.endPx - g.startPx)}px;`;
        const lineClass = rowGroupingInversed ? 'row-grouping-line inversed' : 'row-grouping-line';
        groupingEls.push(html`<div class=${lineClass} style=${lineStyle}></div>`);
      }
      // At the summary row, its right edge 2px short of the bracket's
      // tick end.
      const half = GROUPING_BUTTON_SIZE_PX / 2;
      const btnStyle = `left:${lineLeft + 2 - half}px;top:${g.buttonPx - half}px;`;
      groupingEls.push(this.groupingToggle(g, btnStyle));
    }
    return this.strip(groupingEls, labels, freezeGap, frozenStrip);
  }
}

customElements.define('vaadin-spreadsheet-internal-row-header', SpreadsheetRowHeader);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-row-header': SpreadsheetRowHeader;
  }
}
