import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { sendKeys, sendMouse } from '@web/test-runner-commands';
import { middleOfNode } from '@vaadin/testing-helpers';
import { init, setRootItems, FlowGridSelectionColumn, initSelectionColumn } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - selection – multi mode', () => {
  let grid: FlowGrid;
  let selectionColumn: FlowGridSelectionColumn;
  let selectAllCheckbox: HTMLElement;
  let selectRowCheckboxes: HTMLElement[];

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

    [selectAllCheckbox, ...selectRowCheckboxes] = [...grid.querySelectorAll('vaadin-checkbox')];
  });

  async function mouseClick(element: HTMLElement) {
    const { x, y } = middleOfNode(element);
    await sendMouse({ type: 'click', position: [Math.floor(x), Math.floor(y)] });
  }

  describe('client to server', () => {
    it('should select items', async () => {
      await mouseClick(selectRowCheckboxes[0]);
      expect(grid.selectedItems).to.have.lengthOf(1);
      expect(grid.selectedItems[0].key).to.equal('0');

      await mouseClick(selectRowCheckboxes[1]);
      expect(grid.selectedItems).to.have.lengthOf(2);
      expect(grid.selectedItems[0].key).to.equal('0');
      expect(grid.selectedItems[1].key).to.equal('1');
    });

    it('should deselect items', async () => {
      await mouseClick(selectRowCheckboxes[0]);
      await mouseClick(selectRowCheckboxes[1]);

      await mouseClick(selectRowCheckboxes[1]);
      expect(grid.selectedItems).to.have.lengthOf(1);
      expect(grid.selectedItems[0].key).to.equal('0');

      await mouseClick(selectRowCheckboxes[0]);
      expect(grid.selectedItems).to.be.empty;
    });

    it('should select items on server', async () => {
      await mouseClick(selectRowCheckboxes[0]);
      expect(grid.$server.select).to.be.calledWith('0');
    });

    it('should deselect items on server', async () => {
      await mouseClick(selectRowCheckboxes[0]);
      await mouseClick(selectRowCheckboxes[0]);
      expect(grid.$server.deselect).to.be.calledWith('0');
    });

    it('should set shift key flag on server when selecting with Shift', async () => {
      await sendKeys({ down: 'Shift' });
      expect(grid.$server.setShiftKeyDown).to.be.not.called;

      await mouseClick(selectRowCheckboxes[0]);
      expect(grid.$server.setShiftKeyDown).to.be.calledOnce;
      expect(grid.$server.setShiftKeyDown).to.be.calledWith(true);
      expect(grid.$server.setShiftKeyDown).to.be.calledBefore(grid.$server.select);

      grid.$server.setShiftKeyDown.resetHistory();

      await sendKeys({ up: 'Shift' });
      expect(grid.$server.setShiftKeyDown).to.be.not.called;
    });

    it('should set shift key flag on server when deselecting with Shift', async () => {
      await mouseClick(selectRowCheckboxes[0]);

      await sendKeys({ down: 'Shift' });
      expect(grid.$server.setShiftKeyDown).to.be.not.called;

      await mouseClick(selectRowCheckboxes[1]);
      expect(grid.$server.setShiftKeyDown).to.be.calledOnce;
      expect(grid.$server.setShiftKeyDown).to.be.calledWith(true);
      expect(grid.$server.setShiftKeyDown).to.be.calledBefore(grid.$server.deselect);

      grid.$server.setShiftKeyDown.resetHistory();

      await sendKeys({ up: 'Shift' });
      expect(grid.$server.setShiftKeyDown).to.be.not.called;
    });

    it('should select all items on server', async () => {
      await mouseClick(selectAllCheckbox);
      expect(grid.$server.selectAll).to.be.calledOnce;
    });

    it('should deselect all items on server', async () => {
      selectionColumn.selectAll = true;
      await mouseClick(selectAllCheckbox);
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

    it('should not have duplicates in selectedItems after same item selection', () => {
      grid.$connector.doSelection([{ key: '0' }], false);
      grid.$connector.doSelection([{ key: '0' }], false);
      expect(grid.selectedItems).to.have.lengthOf(1);
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
