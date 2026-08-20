import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems } from './shared.js';
import type { FlowGrid } from './shared.js';
import type { GridColumn } from '@vaadin/grid/vaadin-grid-column.js';
import sinon from 'sinon';

type FlowColumn = GridColumn & { _flowId: string };

describe('grid connector - column events', () => {
  let grid: FlowGrid;
  let columns: FlowColumn[];

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="key"></vaadin-grid-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    columns = [...grid.querySelectorAll<FlowColumn>('vaadin-grid-column')];
    columns[0]._flowId = 'col0';
    columns[1]._flowId = 'col1';

    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    await nextFrame();
  });

  describe('column-resize', () => {
    it('should dispatch column-drag-resize on visible columns', () => {
      const spies = columns.map((column) => {
        const spy = sinon.spy();
        column.addEventListener('column-drag-resize', spy);
        return spy;
      });

      grid.dispatchEvent(new CustomEvent('column-resize', { detail: { resizedColumn: columns[0] } }));

      spies.forEach((spy) => expect(spy.calledOnce).to.be.true);
    });

    it('should dispatch column-drag-resize on the grid with the resized column key', () => {
      const spy = sinon.spy();
      grid.addEventListener('column-drag-resize' as any, spy);

      grid.dispatchEvent(new CustomEvent('column-resize', { detail: { resizedColumn: columns[0] } }));

      expect(spy.calledOnce).to.be.true;
      expect(spy.firstCall.args[0].detail.resizedColumnKey).to.equal('col0');
    });
  });

  describe('column-reorder', () => {
    it('should dispatch column-reorder-all-columns with ordered column ids', () => {
      const spy = sinon.spy();
      grid.addEventListener('column-reorder-all-columns' as any, spy);

      grid.dispatchEvent(new CustomEvent('column-reorder'));

      expect(spy.calledOnce).to.be.true;
      expect(spy.firstCall.args[0].detail.columns).to.deep.equal(['col0', 'col1']);
    });
  });
});
