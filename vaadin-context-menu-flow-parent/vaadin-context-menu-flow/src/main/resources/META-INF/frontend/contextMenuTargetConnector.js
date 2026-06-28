import * as Gestures from '@vaadin/component-base/src/gestures.js';

function init(target) {
  if (target.$contextMenuTargetConnector) {
    return;
  }

  target.$contextMenuTargetConnector = {
    openOnHandler(e) {
      // used by Grid to prevent context menu on selection column click
      if (target.preventContextMenu && target.preventContextMenu(e)) {
        return;
      }
      e.preventDefault();
      e.stopPropagation();
      this.$contextMenuTargetConnector.openEvent = e;
      let detail = {};
      if (target.getContextMenuBeforeOpenDetail) {
        detail = target.getContextMenuBeforeOpenDetail(e);
      }
      target.dispatchEvent(
        new CustomEvent('vaadin-context-menu-before-open', {
          detail: detail
        })
      );
    },

    updateOpenOn(eventType) {
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

    removeListener() {
      if (this.openOnEventType) {
        if (Gestures.gestures[this.openOnEventType]) {
          Gestures.removeListener(target, this.openOnEventType, this.openOnHandler);
        } else {
          target.removeEventListener(this.openOnEventType, this.openOnHandler);
        }
      }
    },

    openMenu(contextMenu) {
      contextMenu.open(this.openEvent);
    },

    removeConnector() {
      this.removeListener();
      target.$contextMenuTargetConnector = undefined;
    }
  };
}

window.Vaadin.Flow.contextMenuTargetConnector = { init };
