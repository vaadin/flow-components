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
  }
}
