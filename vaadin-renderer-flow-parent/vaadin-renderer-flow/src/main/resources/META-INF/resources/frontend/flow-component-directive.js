import { noChange } from 'lit';
import { directive, PartType } from 'lit/directive.js';
import { AsyncDirective } from 'lit/async-directive.js';

class FlowComponentDirective extends AsyncDirective {
  constructor(partInfo) {
    super(partInfo);
    if (partInfo.type !== PartType.CHILD) {
      throw new Error(`${this.constructor.directiveName}() can only be used in child bindings`);
    }
  }

  update(part, [appid, nodeid]) {
    this.updateContent(part, appid, nodeid);
    return noChange;
  }

  updateContent(part, appid, nodeid) {
    const { parentNode, startNode } = part;

    const hasNewNodeId = nodeid !== undefined && nodeid !== null;
    const newNode = hasNewNodeId ? this.getNewNode(appid, nodeid) : null;
    const oldNode = this.getOldNode(part);

    if (hasNewNodeId && !newNode) {
      // If the node is not found, try again later twice.
      this.__handleRetry(part, appid, nodeid);
    } else if (oldNode === newNode) {
      return;
    } else if (oldNode && newNode) {
      parentNode.replaceChild(newNode, oldNode);
    } else if (oldNode) {
      parentNode.removeChild(oldNode);
    } else if (newNode) {
      startNode.after(newNode);
    }
  }

  getNewNode(appid, nodeid) {
    return window.Vaadin.Flow.clients[appid].getByNodeId(nodeid);
  }

  getOldNode(part) {
    const { startNode, endNode } = part;
    if (startNode.nextSibling === endNode) {
      return;
    }
    return startNode.nextSibling;
  }

  disconnected() {
    this.__onRetry = undefined;
  }

  __handleRetry(part, appid, nodeid) {
    // Retry limit achieved
    if (this.__onRetry === 2) {
      this.__onRetry = undefined;
      return;
    }
    const doRetry = () => queueMicrotask(() => this.updateContent(part, appid, nodeid));
    // On second retry
    if (this.__onRetry === 1) {
      this.__onRetry = 2;
      function retry() {
        doRetry();
        window.removeEventListener('animationend', retry);
      }
      window.addEventListener('animationend', retry);
      return;
    }
    // On first retry
    this.__onRetry = 1;
    doRetry();
  }
}

/**
 * Renders the given flow component node.
 *
 * WARNING: This directive is not intended for public use.
 *
 * @param {string} appid
 * @param {number} nodeid
 * @private
 */
export const flowComponentDirective = directive(FlowComponentDirective);
