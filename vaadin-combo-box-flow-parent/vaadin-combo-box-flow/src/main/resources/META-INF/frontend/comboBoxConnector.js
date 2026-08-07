import { Debouncer } from '@vaadin/component-base/src/debounce.js';
import { timeOut } from '@vaadin/component-base/src/async.js';
import { ComboBoxPlaceholder } from '@vaadin/combo-box/src/vaadin-combo-box-placeholder.js';

/**
 * comboBoxConnector is a communication layer between ComboBox's flow component
 * (server-side) and web component (client-side).
 */
export class ComboBoxConnector {
  #comboBox;
  #placeholder = new window.Vaadin.ComboBoxPlaceholder();

  #cache = {};

  #lastTypedFilter = '';
  #lastRequestedRange = [-1, -1];
  #lastRequestedFilter = '';
  #needsDataCommunicatorReset = false;

  constructor(comboBox) {
    this.#comboBox = comboBox;

    // Prevent setting the custom value as the 'value'-prop automatically
    comboBox.addEventListener('custom-value-set', (e) => e.preventDefault());

    comboBox.itemClassNameGenerator = (item) => item.className || '';

    // Assign last: setting the data provider can synchronously trigger a first
    // page load that calls back into the connector.
    comboBox.dataProvider = (params, callback) => this.#loadPage(params, callback);
  }

  clear(start, length) {
    const comboBox = this.#comboBox;
    const { pageSize } = comboBox;
    const firstPage = Math.floor(start / pageSize);
    const lastPage = firstPage + Math.ceil(length / pageSize);

    for (let page = firstPage; page < lastPage; page++) {
      delete this.#cache[page];
    }

    for (let index = firstPage * pageSize; index < lastPage * pageSize; index++) {
      if (comboBox.filteredItems[index]) {
        comboBox.filteredItems[index] = this.#placeholder;
      }
    }
  }

  set(index, items, filter) {
    const comboBox = this.#comboBox;

    if (filter !== this.#lastTypedFilter) {
      return;
    }

    if (index % comboBox.pageSize != 0) {
      throw 'Got new data to index ' + index + ' which is not aligned with the page size of ' + comboBox.pageSize;
    }

    const { pendingRequests } = comboBox.__dataProviderController.rootCache;
    if (index === 0 && items.length === 0 && pendingRequests[0]) {
      // Makes sure that the dataProvider callback is called even when server
      // returns empty data set (no items match the filter).
      this.#cache[0] = [];
      return;
    }

    const firstPageToSet = index / comboBox.pageSize;
    const updatedPageCount = Math.ceil(items.length / comboBox.pageSize);

    for (let i = 0; i < updatedPageCount; i++) {
      const page = firstPageToSet + i;
      const slice = items.slice(i * comboBox.pageSize, (i + 1) * comboBox.pageSize);

      this.#cache[page] = slice;
    }
  }

  updateData(items) {
    const comboBox = this.#comboBox;
    const itemsMap = new Map(items.map((item) => [item.key, item]));

    comboBox.filteredItems = comboBox.filteredItems.map((item) => {
      return itemsMap.get(item.key) || item;
    });
  }

  updateSize(newSize) {
    const comboBox = this.#comboBox;

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
  }

  reset() {
    const comboBox = this.#comboBox;

    comboBox._filterDebouncer?.cancel();
    comboBox._filterDebouncer = null;
    this.#cache = {};
    this.#lastRequestedRange = [-1, -1];
    this.#lastTypedFilter = '';
    comboBox.clearCache();
  }

  confirm(id, filter) {
    const comboBox = this.#comboBox;

    if (filter !== this.#lastTypedFilter) {
      return;
    }

    // We're done applying changes from this batch, resolve pending
    // callbacks
    const { pendingRequests } = comboBox.__dataProviderController.rootCache;
    Object.entries(pendingRequests).forEach(([page, callback]) => {
      const items = this.#cache[page];

      if (comboBox._clientSideFilter && items) {
        this.#performClientSideFilter(items, comboBox.filter, callback);
        return;
      }

      callback(items ?? [], comboBox.size);
      delete this.#cache[page];
    });

    // Let server know we're done
    comboBox.$server.confirmUpdate(id);
  }

  #loadPage(params, callback) {
    const comboBox = this.#comboBox;

    if (params.pageSize != comboBox.pageSize) {
      throw 'Invalid pageSize';
    }

    if (comboBox._clientSideFilter) {
      if (this.#cache[0]) {
        this.#performClientSideFilter(this.#cache[0], params.filter, callback);
        return;
      }

      // First fetch: ignore the typed filter so we get the full dataset
      params = { ...params, filter: '' };
    }

    if (this.#lastTypedFilter !== params.filter) {
      this.#cache = {};
      this.#lastTypedFilter = params.filter;
      this.#lastRequestedRange = [-1, -1];

      comboBox._filterDebouncer = Debouncer.debounce(
        comboBox._filterDebouncer,
        timeOut.after(comboBox._filterTimeout ?? 500),
        () => {
          // Filter cycled back to what server last received — force re-emit.
          if (params.filter === this.#lastRequestedFilter) {
            this.#needsDataCommunicatorReset = true;
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
    if (this.#cache[params.page]) {
      callback(this.#cache[params.page], comboBox.size);
      return;
    }

    this.#requestPage(params.page, params.filter);
  }

  /**
   * Asks the server for the pages around the viewport, so that scrolling has
   * data ready ahead of rendering.
   */
  #requestPage(page, filter) {
    const comboBox = this.#comboBox;

    const viewportRange = this.#getViewportRange();
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

    if (this.#lastRequestedRange[0] != viewportPageRange[0] || this.#lastRequestedRange[1] != viewportPageRange[1]) {
      const startIndex = viewportPageRange[0] * comboBox.pageSize;
      const endIndex = (viewportPageRange[1] + 1) * comboBox.pageSize;
      comboBox.$server.setViewportRange(startIndex, endIndex - startIndex, filter);
    }

    if (this.#needsDataCommunicatorReset) {
      comboBox.$server.resetDataCommunicator();
      this.#needsDataCommunicatorReset = false;
    }

    this.#lastRequestedRange = viewportPageRange;
    this.#lastRequestedFilter = filter;
  }

  /** The range of item indexes currently rendered in the dropdown */
  #getViewportRange() {
    const comboBox = this.#comboBox;

    const indices = Array.from(comboBox._scroller?.children ?? [])
      .map((child) => child.index)
      .filter((index) => Number.isFinite(index))
      .sort((a, b) => a - b);
    if (indices.length === 0) {
      return [0, 0];
    }
    return [indices[0], indices[indices.length - 1]];
  }

  #matchesFilter(item, filter) {
    filter = filter ? filter.toString().toLowerCase() : '';
    return (
      this.#comboBox._getItemLabel(item, this.#comboBox.itemLabelPath).toString().toLowerCase().indexOf(filter) > -1
    );
  }

  /**
   * Perform filter on client side (here) using the items from specified page
   * and submitting the filtered items to specified callback.
   * The filter used is the one from combobox, not the lastFilter stored since
   * that may not reflect user's input.
   */
  #performClientSideFilter(page, filter, callback) {
    let filteredItems = page;

    if (filter) {
      filteredItems = page.filter((item) => this.#matchesFilter(item, filter));
    }

    callback(filteredItems, filteredItems.length);
  }
}

function initLazy(comboBox) {
  // Init the connector only once for the combo box
  comboBox.$connector ??= new ComboBoxConnector(comboBox);
}

window.Vaadin.Flow.comboBoxConnector = { initLazy };

window.Vaadin.ComboBoxPlaceholder = ComboBoxPlaceholder;
