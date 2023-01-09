import '@polymer/polymer/lib/elements/dom-if.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';
import { Debouncer } from '@polymer/polymer/lib/utils/debounce.js';
import { idlePeriod } from '@polymer/polymer/lib/utils/async.js';
import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import { until } from 'lit/directives/until.js';

/**
 * Returns the requested node from the Flow client.
 * @param {string} appid 
 * @param {number} nodeid 
 * @returns {Element | null} The element if found, null otherwise.
 */
function getNodeInternal(appid, nodeid) {
  return window.Vaadin.Flow.clients[appid].getByNodeId(nodeid);
}

/**
 * Returns the requested node in a form suitable for Lit template interpolation.
 * @param {string} appid 
 * @param {number} nodeid 
 * @returns {any} The element if found, null otherwise.
 */
function getNode(appid, nodeid) {
  // Theoretically, this method could just return the node as-is.
  // The `until` directive is used for now to work around sizing issues
  // with ComponentRenderer. The previously used <flow-component-renderer> was
  // asynchronous by nature and thus worked out of the box.
  //
  // Test in ComponentColumnWithHeightIT::shouldPositionItemsCorrectlyAfterScrollingToEnd
  // makes sure the sizing works correctly. The sizing issue should eventually
  // be fixed in the Virtualizer.
  return until(new Promise((resolve) => resolve(getNodeInternal(appid, nodeid))));
}

/**
 * Sets the nodes defined by the given node ids as the child nodes of the
 * given root element.
 * @param {string} appid 
 * @param {number[]} nodeIds
 * @param {Element} root 
 */
function setChildNodes(appid, nodeIds, root) {
  root.textContent = '';
  root.append(...nodeIds.map(id => getNodeInternal(appid, id)));
}

/**
 * SimpleElementBindingStrategy::addChildren uses insertBefore to add child
 * elements to the container. When the children are manually placed under
 * another element, the call to insertBefore can occasionally fail due to
 * an invalid reference node.
 * 
 * This is a temporary workaround which patches the container's native API
 * to not fail when called with invalid arguments.
 */
function patchVirtualContainer(container) {
  const originalInsertBefore = container.insertBefore;
  
  container.insertBefore = function (newNode, referenceNode) {
    if (referenceNode && referenceNode.parentNode === this) {
      return originalInsertBefore.call(this, newNode, referenceNode);
    } else {
      return originalInsertBefore.call(this, newNode, null);
    }
  };
}

window.Vaadin ||= {};
window.Vaadin.FlowComponentHost ||= { patchVirtualContainer, getNode, setChildNodes };

class FlowComponentRenderer extends PolymerElement {
  static get template() {
    return html`
      <style>
        :host {
          animation: 1ms flow-component-renderer-appear;
        }

        @keyframes flow-component-renderer-appear {
          to {
            opacity: 1;
          }
        }
      </style>
      <slot></slot>
    `;
  }

  static get is() {
    return 'flow-component-renderer';
  }
  static get properties() {
    return {
      nodeid: Number,
      appid: String,
    };
  }
  static get observers() {
    return ['_attachRenderedComponentIfAble(appid, nodeid)'];
  }

  ready() {
    super.ready();
    this.addEventListener('click', function (event) {
      if (
        this.firstChild &&
        typeof this.firstChild.click === 'function' &&
        event.target === this
      ) {
        event.stopPropagation();
        this.firstChild.click();
      }
    });
    this.addEventListener('animationend', this._onAnimationEnd);
  }

  _asyncAttachRenderedComponentIfAble() {
    this._debouncer = Debouncer.debounce(this._debouncer, idlePeriod, () =>
      this._attachRenderedComponentIfAble()
    );
  }

  _attachRenderedComponentIfAble() {
    if (!this.nodeid || !this.appid) {
      return;
    }
    const renderedComponent = this._getRenderedComponent();
    if (this.firstChild) {
      if (!renderedComponent) {
        this._asyncAttachRenderedComponentIfAble();
      } else if (this.firstChild !== renderedComponent) {
        this.replaceChild(renderedComponent, this.firstChild);
        this._defineFocusTarget();
        this.onComponentRendered();
      } else {
        this._defineFocusTarget();
        this.onComponentRendered();
      }
    } else {
      if (renderedComponent) {
        this.appendChild(renderedComponent);
        this._defineFocusTarget();
        this.onComponentRendered();
      } else {
        this._asyncAttachRenderedComponentIfAble();
      }
    }
  }

  _getRenderedComponent() {
    try {
      return window.Vaadin.Flow.clients[this.appid].getByNodeId(this.nodeid);
    } catch (error) {
      console.error(
        'Could not get node %s from app %s',
        this.nodeid,
        this.appid
      );
      console.error(error);
    }
    return null;
  }

  onComponentRendered() {
    // subclasses can override this method to execute custom logic on resize
  }

  /* Setting the `focus-target` attribute to the first focusable descendant
  starting from the firstChild necessary for the focus to be delegated
  within the flow-component-renderer when used inside a vaadin-grid cell  */
  _defineFocusTarget() {
    var focusable = this._getFirstFocusableDescendant(this.firstChild);
    if (focusable !== null) {
      focusable.setAttribute('focus-target', 'true');
    }
  }

  _getFirstFocusableDescendant(node) {
    if (this._isFocusable(node)) {
      return node;
    }
    if (node.hasAttribute && (node.hasAttribute('disabled') || node.hasAttribute('hidden'))) {
      return null;
    }
    if (!node.children) {
      return null;
    }
    for (var i = 0; i < node.children.length; i++) {
      var focusable = this._getFirstFocusableDescendant(node.children[i]);
      if (focusable !== null) {
        return focusable;
      }
    }
    return null;
  }

  _isFocusable(node) {
    if (
      node.hasAttribute &&
      typeof node.hasAttribute === 'function' &&
      (node.hasAttribute('disabled') || node.hasAttribute('hidden'))
    ) {
      return false;
    }

    return node.tabIndex === 0;
  }

  _onAnimationEnd(e) {
    // ShadyCSS applies scoping suffixes to animation names
    // To ensure that child is attached once element is unhidden
    // for when it was filtered out from, eg, ComboBox
    // https://github.com/vaadin/vaadin-flow-components/issues/437
    if (e.animationName.indexOf('flow-component-renderer-appear') === 0) {
      this._attachRenderedComponentIfAble();
    }
  }
}
window.customElements.define(FlowComponentRenderer.is, FlowComponentRenderer);
