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
    const items = Array.from({ length: 100 }, (_, i) => ({ key: `${i}`, name: `foo${i}` }));

    beforeEach(async () => {
      // Use a smaller page size for testing
      grid.pageSize = 25;
      grid.$connector.reset();

      // Add all root items
      setRootItems(grid.$connector, items);
      await nextFrame();
    });

    describe('last requested range is in viewport', () => {
      beforeEach(async () => {
        // Request a range of items at the top
        clear(grid.$connector, 0, 50);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setRequestedRange).to.be.calledOnceWith(0, 50);
        setRootItems(grid.$connector, items, 0, 50);
        grid.$server.setRequestedRange.resetHistory();
      });

      it('should request new items after incomplete confirm', async () => {
        // Clear the items again
        clear(grid.$connector, 0, 100);

        // Add the first page items back before the request timeout (partial/incomplete preload)
        setRootItems(grid.$connector, items, 0, grid.pageSize);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

        // Grid should have requested for the missing items
        expect(grid.$server.setRequestedRange).to.be.calledOnceWith(0, 50);
      });

      it('should not request for new items after complete confirm', async () => {
        // Clear the items again
        clear(grid.$connector, 0, 100);

        // Add all the items back before the request timeout
        setRootItems(grid.$connector, items);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

        // Grid should not have request for items
        expect(grid.$server.setRequestedRange).to.be.not.called;
      });
    });

    describe('last requested range is not in viewport', () => {
      beforeEach(async () => {
        // Request a range of items further down
        clear(grid.$connector, 50, 50);
        grid.scrollToIndex(50);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setRequestedRange).to.have.been.calledOnceWith(25, 75);
        setRootItems(grid.$connector, items, 25, 75);
        grid.$server.setRequestedRange.resetHistory();
      });

      it('should request for items if part of the last range was cleared', async () => {
        // Simulate preloading of items when scrolling to top programmatically on server-side, which may also partially clear the last requested range:
        // - Scroll to top
        // - Clear last requested range partially
        // - Preload first two pages so that grid doesn't need to request a new range yet
        grid.scrollToIndex(0);
        clear(grid.$connector, 50, grid.pageSize);
        setRootItems(grid.$connector, items, 0, 50);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setRequestedRange).to.not.have.been.called;

        // Scroll down again, should reload the range because part of it was cleared
        grid.scrollToIndex(50);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setRequestedRange).to.have.been.calledOnceWith(25, 75);
      });

      it('should not request for items if data outside of the last range was cleared', async () => {
        // Simulate preloading of items when scrolling to top programmatically on server-side, which may also partially clear the last requested range:
        // - Scroll to top
        // - Clear data outside the requested range
        // - Preload first two pages so that grid doesn't need to request a new range yet
        grid.scrollToIndex(0);
        clear(grid.$connector, 75, grid.pageSize);
        grid.$connector.confirm(-1);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setRequestedRange).to.not.have.been.called;

        // Scroll down again, should not reload the range because nothing from it was cleared
        grid.scrollToIndex(50);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setRequestedRange).to.not.have.been.called;
      });
    });
  });
});
