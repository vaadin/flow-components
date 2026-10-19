/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { CellGridRenderState } from '../types.js';

/**
 * The panes the cells are drawn in: the scrolling one, the frozen rows above
 * it, the frozen columns beside it, and the corner where both are frozen.
 */
export type QuadrantName = 'main' | 'top' | 'left' | 'corner';

/** A box on the grid, in pixels. */
export interface GridBox {
  left: number;
  top: number;
  width: number;
  height: number;
}

/**
 * One pane: the columns and rows it shows, and where each of them is drawn
 * in the pane's own coordinates. The scrolling pane reads the arrays of the
 * cells in view, a frozen pane those of the frozen rows or columns.
 */
export interface QuadrantRenderOpts {
  state: CellGridRenderState;
  quadrantName: QuadrantName;
  colStart: number;
  colEnd: number;
  rowStart: number;
  rowEnd: number;
  getColLeft: (col: number) => number;
  getColWidth: (col: number) => number;
  getRowTop: (row: number) => number;
  getRowHeight: (row: number) => number;
}

type ColumnAxis = Pick<QuadrantRenderOpts, 'colStart' | 'colEnd' | 'getColLeft' | 'getColWidth'>;
type RowAxis = Pick<QuadrantRenderOpts, 'rowStart' | 'rowEnd' | 'getRowTop' | 'getRowHeight'>;

/**
 * The panes drawn, the scrolling one first and then the frozen ones there
 * are, in the order they stack: the frozen rows, the frozen columns, and the
 * corner over both. None while nothing is measured.
 */
export function quadrants(state: CellGridRenderState): QuadrantRenderOpts[] {
  const { viewport, verticalSplitPosition, horizontalSplitPosition } = state;
  if (viewport.lastColumnIndex < viewport.firstColumnIndex || viewport.lastRowIndex < viewport.firstRowIndex) {
    return [];
  }
  const firstCol = viewport.firstColumnIndex;
  const firstRow = viewport.firstRowIndex;
  const scrollingCols: ColumnAxis = {
    colStart: firstCol,
    colEnd: viewport.lastColumnIndex,
    getColLeft: (col) => state.colX[col - firstCol] ?? 0,
    getColWidth: (col) => state.colWidths[col - firstCol] ?? 0
  };
  const frozenCols: ColumnAxis = {
    colStart: 1,
    colEnd: horizontalSplitPosition,
    getColLeft: (col) => state.frozenColX[col - 1] ?? 0,
    getColWidth: (col) => state.frozenColWidths[col - 1] ?? 0
  };
  const scrollingRows: RowAxis = {
    rowStart: firstRow,
    rowEnd: viewport.lastRowIndex,
    getRowTop: (row) => state.rowY[row - firstRow] ?? 0,
    getRowHeight: (row) => state.rowHeights[row - firstRow] ?? 0
  };
  const frozenRows: RowAxis = {
    rowStart: 1,
    rowEnd: verticalSplitPosition,
    getRowTop: (row) => state.frozenRowY[row - 1] ?? 0,
    getRowHeight: (row) => state.frozenRowHeights[row - 1] ?? 0
  };
  const panes: QuadrantRenderOpts[] = [{ state, quadrantName: 'main', ...scrollingCols, ...scrollingRows }];
  if (verticalSplitPosition > 0) {
    panes.push({ state, quadrantName: 'top', ...scrollingCols, ...frozenRows });
  }
  if (horizontalSplitPosition > 0) {
    panes.push({ state, quadrantName: 'left', ...frozenCols, ...scrollingRows });
  }
  if (verticalSplitPosition > 0 && horizontalSplitPosition > 0) {
    panes.push({ state, quadrantName: 'corner', ...frozenCols, ...frozenRows });
  }
  return panes;
}

/**
 * The cell ranges the grid draws: the scrolling pane's, and those of the
 * frozen panes there are. 1-based and inclusive; empty while nothing is
 * measured.
 */
export function drawnRanges(state: CellGridRenderState): Array<{ c1: number; c2: number; r1: number; r2: number }> {
  return quadrants(state).map((pane) => ({ c1: pane.colStart, c2: pane.colEnd, r1: pane.rowStart, r2: pane.rowEnd }));
}

/** The pane a cell is drawn in, by which side of each freeze it lies. */
export function quadrantOf(col: number, row: number, frozenCols: number, frozenRows: number): QuadrantName {
  const top = row <= frozenRows;
  const left = col <= frozenCols;
  if (top) {
    return left ? 'corner' : 'top';
  }
  return left ? 'left' : 'main';
}

/** How far the frozen columns reach across and the frozen rows down: the far edge of the last of each. */
export function frozenExtentOf(state: CellGridRenderState): { width: number; height: number } {
  return {
    width: farEdge(state.frozenColX, state.frozenColWidths, state.horizontalSplitPosition),
    height: farEdge(state.frozenRowY, state.frozenRowHeights, state.verticalSplitPosition)
  };
}

/** Where the first `count` rows or columns end: the far edge of the last one. */
function farEdge(starts: ReadonlyArray<number>, sizes: ReadonlyArray<number>, count: number): number {
  return count > 0 ? (starts[count - 1] ?? 0) + (sizes[count - 1] ?? 0) : 0;
}

/**
 * Where a cell is on screen, in the grid's own coordinates, or null when it
 * is not drawn. A frozen cell sits in its pane's wrapper, which the scroll
 * offset moves along with the view, so its box moves with it: something at
 * grid level placed by the frozen arrays alone is left behind on a scroll.
 */
export function cellBoxOnGrid(state: CellGridRenderState, col: number, row: number): GridBox | null {
  const { viewport, horizontalSplitPosition, verticalSplitPosition } = state;
  let left: number;
  let width: number;
  if (col >= 1 && col <= horizontalSplitPosition) {
    left = (state.frozenColX[col - 1] ?? 0) + state.scrollLeft;
    width = state.frozenColWidths[col - 1] ?? 0;
  } else if (col >= viewport.firstColumnIndex && col <= viewport.lastColumnIndex) {
    left = state.colX[col - viewport.firstColumnIndex] ?? 0;
    width = state.colWidths[col - viewport.firstColumnIndex] ?? 0;
  } else {
    return null;
  }
  let top: number;
  let height: number;
  if (row >= 1 && row <= verticalSplitPosition) {
    top = (state.frozenRowY[row - 1] ?? 0) + state.scrollTop;
    height = state.frozenRowHeights[row - 1] ?? 0;
  } else if (row >= viewport.firstRowIndex && row <= viewport.lastRowIndex) {
    top = state.rowY[row - viewport.firstRowIndex] ?? 0;
    height = state.rowHeights[row - viewport.firstRowIndex] ?? 0;
  } else {
    return null;
  }
  return { left, top, width, height };
}

/**
 * The box a cell is drawn in: its own, or the merged cell's when the cell
 * starts a merge - the box a note hangs off and the editor lies over. In
 * grid coordinates, frozen panes included; null while the cell is not drawn.
 */
export function drawnCellBox(state: CellGridRenderState, col: number, row: number): GridBox | null {
  const cell = cellBoxOnGrid(state, col, row);
  if (cell === null) {
    return null;
  }
  const index = state.mergedRegions.findIndex((region) => region.col1 === col && region.row1 === row);
  const merged = index < 0 ? undefined : state.mergedRegionBoxes?.[index];
  return merged === undefined ? cell : { left: cell.left, top: cell.top, width: merged.width, height: merged.height };
}

/** A drawn column's width, frozen ones included; 0 when it is not drawn. */
export function columnWidthOf(state: CellGridRenderState, col: number): number {
  if (col >= 1 && col <= state.horizontalSplitPosition) {
    return state.frozenColWidths[col - 1] ?? 0;
  }
  return state.colWidths[col - state.viewport.firstColumnIndex] ?? 0;
}
