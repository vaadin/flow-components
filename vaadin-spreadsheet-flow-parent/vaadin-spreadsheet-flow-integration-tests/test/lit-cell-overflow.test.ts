/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import {
    overflowWidth,
    type CellOverflowInput
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/CellOverflow.js';

/**
 * How far text spills out of its column.
 *
 * A spreadsheet runs text on across empty neighbours instead of clipping it
 * at the column edge. The Lit client clipped every cell, so anything wider
 * than its column was cut — visible in the Phase 3.0 baselines, where the
 * GWT reference reads "File opened" and the Lit shot reads "File open".
 */

function input(over: Partial<CellOverflowInput> = {}): CellOverflowInput {
    return {
        col: 1,
        columnWidth: 50,
        contentWidth: 120,
        rightAligned: false,
        columnWidths: new Map([
            [1, 50],
            [2, 50],
            [3, 50],
            [4, 50]
        ]),
        occupiedColumns: new Set<number>(),
        horizontalSplitPosition: 0,
        ...over
    };
}

describe('overflowWidth — text spilling into empty neighbours', () => {
    it('leaves a cell alone when its text fits', () => {
        expect(overflowWidth(input({ contentWidth: 50 }))).to.be.null;
        expect(overflowWidth(input({ contentWidth: 20 }))).to.be.null;
    });

    it('spills across as many empty columns as the text needs', () => {
        // 120px of text in a 50px column overshoots by 70, plus 2px of
        // padding: two more columns of 50 cover it.
        expect(overflowWidth(input())).to.equal(150);
    });

    it('stops at the first column holding a value', () => {
        expect(
            overflowWidth(input({ occupiedColumns: new Set([3]) }))
        ).to.equal(100);
    });

    it('does not spill when the very next column is taken', () => {
        expect(overflowWidth(input({ occupiedColumns: new Set([2]) }))).to.be
            .null;
    });

    it('does not spill a right-aligned cell', () => {
        // Excel runs these leftwards; the GWT client never did, and this
        // branch is about matching the GWT client.
        expect(overflowWidth(input({ rightAligned: true }))).to.be.null;
    });

    it('stops at the frozen/scrolling split', () => {
        // Column 2 is the last frozen one. Text in it must not run into
        // column 3, which lives in a different quadrant and scrolls away.
        expect(
            overflowWidth(input({ col: 2, horizontalSplitPosition: 2 }))
        ).to.be.null;
    });

    it('spills freely inside the frozen block', () => {
        expect(
            overflowWidth(input({ col: 1, horizontalSplitPosition: 3 }))
        ).to.equal(150);
    });

    it('spills freely outside the frozen block', () => {
        expect(
            overflowWidth(input({ col: 2, horizontalSplitPosition: 1 }))
        ).to.equal(150);
    });

    it('stops at the last column it was given a width for', () => {
        expect(
            overflowWidth(
                input({
                    col: 3,
                    columnWidths: new Map([
                        [3, 50],
                        [4, 50]
                    ])
                })
            )
        ).to.equal(100);
    });

    it('does not spill from the last column', () => {
        expect(
            overflowWidth(input({ col: 4, columnWidths: new Map([[4, 50]]) }))
        ).to.be.null;
    });

    it('counts the neighbours actual widths, not the cell own', () => {
        expect(
            overflowWidth(
                input({
                    columnWidth: 50,
                    contentWidth: 130,
                    columnWidths: new Map([
                        [1, 50],
                        [2, 10],
                        [3, 10],
                        [4, 200]
                    ])
                })
            )
        ).to.equal(270);
    });

    it('takes one column when the text only just overshoots', () => {
        expect(overflowWidth(input({ contentWidth: 51 }))).to.equal(100);
    });
});
