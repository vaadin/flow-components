import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, getBodyCellContent, setRootItems, getHeaderCellContent, FlowGridSelectionColumn, initSelectionColumn } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - selection – multi mode', () => {
  let grid: FlowGrid;
  let selectionColumn: FlowGridSelectionColumn;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-flow-selection-column></vaadin-grid-flow-selection-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);
    init(grid);

    selectionColumn = grid.querySelector('vaadin-grid-flow-selection-column')!;
    initSelectionColumn(grid, selectionColumn);

    setRootItems(grid.$connector, [
      { key: '0', name: 'foo' },
      { key: '1', name: 'bar' }
    ]);
    await nextFrame();

    grid.$connector.setSelectionMode('MULTI');
  });

  function clickSelectCheckbox(row: number) {
    getBodyCellContent(grid, row, 0)!.querySelector('vaadin-checkbox')!.click();
  }

  function clickSelectAllCheckbox() {
    getHeaderCellContent(selectionColumn).querySelector('vaadin-checkbox')!.click();
  }

  describe('client to server', () => {
    it('should select items', () => {
      clickSelectCheckbox(0);
      expect(grid.selectedItems).to.have.lengthOf(1);
      expect(grid.selectedItems[0].key).to.equal('0');

      clickSelectCheckbox(1);
      expect(grid.selectedItems).to.have.lengthOf(2);
      expect(grid.selectedItems[0].key).to.equal('0');
      expect(grid.selectedItems[1].key).to.equal('1');
    });

    it('should deselect items', () => {
      clickSelectCheckbox(0);
      clickSelectCheckbox(1);

      clickSelectCheckbox(1);
      expect(grid.selectedItems).to.have.lengthOf(1);
      expect(grid.selectedItems[0].key).to.equal('0');

      clickSelectCheckbox(0);
      expect(grid.selectedItems).to.be.empty;
    });

    it('should select items on server', () => {
      clickSelectCheckbox(0);
      expect(grid.$server.select).to.be.calledWith('0');
    });

    it('should deselect items on server', () => {
      clickSelectCheckbox(0);
      clickSelectCheckbox(0);
      expect(grid.$server.deselect).to.be.calledWith('0');
    });

    it('should select all items on server', () => {
      clickSelectAllCheckbox();
      expect(grid.$server.selectAll).to.be.calledOnce;
    });

    it('should deselect all items on server', () => {
      selectionColumn.selectAll = true;
      clickSelectAllCheckbox();
      expect(grid.$server.deselectAll).to.be.calledOnce;
    });
  });

  describe('server to client', () => {
    it('should select items', () => {
      grid.$connector.doSelection([{ key: '0' }, { key: '1' }], false);
      expect(grid.selectedItems).to.have.lengthOf(2);
      expect(grid.selectedItems[0].key).to.equal('0');
      expect(grid.selectedItems[1].key).to.equal('1');
    });

    it('should deselect items', () => {
      grid.$connector.doSelection([{ key: '0' }, { key: '1' }], false);
      grid.$connector.doDeselection([{ key: '1' }], false);
      expect(grid.selectedItems).to.have.lengthOf(1);
      expect(grid.selectedItems[0].key).to.equal('0');

      grid.$connector.doDeselection([{ key: '0' }], false);
      expect(grid.selectedItems).to.be.empty;
    });

    it('should not request server to select already selected items', () => {
      grid.$connector.doSelection([{ key: '0' }], false);
      expect(grid.$server.select).not.to.be.called;
    });

    it('should not request server to deselect already deselected items', () => {
      grid.$connector.doSelection([{ key: '0' }], false);
      grid.$connector.doDeselection([{ key: '0' }], false);
      expect(grid.$server.deselect).not.to.be.called;
    });
  });
});
