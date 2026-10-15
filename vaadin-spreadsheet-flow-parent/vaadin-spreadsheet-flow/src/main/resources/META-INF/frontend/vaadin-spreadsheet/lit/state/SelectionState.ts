/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import type {
  CellRangeSelectedData,
  CellSelectedData,
  ColumnSelectedData,
  RowSelectedData,
  SpreadsheetEvent
} from '../types/rpc-client-to-server.js';

/**
 * Pure-state model for the spreadsheet's cell selection.
 *
 * The state is kept in one typed, immutable-ish shape that the renderer and
 * the keyboard handling share.
 *
 * Coordinates are **1-based** throughout, matching the rest of the
 * spreadsheet API. Anchors and ranges follow Excel conventions.
 */

/**
 * A single cell coordinate. Mirrors {@code CellCoord} from
 * `algorithms/CellRef.ts` — re-declared here so this module doesn't
 * depend on the formula-bar algorithms.
 */
export interface CellPosition {
  /** 1-based column index. */
  col: number;
  /** 1-based row index. */
  row: number;
}

/**
 * A rectangular cell range. All 1-based, all inclusive.
 *
 * `r1 <= r2` and `c1 <= c2` are invariants — the state-machine
 * helpers below enforce them whenever a new range is constructed.
 * Callers reading the state can rely on this without re-normalizing.
 */
export interface CellRange {
  /** Top-left column, 1-based, inclusive. */
  c1: number;
  /** Top-left row, 1-based, inclusive. */
  r1: number;
  /** Bottom-right column, 1-based, inclusive. */
  c2: number;
  /** Bottom-right row, 1-based, inclusive. */
  r2: number;
}

/**
 * Complete selection state.
 *
 * Three layered concepts:
 *
 * 1. **The active cell** ({@code activeCell}). The single cell with
 *    keyboard focus, displayed in the formula bar. Always exactly
 *    one. After a range selection, the active cell is one corner of
 *    the range (typically the anchor).
 *
 * 2. **The active range** ({@code activeRange}). The most-recently-
 *    extended range. Always contains {@code activeCell}. For
 *    single-cell selection, equals `{c1: col, c2: col, r1: row,
 *    r2: row}`.
 *
 * 3. **Auxiliary ranges** ({@code auxRanges}). Additional ranges
 *    added via Ctrl/Cmd-click. May be empty.
 *
 * The selection is "coherent" iff {@code auxRanges.length === 0} —
 * only a coherent selection can go on the clipboard, so the
 * copy/paste path checks this before serializing.
 */
export interface SelectionState {
  /** The single cell with keyboard focus + formula-bar display. */
  activeCell: CellPosition;
  /** The most recently extended range; contains {@code activeCell}. */
  activeRange: CellRange;
  /** Additional Ctrl/Cmd-click ranges. Empty array = coherent selection. */
  auxRanges: CellRange[];
}

/**
 * Construct a fresh single-cell selection at {@code (col, row)}. Used
 * as the initial state on spreadsheet mount and after a sheet change.
 */
export function selectSingleCell(col: number, row: number): SelectionState {
  const cell: CellPosition = { col, row };
  return {
    activeCell: cell,
    activeRange: { c1: col, r1: row, c2: col, r2: row },
    auxRanges: []
  };
}

/**
 * Build a {@code CellRange} with normalized corners.
 */
export function normalizedRange(c1: number, r1: number, c2: number, r2: number): CellRange {
  return {
    c1: Math.min(c1, c2),
    c2: Math.max(c1, c2),
    r1: Math.min(r1, r2),
    r2: Math.max(r1, r2)
  };
}

/**
 * Returns {@code true} iff the selection is coherent (single range,
 * no Ctrl-click auxiliary ranges). The clipboard serializer in
 * {@code algorithms/Tsv.ts} only accepts coherent selections.
 */
export function isCoherentSelection(state: SelectionState): boolean {
  return state.auxRanges.length === 0;
}

/**
 * Add a new auxiliary range to the selection (Ctrl/Cmd-click case).
 * The active range and active cell shift to the new range — matches
 * Excel's "Ctrl-click moves focus" behavior.
 */
export function addAuxRange(state: SelectionState, c1: number, r1: number, c2: number, r2: number): SelectionState {
  const newActive = normalizedRange(c1, r1, c2, r2);
  return {
    activeCell: { col: c1, row: r1 },
    activeRange: newActive,
    // The old active range becomes an aux range — joining the
    // previous aux list.
    auxRanges: [...state.auxRanges, state.activeRange]
  };
}

/**
 * Returns {@code true} iff the cell at {@code (col, row)} is part of
 * the selection (active range OR any auxiliary range). Used by the
 * cell renderer to apply selection styling.
 */
export function isCellSelected(state: SelectionState, col: number, row: number): boolean {
  if (containsCell(state.activeRange, col, row)) {
    return true;
  }
  for (const range of state.auxRanges) {
    if (containsCell(range, col, row)) {
      return true;
    }
  }
  return false;
}

/**
 * Returns {@code true} iff the cell at {@code (col, row)} is the
 * single active cell (the one with keyboard focus).
 */
export function isActiveCell(state: SelectionState, col: number, row: number): boolean {
  return state.activeCell.col === col && state.activeCell.row === row;
}

/**
 * Does {@code range} contain the cell at {@code (col, row)}? Inclusive
 * on all four corners.
 */
export function containsCell(range: CellRange, col: number, row: number): boolean {
  return col >= range.c1 && col <= range.c2 && row >= range.r1 && row <= range.r2;
}

/**
 * Pure helper: return the set of column indices that intersect
 * the active range of the selection, fed to the
 * column-header element so each cell whose column is inside the
 * active range emits {@code part="column-header header-selected"}.
 */
export function selectedColumnSet(selection: SelectionState): Set<number> {
  // Every range's columns, not only the last one added.
  const set = new Set<number>();
  for (const range of [selection.activeRange, ...selection.auxRanges]) {
    for (let c = Math.min(range.c1, range.c2); c <= Math.max(range.c1, range.c2); c++) {
      set.add(c);
    }
  }
  return set;
}

/**
 * Pure helper: symmetric row variant of {@link selectedColumnSet}.
 */
export function selectedRowSet(selection: SelectionState): Set<number> {
  const set = new Set<number>();
  for (const range of [selection.activeRange, ...selection.auxRanges]) {
    for (let r = Math.min(range.r1, range.r2); r <= Math.max(range.r1, range.r2); r++) {
      set.add(r);
    }
  }
  return set;
}

/**
 * Pure helper: is the given (col, row) inside the active range OR
 * any aux range of the current selection? A right-click outside the
 * selection moves the selection to the clicked cell, as in Excel.
 */
export function isCellInSelection(col: number, row: number, selection: SelectionState): boolean {
  const inRange = (range: CellRange): boolean => {
    const cMin = Math.min(range.c1, range.c2);
    const cMax = Math.max(range.c1, range.c2);
    const rMin = Math.min(range.r1, range.r2);
    const rMax = Math.max(range.r1, range.r2);
    return col >= cMin && col <= cMax && row >= rMin && row <= rMax;
  };
  if (inRange(selection.activeRange)) {
    return true;
  }
  for (const aux of selection.auxRanges) {
    if (inRange(aux)) {
      return true;
    }
  }
  return false;
}

/**
 * How Excel's name box counts a block being selected: rows first, then
 * columns, as in "3R x 3C".
 */
export function selectionSizeLabel(range: CellRange): string {
  const rows = Math.abs(range.r2 - range.r1) + 1;
  const cols = Math.abs(range.c2 - range.c1) + 1;
  return `${rows}R x ${cols}C`;
}

/**
 * Pure helper: classify a selection shape and build the matching
 * server-bound {@link SpreadsheetEvent}.
 *
 * Detection order (specific → general):
 *   1. whole column: r1=1, r2=rows, c1=c2 → {@code columnSelected}
 *   2. whole row:    c1=1, c2=cols, r1=r2 → {@code rowSelected}
 *   3. single cell:  c1=c2, r1=r2         → {@code cellSelected}
 *   4. otherwise:                          → {@code cellRangeSelected}
 *
 * Note: when `rows`/`cols` is 0 (uninitialized grid) the "whole
 * column / row" branches collapse to cellSelected since the
 * full-axis match can't succeed against a zero-width axis.
 */
export function buildSelectionEvent(
  activeCell: { col: number; row: number },
  range: CellRange,
  rows: number,
  cols: number
): SpreadsheetEvent {
  const isSingleCol = range.c1 === range.c2;
  const isSingleRow = range.r1 === range.r2;
  const isWholeCol = cols > 0 && rows > 0 && isSingleCol && range.r1 === 1 && range.r2 === rows;
  const isWholeRow = cols > 0 && rows > 0 && isSingleRow && range.c1 === 1 && range.c2 === cols;

  if (isWholeCol) {
    const data: ColumnSelectedData = {
      column: range.c1,
      firstRowIndex: range.r1
    };
    return { type: 'columnSelected', data };
  }
  if (isWholeRow) {
    const data: RowSelectedData = {
      row: range.r1,
      firstColumnIndex: range.c1
    };
    return { type: 'rowSelected', data };
  }
  if (isSingleCol && isSingleRow) {
    const data: CellSelectedData = {
      row: activeCell.row,
      column: activeCell.col,
      // A plain selection replaces every earlier range;
      // Ctrl-click sends events of its own.
      oldSelectionRangeDiscarded: true
    };
    return { type: 'cellSelected', data };
  }
  const data: CellRangeSelectedData = {
    row1: range.r1,
    col1: range.c1,
    row2: range.r2,
    col2: range.c2
  };
  return { type: 'cellRangeSelected', data };
}
