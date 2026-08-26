import './env-setup.js';
import '@vaadin/context-menu/src/vaadin-context-menu.js';
import '@vaadin/context-menu/src/vaadin-context-menu-item.js';
import '../frontend/generated/jar-resources/vaadin-context-menu/contextMenuConnector.ts';
import type {} from '@web/test-runner-mocha';
import type {
  FlowContextMenu,
  FlowContextMenuItem,
  FlowContextMenuItemComponent
} from '../frontend/generated/jar-resources/vaadin-context-menu/vaadin-context-menu-types.js';

export type { FlowContextMenu, FlowContextMenuItem, FlowContextMenuItemComponent };

export const APP_ID = 'test-app';

export const contextMenuConnector = window.Vaadin.Flow.contextMenuConnector;

const containers = new Map<number, Element>();
let nextNodeId = 1;

/**
 * Installs a Flow client that resolves the node ids handed out by
 * {@link createContainer}, and forgets any containers from a previous test.
 */
export function initFlowClient(): void {
  containers.clear();
  nextNodeId = 1;
  window.Vaadin.Flow.clients = {
    [APP_ID]: { getByNodeId: (nodeId: number) => containers.get(nodeId) }
  };
}

/**
 * Registers a container holding the given item elements, the way
 * `MenuItemsArrayGenerator` does for a menu and each of its sub menus, and
 * returns the node id to reach it by.
 */
export function createContainer(...items: Element[]): number {
  const container = document.createElement('div');
  items.forEach((item) => container.appendChild(item));
  const nodeId = nextNodeId++;
  containers.set(nodeId, container);
  return nodeId;
}

/** Creates a menu item element, optionally with some of the state Flow sets on it */
export function createItem(state: Partial<FlowContextMenuItemComponent> = {}): FlowContextMenuItemComponent {
  const item = document.createElement('vaadin-context-menu-item') as FlowContextMenuItemComponent;
  return Object.assign(item, state);
}

/**
 * Creates an element that is not a menu item, the way a separator or a
 * component added to a menu appears among the item elements.
 */
export function createComponent(): FlowContextMenuItemComponent {
  return document.createElement('div') as FlowContextMenuItemComponent;
}
