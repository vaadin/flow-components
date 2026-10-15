/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { CellRect } from './SelectionPaint.js';

/** The four ways the active cell can step inside a selection. */
export type WalkDirection = 'down' | 'up' | 'right' | 'left';

export interface WalkInput {
  /** The selected range the active cell stays inside of. */
  range: CellRect;
  /** Where the active cell is now, 1-based. */
  col: number;
  row: number;
  direction: WalkDirection;
  /** Columns and rows the user cannot see; the walk steps over them. */
  hiddenColumns?: ReadonlySet<number>;
  hiddenRows?: ReadonlySet<number>;
}

/**
 * Whether a range holds more than one cell.
 *
 * Enter and Tab only walk the active cell around inside a selection when
 * there is a selection to walk around in. On a single cell they mean what
 * they always mean: start editing, or move on to the next cell.
 */
export function isMultiCellRange(range: CellRect): boolean {
  return range.c1 !== range.c2 || range.r1 !== range.r2;
}

/** One step, wrapping at the range's edges. Ignores hidden cells. */
function step(col: number, row: number, direction: WalkDirection, range: CellRect): { col: number; row: number } {
  const { c1, c2, r1, r2 } = range;
  switch (direction) {
    case 'down':
      return row < r2 ? { col, row: row + 1 } : { col: col < c2 ? col + 1 : c1, row: r1 };
    case 'up':
      return row > r1 ? { col, row: row - 1 } : { col: col > c1 ? col - 1 : c2, row: r2 };
    case 'right':
      return col < c2 ? { col: col + 1, row } : { col: c1, row: row < r2 ? row + 1 : r1 };
    case 'left':
      return col > c1 ? { col: col - 1, row } : { col: c2, row: row > r1 ? row - 1 : r2 };
  }
}

/**
 * Move the active cell one step inside a selected range.
 *
 * This is what Excel does with a range selected: Enter walks down a column
 * and carries on at the top of the next one, Tab walks along a row and
 * carries on at the start of the next, and both wrap around at the end so
 * the user can keep going through the block. Shift reverses the direction.
 * The selection itself does not move - only the cell inside it does.
 *
 * A cell the user cannot see is not somewhere the active cell can land, so
 * hidden rows and columns are stepped over. If every cell in the range is
 * hidden there is nowhere to go, and the active cell stays where it is.
 *
 * The active cell is assumed to be inside the range; a caller that has just
 * clicked outside it has a single-cell selection anyway.
 */
export function walkInsideSelection(input: WalkInput): { col: number; row: number } {
  const { range, direction, hiddenColumns, hiddenRows } = input;
  const width = range.c2 - range.c1 + 1;
  const height = range.r2 - range.r1 + 1;
  const hidden = (col: number, row: number): boolean =>
    hiddenColumns?.has(col) === true || hiddenRows?.has(row) === true;

  let { col, row } = input;
  // At most one full sweep of the range: every cell is visited once, so a
  // range that is hidden end to end ends the loop instead of spinning.
  for (let taken = 0; taken < width * height; taken++) {
    const next = step(col, row, direction, range);
    col = next.col;
    row = next.row;
    if (!hidden(col, row)) {
      return { col, row };
    }
  }
  return { col: input.col, row: input.row };
}
