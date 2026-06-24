// @ts-nocheck
import { Debouncer } from '@vaadin/component-base/src/debounce.js';
import { timeOut } from '@vaadin/component-base/src/async.js';
import { isFocusable } from '@vaadin/grid/src/vaadin-grid-active-item-mixin.js';
import { GridFlowSelectionColumn } from './vaadin-grid-flow-selection-column.js';

function isRangeEqual(range1, range2) {
  return range1?.[0] === range2?.[0] && range1?.[1] === range2?.[1];
}

function singleTimeRenderer(renderer) {
  return (root) => {
    if (renderer) {
      renderer(root);
      renderer = null;
    }
  };
}

function renderContent(root, content) {
  if (content instanceof Node) {
    root.appendChild(content);
  } else {
    root.textContent = content;
  }
}

window.Vaadin.Flow.gridConnector = {};
window.Vaadin.Flow.gridConnector.initLazy = (grid) => {
  // Check whether the connector was already initialized for the grid
  if (grid.$connector) {
    return;
  }

  const dataProviderController = grid._dataProviderController;

  const requestDebouncerDelay = 150;
  let requestDebouncer;
  let requestedRange = null;

  let selectedKeys = {};
  let selectionMode = 'SINGLE';

  let sorterDirectionsSetFromServer = false;

  grid.size = 0; // To avoid NaN here and there before we get proper data
  grid.itemIdPath = 'key';

  grid.$connector = {};

  grid.$connector.hasRootRequestQueue = () => {
    const { pendingRequests } = dataProviderController.rootCache;
    return Object.keys(pendingRequests).length > 0 || !!requestDebouncer?.isActive();
  };

  grid.$connector.doSelection = function (items, userOriginated) {
    if (selectionMode === 'NONE' || !items.length || (userOriginated && grid.hasAttribute('disabled'))) {
      return;
    }
    if (selectionMode === 'SINGLE') {
      selectedKeys = {};
    }

    let selectedItemsChanged = false;
    items.forEach((item) => {
      const selectable = !userOriginated || grid.isItemSelectable(item);
      selectedItemsChanged = selectedItemsChanged || selectable;
      if (item && selectable) {
        selectedKeys[item.key] = item;
        item.selected = true;
        if (userOriginated) {
          grid.$server.select(item.key);
        }
      }

      // FYI: In single selection mode, the server can send items = [null]
      // which means a "Deselect All" command.
      const isSelectedItemDifferentOrNull = !grid.activeItem || !item || item.key !== grid.activeItem.key;
      if (!userOriginated && selectionMode === 'SINGLE' && isSelectedItemDifferentOrNull) {
        grid.activeItem = item;
      }
    });

    if (selectedItemsChanged) {
      grid.selectedItems = Object.values(selectedKeys);
    }
  };

  grid.$connector.doDeselection = function (items, userOriginated) {
    if (selectionMode === 'NONE' || !items.length || (userOriginated && grid.hasAttribute('disabled'))) {
      return;
    }

    const updatedSelectedItems = grid.selectedItems.slice();
    while (items.length) {
      const itemToDeselect = items.shift();
      const selectable = !userOriginated || grid.isItemSelectable(itemToDeselect);
      if (!selectable) {
        continue;
      }
      for (let i = 0; i < updatedSelectedItems.length; i++) {
        const selectedItem = updatedSelectedItems[i];
        if (itemToDeselect?.key === selectedItem.key) {
          updatedSelectedItems.splice(i, 1);
          break;
        }
      }
      if (itemToDeselect) {
        delete selectedKeys[itemToDeselect.key];
        delete itemToDeselect.selected;
        if (userOriginated) {
          grid.$server.deselect(itemToDeselect.key);
        }
      }
    }
    grid.selectedItems = updatedSelectedItems;
  };

  grid.__activeItemChanged = function (newVal, oldVal) {
    if (selectionMode !== 'SINGLE') {
      return;
    }
    if (!newVal) {
      if (oldVal && selectedKeys[oldVal.key]) {
        if (grid.__deselectDisallowed) {
          grid.activeItem = oldVal;
        } else {
          // The item instance may have changed since the item was stored as active item
          // and information such as whether the item may be selected or deselected may
          // be stale. Use data provider controller to get updated instance from grid
          // cache.
          oldVal = dataProviderController.getItemContext(oldVal).item;
          grid.$connector.doDeselection([oldVal], true);
        }
      }
    } else if (!selectedKeys[newVal.key]) {
      grid.$connector.doSelection([newVal], true);
    }
  };
  grid._createPropertyObserver('activeItem', '__activeItemChanged');

  grid.__activeItemChangedDetails = function (newVal, oldVal) {
    if (grid.__disallowDetailsOnClick) {
      return;
    }
    // when grid is attached, newVal is not set and oldVal is undefined
    // do nothing
    if (newVal == null && oldVal === undefined) {
      return;
    }
    if (newVal && !newVal.detailsOpened) {
      grid.$server.setDetailsVisible(newVal.key);
    } else {
      grid.$server.setDetailsVisible(null);
    }
  };
  grid._createPropertyObserver('activeItem', '__activeItemChangedDetails');

  grid.$connector.getRenderedRange = function () {
    const renderedRows = grid._getRenderedRows();
    return [renderedRows.at(0)?.index ?? 0, renderedRows.at(-1)?.index ?? 0];
  };

  grid.$connector.getFetchRange = function () {
    // Get the range of currently rendered rows
    let range = grid.$connector.getRenderedRange();

    // Expand the range in both directions to add a buffer, e.g. a rendered
    // range of [100, 120] becomes [80, 140]
    const buffer = range[1] - range[0];
    range[0] = Math.max(range[0] - buffer, 0);
    range[1] = Math.min(range[1] + buffer, grid.size - 1);

    // Align the range to page boundaries (inclusive), e.g. with pageSize 50,
    // a range of [60, 110] becomes [50, 149]
    range[0] = Math.floor(range[0] / grid.pageSize) * grid.pageSize;
    range[1] = (Math.floor(range[1] / grid.pageSize) + 1) * grid.pageSize - 1;

    return range;
  };

  grid.$connector.fetchCurrentRange = async () => {
    const range = grid.$connector.getFetchRange();

    if (isRangeEqual(range, requestedRange)) {
      // Skip duplicate requests for the same range.
      return;
    }

    requestedRange = range;

    // The range is inclusive while the server expects a length, hence + 1,
    // e.g. a range of [50, 149] results in a length of 100
    await grid.$server.setViewportRange(range[0], range[1] - range[0] + 1);

    // Resolve any pending callbacks in case the server responded with no new
    // data and $connector.confirm wasn't called because the server assumes all
    // the data is already on the client. This can happen, for example, when
    // scrolling quickly back and forth so that the grid returns to a position
    // whose data has already been delivered and is cached. In that case,
    // resolving the callbacks lets the grid exit the loading state correctly.
    grid.$connector.resolvePendingCallbacks();
  };

  grid.$connector.resolvePendingCallbacks = () => {
    const { rootCache } = dataProviderController;

    preventRowUpdates(() => {
      Object.values(rootCache.pendingRequests).forEach((callback) => {
        // Set a flag so the grid re-checks all rendered rows after all callbacks
        // are resolved, and requests any that are still missing, not just the ones
        // covered by this resolved callback.
        grid._shouldLoadAllRenderedRowsAfterPageLoad = true;

        // Resolve this pending callback. This may synchronously trigger new ones
        // to be created or reissued.
        callback([]);
      });
    });

    // If no new data provider requests came in while resolving the callbacks
    // above, clear the current requested range and cancel the active request
    // debouncer (if any) to avoid sending a server request that is no longer
    // needed.
    if (Object.values(rootCache.pendingRequests).length === 0) {
      requestDebouncer?.cancel();
      requestedRange = null;
    }
  };

  // The grid requests a page only when a row from that page gets rendered,
  // which is too late to keep up while scrolling and leads to blank rows.
  // To load data ahead of rendering, request the fetch range (rendered
  // rows + buffer) on every virtualizer update while scrolling.
  grid.__updateVirtualizerElement = function (...args) {
    Object.getPrototypeOf(this).__updateVirtualizerElement.call(this, ...args);

    if (grid.$.scroller.hasAttribute('scrolling')) {
      const fetchRange = grid.$connector.getFetchRange();
      dataProviderController.ensureFlatIndexLoaded(fetchRange[0]);
      dataProviderController.ensureFlatIndexLoaded(fetchRange[1]);
    }
  };

  grid.dataProvider = function (params, callback) {
    if (params.pageSize !== grid.pageSize) {
      throw 'Invalid pageSize';
    }

    // size is controlled by the server (data communicator), so if the
    // size is zero, we know that there is no data to fetch.
    // This also prevents an empty grid getting stuck in a loading state.
    // The connector does not cache empty pages, so if the grid requests
    // data again, there would be no cache entry, causing a request to
    // the server. However, the data communicator will never respond,
    // as it assumes that the data is already cached.
    if (grid.size === 0) {
      callback([], 0);
      return;
    }

    requestDebouncer = Debouncer.debounce(
      requestDebouncer,
      timeOut.after(grid._hasData ? requestDebouncerDelay : 0),
      () => {
        grid.$connector.fetchCurrentRange();
      }
    );
  };

  grid.$connector.setSorterDirections = function (directions) {
    sorterDirectionsSetFromServer = true;
    setTimeout(() => {
      try {
        // Sorters for hidden columns are removed from DOM but stored in the web component.
        // We need to ensure that all the sorters are reset when using `grid.sort(null)`.
        const sorters = [...new Set([...grid.querySelectorAll('vaadin-grid-sorter'), ...grid._sorters])];

        sorters.forEach((sorter) => {
          sorter.direction = null;
        });

        // Apply directions in correct order, depending on configured multi-sort priority.
        // For the default "prepend" mode, directions need to be applied in reverse, in
        // order for the sort indicators to match the order on the server. For "append"
        // just keep the order passed from the server.
        if (grid.multiSortPriority !== 'append') {
          directions = directions.reverse();
        }
        directions.forEach(({ column, direction }) => {
          sorters.forEach((sorter) => {
            if (sorter.getAttribute('path') === column) {
              sorter.direction = direction;
            }
          });
        });

        // Manually trigger a re-render of the sorter priority indicators
        // in case some of the sorters were hidden while being updated above
        // and therefore didn't notify the grid about their direction change.
        grid.__applySorters();
      } finally {
        sorterDirectionsSetFromServer = false;
      }
    });
  };

  let preventRowUpdatesActive = 0;

  function preventRowUpdates(callback) {
    try {
      preventRowUpdatesActive++;
      callback();
    } finally {
      preventRowUpdatesActive--;
    }
  }

  grid.__updateRow = function (row, ...args) {
    if (preventRowUpdatesActive !== 0) {
      return;
    }

    Object.getPrototypeOf(this).__updateRow.call(this, row, ...args);
  };

  grid.$connector.set = function (startIndex, items) {
    const { rootCache } = dataProviderController;
    items.forEach((item, i) => {
      rootCache.items[startIndex + i] = item;
    });

    preventRowUpdates(() => {
      grid.$connector.doSelection(items.filter((item) => item.selected));
      grid.$connector.doDeselection(items.filter((item) => !item.selected && selectedKeys[item.key]));
    });

    grid.__updateVisibleRows(startIndex, startIndex + items.length - 1);
  };

  /**
   * Updates the given items for a non-hierarchical grid.
   *
   * @param updatedItems the updated items array
   */
  grid.$connector.updateFlatData = function (updatedItems) {
    const { rootCache } = dataProviderController;

    updatedItems.forEach((item) => {
      const itemContext = dataProviderController.getItemContext(item);
      if (!itemContext) {
        return;
      }

      const { index } = itemContext;
      rootCache.items[index] = item;

      grid.__updateVisibleRows(index, index);
    });
  };

  grid.$connector.clear = function (index, length) {
    const { rootCache } = dataProviderController;

    if (index % grid.pageSize !== 0) {
      throw 'Got cleared data for index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
    }

    const items = rootCache.items.slice(index, index + length).filter(Boolean);
    if (items.length === 0) {
      return;
    }

    preventRowUpdates(() => {
      grid.$connector.doDeselection(items.filter((item) => selectedKeys[item.key]));
    });

    rootCache.items.fill(undefined, index, index + length);

    grid.__updateVisibleRows(index, index + length - 1);
  };

  grid.$connector.reset = function () {
    dataProviderController.clearCache();
    requestedRange = null;
    requestDebouncer?.cancel();
    grid.__updateVisibleRows();
  };

  grid.$connector.updateSize = (newSize) => (grid.size = newSize);

  grid.$connector.updateUniqueItemIdPath = (path) => (grid.itemIdPath = path);

  grid.$connector.confirm = function (id) {
    // We're done applying changes from this batch, resolve pending
    // callbacks
    grid.$connector.resolvePendingCallbacks();

    // Let server know we're done
    grid.$server.confirmUpdate(id);
  };

  grid.$connector.setSelectionMode = function (mode) {
    selectionMode = mode;
    selectedKeys = {};
    grid.selectedItems = [];
    grid.__a11yUpdateMutiSelectable();
  };

  grid.__a11yUpdateRowSelected = function (row, selected) {
    if (selectionMode === 'NONE') {
      [row, ...row.children].forEach((el) => el.removeAttribute('aria-selected'));
      return;
    }

    Object.getPrototypeOf(this).__a11yUpdateRowSelected.call(this, row, selected);
  };

  /*
   * Manage aria-multiselectable attribute depending on the selection mode.
   * see more: https://github.com/vaadin/web-components/issues/1536
   * or: https://www.w3.org/TR/wai-aria-1.1/#aria-multiselectable
   */
  grid.__a11yUpdateMutiSelectable = function () {
    if (!grid.$) {
      return;
    }

    switch (selectionMode) {
      case 'SINGLE':
        grid.$.table.setAttribute('aria-multiselectable', 'false');
        break;
      case 'MULTI':
        grid.$.table.setAttribute('aria-multiselectable', 'true');
        break;
      default:
        grid.$.table.removeAttribute('aria-multiselectable');
    }
  };
  grid._createPropertyObserver('isAttached', '__a11yUpdateMutiSelectable');

  grid.$connector.setHeaderRenderer = function (column, options) {
    const { content, showSorter, sorterPath } = options;

    if (content === null) {
      column.headerRenderer = null;
      return;
    }

    column.headerRenderer = singleTimeRenderer((root) => {
      // Render sorter
      let contentRoot = root;
      if (showSorter) {
        const sorter = document.createElement('vaadin-grid-sorter');
        sorter.setAttribute('path', sorterPath);
        const ariaLabel = content instanceof Node ? content.textContent : content;
        if (ariaLabel) {
          sorter.setAttribute('aria-label', `Sort by ${ariaLabel}`);
        }
        root.appendChild(sorter);

        // Use sorter as content root
        contentRoot = sorter;
      }

      renderContent(contentRoot, content);
    });
  };

  // This method is overridden to prevent the grid web component from
  // automatically excluding columns from sorting when they get hidden.
  // In Flow, it's the developer's responsibility to remove the column
  // from the backend sort order when the column gets hidden.
  grid._getActiveSorters = function () {
    return this._sorters.filter((sorter) => sorter.direction);
  };

  grid.__applySorters = function (...args) {
    const sorters = grid._mapSorters();
    const sortersChanged = JSON.stringify(grid._previousSorters) !== JSON.stringify(sorters);

    // Update the _previousSorters in vaadin-grid-sort-mixin so that the __applySorters
    // method in the mixin will skip calling clearCache().
    //
    // In Flow Grid's case, we never want to clear the cache eagerly when the sorter elements
    // change due to one of the following reasons:
    //
    // 1. Sorted by user: The items in the new sort order need to be fetched from the server,
    // and we want to avoid a heavy re-render before the updated items have actually been fetched.
    //
    // 2. Sorted programmatically on the server: The items in the new sort order have already
    // been fetched and applied to the grid. The sorter element states are updated programmatically
    // to reflect the new sort order, but there's no need to re-render the grid rows.
    grid._previousSorters = sorters;

    // Call the original __applySorters method in vaadin-grid-sort-mixin
    Object.getPrototypeOf(this).__applySorters.call(this, ...args);

    if (sortersChanged && !sorterDirectionsSetFromServer) {
      grid.$server.sortersChanged(sorters);
    }
  };

  grid.$connector.setFooterRenderer = function (column, options) {
    const { content } = options;

    if (content === null) {
      column.footerRenderer = null;
      return;
    }

    column.footerRenderer = singleTimeRenderer((root) => renderContent(root, content));
  };

  grid.addEventListener('vaadin-context-menu-before-open', function (e) {
    const { key, columnId } = e.detail;
    grid.$server.updateContextMenuTargetItem(key, columnId);
  });

  grid.getContextMenuBeforeOpenDetail = function (event) {
    // For `contextmenu` events, we need to access the source event,
    // when using open on click we just use the click event itself
    const sourceEvent = event.detail.sourceEvent || event;
    const eventContext = grid.getEventContext(sourceEvent);
    const key = eventContext.item?.key || '';
    const columnId = eventContext.column?.id || '';
    return { key, columnId };
  };

  grid.preventContextMenu = function (event) {
    const isLeftClick = event.type === 'click';
    const { column } = grid.getEventContext(event);

    return isLeftClick && column instanceof GridFlowSelectionColumn;
  };

  grid.addEventListener('click', (e) => _fireClickEvent(e, 'item-click'));
  grid.addEventListener('dblclick', (e) => _fireClickEvent(e, 'item-double-click'));

  grid.addEventListener('column-resize', (e) => {
    const cols = grid._getColumnsInOrder().filter((col) => !col.hidden);

    cols.forEach((col) => {
      col.dispatchEvent(new CustomEvent('column-drag-resize'));
    });

    grid.dispatchEvent(
      new CustomEvent('column-drag-resize', {
        detail: {
          resizedColumnKey: e.detail.resizedColumn._flowId
        }
      })
    );
  });

  grid.addEventListener('column-reorder', (e) => {
    const columns = grid._columnTree
      .at(-1)
      .filter((c) => c._flowId)
      .sort((a, b) => a._order - b._order)
      .map((c) => c._flowId);

    grid.dispatchEvent(
      new CustomEvent('column-reorder-all-columns', {
        detail: { columns }
      })
    );
  });

  grid.addEventListener('cell-focus', (e) => {
    const eventContext = grid.getEventContext(e);

    if (!['header', 'body', 'footer'].includes(eventContext.section)) {
      return;
    }

    grid.dispatchEvent(
      new CustomEvent('grid-cell-focus', {
        detail: {
          itemKey: eventContext.item?.key ?? null,
          internalColumnId: eventContext.column?._flowId ?? null,
          section: eventContext.section
        }
      })
    );
  });

  function _fireClickEvent(event, eventName) {
    // Click event was handled by the component inside grid, do nothing.
    if (event.defaultPrevented) {
      return;
    }

    const path = event.composedPath();
    const idx = path.findIndex((node) => node.localName === 'td' || node.localName === 'th');
    const cell = path[idx];
    const content = path.slice(0, idx);

    // Do not fire item click event if the click originated inside a Vaadin overlay
    // (Select, ComboBox, DatePicker, MenuBar, ContextMenu, ...). The overlay's
    // menu/list lives in the host component's light DOM, so its clicks bubble
    // through the cell — but by the time we get here, the overlay has typically
    // been hidden synchronously, defeating the offsetParent-based isFocusable check.
    if (content.some((node) => typeof node.localName === 'string' && node.localName.endsWith('-overlay'))) {
      return;
    }

    // Do not fire item click event if cell content contains focusable elements.
    // Use this instead of event.target to detect cases like icon inside button.
    // See https://github.com/vaadin/flow-components/issues/4065
    if (
      content.some((node) => {
        // Ignore focus buttons that the component renders into cells in focus button mode on MacOS
        const focusable = cell?._focusButton !== node && isFocusable(node);
        return focusable || node instanceof HTMLLabelElement;
      })
    ) {
      return;
    }

    const eventContext = grid.getEventContext(event);
    const section = eventContext.section;

    if (eventContext.item && section !== 'details') {
      event.itemKey = eventContext.item.key;
      // if you have a details-renderer, getEventContext().column is undefined
      if (eventContext.column) {
        event.internalColumnId = eventContext.column._flowId;
      }
      grid.dispatchEvent(new CustomEvent(eventName, { detail: event }));
    }
  }

  grid.cellPartNameGenerator = function (column, rowData) {
    const part = rowData.item.part;
    if (!part) {
      return;
    }
    return (part.row || '') + ' ' + ((column && part[column._flowId]) || '');
  };

  grid.dropFilter = (rowData) => rowData.item && !rowData.item.dropDisabled;

  grid.dragFilter = (rowData) => rowData.item && !rowData.item.dragDisabled;

  grid.addEventListener('grid-dragstart', (e) => {
    const { draggedItems, setDragData, setDraggedItemsCount } = e.detail;

    if (grid._isSelected(draggedItems[0])) {
      // Dragging selected (possibly multiple) items
      if (grid.__selectionDragData) {
        Object.entries(grid.__selectionDragData).forEach(([type, data]) => setDragData(type, data));
      } else {
        (grid.__dragDataTypes || []).forEach((type) => {
          setDragData(type, draggedItems.map((item) => item.dragData[type]).join('\n'));
        });
      }

      if (grid.__selectionDraggedItemsCount > 1) {
        setDraggedItemsCount(grid.__selectionDraggedItemsCount);
      }
    } else {
      // Dragging just one (non-selected) item
      (grid.__dragDataTypes || []).forEach((type) => {
        setDragData(type, draggedItems[0].dragData[type]);
      });
    }
  });

  grid.isItemSelectable = (item) => {
    // If there is no selectable data, assume the item is selectable
    return item?.selectable === undefined || item.selectable;
  };

  grid._isDetailsOpened = function (item) {
    return item?.detailsOpened ?? false;
  };

  function isRowFullyInViewport(row) {
    const rowRect = row.getBoundingClientRect();
    const tableRect = grid.$.table.getBoundingClientRect();
    const headerRect = grid.$.header.getBoundingClientRect();
    const footerRect = grid.$.footer.getBoundingClientRect();
    return rowRect.top >= tableRect.top + headerRect.height && rowRect.bottom <= tableRect.bottom - footerRect.height;
  }

  grid.$connector.scrollToItem = function (itemKey, ...args) {
    const targetRow = grid._getRenderedRows().find((row) => {
      const { item } = grid.__getRowModel(row);
      return grid.getItemId(item) === itemKey;
    });
    if (targetRow && isRowFullyInViewport(targetRow)) {
      return;
    }

    grid.scrollToIndex(...args);
  };
};
