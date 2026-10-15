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
 * Cell editor.
 *
 * Tests the in-place cell editor across two layers:
 *  (1) cell-grid element: render the input when editor state is
 * set, dispatch cell-doubleclick, dispatch cell-editor-commit
 *      / cell-editor-cancel from input keydown.
 *  (2) connector: double-click triggers editor activation,
 * commit dispatches cellValueEdited spreadsheet-event,
 * cancel clears editor state.
 */

async function attachCellGridWithState(
    state: CellGridRenderState
): Promise<SpreadsheetCellGrid> {
    const el = document.createElement(
        'vaadin-spreadsheet-internal-cell-grid'
    ) as SpreadsheetCellGrid;
    el.renderState = state;
    document.body.appendChild(el);
    await el.updateComplete;
    return el;
}

function buildEditorState(opts: {
    col: number;
    row: number;
    initialValue: string;
}): CellGridRenderState {
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
        editor: { col: opts.col, row: opts.row, initialValue: opts.initialValue },
        overlays: [],
        selection: emptyCellGridRenderState().selection,
    };
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

/**
 * What the editor opens with.
 *
 * The editor shows what the cell IS, not what it displays: the formula
 * behind a computed cell, and the unformatted value otherwise. The
 * displayed value has already dropped a text cell's leading apostrophe
 * and a number's full precision, and editing from it would silently
 * rewrite the cell.
 */
describe('Spreadsheet cell editor — the value it opens with', () => {
  function attach(): { sheet: Spreadsheet; cleanup: () => void } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    const sheet = new Spreadsheet(host);
    sheet.setRows(20);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(20).fill(15));
    return {
      sheet,
      cleanup: () => {
        sheet.dispose();
        host.remove();
      }
    };
  }

  function openWithF2(sheet: Spreadsheet, col: number, row: number): void {
    sheet.setSelectedCellAndRange('X', col, row, col, col, row, row, false);
    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'F2', bubbles: true, composed: true })
    );
  }

  it('opens a text cell with its leading apostrophe', async () => {
    const { sheet, cleanup } = attach();
    sheet.updateBottomRightCellValues([
      { row: 4, col: 2, value: 'abc', originalValue: "'abc", cellStyle: 'cs0' } as any
    ]);
    sheet.recomputeViewport();

    openWithF2(sheet, 2, 4);
    await sheet.getCellGrid().updateComplete;

    const input = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLInputElement>('[part~="cell-input"]')!;
    expect(input.value).to.equal("'abc");
    cleanup();
  });

  it('opens a computed cell with its formula', async () => {
    const { sheet, cleanup } = attach();
    sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: '42', formulaValue: 'SUM(B1:B9)', cellStyle: 'cs0' } as any
    ]);
    sheet.recomputeViewport();

    openWithF2(sheet, 2, 2);
    await sheet.getCellGrid().updateComplete;

    const input = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLInputElement>('[part~="cell-input"]')!;
    expect(input.value).to.equal('=SUM(B1:B9)');
    cleanup();
  });

  it('opens a double-clicked cell the same way', async () => {
    const { sheet, cleanup } = attach();
    sheet.updateBottomRightCellValues([
      { row: 4, col: 2, value: 'abc', originalValue: "'abc", cellStyle: 'cs0' } as any
    ]);
    sheet.recomputeViewport();

    sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-doubleclick', {
        detail: { col: 2, row: 4 },
        bubbles: true,
        composed: true
      })
    );
    await sheet.getCellGrid().updateComplete;

    const input = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLInputElement>('[part~="cell-input"]')!;
    expect(input.value).to.equal("'abc");
    cleanup();
  });

  it('opens an empty cell empty', async () => {
    const { sheet, cleanup } = attach();
    sheet.recomputeViewport();

    openWithF2(sheet, 3, 3);
    await sheet.getCellGrid().updateComplete;

    const input = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLInputElement>('[part~="cell-input"]')!;
    expect(input.value).to.equal('');
    cleanup();
  });
});

describe('SpreadsheetCellGrid — cell editor render', () => {
    it('renders nothing when editor state is null', async () => {
        const empty = emptyCellGridRenderState();
        const el = await attachCellGridWithState(empty);

        expect(el.shadowRoot!.querySelector('.cell-editor')).to.be.null;

        el.remove();
    });

    it('renders an input.cell-editor when editor state is set', async () => {
        const el = await attachCellGridWithState(
            buildEditorState({ col: 2, row: 2, initialValue: 'hello' })
        );

        const input = el.shadowRoot!.querySelector(
            'input.cell-editor'
        ) as HTMLInputElement;
        expect(input).to.not.be.null;
        expect(input.value).to.equal('hello');

        el.remove();
    });

    it('positions the input over the edited cell (matching colX/rowY/widths/heights)', async () => {
        const el = await attachCellGridWithState(
            buildEditorState({ col: 2, row: 3, initialValue: '' })
        );

        const input = el.shadowRoot!.querySelector(
            '.cell-editor'
        ) as HTMLElement;
        // col 2 → colX[1] = 50; row 3 → rowY[2] = 40.
        // width = colWidths[1] = 50, and a pixel more over the gridline at
        // its right, as the GWT client sized it; height = rowHeights[2] = 20.
        expect(input.style.left).to.equal('50px');
        expect(input.style.top).to.equal('40px');
        expect(input.style.width).to.equal('51px');
        expect(input.style.height).to.equal('20px');

        el.remove();
    });

    it('returns nothing when editor coord is outside the viewport AND frozen ranges', async () => {
        const el = await attachCellGridWithState({
            ...buildEditorState({ col: 100, row: 100, initialValue: 'x' }),
        });

        // The editor's col=100 is past lastColumnIndex (3) and not
        // in frozenColX (length 0) — render() returns no input.
        expect(el.shadowRoot!.querySelector('.cell-editor')).to.be.null;

        el.remove();
    });
});

/**
 * The editor's size, as the GWT client set it: the cell, or the whole merged
 * cell when the cell starts one, and a pixel more over the gridline at its
 * right. Text that outgrows it widens it by whole columns, as it is typed,
 * until the text fits; deleting narrows it again.
 */
describe('SpreadsheetCellGrid — the editor stand-in', () => {
    // The GWT client's input was always there, hidden while nothing was
    // edited, and TestBench's getCellValueInput() hands this one out then,
    // so tests asking whether the editor is displayed keep working.
    /** A drawn sheet with no cell being edited. */
    function drawnWithoutEditor(): CellGridRenderState {
        return { ...buildEditorState({ col: 1, row: 1, initialValue: '' }), editor: null };
    }

    it('is there, hidden, while no cell is being edited', async () => {
        const el = await attachCellGridWithState(drawnWithoutEditor());

        const standIn = el.shadowRoot!.querySelector<HTMLInputElement>('.cell-input-stand-in')!;
        expect(standIn).to.not.be.null;
        expect(getComputedStyle(standIn).display).to.equal('none');
        expect(standIn.inert).to.be.true;
        expect(standIn.tabIndex).to.equal(-1);
        expect(standIn.getAttribute('aria-hidden')).to.equal('true');

        el.remove();
    });

    it('is not taken for the editor, nor styled as it', async () => {
        const el = await attachCellGridWithState(drawnWithoutEditor());

        expect(el.shadowRoot!.querySelector('[part~="cell-input"]')).to.be.null;
        expect(el.shadowRoot!.querySelector('.cell-input-stand-in')!.hasAttribute('part')).to.be.false;

        el.remove();
    });

    it('stays hidden while a cell is being edited, beside the real editor', async () => {
        const el = await attachCellGridWithState(buildEditorState({ col: 2, row: 2, initialValue: 'x' }));

        expect(el.shadowRoot!.querySelectorAll('[part~="cell-input"]')).to.have.length(1);
        expect(getComputedStyle(el.shadowRoot!.querySelector('.cell-input-stand-in')!).display).to.equal('none');

        el.remove();
    });
});

describe('SpreadsheetCellGrid — the editor grows with its text', () => {
    // Five 50px columns and three 20px rows.
    function state(editor: CellGridRenderState['editor'], overrides: Partial<CellGridRenderState> = {}): CellGridRenderState {
        return {
            ...buildEditorState({ col: 1, row: 1, initialValue: '' }),
            viewport: {
                firstColumnIndex: 1,
                firstColumnPosition: 0,
                lastColumnIndex: 5,
                lastColumnPosition: 250,
                firstRowIndex: 1,
                firstRowPosition: 0,
                lastRowIndex: 3,
                lastRowPosition: 60,
            },
            colX: [0, 50, 100, 150, 200],
            colWidths: [50, 50, 50, 50, 50],
            totalGridWidthPx: 250,
            editor,
            ...overrides,
        };
    }

    function input(el: SpreadsheetCellGrid): HTMLInputElement {
        return el.shadowRoot!.querySelector('.cell-editor') as HTMLInputElement;
    }

    /** What the text is drawn at in a cell of the sheet, padding and all. */
    function drawnWidth(el: SpreadsheetCellGrid, text: string): number {
        const cell = getComputedStyle(el.shadowRoot!.querySelector('.cell.col1.row1')!);
        const context = document.createElement('canvas').getContext('2d')!;
        context.font = cell.font;
        return context.measureText(text).width + parseFloat(cell.paddingLeft) + parseFloat(cell.paddingRight);
    }

    /** As many x's as come out between two widths, clear of both. */
    function textBetween(el: SpreadsheetCellGrid, min: number, max: number): string {
        for (let text = 'x'; text.length < 200; text += 'x') {
            const width = drawnWidth(el, text);
            if (width > min + 3 && width < max - 3) {
                return text;
            }
        }
        throw new Error(`no run of x's between ${min} and ${max}px`);
    }

    function type(el: SpreadsheetCellGrid, value: string): void {
        input(el).value = value;
        input(el).dispatchEvent(new InputEvent('input', { bubbles: true, composed: true }));
    }

    it('covers a merged cell whole', async () => {
        const el = await attachCellGridWithState(
            state(
                { col: 2, row: 1, initialValue: '' },
                {
                    mergedRegions: [{ id: 1, col1: 2, row1: 1, col2: 3, row2: 2 }],
                    mergedRegionBoxes: [{ left: 50, top: 0, width: 100, height: 40 }],
                }
            )
        );

        const box = input(el).getBoundingClientRect();
        const grid = el.getBoundingClientRect();
        expect([box.left - grid.left, box.top - grid.top, box.width, box.height]).to.deep.equal([50, 0, 101, 40]);

        el.remove();
    });

    it('opens wide enough for the value it opens with', async () => {
        const probe = await attachCellGridWithState(state(null));
        const value = textBetween(probe, 100, 150);
        probe.remove();

        const el = await attachCellGridWithState(state({ col: 1, row: 1, initialValue: value }));

        expect(input(el).getBoundingClientRect().width).to.equal(151);

        el.remove();
    });

    it('widens by whole columns as the text outgrows it', async () => {
        const el = await attachCellGridWithState(state({ col: 1, row: 1, initialValue: '' }));
        expect(input(el).getBoundingClientRect().width).to.equal(51);

        type(el, textBetween(el, 50, 100));
        expect(input(el).getBoundingClientRect().width).to.equal(101);

        type(el, textBetween(el, 150, 200));
        expect(input(el).getBoundingClientRect().width).to.equal(201);

        el.remove();
    });

    it('narrows again as the text is deleted', async () => {
        const el = await attachCellGridWithState(state({ col: 1, row: 1, initialValue: '' }));
        type(el, textBetween(el, 150, 200));

        type(el, 'x');

        expect(input(el).getBoundingClientRect().width).to.equal(51);

        el.remove();
    });

    it('grows from the far side of a merged cell', async () => {
        const el = await attachCellGridWithState(
            state(
                { col: 1, row: 1, initialValue: '' },
                {
                    mergedRegions: [{ id: 1, col1: 1, row1: 1, col2: 2, row2: 1 }],
                    mergedRegionBoxes: [{ left: 0, top: 0, width: 100, height: 20 }],
                }
            )
        );

        type(el, textBetween(el, 100, 150));

        expect(input(el).getBoundingClientRect().width).to.equal(151);

        el.remove();
    });

    it('stops at the last column drawn', async () => {
        const el = await attachCellGridWithState(state({ col: 4, row: 1, initialValue: '' }));

        type(el, textBetween(el, 150, 200));

        // Columns 4 and 5 are all there is; the rest of the text scrolls
        // inside, as it would past the sheet's last column.
        expect(input(el).getBoundingClientRect().width).to.equal(101);

        el.remove();
    });

    it('keeps its width when the sheet draws again', async () => {
        const el = await attachCellGridWithState(state({ col: 1, row: 1, initialValue: '' }));
        type(el, textBetween(el, 50, 100));

        // The columns move along, as a scroll that draws other ones moves
        // them, so the editor's place is written afresh.
        el.renderState = { ...el.renderState, colX: [10, 60, 110, 160, 210] };
        await el.updateComplete;

        expect(input(el).style.left).to.equal('10px');
        expect(input(el).getBoundingClientRect().width).to.equal(101);

        el.remove();
    });

    it('starts from the cell again when the next edit opens', async () => {
        const el = await attachCellGridWithState(state({ col: 1, row: 1, initialValue: '' }));
        type(el, textBetween(el, 50, 100));

        el.renderState = { ...el.renderState, editor: null };
        await el.updateComplete;
        el.renderState = { ...el.renderState, editor: { col: 1, row: 1, initialValue: '' } };
        await el.updateComplete;

        expect(input(el).getBoundingClientRect().width).to.equal(51);

        el.remove();
    });
});

/**
 * The editor's frame, as the GWT client drew it: no border, lifted off the
 * sheet on a soft shadow, with the text right at the cell's edge. The ring
 * is the editor colour, which the Lumo theme sets and an application may.
 */
describe('SpreadsheetCellGrid — the editor frame', () => {
    const SHADOW = 'rgba(0, 0, 0, 0.75) 0px 0px 10px 0px';

    function editorStyle(el: SpreadsheetCellGrid): CSSStyleDeclaration {
        return getComputedStyle(el.shadowRoot!.querySelector('.cell-editor')!);
    }

    it('has no border or padding, and a soft shadow', async () => {
        const el = await attachCellGridWithState(buildEditorState({ col: 2, row: 2, initialValue: 'x' }));

        const style = editorStyle(el);
        expect([style.borderTopWidth, style.borderRightWidth, style.borderBottomWidth, style.borderLeftWidth])
            .to.deep.equal(['0px', '0px', '0px', '0px']);
        expect(style.padding).to.equal('0px');
        expect(style.boxShadow).to.equal(SHADOW);

        el.remove();
    });

    it('has no ring of its own', async () => {
        const el = await attachCellGridWithState(buildEditorState({ col: 2, row: 2, initialValue: 'x' }));

        expect(editorStyle(el).outlineColor).to.equal('rgba(0, 0, 0, 0)');

        el.remove();
    });

    it('is ringed in the colour an application sets', async () => {
        const el = await attachCellGridWithState(buildEditorState({ col: 2, row: 2, initialValue: 'x' }));
        el.style.setProperty('--vaadin-spreadsheet-editor-border', 'rgb(255, 0, 255)');

        const style = editorStyle(el);
        expect(`${style.outlineWidth} ${style.outlineStyle} ${style.outlineColor}`).to.equal('1px solid rgb(255, 0, 255)');
        expect(style.borderTopWidth).to.equal('0px');
        expect(style.boxShadow).to.equal(SHADOW);

        el.remove();
    });
});

/**
 * The editor looks like the cell it edits.
 *
 * The GWT client gave the input the edited cell's own classes, so the user
 * typed in the cell's font, colour, fill and alignment. Without them the
 * editor wrote in the page's font on white, and a bold red value turned
 * plain black the moment it was opened.
 */
describe('SpreadsheetCellGrid — the editor looks like the cell it edits', () => {
    // The workbook default in the shape the server writes it.
    const DEFAULT_STYLE =
        'background-color: rgb(255, 255, 255); color: rgb(0, 0, 0); ' +
        'font-family: var(--default-font-family); --default-font-family: Georgia; ' +
        'font-size: var(--default-font-size); --default-font-size: 20px;';

    function editing(
        cellStyle: string | undefined,
        overrides: Partial<CellGridRenderState> = {}
    ): CellGridRenderState {
        const cellData = new Map<string, CellData>();
        if (cellStyle !== undefined) {
            cellData.set('col2 row2', {
                col: 2,
                row: 2,
                value: 'x',
                cellStyle,
                locked: false,
                needsMeasure: false,
                isPercentage: false,
            });
        }
        return {
            ...buildEditorState({ col: 2, row: 2, initialValue: 'x' }),
            cellData,
            cellStyleToCSSStyle: { 0: DEFAULT_STYLE },
            ...overrides,
        };
    }

    function editorStyle(el: SpreadsheetCellGrid): CSSStyleDeclaration {
        return getComputedStyle(el.shadowRoot!.querySelector('.cell-editor')!);
    }

    it('writes in the workbook font when the cell has no style of its own', async () => {
        const el = await attachCellGridWithState(editing(undefined));

        const style = editorStyle(el);
        expect(style.fontFamily).to.equal('Georgia');
        expect(style.fontSize).to.equal('20px');
        expect(style.color).to.equal('rgb(0, 0, 0)');

        el.remove();
    });

    it("takes the font and colour of the cell's own style", async () => {
        const el = await attachCellGridWithState(
            editing('cs1', {
                cellStyleToCSSStyle: {
                    0: DEFAULT_STYLE,
                    1:
                        'color: rgb(255, 0, 0); font-weight: bold; font-style: italic; ' +
                        'font-family: var(--default-font-family); --default-font-family: Verdana;',
                },
            })
        );

        const style = editorStyle(el);
        expect(style.color).to.equal('rgb(255, 0, 0)');
        expect(style.fontWeight).to.equal('700');
        expect(style.fontStyle).to.equal('italic');
        expect(style.fontFamily).to.equal('Verdana');

        el.remove();
    });

    it("wears the cell's fill", async () => {
        const el = await attachCellGridWithState(
            editing('cs1', {
                cellStyleToCSSStyle: { 0: DEFAULT_STYLE, 1: 'background-color: rgb(255, 255, 0);' },
            })
        );

        expect(editorStyle(el).backgroundColor).to.equal('rgb(255, 255, 0)');

        el.remove();
    });

    it('right-aligns a number, as the cell does', async () => {
        const el = await attachCellGridWithState(editing('cs0 r'));

        expect(editorStyle(el).textAlign).to.equal('right');

        el.remove();
    });

    it('follows an alignment the workbook sets', async () => {
        const el = await attachCellGridWithState(
            editing('cs1', {
                cellStyleToCSSStyle: { 0: DEFAULT_STYLE, 1: 'text-align: center;' },
            })
        );

        expect(editorStyle(el).textAlign).to.equal('center');

        el.remove();
    });

    it('takes the style of a formatted row when the cell has none', async () => {
        const el = await attachCellGridWithState(
            editing('cs0', {
                cellStyleToCSSStyle: { 0: DEFAULT_STYLE, 1: 'color: rgb(0, 128, 0);' },
                rowIndexToStyleIndex: { 2: 1 },
            })
        );

        expect(editorStyle(el).color).to.equal('rgb(0, 128, 0)');

        el.remove();
    });

    it('shows a conditional format the cell matches', async () => {
        const el = await attachCellGridWithState(
            editing('cs1 cf3', {
                cellStyleToCSSStyle: { 0: DEFAULT_STYLE, 1: 'color: rgb(255, 0, 0);' },
                conditionalFormattingStyles: { 3: 'color: rgb(0, 0, 255);' },
            })
        );

        expect(editorStyle(el).color).to.equal('rgb(0, 0, 255)');

        el.remove();
    });

    it("keeps its own border and padding whatever the cell's style says", async () => {
        // The cell's borders, indent and wrapping are the cell's box, not
        // its text. Worn by the editor, they replaced its border and pushed
        // the caret along.
        const el = await attachCellGridWithState(
            editing('cs1', {
                cellStyleToCSSStyle: {
                    0: DEFAULT_STYLE,
                    1:
                        'border-right: 5px solid rgb(0, 128, 0); border-bottom: 5px solid rgb(0, 128, 0); ' +
                        'padding-left: 3em; overflow:hidden;white-space:normal;word-wrap:break-word;',
                },
            })
        );

        const style = editorStyle(el);
        expect(style.borderRightWidth).to.equal('0px');
        expect(style.borderBottomWidth).to.equal('0px');
        expect(style.paddingLeft).to.equal('0px');

        el.remove();
    });

    it('lets an application set the font through the documented property', async () => {
        const el = await attachCellGridWithState(editing(undefined));
        el.style.setProperty('--vaadin-spreadsheet-cell-font', 'monospace');
        await el.updateComplete;

        expect(editorStyle(el).fontFamily).to.equal('monospace');

        el.remove();
    });

    // The editor's style is gathered apart from the rules that style the
    // cells, so the two must agree: whatever the cell under it looks like,
    // the editor looks the same.
    const CASES: Array<[string, string | undefined, Partial<CellGridRenderState>]> = [
        ['a cell the server sent nothing for', undefined, {}],
        ['a cell with a style of its own', 'cs1', {}],
        ['an unstyled cell in a formatted row', 'cs0', { rowIndexToStyleIndex: { 2: 1 } }],
        ['an unstyled cell in a formatted column', 'cs0', { columnIndexToStyleIndex: { 2: 2 } }],
        [
            'an unstyled cell where a formatted row meets a formatted column',
            'cs0',
            { rowIndexToStyleIndex: { 2: 2 }, columnIndexToStyleIndex: { 2: 1 } },
        ],
        ['a styled cell a conditional format matches', 'cs1 cf0', {}],
        [
            'an unstyled cell in a formatted row that a conditional format matches',
            'cs0 cf0',
            { rowIndexToStyleIndex: { 2: 1 } },
        ],
        ['a number', 'cs0 r', {}],
        ['a number in a formatted row', 'cs0 r', { rowIndexToStyleIndex: { 2: 2 } }],
    ];
    const PROPERTIES = [
        'color',
        'backgroundColor',
        'fontFamily',
        'fontSize',
        'fontWeight',
        'fontStyle',
        'textAlign',
    ] as const;

    for (const [name, cellStyle, overrides] of CASES) {
        it(`matches the cell under it: ${name}`, async () => {
            const el = await attachCellGridWithState(
                editing(cellStyle, {
                    cellStyleToCSSStyle: {
                        0: DEFAULT_STYLE,
                        1: 'color: rgb(255, 0, 0); font-weight: bold; background-color: rgb(255, 255, 0);',
                        2: 'font-style: italic; text-align: center; color: rgb(0, 128, 0); ' +
                            'font-family: var(--default-font-family); --default-font-family: Verdana;',
                    },
                    conditionalFormattingStyles: { 0: 'color: rgb(0, 0, 255);' },
                    ...overrides,
                })
            );

            const cell = getComputedStyle(el.shadowRoot!.querySelector('.cell.col2.row2')!);
            const editor = editorStyle(el);
            for (const property of PROPERTIES) {
                expect(editor[property], property).to.equal(cell[property]);
            }

            el.remove();
        });
    }
});

describe('SpreadsheetCellGrid — double-click delegation', () => {
    it('dispatches cell-doubleclick on a cell dblclick with the coord', async () => {
        const empty = emptyCellGridRenderState();
        // Build a state with cells visible.
        const state: CellGridRenderState = {
            ...empty,
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
            totalGridWidthPx: 150,
            totalGridHeightPx: 60,
        };
        const el = await attachCellGridWithState(state);

        let received: { col: number; row: number } | undefined;
        el.addEventListener('cell-doubleclick', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        const cell = el.shadowRoot!.querySelector(
            '.col2.row2'
        ) as HTMLElement;
        cell.dispatchEvent(new MouseEvent('dblclick', {
            bubbles: true, composed: true,
        }));

        expect(received).to.deep.equal({ col: 2, row: 2 });

        el.remove();
    });

    it('does NOT dispatch cell-doubleclick when dblclick is not on a cell', async () => {
        const empty = emptyCellGridRenderState();
        const el = await attachCellGridWithState(empty);

        let received = false;
        el.addEventListener('cell-doubleclick', () => { received = true; });

        el.shadowRoot!.dispatchEvent(new MouseEvent('dblclick', {
            bubbles: true,
        }));

        expect(received).to.be.false;
        el.remove();
    });
});

describe('SpreadsheetCellGrid — editor input keydown', () => {
    it('Enter dispatches cell-editor-commit with the input value', async () => {
        const el = await attachCellGridWithState(
            buildEditorState({ col: 2, row: 2, initialValue: 'old' })
        );

        let received: {
            col: number; row: number; value: string;
            key?: string; shiftKey?: boolean;
        } | undefined;
        el.addEventListener('cell-editor-commit', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        const input = el.shadowRoot!.querySelector(
            '.cell-editor'
        ) as HTMLInputElement;
        input.value = 'new value';
        input.dispatchEvent(new KeyboardEvent('keydown', {
            key: 'Enter',
            bubbles: true,
            composed: true,
            cancelable: true,
        }));

        expect(received!.col).to.equal(2);
        expect(received!.row).to.equal(2);
        expect(received!.value).to.equal('new value');
        // Key+shift are also in the detail so the
        // connector can pick the next active-cell direction.
        expect(received!.key).to.equal('Enter');
        expect(received!.shiftKey).to.equal(false);
        el.remove();
    });

    it('Tab dispatches cell-editor-commit too', async () => {
        const el = await attachCellGridWithState(
            buildEditorState({ col: 1, row: 1, initialValue: '' })
        );

        let received = false;
        el.addEventListener('cell-editor-commit', () => { received = true; });

        const input = el.shadowRoot!.querySelector(
            '.cell-editor'
        ) as HTMLInputElement;
        input.dispatchEvent(new KeyboardEvent('keydown', {
            key: 'Tab',
            bubbles: true,
            composed: true,
        }));

        expect(received).to.be.true;
        el.remove();
    });

    it('Escape dispatches cell-editor-cancel (NOT commit)', async () => {
        const el = await attachCellGridWithState(
            buildEditorState({ col: 1, row: 1, initialValue: 'x' })
        );

        let commit = false;
        let cancel = false;
        el.addEventListener('cell-editor-commit', () => { commit = true; });
        el.addEventListener('cell-editor-cancel', () => { cancel = true; });

        const input = el.shadowRoot!.querySelector(
            '.cell-editor'
        ) as HTMLInputElement;
        input.dispatchEvent(new KeyboardEvent('keydown', {
            key: 'Escape',
            bubbles: true,
            composed: true,
        }));

        expect(cancel).to.be.true;
        expect(commit).to.be.false;
        el.remove();
    });

    it('blur dispatches cell-editor-commit (Excel convention)', async () => {
        const el = await attachCellGridWithState(
            buildEditorState({ col: 1, row: 1, initialValue: 'bye' })
        );

        let committed: { value: string } | undefined;
        el.addEventListener('cell-editor-commit', (e: Event) => {
            committed = (e as CustomEvent).detail;
        });

        const input = el.shadowRoot!.querySelector(
            '.cell-editor'
        ) as HTMLInputElement;
        input.value = 'changed';
        input.dispatchEvent(new FocusEvent('blur', { bubbles: false }));

        expect(committed!.value).to.equal('changed');
        el.remove();
    });
});

/**
 * Keys an input method composes with are its own. Confirming a Japanese or
 * Chinese conversion with Enter committed the cell and moved on, before the
 * text was even in it.
 */
describe('SpreadsheetCellGrid — the editor and an input method', () => {
    async function editor(): Promise<{ el: HTMLElement; input: HTMLInputElement; seen: string[] }> {
        const el = await attachCellGridWithState(
            buildEditorState({ col: 2, row: 2, initialValue: '' })
        );
        const seen: string[] = [];
        el.addEventListener('cell-editor-commit', () => seen.push('commit'));
        el.addEventListener('cell-editor-cancel', () => seen.push('cancel'));
        const input = el.shadowRoot!.querySelector('.cell-editor') as HTMLInputElement;
        return { el, input, seen };
    }

    function keydown(input: HTMLElement, key: string, init: KeyboardEventInit = {}, keyCode?: number): void {
        const event = new KeyboardEvent('keydown', { key, bubbles: true, composed: true, cancelable: true, ...init });
        if (keyCode !== undefined) {
            Object.defineProperty(event, 'keyCode', { get: () => keyCode });
        }
        input.dispatchEvent(event);
    }

    it('leaves the Enter that confirms a conversion to the input method', async () => {
        const { el, input, seen } = await editor();

        keydown(input, 'Enter', { isComposing: true });

        expect(seen).to.eql([]);
        el.remove();
    });

    it("leaves Safari's confirming Enter, with key code 229, to it as well", async () => {
        const { el, input, seen } = await editor();

        keydown(input, 'Enter', {}, 229);

        expect(seen).to.eql([]);
        el.remove();
    });

    it('leaves the Escape that drops a conversion to it, and keeps the edit', async () => {
        const { el, input, seen } = await editor();

        keydown(input, 'Escape', { isComposing: true });

        expect(seen).to.eql([]);
        el.remove();
    });

    it('commits on the Enter after the text is in', async () => {
        const { el, input, seen } = await editor();

        keydown(input, 'Enter', { isComposing: false }, 13);

        expect(seen).to.eql(['commit']);
        el.remove();
    });
});

describe('Spreadsheet — cell editor lifecycle through the connector', () => {
    it('cell-doubleclick activates the editor at the dbl-clicked cell', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 3, row: 2 },
            bubbles: true,
            composed: true,
        }));

        const editor = sheet.getCellGrid().renderState.editor;
        expect(editor).to.not.be.null;
        expect(editor!.col).to.equal(3);
        expect(editor!.row).to.equal(2);

        cleanup();
    });

    it('editor loads the cell value when the cell has cellData', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.updateBottomRightCellValues([
            { col: 2, row: 2, value: 'existing', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
        ]);
        sheet.recomputeViewport();

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 2 },
            bubbles: true,
            composed: true,
        }));

        const editor = sheet.getCellGrid().renderState.editor;
        expect(editor!.initialValue).to.equal('existing');

        cleanup();
    });

    it('editor loads an empty initial value when the cell has no data', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 4, row: 4 },
            bubbles: true,
            composed: true,
        }));

        const editor = sheet.getCellGrid().renderState.editor;
        expect(editor!.initialValue).to.equal('');

        cleanup();
    });

    it('cell-editor-commit dispatches cellValueEdited spreadsheet-event + clears editor', () => {
        const { host, sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();

        // Activate editor at (3, 2) first.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 3, row: 2 },
            bubbles: true,
            composed: true,
        }));
        expect(sheet.getCellGrid().renderState.editor).to.not.be.null;

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'cellValueEdited') {
                received = detail;
            }
        });

        // Commit with a new value.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-commit', {
            detail: { col: 3, row: 2, value: 'committed value' },
            bubbles: true,
            composed: true,
        }));

        // The cellValueEdited event was dispatched with the correct payload.
        expect(received).to.exist;
        expect(received!.data).to.deep.equal({
            row: 2, col: 3, value: 'committed value',
        });
        // The editor was cleared.
        expect(sheet.getCellGrid().renderState.editor).to.be.null;

        cleanup();
    });

    it('F2 opens the editor at the active cell with the cell value', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.updateBottomRightCellValues([
            { col: 2, row: 2, value: 'cell value', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
        ]);
        // Move active to (2, 2).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'F2',
            bubbles: true,
            cancelable: true,
        }));

        const editor = sheet.getCellGrid().renderState.editor;
        expect(editor).to.not.be.null;
        expect(editor!.col).to.equal(2);
        expect(editor!.row).to.equal(2);
        expect(editor!.initialValue).to.equal('cell value');

        cleanup();
    });

    it('F2 is a no-op when the editor is already open', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Open editor at (3, 3) via dblclick.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 3, row: 3 },
            bubbles: true, composed: true,
        }));
        const beforeEditor = sheet.getCellGrid().renderState.editor;

        // F2 should not re-open / change anything.
        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'F2',
            bubbles: true,
            cancelable: true,
        }));

        // Editor unchanged (same coords, same value).
        const afterEditor = sheet.getCellGrid().renderState.editor;
        expect(afterEditor!.col).to.equal(beforeEditor!.col);
        expect(afterEditor!.row).to.equal(beforeEditor!.row);

        cleanup();
    });

    it('type-to-edit: a printable key opens the editor with that char as initial value', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        // Move active to (3, 3) — the cell has no existing value.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'x',
            bubbles: true,
            cancelable: true,
        }));

        const editor = sheet.getCellGrid().renderState.editor;
        expect(editor).to.not.be.null;
        expect(editor!.col).to.equal(3);
        expect(editor!.row).to.equal(3);
        // Initial value is the typed char (Excel convention — the
        // typed key REPLACES the cell's content; if the user wants
        // to keep the cell and append, they'd press F2 instead).
        expect(editor!.initialValue).to.equal('x');

        cleanup();
    });

    it('Space opens the editor empty, as the GWT client did', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.updateBottomRightCellValues([
            { col: 3, row: 3, value: 'old', cellStyle: 'cs0', locked: false, needsMeasure: false, isPercentage: false },
        ]);
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: ' ',
            bubbles: true,
            cancelable: true,
        }));

        const editor = sheet.getCellGrid().renderState.editor;
        expect(editor).to.not.be.null;
        expect(editor!.initialValue).to.equal('');

        cleanup();
    });

    it('type-to-edit ignores keys with Ctrl/Meta modifiers', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Ctrl+c (copy shortcut) should NOT open the editor.
        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'c',
            ctrlKey: true,
            bubbles: true,
            cancelable: true,
        }));
        expect(sheet.getCellGrid().renderState.editor).to.be.null;

        // Meta+v (paste on Mac) should NOT open the editor either.
        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'v',
            metaKey: true,
            bubbles: true,
            cancelable: true,
        }));
        expect(sheet.getCellGrid().renderState.editor).to.be.null;

        cleanup();
    });

    it('type-to-edit is a no-op when editor is already open', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Open editor at (3, 3) via F2 first.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'F2',
            bubbles: true,
            cancelable: true,
        }));
        const beforeInitial = sheet.getCellGrid().renderState.editor!.initialValue;

        // Typing 'a' should NOT overwrite the open editor; the
        // input itself handles the keystroke via DOM focus.
        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'a',
            bubbles: true,
            cancelable: true,
        }));

        // Editor's initialValue is unchanged (typing into the
        // input updates input.value, not renderState.editor).
        expect(sheet.getCellGrid().renderState.editor!.initialValue).to.equal(beforeInitial);

        cleanup();
    });

    it('type-to-edit does not fire for navigation keys', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Enter is not in this list: on a single cell it opens the
        // editor, the same case F2 is, which is what the GWT client did
        // and what the ITs that fill a column depend on.
        for (const key of ['ArrowDown', 'Tab', 'Home', 'End', 'Escape', 'F1', 'PageDown']) {
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key,
                bubbles: true,
                cancelable: true,
            }));
            expect(sheet.getCellGrid().renderState.editor, key).to.be.null;
        }

        cleanup();
    });

    it('Enter commit moves the active cell DOWN by 1', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();

        // Open editor at (2, 2).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 2 },
            bubbles: true, composed: true,
        }));

        // Commit via Enter.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-commit', {
            detail: { col: 2, row: 2, value: 'x', key: 'Enter', shiftKey: false },
            bubbles: true, composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 3 });
        cleanup();
    });

    it('Shift+Enter commit moves the active cell UP by 1', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 3 },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-commit', {
            detail: { col: 2, row: 3, value: 'x', key: 'Enter', shiftKey: true },
            bubbles: true, composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
        cleanup();
    });

    it('Tab commit moves the active cell RIGHT by 1', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 2 },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-commit', {
            detail: { col: 2, row: 2, value: 'x', key: 'Tab', shiftKey: false },
            bubbles: true, composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 2 });
        cleanup();
    });

    it('Shift+Tab commit moves the active cell LEFT by 1', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 3, row: 2 },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-commit', {
            detail: { col: 3, row: 2, value: 'x', key: 'Tab', shiftKey: true },
            bubbles: true, composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
        cleanup();
    });

    it('blur commit (no key) leaves the active cell unchanged', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Real-world: a click precedes the dblclick, setting active.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 3, row: 3 },
            bubbles: true, composed: true,
        }));
        // Blur commit: no key in detail.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-commit', {
            detail: { col: 3, row: 3, value: 'x' },
            bubbles: true, composed: true,
        }));

        // Active cell stays at the committed cell — no move.
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 3 });
        cleanup();
    });

    it('Enter commit at last row stays at last row (clamped)', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(3);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15]);

        // Real-world: a click precedes the dblclick, setting active.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 3 },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-commit', {
            detail: { col: 2, row: 3, value: 'x', key: 'Enter', shiftKey: false },
            bubbles: true, composed: true,
        }));

        // row stays at 3 (last).
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 3 });
        cleanup();
    });

    it('double-click does NOT open editor on a locked cell when sheet is protected', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setSheetProtected(true);
        sheet.updateBottomRightCellValues([
            { col: 2, row: 2, value: 'x', cellStyle: 'cs0',
              locked: true, needsMeasure: false, isPercentage: false },
        ]);
        sheet.recomputeViewport();

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 2 },
            bubbles: true,
            composed: true,
        }));

        expect(sheet.getCellGrid().renderState.editor).to.be.null;
        cleanup();
    });

    it('double-click opens editor on a locked cell when sheet is NOT protected', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setSheetProtected(false);
        sheet.updateBottomRightCellValues([
            { col: 2, row: 2, value: 'x', cellStyle: 'cs0',
              locked: true, needsMeasure: false, isPercentage: false },
        ]);
        sheet.recomputeViewport();

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 2 },
            bubbles: true,
            composed: true,
        }));

        expect(sheet.getCellGrid().renderState.editor).to.not.be.null;
        cleanup();
    });

    it('double-click opens editor on an unlocked cell when sheet IS protected', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setSheetProtected(true);
        sheet.updateBottomRightCellValues([
            { col: 2, row: 2, value: 'x', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
        ]);
        sheet.recomputeViewport();

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 2 },
            bubbles: true,
            composed: true,
        }));

        expect(sheet.getCellGrid().renderState.editor).to.not.be.null;
        cleanup();
    });

    it('F2 does NOT open editor on a locked cell when sheet is protected', () => {
        const { host, sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setSheetProtected(true);
        // Default-locked: no CellData entry at the active cell.
        sheet.recomputeViewport();

        host.dispatchEvent(new KeyboardEvent('keydown', {
            key: 'F2', bubbles: true, composed: true,
        }));

        expect(sheet.getCellGrid().renderState.editor).to.be.null;
        cleanup();
    });

    it('type-to-edit does NOT open editor on a locked cell when sheet is protected', () => {
        const { host, sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setSheetProtected(true);
        sheet.recomputeViewport();

        host.dispatchEvent(new KeyboardEvent('keydown', {
            key: 'a', bubbles: true, composed: true,
        }));

        expect(sheet.getCellGrid().renderState.editor).to.be.null;
        cleanup();
    });

    it('cell-editor-cancel clears the editor WITHOUT dispatching cellValueEdited', () => {
        const { host, sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 1, row: 1 },
            bubbles: true,
            composed: true,
        }));

        let received = false;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            if ((e as CustomEvent).detail.type === 'cellValueEdited') {
                received = true;
            }
        });

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-cancel', {
            detail: { col: 1, row: 1, value: 'ignored' },
            bubbles: true,
            composed: true,
        }));

        expect(received).to.be.false;
        expect(sheet.getCellGrid().renderState.editor).to.be.null;

        cleanup();
    });
});

/**
 * Excel edits cells in two modes, and only the arrow keys tell them apart.
 *
 * Typing over a cell is "enter" mode: an arrow commits what was typed and
 * moves the selection the way it points. Opening the existing value with F2
 * or a double-click is "edit" mode: an arrow moves the caret through the
 * text instead. The connector only ever did the second, so typing a value
 * and pressing an arrow left the editor open and the value uncommitted.
 */
describe('Spreadsheet — committing an edit with a key', () => {
    function editorInput(sheet: Spreadsheet): HTMLInputElement | null {
        return sheet.getCellGrid().shadowRoot!.querySelector('[part~="cell-input"]');
    }

    function attach(): { host: HTMLElement; sheet: Spreadsheet; sent: any[]; cleanup: () => void } {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setCols(10);
        sheet.setRows(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));
        sheet.recomputeViewport();
        const sent: any[] = [];
        host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));
        return {
            host,
            sheet,
            sent,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    /** Select a cell and start typing into it — Excel's "enter" mode. */
    async function typeOver(sheet: Spreadsheet, col: number, row: number, text: string) {
        sheet.setSelectedCellAndRange('', col, row, col, col, row, row, false);
        sheet
            .getScrollContainer()
            .dispatchEvent(
                new KeyboardEvent('keydown', { key: text[0], bubbles: true, composed: true })
            );
        await sheet.getCellGrid().updateComplete;
        const input = editorInput(sheet)!;
        input.value = text;
        return input;
    }

    /** Open the existing value with F2 — Excel's "edit" mode. */
    async function openExisting(sheet: Spreadsheet, col: number, row: number) {
        sheet.setSelectedCellAndRange('', col, row, col, col, row, row, false);
        sheet
            .getScrollContainer()
            .dispatchEvent(new KeyboardEvent('keydown', { key: 'F2', bubbles: true, composed: true }));
        await sheet.getCellGrid().updateComplete;
        return editorInput(sheet)!;
    }

    function press(input: HTMLInputElement, key: string, shiftKey = false) {
        input.dispatchEvent(
            new KeyboardEvent('keydown', { key, shiftKey, bubbles: true, composed: true })
        );
    }

    let ctx: ReturnType<typeof attach>;

    beforeEach(() => {
        ctx = attach();
    });

    afterEach(() => {
        ctx.cleanup();
    });

    it('commits a typed value when an arrow key is pressed', async () => {
        const input = await typeOver(ctx.sheet, 2, 2, '123');
        ctx.sent.length = 0;

        press(input, 'ArrowDown');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sent.filter((e) => e.type === 'cellValueEdited')).to.eql([
            { type: 'cellValueEdited', data: { row: 2, col: 2, value: '123' } },
        ]);
    });

    it('moves the selection the way the arrow points', async () => {
        const input = await typeOver(ctx.sheet, 2, 2, '123');

        press(input, 'ArrowDown');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sheet.getSelection().activeCell).to.eql({ col: 2, row: 3 });
    });

    it('moves right on ArrowRight', async () => {
        const input = await typeOver(ctx.sheet, 2, 2, '123');

        press(input, 'ArrowRight');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sheet.getSelection().activeCell).to.eql({ col: 3, row: 2 });
    });

    it('moves up on ArrowUp', async () => {
        const input = await typeOver(ctx.sheet, 2, 2, '123');

        press(input, 'ArrowUp');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sheet.getSelection().activeCell).to.eql({ col: 2, row: 1 });
    });

    it('closes the editor after an arrow commit', async () => {
        const input = await typeOver(ctx.sheet, 2, 2, '123');

        press(input, 'ArrowDown');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(editorInput(ctx.sheet)).to.be.null;
    });

    it('leaves the caret alone when the existing value is being edited', async () => {
        const input = await openExisting(ctx.sheet, 2, 2);
        ctx.sent.length = 0;

        press(input, 'ArrowDown');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sent.filter((e) => e.type === 'cellValueEdited')).to.be.empty;
        expect(editorInput(ctx.sheet), 'the editor closed').to.not.be.null;
    });

    it('still commits on Enter from edit mode', async () => {
        const input = await openExisting(ctx.sheet, 2, 2);
        input.value = 'edited';
        ctx.sent.length = 0;

        press(input, 'Enter');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sent.filter((e) => e.type === 'cellValueEdited')).to.eql([
            { type: 'cellValueEdited', data: { row: 2, col: 2, value: 'edited' } },
        ]);
        expect(ctx.sheet.getSelection().activeCell).to.eql({ col: 2, row: 3 });
    });

    it('puts the keyboard back on the sheet after a commit', async () => {
        // Closing the editor drops the focus on the body, where the
        // sheet's own shortcuts — arrows, Ctrl+Z, Ctrl+C — never arrive.
        const input = await typeOver(ctx.sheet, 2, 2, '123');

        press(input, 'Enter');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sheet.getScrollContainer().matches(':focus')).to.be.true;
    });

    it('puts the keyboard back on the sheet after Escape', async () => {
        const input = await typeOver(ctx.sheet, 2, 2, '123');

        press(input, 'Escape');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sheet.getScrollContainer().matches(':focus')).to.be.true;
    });

    it('stops at the edge of the sheet', async () => {
        const input = await typeOver(ctx.sheet, 1, 1, 'x');

        press(input, 'ArrowUp');
        await ctx.sheet.getCellGrid().updateComplete;

        expect(ctx.sheet.getSelection().activeCell).to.eql({ col: 1, row: 1 });
    });
});

/**
 * Enter on a single cell opens its editor, exactly as F2 does. The GWT
 * client treated the two keys as one case, and the ITs that fill a
 * column type a value, press Enter to commit, and press Enter again to
 * open the next cell. Treating the second Enter as "move on" skipped
 * every other row, and the values landed in A1, A3, A5 instead of
 * A1, A2, A3.
 */
describe('Spreadsheet cell editor — Enter opens it', () => {
    function attach(): { sheet: Spreadsheet; cleanup: () => void } {
        const host = document.createElement('div');
        host.style.cssText =
            'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setRows(20);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        return {
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    function press(sheet: Spreadsheet, key: string): void {
        sheet
            .getScrollContainer()
            .dispatchEvent(
                new KeyboardEvent('keydown', {
                    key,
                    bubbles: true,
                    composed: true,
                })
            );
    }

    async function editorInput(
        sheet: Spreadsheet
    ): Promise<HTMLInputElement | null> {
        await sheet.getCellGrid().updateComplete;
        return sheet
            .getCellGrid()
            .shadowRoot!.querySelector<HTMLInputElement>('.cell-editor');
    }

    it('opens the editor on the selected cell', async () => {
        const { sheet, cleanup } = attach();
        sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);

        press(sheet, 'Enter');

        expect(await editorInput(sheet), 'no editor').to.not.be.null;
        expect(sheet.getSelection().activeCell).to.eql({ col: 2, row: 2 });
        cleanup();
    });

    it('opens it with the value the cell already has', async () => {
        const { sheet, cleanup } = attach();
        sheet.updateBottomRightCellValues([
            {
                row: 2,
                col: 2,
                value: '42',
                originalValue: '42',
                cellStyle: 'cs0',
            } as any,
        ]);
        sheet.recomputeViewport();
        sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);

        press(sheet, 'Enter');

        expect((await editorInput(sheet))!.value).to.equal('42');
        cleanup();
    });

    it('moves on instead when the cell is hidden', async () => {
        const { sheet, cleanup } = attach();
        sheet.setHiddenRowIndexes([2]);
        sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);

        press(sheet, 'Enter');

        expect(await editorInput(sheet), 'editor on a hidden cell').to.be.null;
        expect(sheet.getSelection().activeCell).to.eql({ col: 2, row: 3 });
        cleanup();
    });

    it('walks the selection instead when a range is selected', async () => {
        const { sheet, cleanup } = attach();
        sheet.setSelectedCellAndRange('B2', 2, 2, 2, 3, 2, 4, false);

        press(sheet, 'Enter');

        expect(await editorInput(sheet), 'editor on a range').to.be.null;
        expect(sheet.getSelection().activeCell).to.eql({ col: 2, row: 3 });
        cleanup();
    });
});
