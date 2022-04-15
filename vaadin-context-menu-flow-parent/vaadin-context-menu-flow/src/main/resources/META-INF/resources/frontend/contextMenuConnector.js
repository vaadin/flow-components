(function() {
  const tryCatchWrapper = function(callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Context Menu');
  };

  window.Vaadin.Flow.contextMenuConnector = {
    generateItems: (menu, appId, nodeId) =>
      tryCatchWrapper(function(menu, appId, nodeId) {
        menu._containerNodeId = nodeId;

        const getContainer = function(nodeId) {
          try {
            return window.Vaadin.Flow.clients[appId].getByNodeId(nodeId);
          } catch (error) {
            console.error("Could not get node %s from app %s", nodeId, appId);
            console.error(error);
          }
        };

        const getChildItems = function(parent) {
          const container = getContainer(parent._containerNodeId);
          const items =
            container &&
            Array.from(container.children).map(child => {
              const item = {
                  component: child,
                  checked: child._checked,
                  theme: child.__theme
              };
              if (
                child.tagName == "VAADIN-CONTEXT-MENU-ITEM" &&
                child._containerNodeId
              ) {
                item.children = getChildItems(child);
              }
              child._item = item;
              return item;
            });
          return items;
        };

        const items = getChildItems(menu);
        menu.items = items;
      })(menu, appId, nodeId),

    setChecked: (component, checked) =>
      tryCatchWrapper(function(component, checked) {
        if (component._item) {
          component._item.checked = checked;
        }
      })(component, checked),

    setTheme: (component, theme) =>
        tryCatchWrapper((component, theme) => {
            if (component._item) {
                component._item.theme = theme;
            }
        })(component, theme)
  };
})();
