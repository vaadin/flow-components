(function() {
  function tryCatchWrapper(callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Context Menu');
  }

  function getContainer(appId, nodeId) {
    try {
      return window.Vaadin.Flow.clients[appId].getByNodeId(nodeId);
    } catch (error) {
      console.error("Could not get node %s from app %s", nodeId, appId);
      console.error(error);
    }
  }

  window.Vaadin.Flow.contextMenuConnector = {
    initLazy: tryCatchWrapper((appId) => {
      if (contextMenu.$connector) {
        return;
      }

      contextMenu.$connector = {
        /**
         * @param {string} nodeId
         */
        assignItems: tryCatchWrapper((nodeId) => {
          const items = window.Vaadin.Flow.contextMenuConnector.constructItemsTree(
            appId,
            nodeId
          );

          contextMenu.items = items;
        })
      }
    }),

    /**
     * @param {HTMLElement} itemElement
     * @param {string | undefined | null} theme
     */
    setItemTheme: tryCatchWrapper((itemElement, theme) => {
      if (itemElement._item) {
        itemElement._item.theme = theme;
      }
    }),

    /**
     * @param {HTMLElement} itemElement
     * @param {boolean} checked
     */
    setItemChecked: tryCatchWrapper((itemElement, checked) => {
      if (itemElement._item) {
        itemElement._item.checked = checked;
      }
    }),

    /**
     * @param {string} appId
     * @param {number} nodeId
     */
    constructItemsTree: tryCatchWrapper(function constructItemsTree(appId, nodeId) {
      const container = getContainer(appId, nodeId);
      if (!container) {
        return;
      }

      return Array.from(container.children).map(child => {
        const item = {
          component: child,
          checked: child._checked,
          theme: child.__theme,
        }
        if (
          child.localName == "vaadin-context-menu-item" &&
          child._containerNodeId
        ) {
          item.children = constructItemsTree(appId, child._containerNodeId);
        }
        child._item = item;
        return item;
      });
    })
  }
})();
