import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, expandItems, setChildItems, setRootItems, getBodyCellText } from './shared.js';
import sinon from 'sinon';
import type { FlowGrid } from './shared.js';

describe('grid connector - tree', () => {
  let grid: FlowGrid;

  beforeEach(() => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-tree-column path="name"></vaadin-grid-tree-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
  });

  it('should render the tree structure', async () => {
    // Add an expanded root item
    const rootItems = [{ key: '0', name: 'foo', children: true }];
    setRootItems(grid.$connector, rootItems);
    expandItems(grid.$connector, rootItems);
    
    // Add an expanded child item
    const childItems = [{ key: '1', name: 'foo bar', children: true }];
    expandItems(grid.$connector, childItems);
    setChildItems(grid.$connector, '0', childItems);

    // Add a grandchild item
    const grandchildItems = [{ key: '2', name: 'foo bar baz' }];
    setChildItems(grid.$connector, '1', grandchildItems);

    await nextFrame();
    await nextFrame();

    expect(getBodyCellText(grid, 0, 0)).to.equal('foo');
    expect(getBodyCellText(grid, 1, 0)).to.equal('foo bar');
    expect(getBodyCellText(grid, 2, 0)).to.equal('foo bar baz');
  });


  it('should not update rows each time child items are set', async () => {
    // Add an expanded root item
    const rootItems = [{ key: '0', name: 'foo', children: true }];
    setRootItems(grid.$connector, rootItems);
    expandItems(grid.$connector, rootItems);
    
    const spy = sinon.spy(grid, '__updateVisibleRows');

    // Add an expanded child item
    const childItems = [{ key: '1', name: 'foo bar', children: true }];
    expandItems(grid.$connector, childItems);
    setChildItems(grid.$connector, '0', childItems);

    // Add a grandchild item
    const grandchildItems = [{ key: '2', name: 'foo bar baz' }];
    setChildItems(grid.$connector, '1', grandchildItems);

    await nextFrame();
    await nextFrame();
    
    // This number may come down from further optimization
    expect(spy.callCount).to.equal(5);
  });
});
