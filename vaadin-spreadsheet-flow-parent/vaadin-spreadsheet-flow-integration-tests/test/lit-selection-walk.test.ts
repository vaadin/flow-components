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
    isMultiCellRange,
    walkInsideSelection,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/SelectionWalk.js';
import {
    inSelectionWalkDirection,
    Spreadsheet,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * With a block of cells selected, Enter and Tab do not leave it: they walk
 * the active cell around inside, so the user can fill the block without
 * ever re-selecting it. Enter goes down the column and carries on at the
 * top of the next one; Tab goes along the row and carries on at the start
 * of the next; both wrap at the end and shift reverses them.
 */

// A1:C2 — three columns, two rows.
const A1_C2 = { c1: 1, c2: 3, r1: 1, r2: 2 };

describe('isMultiCellRange', () => {
    it('is false for a single cell', () => {
        expect(isMultiCellRange({ c1: 2, c2: 2, r1: 5, r2: 5 })).to.be.false;
    });

    it('is true for a row of cells', () => {
        expect(isMultiCellRange({ c1: 2, c2: 4, r1: 5, r2: 5 })).to.be.true;
    });

    it('is true for a column of cells', () => {
        expect(isMultiCellRange({ c1: 2, c2: 2, r1: 5, r2: 9 })).to.be.true;
    });
});

describe('inSelectionWalkDirection', () => {
    it('sends Enter down and shift+Enter up', () => {
        expect(inSelectionWalkDirection('Enter', false)).to.equal('down');
        expect(inSelectionWalkDirection('Enter', true)).to.equal('up');
    });

    it('sends Tab right and shift+Tab left', () => {
        expect(inSelectionWalkDirection('Tab', false)).to.equal('right');
        expect(inSelectionWalkDirection('Tab', true)).to.equal('left');
    });

    it('does not walk on an arrow key — that leaves the selection', () => {
        expect(inSelectionWalkDirection('ArrowDown', false)).to.be.null;
        expect(inSelectionWalkDirection('ArrowRight', false)).to.be.null;
    });

    it('does not walk on a key with no direction', () => {
        expect(inSelectionWalkDirection('a', false)).to.be.null;
        expect(inSelectionWalkDirection(undefined, false)).to.be.null;
    });
});

describe('walkInsideSelection — down and up', () => {
    const down = (col: number, row: number) =>
        walkInsideSelection({ range: A1_C2, col, row, direction: 'down' });

    it('steps down the column', () => {
        expect(down(1, 1)).to.eql({ col: 1, row: 2 });
    });

    it('carries on at the top of the next column', () => {
        expect(down(1, 2)).to.eql({ col: 2, row: 1 });
    });

    it('wraps from the last cell back to the first', () => {
        expect(down(3, 2)).to.eql({ col: 1, row: 1 });
    });

    it('walks the whole block in width * height steps', () => {
        const seen: string[] = [];
        let at = { col: 1, row: 1 };
        for (let i = 0; i < 6; i++) {
            seen.push(`${at.col}:${at.row}`);
            at = down(at.col, at.row);
        }
        expect(seen).to.eql(['1:1', '1:2', '2:1', '2:2', '3:1', '3:2']);
        expect(at).to.eql({ col: 1, row: 1 });
    });

    it('goes back the way it came on shift+Enter', () => {
        const up = (col: number, row: number) =>
            walkInsideSelection({ range: A1_C2, col, row, direction: 'up' });
        expect(up(2, 1)).to.eql({ col: 1, row: 2 });
        expect(up(1, 2)).to.eql({ col: 1, row: 1 });
        expect(up(1, 1)).to.eql({ col: 3, row: 2 });
    });
});

describe('walkInsideSelection — right and left', () => {
    const right = (col: number, row: number) =>
        walkInsideSelection({ range: A1_C2, col, row, direction: 'right' });

    it('steps along the row', () => {
        expect(right(1, 1)).to.eql({ col: 2, row: 1 });
    });

    it('carries on at the start of the next row', () => {
        expect(right(3, 1)).to.eql({ col: 1, row: 2 });
    });

    it('wraps from the last cell back to the first', () => {
        expect(right(3, 2)).to.eql({ col: 1, row: 1 });
    });

    it('goes back the way it came on shift+Tab', () => {
        const left = (col: number, row: number) =>
            walkInsideSelection({ range: A1_C2, col, row, direction: 'left' });
        expect(left(1, 2)).to.eql({ col: 3, row: 1 });
        expect(left(3, 1)).to.eql({ col: 2, row: 1 });
        expect(left(1, 1)).to.eql({ col: 3, row: 2 });
    });
});

describe('walkInsideSelection — hidden rows and columns', () => {
    it('steps over a hidden row', () => {
        // A1:A4 with row 2 hidden.
        expect(
            walkInsideSelection({
                range: { c1: 1, c2: 1, r1: 1, r2: 4 },
                col: 1,
                row: 1,
                direction: 'down',
                hiddenRows: new Set([2]),
            })
        ).to.eql({ col: 1, row: 3 });
    });

    it('steps over a hidden column', () => {
        expect(
            walkInsideSelection({
                range: { c1: 1, c2: 4, r1: 1, r2: 1 },
                col: 1,
                row: 1,
                direction: 'right',
                hiddenColumns: new Set([2, 3]),
            })
        ).to.eql({ col: 4, row: 1 });
    });

    it('skips a hidden column it wraps into', () => {
        // Walking down out of column 1 lands in column 2, which is gone.
        expect(
            walkInsideSelection({
                range: A1_C2,
                col: 1,
                row: 2,
                direction: 'down',
                hiddenColumns: new Set([2]),
            })
        ).to.eql({ col: 3, row: 1 });
    });

    it('stays put when every cell in the range is hidden', () => {
        expect(
            walkInsideSelection({
                range: { c1: 1, c2: 2, r1: 1, r2: 2 },
                col: 1,
                row: 1,
                direction: 'down',
                hiddenRows: new Set([1, 2]),
            })
        ).to.eql({ col: 1, row: 1 });
    });
});

describe('Spreadsheet — Enter and Tab inside a selected range', () => {
    let host: HTMLDivElement;
    let sheet: Spreadsheet;
    let events: { type: string; data: Record<string, unknown> }[];

    beforeEach(() => {
        host = document.createElement('div');
        host.style.cssText =
            'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        sheet = new Spreadsheet(host);
        sheet.setRows(20);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        events = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });
    });

    afterEach(() => {
        sheet.dispose();
        host.remove();
    });

    function press(key: string, shiftKey = false): void {
        sheet
            .getScrollContainer()
            .dispatchEvent(
                new KeyboardEvent('keydown', {
                    key,
                    shiftKey,
                    bubbles: true,
                    composed: true,
                })
            );
    }

    function selectA1C2(): void {
        // A1 active, A1:C2 selected — what the server sends back after the
        // user drags the range out.
        sheet.setSelectedCellAndRange('A1', 1, 1, 1, 3, 1, 2, false);
    }

    it('walks the active cell down the column on Enter', () => {
        selectA1C2();
        press('Enter');
        expect(sheet.getSelection().activeCell).to.eql({ col: 1, row: 2 });
    });

    it('carries on into the next column', () => {
        selectA1C2();
        press('Enter');
        press('Enter');
        expect(sheet.getSelection().activeCell).to.eql({ col: 2, row: 1 });
    });

    it('walks along the row on Tab and wraps to the next', () => {
        selectA1C2();
        press('Tab');
        expect(sheet.getSelection().activeCell).to.eql({ col: 2, row: 1 });
        press('Tab');
        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 1 });
        press('Tab');
        expect(sheet.getSelection().activeCell).to.eql({ col: 1, row: 2 });
    });

    it('goes back on shift+Tab', () => {
        selectA1C2();
        press('Tab');
        press('Tab');
        press('Tab');
        press('Tab', true);
        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 1 });
    });

    it('keeps the range the whole way round', () => {
        selectA1C2();
        for (let i = 0; i < 7; i++) {
            press('Enter');
        }
        expect(sheet.getSelection().activeRange).to.eql({
            c1: 1,
            c2: 3,
            r1: 1,
            r2: 2,
        });
    });

    it('tells the server the old range was NOT discarded', () => {
        selectA1C2();
        events.length = 0;
        press('Enter');
        const selected = events.filter((e) => e.type === 'cellSelected');
        expect(selected).to.have.length(1);
        expect(selected[0].data).to.eql({
            row: 2,
            column: 1,
            oldSelectionRangeDiscarded: false,
        });
    });

    it('does not walk a single-cell selection', () => {
        // There is nowhere to walk to, so Enter is the editing key it
        // always was and the active cell stays put.
        sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
        events.length = 0;
        press('Enter');
        expect(sheet.getSelection().activeCell).to.eql({ col: 2, row: 2 });
        expect(events.filter((e) => e.type === 'cellSelected')).to.be.empty;
    });

    it('walks on Tab even though Enter opens the editor', () => {
        sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
        events.length = 0;
        press('Tab');
        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 2 });
        const selected = events.filter((e) => e.type === 'cellSelected');
        expect(selected[0].data).to.eql({
            row: 2,
            column: 3,
            oldSelectionRangeDiscarded: true,
        });
    });

    it('still leaves the selection on an arrow key', () => {
        selectA1C2();
        press('ArrowDown');
        expect(sheet.getSelection().activeCell).to.eql({ col: 1, row: 2 });
        expect(sheet.getSelection().activeRange).to.eql({
            c1: 1,
            c2: 1,
            r1: 2,
            r2: 2,
        });
    });
});
