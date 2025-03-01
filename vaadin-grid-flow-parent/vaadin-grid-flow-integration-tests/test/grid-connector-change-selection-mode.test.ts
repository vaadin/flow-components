import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, getBodyCellContent, setRootItems, initSelectionColumn } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - change selection mode', () => {
  let grid: FlowGrid;

  function clickSelectCheckbox(row: number) {
    getBodyCellContent(grid, row, 0)!.querySelector('vaadin-checkbox')!.click();
  }

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-flow-selection-column></vaadin-grid-flow-selection-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    const selectionColumn = grid.querySelector('vaadin-grid-flow-selection-column')!;
    initSelectionColumn(grid, selectionColumn);

    setRootItems(grid, [
      { key: '0', name: 'foo' },
      { key: '1', name: 'bar' }
    ]);
    await nextFrame();
  });

  describe('clear selection', () => {
    it('should clear selection when changing from single to none', () => {
      grid.$connector.setSelectionMode('SINGLE');
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems.length).to.equal(1);

      grid.$connector.setSelectionMode('NONE');

      expect(grid.selectedItems).to.be.empty;
    });

    it('should clear selection when changing from single to multi ', () => {
      grid.$connector.setSelectionMode('SINGLE');
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems.length).to.equal(1);

      grid.$connector.setSelectionMode('MULTI');

      expect(grid.selectedItems).to.be.empty;
    });

    it('should clear selection when changing from multi to none', () => {
      grid.$connector.setSelectionMode('MULTI');
      clickSelectCheckbox(0);
      clickSelectCheckbox(1);
      expect(grid.selectedItems.length).to.equal(2);

      grid.$connector.setSelectionMode('NONE');

      expect(grid.selectedItems).to.be.empty;
    });

    it('should clear selection when changing from multi to single', () => {
      grid.$connector.setSelectionMode('MULTI');
      clickSelectCheckbox(0);
      clickSelectCheckbox(1);
      expect(grid.selectedItems.length).to.equal(2);

      grid.$connector.setSelectionMode('SINGLE');

      expect(grid.selectedItems).to.be.empty;
    });
  });
});
