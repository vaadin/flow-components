import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, getBodyCellContent, setRootItems, initSelectionColumn } from './shared.js';
import type { FlowGrid, FlowGridSelectionColumn } from './shared.js';

describe('grid connector - change selection mode', () => {
  let grid: FlowGrid;

  function clickSelectCheckbox(row: number) {
    getBodyCellContent(grid, row, 0)!.querySelector<HTMLElement>('vaadin-checkbox')!.click();
  }

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-flow-selection-column></vaadin-grid-flow-selection-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    const selectionColumn = grid.querySelector<FlowGridSelectionColumn>('vaadin-grid-flow-selection-column')!;
    initSelectionColumn(grid, selectionColumn);

    setRootItems(grid.$connector, [
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

    it('should not restore items selected before mode change on subsequent selections', () => {
      grid.$connector.setSelectionMode('MULTI');
      grid.$connector.doSelection([{ key: '0' }], false);

      grid.$connector.setSelectionMode('MULTI');

      grid.$connector.doSelection([{ key: '1' }], false);
      expect(grid.selectedItems).to.have.lengthOf(1);
      expect(grid.selectedItems[0].key).to.equal('1');
    });
  });
});
