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
            cellStyleToCSSStyle: {},
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
            cellStyleToCSSStyle: {},
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
            cellStyleToCSSStyle: {},
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

describe('SpreadsheetCellGrid — cell styling (slice 135)', () => {
    function build1x1State(
        cellData: Map<string, CellData>,
        cellStyleToCSSStyle: Record<number, string>
    ): CellGridRenderState {
        return {
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
            cellData,
            colX: [0],
            rowY: [0],
            colWidths: [50],
            rowHeights: [20],
            cellStyleToCSSStyle,
            mergedRegions: [],
            selection: emptyCellGridRenderState().selection,
        };
    }

    it('emits a <style> block with the cellStyleToCSSStyle rules', async () => {
        const el = await attachWithState(
            build1x1State(new Map(), {
                0: 'font-weight: bold;',
                3: 'color: red;',
            })
        );

        const styleEl = el.shadowRoot!.querySelector('style:not([data-style-source])');
        // The Lit-injected <style> for component styles is one element;
        // the cell-style block is a sibling. Lit's static styles use a
        // <style> too, so we look for ours by content match.
        const allStyles = el.shadowRoot!.querySelectorAll('style');
        const ourStyles = Array.from(allStyles).filter((s) =>
            (s.textContent ?? '').includes('.cell.cs')
        );
        expect(ourStyles).to.have.length.greaterThanOrEqual(1);
        const cssText = ourStyles.map((s) => s.textContent ?? '').join('\n');
        expect(cssText).to.include('.cell.cs0 { font-weight: bold; }');
        expect(cssText).to.include('.cell.cs3 { color: red; }');

        cleanup(el);
    });

    it('does not emit a <style> block when cellStyleToCSSStyle is empty', async () => {
        const el = await attachWithState(build1x1State(new Map(), {}));

        const allStyles = el.shadowRoot!.querySelectorAll('style');
        const ourStyles = Array.from(allStyles).filter((s) =>
            (s.textContent ?? '').includes('.cell.cs')
        );
        expect(ourStyles).to.have.length(0);

        cleanup(el);
    });

    it('applies the cellStyle class from cellData to the rendered cell', async () => {
        const cellData = new Map<string, CellData>([
            ['col1 row1', { ...makeCellData(1, 1, 'X'), cellStyle: 'cs7' }],
        ]);
        const el = await attachWithState(
            build1x1State(cellData, { 7: 'background: yellow;' })
        );

        const cell = el.shadowRoot!.querySelector('.col1.row1');
        expect(cell!.classList.contains('cs7')).to.be.true;
        expect(cell!.classList.contains('cell')).to.be.true;

        cleanup(el);
    });

    it('omits the cellStyle class when cellData has none', async () => {
        // Cell at (1,1) has no cellData entry.
        const el = await attachWithState(
            build1x1State(new Map(), { 0: 'font-weight: bold;' })
        );

        const cell = el.shadowRoot!.querySelector('.col1.row1') as HTMLElement;
        // Should have "cell col1 row1" but NOT any "cs<N>" class.
        const csClasses = Array.from(cell.classList).filter((c) => c.startsWith('cs'));
        expect(csClasses).to.have.length(0);

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — selection overlays (slice 136)', () => {
    function build3x3State(
        selection: CellGridRenderState['selection']
    ): CellGridRenderState {
        return {
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 3,
                lastColumnPosition: 150,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 3,
                lastRowPosition: 60,
            },
            cellData: new Map(),
            colX: [0, 50, 100],
            rowY: [0, 20, 40],
            colWidths: [50, 50, 50],
            rowHeights: [20, 20, 20],
            cellStyleToCSSStyle: {},
            mergedRegions: [],
            selection,
        };
    }

    it('does NOT add cell-range to the active cell', async () => {
        // Single-cell selection at (2, 2).
        const el = await attachWithState(build3x3State({
            activeCell: { col: 2, row: 2 },
            activeRange: { c1: 2, c2: 2, r1: 2, r2: 2 },
            auxRanges: [],
        }));

        const cell = el.shadowRoot!.querySelector('.col2.row2');
        expect(cell!.classList.contains('cell-range')).to.be.false;

        cleanup(el);
    });

    it('adds cell-range to non-active cells inside the active range', async () => {
        // Range (1,1)..(3,2); active at (1,1).
        const el = await attachWithState(build3x3State({
            activeCell: { col: 1, row: 1 },
            activeRange: { c1: 1, c2: 3, r1: 1, r2: 2 },
            auxRanges: [],
        }));

        // Active cell (1,1) — NO cell-range.
        expect(
            el.shadowRoot!.querySelector('.col1.row1')!.classList.contains('cell-range')
        ).to.be.false;
        // Other cells in the range (1,2..3 × 1..2) — YES cell-range.
        expect(
            el.shadowRoot!.querySelector('.col2.row1')!.classList.contains('cell-range')
        ).to.be.true;
        expect(
            el.shadowRoot!.querySelector('.col3.row1')!.classList.contains('cell-range')
        ).to.be.true;
        expect(
            el.shadowRoot!.querySelector('.col1.row2')!.classList.contains('cell-range')
        ).to.be.true;
        expect(
            el.shadowRoot!.querySelector('.col3.row2')!.classList.contains('cell-range')
        ).to.be.true;
        // Outside the range — no cell-range.
        expect(
            el.shadowRoot!.querySelector('.col1.row3')!.classList.contains('cell-range')
        ).to.be.false;

        cleanup(el);
    });

    it('adds cell-range to cells in aux ranges', async () => {
        // Active at (1,1); aux range (3,3)..(3,3).
        const el = await attachWithState(build3x3State({
            activeCell: { col: 1, row: 1 },
            activeRange: { c1: 1, c2: 1, r1: 1, r2: 1 },
            auxRanges: [{ c1: 3, c2: 3, r1: 3, r2: 3 }],
        }));

        // Active cell — no cell-range.
        expect(
            el.shadowRoot!.querySelector('.col1.row1')!.classList.contains('cell-range')
        ).to.be.false;
        // Cell in aux range — cell-range.
        expect(
            el.shadowRoot!.querySelector('.col3.row3')!.classList.contains('cell-range')
        ).to.be.true;
        // Cell outside everything — no cell-range.
        expect(
            el.shadowRoot!.querySelector('.col2.row2')!.classList.contains('cell-range')
        ).to.be.false;

        cleanup(el);
    });

    it('handles overlapping active + aux ranges (cell-range stays on)', async () => {
        // Active (1,1); aux range (1,1)..(2,2) overlaps the active cell.
        const el = await attachWithState(build3x3State({
            activeCell: { col: 1, row: 1 },
            activeRange: { c1: 1, c2: 1, r1: 1, r2: 1 },
            auxRanges: [{ c1: 1, c2: 2, r1: 1, r2: 2 }],
        }));

        // (1,1) is the active cell — even though aux range covers it,
        // the active-cell rule wins (no cell-range).
        expect(
            el.shadowRoot!.querySelector('.col1.row1')!.classList.contains('cell-range')
        ).to.be.false;
        // (2,2) is in the aux range, not the active cell → cell-range.
        expect(
            el.shadowRoot!.querySelector('.col2.row2')!.classList.contains('cell-range')
        ).to.be.true;

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — .sheet-selection overlay (slice 137)', () => {
    function buildOverlayState(
        selection: CellGridRenderState['selection']
    ): CellGridRenderState {
        return {
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 4,
                lastColumnPosition: 200,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 4,
                lastRowPosition: 80,
            },
            cellData: new Map(),
            colX: [0, 50, 100, 150],
            rowY: [0, 20, 40, 60],
            colWidths: [50, 50, 50, 50],
            rowHeights: [20, 20, 20, 20],
            cellStyleToCSSStyle: {},
            mergedRegions: [],
            selection,
        };
    }

    it('renders the .sheet-selection overlay with 4 border children', async () => {
        const el = await attachWithState(buildOverlayState({
            activeCell: { col: 2, row: 2 },
            activeRange: { c1: 2, c2: 3, r1: 2, r2: 3 },
            auxRanges: [],
        }));

        const overlay = el.shadowRoot!.querySelector('.sheet-selection');
        expect(overlay).to.not.be.null;

        expect(overlay!.querySelector('.s-top')).to.not.be.null;
        expect(overlay!.querySelector('.s-left')).to.not.be.null;
        expect(overlay!.querySelector('.s-right')).to.not.be.null;
        expect(overlay!.querySelector('.s-bottom')).to.not.be.null;

        cleanup(el);
    });

    it('positions the overlay over the active range bounds', async () => {
        const el = await attachWithState(buildOverlayState({
            activeCell: { col: 2, row: 2 },
            activeRange: { c1: 2, c2: 3, r1: 2, r2: 3 },
            auxRanges: [],
        }));

        const overlay = el.shadowRoot!.querySelector('.sheet-selection') as HTMLElement;
        // Range cols 2-3 → left = colX[1] = 50, right = colX[2] + colWidths[2] = 100 + 50 = 150
        // Range rows 2-3 → top = rowY[1] = 20, bottom = rowY[2] + rowHeights[2] = 40 + 20 = 60
        expect(overlay.style.left).to.equal('50px');
        expect(overlay.style.top).to.equal('20px');
        expect(overlay.style.width).to.equal('100px');
        expect(overlay.style.height).to.equal('40px');

        cleanup(el);
    });

    it('positions the overlay over a single-cell selection', async () => {
        const el = await attachWithState(buildOverlayState({
            activeCell: { col: 1, row: 1 },
            activeRange: { c1: 1, c2: 1, r1: 1, r2: 1 },
            auxRanges: [],
        }));

        const overlay = el.shadowRoot!.querySelector('.sheet-selection') as HTMLElement;
        expect(overlay.style.left).to.equal('0px');
        expect(overlay.style.top).to.equal('0px');
        expect(overlay.style.width).to.equal('50px');
        expect(overlay.style.height).to.equal('20px');

        cleanup(el);
    });

    it('clamps the overlay to the visible viewport when range extends past', async () => {
        // viewport: cols 1..4, rows 1..4
        // range: cols 3..10, rows 3..10 — extends past viewport
        const el = await attachWithState(buildOverlayState({
            activeCell: { col: 3, row: 3 },
            activeRange: { c1: 3, c2: 10, r1: 3, r2: 10 },
            auxRanges: [],
        }));

        const overlay = el.shadowRoot!.querySelector('.sheet-selection') as HTMLElement;
        // Clamps to (3, 3)..(4, 4):
        // left = colX[2] = 100, right = colX[3] + colWidths[3] = 150 + 50 = 200
        // top = rowY[2] = 40, bottom = rowY[3] + rowHeights[3] = 60 + 20 = 80
        expect(overlay.style.left).to.equal('100px');
        expect(overlay.style.width).to.equal('100px');
        expect(overlay.style.top).to.equal('40px');
        expect(overlay.style.height).to.equal('40px');

        cleanup(el);
    });

    it('does NOT render the overlay when range is entirely outside viewport', async () => {
        // viewport: cols 1..4. range: cols 10..12 — no overlap.
        const el = await attachWithState(buildOverlayState({
            activeCell: { col: 10, row: 10 },
            activeRange: { c1: 10, c2: 12, r1: 10, r2: 12 },
            auxRanges: [],
        }));

        const overlay = el.shadowRoot!.querySelector('.sheet-selection');
        expect(overlay).to.be.null;

        cleanup(el);
    });

    it('overlay updates when the selection changes', async () => {
        const el = await attachWithState(buildOverlayState({
            activeCell: { col: 1, row: 1 },
            activeRange: { c1: 1, c2: 1, r1: 1, r2: 1 },
            auxRanges: [],
        }));

        // Initial: 50px wide overlay at (0, 0).
        let overlay = el.shadowRoot!.querySelector('.sheet-selection') as HTMLElement;
        expect(overlay.style.left).to.equal('0px');
        expect(overlay.style.width).to.equal('50px');

        // Re-assign with extended range (1..3, 1..2).
        el.renderState = {
            ...el.renderState,
            selection: {
                activeCell: { col: 1, row: 1 },
                activeRange: { c1: 1, c2: 3, r1: 1, r2: 2 },
                auxRanges: [],
            },
        };
        await el.updateComplete;

        overlay = el.shadowRoot!.querySelector('.sheet-selection') as HTMLElement;
        // left = 0, right = colX[2] + colWidths[2] = 100 + 50 = 150
        // top = 0, bottom = rowY[1] + rowHeights[1] = 20 + 20 = 40
        expect(overlay.style.width).to.equal('150px');
        expect(overlay.style.height).to.equal('40px');

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
            cellStyleToCSSStyle: {},
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
