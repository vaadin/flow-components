import { aTimeout, expect, fixtureSync, nextFrame } from '@open-wc/testing';
import {
  init,
  gridConnector,
  getBodyRowCount,
  getBodyCellText,
  setRootItems,
  clear,
  GRID_CONNECTOR_ROOT_REQUEST_DELAY
} from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector', () => {
  let grid: FlowGrid;

  beforeEach(() => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
  });

  it('should not reinitialize the connector', () => {
    const connector = grid.$connector;
    gridConnector.initLazy(grid);
    expect(grid.$connector).to.equal(connector);
  });

  it('should add root level items', async () => {
    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    await nextFrame();

    expect(getBodyRowCount(grid)).to.equal(1);
    expect(getBodyCellText(grid, 0, 0)).to.equal('foo');
  });

  it('should request new items on clear', async () => {
    // Use a smaller page size for testing
    const pageSize = 5;
    grid.pageSize = pageSize;
    grid.$connector.reset();

    // Add 10 root items
    setRootItems(grid.$connector, Array.from({ length: 10 }, (_, i) => ({ key: `${i}`, name: `foo${i}` })));

    await nextFrame();
    grid.$server.setRequestedRange.resetHistory();

    // Clear the items
    clear(grid.$connector, 0, 10);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

    // Grid should have requested new items
    expect(grid.$server.setRequestedRange.calledOnce).to.be.true;

    // Add the requested items
    setRootItems(grid.$connector, Array.from({ length: 10 }, (_, i) => ({ key: `${i}`, name: `foo${i}` })));

    grid.$server.setRequestedRange.resetHistory();

    // Clear the items again
    clear(grid.$connector, 0, 10);

    // Add the first page items back before the request timeout
    grid.$connector.set(0, Array.from({ length: 5 }, (_, i) => ({ key: `${i}`, name: `foo${i}` })));
    grid.$connector.confirm(-1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

    // Grid should have requested for the missing items
    expect(grid.$server.setRequestedRange.calledOnce).to.be.true;
  });

  it('should not request for new items if they are available', async () => {
    // Add one root item
    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    await nextFrame();
    grid.$server.setRequestedRange.resetHistory();

    // Clear the first item
    clear(grid.$connector, 0, 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

    // Grid should have requested new items
    expect(grid.$server.setRequestedRange.calledOnce).to.be.true;

    // Add the requested items
    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    grid.$server.setRequestedRange.resetHistory();

    // Clear the first item again
    clear(grid.$connector, 0, 1);
    // Add the item back before the request timeout
    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);

    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

    // Grid should have not requested for items
    expect(grid.$server.setRequestedRange.calledOnce).to.be.false;
  });

  describe('empty grid', () => {
    it('should not have loading state when refreshing grid', async () => {
      setRootItems(grid.$connector, []);
      await nextFrame();

      // Force grid to refresh data
      grid.clearCache();
      await nextFrame();

      expect(grid.hasAttribute('loading')).to.be.false;
    });

    it('should not request items when refreshing grid', async () => {
      setRootItems(grid.$connector, []);
      await nextFrame();

      // Force grid to refresh data
      grid.clearCache();
      await nextFrame();

      expect(grid.$server.setRequestedRange.called).to.be.false;
    });
  });
});
