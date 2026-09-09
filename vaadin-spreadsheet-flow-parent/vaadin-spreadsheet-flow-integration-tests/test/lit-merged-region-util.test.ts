/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import type { MergedRegion } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';
import {
    createMergedRegion,
    createMergedRegionFromCorners,
    type MergedRegionContainer,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/MergedRegion.js';
import { findIncreasingSelection } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/MergedRegionUtil.js';

/**
 * Test helper: an in-memory {@link MergedRegionContainer} that holds a
 * fixed list of merged regions and answers lookup queries against them.
 */
class FakeContainer implements MergedRegionContainer {
    private readonly regions: MergedRegion[];

    constructor(regions: MergedRegion[]) {
        this.regions = regions;
    }

    getMergedRegionStartingFrom(column: number, row: number): MergedRegion | null {
        for (const r of this.regions) {
            if (r.col1 === column && r.row1 === row) {
                return r;
            }
        }
        return null;
    }

    getMergedRegion(column: number, row: number): MergedRegion | null {
        for (const r of this.regions) {
            if (column >= r.col1 && column <= r.col2 && row >= r.row1 && row <= r.row2) {
                return r;
            }
        }
        return null;
    }
}

function mr(id: number, col1: number, row1: number, col2: number, row2: number): MergedRegion {
    return { id, col1, col2, row1, row2 };
}

describe('MergedRegionUtil — findIncreasingSelection', () => {
    describe('single-cell selection', () => {
        it('returns a transient region (id=0) when the cell is not merged', () => {
            const container = new FakeContainer([]);

            const result = findIncreasingSelection(container, 5, 5, 3, 3);

            expect(result).to.deep.equal({ id: 0, col1: 3, col2: 3, row1: 5, row2: 5 });
        });

        it('returns the existing merged region when the cell is inside one', () => {
            const region = mr(42, 2, 3, 4, 6);
            const container = new FakeContainer([region]);

            const result = findIncreasingSelection(container, 5, 5, 3, 3);

            expect(result).to.equal(region);
            expect(result.id).to.equal(42);
        });
    });

    describe('exact-match shortcut', () => {
        it('returns the existing region when it covers the selection exactly', () => {
            const region = mr(7, 1, 1, 5, 5);
            const container = new FakeContainer([region]);

            const result = findIncreasingSelection(container, 1, 5, 1, 5);

            expect(result).to.equal(region);
        });

        it('returns the existing region when it fully contains a smaller selection anchored at its top-left', () => {
            const region = mr(7, 1, 1, 10, 10);
            const container = new FakeContainer([region]);

            const result = findIncreasingSelection(container, 1, 3, 1, 3);

            expect(result).to.equal(region);
        });
    });

    describe('multi-cell selection without overlap', () => {
        it('returns a transient region matching the selection corners', () => {
            const container = new FakeContainer([]);

            const result = findIncreasingSelection(container, 2, 5, 1, 4);

            expect(result).to.deep.equal({ id: 0, col1: 1, col2: 4, row1: 2, row2: 5 });
        });
    });

    describe('top-edge expansion', () => {
        it('grows the selection upward and outward when the top edge crosses a merged region', () => {
            // Merged region: rows 1-3, cols 2-4.
            // Selection: rows 2-5, cols 2-5 — top edge cuts through the merged region.
            // Expected: rows grow upward to 1; cols don't grow (within range).
            const container = new FakeContainer([mr(1, 2, 1, 4, 3)]);

            const result = findIncreasingSelection(container, 2, 5, 2, 5);

            expect(result.row1).to.equal(1);
            expect(result.row2).to.equal(5);
            expect(result.col1).to.equal(2);
            expect(result.col2).to.equal(5);
        });
    });

    describe('right-edge expansion', () => {
        it('grows the selection rightward when the right edge crosses a merged region', () => {
            // Merged region: rows 2-4, cols 4-6.
            // Selection: rows 2-4, cols 1-5 — right edge cuts through the merged region.
            const container = new FakeContainer([mr(1, 4, 2, 6, 4)]);

            const result = findIncreasingSelection(container, 2, 4, 1, 5);

            expect(result.col2).to.equal(6);
            expect(result.col1).to.equal(1);
            expect(result.row1).to.equal(2);
            expect(result.row2).to.equal(4);
        });
    });

    describe('bottom-edge expansion', () => {
        it('grows the selection downward when the bottom edge crosses a merged region', () => {
            // Merged region: rows 4-6, cols 2-4.
            // Selection: rows 1-4, cols 2-4 — bottom edge cuts through.
            const container = new FakeContainer([mr(1, 2, 4, 4, 6)]);

            const result = findIncreasingSelection(container, 1, 4, 2, 4);

            expect(result.row2).to.equal(6);
        });
    });

    describe('left-edge expansion', () => {
        it('grows the selection leftward when the left edge crosses a merged region', () => {
            // Merged region: rows 2-4, cols 1-3.
            // Selection: rows 2-4, cols 2-5 — left edge cuts through.
            const container = new FakeContainer([mr(1, 1, 2, 3, 4)]);

            const result = findIncreasingSelection(container, 2, 4, 2, 5);

            expect(result.col1).to.equal(1);
        });
    });

    describe('recursive expansion', () => {
        it('keeps growing until no more edges cut merged regions', () => {
            // Two adjacent merged regions:
            // A: rows 1-3, cols 1-3
            // B: rows 3-5, cols 3-5
            // Selection: rows 2-4, cols 2-4 — should grow to cover both: rows 1-5, cols 1-5.
            const container = new FakeContainer([
                mr(1, 1, 1, 3, 3),
                mr(2, 3, 3, 5, 5),
            ]);

            const result = findIncreasingSelection(container, 2, 4, 2, 4);

            expect(result.row1).to.equal(1);
            expect(result.row2).to.equal(5);
            expect(result.col1).to.equal(1);
            expect(result.col2).to.equal(5);
        });
    });

    describe('createMergedRegion helper', () => {
        it('returns a default-constructed region with all corners at 0', () => {
            const region = createMergedRegion();

            expect(region).to.deep.equal({ id: 0, col1: 0, col2: 0, row1: 0, row2: 0 });
        });

        it('returns a fresh object on each call (not a shared singleton)', () => {
            const a = createMergedRegion();
            const b = createMergedRegion();

            a.col1 = 99;

            expect(b.col1).to.equal(0);
        });
    });

    describe('createMergedRegionFromCorners helper', () => {
        it('takes corners in (c1, r1, c2, r2) order — columns first, then rows', () => {
            const region = createMergedRegionFromCorners(2, 3, 7, 9);

            expect(region.col1).to.equal(2);
            expect(region.row1).to.equal(3);
            expect(region.col2).to.equal(7);
            expect(region.row2).to.equal(9);
        });

        it('returns a transient region (id = 0)', () => {
            const region = createMergedRegionFromCorners(1, 1, 1, 1);

            // The `id = 0` is the wire-protocol signal that this
            // region was synthesized client-side rather than read
            // from the workbook.
            expect(region.id).to.equal(0);
        });

        it('returns a fresh object on each call (not a shared singleton)', () => {
            const a = createMergedRegionFromCorners(1, 1, 2, 2);
            const b = createMergedRegionFromCorners(1, 1, 2, 2);

            a.col1 = 99;

            expect(b.col1).to.equal(1);
        });

        it('accepts a single-cell region (corners at the same coord)', () => {
            const region = createMergedRegionFromCorners(5, 5, 5, 5);

            expect(region).to.deep.equal({
                id: 0,
                col1: 5,
                col2: 5,
                row1: 5,
                row2: 5,
            });
        });
    });
});
