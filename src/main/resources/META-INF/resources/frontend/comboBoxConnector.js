window.Vaadin.Flow.comboBoxConnector = {
  initLazy: function (comboBox) {
    // Check whether the connector was already initialized for the ComboBox
    if (comboBox.$connector) {
      return;
    }

    comboBox.$connector = {};

    let pageCallbacks = {};
    let cache = {};
    let firstPage;
    let lastFilter = '';

    comboBox.size = 0; // To avoid NaN here and there before we get proper data

    comboBox.dataProvider = function (params, callback) {

      if (params.pageSize != comboBox.pageSize) {
        throw 'Invalid pageSize';
      }

      const filterChanged = params.filter !== lastFilter;
      if (filterChanged) {
        cache = {};
        lastFilter = params.filter;
      }

      if (comboBox._clientSideFilter && firstPage) {
        // Data size is less than page size and client has all the data,
        // so client-side filtering is used
        const filteredItems = firstPage.filter(item =>
          comboBox.$connector.filter(item, comboBox.filter));
        callback(filteredItems, filteredItems.size);
        return;
      }

      if (cache[params.page]) {
        // This may happen after skipping pages by scrolling fast
        commitPage(params.page, callback);
      } else {
        const upperLimit = params.pageSize * (params.page + 1);

        if (filterChanged) {
          this._debouncer = Polymer.Debouncer.debounce(
            this._debouncer,
            Polymer.Async.timeOut.after(500),
            () => {
              comboBox.$server.setRequestedRange(0, upperLimit, params.filter);
              if (params.filter === '') {
                // Fixes the case when the filter changes 
                // from '' to something else and back to '' 
                // within debounce timeout, and the
                // DataCommunicator thinks it doesn't need to send data
                comboBox.$server.resetDataCommunicator();
              }
            });
        }
        else {
          comboBox.$server.setRequestedRange(0, upperLimit, params.filter);
        }

        pageCallbacks[params.page] = callback;
      }
    }

    comboBox.$connector.filter = function (item, filter) {
      filter = filter ? filter.toString().toLowerCase() : '';
      return comboBox._getItemLabel(item).toString().toLowerCase().indexOf(filter) > -1;
    }

    comboBox.$connector.set = function (index, items) {
      if (index % comboBox.pageSize != 0) {
        throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + comboBox.pageSize;
      }

      const firstPageToSet = index / comboBox.pageSize;
      const updatedPageCount = Math.ceil(items.length / comboBox.pageSize);

      for (let i = 0; i < updatedPageCount; i++) {
        let page = firstPageToSet + i;
        let slice = items.slice(i * comboBox.pageSize, (i + 1) * comboBox.pageSize);

        cache[page] = slice;
      }
    };

    comboBox.$connector.updateData = function (items) {
      // IE11 doesn't work with the transpiled version of the forEach.
      for (let i = 0; i < items.length; i++) {
        let item = items[i];

        for (let j = 0; j < comboBox.filteredItems.length; j++) {
          if (comboBox.filteredItems[j].key === item.key) {
            comboBox.set('filteredItems.' + j, item);
            break;
          }
        }
      }
    }

    comboBox.$connector.updateSize = function (newSize) {
      comboBox.size = newSize;
    };

    comboBox.$connector.reset = function () {
      pageCallbacks = {};
      cache = {};
      firstPage = undefined;
      comboBox.clearCache();
      comboBox._pendingRequests = {}; // TODO: This can be removed as soon as there's a webjar release newer than 4.2.0-alpha4 (this will be handled by clearCache())
    };

    comboBox.$connector.confirm = function (id) {
      // We're done applying changes from this batch, resolve outstanding
      // callbacks
      let outstandingRequests = Object.getOwnPropertyNames(pageCallbacks);
      for (let i = 0; i < outstandingRequests.length; i++) {
        let page = outstandingRequests[i];

        if (cache[page]) {
          let callback = pageCallbacks[page];
          delete pageCallbacks[page];

          commitPage(page, callback);
        }
      }

      // Let server know we're done
      comboBox.$server.confirmUpdate(id);
    }

    const commitPage = function (page, callback) {
      let data = cache[page];
      delete cache[page];

      if (page == 0) {
        // Keep the data for client-side filtering
        firstPage = data;
      }
      callback(data, comboBox.size);
    }
  }
}
