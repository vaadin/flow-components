/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
// Not using ES6 imports in this file yet because the connector in V14 must
// still work in Legacy bower projects. See: `ironListConnector-es6.js` for 
// the Polymer3 approach.
window.Vaadin.Flow.Legacy = window.Vaadin.Flow.Legacy || {};

window.Vaadin.Flow.ironListConnector = {
  initLazy: function(list) {

    // Check whether the connector was already initialized for the Iron list
    if (list.$connector){
      return;
    }

    if (window.Polymer) {
        // Polymer2 approach.
        window.Vaadin.Flow.Legacy.Debouncer = window.Vaadin.Flow.Legacy.Debouncer || Polymer.Debouncer;
        window.Vaadin.Flow.Legacy.timeOut = window.Vaadin.Flow.Legacy.timeOut || Polymer.Async.timeOut;
    } else if (!window.Vaadin.Flow.Legacy.Debouncer) {
      console.log("IronList is unable to load Polymer helpers.");
      return;
    }

    const Debouncer = window.Vaadin.Flow.Legacy.Debouncer;
    const timeOut = window.Vaadin.Flow.Legacy.timeOut;

    const extraItemsBuffer = 20;

    let lastRequestedRange = [0, 0];

    list.$connector = {};
    list.$connector.placeholderItem = {__placeholder: true};

    const updateRequestedItem = function() {
        /*
         * TODO Iron list seems to do a small index adjustment after scrolling
         * has stopped. This causes a redundant request to be sent to make a
         * corresponding minimal change to the buffer. We should avoid these
         * requests by making the logic skip doing a request if the available
         * buffer is within some tolerance compared to the requested buffer.
         */
        let firstNeededItem = list._virtualStart;
        let lastNeededItem = list._virtualEnd;

        let first = Math.max(0,  firstNeededItem - extraItemsBuffer);
        let last = Math.min(lastNeededItem + extraItemsBuffer, list.items.length);

        if (lastRequestedRange[0] != first || lastRequestedRange[1] != last) {
          lastRequestedRange = [first, last];
          const count = 1 + last - first;
          list.$server.setRequestedRange(first, count);
        }
    }

    let requestDebounce;
    const scheduleUpdateRequest = function() {
        requestDebounce = Debouncer.debounce(
                requestDebounce,
                  timeOut.after(10),
                  updateRequestedItem);
    }

    /*
     * Ensure all items that iron list will be looking at are actually defined.
     * If this is not done, the component will keep looking ahead through the
     * array until finding enough present items to render. In our case, that's
     * a really slow way of achieving nothing since the rest of the array is
     * empty.
     */
    const originalAssign = list._assignModels;
    list._assignModels = function() {
        const tempItems = [];
        const start = list._virtualStart;
        const count = Math.min(list.items.length, list._physicalCount);
        for(let i = 0; i < count; i++) {
            if (list.items[start + i] === undefined) {
                tempItems.push(i);
                list.items[start + i] = list.$connector.placeholderItem;
            }
        }

        originalAssign.apply(list, arguments);

        /*
         * TODO: Keep track of placeholder items in the "active" range and
         * avoid deleting them so that the next pass will be faster. Instead,
         * the end of each pass should only delete placeholders that are no
         * longer needed.
         */
        for(let i = 0; i < tempItems.length; i++) {
            delete list.items[start + tempItems[i]];
        }

        /*
         * Check if we need to do anything once things have settled down.
         * This method is called multiple times in sequence for the same user
         * action, but we only want to do the check once.
         */
        scheduleUpdateRequest();
    }

    list.items = [];

    list.$connector.set = function(index, items) {
        for(let i = 0; i < items.length; i++) {
            const itemsIndex = index + i;
            list.items[itemsIndex] = items[i];
        }
        // Do a full render since dirty detection for splices is broken
        list._render();
    };

    list.$connector.updateData = function(items) {
        // Find the items by key inside the list update them
        const oldItems = list.items;
        const mapByKey = {};
        let leftToUpdate = items.length;

        for (let i = 0; i< items.length; i++) {
            const item = items[i];
            mapByKey[item.key] = item;
        }

        for (let i = 0; i< oldItems.length; i++) {
            const oldItem = oldItems[i];
            const newItem = mapByKey[oldItem.key];
            if (newItem) {
                list.items[i] = newItem;
                list.notifyPath("items." + i)
                leftToUpdate--;
                if (leftToUpdate == 0) {
                    break;
                }
            }
        }
    };

    list.$connector.clear = function(index, length) {
        for(let i = 0; i < length; i++) {
            const itemsIndex = index + i;
            delete list.items[itemsIndex];

            // Most likely a no-op since the affected index isn't in view
            list.notifyPath("items." + itemsIndex)
        }
    };

    list.$connector.updateSize = function(newSize) {
        const delta = newSize - list.items.length;
        if (delta > 0) {
            list.items.length = newSize;

            list.notifySplices("items", [{index: newSize - delta, removed: [], addedCount : delta, object: list.items, type: "splice"}]);
        } else if (delta < 0){
            const removed = list.items.slice(newSize, list.items.length);
            list.items.splice(newSize);
            list.notifySplices("items", [{index: newSize, removed: removed, addedCount : 0, object: list.items, type: "splice"}]);
        }
    };

    list.$connector.setPlaceholderItem = function(placeholderItem) {
        if (!placeholderItem) {
            placeholderItem = {};
        }
        placeholderItem.__placeholder = true;
        list.$connector.placeholderItem = placeholderItem;
    };
  }
}
