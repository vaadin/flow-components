import { expect } from 'chai';
import {
  APP_ID,
  contextMenuConnector,
  createComponent,
  createContainer,
  createItem,
  initFlowClient
} from './shared.js';
import type { FlowContextMenu, FlowContextMenuItem, FlowContextMenuItemComponent } from './shared.js';

describe('context menu connector', () => {
  beforeEach(() => {
    initFlowClient();
  });

  describe('generateItemsTree', () => {
    it('should generate an item for each element in the container', () => {
      const nodeId = createContainer(createItem(), createItem());

      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;

      expect(items).to.have.lengthOf(2);
    });

    it('should reference the item element as the item component', () => {
      const item = createItem();
      const nodeId = createContainer(item);

      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;

      expect(items[0].component).to.equal(item);
    });

    it('should reference the generated item from the item element', () => {
      const item = createItem();
      const nodeId = createContainer(item);

      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;

      expect(item._item).to.equal(items[0]);
    });

    it('should return undefined for an unknown node id', () => {
      expect(contextMenuConnector.generateItemsTree(APP_ID, 404)).to.be.undefined;
    });
  });

  describe('item state', () => {
    let element: FlowContextMenuItemComponent;
    let item: FlowContextMenuItem;

    beforeEach(() => {
      element = createItem();
      const nodeId = createContainer(element);
      item = contextMenuConnector.generateItemsTree(APP_ID, nodeId)![0];
    });

    it('should read the checked state from the element', () => {
      element._checked = true;

      expect(item.checked).to.be.true;
    });

    it('should read the keep open state from the element', () => {
      element._keepOpen = true;

      expect(item.keepOpen).to.be.true;
    });

    it('should read the disabled state from the element', () => {
      element.disabled = true;

      expect(item.disabled).to.be.true;
    });

    it('should read the class name from the element', () => {
      element.className = 'custom';

      expect(item.className).to.equal('custom');
    });

    it('should read the theme from the element', () => {
      element.__theme = 'custom';

      expect(item.theme).to.equal('custom');
    });

    it('should read the tooltip from the element', () => {
      element.tooltip = 'Tooltip';

      expect(item.tooltip).to.equal('Tooltip');
    });

    it('should read the tooltip position from the element', () => {
      element.tooltipPosition = 'end';

      expect(item.tooltipPosition).to.equal('end');
    });
  });

  describe('sub menu', () => {
    it('should have no sub menu when the element has no container node id', () => {
      const nodeId = createContainer(createItem());

      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;

      expect(items[0].children).to.be.undefined;
    });

    it('should generate the sub menu from the container node id', () => {
      const subItem = createItem();
      const element = createItem({ _containerNodeId: createContainer(subItem) });
      const nodeId = createContainer(element);

      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;

      expect(items[0].children![0].component).to.equal(subItem);
    });

    it('should generate the sub menu for a container node id set after generating', () => {
      const element = createItem();
      const nodeId = createContainer(element);
      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;
      expect(items[0].children).to.be.undefined;

      // Flow only sends the container node id once the item is visible
      const subItem = createItem();
      element._containerNodeId = createContainer(subItem);

      expect(items[0].children![0].component).to.equal(subItem);
    });

    it('should generate the sub menu only once', () => {
      const element = createItem({ _containerNodeId: createContainer(createItem()) });
      const nodeId = createContainer(element);

      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;

      const children = items[0].children;
      expect(items[0].children).to.equal(children);
    });

    it('should have no sub menu for an element that is not a menu item', () => {
      const element = createComponent();
      element._containerNodeId = createContainer(createItem());
      const nodeId = createContainer(element);

      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;

      expect(items[0].children).to.be.undefined;
    });

    it('should read the state of a sub menu item from its element', () => {
      const subItem = createItem();
      const element = createItem({ _containerNodeId: createContainer(subItem) });
      const nodeId = createContainer(element);
      const items = contextMenuConnector.generateItemsTree(APP_ID, nodeId)!;

      subItem.tooltip = 'Tooltip';

      expect(items[0].children![0].tooltip).to.equal('Tooltip');
    });
  });

  describe('initLazy', () => {
    let contextMenu: FlowContextMenu;

    beforeEach(() => {
      contextMenu = document.createElement('vaadin-context-menu') as FlowContextMenu;
      document.body.appendChild(contextMenu);
      contextMenuConnector.initLazy(contextMenu, APP_ID);
    });

    afterEach(() => {
      contextMenu.remove();
    });

    it('should keep the connector of an already initialized menu', () => {
      const connector = contextMenu.$connector;

      contextMenuConnector.initLazy(contextMenu, APP_ID);

      expect(contextMenu.$connector).to.equal(connector);
    });

    it('should assign the generated items to the menu', () => {
      const element = createItem();
      const nodeId = createContainer(element);

      contextMenu.$connector.generateItems(nodeId);

      expect(contextMenu.items).to.have.lengthOf(1);
      expect(contextMenu.items![0].component).to.equal(element);
    });
  });

  describe('setChecked', () => {
    it('should toggle the checkmark attribute for a keep open item', () => {
      const element = createItem({ _keepOpen: true });
      contextMenuConnector.generateItemsTree(APP_ID, createContainer(element));

      contextMenuConnector.setChecked(element, true);

      expect(element.hasAttribute('menu-item-checked')).to.be.true;
    });

    it('should remove the checkmark attribute for a keep open item', () => {
      const element = createItem({ _keepOpen: true });
      contextMenuConnector.generateItemsTree(APP_ID, createContainer(element));
      contextMenuConnector.setChecked(element, true);

      contextMenuConnector.setChecked(element, false);

      expect(element.hasAttribute('menu-item-checked')).to.be.false;
    });

    it('should not toggle the checkmark attribute for an item that does not keep open', () => {
      const element = createItem();
      contextMenuConnector.generateItemsTree(APP_ID, createContainer(element));

      contextMenuConnector.setChecked(element, true);

      expect(element.hasAttribute('menu-item-checked')).to.be.false;
    });

    it('should not fail for an element without a generated item', () => {
      const element = createItem({ _keepOpen: true });

      contextMenuConnector.setChecked(element, true);

      expect(element.hasAttribute('menu-item-checked')).to.be.false;
    });
  });
});
