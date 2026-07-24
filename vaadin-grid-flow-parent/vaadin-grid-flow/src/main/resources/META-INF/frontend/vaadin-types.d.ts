// Types for the Flow-specific grid API, including the private/protected
// @vaadin/grid API that the connector files rely on. The public API types
// come from the @vaadin npm packages, resolved from the integration tests
// module's node_modules (see tsconfig.json in the module root).
import type { Grid, GridDefaultItem } from '@vaadin/grid/src/vaadin-grid.js';
import type { GridColumn } from '@vaadin/grid/src/vaadin-grid-column.js';
import type { GridSorter } from '@vaadin/grid/src/vaadin-grid-sorter.js';
import type { GridCellActivateEvent, GridItemModel } from '@vaadin/grid/src/vaadin-grid-mixin.js';

/** An item sent by the server-side data communicator */
export interface Item {
  key: string;
  selected?: boolean;
  selectable?: boolean;
  detailsOpened?: boolean;
  expanded?: boolean;
  level?: number;
  part?: Record<string, string>;
  dragData?: Record<string, string>;
  dragDisabled?: boolean;
  dropDisabled?: boolean;
}

/** An inclusive range of item indexes: [start, end] */
export type ItemRange = [start: number, end: number];

export type SelectionMode = 'SINGLE' | 'MULTI' | 'NONE';

export type SorterDirection = 'asc' | 'desc' | null;

/** A sorter state sent to the server */
export interface ServerSorter {
  path: string;
  direction: SorterDirection;
}

/** A row element in the grid body */
export type FlowGridRow = HTMLTableRowElement & {
  index: number;
  _item?: Item;
};

/** The server-side RPC proxy of the grid (and of its selection column) */
export interface GridServer {
  confirmUpdate(id: number): void;
  select(key: string): void;
  selectAll(): void;
  deselect(key: string): void;
  deselectAll(): void;
  setDetailsVisible(key: string | null): void;
  setShiftKeyDown(shiftKeyDown: boolean): void;
  setViewportRange(firstIndex: number, size: number): Promise<void>;
  setViewportRangeByIndexPath(indexes: number[], padding: number): Promise<number>;
  sortersChanged(sorters: ServerSorter[]): void;
  updateContextMenuTargetItem(key: string, columnId: string): void;
  updateExpandedState(key: unknown, expanded: boolean): void;
}

/** The client-side connector API, called by the server-side Flow component */
export interface GridConnector {
  hasRootRequestQueue(): boolean;
  doSelection(items: (Item | null)[], userOriginated?: boolean): void;
  doDeselection(items: Item[], userOriginated?: boolean): void;
  getRenderedRange(): ItemRange;
  getFetchRange(): ItemRange;
  fetchCurrentRange(): Promise<void>;
  resolvePendingCallbacks(): void;
  set(startIndex: number, items: Item[]): void;
  updateFlatData(updatedItems: Item[]): void;
  clear(index: number, length: number): void;
  reset(): void;
  updateSize(size: number): void;
  updateUniqueItemIdPath(path: string): void;
  confirm(id: number): void;
  setSelectionMode(mode: SelectionMode): void;
  setSorterDirections(directions: { column: string; direction: SorterDirection }[]): void;
  setHeaderRenderer(
    column: GridColumn<Item>,
    options: { content: Node | string | null; showSorter?: boolean; sorterPath?: string }
  ): void;
  setFooterRenderer(column: GridColumn<Item>, options: { content: Node | string | null }): void;
  scrollToItem(itemKey: string, ...args: number[]): void;
}

/** The controller managing the grid's data cache */
export interface DataProviderController {
  rootCache: {
    pendingRequests: Record<string, (items: Item[], size?: number) => void>;
    items: (Item | undefined)[];
  };
  getItemContext(item: Item): { index: number } | undefined;
  ensureFlatIndexLoaded(index: number): void;
  clearCache(): void;
  isLoading(): boolean;
  _shouldLoadCachePage: (cache: unknown, page: number) => boolean;
}

/**
 * The private/protected @vaadin/grid API and the Flow-specific API that the
 * grid connector relies on.
 */
export interface FlowGridInternals {
  $: { scroller: HTMLElement; table: HTMLElement; header: HTMLElement; footer: HTMLElement };
  $connector: GridConnector;
  $server: GridServer;
  __deselectDisallowed: boolean;
  __disallowDetailsOnClick: boolean;
  __dragDataTypes?: string[];
  __selectionDragData?: Record<string, string>;
  __selectionDraggedItemsCount?: number;
  _columnTree: GridColumn<Item>[][];
  _dataProviderController: DataProviderController;
  _hasData: boolean;
  _previousSorters: ServerSorter[];
  _shouldLoadAllRenderedRowsAfterPageLoad: boolean;
  _sorters: GridSorter[];
  __a11yUpdateMutiSelectable(): void;
  __a11yUpdateRowSelected(row: HTMLElement, selected: boolean): void;
  __applySorters(...args: unknown[]): void;
  __getRowModel(row: FlowGridRow): GridItemModel<Item>;
  __updateRow(row: HTMLElement, ...args: unknown[]): void;
  __updateVirtualizerElement(...args: unknown[]): void;
  __updateVisibleRows(start?: number, end?: number): void;
  _createPropertyObserver(property: string, methodName: string): void;
  _getActiveSorters(): GridSorter[];
  _getColumnsInOrder(): GridColumn<Item>[];
  _getRenderedRows(): FlowGridRow[];
  _isDetailsOpened(item: Item | undefined): boolean;
  _isSelected(item: Item): boolean;
  isItemSelectable(item: Item | null | undefined): boolean;
  _mapSorters(): ServerSorter[];
  getContextMenuBeforeOpenDetail(event: CustomEvent<{ sourceEvent?: Event }>): { key: string; columnId: string };
  preventContextMenu(event: MouseEvent): boolean;
}

/** The Flow grid element */
export type FlowGrid = Grid<Item> & FlowGridInternals;

/**
 * The private/protected @vaadin/grid API and the Flow-specific API that the
 * tree grid connector relies on, in addition to the grid connector's.
 */
export interface FlowTreeGridInternals {
  __pendingScrollToIndexes?: number[];
  __getRowLevel(row: FlowGridRow): number;
  _isExpanded(item: Item | undefined): boolean;
  _scrollToFlatIndex(flatIndex: number): void;
  expandItem(item: Item | undefined): void;
  collapseItem(item: Item | undefined): void;
}

/** The Flow tree grid element */
export type FlowTreeGrid = FlowGrid & FlowTreeGridInternals;

declare global {
  // Augments the global Vaadin interface declared by @vaadin/component-base
  // with the Flow namespace used by the connectors
  interface Vaadin {
    Flow: {
      gridConnector: { initLazy(grid: FlowGrid): void };
      treeGridConnector: { initLazy(grid: FlowTreeGrid): void };
    };
  }
}

declare module '@vaadin/grid/src/vaadin-grid-mixin.js' {
  interface GridCustomEventMap<TItem> {
    'row-activate': GridCellActivateEvent<TItem>;
    'vaadin-context-menu-before-open': CustomEvent<{ key: string; columnId: string }>;
  }
}

declare module '@vaadin/grid/src/vaadin-grid-column.js' {
  interface GridColumn<TItem = GridDefaultItem> {
    _flowId?: string;
    _order?: number | null;
    _grid: FlowGrid;
  }
}

declare module '@vaadin/grid/src/vaadin-grid-selection-column-base-mixin.js' {
  interface GridSelectionColumnBaseMixinClass<TItem> {
    _defaultHeaderRenderer(root: HTMLElement, column: GridColumn): void;
  }
}
