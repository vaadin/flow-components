// @ts-nocheck
import { Debouncer } from '@polymer/polymer/lib/utils/debounce.js';
import { timeOut, animationFrame } from '@polymer/polymer/lib/utils/async.js';
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

  dataProviderController.ensureFlatIndexHierarchyOriginal = dataProviderController.ensureFlatIndexHierarchy;
  dataProviderController.ensureFlatIndexHierarchy = function (flatIndex) {
    const { item } = this.getFlatIndexContext(flatIndex);
    if (!item || !this.isExpanded(item)) {
      return;
    }

    const isCached = grid.$connector.hasCacheForParentKey(grid.getItemId(item));
    if (isCached) {
      // The sub-cache items are already in the connector's cache. Skip the debouncing process.
      this.ensureFlatIndexHierarchyOriginal(flatIndex);
    } else {
      grid.$connector.beforeEnsureFlatIndexHierarchy(flatIndex, item);
    }
  };

  dataProviderController.getItemSubCache = function (item) {
    return this.getItemContext(item)?.subCache;
  };

  let cache = {};

  let parentRequestQueue = [];
  let parentRequestDebouncer;
  let ensureSubCacheQueue = [];
  let ensureSubCacheDebouncer;

  const rootRequestDelay = 150;
  let rootRequestDebouncer;

  let lastRequestedRanges = {};
  const root = 'null';
  lastRequestedRanges[root] = [0, 0];

  const validSelectionModes = ['SINGLE', 'NONE', 'MULTI'];
  let selectedKeys = {};
  let selectionMode = 'SINGLE';

  let sorterDirectionsSetFromServer = false;

  grid.itemIdPath = 'key';

  function createEmptyItemFromKey(key) {
    return { [grid.itemIdPath]: key };
  }

  grid.$connector = {};

  grid.$connector.hasCacheForParentKey = (parentKey) => cache[parentKey]?.size !== undefined;

  grid.$connector.hasEnsureSubCacheQueue = () => ensureSubCacheQueue.length > 0;

  grid.$connector.hasParentRequestQueue = () => parentRequestQueue.length > 0;

  grid.$connector.hasRootRequestQueue = () => {
    const { pendingRequests } = dataProviderController.rootCache;
    return Object.keys(pendingRequests).length > 0 || !!rootRequestDebouncer?.isActive();
  };

  grid.$connector.beforeEnsureFlatIndexHierarchy = function (flatIndex, item) {
    // add call to queue
    ensureSubCacheQueue.push({
      flatIndex,
      itemkey: grid.getItemId(item)
    });

    ensureSubCacheDebouncer = Debouncer.debounce(ensureSubCacheDebouncer, animationFrame, () => {
      while (ensureSubCacheQueue.length) {
        grid.$connector.flushEnsureSubCache();
      }
    });
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

  grid.$connector._getSameLevelPage = function (parentKey, currentCache, currentCacheItemIndex) {
    const currentParentKey = currentCache.parentItem ? grid.getItemId(currentCache.parentItem) : root;
    if (currentParentKey === parentKey) {
      // Level match found, return the page number.
      return Math.floor(currentCacheItemIndex / grid.pageSize);
    }
    const { parentCache, parentCacheIndex } = currentCache;
    if (!parentCache) {
      // There is no parent cache to match level
      return null;
    }
    // Traverse the tree upwards until a match is found or the end is reached
    return this._getSameLevelPage(parentKey, parentCache, parentCacheIndex);
  };

  grid.$connector.flushEnsureSubCache = function () {
    const pendingFetch = ensureSubCacheQueue.shift();
    if (pendingFetch) {
      dataProviderController.ensureFlatIndexHierarchyOriginal(pendingFetch.flatIndex);
      return true;
    }
    return false;
  };

  let pendingRequests = {};
  let loadDebouncer;
  let isLoading = false;

  dataProviderController.isLoading = function () {
    return isLoading || Object.keys(pendingRequests).length > 0;
  };

  grid.$connector.getCache = function (parentKey) {
    const parentItem = createEmptyItemFromKey(parentKey);
    return parentKey === root ? dataProviderController.rootCache : dataProviderController.getItemSubCache(parentItem);
  };

  grid.dataProvider = function (params) {
    const { page, parentItem } = params;
    const parentKey = parentItem ? grid.getItemId(parentItem) : root;
    const cache = grid.$connector.getCache(parentKey);

    // Clear pending requests from cache as we manage the cache ourselves
    cache.pendingRequests[page] = null;

    // Skip if the page has already been requested, this avoids rescheduling the debouncer for every single item
    if (pendingRequests[parentKey] === page) {
      return;
    }

    // Otherwise store request and schedule load
    pendingRequests[parentKey] = page;
    const delay = grid._hasData ? rootRequestDelay : 0;
    loadDebouncer = Debouncer.debounce(loadDebouncer, timeOut.after(delay), () => {
      grid.$connector.load();
    });
  };

  grid.$connector.load = function () {
    if (isLoading) {
      return;
    }

    // Get required ranges, excluding those for which data is already in cache
    const ranges = Object.keys(pendingRequests)
      .map((parentKey) => grid.$connector.determineRange(pendingRequests[parentKey], parentKey))
      .filter((range) => range);

    if (ranges.length) {
      // Trigger server-side fetch in data communicator
      isLoading = true;
      const rootRange = ranges.find((range) => range.parentKey === root);
      if (rootRange) {
        grid.$server.setRequestedRange(rootRange.firstIndex, rootRange.size);
      }

      const parentRanges = ranges.filter((range) => range.parentKey !== root);
      if (parentRanges.length) {
        grid.$server.setParentRequestedRanges(parentRanges);
      }
    }
    pendingRequests = {};
  };

  grid.$connector.determineRange = function (page, parentKey) {
    // Adjust the requested page to be within the valid range in case
    // the grid size has changed while fetchPage was debounced.
    if (parentKey === root) {
      page = Math.min(page, Math.floor((grid.size - 1) / grid.pageSize));
    }

    // Determine what to fetch based on scroll position and not only
    // what grid asked for
    const visibleRows = grid._getRenderedRows();
    let start = visibleRows.length > 0 ? visibleRows[0].index : 0;
    let end = visibleRows.length > 0 ? visibleRows[visibleRows.length - 1].index : 0;

    // The buffer size could be multiplied by some constant defined by the user,
    // if he needs to reduce the number of items sent to the Grid to improve performance
    // or to increase it to make Grid smoother when scrolling
    let buffer = end - start;
    let firstNeededIndex = Math.max(0, start - buffer);
    let lastNeededIndex = Math.min(end + buffer, grid._flatSize);

    let pageRange = [null, null];
    for (let idx = firstNeededIndex; idx <= lastNeededIndex; idx++) {
      const { cache, index } = dataProviderController.getFlatIndexContext(idx);
      // Try to match level by going up in hierarchy. The page range should include
      // pages that contain either of the following:
      //   - visible items of the current cache
      //   - same level parents of visible descendant items
      // If the parent items are not considered, Flow would remove the hidden parent
      // items from the current level cache. This can lead to an infinite loop when using
      // scrollToIndex feature.
      const sameLevelPage = grid.$connector._getSameLevelPage(parentKey, cache, index);
      if (sameLevelPage === null) {
        continue;
      }
      pageRange[0] = Math.min(pageRange[0] ?? sameLevelPage, sameLevelPage);
      pageRange[1] = Math.max(pageRange[1] ?? sameLevelPage, sameLevelPage);
    }

    // When the viewport doesn't contain the requested page or it doesn't contain any items from
    // the requested level at all, it means that the scroll position has changed while fetchPage
    // was debounced. For example, it can happen if the user scrolls the grid to the bottom and
    // then immediately back to the top. In this case, the request for the last page will be left
    // hanging. To avoid this, as a workaround, we reset the range to only include the requested page
    // to make sure all hanging requests are resolved. After that, the grid requests the first page
    // or whatever in the viewport again.
    if (pageRange.some((p) => p === null) || page < pageRange[0] || page > pageRange[1]) {
      pageRange = [page, page];
    }

    // Clamp range
    const cache = grid.$connector.getCache(parentKey);
    pageRange[0] = Math.max(pageRange[0], 0);
    pageRange[1] = Math.min(pageRange[1], Math.ceil(cache.size / grid.pageSize) - 1);

    // If all pages are already in cache, return null to indicate that no range request is needed
    for (let page = pageRange[0]; page <= pageRange[1]; page++) {
      const pageIndex = page * grid.pageSize;
      if (!cache.items[pageIndex]) {
        const firstIndex = pageRange[0] * grid.pageSize;
        const size = (pageRange[1] - pageRange[0] + 1) * grid.pageSize;
        return { parentKey, firstIndex, size };
      }
    }
    return null;
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

  grid._updateItem = function (row, item) {
    Grid.prototype._updateItem.call(grid, row, item);

    // There might be inactive component renderers on hidden rows that still refer to the
    // same component instance as one of the renderers on a visible row. Making the
    // inactive/hidden renderer attach the component might steal it from a visible/active one.
    if (!row.hidden) {
      // make sure that component renderers are updated
      Array.from(row.children).forEach((cell) => {
        Array.from(cell?._content?.__templateInstance?.children || []).forEach((content) => {
          if (content._attachRenderedComponentIfAble) {
            content._attachRenderedComponentIfAble();
          }
          // In hierarchy column of tree grid, the component renderer is inside its content,
          // this updates it renderer from innerContent
          Array.from(content?.children || []).forEach((innerContent) => {
            if (innerContent._attachRenderedComponentIfAble) {
              innerContent._attachRenderedComponentIfAble();
            }
          });
        });
      });
    }
    // since no row can be selected when selection mode is NONE
    // if selectionMode is set to NONE, remove aria-selected attribute from the row
    if (selectionMode === validSelectionModes[1]) {
      // selectionMode === NONE
      row.removeAttribute('aria-selected');
      Array.from(row.children).forEach((cell) => cell.removeAttribute('aria-selected'));
    }
  };

  const itemExpandedChanged = function (item, expanded) {
    // method available only for the TreeGrid server-side component
    if (item == undefined || grid.$server.updateExpandedState == undefined) {
      return;
    }
    let parentKey = grid.getItemId(item);
    grid.$server.updateExpandedState(parentKey, expanded);
  };

  // Patch grid.expandItem and grid.collapseItem to have
  // itemExpandedChanged run when either happens.
  grid.expandItem = function (item) {
    itemExpandedChanged(item, true);
    Grid.prototype.expandItem.call(grid, item);
  };

  grid.collapseItem = function (item) {
    itemExpandedChanged(item, false);
    Grid.prototype.collapseItem.call(grid, item);
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
   * @param parentKey the key of the parent item for the page
   * @returns an array of the updated items for the page, or undefined if no items were cached for the page
   */
  const updateGridCache = function (page, parentKey = root) {
    const items = cache[parentKey][page];
    const parentItem = createEmptyItemFromKey(parentKey);

    let gridCache =
      parentKey === root ? dataProviderController.rootCache : dataProviderController.getItemSubCache(parentItem);

    // Force update unless there's a callback waiting
    if (gridCache && !gridCache.pendingRequests[page]) {
      // Update the items in the grid cache or set an array of undefined items
      // to remove the page from the grid cache if there are no corresponding items
      // in the connector cache.
      gridCache.setPage(page, items || Array.from({ length: grid.pageSize }));
    }

    return items;
  };

  /**
   * Updates all visible grid rows in DOM.
   */
  const updateAllGridRowsInDomBasedOnCache = function () {
    updateGridFlatSize();
    grid.__updateVisibleRows();
  };

  /**
   * Updates the <vaadin-grid>'s internal cache size and flat size.
   */
  const updateGridFlatSize = function () {
    dataProviderController.recalculateFlatSize();
    grid._flatSize = dataProviderController.flatSize;
  };

  /**
   * Update the given items in DOM if currently visible.
   *
   * @param array items the items to update in DOM
   */
  const updateGridItemsInDomBasedOnCache = function (items) {
    if (!items || !grid.$ || grid.$.items.childElementCount === 0) {
      return;
    }

    const itemKeys = items.map((item) => item.key);
    const indexes = grid
      ._getRenderedRows()
      .filter((row) => row._item && itemKeys.includes(row._item.key))
      .map((row) => row.index);
    if (indexes.length > 0) {
      grid.__updateVisibleRows(indexes[0], indexes[indexes.length - 1]);
    }
  };

  grid.$connector.set = function (index, items, parentKey) {
    if (index % grid.pageSize != 0) {
      throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
    }

    // Update cache
    const cache = grid.$connector.getCache(parentKey || root);
    const firstPage = index / grid.pageSize;
    const numPages = Math.ceil(items.length / grid.pageSize);

    for (let i = 0; i < numPages; i++) {
      const pageItems = items.slice(i * grid.pageSize, (i + 1) * grid.pageSize);
      cache.setPage(firstPage + i, pageItems);
    }

    // Update client-side state
    grid.$connector.doSelection(items.filter((item) => item.selected));
    grid.$connector.doDeselection(items.filter((item) => !item.selected && selectedKeys[item.key]));
    itemsUpdated(items);
  };

  const itemToCacheLocation = function (item) {
    let parent = item.parentUniqueKey || root;
    if (cache[parent]) {
      for (let page in cache[parent]) {
        for (let index in cache[parent][page]) {
          if (grid.getItemId(cache[parent][page][index]) === grid.getItemId(item)) {
            return { page: page, index: index, parentKey: parent };
          }
        }
      }
    }
    return null;
  };

  /**
   * Updates the given items for a hierarchical grid.
   *
   * @param updatedItems the updated items array
   */
  grid.$connector.updateHierarchicalData = function (updatedItems) {
    let pagesToUpdate = [];
    // locate and update the items in cache
    // find pages that need updating
    for (let i = 0; i < updatedItems.length; i++) {
      let cacheLocation = itemToCacheLocation(updatedItems[i]);
      if (cacheLocation) {
        cache[cacheLocation.parentKey][cacheLocation.page][cacheLocation.index] = updatedItems[i];
        let key = cacheLocation.parentKey + ':' + cacheLocation.page;
        if (!pagesToUpdate[key]) {
          pagesToUpdate[key] = {
            parentKey: cacheLocation.parentKey,
            page: cacheLocation.page
          };
        }
      }
    }
    // IE11 doesn't work with the transpiled version of the forEach.
    let keys = Object.keys(pagesToUpdate);
    for (let i = 0; i < keys.length; i++) {
      let pageToUpdate = pagesToUpdate[keys[i]];
      const affectedUpdatedItems = updateGridCache(pageToUpdate.page, pageToUpdate.parentKey);
      if (affectedUpdatedItems) {
        itemsUpdated(affectedUpdatedItems);
        updateGridItemsInDomBasedOnCache(affectedUpdatedItems);
      }
    }
  };

  /**
   * Updates the given items for a non-hierarchical grid.
   *
   * @param updatedItems the updated items array
   */
  grid.$connector.updateFlatData = function (updatedItems) {
    // Update cache
    updatedItems.forEach((item) => {
      const context = dataProviderController.getItemContext(item);
      if (context) {
        context.cache.items[context.index] = item;
      }
    });
    // Update client-side state and rerender
    itemsUpdated(updatedItems);
    grid.requestContentUpdate();
  };

  grid.$connector.clearExpanded = function () {
    grid.expandedItems = [];
    ensureSubCacheQueue = [];
    parentRequestQueue = [];
  };

  grid.$connector.clear = function (index, length, parentKey) {
    if (index % grid.pageSize != 0) {
      throw 'Got cleared data for index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
    }

    const cache = grid.$connector.getCache(parentKey || root);
    const endIndex = index + length;
    const items = cache.items.slice(index, endIndex).filter((item) => item);

    // Cleanup client-side state
    grid.$connector.doDeselection(items.filter((item) => selectedKeys[item.key]));
    items.forEach((item) => grid.closeItemDetails(item));

    // Clear the items from the cache
    for (let itemIndex = index; itemIndex < endIndex; itemIndex++) {
      delete cache.items[itemIndex];
      cache.removeSubCache(itemIndex);
    }

    // TODO: Check if necessary, clear does not change size and the following confirm should update flat size anyway
    updateGridFlatSize();
  };

  grid.$connector.reset = function () {
    cache = {};
    dataProviderController.clearCache();
    lastRequestedRanges = {};
    ensureSubCacheDebouncer?.cancel();
    parentRequestDebouncer?.cancel();
    rootRequestDebouncer?.cancel();
    ensureSubCacheQueue = [];
    parentRequestQueue = [];
    updateAllGridRowsInDomBasedOnCache();
  };

  grid.$connector.updateUniqueItemIdPath = (path) => (grid.itemIdPath = path);

  grid.$connector.expandItems = function (items) {
    let newExpandedItems = Array.from(grid.expandedItems);
    items.filter((item) => !grid._isExpanded(item)).forEach((item) => newExpandedItems.push(item));
    grid.expandedItems = newExpandedItems;
  };

  grid.$connector.collapseItems = function (items) {
    let newExpandedItems = Array.from(grid.expandedItems);
    items.forEach((item) => {
      let index = grid._getItemIndexInArray(item, newExpandedItems);
      if (index >= 0) {
        newExpandedItems.splice(index, 1);
      }
    });
    grid.expandedItems = newExpandedItems;
    items.forEach((item) => grid.$connector.removeFromQueue(item));
  };

  grid.$connector.removeFromQueue = function (item) {
    // The page callbacks for the given item are about to be discarded ->
    // Resolve the callbacks with an empty array to not leave grid in loading state
    const itemSubCache = dataProviderController.getItemSubCache(item);
    Object.values(itemSubCache?.pendingRequests || {}).forEach((callback) => callback([]));

    const itemId = grid.getItemId(item);
    ensureSubCacheQueue = ensureSubCacheQueue.filter((item) => item.itemkey !== itemId);
    parentRequestQueue = parentRequestQueue.filter((item) => item.parentKey !== itemId);
  };

  grid.$connector.confirmParent = function (id, parentKey, levelSize) {
    // Create connector cache if it doesn't exist
    if (!cache[parentKey]) {
      cache[parentKey] = {};
    }
    // Update connector cache size
    const hasSizeChanged = cache[parentKey].size !== levelSize;
    cache[parentKey].size = levelSize;
    if (levelSize === 0) {
      cache[parentKey][0] = [];
    }

    const parentItem = createEmptyItemFromKey(parentKey);
    const parentItemSubCache = dataProviderController.getItemSubCache(parentItem);
    if (parentItemSubCache) {
      // If grid has pending requests for this parent, then resolve them
      // and let grid update the flat size and re-render.
      const { pendingRequests } = parentItemSubCache;
      Object.entries(pendingRequests).forEach(([page, callback]) => {
        let lastRequestedRange = lastRequestedRanges[parentKey] || [0, 0];

        if (
          (cache[parentKey] && cache[parentKey][page]) ||
          page < lastRequestedRange[0] ||
          page > lastRequestedRange[1]
        ) {
          let items = cache[parentKey][page] || new Array(levelSize);
          callback(items, levelSize);
        } else if (callback && levelSize === 0) {
          // The parent item has 0 child items => resolve the callback with an empty array
          callback([], levelSize);
        }
      });

      // If size has changed, and there are no pending requests, then
      // manually update the size of the grid cache and update the effective
      // size, effectively re-rendering the grid. This is necessary when
      // individual items are refreshed on the server, in which case there
      // is no loading request from the grid itself. In that case, if
      // children were added or removed, the grid will not be aware of it
      // unless we manually update the size.
      if (hasSizeChanged && Object.keys(pendingRequests).length === 0) {
        parentItemSubCache.size = levelSize;
        updateGridFlatSize();
      }
    }

    // Let server know we're done
    grid.$server.confirmParentUpdate(id, parentKey);
  };

  grid.$connector.confirm = function (id) {
    // Reproduce controller behavior when it receives a page from data provider
    // This should update loading state, trigger a rerender and potentially schedule a new load if items are missing
    // TODO: Add tests for triggering follow up loading
    dataProviderController.recalculateFlatSize();
    dataProviderController.dispatchEvent(new CustomEvent('page-received'));
    isLoading = false;
    dataProviderController.dispatchEvent(new CustomEvent('page-loaded'));

    // Let server know we're done
    grid.$server.confirmUpdate(id);
  };

  grid.$connector.ensureHierarchy = function () {
    for (let parentKey in cache) {
      if (parentKey !== root) {
        delete cache[parentKey];
      }
    }

    lastRequestedRanges = {};

    dataProviderController.rootCache.removeSubCaches();

    updateAllGridRowsInDomBasedOnCache();
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

  grid.__applySorters = () => {
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
    Grid.prototype.__applySorters.call(grid);

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
    const content = path.slice(0, idx);

    // Do not fire item click event if cell content contains focusable elements.
    // Use this instead of event.target to detect cases like icon inside button.
    // See https://github.com/vaadin/flow-components/issues/4065
    if (content.some((node) => isFocusable(node) || node instanceof HTMLLabelElement)) {
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

  grid.cellClassNameGenerator = function (column, rowData) {
    const style = rowData.item.style;
    if (!style) {
      return;
    }
    return (style.row || '') + ' ' + ((column && style[column._flowId]) || '');
  };

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
};
