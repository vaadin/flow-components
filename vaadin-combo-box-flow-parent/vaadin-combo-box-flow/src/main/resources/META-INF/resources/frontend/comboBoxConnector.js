import { Debouncer } from '@polymer/polymer/lib/utils/debounce.js';
import { timeOut } from '@polymer/polymer/lib/utils/async.js';
import { ComboBoxPlaceholder } from '@vaadin/combo-box/src/vaadin-combo-box-placeholder.js';
import { createRangeDataProvider } from '@vaadin/combo-box/src/vaadin-combo-box-range-data-provider.js';

(function () {
  const tryCatchWrapper = function (callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Combo Box');
  };

  window.Vaadin.Flow.comboBoxConnector = {
    initLazy: (comboBox) =>
      tryCatchWrapper(function (comboBox) {
        // Check whether the connector was already initialized for the ComboBox
        if (comboBox.$connector) {
          return;
        }

        comboBox.$connector = {};

        let filterDebouncer;
        let lastFilter = '';

        comboBox.dataProvider = createRangeDataProvider(
          ({ pageRange, pageSize, filter }) => {
            if (lastFilter !== filter) {
              lastFilter = filter;
              filterDebouncer = Debouncer.debounce(filterDebouncer, timeOut.after(500), () => {
                // Trigger the web component to reload visible pages with the new filter.
                comboBox.clearCache();
              });
              return;
            }

            // Ignore page requests that come while the filter debouncer is active.
            // Those pages will get a chance to be requested again after
            // `clearCache()` in the debouncer.
            if (filterDebouncer && filterDebouncer.isActive()) {
              return;
            }

            // Request the range from the server.
            const startIndex = pageSize * pageRange[0];
            const endIndex = pageSize * (pageRange[1] + 1);
            const itemsCount = endIndex - startIndex;
            comboBox.$server.setRequestedRange(startIndex, itemsCount, filter);

            // If the user interacted with the filter by any means,
            // force the data communicator to send the whole range,
            // even if the filter has remained the same in the end.
            if (filterDebouncer) {
              filterDebouncer = null;
              comboBox.$server.resetDataCommunicator();
            }
          },
          {
            maxRangeSize: 10
          }
        );

        comboBox.$connector.updateData = tryCatchWrapper(function (items) {
          const itemsMap = new Map(items.map((item) => [item.key, item]));

          comboBox.filteredItems = comboBox.filteredItems.map((item) => {
            return itemsMap.get(item.key) || item;
          });
        });

        comboBox.$connector.reset = tryCatchWrapper(function () {
          comboBox.clearCache();
        });

        comboBox.$connector.commit = tryCatchWrapper(function (startIndex, items, size, filter, updateId) {
          if (filter != lastFilter) {
            return;
          }

          if (startIndex % comboBox.pageSize != 0) {
            throw (
              'Got new data to index ' + startIndex + ' which is not aligned with the page size of ' + comboBox.pageSize
            );
          }

          const startPage = startIndex / comboBox.pageSize;
          const pagesCount = Math.ceil(items.length / comboBox.pageSize);
          const pages = {};

          if (startIndex === 0 && items.length === 0) {
            // Makes sure that the dataProvider callback is called even when server
            // returns empty data set (no items match the filter).
            pages[0] = [];
          } else {
            for (let i = 0; i < pagesCount; i++) {
              const page = startPage + i;
              const pageStartIndex = i * comboBox.pageSize;
              const pageEndIndex = (i + 1) * comboBox.pageSize;
              pages[page] = items.slice(pageStartIndex, pageEndIndex);
            }
          }

          comboBox.dataProvider.onPagesLoaded(pages, size);

          // Let server know we're done
          comboBox.$server.confirmUpdate(updateId);
        });

        // Prevent setting the custom value as the 'value'-prop automatically
        comboBox.addEventListener(
          'custom-value-set',
          tryCatchWrapper((e) => e.preventDefault())
        );
      })(comboBox)
  };
})();

window.Vaadin.ComboBoxPlaceholder = ComboBoxPlaceholder;
