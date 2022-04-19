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
    /**
     * Initializes the connector for a context menu element.
     *
     * @param {HTMLElement} contextMenu
     * @param {string} appId
     */
    initLazy: tryCatchWrapper((contextMenu, appId) => {
      if (contextMenu.$connector) {
        return;
      }

      contextMenu.$connector = {
        /**
         * Generates and assigns the items to the context menu.
         *
         * @param {number} nodeId
         */
        generateItems: tryCatchWrapper((nodeId) => {
          const items = window.Vaadin.Flow.contextMenuConnector.generateItemsTree(
            appId,
            nodeId
          );

          contextMenu.items = items;
        })
      }
    }),

    /**
     * Generates an items tree compatible with the context-menu web component
     * by traversing the given Flow DOM tree of context menu item nodes
     * whose root node is identified by the `nodeId` argument.
     *
     * The app id is required to access the store of Flow DOM nodes.
     *
     * @param {string} appId
     * @param {number} nodeId
     */
    generateItemsTree: tryCatchWrapper(function generateItemsTree(appId, nodeId) {
      const container = getContainer(appId, nodeId);
      if (!container) {
        return;
      }

      return Array.from(container.children).map(child => {
        const item = {
          component: child,
          checked: child._checked,
          theme: child._theme,
        }
        if (
          child.localName == "vaadin-context-menu-item" &&
          child._containerNodeId
        ) {
          item.children = generateItemsTree(appId, child._containerNodeId);
        }
        child._item = item;
        return item;
      });
    }),

    /**
     * Sets the checked state for a context menu item.
     *
     * This method is supposed to be called when the context menu item is closed,
     * so there is no need for triggering a re-render eagarly.
     *
     * @param {HTMLElement} component
     * @param {boolean} checked
     */
    setChecked: tryCatchWrapper((component, checked) => {
      if (component._item) {
        component._item.checked = checked;
      }
    }),

    /**
     * Sets the theme for a context menu item.
     *
     * This method is supposed to be called when the context menu item is closed,
     * so there is no need for triggering a re-render eagarly.
     *
     * @param {HTMLElement} component
     * @param {string | undefined | null} theme
     */
    setTheme: tryCatchWrapper((component, theme) => {
      if (component._item) {
        component._item.theme = theme;
      }
    })
  };
})();
