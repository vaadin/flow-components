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

  const getPendingRequests = () => comboBox.__dataProviderController.rootCache.pendingRequests;
  let cache = {};
  const placeHolder = new window.Vaadin.ComboBoxPlaceholder();
  let lastRequestedRange = [-1, -1];
  let hasData = false;
  let lastFilter = '';
  let requestDebouncer;
  let filterDebouncer;

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

  const clearPageCallbacks = (pages = Object.keys(getPendingRequests())) => {
    // Flush and empty the existing requests
    const pendingRequests = getPendingRequests();
    pages.forEach((page) => {
      pendingRequests[page]([], comboBox.size);

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

    if (params.filter !== lastFilter) {
      lastFilter = params.filter;
      cache = {};
      lastRequestedRange = [-1, -1];
      filterDebouncer = Debouncer.debounce(filterDebouncer, timeOut.after(comboBox._filterTimeout ?? 500), () => {
        // Filter cycled back to previously sent value — force re-emit.
        if (params.filter === serverFacade.getLastFilterSentToServer()) {
          serverFacade.needsDataCommunicatorReset();
        }
        comboBox.$connector.requestPage(params.page, params.filter);
      });
      return;
    }

    if (filterDebouncer?.isActive()) {
      return;
    }

    requestDebouncer = Debouncer.debounce(requestDebouncer, timeOut.after(hasData ? 150 : 0), () => {
      comboBox.$connector.requestPage(params.page, params.filter);
    });
  };

  comboBox.$connector.getViewportRange = function () {
    return [comboBox._scroller.__virtualizer.firstVisibleIndex, comboBox._scroller.__virtualizer.lastVisibleIndex];
  };

  comboBox.$connector.requestPage = function (page, filter) {
    let viewportRange = comboBox.$connector.getViewportRange();
    const buffer = viewportRange[1] - viewportRange[0];
    viewportRange[0] = Math.max(viewportRange[0] - buffer, 0);
    viewportRange[1] = Math.min(viewportRange[1] + buffer, comboBox.size);

    let viewportPageRange = [
      Math.floor(viewportRange[0] / comboBox.pageSize),
      Math.floor(viewportRange[1] / comboBox.pageSize)
    ];

    // Collapse to the requested page when it's outside the current viewport,
    // so confirm() can resolve callbacks left behind by fast scrolling.
    if (page < viewportPageRange[0] || page > viewportPageRange[1]) {
      viewportPageRange = [page, page];
    }

    if (lastRequestedRange[0] != viewportPageRange[0] || lastRequestedRange[1] != viewportPageRange[1]) {
      lastRequestedRange = viewportPageRange;
      const startIndex = viewportPageRange[0] * comboBox.pageSize;
      const endIndex = (viewportPageRange[1] + 1) * comboBox.pageSize;
      serverFacade.requestData(startIndex, endIndex, { filter });
    }
  };

  comboBox.$connector.clear = (start, length) => {
    const firstPageToClear = Math.floor(start / comboBox.pageSize);
    const numberOfPagesToClear = Math.ceil(length / comboBox.pageSize);

    for (let i = firstPageToClear; i < firstPageToClear + numberOfPagesToClear; i++) {
      delete cache[i];
      for (let j = i * comboBox.pageSize; j < (i + 1) * comboBox.pageSize; j++) {
        if (comboBox.filteredItems[j]) {
          comboBox.filteredItems[j] = placeHolder;
        }
      }
    }
  };

  comboBox.$connector.filter = (item, filter) => {
    filter = filter ? filter.toString().toLowerCase() : '';
    return comboBox._getItemLabel(item, comboBox.itemLabelPath).toString().toLowerCase().indexOf(filter) > -1;
  };

  comboBox.$connector.set = (index, items, filter) => {
    if (filter !== lastFilter) {
      return;
    }

    if (index % comboBox.pageSize != 0) {
      throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + comboBox.pageSize;
    }

    if (index === 0 && items.length === 0 && getPendingRequests()[0]) {
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

    if (items.length > 0) {
      hasData = true;
    }
  };

  comboBox.$connector.updateData = (items) => {
    const itemsMap = new Map(items.map((item) => [item.key, item]));

    comboBox.filteredItems = comboBox.filteredItems.map((item) => {
      return itemsMap.get(item.key) || item;
    });
  };

  comboBox.$connector.updateSize = function (newSize) {
    if (!comboBox._clientSideFilter) {
      // FIXME: It may be that this size set is unnecessary, since when
      // providing data to combobox via callback we may use data's size.
      // However, if this size reflect the whole data size, including
      // data not fetched yet into client side, and combobox expect it
      // to be set as such, the at least, we don't need it in case the
      // filter is clientSide only, since it'll increase the height of
      // the popup at only at first user filter to this size, while the
      // filtered items count are less.
      comboBox.size = newSize;
    }
  };

  comboBox.$connector.reset = function () {
    filterDebouncer?.cancel();
    requestDebouncer?.cancel();
    clearPageCallbacks();
    cache = {};
    lastRequestedRange = [-1, -1];
    hasData = false;
    lastFilter = '';
    comboBox.clearCache();
  };

  comboBox.$connector.confirm = function (id, filter) {
    if (filter !== lastFilter) {
      return;
    }

    // We're done applying changes from this batch, resolve pending
    // callbacks
    Object.entries(getPendingRequests()).forEach(([page, callback]) => {
      if (cache[page]) {
        commitPage(page, callback);
      }
    });

    // Resolve callbacks left pending for pages outside the requested range
    // so `comboBox.loading` doesn't stay true after fast scrolling.
    const stalePages = Object.keys(getPendingRequests()).filter(
      (page) => +page < lastRequestedRange[0] || +page > lastRequestedRange[1]
    );
    if (stalePages.length > 0) {
      clearPageCallbacks(stalePages);
    }

    // Let server know we're done
    comboBox.$server.confirmUpdate(id);
  };

  const commitPage = function (page, callback) {
    let data = cache[page];

    if (comboBox._clientSideFilter) {
      performClientSideFilter(data, comboBox.filter, callback);
    } else {
      // Remove the data if server-side filtering, but keep it for client-side
      // filtering
      delete cache[page];

      // FIXME: It may be that we ought to provide data.length instead of
      // comboBox.size and remove updateSize function.
      callback(data, comboBox.size);
    }
  };

  // Perform filter on client side (here) using the items from specified page
  // and submitting the filtered items to specified callback.
  // The filter used is the one from combobox, not the lastFilter stored since
  // that may not reflect user's input.
  const performClientSideFilter = function (page, filter, callback) {
    let filteredItems = page;

    if (filter) {
      filteredItems = page.filter((item) => comboBox.$connector.filter(item, filter));
    }

    callback(filteredItems, filteredItems.length);
  };

  // Prevent setting the custom value as the 'value'-prop automatically
  comboBox.addEventListener('custom-value-set', (e) => e.preventDefault());

  comboBox.itemClassNameGenerator = function (item) {
    return item.className || '';
  };
};

window.Vaadin.ComboBoxPlaceholder = ComboBoxPlaceholder;
