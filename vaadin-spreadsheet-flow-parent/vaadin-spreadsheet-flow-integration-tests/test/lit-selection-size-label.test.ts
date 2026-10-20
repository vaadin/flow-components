/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import { selectionSizeLabel } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';
import { rendered } from './spreadsheet-harness.js';

/**
 * While a selection is being grown with shift, Excel's name box counts
 * the block out - "3R x 3C" - and goes back to naming the anchor cell
 * once shift is let go. The address field does the same for shift+arrow.
 *
 * The part that needs care is that the server echoes every selection
 * straight back, so the count has to survive that echo or it is replaced
 * by the anchor's address before anyone can read it.
 */

describe('selectionSizeLabel', () => {
    it('counts rows before columns', () => {
        expect(selectionSizeLabel({ c1: 1, c2: 3, r1: 1, r2: 2 })).to.equal(
            '2R x 3C'
        );
    });

    it('counts a single cell as one by one', () => {
        expect(selectionSizeLabel({ c1: 4, c2: 4, r1: 9, r2: 9 })).to.equal(
            '1R x 1C'
        );
    });

    it('does not care which corner came first', () => {
        expect(selectionSizeLabel({ c1: 5, c2: 2, r1: 8, r2: 4 })).to.equal(
            '5R x 4C'
        );
    });
});

describe('Spreadsheet — the address field while shift-selecting', () => {
    let host: HTMLDivElement;
    let sheet: Spreadsheet;

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
        sheet.setSelectedCellAndRange('C3', 3, 3, 3, 3, 3, 3, false);
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
                    cancelable: true,
                })
            );
    }

    function address(): string {
        return sheet.getFormulaBar().renderState.address;
    }

    it('names the cell before anything is selected', async () => {
        await rendered(sheet);
        expect(address()).to.equal('C3');
    });

    it('counts the block as it grows', () => {
        press('ArrowRight', true);
        expect(address()).to.equal('1R x 2C');
        press('ArrowRight', true);
        expect(address()).to.equal('1R x 3C');
        press('ArrowDown', true);
        expect(address()).to.equal('2R x 3C');
    });

    it('keeps the count when the server echoes the selection back', () => {
        press('ArrowRight', true);
        // What the server sends in reply to the range the user is still
        // making. Taking its word for the address would wipe the count.
        sheet.setSelectedCellAndRange('C3', 3, 3, 3, 4, 3, 3, false);

        expect(address()).to.equal('1R x 2C');
    });

    it('keeps the count when the server answers the anchor too', () => {
        // A range selection sends two events, and the server answers
        // both: showSelectedCell for the anchor and
        // setSelectedCellAndRange for the range. Either one taking over
        // the address field loses the count.
        press('ArrowRight', true);
        sheet.showSelectedCell('C3', 3, 3, '', false, false, false);

        expect(address()).to.equal('1R x 2C');
    });

    it('names the anchor cell again once shift is let go', () => {
        press('ArrowRight', true);
        press('ArrowDown', true);
        document.dispatchEvent(new KeyboardEvent('keyup', { key: 'Shift', bubbles: true }));

        expect(address()).to.equal('C3');
    });

    it('takes the server at its word once the selection is made', async () => {
        press('ArrowRight', true);
        document.dispatchEvent(new KeyboardEvent('keyup', { key: 'Shift', bubbles: true }));
        sheet.setSelectedCellAndRange('MyRange', 3, 3, 3, 4, 3, 3, false);

        await rendered(sheet);
        expect(address()).to.equal('MyRange');
    });

    it('goes back to naming a cell when a plain arrow moves on', () => {
        press('ArrowRight', true);
        press('ArrowDown');

        expect(address()).to.not.contain('R x');
    });

    it('ignores a shift let go when nothing was being selected', () => {
        sheet.setSelectedCellAndRange('MyRange', 3, 3, 3, 3, 3, 3, false);
        document.dispatchEvent(new KeyboardEvent('keyup', { key: 'Shift', bubbles: true }));

        expect(address()).to.equal('MyRange');
    });
});
