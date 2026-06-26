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
    SpreadsheetRowHeader,
    emptyRowHeaderRenderState,
    type RowHeaderRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetRowHeader.js';

/**
 * Slice 144 — row header strip.
 *
 * Mirrors the column-header tests (slice 143). Pins the wire-format
 * contract of the row-header element: one
 * {@code .row-header-cell .row<N>} per visible row with the row's
 * 1-based number inside.
 */

async function attachWithState(
    state: RowHeaderRenderState
): Promise<SpreadsheetRowHeader> {
    const el = document.createElement(
        'vaadin-spreadsheet-internal-row-header'
    ) as SpreadsheetRowHeader;
    el.renderState = state;
    document.body.appendChild(el);
    await el.updateComplete;
    return el;
}

function cleanup(el: HTMLElement) {
    el.remove();
}

function buildState(opts: {
    firstRowIndex: number;
    lastRowIndex: number;
    firstRowPosition: number;
    rowY: number[];
    rowHeights: number[];
    headerWidth: number;
    totalGridHeightPx?: number;
}): RowHeaderRenderState {
    const empty = emptyRowHeaderRenderState();
    return {
        viewport: {
            firstColumnIndex: 1,
            firstColumnPosition: 0,
            lastColumnIndex: 0,
            lastColumnPosition: 0,
            firstRowIndex: opts.firstRowIndex,
            firstRowPosition: opts.firstRowPosition,
            lastRowIndex: opts.lastRowIndex,
            lastRowPosition: 0,
        },
        rowY: opts.rowY,
        rowHeights: opts.rowHeights,
        headerWidth: opts.headerWidth,
        totalGridHeightPx: opts.totalGridHeightPx ?? empty.totalGridHeightPx,
    };
}

describe('emptyRowHeaderRenderState — helper for tests + connector initial state', () => {
    it('returns an empty viewport with zero headerWidth', () => {
        const state = emptyRowHeaderRenderState();

        expect(state.viewport.lastRowIndex).to.be.lessThan(
            state.viewport.firstRowIndex
        );
        expect(state.rowY).to.deep.equal([]);
        expect(state.rowHeights).to.deep.equal([]);
        expect(state.headerWidth).to.equal(0);
        expect(state.totalGridHeightPx).to.equal(0);
    });

    it('returns a fresh object on each call', () => {
        const a = emptyRowHeaderRenderState();
        const b = emptyRowHeaderRenderState();

        expect(a === b).to.be.false;
    });
});

describe('SpreadsheetRowHeader — element registration', () => {
    it('is a registered custom element', () => {
        expect(customElements.get('vaadin-spreadsheet-internal-row-header'))
            .to.equal(SpreadsheetRowHeader);
    });

    it('is constructable with default state', () => {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-row-header'
        ) as SpreadsheetRowHeader;

        expect(el).to.be.instanceOf(SpreadsheetRowHeader);
        expect(el.renderState.headerWidth).to.equal(0);
        expect(el.renderState.rowY).to.deep.equal([]);
    });
});

describe('SpreadsheetRowHeader — render with header active', () => {
    it('renders nothing when headerWidth is 0', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 40],
                rowHeights: [20, 20, 20],
                headerWidth: 0,
            })
        );

        expect(el.shadowRoot!.querySelectorAll('.row-header-cell')).to.have.length(0);

        cleanup(el);
    });

    it('renders nothing when viewport is empty (lastRow < firstRow)', async () => {
        const el = await attachWithState({
            ...emptyRowHeaderRenderState(),
            headerWidth: 40,
        });

        expect(el.shadowRoot!.querySelectorAll('.row-header-cell')).to.have.length(0);

        cleanup(el);
    });

    it('renders one cell per visible row with .row<N> classes', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 40],
                rowHeights: [20, 20, 20],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        expect(cells).to.have.length(3);
        expect(cells[0].classList.contains('row1')).to.be.true;
        expect(cells[1].classList.contains('row2')).to.be.true;
        expect(cells[2].classList.contains('row3')).to.be.true;
    });

    it('renders the row number as text content (1-based)', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 10,
                lastRowIndex: 12,
                firstRowPosition: 200,
                rowY: [200, 220, 240],
                rowHeights: [20, 20, 20],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        expect(cells[0].textContent?.trim()).to.equal('10');
        expect(cells[1].textContent?.trim()).to.equal('11');
        expect(cells[2].textContent?.trim()).to.equal('12');
    });

    it('positions each header cell at rowY[i] with height = rowHeights[i]', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 60],
                rowHeights: [20, 40, 30],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll(
            '.row-header-cell'
        ) as NodeListOf<HTMLElement>;
        expect(cells[0].style.top).to.equal('0px');
        expect(cells[0].style.height).to.equal('20px');
        expect(cells[1].style.top).to.equal('20px');
        expect(cells[1].style.height).to.equal('40px');
        expect(cells[2].style.top).to.equal('60px');
        expect(cells[2].style.height).to.equal('30px');
    });

    it('sets the width of each header cell to headerWidth and left to 0', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 1,
                firstRowPosition: 0,
                rowY: [0],
                rowHeights: [20],
                headerWidth: 40,
            })
        );

        const cell = el.shadowRoot!.querySelector(
            '.row-header-cell'
        ) as HTMLElement;
        expect(cell.style.width).to.equal('40px');
        expect(cell.style.left).to.equal('0px');
    });

    it('line-height = height so the row number is vertically centered', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 1,
                firstRowPosition: 0,
                rowY: [0],
                rowHeights: [30],
                headerWidth: 40,
            })
        );

        const cell = el.shadowRoot!.querySelector(
            '.row-header-cell'
        ) as HTMLElement;
        expect(cell.style.lineHeight).to.equal('30px');
    });

    it('skips rows whose height is 0 (hidden rows)', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 20],
                rowHeights: [20, 0, 20],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        // Row 2 hidden → only 2 cells, for rows 1 and 3.
        expect(cells).to.have.length(2);
        expect(cells[0].classList.contains('row1')).to.be.true;
        expect(cells[1].classList.contains('row3')).to.be.true;
    });

    it('host has position:sticky CSS so the strip pins to the left of the scroll container', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 1,
                firstRowPosition: 0,
                rowY: [0],
                rowHeights: [20],
                headerWidth: 40,
            })
        );
        document.body.appendChild(el);

        const computed = getComputedStyle(el);
        expect(computed.position).to.equal('sticky');

        cleanup(el);
    });
});

describe('SpreadsheetRowHeader — re-renders on renderState reassignment', () => {
    it('updates cells when renderState changes', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 2,
                firstRowPosition: 0,
                rowY: [0, 20],
                rowHeights: [20, 20],
                headerWidth: 40,
            })
        );

        expect(el.shadowRoot!.querySelectorAll('.row-header-cell')).to.have.length(2);

        el.renderState = buildState({
            firstRowIndex: 100,
            lastRowIndex: 101,
            firstRowPosition: 1980,
            rowY: [1980, 2000],
            rowHeights: [20, 20],
            headerWidth: 40,
        });
        await el.updateComplete;

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        expect(cells).to.have.length(2);
        expect(cells[0].textContent?.trim()).to.equal('100');
        expect(cells[1].textContent?.trim()).to.equal('101');

        cleanup(el);
    });
});
