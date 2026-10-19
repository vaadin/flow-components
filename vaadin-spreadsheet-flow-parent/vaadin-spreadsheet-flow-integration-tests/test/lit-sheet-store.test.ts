/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { toKey } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/CellKeys.js';
import { IDLE, openEditor } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/Interaction.js';
import { selectSingleCell } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * What changes as the sheet is used, on its own: each change replaces its
 * part whole, the client's own changes are drawn and the server's wait for
 * their batch, and what is worked out from the parts.
 */
describe('SheetStore', () => {
  /** B2:C4. */
  const merge = { col1: 2, col2: 3, row1: 2, row2: 4 };
  let renders: number;
  let store: SheetStore;

  function cell(col: number, row: number, data: Partial<CellData> = {}): void {
    store.sheetData.cells.set(toKey(col, row), { col, row, value: null, cellStyle: 'cs0', ...data });
  }

  function openEditorOn(col: number, row: number): void {
    store.changeEdit(openEditor(store.interaction.edit, { col, row, initialValue: '', mode: 'edit' }, false));
  }

  beforeEach(() => {
    renders = 0;
    store = new SheetStore(() => renders++);
  });

  it('starts with A1 selected, nothing under way and no cells', () => {
    expect(store.selection.activeCell).to.eql({ col: 1, row: 1 });
    expect(store.mergeEntry).to.equal(null);
    expect(store.interaction).to.equal(IDLE);
    expect(store.editor).to.equal(null);
    expect(store.writing).to.equal(null);
    expect(store.draft).to.equal(null);
    expect(store.sheetData.cells.size).to.equal(0);
  });

  describe('the model', () => {
    it('takes a property from the server as a new model, drawn with its batch rather than at once', () => {
      const before = store.model;

      store.write('rows', 50);

      expect(store.model.rows).to.equal(50);
      expect(store.model).to.not.equal(before);
      expect(before.rows).to.equal(0);
      expect(renders).to.equal(0);
    });

    it("takes the client's own values and has them drawn", () => {
      store.change({ rows: 7, cols: 3 });

      expect([store.model.rows, store.model.cols]).to.eql([7, 3]);
      expect(renders).to.equal(1);
    });

    it('has cells once the server has sent both the rows and the columns', () => {
      expect(store.hasCells(), 'nothing sent').to.be.false;
      store.write('rows', 5);
      expect(store.hasCells(), 'rows only').to.be.false;
      store.write('cols', 3);
      expect(store.hasCells()).to.be.true;
      store.write('rows', 0);
      expect(store.hasCells(), 'columns only').to.be.false;
    });

    it('gives the arrow keys the size, the merges and the hidden rows and columns', () => {
      store.write('rows', 20);
      store.write('cols', 8);
      store.write('mergedRegions', [merge]);
      store.write('hiddenColumnIndexes', [4]);
      store.write('hiddenRowIndexes', [6]);

      const shape = store.sheetShape();

      expect([shape.rows, shape.cols, shape.mergedRegions]).to.eql([20, 8, [merge]]);
      expect([...shape.hiddenColumns, ...shape.hiddenRows]).to.eql([4, 6]);
    });

    it('draws a hidden column and row as nothing, and others at the default size past those sized', () => {
      store.write('hiddenColumnIndexes', [2]);
      store.write('hiddenRowIndexes', [3]);
      store.write('defColW', 80);

      expect([store.colWidthPx(2), store.colWidthPx(5)]).to.eql([0, 80]);
      // The default row height is 15 points: 20 pixels at 96 to the inch.
      expect([store.rowHeightPx(3), store.rowHeightPx(4), store.rowHeightPx(4, 72)]).to.eql([0, 20, 15]);
    });
  });

  describe('the selection', () => {
    it('selects the whole merge a cell is in, entered by that cell, and has it drawn', () => {
      store.write('mergedRegions', [merge]);

      store.selectCellOrMerge(3, 3);

      expect(store.selection.activeCell).to.eql({ col: 2, row: 2 });
      expect(store.selection.activeRange).to.eql({ c1: 2, c2: 3, r1: 2, r2: 4 });
      expect(store.mergeEntry).to.eql({ col: 3, row: 3 });
      expect(renders).to.equal(1);
    });

    it('selects other cells with the merge still entered by the same cell', () => {
      store.write('mergedRegions', [merge]);
      store.selectCellOrMerge(3, 3);

      store.select(selectSingleCell(5, 5));

      expect(store.sheetSelection.cells.activeCell).to.eql({ col: 5, row: 5 });
      expect(store.mergeEntry).to.eql({ col: 3, row: 3 });
      expect(renders).to.equal(2);
    });

    it('picks a cell with no merge entered, and has it drawn', () => {
      store.write('mergedRegions', [merge]);
      store.selectCellOrMerge(3, 3);

      store.pick({ activeCell: { col: 4, row: 6 }, activeRange: { c1: 4, c2: 4, r1: 6, r2: 6 }, entry: null });

      expect(store.selection.activeCell).to.eql({ col: 4, row: 6 });
      expect(store.mergeEntry).to.equal(null);
      expect(renders).to.equal(2);
    });

    it('selects a range only with more than one cell, and more than the merge the active cell is in', () => {
      store.write('mergedRegions', [merge]);
      store.selectCellOrMerge(3, 3);
      expect(store.selectsRange(), 'the merge').to.be.false;

      store.select({ activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, c2: 5, r1: 2, r2: 4 }, auxRanges: [] });
      expect(store.selectsRange(), 'past the merge').to.be.true;

      store.select(selectSingleCell(7, 7));
      expect(store.selectsRange(), 'one cell').to.be.false;
    });

    it('grows a range to take in whole merges, whichever way it is drawn', () => {
      store.write('mergedRegions', [merge]);

      expect(store.rangeTo({ col: 1, row: 1 }, 2, 2)).to.eql({ c1: 1, c2: 3, r1: 1, r2: 4 });
      expect(store.rangeTo({ col: 5, row: 5 }, 4, 3)).to.eql({ c1: 4, c2: 5, r1: 3, r2: 5 });
    });
  });

  describe('what the user is in the middle of', () => {
    it('opens the editor and has it drawn', () => {
      openEditorOn(2, 3);

      expect([store.editor?.col, store.editor?.row]).to.eql([2, 3]);
      expect(renders).to.equal(1);
    });

    it('changes what is written, the editor kept, and the formula being written with it', () => {
      openEditorOn(2, 3);

      store.changeWriting({ text: '=A', caret: 2, source: 'cell' });

      expect(store.writing).to.eql({ text: '=A', caret: 2, source: 'cell' });
      expect(store.draft).to.equal(store.writing);
      expect(store.editor?.col).to.equal(2);
      expect(renders).to.equal(2);
    });

    it('changes what is pointed at, the editor kept', () => {
      openEditorOn(2, 3);

      store.changeFormula({ lastPointed: { col: 1, row: 1 } });

      expect(store.formula.lastPointed).to.eql({ col: 1, row: 1 });
      expect(store.editor?.col).to.equal(2);
      expect(renders).to.equal(2);
    });

    it('paints the strip of a fill drag, and nothing for any other press', () => {
      const from = { c1: 1, c2: 1, r1: 1, r2: 1 };
      const painted = { c1: 1, c2: 1, r1: 2, r2: 5 };

      store.changePress({ kind: 'select', crossed: false });
      expect(store.paintRect(), 'a selection').to.equal(null);
      store.changePress({ kind: 'fill', from, outcome: { kind: 'increase', painted } });
      expect(store.paintRect(), 'a fill').to.eql(painted);
      store.changePress({ kind: 'fill', from, outcome: { kind: 'none' } });
      expect(store.paintRect(), 'a fill that paints nothing').to.equal(null);
      expect(renders).to.equal(3);
    });

    it('drags over the cells while a press selects, fills or points at cells for a formula', () => {
      expect(store.dragging(), 'nothing pressed').to.be.false;
      store.changePress({ kind: 'select', crossed: false });
      expect(store.dragging(), 'a selection').to.be.true;
      store.changePress({ kind: 'fill', from: { c1: 1, c2: 1, r1: 1, r2: 1 }, outcome: { kind: 'none' } });
      expect(store.dragging(), 'a fill').to.be.true;
      store.changePress(null);
      openEditorOn(2, 3);
      expect(store.dragging(), 'an edit').to.be.false;
      store.changeFormula({ press: 'pointing' });
      expect(store.dragging(), 'pointing for a formula').to.be.true;
      store.changeFormula({ press: 'pointed' });
      expect(store.dragging(), 'pointed, the button up').to.be.false;
    });

    it('is used by touch or not, and fills by touch once Fill is chosen, drawing each change', () => {
      expect(store.interaction.touch).to.equal('off');

      store.useTouch('select');
      expect(store.interaction.touch).to.equal('select');
      store.useTouch('select');
      expect(renders, 'the same again draws nothing').to.equal(1);
      store.useTouch('fill');
      expect(store.interaction.touch).to.equal('fill');
      expect(renders).to.equal(2);
    });

    it('ends everything, and has that drawn', () => {
      openEditorOn(2, 3);
      store.changePress({ kind: 'select', crossed: true });

      store.interact(IDLE);

      expect(store.interaction).to.equal(IDLE);
      expect(store.editor).to.equal(null);
      expect(renders).to.equal(3);
    });
  });

  describe('what the formula bar shows', () => {
    it('starts with no name for the selection, nothing in the formula field and no named ranges', () => {
      expect(store.formulaBar).to.eql({ selectionName: null, fieldText: '', namedRanges: [] });
    });

    it('takes the name the server gave the selection, and its taking back, and has them drawn', () => {
      store.nameSelection('Sales');
      expect(store.formulaBar.selectionName).to.equal('Sales');

      store.nameSelection(null);
      expect(store.formulaBar.selectionName).to.equal(null);
      expect(renders).to.equal(2);
    });

    it('shows a text in the formula field again, the same text included', () => {
      store.showInFormulaField('=A1');
      const first = store.formulaBar;
      store.showInFormulaField('=A1');

      expect(store.formulaBar.fieldText).to.equal('=A1');
      expect(store.formulaBar, 'replaced, to be written again').to.not.equal(first);
      expect(renders).to.equal(2);
    });

    it('takes the named ranges, and has them drawn', () => {
      store.setNamedRanges(['Sales', 'Costs']);

      expect(store.formulaBar.namedRanges).to.eql(['Sales', 'Costs']);
      expect(renders).to.equal(1);
    });

    it('marks a range reported on a release until the click takes it, asking for no drawing when nothing changes', () => {
      store.markRangeReportedOnRelease(true);
      expect(store.interaction.rangeReportedOnRelease).to.be.true;

      store.markRangeReportedOnRelease(true);
      store.markRangeReportedOnRelease(false);
      expect(store.interaction.rangeReportedOnRelease).to.be.false;
      expect(renders).to.equal(2);
    });

    it('marks the selection grown with Shift held, and let go, and has it drawn; ending everything lets go too', () => {
      store.extendSelection(true);
      expect(store.interaction.extending).to.be.true;

      store.interact(IDLE);
      expect(store.interaction.extending).to.be.false;
      expect(renders).to.equal(2);
    });
  });

  describe('what the client knows of the sheet shown', () => {
    it('starts over on a reset', () => {
      const before = store.sheetData;
      cell(1, 1, { value: 'x' });
      store.sheetData.serverSelectedCell = { col: 1, row: 1 };

      store.resetSheetData();

      expect(store.sheetData).to.not.equal(before);
      expect(store.sheetData.cells.size).to.equal(0);
      expect(store.sheetData.serverSelectedCell).to.equal(null);
    });

    it('shows a cell in the formula field by its formula, else its value as entered, else its value', () => {
      cell(1, 1, { value: '42', formulaValue: 'B1*2' });
      cell(1, 2, { value: '0.12', originalValue: '0.123456' });
      cell(1, 3, { value: 'abc' });
      cell(1, 4, { value: '42', formulaValue: '' });

      expect(store.formulaFieldValueFor(1, 1)).to.equal('=B1*2');
      expect(store.formulaFieldValueFor(1, 2)).to.equal('0.123456');
      expect(store.formulaFieldValueFor(1, 3)).to.equal('abc');
      expect(store.formulaFieldValueFor(1, 4), 'an empty formula').to.equal('42');
      expect(store.formulaFieldValueFor(1, 5), 'no data').to.equal('');
      cell(1, 6);
      expect(store.formulaFieldValueFor(1, 6), 'no value').to.equal('');
    });

    it('locks nothing on a sheet that is not protected', () => {
      cell(1, 1, { locked: true });

      expect(store.isCellLockedForEdit(1, 1)).to.be.false;
    });

    it("locks a cell of a protected sheet by its own flag, and an empty one by both its column's and its row's", () => {
      store.write('sheetProtected', true);
      store.write('lockedColumnIndexes', [2, 4]);
      store.write('lockedRowIndexes', [3]);
      cell(1, 1, { locked: true });
      cell(1, 2, { locked: false });
      cell(4, 3, { locked: false });

      expect(store.isCellLockedForEdit(1, 1), 'locked').to.be.true;
      expect(store.isCellLockedForEdit(1, 2), 'unlocked').to.be.false;
      expect(store.isCellLockedForEdit(4, 3), 'its own flag over its column and row').to.be.false;
      expect(store.isCellLockedForEdit(2, 3), 'empty, its column and row locked').to.be.true;
      expect(store.isCellLockedForEdit(2, 4), 'empty, only its column locked').to.be.false;
      expect(store.isCellLockedForEdit(5, 3), 'empty, only its row locked').to.be.false;
    });
  });
});
