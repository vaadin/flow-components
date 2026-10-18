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
    isValidSheetName,
    resolveSheetRename,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/SheetName.js';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * Double-clicking the active sheet tab renames it: the name becomes an
 * input, Enter or Tab or clicking away keeps what was typed, Escape
 * throws it away. A name the workbook would refuse and a name another
 * sheet has taken leave the tab as it was.
 */

describe('isValidSheetName', () => {
    it('accepts an ordinary name', () => {
        expect(isValidSheetName('Budget 2026')).to.be.true;
    });

    it('refuses an empty name', () => {
        expect(isValidSheetName('')).to.be.false;
    });

    it('refuses a name past 31 characters', () => {
        expect(isValidSheetName('a'.repeat(31))).to.be.true;
        expect(isValidSheetName('a'.repeat(32))).to.be.false;
    });

    it('refuses the characters a formula needs', () => {
        for (const ch of ['/', '\\', '?', '*', ']', '[', ':']) {
            expect(isValidSheetName(`Sheet${ch}1`), ch).to.be.false;
        }
    });

    it('refuses nothing at all', () => {
        expect(isValidSheetName(null)).to.be.false;
        expect(isValidSheetName(undefined)).to.be.false;
    });
});

describe('resolveSheetRename', () => {
    const names = ['Sheet1', 'Budget', 'Notes'];

    it('renames to a free, valid name', () => {
        expect(
            resolveSheetRename({ proposed: 'Q4', index: 1, names })
        ).to.eql({ kind: 'rename', name: 'Q4' });
    });

    it('leaves a name the workbook would refuse', () => {
        expect(
            resolveSheetRename({ proposed: 'a/b', index: 1, names })
        ).to.eql({ kind: 'revert', reason: 'invalid' });
    });

    it('leaves a name that has not changed', () => {
        expect(
            resolveSheetRename({ proposed: 'Budget', index: 1, names })
        ).to.eql({ kind: 'revert', reason: 'unchanged' });
    });

    it('leaves a name another sheet has taken', () => {
        expect(
            resolveSheetRename({ proposed: 'Notes', index: 1, names })
        ).to.eql({ kind: 'revert', reason: 'taken' });
    });

    it('leaves a name another sheet has taken in another case', () => {
        // POI refused it on the server while the tab already showed it.
        expect(
            resolveSheetRename({ proposed: 'NOTES', index: 1, names })
        ).to.eql({ kind: 'revert', reason: 'taken' });
    });

    it('lets a sheet change the case of its own name', () => {
        expect(
            resolveSheetRename({ proposed: 'BUDGET', index: 1, names })
        ).to.eql({ kind: 'rename', name: 'BUDGET' });
    });
});

describe('Spreadsheet — renaming a sheet tab', () => {
    let host: HTMLDivElement;
    let sheet: Spreadsheet;
    let events: { type: string; data: Record<string, unknown> }[];

    beforeEach(() => {
        host = document.createElement('div');
        host.style.cssText =
            'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        sheet = new Spreadsheet(host);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));
        sheet.setSheetNames(['Sheet1', 'Budget', 'Notes']);
        sheet.setSheetIndex(2);
        events = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });
    });

    afterEach(() => {
        sheet.dispose();
        host.remove();
    });

    async function tabs(): Promise<HTMLElement[]> {
        const strip = sheet.getSheetTabs();
        await strip.updateComplete;
        return Array.from(
            strip.shadowRoot!.querySelectorAll<HTMLElement>('.sheet-tab')
        );
    }

    async function doubleClick(index: number): Promise<void> {
        const tab = (await tabs())[index - 1];
        tab.dispatchEvent(
            new MouseEvent('dblclick', { bubbles: true, composed: true })
        );
        await sheet.getSheetTabs().updateComplete;
    }

    async function input(): Promise<HTMLInputElement | null> {
        const strip = sheet.getSheetTabs();
        await strip.updateComplete;
        return strip.shadowRoot!.querySelector<HTMLInputElement>(
            '.sheet-tab-rename'
        );
    }

    async function type(value: string): Promise<HTMLInputElement> {
        const el = (await input())!;
        el.value = value;
        return el;
    }

    function press(el: HTMLElement, key: string): void {
        el.dispatchEvent(
            new KeyboardEvent('keydown', {
                key,
                bubbles: true,
                composed: true,
                cancelable: true,
            })
        );
    }

    it('turns the active tab into an input', async () => {
        await doubleClick(2);

        const el = await input();
        expect(el, 'no rename input').to.not.be.null;
        expect(el!.value).to.equal('Budget');
    });

    it('leaves a tab that is not the active one alone', async () => {
        // The first click of the double-click already asked to switch to
        // it; Excel does not put two gestures on one action.
        await doubleClick(3);

        expect(await input()).to.be.null;
    });

    it('renames nothing in a protected workbook', async () => {
        sheet.setWorkbookProtected(true);
        await doubleClick(2);

        expect(await input()).to.be.null;
    });

    it('tells the server the new name on Enter', async () => {
        await doubleClick(2);
        const el = await type('Q4');
        events.length = 0;
        press(el, 'Enter');

        expect(events).to.have.length(1);
        expect(events[0].type).to.equal('sheetRenamed');
        // The server counts sheets from 0.
        expect(events[0].data).to.eql({ sheetIndex: 1, newName: 'Q4' });
    });

    it('shows the new name without waiting for the server', async () => {
        await doubleClick(2);
        press(await type('Q4'), 'Enter');

        const labels = (await tabs()).map((t) => t.textContent!.trim());
        expect(labels).to.eql(['Sheet1', 'Q4', 'Notes']);
    });

    it('keeps the name on Tab as well', async () => {
        await doubleClick(2);
        const el = await type('Q4');
        events.length = 0;
        press(el, 'Tab');

        expect(events.map((e) => e.type)).to.eql(['sheetRenamed']);
    });

    it('keeps the name when the input loses the focus', async () => {
        await doubleClick(2);
        const el = await type('Q4');
        events.length = 0;
        el.dispatchEvent(new FocusEvent('blur', { bubbles: true }));

        expect(events.map((e) => e.type)).to.eql(['sheetRenamed']);
    });

    it('throws the name away on Escape', async () => {
        await doubleClick(2);
        const el = await type('Q4');
        events.length = 0;
        press(el, 'Escape');

        expect(events).to.be.empty;
        expect(await input()).to.be.null;
        const labels = (await tabs()).map((t) => t.textContent!.trim());
        expect(labels).to.eql(['Sheet1', 'Budget', 'Notes']);
    });

    it('says nothing when the name has not changed', async () => {
        await doubleClick(2);
        const el = await type('Budget');
        events.length = 0;
        press(el, 'Enter');

        expect(events).to.be.empty;
    });

    it('says nothing when another sheet has the name', async () => {
        await doubleClick(2);
        const el = await type('Notes');
        events.length = 0;
        press(el, 'Enter');

        expect(events).to.be.empty;
        const labels = (await tabs()).map((t) => t.textContent!.trim());
        expect(labels).to.eql(['Sheet1', 'Budget', 'Notes']);
    });

    it('says nothing when the workbook would refuse the name', async () => {
        await doubleClick(2);
        const el = await type('a/b');
        events.length = 0;
        press(el, 'Enter');

        expect(events).to.be.empty;
    });

    it('reports the rename once, not again as the input leaves', async () => {
        // Ending a rename takes the input out of the DOM, and an input
        // that had the focus blurs on its way out.
        await doubleClick(2);
        const el = await type('Q4');
        el.focus();
        events.length = 0;
        press(el, 'Enter');
        el.dispatchEvent(new FocusEvent('blur', { bubbles: true }));

        expect(events.map((e) => e.type)).to.eql(['sheetRenamed']);
    });

    it('keeps what was typed when the strip repaints', async () => {
        // The strip repaints for its own reasons while a name is being
        // typed - a scroll, a sheet added elsewhere. Binding the value
        // as a property would put the old name back on every one of
        // them; as an attribute it only seeds an input nobody has
        // typed into yet.
        await doubleClick(2);
        const el = await type('Half-typed');
        // The server pushing the sheet names back is the repaint that
        // matters: the name this tab is bound to changes underneath the
        // input.
        sheet.setSheetNames(['Sheet1', 'Renamed elsewhere', 'Notes']);
        await sheet.getSheetTabs().updateComplete;

        expect((await input())!.value).to.equal('Half-typed');
        expect(el.isConnected, 'the input was replaced').to.be.true;
    });

    it('keeps the keys typed into the name from the page', async () => {
        // The page is where the sheet's own page listeners and the
        // application's shortcuts listen. The sheet's cells are not on the
        // way, so listening on them proved nothing.
        await doubleClick(2);
        const reachedPage: string[] = [];
        const listener = (e: KeyboardEvent) => reachedPage.push(e.key);
        document.addEventListener('keydown', listener);
        try {
            press((await input())!, 'a');
        } finally {
            document.removeEventListener('keydown', listener);
        }

        expect(reachedPage).to.deep.equal([]);
    });

    function compose(el: HTMLElement, key: string): void {
        el.dispatchEvent(
            new KeyboardEvent('keydown', { key, isComposing: true, bubbles: true, composed: true, cancelable: true })
        );
    }

    it('keeps the name open on the Enter that confirms a conversion', async () => {
        // The key is the input method's, as in a cell.
        await doubleClick(2);
        const el = await type('予算');
        events.length = 0;

        compose(el, 'Enter');

        expect(events).to.be.empty;
        expect(await input()).to.not.be.null;
    });

    it('keeps the name open on the Escape that drops a conversion', async () => {
        await doubleClick(2);
        const el = await type('予算');

        compose(el, 'Escape');

        expect(await input()).to.not.be.null;
    });
});
