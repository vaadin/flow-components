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

/**
 * A value typed into the formula field belongs to the cell the sheet has
 * selected. Enter commits it and moves on down, Tab commits and moves
 * right, shift+Tab commits without moving, Escape throws it away, and
 * clicking elsewhere commits - which is how a value typed here reaches
 * the sheet when the user goes straight back to the cell.
 *
 * None of it was wired: the field took typing and nothing listened, so
 * editing a cell through the formula bar did nothing at all.
 */
describe('Spreadsheet — editing through the formula field', () => {
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
        sheet.setSelectedCellAndRange('C3', 3, 3, 3, 3, 3, 3, false);
        events = [];
        host.addEventListener('spreadsheet-event', (e: Event) =>
            events.push((e as CustomEvent).detail)
        );
    });

    afterEach(() => {
        sheet.dispose();
        host.remove();
    });

    async function field(): Promise<HTMLInputElement> {
        const bar = sheet.getFormulaBar();
        await bar.updateComplete;
        return bar.shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
    }

    async function type(value: string): Promise<HTMLInputElement> {
        const el = await field();
        el.dispatchEvent(new FocusEvent('focusin', { bubbles: true, composed: true }));
        el.value = value;
        return el;
    }

    function press(el: HTMLElement, key: string, shiftKey = false): void {
        el.dispatchEvent(
            new KeyboardEvent('keydown', {
                key,
                shiftKey,
                bubbles: true,
                composed: true,
                cancelable: true,
            })
        );
    }

    function edits(): Record<string, unknown>[] {
        return events.filter((e) => e.type === 'cellValueEdited').map((e) => e.data);
    }

    it('reports the value against the selected cell on Enter', async () => {
        const el = await type('2');
        press(el, 'Enter');

        expect(edits()).to.eql([{ row: 3, col: 3, value: '2' }]);
    });

    it('moves on down after Enter', async () => {
        press(await type('2'), 'Enter');

        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 4 });
    });

    it('moves right after Tab', async () => {
        press(await type('2'), 'Tab');

        expect(edits()).to.eql([{ row: 3, col: 3, value: '2' }]);
        expect(sheet.getSelection().activeCell).to.eql({ col: 4, row: 3 });
    });

    it('commits without moving on shift+Tab', async () => {
        press(await type('2'), 'Tab', true);

        expect(edits()).to.eql([{ row: 3, col: 3, value: '2' }]);
        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 3 });
    });

    it('commits when the field is left, without moving', async () => {
        const el = await type('2');
        el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));

        expect(edits()).to.eql([{ row: 3, col: 3, value: '2' }]);
        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 3 });
    });

    it('reports the edit once, not again as the field is left', async () => {
        const el = await type('2');
        press(el, 'Enter');
        el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));

        expect(edits()).to.have.length(1);
    });

    it('throws the edit away on Escape', async () => {
        const el = await type('nonsense');
        press(el, 'Escape');

        expect(edits()).to.be.empty;
        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 3 });
    });

    it('puts the cell back in the field on Escape', async () => {
        sheet.updateBottomRightCellValues([
            { row: 3, col: 3, value: '42', originalValue: '42', cellStyle: 'cs0' } as any,
        ]);
        sheet.showSelectedCell('C3', 3, 3, '42', false, false, false);
        const el = await type('half-typed');
        press(el, 'Escape');

        expect(sheet.getFormulaBar().renderState.formula).to.equal('42');
    });

    it('says nothing when the field is left untouched', async () => {
        // A blur with nothing typed is not an edit.
        const el = await field();
        el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));

        expect(edits()).to.be.empty;
    });

    it('refuses to write to a locked cell', async () => {
        sheet.setSheetProtected(true);
        sheet.updateBottomRightCellValues([
            { row: 3, col: 3, value: '', cellStyle: 'cs0', locked: true } as any,
        ]);
        const el = await type('2');
        press(el, 'Enter');

        expect(edits()).to.be.empty;
    });

    it('keeps the keys out of the sheet underneath', async () => {
        // Without stopping them, typing a formula would also be typing
        // into the cell the sheet has selected.
        let reachedSheet = false;
        sheet.getScrollContainer().addEventListener('keydown', () => {
            reachedSheet = true;
        });
        press(await type('='), 'a');

        expect(reachedSheet).to.be.false;
    });
});
