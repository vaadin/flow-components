/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import {
    type CellGridRenderState,
    emptyCellGridRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/elements/types.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * Slice 132's cell-grid tests pin the wire-format-class-name +
 * absolute-positioning contract. Render-correctness for styling /
 * selection / merged-cell overlays is deferred to slices 135-137.
 */

function makeCellData(col: number, row: number, value: string): CellData {
    return {
        row,
        col,
        value,
        cellStyle: 'cs0',
        locked: false,
        needsMeasure: false,
        isPercentage: false,
    };
}

async function attachWithState(state: CellGridRenderState): Promise<SpreadsheetCellGrid> {
    const el = document.createElement(
        'vaadin-spreadsheet-internal-cell-grid'
    ) as SpreadsheetCellGrid;
    el.renderState = state;
    document.body.appendChild(el);
    await el.updateComplete;
    return el;
}

function cleanup(el: HTMLElement) {
    el.remove();
}

describe('SpreadsheetCellGrid — element registration', () => {
    it('is a registered custom element', () => {
        expect(customElements.get('vaadin-spreadsheet-internal-cell-grid'))
            .to.equal(SpreadsheetCellGrid);
    });

    it('is constructable with default state', () => {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-cell-grid'
        ) as SpreadsheetCellGrid;

        expect(el).to.be.instanceOf(SpreadsheetCellGrid);
        expect(el.renderState).to.deep.equal(emptyCellGridRenderState());
    });
});

describe('SpreadsheetCellGrid — empty viewport', () => {
    it('renders nothing when the viewport range is empty', async () => {
        const el = await attachWithState(emptyCellGridRenderState());

        const cells = el.shadowRoot!.querySelectorAll('.cell');
        expect(cells).to.have.length(0);

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — non-empty render', () => {
    function build3x2State(): CellGridRenderState {
        // Visible: cols 1..3, rows 1..2. Uniform 50px × 20px cells.
        return {
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 3,
                lastColumnPosition: 150,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 2,
                lastRowPosition: 40,
            },
            cellData: new Map<string, CellData>([
                ['col1 row1', makeCellData(1, 1, 'A1')],
                ['col2 row1', makeCellData(2, 1, 'B1')],
                ['col1 row2', makeCellData(1, 2, 'A2')],
            ]),
            colX: [0, 50, 100],
            rowY: [0, 20],
            colWidths: [50, 50, 50],
            rowHeights: [20, 20],
            mergedRegions: [],
            selection: {
                activeCell: { col: 1, row: 1 },
                activeRange: { c1: 1, c2: 1, r1: 1, r2: 1 },
                auxRanges: [],
            },
        };
    }

    it('renders one cell per (col, row) in the visible range', async () => {
        const el = await attachWithState(build3x2State());

        const cells = el.shadowRoot!.querySelectorAll('.cell');
        // 3 cols × 2 rows = 6 cells, regardless of cellData population.
        expect(cells).to.have.length(6);

        cleanup(el);
    });

    it('applies "col<C> row<R>" CSS classes (wire-format contract)', async () => {
        const el = await attachWithState(build3x2State());

        // Cell at (col 2, row 1) must have class="cell col2 row1".
        const cell = el.shadowRoot!.querySelector('.col2.row1');
        expect(cell).to.not.be.null;
        expect(cell!.classList.contains('cell')).to.be.true;
        expect(cell!.classList.contains('col2')).to.be.true;
        expect(cell!.classList.contains('row1')).to.be.true;

        cleanup(el);
    });

    it('positions each cell absolutely from colX + rowY', async () => {
        const el = await attachWithState(build3x2State());

        // Cell (col 2, row 1): colX[1]=50, rowY[0]=0, width 50, height 20.
        const cell = el.shadowRoot!.querySelector('.col2.row1') as HTMLElement;
        expect(cell.style.left).to.equal('50px');
        expect(cell.style.top).to.equal('0px');
        expect(cell.style.width).to.equal('50px');
        expect(cell.style.height).to.equal('20px');

        cleanup(el);
    });

    it('renders cellData value when present', async () => {
        const el = await attachWithState(build3x2State());

        const a1 = el.shadowRoot!.querySelector('.col1.row1');
        expect(a1!.textContent?.trim()).to.equal('A1');

        const b1 = el.shadowRoot!.querySelector('.col2.row1');
        expect(b1!.textContent?.trim()).to.equal('B1');

        cleanup(el);
    });

    it('renders empty text for coordinates without cellData', async () => {
        const el = await attachWithState(build3x2State());

        // (col 3, row 1) and (col 3, row 2) have no cellData entry.
        const c1 = el.shadowRoot!.querySelector('.col3.row1');
        expect(c1!.textContent?.trim()).to.equal('');

        const c2 = el.shadowRoot!.querySelector('.col3.row2');
        expect(c2!.textContent?.trim()).to.equal('');

        cleanup(el);
    });

    it('handles null cellData.value as empty text', async () => {
        const state = build3x2State();
        const cellData = new Map(state.cellData);
        cellData.set('col1 row1', { ...makeCellData(1, 1, ''), value: null });

        const el = await attachWithState({ ...state, cellData });

        const cell = el.shadowRoot!.querySelector('.col1.row1');
        expect(cell!.textContent?.trim()).to.equal('');

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — hidden rows + columns', () => {
    it('skips rendering rows whose height is 0 (hidden)', async () => {
        const state: CellGridRenderState = {
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 2,
                lastColumnPosition: 100,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 3,
                lastRowPosition: 40,
            },
            cellData: new Map(),
            colX: [0, 50],
            rowY: [0, 0, 20],
            colWidths: [50, 50],
            rowHeights: [20, 0, 20], // row 2 hidden
            mergedRegions: [],
            selection: emptyCellGridRenderState().selection,
        };
        const el = await attachWithState(state);

        // 2 cols × 2 visible rows (row 2 is hidden) = 4 cells.
        const cells = el.shadowRoot!.querySelectorAll('.cell');
        expect(cells).to.have.length(4);
        expect(el.shadowRoot!.querySelector('.row2')).to.be.null;
        expect(el.shadowRoot!.querySelector('.row1')).to.not.be.null;
        expect(el.shadowRoot!.querySelector('.row3')).to.not.be.null;

        cleanup(el);
    });

    it('skips rendering columns whose width is 0 (hidden)', async () => {
        const state: CellGridRenderState = {
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 3,
                lastColumnPosition: 100,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 1,
                lastRowPosition: 20,
            },
            cellData: new Map(),
            colX: [0, 0, 50],
            rowY: [0],
            colWidths: [50, 0, 50], // col 2 hidden
            rowHeights: [20],
            mergedRegions: [],
            selection: emptyCellGridRenderState().selection,
        };
        const el = await attachWithState(state);

        const cells = el.shadowRoot!.querySelectorAll('.cell');
        expect(cells).to.have.length(2); // col 1 + col 3
        expect(el.shadowRoot!.querySelector('.col2')).to.be.null;

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — render state updates', () => {
    it('re-renders when renderState is reassigned', async () => {
        const el = await attachWithState({
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 1,
                lastColumnPosition: 50,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 1,
                lastRowPosition: 20,
            },
            cellData: new Map([['col1 row1', makeCellData(1, 1, 'first')]]),
            colX: [0],
            rowY: [0],
            colWidths: [50],
            rowHeights: [20],
            mergedRegions: [],
            selection: emptyCellGridRenderState().selection,
        });

        expect(el.shadowRoot!.querySelector('.col1.row1')!.textContent?.trim())
            .to.equal('first');

        // Re-assign with different data.
        el.renderState = {
            ...el.renderState,
            cellData: new Map([['col1 row1', makeCellData(1, 1, 'updated')]]),
        };
        await el.updateComplete;

        expect(el.shadowRoot!.querySelector('.col1.row1')!.textContent?.trim())
            .to.equal('updated');

        cleanup(el);
    });
});
