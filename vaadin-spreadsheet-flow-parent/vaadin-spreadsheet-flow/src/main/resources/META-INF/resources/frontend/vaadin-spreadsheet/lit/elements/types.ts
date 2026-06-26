/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { ViewportBounds } from '../algorithms/ViewportMath.js';
import type { SelectionState } from '../state/SelectionState.js';
import type { CellData, MergedRegion } from '../types/wire.js';

/**
 * Input state for the {@link SpreadsheetCellGrid} element. The
 * Spreadsheet connector builds a fresh instance on every render
 * cycle (scroll, selection change, data update) and assigns it to
 * the element's {@code renderState} property.
 *
 * Naming + structure follow the {@code renderState} convention used
 * by other Vaadin Flow components (see vaadin-grid-flow's
 * {@code gridConnector.ts}): single object input rather than dozens
 * of individual property setters.
 *
 * All coordinates are 1-based (the codebase-wide convention). Pixel
 * positions are 0-based and measured from the cell-grid's top-left
 * corner.
 */
export interface CellGridRenderState {
    /**
     * Which rows + columns are currently visible (output of
     * {@code computeViewportBounds}). The cell-grid iterates
     * {@code firstColumnIndex..lastColumnIndex} × {@code firstRowIndex
     * ..lastRowIndex} and renders one cell per coordinate.
     */
    viewport: ViewportBounds;

    /**
     * Cell content keyed by {@code toKey(col, row)} (the "col1 row1"
     * wire format from {@code CellKeys.toKey}). Coordinates without
     * an entry render as empty cells.
     */
    cellData: ReadonlyMap<string, CellData>;

    /**
     * Pixel x-offsets for visible columns, indexed by
     * {@code col - viewport.firstColumnIndex}. Pixel widths are the
     * differences between consecutive offsets, except for the last
     * column where the width comes from {@code colWidths}.
     */
    colX: ReadonlyArray<number>;

    /**
     * Pixel y-offsets for visible rows, indexed by
     * {@code row - viewport.firstRowIndex}. Pixel heights are the
     * differences between consecutive offsets, except for the last
     * row where the height comes from {@code rowHeights}.
     */
    rowY: ReadonlyArray<number>;

    /** Pixel width of each visible column, same indexing as {@code colX}. */
    colWidths: ReadonlyArray<number>;

    /** Pixel height of each visible row, same indexing as {@code rowY}. */
    rowHeights: ReadonlyArray<number>;

    /**
     * Style-index → CSS-rule-body lookup. Each rendered cell carries
     * its style class (e.g. {@code "cs0"}) on its element; the
     * cell-grid emits a {@code <style>} block in its shadow DOM with
     * rules of the form {@code .cell.cs<N> { <body> }}. The body is
     * everything between the braces — declarations like
     * {@code "font-weight:bold;color:red;"}.
     *
     * Keys are integer style indices (Excel's per-cell-style table is
     * indexed). The {@code cellStyle} field on {@link CellData} stores
     * the corresponding {@code "cs<N>"} string.
     */
    cellStyleToCSSStyle: Readonly<Record<number, string>>;

    /**
     * Merged regions that overlap the visible viewport. Slice 137 will
     * render these as larger cells overlaying the underlying single-
     * cell grid; slice 132's minimal render ignores them.
     */
    mergedRegions: ReadonlyArray<MergedRegion>;

    /**
     * Current selection state. Slice 136 will render selection
     * overlays; slice 132's minimal render ignores it.
     */
    selection: SelectionState;

    /**
     * Number of frozen rows at the top of the sheet (1-based count;
     * 0 = no row freeze). Rows {@code 1..verticalSplitPosition} render
     * in the top-row + corner quadrants and don't scroll vertically;
     * the main viewport's {@code firstRowIndex} starts above this
     * count.
     */
    verticalSplitPosition: number;

    /**
     * Symmetric column variant of {@link verticalSplitPosition}.
     * {@code 0} = no column freeze.
     */
    horizontalSplitPosition: number;

    /**
     * Pixel x-offsets for the frozen columns (cols
     * {@code 1..horizontalSplitPosition}). Indexed by
     * {@code col - 1} (col 1 → index 0). Empty when no column
     * freeze is active.
     */
    frozenColX: ReadonlyArray<number>;

    /** Pixel widths for the frozen columns, same indexing as {@link frozenColX}. */
    frozenColWidths: ReadonlyArray<number>;

    /** Pixel y-offsets for the frozen rows ({@code 1..verticalSplitPosition}). */
    frozenRowY: ReadonlyArray<number>;

    /** Pixel heights for the frozen rows, same indexing as {@link frozenRowY}. */
    frozenRowHeights: ReadonlyArray<number>;
}

/**
 * Helper: build an empty {@link CellGridRenderState} for tests +
 * for the connector's initial render before any state has been
 * pushed.
 */
export function emptyCellGridRenderState(): CellGridRenderState {
    return {
        viewport: {
            firstColumnIndex: 1,
            firstColumnPosition: 0,
            lastColumnIndex: 0,
            lastColumnPosition: 0,
            firstRowIndex: 1,
            firstRowPosition: 0,
            lastRowIndex: 0,
            lastRowPosition: 0,
        },
        cellData: new Map(),
        colX: [],
        rowY: [],
        colWidths: [],
        rowHeights: [],
        cellStyleToCSSStyle: {},
        mergedRegions: [],
        verticalSplitPosition: 0,
        horizontalSplitPosition: 0,
        frozenColX: [],
        frozenColWidths: [],
        frozenRowY: [],
        frozenRowHeights: [],
        selection: {
            activeCell: { col: 1, row: 1 },
            activeRange: { c1: 1, c2: 1, r1: 1, r2: 1 },
            auxRanges: [],
        },
    };
}
