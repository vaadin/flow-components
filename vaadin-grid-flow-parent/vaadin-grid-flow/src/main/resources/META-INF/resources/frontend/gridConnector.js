window.Vaadin.Flow.gridConnector = {
  initLazy: function(grid) {
    // Check whether the connector was already initialized for the grid
    if (grid.$connector){
      return;
    }

    Vaadin.Grid.ItemCache.prototype.ensureSubCacheForScaledIndex = function(scaledIndex) {
      if (!this.itemCaches[scaledIndex]) {

        if(ensureSubCacheDelay) {
          this.grid.$connector.beforeEnsureSubCacheForScaledIndex(this, scaledIndex);
        } else {
          this.doEnsureSubCacheForScaledIndex(scaledIndex);
        }
      }
    }

    Vaadin.Grid.ItemCache.prototype.doEnsureSubCacheForScaledIndex = function(scaledIndex) {
      if (!this.itemCaches[scaledIndex]) {
        const subCache = new Vaadin.Grid.ItemCache(this.grid, this, this.items[scaledIndex]);
        subCache.itemkeyCaches = {};
        if(!this.itemkeyCaches) {
          this.itemkeyCaches = {};
        }
        this.itemCaches[scaledIndex] = subCache;
        this.itemkeyCaches[this.grid.getItemId(subCache.parentItem)] = subCache;
        this.grid._loadPage(0, subCache);
      }
    }

    Vaadin.Grid.ItemCache.prototype.getCacheAndIndexByKey = function(key) {
      for (let index in this.items) {
        if(grid.getItemId(this.items[index]) === key) {
          return {cache: this, scaledIndex: index};
        }
      }
      const keys = Object.keys(this.itemkeyCaches);
      for (let i = 0; i < keys.length; i++) {
        const expandedKey = keys[i];
        const subCache = this.itemkeyCaches[expandedKey];
        let cacheAndIndex = subCache.getCacheAndIndexByKey(key);
        if(cacheAndIndex) {
          return cacheAndIndex;
        }
      }
      return undefined;
    }

    Vaadin.Grid.ItemCache.prototype.getLevel = function() {
      let cache = this;
      let level = 0;
      while (cache.parentCache) {
        cache = cache.parentCache;
        level++;
      }
      return level;
    }

    const rootPageCallbacks = {};
    const treePageCallbacks = {};
    const cache = {};

    /* ensureSubCacheDelay - true optimizes scrolling performance by adding small
    *  delay between each first page fetch of expanded item.
    *  Disable by setting to false.
    */
    const ensureSubCacheDelay = true;

    /* parentRequestDelay - optimizes parent requests by batching several requests
    *  into one request. Delay in milliseconds. Disable by setting to 0.
    *  parentRequestBatchMaxSize - maximum size of the batch.
    */
    const parentRequestDelay = 20;
    const parentRequestBatchMaxSize = 20;

    let parentRequestQueue = [];
    let parentRequestDebouncer;
    let ensureSubCacheQueue = [];
    let ensureSubCacheDebouncer;

    let lastRequestedRanges = {};
    const root = 'null';
    lastRequestedRanges[root] = [0, 0];

    const validSelectionModes = ['SINGLE', 'NONE', 'MULTI'];
    let selectedKeys = {};
    let selectionMode = 'SINGLE';

    let detailsVisibleOnClick = true;

    let sorterDirectionsSetFromServer = false;

    grid.size = 0; // To avoid NaN here and there before we get proper data
    grid.itemIdPath = 'key';
    
    grid.$connector = {};

    grid.$connector.hasEnsureSubCacheQueue = function() {
        return ensureSubCacheQueue.length > 0;
    }

    grid.$connector.hasParentRequestQueue = function() {
        return parentRequestQueue.length > 0;
    }
    
    grid.$connector.beforeEnsureSubCacheForScaledIndex = function(targetCache, scaledIndex) {
      // add call to queue
      ensureSubCacheQueue.push({
        cache: targetCache,
        scaledIndex: scaledIndex,
        itemkey: grid.getItemId(targetCache.items[scaledIndex]),
        level: targetCache.getLevel()
      });
      // sort by ascending scaledIndex and level
      ensureSubCacheQueue.sort(function(a, b) {
        return a.scaledIndex - b.scaledIndex || a.level - b.level;
      });
      if(!ensureSubCacheDebouncer) {
          grid.$connector.flushQueue(
            (debouncer) => ensureSubCacheDebouncer = debouncer,
            () => grid.$connector.hasEnsureSubCacheQueue(),
            () => grid.$connector.flushEnsureSubCache(),
            (action) => Polymer.Debouncer.debounce(ensureSubCacheDebouncer, Polymer.Async.animationFrame, action));
      }
    }

    grid.$connector.doSelection = function(item, userOriginated) {
      if (selectionMode === 'NONE') {
        return;
      }
      if (userOriginated && grid.hasAttribute('disabled')) {
          return;
      }
      if (selectionMode === 'SINGLE') {
        grid.selectedItems = [];
        selectedKeys = {};
      }
      grid.selectItem(item);
      selectedKeys[item.key] = item;
      if (userOriginated) {
          item.selected = true;
          grid.$server.select(item.key);
      } else {
          grid.fire('select', {item: item, userOriginated: userOriginated});
      }

      if (selectionMode === 'MULTI' && arguments.length > 2) {
          for (i = 2; i < arguments.length; i++) {
              grid.$connector.doSelection(arguments[i], userOriginated);
          }
      }
    };

    grid.$connector.doDeselection = function(item, userOriginated) {
      if (userOriginated && grid.hasAttribute('disabled')) {
        return;
      }

      if (selectionMode === 'SINGLE' || selectionMode === 'MULTI') {
        grid.deselectItem(item);
        delete selectedKeys[item.key];
        if (userOriginated) {
          delete item.selected;
          grid.$server.deselect(item.key);
        } else {
          grid.fire('deselect', {item: item, userOriginated: userOriginated});
        }
      }

      if (selectionMode === 'MULTI' && arguments.length > 2) {
          for (i = 2; i < arguments.length; i++) {
              grid.$connector.doDeselection(arguments[i], userOriginated);
          }
      }
    };

    grid.__activeItemChanged = function(newVal, oldVal) {
      if (selectionMode != 'SINGLE') {
        return;
      }
      if (!newVal) {
        if (oldVal && selectedKeys[oldVal.key]) {
          grid.$connector.doDeselection(oldVal, true);
        }
        return;
      }
      if (!selectedKeys[newVal.key]) {
        grid.$connector.doSelection(newVal, true);
      } else {
        grid.$connector.doDeselection(newVal, true);
      }
    };
    grid._createPropertyObserver('activeItem', '__activeItemChanged', true);

    grid.__activeItemChangedDetails = function(newVal, oldVal) {
      if(!detailsVisibleOnClick) {
        return;
      }
      if (newVal && !newVal.detailsOpened) {
        grid.$server.setDetailsVisible(newVal.key);
      } else {
        grid.$server.setDetailsVisible(null);
      }
    }
    grid._createPropertyObserver('activeItem', '__activeItemChangedDetails', true);

    grid.$connector.setDetailsVisibleOnClick = function(visibleOnClick) {
      detailsVisibleOnClick = visibleOnClick;
    };

    grid.$connector._getPageIfSameLevel = function(parentKey, index, defaultPage) {
      let cacheAndIndex = grid._cache.getCacheAndIndex(index);
      let parentItem = cacheAndIndex.cache.parentItem;
      let parentKeyOfIndex = (parentItem) ? grid.getItemId(parentItem) : root;
      if(parentKey !== parentKeyOfIndex) {
        return defaultPage;
      } else {
        return grid._getPageForIndex(cacheAndIndex.scaledIndex);
      }
    }

    grid.$connector.getCacheByKey = function(key) {
      let cacheAndIndex = grid._cache.getCacheAndIndexByKey(key);
      if(cacheAndIndex) {
        return cacheAndIndex.cache;
      }
      return undefined;
    }

    grid.$connector.flushQueue = function(timeoutIdSetter, hasQueue, flush, startTimeout) {
      if(!hasQueue()) {
        timeoutIdSetter(undefined);
        return;
      }
      if(flush()) {
          timeoutIdSetter(startTimeout(() =>
            grid.$connector.flushQueue(timeoutIdSetter, hasQueue, flush, startTimeout)));
      } else {
        grid.$connector.flushQueue(timeoutIdSetter, hasQueue, flush, startTimeout);
      }
    }

    grid.$connector.flushEnsureSubCache = function() {
      let fetched = false;
      let pendingFetch = ensureSubCacheQueue.splice(0, 1)[0];
      let itemkey =  pendingFetch.itemkey;

      let start = grid._virtualStart;
      let end = grid._virtualEnd;
      let buffer = end - start;
      let firstNeededIndex = Math.max(0, start + grid._vidxOffset - buffer);
      let lastNeededIndex = Math.min(end + grid._vidxOffset + buffer, grid._virtualCount);

      // only fetch if given item is still in visible range
      for(let index = firstNeededIndex; index <= lastNeededIndex; index++) {
        let item = grid._cache.getItemForIndex(index);

        if(grid.getItemId(item) === itemkey) {
          if(grid._isExpanded(item)) {
            pendingFetch.cache.doEnsureSubCacheForScaledIndex(pendingFetch.scaledIndex);
            return true;
          } else {
            break;
          }
        }
      }
      return false;
    }

    grid.$connector.flushParentRequests = function() {
      let pendingFetches = parentRequestQueue.splice(0, parentRequestBatchMaxSize);

      if(pendingFetches.length) {
          grid.$server.setParentRequestedRanges(pendingFetches);
          return true;
      }
      return false;
    }

    grid.$connector.beforeParentRequest = function(firstIndex, size, parentKey) {
      if(parentRequestDelay > 0) {
        // add request in queue
        parentRequestQueue.push({
          firstIndex: firstIndex,
          size: size,
          parentKey: parentKey
        });

        if(!parentRequestDebouncer) {
            grid.$connector.flushQueue(
              (debouncer) => parentRequestDebouncer = debouncer,
              () => grid.$connector.hasParentRequestQueue(),
              () => grid.$connector.flushParentRequests(),
              (action) => Polymer.Debouncer.debounce(parentRequestDebouncer, Polymer.Async.timeOut.after(parentRequestDelay), action)
              );
        }

      } else {
        grid.$server.setParentRequestedRange(firstIndex, size, parentKey);
      }
    }

    grid.$connector.fetchPage = function(fetch, page, parentKey) {
      // Determine what to fetch based on scroll position and not only
      // what grid asked for

      // The buffer size could be multiplied by some constant defined by the user,
      // if he needs to reduce the number of items sent to the Grid to improve performance
      // or to increase it to make Grid smoother when scrolling
      let start = grid._virtualStart;
      let end = grid._virtualEnd;
      let buffer = end - start;

      let firstNeededIndex = Math.max(0, start + grid._vidxOffset - buffer);
      let lastNeededIndex = Math.min(end + grid._vidxOffset + buffer, grid._virtualCount);

      let firstNeededPage = page;
      let lastNeededPage = page;
      for(let idx = firstNeededIndex; idx <= lastNeededIndex; idx++) {
        firstNeededPage = Math.min(firstNeededPage, grid.$connector._getPageIfSameLevel(parentKey, idx, firstNeededPage));
        lastNeededPage = Math.max(lastNeededPage, grid.$connector._getPageIfSameLevel(parentKey, idx, lastNeededPage));
      }

      let firstPage = Math.max(0,  firstNeededPage);
      let lastPage = (parentKey !== root) ? lastNeededPage: Math.min(lastNeededPage, Math.floor(grid.size / grid.pageSize));
      let lastRequestedRange = lastRequestedRanges[parentKey];
      if(!lastRequestedRange) {
        lastRequestedRange = [-1, -1];
      }
      if (lastRequestedRange[0] != firstPage || lastRequestedRange[1] != lastPage) {
        lastRequestedRange = [firstPage, lastPage];
        lastRequestedRanges[parentKey] = lastRequestedRange;
        let count = lastPage - firstPage + 1;
        fetch(firstPage * grid.pageSize, count * grid.pageSize);
      }
    }

    grid.dataProvider = function(params, callback) {
      if (params.pageSize != grid.pageSize) {
        throw 'Invalid pageSize';
      }

      let page = params.page;

      if(params.parentItem) {
        let parentUniqueKey = grid.getItemId(params.parentItem);
        if(!treePageCallbacks[parentUniqueKey]) {
          treePageCallbacks[parentUniqueKey] = {};
        }

        let parentCache = grid.$connector.getCacheByKey(parentUniqueKey);
        let itemCache = (parentCache && parentCache.itemkeyCaches) ? parentCache.itemkeyCaches[parentUniqueKey] : undefined;
        if(cache[parentUniqueKey] && cache[parentUniqueKey][page] && itemCache) {
          // workaround: sometimes grid-element gives page index that overflows
          page = Math.min(page, Math.floor(itemCache.size / grid.pageSize));

          callback(cache[parentUniqueKey][page], itemCache.size);
        } else {
          treePageCallbacks[parentUniqueKey][page] = callback;
        }
        grid.$connector.fetchPage((firstIndex, size) =>
            grid.$connector.beforeParentRequest(firstIndex, size, params.parentItem.key),
            page, parentUniqueKey);

      } else {
        // workaround: sometimes grid-element gives page index that overflows
        page = Math.min(page, Math.floor(grid.size / grid.pageSize));

        if (cache[root] && cache[root][page]) {
          callback(cache[root][page]);
        } else {
          rootPageCallbacks[page] = callback;
        }

        grid.$connector.fetchPage((firstIndex, size) => grid.$server.setRequestedRange(firstIndex, size), page, root);
      }
    }

    const sorterChangeListener = function() {
      if (!sorterDirectionsSetFromServer) {
        grid.$server.sortersChanged(grid._sorters.map(function(sorter) {
          return {
            path: sorter.path,
            direction: sorter.direction
          };
        }));
      }
    }

    grid.$connector.setSorterDirections = function(directions) {
      try {
        sorterDirectionsSetFromServer = true;

        let allSorters = grid.querySelectorAll("vaadin-grid-sorter");
        allSorters.forEach(sorter => sorter.direction = null);

        for (let i = directions.length - 1; i >= 0; i--) {
          const columnId = directions[i].column;
          let sorter = grid.querySelector("vaadin-grid-sorter[path='" + columnId + "']");
          if (sorter) {
            sorter.direction = directions[i].direction;
          }
        }
      } finally {
        sorterDirectionsSetFromServer = false;
      }
    }
    grid._createPropertyObserver("_previousSorters", sorterChangeListener);

    grid._updateItem = function(row, item) {
      Vaadin.GridElement.prototype._updateItem.call(grid, row, item);

      // make sure that component renderers are updated
      Array.from(row.children).forEach(cell => {
        if(cell._instance && cell._instance.children) {
          Array.from(cell._instance.children).forEach(content => {
            if(content._attachRenderedComponentIfAble) {
              content._attachRenderedComponentIfAble();
            }
          });
        }
      });
    }

    grid._expandedInstanceChangedCallback = function(inst, value) {
      // method available only for the TreeGrid server-side component
      if (inst.item == undefined || grid.$server.updateExpandedState == undefined) {
        return;
      }
      let parentKey = grid.getItemId(inst.item);
      grid.$server.updateExpandedState(parentKey, value);
      if (value) {
        this.expandItem(inst.item);
      } else {
        delete cache[parentKey];
        let parentCache = grid.$connector.getCacheByKey(parentKey);
        if (parentCache && parentCache.itemkeyCaches && parentCache.itemkeyCaches[parentKey]) {
          delete parentCache.itemkeyCaches[parentKey];
        }
        if (parentCache && parentCache.itemCaches && parentCache.itemCaches[parentKey]) {
          delete parentCache.itemCaches[parentKey];
        }
        delete lastRequestedRanges[parentKey];

        this.collapseItem(inst.item);
      }
    }

    const itemsUpdated = function(items) {
      if (!items || !Array.isArray(items)) {
        throw 'Attempted to call itemsUpdated with an invalid value: ' + JSON.stringify(items);
      }
      let detailsOpenedItems = Array.from(grid.detailsOpenedItems);
      let updatedSelectedItem = false;
      for (let i = 0; i < items.length; ++i) {
        const item = items[i];
        if(!item) {
          continue;
        }
        if (item.detailsOpened) {
          if(grid._getItemIndexInArray(item, detailsOpenedItems) < 0) {
            detailsOpenedItems.push(item);
          }
        } else if(grid._getItemIndexInArray(item, detailsOpenedItems) >= 0) {
          detailsOpenedItems.splice(grid._getItemIndexInArray(item, detailsOpenedItems), 1)
        }
        if (selectedKeys[item.key]) {
          selectedKeys[item.key] = item;
          item.selected = true;
          updatedSelectedItem = true;
        }
      }
      grid.detailsOpenedItems = detailsOpenedItems;
      if (updatedSelectedItem) {
        // IE 11 Object doesn't support method values
        grid.selectedItems = Object.keys(selectedKeys).map(function(e) {
          return selectedKeys[e]
        });
      }
    }

    /**
     * Updates the cache for the given page for grid or tree-grid.
     *
     * @param page index of the page to update
     * @param parentKey the key of the parent item for the page
     * @returns an array of the updated items for the page, or undefined if no items were cached for the page
     */
    const updateGridCache = function(page, parentKey) {
      let items;
      if((parentKey || root) !== root) {
        items = cache[parentKey][page];
        let parentCache = grid.$connector.getCacheByKey(parentKey);
        if(parentCache && parentCache.itemkeyCaches) {
          let _cache = parentCache.itemkeyCaches[parentKey];
          _updateGridCache(page, items,
            treePageCallbacks[parentKey][page],
            _cache);
        }

      } else {
        items = cache[root][page];
        _updateGridCache(page, items, rootPageCallbacks[page], grid._cache);
      }
      return items;
    };

    const _updateGridCache = function(page, items, callback, levelcache) {
      // Force update unless there's a callback waiting
      if (!callback) {
        let rangeStart = page * grid.pageSize;
        let rangeEnd = rangeStart + grid.pageSize;
        if (!items) {
          if (levelcache && levelcache.items) {
            for (let idx = rangeStart; idx < rangeEnd; idx++) {
              delete levelcache.items[idx];
            }
          }
        } else {
          if (levelcache && levelcache.items) {
            for (let idx = rangeStart; idx < rangeEnd; idx++) {
              if (levelcache.items[idx]) {
                levelcache.items[idx] = items[idx - rangeStart];
              }
            }
          }
        }
      }
    };

    /**
     * Updates all visible grid rows in DOM.
     */
    const updateAllGridRowsInDomBasedOnCache = function () {
      grid._assignModels();
    }

    /**
     * Update the given items in DOM if currently visible.
     *
     * @param array items the items to update in DOM
     */
    const updateGridItemsInDomBasedOnCache = function(items) {
      if (!items) {
        return;
      }
      /**
       * Calls the _assignModels function from GridScrollerElement, that triggers
       * the internal revalidation of the items based on the _cache of the DataProviderMixin.
       * First mapping the item to physical (iron list) indexes, so that we update
       * only items in with the correct index that are cached in the iron list.
       */
      const itemKeys = items.map(item => item.key);
      const indexes = grid._physicalItems
          .map((tr, index) => tr._item && tr._item.key && itemKeys.indexOf(tr._item.key) > -1 ? index : null)
          .filter(idx => idx !== null);
      if (indexes.length > 0) {
        grid._assignModels(indexes);
      }
    };

    grid.$connector.set = function(index, items, parentKey) {
      if (index % grid.pageSize != 0) {
        throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
      }
      let pkey = parentKey || root;

      const firstPage = index / grid.pageSize;
      const updatedPageCount = Math.ceil(items.length / grid.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPage + i;
        let slice = items.slice(i * grid.pageSize, (i + 1) * grid.pageSize);
        if(!cache[pkey]) {
          cache[pkey] = {};
        }
        cache[pkey][page] = slice;
        for(let j = 0; j < slice.length; j++) {
          let item = slice[j]
          if (item.selected && !isSelectedOnGrid(item)) {
            grid.$connector.doSelection(item);
          } else if (!item.selected && (selectedKeys[item.key] || isSelectedOnGrid(item))) {
            grid.$connector.doDeselection(item);
          }
        }
        const updatedItems = updateGridCache(page, pkey);
        if (updatedItems) {
          itemsUpdated(updatedItems);
          updateGridItemsInDomBasedOnCache(updatedItems);
        }
      }
    };

    const itemToCacheLocation = function(item) {
      let parent = item.parentUniqueKey || root;
      if(cache[parent]) {
        for (let page in cache[parent]) {
          for (let index in cache[parent][page]) {
            if (grid.getItemId(cache[parent][page][index]) === grid.getItemId(item)) {
              return {page: page, index: index, parentKey: parent};
            }
          }
        }
      }
      return null;
    }

    /**
     * Updates the given items for a hierarchical grid.
     *
     * @param updatedItems the updated items array
     */
    grid.$connector.updateHierarchicalData = function(updatedItems) {
      let pagesToUpdate = [];
      // locate and update the items in cache
      // find pages that need updating
      for (let i = 0; i < updatedItems.length; i++) {
        let cacheLocation = itemToCacheLocation(updatedItems[i]);
        if (cacheLocation) {
          cache[cacheLocation.parentKey][cacheLocation.page][cacheLocation.index] = updatedItems[i];
          let key = cacheLocation.parentKey+':'+cacheLocation.page;
          if (!pagesToUpdate[key]) {
            pagesToUpdate[key] = {parentKey: cacheLocation.parentKey, page: cacheLocation.page};
          }
        }
      }
      // IE11 doesn't work with the transpiled version of the forEach.
      let keys = Object.keys(pagesToUpdate);
      for (var i = 0; i < keys.length; i++) {
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
    grid.$connector.updateFlatData = function(updatedItems) {
      // update (flat) caches
      for (let i = 0; i < updatedItems.length; i++) {
        let cacheLocation = itemToCacheLocation(updatedItems[i]);
        if (cacheLocation) {
          // update connector cache
          cache[cacheLocation.parentKey][cacheLocation.page][cacheLocation.index] = updatedItems[i];

          // update grid's cache
          const index = parseInt(cacheLocation.page) * grid.pageSize + parseInt(cacheLocation.index);
          if (grid._cache.items[index]) {
            grid._cache.items[index] = updatedItems[i];
          }
        }
      }
      itemsUpdated(updatedItems);

      updateGridItemsInDomBasedOnCache(updatedItems);
    };

    grid.$connector.clearExpanded = function() {
      grid.expandedItems = [];
      ensureSubCacheQueue = [];
      parentRequestQueue = [];
    }

    grid.$connector.clear = function(index, length, parentKey) {
      let pkey = parentKey || root;
      if (!cache[pkey] || Object.keys(cache[pkey]).length === 0){
        return;
      }
      if (index % grid.pageSize != 0) {
        throw 'Got cleared data for index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
      }

      let firstPage = Math.floor(index / grid.pageSize);
      let updatedPageCount = Math.ceil(length / grid.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPage + i;
        let items = cache[pkey][page];
        for (let j = 0; j < items.length; j++) {
          let item = items[j];
          if (selectedKeys[item.key]) {
            grid.$connector.doDeselection(item);
          }
        }
        delete cache[pkey][page];
        const updatedItems = updateGridCache(page, parentKey);
        if (updatedItems) {
          itemsUpdated(updatedItems);
        }
        updateGridItemsInDomBasedOnCache(items);
      }
    };

    const isSelectedOnGrid = function(item) {
      const selectedItems = grid.selectedItems;
      for(let i = 0; i < selectedItems; i++) {
        let selectedItem = selectedItems[i];
        if (selectedItem.key === item.key) {
          return true;
        }
      }
      return false;
    }

    grid.$connector.reset = function() {
      grid.size = 0;
      deleteObjectContents(cache);
      deleteObjectContents(grid._cache.items);
      deleteObjectContents(lastRequestedRanges);
      if(ensureSubCacheDebouncer) {
        ensureSubCacheDebouncer.cancel();
      }
      if(parentRequestDebouncer) {
        parentRequestDebouncer.cancel();
      }
      ensureSubCacheDebouncer = undefined;
      parentRequestDebouncer = undefined;
      ensureSubCacheQueue = [];
      parentRequestQueue = [];
      updateAllGridRowsInDomBasedOnCache();
    };

    const deleteObjectContents = function(obj) {
      let props = Object.keys(obj);
      for (let i = 0; i < props.length; i++) {
        delete obj[props[i]];
      }
    }

    grid.$connector.updateSize = function(newSize) {
      grid.size = newSize;
    };

    grid.$connector.updateUniqueItemIdPath = function(path) {
      grid.itemIdPath = path;
    }

    grid.$connector.expandItems = function(items) {
      let newExpandedItems = Array.from(grid.expandedItems);
      items.filter(item => !grid._isExpanded(item))
        .forEach(item =>
          newExpandedItems.push(item));
      grid.expandedItems = newExpandedItems;
    }

    grid.$connector.collapseItems = function(items) {
      let newExpandedItems = Array.from(grid.expandedItems);
      items.forEach(item => {
        let index = grid._getItemIndexInArray(item, newExpandedItems);
        if(index >= 0) {
            newExpandedItems.splice(index, 1);
        }
      });
      grid.expandedItems = newExpandedItems;
      items.forEach(item => grid.$connector.removeFromQueue(item));
    }

    grid.$connector.removeFromQueue = function(item) {
      let itemId = grid.getItemId(item);
      delete treePageCallbacks[itemId];
      grid.$connector.removeFromArray(ensureSubCacheQueue, item => item.itemkey === itemId);
      grid.$connector.removeFromArray(parentRequestQueue, item => item.parentKey === itemId);
    }

    grid.$connector.removeFromArray = function(array, removeTest) {
      if(array.length) {
        for(let index = array.length - 1; index--; ) {
           if (removeTest(array[index])) {
             array.splice(index, 1);
           }
        }
      }
    }

    grid.$connector.confirmParent = function(id, parentKey, levelSize) {
      if(!treePageCallbacks[parentKey]) {
        return;
      }
      let outstandingRequests = Object.getOwnPropertyNames(treePageCallbacks[parentKey]);
      for(let i = 0; i < outstandingRequests.length; i++) {
        let page = outstandingRequests[i];

        let lastRequestedRange = lastRequestedRanges[parentKey] || [0, 0];
        if((cache[parentKey] && cache[parentKey][page]) || page < lastRequestedRange[0] || page > lastRequestedRange[1]) {
          let callback = treePageCallbacks[parentKey][page];
          delete treePageCallbacks[parentKey][page];
          let items = cache[parentKey][page] || new Array(levelSize);
          callback(items, levelSize);
        }
      }
      // Let server know we're done
      grid.$server.confirmParentUpdate(id, parentKey);
    };

    grid.$connector.confirm = function(id) {
      // We're done applying changes from this batch, resolve outstanding
      // callbacks
      let outstandingRequests = Object.getOwnPropertyNames(rootPageCallbacks);
      for(let i = 0; i < outstandingRequests.length; i++) {
        let page = outstandingRequests[i];
        let lastRequestedRange = lastRequestedRanges[root] || [0, 0];
        // Resolve if we have data or if we don't expect to get data
        if ((cache[root] && cache[root][page]) || page < lastRequestedRange[0] || page > lastRequestedRange[1]) {
          let callback = rootPageCallbacks[page];
          delete rootPageCallbacks[page];
          callback(cache[root][page] || new Array(grid.pageSize));
          // Makes sure to push all new rows before this stack execution is done so any timeout expiration called after will be applied on a fully updated grid
          //Resolves https://github.com/vaadin/vaadin-grid-flow/issues/511
          if(grid._debounceIncreasePool){
              grid._debounceIncreasePool.flush();
          }

        }
      }

      // Let server know we're done
      grid.$server.confirmUpdate(id);
    }

    grid.$connector.ensureHierarchy = function() {
      for (let parentKey in cache) {
        if(parentKey !== root) {
          delete cache[parentKey];
        }
      }
      deleteObjectContents(lastRequestedRanges);

      grid._cache.itemCaches = {};
      grid._cache.itemkeyCaches = {};

      updateAllGridRowsInDomBasedOnCache();
    }

    grid.$connector.setSelectionMode = function(mode) {
      if ((typeof mode === 'string' || mode instanceof String)
      && validSelectionModes.indexOf(mode) >= 0) {
        selectionMode = mode;
        selectedKeys = {};
      } else {
        throw 'Attempted to set an invalid selection mode';
      }
    }

    // TODO: should be removed once https://github.com/vaadin/vaadin-grid/issues/1471 gets implemented
    grid.$connector.setVerticalScrollingEnabled = function(enabled) {
      // There are two scollable containers in grid so apply the changes for both
      setVerticalScrollingEnabled(grid.$.table, enabled);
      setVerticalScrollingEnabled(grid.$.outerscroller, enabled);

      // Since the scrollbars were toggled, there might have been some changes to layout
      // size. Notify grid of the resize to ensure everything is in place.
      grid.notifyResize();
    }

    const setVerticalScrollingEnabled = function(scrollable, enabled) {
      // Prevent Y axis scrolling with CSS. This will hide the vertical scrollbar.
      scrollable.style.overflowY = enabled ? '' : 'hidden';
      // Clean up an existing listener
      scrollable.removeEventListener('wheel', scrollable.__wheelListener);
      // Add a wheel event listener with the horizontal scrolling prevention logic
      !enabled && scrollable.addEventListener('wheel', scrollable.__wheelListener = e => {
        if (e.deltaX) {
          // If there was some horizontal delta related to the wheel event, force the vertical
          // delta to 0 and let grid process the wheel event normally
          Object.defineProperty(e, 'deltaY', { value: 0 });
        } else {
          // If there was verical delta only, skip the grid's wheel event processing to
          // enable scrolling the page even if grid isn't scrolled to end
          e.stopImmediatePropagation();
        }
      });
    }

    const contextMenuListener = function(e) {
      // https://github.com/vaadin/vaadin-grid/issues/1318
      const path = e.composedPath();
      const row = path[path.indexOf(grid.$.table) - 2]; // <tr> element in shadow dom
      let key;
      if (row && row._item) {
        key = row._item.key;
      }
      grid.$server.updateContextMenuTargetItem(key);
    }

    grid.addEventListener('vaadin-context-menu-before-open', function(e) {
      contextMenuListener(grid.$contextMenuConnector.openEvent);
    });
    
    function _runWhenReady(){
        if ( grid.$ ){
            grid.$.scroller.addEventListener('click', _onClick);
            grid.$.scroller.addEventListener('dblclick', _onDblClick);
            grid.addEventListener('cell-activate', _cellActivated);
        }
        else {
            window.setTimeout(_runWhenReady, 0 );
        }
    }
    
    _runWhenReady();
    
    function _cellActivated(event){
        grid.$connector.clickedItem = event.detail.model.item;
    }
    
    function _onClick(event){
        _fireClickEvent(event, 'item-click');
    }
    
    function _onDblClick(event){
        _fireClickEvent(event, 'item-double-click');
    }
    
    function _fireClickEvent(event, eventName){
        // if there was no click on item then don't do anything
        if (grid.$connector.clickedItem){
            event.itemKey = grid.$connector.clickedItem.key;
            grid.dispatchEvent(new CustomEvent(eventName, 
                    { 
                        detail: event
                    }));
            // can't clear the clicked item right away since there may be 
            // not handled double click event (or may be not, it's not known)
            // schedule this for the next cycle
            window.setTimeout(_clearClickedItem, 0 );
        }
    }
    
    function _clearClickedItem(){
        grid.$connector.clickedItem = null;
    }

    grid.$connector.columnToIdMap = new Map();
    grid.$connector.setColumnId = function(column, id) {
        grid.$connector.columnToIdMap.set(column, id);
    }

    grid.$connector.columnRemoved = function(columnId) {
        const entries = Array.from(grid.$connector.columnToIdMap);
        const entryToRemove = entries.filter(function(entry) {
            return entry[1] === columnId;
        })[0];
        if (entryToRemove) {
            grid.$connector.columnToIdMap.delete(entryToRemove[0]);
        }
    }

    grid.cellClassNameGenerator = function(column, rowData) {
        const style = rowData.item.style;
        if (!style) {
            return;
        }
        const columnId = grid.$connector.columnToIdMap.get(column);

        return (style.row || '') + ' ' + (style[columnId] || '');
    }
  }
}
