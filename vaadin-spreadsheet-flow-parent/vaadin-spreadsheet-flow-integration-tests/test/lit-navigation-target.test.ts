/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

import { computeNavigationTarget } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * Slice 149 — extended keyboard navigation.
 *
 * The pure helper `computeNavigationTarget` resolves a key + modifier
 * combo into either a "move the active cell" or "move the extend cell"
 * target. Tests exercise it in isolation; the connector then writes
 * the result into `selection` + recomputes the viewport.
 */

function noMods() {
    return { shiftKey: false, ctrlKey: false, metaKey: false };
}

const STATE_MID = {
    activeCell: { col: 5, row: 5 },
    extendCol: 5,
    extendRow: 5,
    cols: 10,
    rows: 10,
};

const STATE_TOP_LEFT = {
    activeCell: { col: 1, row: 1 },
    extendCol: 1,
    extendRow: 1,
    cols: 10,
    rows: 10,
};

const STATE_BOTTOM_RIGHT = {
    activeCell: { col: 10, row: 10 },
    extendCol: 10,
    extendRow: 10,
    cols: 10,
    rows: 10,
};

describe('computeNavigationTarget — unknown keys', () => {
    it('returns null for non-navigation keys', () => {
        expect(computeNavigationTarget('a', noMods(), STATE_MID)).to.be.null;
        expect(computeNavigationTarget('F1', noMods(), STATE_MID)).to.be.null;
        expect(computeNavigationTarget(' ', noMods(), STATE_MID)).to.be.null;
        expect(computeNavigationTarget('Escape', noMods(), STATE_MID)).to.be.null;
    });
});

describe('computeNavigationTarget — arrow keys', () => {
    it('plain arrows move the active cell ±1', () => {
        expect(computeNavigationTarget('ArrowRight', noMods(), STATE_MID))
            .to.deep.equal({ kind: 'move', col: 6, row: 5 });
        expect(computeNavigationTarget('ArrowLeft', noMods(), STATE_MID))
            .to.deep.equal({ kind: 'move', col: 4, row: 5 });
        expect(computeNavigationTarget('ArrowDown', noMods(), STATE_MID))
            .to.deep.equal({ kind: 'move', col: 5, row: 6 });
        expect(computeNavigationTarget('ArrowUp', noMods(), STATE_MID))
            .to.deep.equal({ kind: 'move', col: 5, row: 4 });
    });

    it('shift+arrow moves the extend position', () => {
        // extendCol/Row differ from activeCell to make the
        // base-pick observable.
        const state = {
            activeCell: { col: 3, row: 3 },
            extendCol: 5,
            extendRow: 5,
            cols: 10,
            rows: 10,
        };
        const result = computeNavigationTarget(
            'ArrowDown',
            { shiftKey: true, ctrlKey: false, metaKey: false },
            state
        );
        expect(result).to.deep.equal({ kind: 'extend', col: 5, row: 6 });
    });

    it('arrows clamp at grid edges', () => {
        expect(computeNavigationTarget('ArrowUp', noMods(), STATE_TOP_LEFT))
            .to.deep.equal({ kind: 'move', col: 1, row: 1 });
        expect(computeNavigationTarget('ArrowLeft', noMods(), STATE_TOP_LEFT))
            .to.deep.equal({ kind: 'move', col: 1, row: 1 });
        expect(computeNavigationTarget('ArrowDown', noMods(), STATE_BOTTOM_RIGHT))
            .to.deep.equal({ kind: 'move', col: 10, row: 10 });
        expect(computeNavigationTarget('ArrowRight', noMods(), STATE_BOTTOM_RIGHT))
            .to.deep.equal({ kind: 'move', col: 10, row: 10 });
    });
});

describe('computeNavigationTarget — Tab / Shift+Tab', () => {
    it('Tab moves active cell right by 1', () => {
        expect(computeNavigationTarget('Tab', noMods(), STATE_MID))
            .to.deep.equal({ kind: 'move', col: 6, row: 5 });
    });

    it('Shift+Tab moves active cell LEFT by 1 (still kind=move, not extend)', () => {
        const result = computeNavigationTarget(
            'Tab',
            { shiftKey: true, ctrlKey: false, metaKey: false },
            STATE_MID
        );
        expect(result).to.deep.equal({ kind: 'move', col: 4, row: 5 });
    });

    it('Tab clamps at last column', () => {
        expect(computeNavigationTarget('Tab', noMods(), STATE_BOTTOM_RIGHT))
            .to.deep.equal({ kind: 'move', col: 10, row: 10 });
    });

    it('Shift+Tab clamps at column 1', () => {
        expect(computeNavigationTarget(
            'Tab',
            { shiftKey: true, ctrlKey: false, metaKey: false },
            STATE_TOP_LEFT
        )).to.deep.equal({ kind: 'move', col: 1, row: 1 });
    });
});

describe('computeNavigationTarget — Enter / Shift+Enter', () => {
    it('Enter moves active cell DOWN by 1', () => {
        expect(computeNavigationTarget('Enter', noMods(), STATE_MID))
            .to.deep.equal({ kind: 'move', col: 5, row: 6 });
    });

    it('Shift+Enter moves active cell UP by 1', () => {
        const result = computeNavigationTarget(
            'Enter',
            { shiftKey: true, ctrlKey: false, metaKey: false },
            STATE_MID
        );
        expect(result).to.deep.equal({ kind: 'move', col: 5, row: 4 });
    });
});

describe('computeNavigationTarget — Home / End', () => {
    it('Home jumps to column 1 in current row', () => {
        expect(computeNavigationTarget('Home', noMods(), STATE_MID))
            .to.deep.equal({ kind: 'move', col: 1, row: 5 });
    });

    it('End jumps to last column in current row', () => {
        expect(computeNavigationTarget('End', noMods(), STATE_MID))
            .to.deep.equal({ kind: 'move', col: 10, row: 5 });
    });

    it('Ctrl+Home jumps to A1 regardless of current row', () => {
        const result = computeNavigationTarget(
            'Home',
            { shiftKey: false, ctrlKey: true, metaKey: false },
            STATE_MID
        );
        expect(result).to.deep.equal({ kind: 'move', col: 1, row: 1 });
    });

    it('Cmd+Home (Mac convention) is the same as Ctrl+Home', () => {
        const result = computeNavigationTarget(
            'Home',
            { shiftKey: false, ctrlKey: false, metaKey: true },
            STATE_MID
        );
        expect(result).to.deep.equal({ kind: 'move', col: 1, row: 1 });
    });

    it('Ctrl+End jumps to (cols, rows)', () => {
        const result = computeNavigationTarget(
            'End',
            { shiftKey: false, ctrlKey: true, metaKey: false },
            STATE_MID
        );
        expect(result).to.deep.equal({ kind: 'move', col: 10, row: 10 });
    });
});
