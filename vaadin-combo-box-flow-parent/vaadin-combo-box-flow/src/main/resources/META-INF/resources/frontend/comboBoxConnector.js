import { Debouncer } from '@vaadin/component-base/src/debounce.js';
import { timeOut } from '@vaadin/component-base/src/async.js';
import { ComboBoxPlaceholder } from '@vaadin/combo-box/src/vaadin-combo-box-placeholder.js';

function isRangeEqual(range1, range2) {
  return new Set(range1).difference(new Set(range2)).size === 0;
}

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
  let requestedRange = null;
  let requestedFilter = '';
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
      requestedRange = null;

      comboBox._filterDebouncer = Debouncer.debounce(
        comboBox._filterDebouncer,
        timeOut.after(comboBox._filterTimeout ?? 500),
        () => {
          // Filter cycled back to what server last received — force re-emit.
          if (params.filter === requestedFilter) {
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

    comboBox.$connector.fetchCurrentRange(params.filter);
  };

  comboBox.$connector.getRenderedRange = function () {
    const indices = Array.from(comboBox._scroller?.children ?? [])
      .map((child) => child.index)
      .filter((index) => Number.isFinite(index))
      .sort((a, b) => a - b);
    if (indices.length === 0) {
      return [0, 0];
    }
    return [indices[0], indices[indices.length - 1]];
  };

  comboBox.$connector.getFetchRange = function () {
    // Get the range of currently rendered rows
    let range = comboBox.$connector.getRenderedRange();

    // Expand the range in both directions to add a buffer
    const buffer = range[1] - range[0];
    const sizeLimit = Number.isFinite(comboBox.size) ? comboBox.size : Number.POSITIVE_INFINITY;
    range[0] = Math.max(range[0] - buffer, 0);
    range[1] = Math.min(range[1] + buffer, sizeLimit);

    // Align the range to page boundaries. range[1] is inclusive of the last
    // rendered row, so round it up to the end of that row's page.
    range[0] = Math.floor(range[0] / comboBox.pageSize) * comboBox.pageSize;
    range[1] = (Math.floor(range[1] / comboBox.pageSize) + 1) * comboBox.pageSize;

    return range;
  };

  comboBox.$connector.fetchCurrentRange = async (filter) => {
    const range = comboBox.$connector.getFetchRange();

    if (isRangeEqual(range, requestedRange) && filter === requestedFilter) {
      // Skip duplicate requests for the same range and filter.
      return;
    }

    requestedRange = range;
    requestedFilter = filter;

    const promise = comboBox.$server.setViewportRange(range[0], range[1] - range[0], filter);

    if (needsDataCommunicatorReset) {
      comboBox.$server.resetDataCommunicator();
      needsDataCommunicatorReset = false;
    }

    await promise;

    if (isRangeEqual(range, requestedRange)) {
      // If requestedRange is still set and matches the current range, it means
      // the server responded with no new data and $connector.confirm wasn't called
      // because the server assumes all the data is already on the client. This can
      // happen, for example, when scrolling quickly back and forth so that the
      // combo-box returns to a position whose data has already been delivered and
      // is cached. In this case, just resolve the callbacks so the combo-box can
      // exit the loading state correctly.
      comboBox.$connector.resolvePendingCallbacks(filter);
    }
  };

  comboBox.$connector.resolvePendingCallbacks = (filter) => {
    if (filter !== lastTypedFilter) {
      return;
    }

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

    // If no new data provider requests came in while resolving the callbacks
    // above, clear the current requested range and filter to allow subsequent
    // fetches for the same range and filter to proceed.
    if (Object.values(pendingRequests).length === 0) {
      requestedRange = null;
      requestedFilter = '';
    }
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
    requestedRange = null;
    requestedFilter = '';
    lastTypedFilter = '';
    comboBox.clearCache();
  };

  comboBox.$connector.confirm = function (id, filter) {
    // We're done applying changes from this batch, resolve pending callbacks
    comboBox.$connector.resolvePendingCallbacks(filter);

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
