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

/**
 * Smoke tests for the skeleton `Spreadsheet` class. These don't
 * exercise rendering behavior (that's Phase 3.2+); they pin the
 * contract that the class is constructable + every setter accepts its
 * documented type + the read accessors return what was set.
 *
 * The real value is COMPILE-TIME: every test below requires the class
 * to satisfy `SpreadsheetClientApi`. If a method is renamed / removed
 * / wrong-typed, this file fails to compile, surfacing the
 * contract drift immediately.
 */

describe('Spreadsheet skeleton — constructor + identity', () => {
    it('is constructable with a host element', () => {
        const host = document.createElement('div');
        const sheet = new Spreadsheet(host);

        expect(sheet).to.be.instanceOf(Spreadsheet);
    });

    it('sets host width + height via setters', () => {
        const host = document.createElement('div');
        const sheet = new Spreadsheet(host);

        sheet.setWidth('500px');
        sheet.setHeight('400px');

        expect(host.style.width).to.equal('500px');
        expect(host.style.height).to.equal('400px');
    });
});

describe('Spreadsheet skeleton — property mirrors', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = new Spreadsheet(document.createElement('div'));
    });

    it('stores + returns rows + cols', () => {
        sheet.setRows(100);
        sheet.setCols(20);

        expect(sheet.getRows()).to.equal(100);
        expect(sheet.getCols()).to.equal(20);
    });

    it('stores + returns sheet navigation state', () => {
        sheet.setSheetIndex(2);
        sheet.setSheetNames(['Sheet1', 'Sheet2', 'Sheet3']);

        expect(sheet.getSheetIndex()).to.equal(2);
        expect(sheet.getSheetNames()).to.deep.equal([
            'Sheet1',
            'Sheet2',
            'Sheet3',
        ]);
    });

    it('stores + returns merged regions', () => {
        const regions = [
            { id: 1, col1: 1, col2: 3, row1: 1, row2: 1 },
            { id: 2, col1: 5, col2: 7, row1: 2, row2: 4 },
        ];

        sheet.setMergedRegions(regions);

        expect(sheet.getMergedRegions()).to.deep.equal(regions);
    });

    it('stores + returns sheet-protected state', () => {
        expect(sheet.isSheetProtected()).to.be.false;

        sheet.setSheetProtected(true);

        expect(sheet.isSheetProtected()).to.be.true;
    });
});

describe('Spreadsheet skeleton — RPC stubs are callable without throwing', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = new Spreadsheet(document.createElement('div'));
    });

    it('the 12 explicit RPC methods are all callable', () => {
        expect(() => sheet.updateBottomRightCellValues([])).to.not.throw();
        expect(() => sheet.updateTopLeftCellValues([])).to.not.throw();
        expect(() => sheet.updateTopRightCellValues([])).to.not.throw();
        expect(() => sheet.updateBottomLeftCellValues([])).to.not.throw();
        expect(() => sheet.updateFormulaBar('Sheet1!A1', 1, 1)).to.not.throw();
        expect(() => sheet.invalidCellAddress()).to.not.throw();
        expect(() =>
            sheet.showSelectedCell('A1', 1, 1, 'val', false, false, true)
        ).to.not.throw();
        expect(() => sheet.showActions([])).to.not.throw();
        expect(() =>
            sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false)
        ).to.not.throw();
        expect(() => sheet.cellsUpdated([])).to.not.throw();
        expect(() => sheet.refreshCellStyles()).to.not.throw();
        expect(() => sheet.editCellComment(1, 1)).to.not.throw();
    });

    it('lifecycle methods are callable', () => {
        expect(() => sheet.relayout()).to.not.throw();
        expect(() => sheet.resize()).to.not.throw();
        expect(() => sheet.notifyStateChanges([], true)).to.not.throw();
    });

    it('popup-button methods are callable', () => {
        expect(() =>
            sheet.onPopupButtonOpened(1, 1, 'content', 'app')
        ).to.not.throw();
        expect(() => sheet.closePopup(1, 1)).to.not.throw();
        expect(() => sheet.addPopupButton('{}')).to.not.throw();
        expect(() => sheet.removePopupButton('{}')).to.not.throw();
    });
});

describe('Spreadsheet skeleton — initial selection state', () => {
    it('starts with A1 (single-cell) selected', () => {
        const sheet = new Spreadsheet(document.createElement('div'));

        const sel = sheet.getSelection();

        expect(sel.activeCell).to.deep.equal({ col: 1, row: 1 });
        expect(sel.activeRange).to.deep.equal({ c1: 1, c2: 1, r1: 1, r2: 1 });
        expect(sel.auxRanges).to.deep.equal([]);
    });
});

describe('Spreadsheet skeleton — freeze + display state (slice 126)', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = new Spreadsheet(document.createElement('div'));
    });

    it('freeze split positions are all callable + storable', () => {
        expect(() => sheet.setVerticalSplitPosition(3)).to.not.throw();
        expect(() => sheet.setHorizontalSplitPosition(2)).to.not.throw();
    });

    it('display flags are all callable', () => {
        expect(() => sheet.setDisplayGridlines(false)).to.not.throw();
        expect(() => sheet.setDisplayRowColHeadings(false)).to.not.throw();
        expect(() => sheet.setLockFormatColumns(true)).to.not.throw();
        expect(() => sheet.setLockFormatRows(true)).to.not.throw();
        expect(() => sheet.setHasActions(true)).to.not.throw();
        expect(() => sheet.setWorkbookChangeToggle(true)).to.not.throw();
        expect(() => sheet.setWorkbookProtected(true)).to.not.throw();
    });

    it('display content setters are all callable', () => {
        expect(() => sheet.setInfoLabelValue('Sheet1: A1')).to.not.throw();
        expect(() => sheet.setHyperlinksTooltips({ 'col1 row1': 'see http://...' }))
            .to.not.throw();
        expect(() => sheet.setId('spreadsheet-1')).to.not.throw();
        expect(() => sheet.setClass('spreadsheet themed')).to.not.throw();
    });
});

describe('Spreadsheet skeleton — callback registry (slice 126)', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = new Spreadsheet(document.createElement('div'));
    });

    it('representative selection-event callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setCellSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setCellRangeSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setRowSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setColumnSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setSelectionIncreasePaintedCallback(noop)).to.not.throw();
        expect(() => sheet.setSelectionDecreasePaintedCallback(noop)).to.not.throw();
        expect(() =>
            sheet.setCellAddedToSelectionAndSelectedCallback(noop)
        ).to.not.throw();
        expect(() =>
            sheet.setCellsAddedToRangeSelectionCallback(noop)
        ).to.not.throw();
        expect(() =>
            sheet.setRowAddedToRangeSelectionCallback(noop)
        ).to.not.throw();
        expect(() =>
            sheet.setColumnAddedToSelectionCallback(noop)
        ).to.not.throw();
    });

    it('sheet-lifecycle + edit + scroll callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setSheetSelectedCallback(noop)).to.not.throw();
        expect(() => sheet.setSheetRenamedCallback(noop)).to.not.throw();
        expect(() => sheet.setSheetCreatedCallback(noop)).to.not.throw();
        expect(() => sheet.setCellValueEditedCallback(noop)).to.not.throw();
        expect(() => sheet.setOnSheetScrollCallback(noop)).to.not.throw();
        expect(() => sheet.setSheetAddressChangedCallback(noop)).to.not.throw();
        expect(() => sheet.setCellRangePaintedCallback(noop)).to.not.throw();
        expect(() => sheet.setDeleteSelectedCellsCallback(noop)).to.not.throw();
    });

    it('action + context-menu callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setContextMenuClosedCallback(noop)).to.not.throw();
        expect(() => sheet.setContextMenuOpenOnSelectionCallback(noop)).to.not.throw();
        expect(() => sheet.setRowHeaderContextMenuOpenCallback(noop)).to.not.throw();
        expect(() =>
            sheet.setColumnHeaderContextMenuOpenCallback(noop)
        ).to.not.throw();
        expect(() => sheet.setActionOnCurrentSelectionCallback(noop)).to.not.throw();
        expect(() => sheet.setActionOnRowHeaderCallback(noop)).to.not.throw();
        expect(() => sheet.setActionOnColumnHeaderCallback(noop)).to.not.throw();
    });

    it('resize + autofit + undo/redo + grouping callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setRowsResizedCallback(noop)).to.not.throw();
        expect(() => sheet.setColumnResizedCallback(noop)).to.not.throw();
        expect(() => sheet.setOnRowAutofitCallback(noop)).to.not.throw();
        expect(() => sheet.setOnColumnAutofitCallback(noop)).to.not.throw();
        expect(() => sheet.setOnUndoCallback(noop)).to.not.throw();
        expect(() => sheet.setOnRedoCallback(noop)).to.not.throw();
        expect(() => sheet.setGroupingCollapsedCallback(noop)).to.not.throw();
        expect(() => sheet.setLevelHeaderClickedCallback(noop)).to.not.throw();
    });

    it('cut/paste + protected-write + comment + connector callbacks are all callable', () => {
        const noop = () => {};
        expect(() => sheet.setOnPasteCallback(noop)).to.not.throw();
        expect(() => sheet.setClearSelectedCellsOnCutCallback(noop)).to.not.throw();
        expect(() => sheet.setProtectedCellWriteAttemptedCallback(noop)).to.not.throw();
        expect(() => sheet.setUpdateCellCommentCallback(noop)).to.not.throw();
        expect(() => sheet.setOnConnectorInitCallback(noop)).to.not.throw();
        expect(() => sheet.setLinkCellClickedCallback(noop)).to.not.throw();
        expect(() => sheet.setSetCellStyleWidthRatiosCallback(noop)).to.not.throw();
        expect(() => sheet.setPopupButtonClickCallback(noop)).to.not.throw();
        expect(() => sheet.setPopupCloseCallback(noop)).to.not.throw();
    });
});

describe('Spreadsheet skeleton — legacy spreadsheetWidget escape hatch (slice 126)', () => {
    it('exposes spreadsheetWidget as undefined (not throw on read)', () => {
        const sheet = new Spreadsheet(document.createElement('div'));
        // The GWT-era wrapper reads `api.spreadsheetWidget.sheetWidget.
        // definedRowHeights` to time first render. The new impl returns
        // undefined; the wrapper's existence check handles this safely.
        expect(sheet.spreadsheetWidget).to.be.undefined;
    });
});

describe('Spreadsheet — render pipeline (slice 134)', () => {
    function makeSheet(): Spreadsheet {
        const host = document.createElement('div');
        return new Spreadsheet(host);
    }

    it('creates + owns a SpreadsheetCellGrid sub-element on construction', () => {
        const sheet = makeSheet();

        const cellGrid = sheet.getCellGrid();
        expect(cellGrid).to.not.be.undefined;
        expect(cellGrid.tagName.toLowerCase()).to.equal(
            'vaadin-spreadsheet-internal-cell-grid'
        );
    });

    it('appends the cell-grid to the host element (via the scroll container)', () => {
        // Slice 140: a scroll container now wraps the cell-grid so the
        // host's scrollbar tracks reflect the full virtual-grid extent.
        // The cell-grid is the scroll container's sole child.
        const host = document.createElement('div');
        const sheet = new Spreadsheet(host);

        expect(host.children).to.have.length(1);
        expect(host.firstElementChild).to.equal(sheet.getScrollContainer());
        expect(sheet.getScrollContainer().firstElementChild).to.equal(
            sheet.getCellGrid()
        );
    });

    it('buildRenderState returns a viewport that covers the visible range', () => {
        const sheet = makeSheet();
        // Configure a small sheet: 10x10 with uniform sizes.
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        // Row heights are in POINTS; 15pt × 96/72 = 20px.
        sheet.setRowH(new Array(10).fill(15));

        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 200,
            viewportHeight: 80,
        });

        // 200px / 50px = 4 columns. Loop fills lastColumnPosition until
        // it crosses the threshold; at scrollLeft=0 + viewport 200, this
        // means lastColumnIndex satisfies lastColumnPosition >= 200.
        expect(state.viewport.firstColumnIndex).to.equal(1);
        expect(state.viewport.lastColumnIndex).to.be.greaterThanOrEqual(4);
        expect(state.viewport.firstRowIndex).to.equal(1);
        expect(state.viewport.lastRowIndex).to.be.greaterThanOrEqual(4);
    });

    it('buildRenderState populates colX, rowY, widths, heights arrays for visible range', () => {
        const sheet = makeSheet();
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]); // 20px each at 96 ppi
        sheet.setRowBufferSize(0);
        sheet.setColumnBufferSize(0);

        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 100,
            viewportHeight: 40,
        });

        // colX[0] should be 0 (firstColumnPosition with no freeze + no scroll).
        expect(state.colX[0]).to.equal(0);
        // Each colWidth should be 50.
        expect(state.colWidths[0]).to.equal(50);
        // rowY[0] = 0; rowHeights[0] = 20 (15pt → 20px at 96 ppi).
        expect(state.rowY[0]).to.equal(0);
        expect(state.rowHeights[0]).to.equal(20);
        // colX[1] = 50 (cumulative).
        if (state.colX.length > 1) {
            expect(state.colX[1]).to.equal(50);
        }
    });

    it('buildRenderState passes through stored cellData', () => {
        const sheet = makeSheet();
        sheet.setRows(3);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15]);

        sheet.updateBottomRightCellValues([
            {
                col: 1,
                row: 1,
                value: 'A1',
                cellStyle: 'cs0',
                locked: false,
                needsMeasure: false,
                isPercentage: false,
            },
        ]);

        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 200,
            viewportHeight: 80,
        });

        expect(state.cellData.get('col1 row1')?.value).to.equal('A1');
    });

    it('applyRenderState pushes a fresh renderState to the cell-grid', () => {
        const sheet = makeSheet();
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        const before = sheet.getCellGrid().renderState;
        sheet.applyRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 100,
            viewportHeight: 40,
        });
        const after = sheet.getCellGrid().renderState;

        // A fresh object should have been assigned (not the empty default).
        expect(after).to.not.equal(before);
        expect(after.viewport.lastColumnIndex).to.be.greaterThan(0);
    });

    it('all four quadrant update methods populate cellData consistently', () => {
        // Each of the 4 update*CellValues methods should make its
        // payload reachable via buildRenderState. (Today they all
        // route through a single shared map; if a future slice
        // splits them by quadrant, this test still verifies the
        // pipeline works end-to-end for each.)
        const sheet = makeSheet();
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.updateTopLeftCellValues([
            {
                col: 1, row: 1, value: 'TL',
                cellStyle: 'cs0', locked: false,
                needsMeasure: false, isPercentage: false,
            },
        ]);
        sheet.updateTopRightCellValues([
            {
                col: 4, row: 1, value: 'TR',
                cellStyle: 'cs0', locked: false,
                needsMeasure: false, isPercentage: false,
            },
        ]);
        sheet.updateBottomLeftCellValues([
            {
                col: 1, row: 4, value: 'BL',
                cellStyle: 'cs0', locked: false,
                needsMeasure: false, isPercentage: false,
            },
        ]);
        sheet.updateBottomRightCellValues([
            {
                col: 4, row: 4, value: 'BR',
                cellStyle: 'cs0', locked: false,
                needsMeasure: false, isPercentage: false,
            },
        ]);

        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 250,
            viewportHeight: 100,
        });

        expect(state.cellData.get('col1 row1')?.value).to.equal('TL');
        expect(state.cellData.get('col4 row1')?.value).to.equal('TR');
        expect(state.cellData.get('col1 row4')?.value).to.equal('BL');
        expect(state.cellData.get('col4 row4')?.value).to.equal('BR');
    });

    it('buildRenderState honors a non-default ppi for row-height conversion', () => {
        // At ppi 192 (2x retina-like), the default 15-point row
        // height converts to 40 pixels instead of the 20 pixels at
        // 96 ppi. The connector passes opts.ppi all the way through
        // getDefaultRowHeightPx + convertPointsToPixel.
        const sheet = makeSheet();
        sheet.setRows(3);
        sheet.setCols(1);
        sheet.setColW([50]);
        sheet.setRowH([15, 15, 15]);

        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 50,
            viewportHeight: 200,
            ppi: 192,
        });

        // 15pt * 192/72 = 40px per row
        expect(state.rowHeights[0]).to.equal(40);
    });

    it('buildRenderState honors hiddenRowIndexes (height 0)', () => {
        const sheet = makeSheet();
        sheet.setRows(5);
        sheet.setCols(2);
        sheet.setColW([50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHiddenRowIndexes([2]); // row 2 hidden

        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 100,
            viewportHeight: 100,
        });

        // rowHeights array, indexed by (row - firstRowIndex):
        // row 1 → index 0 → 20px
        // row 2 → index 1 → 0 (hidden)
        // row 3 → index 2 → 20px
        expect(state.rowHeights[0]).to.equal(20);
        expect(state.rowHeights[1]).to.equal(0);
        if (state.rowHeights.length > 2) {
            expect(state.rowHeights[2]).to.equal(20);
        }
    });
});

describe('Spreadsheet — scroll-event wiring (slice 140)', () => {
    /**
     * Helper: attach the connector to a real DOM node so the scroll
     * container has a measurable {@code clientWidth} / {@code clientHeight}.
     * The host is appended to document.body and removed after each test.
     */
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
            cleanup: () => host.remove(),
        };
    }

    it('wraps the cell-grid in a scroll container with overflow:auto', () => {
        const { sheet, cleanup } = attachSheet(200, 100);

        const scroller = sheet.getScrollContainer();
        expect(scroller).to.be.instanceOf(HTMLElement);
        expect(scroller.className).to.equal('sheet-scroll');
        expect(scroller.style.overflow).to.equal('auto');
        // The cell-grid is the scroll container's only child.
        expect(scroller.children).to.have.length(1);
        expect(scroller.firstElementChild).to.equal(sheet.getCellGrid());

        cleanup();
    });

    it('buildRenderState computes totalGridWidthPx + totalGridHeightPx', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);   // 250 total
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]); // 20 × 10 = 200 total

        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 200,
            viewportHeight: 100,
        });

        expect(state.totalGridWidthPx).to.equal(250);
        expect(state.totalGridHeightPx).to.equal(200);

        cleanup();
    });

    it('applyRenderState sets the cell-grid inline size to total grid dimensions', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        sheet.applyRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 200,
            viewportHeight: 100,
        });

        const cellGrid = sheet.getCellGrid();
        expect(cellGrid.style.width).to.equal('250px');
        expect(cellGrid.style.height).to.equal('200px');

        cleanup();
    });

    it('scrolling the scroll container triggers a fresh render state', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(50);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50)); // 1000 total
        sheet.setRowH(new Array(50).fill(15)); // 20 × 50 = 1000 total
        sheet.setRowBufferSize(0);
        sheet.setColumnBufferSize(0);

        // Establish initial state + cell-grid intrinsic size.
        sheet.applyRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 200,
            viewportHeight: 100,
        });
        const before = sheet.getCellGrid().renderState;

        // Scroll horizontally + synchronously dispatch the scroll event
        // (instead of waiting on a real rAF, which can be delayed under
        // parallel wtr runs and time out the test).
        const scroller = sheet.getScrollContainer();
        scroller.scrollLeft = 100;
        scroller.dispatchEvent(new Event('scroll'));

        const after = sheet.getCellGrid().renderState;
        // The render state should reflect the new scroll position —
        // firstColumnIndex shifts past col 1 (since 100/50 = 2 cols scrolled).
        expect(after).to.not.equal(before);
        expect(after.viewport.firstColumnIndex).to.be.greaterThan(1);

        cleanup();
    });

    it('scroll listener reads scrollTop + recomputes the viewport vertically', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(20);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        sheet.setRowBufferSize(0);
        sheet.setColumnBufferSize(0);

        // Establish cell-grid intrinsic size so scrolls don't clamp.
        sheet.applyRenderState({
            scrollLeft: 0, scrollTop: 0,
            viewportWidth: 200, viewportHeight: 100,
        });

        const scroller = sheet.getScrollContainer();
        // scrollTop = 200 → 10 rows scrolled (each 20px) → row 11 first.
        scroller.scrollTop = 200;
        scroller.dispatchEvent(new Event('scroll'));

        const state = sheet.getCellGrid().renderState;
        // 200 / 20 = 10 rows scrolled → firstRowIndex = 11 (no buffer).
        expect(state.viewport.firstRowIndex).to.be.greaterThan(1);

        cleanup();
    });

    it('emptyCellGridRenderState defaults total grid dimensions to 0', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        // No rows/cols set — buildRenderState should produce 0×0 totals.
        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 0,
            viewportWidth: 200,
            viewportHeight: 100,
        });

        expect(state.totalGridWidthPx).to.equal(0);
        expect(state.totalGridHeightPx).to.equal(0);

        cleanup();
    });

    it('totalGridHeightPx respects the ppi used for row-height conversion', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(1);
        sheet.setColW([50]);
        sheet.setRowH([15, 15, 15, 15, 15]); // 15 pt each

        // At 96 ppi: 15 pt → 20 px, total = 100.
        const state96 = sheet.buildRenderState({
            scrollLeft: 0, scrollTop: 0, viewportWidth: 50, viewportHeight: 200,
        });
        expect(state96.totalGridHeightPx).to.equal(100);

        // At 192 ppi: 15 pt → 40 px, total = 200.
        const state192 = sheet.buildRenderState({
            scrollLeft: 0, scrollTop: 0, viewportWidth: 50, viewportHeight: 200,
            ppi: 192,
        });
        expect(state192.totalGridHeightPx).to.equal(200);

        cleanup();
    });

    it('hidden rows + columns reduce totalGrid dimensions', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(3);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15]);
        sheet.setHiddenRowIndexes([2]);
        sheet.setHiddenColumnIndexes([2]);

        const state = sheet.buildRenderState({
            scrollLeft: 0, scrollTop: 0, viewportWidth: 200, viewportHeight: 100,
        });
        // Cols: 50 + 0 + 50 = 100. Rows: 20 + 0 + 20 = 40.
        expect(state.totalGridWidthPx).to.equal(100);
        expect(state.totalGridHeightPx).to.equal(40);

        cleanup();
    });
});
