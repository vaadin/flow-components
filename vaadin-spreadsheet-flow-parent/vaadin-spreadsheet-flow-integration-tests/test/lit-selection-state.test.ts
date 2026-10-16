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
    addAuxRange,
    containsCell,
    isActiveCell,
    isCellSelected,
    isCoherentSelection,
    normalizedRange,
    selectSingleCell,
    type SelectionState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';

/** A range selection from one corner to the other, the active cell at the first. */
function selectRange(col1: number, row1: number, col2: number, row2: number): SelectionState {
    return { activeCell: { col: col1, row: row1 }, activeRange: normalizedRange(col1, row1, col2, row2), auxRanges: [] };
}

describe('SelectionState — selectSingleCell', () => {
    it('builds a single-cell selection with matching active range', () => {
        const state = selectSingleCell(3, 5);

        expect(state.activeCell).to.deep.equal({ col: 3, row: 5 });
        expect(state.activeRange).to.deep.equal({ c1: 3, c2: 3, r1: 5, r2: 5 });
        expect(state.auxRanges).to.deep.equal([]);
    });

    it('is coherent (no aux ranges)', () => {
        const state = selectSingleCell(1, 1);

        expect(isCoherentSelection(state)).to.be.true;
    });
});

describe('SelectionState — normalizedRange', () => {
    it('orders corners so c1 <= c2 and r1 <= r2', () => {
        expect(normalizedRange(5, 5, 1, 1)).to.deep.equal({
            c1: 1, c2: 5, r1: 1, r2: 5,
        });
        expect(normalizedRange(1, 5, 5, 1)).to.deep.equal({
            c1: 1, c2: 5, r1: 1, r2: 5,
        });
        expect(normalizedRange(3, 3, 3, 3)).to.deep.equal({
            c1: 3, c2: 3, r1: 3, r2: 3,
        });
    });
});

describe('SelectionState — containsCell', () => {
    const range = { c1: 2, c2: 5, r1: 3, r2: 7 };

    it('returns true for cells inside the range', () => {
        expect(containsCell(range, 3, 5)).to.be.true;
        expect(containsCell(range, 2, 3)).to.be.true;  // top-left
        expect(containsCell(range, 5, 7)).to.be.true;  // bottom-right
    });

    it('returns true on all four corners (inclusive)', () => {
        expect(containsCell(range, 2, 3)).to.be.true;
        expect(containsCell(range, 5, 3)).to.be.true;
        expect(containsCell(range, 2, 7)).to.be.true;
        expect(containsCell(range, 5, 7)).to.be.true;
    });

    it('returns false for cells outside the range', () => {
        expect(containsCell(range, 1, 5)).to.be.false;  // left of
        expect(containsCell(range, 6, 5)).to.be.false;  // right of
        expect(containsCell(range, 3, 2)).to.be.false;  // above
        expect(containsCell(range, 3, 8)).to.be.false;  // below
    });
});

describe('SelectionState — addAuxRange', () => {
    it('adds the current active range to aux and makes the new range active', () => {
        const initial = selectSingleCell(1, 1);
        const withAux = addAuxRange(initial, 5, 5, 7, 7);

        // New active range
        expect(withAux.activeRange).to.deep.equal({ c1: 5, c2: 7, r1: 5, r2: 7 });
        expect(withAux.activeCell).to.deep.equal({ col: 5, row: 5 });
        // Old single-cell range moves to aux
        expect(withAux.auxRanges).to.have.length(1);
        expect(withAux.auxRanges[0]).to.deep.equal({ c1: 1, c2: 1, r1: 1, r2: 1 });
    });

    it('preserves existing aux ranges when adding more', () => {
        const s0 = selectSingleCell(1, 1);
        const s1 = addAuxRange(s0, 5, 5, 7, 7);
        const s2 = addAuxRange(s1, 10, 10, 12, 12);

        expect(s2.activeRange).to.deep.equal({ c1: 10, c2: 12, r1: 10, r2: 12 });
        expect(s2.auxRanges).to.have.length(2);
    });

    it('makes the cell given the active one, in the range it adds', () => {
        // A column added with Ctrl-click: active on the first row on screen.
        const added = addAuxRange(selectSingleCell(1, 1), 4, 1, 4, 100, { col: 4, row: 51 });

        expect(added.activeCell).to.deep.equal({ col: 4, row: 51 });
        expect(added.activeRange).to.deep.equal({ c1: 4, c2: 4, r1: 1, r2: 100 });
    });

    it('breaks coherence', () => {
        const s0 = selectSingleCell(1, 1);
        expect(isCoherentSelection(s0)).to.be.true;

        const s1 = addAuxRange(s0, 5, 5, 7, 7);
        expect(isCoherentSelection(s1)).to.be.false;
    });
});

describe('SelectionState — isCellSelected', () => {
    it('returns true for cells in the active range', () => {
        const state = selectRange(2, 2, 4, 4);

        expect(isCellSelected(state, 3, 3)).to.be.true;
        expect(isCellSelected(state, 2, 2)).to.be.true;
        expect(isCellSelected(state, 4, 4)).to.be.true;
    });

    it('returns true for cells in any aux range', () => {
        let state = selectRange(2, 2, 4, 4);
        state = addAuxRange(state, 10, 10, 12, 12);

        expect(isCellSelected(state, 11, 11)).to.be.true;
        expect(isCellSelected(state, 3, 3)).to.be.true;  // still in the old range now in aux
    });

    it('returns false for cells outside all ranges', () => {
        const state = selectRange(2, 2, 4, 4);

        expect(isCellSelected(state, 1, 1)).to.be.false;
        expect(isCellSelected(state, 5, 5)).to.be.false;
    });
});

describe('SelectionState — isActiveCell', () => {
    it('returns true only for the single active cell', () => {
        const state = selectRange(2, 2, 4, 4);

        expect(isActiveCell(state, 2, 2)).to.be.true;  // anchor
        expect(isActiveCell(state, 3, 3)).to.be.false; // in range but not active
        expect(isActiveCell(state, 4, 4)).to.be.false; // far corner
    });
});
