function getContainer(appId, nodeId) {
  try {
    return window.Vaadin.Flow.clients[appId].getByNodeId(nodeId);
  } catch (error) {
    console.error('Could not get node %s from app %s', nodeId, appId);
    console.error(error);
  }
}

/**
 * Initializes the connector for a context menu element.
 *
 * @param {HTMLElement} contextMenu
 * @param {string} appId
 */
function initLazy(contextMenu, appId) {
  if (contextMenu.$connector) {
    return;
  }

  contextMenu.$connector = {
    /**
     * Generates and assigns the items to the context menu.
     *
     * @param {number} nodeId
     */
    generateItems(nodeId) {
      const items = generateItemsTree(appId, nodeId);

      contextMenu.items = items;
    }
  };
}

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
function generateItemsTree(appId, nodeId) {
  const container = getContainer(appId, nodeId);
  if (!container) {
    return;
  }

  return Array.from(container.children).map((child) => {
    // Use getters to provide up to date values for the web component when
    // the menu is rendered or tooltip is shown without regenerating items.
    const item = {
      component: child,
      get checked() {
        return child._checked;
      },
      get keepOpen() {
        return child._keepOpen;
      },
      get disabled() {
        return child.disabled;
      },
      get className() {
        return child.className;
      },
      get theme() {
        return child.__theme;
      },
      get tooltip() {
        return child.tooltip;
      },
      get tooltipPosition() {
        return child.tooltipPosition;
      }
    };
    // Do not hardcode tag name to allow `vaadin-menu-bar-item`
    if (child._hasVaadinItemMixin) {
      // Flow does not send the container node id for the invisible item, so
      // reading it while generating items would leave item without sub menu.
      // Result is cached as web component reads `children` on every render.
      let children;
      Object.defineProperty(item, 'children', {
        enumerable: true,
        get() {
          if (!children && child._containerNodeId) {
            children = generateItemsTree(appId, child._containerNodeId);
          }
          return children;
        }
      });
    }
    child._item = item;
    return item;
  });
}

/**
 * Toggles the checkmark attribute for a keep-open context menu item.
 *
 * The items array reflects the new checked state through a getter, but a menu
 * that stays open after a click does not re-render its items, so the attribute
 * is toggled directly to show the checkmark immediately.
 *
 * @param {HTMLElement} component
 * @param {boolean} checked
 */
function setChecked(component, checked) {
  if (component._item && component._item.keepOpen) {
    component.toggleAttribute('menu-item-checked', checked);
  }
}

window.Vaadin.Flow.contextMenuConnector = {
  initLazy,
  generateItemsTree,
  setChecked
};
