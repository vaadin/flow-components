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
import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import {
    type CellGridRenderState,
    emptyCellGridRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

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

describe('emptyCellGridRenderState — helper for tests + connector initial state', () => {
    it('returns a state with an empty viewport (no cells render from it)', () => {
        const state = emptyCellGridRenderState();

        // The "empty" sentinel: lastColumnIndex < firstColumnIndex
        // makes the cell-grid's render() return without emitting any
        // cells. Pinned here because external code (the connector's
        // initial-measurement render, attachWithState in tests) reads
        // this contract.
        expect(state.viewport.lastColumnIndex).to.be.lessThan(
            state.viewport.firstColumnIndex
        );
        expect(state.viewport.lastRowIndex).to.be.lessThan(
            state.viewport.firstRowIndex
        );
    });

    it('returns a state with an empty cellData map + empty arrays', () => {
        const state = emptyCellGridRenderState();

        expect(state.cellData.size).to.equal(0);
        expect(state.colX).to.deep.equal([]);
        expect(state.rowY).to.deep.equal([]);
        expect(state.colWidths).to.deep.equal([]);
        expect(state.rowHeights).to.deep.equal([]);
        expect(state.cellStyleToCSSStyle).to.deep.equal({});
        expect(state.mergedRegions).to.deep.equal([]);
    });

    it('returns a state with single-cell A1 selection by default', () => {
        const state = emptyCellGridRenderState();

        expect(state.selection.activeCell).to.deep.equal({ col: 1, row: 1 });
        expect(state.selection.activeRange).to.deep.equal({
            c1: 1,
            c2: 1,
            r1: 1,
            r2: 1,
        });
        expect(state.selection.auxRanges).to.deep.equal([]);
    });

    it('returns a fresh object on each call (not a shared singleton)', () => {
        const a = emptyCellGridRenderState();
        const b = emptyCellGridRenderState();

        expect(a).to.not.equal(b);
    });
});

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
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            frozenColX: [],
            frozenColWidths: [],
            frozenRowY: [],
            frozenRowHeights: [],
            totalGridWidthPx: 0,
            totalGridHeightPx: 0,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
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
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            frozenColX: [],
            frozenColWidths: [],
            frozenRowY: [],
            frozenRowHeights: [],
            totalGridWidthPx: 0,
            totalGridHeightPx: 0,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
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
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            frozenColX: [],
            frozenColWidths: [],
            frozenRowY: [],
            frozenRowHeights: [],
            totalGridWidthPx: 0,
            totalGridHeightPx: 0,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
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
        cellStyleToCSSStyle: Record<number, string>,
        extra: Partial<CellGridRenderState> = {}
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
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            frozenColX: [],
            frozenColWidths: [],
            frozenRowY: [],
            frozenRowHeights: [],
            totalGridWidthPx: 0,
            totalGridHeightPx: 0,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
            conditionalFormattingStyles: {},
            shiftedCellBorderStyles: [],
            selection: emptyCellGridRenderState().selection,
            ...extra,
        };
    }

    /** The one cell of the 1x1 fixture, as the server would send it. */
    function a1(over: Partial<CellData> = {}): Map<string, CellData> {
        return new Map([
            ['col1 row1', { row: 1, col: 1, value: '42', cellStyle: 'cs0', ...over } as CellData],
        ]);
    }

    function cellOf(el: SpreadsheetCellGrid): HTMLElement {
        return el.shadowRoot!.querySelector<HTMLElement>('.cell.col1.row1')!;
    }

    it('takes the cell values as the JSON string the server can send', async () => {
        // Flow hands collections over as a JSON string on some lanes and as
        // an array on others. Iterating the string walks it character by
        // character: one entry lands under "colundefined rowundefined" and
        // every value on the sheet disappears.
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        sheet.updateBottomRightCellValues(
            JSON.stringify([
                { row: 1, col: 1, value: 'A1', cellStyle: 'cs0' },
                { row: 2, col: 1, value: 'A2', cellStyle: 'cs0' },
            ]) as any
        );
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        expect(sheet.getCellData().size).to.equal(2);
        expect(sheet.getCellData().get('col1 row1')!.value).to.equal('A1');
        const cell = sheet
            .getCellGrid()
            .shadowRoot!.querySelector<HTMLElement>('.cell.col1.row1')!;
        expect(cell.textContent!.trim()).to.equal('A1');

        sheet.dispose();
        host.remove();
    });

    it('still takes the cell values as a plain array', async () => {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        sheet.updateBottomRightCellValues([
            { row: 1, col: 1, value: 'A1', cellStyle: 'cs0' },
        ] as any);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        expect(sheet.getCellData().get('col1 row1')!.value).to.equal('A1');
        sheet.dispose();
        host.remove();
    });

    it('paints the colour the number format names on the cell', async () => {
        // A four-part format paints positive, negative, zero and text
        // differently, so the colour follows the value and cannot ride on
        // the cs<N> class. The server resolves it per cell and sends the
        // hex digits with no leading '#'.
        const el = await attachWithState(
            build1x1State(a1({ textColor: '0000FF' }), {})
        );

        expect(cellOf(el).style.color).to.equal('rgb(0, 0, 255)');
        cleanup(el);
    });

    it('leaves the colour alone when the format names none', async () => {
        const el = await attachWithState(build1x1State(a1(), {}));

        expect(cellOf(el).style.color).to.equal('');
        cleanup(el);
    });

    it('right-aligns a cell the server marked with r', async () => {
        const el = await attachWithState(
            build1x1State(a1({ cellStyle: 'cs0 r' }), {})
        );

        const cell = cellOf(el);
        expect(cell.classList.contains('r')).to.be.true;
        expect(getComputedStyle(cell).textAlign).to.equal('right');
        cleanup(el);
    });

    it('emits conditional-formatting rules after the cell styles', async () => {
        // A matching rule has to beat the cell's own style, so it must come
        // later in the sheet.
        const el = await attachWithState(
            build1x1State(a1({ cellStyle: 'cs3 cf1' }), { 3: 'color: red;' }, {
                conditionalFormattingStyles: { 1: 'color: lime;' },
            })
        );

        const cssText = Array.from(el.shadowRoot!.querySelectorAll('style'))
            .map((n) => n.textContent ?? '')
            .join('\n');
        expect(cssText).to.include('.cell.cf1 { color: lime; }');
        expect(cssText.indexOf('.cell.cf1')).to.be.greaterThan(
            cssText.indexOf('.cell.cs3')
        );
        expect(getComputedStyle(cellOf(el)).color).to.equal('rgb(0, 255, 0)');
        cleanup(el);
    });

    it('orders conditional-formatting rules by ascending index', async () => {
        // Excel resolves later rules over earlier ones; equal-specificity
        // CSS resolves by source order, so the emission order is the
        // behaviour.
        const el = await attachWithState(
            build1x1State(a1({ cellStyle: 'cs0 cf2 cf10' }), {}, {
                conditionalFormattingStyles: {
                    10: 'color: lime;',
                    2: 'color: red;',
                },
            })
        );

        const cssText = Array.from(el.shadowRoot!.querySelectorAll('style'))
            .map((n) => n.textContent ?? '')
            .join('\n');
        expect(cssText.indexOf('.cell.cf2')).to.be.lessThan(
            cssText.indexOf('.cell.cf10')
        );
        cleanup(el);
    });

    it('scopes shifted cell-border rules to cells and emits them last', async () => {
        // These arrive as complete rules selecting .col<N>.row<N>; unscoped
        // they would also match the column headers.
        const el = await attachWithState(
            build1x1State(a1(), { 0: 'border-right: 1px solid black;' }, {
                shiftedCellBorderStyles: [
                    '.col1.row1 { border-right: 2px solid blue; }',
                ],
            })
        );

        const cssText = Array.from(el.shadowRoot!.querySelectorAll('style'))
            .map((n) => n.textContent ?? '')
            .join('\n');
        expect(cssText).to.include(
            '.cell.col1.row1 { border-right: 2px solid blue; }'
        );
        expect(cssText.indexOf('.cell.col1.row1')).to.be.greaterThan(
            cssText.indexOf('.cell.cs0')
        );
        cleanup(el);
    });

    it('emits a <style> block with the cellStyleToCSSStyle rules', async () => {
        const el = await attachWithState(
            build1x1State(new Map(), {
                0: 'font-weight: bold;',
                3: 'color: red;',
            })
        );

        // Lit's static styles produce their own <style>; we identify
        // ours by content match (rules containing ".cell.cs").
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
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            frozenColX: [],
            frozenColWidths: [],
            frozenRowY: [],
            frozenRowHeights: [],
            totalGridWidthPx: 0,
            totalGridHeightPx: 0,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
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
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            frozenColX: [],
            frozenColWidths: [],
            frozenRowY: [],
            frozenRowHeights: [],
            totalGridWidthPx: 0,
            totalGridHeightPx: 0,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
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

describe('SpreadsheetCellGrid — merged-cell overlays (slice 138)', () => {
    function buildMergedState(
        mergedRegions: CellGridRenderState['mergedRegions'],
        cellData: Map<string, CellData> = new Map()
    ): CellGridRenderState {
        // viewport: cols 1..4, rows 1..4, uniform 50×20.
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
            cellData,
            colX: [0, 50, 100, 150],
            rowY: [0, 20, 40, 60],
            colWidths: [50, 50, 50, 50],
            rowHeights: [20, 20, 20, 20],
            cellStyleToCSSStyle: {},
            mergedRegions,
            selection: emptyCellGridRenderState().selection,
        };
    }

    it('renders one .merged-cell div per merged region at the top-left', async () => {
        const el = await attachWithState(buildMergedState([
            { id: 1, col1: 2, col2: 3, row1: 2, row2: 3 },
        ]));

        // Single merged region → 1 merged-cell div.
        const merged = el.shadowRoot!.querySelectorAll('.merged-cell');
        expect(merged).to.have.length(1);
        // CSS classes match the TestBench selector: div.col2.row2.merged-cell
        const cell = merged[0] as HTMLElement;
        expect(cell.classList.contains('cell')).to.be.true;
        expect(cell.classList.contains('col2')).to.be.true;
        expect(cell.classList.contains('row2')).to.be.true;

        cleanup(el);
    });

    it('sizes the merged-cell div to span the full region', async () => {
        const el = await attachWithState(buildMergedState([
            // cols 2..3, rows 2..3 → 100×40 area
            { id: 1, col1: 2, col2: 3, row1: 2, row2: 3 },
        ]));

        const cell = el.shadowRoot!.querySelector('.merged-cell') as HTMLElement;
        expect(cell.style.left).to.equal('50px');
        expect(cell.style.top).to.equal('20px');
        expect(cell.style.width).to.equal('100px');
        expect(cell.style.height).to.equal('40px');

        cleanup(el);
    });

    it('renders the merged-cell value from the top-left cellData entry', async () => {
        const cellData = new Map<string, CellData>([
            ['col2 row2', { ...makeCellData(2, 2, 'Merged!'), cellStyle: 'cs0' }],
        ]);
        const el = await attachWithState(buildMergedState(
            [{ id: 1, col1: 2, col2: 3, row1: 2, row2: 3 }],
            cellData,
        ));

        const cell = el.shadowRoot!.querySelector('.merged-cell');
        expect(cell!.textContent?.trim()).to.equal('Merged!');

        cleanup(el);
    });

    it('skips rendering the underlying single-cell divs covered by the merge', async () => {
        const el = await attachWithState(buildMergedState([
            { id: 1, col1: 2, col2: 3, row1: 2, row2: 3 },
        ]));

        // (2,2) is the top-left — replaced by the merged-cell overlay,
        // no plain single-cell at that coord.
        const cellsAt22 = el.shadowRoot!.querySelectorAll('.col2.row2');
        // Exactly one element with both classes: the merged-cell.
        expect(cellsAt22).to.have.length(1);
        expect(cellsAt22[0].classList.contains('merged-cell')).to.be.true;

        // Underlying single cells (2,3), (3,2), (3,3) should NOT be
        // rendered — they're covered by the merge.
        expect(el.shadowRoot!.querySelector('.col2.row3')).to.be.null;
        expect(el.shadowRoot!.querySelector('.col3.row2')).to.be.null;
        expect(el.shadowRoot!.querySelector('.col3.row3')).to.be.null;

        // Cells OUTSIDE the merge still render normally.
        expect(el.shadowRoot!.querySelector('.col1.row1')).to.not.be.null;
        expect(el.shadowRoot!.querySelector('.col4.row4')).to.not.be.null;

        cleanup(el);
    });

    it('does NOT render an overlay when the merge top-left is outside the viewport', async () => {
        // viewport: cols 1..4, rows 1..4. Merge at (10..11, 10..11).
        const el = await attachWithState(buildMergedState([
            { id: 1, col1: 10, col2: 11, row1: 10, row2: 11 },
        ]));

        expect(el.shadowRoot!.querySelector('.merged-cell')).to.be.null;

        cleanup(el);
    });

    it('clamps the overlay size when the merge extends past the viewport', async () => {
        // viewport: cols 1..4. Merge at cols 3..6 (extends past) rows 3..6.
        const el = await attachWithState(buildMergedState([
            { id: 1, col1: 3, col2: 6, row1: 3, row2: 6 },
        ]));

        const cell = el.shadowRoot!.querySelector('.merged-cell') as HTMLElement;
        // top-left at (3, 3) → left = 100, top = 40
        expect(cell.style.left).to.equal('100px');
        expect(cell.style.top).to.equal('40px');
        // Visible cols 3..4 → width = 50 + 50 = 100
        // Visible rows 3..4 → height = 20 + 20 = 40
        expect(cell.style.width).to.equal('100px');
        expect(cell.style.height).to.equal('40px');

        cleanup(el);
    });

    it('renders multiple merged-cell overlays for multiple regions', async () => {
        const el = await attachWithState(buildMergedState([
            { id: 1, col1: 1, col2: 2, row1: 1, row2: 1 },
            { id: 2, col1: 3, col2: 4, row1: 3, row2: 4 },
        ]));

        const merged = el.shadowRoot!.querySelectorAll('.merged-cell');
        expect(merged).to.have.length(2);

        // First overlay at (1, 1).
        expect(el.shadowRoot!.querySelector('.col1.row1.merged-cell')).to.not.be.null;
        // Second overlay at (3, 3).
        expect(el.shadowRoot!.querySelector('.col3.row3.merged-cell')).to.not.be.null;

        cleanup(el);
    });

    it('applies cell-range to the merged overlay when its top-left is in the selection range', async () => {
        const state = buildMergedState([
            { id: 1, col1: 2, col2: 3, row1: 2, row2: 3 },
        ]);
        // Active cell elsewhere; selection range covers the merge's top-left.
        state.selection = {
            activeCell: { col: 1, row: 1 },
            activeRange: { c1: 1, c2: 4, r1: 1, r2: 4 },
            auxRanges: [],
        };
        const el = await attachWithState(state);

        const cell = el.shadowRoot!.querySelector('.merged-cell') as HTMLElement;
        expect(cell.classList.contains('cell-range')).to.be.true;

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — frozen-pane quadrants (slice 139)', () => {
    function buildFreezeState(opts: {
        verticalSplitPosition: number;
        horizontalSplitPosition: number;
    }): CellGridRenderState {
        // Visible (main) viewport: cols 3..4, rows 3..4 with the
        // freeze ending at col 2 / row 2. Main coords + frozen coords
        // are populated; mainTotal width = 100 (50×2 cols), frozen
        // width = 100 too. Same for rows.
        return {
            viewport: {
                firstColumnIndex: 3,
                firstColumnPosition: 100,
                lastColumnIndex: 4,
                lastColumnPosition: 200,
                firstRowIndex: 3,
                firstRowPosition: 40,
                lastRowIndex: 4,
                lastRowPosition: 80,
            },
            cellData: new Map(),
            colX: [100, 150],
            rowY: [40, 60],
            colWidths: [50, 50],
            rowHeights: [20, 20],
            cellStyleToCSSStyle: {},
            mergedRegions: [],
            verticalSplitPosition: opts.verticalSplitPosition,
            horizontalSplitPosition: opts.horizontalSplitPosition,
            frozenColX: opts.horizontalSplitPosition > 0 ? [0, 50] : [],
            frozenColWidths: opts.horizontalSplitPosition > 0 ? [50, 50] : [],
            frozenRowY: opts.verticalSplitPosition > 0 ? [0, 20] : [],
            frozenRowHeights: opts.verticalSplitPosition > 0 ? [20, 20] : [],
            totalGridWidthPx: 0,
            totalGridHeightPx: 0,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
            selection: emptyCellGridRenderState().selection,
        };
    }

    it('renders only main-quadrant cells when no freeze is active', async () => {
        const el = await attachWithState(buildFreezeState({
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
        }));

        expect(el.shadowRoot!.querySelectorAll('.quadrant-main')).to.have.length(4);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-top')).to.have.length(0);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-left')).to.have.length(0);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-corner')).to.have.length(0);

        cleanup(el);
    });

    it('renders frozen-row (top) cells when verticalSplitPosition > 0', async () => {
        const el = await attachWithState(buildFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 0,
        }));

        // Top quadrant: scrolling cols (3, 4) × frozen rows (1, 2) = 4 cells.
        expect(el.shadowRoot!.querySelectorAll('.quadrant-top')).to.have.length(4);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-main')).to.have.length(4);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-left')).to.have.length(0);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-corner')).to.have.length(0);

        cleanup(el);
    });

    it('renders frozen-column (left) cells when horizontalSplitPosition > 0', async () => {
        const el = await attachWithState(buildFreezeState({
            verticalSplitPosition: 0,
            horizontalSplitPosition: 2,
        }));

        // Left quadrant: frozen cols (1, 2) × scrolling rows (3, 4) = 4 cells.
        expect(el.shadowRoot!.querySelectorAll('.quadrant-left')).to.have.length(4);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-main')).to.have.length(4);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-top')).to.have.length(0);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-corner')).to.have.length(0);

        cleanup(el);
    });

    it('renders all 4 quadrants when both freezes are active', async () => {
        const el = await attachWithState(buildFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
        }));

        expect(el.shadowRoot!.querySelectorAll('.quadrant-corner')).to.have.length(4);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-top')).to.have.length(4);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-left')).to.have.length(4);
        expect(el.shadowRoot!.querySelectorAll('.quadrant-main')).to.have.length(4);

        cleanup(el);
    });

    it('positions frozen-row cells using frozenRowY (independent of scroll)', async () => {
        const el = await attachWithState(buildFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 0,
        }));

        // Top quadrant cell at (col 3, row 1) uses:
        //   left = colX[col - firstColumnIndex] = colX[0] = 100
        //   top  = frozenRowY[row - 1] = frozenRowY[0] = 0
        const cell = el.shadowRoot!.querySelector('.col3.row1') as HTMLElement;
        expect(cell.classList.contains('quadrant-top')).to.be.true;
        expect(cell.style.left).to.equal('100px');
        expect(cell.style.top).to.equal('0px');

        cleanup(el);
    });

    it('positions frozen-column cells using frozenColX (independent of scroll)', async () => {
        const el = await attachWithState(buildFreezeState({
            verticalSplitPosition: 0,
            horizontalSplitPosition: 2,
        }));

        // Left quadrant cell at (col 1, row 3) uses:
        //   left = frozenColX[col - 1] = frozenColX[0] = 0
        //   top  = rowY[row - firstRowIndex] = rowY[0] = 40
        const cell = el.shadowRoot!.querySelector('.col1.row3') as HTMLElement;
        expect(cell.classList.contains('quadrant-left')).to.be.true;
        expect(cell.style.left).to.equal('0px');
        expect(cell.style.top).to.equal('40px');

        cleanup(el);
    });

    it('positions corner cells using frozenColX + frozenRowY (both fixed)', async () => {
        const el = await attachWithState(buildFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
        }));

        // Corner cell at (col 2, row 2) uses:
        //   left = frozenColX[1] = 50
        //   top  = frozenRowY[1] = 20
        const cell = el.shadowRoot!.querySelector('.col2.row2') as HTMLElement;
        expect(cell.classList.contains('quadrant-corner')).to.be.true;
        expect(cell.style.left).to.equal('50px');
        expect(cell.style.top).to.equal('20px');

        cleanup(el);
    });

    it('renders cellData values for cells across all 4 quadrants', async () => {
        const state = buildFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
        });
        const cellData = new Map<string, CellData>([
            ['col1 row1', makeCellData(1, 1, 'CORNER')],
            ['col3 row1', makeCellData(3, 1, 'TOP')],
            ['col1 row3', makeCellData(1, 3, 'LEFT')],
            ['col3 row3', makeCellData(3, 3, 'MAIN')],
        ]);
        const el = await attachWithState({ ...state, cellData });

        expect(
            el.shadowRoot!.querySelector('.col1.row1')!.textContent?.trim()
        ).to.equal('CORNER');
        expect(
            el.shadowRoot!.querySelector('.col3.row1')!.textContent?.trim()
        ).to.equal('TOP');
        expect(
            el.shadowRoot!.querySelector('.col1.row3')!.textContent?.trim()
        ).to.equal('LEFT');
        expect(
            el.shadowRoot!.querySelector('.col3.row3')!.textContent?.trim()
        ).to.equal('MAIN');

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — cell-click event delegation (slice 146)', () => {
    function build3x3State(): CellGridRenderState {
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
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            frozenColX: [],
            frozenColWidths: [],
            frozenRowY: [],
            frozenRowHeights: [],
            totalGridWidthPx: 150,
            totalGridHeightPx: 60,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
            selection: emptyCellGridRenderState().selection,
        };
    }

    it('puts data-col + data-row on each rendered cell', async () => {
        const el = await attachWithState(build3x3State());

        const cell = el.shadowRoot!.querySelector(
            '.col2.row3'
        ) as HTMLElement;
        expect(cell.dataset.col).to.equal('2');
        expect(cell.dataset.row).to.equal('3');

        cleanup(el);
    });

    it('dispatches a cell-click CustomEvent with the cell coord on click', async () => {
        const el = await attachWithState(build3x3State());

        let received: { col: number; row: number } | undefined;
        el.addEventListener('cell-click', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            received = { col: detail.col, row: detail.row };
        });

        const cell = el.shadowRoot!.querySelector(
            '.col2.row3'
        ) as HTMLElement;
        cell.click();

        expect(received).to.deep.equal({ col: 2, row: 3 });

        cleanup(el);
    });

    it('cell-click detail includes shiftKey + ctrlKey + metaKey + altKey', async () => {
        const el = await attachWithState(build3x3State());

        let received: {
            col: number; row: number;
            shiftKey: boolean; ctrlKey: boolean;
            metaKey: boolean; altKey: boolean;
        } | undefined;
        el.addEventListener('cell-click', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        const cell = el.shadowRoot!.querySelector(
            '.col2.row2'
        ) as HTMLElement;
        // Dispatch a MouseEvent with modifier flags so the handler
        // reads them off the event.
        cell.dispatchEvent(new MouseEvent('click', {
            bubbles: true,
            composed: true,
            shiftKey: true,
            ctrlKey: false,
            metaKey: true,
            altKey: false,
        }));

        expect(received).to.exist;
        expect(received!.shiftKey).to.be.true;
        expect(received!.ctrlKey).to.be.false;
        expect(received!.metaKey).to.be.true;
        expect(received!.altKey).to.be.false;

        cleanup(el);
    });

    it('cell-click event bubbles + crosses the shadow DOM boundary', async () => {
        const el = await attachWithState(build3x3State());

        // Listening on the host element (outside the shadow root)
        // should still receive the event because it's
        // composed: true.
        let received = false;
        el.addEventListener('cell-click', () => {
            received = true;
        });

        const cell = el.shadowRoot!.querySelector(
            '.col1.row1'
        ) as HTMLElement;
        cell.click();

        expect(received).to.be.true;

        cleanup(el);
    });

    it('does NOT dispatch cell-click for clicks outside any cell', async () => {
        const el = await attachWithState(build3x3State());

        let received = false;
        el.addEventListener('cell-click', () => {
            received = true;
        });

        // Click the shadow root itself (no .cell ancestor) — should
        // not trigger the event.
        el.shadowRoot!.dispatchEvent(new MouseEvent('click', {
            bubbles: true,
        }));

        expect(received).to.be.false;

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — frozen-pane transform wrappers (slice 141)', () => {
    function buildScrolledFreezeState(opts: {
        verticalSplitPosition: number;
        horizontalSplitPosition: number;
        scrollLeft: number;
        scrollTop: number;
    }): CellGridRenderState {
        // Same shape as slice 139's buildFreezeState — adding
        // scrollLeft + scrollTop so the wrappers' transforms are
        // observable.
        return {
            viewport: {
                firstColumnIndex: 3,
                firstColumnPosition: 100,
                lastColumnIndex: 4,
                lastColumnPosition: 200,
                firstRowIndex: 3,
                firstRowPosition: 40,
                lastRowIndex: 4,
                lastRowPosition: 80,
            },
            cellData: new Map(),
            colX: [100, 150],
            rowY: [40, 60],
            colWidths: [50, 50],
            rowHeights: [20, 20],
            cellStyleToCSSStyle: {},
            mergedRegions: [],
            verticalSplitPosition: opts.verticalSplitPosition,
            horizontalSplitPosition: opts.horizontalSplitPosition,
            frozenColX: opts.horizontalSplitPosition > 0 ? [0, 50] : [],
            frozenColWidths: opts.horizontalSplitPosition > 0 ? [50, 50] : [],
            frozenRowY: opts.verticalSplitPosition > 0 ? [0, 20] : [],
            frozenRowHeights: opts.verticalSplitPosition > 0 ? [20, 20] : [],
            totalGridWidthPx: 1000,
            totalGridHeightPx: 500,
            scrollLeft: opts.scrollLeft,
            scrollTop: opts.scrollTop,
            selection: emptyCellGridRenderState().selection,
        };
    }

    it('renders NO wrappers when freeze is inactive', async () => {
        const el = await attachWithState(buildScrolledFreezeState({
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            scrollLeft: 200,
            scrollTop: 100,
        }));

        expect(el.shadowRoot!.querySelectorAll('.quadrant-wrapper')).to.have.length(0);

        cleanup(el);
    });

    it('renders a top wrapper with translateY(scrollTop) when row-freeze is active', async () => {
        const el = await attachWithState(buildScrolledFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 0,
            scrollLeft: 0,
            scrollTop: 100,
        }));

        const wrapper = el.shadowRoot!.querySelector(
            '.quadrant-wrapper-top'
        ) as HTMLElement;
        expect(wrapper).to.not.be.null;
        expect(wrapper.style.transform).to.equal('translateY(100px)');
        // Cells inside the wrapper still carry their quadrant-top class.
        expect(wrapper.querySelectorAll('.quadrant-top')).to.have.length(4);

        cleanup(el);
    });

    it('renders a left wrapper with translateX(scrollLeft) when col-freeze is active', async () => {
        const el = await attachWithState(buildScrolledFreezeState({
            verticalSplitPosition: 0,
            horizontalSplitPosition: 2,
            scrollLeft: 200,
            scrollTop: 0,
            editor: null,
            overlays: [],
        }));

        const wrapper = el.shadowRoot!.querySelector(
            '.quadrant-wrapper-left'
        ) as HTMLElement;
        expect(wrapper).to.not.be.null;
        expect(wrapper.style.transform).to.equal('translateX(200px)');
        expect(wrapper.querySelectorAll('.quadrant-left')).to.have.length(4);

        cleanup(el);
    });

    it('renders a corner wrapper with translate(scrollLeft, scrollTop) when both freezes are active', async () => {
        const el = await attachWithState(buildScrolledFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
            scrollLeft: 200,
            scrollTop: 100,
        }));

        const wrapper = el.shadowRoot!.querySelector(
            '.quadrant-wrapper-corner'
        ) as HTMLElement;
        expect(wrapper).to.not.be.null;
        expect(wrapper.style.transform).to.equal('translate(200px, 100px)');
        expect(wrapper.querySelectorAll('.quadrant-corner')).to.have.length(4);

        cleanup(el);
    });

    it('main quadrant cells are NOT inside any wrapper', async () => {
        const el = await attachWithState(buildScrolledFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
            scrollLeft: 200,
            scrollTop: 100,
        }));

        // Main cells stay direct descendants of the shadow root, NOT
        // wrapped — they need to scroll naturally with the scroll
        // container. closest('.quadrant-wrapper') walks up the tree
        // and returns null if the cell isn't inside any wrapper.
        const mainCells = el.shadowRoot!.querySelectorAll('.quadrant-main');
        for (const cell of Array.from(mainCells)) {
            expect(cell.closest('.quadrant-wrapper')).to.be.null;
        }

        cleanup(el);
    });

    it('wrapper transforms update when scroll position changes', async () => {
        const el = await attachWithState(buildScrolledFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
        }));

        let corner = el.shadowRoot!.querySelector(
            '.quadrant-wrapper-corner'
        ) as HTMLElement;
        expect(corner.style.transform).to.equal('translate(0px, 0px)');

        el.renderState = buildScrolledFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
            scrollLeft: 300,
            scrollTop: 150,
        });
        await el.updateComplete;

        corner = el.shadowRoot!.querySelector(
            '.quadrant-wrapper-corner'
        ) as HTMLElement;
        expect(corner.style.transform).to.equal('translate(300px, 150px)');

        cleanup(el);
    });

    it('frozen wrappers are positioned absolutely at top:0 left:0 so cells anchor to grid origin', async () => {
        const el = await attachWithState(buildScrolledFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
            scrollLeft: 100,
            scrollTop: 50,
        }));

        const wrappers = el.shadowRoot!.querySelectorAll('.quadrant-wrapper');
        // 3 wrappers: top, left, corner. (Main is not wrapped.)
        expect(wrappers).to.have.length(3);
        for (const w of Array.from(wrappers)) {
            const computed = getComputedStyle(w);
            // Wrappers must be position:absolute (set via static CSS rule).
            expect(computed.position).to.equal('absolute');
        }

        cleanup(el);
    });

    it('frozen cell positions inside wrappers stay anchored at grid coords (not subtracted)', async () => {
        // Verify the wrapper-transform approach: cells inside the
        // wrapper still report virtual positions, the wrapper's
        // transform does the visual offset.
        const el = await attachWithState(buildScrolledFreezeState({
            verticalSplitPosition: 2,
            horizontalSplitPosition: 0,
            scrollLeft: 0,
            scrollTop: 100,
        }));

        // A top-quadrant cell at row 1: top should still be 0
        // (frozenRowY[0]), NOT 0 - 100 or anything subtracted. The
        // wrapper's translateY does the visual shift; the cell's
        // inline style stays at its grid coord.
        const cell = el.shadowRoot!.querySelector(
            '.quadrant-top.row1'
        ) as HTMLElement;
        expect(cell.style.top).to.equal('0px');

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
            verticalSplitPosition: 0,
            horizontalSplitPosition: 0,
            frozenColX: [],
            frozenColWidths: [],
            frozenRowY: [],
            frozenRowHeights: [],
            totalGridWidthPx: 0,
            totalGridHeightPx: 0,
            scrollLeft: 0,
            scrollTop: 0,
            editor: null,
            overlays: [],
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

describe('Cell grid — invalid-formula indicator', () => {
    function stateWith(invalid: string[]): CellGridRenderState {
        return {
            ...emptyCellGridRenderState(),
            viewport: {
                firstColumnIndex: 1, firstColumnPosition: 0,
                lastColumnIndex: 3, lastColumnPosition: 150,
                firstRowIndex: 1, firstRowPosition: 0,
                lastRowIndex: 3, lastRowPosition: 60,
            },
            colX: [0, 50, 100],
            rowY: [0, 20, 40],
            colWidths: [50, 50, 50],
            rowHeights: [20, 20, 20],
            invalidFormulaCells: new Set(invalid),
        };
    }

    async function attach(state: CellGridRenderState): Promise<SpreadsheetCellGrid> {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-cell-grid'
        ) as SpreadsheetCellGrid;
        el.renderState = state;
        document.body.appendChild(el);
        await el.updateComplete;
        return el;
    }

    it('marks a cell whose formula could not be evaluated', async () => {
        const el = await attach(stateWith(['col2 row2']));

        const marks = el.shadowRoot!.querySelectorAll('.invalidformula-indicator');
        expect(marks).to.have.length(1);
        el.remove();
    });

    it('carries the documented invalid-triangle part', async () => {
        const el = await attach(stateWith(['col2 row2']));

        const mark = el.shadowRoot!.querySelector('.invalidformula-indicator')!;
        expect(mark.getAttribute('part')).to.equal('invalid-triangle');
        el.remove();
    });

    it('marks every invalid cell, and only those', async () => {
        const el = await attach(stateWith(['col1 row1', 'col3 row3']));

        expect(el.shadowRoot!.querySelectorAll('.invalidformula-indicator'))
            .to.have.length(2);
        el.remove();
    });

    it('marks nothing when every formula evaluated', async () => {
        const el = await attach(stateWith([]));

        expect(el.shadowRoot!.querySelectorAll('.invalidformula-indicator'))
            .to.have.length(0);
        el.remove();
    });

    it('lives inside the cell it belongs to', async () => {
        // A child rather than a sibling: it scrolls and positions with the
        // cell, and tests can reach it from the cell element.
        const el = await attach(stateWith(['col2 row2']));

        const mark = el.shadowRoot!.querySelector<HTMLElement>('.invalidformula-indicator')!;
        const cell = mark.closest<HTMLElement>('.cell')!;
        expect(cell.dataset.col).to.equal('2');
        expect(cell.dataset.row).to.equal('2');
        el.remove();
    });
});
