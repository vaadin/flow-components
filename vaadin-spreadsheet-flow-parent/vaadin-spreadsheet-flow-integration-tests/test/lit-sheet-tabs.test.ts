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
    async function strip(names: string[], firstVisibleTab = 0) {
        const el = document.createElement('vaadin-spreadsheet-internal-sheet-tabs') as SpreadsheetSheetTabs;
        document.body.appendChild(el);
        el.renderState = {
            sheetNames: names,
            activeIndex: 1,
            firstVisibleTab,
            readOnly: false,
        };
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
