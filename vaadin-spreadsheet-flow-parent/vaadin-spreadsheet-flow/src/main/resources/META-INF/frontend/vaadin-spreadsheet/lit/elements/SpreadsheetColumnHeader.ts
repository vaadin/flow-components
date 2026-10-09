/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, nothing, type TemplateResult } from 'lit';
import { repeat } from 'lit/directives/repeat.js';

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
   * an empty fragment). 27 by default, as in the GWT client.
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
  /**
   * Whether rows are frozen as well. Only then does the freeze line run on
   * through the letters, as the GWT client's corner pane border did; with
   * columns frozen alone that pane had no border, and the letters keep a
   * pixel's gap at the freeze line that the sheet's background shows
   * through.
   */
  frozenRows?: boolean;
  /**
   * How far the sheet is scrolled down. With columns frozen alone, the GWT
   * client's pane of frozen columns slid up under the letters as the sheet
   * scrolled down, and its border showed through the letters' gap at the
   * freeze line, from the bottom up, over the letters scrolled under it.
   */
  scrollTop?: number;
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
   * Whether a group's summary column is the one before it - Excel's summary
   * columns to the left of their detail - which puts the group's button
   * there and turns its bracket's tick to the far end. The gutter stays
   * above the letters either way.
   */
  colGroupingInversed: boolean;
  /**
   * How far left of the first column the sheet's edge is - the row numbers
   * and their outline gutter. The dotted lines between the outline levels
   * start there, where the GWT client's did, so their dots fall on the
   * same pixels.
   */
  leadingPx?: number;
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
  /** Where the group's bracket starts, its tick included. */
  startPx: number;
  /** Where the group's bracket ends. */
  endPx: number;
  /**
   * Centre of the group's button: over the middle of the summary column -
   * the one after the group, or before it when the grouping is inversed.
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

/** Height of one column outline level in the gutter: the GWT client's 18px. */
export const COLUMN_GROUPING_LEVEL_PX = 18;
/** Width of one row outline level in the gutter: the GWT client's 15px. */
export const ROW_GROUPING_LEVEL_PX = 15;
/** Diameter of a group's round expand/collapse button. */
export const GROUPING_BUTTON_SIZE_PX = 12;

/**
 * Height of the column outline gutter, in the GWT client's sizes: a level
 * per depth of grouping and, while the headings show, one more for the level
 * button that expands everything. Nothing when no column is grouped.
 */
export function columnGroupingGutterPx(maxLevel: number, headings: boolean): number {
  return maxLevel <= 0 ? 0 : 3 + (maxLevel + (headings ? 1 : 0)) * COLUMN_GROUPING_LEVEL_PX;
}

/** Width of the row outline gutter, likewise. */
export function rowGroupingGutterPx(maxLevel: number, headings: boolean): number {
  return maxLevel <= 0 ? 0 : 1 + (maxLevel + (headings ? 1 : 0)) * ROW_GROUPING_LEVEL_PX;
}

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
      background: var(--vaadin-spreadsheet-header-bg, var(--_vaadin-spreadsheet-header-bg, #fafafa));
      border-bottom: 1px solid
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      font-family: var(--vaadin-spreadsheet-header-font, var(--_vaadin-spreadsheet-header-font, Helvetica));
      font-size: var(--vaadin-spreadsheet-header-font-size, var(--_vaadin-spreadsheet-header-font-size, 13px));
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

    /* Over the strip's bottom border too, as the GWT client's pane border
       crossed it. */
    .frozen-header-strip > .freeze-line {
      position: absolute;
      width: 1px;
      z-index: 3;
      background: #6a6a6a;
      pointer-events: none;
    }

    .freeze-gutter-pane {
      position: absolute;
      left: 0;
      top: 0;
      box-sizing: border-box;
      border-right: 1px solid #6a6a6a;
      border-bottom: 1px solid
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      pointer-events: none;
    }

    :host([theme~='lumo']) .freeze-gutter-pane {
      border-right-color: var(--lumo-contrast-50pct);
    }

    /* The gap the GWT client left at the freeze line in the letters when
       only columns were frozen: under the letters, the sheet's background. */
    .freeze-gap {
      position: absolute;
      bottom: -1px;
      width: 1px;
      z-index: -1;
      background: var(--default-background-color, #fff);
      pointer-events: none;
    }

    /* A letter as the GWT client drew it: on a gradient from the header
       colour down to a shade darker, over a border of its own along the
       cells - which the active column's marker replaces, and so does an
       application's ::part(column-header) border. The pointer is an arrow
       down the column a click selects. The divider between letters is
       drawn inside the letter's box, as the GWT client's was: it takes no
       room from the letter, which is centred on the whole column, and the
       border along the cells runs under it to the end, the active column's
       marker too. The shades blend in sRGB, as the GWT client's two hex
       colours did: with a mixed colour in it, a gradient blends in Oklab,
       a level off in places. */
    .col-header-cell {
      position: absolute;
      box-sizing: border-box;
      overflow: hidden;
      text-align: center;
      box-shadow: inset -1px 0 0 var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      border-bottom: 1px solid
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      background: linear-gradient(
        to bottom in srgb,
        var(--vaadin-spreadsheet-header-bg, var(--_vaadin-spreadsheet-header-bg, #fafafa)) 2%,
        color-mix(in srgb, var(--vaadin-spreadsheet-header-bg, var(--_vaadin-spreadsheet-header-bg, #fafafa)) 96%, #000)
          98%
      );
      cursor: s-resize;
    }

    /* The active cell's headers are tinted so the cursor's row and
       column stay findable on a wide sheet. This is the tint the GWT
       client painted; the cell also carries the documented
       ::part(header-selected) for consumers who want another one. */
    .col-header-cell[part~='header-selected'] {
      background: var(--vaadin-spreadsheet-header-selected-bg, var(--_vaadin-spreadsheet-header-selected-bg, #e6edf4));
      /* The GWT client's marker along the edge that faces the cells. */
      border-bottom: 2px solid #63b1ff;
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

    /* The outline gutter above the letters, as the GWT client drew it: a
       bracket over each group - a line with a tick where the group
       starts - and a round button over its summary column, which sits
       after the group or, inversed, before it. */
    .col-grouping-line {
      position: absolute;
      height: 7px;
      border-top: 2px solid
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      border-left: 2px solid
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      pointer-events: none;
    }

    .col-grouping-line.inversed {
      border-left: none;
      border-right: 2px solid
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
    }

    /* The sign sits where the GWT client's did, on an 11px line at the
       top of a borderless button: the ring is drawn inside, taking no room,
       and the pixel of padding fits the box to the line - a box taller than
       its line, and the browser centres the line instead. */
    .col-grouping-toggle {
      position: absolute;
      width: 12px;
      height: 12px;
      box-sizing: border-box;
      padding: 0 0 1px;
      margin: 0;
      border: 0;
      box-shadow: inset 0 0 0 1px
        var(
          --vaadin-spreadsheet-grouping-toggle-border-color,
          var(--_vaadin-spreadsheet-grouping-toggle-border-color, #c7c7c7)
        );
      border-radius: 50%;
      background: var(--vaadin-spreadsheet-grouping-toggle-bg, var(--_vaadin-spreadsheet-grouping-toggle-bg, #c7c7c7));
      color: #fff;
      font: inherit;
      font-size: 12px;
      line-height: 11px;
      text-align: center;
      cursor: pointer;
      z-index: 2;
    }

    .col-grouping-gutter {
      position: absolute;
      left: 0;
      top: 0;
      box-sizing: border-box;
      border-bottom: 1px solid
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      pointer-events: none;
    }

    /* Divides one outline level from the next. Over the freeze line in
       the gutter, as the GWT client drew its level lines over its panes. */
    .col-grouping-separator {
      position: absolute;
      z-index: 3;
      height: 0;
      border-bottom: 1px dotted
        var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7));
      pointer-events: none;
    }

    /* SpreadsheetTheme.LUMO, as the GWT client drew it: the strip and its
       outline gutter tinted over the Lumo ground, the active cell's column
       in the primary tint with its marker below. */
    :host([theme~='lumo']) {
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    }

    /* No gradient on the letters: each carries the strip's tint over the
       Lumo ground, as in the GWT client, so a frozen column's letter
       covers the scrolled one under it. */
    :host([theme~='lumo']) .col-header-cell {
      background-color: var(--vaadin-spreadsheet-header-bg, var(--_vaadin-spreadsheet-header-bg, #fafafa));
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    }

    /* Over the sheet's ground, as the GWT client's pane border was, not
       over the letters' tint. */
    :host([theme~='lumo']) .frozen-header-strip > .freeze-line {
      background: linear-gradient(var(--lumo-contrast-50pct), var(--lumo-contrast-50pct)) var(--lumo-base-color);
    }

    :host([theme~='lumo']) .col-header-cell[part~='header-selected'] {
      background-color: var(
        --vaadin-spreadsheet-header-selected-bg,
        var(--_vaadin-spreadsheet-header-selected-bg, #e6edf4)
      );
      background-image: linear-gradient(var(--lumo-primary-color-10pct), var(--lumo-primary-color-10pct));
      border-bottom-color: var(--lumo-primary-color-50pct);
    }

    :host([theme~='lumo']) .col-grouping-toggle {
      background-image: linear-gradient(var(--lumo-contrast-30pct), var(--lumo-contrast-30pct));
      font-size: var(--lumo-font-size-s);
      cursor: var(--lumo-clickable-cursor);
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
    // server. The browser's native menu is suppressed when the
    // connector takes the event.
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
    // The browser's own menu stays unless the sheet takes the right-click
    // for a menu of its own, as with the GWT client.
    const taken = !this.dispatchEvent(
      new CustomEvent('column-header-contextmenu', {
        detail: { col, clientX: e.clientX, clientY: e.clientY },
        bubbles: true,
        composed: true,
        cancelable: true
      })
    );
    if (taken) {
      e.preventDefault();
    }
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
    // Where the column starts on screen: the GWT client sized it from
    // there to the pointer.
    const letter = this.shadowRoot!.querySelector<HTMLElement>(`.col-header-cell.col${col}`);
    this.dispatchEvent(
      new CustomEvent('column-resize-start', {
        detail: { col, clientX: ev.clientX, clientY: ev.clientY, left: letter?.getBoundingClientRect().left },
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
      frozenColWidths = [],
      scrollLeft = 0
    } = this.renderState;

    // A header of no height still draws its grouping gutter: a
    // workbook can hide its headers and keep its column groups.
    if ((headerHeight === 0 && colGroupingMax === 0) || viewport.lastColumnIndex < viewport.firstColumnIndex) {
      return html``;
    }

    // The outline gutter sits above the letters whichever side a group's
    // summary column is on, as it did in the GWT client.
    const gutterHeight = columnGroupingGutterPx(colGroupingMax, headerHeight > 0);
    const letterTop = gutterHeight;

    // Keyed by column, so a letter keeps its element as the sheet scrolls:
    // matched by position, every one was rewritten on every scroll step.
    const cells: Array<{ key: number; template: TemplateResult }> = [];
    const handles: Array<{ key: number; template: TemplateResult }> = [];
    const buildColumn = (col: number, left: number, width: number): void => {
      // Headings hidden, the outline still drawn: no letters at all, as in
      // the GWT client. A letter of no height still draws its border.
      if (headerHeight <= 0) {
        return;
      }
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
      cells.push({
        key: col,
        template: html`
          <div class="col-header-cell col${col}" part=${cellPart} style=${style} data-col=${col}> ${letter} </div>
        `
      });
      // Resize handle on the right edge of this column
      // (slice 164). Anchored at the column's right edge so
      // mousedown begins a resize-drag for THIS column.
      const handleStyle = `left:${left + width - 3}px;top:${letterTop}px;` + `height:${headerHeight}px;`;
      handles.push({
        key: col,
        template: html` <div class="col-resize-handle" style=${handleStyle} data-col=${col}></div> `
      });
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
    let freezeGap: TemplateResult | typeof nothing = nothing;
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
      frozenCells.push(...cells.splice(scrolling).map((cell) => cell.template));
      frozenHandles.push(...handles.splice(scrollingHandles).map((handle) => handle.template));
      // The freeze line runs on up through the outline gutter, as the GWT
      // client's frozen gutter pane border did, on the pixel it has to
      // itself among the cells; through the letters too when rows are
      // frozen as well, and otherwise a gap there.
      const frozenWidth =
        (frozenColX[horizontalSplitPosition - 1] ?? 0) + (frozenColWidths[horizontalSplitPosition - 1] ?? 0);
      // In the gutter, the right border of a pane over the frozen columns'
      // part of it, bordered along the bottom as the gutter is - the GWT
      // client's frozen gutter pane, whose two borders met in a blended
      // corner pixel.
      const gutterLine =
        gutterHeight > 0
          ? html`<div class="freeze-gutter-pane" style="width:${frozenWidth + 1}px;height:${gutterHeight}px;"></div>`
          : nothing;
      const letterLine =
        headerHeight > 0 && frozenRows
          ? html`<div class="freeze-line" style="left:${frozenWidth}px;top:${gutterHeight}px;bottom:-1px;"></div>`
          : nothing;
      // With columns frozen alone the GWT client's corner pane had no
      // border, and its pixel was see-through: the sheet's background at
      // rest, the letters that scroll under it as the sheet scrolls. So
      // the gap lies under the letters, pinned with the frozen ones.
      freezeGap =
        headerHeight > 0 && !frozenRows
          ? html`<div
              class="freeze-gap"
              style="transform:translateX(${scrollLeft}px);left:${frozenWidth}px;top:${gutterHeight}px;"
            ></div>`
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
        <div class="frozen-header-strip" style="transform:translateX(${scrollLeft}px);">
          ${frozenCells}${frozenHandles}${gutterLine}${letterLine}${risenLine}
        </div>
      `;
    }

    const groupingEls: TemplateResult[] = [];
    // The gutter is bordered off from the letters below it, as the GWT
    // client's was; without letters the strip's own border does it.
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
        // reaches just past the button's centre, as the GWT client's did.
        const lineStyle = `left:${g.startPx}px;top:${lineTop}px;width:${Math.max(0, g.endPx - g.startPx)}px;`;
        const lineClass = colGroupingInversed ? 'col-grouping-line inversed' : 'col-grouping-line';
        groupingEls.push(html`<div class=${lineClass} style=${lineStyle}></div>`);
      }
      // Centred on the bracket's line, at the summary column.
      const half = GROUPING_BUTTON_SIZE_PX / 2;
      const btnStyle = `left:${g.buttonPx - half}px;top:${lineTop + 1 - half}px;`;
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
    return html`${groupingEls}${repeat(
      cells,
      (cell) => cell.key,
      (cell) => cell.template
    )}${repeat(
      handles,
      (handle) => handle.key,
      (handle) => handle.template
    )}${freezeGap}${frozenStrip}`;
  }
}

customElements.define('vaadin-spreadsheet-internal-column-header', SpreadsheetColumnHeader);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-column-header': SpreadsheetColumnHeader;
  }
}
