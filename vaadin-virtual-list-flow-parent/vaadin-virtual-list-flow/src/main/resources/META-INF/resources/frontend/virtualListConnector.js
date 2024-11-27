import { Debouncer } from '@polymer/polymer/lib/utils/debounce.js';
import { timeOut } from '@polymer/polymer/lib/utils/async.js';

window.Vaadin.Flow.virtualListConnector = {
  initLazy: function (list) {
    // Check whether the connector was already initialized for the virtual list
    if (list.$connector) {
      return;
    }

    list.itemIdPath = 'key';
    const extraItemsBuffer = 20;

    let lastRequestedRange = [0, 0];

    list.$connector = {};
    list.$connector.placeholderItem = { __placeholder: true };

    const updateRequestedItem = function () {
      /*
       * TODO virtual list seems to do a small index adjustment after scrolling
       * has stopped. This causes a redundant request to be sent to make a
       * corresponding minimal change to the buffer. We should avoid these
       * requests by making the logic skip doing a request if the available
       * buffer is within some tolerance compared to the requested buffer.
       */
      const visibleIndexes = [...list.children]
        .filter((el) => '__virtualListIndex' in el)
        .map((el) => el.__virtualListIndex);
      const firstNeededItem = Math.min(...visibleIndexes);
      const lastNeededItem = Math.max(...visibleIndexes);

      let first = Math.max(0, firstNeededItem - extraItemsBuffer);
      let last = Math.min(lastNeededItem + extraItemsBuffer, list.items.length);

      if (lastRequestedRange[0] != first || lastRequestedRange[1] != last) {
        lastRequestedRange = [first, last];
        const count = 1 + last - first;
        list.$server.setRequestedRange(first, count);
      }
    };

    const scheduleUpdateRequest = function () {
      list.__requestDebounce = Debouncer.debounce(list.__requestDebounce, timeOut.after(50), updateRequestedItem);
    };

    requestAnimationFrame(() => updateRequestedItem);

    // Add an observer function that will invoke on virtualList.renderer property
    // change and then patches it with a wrapper renderer
    list.patchVirtualListRenderer = function () {
      if (!list.renderer || list.renderer.__virtualListConnectorPatched) {
        // The list either doesn't have a renderer yet or it's already been patched
        return;
      }

      const originalRenderer = list.renderer;

      const renderer = (root, list, model) => {
        root.__virtualListIndex = model.index;

        if (model.item === undefined) {
          if (list.$connector.placeholderElement) {
            // ComponentRenderer
            if (!root.__hasComponentRendererPlaceholder) {
              // The root was previously rendered by the ComponentRenderer. Clear and add a placeholder.
              root.innerHTML = '';
              delete root._$litPart$;
              root.appendChild(list.$connector.placeholderElement.cloneNode(true));
              root.__hasComponentRendererPlaceholder = true;
            }
          } else {
            // LitRenderer
            originalRenderer.call(list, root, list, {
              ...model,
              item: list.$connector.placeholderItem
            });
          }
        } else {
          if (root.__hasComponentRendererPlaceholder) {
            // The root was previously populated with a placeholder. Clear it.
            root.innerHTML = '';
            root.__hasComponentRendererPlaceholder = false;
          }

          originalRenderer.call(list, root, list, model);
        }

        /*
         * Check if we need to do anything once things have settled down.
         * This method is called multiple times in sequence for the same user
         * action, but we only want to do the check once.
         */
        scheduleUpdateRequest();
      };
      renderer.__virtualListConnectorPatched = true;
      renderer.__rendererId = originalRenderer.__rendererId;

      list.renderer = renderer;
    };

    list._createPropertyObserver('renderer', 'patchVirtualListRenderer', true);
    list.patchVirtualListRenderer();

    list.items = [];

    list.$connector.set = function (index, items) {
      list.items.splice(index, items.length, ...items);
      list.$connector.updateItems([...list.items]);
    };

    list.$connector.clear = function (index, length) {
      // How many items, starting from "index", should be set as undefined
      const clearCount = Math.min(length, list.items.length - index);
      list.$connector.set(index, [...Array(clearCount)]);
    };

    list.$connector.updateData = function (items) {
      const updatedItemsMap = items.reduce((map, item) => {
        map[item.key] = item;
        return map;
      }, {});

      const newItems = list.items.map((item) => {
        // Items can be undefined if they are outside the viewport
        if (!item) {
          return item;
        }
        // Replace existing item with updated item,
        // return existing item as fallback if it was not updated
        return updatedItemsMap[item.key] || item;
      });
      list.$connector.updateItems(newItems);
    };

    list.$connector.updateSize = function (newSize) {
      const delta = newSize - list.items.length;
      if (delta > 0) {
        list.$connector.updateItems([...list.items, ...Array(delta)]);
      } else if (delta < 0) {
        list.$connector.updateItems(list.items.slice(0, newSize));
      }
    };

    list.$connector.setPlaceholderItem = function (placeholderItem = {}, appId) {
      placeholderItem.__placeholder = true;
      list.$connector.placeholderItem = placeholderItem;
      const nodeId = Object.entries(placeholderItem).find(([key]) => key.endsWith('_nodeid'));
      list.$connector.placeholderElement = nodeId ? Vaadin.Flow.clients[appId].getByNodeId(nodeId[1]) : null;
    };

    list.$connector.updateItems = function (items) {
      // Update the virtual list's items
      list.items = items;

      // Update the virtual list's selectedItems
      list.$connector.__updatingSelectedItemsFromServer = true;
      list.selectedItems = items.filter((item) => item && item.selected);
      list.$connector.__updatingSelectedItemsFromServer = false;
    };

    let previousSelectedKeys = [];

    list.addEventListener('selected-items-changed', function (event) {
      const selectedKeys = event.detail.value.map((item) => item.key);
      const addedKeys = selectedKeys.filter((key) => !previousSelectedKeys.includes(key));
      const removedKeys = previousSelectedKeys.filter((key) => !selectedKeys.includes(key));
      previousSelectedKeys = selectedKeys;

      if (list.$connector.__updatingSelectedItemsFromServer) {
        // Items are being updated from the server, don't send the selection changes back
        return;
      }

      // If server sends partial updates while still making selections, other items might get temporarily
      // de-selected / selected if their state is now yet synced from the server.
      // Workaround the issue by updating the item selection state immediately on the client.
      list.items.filter((item) => item).forEach((item) => (item.selected = selectedKeys.includes(item.key)));

      list.$server.updateSelection(addedKeys, removedKeys);
    });
  }
};
