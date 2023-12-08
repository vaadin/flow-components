import { aTimeout, expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { getHeaderCellContent, init, setRootItems } from './shared.js';
import type { FlowGrid, Item } from './shared.js';
import { GridColumn } from '@vaadin/grid';
import { GridSorter } from '@vaadin/grid/vaadin-grid-sorter.js';

describe('grid connector - sorting', () => {
  let grid: FlowGrid;
  let columns: GridColumn<Item>[];
  let sorters: Array<GridSorter & { _order?: number | null }>;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
        <vaadin-grid-column path="price"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    columns = [...grid.querySelectorAll<GridColumn<Item>>('vaadin-grid-column')];

    grid.$connector.setHeaderRenderer(columns[0], { content: 'Name', showSorter: true, sorterPath: 'name' });
    grid.$connector.setHeaderRenderer(columns[1], { content: 'Price', showSorter: true, sorterPath: 'price' });

    setRootItems(grid.$connector, [
      { key: '0', name: 'Macbook', price: 2500 },
      { key: '1', name: 'iPad', price: 1000 }
    ]);
    await nextFrame();

    sorters = columns.map((column) => getHeaderCellContent(column).querySelector<GridSorter>('vaadin-grid-sorter')!);
  });

  it('should not make sort requests by default', () => {
    expect(grid.$server.sortersChanged).to.not.be.called;
  });

  describe('single column sorting', () => {
    it('should make a sort request on sorter click', () => {
      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'name', direction: 'asc' }]);

      grid.$server.sortersChanged.resetHistory();

      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'name', direction: 'desc' }]);

      grid.$server.sortersChanged.resetHistory();

      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'name', direction: null }]);
    });

    it('should make a sort request when switching sorters', () => {
      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'name', direction: 'asc' }]);

      grid.$server.sortersChanged.resetHistory();

      sorters[1].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'price', direction: 'asc' }]);
    });

    describe('setSorterDirections', () => {
      it('should prevent sort requests while setting directions', async () => {
        grid.$connector.setSorterDirections([{ column: 'name', direction: 'asc' }]);
        await aTimeout(0);
        expect(grid.$server.sortersChanged).to.be.not.called;
      });

      it('should not prevent sort requests after directions are set', async () => {
        grid.$connector.setSorterDirections([{ column: 'name', direction: 'asc' }]);
        await aTimeout(0);
        sorters[0].click();
        expect(grid.$server.sortersChanged).to.be.calledOnce;
      });

      it('should update direction on sorters', async () => {
        grid.$connector.setSorterDirections([{ column: 'name', direction: 'asc' }]);
        await aTimeout(0);
        expect(sorters[0].direction).to.equal('asc');
        expect(sorters[1].direction).to.be.null;

        grid.$connector.setSorterDirections([{ column: 'name', direction: 'desc' }]);
        await aTimeout(0);
        expect(sorters[0].direction).to.equal('desc');
        expect(sorters[1].direction).to.be.null;

        grid.$connector.setSorterDirections([{ column: 'price', direction: 'asc' }]);
        await aTimeout(0);
        expect(sorters[0].direction).to.be.null;
        expect(sorters[1].direction).to.equal('asc');

        grid.$connector.setSorterDirections([]);
        await aTimeout(0);
        expect(sorters[0].direction).to.be.null;
        expect(sorters[1].direction).to.be.null;
      });

      it('should update direction on hidden sorters', async () => {
        grid.$connector.setSorterDirections([{ column: 'name', direction: 'asc' }]);
        await aTimeout(0);
        columns[0].hidden = true;
        await nextFrame();

        grid.$connector.setSorterDirections([]);
        await aTimeout(0);
        expect(sorters[0].direction).to.be.null;
      });
    });
  });

  describe('multiple column sorting', () => {
    beforeEach(() => {
      grid.multiSort = true;
    });

    it('should make a sort request on sorter click', () => {
      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'name', direction: 'asc' }]);

      grid.$server.sortersChanged.resetHistory();

      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'name', direction: 'desc' }]);

      grid.$server.sortersChanged.resetHistory();

      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([]);
    });

    it('should make a sort request when joining sorters with multiSortPriority=append', () => {
      grid.multiSortPriority = 'append';
      sorters[0].click();
      grid.$server.sortersChanged.resetHistory();

      sorters[1].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([
        { path: 'name', direction: 'asc' },
        { path: 'price', direction: 'asc' }
      ]);
    });

    it('should make a sort request when joining sorters with multiSortPriority=prepend', () => {
      grid.multiSortPriority = 'prepend';
      sorters[0].click();
      grid.$server.sortersChanged.resetHistory();

      sorters[1].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([
        { path: 'price', direction: 'asc' },
        { path: 'name', direction: 'asc' }
      ]);
    });

    describe('setSorterDirections', () => {
      (['append', 'prepend'] as const).forEach((multiSortPriority) => {
        describe(`multiSortPriority=${multiSortPriority}`, () => {
          beforeEach(() => {
            grid.multiSortPriority = multiSortPriority;
          });

          it('should update direction on sorters', async () => {
            grid.$connector.setSorterDirections([{ column: 'name', direction: 'asc' }]);
            await aTimeout(0);
            expect(sorters[0].direction).to.equal('asc');
            expect(sorters[1].direction).to.be.null;

            grid.$connector.setSorterDirections([
              { column: 'name', direction: 'asc' },
              { column: 'price', direction: 'asc' }
            ]);
            await aTimeout(0);
            expect(sorters[0].direction).to.equal('asc');
            expect(sorters[1].direction).to.equal('asc');

            grid.$connector.setSorterDirections([
              { column: 'price', direction: 'desc' },
              { column: 'name', direction: 'desc' }
            ]);
            await aTimeout(0);
            expect(sorters[0].direction).to.equal('desc');
            expect(sorters[1].direction).to.equal('desc');

            grid.$connector.setSorterDirections([{ column: 'price', direction: 'desc' }]);
            await aTimeout(0);
            expect(sorters[0].direction).to.be.null;
            expect(sorters[1].direction).to.equal('desc');

            grid.$connector.setSorterDirections([]);
            await aTimeout(0);
            expect(sorters[0].direction).to.be.null;
            expect(sorters[1].direction).to.be.null;
          });

          it('should update sorters order', async () => {
            grid.$connector.setSorterDirections([{ column: 'name', direction: 'asc' }]);
            await aTimeout(0);
            expect(sorters[0]._order).to.be.null;
            expect(sorters[1]._order).to.be.null;

            grid.$connector.setSorterDirections([
              { column: 'name', direction: 'asc' },
              { column: 'price', direction: 'asc' }
            ]);
            await aTimeout(0);
            expect(sorters[0]._order).to.equal(0);
            expect(sorters[1]._order).to.equal(1);

            grid.$connector.setSorterDirections([
              { column: 'price', direction: 'desc' },
              { column: 'name', direction: 'desc' }
            ]);
            await aTimeout(0);
            expect(sorters[0]._order).to.equal(1);
            expect(sorters[1]._order).to.equal(0);

            grid.$connector.setSorterDirections([{ column: 'price', direction: 'desc' }]);
            await aTimeout(0);
            expect(sorters[0]._order).to.be.null;
            expect(sorters[1]._order).to.be.null;
          });
        });
      });
    });
  });
});
