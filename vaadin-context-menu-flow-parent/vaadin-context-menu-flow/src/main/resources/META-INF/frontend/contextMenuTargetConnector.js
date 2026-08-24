import * as Gestures from '@vaadin/component-base/src/gestures.js';

/**
 * contextMenuTargetConnector listens for the event that opens a context menu on
 * the menu's target element, and notifies the server so that it can open the
 * menu after the items have been generated.
 */
class ContextMenuTargetConnector {
  #target;
  #openOnEventType;
  #openEvent;

  // Registered as an event listener, so it has to stay the same function object
  // for the listener to be removable, and it has to be bound to the connector
  // rather than to the target the event is dispatched on.
  #openOnHandler = (e) => {
    const target = this.#target;

    // used by Grid to prevent context menu on selection column click
    if (target.preventContextMenu && target.preventContextMenu(e)) {
      return;
    }
    e.preventDefault();
    e.stopPropagation();
    // The menu is opened later, after a server round-trip, when the event has
    // finished dispatching and `composedPath()` returns an empty array. Capture
    // the composed path now so the menu can resolve the target inside a shadow
    // root (e.g. a grid cell) instead of the retargeted host.
    e.__composedPath = e.composedPath();
    this.#openEvent = e;

    let detail = {};
    if (target.getContextMenuBeforeOpenDetail) {
      detail = target.getContextMenuBeforeOpenDetail(e);
    }
    target.dispatchEvent(
      new CustomEvent('vaadin-context-menu-before-open', {
        detail: detail
      })
    );
  };

  constructor(target) {
    this.#target = target;
  }

  updateOpenOn(eventType) {
    this.#removeListener();
    this.#openOnEventType = eventType;

    customElements.whenDefined('vaadin-context-menu').then(() => {
      if (this.#target.$contextMenuTargetConnector !== this) {
        // The connector was removed while waiting, so the listener would be
        // left behind on the target with no way to remove it
        return;
      }

      if (Gestures.gestures[eventType]) {
        Gestures.addListener(this.#target, eventType, this.#openOnHandler);
      } else {
        this.#target.addEventListener(eventType, this.#openOnHandler);
      }
    });
  }

  openMenu(contextMenu) {
    contextMenu.open(this.#openEvent);
  }

  removeConnector() {
    this.#removeListener();
    this.#target.$contextMenuTargetConnector = undefined;
  }

  #removeListener() {
    if (this.#openOnEventType) {
      if (Gestures.gestures[this.#openOnEventType]) {
        Gestures.removeListener(this.#target, this.#openOnEventType, this.#openOnHandler);
      } else {
        this.#target.removeEventListener(this.#openOnEventType, this.#openOnHandler);
      }
    }
  }
}

function init(target) {
  // Init the connector only once for the target
  target.$contextMenuTargetConnector ??= new ContextMenuTargetConnector(target);
}

window.Vaadin.Flow.contextMenuTargetConnector = { init };
