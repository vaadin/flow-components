/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { toKey } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/CellKeys.js';
import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import { SpreadsheetColumnHeader } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetColumnHeader.js';
import {
  emptyContextMenuRenderState,
  SpreadsheetContextMenu
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetContextMenu.js';
import { SpreadsheetFormulaBar } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetFormulaBar.js';
import { SpreadsheetHeaderCorner } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetHeaderCorner.js';
import { SpreadsheetRowHeader } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetRowHeader.js';
import {
  emptySheetTabsRenderState,
  SpreadsheetSheetTabs
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetSheetTabs.js';
import {
  type SheetParts,
  SheetRenderer
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/render/SheetRenderer.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';

/**
 * Drawing the sheet, on its own: the state the cell grid is handed, the
 * strips and the corner, the scroll a sheet shown again goes back to, a row
 * kept in place, and bringing a cell into view.
 */
describe('SheetRenderer', () => {
  /** A view of 300 by 200 pixels onto a sheet of 20 rows of 20 pixels and 10 columns of 64. */
  const VIEW = { scrollLeft: 0, scrollTop: 0, viewportWidth: 300, viewportHeight: 200 };
  let store: SheetStore;
  let parts: SheetParts;
  let calls: string[];
  let placed: unknown[];
  let renderer: SheetRenderer;

  beforeEach(() => {
    store = new SheetStore(() => {});
    store.write('rows', 20);
    store.write('cols', 10);
    const scrollContainer = document.createElement('div');
    scrollContainer.style.cssText = 'width:300px;height:200px;overflow:auto;position:relative;';
    parts = {
      scrollContainer,
      cellGrid: new SpreadsheetCellGrid(),
      columnHeader: new SpreadsheetColumnHeader(),
      rowHeader: new SpreadsheetRowHeader(),
      headerCorner: new SpreadsheetHeaderCorner(),
      formulaBar: new SpreadsheetFormulaBar(),
      sheetTabs: new SpreadsheetSheetTabs(),
      contextMenu: new SpreadsheetContextMenu()
    };
    scrollContainer.append(parts.cellGrid);
    document.body.append(scrollContainer);
    calls = [];
    placed = [];
    renderer = new SheetRenderer(store, parts, {
      drawingStarts: () => calls.push('drawing starts'),
      drawn: () => calls.push('drawn'),
      sheetName: () => 'Sheet1',
      tabs: () => emptySheetTabsRenderState(),
      menu: () => emptyContextMenuRenderState(),
      menuDrawn: () => calls.push('menu drawn'),
      placeHosted: (drawn) => placed.push(drawn),
      popupButtons: () => []
    });
  });

  afterEach(() => {
    parts.scrollContainer.remove();
  });

  describe('the state the cell grid is drawn from', () => {
    it('lays out the cells on screen, from the top left of the sheet', () => {
      const state = renderer.build(VIEW);

      expect([state.viewport.firstColumnIndex, state.viewport.firstRowIndex]).to.eql([1, 1]);
      expect(state.colX.slice(0, 2)).to.eql([0, 64]);
      expect(state.rowY.slice(0, 2)).to.eql([0, 20]);
      expect([state.totalGridWidthPx, state.totalGridHeightPx]).to.eql([640, 400]);
      expect(state.selection).to.equal(store.selection);
      expect(state.cellData).to.equal(store.sheetData.cells);
    });

    it('starts the scrolling columns past the frozen ones and the freeze line', () => {
      store.write('horizontalSplitPosition', 2);

      const state = renderer.build(VIEW);

      expect(state.viewport.firstColumnIndex).to.equal(3);
      expect(state.colX[0]).to.equal(129);
      expect(state.frozenColWidths).to.eql([64, 64]);
    });
  });

  describe('a drawing', () => {
    it('hands the grid its state and size, draws the strips and the corner, and the tabs, the menu and the hosted components', () => {
      renderer.apply(VIEW);

      const { cellGrid, columnHeader, rowHeader, headerCorner } = parts;
      expect(cellGrid.renderState.totalGridWidthPx).to.equal(640);
      expect([cellGrid.style.width, cellGrid.style.height]).to.eql(['640px', '400px']);
      expect([columnHeader.hidden, rowHeader.hidden, headerCorner.hidden]).to.eql([false, false, false]);
      expect([headerCorner.style.width, headerCorner.style.height]).to.eql(['50px', '27px']);
      expect(columnHeader.style.height).to.equal('27px');
      expect(calls).to.eql(['menu drawn']);
      expect(placed).to.eql([cellGrid.renderState]);
    });

    it('leaves the strips and the corner out while the server hides the headings', () => {
      store.write('displayRowColHeadings', false);

      renderer.apply(VIEW);

      expect([parts.columnHeader.hidden, parts.rowHeader.hidden, parts.headerCorner.hidden]).to.eql([true, true, true]);
    });

    it('starts, draws the sheet where it is scrolled, and tells the server, in that order', () => {
      renderer.recompute();

      expect(calls).to.eql(['drawing starts', 'menu drawn', 'drawn']);
      expect(parts.cellGrid.renderState.viewport.firstRowIndex).to.equal(1);
    });

    it('leaves the headers and frozen panes out of the view the browser pages', () => {
      store.write('verticalSplitPosition', 2);

      renderer.apply(VIEW);

      // The corner's 27 pixels, two frozen rows of 20, the freeze line, less
      // the pixel the tabs below take back.
      expect(parts.scrollContainer.style.scrollPaddingTop).to.equal('67px');
      expect(parts.scrollContainer.style.scrollPaddingLeft).to.equal('50px');
    });
  });

  describe('a sheet shown', () => {
    it('goes back to where a sheet shown again was scrolled', () => {
      renderer.sheetShown('One', { top: 0, left: 0 }, false);
      renderer.recompute();

      renderer.sheetShown('Two', { top: 100, left: 64 }, true);
      renderer.recompute();

      expect([parts.scrollContainer.scrollTop, parts.scrollContainer.scrollLeft]).to.eql([100, 64]);
      expect(parts.scrollContainer.getAttribute('aria-label')).to.equal('Two');
    });

    it('stays where it is when the sheet shown gets another index without changing', () => {
      renderer.sheetShown('One', { top: 0, left: 0 }, false);
      renderer.recompute();
      parts.scrollContainer.scrollTop = 40;

      renderer.sheetShown('One', { top: 100, left: 0 }, true);
      renderer.recompute();

      expect(parts.scrollContainer.scrollTop).to.equal(40);
    });

    it('is named for what it is in the page, a sheet of no name as a spreadsheet', () => {
      renderer.sheetShown(null, { top: 0, left: 0 }, false);

      expect(parts.scrollContainer.getAttribute('aria-label')).to.equal('Spreadsheet');
    });
  });

  describe('scrolling', () => {
    it('says which cells the grid draws, none before anything is measured', () => {
      expect(renderer.drawnRange()).to.equal(null);

      renderer.apply(VIEW);

      expect(renderer.drawnRange()).to.include({ r1: 1, c1: 1 });
    });

    it('brings a cell below the view into it, and scrolls nothing on a sheet with no cells', () => {
      renderer.apply(VIEW);

      renderer.scrollCellIntoView({ col: 1, row: 18 });
      const { scrollTop } = parts.scrollContainer;
      expect(scrollTop <= 17 * 20 && scrollTop + 200 >= 18 * 20, `row 18 in view at ${scrollTop}`).to.be.true;

      parts.scrollContainer.scrollTop = 0;
      store.write('cols', 0);
      renderer.scrollCellIntoView({ col: 1, row: 18 });
      expect(parts.scrollContainer.scrollTop).to.equal(0);
    });

    it('keeps the first row on screen where it is when rows above it change height, and knows that scroll for its own', () => {
      renderer.apply(VIEW);
      parts.scrollContainer.scrollTop = 100;
      expect(renderer.scrolled(), "the user's scroll").to.be.true;

      renderer.holdRowInPlace();
      // Rows 1 to 5 grow from 15 to 22.5 points: 30 pixels each.
      store.write('rowH', [22.5, 22.5, 22.5, 22.5, 22.5]);
      renderer.recompute();

      expect(parts.scrollContainer.scrollTop).to.equal(150);
      expect(renderer.scrolled(), "the sheet's own").to.be.false;
    });

    it("lets the user's own scroll end keeping a row in place", () => {
      renderer.apply(VIEW);
      parts.scrollContainer.scrollTop = 100;
      renderer.holdRowInPlace();
      parts.scrollContainer.scrollTop = 140;
      expect(renderer.scrolled()).to.be.true;

      store.write('rowH', [22.5, 22.5, 22.5, 22.5, 22.5]);
      renderer.recompute();

      expect(parts.scrollContainer.scrollTop).to.equal(140);
    });

    it('forgets the row to keep when told the rows are another sheet', () => {
      renderer.apply(VIEW);
      parts.scrollContainer.scrollTop = 100;
      renderer.holdRowInPlace();
      store.write('rowH', [22.5, 22.5, 22.5, 22.5, 22.5]);

      renderer.forgetRowAnchor();
      renderer.recompute();

      expect(parts.scrollContainer.scrollTop).to.equal(100);
    });

    it('gives the scrolling pane on screen, past the frozen columns and the freeze line', () => {
      store.write('horizontalSplitPosition', 1);
      renderer.apply(VIEW);

      const box = parts.scrollContainer.getBoundingClientRect();
      const pane = renderer.scrollingPaneClientRect();

      expect(pane.left).to.equal(box.left + parts.scrollContainer.clientLeft + 64 + 1);
      expect(pane.bottom).to.equal(box.bottom);
    });

    it('gives where the cells are on screen, and the pane of those that scroll', () => {
      store.write('horizontalSplitPosition', 1);
      store.write('verticalSplitPosition', 2);
      renderer.apply(VIEW);
      const container = parts.scrollContainer;
      const box = container.getBoundingClientRect();
      const left = box.left + container.clientLeft;
      const top = box.top + container.clientTop;

      const { cells, scrolling } = renderer.cellPanes();

      expect(cells).to.eql({ left, top, right: left + container.clientWidth, bottom: top + container.clientHeight });
      // One frozen 64px column and two frozen 20px rows, and the freeze line's pixel past each.
      expect(scrolling).to.eql({ ...cells, left: left + 65, top: top + 41 });
    });

    it('leaves the border and the scroll bars out of the cells', () => {
      parts.scrollContainer.style.border = '3px solid black';
      renderer.apply(VIEW);
      const container = parts.scrollContainer;
      const box = container.getBoundingClientRect();

      const { cells } = renderer.cellPanes();

      expect(cells.right).to.equal(box.left + 3 + container.clientWidth);
      expect(cells.bottom).to.equal(box.top + 3 + container.clientHeight);
      expect(cells.right).to.be.below(box.right);
    });

    it('finds the cell at a point, on the frozen cells or the ones scrolled', () => {
      store.write('horizontalSplitPosition', 1);
      store.write('verticalSplitPosition', 2);
      renderer.apply(VIEW);
      parts.scrollContainer.scrollTop = 100;
      const { cells, scrolling } = renderer.cellPanes();

      expect(renderer.cellAt(cells.left + 10, cells.top + 10), 'frozen').to.eql({ col: 1, row: 1 });
      // 5px into the scrolling cells, scrolled 100px down: 105px past row 3's top, in row 8.
      expect(renderer.cellAt(scrolling.left + 5, scrolling.top + 5), 'scrolled').to.eql({ col: 2, row: 8 });
      expect(renderer.cellAt(cells.right + 500, cells.bottom + 500), 'past the sheet').to.eql({ col: 10, row: 20 });
    });
  });

  it('reads the resources from the element they are published on, and none before', () => {
    store.write('resources', ['picture', 'gone']);
    expect(renderer.resources()).to.eql({});

    const host = document.createElement('div');
    host.setAttribute('resource-picture', 'https://example.com/picture.png');
    renderer.useResourcesOf(host);

    expect(renderer.resources()).to.eql({ picture: 'https://example.com/picture.png' });
  });

  it("gives the component each cell shows, from the server's map", () => {
    store.write('componentIDtoCellKeysMap', { '5': toKey(2, 3) });

    expect(renderer.cellSlotAssignments()[toKey(2, 3)]).to.include({ nodeId: '5', kind: 'component' });
  });
});
