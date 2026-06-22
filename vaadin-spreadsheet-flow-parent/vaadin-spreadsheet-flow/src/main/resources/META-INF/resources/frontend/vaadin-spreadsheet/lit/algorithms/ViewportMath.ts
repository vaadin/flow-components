/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Pure-algorithm port of the GWT client's coordinate / viewport math.
 *
 * Ported from `SheetWidget.java` in `vaadin-spreadsheet-flow-client`:
 *  - `convertPointsToPixel` / `convertPixelsToPoint` (lines 4505-4518)
 *  - `getDefaultRowHeight` (lines 4486-4503)
 *  - `getRowHeight` (lines 4472-4480)
 *  - `calculateLeftValueOfScrolledColumns` (lines 2312-2318)
 *  - `calculateTopValueOfScrolledRows` (lines 2320-2326)
 *  - the pure portion of `resetScrollView` (lines 2851-2910)
 *
 * "Pure" means: no DOM access, no side effects. Inputs are the sheet's
 * geometric state; outputs are derived numbers. The caller (the Lit
 * Spreadsheet class) is responsible for sourcing the inputs from the
 * current scroll position + sheet model, and for applying the outputs
 * to the rendered cells.
 *
 * Coordinates are 1-based (matching Apache POI + the GWT client +
 * `SelectionState.ts`). Pixel offsets are 0-based.
 */

/** Pixels-per-inch — used to convert Excel's point-based row heights to pixels. */
export const FALLBACK_PPI = 96;

/**
 * Convert points (Excel's row-height unit, 1/72 inch) to integer pixels.
 *
 * Mirrors `SheetWidget.convertPointsToPixel(float points)`. The GWT
 * version uses `BigDecimal.intValue()` (truncation toward zero); we
 * mirror that with `Math.trunc` rather than `Math.round` to preserve
 * byte-equivalent output.
 */
export function convertPointsToPixel(points: number, ppi: number): number {
    return Math.trunc((points * ppi) / 72);
}

/**
 * Inverse of {@link convertPointsToPixel}. Returns a float (no truncation).
 * Mirrors `SheetWidget.convertPixelsToPoint(int pixels)`.
 */
export function convertPixelsToPoint(pixels: number, ppi: number): number {
    return (pixels / ppi) * 72;
}

/**
 * Default per-row height in pixels, converted from POI's points-based
 * `defaultRowHeight` using the supplied PPI.
 *
 * Mirrors `SheetWidget.getDefaultRowHeight()` minus the lazy
 * DOM-measurement of PPI (the Lit caller measures PPI explicitly and
 * passes it in).
 */
export function getDefaultRowHeightPx(
    defaultRowHeightPoints: number,
    ppi: number
): number {
    const effectivePpi = ppi > 0 ? ppi : FALLBACK_PPI;
    return Math.trunc((defaultRowHeightPoints * effectivePpi) / 72);
}

/**
 * Per-row height in pixels, accounting for hidden rows and rows whose
 * index is beyond the explicitly defined heights.
 *
 * Mirrors `SheetWidget.getRowHeight(int row)` byte-equivalent. Note
 * the GWT check is `row >= definedRowHeights.length`, NOT `>`. This
 * means a length-N array provides heights for rows 1..N-1, and row N
 * itself falls back to the default. Preserved verbatim for behavior
 * equivalence; the GWT-side array is presumably sized N+1 to make
 * this work in practice, but documenting the quirk here as an open
 * question for review.
 *
 * @param row1Based 1-based row index (POI/GWT convention)
 * @param definedRowHeightsPx Pre-computed pixel heights for rows
 *   1..length-1 (0-indexed in the array). Row index equal to
 *   `length` and above uses `defaultRowHeightPx`.
 * @param hiddenRows Set of 1-based row indices that are hidden.
 *   Hidden rows return 0 regardless of their defined height.
 * @param defaultRowHeightPx Fallback height.
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
    if (row1Based >= definedRowHeightsPx.length) {
        return defaultRowHeightPx;
    }
    return definedRowHeightsPx[row1Based - 1];
}

/**
 * Sum of column widths between the horizontal freeze-pane split and
 * the first visible (non-frozen) column. Used to compute the visual
 * offset of the scrolled pane relative to the frozen pane.
 *
 * Mirrors `SheetWidget.calculateLeftValueOfScrolledColumns()`. The
 * caller provides `getColWidth` (the actionHandler analogue) and the
 * current `horizontalSplitPosition` + `firstColumnIndex`.
 *
 * `horizontalSplitPosition` is 0 when no freeze is active. In that
 * case the loop's `i = 1` start scans from column 1.
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

/**
 * Symmetric row variant of {@link calculateLeftValueOfScrolledColumns}.
 * Mirrors `SheetWidget.calculateTopValueOfScrolledRows()`.
 */
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
     * Mirrors `actionHandler.getDefinedRows()`.
     */
    definedRows: number;
    /** Default per-row height in pixels (see {@link getDefaultRowHeightPx}). */
    defaultRowHeightPx: number;
    /**
     * Per-column width getter. Mirrors `actionHandler.getColWidthActual`
     * — already account for hidden columns (returning 0).
     */
    getColWidthActual: (col1Based: number) => number;
    /**
     * Per-row height getter for rows within `definedRows`. Mirrors
     * `SheetWidget.getRowHeight(int row)` — must return 0 for hidden
     * rows.
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
 * Mirrors the pure portion of `SheetWidget.resetScrollView(int, int)`:
 *  1. Start at column/row 1 (or one past the freeze split).
 *  2. Advance the "first" index until its accumulated pixel position
 *     reaches `scroll - buffer` (so the first rendered cell is within
 *     the buffer zone above/left of the viewport).
 *  3. Advance the "last" index from the first index until its
 *     accumulated position covers `scroll + viewport + buffer`.
 *
 * The row branch has an extra wrinkle: rows beyond `definedRows` use
 * `defaultRowHeightPx` directly (skipping the `getRowHeight` hidden
 * check). This mirrors `SheetWidget` exactly.
 */
export function computeViewportBounds(input: ViewportInput): ViewportBounds {
    let firstRowIndex = input.verticalSplitPosition > 0
        ? input.verticalSplitPosition + 1
        : 1;
    let firstRowPosition = 0;
    let firstColumnIndex = input.horizontalSplitPosition > 0
        ? input.horizontalSplitPosition + 1
        : 1;
    let firstColumnPosition = 0;

    while (firstColumnPosition < (input.scrollLeft - input.columnBufferSize)) {
        firstColumnPosition += input.getColWidthActual(firstColumnIndex);
        firstColumnIndex++;
    }
    let lastColumnIndex = firstColumnIndex;
    let lastColumnPosition = firstColumnPosition
        + input.getColWidthActual(firstColumnIndex);

    while (firstRowPosition < (input.scrollTop - input.rowBufferSize)) {
        firstRowPosition += firstRowIndex >= input.definedRows
            ? input.defaultRowHeightPx
            : input.getRowHeight(firstRowIndex);
        firstRowIndex++;
    }
    let lastRowIndex = firstRowIndex;
    let lastRowPosition = firstRowPosition + input.getRowHeight(lastRowIndex);

    while (lastColumnPosition < (input.scrollLeft + input.scrollViewWidth
            + input.columnBufferSize)
            && lastColumnIndex < input.maxColumns) {
        lastColumnIndex++;
        lastColumnPosition += input.getColWidthActual(lastColumnIndex);
    }

    while (lastRowPosition < (input.scrollTop + input.scrollViewHeight
            + input.rowBufferSize)
            && lastRowIndex < input.maxRows) {
        lastRowIndex++;
        lastRowPosition += lastRowIndex >= input.definedRows
            ? input.defaultRowHeightPx
            : input.getRowHeight(lastRowIndex);
    }

    return {
        firstColumnIndex,
        firstColumnPosition,
        lastColumnIndex,
        lastColumnPosition,
        firstRowIndex,
        firstRowPosition,
        lastRowIndex,
        lastRowPosition,
    };
}
