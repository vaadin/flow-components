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

  let cache = {};
  const placeHolder = new window.Vaadin.ComboBoxPlaceholder();

  let lastTypedFilter = '';
  let lastRequestedRange = [-1, -1];
  let lastRequestedFilter = '';
  let needsDataCommunicatorReset = false;

  comboBox.dataProvider = function (params, callback) {
    if (params.pageSize != comboBox.pageSize) {
      throw 'Invalid pageSize';
    }

    if (comboBox._clientSideFilter) {
      if (cache[0]) {
        performClientSideFilter(cache[0], params.filter, callback);
        return;
      }

      // First fetch: ignore the typed filter so we get the full dataset
      params = { ...params, filter: '' };
    }

    if (lastTypedFilter !== params.filter) {
      cache = {};
      lastTypedFilter = params.filter;
      lastRequestedRange = [-1, -1];

      comboBox._filterDebouncer = Debouncer.debounce(
        comboBox._filterDebouncer,
        timeOut.after(comboBox._filterTimeout ?? 500),
        () => {
          // Filter cycled back to what server last received — force re-emit.
          if (params.filter === lastRequestedFilter) {
            needsDataCommunicatorReset = true;
          }

          comboBox.clearCache();
        }
      );
      return;
    }

    if (comboBox._filterDebouncer?.isActive()) {
      return;
    }

    // If buffer-prefetch already cached this page, commit it without a server
    // round-trip; otherwise ask the server.
    if (cache[params.page]) {
      callback(cache[params.page], comboBox.size);
      return;
    }

    comboBox.$connector.requestPage(params.page, params.filter);
  };

  comboBox.$connector.getViewportRange = function () {
    const indices = Array.from(comboBox._scroller?.children ?? [])
      .map((child) => child.index)
      .filter((index) => Number.isFinite(index))
      .sort((a, b) => a - b);
    if (indices.length === 0) {
      return [0, 0];
    }
    return [indices[0], indices[indices.length - 1]];
  };

  comboBox.$connector.requestPage = function (page, filter) {
    let viewportRange = comboBox.$connector.getViewportRange();
    const buffer = viewportRange[1] - viewportRange[0];
    const sizeLimit = Number.isFinite(comboBox.size) ? comboBox.size : Number.POSITIVE_INFINITY;
    viewportRange[0] = Math.max(viewportRange[0] - buffer, 0);
    viewportRange[1] = Math.min(viewportRange[1] + buffer, sizeLimit - 1);

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
      const startIndex = viewportPageRange[0] * comboBox.pageSize;
      const endIndex = (viewportPageRange[1] + 1) * comboBox.pageSize;
      comboBox.$server.setViewportRange(startIndex, endIndex - startIndex, filter);
    }

    if (needsDataCommunicatorReset) {
      comboBox.$server.resetDataCommunicator();
      needsDataCommunicatorReset = false;
    }

    lastRequestedRange = viewportPageRange;
    lastRequestedFilter = filter;
  };

  comboBox.$connector.clear = (start, length) => {
    const { pageSize } = comboBox;
    const firstPage = Math.floor(start / pageSize);
    const lastPage = firstPage + Math.ceil(length / pageSize);

    for (let page = firstPage; page < lastPage; page++) {
      delete cache[page];
    }

    for (let index = firstPage * pageSize; index < lastPage * pageSize; index++) {
      if (comboBox.filteredItems[index]) {
        comboBox.filteredItems[index] = placeHolder;
      }
    }
  };

  comboBox.$connector.filter = (item, filter) => {
    filter = filter ? filter.toString().toLowerCase() : '';
    return comboBox._getItemLabel(item, comboBox.itemLabelPath).toString().toLowerCase().indexOf(filter) > -1;
  };

  comboBox.$connector.set = (index, items, filter) => {
    if (filter !== lastTypedFilter) {
      return;
    }

    if (index % comboBox.pageSize != 0) {
      throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + comboBox.pageSize;
    }

    const { pendingRequests } = comboBox.__dataProviderController.rootCache;
    if (index === 0 && items.length === 0 && pendingRequests[0]) {
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
    comboBox._filterDebouncer?.cancel();
    comboBox._filterDebouncer = null;
    cache = {};
    lastRequestedRange = [-1, -1];
    lastTypedFilter = '';
    comboBox.clearCache();
  };

  comboBox.$connector.confirm = function (id, filter) {
    if (filter !== lastTypedFilter) {
      return;
    }

    // We're done applying changes from this batch, resolve pending
    // callbacks
    const { pendingRequests } = comboBox.__dataProviderController.rootCache;
    Object.entries(pendingRequests).forEach(([page, callback]) => {
      const items = cache[page];

      if (comboBox._clientSideFilter && items) {
        performClientSideFilter(items, comboBox.filter, callback);
        return;
      }

      callback(items ?? [], comboBox.size);
      delete cache[page];
    });

    // Let server know we're done
    comboBox.$server.confirmUpdate(id);
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
