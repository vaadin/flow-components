import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { sendKeys } from '@web/test-runner-commands';
import './env-setup.js';
import '@vaadin/grid-pro/src/vaadin-grid-pro.js';
import '@vaadin/grid-pro/src/vaadin-grid-pro-edit-column.js';
import '../frontend/generated/jar-resources/gridProConnector.js';
import type {} from '@web/test-runner-mocha';

const gridProConnector = (window.Vaadin.Flow as any).gridProConnector;

describe('grid-pro connector', () => {
  let grid: any;
  let editor: HTMLInputElement;

  function getBodyCell(rowIndex: number, columnIndex: number): HTMLElement {
    const row = grid.$.items.querySelector(`tr:nth-child(${rowIndex + 1})`);
    return row.querySelectorAll('.body-cell')[columnIndex];
  }

  function hasUpdatingCellPart(cell: HTMLElement): boolean {
    const target = (cell as any)._focusButton || cell;
    return target.part.contains('updating-cell');
  }

  describe('updating cell animation', () => {
    beforeEach(async () => {
      grid = fixtureSync(`
        <vaadin-grid-pro>
          <vaadin-grid-pro-edit-column path="name" editor-type="custom"></vaadin-grid-pro-edit-column>
          <vaadin-grid-pro-edit-column path="age"></vaadin-grid-pro-edit-column>
        </vaadin-grid-pro>
      `);
      grid.items = [
        { key: '0', name: 'foo', age: 1 },
        { key: '1', name: 'bar', age: 2 }
      ];
      const column = grid.querySelector('[path="name"]');
      editor = document.createElement('input');
      gridProConnector.setEditModeRenderer(column, editor);
      gridProConnector.initUpdatingCellAnimation(grid);
      await nextFrame();
    });

    async function editCustomCell(rowIndex: number) {
      getBodyCell(rowIndex, 0).dispatchEvent(new CustomEvent('dblclick', { bubbles: true, composed: true }));
      await nextFrame();
      // Simulate the server setting the editor value
      editor.value = 'changed';
      gridProConnector.handleCustomEditorValueUpdate(editor, grid.items[rowIndex].key, grid);
      await sendKeys({ press: 'Enter' });
      await nextFrame();
    }

    it('should add the updating-cell part to the edited cell until the update is done', async () => {
      await editCustomCell(0);
      expect(hasUpdatingCellPart(getBodyCell(0, 0))).to.be.true;

      gridProConnector.clearUpdatingCell(grid);
      expect(hasUpdatingCellPart(getBodyCell(0, 0))).to.be.false;
    });

    it('should not add the updating-cell part to other cells', async () => {
      await editCustomCell(0);
      expect(hasUpdatingCellPart(getBodyCell(0, 1))).to.be.false;
      expect(hasUpdatingCellPart(getBodyCell(1, 0))).to.be.false;
    });

    it('should keep the updating-cell part when the rows are re-rendered', async () => {
      await editCustomCell(0);
      grid.requestContentUpdate();
      expect(hasUpdatingCellPart(getBodyCell(0, 0))).to.be.true;
    });

    it('should not add the updating-cell part to loading rows', async () => {
      await editCustomCell(0);
      grid.dataProvider = () => {};
      expect(hasUpdatingCellPart(getBodyCell(0, 0))).to.be.false;
    });
  });
});
