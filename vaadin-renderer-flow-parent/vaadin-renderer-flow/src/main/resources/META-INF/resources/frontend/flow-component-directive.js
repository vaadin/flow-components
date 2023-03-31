import { noChange } from 'lit';
import { directive, Directive, PartType } from 'lit/directive.js';

class FlowComponentDirective extends Directive {
  constructor(partInfo) {
    super(partInfo);
    if (partInfo.type !== PartType.CHILD) {
      throw new Error(`${this.constructor.directiveName}() can only be used in child bindings`);
    }
  }

  render() {
    return noChange;
  }

  update(part, [newNode]) {
    const { parentNode } = part;
    const oldNode = [...parentNode.childNodes].find((node) => {
      return [Node.ELEMENT_NODE, Node.TEXT_NODE].includes(node.nodeType);
    });

    if (oldNode === newNode) {
      return noChange;
    } else if (oldNode && newNode) {
      parentNode.replaceChild(newNode, oldNode);
    } else if (oldNode) {
      parentNode.removeChild(oldNode);
    } else if (newNode) {
      parentNode.appendChild(newNode);
    }
  }
}

export const flowComponentDirective = directive(FlowComponentDirective);
