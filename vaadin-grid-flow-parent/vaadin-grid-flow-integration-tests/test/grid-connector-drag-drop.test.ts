import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems, getBodyCell } from './shared.js';
import type { FlowGrid, Item } from './shared.js';
import sinon from 'sinon';

describe('grid connector - drag and drop', () => {
  let grid: FlowGrid;
  let items: Item[];

  function dispatchDragStart(draggedItems: Item[]) {
    const setDragData = sinon.spy();
    const setDraggedItemsCount = sinon.spy();
    grid.dispatchEvent(
      new CustomEvent('grid-dragstart', {
        detail: { draggedItems, setDragData, setDraggedItemsCount }
      })
    );
    return { setDragData, setDraggedItemsCount };
  }

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    items = [
      { key: '0', name: 'foo', dragData: { text: 'foo drag data' } },
      { key: '1', name: 'bar', dragData: { text: 'bar drag data' } }
    ];
    setRootItems(grid.$connector, items);
    await nextFrame();
  });

  describe('row drag and drop filters', () => {
    beforeEach(async () => {
      setRootItems(grid.$connector, [
        { key: '0', name: 'foo', dragDisabled: true, dropDisabled: true },
        { key: '1', name: 'bar' }
      ]);
      grid.rowsDraggable = true;
      grid.dropMode = 'on-top';
      await nextFrame();
    });

    it('should disable dragging rows based on item drag data', () => {
      expect(getBodyCell(grid, 0, 0)!.parentElement!.getAttribute('part')).to.contain('drag-disabled-row');
      expect(getBodyCell(grid, 1, 0)!.parentElement!.getAttribute('part')).to.not.contain('drag-disabled-row');
    });

    it('should disable dropping on rows based on item drop data', () => {
      expect(getBodyCell(grid, 0, 0)!.parentElement!.getAttribute('part')).to.contain('drop-disabled-row');
      expect(getBodyCell(grid, 1, 0)!.parentElement!.getAttribute('part')).to.not.contain('drop-disabled-row');
    });
  });

  describe('drag data', () => {
    beforeEach(() => {
      grid.__dragDataTypes = ['text'];
    });

    it('should set drag data for a dragged non-selected item', () => {
      const { setDragData } = dispatchDragStart([items[0]]);
      expect(setDragData.calledWith('text', 'foo drag data')).to.be.true;
    });

    it('should set combined drag data for dragged selected items', () => {
      grid.$connector.setSelectionMode('MULTI');
      grid.$connector.doSelection(items, false);

      const { setDragData } = dispatchDragStart(items);
      expect(setDragData.calledWith('text', 'foo drag data\nbar drag data')).to.be.true;
    });

    it('should set selection drag data for dragged selected items when defined', () => {
      grid.$connector.setSelectionMode('MULTI');
      grid.$connector.doSelection(items, false);
      grid.__selectionDragData = { text: 'selection drag data' };

      const { setDragData } = dispatchDragStart(items);
      expect(setDragData.calledWith('text', 'selection drag data')).to.be.true;
    });

    it('should set dragged items count for dragged selected items', () => {
      grid.$connector.setSelectionMode('MULTI');
      grid.$connector.doSelection(items, false);
      grid.__selectionDraggedItemsCount = 5;

      const { setDraggedItemsCount } = dispatchDragStart(items);
      expect(setDraggedItemsCount.calledWith(5)).to.be.true;
    });
  });
});
