/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import {
  cellBoxOnGrid,
  columnWidthOf,
  drawnCellBox,
  frozenExtentOf,
  quadrantOf,
  quadrants
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/GridGeometry.js';
import {
  type CellGridRenderState,
  emptyCellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';

/**
 * Where the cell grid draws things, on its own: the panes, the cells they
 * show, the box of a cell on screen, and the width of a column.
 */
describe('GridGeometry', () => {
  /**
   * Columns 3 to 5 of 60 pixels and rows 20 to 22 of 20 scrolled into view,
   * past two frozen columns of 50 and 70 and one frozen row of 30, the view
   * scrolled 300 across and 400 down.
   */
  function frozenSheet(overrides: Partial<CellGridRenderState> = {}): CellGridRenderState {
    const empty = emptyCellGridRenderState();
    return {
      ...empty,
      viewport: { ...empty.viewport, firstColumnIndex: 3, lastColumnIndex: 5, firstRowIndex: 20, lastRowIndex: 22 },
      colX: [121, 181, 241],
      colWidths: [60, 60, 60],
      rowY: [431, 451, 471],
      rowHeights: [20, 20, 20],
      horizontalSplitPosition: 2,
      verticalSplitPosition: 1,
      frozenColX: [0, 50],
      frozenColWidths: [50, 70],
      frozenRowY: [0],
      frozenRowHeights: [30],
      scrollLeft: 300,
      scrollTop: 400,
      ...overrides
    };
  }

  function unfrozen(): CellGridRenderState {
    return frozenSheet({ horizontalSplitPosition: 0, verticalSplitPosition: 0 });
  }

  describe('the panes', () => {
    it('are the scrolling pane alone with nothing frozen, reading the arrays of the cells in view', () => {
      const [main, ...others] = quadrants(unfrozen());

      expect(others).to.eql([]);
      expect([main.quadrantName, main.colStart, main.colEnd, main.rowStart, main.rowEnd]).to.eql(['main', 3, 5, 20, 22]);
      expect([main.getColLeft(4), main.getColWidth(4), main.getRowTop(21), main.getRowHeight(21)]).to.eql([
        181, 60, 451, 20
      ]);
    });

    it('add the frozen rows, the frozen columns and the corner, each reading the frozen arrays from 1 on', () => {
      const panes = quadrants(frozenSheet());

      expect(panes.map((pane) => [pane.quadrantName, pane.colStart, pane.colEnd, pane.rowStart, pane.rowEnd])).to.eql([
        ['main', 3, 5, 20, 22],
        ['top', 3, 5, 1, 1],
        ['left', 1, 2, 20, 22],
        ['corner', 1, 2, 1, 1]
      ]);
      const [, top, left, corner] = panes;
      expect([top.getColLeft(3), top.getRowTop(1), top.getRowHeight(1)]).to.eql([121, 0, 30]);
      expect([left.getColLeft(2), left.getColWidth(2), left.getRowTop(20)]).to.eql([50, 70, 431]);
      expect([corner.getColLeft(2), corner.getRowHeight(1)]).to.eql([50, 30]);
    });

    it('add only the side that is frozen', () => {
      expect(quadrants(frozenSheet({ horizontalSplitPosition: 0 })).map((pane) => pane.quadrantName)).to.eql([
        'main',
        'top'
      ]);
      expect(quadrants(frozenSheet({ verticalSplitPosition: 0 })).map((pane) => pane.quadrantName)).to.eql([
        'main',
        'left'
      ]);
    });

    it('place a column or row they have no size for at 0', () => {
      const [main] = quadrants(unfrozen());

      expect([main.getColLeft(9), main.getColWidth(9), main.getRowTop(99), main.getRowHeight(99)]).to.eql([0, 0, 0, 0]);
    });

    it('all share the state they are drawn from', () => {
      const state = frozenSheet();

      expect(quadrants(state).every((pane) => pane.state === state)).to.be.true;
    });

    it('are none before anything is measured', () => {
      expect(quadrants(emptyCellGridRenderState())).to.eql([]);
    });
  });

  it('puts a cell in the pane on its side of each freeze, the last frozen row and column included', () => {
    expect(quadrantOf(3, 5, 2, 1)).to.equal('main');
    expect(quadrantOf(3, 1, 2, 1)).to.equal('top');
    expect(quadrantOf(2, 5, 2, 1)).to.equal('left');
    expect(quadrantOf(2, 1, 2, 1)).to.equal('corner');
    expect(quadrantOf(1, 1, 0, 0)).to.equal('main');
  });

  it('measures the frozen panes to the far edge of the last frozen column and row, and nothing unfrozen', () => {
    expect(frozenExtentOf(frozenSheet())).to.eql({ width: 120, height: 30 });
    expect(frozenExtentOf(unfrozen())).to.eql({ width: 0, height: 0 });
  });

  describe('the box of a cell on screen', () => {
    it('is where its column and row are drawn, for a cell scrolled into view', () => {
      expect(cellBoxOnGrid(frozenSheet(), 4, 21)).to.eql({ left: 181, top: 451, width: 60, height: 20 });
    });

    it('moves a frozen cell with the scroll, as its pane does', () => {
      expect(cellBoxOnGrid(frozenSheet(), 2, 1)).to.eql({ left: 350, top: 400, width: 70, height: 30 });
    });

    it('is none for a cell out of view across or down', () => {
      expect(cellBoxOnGrid(frozenSheet(), 6, 21)).to.equal(null);
      expect(cellBoxOnGrid(frozenSheet(), 4, 23)).to.equal(null);
      expect(cellBoxOnGrid(frozenSheet(), 4, 2)).to.equal(null);
    });

    it("is as big as the whole merged cell for the cell a merge starts at, and the cell's own for the others", () => {
      // A merge in the frozen corner, its box in its pane's coordinates: on
      // screen it moves with the scroll, as the cell it starts at does.
      const state = frozenSheet({
        mergedRegions: [{ col1: 2, row1: 1, col2: 3, row2: 1 }],
        mergedRegionBoxes: [{ left: 50, top: 0, width: 130, height: 30 }]
      });

      expect(drawnCellBox(state, 2, 1)).to.eql({ left: 350, top: 400, width: 130, height: 30 });
      expect(drawnCellBox(state, 3, 1)).to.eql({ left: 121, top: 400, width: 60, height: 30 });
      expect(drawnCellBox(state, 6, 1)).to.equal(null);
    });
  });

  it("gives a column's width, frozen or in view, and 0 for one not drawn", () => {
    expect(columnWidthOf(frozenSheet(), 2)).to.equal(70);
    expect(columnWidthOf(frozenSheet(), 5)).to.equal(60);
    expect(columnWidthOf(frozenSheet(), 9)).to.equal(0);
  });
});
