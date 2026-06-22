/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { MergedRegion } from '../types/wire.js';
import {
    createMergedRegion,
    type MergedRegionContainer,
} from './MergedRegion.js';

/**
 * Expand a rectangular selection so its edges don't cut through any
 * merged region — Excel's convention is that a selection always covers
 * complete merged regions.
 *
 * The algorithm walks the 4 edges of the selection (top, right, bottom,
 * left) and, for each cell on an edge, checks the merged-region cache.
 * When a merged region overlaps an edge, the selection corner is pushed
 * out to include the full region; the algorithm recurses until no
 * further expansion is needed.
 *
 * All coordinates are **1-based** (matching POI's convention and the
 * server-side spreadsheet API).
 *
 * The behavior contract is pinned by the server-side
 * `MergedRegionLookup` + `MergedRegionCascade` Phase 3 specs +
 * the unit tests in `lit-merged-region-util.test.ts`.
 *
 * Invariants:
 * - **Single-cell shortcut**. When the selection collapses to one
 *   cell and that cell isn't merged, return a transient region for
 *   just that cell. When that cell IS merged, return the full merged
 *   region.
 * - **Start-coord exact match**. When a region starts exactly at the
 *   selection's top-left AND fully contains the selection, return it
 *   directly without scanning edges.
 * - **The `trouble` flag drives recursion**. Each edge pass sets
 *   `trouble = true` if it had to expand a corner; the recursive
 *   tail re-walks with the new corners.
 * - **Inner clamp before each edge**. After expanding via the top
 *   edge, `topRow > bottomRow` is clamped to `topRow = bottomRow`
 *   (and analogous for the other edges) so the inner loops always see
 *   sane bounds.
 * - **Loop steps over merged cells**. When a cell on an edge is part
 *   of a merged region, the iteration jumps past that region (e.g.
 *   `i = region.col2 + 1`) rather than incrementing by 1.
 *
 * @param container     The merged-region cache for the active sheet.
 * @param topRow        Top edge of the selection, 1-based, inclusive.
 * @param bottomRow     Bottom edge, 1-based, inclusive.
 * @param leftColumn    Left edge, 1-based, inclusive.
 * @param rightColumn   Right edge, 1-based, inclusive.
 * @returns The smallest rectangular `MergedRegion` that contains the
 *          input selection and respects all overlapping merged regions.
 */
export function findIncreasingSelection(
    container: MergedRegionContainer,
    topRow: number,
    bottomRow: number,
    leftColumn: number,
    rightColumn: number
): MergedRegion {
    // Single-cell shortcut: collapsed selection.
    if (topRow === bottomRow && leftColumn === rightColumn) {
        const mergedRegion = container.getMergedRegion(leftColumn, topRow);
        if (mergedRegion === null) {
            // Synthesize a transient region for the single cell.
            const synth = createMergedRegion();
            synth.col1 = leftColumn;
            synth.col2 = rightColumn;
            synth.row1 = topRow;
            synth.row2 = bottomRow;
            return synth;
        }
        return mergedRegion;
    } else {
        // Multi-cell selection: check if a single merged region already
        // covers the selection exactly (anchored at top-left).
        const merged = container.getMergedRegionStartingFrom(
            leftColumn,
            topRow
        );
        if (
            merged !== null &&
            merged.col2 >= rightColumn &&
            merged.row2 >= bottomRow
        ) {
            return merged;
        }
    }

    let trouble = false;
    let i: number;

    // Edge 1: top — walk left-to-right.
    i = leftColumn;
    while (i <= rightColumn) {
        const region = container.getMergedRegion(i, topRow);
        if (region !== null) {
            i = region.col2 + 1;
            if (leftColumn > region.col1) {
                leftColumn = region.col1;
                trouble = true;
            }
            if (rightColumn < region.col2) {
                rightColumn = region.col2;
                trouble = true;
            }
            if (topRow > region.row1) {
                topRow = region.row1;
                trouble = true;
            }
        } else {
            i++;
        }
    }
    // Clamp after top edge.
    if (topRow > bottomRow) {
        topRow = bottomRow;
    }

    // Edge 2: right — walk top-to-bottom.
    i = topRow;
    while (i <= bottomRow) {
        const region = container.getMergedRegion(rightColumn, i);
        if (region !== null) {
            i = region.row2 + 1;
            if (rightColumn < region.col2) {
                rightColumn = region.col2;
                trouble = true;
            }
            if (topRow > region.row1) {
                topRow = region.row1;
                trouble = true;
            }
            if (bottomRow < region.row2) {
                bottomRow = region.row2;
                trouble = true;
            }
        } else {
            i++;
        }
    }
    // Clamp after right edge.
    if (rightColumn < leftColumn) {
        rightColumn = leftColumn;
    }

    // Edge 3: bottom — walk left-to-right.
    i = leftColumn;
    while (i <= rightColumn) {
        const region = container.getMergedRegion(i, bottomRow);
        if (region !== null) {
            i = region.col2 + 1;
            if (leftColumn > region.col1) {
                leftColumn = region.col1;
                trouble = true;
            }
            if (rightColumn < region.col2) {
                rightColumn = region.col2;
                trouble = true;
            }
            if (bottomRow < region.row2) {
                bottomRow = region.row2;
                trouble = true;
            }
        } else {
            i++;
        }
    }
    // Clamp after bottom edge.
    if (bottomRow < topRow) {
        bottomRow = topRow;
    }

    // Edge 4: left — walk top-to-bottom.
    i = topRow;
    while (i <= bottomRow) {
        const region = container.getMergedRegion(leftColumn, i);
        if (region !== null) {
            i = region.row2 + 1;
            if (leftColumn > region.col1) {
                leftColumn = region.col1;
                trouble = true;
            }
            if (topRow > region.row1) {
                topRow = region.row1;
                trouble = true;
            }
            if (bottomRow < region.row2) {
                bottomRow = region.row2;
                trouble = true;
            }
        } else {
            i++;
        }
    }
    // Clamp after left edge.
    if (leftColumn > rightColumn) {
        leftColumn = rightColumn;
    }

    if (trouble) {
        // Selection grew; re-walk with the new corners. Recursion
        // terminates because either an edge stops expanding (no merged
        // region on the perimeter) or the selection grows monotonically
        // to the whole sheet.
        return findIncreasingSelection(
            container,
            topRow,
            bottomRow,
            leftColumn,
            rightColumn
        );
    } else if (topRow === bottomRow && leftColumn === rightColumn) {
        // Collapsed after the walk — same single-cell shortcut as
        // entry.
        const mergedRegion = container.getMergedRegion(leftColumn, topRow);
        if (mergedRegion === null) {
            const synth = createMergedRegion();
            synth.col1 = leftColumn;
            synth.col2 = rightColumn;
            synth.row1 = topRow;
            synth.row2 = bottomRow;
            return synth;
        }
        return mergedRegion;
    } else {
        // Same start-coord-exact-match check as entry.
        const merged = container.getMergedRegionStartingFrom(
            leftColumn,
            topRow
        );
        if (
            merged !== null &&
            merged.col2 >= rightColumn &&
            merged.row2 >= bottomRow
        ) {
            return merged;
        }
    }

    // Fallthrough: no exact-match region; emit a transient region for
    // the expanded corners.
    const result = createMergedRegion();
    result.col1 = leftColumn;
    result.col2 = rightColumn;
    result.row1 = topRow;
    result.row2 = bottomRow;
    return result;
}
