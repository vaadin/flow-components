/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import {
    SpreadsheetSheetTabs,
    emptySheetTabsRenderState,
    type SheetTabsRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetSheetTabs.js';

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
    host.style.cssText = `width:${width}px;height:${height}px;`;
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

describe('Spreadsheet — sheet tabs lifecycle through the connector (slice 154)', () => {
    it('connector creates a SpreadsheetSheetTabs element below the scroll container', () => {
        const { host, sheet, cleanup } = attachSheet(200, 100);
        expect(host.lastElementChild === sheet.getSheetTabs()).to.be.true;
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
        // sheetName = sheetNames[index - 1] = 'Beta'.
        expect(received!.data.sheetName).to.equal('Beta');

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
