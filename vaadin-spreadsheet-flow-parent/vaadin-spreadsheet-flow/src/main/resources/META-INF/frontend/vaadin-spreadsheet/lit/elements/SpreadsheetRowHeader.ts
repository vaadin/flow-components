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
import { repeat } from 'lit/directives/repeat.js';

import {
  GROUPING_BUTTON_SIZE_PX,
  type PositionedGrouping,
  ROW_GROUPING_LEVEL_PX,
  rowGroupingGutterPx
} from './SpreadsheetColumnHeader.js';
import type { ViewportBounds } from '../algorithms/ViewportMath.js';
import {
  groupingToggleBg,
  groupingToggleBorderColor,
  headerBg,
  headerBorderColor,
  headerFont,
  headerFontSize,
  headerSelectedBg
} from './ThemeProperties.js';

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
   * on through the numbers, as the GWT client's corner pane border did;
   * with rows frozen alone that pane had no border, and the numbers keep a
   * pixel's gap at the freeze line that the sheet's background shows
   * through.
   */
  frozenColumns?: boolean;
  /**
   * How far the sheet is scrolled right. With rows frozen alone, the GWT
   * client's pane of frozen rows slid left under the numbers as the sheet
   * scrolled right, and its border showed through the numbers' gap at the
   * freeze line from the right - under the numbers scrolled over it.
   */
  scrollLeft?: number;
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
   * levels start there, where the GWT client's did, so their dots fall on
   * the same pixels.
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
 * the cell-grid + column-header.
 */
export class SpreadsheetRowHeader extends SpreadsheetPart {
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
      border-top: 2px solid ${headerBorderColor};
      border-left: 2px solid ${headerBorderColor};
      pointer-events: none;
    }

    .row-grouping-line.inversed {
      border-top: none;
      border-bottom: 2px solid ${headerBorderColor};
    }

    /* The sign sits where the GWT client's did, on an 11px line at the
       top of a borderless button: the ring is drawn inside, taking no room,
       and the pixel of padding fits the box to the line - a box taller than
       its line, and the browser centres the line instead. */
    .row-grouping-toggle {
      position: absolute;
      width: 12px;
      height: 12px;
      box-sizing: border-box;
      padding: 0 0 1px;
      margin: 0;
      border: 0;
      box-shadow: inset 0 0 0 1px ${groupingToggleBorderColor};
      border-radius: 50%;
      background: ${groupingToggleBg};
      color: #fff;
      font: inherit;
      font-size: 12px;
      line-height: 11px;
      text-align: center;
      cursor: pointer;
      z-index: 2;
    }

    .row-grouping-gutter {
      position: absolute;
      left: 0;
      top: 0;
      box-sizing: border-box;
      border-right: 1px solid ${headerBorderColor};
      pointer-events: none;
    }

    /* Divides one outline level from the next. Over the freeze line in
       the gutter, as the GWT client drew its level lines over its panes. */
    .row-grouping-separator {
      position: absolute;
      z-index: 3;
      width: 0;
      border-right: 1px dotted ${headerBorderColor};
      pointer-events: none;
    }

    :host {
      display: block;
      position: sticky;
      left: 0;
      z-index: 4;
      box-sizing: border-box;
      background: ${headerBg};
      border-right: 1px solid ${headerBorderColor};
      font-family: ${headerFont};
      font-size: ${headerFontSize};
    }

    :host([hidden]) {
      display: none !important;
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

    /* Over the strip's right border too, as the GWT client's pane border
       crossed it. */
    .frozen-header-strip > .freeze-line {
      position: absolute;
      height: 1px;
      z-index: 3;
      background: #6a6a6a;
      pointer-events: none;
    }

    /* Over the sheet's ground, as the GWT client's pane border was, not
       over the numbers' tint. */
    :host([theme~='lumo']) .frozen-header-strip > .freeze-line {
      background: linear-gradient(var(--lumo-contrast-50pct), var(--lumo-contrast-50pct)) var(--lumo-base-color);
    }

    .freeze-gutter-pane {
      position: absolute;
      left: 0;
      top: 0;
      box-sizing: border-box;
      border-right: 1px solid ${headerBorderColor};
      border-bottom: 1px solid #6a6a6a;
      pointer-events: none;
    }

    :host([theme~='lumo']) .freeze-gutter-pane {
      border-bottom-color: var(--lumo-contrast-50pct);
    }

    /* The gap the GWT client left at the freeze line in the numbers when
       only rows were frozen: under the numbers, the sheet's background. */
    .freeze-gap {
      position: absolute;
      right: -1px;
      height: 1px;
      z-index: -1;
      background: var(--default-background-color, #fff);
      pointer-events: none;
    }

    .freeze-gap.slid {
      background: #6a6a6a;
    }

    :host([theme~='lumo']) .freeze-gap.slid {
      background: linear-gradient(var(--lumo-contrast-50pct), var(--lumo-contrast-50pct)) var(--lumo-base-color);
    }

    /* A number as the GWT client drew it, over a border of its own along
       the cells - which the active row's marker replaces, and so does an
       application's ::part(row-header) border. The pointer is an arrow
       along the row a click selects. Each number has the strip's own
       background, as in the GWT client, so a frozen row's number covers
       the scrolled one under it. The number is centred the GWT client's
       way, a line of the font's own height in the middle of the whole row.
       The divider between numbers is drawn inside the number's box, as the
       GWT client's was: it takes no room from the number, and the border
       along the cells runs past it to the end, the active row's marker
       too. */
    .row-header-cell {
      position: absolute;
      box-sizing: border-box;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      justify-content: center;
      line-height: 100%;
      text-align: center;
      background: ${headerBg};
      box-shadow: inset 0 -1px 0 ${headerBorderColor};
      border-right: 1px solid ${headerBorderColor};
      cursor: e-resize;
    }

    /* The active cell's headers are tinted so the cursor's row and
       column stay findable on a wide sheet. This is the tint the GWT
       client painted; the cell also carries the documented
       ::part(header-selected) for consumers who want another one. */
    .row-header-cell[part~='header-selected'] {
      background: ${headerSelectedBg};
      /* The GWT client's marker along the edge that faces the cells. */
      border-right: 2px solid #63b1ff;
    }

    /* Resize handle on the bottom edge of each row header.
           5px tall, full-width, cursor:row-resize. */
    .row-resize-handle {
      position: absolute;
      left: 0;
      height: 5px;
      cursor: row-resize;
      z-index: 1;
    }

    /* SpreadsheetTheme.LUMO, as the GWT client drew it: the strip and its
       outline gutter tinted over the Lumo ground, the active cell's row in
       the primary tint with its marker beside it. */
    :host([theme~='lumo']) {
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    }

    /* Each number carries the strip's tint too, so a frozen row's number
       covers the scrolled one under it. */
    :host([theme~='lumo']) .row-header-cell {
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    }

    :host([theme~='lumo']) .row-header-cell[part~='header-selected'] {
      background-image: linear-gradient(var(--lumo-primary-color-10pct), var(--lumo-primary-color-10pct));
      border-right-color: var(--lumo-primary-color-50pct);
    }

    :host([theme~='lumo']) .row-grouping-toggle {
      background-image: linear-gradient(var(--lumo-contrast-30pct), var(--lumo-contrast-30pct));
      font-size: var(--lumo-font-size-xs);
      cursor: var(--lumo-clickable-cursor);
    }
  `;

  declare renderState: RowHeaderRenderState;

  constructor() {
    super();
    this.renderState = emptyRowHeaderRenderState();
  }

  /**
   * Shadow-root click delegation — same pattern as the column
   * header. Dispatches {@code row-header-click}
   * with the row coordinate + modifier-key state.
   */
  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
    // Row-resize drag mousedown.
    this.shadowRoot!.addEventListener('mousedown', this.handleShadowMouseDown as EventListener);
    // Right-click on a row header dispatches
    // `row-header-contextmenu`. When the connector takes it,
    // the browser's native menu is suppressed so only the
    // server-supplied ContextMenu is shown.
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
    // The browser's own menu stays unless the sheet takes the right-click
    // for a menu of its own, as with the GWT client.
    const taken = !this.dispatchEvent(
      new CustomEvent('row-header-contextmenu', {
        detail: { row, clientX: e.clientX, clientY: e.clientY },
        bubbles: true,
        composed: true,
        cancelable: true
      })
    );
    if (taken) {
      e.preventDefault();
    }
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
    // Where the row starts on screen: the GWT client sized it from there
    // to the pointer.
    const number = this.shadowRoot!.querySelector<HTMLElement>(`.row-header-cell.row${row}`);
    this.dispatchEvent(
      new CustomEvent('row-resize-start', {
        detail: { row, clientX: ev.clientX, clientY: ev.clientY, top: number?.getBoundingClientRect().top },
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
      rowGroupingHeadings = true,
      verticalSplitPosition = 0,
      frozenColumns = false,
      scrollLeft = 0,
      frozenRowY = [],
      frozenRowHeights = [],
      scrollTop = 0,
      leadingPx = 0
    } = this.renderState;

    // As in the column header: no width still draws the grouping
    // gutter, for a workbook that hides its headers and groups rows.
    if ((headerWidth === 0 && rowGroupingMax === 0) || viewport.lastRowIndex < viewport.firstRowIndex) {
      return html``;
    }

    // The gutter takes a fixed slice of the header's width, on the left
    // whichever side a group's summary row is on, as in the GWT client;
    // the numbers occupy the rest.
    const gutterWidth = rowGroupingGutterPx(rowGroupingMax, rowGroupingHeadings);
    const numbersLeft = gutterWidth;
    const numbersWidth = Math.max(0, headerWidth - gutterWidth);

    // Keyed by row, so a number keeps its element as the sheet scrolls:
    // matched by position, every one was rewritten on every scroll step.
    const cells: Array<{ key: number; template: TemplateResult }> = [];
    const handles: Array<{ key: number; template: TemplateResult }> = [];
    const buildRow = (row: number, top: number, height: number): void => {
      // Headings hidden, the outline still drawn: no numbers at all, as in
      // the GWT client. A number of no width still draws its border.
      if (numbersWidth <= 0) {
        return;
      }
      const style = `left:${numbersLeft}px;top:${top}px;width:${numbersWidth}px;height:${height}px;`;
      // `part="row-header"` (+ `header-selected`
      // when the row intersects the active selection) mirrors
      // the documented public styling API.
      const isSelected = selectedRows.has(row);
      const cellPart = isSelected ? 'row-header header-selected' : 'row-header';
      // data-row drives the click-delegation handler in
      // firstUpdated().
      cells.push({
        key: row,
        template: html`
          <div class="row-header-cell row${row}" part=${cellPart} style=${style} data-row=${row}> ${row} </div>
        `
      });
      // Resize handle on the row's bottom edge.
      const handleStyle = `top:${top + height - 3}px;left:${numbersLeft}px;width:${numbersWidth}px;`;
      handles.push({
        key: row,
        template: html` <div class="row-resize-handle" style=${handleStyle} data-row=${row}></div> `
      });
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
    let freezeGap: TemplateResult | typeof nothing = nothing;
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
      // The freeze line runs on across the outline gutter, as the GWT
      // client's frozen gutter pane border did, on the pixel it has to
      // itself among the cells; across the numbers too when columns are
      // frozen as well, and otherwise a gap there.
      const frozenHeight =
        (frozenRowY[verticalSplitPosition - 1] ?? 0) + (frozenRowHeights[verticalSplitPosition - 1] ?? 0);
      // In the gutter, the bottom border of a pane over the frozen rows'
      // part of it, bordered along the right as the gutter is - the GWT
      // client's frozen gutter pane, whose two borders met in a blended
      // corner pixel.
      const gutterLine =
        gutterWidth > 0
          ? html`<div class="freeze-gutter-pane" style="width:${gutterWidth}px;height:${frozenHeight + 1}px;"></div>`
          : nothing;
      const numberLine =
        numbersWidth > 0 && frozenColumns
          ? html`<div class="freeze-line" style="top:${frozenHeight}px;left:${gutterWidth}px;right:-1px;"></div>`
          : nothing;
      // With rows frozen alone the GWT client's corner pane had no border,
      // and its pixel was see-through: the sheet's background at rest, the
      // numbers that scroll under it as the sheet scrolls. So the gap lies
      // under the numbers, pinned with the frozen ones.
      // What of the frozen rows' line has slid left under the numbers lies
      // in the gap too, under the numbers.
      freezeGap =
        numbersWidth > 0 && !frozenColumns
          ? html`<div
                class="freeze-gap"
                style="transform:translateY(${scrollTop}px);top:${frozenHeight}px;left:${gutterWidth}px;"
              ></div
              >${scrollLeft > 0
                ? html`<div
                    class="freeze-gap slid"
                    style="transform:translateY(${scrollTop}px);top:${frozenHeight}px;left:${Math.max(
                      gutterWidth,
                      headerWidth - scrollLeft
                    )}px;"
                  ></div>`
                : nothing}`
          : nothing;
      frozenStrip = html`
        <div class="frozen-header-strip" style="transform:translateY(${scrollTop}px);">
          ${frozenCells.map((cell) => cell.template)}${frozenHandles.map(
            (handle) => handle.template
          )}${gutterLine}${numberLine}
        </div>
      `;
    }

    const groupingEls: TemplateResult[] = [];
    // The gutter is bordered off from the numbers beside it, as the GWT
    // client's was; without numbers the strip's own border does it.
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
        // reaches just past the button's centre, as the GWT client's did.
        const lineStyle = `left:${lineLeft}px;top:${g.startPx}px;height:${Math.max(0, g.endPx - g.startPx)}px;`;
        const lineClass = rowGroupingInversed ? 'row-grouping-line inversed' : 'row-grouping-line';
        groupingEls.push(html`<div class=${lineClass} style=${lineStyle}></div>`);
      }
      // At the summary row, its right edge 2px short of the bracket's
      // tick end, as the GWT client placed it.
      const half = GROUPING_BUTTON_SIZE_PX / 2;
      const btnStyle = `left:${lineLeft + 2 - half}px;top:${g.buttonPx - half}px;`;
      groupingEls.push(html`
        <button
          class="row-grouping-toggle"
          type="button"
          aria-expanded=${g.collapsed ? 'false' : 'true'}
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

customElements.define('vaadin-spreadsheet-internal-row-header', SpreadsheetRowHeader);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-row-header': SpreadsheetRowHeader;
  }
}
