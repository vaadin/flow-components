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

import { columnIndexToLetters } from '../algorithms/CellRef.js';
import type { ViewportBounds } from '../algorithms/ViewportMath.js';
import {
  COLUMN_GROUPING_LEVEL_PX,
  columnGroupingGutterPx,
  GROUPING_BUTTON_SIZE_PX,
  type PositionedGrouping
} from './headers/Grouping.js';
import { headerBg, headerBorderColor, headerSelectedBg } from './ThemeProperties.js';

/**
 * Input state for the {@link SpreadsheetColumnHeader} element.
 * The Spreadsheet connector builds a fresh instance alongside the
 * cell-grid's {@code CellGridRenderState} and pushes it to the
 * header on every render cycle.
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
   * an empty fragment). 27 by default.
   */
  headerHeight: number;
  /**
   * Total grid pixel width — used to size the header's intrinsic
   * width so it contributes to the scroll container's scroll
   * extent the same way the cell-grid does.
   */
  totalGridWidthPx: number;
  /**
   * Number of frozen columns at the left of the sheet. Those columns
   * stay on screen whatever the scroll position, so their headers have
   * to stay with them — a header that scrolls away from the cells it
   * names is worse than none.
   */
  horizontalSplitPosition?: number;
  /** Pixel x-offsets of the frozen columns, indexed by {@code col - 1}. */
  frozenColX?: ReadonlyArray<number>;
  /**
   * Whether rows are frozen as well. Only then does the freeze line run on
   * through the letters; with columns frozen alone it does not, and the
   * letters keep a pixel's gap at the freeze line that the sheet's background shows
   * through.
   */
  frozenRows?: boolean;
  /**
   * How far the sheet is scrolled down. With columns frozen alone, the
   * frozen columns' border shows through the letters' gap at the freeze
   * line as the sheet scrolls down, from the bottom up, over the letters
   * scrolled under it.
   */
  scrollTop?: number;
  /** Pixel widths of the frozen columns, same indexing as frozenColX. */
  frozenColWidths?: ReadonlyArray<number>;

  /**
   * Column groupings (outline) with pre-computed pixel positions.
   * Pushed by the connector after computing the
   * absolute startPx/endPx from {@code colWidths}. The header
   * renders one row per level above (or below, if inversed) the
   * letter cells; each grouping shows a clickable +/- button at
   * one end of its range.
   */
  colGroupings: ReadonlyArray<PositionedGrouping>;
  /**
   * Number of grouping levels (rows in the gutter). {@code 0}
   * means no grouping data — the gutter renders nothing and
   * doesn't add to the element's height.
   */
  colGroupingMax: number;
  /**
   * Whether a group's summary column is the one before it - Excel's summary
   * columns to the left of their detail - which puts the group's button
   * there and turns its bracket's tick to the far end. The gutter stays
   * above the letters either way.
   */
  colGroupingInversed: boolean;
  /**
   * How far left of the first column the sheet's edge is - the row numbers
   * and their outline gutter. The dotted lines between the outline levels
   * start there, so their dots fall on the same pixels, and the frozen
   * letters are held that far in from the view's left edge.
   */
  leadingPx?: number;
  /**
   * Columns intersecting the active selection. Each cell whose
   * column is in this set gets the {@code header-selected} part
   * in addition to {@code column-header}, mirroring the
   * documented public styling API.
   */
  selectedColumns: ReadonlySet<number>;
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
    colGroupings: [],
    colGroupingMax: 0,
    colGroupingInversed: false,
    selectedColumns: new Set()
  };
}

/** The letters along the columns, for what both header strips do alike. */
const COLUMN_AXIS: HeaderAxis = {
  key: 'col',
  cellClass: 'col-header-cell',
  handleClass: 'col-resize-handle',
  toggleClass: 'col-grouping-toggle',
  edge: 'left',
  events: {
    click: 'column-header-click',
    contextMenu: 'column-header-contextmenu',
    resizeStart: 'column-resize-start',
    autofit: 'column-autofit',
    groupingToggle: 'column-grouping-toggle'
  }
};

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
 * {@link SpreadsheetCellGrid}.
 */
export class SpreadsheetColumnHeader extends HeaderStrip {
  static readonly EXPORTED_PARTS = 'column-header,header-selected';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = [
    headerStripStyles,
    css`
      :host {
        top: 0;
        z-index: 5;
        /* Nothing in the header may widen the sheet's scroll area: the last
           column's resize handle reaches past its end, and so can the frozen
           strip, held in place as the sheet scrolls. Clipped across only:
           along the cells, the active letter's marker and the freeze line
           run over the header's border. */
        overflow-x: clip;
        border-bottom: 1px solid ${headerBorderColor};
      }

      .frozen-header-strip,
      .frozen-header-under {
        width: 0;
      }

      .frozen-header-strip > .freeze-line {
        width: 1px;
      }

      .freeze-gutter-pane {
        border-right: 1px solid #6a6a6a;
        border-bottom: 1px solid ${headerBorderColor};
      }

      :host([theme~='lumo']) .freeze-gutter-pane {
        border-right-color: var(--lumo-contrast-50pct);
      }

      .freeze-gap {
        bottom: -1px;
        width: 1px;
      }

      /* A letter: on a gradient from the header
         colour down to a shade darker, over a border of its own along the
         cells - which the active column's marker replaces, and so does an
         application's ::part(column-header) border. The pointer is an arrow
         down the column a click selects. The divider between letters is
         drawn inside the letter's box: it takes no room from the letter, which is centred on the whole column, and the
         border along the cells runs under it to the end, the active column's
         marker too. The shades blend in sRGB, as two hex colours do: with a
         mixed colour in it, a gradient blends in Oklab, a level off in
         places. */
      .col-header-cell {
        box-shadow: inset -1px 0 0 ${headerBorderColor};
        border-bottom: 1px solid ${headerBorderColor};
        background: linear-gradient(to bottom in srgb, ${headerBg} 2%, color-mix(in srgb, ${headerBg} 96%, #000) 98%);
        cursor: s-resize;
      }

      /* A marker along the edge that faces the cells. */
      .col-header-cell[part~='header-selected'] {
        border-bottom: 2px solid #63b1ff;
      }

      /* Resize handle on the right edge of each column header, full-height.
         A press begins a resize drag. */
      .col-resize-handle {
        top: 0;
        width: 5px;
        cursor: col-resize;
      }

      /* The outline gutter above the letters: the brackets run across, their
         ticks down, and the summary column sits after the group or, inversed,
         before it. */
      .col-grouping-line {
        height: 7px;
      }

      .col-grouping-line.inversed {
        border-left: none;
        border-right: 2px solid ${headerBorderColor};
      }

      .col-grouping-gutter {
        border-bottom: 1px solid ${headerBorderColor};
      }

      .col-grouping-separator {
        height: 0;
        border-bottom: 1px dotted ${headerBorderColor};
      }

      /* No gradient on the letters in Lumo: each carries the strip's tint
         over the Lumo ground, so a frozen column's letter covers the scrolled
         one under it. The active cell's column is in the primary tint, with
         its marker below. */
      :host([theme~='lumo']) .col-header-cell {
        background-color: ${headerBg};
        background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
      }

      :host([theme~='lumo']) .col-header-cell[part~='header-selected'] {
        background-color: ${headerSelectedBg};
        background-image: linear-gradient(var(--lumo-primary-color-10pct), var(--lumo-primary-color-10pct));
        border-bottom-color: var(--lumo-primary-color-50pct);
      }

      :host([theme~='lumo']) .col-grouping-toggle {
        font-size: var(--lumo-font-size-s);
      }
    `
  ];

  declare renderState: ColumnHeaderRenderState;

  protected readonly axis = COLUMN_AXIS;

  constructor() {
    super();
    this.renderState = emptyColumnHeaderRenderState();
  }

  render(): TemplateResult {
    const {
      viewport,
      colX,
      colWidths,
      headerHeight,
      totalGridWidthPx,
      colGroupings = [],
      colGroupingMax = 0,
      colGroupingInversed = false,
      leadingPx = 0,
      selectedColumns = new Set<number>(),
      horizontalSplitPosition = 0,
      frozenRows = false,
      scrollTop = 0,
      frozenColX = [],
      frozenColWidths = []
    } = this.renderState;

    // A header of no height still draws its grouping gutter: a
    // workbook can hide its headers and keep its column groups.
    if ((headerHeight === 0 && colGroupingMax === 0) || viewport.lastColumnIndex < viewport.firstColumnIndex) {
      return html``;
    }

    // The outline gutter sits above the letters whichever side a group's
    // summary column is on.
    const gutterHeight = columnGroupingGutterPx(colGroupingMax, headerHeight > 0);
    const letterTop = gutterHeight;

    const labels = this.labels(
      {
        first: viewport.firstColumnIndex,
        last: viewport.lastColumnIndex,
        starts: colX,
        sizes: colWidths,
        frozen: horizontalSplitPosition,
        frozenStarts: frozenColX,
        frozenSizes: frozenColWidths
      },
      (col, left, width) => {
        // Headings hidden, the outline still drawn: no letters at all. A letter of no height still draws its border.
        if (headerHeight <= 0) {
          return null;
        }
        const letter = columnIndexToLetters(col);
        const style =
          `left:${left}px;top:${letterTop}px;width:${width}px;` +
          `height:${headerHeight}px;line-height:${headerHeight}px;`;
        // `part="column-header"` (+ `header-selected` when the column
        // intersects the active selection) mirrors the documented public
        // styling API. data-col is what the click delegation reads.
        const isSelected = selectedColumns.has(col);
        const cellPart = isSelected ? 'column-header header-selected' : 'column-header';
        // Resize handle on the right edge of this column, so a press on it
        // begins a resize of THIS column.
        const handleStyle = `left:${left + width - 3}px;top:${letterTop}px;` + `height:${headerHeight}px;`;
        return {
          cell: html`
            <div class="col-header-cell col${col}" part=${cellPart} style=${style} data-col=${col}> ${letter} </div>
          `,
          handle: html` <div class="col-resize-handle" style=${handleStyle} data-col=${col}></div> `
        };
      }
    );

    // Frozen columns get their own strip, held in place by the browser as
    // the sheet scrolls, as the cell-grid's frozen panes are, while the rest
    // slide underneath: as far in from the view's left edge as the row
    // numbers reach. Drawn after the scrolling columns so it draws over
    // them.
    let frozenStrip: TemplateResult | string = '';
    let freezeGap: TemplateResult | typeof nothing = nothing;
    if (horizontalSplitPosition > 0) {
      // The freeze line runs on up through the outline gutter, on the pixel
      // it has to
      // itself among the cells; through the letters too when rows are
      // frozen as well, and otherwise a gap there.
      const frozenWidth =
        (frozenColX[horizontalSplitPosition - 1] ?? 0) + (frozenColWidths[horizontalSplitPosition - 1] ?? 0);
      // In the gutter, the right border of a pane over the frozen columns'
      // part of it, bordered along the bottom as the gutter is, the two
      // borders meeting in a blended corner pixel.
      const gutterLine =
        gutterHeight > 0
          ? html`<div class="freeze-gutter-pane" style="width:${frozenWidth + 1}px;height:${gutterHeight}px;"></div>`
          : nothing;
      const letterLine =
        headerHeight > 0 && frozenRows
          ? html`<div class="freeze-line" style="left:${frozenWidth}px;top:${gutterHeight}px;bottom:-1px;"></div>`
          : nothing;
      // With columns frozen alone the corner has no border, and its pixel
      // is see-through: the sheet's background at rest, the letters that
      // scroll under it as the sheet scrolls. So the gap lies under the
      // letters, held in place with the frozen ones.
      freezeGap =
        headerHeight > 0 && !frozenRows
          ? html`<div class="frozen-header-under" style="left:${leadingPx}px;">
              <div class="freeze-gap" style="left:${frozenWidth}px;top:${gutterHeight}px;"></div>
            </div>`
          : nothing;
      // What of the frozen columns' line has slid up under the letters.
      const risenLine =
        headerHeight > 0 && !frozenRows && scrollTop > 0
          ? html`<div
              class="freeze-line"
              style="left:${frozenWidth}px;top:${Math.max(
                gutterHeight,
                gutterHeight + headerHeight - scrollTop
              )}px;bottom:-1px;"
            ></div>`
          : nothing;
      frozenStrip = html`
        <div class="frozen-header-strip" style="left:${leadingPx}px;">
          ${labels.frozenCells}${labels.frozenHandles}${gutterLine}${letterLine}${risenLine}
        </div>
      `;
    }

    const groupingEls: TemplateResult[] = [];
    // The gutter is bordered off from the letters below it; without letters the strip's own border does it.
    if (gutterHeight > 0 && headerHeight > 0) {
      const style = `height:${gutterHeight}px;width:${totalGridWidthPx}px;`;
      groupingEls.push(html`<div class="col-grouping-gutter" style=${style}></div>`);
    }
    // From the sheet's edge, and an odd length: the browser lays a dotted
    // line's dots every other pixel from its start only along an odd
    // length, and along an even one doubles the first and shifts the rest.
    const levels = Math.round((gutterHeight - 3) / COLUMN_GROUPING_LEVEL_PX);
    for (let level = 1; level < levels; level++) {
      const style =
        `left:${-leadingPx}px;top:${level * COLUMN_GROUPING_LEVEL_PX}px;` +
        `width:${Math.ceil(totalGridWidthPx + leadingPx) | 1}px;`;
      groupingEls.push(html`<div class="col-grouping-separator" style=${style}></div>`);
    }
    for (const g of colGroupings) {
      const lineTop = 9 + (g.level - 1) * COLUMN_GROUPING_LEVEL_PX;
      // A collapsed group keeps only its button, showing "+".
      if (!g.collapsed) {
        // The tick's border comes on top of the width, so the bracket
        // reaches just past the button's centre.
        const lineStyle = `left:${g.startPx}px;top:${lineTop}px;width:${Math.max(0, g.endPx - g.startPx)}px;`;
        const lineClass = colGroupingInversed ? 'col-grouping-line inversed' : 'col-grouping-line';
        groupingEls.push(html`<div class=${lineClass} style=${lineStyle}></div>`);
      }
      // Centred on the bracket's line, at the summary column.
      const half = GROUPING_BUTTON_SIZE_PX / 2;
      const btnStyle = `left:${g.buttonPx - half}px;top:${lineTop + 1 - half}px;`;
      groupingEls.push(this.groupingToggle(g, btnStyle));
    }
    return this.strip(groupingEls, labels, freezeGap, frozenStrip);
  }
}

customElements.define('vaadin-spreadsheet-internal-column-header', SpreadsheetColumnHeader);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-column-header': SpreadsheetColumnHeader;
  }
}
