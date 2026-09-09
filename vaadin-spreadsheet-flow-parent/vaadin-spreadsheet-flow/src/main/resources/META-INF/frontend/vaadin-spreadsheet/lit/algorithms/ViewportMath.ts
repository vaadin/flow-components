/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Pure-algorithm coordinate / viewport math for the spreadsheet
 * client.
 *
 * "Pure" means: no DOM access, no side effects. Inputs are the sheet's
 * geometric state; outputs are derived numbers. The caller (the Lit
 * Spreadsheet class) is responsible for sourcing the inputs from the
 * current scroll position + sheet model, and for applying the outputs
 * to the rendered cells.
 *
 * Behavior contract:
 *
 * - Coordinates are 1-based (matching Apache POI + Excel's A1
 *   convention). Pixel offsets are 0-based.
 * - Row heights are stored as points (Excel's unit, 1/72 inch) and
 *   converted to pixels using a supplied PPI. Pixel values are
 *   TRUNCATED, not rounded — fractional pixels render
 *   inconsistently across browsers + DPRs.
 * - Hidden rows / columns contribute 0 to all height / width sums.
 * - Frozen panes shift the visible-range start by the split position
 *   (rows 1..verticalSplitPosition stay pinned).
 *
 * The new Spreadsheet class composes these helpers in its render +
 * scroll-handler paths.
 */

/** Pixels-per-inch — used to convert Excel's point-based row heights to pixels. */
export const FALLBACK_PPI = 96;

/**
 * Convert points (Excel's row-height unit, 1/72 inch) to integer pixels.
 *
 * Truncate toward zero rather than rounding. Fractional pixels would
 * render inconsistently across browsers and devicePixelRatios — every
 * spreadsheet implementation that targets visual stability uses
 * truncation here. (Excel itself does the same.)
 */
export function convertPointsToPixel(points: number, ppi: number): number {
  return Math.trunc((points * ppi) / 72);
}

/** Inverse of {@link convertPointsToPixel}. Returns a float (no truncation). */
export function convertPixelsToPoint(pixels: number, ppi: number): number {
  return (pixels / ppi) * 72;
}

/**
 * Default per-row height in pixels, converted from POI's points-based
 * `defaultRowHeight` using the supplied PPI. When the caller can't
 * measure PPI (early in attach), pass 0 — the 96-PPI fallback applies.
 */
export function getDefaultRowHeightPx(defaultRowHeightPoints: number, ppi: number): number {
  const effectivePpi = ppi > 0 ? ppi : FALLBACK_PPI;
  return Math.trunc((defaultRowHeightPoints * effectivePpi) / 72);
}

/**
 * Per-row height in pixels, accounting for hidden rows and rows whose
 * index is beyond the explicitly defined heights.
 *
 * @param row1Based 1-based row index (Apache POI / A1-notation convention).
 * @param definedRowHeightsPx Pre-computed pixel heights for rows
 *   1..length (0-indexed in the array — element 0 corresponds to
 *   row 1, element length-1 to row length). Rows beyond the array
 *   use {@code defaultRowHeightPx}.
 * @param hiddenRows Set of 1-based row indices that are hidden.
 *   Hidden rows return 0 regardless of their defined height.
 * @param defaultRowHeightPx Fallback height for rows beyond the array.
 */
export function getRowHeightPx(
  row1Based: number,
  definedRowHeightsPx: number[],
  hiddenRows: ReadonlySet<number>,
  defaultRowHeightPx: number
): number {
  if (hiddenRows.has(row1Based)) {
    return 0;
  }
  if (row1Based > definedRowHeightsPx.length) {
    return defaultRowHeightPx;
  }
  return definedRowHeightsPx[row1Based - 1];
}

/**
 * Sum of column widths between the horizontal freeze-pane split and
 * the first visible (non-frozen) column. Used to compute the visual
 * offset of the scrolled pane relative to the frozen pane.
 *
 * `horizontalSplitPosition` is 0 when no freeze is active — the loop
 * sums column widths starting at column 1, which is the natural origin.
 */
export function calculateLeftValueOfScrolledColumns(
  horizontalSplitPosition: number,
  firstColumnIndex: number,
  getColWidth: (col1Based: number) => number
): number {
  let left = 0;
  for (let i = horizontalSplitPosition + 1; i < firstColumnIndex; i++) {
    left += getColWidth(i);
  }
  return left;
}

/** Symmetric row variant of {@link calculateLeftValueOfScrolledColumns}. */
export function calculateTopValueOfScrolledRows(
  verticalSplitPosition: number,
  firstRowIndex: number,
  getRowHeightForIndex: (row1Based: number) => number
): number {
  let top = 0;
  for (let i = verticalSplitPosition + 1; i < firstRowIndex; i++) {
    top += getRowHeightForIndex(i);
  }
  return top;
}

/** Inputs for {@link computeViewportBounds}. */
export interface ViewportInput {
  /** Current horizontal scroll offset of the sheet's scroll pane, in pixels. */
  scrollLeft: number;
  /** Current vertical scroll offset, in pixels. */
  scrollTop: number;
  /** Viewport width in pixels (the sheet's offsetWidth). */
  scrollViewWidth: number;
  /** Viewport height in pixels (the sheet's offsetHeight). */
  scrollViewHeight: number;
  /**
   * Index (1-based) of the last frozen column. 0 when no horizontal
   * freeze is active. Visible columns start at
   * `horizontalSplitPosition + 1` after the freeze.
   */
  horizontalSplitPosition: number;
  /** Symmetric row variant. */
  verticalSplitPosition: number;
  /** Pixel buffer used to render slightly beyond the viewport for smoother scrolling. */
  columnBufferSize: number;
  /** Pixel buffer on the row axis. */
  rowBufferSize: number;
  /** Total number of columns in the active sheet. */
  maxColumns: number;
  /** Total number of rows in the active sheet. */
  maxRows: number;
  /**
   * Index (1-based, exclusive) past which row heights aren't explicitly
   * defined — beyond this point, the default row height is assumed.
   */
  definedRows: number;
  /** Default per-row height in pixels (see {@link getDefaultRowHeightPx}). */
  defaultRowHeightPx: number;
  /**
   * Per-column width getter. Must already account for hidden
   * columns (returning 0 for them).
   */
  getColWidthActual: (col1Based: number) => number;
  /**
   * Per-row height getter for rows within `definedRows`. Must
   * return 0 for hidden rows.
   */
  getRowHeight: (row1Based: number) => number;
}

/**
 * Output of {@link computeViewportBounds}. The four "index" values are
 * 1-based; the four "position" values are pixel offsets from the
 * sheet's logical origin (col 1 / row 1).
 */
export interface ViewportBounds {
  firstColumnIndex: number;
  firstColumnPosition: number;
  lastColumnIndex: number;
  lastColumnPosition: number;
  firstRowIndex: number;
  firstRowPosition: number;
  lastRowIndex: number;
  lastRowPosition: number;
}

/**
 * Core virtual-scrolling math. Given the current scroll state plus the
 * sheet's row/column geometry, return the index range that should be
 * rendered. The caller decides what to do with the range (mount cells,
 * recycle, etc.).
 *
 * Algorithm:
 *  1. Start at column/row 1 (or one past the freeze split).
 *  2. Advance the "first" index until its accumulated pixel position
 *     reaches `scroll - buffer` (so the first rendered cell is within
 *     the buffer zone above/left of the viewport).
 *  3. Advance the "last" index from the first index until its
 *     accumulated position covers `scroll + viewport + buffer`.
 *
 * The row branch has an extra wrinkle: rows beyond `definedRows` use
 * `defaultRowHeightPx` directly (skipping the `getRowHeight` hidden
 * check), because the per-row hidden-set is only populated for rows
 * within the defined range.
 */
export function computeViewportBounds(input: ViewportInput): ViewportBounds {
  let firstRowIndex = input.verticalSplitPosition > 0 ? input.verticalSplitPosition + 1 : 1;
  let firstRowPosition = 0;
  let firstColumnIndex = input.horizontalSplitPosition > 0 ? input.horizontalSplitPosition + 1 : 1;
  let firstColumnPosition = 0;

  while (firstColumnPosition < input.scrollLeft - input.columnBufferSize) {
    firstColumnPosition += input.getColWidthActual(firstColumnIndex);
    firstColumnIndex++;
  }
  let lastColumnIndex = firstColumnIndex;
  let lastColumnPosition = firstColumnPosition + input.getColWidthActual(firstColumnIndex);

  while (firstRowPosition < input.scrollTop - input.rowBufferSize) {
    firstRowPosition +=
      firstRowIndex >= input.definedRows ? input.defaultRowHeightPx : input.getRowHeight(firstRowIndex);
    firstRowIndex++;
  }
  let lastRowIndex = firstRowIndex;
  let lastRowPosition = firstRowPosition + input.getRowHeight(lastRowIndex);

  while (
    lastColumnPosition < input.scrollLeft + input.scrollViewWidth + input.columnBufferSize &&
    lastColumnIndex < input.maxColumns
  ) {
    lastColumnIndex++;
    lastColumnPosition += input.getColWidthActual(lastColumnIndex);
  }

  while (
    lastRowPosition < input.scrollTop + input.scrollViewHeight + input.rowBufferSize &&
    lastRowIndex < input.maxRows
  ) {
    lastRowIndex++;
    lastRowPosition += lastRowIndex >= input.definedRows ? input.defaultRowHeightPx : input.getRowHeight(lastRowIndex);
  }

  return {
    firstColumnIndex,
    firstColumnPosition,
    lastColumnIndex,
    lastColumnPosition,
    firstRowIndex,
    firstRowPosition,
    lastRowIndex,
    lastRowPosition
  };
}

/** Inputs for {@link computeTotalGridSize}. */
export interface TotalGridSizeInput {
  /** Total number of rows in the sheet (1-based count). */
  rows: number;
  /** Total number of columns in the sheet (1-based count). */
  cols: number;
  /**
   * Per-row pixel heights, indexed by {@code row - 1}. Rows beyond
   * the array use {@link defaultRowHeightPx}.
   */
  definedRowHeightsPx: ReadonlyArray<number>;
  /** Per-column pixel widths, indexed by {@code col - 1}. */
  definedColWidthsPx: ReadonlyArray<number>;
  /** 1-based row indices that are hidden — contribute 0 to total height. */
  hiddenRows: ReadonlySet<number>;
  /** 1-based column indices that are hidden — contribute 0 to total width. */
  hiddenColumns: ReadonlySet<number>;
  /** Pixel height for rows beyond {@link definedRowHeightsPx}. */
  defaultRowHeightPx: number;
  /** Pixel width for columns beyond {@link definedColWidthsPx}. */
  defaultColWidthPx: number;
}

/**
 * Total pixel dimensions of the entire virtual grid. Used by the
 * connector to size the scroll surface so the host's scrollbar tracks
 * reflect actual scroll extent.
 *
 * Hidden rows/columns contribute 0. Out-of-range indices use the
 * defaults — matches {@link getRowHeightPx} behavior so the scroll
 * extent and the per-cell positions stay consistent.
 */
export function computeTotalGridSize(input: TotalGridSizeInput): {
  totalWidthPx: number;
  totalHeightPx: number;
} {
  let totalWidthPx = 0;
  for (let col = 1; col <= input.cols; col++) {
    if (input.hiddenColumns.has(col)) {
      continue;
    }
    totalWidthPx += col > input.definedColWidthsPx.length ? input.defaultColWidthPx : input.definedColWidthsPx[col - 1];
  }
  let totalHeightPx = 0;
  for (let row = 1; row <= input.rows; row++) {
    if (input.hiddenRows.has(row)) {
      continue;
    }
    totalHeightPx +=
      row > input.definedRowHeightsPx.length ? input.defaultRowHeightPx : input.definedRowHeightsPx[row - 1];
  }
  return { totalWidthPx, totalHeightPx };
}
