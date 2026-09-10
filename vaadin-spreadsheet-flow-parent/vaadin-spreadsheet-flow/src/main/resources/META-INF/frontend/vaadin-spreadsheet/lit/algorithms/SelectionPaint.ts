/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * The fill handle — dragging the square at the bottom-right corner of the
 * selection to copy its values over neighbouring cells, or back over itself
 * to clear them.
 *
 * The geometry is a pure function of the selected range and the cell under
 * the pointer, so it lives here rather than in the connector: the rules are
 * fiddly (the drag locks to whichever axis it favours, and the two
 * directions report to the server through different events) and are worth
 * testing without a DOM.
 *
 * Coordinates are 1-based throughout, matching the wire.
 */

/** An inclusive rectangle of cells. */
export interface CellRect {
  c1: number;
  c2: number;
  r1: number;
  r2: number;
}

/**
 * What a drag to a given cell would do.
 *
 * `none` — the pointer is still on the corner it started from, so there is
 * nothing to paint.
 * `decrease` — the pointer is inside the selection: `painted` is the strip
 * being cut away from it.
 * `increase` — the pointer is outside: `painted` is the strip being added.
 */
export type PaintOutcome =
  | { kind: 'none' }
  | { kind: 'decrease'; painted: CellRect }
  | { kind: 'increase'; painted: CellRect };

/**
 * The strip the fill handle would paint, dragged to {@code (col, row)}.
 *
 * The drag locks to one axis: whichever of the four directions the pointer
 * has travelled furthest in wins, so dragging diagonally down-and-right
 * extends downwards only, as in Excel.
 */
export function computePaintRectangle(selection: CellRect, col: number, row: number): PaintOutcome {
  const { c1, c2, r1, r2 } = selection;

  if (col >= c1 && col <= c2 && row >= r1 && row <= r2) {
    // Inside the selection: dragging back over it takes cells away.
    const vDiff = Math.abs(r2 - row);
    const hDiff = Math.abs(c2 - col);
    if (vDiff === 0 && hDiff === 0) {
      // Still on the corner cell — nothing is being cut.
      return { kind: 'none' };
    }
    if (vDiff > hDiff) {
      return { kind: 'decrease', painted: { c1, c2, r1: Math.max(r1 + 1, r2 - vDiff + 1), r2 } };
    }
    return { kind: 'decrease', painted: { c1: Math.max(c1 + 1, c2 - hDiff + 1), c2, r1, r2 } };
  }

  // Outside: the selection grows towards the pointer.
  const diffDown = row - r2;
  const diffUp = r1 - row;
  const diffLeft = c1 - col;
  const diffRight = col - c2;

  if (Math.max(diffDown, diffUp) > Math.max(diffLeft, diffRight)) {
    return diffDown > diffUp
      ? { kind: 'increase', painted: { c1, c2, r1: r2 + 1, r2: row } }
      : { kind: 'increase', painted: { c1, c2, r1: row, r2: r1 - 1 } };
  }
  return diffRight > diffLeft
    ? { kind: 'increase', painted: { c1: c2 + 1, c2: col, r1, r2 } }
    : { kind: 'increase', painted: { c1: col, c2: c1 - 1, r1, r2 } };
}

/**
 * The rectangle the selection becomes once an `increase` drag is released:
 * the old selection plus the painted strip.
 */
export function grownSelection(selection: CellRect, painted: CellRect): CellRect {
  return {
    c1: Math.min(selection.c1, painted.c1),
    c2: Math.max(selection.c2, painted.c2),
    r1: Math.min(selection.r1, painted.r1),
    r2: Math.max(selection.r2, painted.r2)
  };
}

/** Whether a rectangle covers at least one cell. */
export function isNonEmpty(rect: CellRect): boolean {
  return rect.c1 <= rect.c2 && rect.r1 <= rect.r2;
}
