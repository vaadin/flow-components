import { aTimeout, expect, fixtureSync, nextFrame } from '@open-wc/testing';
import {
  init,
  expandItems,
  setChildItems,
  setRootItems,
  getBodyCellText,
  getBodyCellContent,
  GRID_CONNECTOR_PARENT_REQUEST_DELAY,
  getHeaderCellContent,
  collapseItems
} from './shared.js';
import sinon from 'sinon';
import type { FlowGrid } from './shared.js';
import type { GridColumn } from '@vaadin/grid';

function isGridInLoadingState(grid: FlowGrid) {
  return (grid as any)._cache.isLoading();
}

describe('grid connector - tree', () => {
  let grid: FlowGrid;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-tree-column path="name"></vaadin-grid-tree-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
    await nextFrame();
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

    // The column should have the expected width
    const expectedWidth = getBodyCellContent(grid, 0, 2)!.offsetWidth;
    const width = getBodyCellContent(grid, 0, 1)!.offsetWidth;
    expect(width).to.equal(expectedWidth);
  });

  it('should request server for child items', async () => {
    // Add an expanded root item
    const rootItem = { key: '0', name: 'foo', children: true };
    setRootItems(grid.$connector, [rootItem]);
    expandItems(grid.$connector, [rootItem]);
    await nextFrame();

    // Wait for the request for child items to be sent
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expect(grid.$server.setParentRequestedRanges).to.be.calledOnce;
    expect(grid.$server.setParentRequestedRanges.firstCall.firstArg).to.deep.equal([
      {
        parentKey: rootItem.key,
        firstIndex: 0,
        size: grid.pageSize
      }
    ]);
  });

  it('should render child nodes lazily', async () => {
    // Add an expanded root item
    const rootItem = { key: '0', name: 'foo', children: true };
    setRootItems(grid.$connector, [rootItem]);
    expandItems(grid.$connector, [rootItem]);
    await nextFrame();

    // Add a child item
    const childItem = { key: '1', name: 'foo bar', children: true };
    setChildItems(grid.$connector, rootItem, [childItem]);

    await nextFrame();

    expect(getBodyCellText(grid, 0, 0)).to.equal('foo');
    expect(getBodyCellText(grid, 1, 0)).to.equal('foo bar');
  });

  it('should not compute column auto-width while fetching child items', async () => {
    // Make a column use auto-width
    const column = grid.querySelector<GridColumn>('vaadin-grid-column')!;
    column.autoWidth = true;
    column.flexGrow = 0;

    // Get the initial width of the column before any items are added
    const headerCellContent = getHeaderCellContent(column);
    const initialWidth = headerCellContent.offsetWidth;

    // Add an expanded root item with long content
    const rootItem = { key: '0', name: 'foo bar baz qux', children: true };
    expandItems(grid.$connector, [rootItem]);
    setRootItems(grid.$connector, [rootItem]);

    await nextFrame();

    // Wait for the request for child items to be sent
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);

    // Explicilty request a column auto-width recalculation
    grid.recalculateColumnWidths();

    // Expect the column to still have the same width as initially (not recaulcated)
    expect(headerCellContent.offsetWidth).to.equal(initialWidth);
  });

  it('should render the preloaded child synchronously', async () => {
    // Create an expanded root item
    const rootItem = { key: '0', name: 'foo', children: true };
    expandItems(grid.$connector, [rootItem]);

    // Add a child item
    const childItem = { key: '1', name: 'foo bar', children: false };
    setChildItems(grid.$connector, rootItem, [childItem]);

    // Add the expanded root item
    setRootItems(grid.$connector, [rootItem]);

    expect(getBodyCellText(grid, 1, 0)).to.equal('foo bar');
  });

  it('should not request items for expanded parent outside the DOM', async () => {
    // Add expanded root items
    const rootItem = { key: '0', name: 'foo', children: true };
    const rootItem2 = { key: '1', name: 'bat', children: true };
    setRootItems(grid.$connector, [rootItem, rootItem2]);
    expandItems(grid.$connector, [rootItem, rootItem2]);

    // Add 100 child items for the first root item
    const childItems = [...Array(100).keys()].map((i) => ({ key: `0-${i}`, name: `foo-${i}` }));
    setChildItems(grid.$connector, rootItem, childItems);
    await nextFrame();

    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    // The children of the first root item were preloaded and the second (expanded)
    // root item is outside the DOM so no requests should have been sent
    expect(grid.$server.setParentRequestedRanges).to.not.be.called;
  });

  it('should not request items for expanded and immediately collapsed parent', async () => {
    // Add expandable root item
    const rootItem = { key: '0', name: 'foo', children: true };
    setRootItems(grid.$connector, [rootItem]);

    // Expand the root item
    expandItems(grid.$connector, [rootItem]);
    expect(isGridInLoadingState(grid)).to.be.true;
    // Immediately collapse the root item
    collapseItems(grid.$connector, [rootItem]);
    await nextFrame();
    
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);

    expect(isGridInLoadingState(grid)).to.be.false;
    expect(grid.$server.setParentRequestedRanges).to.not.be.called;
  });

  it('should not request items for expanded and shortly collapsed parent', async () => {
    // Add expandable root item
    const rootItem = { key: '0', name: 'foo', children: true };
    setRootItems(grid.$connector, [rootItem]);

    // Expand the root item
    expandItems(grid.$connector, [rootItem]);
    expect(isGridInLoadingState(grid)).to.be.true;
    // Collapse the root item after a short delay
    await nextFrame();
    collapseItems(grid.$connector, [rootItem]);

    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);

    expect(isGridInLoadingState(grid)).to.be.false;
    expect(grid.$server.setParentRequestedRanges).to.not.be.called;
  });
});
