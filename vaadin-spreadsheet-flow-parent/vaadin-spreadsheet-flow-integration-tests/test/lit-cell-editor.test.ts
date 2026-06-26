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
import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import {
    type CellGridRenderState,
    emptyCellGridRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/elements/types.js';

/**
 * Slice 152 — cell editor.
 *
 * Tests the in-place cell editor across two layers:
 *  (1) cell-grid element: render the input when editor state is
 *      set, dispatch cell-doubleclick, dispatch cell-editor-commit
 *      / cell-editor-cancel from input keydown.
 *  (2) connector: double-click triggers editor activation,
 *      commit dispatches cellValueEdited spreadsheet-event,
 *      cancel clears editor state.
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
        selection: emptyCellGridRenderState().selection,
    };
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

describe('SpreadsheetCellGrid — cell editor render (slice 152)', () => {
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
        // width = colWidths[1] = 50; height = rowHeights[2] = 20.
        expect(input.style.left).to.equal('50px');
        expect(input.style.top).to.equal('40px');
        expect(input.style.width).to.equal('50px');
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

describe('SpreadsheetCellGrid — double-click delegation (slice 152)', () => {
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

describe('SpreadsheetCellGrid — editor input keydown (slice 152)', () => {
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
        // Slice 158: key+shift are also in the detail so the
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

describe('Spreadsheet — cell editor lifecycle through the connector (slice 152)', () => {
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

    it('F2 opens the editor at the active cell with the cell value (slice 153)', () => {
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

    it('F2 is a no-op when the editor is already open (slice 153)', () => {
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

    it('type-to-edit: a printable key opens the editor with that char as initial value (slice 153)', () => {
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

    it('type-to-edit ignores keys with Ctrl/Meta modifiers (slice 153)', () => {
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

    it('type-to-edit is a no-op when editor is already open (slice 153)', () => {
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

    it('type-to-edit does not fire for navigation keys (slice 153)', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        for (const key of ['ArrowDown', 'Tab', 'Enter', 'Home', 'End', 'Escape', 'F1', 'PageDown']) {
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key,
                bubbles: true,
                cancelable: true,
            }));
            expect(sheet.getCellGrid().renderState.editor, key).to.be.null;
        }

        cleanup();
    });

    it('Enter commit moves the active cell DOWN by 1 (slice 158)', () => {
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

    it('Shift+Enter commit moves the active cell UP by 1 (slice 158)', () => {
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

    it('Tab commit moves the active cell RIGHT by 1 (slice 158)', () => {
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

    it('Shift+Tab commit moves the active cell LEFT by 1 (slice 158)', () => {
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

    it('blur commit (no key) leaves the active cell unchanged (slice 158)', () => {
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

    it('Enter commit at last row stays at last row (clamped) (slice 158)', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(3);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15]);

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
