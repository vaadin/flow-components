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
  // Pages whose data has been delivered to filteredItems. Combined with
  // pendingRequests to compute the active range for memory-cap eviction.
  const committedPages = new Set();
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

  const clearPageCallbacks = (pages) => {
    const pendingRequests = getPendingRequests();
    if (pages === undefined) {
      pages = [...new Set([...Object.keys(pendingRequests), ...[...committedPages].map(String)])];
    }
    pages.forEach((page) => {
      // Skip the flush if the page was already committed (its callback
      // fired and pendingRequests entry deleted); the placeholder fill
      // still runs.
      if (pendingRequests[page]) {
        pendingRequests[page]([], comboBox.size);
        delete pendingRequests[page];
      }
      // Refill filteredItems with placeholders so the combo-box re-requests.
      const pageStart = parseInt(page) * comboBox.pageSize;
      const pageEnd = pageStart + comboBox.pageSize;
      const end = Math.min(pageEnd, comboBox.filteredItems.length);
      for (let i = pageStart; i < end; i++) {
        comboBox.filteredItems[i] = placeHolder;
      }
      committedPages.delete(parseInt(page));
    });
  };

  comboBox.dataProvider = function (params, callback) {
    if (params.pageSize != comboBox.pageSize) {
      throw 'Invalid pageSize';
    }

    if (comboBox._clientSideFilter) {
      // For clientside filter we first make sure we have all data which we also
      // filter based on comboBox.filter. While later we only filter clientside data.

      if (cache[0]) {
        performClientSideFilter(cache[0], params.filter, callback);
        return;
      } else {
        // If client side filter is enabled then we need to first ask all data
        // and filter it on client side, otherwise next time when user will
        // input another filter, eg. continue to type, the local cache will be only
        // what was received for the first filter, which may not be the whole
        // data from server (keep in mind that client side filter is enabled only
        // when the items count does not exceed one page).
        params.filter = '';
      }
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
      getPendingRequests()[params.page] = callback;
      return;
    }

    if (cache[params.page]) {
      // This may happen after skipping pages by scrolling fast
      commitPage(params.page, callback);
    } else {
      getPendingRequests()[params.page] = callback;
      const maxRangeCount = Math.max(params.pageSize * 2, 500);
      // activePages covers both pending and committed pages, so the cap
      // triggers on the *total* loaded item count.
      const activePages = [
        ...new Set([...Object.keys(getPendingRequests()).map((page) => parseInt(page)), ...committedPages])
      ];
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
        // Non-contiguous active range. setViewportRange is last-write-wins
        // server-side, so the request must cover every pending page or the
        // earlier ones never receive data and stay stuck on loading=true.
        const pendingPages = Object.keys(getPendingRequests()).map((page) => parseInt(page));
        const newRangeMin = Math.min(...pendingPages);
        const newRangeMax = Math.max(...pendingPages);

        // If the pending-page bounding box itself exceeds the cap (e.g. a
        // deferred page-0 re-fetch racing a deep scrollToIndex), drop the
        // farther extreme and recurse.
        if ((newRangeMax - newRangeMin + 1) * params.pageSize > maxRangeCount) {
          const farthest = pendingPages.reduce((a, b) =>
            Math.abs(a - params.page) >= Math.abs(b - params.page) ? a : b
          );
          clearPageCallbacks([String(farthest)]);
          comboBox.dataProvider(params, callback);
          return;
        }

        const startIndex = params.pageSize * newRangeMin;
        const endIndex = params.pageSize * (newRangeMax + 1);

        // Renderer pages outside the new range hold server-side keys that
        // this RPC will passivate; evict so a scroll-back re-fetches fresh
        // state. Skip the focused page so a focusSelectedItem scroll isn't
        // undone by a stray index-requested for index 0.
        if (comboBox.renderer) {
          const focusedPage = comboBox._focusedIndex >= 0 ? Math.floor(comboBox._focusedIndex / params.pageSize) : -1;
          const pagesToEvict = [...committedPages]
            .filter((page) => (page < newRangeMin || page > newRangeMax) && page !== focusedPage)
            .map(String);
          // clearPageCallbacks(pagesToEvict);
        }

        serverFacade.requestData(startIndex, endIndex, params);
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

  comboBox.$connector.filter = (item, filter) => {
    filter = filter ? filter.toString().toLowerCase() : '';
    return comboBox._getItemLabel(item, comboBox.itemLabelPath).toString().toLowerCase().indexOf(filter) > -1;
  };

  comboBox.$connector.set = (index, items, filter) => {
    if (filter != serverFacade.getLastFilterSentToServer()) {
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
    const pendingRequests = getPendingRequests();
    let activePages = Object.getOwnPropertyNames(pendingRequests);
    for (let i = 0; i < activePages.length; i++) {
      let page = activePages[i];

      if (cache[page]) {
        commitPage(page, pendingRequests[page]);
      }
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
    committedPages.add(parseInt(page));
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

  let focusSelectedItemEnabled = false;
  comboBox.$connector.setFocusSelectedItem = (value) => {
    focusSelectedItemEnabled = !!value;
  };

  // On open, ask the server for the selected item's flat index and scroll
  // to it. The token cancels older invocations at each async boundary
  // (microtask, page-loaded wait, RPC response) — switchMap-style. The
  // RPC-response check is correctness-critical: a stale answer would
  // scroll into the wrong filtered array.
  let focusSelectedItemToken = 0;
  const resolveFocusSelectedItem = () => {
    if (!focusSelectedItemEnabled) return;
    // While filtering, navigation starts from the top of the filtered list.
    if (comboBox.filter) return;
    const token = ++focusSelectedItemToken;
    queueMicrotask(() => {
      if (token !== focusSelectedItemToken) return;
      if (!comboBox.selectedItem) return;
      const selectedValue = comboBox._getItemValue(comboBox.selectedItem);
      const idxOfSelected = comboBox.__getItemIndexByValue(comboBox._dropdownItems, selectedValue);
      if (idxOfSelected >= 0 && idxOfSelected === comboBox._focusedIndex) {
        return;
      }
      const invoke = () => {
        if (token !== focusSelectedItemToken) return;
        comboBox.$server.resolveSelectedItemIndex().then(
          (index) => {
            if (token !== focusSelectedItemToken) return;
            if (index != null) {
              comboBox.scrollToIndex(index);
            }
          },
          () => {}
        );
      };
      if (comboBox.loading) {
        // Wait for loading to fully settle — under overlapping fetches,
        // page-loaded fires per-fetch but the server's filter state may not
        // match the client until all pending fetches land.
        const onPageLoaded = () => {
          if (token !== focusSelectedItemToken) {
            comboBox.__dataProviderController.removeEventListener('page-loaded', onPageLoaded);
            return;
          }
          if (!comboBox.loading) {
            comboBox.__dataProviderController.removeEventListener('page-loaded', onPageLoaded);
            invoke();
          }
        };
        comboBox.__dataProviderController.addEventListener('page-loaded', onPageLoaded);
      } else {
        invoke();
      }
    });
  };

  // `opened-changed` fires only on real opens; `vaadin-combo-box-dropdown-opened`
  // would also fire during the brief `_overlayOpened` toggle that filter
  // changes cause (even though `opened` itself stays true).
  comboBox.addEventListener('opened-changed', (e) => {
    if (e.detail.value === true) {
      resolveFocusSelectedItem();
    }
  });

  // `_setDropdownItems` carries the previously focused item across a filter
  // change by identity, so type-then-clear would re-focus the auto-focused
  // selectedItem. Resetting `_focusedIndex` makes its fallback return -1
  // for an empty filter. The token bump cancels any in-flight resolve whose
  // late scrollToIndex would park `__scrollToPendingIndex` on the next fetch.
  comboBox.addEventListener('filter-changed', () => {
    if (!focusSelectedItemEnabled) return;
    focusSelectedItemToken++;
    comboBox._focusedIndex = -1;
  });

  // On close, wipe client-side cache and arm a DataCommunicator reset so the
  // next open re-fetches. Otherwise the same viewport-range RPC would be a
  // no-op and leave pending callbacks unresolved; the reset also re-keys
  // items so cached pages don't mismatch the freshly-keyed selectedItem.
  comboBox.addEventListener('opened-changed', (e) => {
    if (e.detail.value === false && focusSelectedItemEnabled && comboBox.selectedItem) {
      serverFacade.needsDataCommunicatorReset();
      cache = {};
      committedPages.clear();
      comboBox.__dataProviderController.clearCache();
      comboBox._forceNextRequest = true;
    }
  });

  comboBox.itemClassNameGenerator = function (item) {
    return item.className || '';
  };
};

window.Vaadin.ComboBoxPlaceholder = ComboBoxPlaceholder;
