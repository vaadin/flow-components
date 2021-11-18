// Not using ES6 imports in this file yet because the connector in V14 must
// still work in Legacy bower projects. See: `contextMenuConnector-es6.js` for
// the Polymer3 approach.
(function () {
  const tryCatchWrapper = function (callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Context Menu', 'vaadin-context-menu-flow');
  };

  window.Vaadin.Flow.Legacy = window.Vaadin.Flow.Legacy || {};

  window.Vaadin.Flow.contextMenuConnector = {

    // NOTE: This is for the TARGET component, not for the <vaadin-context-menu> itself
    init: target => tryCatchWrapper(function(target) {
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

        openOnHandler: tryCatchWrapper(function(e) {
          e.preventDefault();
          e.stopPropagation();
          this.$contextMenuConnector.openEvent = e;
          let detail = {};
          if (target.getContextMenuBeforeOpenDetail) {
            detail = target.getContextMenuBeforeOpenDetail(e);
          }
          target.dispatchEvent(new CustomEvent('vaadin-context-menu-before-open', {detail: detail}));
        }),

        updateOpenOn: tryCatchWrapper(function(eventType) {
          this.removeListener();
          this.openOnEventType = eventType;

          customElements.whenDefined('vaadin-context-menu').then(tryCatchWrapper(() => {
            if (Gestures.gestures[eventType]) {
              Gestures.addListener(target, eventType, this.openOnHandler);
            } else {
              target.addEventListener(eventType, this.openOnHandler);
            }
          }));
        }),

        removeListener: tryCatchWrapper(function() {
          if (this.openOnEventType) {
            if (Gestures.gestures[this.openOnEventType]) {
              Gestures.removeListener(target, this.openOnEventType, this.openOnHandler);
            } else {
              target.removeEventListener(this.openOnEventType, this.openOnHandler);
            }
          }
        }),

        openMenu: tryCatchWrapper(function(contextMenu) {
          contextMenu.open(this.openEvent);
        }),

        removeConnector: tryCatchWrapper(function() {
          this.removeListener();
          target.$contextMenuConnector = undefined;
        })

      };
    })(target),

    generateItems: (menu, appId, nodeId) => tryCatchWrapper(function(menu, appId, nodeId) {
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
          const item = {
              component: child,
              checked: child._checked,
              theme: child._theme
          };
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
    })(menu, appId, nodeId),

    setChecked: (component, checked) => tryCatchWrapper(function(component, checked) {
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
