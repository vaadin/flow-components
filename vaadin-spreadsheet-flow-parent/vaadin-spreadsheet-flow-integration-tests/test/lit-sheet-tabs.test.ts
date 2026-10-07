/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { resetMouse, sendMouse } from '@web/test-runner-commands';

import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import {
    SpreadsheetSheetTabs,
    emptySheetTabsRenderState,
    type SheetTabsRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetSheetTabs.js';

/**
 * Slice 154 — sheet tabs.
 *
 * Element-level tests pin the wire-format contract for the
 * sheet-tab strip; connector-level tests verify setSheetNames /
 * setSheetIndex feed into the strip and that clicks dispatch
 * `sheetSelected` to the server.
 */

async function attachWithState(
    state: SheetTabsRenderState
): Promise<SpreadsheetSheetTabs> {
    const el = document.createElement(
        'vaadin-spreadsheet-internal-sheet-tabs'
    ) as SpreadsheetSheetTabs;
    el.renderState = state;
    document.body.appendChild(el);
    await el.updateComplete;
    return el;
}

function cleanup(el: HTMLElement) {
    el.remove();
}

function attachSheet(width: number, height: number): {
    host: HTMLElement;
    sheet: Spreadsheet;
    cleanup: () => void;
} {
    const host = document.createElement('div');
    host.style.cssText = `width:${width}px;height:${height}px;display:flex;flex-direction:column;`;
    document.body.appendChild(host);
    const sheet = new Spreadsheet(host);
    return {
        host,
        sheet,
        cleanup: () => {
            sheet.dispose();
            host.remove();
        },
    };
}

describe('emptySheetTabsRenderState — helper', () => {
    it('returns an empty tabs state', () => {
        const state = emptySheetTabsRenderState();
        expect(state.sheetNames).to.deep.equal([]);
        expect(state.activeIndex).to.equal(0);
    });

    it('returns a fresh object on each call', () => {
        const a = emptySheetTabsRenderState();
        const b = emptySheetTabsRenderState();
        expect(a === b).to.be.false;
    });
});

describe('SpreadsheetSheetTabs — element registration', () => {
    it('is a registered custom element', () => {
        expect(customElements.get('vaadin-spreadsheet-internal-sheet-tabs'))
            .to.equal(SpreadsheetSheetTabs);
    });

    it('is constructable with default state', () => {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-sheet-tabs'
        ) as SpreadsheetSheetTabs;
        expect(el).to.be.instanceOf(SpreadsheetSheetTabs);
    });
});

describe('SpreadsheetSheetTabs — render', () => {
    it('renders nothing when sheetNames is empty', async () => {
        const el = await attachWithState(emptySheetTabsRenderState());
        expect(el.shadowRoot!.querySelectorAll('.sheet-tab')).to.have.length(0);
        cleanup(el);
    });

    it('renders one tab per sheet name with the name as text content', async () => {
        const el = await attachWithState({
            sheetNames: ['Sheet1', 'Budget', 'Q4 Results'],
            activeIndex: 1,
        });
        const tabs = el.shadowRoot!.querySelectorAll('.sheet-tab');
        expect(tabs).to.have.length(3);
        expect(tabs[0].textContent?.trim()).to.equal('Sheet1');
        expect(tabs[1].textContent?.trim()).to.equal('Budget');
        expect(tabs[2].textContent?.trim()).to.equal('Q4 Results');
        cleanup(el);
    });

    it('marks the active tab with the .active class (1-based index)', async () => {
        const el = await attachWithState({
            sheetNames: ['Sheet1', 'Sheet2', 'Sheet3'],
            activeIndex: 2,
        });
        const tabs = el.shadowRoot!.querySelectorAll('.sheet-tab');
        expect(tabs[0].classList.contains('active')).to.be.false;
        expect(tabs[1].classList.contains('active')).to.be.true;
        expect(tabs[2].classList.contains('active')).to.be.false;
        cleanup(el);
    });

    it('renders NO active tab when activeIndex is 0', async () => {
        const el = await attachWithState({
            sheetNames: ['Sheet1', 'Sheet2'],
            activeIndex: 0,
        });
        const tabs = el.shadowRoot!.querySelectorAll('.sheet-tab');
        expect(tabs[0].classList.contains('active')).to.be.false;
        expect(tabs[1].classList.contains('active')).to.be.false;
        cleanup(el);
    });

    it('gives each tab its full name as the title', async () => {
        const el = await attachWithState({
            sheetNames: ['Sheet1', 'A rather long sheet name'],
            activeIndex: 1,
        });
        const tabs = el.shadowRoot!.querySelectorAll('.sheet-tab');
        expect(tabs[0].getAttribute('title')).to.equal('Sheet1');
        expect(tabs[1].getAttribute('title')).to.equal(
            'A rather long sheet name'
        );
        cleanup(el);
    });

    it('puts data-index on each tab (1-based)', async () => {
        const el = await attachWithState({
            sheetNames: ['A', 'B', 'C'],
            activeIndex: 1,
        });
        const tabs = el.shadowRoot!.querySelectorAll(
            '.sheet-tab'
        ) as NodeListOf<HTMLElement>;
        expect(tabs[0].dataset.index).to.equal('1');
        expect(tabs[1].dataset.index).to.equal('2');
        expect(tabs[2].dataset.index).to.equal('3');
        cleanup(el);
    });
});

describe('SpreadsheetSheetTabs — click delegation', () => {
    it('dispatches sheet-tab-click with the 1-based index on tab click', async () => {
        const el = await attachWithState({
            sheetNames: ['Sheet1', 'Sheet2', 'Sheet3'],
            activeIndex: 1,
        });

        let received: { index: number } | undefined;
        el.addEventListener('sheet-tab-click', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        const tab = el.shadowRoot!.querySelectorAll('.sheet-tab')[1] as HTMLElement;
        tab.click();

        expect(received).to.deep.equal({ index: 2 });
        cleanup(el);
    });

    it('clicks outside any tab are no-ops', async () => {
        const el = await attachWithState({
            sheetNames: ['A', 'B'],
            activeIndex: 1,
        });

        let received = false;
        el.addEventListener('sheet-tab-click', () => { received = true; });

        el.shadowRoot!.dispatchEvent(new MouseEvent('click', { bubbles: true }));

        expect(received).to.be.false;
        cleanup(el);
    });
});

describe('SpreadsheetSheetTabs — the strip buttons', () => {
    /**
     * Four scroll buttons and an add button, all documented as styling
     * parts. None of them existed in the Lit DOM: with more sheets than
     * fit, there was no way to reach the ones off the end.
     */
    // 200px leaves 70px beside the 130px of buttons: room for one 69px tab,
    // so these tabs run past the strip and the forward buttons have work.
    async function strip(names: string[], firstVisibleTab = 0, width = 200) {
        const el = document.createElement('vaadin-spreadsheet-internal-sheet-tabs') as SpreadsheetSheetTabs;
        el.style.width = `${width}px`;
        document.body.appendChild(el);
        el.renderState = {
            sheetNames: names,
            activeIndex: 1,
            firstVisibleTab,
            readOnly: false,
        };
        await el.updateComplete;
        // Measured after rendering; the buttons follow in a second pass.
        await el.updateComplete;
        return el;
    }

    function partOf(el: SpreadsheetSheetTabs, cls: string): string {
        return el.shadowRoot!.querySelector(`.${cls}`)!.getAttribute('part')!;
    }

    it('renders all five buttons', async () => {
        const el = await strip(['One', 'Two', 'Three']);
        for (const cls of ['scroll-tabs-beginning', 'scroll-tabs-left',
                           'scroll-tabs-right', 'scroll-tabs-end', 'add-new-tab']) {
            expect(el.shadowRoot!.querySelector(`.${cls}`), cls).to.not.be.null;
        }
        el.remove();
    });

    it('gives each button the part the styling docs promise', async () => {
        const el = await strip(['One', 'Two', 'Three'], 1);
        expect(partOf(el, 'scroll-tabs-beginning')).to.contain('scroll-tabs-to-start-button');
        expect(partOf(el, 'scroll-tabs-left')).to.contain('scroll-tabs-backward-button');
        expect(partOf(el, 'scroll-tabs-right')).to.contain('scroll-tabs-forward-button');
        expect(partOf(el, 'scroll-tabs-end')).to.contain('scroll-tabs-to-end-button');
        expect(partOf(el, 'add-new-tab')).to.equal('new-tab-button');
        el.remove();
    });

    it('marks the backward buttons disabled at the first tab', async () => {
        // A stylesheet greys them out through the part, so the state has
        // to be on the part and not only in a class.
        const el = await strip(['One', 'Two', 'Three'], 0);
        expect(partOf(el, 'scroll-tabs-beginning')).to.contain('scroll-tabs-button-disabled');
        expect(partOf(el, 'scroll-tabs-left')).to.contain('scroll-tabs-button-disabled');
        expect(partOf(el, 'scroll-tabs-right')).to.contain('scroll-tabs-button');
        expect(partOf(el, 'scroll-tabs-right')).to.not.contain('disabled');
        el.remove();
    });

    it('marks the forward buttons disabled at the last tab', async () => {
        const el = await strip(['One', 'Two', 'Three'], 2);
        expect(partOf(el, 'scroll-tabs-right')).to.contain('scroll-tabs-button-disabled');
        expect(partOf(el, 'scroll-tabs-end')).to.contain('scroll-tabs-button-disabled');
        expect(partOf(el, 'scroll-tabs-left')).to.not.contain('disabled');
        el.remove();
    });

    it('shows only the tabs from the first visible one', async () => {
        const el = await strip(['One', 'Two', 'Three'], 1);
        const shown = [...el.shadowRoot!.querySelectorAll('.sheet-tab')].map(
            (t) => t.textContent!.trim()
        );
        expect(shown).to.deep.equal(['Two', 'Three']);
        el.remove();
    });

    it('reports a scroll direction when a button is clicked', async () => {
        const el = await strip(['One', 'Two', 'Three'], 1);
        const seen: string[] = [];
        el.addEventListener('sheet-tabs-scroll', (e) =>
            seen.push((e as CustomEvent).detail.direction)
        );

        for (const cls of ['scroll-tabs-beginning', 'scroll-tabs-left',
                           'scroll-tabs-right', 'scroll-tabs-end']) {
            el.shadowRoot!.querySelector<HTMLElement>(`.${cls}`)!.click();
        }

        expect(seen).to.deep.equal(['start', 'backward', 'forward', 'end']);
        el.remove();
    });

    it('says nothing when a disabled button is clicked', async () => {
        const el = await strip(['One', 'Two', 'Three'], 0);
        const seen: string[] = [];
        el.addEventListener('sheet-tabs-scroll', (e) =>
            seen.push((e as CustomEvent).detail.direction)
        );

        el.shadowRoot!.querySelector<HTMLElement>('.scroll-tabs-left')!.click();
        el.shadowRoot!.querySelector<HTMLElement>('.scroll-tabs-beginning')!.click();

        expect(seen).to.deep.equal([]);
        el.remove();
    });

    it('asks for a new sheet when the add button is clicked', async () => {
        const el = await strip(['One']);
        let asked = 0;
        el.addEventListener('sheet-tab-add', () => (asked += 1));

        el.shadowRoot!.querySelector<HTMLElement>('.add-new-tab')!.click();

        expect(asked).to.equal(1);
        el.remove();
    });

    it('does not treat a button click as a tab click', async () => {
        const el = await strip(['One', 'Two'], 0);
        const tabClicks: number[] = [];
        el.addEventListener('sheet-tab-click', (e) =>
            tabClicks.push((e as CustomEvent).detail.index)
        );

        el.shadowRoot!.querySelector<HTMLElement>('.add-new-tab')!.click();
        el.shadowRoot!.querySelector<HTMLElement>('.scroll-tabs-right')!.click();

        expect(tabClicks).to.deep.equal([]);
        el.remove();
    });
});

/**
 * Scrolling forward only while there is something to scroll to.
 *
 * The GWT client enabled the forward buttons only while tabs ran past the
 * end of the strip, and stopped them at the leftmost tab that still showed
 * the last one. The Lit strip enabled them whenever the first tab shown was
 * not the last tab: two sheets that fitted easily still offered to scroll,
 * and the end button scrolled every tab but the last out of view.
 */
describe('SpreadsheetSheetTabs — scrolling forward only into tabs', () => {
    const FIVE = ['One', 'Two', 'Three', 'Four', 'Five'];

    // Each tab is 69px with its margin: a 50px minimum, 12px of padding,
    // 2px of border and 5px to its left.
    async function strip(
        names: string[],
        width: number,
        firstVisibleTab = 0,
        infoLabel: string | null = null
    ): Promise<SpreadsheetSheetTabs> {
        const el = document.createElement('vaadin-spreadsheet-internal-sheet-tabs') as SpreadsheetSheetTabs;
        el.style.width = `${width}px`;
        document.body.appendChild(el);
        el.renderState = {
            ...emptySheetTabsRenderState(),
            sheetNames: names,
            activeIndex: 1,
            firstVisibleTab,
            infoLabel,
        };
        await el.updateComplete;
        // The strip measures the tabs after it renders them, and renders
        // the buttons again if that changed anything.
        await el.updateComplete;
        return el;
    }

    function forwardDisabled(el: SpreadsheetSheetTabs): boolean[] {
        return ['scroll-tabs-right', 'scroll-tabs-end'].map((cls) => {
            const button = el.shadowRoot!.querySelector(`.${cls}`)!;
            const disabled = button.getAttribute('part')!.includes('scroll-tabs-button-disabled');
            expect(button.classList.contains('hidden'), cls).to.equal(disabled);
            return disabled;
        });
    }

    it('leaves them disabled while every tab fits', async () => {
        const el = await strip(['One', 'Two', 'Three'], 800);

        expect(forwardDisabled(el)).to.deep.equal([true, true]);

        el.remove();
    });

    it('enables them once the tabs run past the strip', async () => {
        // 300px leaves 170px: two tabs.
        const el = await strip(FIVE, 300);

        expect(forwardDisabled(el)).to.deep.equal([false, false]);

        el.remove();
    });

    it('stops them at the leftmost tab that still shows the last one', async () => {
        // Two tabs fit, so from the fourth tab the fifth is in view.
        const el = await strip(FIVE, 300, 3);

        expect(forwardDisabled(el)).to.deep.equal([true, true]);
        const shown = [...el.shadowRoot!.querySelectorAll('.sheet-tab')].map((t) => t.textContent!.trim());
        expect(shown).to.deep.equal(['Four', 'Five']);

        el.remove();
    });

    it('sends the end button to that tab rather than to the last', async () => {
        const el = await strip(FIVE, 300);
        let detail: { direction: string; lastFirstTab: number } | undefined;
        el.addEventListener('sheet-tabs-scroll', (e) => (detail = (e as CustomEvent).detail));

        el.shadowRoot!.querySelector<HTMLElement>('.scroll-tabs-end')!.click();

        expect(detail).to.deep.equal({ direction: 'end', lastFirstTab: 3 });

        el.remove();
    });

    it('counts the status label out of the room', async () => {
        // 420px leaves 290px, room for four tabs - until the 211px label
        // takes all but one tab's worth of it.
        const without = await strip(['One', 'Two', 'Three', 'Four'], 420);
        expect(forwardDisabled(without)).to.deep.equal([true, true]);
        without.remove();

        const withLabel = await strip(['One', 'Two', 'Three', 'Four'], 420, 0, 'Sum: 10');
        expect(forwardDisabled(withLabel)).to.deep.equal([false, false]);
        withLabel.remove();
    });

    it('looks again when the strip is resized', async () => {
        const el = await strip(['One', 'Two', 'Three'], 800);
        expect(forwardDisabled(el)).to.deep.equal([true, true]);

        el.style.width = '200px';
        await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
        await el.updateComplete;

        expect(forwardDisabled(el)).to.deep.equal([false, false]);

        el.remove();
    });
});

/**
 * The status label.
 *
 * `Spreadsheet.setStatusLabelValue` puts a label at the right end of the
 * sheet tabs. The Lit client kept the value and showed nothing, and the
 * TestBench element's `getInfoLabel()` returned null.
 */
describe('SpreadsheetSheetTabs — the status label', () => {
    async function strip(infoLabel: string | null | undefined): Promise<SpreadsheetSheetTabs> {
        const el = document.createElement('vaadin-spreadsheet-internal-sheet-tabs') as SpreadsheetSheetTabs;
        el.style.width = '800px';
        document.body.appendChild(el);
        el.renderState = {
            ...emptySheetTabsRenderState(),
            sheetNames: ['One', 'Two'],
            activeIndex: 1,
            infoLabel,
        };
        await el.updateComplete;
        return el;
    }

    function label(el: SpreadsheetSheetTabs): HTMLElement | null {
        return el.shadowRoot!.querySelector<HTMLElement>('.sheet-tabsheet-infolabel');
    }

    it('shows none until one is set', async () => {
        for (const value of [null, undefined]) {
            const el = await strip(value);
            expect(label(el), String(value)).to.be.null;
            expect(getComputedStyle(el.shadowRoot!.querySelector('.sheet-tabsheet-container')!).marginRight).to.equal(
                '0px'
            );
            el.remove();
        }
    });

    it('draws it at the right end as the GWT client did', async () => {
        const el = await strip('Sum: 42');

        const box = label(el)!.getBoundingClientRect();
        const host = el.getBoundingClientRect();
        expect(label(el)!.textContent).to.equal('Sum: 42');
        // 200px of text, 5px of padding either side and a 1px border, the
        // strip's height under its top border.
        expect(box.width).to.equal(211);
        expect(box.height).to.equal(28);
        expect(box.right).to.equal(host.right);
        expect(box.top).to.equal(host.top + 1);
        const style = getComputedStyle(label(el)!);
        expect(style.fontWeight).to.equal('700');
        expect(style.fontSize).to.equal('11px');
        expect(style.lineHeight).to.equal('29px');
        expect(style.backgroundColor).to.equal('rgb(250, 250, 250)');
        expect(style.borderLeft).to.equal('1px solid rgb(199, 199, 199)');
        // The tabs keep out from under it.
        expect(getComputedStyle(el.shadowRoot!.querySelector('.sheet-tabsheet-container')!).marginRight).to.equal(
            '206px'
        );

        el.remove();
    });

    it('shows an empty label for an empty string', async () => {
        const el = await strip('');

        expect(label(el)).to.not.be.null;
        expect(label(el)!.getBoundingClientRect().width).to.equal(211);

        el.remove();
    });

    it('cuts a long one short with an ellipsis', async () => {
        const el = await strip('A status far too long to fit in two hundred pixels of bold eleven point type');

        const style = getComputedStyle(label(el)!);
        expect(style.textOverflow).to.equal('ellipsis');
        expect(style.whiteSpace).to.equal('nowrap');
        expect(label(el)!.scrollWidth).to.be.greaterThan(label(el)!.clientWidth);

        el.remove();
    });
});

/**
 * The strip, drawn as the GWT client drew it.
 *
 * The Lit strip was 34px tall with flat tabs stretched to its height and
 * the active one underlined, so it took more room from the sheet than it
 * used to and looked nothing like the strip users knew: folder tabs hanging
 * from the top border, the active one white with its name in blue.
 */
describe('SpreadsheetSheetTabs — drawn as the GWT client drew it', () => {
    const LONG = 'A sheet name far too long to show whole in a tab';

    async function strip(): Promise<SpreadsheetSheetTabs> {
        const el = await attachWithState({
            ...emptySheetTabsRenderState(),
            sheetNames: ['Sheet1', 'Sheet2', LONG],
            activeIndex: 1,
        });
        el.style.width = '900px';
        await el.updateComplete;
        return el;
    }

    function tab(el: SpreadsheetSheetTabs, index: number): HTMLElement {
        return el.shadowRoot!.querySelectorAll<HTMLElement>('.sheet-tab')[index];
    }

    it('is 29px tall, its top border included', async () => {
        const el = await strip();

        expect(el.getBoundingClientRect().height).to.equal(29);

        cleanup(el);
    });

    it('hangs each tab from the top border, 25px tall with rounded corners', async () => {
        const el = await strip();

        const sheet2 = tab(el, 1);
        const box = sheet2.getBoundingClientRect();
        expect(box.top - el.getBoundingClientRect().top, 'right under the border').to.equal(1);
        expect(box.height).to.equal(25);
        const style = getComputedStyle(sheet2);
        expect(style.borderTopStyle).to.equal('none');
        expect(style.borderBottomLeftRadius).to.equal('5px');

        cleanup(el);
    });

    it('starts the tabs after the 130px of buttons', async () => {
        const el = await strip();

        const left = tab(el, 0).getBoundingClientRect().left - el.getBoundingClientRect().left;
        expect(left).to.equal(130 + 5);

        cleanup(el);
    });

    it('writes the other names in light grey, the active one in blue on white', async () => {
        const el = await strip();

        const other = getComputedStyle(tab(el, 1));
        expect(other.color).to.equal('rgb(106, 106, 106)');
        expect(other.fontWeight).to.equal('300');
        const active = getComputedStyle(tab(el, 0));
        expect(active.color).to.equal('rgb(37, 132, 226)');
        expect(active.backgroundColor).to.equal('rgb(255, 255, 255)');
        expect(active.borderBottomWidth, 'no underline').to.equal('1px');

        cleanup(el);
    });

    it("turns a button GWT's blue under the pointer, whatever the active tab's colour", async () => {
        // The active tab's colour is the app's to set through its property;
        // the GWT client's buttons kept their own blue.
        const el = await strip();
        el.style.setProperty('--vaadin-spreadsheet-tabs-active-fg', 'rgb(200, 0, 0)');
        const add = el.shadowRoot!.querySelector<HTMLElement>('.add-new-tab')!;
        const box = add.getBoundingClientRect();

        await sendMouse({ type: 'move', position: [Math.round(box.left + box.width / 2), Math.round(box.top + box.height / 2)] });
        try {
            expect(getComputedStyle(add).color).to.equal('rgb(25, 125, 225)');
            expect(getComputedStyle(tab(el, 0)).color, 'the active tab').to.equal('rgb(200, 0, 0)');
        } finally {
            await resetMouse();
            cleanup(el);
        }
    });

    it('cuts a long name short with an ellipsis', async () => {
        const el = await strip();

        const long = tab(el, 2);
        expect(long.getBoundingClientRect().width).to.equal(200 + 2 * 6 + 2);
        expect(getComputedStyle(long).textOverflow).to.equal('ellipsis');
        expect(long.scrollWidth).to.be.greaterThan(long.clientWidth);

        cleanup(el);
    });
});

describe('Spreadsheet — sheet tabs lifecycle through the connector (slice 154)', () => {
    it('connector creates a SpreadsheetSheetTabs element below the scroll container', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        expect(sheet.getRoot().lastElementChild === sheet.getSheetTabs()).to.be.true;
        expect(sheet.getSheetTabs()).to.be.instanceOf(SpreadsheetSheetTabs);
        cleanup();
    });

    it('setSheetNames pushes the names into the sheet-tabs render state', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setSheetNames(['Inputs', 'Results']);
        const state = sheet.getSheetTabs().renderState;
        expect(Array.from(state.sheetNames)).to.deep.equal(['Inputs', 'Results']);
        cleanup();
    });

    it('setSheetIndex updates the active tab in the strip', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setSheetNames(['One', 'Two', 'Three']);
        sheet.setSheetIndex(3);
        expect(sheet.getSheetTabs().renderState.activeIndex).to.equal(3);
        cleanup();
    });

    it('sheet-tab click dispatches sheetSelected spreadsheet-event', () => {
        const { host, sheet, cleanup } = attachSheet(200, 100);
        sheet.setSheetNames(['Alpha', 'Beta']);
        sheet.setSheetIndex(1);

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'sheetSelected') {
                received = detail;
            }
        });

        sheet.getSheetTabs().dispatchEvent(new CustomEvent('sheet-tab-click', {
            detail: { index: 2 },
            bubbles: true,
            composed: true,
        }));

        expect(received).to.exist;
        expect(received!.type).to.equal('sheetSelected');
        // The server reads a 0-based tab index; the strip renders 1-based.
        expect(received!.data.sheetIndex).to.equal(1);

        cleanup();
    });

    it('clicking the tab that is already active reports nothing', () => {
        // The server answers sheetSelected with setActiveSheetIndex, which
        // runs reloadActiveSheetData even when the index has not moved. That
        // discards the overlays, comments, hyperlinks, tables and popup
        // buttons loadCells had just delivered, and only a scroll brings
        // them back. Excel does nothing when you click the active tab.
        const { host, sheet, cleanup } = attachSheet(200, 100);
        sheet.setSheetNames(['Alpha', 'Beta']);
        sheet.setSheetIndex(1);

        const received: string[] = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            received.push((e as CustomEvent).detail.type);
        });

        sheet.getSheetTabs().dispatchEvent(new CustomEvent('sheet-tab-click', {
            detail: { index: 1 },
            bubbles: true,
            composed: true,
        }));

        expect(received).to.not.include('sheetSelected');
        expect(sheet.getSheetTabs().renderState.activeIndex).to.equal(1);
        cleanup();
    });

    it('still reports a move away from the active tab and back to it', () => {
        const { host, sheet, cleanup } = attachSheet(200, 100);
        sheet.setSheetNames(['Alpha', 'Beta']);
        sheet.setSheetIndex(1);

        const indexes: unknown[] = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'sheetSelected') {
                indexes.push(detail.data.sheetIndex);
            }
        });

        const click = (index: number) =>
            sheet.getSheetTabs().dispatchEvent(new CustomEvent('sheet-tab-click', {
                detail: { index },
                bubbles: true,
                composed: true,
            }));
        click(2);
        click(1);

        expect(indexes).to.deep.equal([1, 0]);
        cleanup();
    });

    it('sheet-tab click optimistically updates the active index BEFORE the server responds', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setSheetNames(['Alpha', 'Beta', 'Gamma']);
        sheet.setSheetIndex(1);
        expect(sheet.getSheetTabs().renderState.activeIndex).to.equal(1);

        sheet.getSheetTabs().dispatchEvent(new CustomEvent('sheet-tab-click', {
            detail: { index: 3 },
            bubbles: true,
            composed: true,
        }));

        // Optimistic update — server's setSheetIndex(3) hasn't
        // landed yet but the tab strip already reflects the click.
        expect(sheet.getSheetTabs().renderState.activeIndex).to.equal(3);
        cleanup();
    });
});

describe('Spreadsheet — scrolling the tab strip', () => {
    function attach(names: string[]): {
        host: HTMLElement;
        sheet: Spreadsheet;
        cleanup: () => void;
    } {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setSheetNames(names);
        sheet.setSheetIndex(1);
        return {
            host,
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    function scroll(sheet: Spreadsheet, direction: string): void {
        sheet.getSheetTabs().dispatchEvent(
            new CustomEvent('sheet-tabs-scroll', {
                detail: { direction },
                bubbles: true,
                composed: true,
            })
        );
    }

    it('moves one tab at a time', () => {
        const { sheet, cleanup } = attach(['One', 'Two', 'Three']);
        scroll(sheet, 'forward');
        expect(sheet.getSheetTabs().renderState.firstVisibleTab).to.equal(1);
        scroll(sheet, 'forward');
        expect(sheet.getSheetTabs().renderState.firstVisibleTab).to.equal(2);
        scroll(sheet, 'backward');
        expect(sheet.getSheetTabs().renderState.firstVisibleTab).to.equal(1);
        cleanup();
    });

    it('jumps to either end', () => {
        const { sheet, cleanup } = attach(['One', 'Two', 'Three', 'Four']);
        scroll(sheet, 'end');
        expect(sheet.getSheetTabs().renderState.firstVisibleTab).to.equal(3);
        scroll(sheet, 'start');
        expect(sheet.getSheetTabs().renderState.firstVisibleTab).to.equal(0);
        cleanup();
    });

    it('stops at the ends rather than running past them', () => {
        const { sheet, cleanup } = attach(['One', 'Two']);
        scroll(sheet, 'backward');
        expect(sheet.getSheetTabs().renderState.firstVisibleTab).to.equal(0);
        scroll(sheet, 'forward');
        scroll(sheet, 'forward');
        scroll(sheet, 'forward');
        expect(sheet.getSheetTabs().renderState.firstVisibleTab).to.equal(1);
        cleanup();
    });

    it('tells the server nothing — the strip position is the client\'s', () => {
        // The GWT client told its own handler, but the Flow server has no
        // counterpart. Scrolling the strip does not change the sheet.
        const { host, sheet, cleanup } = attach(['One', 'Two', 'Three']);
        const events: string[] = [];
        host.addEventListener('spreadsheet-event', (e) =>
            events.push((e as CustomEvent).detail.type)
        );

        scroll(sheet, 'forward');

        expect(events).to.deep.equal([]);
        cleanup();
    });

    it('asks the server for a new sheet, with the scroll position', () => {
        const { host, sheet, cleanup } = attach(['One']);
        const events: any[] = [];
        host.addEventListener('spreadsheet-event', (e) =>
            events.push((e as CustomEvent).detail)
        );

        sheet.getSheetTabs().dispatchEvent(
            new CustomEvent('sheet-tab-add', { bubbles: true, composed: true })
        );

        const created = events.find((e) => e.type === 'sheetCreated');
        expect(created).to.not.be.undefined;
        expect(created.data).to.have.keys(['scrollLeft', 'scrollTop']);
        cleanup();
    });

    it('adds no sheet to a protected workbook', () => {
        const { host, sheet, cleanup } = attach(['One']);
        sheet.setWorkbookProtected(true);
        const events: string[] = [];
        host.addEventListener('spreadsheet-event', (e) =>
            events.push((e as CustomEvent).detail.type)
        );

        sheet.getSheetTabs().dispatchEvent(
            new CustomEvent('sheet-tab-add', { bubbles: true, composed: true })
        );

        expect(events).to.not.include('sheetCreated');
        cleanup();
    });

    it('ends at the leftmost tab that still shows the last one', async () => {
        // 400px leaves 270px beside the buttons: three 69px tabs. The end
        // button used to scroll all but the last tab out of view.
        const { sheet, cleanup } = attach(['One', 'Two', 'Three', 'Four', 'Five']);
        const tabs = sheet.getSheetTabs();
        await tabs.updateComplete;
        await tabs.updateComplete;

        tabs.shadowRoot!.querySelector<HTMLElement>('.scroll-tabs-end')!.click();
        await tabs.updateComplete;
        await tabs.updateComplete;

        expect(tabs.renderState.firstVisibleTab).to.equal(2);
        expect(tabs.shadowRoot!.querySelector('.scroll-tabs-right')!.classList.contains('hidden')).to.be.true;
        cleanup();
    });
});

describe('Spreadsheet — the status label', () => {
    function attach(): { sheet: Spreadsheet; cleanup: () => void } {
        const host = document.createElement('div');
        host.style.cssText = 'width:600px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setSheetNames(['One']);
        sheet.setSheetIndex(1);
        return {
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    async function shownLabel(sheet: Spreadsheet): Promise<string | null> {
        const tabs = sheet.getSheetTabs();
        await tabs.updateComplete;
        return tabs.shadowRoot!.querySelector('.sheet-tabsheet-infolabel')?.textContent ?? null;
    }

    it('shows none until the server sets one', async () => {
        const { sheet, cleanup } = attach();

        expect(await shownLabel(sheet)).to.be.null;

        cleanup();
    });

    it('shows what the server sets, and hides it when the server clears it', async () => {
        const { sheet, cleanup } = attach();

        sheet.setInfoLabelValue('Sum: 42');
        expect(await shownLabel(sheet)).to.equal('Sum: 42');

        sheet.setInfoLabelValue(null);
        expect(await shownLabel(sheet)).to.be.null;

        sheet.setInfoLabelValue('');
        expect(await shownLabel(sheet)).to.equal('');

        sheet.setInfoLabelValue(undefined);
        expect(await shownLabel(sheet)).to.be.null;

        cleanup();
    });
});

/**
 * Moving between sheets is navigation, so the keyboard has to end up on the
 * grid — not on the tab or the strip button that was just clicked. Without
 * this the arrow keys that naturally follow a sheet switch scroll the tab
 * strip instead of moving the selection.
 */
describe('Spreadsheet — focus after a sheet switch', () => {
    function attach(names: string[]): {
        host: HTMLElement;
        sheet: Spreadsheet;
        cleanup: () => void;
    } {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setSheetNames(names);
        sheet.setSheetIndex(1);
        return {
            host,
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    function clickTab(sheet: Spreadsheet, index: number): void {
        sheet.getSheetTabs().dispatchEvent(
            new CustomEvent('sheet-tab-click', {
                detail: { index },
                bubbles: true,
                composed: true,
            })
        );
    }

    it('focuses the sheet after switching to another tab', () => {
        const { sheet, cleanup } = attach(['One', 'Two']);
        document.body.focus();
        clickTab(sheet, 2);
        expect(sheet.getScrollContainer().matches(':focus')).to.be.true;
        cleanup();
    });

    it('focuses the sheet after clicking the tab already active', () => {
        const { sheet, cleanup } = attach(['One', 'Two']);
        document.body.focus();
        clickTab(sheet, 1);
        expect(sheet.getScrollContainer().matches(':focus')).to.be.true;
        cleanup();
    });

    it('focuses the sheet after adding one', () => {
        const { sheet, cleanup } = attach(['One']);
        document.body.focus();
        sheet.getSheetTabs().dispatchEvent(
            new CustomEvent('sheet-tab-add', { bubbles: true, composed: true })
        );
        expect(sheet.getScrollContainer().matches(':focus')).to.be.true;
        cleanup();
    });

    it('leaves the focus alone when a protected workbook refuses the add', () => {
        const { sheet, cleanup } = attach(['One']);
        sheet.setWorkbookProtected(true);
        document.body.focus();
        sheet.getSheetTabs().dispatchEvent(
            new CustomEvent('sheet-tab-add', { bubbles: true, composed: true })
        );
        expect(sheet.getScrollContainer().matches(':focus')).to.be.false;
        cleanup();
    });
});

describe('SpreadsheetSheetTabs — the strip buttons', () => {
    const BUTTONS = [
        'scroll-tabs-beginning',
        'scroll-tabs-left',
        'scroll-tabs-right',
        'scroll-tabs-end',
        'add-new-tab',
    ];

    it('draws an icon in each button rather than a filled box', async () => {
        // The Lit client kept the GWT client's five buttons but not their
        // icons, so they rendered as five grey squares that said nothing
        // about what they did.
        const el = await attachWithState({
            ...emptySheetTabsRenderState(),
            sheetNames: ['One', 'Two'],
            activeIndex: 1,
        });

        for (const cls of BUTTONS) {
            const button = el.shadowRoot!.querySelector<HTMLElement>('.' + cls)!;
            expect(button, cls).to.not.be.null;
            const icon = getComputedStyle(button, '::before');
            const mask =
                icon.getPropertyValue('mask-image') ||
                icon.getPropertyValue('-webkit-mask-image');
            expect(mask, cls + ' carries an icon').to.contain('data:image/svg+xml');
            expect(
                getComputedStyle(button).backgroundColor,
                cls + ' is not a filled box'
            ).to.equal('rgba(0, 0, 0, 0)');
        }

        el.remove();
    });

    it('greys out a button that has nowhere to scroll', async () => {
        const el = await attachWithState({
            ...emptySheetTabsRenderState(),
            sheetNames: ['One'],
            activeIndex: 1,
        });

        const start = el.shadowRoot!.querySelector<HTMLElement>('.scroll-tabs-beginning')!;
        expect(start.classList.contains('hidden')).to.be.true;
        expect(getComputedStyle(start).color).to.equal('rgb(214, 214, 214)');

        el.remove();
    });
});
