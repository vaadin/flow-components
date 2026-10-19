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
import { rendered } from './spreadsheet-harness.js';

/**
 * Run with the browser reporting the given platform: the sheet's shortcuts
 * take Cmd on a Mac and Ctrl elsewhere, as the GWT client's did.
 */
function onPlatform<T>(platform: string, body: () => T): T {
    Object.defineProperty(navigator, 'platform', { value: platform, configurable: true });
    try {
        return body();
    } finally {
        delete (navigator as { platform?: string }).platform;
    }
}

/** Sheets the tests below make, disposed after each test: a sheet listens on the page. */
const sheets: Spreadsheet[] = [];

function newSheet(host: HTMLElement = document.createElement('div')): Spreadsheet {
    const sheet = new Spreadsheet(host);
    sheets.push(sheet);
    return sheet;
}

afterEach(() => {
    sheets.splice(0).forEach((sheet) => sheet.dispose());
});

describe('Spreadsheet skeleton — constructor + identity', () => {
    it('is constructable with a host element', () => {
        const host = document.createElement('div');
        const sheet = newSheet(host);

        expect(sheet).to.be.instanceOf(Spreadsheet);
    });

    it('sizes the sheet, not the component, via the setters', () => {
        // The component's size belongs to the application; the setters
        // are how the wrapper tells the sheet how much room it has.
        const host = document.createElement('div');
        const sheet = newSheet(host);

        sheet.setWidth('500px');
        sheet.setHeight('400px');

        const root = host.querySelector<HTMLElement>('.spreadsheet-root')!;
        expect(root.style.width).to.equal('500px');
        expect(root.style.height).to.equal('400px');
        expect(host.style.width).to.equal('');
        expect(host.style.height).to.equal('');
    });
});

describe('Spreadsheet skeleton — property mirrors', () => {
    let sheet: Spreadsheet;

    beforeEach(() => {
        sheet = newSheet();
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
            { col1: 1, col2: 3, row1: 1, row2: 1 },
            { col1: 5, col2: 7, row1: 2, row2: 4 },
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

describe('Spreadsheet skeleton — initial selection state', () => {
    it('starts with A1 (single-cell) selected', () => {
        const sheet = newSheet();

        const sel = sheet.getSelection();

        expect(sel.activeCell).to.deep.equal({ col: 1, row: 1 });
        expect(sel.activeRange).to.deep.equal({ c1: 1, c2: 1, r1: 1, r2: 1 });
        expect(sel.auxRanges).to.deep.equal([]);
    });
});

describe('Spreadsheet — render pipeline', () => {
    function makeSheet(): Spreadsheet {
        return newSheet();
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
        // The scroll container is a CSS grid of the cell-grid, the column
        // and row headers and the corner box at (col 1, row 1), with the
        // formula bar above it and the sheet-tabs strip below. The context
        // menu stays empty until the server answers a right-click with the
        // actions it offers. They all hang off the component's own root
        // element, which owns the vertical layout — the host belongs to the
        // application.
        const host = document.createElement('div');
        const sheet = newSheet(host);

        expect(host.children).to.have.length(1);
        const root = sheet.getRoot();
        expect(host.firstElementChild).to.equal(root);

        // Root has 12 children: formula bar + context menu + scroll
        // container + sheet tabs + the popup overlay, which is outside the
        // scroll container so the keys typed into a popup are not the
        // sheet's, the cover, line and tooltip a header drag shows, a
        // link cell's tooltip, the command a selection by touch offers,
        // and the point Escape then Tab leaves the sheet from, with its
        // hint.
        expect(root.children).to.have.length(12);
        const rootKids = Array.from(root.children);
        expect(rootKids.includes(sheet.getFormulaBar())).to.be.true;
        expect(rootKids.includes(sheet.getContextMenu())).to.be.true;
        expect(rootKids.includes(sheet.getScrollContainer())).to.be.true;
        expect(rootKids.includes(sheet.getSheetTabs())).to.be.true;
        expect(rootKids.includes(sheet.getPopupOverlay())).to.be.true;

        const scroller = sheet.getScrollContainer();
        // Four scroll-container children: header-corner, column-header,
        // row-header, cell-grid. (Identity checks via `===` to avoid chai
        // serializing LitElements with cyclic renderState.)
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

    it('buildRenderState gives every merged region a box', () => {
        // Rows 1-2 frozen, scrolled 10px: a merge from row 2 to row 4 is
        // pinned with the frozen rows and shortened by the scroll, its rows
        // past the split starting past the freeze line's own pixel.
        const sheet = makeSheet();
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15)); // 20px
        sheet.setVerticalSplitPosition(2);
        sheet.setMergedRegions([{ col1: 3, col2: 4, row1: 2, row2: 4 }]);

        const state = sheet.buildRenderState({
            scrollLeft: 0,
            scrollTop: 10,
            viewportWidth: 200,
            viewportHeight: 80,
        });

        expect(state.mergedRegionBoxes).to.deep.equal([
            { left: 100, top: 20, width: 100, height: 20 + 1 + 30 },
        ]);
    });

    it('buildRenderState makes the scrolling extent a pixel longer on each frozen axis, for the freeze line', () => {
        const sheet = makeSheet();
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15)); // 20px
        const size = () => {
            const state = sheet.buildRenderState({ scrollLeft: 0, scrollTop: 0, viewportWidth: 200, viewportHeight: 80 });
            return `${state.totalGridWidthPx} ${state.totalGridHeightPx}`;
        };

        expect(size()).to.equal('500 200');
        sheet.setHorizontalSplitPosition(2);
        expect(size()).to.equal('501 200');
        sheet.setVerticalSplitPosition(3);
        expect(size()).to.equal('501 201');
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

describe('Spreadsheet — scroll-event wiring', () => {
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
        host.style.cssText = `width:${width}px;height:${height}px;display:flex;flex-direction:column;`;
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        return {
            host,
            sheet,
            // Dispose the sheet on cleanup so the ResizeObserver
            //  + scroll listener don't leak
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
        // + cell-grid (CSS-grid layout).
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

    it('totalGridHeightPx adds up the rows converted from points', () => {
        const { sheet, cleanup } = attachSheet(200, 100);
        sheet.setRows(5);
        sheet.setCols(1);
        sheet.setColW([50]);
        sheet.setRowH([15, 15, 15, 15, 15]); // 15 pt each

        // At 96 ppi: 15 pt → 20 px, total = 100.
        const state = sheet.buildRenderState({
            scrollLeft: 0, scrollTop: 0, viewportWidth: 50, viewportHeight: 200,
        });
        expect(state.totalGridHeightPx).to.equal(100);

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

    it('buildRenderState carries scrollLeft + scrollTop into the render state', () => {
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

describe('Spreadsheet — recomputeViewport + ResizeObserver lifecycle', () => {
    function attachSheetForLifecycle(width: number, height: number): {
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
        host.style.cssText = 'width:200px;height:100px;display:flex;flex-direction:column;';
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

    it('header corner box sits in CSS-grid (col 1, row 1) with sticky positioning', () => {
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

    it('header corner is as wide as the row numbers and as tall as the column letters', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.recomputeViewport();

        // The GWT client's 50 and 27: a sheet's first cell starts where it did.
        const corner = sheet.getHeaderCorner();
        expect(corner.style.width).to.equal('50px');
        expect(corner.style.height).to.equal('27px');

        cleanup();
    });

    it('header corner collapses with the headings hidden, and comes back with them', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.setDisplayRowColHeadings(false);
        sheet.notifyStateChanges(['displayRowColHeadings'], false);
        expect(sheet.getHeaderCorner().style.width).to.equal('0px');
        expect(sheet.getHeaderCorner().style.height).to.equal('0px');

        sheet.setDisplayRowColHeadings(true);
        sheet.notifyStateChanges(['displayRowColHeadings'], false);
        expect(sheet.getHeaderCorner().style.width).to.equal('50px');
        expect(sheet.getHeaderCorner().style.height).to.equal('27px');

        cleanup();
    });

    it('row header gets its width and the row heights in pixels', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 18, 15, 12, 15]); // varying row heights

        sheet.recomputeViewport();

        const rowState = sheet.getRowHeader().renderState;
        expect(rowState.headerWidth).to.equal(50);
        // rowHeights are in points → pixels at 96 ppi (15pt→20, 18pt→24,
        // 12pt→16). Use Array.from to avoid chai serializing the
        // ReadonlyArray + LitElement cycle on assertion failure.
        expect(Array.from(rowState.rowHeights)).to.deep.equal([
            20, 24, 20, 16, 20,
        ]);
        // Total grid height matches what the cell-grid uses.
        expect(rowState.totalGridHeightPx).to.equal(20 + 24 + 20 + 16 + 20);

        // The row header's inline width matches.
        expect(sheet.getRowHeader().style.width).to.equal('50px');

        cleanup();
    });

    it('column header gets its height and the column widths', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 60, 40]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        sheet.recomputeViewport();

        const headerState = sheet.getColumnHeader().renderState;
        expect(headerState.headerHeight).to.equal(27);
        // Use plain-array comparison, not chai .deep.equal, to avoid
        // chai walking into the LitElement's cyclic renderState on
        // assertion failure.
        expect(Array.from(headerState.colWidths)).to.deep.equal([50, 60, 40]);
        expect(headerState.colX[0]).to.equal(0);
        // Total grid width matches the cell-grid's total: 50+60+40=150.
        expect(headerState.totalGridWidthPx).to.equal(150);

        // The column header's inline height matches.
        expect(sheet.getColumnHeader().style.height).to.equal('27px');

        cleanup();
    });

    it('cell-click event from the cell-grid updates the active cell selection', () => {
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

    it('shift-click extends the active range from the previous active cell', () => {
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

    it('cell-click triggers a re-render with the new selection', () => {
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

    it('scroll container is focusable via tabindex=0', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        expect(sheet.getScrollContainer().tabIndex).to.equal(0);
        cleanup();
    });

    it('ArrowRight moves the active cell one column right', () => {
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

    it('ArrowDown / ArrowLeft / ArrowUp all move the active cell one cell', () => {
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

    it('Arrow keys clamp at the grid edges', () => {
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

    it('Shift+ArrowDown extends the range without moving the active cell', () => {
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

    it('Shift+arrow contracts the range when extending back toward the anchor', () => {
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

    it('plain arrow after a shift-extended range resets the extend position', () => {
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

    it('keyboard navigation is a no-op when rows or cols is 0', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        // No setRows/setCols — rows=cols=0.
        const before = sheet.getSelection();

        dispatchKey(sheet.getScrollContainer(), 'ArrowDown');

        // Selection unchanged.
        expect(sheet.getSelection() === before).to.be.true;

        cleanup();
    });

    it('column-header-click selects the entire column', () => {
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

    it('shift-click on column header extends from anchor column to clicked column', () => {
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

    it('row-header-click selects the entire row', () => {
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

    it('shift-click on row header extends from anchor row to clicked row', () => {
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

    it('Tab moves active cell right + Shift+Tab moves left', () => {
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

    it('Enter opens the editor on a single cell, shift or no shift', () => {
        // Enter moves on once an edit is committed, and walks a range
        // that is selected; on one cell it is the same case as F2.
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
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 3 });
        expect(sheet.getCellGrid().renderState.editor).to.not.be.null;

        sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-editor-cancel', {
            bubbles: true,
            composed: true,
        }));
        dispatchKey(scroller, 'Enter', { shiftKey: true });
        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 3 });
        expect(sheet.getCellGrid().renderState.editor).to.not.be.null;
        cleanup();
    });

    it('Home / End jump to first / last column in current row', () => {
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

    it('Ctrl+Home and Ctrl+End jump to A1 / last cell', () => {
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

    it('cell-click dispatches spreadsheet-event with cellSelected', () => {
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

    it('shift-click dispatches cellRangeSelected with the new range', () => {
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

        // The range alone: the server holds its starting point, the cell
        // clicked before. Told the point again first, the server reported
        // a selection of that one cell in between.
        expect(received).to.have.length(1);
        expect(received[0].type).to.equal('cellRangeSelected');
        expect(received[0].data).to.deep.equal({
            row1: 2, col1: 2, row2: 4, col2: 5,
        });
        cleanup();
    });

    it('column-header click dispatches columnSelected', () => {
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

    it('row-header click dispatches rowSelected', () => {
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

    it('keyboard arrow dispatches cellSelected', () => {
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

    it('shift+arrow dispatches cellRangeSelected', () => {
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

    it('Delete key dispatches deleteSelectedCells spreadsheet-event', () => {
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

    it('Backspace also dispatches deleteSelectedCells', () => {
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

    it('Delete does NOT dispatch deleteSelectedCells on an empty grid', () => {
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

    it('Delete does NOT modify the client-side selection', () => {
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

    it('mousedown on a cell starts a drag and selects the anchor cell', () => {
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

    it('mouseover during drag extends the range from anchor to hover cell', () => {
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

    it('mouseover when NOT dragging does not change selection', () => {
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

    it('mouseup ends the drag and dispatches cellRangePainted, with where it started', () => {
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

        const received: { type: string; data: Record<string, unknown> }[] = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            received.push((e as CustomEvent).detail);
        });

        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        // One event, carrying the cell the drag started on, which the
        // server has not heard of.
        expect(received.map((e) => e.type)).to.deep.equal(['cellRangePainted']);
        expect(received[0].data).to.deep.equal({
            selectedCellRow: 2, selectedCellColumn: 2, row1: 2, col1: 2, row2: 4, col2: 5,
        });
        cleanup();
    });

    it('mouseup after a single-cell drag does NOT dispatch a duplicate cellSelected', () => {
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

    it('mouseup is a no-op when no drag was in progress', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);

        let received = false;
        host.addEventListener('spreadsheet-event', () => { received = true; });

        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

        expect(received).to.be.false;
        cleanup();
    });

    it('shift+mousedown extends range from existing anchor', () => {
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

    it('keyboard nav past the right edge scrolls the active cell into view', () => {
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

    it('keyboard nav past the bottom edge scrolls the active cell into view', () => {
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

    it('keyboard nav within visible viewport does NOT scroll', () => {
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

    it('ArrowLeft past the left edge scrolls back to bring active into view', () => {
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

    it('scrolling dispatches onSheetScroll with the new viewport bounds', () => {
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
        // The range to load, and the cells drawn, which the server's getters
        // of the range shown give.
        expect(received!.data).to.have.all.keys(
            'firstRow', 'firstColumn', 'lastRow', 'lastColumn',
            'drawnFirstRow', 'drawnFirstColumn', 'drawnLastRow', 'drawnLastColumn'
        );
        expect(received!.data.drawnFirstColumn).to.be.greaterThanOrEqual(3);
        cleanup();
    });

    it('vertical scroll asks for the rows scrolled into view and a margin beyond them', async () => {
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
        // None of them was loaded: they are asked for first, and once the
        // scroll has stopped, the cells past them.
        await new Promise((resolve) => setTimeout(resolve, 300));

        // Rows from 11 are in view. The request covers them and loads
        // ahead, so the next steps of a scroll need no request of their own.
        expect(received).to.exist;
        expect(received!.data.firstRow).to.be.at.most(11);
        expect(received!.data.lastRow).to.be.greaterThan(15);
        cleanup();
    });

    it('reports each change of the visible range to the server once', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(50);
        sheet.setCols(20);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(50).fill(15));
        sheet.setRowBufferSize(0);
        sheet.setColumnBufferSize(0);
        // The startup handshake, so what follows is plain scrolling.
        sheet.recomputeViewport();

        const scrolls: unknown[] = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'onSheetScroll') {
                scrolls.push(detail.data);
            }
        });

        const scroller = sheet.getScrollContainer();
        scroller.scrollTop = 200;
        scroller.dispatchEvent(new Event('scroll'));
        // Another scroll event at the same place: there is nothing new for
        // the server to load. The GWT client asked only once the view had
        // moved on.
        scroller.dispatchEvent(new Event('scroll'));

        expect(scrolls).to.have.length(1);
        cleanup();
    });

    it('Ctrl+A selects the entire sheet off a Mac', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(20);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(20).fill(15));

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'cellRangePainted') {
                received = detail;
            }
        });

        onPlatform('Win32', () =>
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key: 'a',
                ctrlKey: true,
                bubbles: true,
                cancelable: true,
            }))
        );

        expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 1 });
        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 1, c2: 10, r1: 1, r2: 20,
        });
        // With A1 as its starting point, as the GWT client sent it.
        expect(received!.data).to.deep.equal({
            selectedCellRow: 1, selectedCellColumn: 1, row1: 1, col1: 1, row2: 20, col2: 10,
        });
        cleanup();
    });

    it('Cmd+A (Mac convention) also selects the entire sheet', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);

        onPlatform('MacIntel', () =>
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key: 'a',
                metaKey: true,
                bubbles: true,
                cancelable: true,
            }))
        );

        expect(sheet.getSelection().activeRange).to.deep.equal({
            c1: 1, c2: 5, r1: 1, r2: 5,
        });
        cleanup();
    });

    it('Ctrl+Z dispatches onUndo off a Mac', () => {
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

        onPlatform('Win32', () =>
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key: 'z',
                ctrlKey: true,
                bubbles: true,
                cancelable: true,
            }))
        );

        expect(received).to.exist;
        cleanup();
    });

    it('Ctrl+Y dispatches onRedo off a Mac', () => {
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

        onPlatform('Win32', () =>
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key: 'y',
                ctrlKey: true,
                bubbles: true,
                cancelable: true,
            }))
        );

        expect(received).to.exist;
        cleanup();
    });

    it('Cmd+Shift+Z (Mac convention) also dispatches onRedo', () => {
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

        onPlatform('MacIntel', () =>
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key: 'z',
                metaKey: true,
                shiftKey: true,
                bubbles: true,
                cancelable: true,
            }))
        );

        expect(received).to.exist;
        cleanup();
    });

    it('Ctrl+Shift+Z dispatches onRedo (Windows convention)', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);

        let received = false;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            if ((e as CustomEvent).detail.type === 'onRedo') {
                received = true;
            }
        });

        onPlatform('Win32', () =>
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key: 'z',
                ctrlKey: true,
                shiftKey: true,
                bubbles: true,
                cancelable: true,
            }))
        );

        expect(received).to.be.true;
        cleanup();
    });

    it('plain Ctrl+Z without shift does NOT dispatch onRedo', () => {
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

        onPlatform('Win32', () =>
            sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                key: 'z',
                ctrlKey: true,
                bubbles: true,
                cancelable: true,
            }))
        );

        expect(undoReceived).to.be.true;
        expect(redoReceived).to.be.false;
        cleanup();
    });

    it('Ctrl+A and Ctrl+Z do nothing on a Mac, where the GWT client took Cmd', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        const types: string[] = [];
        host.addEventListener('spreadsheet-event', (e: Event) => types.push((e as CustomEvent).detail.type));

        onPlatform('MacIntel', () => {
            for (const key of ['a', 'z', 'y']) {
                sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', {
                    key,
                    ctrlKey: true,
                    bubbles: true,
                    cancelable: true,
                }));
            }
        });

        expect(sheet.getSelection().activeRange).to.deep.equal({ c1: 1, c2: 1, r1: 1, r2: 1 });
        expect(types.filter((t) => ['onUndo', 'onRedo', 'cellRangeSelected', 'cellRangePainted'].includes(t))).to.be.empty;
        cleanup();
    });

    it('constructor accepts an optional renderTarget arg for shadow-DOM hosts', () => {
        // The Lit wrapper passes (element, element.renderRoot) so
        // DOM goes into the shadow root, not the light DOM. Verify
        // the second arg controls where children are appended.
        const host = document.createElement('div');
        const shadow = host.attachShadow({ mode: 'open' });
        document.body.appendChild(host);

        const sheet = new Spreadsheet(host, shadow);

        // Everything the component draws goes inside the shadow root,
        // under its own root element, not into the light DOM.
        expect(shadow.children.length).to.equal(1);
        expect(shadow.firstElementChild).to.equal(sheet.getRoot());
        // Formula bar, context menu, popup overlay, scroll container, tabs,
        // what a header drag shows, a link cell's tooltip, the command a
        // selection by touch offers, and the way out of the sheet with its
        // hint.
        expect(sheet.getRoot().children.length).to.equal(12);
        expect(host.children.length).to.equal(0);
        // The host belongs to the application: the component must not
        // write a layout onto it. An inline style would beat any
        // stylesheet the application writes, leaving it no way to
        // position the component but `!important`.
        expect(host.style.display).to.equal('');
        expect(host.style.flexDirection).to.equal('');

        sheet.dispose();
        host.remove();
    });

    it('draws the first property batch even when it is empty', () => {
        const { sheet, cleanup } = attachSheetForLifecycle(200, 100);
        sheet.setRows(10);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(10).fill(15));

        const before = sheet.getCellGrid().renderState;
        sheet.notifyStateChanges([], true);
        const afterFirst = sheet.getCellGrid().renderState;
        expect(afterFirst === before, 'the first batch drew nothing').to.be.false;

        sheet.notifyStateChanges([], false);
        expect(sheet.getCellGrid().renderState === afterFirst, 'an empty later batch drew').to.be.true;

        cleanup();
    });

    it('copy writes selected cells as TSV to the clipboard', () => {
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

    it('cut dispatches clearSelectedCellsOnCut AND writes to clipboard', () => {
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

    it('paste dispatches onPaste with the clipboard text', () => {
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

    it('copy is a no-op when the cell editor is open', () => {
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

    it('column resize: mousedown on handle + drag + mouseup dispatches columnResized', () => {
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

    it('row resize: mousedown on handle + drag + mouseup dispatches rowsResized', () => {
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

    it('column resize keeps the width until the drag ends, as the GWT client did', () => {
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
        sheet.recomputeViewport();

        // A line and a tooltip show the 80px it would take; the column has
        // its 50px until the pointer is let go.
        expect(sheet.getCellGrid().renderState.colWidths[0]).to.equal(50);
        document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
        expect(sheet.getCellGrid().renderState.colWidths[0]).to.equal(80);
        cleanup();
    });

    it('column resize clamps at 0 width (no negative columns)', () => {
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

    it('column-header renders a resize handle for each column', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(3);
        sheet.setColW([50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const handles = sheet.getColumnHeader().shadowRoot!.querySelectorAll(
            '.col-resize-handle'
        );
        expect(handles).to.have.length(3);
        cleanup();
    });

    it('cell comments: setCellComments + recomputeViewport renders comment-indicator divs', async () => {
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

    it('hyperlink cells: setHyperlinksTooltips + click dispatches linkCellClicked', () => {
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

    it('non-hyperlink cells do NOT dispatch linkCellClicked on click', () => {
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

    it('hyperlink cells render with .link-cell class for visual styling', async () => {
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
    // Right-click context menu
    // ===================================================================
    it('right-click on a cell inside the selection dispatches contextMenuOpenOnSelection', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHasActions(true);
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
            e.type === 'cellSelected' || e.type === 'cellRangeSelected' || e.type === 'cellRangePainted'
        );
        expect(selEvents).to.have.length(0);
        cleanup();
    });

    it('right-click on a cell outside the selection moves selection and then dispatches contextMenuOpenOnSelection', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHasActions(true);
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

    it('right-click on a column header leaves the selection and dispatches columnHeaderContextMenuOpen', () => {
        // The GWT client left the selection alone: the menu acts on the
        // column whatever is selected.
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHasActions(true);

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        sheet.getColumnHeader().dispatchEvent(new CustomEvent('column-header-contextmenu', {
            detail: { col: 3 },
            bubbles: true, composed: true,
        }));

        expect(events.filter(e => e.type === 'columnSelected')).to.be.empty;
        expect(sheet.getSelection().activeRange).to.deep.equal({ c1: 1, c2: 1, r1: 1, r2: 1 });
        const ctxEvents = events.filter(e => e.type === 'columnHeaderContextMenuOpen');
        expect(ctxEvents).to.have.length(1);
        expect(ctxEvents[0].data).to.deep.equal({ columnIndex: 3 });
        cleanup();
    });

    it('right-click on a row header leaves the selection and dispatches rowHeaderContextMenuOpen', () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHasActions(true);

        const events: Array<{ type: string; data: Record<string, unknown> }> = [];
        host.addEventListener('spreadsheet-event', (e: Event) => {
            events.push((e as CustomEvent).detail);
        });

        sheet.getRowHeader().dispatchEvent(new CustomEvent('row-header-contextmenu', {
            detail: { row: 4 },
            bubbles: true, composed: true,
        }));

        expect(events.filter(e => e.type === 'rowSelected')).to.be.empty;
        expect(sheet.getSelection().activeRange).to.deep.equal({ c1: 1, c2: 1, r1: 1, r2: 1 });
        const ctxEvents = events.filter(e => e.type === 'rowHeaderContextMenuOpen');
        expect(ctxEvents).to.have.length(1);
        expect(ctxEvents[0].data).to.deep.equal({ rowIndex: 4 });
        cleanup();
    });

    it('right-click contextmenu listener calls preventDefault to suppress the browser menu', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setHasActions(true);
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

    it('right-click leaves the browser its own menu when the sheet has no actions, as with the GWT client', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();
        await Promise.all([
            sheet.getCellGrid().updateComplete,
            sheet.getColumnHeader().updateComplete,
            sheet.getRowHeader().updateComplete,
        ]);
        const types: string[] = [];
        host.addEventListener('spreadsheet-event', (e: Event) => types.push((e as CustomEvent).detail.type));

        const targets = [
            sheet.getCellGrid().shadowRoot!.querySelector('.cell.col3.row3')!,
            sheet.getColumnHeader().shadowRoot!.querySelector('.col-header-cell.col3')!,
            sheet.getRowHeader().shadowRoot!.querySelector('.row-header-cell.row3')!,
        ];
        for (const target of targets) {
            const event = new MouseEvent('contextmenu', { bubbles: true, composed: true, cancelable: true });
            target.dispatchEvent(event);
            expect(event.defaultPrevented, target.className).to.be.false;
        }

        expect(types).to.be.empty;
        expect(sheet.getSelection().activeRange).to.deep.equal({ c1: 1, c2: 1, r1: 1, r2: 1 });
        cleanup();
    });

    // ===================================================================
    // Column grouping outline (+/- buttons)
    // ===================================================================
    it('column grouping: setColGroupingData + setColGroupingMax renders toggle buttons', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(8);
        sheet.setColW([50, 50, 50, 50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
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

    it('column grouping: clicking a toggle dispatches groupingCollapsed with flipped state', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(8);
        sheet.setColW([50, 50, 50, 50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
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

    it('column grouping: clicking a collapsed toggle requests EXPAND', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(8);
        sheet.setColW([50, 50, 50, 50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
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

    it('column grouping: toggle click does NOT also fire column-header-click', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(8);
        sheet.setColW([50, 50, 50, 50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
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

    it('column grouping: the gutter above the letters moves the letter row down, in the GWT client\'s sizes', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setColGroupingMax(2);
        sheet.setColGroupingData([]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const firstLetter = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-header-cell'
        ) as HTMLElement;
        expect(firstLetter).to.not.be.null;
        // Two levels and the one more for "expand everything", 18px each,
        // after a 3px margin.
        expect(firstLetter.style.top).to.equal(`${3 + 3 * 18}px`);
        cleanup();
    });

    it('column grouping: inversed keeps the gutter on top and puts the button before the group', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
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
        // The gutter stays above the letters, as in the GWT client.
        expect(firstLetter.style.top).to.equal(`${3 + 2 * 18}px`);
        // The summary column comes before the group: the 12px button is
        // centred over the middle of column A.
        const toggle = sheet.getColumnHeader().shadowRoot!.querySelector(
            '.col-grouping-toggle'
        ) as HTMLButtonElement;
        expect(toggle.style.left).to.equal(`${50 / 2 - 6}px`);
        cleanup();
    });

    it('row grouping: inversed puts the button before the group, as for columns', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setRowGroupingMax(1);
        sheet.setRowGroupingInversed(true);
        sheet.setRowGroupingData([
            { uniqueIndex: 3, level: 1, startIndex: 1, endIndex: 3, collapsed: false },
        ]);
        sheet.recomputeViewport();
        await sheet.getRowHeader().updateComplete;

        // The summary row comes before the group: the 12px button is
        // centred beside the middle of row 1, 15 pt being 20 px.
        const toggle = sheet.getRowHeader().shadowRoot!.querySelector(
            '.row-grouping-toggle'
        ) as HTMLButtonElement;
        expect(toggle.style.top).to.equal(`${20 / 2 - 6}px`);
        cleanup();
    });

    it('row grouping: measures a group past the rows the server sized by the default height', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 300);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        // Rows 1 and 2 are 15 pt, 20 px; past them 30 pt, 40 px.
        sheet.setRowH([15, 15]);
        sheet.setDefRowH(30);
        sheet.setRowGroupingMax(1);
        sheet.setRowGroupingData([
            { uniqueIndex: 4, level: 1, startIndex: 4, endIndex: 5, collapsed: false },
        ]);
        sheet.recomputeViewport();
        await sheet.getRowHeader().updateComplete;

        // Rows 5 and 6 grouped, from 120 px down to 200 px; the 12px button
        // centred beside the middle of row 7 below them.
        const toggle = sheet.getRowHeader().shadowRoot!.querySelector(
            '.row-grouping-toggle'
        ) as HTMLButtonElement;
        expect(toggle.style.top).to.equal(`${200 + 40 / 2 - 6}px`);
        cleanup();
    });

    // ===================================================================
    // `part` attribute scaffolding for documented styling API
    // ===================================================================
    it('cells render with part="cell"', async () => {
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

    it('cells in active range get part="cell cell-range" except the active cell', async () => {
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

    it('column headers render with part="column-header"; cells in selected range get header-selected', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
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

    it('row headers render with part="row-header"; rows in selected range get header-selected', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
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

    it('sheet tabs render with part="tab"; active tab gets tab-selected', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setSheetNames(['One', 'Two', 'Three']);
        sheet.setSheetIndex(2);
        await rendered(sheet);

        const tabs = sheet.getSheetTabs().shadowRoot!.querySelectorAll('.sheet-tab');
        expect(tabs).to.have.length(3);
        expect(tabs[0].getAttribute('part')).to.equal('tab');
        expect(tabs[1].getAttribute('part')).to.equal('tab tab-selected');
        expect(tabs[2].getAttribute('part')).to.equal('tab');
        cleanup();
    });

    it('comment indicators render with part="comment-triangle"', async () => {
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

    it('cell editor input renders with part="cell-input"', async () => {
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

    it('selection overlay carries no part + selection-corner child has part="selection-corner"', async () => {
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
        // cell-range is the range's cells' part, as in the GWT client.
        expect(selection.hasAttribute('part')).to.be.false;
        const corner = selection.querySelector('.selection-corner') as HTMLElement;
        expect(corner).to.not.be.null;
        expect(corner.getAttribute('part')).to.equal('selection-corner');
        cleanup();
    });

    it('popup buttons render with part="popup-button"', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.addPopupButton({ col: 2, row: 2, active: false } as any);
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
    // Honor HIDE_TABSHEET_STYLE on host
    // ===================================================================
    it('sheet tabs hide when host gets the hidetabsheet class', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setSheetNames(['Sheet1', 'Sheet2']);
        sheet.setSheetIndex(1);
        await sheet.getSheetTabs().updateComplete;

        const tabs = sheet.getSheetTabs();
        // Initially visible.
        expect(getComputedStyle(tabs).display).to.not.equal('none');

        // Add hidetabsheet → tabs hidden.
        host.classList.add('hidetabsheet');
        // MutationObserver is async — wait a microtask.
        await Promise.resolve();
        expect(getComputedStyle(tabs).display).to.equal('none');

        // Remove → tabs visible again.
        host.classList.remove('hidetabsheet');
        await Promise.resolve();
        expect(getComputedStyle(tabs).display).to.not.equal('none');
        cleanup();
    });

    it('initial hidetabsheet class on the host is honored at attach time', async () => {
        const host = document.createElement('div');
        host.classList.add('hidetabsheet');
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setSheetNames(['One']);
        sheet.setSheetIndex(1);
        await sheet.getSheetTabs().updateComplete;
        expect(getComputedStyle(sheet.getSheetTabs()).display).to.equal('none');
        sheet.dispose();
        document.body.removeChild(host);
    });

    it('honors a hidetabsheet class given while the sheet is out of the page once it is back', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setSheetNames(['Sheet1', 'Sheet2']);
        sheet.setSheetIndex(1);
        await sheet.getSheetTabs().updateComplete;
        // As the element does, leaving the page and coming back.
        sheet.disconnect();
        host.remove();

        host.classList.add('hidetabsheet');
        await Promise.resolve();
        document.body.appendChild(host);
        sheet.connect();
        await Promise.resolve();

        expect(getComputedStyle(sheet.getSheetTabs()).display).to.equal('none');
        cleanup();
    });

    it('watches the host classes no more once disposed', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setSheetNames(['Sheet1', 'Sheet2']);
        sheet.setSheetIndex(1);
        await sheet.getSheetTabs().updateComplete;
        sheet.dispose();

        host.classList.add('hidetabsheet');
        await Promise.resolve();

        expect(getComputedStyle(sheet.getSheetTabs()).display).to.not.equal('none');
        cleanup();
    });

    // ===================================================================
    // Formula bar implementation
    // ===================================================================
    it('formula bar renders two inputs with documented parts', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        await rendered(sheet);
        const fb = sheet.getFormulaBar().shadowRoot!;
        const addr = fb.querySelector('input.address') as HTMLInputElement;
        const formula = fb.querySelector('input.formula') as HTMLInputElement;
        expect(addr).to.not.be.null;
        expect(formula).to.not.be.null;
        expect(addr.getAttribute('part')).to.equal('address-field');
        expect(formula.getAttribute('part')).to.equal('formula-field');
        cleanup();
    });

    it('updateFormulaBar sets the address-field value', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.setSelectedCellAndRange(null, 2, 7, 2, 2, 7, 7, false);
        sheet.updateFormulaBar('B7', 2, 7);
        await rendered(sheet);
        const addr = sheet.getFormulaBar().shadowRoot!.querySelector(
            'input.address'
        ) as HTMLInputElement;
        expect(addr.value).to.equal('B7');
        cleanup();
    });

    it('showSelectedCell sets both address + formula fields', async () => {
        const { sheet, cleanup } = attachSheetForLifecycle(400, 200);
        // Formula without its leading '=', as the server sends it.
        sheet.showSelectedCell('C3', 3, 3, 'A1+B2', true, false, false);
        await rendered(sheet);
        const fb = sheet.getFormulaBar().shadowRoot!;
        const addr = fb.querySelector('input.address') as HTMLInputElement;
        const formula = fb.querySelector('input.formula') as HTMLInputElement;
        expect(addr.value).to.equal('C3');
        expect(formula.value).to.equal('=A1+B2');
        cleanup();
    });

    it('pressing Enter in the address field dispatches sheetAddressChanged', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        sheet.updateFormulaBar('A1', 1, 1);
        await rendered(sheet);

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

    it('formula bar hides when host gets the hidefunctionbar class', async () => {
        const { host, sheet, cleanup } = attachSheetForLifecycle(400, 200);
        await rendered(sheet);
        expect(getComputedStyle(sheet.getFormulaBar()).display).to.not.equal('none');
        host.classList.add('hidefunctionbar');
        await Promise.resolve();
        expect(getComputedStyle(sheet.getFormulaBar()).display).to.equal('none');
        host.classList.remove('hidefunctionbar');
        await Promise.resolve();
        expect(getComputedStyle(sheet.getFormulaBar()).display).to.not.equal('none');
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

describe('Spreadsheet — how tall it is when nobody says', () => {
    /**
     * A spreadsheet in a host with no height of its own used to grow to
     * the height of the whole sheet: thousands of pixels of page pushed
     * down by a component nobody asked to be that big. The GWT client
     * kept its panes out of the flow and put a 100px floor under the
     * whole thing.
     */
    function attach(
        hostStyle: string
    ): { host: HTMLElement; sheet: Spreadsheet; cleanup: () => void } {
        const host = document.createElement('div');
        host.style.cssText = hostStyle;
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setCols(20);
        sheet.setRows(200);
        sheet.setColW(new Array(20).fill(50));
        sheet.setRowH(new Array(200).fill(15));
        sheet.recomputeViewport();
        return {
            host,
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    // What <vaadin-spreadsheet> is, minus a height: its :host is a flex
    // column at height:100%, which against an auto-height parent is auto.
    const HOST = 'width:400px;display:flex;flex-direction:column;';

    it('does not grow to the height of the sheet', () => {
        const ctx = attach(HOST);
        expect(ctx.host.getBoundingClientRect().height).to.be.below(200);
        ctx.cleanup();
    });

    it('stands 100px tall rather than collapsing to its bars', () => {
        const ctx = attach(HOST);
        expect(ctx.host.getBoundingClientRect().height).to.equal(100);
        ctx.cleanup();
    });

    it('leaves the height the application gave the component alone', () => {
        // The wrapper asks the connector for 100% as it starts up. That
        // used to land on the host and throw away setHeight("250px").
        const ctx = attach(HOST + 'height:250px;');
        ctx.sheet.setHeight('100%');
        ctx.sheet.setWidth('100%');

        expect(ctx.host.style.height).to.equal('250px');
        expect(Math.round(ctx.host.getBoundingClientRect().height)).to.equal(
            250
        );
        ctx.cleanup();
    });

    it('fills a host that does have a height', () => {
        const ctx = attach(HOST + 'height:500px;');
        const scroller = ctx.sheet.getScrollContainer();
        // Everything left over after the formula bar and the sheet tabs.
        expect(scroller.getBoundingClientRect().height).to.be.above(300);
        expect(scroller.getBoundingClientRect().height).to.be.below(500);
        ctx.cleanup();
    });
});

/**
 * A workbook can hide its row and column headers and still group its
 * rows and columns. The outline gutter lives in the header strips, and
 * skipping those strips whenever the headers have no size took the
 * outline with them: a grouped sheet without headers had no way to
 * collapse or expand anything.
 */
describe('Spreadsheet — grouping outline without headers', () => {
    function attach(): { sheet: Spreadsheet; cleanup: () => void } {
        const host = document.createElement('div');
        host.style.cssText =
            'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setRows(20);
        sheet.setCols(8);
        sheet.setColW(new Array(8).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        // No headers at all, which is what the workbook asked for.
        sheet.setDisplayRowColHeadings(false);
        return {
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    it('still draws the column outline', async () => {
        const { sheet, cleanup } = attach();
        sheet.setColGroupingMax(1);
        sheet.setColGroupingData([
            {
                uniqueIndex: 1,
                level: 1,
                startIndex: 1,
                endIndex: 3,
                collapsed: false,
            },
        ]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        expect(
            sheet
                .getColumnHeader()
                .shadowRoot!.querySelectorAll('.col-grouping-toggle')
        ).to.have.length(1);
        cleanup();
    });

    it('still draws the row outline', async () => {
        const { sheet, cleanup } = attach();
        sheet.setRowGroupingMax(1);
        sheet.setRowGroupingData([
            {
                uniqueIndex: 1,
                level: 1,
                startIndex: 1,
                endIndex: 3,
                collapsed: false,
            },
        ]);
        sheet.recomputeViewport();
        await sheet.getRowHeader().updateComplete;

        expect(
            sheet
                .getRowHeader()
                .shadowRoot!.querySelectorAll('.row-grouping-toggle')
        ).to.have.length(1);
        cleanup();
    });

    it('draws no header letters, only the outline', async () => {
        const { sheet, cleanup } = attach();
        sheet.setColGroupingMax(1);
        sheet.setColGroupingData([
            {
                uniqueIndex: 1,
                level: 1,
                startIndex: 1,
                endIndex: 3,
                collapsed: false,
            },
        ]);
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;

        const letters = sheet
            .getColumnHeader()
            .shadowRoot!.querySelectorAll<HTMLElement>('.col-header-cell');
        for (const letter of letters) {
            expect(letter.getBoundingClientRect().height).to.equal(0);
        }
        cleanup();
    });

    it('stays out of the way when there is no grouping either', async () => {
        const { sheet, cleanup } = attach();
        sheet.recomputeViewport();
        await sheet.getColumnHeader().updateComplete;
        await sheet.getRowHeader().updateComplete;

        expect(sheet.getColumnHeader().shadowRoot!.children).to.have.length(0);
        expect(sheet.getRowHeader().shadowRoot!.children).to.have.length(0);
        cleanup();
    });
});

/**
 * The numbered squares Excel puts above and beside the outline gutters.
 * Clicking one shows the sheet down to that level: everything shallower
 * expanded, that level collapsed. The connector declared the event and
 * nothing ever sent it, so there was no way to reach the feature.
 */
describe('Spreadsheet — outline level buttons', () => {
    function attach(): {
        sheet: Spreadsheet;
        events: any[];
        cleanup: () => void;
    } {
        const host = document.createElement('div');
        host.style.cssText =
            'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setRows(20);
        sheet.setCols(8);
        sheet.setColW(new Array(8).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        const events: any[] = [];
        host.addEventListener('spreadsheet-event', (e) =>
            events.push((e as CustomEvent).detail)
        );
        return {
            sheet,
            events,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    async function buttons(sheet: Spreadsheet): Promise<HTMLElement[]> {
        const corner = sheet.getHeaderCorner() as any;
        await corner.updateComplete;
        return Array.from(
            corner.shadowRoot!.querySelectorAll<HTMLElement>('.level-header')
        );
    }

    it('draws no buttons on a sheet with no groups', async () => {
        const ctx = attach();
        ctx.sheet.recomputeViewport();

        expect(await buttons(ctx.sheet)).to.be.empty;
        ctx.cleanup();
    });

    it('draws one button per column outline level, and one more', async () => {
        // As the GWT client and Excel do: the last opens every group.
        const ctx = attach();
        ctx.sheet.setColGroupingMax(2);
        ctx.sheet.recomputeViewport();

        const labels = (await buttons(ctx.sheet)).map((b) =>
            b.textContent!.trim()
        );
        expect(labels).to.eql(['1', '2', '3']);
        ctx.cleanup();
    });

    it('asks for the level past the deepest from the last button', async () => {
        const ctx = attach();
        ctx.sheet.setColGroupingMax(2);
        ctx.sheet.recomputeViewport();
        const all = await buttons(ctx.sheet);
        ctx.events.length = 0;

        all[all.length - 1].click();

        expect(ctx.events[0].data).to.eql({ cols: true, level: 3 });
        ctx.cleanup();
    });

    it('draws the buttons as plain numbers, as the GWT client did', async () => {
        const ctx = attach();
        ctx.sheet.setColGroupingMax(1);
        ctx.sheet.recomputeViewport();

        const style = getComputedStyle((await buttons(ctx.sheet))[0]);
        expect(style.borderTopStyle).to.equal('none');
        expect(style.backgroundColor).to.equal('rgba(0, 0, 0, 0)');
        expect(`${style.fontSize} ${style.color}`).to.equal('11px rgb(72, 72, 72)');
        ctx.cleanup();
    });

    it('leaves the extra level out of the gutters while the headings are hidden', async () => {
        // As the GWT client did: with nowhere to put the button that opens
        // every group, there is no level for it either.
        const ctx = attach();
        ctx.sheet.setColGroupingMax(1);
        ctx.sheet.setRowGroupingMax(1);
        ctx.sheet.setDisplayRowColHeadings(false);
        ctx.sheet.recomputeViewport();

        expect(ctx.sheet.getRowHeader().style.width).to.equal(`${1 + 15}px`);
        expect(ctx.sheet.getColumnHeader().style.height).to.equal(`${3 + 18}px`);
        ctx.cleanup();
    });

    it('draws no buttons while the headings are hidden', async () => {
        const ctx = attach();
        ctx.sheet.setColGroupingMax(2);
        ctx.sheet.setDisplayRowColHeadings(false);
        ctx.sheet.recomputeViewport();

        expect(await buttons(ctx.sheet)).to.be.empty;
        ctx.cleanup();
    });

    it('draws the row levels too, on their own axis', async () => {
        const ctx = attach();
        ctx.sheet.setColGroupingMax(1);
        ctx.sheet.setRowGroupingMax(2);
        ctx.sheet.recomputeViewport();

        const all = await buttons(ctx.sheet);
        expect(all.map((b) => b.dataset.axis)).to.eql(['cols', 'cols', 'rows', 'rows', 'rows']);
        ctx.cleanup();
    });

    it('lines each column button up with its gutter row', async () => {
        const ctx = attach();
        ctx.sheet.setColGroupingMax(2);
        ctx.sheet.recomputeViewport();

        const all = await buttons(ctx.sheet);
        const corner = ctx.sheet.getHeaderCorner().getBoundingClientRect().top;
        expect(all.map((b) => b.getBoundingClientRect().top - corner)).to.eql([0, 18, 36]);
        ctx.cleanup();
    });

    it('tells the server which level was asked for', async () => {
        const ctx = attach();
        ctx.sheet.setColGroupingMax(2);
        ctx.sheet.recomputeViewport();
        const all = await buttons(ctx.sheet);
        ctx.events.length = 0;

        all[1].click();

        expect(ctx.events).to.have.length(1);
        expect(ctx.events[0].type).to.equal('levelHeaderClicked');
        expect(ctx.events[0].data).to.eql({ cols: true, level: 2 });
        ctx.cleanup();
    });

    it('says it was a row level when a row button is clicked', async () => {
        const ctx = attach();
        ctx.sheet.setRowGroupingMax(1);
        ctx.sheet.recomputeViewport();
        const all = await buttons(ctx.sheet);
        ctx.events.length = 0;

        all[0].click();

        expect(ctx.events[0].data).to.eql({ cols: false, level: 1 });
        ctx.cleanup();
    });

    it('grows the corner over the gutters so the buttons have room', async () => {
        const ctx = attach();
        ctx.sheet.setColGroupingMax(2);
        ctx.sheet.setRowGroupingMax(1);
        ctx.sheet.recomputeViewport();

        // The GWT client's gutters: a 15px column per row level and one
        // more, after a pixel; an 18px row per column level and one more,
        // after three.
        expect(ctx.sheet.getHeaderCorner().style.width).to.equal(`${50 + 1 + 2 * 15}px`);
        expect(ctx.sheet.getHeaderCorner().style.height).to.equal(`${27 + 3 + 3 * 18}px`);
        ctx.cleanup();
    });
});
