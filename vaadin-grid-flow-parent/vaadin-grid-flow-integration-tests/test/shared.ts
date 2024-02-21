import './env-setup.js';
import '@vaadin/grid/all-imports.js';
import '../frontend/generated/jar-resources/gridConnector.ts';
import '../frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
// For some reason vaadin-grid-flow-selection-column doesn't import vaadin-checkbox
import '@vaadin/checkbox';
import sinon from 'sinon';
import type { Grid, GridColumn } from '@vaadin/grid';
import type {} from '@web/test-runner-mocha';

export type GridConnector = {
  updateFlatData: (updatedItems: Item[]) => void;
  initLazy: (grid: Grid) => void;
  updateSize: (size: number) => void;
  set: (index: number, items: any[], parentKey?: string) => void;
  confirm: (index: number) => void;
  confirmParent: (index: number, parentKey: string, levelSize: number) => void;
  setSelectionMode: (mode: 'SINGLE' | 'NONE' | 'MULTI') => void;
  expandItems: (items: Item[]) => void;
  collapseItems: (items: Item[]) => void;
  ensureHierarchy: () => void;
  reset: () => void;
  doSelection: (items: Item[] | [null], userOriginated: boolean) => void;
  doDeselection: (items: Item[], userOriginated: boolean) => void;
  clear: (index: number, length: number, parentKey?: string) => void;
  setSorterDirections: (sorters: { column: string, direction: string }[]) => void;
  setHeaderRenderer: (column: GridColumn, options: { content: Node | string, showSorter: boolean, sorterPath?: string }) => void;
};

export type GridServer = {
  confirmUpdate: ((index: number) => void) & sinon.SinonSpy;
  confirmParentUpdate: ((index: number, parentKey: string) => void) & sinon.SinonSpy;
  select: ((key: string) => void) & sinon.SinonSpy;
  selectAll: () => void & sinon.SinonSpy;
  deselect: ((key: string) => void) & sinon.SinonSpy;
  deselectAll: () => void & sinon.SinonSpy;
  setDetailsVisible: ((key: string) => void) & sinon.SinonSpy;
  setRequestedRange: ((firstIndex: number, size: number) => void) & sinon.SinonSpy;
  setParentRequestedRanges: ((ranges: { firstIndex: number; size: number; parentKey: string }[]) => void) &
    sinon.SinonSpy;
  sortersChanged: ((sorters: { path: string, direction: string }[]) => void) & sinon.SinonSpy;
};

export type Item = {
  key: string;
  name?: string;
  price?: number,
  selected?: boolean;
  detailsOpened?: boolean;
  style?: Record<string, string>;
  part?: Record<string, string>;
};

export type FlowGrid = Grid<Item> & {
  $connector: GridConnector;
  $server: GridServer;
  __deselectDisallowed: boolean;
  __disallowDetailsOnClick: boolean;
  _flatSize: number;
  __updateVisibleRows: () => void;
  _updateItem: (index: number, item: Item) => void;
};

export type FlowGridSelectionColumn = GridColumn & {
  selectAll: boolean;
  $server: GridServer;
};

type Vaadin = {
  Flow: {
    gridConnector: GridConnector;
  };
};

const Vaadin = window.Vaadin as Vaadin;
export const gridConnector = Vaadin.Flow.gridConnector;

export const GRID_CONNECTOR_PARENT_REQUEST_DELAY = 50;
export const GRID_CONNECTOR_ROOT_REQUEST_DELAY = 150;

/**
 * Initializes the grid connector and the grid server mock.
 */
export function init(grid: FlowGrid): void {
  grid.$server = {
    confirmUpdate: sinon.spy(),
    confirmParentUpdate: sinon.spy(),
    select: sinon.spy(),
    selectAll: sinon.spy(),
    deselect: sinon.spy(),
    deselectAll: sinon.spy(),
    setDetailsVisible: sinon.spy(),
    setRequestedRange: sinon.spy(),
    setParentRequestedRanges: sinon.spy(),
    sortersChanged: sinon.spy()
  };

  gridConnector.initLazy(grid);

  grid.$connector.reset();
}

/**
 * Initializes the grid selection column.
 */
export function initSelectionColumn(grid: FlowGrid, column: FlowGridSelectionColumn) {
  column.$server = grid.$server;
}

/**
 * Returns the number of rows in the grid body.
 */
export function getBodyRowCount(grid: FlowGrid): number {
  return grid._flatSize;
}

/**
 * Returns the content of a header cell.
 */
export function getHeaderCellContent(column: GridColumn): HTMLElement {
  return (column as any)._headerCell._content as HTMLElement;
}

/**
 * Returns a cell in the grid body.
 */
export function getBodyCell(grid: Grid, rowIndex: number, columnIndex: number): HTMLElement | null {
  const items = grid.shadowRoot!.querySelector(`#items`)!;

  const row = [...items.children].find((row) => (row as any).index === rowIndex);
  if (!row) {
    return null;
  }

  const cellsInVisualOrder = [...row.children].sort((a, b) => {
    const aOrder = parseInt(getComputedStyle(a).order) || 0;
    const bOrder = parseInt(getComputedStyle(b).order) || 0;
    return aOrder - bOrder;
  }).map(cell => cell as HTMLElement);

  return cellsInVisualOrder[columnIndex];
}

/**
 * Returns the content of a cell in the grid body.
 */
export function getBodyCellContent(grid: Grid, rowIndex: number, columnIndex: number): HTMLElement | null {
  return (getBodyCell(grid, rowIndex, columnIndex) as any)._content;
}

/**
 * Returns the text content of a cell in the grid body.
 */
export function getBodyCellText(grid: Grid, rowIndex: number, columnIndex: number): string | null {
  return getBodyCellContent(grid, rowIndex, columnIndex)?.textContent || null;
}

/**
 * Sets the root level items for the grid connector.
 */
export function setRootItems(gridConnector: GridConnector, items: Item[], index = 0, length?: number): void {
  gridConnector.updateSize(items.length);
  const itemSlice = length ? items.slice(index, index + length) : items;
  gridConnector.set(index, itemSlice, undefined);
  gridConnector.confirm(0);
}

/**
 * Sets child items for the grid connector.
 */
export function setChildItems(gridConnector: GridConnector, parent: Item, items: Item[]): void {
  gridConnector.set(0, items, parent.key);
  gridConnector.confirmParent(0, parent.key, items.length);
}

/**
 * Expands the given items.
 */
export function expandItems(gridConnector: GridConnector, items: Item[]): void {
  gridConnector.ensureHierarchy();
  gridConnector.expandItems(items);
}

/**
 * Collapse the given items.
 */
export function collapseItems(gridConnector: GridConnector, items: Item[]): void {
  gridConnector.collapseItems(items);
}

/**
 * Clears the given range of the given parent's children.
 */
export function clear(gridConnector: GridConnector, index: number, length: number, parent?: Item): void {
  gridConnector.clear(index, length, parent?.key);
}
