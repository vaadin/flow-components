/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { MergedRegion } from '../types/wire.js';

/**
 * Factory for a default-constructed `MergedRegion` (id=0, all corners at
 * 0). Used inside `MergedRegionUtil.findIncreasingSelection` to build a
 * transient region when no POI-tracked region was found at the resolved
 * coordinates.
 *
 * The transient `id=0` is the wire-protocol signal that this region was
 * synthesized client-side rather than read from the workbook.
 */
export function createMergedRegion(): MergedRegion {
    return { id: 0, col1: 0, col2: 0, row1: 0, row2: 0 };
}

/**
 * Factory taking corner coordinates. Returns a transient region
 * (`id=0`).
 *
 * Argument order is `c1, r1, c2, r2` — columns-then-rows. This is the
 * same convention the rest of the codebase uses (col first, row
 * second), distinct from the (row, col) order POI uses internally.
 */
export function createMergedRegionFromCorners(
    c1: number,
    r1: number,
    c2: number,
    r2: number
): MergedRegion {
    return { id: 0, col1: c1, col2: c2, row1: r1, row2: r2 };
}

/**
 * Lookup interface implemented by code that maintains the active sheet's
 * merged-region cache. Used by `MergedRegionUtil.findIncreasingSelection`
 * to ask: "is there a merged region at / overlapping these
 * coordinates?".
 *
 * Implementations must be cheap (O(1) ideally) — the selection-expansion
 * algorithm calls these methods inside a loop over every cell on the
 * selection's perimeter, and the algorithm can recurse.
 */
export interface MergedRegionContainer {
    /**
     * Returns the merged region whose top-left cell is at the given
     * coordinates, or `null` if none.
     *
     * @param column Column index, 1-based.
     * @param row    Row index, 1-based.
     */
    getMergedRegionStartingFrom(column: number, row: number): MergedRegion | null;

    /**
     * Returns the merged region that overlaps the given coordinates
     * (containing them, possibly not anchored there), or `null` if no
     * region overlaps.
     *
     * @param column Column index, 1-based.
     * @param row    Row index, 1-based.
     */
    getMergedRegion(column: number, row: number): MergedRegion | null;
}
