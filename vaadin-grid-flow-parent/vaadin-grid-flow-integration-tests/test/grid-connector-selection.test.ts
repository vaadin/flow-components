import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, getBodyCellContent, setRootItems } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - selection', () => {
  let grid: FlowGrid;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    setRootItems(grid.$connector, [
      { key: '0', name: 'foo' },
      { key: '1', name: 'bar' }
    ]);
    await nextFrame();
  });

  describe('single selection mode', () => {
    beforeEach(() => {
      grid.$connector.setSelectionMode('SINGLE');
    });

    it('should select item on click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems.length).to.equal(1);
      expect(grid.selectedItems[0].key).to.equal('0');
    });

    it('should mark the item selected', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems[0].selected).to.be.true;
    });

    it('should deselect old selection on another item click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 1, 0)!.click();
      expect(grid.selectedItems.length).to.equal(1);
      expect(grid.selectedItems[0].key).to.equal('1');
    });

    it('should mark the item deselected', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      const item = grid.selectedItems[0];
      getBodyCellContent(grid, 0, 0)!.click();
      expect(item.selected).not.to.be.true;
    });

    it('should deselect on selected item click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems).to.be.empty;
    });

    it('should not deselect on selected item click when deselect is disallowed', () => {
      grid.__deselectDisallowed = true;
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems.length).to.equal(1);
      expect(grid.selectedItems[0].key).to.equal('0');
    });

    it('should not select item on click when grid is disabled', () => {
      grid.disabled = true;
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems).to.be.empty;
    });

    it('should select on server', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.$server.select).to.be.calledWith('0');
    });

    it('should deselect on server', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.$server.deselect).to.be.calledWith('0');
    });

    it('should apply selection from data', async () => {
      setRootItems(grid.$connector, [{ key: '0', name: 'foo', selected: true}]);
      expect(grid.selectedItems.length).to.equal(1);
      expect(grid.selectedItems[0].key).to.equal('0');
    });

    it('should apply deselection from data', async () => {
      getBodyCellContent(grid, 0, 0)!.click();
      setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
      expect(grid.selectedItems).to.be.empty;
    });
  });

  describe('none selection mode', () => {
    beforeEach(() => {
      grid.$connector.setSelectionMode('NONE');
    });

    it('should not select item on click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems).to.be.empty;
    });

    it('should not apply selection from data', async () => {
      setRootItems(grid.$connector, [{ key: '0', name: 'foo', selected: true }]);
      expect(grid.selectedItems).to.be.empty;
    });
  });
});
