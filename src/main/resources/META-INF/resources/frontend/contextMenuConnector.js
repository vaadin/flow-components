window.Vaadin.Flow.contextMenuConnector = {

  // NOTE: This is for the TARGET component, not for the <vaadin-context-menu> itself
  init: function(target) {
    if (target.$contextMenuConnector) {
      return;
    }

    target.$contextMenuConnector = {

      openOnHandler: function(e) {
        e.preventDefault();
        this.$contextMenuConnector.openEvent = e;
        target.dispatchEvent(new CustomEvent('vaadin-context-menu-before-open'));
      },

      updateOpenOn: function(eventType) {
        this.removeListener();
        this.openOnEventType = eventType;

        if (Polymer.Gestures.gestures[eventType]) {
          Polymer.Gestures.addListener(target, eventType, this.openOnHandler);
        } else {
          target.addEventListener(eventType, this.openOnHandler);
        }
      },

      removeListener: function() {
        if (this.openOnEventType) {
          if (Polymer.Gestures.gestures[this.openOnEventType]) {
            Polymer.Gestures.removeListener(target, this.openOnEventType, this.openOnHandler);
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
      const items = Array.from(container.children).map(child => {
        const item = {component: child, checked: child._checked};
        if (Vaadin.ItemElement && (child instanceof Vaadin.ItemElement) && child._containerNodeId) {
          item.children = getChildItems(child);
        }
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
