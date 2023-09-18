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

  // A setup where the grid has requested items, and the server has successfully
  // responded with a full item set.
  describe('grid with a requested data range', () => {
    const items = Array.from({ length: 10 }, (_, i) => ({ key: `${i}`, name: `foo${i}` }));

    beforeEach(async () => {
      // Use a smaller page size for testing
      grid.pageSize = 5;
      grid.$connector.reset();

      // Add 10 root items
      setRootItems(grid.$connector, items);

      await nextFrame();
      // Grid should not have requested for items yet (all the 10 items were preloaded)
      expect(grid.$server.setRequestedRange.called).to.be.false;

      // Clear the items
      clear(grid.$connector, 0, 10);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

      // Grid should have requested new items
      expect(grid.$server.setRequestedRange).to.be.calledOnce;

      // Add the requested items
      setRootItems(grid.$connector, items);

      grid.$server.setRequestedRange.resetHistory();
    });

    it('should request new items after incomplete confirm', async () => {
      // Clear the items again
      clear(grid.$connector, 0, 10);

      // Add the first page items back before the request timeout (partial/incomplete preload)
      grid.$connector.set(0, items.slice(0, grid.pageSize));
      grid.$connector.confirm(-1);

      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

      // Grid should have requested for the missing items
      expect(grid.$server.setRequestedRange).to.be.calledOnce;
    });

    it('should not request for new items after complete confirm', async () => {
      // Clear the items again
      clear(grid.$connector, 0, 10);

      // Add all the items back before the request timeout
      grid.$connector.set(0, items);
      grid.$connector.confirm(-1);

      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

      // Grid should not have request for items
      expect(grid.$server.setRequestedRange.called).to.be.false;
    });
  });
});
