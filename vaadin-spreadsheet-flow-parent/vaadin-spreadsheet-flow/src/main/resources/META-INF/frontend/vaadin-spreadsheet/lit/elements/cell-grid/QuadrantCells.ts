/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { html, type TemplateResult } from 'lit';
import { toKey } from '../../algorithms/CellKeys.js';
import { keyedList } from '../../dom/KeyedList.js';
import { isCellEditorSlot } from '../../dom/VirtualChildren.js';
import { isActiveCell, isCellSelected, isCoherentSelection, type SelectionState } from '../../state/SelectionState.js';
import type { CellData } from '../../types/wire.js';
import type { CellGridRenderState } from '../types.js';
import { type QuadrantRenderOpts, quadrantOf } from './GridGeometry.js';

/**
 * Every coordinate of a quadrant that a merged region covers, anchors
 * included.
 *
 * These cells are still rendered — an application, and the tests, ask
 * about a cell by its coordinates whether or not a merge sits over it —
 * but the merge owns the region's content, so they render their text and
 * nothing else. A second slot of the same name underneath would capture
 * the component and hide it below the merge.
 *
 * Each region is clipped to the quadrant first: a merge as tall as the
 * sheet cost a million coordinates on every render otherwise.
 */
export function coveredByMergeCoords(
  mergedRegions: ReadonlyArray<{ col1: number; col2: number; row1: number; row2: number }>,
  colStart: number,
  colEnd: number,
  rowStart: number,
  rowEnd: number
): Set<string> {
  const covered = new Set<string>();
  for (const region of mergedRegions) {
    const lastRow = Math.min(region.row2, rowEnd);
    const lastCol = Math.min(region.col2, colEnd);
    for (let row = Math.max(region.row1, rowStart); row <= lastRow; row++) {
      for (let col = Math.max(region.col1, colStart); col <= lastCol; col++) {
        covered.add(toKey(col, row));
      }
    }
  }
  return covered;
}

/** How a cell shows its place in the selection. */
export interface SelectionMark {
  /** The classes it adds to the cell's own, each after a space; empty when it adds none. */
  classes: string;
  /** The cell's part, which mirrors the documented styling API. */
  part: string;
}

/**
 * How a cell - a plain one, or the merged cell its anchor starts - shows its
 * place in the selection. A cell of the range other than the active one is
 * tinted, and carries the `cell-range` part too, so
 * `vaadin-spreadsheet::part(cell-range)` reaches it. In a selection of
 * several ranges the active cell has no outline around it, and is ringed
 * instead.
 */
export function selectionClassFor(selection: SelectionState, col: number, row: number): SelectionMark {
  const isActive = isActiveCell(selection, col, row);
  if (!isActive && isCellSelected(selection, col, row)) {
    return { classes: ' cell-range', part: 'cell cell-range' };
  }
  if (isActive && !isCoherentSelection(selection)) {
    return { classes: ' selected-cell-highlight', part: 'cell' };
  }
  return { classes: '', part: 'cell' };
}

/** The markers in a cell's top-right corner, each nothing when the cell has none. */
export interface CellMarkers {
  /** A formula that could not be evaluated. */
  invalid: TemplateResult | '';
  /** A comment. */
  comment: TemplateResult | '';
}

/** A cell with no markers. */
export const NO_MARKERS: CellMarkers = { invalid: '', comment: '' };

/**
 * The markers in a cell's corner, inside its border: one for a formula that
 * could not be evaluated, and one for a comment. The comment's carries the
 * cell's anchor as data attributes too, so it can be found by the cell it
 * marks.
 */
export function cellMarkers(state: CellGridRenderState, key: string, col: number, row: number): CellMarkers {
  return {
    invalid:
      state.invalidFormulaCells?.has(key) === true
        ? html`<div class="invalidformula-indicator" part="invalid-triangle"></div>`
        : '',
    comment:
      state.commentCells?.has(key) === true
        ? html`<div class="comment-indicator" part="comment-triangle" data-col=${col} data-row=${row}></div>`
        : ''
  };
}

/**
 * A hidden row or column collapses its cells to nothing.
 *
 * `display:none` rather than a zero-sized box: a box with no width still
 * paints its borders, still answers a hit test on its edge, and still
 * reports itself as laid out. Hidden means gone.
 */
function collapsedDeclaration(width: number, height: number): string {
  return width === 0 || height === 0 ? 'display:none;' : '';
}

/**
 * Inline {@code color:} declaration for a cell whose number format names a
 * colour — `[Red]#,##0` and friends.
 *
 * This cannot ride on a `cs<N>` class: the colour depends on the cell's
 * *value* (a four-part format paints positive, negative, zero and text
 * differently) while the style index describes the format itself. So the
 * server resolves it per cell and sends the hex digits without the `#`.
 */
function textColorDeclaration(data: CellData | undefined): string {
  const color = data?.textColor;
  return color ? `color:#${color};` : '';
}

/**
 * Render the cells of a single quadrant. Each cell carries a
 * {@code quadrant-<name>} class so CSS can apply z-index stacking
 * (corner > top, left > main). The per-cell position comes from the
 * caller's {@code getColLeft/getRowTop/getColWidth/getRowHeight}
 * closures — different quadrants use different position arrays
 * (frozen arrays vs main arrays).
 */
export function renderQuadrant(opts: QuadrantRenderOpts): unknown {
  const { state, quadrantName, colStart, colEnd, rowStart, rowEnd, getColLeft, getColWidth, getRowTop, getRowHeight } =
    opts;
  const { cellData, selection } = state;
  // Defensive defaults, once for every cell: some test fixtures do not
  // declare the slots or the link cells.
  const cellSlots = state.cellSlots ?? {};
  const hyperlinkCells = state.hyperlinkCells ?? new Set<string>();

  const covered = coveredByMergeCoords(state.mergedRegions, colStart, colEnd, rowStart, rowEnd);
  const rows: Array<{ row: number; cells: Array<{ key: string; template: TemplateResult }> }> = [];
  for (let row = rowStart; row <= rowEnd; row++) {
    const cells: Array<{ key: string; template: TemplateResult }> = [];
    const top = getRowTop(row);
    const height = getRowHeight(row);
    for (let col = colStart; col <= colEnd; col++) {
      const left = getColLeft(col);
      const width = getColWidth(col);
      // A cell in a hidden row or column is still rendered, collapsed by
      // `display:none` — see collapsedDeclaration. Leaving it out instead
      // makes it unfindable, and "is this cell hidden?" is a question
      // both the tests and an application's own scripting ask.
      const key = toKey(col, row);
      const data = cellData.get(key);
      // A cell holding a server-side component or a custom editor shows
      // that instead of its own text — it is what the cell is for.
      const underMerge = covered.has(key);
      const slotName = underMerge ? undefined : cellSlots[key];
      // A number too wide for its column shows as hashes rather than a
      // clipped prefix: "$246 913" where the value is 246 trillion reads as
      // a number, and a wrong one. The server sets needsMeasure when its
      // fit check says the value does not fit.
      const tooNarrow = data?.needsMeasure === true && (state.tooNarrowCells?.has(key) ?? false);
      const cellText = tooNarrow ? '###' : data?.value ?? '';
      const rawText = slotName === undefined ? cellText : html`<slot name=${slotName}></slot>`;
      // Text too wide for its column runs into the empty cells beside it,
      // and what still does not fit ends in an ellipsis. The server settles
      // how far, because it is the side that knows what the neighbours hold;
      // a cell showing a component or sitting under a merge keeps to itself.
      const fitsAfterAll = state.fittingSpills?.has(key) ?? false;
      const spill = slotName === undefined && !underMerge && !fitsAfterAll ? data?.overflowWidth ?? 0 : 0;
      // A cell the server sent nothing for still wears the workbook's
      // default style. Without it the cell falls through to the host's own
      // font and colour - Helvetica 14px in grey - instead of the sheet's
      // default, Calibri 11pt in black, which every cell has before anything
      // else applies.
      const cellStyleClass = data?.cellStyle ?? 'cs0';
      // A right-aligned value too wide for its cell - a date, a number in
      // scientific notation - overflows it as any value does: it starts at the cell's left edge and runs on to the right, over
      // its own padding and border too, until a neighbour holding a value
      // cuts it off, without an ellipsis.
      const runsRight = spill > 0 && cellStyleClass.split(/\s+/).includes('r');
      const overflowing = runsRight || spill > width;
      // With nowhere to go the text is still held at the column's width from
      // the padding on: it reaches past the cell's
      // right edge, under the neighbour, which is drawn later on a ground of
      // its own - see .cut-short.
      const cutShort = spill > 0 && !overflowing;
      const text =
        spill === 0
          ? rawText
          : runsRight
          ? html`<div class="cell-spill runs-right" style="width:${spill - 2}px">${rawText}</div>`
          : html`<div class="cell-spill" style="width:${spill}px">${rawText}</div>`;
      const selected = selectionClassFor(selection, col, row);
      // Hyperlink cells get a `link-cell` class: a pointer cursor,
      // the workbook styling the link itself. The click handler in
      // the connector dispatches `linkCellClicked` based
      // on the same hyperlinks map.
      const linkClass = hyperlinkCells.has(key) ? ' link-cell' : '';
      const classList =
        `cell col${col} row${row} quadrant-${quadrantName}` +
        (cellStyleClass ? ` ${cellStyleClass}` : '') +
        selected.classes +
        linkClass +
        (overflowing ? ' overflowing' : '') +
        (cutShort ? ' cut-short' : '') +
        (slotName !== undefined && isCellEditorSlot(slotName) ? ' custom-editor-cell' : '');
      const style =
        `left:${left}px;top:${top}px;width:${width}px;height:${height}px;` +
        collapsedDeclaration(width, height) +
        textColorDeclaration(data);
      // A cell under a merge leaves its markers to the merged cell.
      const markers = underMerge ? NO_MARKERS : cellMarkers(state, key, col, row);
      // data-col + data-row drive the click-delegation handler
      // in firstUpdated() — single source of truth
      // for the cell's coordinate when the event bubbles.
      // On one line, and the marker only where there is one: whitespace
      // between the tags is a text node of its own and every binding a
      // marker, and with a thousand cells drawn they doubled the nodes the
      // sheet makes and restyles as it scrolls.
      const content =
        markers.invalid === '' && markers.comment === '' ? text : html`${markers.invalid}${markers.comment}${text}`;
      cells.push({
        key,
        template: html`<div class=${classList} part=${selected.part} style=${style} data-col=${col} data-row=${row}
          >${content}</div
        >`
      });
    }
    rows.push({ row, cells });
  }
  // Keyed by row, and each row's cells by cell, so a cell keeps its element
  // as the sheet scrolls: cells are placed in sheet coordinates, and one
  // still in view needs no change at all. A row scrolled out of view goes in
  // one removal. Keyed by cell alone, each step removed every cell of every
  // row that left the view one by one, which took most of a frame while
  // scrolling fast; matched by position instead, every cell was rewritten -
  // its class, place and text - and restyled on every step.
  return keyedList(
    rows,
    (group) => group.row,
    (group) =>
      html`<div class="cell-row"
        >${keyedList(
          group.cells,
          (cell) => cell.key,
          (cell) => cell.template
        )}</div
      >`
  );
}

/**
 * Build merged-cell overlay divs for every {@link MergedRegion} whose
 * top-left cell lies in one quadrant, placed with that quadrant's
 * geometry. Each overlay is a single div positioned at the merge's
 * top-left cell, sized to span the region as far as the quadrant shows it.
 *
 * The CSS class list includes {@code cell col<C1> row<R1> merged-cell}
 * — the {@code merged-cell} class is the TestBench-side contract
 * (see {@code AbstractSpreadsheetIT.mergedCell:567-575}).
 *
 * Every quadrant draws its own: a frozen quadrant's overlays go in its
 * wrapper, so they stay put with its cells. Drawn for the scrolling
 * quadrant alone, a merge in the frozen rows - the title across the top of
 * a report - never appeared, and its first cell showed through instead,
 * clipped at its own column. Merges whose top-left is outside the quadrant
 * are still skipped. Per-cell styling, selection state, and cellData lookup
 * use the top-left cell's data; the merged overlay is treated as a single
 * rendered cell.
 */
export function buildMergedCellOverlays(opts: QuadrantRenderOpts): TemplateResult[] {
  const { state, quadrantName, colStart, colEnd, rowStart, rowEnd } = opts;
  const { getColLeft, getColWidth, getRowTop, getRowHeight } = opts;
  const { cellData, mergedRegions, mergedRegionBoxes, selection } = state;

  const overlays: TemplateResult[] = [];
  for (let index = 0; index < mergedRegions.length; index++) {
    const region = mergedRegions[index];
    const box = mergedRegionBoxes?.[index];
    let left: number;
    let top: number;
    let width = 0;
    let height = 0;
    if (box) {
      // Drawn in the quadrant of its top-left cell, as far as it reaches,
      // for as long as any of it is in view.
      const owner = quadrantOf(region.col1, region.row1, state.horizontalSplitPosition, state.verticalSplitPosition);
      const inView =
        region.col2 >= colStart && region.col1 <= colEnd && region.row2 >= rowStart && region.row1 <= rowEnd;
      if (owner !== quadrantName || !inView) {
        continue;
      }
      ({ left, top, width, height } = box);
    } else {
      if (region.col1 < colStart || region.col1 > colEnd || region.row1 < rowStart || region.row1 > rowEnd) {
        continue;
      }
      left = getColLeft(region.col1);
      top = getRowTop(region.row1);
      // As far as the quadrant shows the region.
      for (let col = region.col1; col <= Math.min(region.col2, colEnd); col++) {
        width += getColWidth(col);
      }
      for (let row = region.row1; row <= Math.min(region.row2, rowEnd); row++) {
        height += getRowHeight(row);
      }
    }

    const key = toKey(region.col1, region.row1);
    const data = cellData.get(key);
    // A merged cell can hold a component or a custom editor just like a
    // plain one, and it is the only element in the region that renders the
    // slot — the cells underneath deliberately leave it out.
    const slotName = (state.cellSlots ?? {})[key];
    const text = slotName === undefined ? data?.value ?? '' : html`<slot name=${slotName}></slot>`;
    // Same default as an ordinary cell - see renderQuadrant.
    const cellStyleClass = data?.cellStyle ?? 'cs0';

    // Selection: a merged cell shows its top-left cell's place in the
    // selection, as a single cell would.
    const selected = selectionClassFor(selection, region.col1, region.row1);

    const classList =
      `cell col${region.col1} row${region.row1} merged-cell` +
      (cellStyleClass ? ` ${cellStyleClass}` : '') +
      selected.classes;
    // The merged cell is drawn over the cells it spans, which are still
    // in the DOM underneath — so it needs a z-index above them, and a
    // ground of its own or their text shows through.
    const style =
      `left:${left}px;top:${top}px;width:${width}px;height:${height}px;z-index:2;` + textColorDeclaration(data);
    // data-col / data-row are what the click delegation reads. Without
    // them a merged region matched `.cell` and then parsed NaN, so
    // every click on one was dropped — a merged cell could not be
    // selected at all. `part` for the same reason a plain cell has it:
    // a merged cell is still a cell to a stylesheet.
    //
    // The markers go on the merged cell: the cells underneath leave theirs
    // out.
    const markers = cellMarkers(state, key, region.col1, region.row1);

    overlays.push(html`
      <div class=${classList} part=${selected.part} style=${style} data-col=${region.col1} data-row=${region.row1}>
        ${markers.invalid}${markers.comment}${text}
      </div>
    `);
  }
  return overlays;
}
