import { expect } from 'chai';
import { aTimeout, fixtureSync, nextFrame } from '@vaadin/testing-helpers';
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
import sinon from 'sinon';

describe('grid connector', () => {
  let grid: FlowGrid;

  beforeEach(() => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
      <style>
        vaadin-grid::part(cell) {
          min-height: 36px;
        }
      </style>
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

  it('should update item id path', () => {
    grid.$connector.updateUniqueItemIdPath('name');
    expect(grid.itemIdPath).to.equal('name');
  });

  it('should confirm updates to the server', () => {
    grid.$connector.confirm(42);
    expect(grid.$server.confirmUpdate.calledWith(42)).to.be.true;
  });

  describe('updateFlatData', () => {
    beforeEach(async () => {
      setRootItems(grid.$connector, [
        { key: '0', name: 'foo' },
        { key: '1', name: 'bar' }
      ]);
      await nextFrame();
    });

    it('should update item data', async () => {
      grid.$connector.updateFlatData([{ key: '1', name: 'bar updated' }]);
      await nextFrame();
      expect(getBodyCellText(grid, 1, 0)).to.equal('bar updated');
    });

    it('should ignore unknown items', async () => {
      expect(() => {
        grid.$connector.updateFlatData([
          { key: '999', name: 'unknown' },
          { key: '0', name: 'foo updated' }
        ]);
      }).to.not.throw();
      await nextFrame();
      expect(getBodyCellText(grid, 0, 0)).to.equal('foo updated');
    });
  });

  describe('multiple set calls', () => {
    beforeEach(async () => {
      setRootItems(grid.$connector, [
        { key: '0', name: 'foo' },
        { key: '1', name: 'bar' },
        { key: '2', name: 'baz' }
      ]);
      await nextFrame();
    });

    it('should re-render changed items', async () => {
      grid.$connector.set(1, [{ key: '1', name: 'bar refreshed' }]);
      grid.$connector.set(2, [{ key: '2', name: 'baz refreshed' }]);
      await nextFrame();
      expect(getBodyCellText(grid, 0, 0)).to.equal('foo');
      expect(getBodyCellText(grid, 1, 0)).to.equal('bar refreshed');
      expect(getBodyCellText(grid, 2, 0)).to.equal('baz refreshed');
    });

    it('should not re-render unchanged items', async () => {
      const rendererSpy = sinon.spy();
      grid.querySelector('vaadin-grid-column')!.renderer = rendererSpy;
      rendererSpy.resetHistory();

      grid.$connector.set(1, [{ key: '1', name: 'bar refreshed' }]);
      await nextFrame();

      rendererSpy.args.forEach(([_root, _column, model]) => {
        expect(model.item.name).to.not.equal('foo');
        expect(model.item.name).to.not.equal('baz');
      });
    });
  });

  it('should cancel debounced requests if all data has already been received', async () => {
    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    await nextFrame();
    grid.$connector.reset();
    setRootItems(grid.$connector, [{ key: '0', name: 'bar' }]);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expect(grid.$server.setViewportRange).to.be.not.called;
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

      expect(grid.$server.setViewportRange.called).to.be.false;
    });

    it('should not schedule debounced requests when refreshing grid', async () => {
      setRootItems(grid.$connector, []);
      await nextFrame();

      // Force grid to refresh data
      grid.clearCache();
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

      expect(grid.$server.setViewportRange.called).to.be.false;
    });
  });

  describe('clear', () => {
    beforeEach(async () => {
      grid.pageSize = 2;
      setRootItems(grid.$connector, [
        { key: '0', name: 'foo' },
        { key: '1', name: 'bar' },
        { key: '2', name: 'baz' },
        { key: '3', name: 'qux' }
      ]);
      await nextFrame();
    });

    it('should re-render only cleared items', async () => {
      const rendererSpy = sinon.spy();
      grid.querySelector('vaadin-grid-column')!.renderer = rendererSpy;
      rendererSpy.resetHistory();

      grid.$connector.clear(2, 2);
      await nextFrame();

      rendererSpy.args.forEach(([_root, _column, model]) => {
        expect(model.item.name).to.not.equal('foo');
        expect(model.item.name).to.not.equal('bar');
      });
    });
  });

  // A setup where the grid has requested items, and the server has successfully
  // responded with a full item set.
  describe('grid with a requested data range', () => {
    const items = Array.from({ length: 100 }, (_, i) => ({ key: `${i}`, name: `foo${i}` }));

    beforeEach(async () => {
      // Use a smaller page size for testing
      grid.pageSize = 10;
      grid.$connector.reset();

      // Add all root items
      setRootItems(grid.$connector, items);
      await nextFrame();
    });

    it('should report a root request queue while requesting data', async () => {
      expect(grid.$connector.hasRootRequestQueue()).to.be.false;

      // Clearing visible items makes the grid request data again (debounced)
      clear(grid.$connector, 0, 30);
      expect(grid.$connector.hasRootRequestQueue()).to.be.true;

      // The debounced request has been sent but not yet confirmed
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$connector.hasRootRequestQueue()).to.be.true;

      // Receiving the items resolves the queue
      setRootItems(grid.$connector, items, 0, 30);
      expect(grid.$connector.hasRootRequestQueue()).to.be.false;
    });

    it('should request data again after reset', async () => {
      grid.$server.setViewportRange.resetHistory();
      grid.$connector.reset();
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$server.setViewportRange.called).to.be.true;
    });

    describe('last requested range is in viewport', () => {
      beforeEach(async () => {
        // Request a range of items at the top
        clear(grid.$connector, 0, 30);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setViewportRange).to.be.calledOnceWith(0, 30);
        setRootItems(grid.$connector, items, 0, 30);
        grid.$server.setViewportRange.resetHistory();
      });

      it('should request new items after incomplete confirm', async () => {
        // Clear the items again
        clear(grid.$connector, 0, 30);

        // Add the first page items back before the request timeout (partial/incomplete preload)
        setRootItems(grid.$connector, items, 0, grid.pageSize);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

        // Grid should have requested for the missing items
        expect(grid.$server.setViewportRange).to.be.calledOnceWith(0, 30);
      });

      it('should not request for new items after complete confirm', async () => {
        // Clear the items again
        clear(grid.$connector, 0, 100);

        // Add all the items back before the request timeout
        setRootItems(grid.$connector, items);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

        // Grid should not have request for items
        expect(grid.$server.setViewportRange).to.be.not.called;
      });

      it('should request the same range again after reset', async () => {
        // Make the grid request a range again, but leave the request unconfirmed
        clear(grid.$connector, 0, 30);
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setViewportRange.calledOnceWith(0, 30)).to.be.true;
        grid.$server.setViewportRange.resetHistory();

        // Resetting while the request is pending should allow requesting
        // the same range again
        grid.$connector.reset();
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
        expect(grid.$server.setViewportRange.calledOnceWith(0, 30)).to.be.true;
      });

      it('should cancel pending debounced requests on reset', async () => {
        // Clearing visible items makes the grid request data again (debounced)
        clear(grid.$connector, 0, 30);

        // The grid becomes empty before the debounced request is sent
        grid.$connector.updateSize(0);
        grid.$connector.reset();
        await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

        expect(grid.$server.setViewportRange.called).to.be.false;
      });
    });
  });
});
