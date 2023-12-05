import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, getBodyCellContent, setRootItems } from './shared.js';
import type { FlowGrid, Item } from './shared.js';
import sinon from 'sinon';
import { GridColumn } from '@vaadin/grid';
import { GridSorter } from '@vaadin/grid/vaadin-grid-sorter.js';

describe('grid connector - sorting', () => {
  let grid: FlowGrid;
  let columns: GridColumn<Item>[];
  let sorters: GridSorter[];

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
        <vaadin-grid-column path="age"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    columns = [...grid.querySelectorAll('vaadin-grid-column')] as GridColumn<Item>[];

    grid.$connector.setHeaderRenderer(columns[0], { content: 'Name', showSorter: true, sorterPath: 'name' });
    grid.$connector.setHeaderRenderer(columns[1], { content: 'Age', showSorter: true, sorterPath: 'age' });

    setRootItems(grid.$connector, [
      { key: '0', name: 'Andrew', age: 25 },
      { key: '1', name: 'Bob', age: 30 }
    ]);
    await nextFrame();

    sorters = [...grid.querySelectorAll('vaadin-grid-sorter')] as GridSorter[];
  });

  it('should not make requests to server by default', () => {
    expect(grid.$server.sortersChanged).to.not.be.called;
  });

  describe('single column sorting', () => {
    it('should notify server on sorter click', () => {
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

    it('should notify server when switching sorters', () => {
      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'name', direction: 'asc' }]);

      grid.$server.sortersChanged.resetHistory();

      sorters[1].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'age', direction: 'asc' }]);
    });
  });

  describe('multiple column sorting', () => {
    beforeEach(() => {
      grid.multiSort = true;
    });

    it('should notify server on sorter click', () => {
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

    it('should notify server when joining sorters', () => {
      sorters[0].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([{ path: 'name', direction: 'asc' }]);

      grid.$server.sortersChanged.resetHistory();

      sorters[1].click();
      expect(grid.$server.sortersChanged).to.be.calledOnce;
      expect(grid.$server.sortersChanged.args[0][0]).to.eql([
        { path: 'age', direction: 'asc' },
        { path: 'name', direction: 'asc' }
      ]);
    });
  });
});
