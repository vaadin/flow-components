/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expandToMergedRegions, type MergedRect } from './MergedSelection.js';
import type { CellRect } from './SelectionPaint.js';

/**
 * Where the arrow keys take the selection, by the GWT client's rules.
 *
 * A merged region is one cell to the user: an arrow that lands anywhere in
 * it selects all of it, and the next arrow leaves it from its far side.
 * The cell the arrow landed on is remembered, so going on through the
 * merge comes out in the column (or row) the user came in by. A hidden row
 * or column is no place to stop, so the arrows step over it. Coordinates
 * are 1-based throughout, matching the wire.
 */

/** The four arrow-key directions. */
export type Direction = 'up' | 'down' | 'left' | 'right';

/** A cell position, 1-based. */
export interface CellAt {
  col: number;
  row: number;
}

/** What the arrows need to know about the sheet. */
export interface SheetShape {
  cols: number;
  rows: number;
  mergedRegions: ReadonlyArray<MergedRect>;
  hiddenColumns: ReadonlySet<number>;
  hiddenRows: ReadonlySet<number>;
}

/** A selection of one cell - or of one merged region, which is one cell. */
export interface SingleCellSelection {
  /** The active cell: a merge's top-left cell when it is a merge. */
  activeCell: CellAt;
  /** The cell itself, or the whole merge. */
  activeRange: CellRect;
  /**
   * The cell the selection was asked for when it falls inside a merge;
   * the arrows leave the merge in its column and row. Null otherwise.
   */
  entry: CellAt | null;
}

/** The merge covering a cell, or null when it is an ordinary cell. */
export function mergeAt(regions: ReadonlyArray<MergedRect>, col: number, row: number): MergedRect | null {
  for (const region of regions) {
    if (col >= region.col1 && col <= region.col2 && row >= region.row1 && row <= region.row2) {
      return region;
    }
  }
  return null;
}

/**
 * Select one cell. A cell inside a merge selects the whole merge, with
 * its top-left cell active, as the GWT client did for a click or an arrow.
 */
export function selectCellAt(col: number, row: number, regions: ReadonlyArray<MergedRect>): SingleCellSelection {
  const region = mergeAt(regions, col, row);
  if (region === null) {
    return { activeCell: { col, row }, activeRange: { c1: col, c2: col, r1: row, r2: row }, entry: null };
  }
  return {
    activeCell: { col: region.col1, row: region.row1 },
    activeRange: { c1: region.col1, c2: region.col2, r1: region.row1, r2: region.row2 },
    entry: { col, row }
  };
}

/**
 * Whether a range is nothing but the merge its active cell is in: one cell
 * to the user, so Enter and Tab move on rather than walk around inside it.
 */
export function isSingleMerge(range: CellRect, active: CellAt, regions: ReadonlyArray<MergedRect>): boolean {
  const region = mergeAt(regions, active.col, active.row);
  return (
    region !== null &&
    region.col1 === range.c1 &&
    region.col2 === range.c2 &&
    region.row1 === range.r1 &&
    region.row2 === range.r2
  );
}

/** The next index along one axis that is not hidden, or null past the end. */
function nextShown(from: number, step: 1 | -1, count: number, hidden: ReadonlySet<number>): number | null {
  for (let i = from + step; i >= 1 && i <= count; i += step) {
    if (!hidden.has(i)) {
      return i;
    }
  }
  return null;
}

/**
 * Where an arrow key takes the active cell.
 *
 * Leaving a merge starts from its far side, in the column or row of the
 * cell the merge was entered by - its top-left cell when it was clicked.
 * Returns null when there is nowhere to go: the edge of the sheet, or
 * nothing but hidden rows or columns beyond it.
 */
export function moveActiveCell(
  from: CellAt,
  entry: CellAt | null,
  direction: Direction,
  shape: SheetShape
): SingleCellSelection | null {
  const region = mergeAt(shape.mergedRegions, from.col, from.row);
  const inside = region !== null && entry !== null && mergeAt([region], entry.col, entry.row) !== null;
  const lane = inside ? entry! : from;
  let col: number | null = lane.col;
  let row: number | null = lane.row;
  switch (direction) {
    case 'down':
      row = nextShown(region ? region.row2 : from.row, 1, shape.rows, shape.hiddenRows);
      break;
    case 'up':
      row = nextShown(region ? region.row1 : from.row, -1, shape.rows, shape.hiddenRows);
      break;
    case 'right':
      col = nextShown(region ? region.col2 : from.col, 1, shape.cols, shape.hiddenColumns);
      break;
    case 'left':
      col = nextShown(region ? region.col1 : from.col, -1, shape.cols, shape.hiddenColumns);
      break;
  }
  if (col === null || row === null) {
    return null;
  }
  return selectCellAt(col, row, shape.mergedRegions);
}

/**
 * How Shift+arrow changes the selected range, by the GWT client's rule.
 *
 * The side away from the active cell moves: with the active cell on the
 * left edge, Shift+Right grows the range to the right and Shift+Left
 * takes its right edge back, until the range is one column wide and
 * Shift+Left grows it to the left instead. An active cell that spans the
 * range - one column, or a merge as wide as the range - grows it either
 * way. Hidden rows and columns are stepped over, a range that grows takes
 * in whole merges, and a range that shrinks lets go of whole merges.
 *
 * Returns the new range and the cell on the side that moved, or null when
 * the range cannot move that way.
 */
export function moveRangeEdge(
  range: CellRect,
  active: CellAt,
  direction: Direction,
  shape: SheetShape
): { range: CellRect; moved: CellAt } | null {
  const horizontal = direction === 'left' || direction === 'right';
  const forward = direction === 'right' || direction === 'down';
  const span = mergeAt(shape.mergedRegions, active.col, active.row);
  // Along the axis of travel: the range's two sides, the active cell's
  // extent and what can be seen.
  const low = horizontal ? range.c1 : range.r1;
  const high = horizontal ? range.c2 : range.r2;
  const activeLow = span ? (horizontal ? span.col1 : span.row1) : horizontal ? active.col : active.row;
  const activeHigh = span ? (horizontal ? span.col2 : span.row2) : horizontal ? active.col : active.row;
  const count = horizontal ? shape.cols : shape.rows;
  const hidden = horizontal ? shape.hiddenColumns : shape.hiddenRows;

  const atLow = activeLow <= low;
  const atHigh = activeHigh >= high;
  // Which side moves, and whether it moves out (grow) or in (shrink).
  let side: 'low' | 'high';
  if (forward) {
    side = atLow || !atHigh ? 'high' : 'low';
  } else {
    side = atHigh || !atLow ? 'low' : 'high';
  }
  const grows = forward ? side === 'high' : side === 'low';

  let nextLow = low;
  let nextHigh = high;
  if (grows) {
    const to = nextShown(side === 'high' ? high : low, forward ? 1 : -1, count, hidden);
    if (to === null) {
      return null;
    }
    if (side === 'high') {
      nextHigh = to;
    } else {
      nextLow = to;
    }
  } else if (side === 'high') {
    // Taking the far side back: never past the active cell, and never
    // through a merge - a merge it would cut goes altogether.
    nextHigh = previousShownWithin(high, activeHigh, hidden);
    nextHigh = letGoOfMerges(range, horizontal, 'high', nextHigh, activeHigh, shape.mergedRegions);
  } else {
    nextLow = nextShownWithin(low, activeLow, hidden);
    nextLow = letGoOfMerges(range, horizontal, 'low', nextLow, activeLow, shape.mergedRegions);
  }

  let next: CellRect = horizontal
    ? { c1: nextLow, c2: nextHigh, r1: range.r1, r2: range.r2 }
    : { c1: range.c1, c2: range.c2, r1: nextLow, r2: nextHigh };
  next = expandToMergedRegions(next, shape.mergedRegions);
  if (next.c1 === range.c1 && next.c2 === range.c2 && next.r1 === range.r1 && next.r2 === range.r2) {
    return null;
  }
  const edge = side === 'high' ? (horizontal ? next.c2 : next.r2) : horizontal ? next.c1 : next.r1;
  const moved = horizontal ? { col: edge, row: active.row } : { col: active.col, row: edge };
  return { range: next, moved };
}

/** One step in from the high side, over hidden lines, but not past the stop. */
function previousShownWithin(high: number, stop: number, hidden: ReadonlySet<number>): number {
  let i = high - 1;
  while (i > stop && hidden.has(i)) {
    i--;
  }
  return Math.max(i, stop);
}

/** One step in from the low side, over hidden lines, but not past the stop. */
function nextShownWithin(low: number, stop: number, hidden: ReadonlySet<number>): number {
  let i = low + 1;
  while (i < stop && hidden.has(i)) {
    i++;
  }
  return Math.min(i, stop);
}

/**
 * Pull a shrinking side further in until it cuts no merge, stopping at the
 * active cell's own extent.
 */
function letGoOfMerges(
  range: CellRect,
  horizontal: boolean,
  side: 'low' | 'high',
  edge: number,
  stop: number,
  regions: ReadonlyArray<MergedRect>
): number {
  let at = edge;
  for (let changed = true; changed; ) {
    changed = false;
    for (const region of regions) {
      const crossLow = horizontal ? region.row1 : region.col1;
      const crossHigh = horizontal ? region.row2 : region.col2;
      const rangeCrossLow = horizontal ? range.r1 : range.c1;
      const rangeCrossHigh = horizontal ? range.r2 : range.c2;
      if (crossHigh < rangeCrossLow || crossLow > rangeCrossHigh) {
        continue;
      }
      const from = horizontal ? region.col1 : region.row1;
      const to = horizontal ? region.col2 : region.row2;
      if (side === 'high' && from <= at && to > at) {
        const pulled = Math.max(from - 1, stop);
        if (pulled < at) {
          at = pulled;
          changed = true;
        }
      } else if (side === 'low' && to >= at && from < at) {
        const pulled = Math.min(to + 1, stop);
        if (pulled > at) {
          at = pulled;
          changed = true;
        }
      }
    }
  }
  return at;
}

/** The parts of a key press {@link typesCharacter} looks at. */
export interface TypedKey {
  key: string;
  ctrlKey: boolean;
  metaKey: boolean;
  altKey: boolean;
  isComposing?: boolean;
  getModifierState?(key: string): boolean;
}

/**
 * Whether a key press types a character - which starts editing the cell,
 * Excel's way - rather than giving a command.
 *
 * Ctrl and Cmd give commands. A character typed with AltGr is still a
 * character, though: Windows reports AltGr as Ctrl and Alt together, and
 * German, French or Polish keyboards type @, €, { and } that way. On a Mac
 * Option types characters as well. Alt alone off a Mac types nothing - the
 * GWT client, which started editing on the character the browser produced,
 * did not start on it either. Text still being composed by an IME is not
 * typed yet.
 */
export function typesCharacter(e: TypedKey, mac: boolean): boolean {
  if (e.key.length !== 1 || e.isComposing === true || e.metaKey) {
    return false;
  }
  if (mac) {
    return !e.ctrlKey;
  }
  const altGraph = (e.ctrlKey && e.altKey) || e.getModifierState?.('AltGraph') === true;
  return altGraph || (!e.ctrlKey && !e.altKey);
}
