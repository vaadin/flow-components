import './env-setup.js';
import '@vaadin/menu-bar/src/vaadin-menu-bar.js';
import '@vaadin/menu-bar/src/vaadin-menu-bar-item.js';
import '../frontend/generated/jar-resources/vaadin-menu-bar/menubarConnector.ts';
import type {} from '@web/test-runner-mocha';
import type {
  FlowMenuBar,
  FlowMenuBarItemComponent
} from '../frontend/generated/jar-resources/vaadin-menu-bar/vaadin-menu-bar-types.js';

export type { FlowMenuBar, FlowMenuBarItemComponent };

const APP_ID = 'test-app';

const menubarConnector = window.Vaadin.Flow.menubarConnector;

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
export function createItem(text: string, state: Partial<FlowMenuBarItemComponent> = {}): FlowMenuBarItemComponent {
  const item = document.createElement('vaadin-menu-bar-item') as FlowMenuBarItemComponent;
  item.textContent = text;
  return Object.assign(item, state);
}

/** Initializes the connector for the menu bar, the way the Flow component does */
export function init(menuBar: FlowMenuBar): void {
  menubarConnector.initLazy(menuBar, APP_ID);
}

/** The text of the buttons the menu bar renders for its items */
export function getButtonTexts(menuBar: FlowMenuBar): string[] {
  return [...menuBar.querySelectorAll('vaadin-menu-bar-button')]
    .filter((button) => !button.hasAttribute('slot'))
    .map((button) => button.textContent!.trim());
}
