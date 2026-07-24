import './env-setup.js';
import '@vaadin/grid/src/all-imports.js';
import '../frontend/generated/jar-resources/gridConnector.ts';
import '../frontend/generated/jar-resources/treeGridConnector.ts';
import '../frontend/generated/jar-resources/vaadin-grid-flow-selection-column.ts';
import sinon from 'sinon';
import type { Grid } from '@vaadin/grid';
import type { GridColumn } from '@vaadin/grid/vaadin-grid-column.js';
import type { GridSorter } from '@vaadin/grid/vaadin-grid-sorter.js';
import type {} from '@web/test-runner-mocha';
import type {} from 'sinon-chai';
import type {
  FlowGrid as ConnectorFlowGrid,
  GridConnector as ConnectorGridConnector,
  GridServer as ConnectorGridServer,
  Item as ConnectorItem
} from '../frontend/generated/jar-resources/vaadin-grid-types.js';

export type GridServer = {
  [K in keyof ConnectorGridServer]: ConnectorGridServer[K] & sinon.SinonSpy;
} & {
  setViewportRange: ConnectorGridServer['setViewportRange'] & sinon.SinonSpy & { promise?: sinon.SinonPromise<void> };
};

export type Item = ConnectorItem & {
  name?: string;
  price?: number;
  children?: boolean;
  style?: Record<string, string>;
};

// The connector API retyped with the test Item, so that tests can pass item
// literals with test-specific properties without excess property errors
export type GridConnector = Omit<ConnectorGridConnector, 'doSelection' | 'doDeselection' | 'set' | 'updateFlatData'> & {
  doSelection(items: (Item | null)[], userOriginated?: boolean): void;
  doDeselection(items: Item[], userOriginated?: boolean): void;
  set(startIndex: number, items: Item[]): void;
  updateFlatData(updatedItems: Item[]): void;
};

export type FlowGrid = {
  $connector: GridConnector;
  $server: GridServer;
} & ConnectorFlowGrid & {
  _flatSize: number;
  _updateItem: (index: number, item: Item) => void;
};

export type FlowGridSorter = GridSorter & {
  _order?: number | null;
};

export type FlowGridSelectionColumn = GridColumn & {
  selectAll: boolean;
  $server: GridServer;
};

export const gridConnector = window.Vaadin.Flow.gridConnector;
export const treeGridConnector = window.Vaadin.Flow.treeGridConnector;

export const GRID_CONNECTOR_ROOT_REQUEST_DELAY = 150;

/**
 * Initializes the grid connector and the grid server mock.
 */
export function init(
  grid: FlowGrid,
  connector: { initLazy(grid: ConnectorFlowGrid): void } = gridConnector
): void {
  grid.$server = {
    confirmUpdate: sinon.spy(),
    select: sinon.spy(),
    selectAll: sinon.spy(),
    deselect: sinon.spy(),
    deselectAll: sinon.spy(),
    setDetailsVisible: sinon.spy(),
    updateExpandedState: sinon.spy(),
    setViewportRange: sinon.spy(() => {
      const promise = sinon.promise<void>();
      grid.$server.setViewportRange.promise = promise;
      return promise;
    }),
    setViewportRangeByIndexPath: sinon.spy(),
    sortersChanged: sinon.spy(),
    setShiftKeyDown: sinon.spy(),
    updateContextMenuTargetItem: sinon.spy(),
  };

  connector.initLazy(grid);

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
 * Returns the content of a footer cell.
 */
export function getFooterCellContent(column: GridColumn): HTMLElement {
  return (column as any)._footerCell._content as HTMLElement;
}

/**
 * Returns a row in the grid body.
 */
export function getBodyRow(grid: Grid, rowIndex: number): HTMLElement | null {
  const row = [...grid.shadowRoot!.querySelectorAll('.row')].find((row) => (row as any).index === rowIndex);
  return row as HTMLElement ?? null;
}

/**
 * Returns a cell in a grid body row.
 */
export function getBodyCell(grid: Grid, rowIndex: number, columnIndex: number): HTMLElement | null {
  const row = getBodyRow(grid, rowIndex);
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
 * Returns the details cell in a grid body row.
 */
export function getDetailsCell(grid: Grid, rowIndex: number): HTMLElement | null {
  return getBodyRow(grid, rowIndex)?.querySelector('.details-cell') ?? null;
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
  gridConnector.set(index, itemSlice);
  gridConnector.confirm(0);
}

/**
 * Clears the given range of the given parent's children.
 */
export function clear(gridConnector: GridConnector, index: number, length: number): void {
  gridConnector.clear(index, length);
}
