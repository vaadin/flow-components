window.Vaadin.Flow.gridConnector = {
  initLazy: function(grid) {    
    // Check whether the connector was already initialized for the grid
    if (grid.$connector){
      return;
    }
    const pageCallbacks = {};
    const cache = {};
    let lastRequestedRange = [0, 0];

    const validSelectionModes = ['SINGLE', 'NONE', 'MULTI'];
    let selectedKeys = {};
    let selectionMode = 'SINGLE';

    let detailsVisibleOnClick = true;

    grid.size = 0; // To avoid NaN here and there before we get proper data

    grid.$connector = {};

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

    grid.dataProvider = function(params, callback) {
      if (params.pageSize != grid.pageSize) {
        throw 'Invalid pageSize';
      }

      const page = params.page;
      if (cache[page]) {
        callback(cache[page]);
      } else {
        pageCallbacks[page] = callback;
      }
      // Determine what to fetch based on scroll position and not only
      // what grid asked for

      // The buffer size could be multiplied by some constant defined by the user,
      // if he needs to reduce the number of items sent to the Grid to improve performance
      // or to increase it to make Grid smoother when scrolling
      let buffer = grid._virtualEnd - grid._virtualStart;
      
      let firstNeededIndex = Math.max(0, grid._virtualStart + grid._vidxOffset - buffer);
      let lastNeededIndex = Math.min(grid._virtualEnd + grid._vidxOffset + buffer, grid.size);

      let firstNeededPage = Math.min(page, grid._getPageForIndex(firstNeededIndex));
      let lastNeededPage = Math.max(page, grid._getPageForIndex(lastNeededIndex));

      let first = Math.max(0,  firstNeededPage);
      let last = Math.min(lastNeededPage, Math.floor(grid.size / grid.pageSize) + 1);

      if (lastRequestedRange[0] != first || lastRequestedRange[1] != last) {
        lastRequestedRange = [first, last];
        let count = last - first + 1;
        grid.$server.setRequestedRange(first * grid.pageSize, count * grid.pageSize);
      }
    }

    const sorterChangeListener = function(event) {
      grid.$server.sortersChanged(grid._sorters.map(function(sorter) {
        return {
          path: sorter.path,
          direction: sorter.direction
        };
      }));
    }
    grid.addEventListener('sorter-changed', sorterChangeListener);

    const itemsUpdated = function(items) {
      if (!items || !Array.isArray(items)) {
        throw 'Attempted to call itemsUpdated with an invalid value: ' + JSON.stringify(items);
      }
      const detailsOpenedItems = [];
      let updatedSelectedItem = false;
      for (let i = 0; i < items.length; ++i) {
        const item = items[i];
        if (item.detailsOpened) {
          detailsOpenedItems.push(item);
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

    const updateGridCache = function(page) {
      const items = cache[page];
      // Force update unless there's a callback waiting
      if (!pageCallbacks[page]) {
        let rangeStart = page * grid.pageSize;
        let rangeEnd = rangeStart + grid.pageSize;
        if (!items) {
          for (let idx = rangeStart; idx < rangeEnd; idx++) {
            delete grid._cache.items[idx];
          }
        }
        else {
          for (let idx = rangeStart; idx < rangeEnd; idx++) {
            if (grid._cache.items[idx]) {
              grid._cache.items[idx] = items[idx - rangeStart];
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

    grid.$connector.set = function(index, items) {
      if (index % grid.pageSize != 0) {
        throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
      }

      const firstPage = index / grid.pageSize;
      const updatedPageCount = Math.ceil(items.length / grid.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPage + i;
        let slice = items.slice(i * grid.pageSize, (i + 1) * grid.pageSize);
        cache[page] = slice;
        for(let j = 0; j < slice.length; j++) {
          let item = slice[j]
          if (item.selected && !isSelectedOnGrid(item)) {
            grid.$connector.doSelection(item);
          } else if (!item.selected && (selectedKeys[item.key] || isSelectedOnGrid(item))) {
            grid.$connector.doDeselection(item);
          }
        }
        updateGridCache(page);
      }
    };

    const itemToCacheLocation = function(itemKey) {
      for (let page in cache) {
        for (let index in cache[page]) {
          if (cache[page][index].key === itemKey) {
            return {page: page, index: index};
          }
        }
      }
      return null;
    }

    grid.$connector.updateData = function(items) {
      let pagesToUpdate = [];
      for (let i = 0; i < items.length; i++) {
        let cacheLocation = itemToCacheLocation(items[i].key);
        if (cacheLocation) {
          cache[cacheLocation.page][cacheLocation.index] = items[i];
          if (pagesToUpdate.indexOf(cacheLocation.page) === -1) {
            pagesToUpdate.push(cacheLocation.page);
          }
        }
      }
      // IE11 doesn't work with the transpiled version of the forEach.
      for (var i = 0; i< pagesToUpdate.length; i++) {
          let page = pagesToUpdate[i];
        updateGridCache(page);
      }
    };

    grid.$connector.clear = function(index, length) {
      if (Object.keys(cache).length === 0){
        return;
      }
      if (index % grid.pageSize != 0) {
        throw 'Got cleared data for index ' + index + ' which is not aligned with the page size of ' + grid.pageSize;
      }

      let firstPage = index / grid.pageSize;
      let updatedPageCount = Math.ceil(length / grid.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPage + i;
        let items = cache[page];
        for (let j = 0; j < items.length; j++) {
          let item = items[j];
          if (selectedKeys[item.key]) {
            grid.$connector.doDeselection(item);
          }
        }
        delete cache[page];
        updateGridCache(page);
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
      lastRequestedRange = [0, 0];
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

    grid.$connector.confirm = function(id) {
      // We're done applying changes from this batch, resolve outstanding
      // callbacks
      let outstandingRequests = Object.getOwnPropertyNames(pageCallbacks);
      for(let i = 0; i < outstandingRequests.length; i++) {
        let page = outstandingRequests[i];
        // Resolve if we have data or if we don't expect to get data
        if (cache[page] || page < lastRequestedRange[0] || page > lastRequestedRange[1]) {
          let callback = pageCallbacks[page];
          delete pageCallbacks[page];
          callback(cache[page] || new Array(grid.pageSize));
        }
      }

      // Let server know we're done
      grid.$server.confirmUpdate(id);
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
  }
}
