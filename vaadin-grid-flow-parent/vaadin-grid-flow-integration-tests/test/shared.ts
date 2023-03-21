import './env-setup.js';
import '@vaadin/grid/all-imports.js';
import '../frontend/generated/jar-resources/gridConnector.js';
import sinon from 'sinon';
import type { Grid } from '@vaadin/grid';
import type {} from '@web/test-runner-mocha';

export type GridConnector = {
  initLazy: (grid: Grid) => void;
  updateSize: (size: number) => void;
  set: (index: number, items: any[], parentKey?: string) => void;
  confirm: (index: number) => void;
  confirmParent: (index: number, parentKey: string, levelSize: number) => void;
  setSelectionMode: (mode: 'SINGLE' | 'NONE') => void;
  expandItems: (items: Item[]) => void;
  ensureHierarchy: () => void;
  reset: () => void;
};

export type GridServer = {
  confirmUpdate: (index: number) => void;
  confirmParentUpdate: (index: number, parentKey: string) => void;
  select: (key: string) => void;
  deselect: (key: string) => void;
  setDetailsVisible: (key: string) => void;
  setParentRequestedRanges: (ranges: { firstIndex: number; size: number; parentKey: string }[]) => void;
};

export type Item = {
  key: string;
  name: string;
  selected?: boolean;
};

export type FlowGrid = Grid<Item> & {
  $connector: GridConnector;
  $server: GridServer;
  __deselectDisallowed: boolean;
  __disallowDetailsOnClick: boolean;
  _effectiveSize: number;
  __updateVisibleRows: () => void;
  _updateItem: (index: number, item: Item) => void;
};

type Vaadin = {
  Flow: {
    gridConnector: GridConnector;
  };
};

const Vaadin = window.Vaadin as Vaadin;
export const gridConnector = Vaadin.Flow.gridConnector;

/**
 * Initializes the grid connector and the grid server mock.
 */
export function init(grid: FlowGrid): void {
  grid.$server = {
    confirmUpdate: sinon.spy(),
    confirmParentUpdate: sinon.spy(),
    select: sinon.spy(),
    deselect: sinon.spy(),
    setDetailsVisible: sinon.spy(),
    setParentRequestedRanges: sinon.spy()
  };

  gridConnector.initLazy(grid);

  grid.$connector.reset();
}

/**
 * Returns the number of rows in the grid body.
 */
export function getBodyRowCount(grid: FlowGrid): number {
  return grid._effectiveSize;
}

/**
 * Returns the content of a cell in the grid body.
 */
export function getBodyCellContent(grid: Grid, rowIndex: number, columnIndex: number): HTMLElement | null {
  const items = grid.shadowRoot!.querySelector(`#items`)!;

  const row = [...items.children].find((row) => (row as any).index === rowIndex);
  if (!row) {
    return null;
  }

  const cellsInVisualOrder = [...row.children].sort((a, b) => {
    const aOrder = parseInt(getComputedStyle(a).order) || 0;
    const bOrder = parseInt(getComputedStyle(b).order) || 0;
    return aOrder - bOrder;
  });

  return (cellsInVisualOrder[columnIndex] as any)._content;
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
export function setRootItems(gridConnector: GridConnector, items: Item[]): void {
  gridConnector.updateSize(items.length);
  gridConnector.set(0, items, undefined);
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
