// Not using ES6 imports in this file yet because the connector in V14 must
// still work in Legacy bower projects. See: `gridConnector-es6.js` for
// the Polymer3 approach.
(function () {
  const tryCatchWrapper = function (callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Grid', 'vaadin-grid-flow');
  };

  window.Vaadin.Flow.Legacy = window.Vaadin.Flow.Legacy || {};
  let isItemCacheInitialized = false;

  window.Vaadin.Flow.gridConnector = {
    initLazy: grid => tryCatchWrapper(function(grid) {
      // Check whether the connector was already initialized for the grid
      if (grid.$connector){
        return;
      }

      // Polymer
      if (window.Polymer) {
        // Polymer2 approach.
        window.Vaadin.Flow.Legacy.Debouncer = Polymer.Debouncer;
        window.Vaadin.Flow.Legacy.timeOut = Polymer.Async.timeOut;
        window.Vaadin.Flow.Legacy.animationFrame = Polymer.Async.animationFrame;
        window.Vaadin.Flow.Legacy.GridElement = Vaadin.GridElement;
        window.Vaadin.Flow.Legacy.ItemCache = Vaadin.Grid.ItemCache;
      }  else if (!window.Vaadin.Flow.Legacy.Debouncer) {
        console.log("Grid is unable to load Polymer helpers.");
        return;
      }

      const Debouncer = window.Vaadin.Flow.Legacy.Debouncer;
      const timeOut = window.Vaadin.Flow.Legacy.timeOut;
      const animationFrame = window.Vaadin.Flow.Legacy.animationFrame;
      const GridElement = window.Vaadin.Flow.Legacy.GridElement;
      const ItemCache = window.Vaadin.Flow.Legacy.ItemCache;

      // Make sure ItemCache patching is done only once, but delay it for when
      // a server grid is initialized
      if (!isItemCacheInitialized) {
        isItemCacheInitialized = true;
        // Storing original implementation of the method to be used for client
        // side only grids
        ItemCache.prototype.ensureSubCacheForScaledIndexOriginal = ItemCache.prototype.ensureSubCacheForScaledIndex;
        ItemCache.prototype.ensureSubCacheForScaledIndex = tryCatchWrapper(function(scaledIndex) {
          if (!this.grid.$connector) {
            this.ensureSubCacheForScaledIndexOriginal(scaledIndex);
            return;
          }

          if (!this.itemCaches[scaledIndex]) {
            this.grid.$connector.beforeEnsureSubCacheForScaledIndex(this, scaledIndex);
          }
        });

        ItemCache.prototype.isLoading = tryCatchWrapper(function() {
          return Boolean(ensureSubCacheQueue.length || Object.keys(this.pendingRequests).length || Object.keys(this.itemCaches).filter(index => {
            return this.itemCaches[index].isLoading();
          })[0]);
        });

        ItemCache.prototype.doEnsureSubCacheForScaledIndex = tryCatchWrapper(function(scaledIndex) {
          if (!this.itemCaches[scaledIndex]) {
            const subCache = new ItemCache.prototype.constructor(this.grid, this, this.items[scaledIndex]);
            subCache.itemkeyCaches = {};
            if(!this.itemkeyCaches) {
              this.itemkeyCaches = {};
            }
            this.itemCaches[scaledIndex] = subCache;
            this.itemkeyCaches[this.grid.getItemId(subCache.parentItem)] = subCache;
            this.grid._loadPage(0, subCache);
          }
        });

        ItemCache.prototype.getCacheAndIndexByKey = tryCatchWrapper(function(key) {
          for (let index in this.items) {
            if(this.grid.getItemId(this.items[index]) === key) {
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
        });

        ItemCache.prototype.getLevel = tryCatchWrapper(function() {
          let cache = this;
          let level = 0;
          while (cache.parentCache) {
            cache = cache.parentCache;
            level++;
          }
          return level;
        });
      }

      const rootPageCallbacks = {};
      const treePageCallbacks = {};
      const cache = {};

      /* parentRequestDelay - optimizes parent requests by batching several requests
      *  into one request. Delay in milliseconds. Disable by setting to 0.
      *  parentRequestBatchMaxSize - maximum size of the batch.
      */
      const parentRequestDelay = 50;
      const parentRequestBatchMaxSize = 20;

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

      let detailsVisibleOnClick = true;

      let sorterDirectionsSetFromServer = false;

      grid.size = 0; // To avoid NaN here and there before we get proper data
      grid.itemIdPath = 'key';

      grid.$connector = {};

      grid.$connector.hasEnsureSubCacheQueue = tryCatchWrapper(() => ensureSubCacheQueue.length > 0);

      grid.$connector.hasParentRequestQueue = tryCatchWrapper(() => parentRequestQueue.length > 0);

      grid.$connector.hasRootRequestQueue = tryCatchWrapper(() => {
        return Object.keys(rootPageCallbacks).length > 0 || (rootRequestDebouncer && rootRequestDebouncer.isActive());
      })

      grid.$connector.beforeEnsureSubCacheForScaledIndex = tryCatchWrapper(function(targetCache, scaledIndex) {
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

        ensureSubCacheDebouncer = Debouncer.debounce(ensureSubCacheDebouncer, animationFrame,
          () => {
            while (ensureSubCacheQueue.length) {
              grid.$connector.flushEnsureSubCache();
            }
          }
        );
      })

      grid.$connector.doSelection = tryCatchWrapper(function(items, userOriginated) {
        if (selectionMode === 'NONE' || !items.length ||
            (userOriginated && grid.hasAttribute('disabled'))) {
          return;
        }
        if (selectionMode === 'SINGLE') {
          grid.selectedItems = [];
          selectedKeys = {};
        }

        grid.selectedItems = grid.selectedItems.concat(items);
        items.forEach(item => {
          if (item) {
            selectedKeys[item.key] = item;
            if (userOriginated) {
              item.selected = true;
              grid.$server.select(item.key);
            }
          }
          const isSelectedItemDifferentOrNull = !grid.activeItem || !item || item.key != grid.activeItem.key;
          if (!userOriginated && selectionMode === 'SINGLE' && isSelectedItemDifferentOrNull) {
            grid.activeItem = item;
            grid.$connector.activeItem = item;
          }
        });
      });

      grid.$connector.doDeselection = tryCatchWrapper(function(items, userOriginated) {
        if (selectionMode === 'NONE' || !items.length ||
            (userOriginated && grid.hasAttribute('disabled'))) {
          return;
        }

        const updatedSelectedItems = grid.selectedItems.slice();
        while (items.length) {
          const itemToDeselect = items.shift();
          for (let i = 0; i < updatedSelectedItems.length; i++) {
            const selectedItem = updatedSelectedItems[i];
            if (itemToDeselect && itemToDeselect.key === selectedItem.key) {
              updatedSelectedItems.splice(i, 1);
              break;
            }
          }
          if (itemToDeselect) {
            delete selectedKeys[itemToDeselect.key];
            if (userOriginated) {
              delete itemToDeselect.selected;
              grid.$server.deselect(itemToDeselect.key);
            }
          }
        }
        grid.selectedItems = updatedSelectedItems;
      });

      grid.__activeItemChanged = tryCatchWrapper(function(newVal, oldVal) {
        if (selectionMode != 'SINGLE') {
          return;
        }
        if (!newVal) {
          if (oldVal && selectedKeys[oldVal.key]) {
            if (!grid.$connector.deselectAllowed) {
              grid.activeItem = oldVal;
            } else {
              grid.$connector.doDeselection([oldVal], true);
            }
          }
        } else if (!selectedKeys[newVal.key]) {
          grid.$connector.doSelection([newVal], true);
        }
      });
      grid._createPropertyObserver('activeItem', '__activeItemChanged', true);

      grid.__activeItemChangedDetails = tryCatchWrapper(function(newVal, oldVal) {
        if(!detailsVisibleOnClick) {
          return;
        }
        // when grid is attached, newVal is not set and oldVal is undefined
        // do nothing
        if ((newVal == null) && (oldVal === undefined)) {
          return;
        }
        if (newVal && !newVal.detailsOpened) {
          grid.$server.setDetailsVisible(newVal.key);
        } else {
          grid.$server.setDetailsVisible(null);
        }
      })
      grid._createPropertyObserver('activeItem', '__activeItemChangedDetails', true);

      grid.$connector.setDetailsVisibleOnClick = tryCatchWrapper(function(visibleOnClick) {
        detailsVisibleOnClick = visibleOnClick;
      });

      grid.$connector._getPageIfSameLevel = tryCatchWrapper(function(parentKey, index, defaultPage) {
        let cacheAndIndex = grid._cache.getCacheAndIndex(index);
        let parentItem = cacheAndIndex.cache.parentItem;
        let parentKeyOfIndex = (parentItem) ? grid.getItemId(parentItem) : root;
        if(parentKey !== parentKeyOfIndex) {
          return defaultPage;
        } else {
          return grid._getPageForIndex(cacheAndIndex.scaledIndex);
        }
      })

      grid.$connector.getCacheByKey = tryCatchWrapper(function(key) {
        let cacheAndIndex = grid._cache.getCacheAndIndexByKey(key);
        if(cacheAndIndex) {
          return cacheAndIndex.cache;
        }
        return undefined;
      });

      grid.$connector.flushEnsureSubCache = tryCatchWrapper(function() {
        let pendingFetch = ensureSubCacheQueue.splice(0, 1)[0];
        let itemkey =  pendingFetch.itemkey;

        let start = grid._virtualStart;
        let end = grid._virtualEnd;
        let buffer = end - start;
        let firstNeededIndex = Math.max(0, start + grid._vidxOffset - buffer);
        let lastNeededIndex = Math.min(end + grid._vidxOffset + buffer, grid._effectiveSize);

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
      })

      grid.$connector.flushParentRequests = tryCatchWrapper(function() {
        let pendingFetches = parentRequestQueue.splice(0, parentRequestBatchMaxSize);

        if(pendingFetches.length) {
          grid.$server.setParentRequestedRanges(pendingFetches);
          return true;
        }
        return false;
      })

      grid.$connector.beforeParentRequest = tryCatchWrapper(function(firstIndex, size, parentKey) {
        // add request in queue
        parentRequestQueue.push({
          firstIndex: firstIndex,
          size: size,
          parentKey: parentKey
        });

        parentRequestDebouncer = Debouncer.debounce(parentRequestDebouncer, timeOut.after(parentRequestDelay),
          () => {
            while (parentRequestQueue.length) {
              grid.$connector.flushParentRequests();
            }
          }
        );
      })

      grid.$connector.fetchPage = tryCatchWrapper(function(fetch, page, parentKey) {
        // Determine what to fetch based on scroll position and not only
        // what grid asked for

        // The buffer size could be multiplied by some constant defined by the user,
        // if he needs to reduce the number of items sent to the Grid to improve performance
        // or to increase it to make Grid smoother when scrolling
        let start = grid._virtualStart;
        let end = grid._virtualEnd;
        let buffer = end - start;

        let firstNeededIndex = Math.max(0, start + grid._vidxOffset - buffer);
        let lastNeededIndex = Math.min(end + grid._vidxOffset + buffer, grid._effectiveSize);

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
      })

      grid.dataProvider = tryCatchWrapper(function(params, callback) {
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
            page = Math.min(page, Math.floor(cache[parentUniqueKey].size / grid.pageSize));

            callback(cache[parentUniqueKey][page], cache[parentUniqueKey].size);
          } else {
            treePageCallbacks[parentUniqueKey][page] = callback;
          }
          grid.$connector.fetchPage(
            (firstIndex, size) => grid.$connector.beforeParentRequest(firstIndex, size, params.parentItem.key),
            page,
            parentUniqueKey
          );

        } else {
          // workaround: sometimes grid-element gives page index that overflows
          page = Math.min(page, Math.floor(grid.size / grid.pageSize));

          if (cache[root] && cache[root][page]) {
            callback(cache[root][page]);
          } else {
            rootPageCallbacks[page] = callback;
          }

          rootRequestDebouncer = Debouncer.debounce(rootRequestDebouncer, timeOut.after(grid._hasData ? rootRequestDelay : 0),
            () => {
              grid.$connector.fetchPage((firstIndex, size) => grid.$server.setRequestedRange(firstIndex, size), page, root);
            }
          );
        }
      })

      const sorterChangeListener = tryCatchWrapper(function(_, oldValue) {
        if (oldValue !== undefined && !sorterDirectionsSetFromServer) {
          grid.$server.sortersChanged(grid._sorters.map(function (sorter) {
            return {
              path: sorter.path,
              direction: sorter.direction
            };
          }));
        }
      })

      grid.$connector.setSorterDirections = tryCatchWrapper(function(directions) {
        sorterDirectionsSetFromServer = true;
        setTimeout(tryCatchWrapper(() => {
          try {
            const sorters = Array.from(grid.querySelectorAll('vaadin-grid-sorter'));

            sorters.forEach(sorter => {
              if (!directions.filter(d => d.column === sorter.getAttribute('path'))[0]) {
                sorter.direction = null;
              }
            });

            directions.reverse().forEach(({column, direction}) => {
              sorters.forEach(sorter => {
                if (sorter.getAttribute('path') === column && sorter.direction !== direction) {
                  sorter.direction = direction
                }
              });
            });
          } finally {
            sorterDirectionsSetFromServer = false;
          }
        }));
      })
      grid._createPropertyObserver("_previousSorters", sorterChangeListener);

      grid._updateItem = tryCatchWrapper(function(row, item) {
        GridElement.prototype._updateItem.call(grid, row, item);

        // There might be inactive component renderers on hidden rows that still refer to the
        // same component instance as one of the renderers on a visible row. Making the
        // inactive/hidden renderer attach the component might steal it from a visible/active one.
        if (!row.hidden) {
          // make sure that component renderers are updated
          Array.from(row.children).forEach(cell => {
            if (cell._instance && cell._instance.children) {
              Array.from(cell._instance.children).forEach(content => {
                if(content._attachRenderedComponentIfAble) {
                  content._attachRenderedComponentIfAble();
                }
                // In hierarchy column of tree grid, the component renderer is inside its content,
                // this updates it renderer from innerContent
                if (content.children) {
                  Array.from(content.children).forEach(innerContent => {
                    if(innerContent._attachRenderedComponentIfAble) {
                        innerContent._attachRenderedComponentIfAble();
                    }
                  });
                }
             });
            }
          });
        }
      })

      grid._expandedInstanceChangedCallback = tryCatchWrapper(function(inst, value) {
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
          if (parentCache && parentCache.itemkeyCaches) {
            Object.keys(parentCache.itemCaches)
                .filter(idx => parentCache.items[idx].key === parentKey)
                .forEach(idx => delete parentCache.itemCaches[idx]);
          }
          delete lastRequestedRanges[parentKey];
          this.collapseItem(inst.item);
        }
      })

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
      };

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
            const callbacksForParentKey = treePageCallbacks[parentKey];
            const callback = callbacksForParentKey && callbacksForParentKey[page];
            _updateGridCache(page, items, callback, _cache);
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
        grid._cache.updateSize();
        grid._effectiveSize = grid._cache.effectiveSize;
        grid._assignModels();
      };

      /**
       * Update the given items in DOM if currently visible.
       *
       * @param array items the items to update in DOM
       */
      const updateGridItemsInDomBasedOnCache = function(items) {
        if (!items || !grid._physicalItems) {
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

      grid.$connector.set = tryCatchWrapper(function(index, items, parentKey) {
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

          grid.$connector.doSelection(slice.filter(
            item => item.selected && !isSelectedOnGrid(item)));
          grid.$connector.doDeselection(slice.filter(
            item => !item.selected  && (selectedKeys[item.key] || isSelectedOnGrid(item))));

          const updatedItems = updateGridCache(page, pkey);
          if (updatedItems) {
            itemsUpdated(updatedItems);
            updateGridItemsInDomBasedOnCache(updatedItems);
          }
        }
      });

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
      };

      /**
       * Updates the given items for a hierarchical grid.
       *
       * @param updatedItems the updated items array
       */
      grid.$connector.updateHierarchicalData = tryCatchWrapper(function(updatedItems) {
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
        for (let i = 0; i < keys.length; i++) {
          let pageToUpdate = pagesToUpdate[keys[i]];
          const affectedUpdatedItems = updateGridCache(pageToUpdate.page, pageToUpdate.parentKey);
          if (affectedUpdatedItems) {
            itemsUpdated(affectedUpdatedItems);
            updateGridItemsInDomBasedOnCache(affectedUpdatedItems);
          }
        }
      });

      /**
       * Updates the given items for a non-hierarchical grid.
       *
       * @param updatedItems the updated items array
       */
      grid.$connector.updateFlatData = tryCatchWrapper(function(updatedItems) {
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
      });

      grid.$connector.clearExpanded = tryCatchWrapper(function() {
        grid.expandedItems = [];
        ensureSubCacheQueue = [];
        parentRequestQueue = [];
      })

      grid.$connector.clear = tryCatchWrapper(function(index, length, parentKey) {
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
          grid.$connector.doDeselection(items.filter(item => selectedKeys[item.key]));
          delete cache[pkey][page];
          const updatedItems = updateGridCache(page, parentKey);
          if (updatedItems) {
            itemsUpdated(updatedItems);
          }
          updateGridItemsInDomBasedOnCache(items);
        }
        let cacheToClear = grid._cache;
        if(parentKey)  {
          const cacheAndIndex = grid._cache.getCacheAndIndexByKey(pkey);
          cacheToClear = cacheAndIndex.cache.itemCaches[cacheAndIndex.scaledIndex];
        }
        const endIndex = index + updatedPageCount * grid.pageSize;
        for(let itemIndex = index; itemIndex < endIndex; itemIndex++) {
          delete cacheToClear.items[itemIndex];
          const subcacheToClear = cacheToClear.itemCaches[itemIndex];
          delete cacheToClear.itemCaches[itemIndex];
          const itemKeyToRemove = subcacheToClear && subcacheToClear.parentItem.key;
          if(itemKeyToRemove) {
            delete cacheToClear.itemkeyCaches[itemKeyToRemove];
          }
        }
        grid._cache.updateSize();
      });

      const isSelectedOnGrid = function(item) {
        const selectedItems = grid.selectedItems;
        for(let i = 0; i < selectedItems; i++) {
          let selectedItem = selectedItems[i];
          if (selectedItem.key === item.key) {
            return true;
          }
        }
        return false;
      };

      grid.$connector.reset = tryCatchWrapper(function() {
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
        if (rootRequestDebouncer) {
          rootRequestDebouncer.cancel();
        }
        ensureSubCacheDebouncer = undefined;
        parentRequestDebouncer = undefined;
        ensureSubCacheQueue = [];
        parentRequestQueue = [];
        updateAllGridRowsInDomBasedOnCache();
      });

      const deleteObjectContents = obj => Object.keys(obj).forEach(key => delete obj[key]);

      grid.$connector.updateSize = newSize => grid.size = newSize;

      grid.$connector.updateUniqueItemIdPath = path => grid.itemIdPath = path;

      grid.$connector.expandItems = tryCatchWrapper(function(items) {
        let newExpandedItems = Array.from(grid.expandedItems);
        items.filter(item => !grid._isExpanded(item))
          .forEach(item =>
            newExpandedItems.push(item));
        grid.expandedItems = newExpandedItems;
      })

      grid.$connector.collapseItems = tryCatchWrapper(function(items) {
        let newExpandedItems = Array.from(grid.expandedItems);
        items.forEach(item => {
          let index = grid._getItemIndexInArray(item, newExpandedItems);
          if(index >= 0) {
            newExpandedItems.splice(index, 1);
          }
        });
        grid.expandedItems = newExpandedItems;
        items.forEach(item => grid.$connector.removeFromQueue(item));
      })

      grid.$connector.removeFromQueue = tryCatchWrapper(function(item) {
        let itemId = grid.getItemId(item);
        delete treePageCallbacks[itemId];
        grid.$connector.removeFromArray(ensureSubCacheQueue, item => item.itemkey === itemId);
        grid.$connector.removeFromArray(parentRequestQueue, item => item.parentKey === itemId);
      })

      grid.$connector.removeFromArray = tryCatchWrapper(function(array, removeTest) {
        if(array.length) {
          for(let index = array.length - 1; index--; ) {
            if (removeTest(array[index])) {
              array.splice(index, 1);
            }
          }
        }
      })

      grid.$connector.confirmParent = tryCatchWrapper(function(id, parentKey, levelSize) {
        if(!treePageCallbacks[parentKey]) {
          return;
        }
        if(cache[parentKey]) {
          cache[parentKey].size = levelSize;
        }
        let outstandingRequests = Object.getOwnPropertyNames(treePageCallbacks[parentKey]);
        for(let i = 0; i < outstandingRequests.length; i++) {
          let page = outstandingRequests[i];

          let lastRequestedRange = lastRequestedRanges[parentKey] || [0, 0];

          const callback = treePageCallbacks[parentKey][page];
          if((cache[parentKey] && cache[parentKey][page]) || page < lastRequestedRange[0] || page > lastRequestedRange[1]) {
            delete treePageCallbacks[parentKey][page];
            let items = cache[parentKey][page] || new Array(levelSize);
            callback(items, levelSize);
          } else if (callback && levelSize === 0) {
            // The parent item has 0 child items => resolve the callback with an empty array
            delete treePageCallbacks[parentKey][page];
            callback([], levelSize);
          }
        }
        // Let server know we're done
        grid.$server.confirmParentUpdate(id, parentKey);

        if (!grid.loading) {
          grid._assignModels();
        }
      });

      grid.$connector.confirm = tryCatchWrapper(function(id) {
        // We're done applying changes from this batch, resolve outstanding
        // callbacks
        let outstandingRequests = Object.getOwnPropertyNames(rootPageCallbacks);
        for(let i = 0; i < outstandingRequests.length; i++) {
          let page = outstandingRequests[i];
          let lastRequestedRange = lastRequestedRanges[root] || [0, 0];

          const lastAvailablePage = grid.size ? Math.ceil(grid.size / grid.pageSize) - 1 : 0;
          // It's possible that the lastRequestedRange includes a page that's beyond lastAvailablePage if the grid's size got reduced during an ongoing data request
          const lastRequestedRangeEnd = Math.min(lastRequestedRange[1], lastAvailablePage);
          // Resolve if we have data or if we don't expect to get data
          const callback = rootPageCallbacks[page];
          if ((cache[root] && cache[root][page]) || page < lastRequestedRange[0] || +page > lastRequestedRangeEnd) {
            delete rootPageCallbacks[page];
            callback(cache[root][page] || new Array(grid.pageSize));
            // Makes sure to push all new rows before this stack execution is done so any timeout expiration called after will be applied on a fully updated grid
            //Resolves https://github.com/vaadin/vaadin-grid-flow/issues/511
            if(grid._debounceIncreasePool){
              grid._debounceIncreasePool.flush();
            }

          } else if (callback && grid.size === 0) {
            // The grid has 0 items => resolve the callback with an empty array
            delete rootPageCallbacks[page];
            callback([]);
          }
        }

        // Let server know we're done
        grid.$server.confirmUpdate(id);
      })

      grid.$connector.ensureHierarchy = tryCatchWrapper(function() {
        for (let parentKey in cache) {
          if(parentKey !== root) {
            delete cache[parentKey];
          }
        }
        deleteObjectContents(lastRequestedRanges);

        grid._cache.itemCaches = {};
        grid._cache.itemkeyCaches = {};

        updateAllGridRowsInDomBasedOnCache();
      })

      grid.$connector.setSelectionMode = tryCatchWrapper(function(mode) {
        if ((typeof mode === 'string' || mode instanceof String)
            && validSelectionModes.indexOf(mode) >= 0) {
          selectionMode = mode;
          selectedKeys = {};
        } else {
          throw 'Attempted to set an invalid selection mode';
        }
      });

      grid.$connector.deselectAllowed = true;

      // TODO: should be removed once https://github.com/vaadin/vaadin-grid/issues/1471 gets implemented
      grid.$connector.setVerticalScrollingEnabled = tryCatchWrapper(function(enabled) {
        setVerticalScrollingEnabled(grid.$.table, enabled);

        // Since the scrollbars were toggled, there might have been some changes to layout
        // size. Notify grid of the resize to ensure everything is in place.
        grid.notifyResize();
      });

      const setVerticalScrollingEnabled = function(scrollable, enabled) {
        // Prevent Y axis scrolling with CSS. This will hide the vertical scrollbar.
        scrollable.style.overflowY = enabled ? '' : 'hidden';
        // Clean up an existing listener
        scrollable.removeEventListener('wheel', scrollable.__wheelListener);
        // Add a wheel event listener with the horizontal scrolling prevention logic
        !enabled && scrollable.addEventListener('wheel', scrollable.__wheelListener = tryCatchWrapper(e => {
          if (e.deltaX) {
            // If there was some horizontal delta related to the wheel event, force the vertical
            // delta to 0 and let grid process the wheel event normally
            Object.defineProperty(e, 'deltaY', { value: 0 });
          } else {
            // If there was verical delta only, skip the grid's wheel event processing to
            // enable scrolling the page even if grid isn't scrolled to end
            e.stopImmediatePropagation();
          }
        }));
      };

      const contextMenuListener = function(e) {
        const eventContext = grid.getEventContext(e);
        const key = eventContext.item && eventContext.item.key;
        const colId = eventContext.column && eventContext.column.id;
        grid.$server.updateContextMenuTargetItem(key, colId);
      };

      grid.addEventListener('vaadin-context-menu-before-open', tryCatchWrapper(function(e) {
        contextMenuListener(grid.$contextMenuConnector.openEvent);
      }));

      grid.getContextMenuBeforeOpenDetail = tryCatchWrapper(function(event) {
        const eventContext = grid.getEventContext(event);
        return {
          key: (eventContext.item && eventContext.item.key) || ""
        };
      });

      grid.addEventListener('cell-activate', tryCatchWrapper(e => {
        grid.$connector.activeItem = e.detail.model.item;
        setTimeout(() => grid.$connector.activeItem = undefined);
      }));
      grid.addEventListener('click', tryCatchWrapper(e => _fireClickEvent(e, 'item-click')));
      grid.addEventListener('dblclick', tryCatchWrapper(e => _fireClickEvent(e, 'item-double-click')));

      grid.addEventListener('column-resize', tryCatchWrapper(e => {
        const cols = grid._getColumnsInOrder().filter(col => !col.hidden);

        cols.forEach(col => {
          col.dispatchEvent(new CustomEvent('column-drag-resize'));
        });

        grid.dispatchEvent(new CustomEvent('column-drag-resize', { detail: {
          resizedColumnKey: e.detail.resizedColumn._flowId
        }}));
      }));

      grid.addEventListener('column-reorder', tryCatchWrapper(e => {
        const columns = grid._columnTree.slice(0).pop()
          .filter(c => c._flowId)
          .sort((b, a) => (b._order - a._order))
          .map(c => c._flowId);

        grid.dispatchEvent(new CustomEvent('column-reorder-all-columns', {
          detail: { columns }
        }));
      }));

      function _fireClickEvent(event, eventName) {
        if (grid.$connector.activeItem) {
          event.itemKey = grid.$connector.activeItem.key;
          const eventContext = grid.getEventContext(event);
          // if you have a details-renderer, getEventContext().column is undefined
          if (eventContext.column) {
            event.internalColumnId = eventContext.column._flowId;
          }
          grid.dispatchEvent(new CustomEvent(eventName,
            { detail: event }));
        }
      }

      grid.cellClassNameGenerator = tryCatchWrapper(function(column, rowData) {
          const style = rowData.item.style;
          if (!style) {
            return;
          }
          return (style.row || '') + ' ' + ((column && style[column._flowId]) || '');
      })

      grid.dropFilter = tryCatchWrapper(rowData => !rowData.item.dropDisabled);

      grid.dragFilter = tryCatchWrapper(rowData => !rowData.item.dragDisabled);

      grid.addEventListener('grid-dragstart', tryCatchWrapper(e => {

        if (grid._isSelected(e.detail.draggedItems[0])) {
          // Dragging selected (possibly multiple) items
          if (grid.__selectionDragData) {
            Object.keys(grid.__selectionDragData).forEach(type => {
              e.detail.setDragData(type, grid.__selectionDragData[type]);
            });
          } else {
            (grid.__dragDataTypes || []).forEach(type => {
              e.detail.setDragData(type, e.detail.draggedItems.map(item => item.dragData[type]).join('\n'));
            });
          }

          if (grid.__selectionDraggedItemsCount > 1) {
            e.detail.setDraggedItemsCount(grid.__selectionDraggedItemsCount);
          }
        } else {
          // Dragging just one (non-selected) item
          (grid.__dragDataTypes || []).forEach(type => {
            e.detail.setDragData(type, e.detail.draggedItems[0].dragData[type]);
          });
        }
      }));
    })(grid)
  }
})();
