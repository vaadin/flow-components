/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Pure-state model for the spreadsheet's cell selection.
 *
 * The GWT side scattered selection state across `SheetWidget` (read
 * accessors) and `SelectionHandler` (mutation methods) — slice 116's
 * survey identified subsystem C (selection, 2,600 LOC) as the third-
 * largest piece. This TS module pulls the STATE part out into a typed,
 * immutable-ish shape so the Lit renderer and the keyboard navigator
 * (coming in Phase 3.2 cont.) work against an explicit contract.
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
 * only the GWT clipboard format supports coherent selections, so
 * the copy/paste path checks this before serializing.
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
 * Construct a range selection from two corner coordinates. Corners may
 * be in any order; the result has the corner invariants enforced
 * ({@code c1 <= c2}, {@code r1 <= r2}). The active cell becomes the
 * FIRST corner — matches Excel's behavior when shift-clicking to
 * extend a selection.
 */
export function selectRange(
  anchorCol: number,
  anchorRow: number,
  extendCol: number,
  extendRow: number
): SelectionState {
  return {
    activeCell: { col: anchorCol, row: anchorRow },
    activeRange: normalizedRange(anchorCol, anchorRow, extendCol, extendRow),
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
 * Iterate the cells in a {@code CellRange} in row-major order. Used by
 * the clipboard serializer and the selection-fill logic.
 */
export function* iterateRange(range: CellRange): Generator<CellPosition> {
  for (let row = range.r1; row <= range.r2; row++) {
    for (let col = range.c1; col <= range.c2; col++) {
      yield { col, row };
    }
  }
}

/**
 * Returns the smallest range that contains all of the selection's
 * ranges (active + auxiliaries). Used by status-bar count + the
 * "select all" extension logic.
 */
export function selectionBoundingBox(state: SelectionState): CellRange {
  let { c1, r1, c2, r2 } = state.activeRange;
  for (const range of state.auxRanges) {
    c1 = Math.min(c1, range.c1);
    r1 = Math.min(r1, range.r1);
    c2 = Math.max(c2, range.c2);
    r2 = Math.max(r2, range.r2);
  }
  return { c1, r1, c2, r2 };
}
