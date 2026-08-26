import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { createContainer, createItem, getButtonTexts, init, initFlowClient } from './shared.js';
import type { FlowMenuBar, FlowMenuBarItemComponent } from './shared.js';

describe('menu bar connector', () => {
  let menuBar: FlowMenuBar;

  beforeEach(async () => {
    initFlowClient();
    menuBar = fixtureSync('<vaadin-menu-bar></vaadin-menu-bar>') as FlowMenuBar;
    await nextFrame();
    init(menuBar);
  });

  it('should keep the connector of an already initialized menu bar', () => {
    const connector = menuBar.$connector;

    init(menuBar);

    expect(menuBar.$connector).to.equal(connector);
  });

  it('should assign the generated items to the menu bar', () => {
    const nodeId = createContainer(createItem('Item 1'), createItem('Item 2'));

    menuBar.$connector.generateItems(nodeId);

    expect(menuBar.items).to.have.lengthOf(2);
  });

  it('should render a button for each item', async () => {
    const nodeId = createContainer(createItem('Item 1'), createItem('Item 2'));

    menuBar.$connector.generateItems(nodeId);
    await nextFrame();

    expect(getButtonTexts(menuBar)).to.eql(['Item 1', 'Item 2']);
  });

  describe('hidden items', () => {
    let hiddenItem: FlowMenuBarItemComponent;

    beforeEach(async () => {
      hiddenItem = createItem('Item 2', { hidden: true });
      const nodeId = createContainer(createItem('Item 1'), hiddenItem);
      menuBar.$connector.generateItems(nodeId);
      await nextFrame();
    });

    it('should leave a hidden item out of the items', () => {
      expect(menuBar.items).to.have.lengthOf(1);
      expect(getButtonTexts(menuBar)).to.eql(['Item 1']);
    });

    it('should include a shown item without generating the items again', async () => {
      hiddenItem.hidden = false;

      menuBar.$connector.generateItems();
      await nextFrame();

      expect(getButtonTexts(menuBar)).to.eql(['Item 1', 'Item 2']);
    });

    it('should include a shown item when Flow removes the hidden attribute', async () => {
      hiddenItem.removeAttribute('hidden');
      await nextFrame();

      expect(getButtonTexts(menuBar)).to.eql(['Item 1', 'Item 2']);
    });

    it('should leave out an item that Flow hides', async () => {
      const item = menuBar.querySelector('vaadin-menu-bar-item')!;

      item.toggleAttribute('hidden', true);
      await nextFrame();

      expect(getButtonTexts(menuBar)).to.eql([]);
    });
  });

  describe('sub menu', () => {
    it('should give a button with a sub menu the popup role', async () => {
      const item = createItem('Item', { _containerNodeId: createContainer(createItem('Sub item')) });
      menuBar.$connector.generateItems(createContainer(item));
      await nextFrame();

      const button = menuBar.querySelector('vaadin-menu-bar-button')!;
      expect(button.getAttribute('aria-haspopup')).to.equal('true');
    });

    it('should give a button a sub menu for a container node id set after generating', async () => {
      const item = createItem('Item');
      menuBar.$connector.generateItems(createContainer(item));
      await nextFrame();
      expect(menuBar.querySelector('vaadin-menu-bar-button')!.getAttribute('aria-haspopup')).to.be.null;

      // Flow only sends the container node id once the item is visible
      item._containerNodeId = createContainer(createItem('Sub item'));
      menuBar.$connector.generateItems();
      await nextFrame();

      const button = menuBar.querySelector('vaadin-menu-bar-button')!;
      expect(button.getAttribute('aria-haspopup')).to.equal('true');
    });
  });

  describe('disabled items', () => {
    it('should disable the button of a disabled item', async () => {
      const item = createItem('Item', { disabled: true });
      menuBar.$connector.generateItems(createContainer(item));
      await nextFrame();

      const button = menuBar.querySelector('vaadin-menu-bar-button')!;
      expect(button.hasAttribute('disabled')).to.be.true;
    });

    it('should disable the button when Flow disables the item', async () => {
      const item = createItem('Item');
      menuBar.$connector.generateItems(createContainer(item));
      await nextFrame();

      item.toggleAttribute('disabled', true);
      await nextFrame();

      const button = menuBar.querySelector('vaadin-menu-bar-button')!;
      expect(button.hasAttribute('disabled')).to.be.true;
    });
  });
});
