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
import { rendered } from './spreadsheet-harness.js';

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
        await rendered(sheet);
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

    it('shows the next cell once Enter has moved on, even when it holds what the last one did', async () => {
        // C3 and C4 are both empty. The field showed '' before the edit and
        // is told '' for C4 after it, so a binding that only compares with
        // what it last rendered left the typed '2' standing.
        const el = await type('2');
        press(el, 'Enter');
        // The server names the new cell.
        sheet.updateFormulaBar(null, 3, 4);
        await rendered(sheet);

        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 4 });
        expect(el.value).to.equal('');
    });

    it('shows the cell again after Escape, even when it is empty', async () => {
        const el = await type('2');
        press(el, 'Escape');
        await rendered(sheet);

        expect(el.value).to.equal('');
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

    it('keeps the keys typed into it from the page', async () => {
        // The page is where the sheet's own page listeners and the
        // application's shortcuts listen: Escape there closes the sheet's
        // menu and pop-up, and Enter can press a button of the view. The
        // sheet's cells are not on the way, so listening on them proved
        // nothing.
        const reachedPage: string[] = [];
        const listener = (e: KeyboardEvent) => reachedPage.push(e.key);
        document.addEventListener('keydown', listener);
        try {
            const el = await type('=');
            press(el, 'a');
            press(el, 'Escape');
        } finally {
            document.removeEventListener('keydown', listener);
        }

        expect(reachedPage).to.deep.equal([]);
    });
});

/**
 * An address the server cannot resolve has to leave the field showing the
 * cell the selection is actually on. The field is bound by property, so Lit
 * leaves it alone when the bound address has not changed - which is exactly
 * this case - and the rejected text stayed on screen naming a cell nobody
 * was standing on.
 */
/**
 * Keys an input method composes with are its own, in the formula field and
 * the address field alike: confirming a conversion with Enter commits
 * nothing, and Escape dropping one keeps what was typed.
 */
describe('Spreadsheet — the formula bar and an input method', () => {
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

    async function input(cls: 'formula' | 'address', value: string): Promise<HTMLInputElement> {
        const bar = sheet.getFormulaBar();
        await rendered(sheet);
        const el = bar.shadowRoot!.querySelector<HTMLInputElement>(`.${cls}`)!;
        el.dispatchEvent(new FocusEvent('focusin', { bubbles: true, composed: true }));
        el.value = value;
        return el;
    }

    function compose(el: HTMLElement, key: string): void {
        el.dispatchEvent(
            new KeyboardEvent('keydown', { key, isComposing: true, bubbles: true, composed: true, cancelable: true })
        );
    }

    function sent(type: string): unknown[] {
        return events.filter((e) => e.type === type);
    }

    it('commits nothing on the Enter that confirms a conversion', async () => {
        compose(await input('formula', '漢字'), 'Enter');

        expect(sent('cellValueEdited')).to.eql([]);
        expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 3 });
    });

    it('keeps what was typed on the Escape that drops a conversion', async () => {
        const el = await input('formula', '漢字');
        let cancelled = false;
        sheet.getFormulaBar().addEventListener('formula-cancel', () => (cancelled = true));

        compose(el, 'Escape');

        expect(cancelled).to.be.false;
        expect(el.value).to.equal('漢字');
    });

    it('goes nowhere on a confirming Enter in the address field', async () => {
        compose(await input('address', 'B2'), 'Enter');

        expect(sent('sheetAddressChanged')).to.eql([]);
    });
});

describe('Spreadsheet — an address the server rejects', () => {
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

    function addressField(): HTMLInputElement {
        return sheet
            .getFormulaBar()
            .shadowRoot!.querySelector('.address') as HTMLInputElement;
    }

    it('puts the real address back', async () => {
        await rendered(sheet);
        expect(addressField().value).to.equal('C3');

        addressField().value = 'NOTACELL';
        sheet.invalidCellAddress();

        expect(addressField().value).to.equal('C3');
    });

    it('leaves a valid address alone', async () => {
        await rendered(sheet);
        sheet.setSelectedCellAndRange('D4', 4, 4, 4, 4, 4, 4, false);
        await rendered(sheet);

        expect(addressField().value).to.equal('D4');
    });
});
