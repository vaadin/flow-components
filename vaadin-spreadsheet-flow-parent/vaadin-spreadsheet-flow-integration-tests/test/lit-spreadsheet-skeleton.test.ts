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

    it('appends the cell-grid + headers + corner + sheet tabs + formula bar to the host element', () => {
        // Slice 140: scroll container wraps the cell-grid. Slice 143
        // added the column-header sibling. Slice 144 added the
        // row-header + restructured the scroll container as a CSS
        // grid. Slice 145 added the corner box at (col 1, row 1).
        // Slice 154 added the sheet-tabs strip below the scroll
        // container; the host is now a flex column.
        // Slice 175 added the formula bar above the scroll container.
        // Slice 191 added the context menu, which stays empty until the
        // server answers a right-click with the actions it offers.
        const host = document.createElement('div');
        const sheet = new Spreadsheet(host);

        // Host has 4 children: formula bar + context menu + scroll
        // container + sheet tabs.
        expect(host.children).to.have.length(4);
        const hostKids = Array.from(host.children);
        expect(hostKids.includes(sheet.getFormulaBar())).to.be.true;
        expect(hostKids.includes(sheet.getContextMenu())).to.be.true;
        expect(hostKids.includes(sheet.getScrollContainer())).to.be.true;
        expect(hostKids.includes(sheet.getSheetTabs())).to.be.true;

        const scroller = sheet.getScrollContainer();
        // Four scroll-container children: header-corner, column-header,
        // row-header, cell-grid. (Identity checks via `===` to avoid
        // chai serializing LitElements with cyclic renderState.)
        expect(scroller.children).to.have.length(4);
        const kids = Array.from(scroller.children);
        expect(kids.includes(sheet.getColumnHeader())).to.be.true;
        expect(kids.includes(sheet.getRowHeader())).to.be.true;
        expect(kids.includes(sheet.getCellGrid())).to.be.true;
        expect(kids.includes(sheet.getHeaderCorner())).to.be.true;
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
            // Dispose the sheet on cleanup so the ResizeObserver
            // (slice 142) + scroll listener (slice 140) don't leak
            // across tests. Without this, many test files in the
            // same wtr session accumulate dangling observers and
            // exceed the test-finish timeout.
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    it('wraps the cell-grid + headers + corner in a CSS-grid scroll container with overflow:auto', () => {
        const { sheet, cleanup } = attachSheet(200, 100);

        const scroller = sheet.getScrollContainer();
        expect(scroller).to.be.instanceOf(HTMLElement);
        expect(scroller.className).to.equal('sheet-scroll');
        expect(scroller.style.overflow).to.equal('auto');
        const computed = getComputedStyle(scroller);
        expect(computed.display).to.equal('grid');
        // Four children: header-corner + column-header + row-header
        // + cell-grid (CSS-grid layout, slice 144+145).
        expect(scroller.children).to.have.length(4);

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

    it('buildRenderState carries scrollLeft + scrollTop into the render state (slice 141)', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(20);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(20).fill(15));

        const state = sheet.buildRenderState({
            scrollLeft: 175,
            scrollTop: 80,
            viewportWidth: 200,
            viewportHeight: 100,
        });

        // The state mirrors the scroll inputs so the cell-grid can
        // emit transform wrappers for frozen quadrants.
        expect(state.scrollLeft).to.equal(175);
        expect(state.scrollTop).to.equal(80);

        cleanup();
    });
});

describe('Spreadsheet — recomputeViewport + ResizeObserver lifecycle (slice 142)', () => {
    function attachSheetForLifecycle(width: number, height: number): {
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

    it('recomputeViewport is callable from outside the connector', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        // Direct call — no scroll event, no observer fire. The
        // wrapper element will call this from its updated() cycle
        // after state changes.
        sheet.recomputeViewport();

        const state = sheet.getCellGrid().renderState;
        // Render state should reflect the connector's current data —
        // 5 columns, total width 250.
        expect(state.totalGridWidthPx).to.equal(250);
        expect(state.viewport.lastColumnIndex).to.be.greaterThanOrEqual(1);

        cleanup();
    });

    it('recomputeViewport reads the scroll container clientWidth + clientHeight', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(180, 60);
        sheet.setRows(20);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        sheet.setRowBufferSize(0);
        sheet.setColumnBufferSize(0);

        sheet.recomputeViewport();

        const state = sheet.getCellGrid().renderState;
        // Viewport width 180 → at most 4 columns (180/50 = 3.6 → 4
        // covers the viewport edge).
        expect(state.viewport.lastColumnIndex).to.be.greaterThanOrEqual(3);
        expect(state.viewport.lastColumnIndex).to.be.lessThanOrEqual(5);

        cleanup();
    });

    it('the constructor observes the scroll container with a ResizeObserver', () => {
        // The connector should set up a ResizeObserver on the scroll
        // container so the first render happens automatically when
        // the host gets its dimensions. We verify the wiring
        // indirectly: constructing a sheet must NOT throw, and the
        // ResizeObserver global must have been called (it would be
        // missing in a non-DOM env where the constructor would fail
        // earlier).
        expect(typeof ResizeObserver).to.equal('function');

        const host = document.createElement('div');
        host.style.cssText = 'width:200px;height:100px;';
        document.body.appendChild(host);
        // Construction completes without throwing — the
        // ResizeObserver was created + observe() succeeded.
        const sheet = new Spreadsheet(host);
        expect(sheet.getScrollContainer().isConnected).to.be.true;
        sheet.dispose();
        host.remove();
    });

    it('dispose removes scroll listener + disconnects resize observer', () => {
        const { host, sheet } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.recomputeViewport();

        const scroller = sheet.getScrollContainer();

        // After dispose, scrolling should no longer trigger a fresh
        // recompute (the listener is removed).
        sheet.dispose();
        const before = sheet.getCellGrid().renderState;
        scroller.scrollLeft = 100;
        scroller.dispatchEvent(new Event('scroll'));
        const after = sheet.getCellGrid().renderState;
        expect(after).to.equal(before);

        host.remove();
    });

    it('header corner box sits in CSS-grid (col 1, row 1) with sticky positioning (slice 145)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        const corner = sheet.getHeaderCorner();
        expect(corner).to.be.instanceOf(HTMLElement);
        expect(corner.className).to.equal('sheet-header-corner');
        expect(corner.style.gridColumn).to.equal('1');
        expect(corner.style.gridRow).to.equal('1');
        const computed = getComputedStyle(corner);
        expect(computed.position).to.equal('sticky');
        cleanup();
    });

    it('header corner sizes to (headerColumnWidth, headerRowHeight) when BOTH are set (slice 145)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(24);
        sheet.setHeaderColumnWidth(40);

        sheet.recomputeViewport();

        const corner = sheet.getHeaderCorner();
        expect(corner.style.width).to.equal('40px');
        expect(corner.style.height).to.equal('24px');

        cleanup();
    });

    it('header corner collapses to 0x0 when EITHER header dimension is 0 (slice 145)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Only column header set, row header at 0 → corner = 0x0.
        sheet.setHeaderRowHeight(24);
        sheet.setHeaderColumnWidth(0);
        sheet.recomputeViewport();
        expect(sheet.getHeaderCorner().style.width).to.equal('0px');
        expect(sheet.getHeaderCorner().style.height).to.equal('0px');

        // Only row header set → still 0x0.
        sheet.setHeaderRowHeight(0);
        sheet.setHeaderColumnWidth(40);
        sheet.recomputeViewport();
        expect(sheet.getHeaderCorner().style.width).to.equal('0px');
        expect(sheet.getHeaderCorner().style.height).to.equal('0px');

        // Both set → real dimensions.
        sheet.setHeaderRowHeight(24);
        sheet.setHeaderColumnWidth(40);
        sheet.recomputeViewport();
        expect(sheet.getHeaderCorner().style.width).to.equal('40px');
        expect(sheet.getHeaderCorner().style.height).to.equal('24px');

        cleanup();
    });

    it('setHeaderColumnWidth + applyRenderState push state to the row-header sub-element (slice 144)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 18, 15, 12, 15]); // varying row heights

        // Slice 180: default header column width is 40.
        expect(sheet.getHeaderColumnWidth()).to.equal(40);

        sheet.setHeaderColumnWidth(40);
        expect(sheet.getHeaderColumnWidth()).to.equal(40);

        sheet.recomputeViewport();

        const rowState = sheet.getRowHeader().renderState;
        expect(rowState.headerWidth).to.equal(40);
        // rowHeights are in points → pixels at 96 ppi (15pt→20, 18pt→24,
        // 12pt→16). Use Array.from to avoid chai serializing the
        // ReadonlyArray + LitElement cycle on assertion failure.
        expect(Array.from(rowState.rowHeights)).to.deep.equal([
            20, 24, 20, 16, 20,
        ]);
        // Total grid height matches what the cell-grid uses.
        expect(rowState.totalGridHeightPx).to.equal(20 + 24 + 20 + 16 + 20);

        // The row header's inline width matches.
        expect(sheet.getRowHeader().style.width).to.equal('40px');

        cleanup();
    });

    it('setHeaderRowHeight + applyRenderState push header state to the column-header sub-element (slice 143)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 60, 40]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Slice 180: default header row height is 20 (was 0 before
        // slice 180 — display-row-col-headings defaults to true, so
        // the headers need a non-zero size to actually render).
        expect(sheet.getHeaderRowHeight()).to.equal(20);

        sheet.setHeaderRowHeight(24);
        expect(sheet.getHeaderRowHeight()).to.equal(24);

        sheet.recomputeViewport();

        const headerState = sheet.getColumnHeader().renderState;
        expect(headerState.headerHeight).to.equal(24);
        // Use plain-array comparison, not chai .deep.equal, to avoid
        // chai walking into the LitElement's cyclic renderState on
        // assertion failure.
        expect(Array.from(headerState.colWidths)).to.deep.equal([50, 60, 40]);
        expect(headerState.colX[0]).to.equal(0);
        // Total grid width matches the cell-grid's total: 50+60+40=150.
        expect(headerState.totalGridWidthPx).to.equal(150);

        // The column header's inline height matches.
        expect(sheet.getColumnHeader().style.height).to.equal('24px');

        cleanup();
    });

    it('cell-click event from the cell-grid updates the active cell selection (slice 146)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Default selection: A1.
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 1 });

        // Dispatch a cell-click event manually (simulates what the
        // shadow-root handler does after a real click).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 2 });
        // Range collapses to the active cell on plain click.
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 3, c2: 3, r1: 2, r2: 2,
        });

        cleanup();
    });

    it('shift-click extends the active range from the previous active cell (slice 146)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // First click sets the active cell to (2, 2).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });

        // Shift-click on (4, 4) extends the range from the anchor.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 4, row: 4, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        // Active cell stays at the anchor (Excel convention).
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
        // Range covers (2,2) to (4,4).
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 2, c2: 4, r1: 2, r2: 4,
        });

        cleanup();
    });

    it('cell-click triggers a re-render with the new selection (slice 146)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();

        const beforeState = sheet.getCellGrid().renderState;
        expect(beforeState.selection.activeCell).to.deep.equal({ col: 1, row: 1 });

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 4, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        const afterState = sheet.getCellGrid().renderState;
        // Fresh state object pushed (not the same reference).
        expect(afterState === beforeState).to.be.false;
        expect(afterState.selection.activeCell).to.deep.equal({ col: 4, row: 3 });

        cleanup();
    });

    function dispatchKey(
        target: HTMLElement,
        key: string,
        opts: { shiftKey?: boolean } = {}
    ): void {
        target.dispatchEvent(new KeyboardEvent('keydown', {
            key,
            shiftKey: opts.shiftKey ?? false,
            bubbles: true,
            cancelable: true,
        }));
    }

    it('scroll container is focusable via tabindex=0 (slice 147)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        expect(sheet.getScrollContainer().tabIndex).to.equal(0);
        cleanup();
    });

    it('ArrowRight moves the active cell one column right (slice 147)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Default selection: A1.
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 1 });

        dispatchKey(sheet.getScrollContainer(), 'ArrowRight');

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 1 });
        // Range collapses to the new active cell.
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 2, c2: 2, r1: 1, r2: 1,
        });
        cleanup();
    });

    it('ArrowDown / ArrowLeft / ArrowUp all move the active cell one cell (slice 147)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        const scroller = sheet.getScrollContainer();
        // Start at (3, 3) via a cell-click.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        dispatchKey(scroller, 'ArrowDown');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 4 });
        dispatchKey(scroller, 'ArrowLeft');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 4 });
        dispatchKey(scroller, 'ArrowUp');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 3 });

        cleanup();
    });

    it('Arrow keys clamp at the grid edges (slice 147)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(3);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15]);

        const scroller = sheet.getScrollContainer();
        // At A1 — ArrowUp should stay at A1, ArrowLeft should stay at A1.
        dispatchKey(scroller, 'ArrowUp');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 1 });
        dispatchKey(scroller, 'ArrowLeft');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 1 });

        // Move to (3, 3) — ArrowDown stays at row 3 (last), ArrowRight stays at col 3 (last).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        dispatchKey(scroller, 'ArrowDown');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 3 });
        dispatchKey(scroller, 'ArrowRight');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 3 });

        cleanup();
    });

    it('Shift+ArrowDown extends the range without moving the active cell (slice 147)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        // Move to (2, 2).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        const scroller = sheet.getScrollContainer();
        // Shift+Down twice → range = (2,2)-(2,4); active cell stays at (2,2).
        dispatchKey(scroller, 'ArrowDown', { shiftKey: true });
        dispatchKey(scroller, 'ArrowDown', { shiftKey: true });

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 2, c2: 2, r1: 2, r2: 4,
        });

        cleanup();
    });

    it('Shift+arrow contracts the range when extending back toward the anchor (slice 147)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        // Anchor at (2,2), extend to (2,5).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        const scroller = sheet.getScrollContainer();
        for (let i = 0; i < 3; i++) {
            dispatchKey(scroller, 'ArrowDown', { shiftKey: true });
        }
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 2, c2: 2, r1: 2, r2: 5,
        });

        // Shift+Up should now contract to (2,2)-(2,4).
        dispatchKey(scroller, 'ArrowUp', { shiftKey: true });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 2, c2: 2, r1: 2, r2: 4,
        });

        cleanup();
    });

    it('plain arrow after a shift-extended range resets the extend position (slice 147)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        // Build a range (2,2)-(2,5).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        const scroller = sheet.getScrollContainer();
        for (let i = 0; i < 3; i++) {
            dispatchKey(scroller, 'ArrowDown', { shiftKey: true });
        }

        // Plain ArrowRight → active cell moves to (3, 2), range collapses.
        dispatchKey(scroller, 'ArrowRight');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 2 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 3, c2: 3, r1: 2, r2: 2,
        });

        cleanup();
    });

    it('keyboard navigation is a no-op when rows or cols is 0 (slice 147)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        // No setRows/setCols — rows=cols=0.
        const before = sheet.getSelection();

        dispatchKey(sheet.getScrollContainer(), 'ArrowDown');

        // Selection unchanged.
        expect(sheet.getSelection() === before).to.be.true;

        cleanup();
    });

    it('column-header-click selects the entire column (slice 148)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-header-click', {
            detail: { col: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        // Active cell at the top of the column.
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 1 });
        // Range covers col 3, rows 1..10.
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 3, c2: 3, r1: 1, r2: 10,
        });

        cleanup();
    });

    it('shift-click on column header extends from anchor column to clicked column (slice 148)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        // Click col 2 first to set anchor at (2, 1) via whole-column.
        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-header-click', {
            detail: { col: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        // Shift-click col 4 → range covers cols 2..4, rows 1..10.
        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-header-click', {
            detail: { col: 4, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        // Active cell stays at the anchor.
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 1 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 2, c2: 4, r1: 1, r2: 10,
        });

        cleanup();
    });

    it('row-header-click selects the entire row (slice 148)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        sheet.getRowHeader().dispatchEvent(new CustomEvent('row-header-click', {
            detail: { row: 4, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 4 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 1, c2: 5, r1: 4, r2: 4,
        });

        cleanup();
    });

    it('shift-click on row header extends from anchor row to clicked row (slice 148)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);

        // Click row 2 first.
        sheet.getRowHeader().dispatchEvent(new CustomEvent('row-header-click', {
            detail: { row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        // Shift-click row 5.
        sheet.getRowHeader().dispatchEvent(new CustomEvent('row-header-click', {
            detail: { row: 5, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 2 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 1, c2: 5, r1: 2, r2: 5,
        });

        cleanup();
    });

    it('Tab moves active cell right + Shift+Tab moves left (slice 149)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        // Move to (3, 3) via cell-click.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        const scroller = sheet.getScrollContainer();
        dispatchKey(scroller, 'Tab');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 4, row: 3 });
        dispatchKey(scroller, 'Tab', { shiftKey: true });
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 3 });
        cleanup();
    });

    it('Enter moves active cell down + Shift+Enter moves up (slice 149)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        const scroller = sheet.getScrollContainer();
        dispatchKey(scroller, 'Enter');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 4 });
        dispatchKey(scroller, 'Enter', { shiftKey: true });
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 3 });
        cleanup();
    });

    it('Home / End jump to first / last column in current row (slice 149)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        const scroller = sheet.getScrollContainer();
        dispatchKey(scroller, 'Home');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 3 });
        dispatchKey(scroller, 'End');
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 5, row: 3 });
        cleanup();
    });

    it('Ctrl+Home and Ctrl+End jump to A1 / last cell (slice 149)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        const scroller = sheet.getScrollContainer();
        scroller.dispatchEvent(new KeyboardEvent('keydown', {
            key: 'End',
            ctrlKey: true,
            bubbles: true,
            cancelable: true,
        }));
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 5, row: 5 });
        scroller.dispatchEvent(new KeyboardEvent('keydown', {
            key: 'Home',
            ctrlKey: true,
            bubbles: true,
            cancelable: true,
        }));
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 1 });
        cleanup();
    });

    it('cell-click dispatches spreadsheet-event with cellSelected (slice 150)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 4, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        expect(received).to.exist;
        expect(received!.type).to.equal('cellSelected');
        expect(received!.data).to.deep.equal({
            row: 3,
            column: 4,
            oldSelectionRangeDiscarded: true,
        });

        cleanup();
    });

    it('shift-click dispatches cellRangeSelected with the new range (slice 150)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        // Set anchor to (2,2) first.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        const received: { type: string; data: Record<string, unknown> }[] = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            received.push((e as CustomEvent).detail);
        });

        // Shift-click to extend.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 5, row: 4, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        // The anchor goes first: onCellRangeSelected reads the active cell
        // from the selection the server already holds, so it has to be told
        // about it before the range arrives.
        expect(received).to.have.length(2);
        expect(received[0].type).to.equal('cellSelected');
        expect(received[0].data).to.deep.equal({
            row: 2, column: 2, oldSelectionRangeDiscarded: true,
        });
        expect(received[1].type).to.equal('cellRangeSelected');
        expect(received[1].data).to.deep.equal({
            row1: 2, col1: 2, row2: 4, col2: 5,
        });
        cleanup();
    });

    it('column-header click dispatches columnSelected (slice 150)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-header-click', {
            detail: { col: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        expect(received!.type).to.equal('columnSelected');
        expect(received!.data).to.deep.equal({
            column: 3,
            firstRowIndex: 1,
        });
        cleanup();
    });

    it('row-header click dispatches rowSelected (slice 150)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        sheet.getRowHeader().dispatchEvent(new CustomEvent('row-header-click', {
            detail: { row: 4, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        expect(received!.type).to.equal('rowSelected');
        expect(received!.data).to.deep.equal({
            row: 4,
            firstColumnIndex: 1,
        });
        cleanup();
    });

    it('keyboard arrow dispatches cellSelected (slice 150)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        dispatchKey(sheet.getScrollContainer(), 'ArrowRight');

        expect(received!.type).to.equal('cellSelected');
        expect(received!.data).to.deep.equal({
            row: 1,
            column: 2,
            oldSelectionRangeDiscarded: true,
        });
        cleanup();
    });

    it('shift+arrow dispatches cellRangeSelected (slice 150)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        // Move active to (3, 3) first via plain ArrowRight x 2 + Down x 2.
        const scroller = sheet.getScrollContainer();
        dispatchKey(scroller, 'ArrowRight');
        dispatchKey(scroller, 'ArrowRight');
        dispatchKey(scroller, 'ArrowDown');
        dispatchKey(scroller, 'ArrowDown');

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            received = (e as CustomEvent).detail;
        });

        // Now shift+ArrowDown — should extend to a 1×2 range and dispatch range.
        dispatchKey(scroller, 'ArrowDown', { shiftKey: true });

        expect(received!.type).to.equal('cellRangeSelected');
        expect(received!.data).to.deep.equal({
            row1: 3, col1: 3, row2: 4, col2: 3,
        });
        cleanup();
    });

    it('Delete key dispatches deleteSelectedCells spreadsheet-event (slice 151)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'deleteSelectedCells') {
                received = detail;
            }
        });

        dispatchKey(sheet.getScrollContainer(), 'Delete');

        expect(received).to.exist;
        expect(received!.type).to.equal('deleteSelectedCells');
        expect(received!.data).to.deep.equal({});
        cleanup();
    });

    it('Backspace also dispatches deleteSelectedCells (slice 151)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        let received: { type: string } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'deleteSelectedCells') {
                received = detail;
            }
        });

        dispatchKey(sheet.getScrollContainer(), 'Backspace');

        expect(received).to.exist;
        cleanup();
    });

    it('Delete does NOT dispatch deleteSelectedCells on an empty grid (slice 151)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        // No setRows/setCols.

        let received = false;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'deleteSelectedCells') {
                received = true;
            }
        });

        dispatchKey(sheet.getScrollContainer(), 'Delete');

        expect(received).to.be.false;
        cleanup();
    });

    it('Delete does NOT modify the client-side selection (slice 151)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        // Set selection to (3, 3) and a range to (3,3)-(5,5).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 5, row: 5, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        const beforeRange = sheet.getSelection().activeRange;

        dispatchKey(sheet.getScrollContainer(), 'Delete');

        // Selection unchanged — server's response (when integrated)
        // will push back updateCellValues for the cleared cells.
        expect(sheet.getSelection().activeRange).to.deep.equal(beforeRange);
        cleanup();
    });

    it('mousedown on a cell starts a drag and selects the anchor cell (slice 155)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mousedown', {
            detail: { col: 4, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 4, row: 3 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 4, c2: 4, r1: 3, r2: 3,
        });
        cleanup();
    });

    it('mouseover during drag extends the range from anchor to hover cell (slice 155)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        // Start drag at (2, 2).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mousedown', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        // Drag over (5, 4).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mouseover', {
            detail: { col: 5, row: 4 },
            bubbles: true,
            composed: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 2, c2: 5, r1: 2, r2: 4,
        });
        cleanup();
    });

    it('mouseover when NOT dragging does not change selection (slice 155)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        const before = sheet.getSelection();
        // Mouseover without mousedown — just hovering.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mouseover', {
            detail: { col: 5, row: 5 },
            bubbles: true,
            composed: true,
        }));
        expect(sheet.getSelection() === before).to.be.true;
        cleanup();
    });

    it('mouseup ends the drag and dispatches cellRangeSelected for multi-cell ranges (slice 155)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        // Drag from (2, 2) to (5, 4).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mousedown', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mouseover', {
            detail: { col: 5, row: 4 },
            bubbles: true,
            composed: true,
        }));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'cellRangeSelected') {
                received = detail;
            }
        });

        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        expect(received).to.exist;
        expect(received!.data).to.deep.equal({
            row1: 2, col1: 2, row2: 4, col2: 5,
        });
        cleanup();
    });

    it('mouseup after a single-cell drag does NOT dispatch a duplicate cellSelected (slice 155)', () => {
        // mousedown + mouseup on the same cell = a click. The
        // existing cell-click handler dispatches `cellSelected`;
        // mouseup should NOT additionally dispatch since the
        // range is single-cell.
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mousedown', {
            detail: { col: 3, row: 3, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        let received = 0;
        host.addEventListener('spreadsheet-event', () => { received++; });

        // No mouseover → range stays single cell. mouseup should not dispatch.
        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        expect(received).to.equal(0);
        cleanup();
    });

    it('mouseup is a no-op when no drag was in progress (slice 155)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);

        let received = false;
        host.addEventListener('spreadsheet-event', () => { received = true; });

        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        expect(received).to.be.false;
        cleanup();
    });

    it('shift+mousedown extends range from existing anchor (slice 155)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        // Initial selection at (2, 2) via plain mousedown.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mousedown', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));
        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        // Shift+mousedown at (5, 5) extends from anchor.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mousedown', {
            detail: { col: 5, row: 5, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true,
            composed: true,
        }));

        // Anchor stays at (2, 2), range covers (2,2)-(5,5).
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 2, c2: 5, r1: 2, r2: 5,
        });
        cleanup();
    });

    it('keyboard nav past the right edge scrolls the active cell into view (slice 156)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(100, 100);
        sheet.setRows(20);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        // Viewport width is 100 → 2 cols visible. Active starts at A1.
        // ArrowRight 3 times → active = col 4. col 4's right edge =
        // 4*50 = 200 > 100; expect scrollLeft to advance.

        const scroller = sheet.getScrollContainer();
        expect(scroller.scrollLeft).to.equal(0);

        for (let i = 0; i < 3; i++) {
            dispatchKey(scroller, 'ArrowRight');
        }
        // After 3 right-arrows: active = col 4. Cell at left=150,
        // width=50, right=200. To make it visible in a 100-wide
        // viewport: scrollLeft = max(0, 200 - 100) = 100.
        expect(scroller.scrollLeft).to.be.greaterThan(0);
        cleanup();
    });

    it('keyboard nav past the bottom edge scrolls the active cell into view (slice 156)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 60);
        sheet.setRows(20);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        // Viewport height = 60 → 3 rows visible (each 20px). Move
        // active to row 5: top=80, bottom=100, needs scrollTop=40.
        const scroller = sheet.getScrollContainer();
        for (let i = 0; i < 4; i++) {
            dispatchKey(scroller, 'ArrowDown');
        }
        expect(scroller.scrollTop).to.be.greaterThan(0);
        cleanup();
    });

    it('keyboard nav within visible viewport does NOT scroll (slice 156)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(20);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        // 400px / 50 = 8 cols. ArrowRight 3 times stays within.
        const scroller = sheet.getScrollContainer();
        for (let i = 0; i < 3; i++) {
            dispatchKey(scroller, 'ArrowRight');
        }
        expect(scroller.scrollLeft).to.equal(0);
        expect(scroller.scrollTop).to.equal(0);
        cleanup();
    });

    it('ArrowLeft past the left edge scrolls back to bring active into view (slice 156)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(100, 100);
        sheet.setRows(20);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(20).fill(15));

        // Set up a scrolled-right state via several ArrowRight presses.
        const scroller = sheet.getScrollContainer();
        for (let i = 0; i < 8; i++) {
            dispatchKey(scroller, 'ArrowRight');
        }
        expect(scroller.scrollLeft).to.be.greaterThan(0);

        // Now move active back to col 1 via Home — scrollLeft should
        // drop back so col 1 is visible.
        dispatchKey(scroller, 'Home');
        expect(scroller.scrollLeft).to.equal(0);
        cleanup();
    });

    it('scrolling dispatches onSheetScroll with the new viewport bounds (slice 157)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(50);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50)); // 1000 total
        sheet.setRowH(new Array(50).fill(15)); // 20*50 = 1000 total
        sheet.setRowBufferSize(0);
        sheet.setColumnBufferSize(0);
        sheet.applyRenderState({
            scrollLeft: 0, scrollTop: 0,
            viewportWidth: 200, viewportHeight: 100,
        });

        let received: { type: string; data: Record<string, number> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'onSheetScroll') {
                received = detail;
            }
        });

        const scroller = sheet.getScrollContainer();
        scroller.scrollLeft = 100;
        scroller.dispatchEvent(new Event('scroll'));

        expect(received).to.exist;
        expect(received!.type).to.equal('onSheetScroll');
        // After scrolling 100px right (col width 50), firstColumnIndex
        // should be at least 3.
        expect(received!.data.firstColumn).to.be.greaterThanOrEqual(3);
        // The dispatched data has firstRow/firstColumn/lastRow/lastColumn.
        expect(received!.data).to.have.all.keys(
            'firstRow', 'firstColumn', 'lastRow', 'lastColumn'
        );
        cleanup();
    });

    it('vertical scroll dispatches onSheetScroll with updated row range (slice 157)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(50);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(50).fill(15));
        sheet.setRowBufferSize(0);
        sheet.setColumnBufferSize(0);
        sheet.applyRenderState({
            scrollLeft: 0, scrollTop: 0,
            viewportWidth: 200, viewportHeight: 100,
        });

        let received: { data: Record<string, number> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'onSheetScroll') {
                received = detail;
            }
        });

        const scroller = sheet.getScrollContainer();
        scroller.scrollTop = 200; // 10 rows down (20px each)
        scroller.dispatchEvent(new Event('scroll'));

        expect(received).to.exist;
        expect(received!.data.firstRow).to.be.greaterThanOrEqual(11);
        cleanup();
    });

    it('Ctrl+A selects the entire sheet (slice 159)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(20);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(20).fill(15));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'cellRangeSelected') {
                received = detail;
            }
        });

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'a',
            ctrlKey: true,
            bubbles: true,
            cancelable: true,
        }));

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 1 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 1, c2: 10, r1: 1, r2: 20,
        });
        expect(received!.data).to.deep.equal({
            row1: 1, col1: 1, row2: 20, col2: 10,
        });
        cleanup();
    });

    it('Cmd+A (Mac convention) also selects the entire sheet (slice 159)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'a',
            metaKey: true,
            bubbles: true,
            cancelable: true,
        }));

        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 1, c2: 5, r1: 1, r2: 5,
        });
        cleanup();
    });

    it('Ctrl+Z dispatches onUndo (slice 159)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);

        let received: { type: string } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'onUndo') {
                received = detail;
            }
        });

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'z',
            ctrlKey: true,
            bubbles: true,
            cancelable: true,
        }));

        expect(received).to.exist;
        cleanup();
    });

    it('Ctrl+Y dispatches onRedo (slice 159)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);

        let received: { type: string } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'onRedo') {
                received = detail;
            }
        });

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'y',
            ctrlKey: true,
            bubbles: true,
            cancelable: true,
        }));

        expect(received).to.exist;
        cleanup();
    });

    it('Cmd+Shift+Z (Mac convention) also dispatches onRedo (slice 159)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);

        let received: { type: string } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'onRedo') {
                received = detail;
            }
        });

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'z',
            metaKey: true,
            shiftKey: true,
            bubbles: true,
            cancelable: true,
        }));

        expect(received).to.exist;
        cleanup();
    });

    it('Ctrl+Shift+Z dispatches onRedo (Windows convention) (slice 159)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);

        let received = false;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            if ((e as CustomEvent).detail.type === 'onRedo') {
                received = true;
            }
        });

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'z',
            ctrlKey: true,
            shiftKey: true,
            bubbles: true,
            cancelable: true,
        }));

        expect(received).to.be.true;
        cleanup();
    });

    it('plain Ctrl+Z without shift does NOT dispatch onRedo (slice 159)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);

        let undoReceived = false;
        let redoReceived = false;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'onUndo') { undoReceived = true; }
            if (detail.type === 'onRedo') { redoReceived = true; }
        });

        sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
            key: 'z',
            ctrlKey: true,
            bubbles: true,
            cancelable: true,
        }));

        expect(undoReceived).to.be.true;
        expect(redoReceived).to.be.false;
        cleanup();
    });

    it('constructor accepts an optional renderTarget arg for shadow-DOM hosts (slice 162)', () => {
        // The Lit wrapper passes (element, element.renderRoot) so
        // DOM goes into the shadow root, not the light DOM. Verify
        // the second arg controls where children are appended.
        const host = document.createElement('div');
        const shadow = host.attachShadow({ mode: 'open' });
        document.body.appendChild(host);

        const sheet = new Spreadsheet(host, shadow);

        // Scroll container + formula bar + context menu + sheet tabs go
        // inside the shadow root, not the light DOM.
        expect(shadow.children.length).to.equal(4);
        expect(host.children.length).to.equal(0);
        // Host is still the event target / style host.
        expect(host.style.display).to.equal('flex');

        sheet.dispose();
        host.remove();
    });

    it('resize() + relayout() call recomputeViewport (slice 162)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        const before = sheet.getCellGrid().renderState;
        sheet.resize();
        const afterResize = sheet.getCellGrid().renderState;
        expect(afterResize === before).to.be.false;

        sheet.relayout();
        const afterRelayout = sheet.getCellGrid().renderState;
        expect(afterRelayout === afterResize).to.be.false;

        cleanup();
    });

    it('copy writes selected cells as TSV to the clipboard (slice 163)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.updateBottomRightCellValues([
            { col: 1, row: 1, value: 'A1', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
            { col: 2, row: 1, value: 'B1', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
            { col: 1, row: 2, value: 'A2', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
            { col: 2, row: 2, value: 'B2', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
        ]);

        // Select (1,1) to (2,2).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 1, row: 1, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));

        // Dispatch synthetic copy event with a ClipboardEvent.
        const clipboard = new DataTransfer();
        const ev = new ClipboardEvent('copy', {
            clipboardData: clipboard,
            bubbles: true,
            cancelable: true,
        });
        sheet.getScrollContainer().dispatchEvent(ev);

        // TSV should be in the clipboard.
        const text = clipboard.getData('text/plain');
        expect(text).to.equal('A1\tB1\nA2\tB2');
        cleanup();
    });

    it('cut dispatches clearSelectedCellsOnCut AND writes to clipboard (slice 163)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.updateBottomRightCellValues([
            { col: 1, row: 1, value: 'x', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
        ]);

        let received = false;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            if ((e as CustomEvent).detail.type === 'clearSelectedCellsOnCut') {
                received = true;
            }
        });

        const clipboard = new DataTransfer();
        sheet.getScrollContainer().dispatchEvent(new ClipboardEvent('cut', {
            clipboardData: clipboard,
            bubbles: true,
            cancelable: true,
        }));

        expect(received).to.be.true;
        expect(clipboard.getData('text/plain')).to.equal('x');
        cleanup();
    });

    it('paste dispatches onPaste with the clipboard text (slice 163)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'onPaste') {
                received = detail;
            }
        });

        const clipboard = new DataTransfer();
        clipboard.setData('text/plain', 'pasted\tdata\nrow2\tval2');
        sheet.getScrollContainer().dispatchEvent(new ClipboardEvent('paste', {
            clipboardData: clipboard,
            bubbles: true,
            cancelable: true,
        }));

        expect(received).to.exist;
        expect(received!.data.text).to.equal('pasted\tdata\nrow2\tval2');
        cleanup();
    });

    it('copy is a no-op when the cell editor is open (slice 163)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.updateBottomRightCellValues([
            { col: 1, row: 1, value: 'x', cellStyle: 'cs0',
              locked: false, needsMeasure: false, isPercentage: false },
        ]);
        // Open the editor.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 1, row: 1 },
            bubbles: true, composed: true,
        }));

        const clipboard = new DataTransfer();
        sheet.getScrollContainer().dispatchEvent(new ClipboardEvent('copy', {
            clipboardData: clipboard,
            bubbles: true,
            cancelable: true,
        }));

        // Editor's own copy handler runs; the sheet's copy did NOT.
        expect(clipboard.getData('text/plain')).to.equal('');
        cleanup();
    });

    it('column resize: mousedown on handle + drag + mouseup dispatches columnResized (slice 164)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'columnResized') {
                received = detail;
            }
        });

        // Start drag at col 2's right-edge handle, clientX=100.
        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-resize-start', {
            detail: { col: 2, clientX: 100 },
            bubbles: true,
            composed: true,
        }));

        // Drag 20px right → new width = 50 + 20 = 70.
        document.dispatchEvent(new MouseEvent('mousemove', { clientX: 120, bubbles: true }));

        // Release.
        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        expect(received).to.exist;
        expect(received!.data).to.deep.equal({
            colIndexes: [2],
            newWidths: [70],
        });
        cleanup();
    });

    it('row resize: mousedown on handle + drag + mouseup dispatches rowsResized (slice 164)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15)); // 15 pt each

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'rowsResized') {
                received = detail;
            }
        });

        // Start drag at row 3's bottom-edge handle, clientY=60.
        sheet.getRowHeader().dispatchEvent(new CustomEvent('row-resize-start', {
            detail: { row: 3, clientY: 60 },
            bubbles: true,
            composed: true,
        }));

        // Drag 24px down. 24px * 72/96 = 18 points. New height = 15 + 18 = 33 pt.
        document.dispatchEvent(new MouseEvent('mousemove', { clientY: 84, bubbles: true }));
        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        expect(received).to.exist;
        expect(received!.data).to.deep.equal({
            rowIndexes: [3],
            newHeights: [33],
        });
        cleanup();
    });

    it('column resize live-updates the colWidths during drag (slice 164)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH(new Array(10).fill(15));

        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-resize-start', {
            detail: { col: 1, clientX: 100 },
            bubbles: true,
            composed: true,
        }));
        document.dispatchEvent(new MouseEvent('mousemove', { clientX: 130, bubbles: true }));

        // The cell-grid's render state reflects the new width
        // (50 + 30 = 80) — visible feedback during drag.
        const state = sheet.getCellGrid().renderState;
        expect(state.colWidths[0]).to.equal(80);
        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
        cleanup();
    });

    it('column resize clamps at 0 width (no negative columns) (slice 164)', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-resize-start', {
            detail: { col: 1, clientX: 100 },
            bubbles: true,
            composed: true,
        }));
        // Drag 200px LEFT → would be -150 but clamps at 0.
        document.dispatchEvent(new MouseEvent('mousemove', { clientX: -100, bubbles: true }));
        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        const state = sheet.getCellGrid().renderState;
        expect(state.colWidths[0]).to.equal(0);
        cleanup();
    });

    it('column-header renders a resize handle for each column (slice 164)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(24);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const handles = sheet.getColumnHeader().shadowRoot!.querySelectorAll(
            '.col-resize-handle'
        );
        expect(handles).to.have.length(3);
        cleanup();
    });

    it('cell comments: setCellComments + recomputeViewport renders comment-indicator divs (slice 165)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setCellComments({
            'col2 row3': 'reviewer note',
            'col1 row1': 'header comment',
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const indicators = sheet.getCellGrid().shadowRoot!.querySelectorAll(
            '.comment-indicator'
        );
        expect(indicators).to.have.length(2);
        cleanup();
    });

    it('hyperlink cells: setHyperlinksTooltips + click dispatches linkCellClicked (slice 165)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHyperlinksTooltips({
            'col3 row2': 'https://example.com',
        });

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'linkCellClicked') {
                received = detail;
            }
        });

        // Click the hyperlink cell.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));

        expect(received).to.exist;
        expect(received!.data).to.deep.equal({ row: 2, column: 3 });
        cleanup();
    });

    it('non-hyperlink cells do NOT dispatch linkCellClicked on click (slice 165)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHyperlinksTooltips({
            'col3 row2': 'https://example.com',
        });

        let received = false;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            if ((e as CustomEvent).detail.type === 'linkCellClicked') {
                received = true;
            }
        });

        // Click a different cell.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 1, row: 1, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));

        expect(received).to.be.false;
        cleanup();
    });

    it('hyperlink cells render with .link-cell class for visual styling (slice 165)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHyperlinksTooltips({
            'col2 row2': 'https://example.com',
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const linkCell = sheet.getCellGrid().shadowRoot!.querySelector(
            '.col2.row2.link-cell'
        );
        expect(linkCell).to.not.be.null;
        // Other cells should NOT have the link-cell class.
        const otherCell = sheet.getCellGrid().shadowRoot!.querySelector(
            '.col1.row1'
        );
        expect(otherCell!.classList.contains('link-cell')).to.be.false;
        cleanup();
    });

    // ===================================================================
    // Slice 166 — right-click context menu
    // ===================================================================
    it('right-click on a cell inside the selection dispatches contextMenuOpenOnSelection (slice 166)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        // Place an explicit range selection covering B2:D4.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 4, row: 4, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        // Right-click on C3 — inside the B2:D4 selection.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-contextmenu', {
            detail: { col: 3, row: 3 },
            bubbles: true, composed: true,
        }));

        const ctxEvents = events.filter(e => e.type === 'contextMenuOpenOnSelection');
        expect(ctxEvents).to.have.length(1);
        expect(ctxEvents[0].data).to.deep.equal({ row: 3, column: 3 });
        // No new selection event since the click was inside the
        // existing selection.
        const selEvents = events.filter(e =>
            e.type === 'cellSelected' || e.type === 'cellRangeSelected'
        );
        expect(selEvents).to.have.length(0);
        cleanup();
    });

    it('right-click on a cell outside the selection moves selection and then dispatches contextMenuOpenOnSelection (slice 166)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        // Select B2 only.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        // Right-click on D4 (outside the selection).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-contextmenu', {
            detail: { col: 4, row: 4 },
            bubbles: true, composed: true,
        }));

        const selEvents = events.filter(e => e.type === 'cellSelected');
        expect(selEvents).to.have.length(1);
        expect(selEvents[0].data).to.include({ row: 4, column: 4 });
        const ctxEvents = events.filter(e => e.type === 'contextMenuOpenOnSelection');
        expect(ctxEvents).to.have.length(1);
        expect(ctxEvents[0].data).to.deep.equal({ row: 4, column: 4 });
        // Selection event should be dispatched BEFORE the context-menu
        // event so the server sees the selection update first.
        const selIdx = events.findIndex(e => e.type === 'cellSelected');
        const ctxIdx = events.findIndex(e => e.type === 'contextMenuOpenOnSelection');
        expect(selIdx).to.be.lessThan(ctxIdx);
        cleanup();
    });

    it('right-click on a column header selects whole column and dispatches columnHeaderContextMenuOpen (slice 166)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-header-contextmenu', {
            detail: { col: 3 },
            bubbles: true, composed: true,
        }));

        const colSelEvents = events.filter(e => e.type === 'columnSelected');
        expect(colSelEvents).to.have.length(1);
        expect(colSelEvents[0].data).to.include({ column: 3 });
        const ctxEvents = events.filter(e => e.type === 'columnHeaderContextMenuOpen');
        expect(ctxEvents).to.have.length(1);
        expect(ctxEvents[0].data).to.deep.equal({ columnIndex: 3 });
        cleanup();
    });

    it('right-click on a row header selects whole row and dispatches rowHeaderContextMenuOpen (slice 166)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        sheet.getRowHeader().dispatchEvent(new CustomEvent('row-header-contextmenu', {
            detail: { row: 4 },
            bubbles: true, composed: true,
        }));

        const rowSelEvents = events.filter(e => e.type === 'rowSelected');
        expect(rowSelEvents).to.have.length(1);
        expect(rowSelEvents[0].data).to.include({ row: 4 });
        const ctxEvents = events.filter(e => e.type === 'rowHeaderContextMenuOpen');
        expect(ctxEvents).to.have.length(1);
        expect(ctxEvents[0].data).to.deep.equal({ rowIndex: 4 });
        cleanup();
    });

    it('right-click contextmenu listener calls preventDefault to suppress the browser menu (slice 166)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const cell = sheet.getCellGrid().shadowRoot!.querySelector('.cell');
        expect(cell).to.not.be.null;

        const event = new MouseEvent('contextmenu', {
            bubbles: true, composed: true, cancelable: true,
        });
        cell!.dispatchEvent(event);
        expect(event.defaultPrevented).to.be.true;
        cleanup();
    });

    // ===================================================================
    // Slice 167 — column grouping outline (+/- buttons)
    // ===================================================================
    it('column grouping: setColGroupingData + setColGroupingMax renders toggle buttons (slice 167)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(8);
        sheet.setColW([50, 50, 50, 50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(20);
        sheet.setHeaderColumnWidth(40);
        sheet.setColGroupingMax(2);
        sheet.setColGroupingData([
            { uniqueIndex: 1, level: 1, startIndex: 1, endIndex: 3, collapsed: false },
            { uniqueIndex: 2, level: 2, startIndex: 4, endIndex: 5, collapsed: true },
        ]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const toggles = sheet.getColumnHeader().shadowRoot!.querySelectorAll(
            '.col-grouping-toggle'
        );
        expect(toggles).to.have.length(2);
        // First grouping is expanded → minus glyph; second is collapsed → plus.
        const glyphs = Array.from(toggles).map(t => t.textContent!.trim());
        expect(glyphs).to.include('+');
        expect(glyphs).to.include('−');
        cleanup();
    });

    it('column grouping: clicking a toggle dispatches groupingCollapsed with flipped state (slice 167)', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(8);
        sheet.setColW([50, 50, 50, 50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(20);
        sheet.setColGroupingMax(1);
        sheet.setColGroupingData([
            { uniqueIndex: 7, level: 1, startIndex: 2, endIndex: 4, collapsed: false },
        ]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        const toggle = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-grouping-toggle'
        ) as HTMLButtonElement;
        expect(toggle).to.not.be.null;
        toggle.click();

        const groupingEvents = events.filter(e => e.type === 'groupingCollapsed');
        expect(groupingEvents).to.have.length(1);
        // Was expanded (collapsed:false) → click requests COLLAPSED (true).
        expect(groupingEvents[0].data).to.deep.equal({
            cols: true,
            colIndex: 7,
            collapsed: true,
        });
        cleanup();
    });

    it('column grouping: clicking a collapsed toggle requests EXPAND (slice 167)', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(8);
        sheet.setColW([50, 50, 50, 50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(20);
        sheet.setColGroupingMax(1);
        sheet.setColGroupingData([
            { uniqueIndex: 9, level: 1, startIndex: 1, endIndex: 2, collapsed: true },
        ]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        const toggle = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-grouping-toggle'
        ) as HTMLButtonElement;
        toggle.click();

        const groupingEvents = events.filter(e => e.type === 'groupingCollapsed');
        expect(groupingEvents).to.have.length(1);
        // Was collapsed → request expanded.
        expect(groupingEvents[0].data).to.include({ collapsed: false });
        cleanup();
    });

    it('column grouping: toggle click does NOT also fire column-header-click (slice 167)', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(8);
        sheet.setColW([50, 50, 50, 50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(20);
        sheet.setColGroupingMax(1);
        sheet.setColGroupingData([
            { uniqueIndex: 1, level: 1, startIndex: 1, endIndex: 2, collapsed: false },
        ]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        const toggle = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-grouping-toggle'
        ) as HTMLButtonElement;
        toggle.click();

        // Only the grouping event. NO selection event (columnSelected/etc).
        expect(events.filter(e => e.type === 'columnSelected')).to.have.length(0);
        expect(events.filter(e => e.type === 'groupingCollapsed')).to.have.length(1);
        cleanup();
    });

    it('column grouping: gutter rows above letters move the letter row down by colGroupingMax * 16 px (slice 167)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(20);
        sheet.setColGroupingMax(2);
        sheet.setColGroupingData([]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const firstLetter = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-header-cell'
        ) as HTMLElement;
        expect(firstLetter).to.not.be.null;
        // Default (non-inversed): gutter on top → letter row at colGroupingMax * 16 = 32px.
        expect(firstLetter.style.top).to.equal('32px');
        cleanup();
    });

    it('column grouping: inversed mode puts gutter BELOW letters and button at END (slice 167)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(20);
        sheet.setColGroupingMax(1);
        sheet.setColGroupingInversed(true);
        sheet.setColGroupingData([
            { uniqueIndex: 3, level: 1, startIndex: 1, endIndex: 3, collapsed: false },
        ]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const firstLetter = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-header-cell'
        ) as HTMLElement;
        // Inversed: letter row stays at top (0).
        expect(firstLetter.style.top).to.equal('0px');
        // Button is at endPx (end of column 4 = 4 * 50 = 200).
        const toggle = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-grouping-toggle'
        ) as HTMLButtonElement;
        expect(toggle.style.left).to.equal('200px');
        cleanup();
    });

    // ===================================================================
    // Slice 173 — `part` attribute scaffolding for documented styling API
    // ===================================================================
    it('cells render with part="cell" (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const cells = sheet.getCellGrid().shadowRoot!.querySelectorAll(
            '[part~="cell"]'
        );
        expect(cells.length).to.be.greaterThan(0);
        // Active cell A1: part="cell" (no cell-range — it's the active
        // cell itself).
        const a1 = sheet.getCellGrid().shadowRoot!.querySelector(
            '.cell.col1.row1'
        ) as HTMLElement;
        expect(a1.getAttribute('part')).to.equal('cell');
        cleanup();
    });

    it('cells in active range get part="cell cell-range" except the active cell (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        // Select B2:C3 (active B2 + range to C3).
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        // Active cell (B2) — no cell-range.
        const b2 = sheet.getCellGrid().shadowRoot!.querySelector(
            '.cell.col2.row2'
        ) as HTMLElement;
        expect(b2.getAttribute('part')).to.equal('cell');
        // C3 inside the range, not active.
        const c3 = sheet.getCellGrid().shadowRoot!.querySelector(
            '.cell.col3.row3'
        ) as HTMLElement;
        expect(c3.getAttribute('part')).to.equal('cell cell-range');
        cleanup();
    });

    it('column headers render with part="column-header"; cells in selected range get header-selected (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderRowHeight(20);
        // Select B2:C3 — columns 2-3 selected, columns 1, 4, 5 not.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const b = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-header-cell.col2'
        ) as HTMLElement;
        const c = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-header-cell.col3'
        ) as HTMLElement;
        const a = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-header-cell.col1'
        ) as HTMLElement;
        expect(b.getAttribute('part')).to.equal('column-header header-selected');
        expect(c.getAttribute('part')).to.equal('column-header header-selected');
        expect(a.getAttribute('part')).to.equal('column-header');
        cleanup();
    });

    it('row headers render with part="row-header"; rows in selected range get header-selected (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHeaderColumnWidth(40);
        // Select B2:C3 — rows 2-3 selected, 1/4/5 not.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', {
            detail: { col: 3, row: 3, shiftKey: true, ctrlKey: false,
                      metaKey: false, altKey: false },
            bubbles: true, composed: true,
        }));
        sheet.recomputeViewport();
        await sheet.getRowHeader().updateComplete;

        const r2 = sheet.getRowHeader().shadowRoot!.querySelector(
            '.row-header-cell.row2'
        ) as HTMLElement;
        const r3 = sheet.getRowHeader().shadowRoot!.querySelector(
            '.row-header-cell.row3'
        ) as HTMLElement;
        const r1 = sheet.getRowHeader().shadowRoot!.querySelector(
            '.row-header-cell.row1'
        ) as HTMLElement;
        expect(r2.getAttribute('part')).to.equal('row-header header-selected');
        expect(r3.getAttribute('part')).to.equal('row-header header-selected');
        expect(r1.getAttribute('part')).to.equal('row-header');
        cleanup();
    });

    it('sheet tabs render with part="tab"; active tab gets tab-selected (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setSheetNames(['One', 'Two', 'Three']);
        sheet.setSheetIndex(2);
        await sheet.getSheetTabs().updateComplete;

        const tabs = sheet.getSheetTabs().shadowRoot!.querySelectorAll('.sheet-tab');
        expect(tabs).to.have.length(3);
        expect(tabs[0].getAttribute('part')).to.equal('tab');
        expect(tabs[1].getAttribute('part')).to.equal('tab tab-selected');
        expect(tabs[2].getAttribute('part')).to.equal('tab');
        cleanup();
    });

    it('comment indicators render with part="comment-triangle" (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setCellComments({ 'col2 row2': 'note' });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const indicator = sheet.getCellGrid().shadowRoot!.querySelector(
            '.comment-indicator'
        ) as HTMLElement;
        expect(indicator).to.not.be.null;
        expect(indicator.getAttribute('part')).to.equal('comment-triangle');
        cleanup();
    });

    it('cell editor input renders with part="cell-input" (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        // Open editor via double-click.
        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col: 2, row: 2 },
            bubbles: true, composed: true,
        }));
        await sheet.getCellGrid().updateComplete;

        const input = sheet.getCellGrid().shadowRoot!.querySelector(
            '.cell-editor'
        ) as HTMLInputElement;
        expect(input).to.not.be.null;
        expect(input.getAttribute('part')).to.equal('cell-input');
        cleanup();
    });

    it('selection overlay renders with part="cell-range" + selection-corner child has part="selection-corner" (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const selection = sheet.getCellGrid().shadowRoot!.querySelector(
            '.sheet-selection'
        ) as HTMLElement;
        expect(selection).to.not.be.null;
        expect(selection.getAttribute('part')).to.equal('cell-range');
        const corner = selection.querySelector('.selection-corner') as HTMLElement;
        expect(corner).to.not.be.null;
        expect(corner.getAttribute('part')).to.equal('selection-corner');
        cleanup();
    });

    it('popup buttons render with part="popup-button" (slice 173)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setPopupButtons([
            { col: 2, row: 2, active: false } as any,
        ]);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const btn = sheet.getCellGrid().shadowRoot!.querySelector(
            '.popup-button'
        ) as HTMLElement;
        expect(btn).to.not.be.null;
        expect(btn.getAttribute('part')).to.equal('popup-button');
        cleanup();
    });

    // ===================================================================
    // Slice 174 — honor HIDE_TABSHEET_STYLE on host
    // ===================================================================
    it('sheet tabs hide when host gets the hidetabsheet class (slice 174)', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setSheetNames(['Sheet1', 'Sheet2']);
        sheet.setSheetIndex(1);
        await sheet.getSheetTabs().updateComplete;

        const tabs = sheet.getSheetTabs();
        // Initially visible.
        expect(tabs.style.display).to.not.equal('none');

        // Add hidetabsheet → tabs hidden.
        host.classList.add('hidetabsheet');
        // MutationObserver is async — wait a microtask.
        await Promise.resolve();
        expect(tabs.style.display).to.equal('none');

        // Remove → tabs visible again.
        host.classList.remove('hidetabsheet');
        await Promise.resolve();
        expect(tabs.style.display).to.not.equal('none');
        cleanup();
    });

    it('initial hidetabsheet class on the host is honored at attach time (slice 174)', async () => {
        const host = document.createElement('div');
        host.classList.add('hidetabsheet');
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setSheetNames(['One']);
        sheet.setSheetIndex(1);
        await sheet.getSheetTabs().updateComplete;
        expect(sheet.getSheetTabs().style.display).to.equal('none');
        sheet.dispose();
        document.body.removeChild(host);
    });

    // ===================================================================
    // Slice 175 — formula bar implementation
    // ===================================================================
    it('formula bar renders two inputs with documented parts (slice 175)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        await sheet.getFormulaBar().updateComplete;
        const fb = sheet.getFormulaBar().shadowRoot!;
        const addr = fb.querySelector('input.address') as HTMLInputElement;
        const formula = fb.querySelector('input.formula') as HTMLInputElement;
        expect(addr).to.not.be.null;
        expect(formula).to.not.be.null;
        expect(addr.getAttribute('part')).to.equal('address-field');
        expect(formula.getAttribute('part')).to.equal('formula-field');
        cleanup();
    });

    it('updateFormulaBar sets the address-field value (slice 175)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.updateFormulaBar('B7', 2, 7);
        await sheet.getFormulaBar().updateComplete;
        const addr = sheet.getFormulaBar().shadowRoot!.querySelector(
            'input.address'
        ) as HTMLInputElement;
        expect(addr.value).to.equal('B7');
        cleanup();
    });

    it('showSelectedCell sets both address + formula fields (slice 175)', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.showSelectedCell('C3', 3, 3, '=A1+B2', true, false, false);
        await sheet.getFormulaBar().updateComplete;
        const fb = sheet.getFormulaBar().shadowRoot!;
        const addr = fb.querySelector('input.address') as HTMLInputElement;
        const formula = fb.querySelector('input.formula') as HTMLInputElement;
        expect(addr.value).to.equal('C3');
        expect(formula.value).to.equal('=A1+B2');
        cleanup();
    });

    it('pressing Enter in the address field dispatches sheetAddressChanged (slice 175)', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.updateFormulaBar('A1', 1, 1);
        await sheet.getFormulaBar().updateComplete;

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        const addr = sheet.getFormulaBar().shadowRoot!.querySelector(
            'input.address'
        ) as HTMLInputElement;
        addr.value = 'D9';
        const ev = new KeyboardEvent('keydown', {
            key: 'Enter', bubbles: true, composed: true, cancelable: true,
        });
        addr.dispatchEvent(ev);

        const addrEvents = events.filter(e => e.type === 'sheetAddressChanged');
        expect(addrEvents).to.have.length(1);
        expect(addrEvents[0].data).to.deep.equal({ value: 'D9' });
        cleanup();
    });

    it('formula bar hides when host gets the hidefunctionbar class (slice 175)', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        await sheet.getFormulaBar().updateComplete;
        expect(sheet.getFormulaBar().style.display).to.not.equal('none');
        host.classList.add('hidefunctionbar');
        await Promise.resolve();
        expect(sheet.getFormulaBar().style.display).to.equal('none');
        host.classList.remove('hidefunctionbar');
        await Promise.resolve();
        expect(sheet.getFormulaBar().style.display).to.not.equal('none');
        cleanup();
    });

    it('recomputeViewport runs without throwing on a detached host (zero dimensions)', () => {
        // Constructor without DOM attachment: clientWidth/Height = 0.
        // The viewport math should still produce a valid render
        // state without crashing — buffers may still kick in, but
        // the call must not throw.
        const host = document.createElement('div');
        const sheet = new Spreadsheet(host);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);

        // The call shouldn't throw + should leave the cell-grid with
        // a valid render state.
        expect(() => sheet.recomputeViewport()).to.not.throw();
        const state = sheet.getCellGrid().renderState;
        // Zero scroll position with zero viewport → first col = 1.
        expect(state.viewport.firstColumnIndex).to.equal(1);
        // Total grid dimensions are computed from the sheet's data,
        // independent of viewport size.
        expect(state.totalGridWidthPx).to.equal(250);

        sheet.dispose();
    });
});
