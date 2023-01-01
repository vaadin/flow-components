import { ComboBoxPlaceholder } from '@vaadin/combo-box/src/vaadin-combo-box-placeholder.js';
import { RangeDataProvider } from '@vaadin/combo-box/src/vaadin-combo-box-range-data-provider.js';

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

        let lastFilter = '';

        const rangeDataProvider = new RangeDataProvider(
          comboBox,
          ({ pageRange, pageSize, filter }) => {
            const startIndex = pageSize * pageRange[0];
            const endIndex = pageSize * (pageRange[1] + 1);
            const itemsCount = endIndex - startIndex;

            comboBox.$server.setRequestedRange(startIndex, itemsCount, filter);

            if (lastFilter !== filter) {
              comboBox.$server.resetDataCommunicator();
              lastFilter = filter;
            }
          },
          {
            maxRangeSize: 10
          }
        );

        comboBox.$connector.reset = tryCatchWrapper(function () {
          rangeDataProvider.clearLoadedPages();
          rangeDataProvider.flushLoadedPages();
        });

        comboBox.$connector.clear = tryCatchWrapper((startIndex, itemsCount) => {
          const startPage = Math.floor(startIndex / comboBox.pageSize);
          const pagesCount = Math.ceil(itemsCount / comboBox.pageSize);

          const pagesToRemove = [];
          for (let i = 0; i < pagesCount; i++) {
            pagesToRemove.push(startPage + i);
          }
          rangeDataProvider.removeLoadedPages(pagesToRemove);
        });

        comboBox.$connector.set = tryCatchWrapper(function (startIndex, items, filter) {
          if (filter != lastFilter) {
            return;
          }

          if (startIndex % comboBox.pageSize != 0) {
            throw 'Got new data to index ' + startIndex + ' which is not aligned with the page size of ' + comboBox.pageSize;
          }

          if (startIndex === 0 && items.length === 0) {
            // Makes sure that the dataProvider callback is called even when server
            // returns empty data set (no items match the filter).
            rangeDataProvider.clearLoadedPages();
            rangeDataProvider.addLoadedPages({ 0: [] });
            return;
          }

          const startPage = startIndex / comboBox.pageSize;
          const pagesCount = Math.ceil(items.length / comboBox.pageSize);

          const pagesToAdd = {};
          for (let i = 0; i < pagesCount; i++) {
            let page = startPage + i;
            let pageStartIndex = i * comboBox.pageSize;
            let pageEndIndex = (i + 1) * comboBox.pageSize;
            pagesToAdd[page] = items.slice(pageStartIndex, pageEndIndex);
          }
          rangeDataProvider.addLoadedPages(pagesToAdd);
        });

        comboBox.$connector.confirm = tryCatchWrapper(function (id, filter) {
          if (filter != lastFilter) {
            return;
          }

          rangeDataProvider.flushLoadedPages();

          // Let server know we're done
          comboBox.$server.confirmUpdate(id);
        });

        comboBox.$connector.updateData = tryCatchWrapper(function (items) {
          const itemsMap = new Map(items.map((item) => [item.key, item]));

          comboBox.filteredItems = comboBox.filteredItems.map((item) => {
            return itemsMap.get(item.key) || item;
          });
        });

        comboBox.$connector.updateSize = tryCatchWrapper(function (newSize) {
          // FIXME: It may be that this size set is unnecessary, since when
          // providing data to combobox via callback we may use data's size.
          // However, if this size reflect the whole data size, including
          // data not fetched yet into client side, and combobox expect it
          // to be set as such, the at least, we don't need it in case the
          // filter is clientSide only, since it'll increase the height of
          // the popup at only at first user filter to this size, while the
          // filtered items count are less.
          comboBox.size = newSize;
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
