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
    SpreadsheetColumnHeader,
    emptyColumnHeaderRenderState,
    type ColumnHeaderRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetColumnHeader.js';

/**
 * Slice 143 — column header strip.
 *
 * Pins the wire-format contract of the column-header element: one
 * {@code .col-header-cell} per visible column with the column letter
 * (A, B, ..., Z, AA, AB, ...) and proper left + width inline styles.
 */

async function attachWithState(
    state: ColumnHeaderRenderState
): Promise<SpreadsheetColumnHeader> {
    const el = document.createElement(
        'vaadin-spreadsheet-internal-column-header'
    ) as SpreadsheetColumnHeader;
    el.renderState = state;
    document.body.appendChild(el);
    await el.updateComplete;
    return el;
}

function cleanup(el: HTMLElement) {
    el.remove();
}

function buildState(opts: {
    firstColumnIndex: number;
    lastColumnIndex: number;
    firstColumnPosition: number;
    colX: number[];
    colWidths: number[];
    headerHeight: number;
    totalGridWidthPx?: number;
}): ColumnHeaderRenderState {
    const empty = emptyColumnHeaderRenderState();
    return {
        viewport: {
            firstColumnIndex: opts.firstColumnIndex,
            firstColumnPosition: opts.firstColumnPosition,
            lastColumnIndex: opts.lastColumnIndex,
            lastColumnPosition: 0,
            firstRowIndex: 1,
            firstRowPosition: 0,
            lastRowIndex: 0,
            lastRowPosition: 0,
        },
        colX: opts.colX,
        colWidths: opts.colWidths,
        headerHeight: opts.headerHeight,
        totalGridWidthPx: opts.totalGridWidthPx ?? empty.totalGridWidthPx,
    };
}

describe('emptyColumnHeaderRenderState — helper for tests + connector initial state', () => {
    it('returns an empty viewport with zero headerHeight', () => {
        const state = emptyColumnHeaderRenderState();

        expect(state.viewport.lastColumnIndex).to.be.lessThan(
            state.viewport.firstColumnIndex
        );
        expect(state.colX).to.deep.equal([]);
        expect(state.colWidths).to.deep.equal([]);
        expect(state.headerHeight).to.equal(0);
        expect(state.totalGridWidthPx).to.equal(0);
    });

    it('returns a fresh object on each call', () => {
        const a = emptyColumnHeaderRenderState();
        const b = emptyColumnHeaderRenderState();

        expect(a).to.not.equal(b);
    });
});

describe('SpreadsheetColumnHeader — element registration', () => {
    it('is a registered custom element', () => {
        expect(customElements.get('vaadin-spreadsheet-internal-column-header'))
            .to.equal(SpreadsheetColumnHeader);
    });

    it('is constructable with default state', () => {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-column-header'
        ) as SpreadsheetColumnHeader;

        expect(el).to.be.instanceOf(SpreadsheetColumnHeader);
        expect(el.renderState).to.deep.equal(emptyColumnHeaderRenderState());
    });
});

describe('SpreadsheetColumnHeader — render with header active', () => {
    it('renders nothing when headerHeight is 0', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 0,
            })
        );

        expect(el.shadowRoot!.querySelectorAll('.col-header-cell')).to.have.length(0);

        cleanup(el);
    });

    it('renders nothing when viewport is empty (lastCol < firstCol)', async () => {
        const el = await attachWithState({
            ...emptyColumnHeaderRenderState(),
            headerHeight: 24,
        });

        expect(el.shadowRoot!.querySelectorAll('.col-header-cell')).to.have.length(0);

        cleanup(el);
    });

    it('renders one cell per visible column with .col<N> classes', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells).to.have.length(3);
        expect(cells[0].classList.contains('col1')).to.be.true;
        expect(cells[1].classList.contains('col2')).to.be.true;
        expect(cells[2].classList.contains('col3')).to.be.true;

        cleanup(el);
    });

    it('renders Excel column letters: A, B, C, ..., Z, AA, AB', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells[0].textContent?.trim()).to.equal('A');
        expect(cells[1].textContent?.trim()).to.equal('B');
        expect(cells[2].textContent?.trim()).to.equal('C');

        cleanup(el);
    });

    it('renders letters past Z correctly (AA at col 27, AB at col 28)', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 27,
                lastColumnIndex: 28,
                firstColumnPosition: 1300,
                colX: [1300, 1350],
                colWidths: [50, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells[0].textContent?.trim()).to.equal('AA');
        expect(cells[1].textContent?.trim()).to.equal('AB');

        cleanup(el);
    });

    it('positions each header cell at colX[i] with width = colWidths[i]', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 60, 40],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll(
            '.col-header-cell'
        ) as NodeListOf<HTMLElement>;
        expect(cells[0].style.left).to.equal('0px');
        expect(cells[0].style.width).to.equal('50px');
        expect(cells[1].style.left).to.equal('50px');
        expect(cells[1].style.width).to.equal('60px');
        expect(cells[2].style.left).to.equal('100px');
        expect(cells[2].style.width).to.equal('40px');

        cleanup(el);
    });

    it('sizes each header cell height + line-height to the headerHeight', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 1,
                firstColumnPosition: 0,
                colX: [0],
                colWidths: [50],
                headerHeight: 30,
            })
        );

        const cell = el.shadowRoot!.querySelector(
            '.col-header-cell'
        ) as HTMLElement;
        expect(cell.style.height).to.equal('30px');
        // line-height = height so the letter is vertically centered.
        expect(cell.style.lineHeight).to.equal('30px');

        cleanup(el);
    });

    it('skips columns whose width is 0 (hidden columns)', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 50],
                colWidths: [50, 0, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        // Col 2 hidden → only 2 cells, for cols 1 and 3.
        expect(cells).to.have.length(2);
        expect(cells[0].classList.contains('col1')).to.be.true;
        expect(cells[1].classList.contains('col3')).to.be.true;

        cleanup(el);
    });

    it('host has position:sticky CSS so the strip pins to the top of the scroll container', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 1,
                firstColumnPosition: 0,
                colX: [0],
                colWidths: [50],
                headerHeight: 24,
            })
        );
        document.body.appendChild(el);

        const computed = getComputedStyle(el);
        // Verify the sticky positioning is applied from static styles.
        expect(computed.position).to.equal('sticky');

        cleanup(el);
    });
});

describe('SpreadsheetColumnHeader — click delegation (slice 148)', () => {
    it('puts data-col on each rendered header cell', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll(
            '.col-header-cell'
        ) as NodeListOf<HTMLElement>;
        expect(cells[0].dataset.col).to.equal('1');
        expect(cells[1].dataset.col).to.equal('2');
        expect(cells[2].dataset.col).to.equal('3');

        cleanup(el);
    });

    it('dispatches column-header-click on click with the column coord', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 24,
            })
        );

        let received: { col: number; shiftKey: boolean } | undefined;
        el.addEventListener('column-header-click', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            received = { col: detail.col, shiftKey: detail.shiftKey };
        });

        const cell = el.shadowRoot!.querySelector(
            '.col-header-cell.col2'
        ) as HTMLElement;
        cell.click();

        expect(received).to.deep.equal({ col: 2, shiftKey: false });
        cleanup(el);
    });

    it('column-header-click detail includes the shiftKey modifier', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 1,
                firstColumnPosition: 0,
                colX: [0],
                colWidths: [50],
                headerHeight: 24,
            })
        );

        let shiftKey = false;
        el.addEventListener('column-header-click', (e: Event) => {
            shiftKey = (e as CustomEvent).detail.shiftKey;
        });

        const cell = el.shadowRoot!.querySelector(
            '.col-header-cell'
        ) as HTMLElement;
        cell.dispatchEvent(new MouseEvent('click', {
            bubbles: true, composed: true, shiftKey: true,
        }));

        expect(shiftKey).to.be.true;
        cleanup(el);
    });
});

describe('SpreadsheetColumnHeader — re-renders on renderState reassignment', () => {
    it('updates cells when renderState changes', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 2,
                firstColumnPosition: 0,
                colX: [0, 50],
                colWidths: [50, 50],
                headerHeight: 24,
            })
        );

        expect(el.shadowRoot!.querySelectorAll('.col-header-cell')).to.have.length(2);

        el.renderState = buildState({
            firstColumnIndex: 5,
            lastColumnIndex: 6,
            firstColumnPosition: 200,
            colX: [200, 250],
            colWidths: [50, 50],
            headerHeight: 24,
        });
        await el.updateComplete;

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells).to.have.length(2);
        expect(cells[0].classList.contains('col5')).to.be.true;
        expect(cells[1].classList.contains('col6')).to.be.true;
        expect(cells[0].textContent?.trim()).to.equal('E');
        expect(cells[1].textContent?.trim()).to.equal('F');

        cleanup(el);
    });
});
