/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * How far a cell's text may spill into the cells beside it.
 *
 * A spreadsheet does not clip text at the column edge the way a table does.
 * Text too wide for its column runs on across the cells to its right for as
 * long as they are empty, and stops at the first one holding anything. That
 * is Excel's behaviour and the GWT client's; the Lit client clipped every
 * cell at its own width, so anything wider than its column was simply cut.
 *
 * The rules, kept as the GWT client had them:
 *
 * - A right-aligned cell does not spill. Excel spills it leftwards, but the
 *   GWT client never did, and matching it is the parity this branch is for.
 * - Spilling stops at the first column holding a value.
 * - Spilling stops at the frozen/scrolling split. The two sides are drawn in
 *   separate quadrants, so text crossing the split would be cut by the
 *   quadrant edge and reappear on the wrong side when the sheet scrolls.
 * - Spilling stops at the last column it was given widths for. Anything past
 *   that is outside the rendered viewport and clipped anyway.
 */
export interface CellOverflowInput {
  /** The cell's own column, 1-based. */
  col: number;
  /** Pixel width of the cell's own column. */
  columnWidth: number;
  /** Pixel width the cell's content wants — its measured scrollWidth. */
  contentWidth: number;
  /** Right-aligned cells stay inside their column. */
  rightAligned: boolean;
  /** Pixel width per 1-based column. Columns outside it end the spill. */
  columnWidths: ReadonlyMap<number, number>;
  /** Columns of this row that hold a value. The spill stops at the first. */
  occupiedColumns: ReadonlySet<number>;
  /** Frozen column count; 0 when the sheet has no column freeze. */
  horizontalSplitPosition: number;
}

/**
 * The width to give the cell's content so its text spills into the empty
 * columns beside it, or {@code null} when it fits and should be left alone.
 *
 * The returned width covers the cell's own column plus every empty column
 * the text reaches into, so the caller can size one element and let the
 * browser clip the remainder.
 */
export function overflowWidth(input: CellOverflowInput): number | null {
  const { col, columnWidth, contentWidth, rightAligned, columnWidths, occupiedColumns, horizontalSplitPosition } =
    input;

  if (rightAligned) {
    return null;
  }
  const overshoot = contentWidth - columnWidth;
  if (overshoot <= 0) {
    return null;
  }

  // The two pixels are the cell's left padding, which the measured content
  // width does not include but the spill has to clear.
  const wanted = overshoot + 2;
  const startsFrozen = isFrozen(col, horizontalSplitPosition);

  let gained = 0;
  let reached = col;
  while (gained < wanted) {
    const next = reached + 1;
    if (startsFrozen !== isFrozen(next, horizontalSplitPosition)) {
      break;
    }
    if (occupiedColumns.has(next)) {
      break;
    }
    const width = columnWidths.get(next);
    if (width === undefined) {
      break;
    }
    gained += width;
    reached = next;
  }

  if (reached === col) {
    // Nowhere to go: the neighbour is taken, frozen off, or off the edge.
    return null;
  }
  return columnWidth + gained;
}

function isFrozen(col: number, horizontalSplitPosition: number): boolean {
  return col <= horizontalSplitPosition;
}
