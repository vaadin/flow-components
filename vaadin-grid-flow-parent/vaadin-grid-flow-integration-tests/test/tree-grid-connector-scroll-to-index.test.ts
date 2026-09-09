import { expect } from 'chai';
import { aTimeout, fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems, getBodyCellText, treeGridConnector, GRID_CONNECTOR_ROOT_REQUEST_DELAY } from './shared.js';
import type { FlowTreeGrid, Item } from './shared.js';

const ITEMS = Array.from({ length: 1000 }, (_, i) => {
  return { key: `${i}`, name: `Item-${i}` } as Item;
});

describe('tree grid connector - scroll to index', () => {
  let grid: FlowTreeGrid;

  beforeEach(() => {
    grid = fixtureSync(`
      <vaadin-grid style="height: 400px">
        <vaadin-grid-tree-column path="name"></vaadin-grid-tree-column>
      </vaadin-grid>
      <style>
        vaadin-grid::part(cell) {
          min-height: 36px;
        }
      </style>
    `);

    init(grid, treeGridConnector);
  });

  /**
   * Emulates the server response to setViewportRangeByIndexPath: the viewport
   * range moves to the target, so the old range gets cleared and the range
   * around the target gets set, while the old range is still rendered. The
   * promise resolves with the flat index of the target after that.
   */
  function resolveViewportRangeByIndexPath(flatIndex: number) {
    grid.$connector.clear(0, grid.pageSize);
    setRootItems(grid.$connector, ITEMS, flatIndex - 100, 200);
    grid.$server.setViewportRangeByIndexPath.promise?.resolve(flatIndex);
  }

  describe('before grid has rendered', () => {
    beforeEach(async () => {
      // Emulates a scroll call that runs in the same round trip in which the
      // grid is attached, e.g. scrollToIndex() in a view constructor. The grid
      // has not rendered yet, so the connector defers the scroll.
      grid.scrollToIndex(2, 5);
      setRootItems(grid.$connector, ITEMS, 0, grid.pageSize);
      await nextFrame();
    });

    it('should scroll to the resolved flat index once rendered', async () => {
      expect(grid.$server.setViewportRangeByIndexPath).to.be.calledOnce;
      expect(grid.$server.setViewportRangeByIndexPath.args[0][0]).to.eql([2, 5]);

      resolveViewportRangeByIndexPath(500);
      await nextFrame();
      expect(getBodyCellText(grid, 500, 0)).to.equal('Item-500');
    });
  });

  describe('after grid has rendered', () => {
    beforeEach(async () => {
      await nextFrame();
      setRootItems(grid.$connector, ITEMS, 0, grid.pageSize);
      await nextFrame();
      grid.$server.setViewportRange.resetHistory();
    });

    it('should scroll to the resolved flat index without requesting the old range', async () => {
      const scrollPromise = grid.scrollToIndex(2, 5);
      expect(grid.$server.setViewportRangeByIndexPath).to.be.calledOnce;
      expect(grid.$server.setViewportRangeByIndexPath.args[0][0]).to.eql([2, 5]);

      resolveViewportRangeByIndexPath(500);
      await scrollPromise;
      expect(getBodyCellText(grid, 500, 0)).to.equal('Item-500');

      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$server.setViewportRange).to.not.be.called;
      expect(grid.loading).to.be.false;
    });

    it('should keep requesting data on scroll after scrollToIndex has finished', async () => {
      const scrollPromise = grid.scrollToIndex(2, 5);
      resolveViewportRangeByIndexPath(500);
      await scrollPromise;

      // Emulates the user scrolling to the end, which the server has not sent
      grid.$.table.scrollTop = grid.$.table.scrollHeight;
      await nextFrame();
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$server.setViewportRange).to.be.calledOnce;
    });
  });
});
