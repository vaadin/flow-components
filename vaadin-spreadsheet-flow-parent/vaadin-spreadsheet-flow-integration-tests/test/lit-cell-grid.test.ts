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
    it('renders a hidden row collapsed rather than leaving it out', async () => {
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

        // Every cell in the viewport is rendered; the hidden row's are
        // collapsed with display:none. Leaving them out makes them
        // unfindable, and "is this cell hidden?" is a question both the
        // tests and an application's own scripting ask.
        const cells = el.shadowRoot!.querySelectorAll('.cell');
        expect(cells).to.have.length(6);
        const hidden = el.shadowRoot!.querySelector<HTMLElement>('.row2');
        expect(hidden, 'the hidden row was left out').to.not.be.null;
        expect(hidden!.style.display).to.equal('none');
        expect(
            el.shadowRoot!.querySelector<HTMLElement>('.row1')!.style.display
        ).to.not.equal('none');
        expect(
            el.shadowRoot!.querySelector<HTMLElement>('.row3')!.style.display
        ).to.not.equal('none');

        cleanup(el);
    });

    it('renders a hidden column collapsed rather than leaving it out', async () => {
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
        expect(cells).to.have.length(3);
        const hidden = el.shadowRoot!.querySelector<HTMLElement>('.col2');
        expect(hidden, 'the hidden column was left out').to.not.be.null;
        expect(hidden!.style.display).to.equal('none');

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
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
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
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
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
        // The default style is laid under every cell rather than scoped to
        // .cs0, as the GWT client did, so styles that only state what they
        // change still get everything else from it.
        expect(cssText).to.include('.cell.cell { font-weight: bold;');
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

    it('gives a cell the server sent nothing for the default style', async () => {
        // Cell at (1,1) has no cellData entry. It still wears cs0, the
        // workbook's default style, as every cell did in the GWT client.
        // Without it the cell fell through to the host's Helvetica 14px in
        // grey rather than the sheet's Calibri 11pt in black.
        const el = await attachWithState(
            build1x1State(new Map(), { 0: 'font-weight: bold;' })
        );

        const cell = el.shadowRoot!.querySelector('.col1.row1') as HTMLElement;
        const csClasses = Array.from(cell.classList).filter((c) => c.startsWith('cs'));
        expect(csClasses).to.deep.equal(['cs0']);
        expect(getComputedStyle(cell).fontWeight).to.equal('700');

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

    it('paints the outline above the cells, not under them', async () => {
        // The four border divs existing is not enough. Cells are
        // positioned with z-index 1 and an opaque background, so an
        // outline left at the default z-index is in the DOM, correctly
        // sized, and completely invisible - which is how it shipped
        // until the Phase 3.0 baselines were read pixel by pixel. Only
        // the corner handle, which had its own z-index, showed.
        const el = await attachWithState(buildOverlayState({
            activeCell: { col: 2, row: 2 },
            activeRange: { c1: 2, c2: 3, r1: 2, r2: 3 },
            auxRanges: [],
        }));

        const overlay = el.shadowRoot!.querySelector('.sheet-selection')!;
        const cell = el.shadowRoot!.querySelector('.cell')!;
        const cellZ = Number(getComputedStyle(cell).zIndex);
        expect(cellZ).to.be.greaterThan(0);

        for (const side of ['s-top', 's-left', 's-right', 's-bottom']) {
            const border = overlay.querySelector('.' + side)!;
            const z = Number(getComputedStyle(border).zIndex);
            expect(z, side + ' must paint above the cells').to.be.greaterThan(
                cellZ
            );
        }

        cleanup(el);
    });

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

describe('SpreadsheetCellGrid — clicking a merged cell', () => {
    /**
     * A merged region is drawn as one element carrying the `cell` class,
     * so the grid's click delegation finds it — but it read the
     * coordinates from data attributes the merged element did not have,
     * parsed NaN and gave up. A merged cell could not be selected at
     * all.
     */
    async function attachMerged(): Promise<{
        sheet: Spreadsheet;
        merged: HTMLElement;
        cleanup: () => void;
    }> {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setRows(10);
        sheet.setCols(6);
        sheet.setColW(new Array(6).fill(50));
        sheet.setRowH(new Array(10).fill(15));
        sheet.setMergedRegions([{ id: 1, col1: 2, row1: 3, col2: 4, row2: 4 }] as any);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;
        const merged = sheet
            .getCellGrid()
            .shadowRoot!.querySelector<HTMLElement>('.merged-cell')!;
        return {
            sheet,
            merged,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    it('carries the coordinates the click delegation reads', async () => {
        const { merged, cleanup } = await attachMerged();
        expect(merged.dataset.col).to.equal('2');
        expect(merged.dataset.row).to.equal('3');
        cleanup();
    });

    it('selects the region when it is clicked', async () => {
        const { sheet, merged, cleanup } = await attachMerged();

        merged.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, composed: true }));
        merged.dispatchEvent(new MouseEvent('click', { bubbles: true, composed: true }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 3 });
        cleanup();
    });

    it('reports the click to the server', async () => {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setRows(10);
        sheet.setCols(6);
        sheet.setColW(new Array(6).fill(50));
        sheet.setRowH(new Array(10).fill(15));
        sheet.setMergedRegions([{ id: 1, col1: 2, row1: 3, col2: 4, row2: 4 }] as any);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;
        const events: any[] = [];
        host.addEventListener('spreadsheet-event', (e) =>
            events.push((e as CustomEvent).detail)
        );

        const merged = sheet
            .getCellGrid()
            .shadowRoot!.querySelector<HTMLElement>('.merged-cell')!;
        merged.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, composed: true }));
        merged.dispatchEvent(new MouseEvent('click', { bubbles: true, composed: true }));

        const selected = events.find((e) => e.type === 'cellSelected');
        expect(selected).to.not.be.undefined;
        expect(selected.data).to.include({ row: 3, column: 2 });

        sheet.dispose();
        host.remove();
    });

    it('is a cell to a stylesheet', async () => {
        // `::part(cell)` should reach a merged region like any other.
        const { merged, cleanup } = await attachMerged();
        expect(merged.getAttribute('part')).to.contain('cell');
        cleanup();
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

    it('keeps the cells a merge covers, drawing the merge over them', async () => {
        const el = await attachWithState(buildMergedState([
            { id: 1, col1: 2, col2: 3, row1: 2, row2: 3 },
        ]));

        // Two elements answer to the anchor coordinate: the cell itself
        // and the merge drawn over it. An application, and the tests, ask
        // about a cell by its coordinates whether or not a merge happens
        // to sit there, so the cell stays.
        const cellsAt22 = el.shadowRoot!.querySelectorAll('.col2.row2');
        expect(cellsAt22).to.have.length(2);
        expect(
            [...cellsAt22].filter((c) => c.classList.contains('merged-cell'))
        ).to.have.length(1);

        // The rest of the covered region is there too.
        expect(el.shadowRoot!.querySelector('.col2.row3')).to.not.be.null;
        expect(el.shadowRoot!.querySelector('.col3.row2')).to.not.be.null;
        expect(el.shadowRoot!.querySelector('.col3.row3')).to.not.be.null;

        // Cells OUTSIDE the merge render as they always did.
        expect(el.shadowRoot!.querySelector('.col1.row1')).to.not.be.null;
        expect(el.shadowRoot!.querySelector('.col4.row4')).to.not.be.null;

        // The merge sits above them, so its content is what shows.
        const merged = el.shadowRoot!.querySelector<HTMLElement>('.merged-cell')!;
        expect(merged.style.zIndex).to.equal('2');

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

/**
 * A hidden row or column collapses its cells to nothing.
 *
 * Sizing them to zero is not enough: a zero-width box still paints its
 * borders, still answers a hit test on its edge, and still reports itself
 * as laid out, so a hidden column showed as a hairline and could be
 * clicked.
 */
describe('SpreadsheetCellGrid — hidden rows and columns', () => {
    function stateWithSizes(
        colWidths: number[],
        rowHeights: number[]
    ): CellGridRenderState {
        let x = 0;
        const colX = colWidths.map((w) => {
            const at = x;
            x += w;
            return at;
        });
        let y = 0;
        const rowY = rowHeights.map((h) => {
            const at = y;
            y += h;
            return at;
        });
        return {
            ...emptyCellGridRenderState(),
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: colWidths.length,
                lastColumnPosition: x,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: rowHeights.length,
                lastRowPosition: y,
            },
            colX,
            rowY,
            colWidths,
            rowHeights,
        };
    }

    function cellStyle(el: SpreadsheetCellGrid, col: number, row: number): string {
        const cell = el.shadowRoot!.querySelector<HTMLElement>(
            `.cell[data-col="${col}"][data-row="${row}"]`
        );
        expect(cell, `no cell ${col},${row}`).to.not.be.null;
        return cell!.style.display;
    }

    it('gives every cell a ground of its own', async () => {
        // Not a window onto whatever is behind the sheet: a conditional
        // format that stops matching must leave the cell white, and a
        // cell under a merge must not show through it. Five conditional-
        // formatting suites read this as rgba(255,255,255,1).
        const el = await attachWithState(stateWithSizes([50, 50], [20, 20]));

        const cell = el.shadowRoot!.querySelector('.cell')!;
        expect(getComputedStyle(cell).backgroundColor).to.equal('rgb(255, 255, 255)');
        cleanup(el);
    });

    it('lets a cell style override the default ground', async () => {
        const el = await attachWithState({
            ...stateWithSizes([50, 50], [20, 20]),
            cellData: new Map([
                ['col1 row1', makeCellData(1, 1, 'x')],
            ]),
            cellStyleToCSSStyle: { 0: 'background: rgb(1, 2, 3);' },
        });

        const cell = el.shadowRoot!.querySelector('.cell.cs0')!;
        expect(cell, 'no styled cell').to.not.be.null;
        expect(getComputedStyle(cell).backgroundColor).to.equal('rgb(1, 2, 3)');
        cleanup(el);
    });

    it('hides a cell in a zero-width column', async () => {
        const el = await attachWithState(stateWithSizes([50, 0, 50], [20, 20]));

        expect(cellStyle(el, 2, 1)).to.equal('none');
        cleanup(el);
    });

    it('hides a cell in a zero-height row', async () => {
        const el = await attachWithState(stateWithSizes([50, 50], [20, 0]));

        expect(cellStyle(el, 1, 2)).to.equal('none');
        cleanup(el);
    });

    it('leaves the cells around a hidden one visible', async () => {
        const el = await attachWithState(stateWithSizes([50, 0, 50], [20, 20]));

        expect(cellStyle(el, 1, 1)).to.not.equal('none');
        expect(cellStyle(el, 3, 1)).to.not.equal('none');
        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — text running past its own column', () => {
    function stateWithSpill(overflowWidth: number): CellGridRenderState {
        const a1 = makeCellData(1, 1, 'a value too wide for its column');
        return {
            ...emptyCellGridRenderState(),
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 3,
                lastColumnPosition: 150,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 1,
                lastRowPosition: 20,
            },
            cellData: new Map<string, CellData>([
                ['col1 row1', { ...a1, overflowWidth }],
            ]),
            colX: [0, 50, 100],
            rowY: [0],
            colWidths: [50, 50, 50],
            rowHeights: [20],
        };
    }

    function firstCell(el: SpreadsheetCellGrid): HTMLElement {
        return el.shadowRoot!.querySelector<HTMLElement>('.cell')!;
    }

    it('leaves a cell that fits alone', async () => {
        const el = await attachWithState(stateWithSpill(0));

        const cell = firstCell(el);
        expect(cell.classList.contains('overflowing')).to.be.false;
        expect(cell.querySelector('.cell-spill')).to.be.null;
        expect(getComputedStyle(cell).overflow).to.equal('hidden');

        cleanup(el);
    });

    it('draws the text at the width the server sent', async () => {
        const el = await attachWithState(stateWithSpill(150));

        const spill = firstCell(el).querySelector<HTMLElement>('.cell-spill')!;
        expect(spill, 'the text is wrapped so it can be given a width').to.not
            .be.null;
        expect(spill.style.width).to.equal('150px');
        expect(spill.textContent).to.equal('a value too wide for its column');

        cleanup(el);
    });

    it('stops clipping the cell so the text can leave it', async () => {
        const el = await attachWithState(stateWithSpill(150));

        expect(getComputedStyle(firstCell(el)).overflow).to.equal('visible');

        cleanup(el);
    });

    it('paints over the empty neighbours the text reaches into', async () => {
        // The neighbours come later in the DOM and have a ground of their
        // own, so at equal z-index they would cover the spilled text.
        const el = await attachWithState(stateWithSpill(150));

        const cells = el.shadowRoot!.querySelectorAll<HTMLElement>('.cell');
        const spilling = Number(getComputedStyle(cells[0]).zIndex);
        const neighbour = Number(getComputedStyle(cells[1]).zIndex);
        expect(spilling).to.be.greaterThan(neighbour);

        cleanup(el);
    });

    it('stays put when the width is no wider than the column', async () => {
        // A server that reports the cell's own width is saying the text has
        // nowhere to go: the neighbour holds a value, or the sheet ends.
        const el = await attachWithState(stateWithSpill(50));

        expect(firstCell(el).classList.contains('overflowing')).to.be.false;
        expect(getComputedStyle(firstCell(el)).overflow).to.equal('hidden');

        cleanup(el);
    });

    it('cuts text with nowhere to go short with an ellipsis', async () => {
        // The GWT client read "Wednesd..." where a neighbour blocked the
        // text. Clipped plainly, it read "Wednesda" with half a letter.
        const el = await attachWithState(stateWithSpill(50));

        const cell = firstCell(el);
        const held = cell.querySelector<HTMLElement>('.cell-spill')!;
        expect(held, 'the text is held so it can be cut short').to.not.be.null;
        expect(getComputedStyle(held).textOverflow).to.equal('ellipsis');
        expect(held.scrollWidth, 'and it really is too wide').to.be.greaterThan(
            held.clientWidth
        );

        cleanup(el);
    });

    it('puts that ellipsis inside the cell, where it can be seen', async () => {
        // At the column's full width the holder would reach past the
        // padding, and the cell - which clips - would cut the ellipsis off.
        const el = await attachWithState(stateWithSpill(50));

        const cell = firstCell(el);
        const held = cell.querySelector<HTMLElement>('.cell-spill')!;
        expect(held.style.width).to.equal('');
        expect(held.getBoundingClientRect().right).to.be.at.most(
            cell.getBoundingClientRect().right - 2
        );

        cleanup(el);
    });

    it('ends spilled text that still does not fit in an ellipsis', async () => {
        const el = await attachWithState(stateWithSpill(100));

        const held = firstCell(el).querySelector<HTMLElement>('.cell-spill')!;
        expect(getComputedStyle(held).textOverflow).to.equal('ellipsis');
        expect(getComputedStyle(held).overflow).to.equal('hidden');

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — a value too wide for its column', () => {
    function stateWith(needsMeasure: boolean): CellGridRenderState {
        const a1 = makeCellData(1, 1, '$246 913 578 024 690,00');
        return {
            ...emptyCellGridRenderState(),
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
            cellData: new Map<string, CellData>([
                ['col1 row1', { ...a1, needsMeasure }],
            ]),
            colX: [0],
            rowY: [0],
            colWidths: [50],
            rowHeights: [20],
        };
    }

    it('shows hashes instead of a clipped number', async () => {
        // A clipped "$246 913" reads as a number, and a wrong one by a
        // factor of a million. Excel shows hashes for exactly this reason.
        const el = await attachWithState(stateWith(true));
        await el.updateComplete;

        expect(
            el.shadowRoot!.querySelector('.cell')!.textContent!.trim()
        ).to.equal('###');

        cleanup(el);
    });

    it('shows the value when it fits', async () => {
        const el = await attachWithState(stateWith(false));

        expect(
            el.shadowRoot!.querySelector('.cell')!.textContent!.trim()
        ).to.equal('$246 913 578 024 690,00');

        cleanup(el);
    });

    it('shows a flagged value that turns out to fit', async () => {
        // The server judges width by one character's width times the
        // character count, which is coarse enough to flag a date that fits.
        // Replacing such a cell with hashes on the flag alone hid perfectly
        // good values - it broke ValueHandlerIT's "29-Jun-06".
        const a1 = makeCellData(1, 1, '29-Jun-06');
        const el = await attachWithState({
            ...emptyCellGridRenderState(),
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 1,
                lastColumnPosition: 400,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 1,
                lastRowPosition: 20,
            },
            cellData: new Map<string, CellData>([
                ['col1 row1', { ...a1, needsMeasure: true }],
            ]),
            colX: [0],
            rowY: [0],
            colWidths: [400],
            rowHeights: [20],
        });
        await el.updateComplete;

        expect(
            el.shadowRoot!.querySelector('.cell')!.textContent!.trim()
        ).to.equal('29-Jun-06');

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — selecting a cell that is already coloured', () => {
    it('tints the cell rather than repainting it', async () => {
        // A background would hide a conditional format's fill for as long
        // as the cell is selected. Excel shows the fill through the
        // selection, so the tint goes on top as an inset shadow.
        const a1 = makeCellData(1, 1, 'x');
        const el = await attachWithState({
            ...emptyCellGridRenderState(),
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 2,
                lastColumnPosition: 100,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 1,
                lastRowPosition: 20,
            },
            cellData: new Map<string, CellData>([
                ['col1 row1', { ...a1, cellStyle: 'cs7' }],
            ]),
            colX: [0, 50],
            rowY: [0],
            colWidths: [50, 50],
            rowHeights: [20],
            cellStyleToCSSStyle: { 7: 'background:rgb(255, 199, 206);' },
            selection: {
                activeCell: { col: 2, row: 1 },
                activeRange: { c1: 1, c2: 2, r1: 1, r2: 1 },
                auxRanges: [],
            },
        });

        const cell = el.shadowRoot!.querySelector<HTMLElement>('.cell')!;
        expect(cell.classList.contains('cell-range')).to.be.true;
        const style = getComputedStyle(cell);
        expect(style.backgroundColor, 'the fill is still there').to.equal(
            'rgb(255, 199, 206)'
        );
        expect(style.boxShadow, 'and the selection tint sits over it').to.contain(
            'inset'
        );

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — where text sits in a tall row', () => {
    function tallState(cellStyleToCSSStyle: Record<number, string>): CellGridRenderState {
        return {
            ...emptyCellGridRenderState(),
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 1,
                lastColumnPosition: 100,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 1,
                lastRowPosition: 80,
            },
            cellData: new Map<string, CellData>([
                ['col1 row1', { ...makeCellData(1, 1, 'text'), cellStyle: 'cs1' }],
            ]),
            colX: [0],
            rowY: [0],
            colWidths: [100],
            rowHeights: [80],
            cellStyleToCSSStyle,
        };
    }

    /** How far the text's line box sits from the top of the cell. */
    function textOffsetFromTop(el: SpreadsheetCellGrid): number {
        const cell = el.shadowRoot!.querySelector<HTMLElement>('.cell')!;
        const range = document.createRange();
        range.selectNodeContents(cell);
        return range.getBoundingClientRect().top - cell.getBoundingClientRect().top;
    }

    it('puts text at the bottom by default, as Excel does', async () => {
        // A block cell put every value at the top of its row. The GWT
        // client made cells bottom-justified flex columns, which is also
        // what the server's alignment rules assume.
        const el = await attachWithState(tallState({ 1: '' }));

        expect(textOffsetFromTop(el)).to.be.greaterThan(40);

        cleanup(el);
    });

    it('follows a top alignment the workbook asks for', async () => {
        // The server says "top" with justify-content, which did nothing
        // while the cell was a block.
        const el = await attachWithState(tallState({ 1: 'justify-content:flex-start;' }));

        expect(textOffsetFromTop(el)).to.be.lessThan(10);

        cleanup(el);
    });

    it('follows a centre alignment the workbook asks for', async () => {
        const el = await attachWithState(tallState({ 1: 'justify-content:center;' }));

        const offset = textOffsetFromTop(el);
        expect(offset).to.be.greaterThan(15);
        expect(offset).to.be.lessThan(45);

        cleanup(el);
    });

    it('does not inherit the page line height', async () => {
        // A Vaadin page sets 1.625, which pushed a wrapped cell's second
        // line out of its row.
        const wrapper = document.createElement('div');
        wrapper.style.lineHeight = '1.625';
        document.body.appendChild(wrapper);
        const el = document.createElement(
            'vaadin-spreadsheet-internal-cell-grid'
        ) as SpreadsheetCellGrid;
        el.renderState = tallState({ 1: '' });
        wrapper.appendChild(el);
        await el.updateComplete;

        const cell = el.shadowRoot!.querySelector<HTMLElement>('.cell')!;
        expect(getComputedStyle(cell).lineHeight).to.equal('normal');

        wrapper.remove();
    });

    it('keeps two pixels either side, which the fit check assumes', async () => {
        const el = await attachWithState(tallState({ 1: '' }));

        const style = getComputedStyle(el.shadowRoot!.querySelector('.cell')!);
        expect(style.paddingLeft).to.equal('2px');
        expect(style.paddingRight).to.equal('2px');

        cleanup(el);
    });
});

describe('SpreadsheetCellGrid — the workbook default style and row/column styles', () => {
    const DEFAULT = '--default-font-family:Calibri;font-family:var(--default-font-family);--default-font-size:11pt;font-size:var(--default-font-size);color:rgb(0, 0, 0);';

    function grid(opts: {
        cells: [string, CellData][];
        styles: Record<number, string>;
        rows?: Record<number, number>;
        cols?: Record<number, number>;
    }): CellGridRenderState {
        return {
            ...emptyCellGridRenderState(),
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
            cellData: new Map<string, CellData>(opts.cells),
            colX: [0, 50, 100],
            rowY: [0, 20, 40],
            colWidths: [50, 50, 50],
            rowHeights: [20, 20, 20],
            cellStyleToCSSStyle: opts.styles,
            rowIndexToStyleIndex: opts.rows,
            columnIndexToStyleIndex: opts.cols,
        };
    }

    function cellAt(el: SpreadsheetCellGrid, col: number, row: number): HTMLElement {
        return el.shadowRoot!.querySelector<HTMLElement>(`.cell.col${col}.row${row}`)!;
    }

    it('gives a styled cell the default colour it does not set itself', async () => {
        // A style is sent with only what it changes from the default, so
        // cs5 here - bold, automatic colour - says nothing about colour. The
        // default had to reach it; scoped to .cs0 alone it did not, and the
        // cell came out the host's grey instead of black.
        const el = await attachWithState(
            grid({
                cells: [['col1 row1', { ...makeCellData(1, 1, 'x'), cellStyle: 'cs5' }]],
                styles: { 0: DEFAULT, 5: 'font-weight:bold;' },
            })
        );

        const style = getComputedStyle(cellAt(el, 1, 1));
        expect(style.color).to.equal('rgb(0, 0, 0)');
        expect(style.fontWeight).to.equal('700');
        expect(style.fontFamily).to.contain('Calibri');

        cleanup(el);
    });

    it('styles the empty cells of a formatted column', async () => {
        const el = await attachWithState(
            grid({
                cells: [],
                styles: { 0: DEFAULT, 7: 'background-color:rgb(255, 255, 0);' },
                cols: { 2: 7 },
            })
        );

        expect(getComputedStyle(cellAt(el, 2, 1)).backgroundColor).to.equal('rgb(255, 255, 0)');
        expect(getComputedStyle(cellAt(el, 2, 3)).backgroundColor).to.equal('rgb(255, 255, 0)');
        expect(getComputedStyle(cellAt(el, 1, 1)).backgroundColor).to.not.equal('rgb(255, 255, 0)');

        cleanup(el);
    });

    it('styles the empty cells of a formatted row', async () => {
        const el = await attachWithState(
            grid({
                cells: [],
                styles: { 0: DEFAULT, 8: 'font-style:italic;' },
                rows: { 3: 8 },
            })
        );

        expect(getComputedStyle(cellAt(el, 1, 3)).fontStyle).to.equal('italic');
        expect(getComputedStyle(cellAt(el, 1, 2)).fontStyle).to.equal('normal');

        cleanup(el);
    });

    it('lets a cell with its own style keep it inside a formatted column', async () => {
        const el = await attachWithState(
            grid({
                cells: [['col2 row1', { ...makeCellData(2, 1, 'x'), cellStyle: 'cs9' }]],
                styles: {
                    0: DEFAULT,
                    7: 'background-color:rgb(255, 255, 0);',
                    9: 'background-color:rgb(0, 128, 0);',
                },
                cols: { 2: 7 },
            })
        );

        expect(getComputedStyle(cellAt(el, 2, 1)).backgroundColor).to.equal('rgb(0, 128, 0)');

        cleanup(el);
    });

    it('lets an application set the cell font over the workbook default', async () => {
        const el = await attachWithState(
            grid({ cells: [], styles: { 0: DEFAULT } })
        );
        el.style.setProperty('--vaadin-spreadsheet-cell-font', 'monospace');
        el.style.setProperty('--vaadin-spreadsheet-cell-font-size', '20px');

        const style = getComputedStyle(cellAt(el, 1, 1));
        expect(style.fontFamily).to.equal('monospace');
        expect(style.fontSize).to.equal('20px');

        cleanup(el);
    });

    it('uses the workbook font when the application sets none', async () => {
        const el = await attachWithState(
            grid({ cells: [], styles: { 0: DEFAULT } })
        );

        const style = getComputedStyle(cellAt(el, 1, 1));
        expect(style.fontFamily).to.contain('Calibri');
        expect(style.fontSize).to.equal('14.6667px');

        cleanup(el);
    });
});

/**
 * The line along a freeze.
 *
 * The GWT client drew a dark line under the last frozen row and beside the
 * last frozen column - the border of its frozen panes - so the user could
 * see where the sheet stops scrolling. The Lit client drew nothing, and a
 * frozen sheet read as an ordinary one until it was scrolled.
 */
describe('SpreadsheetCellGrid — the line along a freeze', () => {
    // Rows 1-2 frozen at 20px each, columns 1-2 frozen at 50px each; rows
    // 3-4 and columns 3-4 scroll.
    function freezeState(rows: number, columns: number): CellGridRenderState {
        return {
            ...emptyCellGridRenderState(),
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
            colX: [100, 150],
            rowY: [40, 60],
            colWidths: [50, 50],
            rowHeights: [20, 20],
            verticalSplitPosition: rows,
            horizontalSplitPosition: columns,
            frozenColX: columns > 0 ? [0, 50] : [],
            frozenColWidths: columns > 0 ? [50, 50] : [],
            frozenRowY: rows > 0 ? [0, 20] : [],
            frozenRowHeights: rows > 0 ? [20, 20] : [],
            totalGridWidthPx: 1000,
            totalGridHeightPx: 500,
        };
    }

    function lines(el: SpreadsheetCellGrid, pane: string): HTMLElement[] {
        return Array.from(
            el.shadowRoot!.querySelectorAll<HTMLElement>(`.quadrant-wrapper-${pane} .freeze-line`)
        );
    }

    /** A box relative to the grid's own top-left corner. */
    function box(el: SpreadsheetCellGrid, line: HTMLElement): { x: number; y: number; w: number; h: number } {
        const origin = el.getBoundingClientRect();
        const r = line.getBoundingClientRect();
        return { x: r.left - origin.left, y: r.top - origin.top, w: r.width, h: r.height };
    }

    it('draws no line when nothing is frozen', async () => {
        const el = await attachWithState(freezeState(0, 0));

        expect(el.shadowRoot!.querySelectorAll('.freeze-line')).to.have.length(0);

        cleanup(el);
    });

    it('draws a dark line under the frozen rows, across the sheet', async () => {
        const el = await attachWithState(freezeState(2, 0));

        const [line] = lines(el, 'top');
        expect(line, 'a line in the frozen rows pane').to.exist;
        // One pixel below the last frozen row, where GWT drew it: over the
        // first scrolling row, leaving the frozen row's own gridline alone.
        expect(box(el, line)).to.deep.equal({ x: 0, y: 40, w: 1000, h: 1 });
        expect(getComputedStyle(line).backgroundColor).to.equal('rgb(106, 106, 106)');

        cleanup(el);
    });

    it('draws a dark line beside the frozen columns, down the sheet', async () => {
        const el = await attachWithState(freezeState(0, 2));

        const [line] = lines(el, 'left');
        expect(line, 'a line in the frozen columns pane').to.exist;
        expect(box(el, line)).to.deep.equal({ x: 100, y: 0, w: 1, h: 500 });

        cleanup(el);
    });

    it('draws the pieces the panes hide where frozen rows and columns meet', async () => {
        // The frozen columns' cells cover the rows' line where they cross
        // it, and the frozen rows' cells cover the columns' line. The
        // corner sits above both.
        const el = await attachWithState(freezeState(2, 2));

        const corner = lines(el, 'corner').map((line) => box(el, line));
        expect(corner).to.deep.include({ x: 0, y: 40, w: 101, h: 1 });
        expect(corner).to.deep.include({ x: 100, y: 0, w: 1, h: 41 });

        cleanup(el);
    });

    it('stays on top of the cells that scroll under it', async () => {
        const state = freezeState(2, 0);
        state.cellData = new Map<string, CellData>([['col3 row3', makeCellData(3, 3, 'under')]]);
        const el = await attachWithState(state);
        // The grid clips to its own box, which a sized sheet gives it.
        el.style.height = '200px';

        const [line] = lines(el, 'top');
        // Hit-testable only for this check, to ask what is on top there.
        line.style.pointerEvents = 'auto';
        const origin = el.getBoundingClientRect();
        const top = el.shadowRoot!.elementFromPoint(origin.left + 120, origin.top + 40);
        expect(top).to.equal(line);

        cleanup(el);
    });

    it('lets a click through to the cell under it', async () => {
        const el = await attachWithState(freezeState(2, 2));

        for (const line of el.shadowRoot!.querySelectorAll<HTMLElement>('.freeze-line')) {
            expect(getComputedStyle(line).pointerEvents).to.equal('none');
        }

        cleanup(el);
    });
});

/**
 * A sheet with its gridlines turned off.
 *
 * The server sent the flag and the Lit client stored it, but nothing read
 * it: every sheet drew its grid, including the ones a workbook - or an
 * application calling setGridlinesVisible(false) - had turned it off for.
 */
describe('SpreadsheetCellGrid — gridlines turned off', () => {
    function gridState(
        displayGridlines: boolean | undefined,
        overrides: Partial<CellGridRenderState> = {}
    ): CellGridRenderState {
        return {
            ...emptyCellGridRenderState(),
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 2,
                lastColumnPosition: 100,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 2,
                lastRowPosition: 40,
            },
            colX: [0, 50],
            rowY: [0, 20],
            colWidths: [50, 50],
            rowHeights: [20, 20],
            displayGridlines,
            ...overrides,
        };
    }

    function borders(el: SpreadsheetCellGrid, selector: string): string {
        const style = getComputedStyle(el.shadowRoot!.querySelector(selector)!);
        return `${style.borderRightWidth} ${style.borderBottomWidth}`;
    }

    it('draws the grid when nothing says otherwise', async () => {
        const el = await attachWithState(gridState(undefined));

        expect(borders(el, '.cell.col1.row1')).to.equal('1px 1px');

        cleanup(el);
    });

    it('drops the grid when the sheet turns it off', async () => {
        const el = await attachWithState(gridState(false));

        expect(borders(el, '.cell.col1.row1')).to.equal('0px 0px');
        expect(borders(el, '.cell.col2.row2')).to.equal('0px 0px');

        cleanup(el);
    });

    it('keeps a border the workbook gives a cell', async () => {
        const el = await attachWithState(
            gridState(false, {
                cellData: new Map<string, CellData>([
                    ['col1 row1', { ...makeCellData(1, 1, 'x'), cellStyle: 'cs1' }],
                ]),
                cellStyleToCSSStyle: { 1: 'border-right: 2px solid rgb(0, 0, 255);' },
            })
        );

        expect(borders(el, '.cell.col1.row1')).to.equal('2px 0px');

        cleanup(el);
    });

    it('keeps a border a conditional format gives a cell', async () => {
        const el = await attachWithState(
            gridState(false, {
                cellData: new Map<string, CellData>([
                    ['col1 row1', { ...makeCellData(1, 1, 'x'), cellStyle: 'cs0 cf2' }],
                ]),
                conditionalFormattingStyles: { 2: 'border-bottom: 3px solid rgb(255, 0, 0);' },
            })
        );

        expect(borders(el, '.cell.col1.row1')).to.equal('0px 3px');

        cleanup(el);
    });

    it('keeps a border shifted onto the neighbour of a cell', async () => {
        // A cell's left border is drawn as its left neighbour's right one.
        const el = await attachWithState(
            gridState(false, {
                shiftedCellBorderStyles: ['.col1.row2 { border-right: 1px solid rgb(0, 128, 0); }'],
            })
        );

        expect(borders(el, '.cell.col1.row2')).to.equal('1px 0px');

        cleanup(el);
    });

    it('drops the grid from merged cells too', async () => {
        const el = await attachWithState(
            gridState(false, {
                mergedRegions: [{ col1: 1, col2: 2, row1: 1, row2: 1, id: 1 }],
            })
        );

        expect(borders(el, '.merged-cell')).to.equal('0px 0px');

        cleanup(el);
    });
});

/**
 * Merged cells in the frozen rows and columns.
 *
 * Only the scrolling quadrant drew its merges, so a merge in the frozen
 * rows - the title across the top of a report, a centred heading over the
 * months - never appeared, and its first cell showed through instead, left
 * aligned and clipped at its own column.
 */
describe('SpreadsheetCellGrid — merged cells in a freeze', () => {
    // Rows 1-2 frozen at 20px, columns 1-2 frozen at 50px; rows 3-4 and
    // columns 3-4 scroll.
    function freezeWithMerges(
        merges: Array<{ col1: number; col2: number; row1: number; row2: number }>,
        scrollTop = 0
    ): CellGridRenderState {
        const cellData = new Map<string, CellData>();
        for (const m of merges) {
            cellData.set(`col${m.col1} row${m.row1}`, makeCellData(m.col1, m.row1, 'Heading'));
        }
        return {
            ...emptyCellGridRenderState(),
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
            cellData,
            colX: [100, 150],
            rowY: [40, 60],
            colWidths: [50, 50],
            rowHeights: [20, 20],
            verticalSplitPosition: 2,
            horizontalSplitPosition: 2,
            frozenColX: [0, 50],
            frozenColWidths: [50, 50],
            frozenRowY: [0, 20],
            frozenRowHeights: [20, 20],
            totalGridWidthPx: 1000,
            totalGridHeightPx: 500,
            scrollTop,
            mergedRegions: merges.map((m, id) => ({ ...m, id })),
        };
    }

    function merged(el: SpreadsheetCellGrid, pane: string): HTMLElement | null {
        return el.shadowRoot!.querySelector<HTMLElement>(`.quadrant-wrapper-${pane} .merged-cell`);
    }

    it('draws a merge in the frozen rows, in their pane', async () => {
        const el = await attachWithState(freezeWithMerges([{ col1: 3, col2: 4, row1: 1, row2: 1 }]));

        const cell = merged(el, 'top');
        expect(cell, 'a merged cell among the frozen rows').to.exist;
        expect(cell!.textContent!.trim()).to.equal('Heading');
        expect(cell!.style.width).to.equal('100px');
        expect(cell!.style.top).to.equal('0px');

        cleanup(el);
    });

    it('draws a merge in the frozen columns, in their pane', async () => {
        const el = await attachWithState(freezeWithMerges([{ col1: 1, col2: 1, row1: 3, row2: 4 }]));

        const cell = merged(el, 'left');
        expect(cell, 'a merged cell among the frozen columns').to.exist;
        expect(cell!.style.height).to.equal('40px');
        expect(cell!.style.left).to.equal('0px');

        cleanup(el);
    });

    it('draws a merge where frozen rows and columns meet, in the corner', async () => {
        const el = await attachWithState(freezeWithMerges([{ col1: 1, col2: 2, row1: 1, row2: 2 }]));

        const cell = merged(el, 'corner');
        expect(cell, 'a merged cell in the corner').to.exist;
        expect(`${cell!.style.width} ${cell!.style.height}`).to.equal('100px 40px');

        cleanup(el);
    });

    it('still draws a merge among the scrolling cells, once', async () => {
        const el = await attachWithState(freezeWithMerges([{ col1: 3, col2: 4, row1: 3, row2: 3 }]));

        const all = el.shadowRoot!.querySelectorAll('.merged-cell');
        expect(all).to.have.length(1);
        expect(all[0].closest('.quadrant-wrapper'), 'outside the frozen panes').to.be.null;

        cleanup(el);
    });

    it('keeps a merge that scrolls under the frozen rows beneath them', async () => {
        // Scrolled down 40px, the frozen rows sit where rows 3-4 are, so a
        // merge in row 3 passes underneath them - it must not cover them.
        const el = await attachWithState(freezeWithMerges([{ col1: 3, col2: 4, row1: 3, row2: 3 }], 40));
        el.style.height = '300px';

        const origin = el.getBoundingClientRect();
        const top = el.shadowRoot!.elementFromPoint(origin.left + 120, origin.top + 50) as HTMLElement;
        expect(top.closest('.quadrant-wrapper-top'), 'a frozen cell is on top').to.not.be.null;

        cleanup(el);
    });
});

/**
 * Merged cells drawn from the boxes the connector works out.
 *
 * A merge used to be drawn only while its top-left cell was on screen and
 * only as far as its own quadrant reached, so a tall merge vanished as soon
 * as its first row scrolled away, and one starting in the frozen rows was
 * cut off at the split.
 */
describe('SpreadsheetCellGrid — merged cells drawn from their boxes', () => {
    // Rows 1-2 frozen at 20px; the scrolling rows on screen are 5-6.
    function withBoxes(
        regions: Array<{ col1: number; col2: number; row1: number; row2: number }>,
        boxes: Array<{ left: number; top: number; width: number; height: number }>
    ): CellGridRenderState {
        return {
            ...emptyCellGridRenderState(),
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 4,
                lastColumnPosition: 200,
                firstRowIndex: 5,
                firstRowPosition: 40,
                lastRowIndex: 6,
                lastRowPosition: 80,
            },
            cellData: new Map<string, CellData>(
                regions.map((r) => [`col${r.col1} row${r.row1}`, makeCellData(r.col1, r.row1, 'Merged')])
            ),
            colX: [0, 50, 100, 150],
            rowY: [80, 100],
            colWidths: [50, 50, 50, 50],
            rowHeights: [20, 20],
            verticalSplitPosition: 2,
            frozenRowY: [0, 20],
            frozenRowHeights: [20, 20],
            totalGridWidthPx: 1000,
            totalGridHeightPx: 1000,
            mergedRegions: regions.map((r, id) => ({ ...r, id })),
            mergedRegionBoxes: boxes,
        };
    }

    it('draws a merge whose first rows have scrolled out of view', async () => {
        // Rows 3-6 merged; rows 3 and 4 are above what is on screen.
        const el = await attachWithState(
            withBoxes([{ col1: 2, col2: 3, row1: 3, row2: 6 }], [{ left: 50, top: 40, width: 100, height: 80 }])
        );

        const cell = el.shadowRoot!.querySelector<HTMLElement>('.merged-cell');
        expect(cell, 'still drawn').to.exist;
        expect(cell!.closest('.quadrant-wrapper'), 'with the scrolling cells').to.be.null;
        expect(`${cell!.style.top} ${cell!.style.height}`).to.equal('40px 80px');

        cleanup(el);
    });

    it('draws a merge that runs past the frozen rows at its whole size', async () => {
        const el = await attachWithState(
            withBoxes([{ col1: 1, col2: 2, row1: 2, row2: 4 }], [{ left: 0, top: 20, width: 100, height: 60 }])
        );

        const cell = el.shadowRoot!.querySelector<HTMLElement>('.quadrant-wrapper-top .merged-cell');
        expect(cell, 'pinned with the frozen rows').to.exist;
        expect(cell!.style.height).to.equal('60px');

        cleanup(el);
    });

    it('leaves out a merge with nothing in view', async () => {
        const el = await attachWithState(
            withBoxes([{ col1: 1, col2: 2, row1: 40, row2: 45 }], [{ left: 0, top: 780, width: 100, height: 120 }])
        );

        expect(el.shadowRoot!.querySelector('.merged-cell')).to.be.null;

        cleanup(el);
    });
});
