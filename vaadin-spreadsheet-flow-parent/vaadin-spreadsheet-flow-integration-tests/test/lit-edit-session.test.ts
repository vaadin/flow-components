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
import { EditSession } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/EditSession.js';
import { FormulaPointer } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/FormulaPointer.js';
import { SelectionReporter } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/SelectionReporter.js';
import { selectSingleCell } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';
import type { SpreadsheetEvent } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * Editing cells, on its own: opening the cell editor, committing and
 * throwing away in it and in the formula field, the server's values in the
 * field, and a note.
 */
describe('EditSession', () => {
  const FORMULA_BAR = new EventTarget();
  const CELL_GRID = new EventTarget();
  let store: SheetStore;
  let sent: SpreadsheetEvent[];
  let fieldFocused: boolean;
  let fieldEdited: boolean;
  let keyboardReturned: number;
  let movedInside: Array<{ col: number; row: number }>;
  let scrolled: Array<{ col: number; row: number }>;
  let renders: number;
  let edit: EditSession;

  const types = (): string[] => sent.map((e) => e.type);

  function cell(col: number, row: number, data: Partial<CellData> = {}): void {
    store.sheetData.cells.set(toKey(col, row), { col, row, value: null, cellStyle: 'cs0', ...data });
  }

  function commitEvent(detail: Record<string, unknown>): CustomEvent<any> {
    return new CustomEvent('cell-editor-commit', { detail, cancelable: true });
  }

  beforeEach(() => {
    store = new SheetStore(() => {});
    store.write('rows', 20);
    store.write('cols', 10);
    store.select(selectSingleCell(2, 2));
    sent = [];
    fieldFocused = false;
    fieldEdited = false;
    keyboardReturned = 0;
    movedInside = [];
    scrolled = [];
    renders = 0;
    const report = new SelectionReporter(store, { send: (e) => sent.push(e), selectedCellReported: () => {} });
    const pointing = new FormulaPointer(store, {
      fieldHasFocus: () => fieldFocused,
      sheetName: () => 'Sheet1',
      scrollIntoView: () => {}
    });
    edit = new EditSession(store, report, pointing, {
      fieldHasFocus: () => fieldFocused,
      fieldEdited: () => fieldEdited,
      isFormulaBar: (target) => target === FORMULA_BAR,
      isCellGrid: (target) => target === CELL_GRID,
      handle: (work) => work(),
      returnFocusToSheet: () => keyboardReturned++,
      moveInsideSelection: (moved) => movedInside.push(moved),
      scrollIntoView: (to) => scrolled.push(to),
      scheduleRender: () => renders++,
      send: (e) => sent.push(e)
    });
  });

  describe('opening the cell editor', () => {
    it('opens on what the cell holds, to change it', () => {
      cell(2, 2, { value: '1.23', originalValue: '1.2345' });

      expect(edit.openExisting(2, 2)).to.be.true;

      expect(store.editor).to.eql({ col: 2, row: 2, initialValue: '1.2345', mode: 'edit' });
      expect(store.writing).to.eql({ text: '1.2345', caret: 6, source: 'cell' });
    });

    it('opens on the key typed over the cell, an empty one on Space, and "5%" on a digit in a percentage cell', () => {
      expect(edit.openTyping(2, 2, 'a')).to.be.true;
      expect(store.editor).to.eql({ col: 2, row: 2, initialValue: 'a', mode: 'enter' });

      edit.editorCancelled();
      edit.openTyping(2, 2, ' ');
      expect(store.editor?.initialValue).to.equal('');

      edit.editorCancelled();
      cell(2, 2, { value: '10%', isPercentage: true });
      edit.openTyping(2, 2, '5');
      expect(store.editor?.initialValue).to.equal('5%');
      const opened = store.interaction.edit;
      expect(opened.kind === 'editing' && opened.autoPercent).to.be.true;
    });

    it('brings the cell into view first, as the editor is drawn over it, and a locked cell not', () => {
      // Scrolled out of view, a cell is not drawn, and neither was the
      // editor: the keys typed after the first went nowhere.
      edit.openTyping(2, 2, 'a');
      edit.editorCancelled();
      edit.openExisting(3, 4);
      expect(scrolled).to.eql([
        { col: 2, row: 2 },
        { col: 3, row: 4 }
      ]);

      edit.editorCancelled();
      store.write('sheetProtected', true);
      cell(5, 5, { locked: true });
      edit.openTyping(5, 5, 'a');
      edit.openExisting(5, 5);
      expect(scrolled).to.have.length(2);
    });

    it('opens nothing on a locked cell, and tells the server it was tried', () => {
      store.write('sheetProtected', true);
      cell(2, 2, { locked: true });

      expect(edit.openExisting(2, 2)).to.be.false;
      expect(edit.openTyping(2, 2, 'a')).to.be.false;

      expect(store.editor).to.equal(null);
      expect(types()).to.eql(['protectedCellWriteAttempted']);
    });
  });

  describe('the cell editor', () => {
    it('commits on Enter: the value shows at once, in the cell and the field, and the selection moves down', () => {
      cell(2, 2, { value: 'old', cellStyle: 'cs3' });
      edit.openTyping(2, 2, 'n');

      edit.editorCommitted(commitEvent({ col: 2, row: 2, value: 'new', key: 'Enter', shiftKey: false }));

      expect(store.editor).to.equal(null);
      expect(store.sheetData.cells.get(toKey(2, 2))).to.include({ value: 'new', cellStyle: 'cs3' });
      expect(store.formulaBar.fieldText).to.equal('new');
      expect(store.selection.activeCell).to.eql({ col: 2, row: 3 });
      expect(types()).to.eql(['cellValueEdited', 'cellSelected']);
      expect(sent[0].data).to.eql({ row: 2, col: 2, value: 'new' });
      expect(keyboardReturned).to.equal(1);
    });

    it('keeps the old value shown while the server works out a committed formula', () => {
      cell(2, 2, { value: 'old' });
      edit.openTyping(2, 2, '=');

      edit.editorCommitted(commitEvent({ col: 2, row: 2, value: '=1+2', key: 'Enter' }));

      expect(store.sheetData.cells.get(toKey(2, 2))?.value).to.equal('old');
    });

    it('commits in place on leaving it, and nothing for an editor already closed', () => {
      edit.openTyping(2, 2, 'x');

      edit.editorCommitted(commitEvent({ col: 3, row: 3, value: 'x' }));
      expect(sent, 'another cell').to.eql([]);

      edit.editorCommitted(commitEvent({ col: 2, row: 2, value: 'x' }));
      expect(types()).to.eql(['cellValueEdited']);
      expect(store.selection.activeCell).to.eql({ col: 2, row: 2 });
    });

    it('walks inside a range selected, as Enter does with nothing edited', () => {
      store.select({ activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, r1: 2, c2: 3, r2: 3 }, auxRanges: [] });
      edit.openTyping(2, 2, 'x');

      edit.editorCommitted(commitEvent({ col: 2, row: 2, value: 'x', key: 'Enter' }));

      expect(movedInside).to.eql([{ col: 2, row: 3 }]);
    });

    it('carries the edit on into the formula field, and commits it once the keyboard settles in the address field', async () => {
      edit.openTyping(2, 2, 'a');
      store.changeWriting({ text: 'abc', caret: 3, source: 'cell' });
      const left = commitEvent({ col: 2, row: 2, value: 'ab', relatedTarget: FORMULA_BAR });

      edit.editorCommitted(left);
      expect(left.defaultPrevented).to.be.true;
      expect(sent, 'nothing yet').to.eql([]);
      await new Promise((resolve) => setTimeout(resolve));

      expect(sent).to.eql([{ type: 'cellValueEdited', data: { row: 2, col: 2, value: 'abc' } }]);
      expect(keyboardReturned, 'the keyboard is where the user put it').to.equal(0);
    });

    it('leaves the edit open while the formula field has the keyboard', async () => {
      edit.openTyping(2, 2, 'a');
      edit.editorCommitted(commitEvent({ col: 2, row: 2, value: 'a', relatedTarget: FORMULA_BAR }));
      fieldFocused = true;
      await new Promise((resolve) => setTimeout(resolve));

      expect(sent).to.eql([]);
      expect(store.editor).to.not.equal(null);
    });

    it('throws an edit away on Escape: nothing is sent and the field shows the cell again', () => {
      cell(2, 2, { value: 'old' });
      store.showInFormulaField('old');
      edit.openTyping(2, 2, 'n');

      edit.editorCancelled();

      expect(store.editor).to.equal(null);
      expect(sent).to.eql([]);
      expect(store.formulaBar.fieldText).to.equal('old');
      expect(keyboardReturned).to.equal(1);
    });

    it('shows the cell as it is now once an edit is thrown away, a value the server sent meanwhile included', () => {
      cell(2, 2, { value: 'old' });
      store.showInFormulaField('old');
      edit.openTyping(2, 2, 'n');
      cell(2, 2, { value: 'fresh' });

      edit.editorCancelled();

      expect(store.formulaBar.fieldText).to.equal('fresh');
    });
  });

  describe('the formula field', () => {
    it('commits into the selected cell, and Enter moves down', () => {
      edit.fieldCommitted({ value: 'new', key: 'Enter', shiftKey: false });

      expect(types()).to.eql(['cellValueEdited', 'cellSelected']);
      expect(store.sheetData.cells.get(toKey(2, 2))?.value).to.equal('new');
      expect(store.formulaBar.fieldText).to.equal('new');
      expect(store.selection.activeCell).to.eql({ col: 2, row: 3 });
      expect(scrolled).to.eql([{ col: 2, row: 3 }]);
    });

    it('commits in place on Shift+Tab and on leaving it', () => {
      edit.fieldCommitted({ value: 'a', key: 'Tab', shiftKey: true });
      edit.fieldCommitted({ value: 'b', shiftKey: false });

      expect(types()).to.eql(['cellValueEdited', 'cellValueEdited']);
      expect(store.selection.activeCell).to.eql({ col: 2, row: 2 });
      expect(keyboardReturned).to.equal(0);
    });

    it('closes an edit started in the cell as it commits', () => {
      edit.openTyping(2, 2, 'a');

      edit.fieldCommitted({ value: 'abc', key: 'Enter', shiftKey: false });

      expect(store.editor).to.equal(null);
    });

    it('commits nothing back to the cell editor the edit came from, on a locked cell, or on a sheet with no cells', () => {
      edit.openTyping(2, 2, 'a');
      edit.fieldCommitted({ value: 'a', shiftKey: false, relatedTarget: CELL_GRID });
      expect(sent, 'back to the cell editor').to.eql([]);
      edit.editorCancelled();

      store.write('sheetProtected', true);
      cell(2, 2, { locked: true });
      edit.fieldCommitted({ value: 'a', key: 'Enter', shiftKey: false });
      expect(types(), 'locked').to.eql(['protectedCellWriteAttempted']);

      store.write('sheetProtected', false);
      store.write('rows', 0);
      edit.fieldCommitted({ value: 'a', key: 'Enter', shiftKey: false });
      expect(types(), 'no cells').to.eql(['protectedCellWriteAttempted']);
    });

    it('puts the cell back on Escape, closing an edit started in the cell', () => {
      cell(2, 2, { value: 'old' });
      edit.openTyping(2, 2, 'n');

      edit.fieldCancelled();

      expect(store.formulaBar.fieldText).to.equal('old');
      expect(store.editor).to.equal(null);
      expect(keyboardReturned).to.equal(1);
    });

    it('shows what the server says of a cell, unless the user is writing there', () => {
      edit.showServerValue('from the server', false);
      expect(store.formulaBar.fieldText).to.equal('from the server');

      fieldEdited = true;
      edit.showServerValue('again', true);
      expect(store.formulaBar.fieldText, 'typed into, for the same cell').to.equal('from the server');
      edit.showServerValue('another cell', false);
      expect(store.formulaBar.fieldText).to.equal('another cell');

      edit.openTyping(2, 2, 'a');
      edit.showServerValue('while editing', false);
      expect(store.formulaBar.fieldText, 'the cell editor open').to.equal('another cell');
    });

    it('ends a formula written in the field alone when the server shows another value', () => {
      fieldFocused = true;
      store.changeWriting({ text: '=A1+', caret: 4, source: 'field' });

      edit.showServerValue('5', false);

      expect(store.draft).to.equal(null);
      expect(store.interaction.edit.kind).to.equal('idle');
    });
  });

  describe('a note', () => {
    it('shows what was typed at once and tells the server, once', () => {
      store.interact({ ...store.interaction, comment: toKey(3, 4) });

      edit.noteCommitted({ key: toKey(3, 4), text: 'Check this' });
      edit.noteCommitted({ key: toKey(3, 4), text: 'Check this' });

      expect(store.model.cellComments[toKey(3, 4)]).to.equal('Check this');
      expect(sent).to.eql([{ type: 'updateCellComment', data: { text: 'Check this', col: 3, row: 4 } }]);
      expect(keyboardReturned).to.equal(1);
    });

    it('tells the server nothing for a note of no cell', () => {
      store.interact({ ...store.interaction, comment: 'nowhere' });

      edit.noteCommitted({ key: 'nowhere', text: 'x' });

      expect(sent).to.eql([]);
      expect(store.interaction.comment).to.equal(null);
    });
  });
});
