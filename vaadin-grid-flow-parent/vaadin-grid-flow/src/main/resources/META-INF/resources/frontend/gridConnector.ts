// @ts-nocheck
import { Debouncer } from '@vaadin/component-base/src/debounce.js';
import { timeOut, animationFrame } from '@vaadin/component-base/src/async.js';
import { Grid } from '@vaadin/grid/src/vaadin-grid.js';
import { isFocusable } from '@vaadin/grid/src/vaadin-grid-active-item-mixin.js';
import { GridFlowSelectionColumn } from './vaadin-grid-flow-selection-column.js';

window.Vaadin.Flow.gridConnector = {};
window.Vaadin.Flow.gridConnector.initLazy = (grid) => {
  // Check whether the connector was already initialized for the grid
  if (grid.$connector) {
    return;
  }

  const dataProviderController = grid._dataProviderController;

  let cache = {};

  const rootRequestDelay = 150;
  let rootRequestDebouncer;

  let lastRequestedRange = [0, 0];

  const validSelectionModes = ['SINGLE', 'NONE', 'MULTI'];
  let selectedKeys = {};
  let selectionMode = 'SINGLE';

  let sorterDirectionsSetFromServer = false;

  grid.size = 0; // To avoid NaN here and there before we get proper data
  grid.itemIdPath = 'key';

  grid.$connector = {};

  grid.$connector.hasRootRequestQueue = () => {
    const { pendingRequests } = dataProviderController.rootCache;
    return Object.keys(pendingRequests).length > 0 || !!rootRequestDebouncer?.isActive();
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
      const isSelectedItemDifferentOrNull = !grid.activeItem || !item || item.key != grid.activeItem.key;
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
    if (selectionMode != 'SINGLE') {
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
  grid._createPropertyObserver('activeItem', '__activeItemChanged', true);

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
  grid._createPropertyObserver('activeItem', '__activeItemChangedDetails', true);

  grid.$connector.debounceRootRequest = function (page) {
    const delay = grid._hasData ? rootRequestDelay : 0;

    rootRequestDebouncer = Debouncer.debounce(rootRequestDebouncer, timeOut.after(delay), () => {
      grid.$connector.fetchPage((firstIndex, size) => grid.$server.setViewportRange(firstIndex, size), page);
    });
  };

  grid.$connector.fetchPage = function (fetch, page) {
    // Adjust the requested page to be within the valid range in case
    // the grid size has changed while fetchPage was debounced.
    page = Math.min(page, Math.floor((grid.size - 1) / grid.pageSize));

    // Determine what to fetch based on scroll position and not only
    // what grid asked for
    const visibleRows = grid._getRenderedRows();
    let start = visibleRows.length > 0 ? visibleRows[0].index : 0;
    let end = visibleRows.length > 0 ? visibleRows[visibleRows.length - 1].index : 0;

    // The buffer size could be multiplied by some constant defined by the user,
    // if he needs to reduce the number of items sent to the Grid to improve performance
    // or to increase it to make Grid smoother when scrolling
    let buffer = end - start;
    start = Math.max(0, start - buffer);
    end = Math.min(end + buffer, grid.size);

    let pageRange = [Math.floor(start / grid.pageSize), Math.floor(end / grid.pageSize)];

    // When the viewport doesn't contain the requested page or it doesn't contain any items from
    // the requested level at all, it means that the scroll position has changed while fetchPage
    // was debounced. For example, it can happen if the user scrolls the grid to the bottom and
    // then immediately back to the top. In this case, the request for the last page will be left
    // hanging. To avoid this, as a workaround, we reset the range to only include the requested page
    // to make sure all hanging requests are resolved. After that, the grid requests the first page
    // or whatever in the viewport again.
    if (page < pageRange[0] || page > pageRange[1]) {
      pageRange = [page, page];
    }

    if (lastRequestedRange[0] != pageRange[0] || lastRequestedRange[1] != pageRange[1]) {
      lastRequestedRange = pageRange;
      let pageCount = pageRange[1] - pageRange[0] + 1;
      fetch(pageRange[0] * grid.pageSize, pageCount * grid.pageSize);
    }
  };

  grid.dataProvider = function (params, callback) {
    if (params.pageSize != grid.pageSize) {
      throw 'Invalid pageSize';
    }

    let page = params.page;

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

    if (cache[page]) {
      callback(cache[page]);
    } else {
      grid.$connector.debounceRootRequest(page);
    }
  };

  grid.$connector.setSorterDirections = function (directions) {
    sorterDirectionsSetFromServer = true;
    setTimeout(() => {
      try {
        const sorters = Array.from(grid.querySelectorAll('vaadin-grid-sorter'));

        // Sorters for hidden columns are removed from DOM but stored in the web component.
        // We need to ensure that all the sorters are reset when using `grid.sort(null)`.
        grid._sorters.forEach((sorter) => {
          if (!sorters.includes(sorter)) {
            sorters.push(sorter);
          }
        });

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

  let preventUpdateVisibleRowsActive = 0;

  function preventUpdateVisibleRows(callback) {
    try {
      preventUpdateVisibleRowsActive++;
      callback();
    } finally {
      preventUpdateVisibleRowsActive--;
    }
  }

  grid.__updateVisibleRows = function (...args) {
    if (preventUpdateVisibleRowsActive === 0) {
      Object.getPrototypeOf(this).__updateVisibleRows.call(this, ...args);
    }
  };

  grid.__updateRow = function (row, ...args) {
    Object.getPrototypeOf(this).__updateRow.call(this, row, ...args);

    // since no row can be selected when selection mode is NONE
    // if selectionMode is set to NONE, remove aria-selected attribute from the row
    if (selectionMode === validSelectionModes[1]) {
      // selectionMode === NONE
      row.removeAttribute('aria-selected');
      Array.from(row.children).forEach((cell) => cell.removeAttribute('aria-selected'));
    }
  };

  const itemsUpdated = function (items) {
    if (!items || !Array.isArray(items)) {
      throw 'Attempted to call itemsUpdated with an invalid value: ' + JSON.stringify(items);
    }
    let detailsOpenedItems = Array.from(grid.detailsOpenedItems);
    for (let i = 0; i < items.length; ++i) {
      const item = items[i];
      if (!item) {
        continue;
      }
      if (item.detailsOpened) {
        if (grid._getItemIndexInArray(item, detailsOpenedItems) < 0) {
          detailsOpenedItems.push(item);
        }
      } else if (grid._getItemIndexInArray(item, detailsOpenedItems) >= 0) {
        detailsOpenedItems.splice(grid._getItemIndexInArray(item, detailsOpenedItems), 1);
      }
    }
    grid.detailsOpenedItems = detailsOpenedItems;
  };

  /**
   * Updates the cache for the given page for grid or tree-grid.
   *
   * @param page index of the page to update
   */
  const updateGridCache = function (page) {
    const { rootCache } = dataProviderController;

    // Force update unless there's a callback waiting.
    if (cache[page] && rootCache.pendingRequests[page]) {
      return;
    }

    for (let i = 0; i < grid.pageSize; i++) {
      const index = page * grid.pageSize + i;
      const item = cache[page]?.[i];
      rootCache.items[index] = item;
    }
  };

  grid.$connector.set = function (startIndex, items) {
    items.forEach((item, i) => {
      const index = startIndex + i;
      const page = Math.floor(index / grid.pageSize);
      cache[page] ??= [];
      cache[page][index % grid.pageSize] = item;
    });

    const firstPage = Math.floor(startIndex / grid.pageSize);
    const updatedPageCount = Math.ceil(items.length / grid.pageSize);
    for (let i = 0; i < updatedPageCount; i++) {
      updateGridCache(firstPage + i);
    }

    preventUpdateVisibleRows(() => {
      grid.$connector.doSelection(items.filter((item) => item.selected));
      grid.$connector.doDeselection(items.filter((item) => !item.selected && selectedKeys[item.key]));
      itemsUpdated(items);
    });

    grid.__updateVisibleRows(startIndex, startIndex + items.length - 1);
  };

  const itemToCacheLocation = function (item) {
    for (let page in cache) {
      for (let index in cache[page]) {
        if (grid.getItemId(cache[page][index]) === grid.getItemId(item)) {
          return { page: page, index: index };
        }
      }
    }
    return null;
  };

  /**
   * Updates the given items for a non-hierarchical grid.
   *
   * @param updatedItems the updated items array
   */
  grid.$connector.updateFlatData = function (updatedItems) {
    const updatedIndexes = [];

    // update (flat) caches
    for (let i = 0; i < updatedItems.length; i++) {
      let cacheLocation = itemToCacheLocation(updatedItems[i]);
      if (cacheLocation) {
        // update connector cache
        cache[cacheLocation.page][cacheLocation.index] = updatedItems[i];

        // update grid's cache
        const index = parseInt(cacheLocation.page) * grid.pageSize + parseInt(cacheLocation.index);
        const { rootCache } = dataProviderController;
        if (rootCache.items[index]) {
          rootCache.items[index] = updatedItems[i];
        }
        updatedIndexes.push(index);
      }
    }

    preventUpdateVisibleRows(() => {
      itemsUpdated(updatedItems);
    });

    updatedIndexes.forEach((index) => grid.__updateVisibleRows(index, index));
  };

  grid.$connector.clear = function (index, length) {
    if (!cache || Object.keys(cache).length === 0) {
      return;
    }
    if (index % grid.pageSize != 0) {
      throw 'Got cleared data for index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
    }

    let firstPage = Math.floor(index / grid.pageSize);
    let updatedPageCount = Math.ceil(length / grid.pageSize);

    for (let i = 0; i < updatedPageCount; i++) {
      let page = firstPage + i;
      let items = cache[page];
      if (items) {
        preventUpdateVisibleRows(() => {
          grid.$connector.doDeselection(items.filter((item) => selectedKeys[item.key]));
          items.forEach((item) => grid.closeItemDetails(item));
        });
        delete cache[page];
        updateGridCache(page);
      }
    }

    grid.__updateVisibleRows(index, index + length - 1);
  };

  grid.$connector.reset = function () {
    cache = {};
    dataProviderController.clearCache();
    lastRequestedRange = [-1, -1];
    rootRequestDebouncer?.cancel();
    grid.__updateVisibleRows();
  };

  grid.$connector.updateSize = (newSize) => (grid.size = newSize);

  grid.$connector.updateUniqueItemIdPath = (path) => (grid.itemIdPath = path);

  grid.$connector.confirm = function (id) {
    // We're done applying changes from this batch, resolve pending
    // callbacks
    const { pendingRequests } = dataProviderController.rootCache;
    Object.entries(pendingRequests).forEach(([page, callback]) => {
      const lastAvailablePage = grid.size ? Math.ceil(grid.size / grid.pageSize) - 1 : 0;
      // It's possible that the lastRequestedRange includes a page that's beyond lastAvailablePage if the grid's size got reduced during an ongoing data request
      const lastRequestedRangeEnd = Math.min(lastRequestedRange[1], lastAvailablePage);
      // Resolve if we have data or if we don't expect to get data
      if (cache[page]) {
        // Cached data is available, resolve the callback
        callback(cache[page]);
      } else if (page < lastRequestedRange[0] || +page > lastRequestedRangeEnd) {
        // No cached data, resolve the callback with an empty array
        callback(new Array(grid.pageSize));
        // Request grid for content update
        grid.requestContentUpdate();
      } else if (callback && grid.size === 0) {
        // The grid has 0 items => resolve the callback with an empty array
        callback([]);
      }
    });

    // If all pending requests have already been resolved (which can happen
    // for example if the server sent preloaded data while the grid had
    // already made its own requests), cancel the request debouncer to
    // prevent further unnecessary calls.
    if (Object.keys(pendingRequests).length === 0) {
      rootRequestDebouncer?.cancel();
      lastRequestedRange = [-1, -1];
    }

    // Let server know we're done
    grid.$server.confirmUpdate(id);
  };

  grid.$connector.setSelectionMode = function (mode) {
    if ((typeof mode === 'string' || mode instanceof String) && validSelectionModes.indexOf(mode) >= 0) {
      selectionMode = mode;
      selectedKeys = {};
      grid.selectedItems = [];
      grid.$connector.updateMultiSelectable();
    } else {
      throw 'Attempted to set an invalid selection mode';
    }
  };

  /*
   * Manage aria-multiselectable attribute depending on the selection mode.
   * see more: https://github.com/vaadin/web-components/issues/1536
   * or: https://www.w3.org/TR/wai-aria-1.1/#aria-multiselectable
   * For selection mode SINGLE, set the aria-multiselectable attribute to false
   */
  grid.$connector.updateMultiSelectable = function () {
    if (!grid.$) {
      return;
    }

    if (selectionMode === validSelectionModes[0]) {
      grid.$.table.setAttribute('aria-multiselectable', false);
      // For selection mode NONE, remove the aria-multiselectable attribute
    } else if (selectionMode === validSelectionModes[1]) {
      grid.$.table.removeAttribute('aria-multiselectable');
      // For selection mode MULTI, set aria-multiselectable to true
    } else {
      grid.$.table.setAttribute('aria-multiselectable', true);
    }
  };

  // Have the multi-selectable state updated on attach
  grid._createPropertyObserver('isAttached', () => grid.$connector.updateMultiSelectable());

  const singleTimeRenderer = (renderer) => {
    return (root) => {
      if (renderer) {
        renderer(root);
        renderer = null;
      }
    };
  };

  grid.$connector.setHeaderRenderer = function (column, options) {
    const { content, showSorter, sorterPath } = options;

    if (content === null) {
      column.headerRenderer = null;
      return;
    }

    column.headerRenderer = singleTimeRenderer((root) => {
      // Clear previous contents
      root.innerHTML = '';
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
      // Add content
      if (content instanceof Node) {
        contentRoot.appendChild(content);
      } else {
        contentRoot.textContent = content;
      }
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

    column.footerRenderer = singleTimeRenderer((root) => {
      // Clear previous contents
      root.innerHTML = '';
      // Add content
      if (content instanceof Node) {
        root.appendChild(content);
      } else {
        root.textContent = content;
      }
    });
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
      .slice(0)
      .pop()
      .filter((c) => c._flowId)
      .sort((b, a) => b._order - a._order)
      .map((c) => c._flowId);

    grid.dispatchEvent(
      new CustomEvent('column-reorder-all-columns', {
        detail: { columns }
      })
    );
  });

  grid.addEventListener('cell-focus', (e) => {
    const eventContext = grid.getEventContext(e);
    const expectedSectionValues = ['header', 'body', 'footer'];

    if (expectedSectionValues.indexOf(eventContext.section) === -1) {
      return;
    }

    grid.dispatchEvent(
      new CustomEvent('grid-cell-focus', {
        detail: {
          itemKey: eventContext.item ? eventContext.item.key : null,

          internalColumnId: eventContext.column ? eventContext.column._flowId : null,

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
    if (grid._isSelected(e.detail.draggedItems[0])) {
      // Dragging selected (possibly multiple) items
      if (grid.__selectionDragData) {
        Object.keys(grid.__selectionDragData).forEach((type) => {
          e.detail.setDragData(type, grid.__selectionDragData[type]);
        });
      } else {
        (grid.__dragDataTypes || []).forEach((type) => {
          e.detail.setDragData(type, e.detail.draggedItems.map((item) => item.dragData[type]).join('\n'));
        });
      }

      if (grid.__selectionDraggedItemsCount > 1) {
        e.detail.setDraggedItemsCount(grid.__selectionDraggedItemsCount);
      }
    } else {
      // Dragging just one (non-selected) item
      (grid.__dragDataTypes || []).forEach((type) => {
        e.detail.setDragData(type, e.detail.draggedItems[0].dragData[type]);
      });
    }
  });

  grid.isItemSelectable = (item) => {
    // If there is no selectable data, assume the item is selectable
    return item?.selectable === undefined || item.selectable;
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
