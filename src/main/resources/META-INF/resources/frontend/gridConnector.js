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
      if (userOriginated && (grid.getAttribute('disabled') || grid.getAttribute('disabled') === '')) {
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
        let itemCache = (parentCache) ? parentCache.itemkeyCaches[parentUniqueKey] : undefined;
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
      grid.$server.sortersChanged(grid._sorters.map(function(sorter) {
        return {
          path: sorter.path,
          direction: sorter.direction
        };
      }));
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
      if (inst.item == undefined) {
        return;
      }
      let parentKey = grid.getItemId(inst.item);
      grid.$server.updateExpandedState(parentKey, value);
      if (value) {
        this.expandItem(inst.item);
      } else {
        delete cache[parentKey];
        let parentCache = grid.$connector.getCacheByKey(parentKey);
        if(parentCache && parentCache.itemkeyCaches[parentKey]) {
          parentCache.itemkeyCaches[parentKey].items = [];
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

    const updateGridCache = function(page, parentKey) {
      if((parentKey || root) !== root) {
        const items = cache[parentKey][page];
        let parentCache = grid.$connector.getCacheByKey(parentKey);
        if(parentCache) {
          let _cache = parentCache.itemkeyCaches[parentKey];
          _updateGridCache(page, items,
            treePageCallbacks[parentKey][page],
            _cache);
        }

      } else {
        const items = cache[root][page];
        _updateGridCache(page, items, rootPageCallbacks[page], grid._cache);
      }
    }

    const _updateGridCache = function(page, items, callback, levelcache) {
      // Force update unless there's a callback waiting
      if(!callback) {
        let rangeStart = page * grid.pageSize;
        let rangeEnd = rangeStart + grid.pageSize;
        if (!items) {
          if(levelcache && levelcache.items) {
            for (let idx = rangeStart; idx < rangeEnd; idx++) {
              delete levelcache.items[idx];
            }
          }

        } else {
          if(levelcache && levelcache.items) {
            for (let idx = rangeStart; idx < rangeEnd; idx++) {
              if (levelcache.items[idx]) {
                levelcache.items[idx] = items[idx - rangeStart];
              }
            }
          }
          itemsUpdated(items);
        }
        /**
         * Calls the _assignModels function from GridScrollerElement, that triggers
         * the internal revalidation of the items based on the _cache of the DataProviderMixin.
         */
        grid._assignModels();
      }
    }

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
        updateGridCache(page, pkey);
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

    grid.$connector.updateData = function(items) {
      let pagesToUpdate = [];
      for (let i = 0; i < items.length; i++) {
        let cacheLocation = itemToCacheLocation(items[i]);
        if (cacheLocation) {
          cache[cacheLocation.parentKey][cacheLocation.page][cacheLocation.index] = items[i];
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
        updateGridCache(pageToUpdate.page, pageToUpdate.parentKey);
      }
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
        updateGridCache(page, parentKey);
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
      grid._assignModels();
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

      grid._assignModels();
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

    const contextMenuListener = function(e) {
      // https://github.com/vaadin/vaadin-grid/issues/1318
      const path = e.composedPath();
      const cell = path[path.indexOf(grid.$.table) - 3]; // <td> element in shadow dom
      var key;
      if (cell && cell._instance.item) {
        key = cell._instance.item.key;
      }
      grid.$server.updateContextMenuTargetItem(key);
    }

    grid.addEventListener('vaadin-context-menu-before-open', function(e) {
      contextMenuListener(grid.$contextMenuConnector.openEvent);
    });

  }
}
