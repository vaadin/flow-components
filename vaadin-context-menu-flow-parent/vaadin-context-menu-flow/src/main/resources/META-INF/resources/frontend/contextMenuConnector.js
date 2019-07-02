// Not using ES6 imports in this file yet because the connector in V14 must
// still work in Legacy bower projects. See: `contextMenuConnector-es6.js` for
// the Polymer3 approach.
window.Vaadin.Flow.Legacy = window.Vaadin.Flow.Legacy || {};

window.Vaadin.Flow.contextMenuConnector = {

  // NOTE: This is for the TARGET component, not for the <vaadin-context-menu> itself
  init: function(target) {
    if (target.$contextMenuConnector) {
      return;
    }

    if (window.Polymer) {
        // Polymer2 approach.
        window.Vaadin.Flow.Legacy.GestureEventListeners = window.Vaadin.Flow.Legacy.GestureEventListeners || Polymer.GestureEventListeners;
        window.Vaadin.Flow.Legacy.Gestures = window.Vaadin.Flow.Legacy.Gestures || Polymer.Gestures;
    } else if (!window.Vaadin.Flow.Legacy.Gestures) {
      console.log("ContextMenu is unable to load Polymer helpers.");
      return;
    }

    const GestureEventListeners = window.Vaadin.Flow.Legacy.GestureEventListeners;
    const Gestures = window.Vaadin.Flow.Legacy.Gestures;

    target.$contextMenuConnector = {

      openOnHandler: function(e) {
        e.preventDefault();
        e.stopPropagation();
        this.$contextMenuConnector.openEvent = e;
        target.dispatchEvent(new CustomEvent('vaadin-context-menu-before-open'));
      },

      updateOpenOn: function(eventType) {
        this.removeListener();
        this.openOnEventType = eventType;

        customElements.whenDefined('vaadin-context-menu').then(() => {
          if (Gestures.gestures[eventType]) {
            Gestures.addListener(target, eventType, this.openOnHandler);
          } else {
            target.addEventListener(eventType, this.openOnHandler);
          }
        });
      },

      removeListener: function() {
        if (this.openOnEventType) {
          if (Gestures.gestures[this.openOnEventType]) {
            Gestures.removeListener(target, this.openOnEventType, this.openOnHandler);
          } else {
            target.removeEventListener(this.openOnEventType, this.openOnHandler);
          }
        }
      },

      openMenu: function(contextMenu) {
        contextMenu.open(this.openEvent);
      },

      removeConnector: function() {
        this.removeListener();
        target.$contextMenuConnector = undefined;
      }

    };
  },

  generateItems: function(menu, appId, nodeId) {
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
      const items = container && Array.from(container.children).map(child => {
        const item = {component: child, checked: child._checked};
        if (child.tagName == "VAADIN-CONTEXT-MENU-ITEM" && child._containerNodeId) {
          item.children = getChildItems(child);
        }
        child._item = item;
        return item;
      });
      return items;
    };

    const items = getChildItems(menu);
    menu.items = items;
  },

  setChecked: function(component, checked) {
    if (component._item) {
      component._item.checked = checked;
    }
  }
}
