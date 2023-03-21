import { aTimeout, expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, expandItems, setChildItems, setRootItems, getBodyCellText, getBodyCellContent } from './shared.js';
import sinon from 'sinon';
import type { FlowGrid } from './shared.js';
import type { GridColumn } from '@vaadin/grid';

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
    const rootItem = { key: '0', name: 'foo', children: true };
    setRootItems(grid.$connector, [rootItem]);
    expandItems(grid.$connector, [rootItem]);

    // Add an expanded child item
    const childItem = { key: '1', name: 'foo bar', children: true };
    expandItems(grid.$connector, [childItem]);
    setChildItems(grid.$connector, rootItem, [childItem]);

    // Add a grandchild item
    const grandchildItems = [{ key: '2', name: 'foo bar baz' }];
    setChildItems(grid.$connector, childItem, grandchildItems);

    await nextFrame();
    await nextFrame();

    expect(getBodyCellText(grid, 0, 0)).to.equal('foo');
    expect(getBodyCellText(grid, 1, 0)).to.equal('foo bar');
    expect(getBodyCellText(grid, 2, 0)).to.equal('foo bar baz');
  });

  it('should not update rows each time child items are set', async () => {
    // Add an expanded root item
    const rootItem = { key: '0', name: 'foo', children: true };
    setRootItems(grid.$connector, [rootItem]);
    expandItems(grid.$connector, [rootItem]);

    const spy = sinon.spy(grid, '__updateVisibleRows');

    // Add an expanded child item
    const childItem = { key: '1', name: 'foo bar', children: true };
    expandItems(grid.$connector, [childItem]);
    setChildItems(grid.$connector, rootItem, [childItem]);

    // Add a grandchild item
    const grandchildItems = [{ key: '2', name: 'foo bar baz' }];
    setChildItems(grid.$connector, childItem, grandchildItems);

    await nextFrame();
    await nextFrame();

    // This number may come down from further optimization
    expect(spy.callCount).to.equal(3);
  });

  it('should not compute column auto-width prematurely', async () => {
    // Make a column use auto-width
    const column = grid.querySelector<GridColumn>('vaadin-grid-column')!;
    column.autoWidth = true;
    column.flexGrow = 0;

    // Add a column that has the expected width because its root cell renders "foo bar".
    const column2 = document.createElement('vaadin-grid-column');
    column2.renderer = (root: HTMLElement) => {
      root.textContent = 'foo bar';
    };
    column2.autoWidth = true;
    column2.flexGrow = 0;
    grid.appendChild(column2);

    // Add an expanded root item
    const rootItem = { key: '0', name: 'foo', children: true };
    setRootItems(grid.$connector, [rootItem]);
    expandItems(grid.$connector, [rootItem]);

    // Add a child item
    const childItems = [{ key: '1', name: 'foo bar' }];
    setChildItems(grid.$connector, rootItem, childItems);

    await nextFrame();
    await nextFrame();

    // The column should have the expected width
    const expectedWidth = getBodyCellContent(grid, 0, 2)!.offsetWidth;
    const width = getBodyCellContent(grid, 0, 1)!.offsetWidth;
    expect(width).to.equal(expectedWidth);
  });
});
