import { Debouncer } from '@vaadin/component-base/src/debounce.js';
import { timeOut } from '@vaadin/component-base/src/async.js';
import { ComboBoxPlaceholder } from '@vaadin/combo-box/src/vaadin-combo-box-placeholder.js';

window.Vaadin.Flow.comboBoxConnector = {};
window.Vaadin.Flow.comboBoxConnector.initLazy = (comboBox) => {
  // Check whether the connector was already initialized for the ComboBox
  if (comboBox.$connector) {
    return;
  }

  comboBox.$connector = {};

  // holds pageIndex -> callback pairs of subsequent indexes (current active range)
  const pageCallbacks = {};
  let cache = {};
  let lastFilter = '';
  const placeHolder = new window.Vaadin.ComboBoxPlaceholder();

  const serverFacade = (() => {
    // Private variables
    let lastFilterSentToServer = '';
    let dataCommunicatorResetNeeded = false;

    // Public methods
    const needsDataCommunicatorReset = () => (dataCommunicatorResetNeeded = true);
    const getLastFilterSentToServer = () => lastFilterSentToServer;
    const requestData = (startIndex, endIndex, params) => {
      const count = endIndex - startIndex;
      const filter = params.filter;

      comboBox.$server.setViewportRange(startIndex, count, filter);
      lastFilterSentToServer = filter;
      if (dataCommunicatorResetNeeded) {
        comboBox.$server.resetDataCommunicator();
        dataCommunicatorResetNeeded = false;
      }
    };

    return {
      needsDataCommunicatorReset,
      getLastFilterSentToServer,
      requestData
    };
  })();

  const clearPageCallbacks = (pages = Object.keys(pageCallbacks)) => {
    // Flush and empty the existing requests
    pages.forEach((page) => {
      pageCallbacks[page]([], comboBox.size);
      delete pageCallbacks[page];

      // Empty the comboBox's internal cache without invoking observers by filling
      // the filteredItems array with placeholders (comboBox will request for data when it
      // encounters a placeholder)
      const pageStart = parseInt(page) * comboBox.pageSize;
      const pageEnd = pageStart + comboBox.pageSize;
      const end = Math.min(pageEnd, comboBox.filteredItems.length);
      for (let i = pageStart; i < end; i++) {
        comboBox.filteredItems[i] = placeHolder;
      }
    });
  };

  comboBox.dataProvider = function (params, callback) {
    if (params.pageSize != comboBox.pageSize) {
      throw 'Invalid pageSize';
    }

    const filterChanged = params.filter !== lastFilter;
    if (filterChanged) {
      cache = {};
      lastFilter = params.filter;
      comboBox._filterDebouncer = Debouncer.debounce(
        comboBox._filterDebouncer,
        timeOut.after(comboBox._filterTimeout ?? 500),
        () => {
          if (serverFacade.getLastFilterSentToServer() === params.filter) {
            // Fixes the case when the filter changes
            // to something else and back to the original value
            // within debounce timeout, and the
            // DataCommunicator thinks it doesn't need to send data
            serverFacade.needsDataCommunicatorReset();
          }
          if (params.filter !== lastFilter) {
            throw new Error("Expected params.filter to be '" + lastFilter + "' but was '" + params.filter + "'");
          }
          // Remove the debouncer before clearing page callbacks.
          // This makes sure that they are executed.
          comboBox._filterDebouncer = undefined;
          // Call the method again after debounce.
          clearPageCallbacks();
          comboBox.dataProvider(params, callback);
        }
      );
      return;
    }

    // Postpone the execution of new callbacks if there is an active debouncer.
    // They will be executed when the page callbacks are cleared within the debouncer.
    if (comboBox._filterDebouncer) {
      pageCallbacks[params.page] = callback;
      return;
    }

    if (cache[params.page]) {
      // This may happen after skipping pages by scrolling fast
      commitPage(params.page, callback);
    } else {
      pageCallbacks[params.page] = callback;
      const maxRangeCount = Math.max(params.pageSize * 2, 500); // Max item count in active range
      const activePages = Object.keys(pageCallbacks).map((page) => parseInt(page));
      const rangeMin = Math.min(...activePages);
      const rangeMax = Math.max(...activePages);

      if (activePages.length * params.pageSize > maxRangeCount) {
        if (params.page === rangeMin) {
          clearPageCallbacks([String(rangeMax)]);
        } else {
          clearPageCallbacks([String(rangeMin)]);
        }
        comboBox.dataProvider(params, callback);
      } else if (rangeMax - rangeMin + 1 !== activePages.length) {
        // Wasn't a sequential page index, clear the cache so combo-box will request for new pages
        clearPageCallbacks();
      } else {
        // The requested page was sequential, extend the requested range
        const startIndex = params.pageSize * rangeMin;
        const endIndex = params.pageSize * (rangeMax + 1);

        serverFacade.requestData(startIndex, endIndex, params);
      }
    }
  };

  comboBox.$connector.clear = (start, length) => {
    const firstPageToClear = Math.floor(start / comboBox.pageSize);
    const numberOfPagesToClear = Math.ceil(length / comboBox.pageSize);

    for (let i = firstPageToClear; i < firstPageToClear + numberOfPagesToClear; i++) {
      delete cache[i];
    }
  };

  comboBox.$connector.set = (index, items, filter) => {
    if (filter != serverFacade.getLastFilterSentToServer()) {
      return;
    }

    if (index % comboBox.pageSize != 0) {
      throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + comboBox.pageSize;
    }

    if (index === 0 && items.length === 0 && pageCallbacks[0]) {
      // Makes sure that the dataProvider callback is called even when server
      // returns empty data set (no items match the filter).
      cache[0] = [];
      return;
    }

    const firstPageToSet = index / comboBox.pageSize;
    const updatedPageCount = Math.ceil(items.length / comboBox.pageSize);

    for (let i = 0; i < updatedPageCount; i++) {
      let page = firstPageToSet + i;
      let slice = items.slice(i * comboBox.pageSize, (i + 1) * comboBox.pageSize);

      cache[page] = slice;
    }
  };

  comboBox.$connector.updateData = (items) => {
    const itemsMap = new Map(items.map((item) => [item.key, item]));

    comboBox.filteredItems = comboBox.filteredItems.map((item) => {
      return itemsMap.get(item.key) || item;
    });
  };

  comboBox.$connector.updateSize = function (newSize) {
    comboBox.size = newSize;
  };

  comboBox.$connector.reset = function () {
    // Cancel pending requests, as clearCache below will set the combo
    // in a state where it will always request new data, regardless
    // what is in the cache already.
    if (comboBox._filterDebouncer) {
      comboBox._filterDebouncer.cancel();
      comboBox._filterDebouncer = undefined;
    }
    clearPageCallbacks();
    cache = {};
    comboBox.clearCache();
  };

  comboBox.$connector.confirm = function (id, filter) {
    if (filter != serverFacade.getLastFilterSentToServer()) {
      return;
    }

    // We're done applying changes from this batch, resolve pending
    // callbacks
    let activePages = Object.getOwnPropertyNames(pageCallbacks);
    for (let i = 0; i < activePages.length; i++) {
      let page = activePages[i];

      if (cache[page]) {
        commitPage(page, pageCallbacks[page]);
      }
    }

    // Let server know we're done
    comboBox.$server.confirmUpdate(id);
  };

  const commitPage = function (page, callback) {
    let data = cache[page];
    delete cache[page];
    callback(data, comboBox.size);
  };

  // Prevent setting the custom value as the 'value'-prop automatically
  comboBox.addEventListener('custom-value-set', (e) => e.preventDefault());

  comboBox.itemClassNameGenerator = function (item) {
    return item.className || '';
  };
};

window.Vaadin.ComboBoxPlaceholder = ComboBoxPlaceholder;
