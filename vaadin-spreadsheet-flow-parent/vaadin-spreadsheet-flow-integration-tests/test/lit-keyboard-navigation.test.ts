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
import { KeyboardNavigation } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/KeyboardNavigation.js';
import { SelectionReporter } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/SelectionReporter.js';
import { selectSingleCell } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';
import type { SpreadsheetEvent } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';

/**
 * The keyboard on the sheet, on its own: moving and growing the selection,
 * the shortcuts, the keys that start an edit, and the keys that belong to
 * something else - a custom editor, a button, leaving the sheet.
 */
describe('KeyboardNavigation', () => {
  let store: SheetStore;
  let sent: SpreadsheetEvent[];
  let insideHosted: boolean;
  let insideEditor: boolean;
  let editorAt: { col: number; row: number } | null;
  let editorFocused: number;
  let exits: boolean;
  let sheetFocused: number;
  let mac: boolean;
  let scrolled: Array<{ col: number; row: number }>;
  let keyboard: KeyboardNavigation;

  const types = (): string[] => sent.map((e) => e.type);
  const active = () => store.selection.activeCell;

  /** Presses a key on the sheet, or on the element given, where the key goes to it. */
  function key(name: string, init: KeyboardEventInit = {}, target?: HTMLElement): KeyboardEvent {
    const e = new KeyboardEvent('keydown', { key: name, cancelable: true, bubbles: true, composed: true, ...init });
    if (target === undefined) {
      keyboard.keyDown(e);
      return e;
    }
    const listener = (event: Event): void => keyboard.keyDown(event as KeyboardEvent);
    document.addEventListener('keydown', listener);
    document.body.append(target);
    target.dispatchEvent(e);
    target.remove();
    document.removeEventListener('keydown', listener);
    return e;
  }

  beforeEach(() => {
    store = new SheetStore(() => {});
    store.write('rows', 20);
    store.write('cols', 10);
    store.select(selectSingleCell(2, 2));
    sent = [];
    insideHosted = false;
    insideEditor = false;
    editorAt = null;
    editorFocused = 0;
    exits = false;
    sheetFocused = 0;
    mac = false;
    scrolled = [];
    const report = new SelectionReporter(store, { send: (e) => sent.push(e), selectedCellReported: () => {} });
    const pointing = new FormulaPointer(store, {
      fieldHasFocus: () => false,
      sheetName: () => 'Sheet1',
      scrollIntoView: () => {}
    });
    const edit = new EditSession(store, report, pointing, {
      fieldHasFocus: () => false,
      fieldEdited: () => false,
      isFormulaBar: () => false,
      isCellGrid: () => false,
      handle: (work) => work(),
      returnFocusToSheet: () => {},
      moveInsideSelection: (cell) => keyboard.moveInsideSelection(cell),
      scrollIntoView: () => {},
      scheduleRender: () => {},
      send: (e) => sent.push(e)
    });
    keyboard = new KeyboardNavigation(store, report, edit, {
      raisedInside: () => insideHosted,
      raisedInEditor: () => insideEditor,
      focusEditorOf: (col, row) => {
        if (editorAt?.col === col && editorAt.row === row) {
          editorFocused++;
          return true;
        }
        return false;
      },
      exits: () => exits,
      focusSheet: () => sheetFocused++,
      isMac: () => mac,
      scrollIntoView: (cell) => scrolled.push(cell),
      send: (e) => sent.push(e)
    });
  });

  describe('moving', () => {
    it('moves the active cell with the arrows and Tab, bringing it into view', () => {
      const e = key('ArrowRight');
      key('ArrowDown');
      key('Tab');
      key('Tab', { shiftKey: true });

      expect(e.defaultPrevented).to.be.true;
      expect(active()).to.eql({ col: 3, row: 3 });
      expect(types()).to.eql(['cellSelected', 'cellSelected', 'cellSelected', 'cellSelected']);
      expect(scrolled).to.eql([
        { col: 3, row: 2 },
        { col: 3, row: 3 },
        { col: 4, row: 3 },
        { col: 3, row: 3 }
      ]);
    });

    it('steps over hidden rows, and stays put at the edge of the sheet', () => {
      store.write('hiddenRowIndexes', [3]);
      key('ArrowDown');
      expect(active()).to.eql({ col: 2, row: 4 });

      store.select(selectSingleCell(1, 1));
      sent.length = 0;
      key('ArrowLeft');
      expect(active()).to.eql({ col: 1, row: 1 });
      expect(sent).to.eql([]);
    });

    it('grows the range from the active cell with Shift and an arrow, counting it out', () => {
      key('ArrowRight', { shiftKey: true });
      key('ArrowDown', { shiftKey: true });

      expect(store.selection.activeRange).to.eql({ c1: 2, c2: 3, r1: 2, r2: 3 });
      expect(active()).to.eql({ col: 2, row: 2 });
      expect(store.interaction.extending).to.be.true;
      // The side that moved, in the active cell's column or row.
      expect(scrolled).to.eql([
        { col: 3, row: 2 },
        { col: 2, row: 3 }
      ]);
    });

    it('lets a plain move end the counting, and Shift let go', () => {
      key('ArrowRight', { shiftKey: true });
      key('ArrowDown');
      expect(store.interaction.extending, 'a plain arrow').to.be.false;

      key('ArrowRight', { shiftKey: true });
      keyboard.keyUp(new KeyboardEvent('keyup', { key: 'a' }));
      expect(store.interaction.extending, 'another key let go').to.be.true;
      keyboard.keyUp(new KeyboardEvent('keyup', { key: 'Shift' }));
      expect(store.interaction.extending).to.be.false;
    });

    it('walks the active cell inside a range selected with Tab and Enter, keeping the range', () => {
      store.select({ activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, c2: 3, r1: 2, r2: 3 }, auxRanges: [] });

      key('Tab');
      expect(active()).to.eql({ col: 3, row: 2 });
      key('Enter');

      expect(store.selection.activeRange).to.eql({ c1: 2, c2: 3, r1: 2, r2: 3 });
      // Tab along the row, Enter down the column.
      expect(sent.map((e) => e.data)).to.eql([
        { row: 2, column: 3, oldSelectionRangeDiscarded: false },
        { row: 3, column: 3, oldSelectionRangeDiscarded: false }
      ]);
    });

    it('goes along the row with Home and End, and to the first and last cell with Ctrl', () => {
      key('End');
      expect(active()).to.eql({ col: 10, row: 2 });
      key('Home', { ctrlKey: true });
      expect(active()).to.eql({ col: 1, row: 1 });
      key('End', { metaKey: true });
      expect(active()).to.eql({ col: 10, row: 20 });
    });
  });

  describe('the shortcuts', () => {
    it('selects the whole sheet with Ctrl+A, from A1', () => {
      const e = key('a', { ctrlKey: true });

      expect(e.defaultPrevented).to.be.true;
      expect(store.selection.activeRange).to.eql({ c1: 1, c2: 10, r1: 1, r2: 20 });
      expect(active()).to.eql({ col: 1, row: 1 });
      expect(types()).to.eql(['cellRangePainted']);
      expect(sent[0].data).to.include({ selectedCellRow: 1, selectedCellColumn: 1 });
    });

    it('takes Cmd for the shortcuts on a Mac, and leaves Ctrl+A to type an a', () => {
      mac = true;

      key('A', { metaKey: true });
      expect(store.selection.activeRange, 'Cmd+A').to.eql({ c1: 1, c2: 10, r1: 1, r2: 20 });

      store.select(selectSingleCell(2, 2));
      key('a', { ctrlKey: true });
      expect(store.selection.activeRange, 'Ctrl+A').to.eql({ c1: 2, c2: 2, r1: 2, r2: 2 });
    });

    it('asks the server to undo with Ctrl+Z, and to redo with Ctrl+Y and Ctrl+Shift+Z', () => {
      key('z', { ctrlKey: true });
      key('Y', { ctrlKey: true });
      key('Z', { ctrlKey: true, shiftKey: true });

      expect(types()).to.eql(['onUndo', 'onRedo', 'onRedo']);
    });

    it('asks the server to clear the selection with Delete and Backspace', () => {
      key('Delete');
      key('Backspace');

      expect(types()).to.eql(['deleteSelectedCells', 'deleteSelectedCells']);
    });
  });

  describe('editing', () => {
    it('opens the cell editor on F2 and Enter, on what the cell holds', () => {
      store.sheetData.cells.set(toKey(2, 2), { col: 2, row: 2, value: 'x', cellStyle: 'cs0' });

      const e = key('F2');

      expect(e.defaultPrevented).to.be.true;
      expect(store.editor).to.eql({ col: 2, row: 2, initialValue: 'x', mode: 'edit' });
    });

    it('moves on with Enter on a cell the user cannot see', () => {
      store.write('hiddenColumnIndexes', [2]);
      key('Enter');
      expect(store.editor, 'hidden').to.equal(null);
      expect(active()).to.eql({ col: 2, row: 3 });
    });

    it('opens the cell editor on a character typed over the cell', () => {
      const e = key('q');

      expect(e.defaultPrevented).to.be.true;
      expect(store.editor).to.eql({ col: 2, row: 2, initialValue: 'q', mode: 'enter' });
    });

    it('gives the keys that start an edit to the custom editor of the cell, letting the character through', () => {
      editorAt = { col: 2, row: 2 };

      const f2 = key('F2');
      const typed = key('q');

      expect(editorFocused).to.equal(2);
      expect(store.editor).to.equal(null);
      expect(f2.defaultPrevented || typed.defaultPrevented).to.be.false;
    });
  });

  it('takes the keys on a sheet with no cells, doing nothing with them', () => {
    store.write('rows', 0);

    for (const name of ['ArrowDown', 'End', 'Delete', 'F2', 'q']) {
      expect(key(name).defaultPrevented, name).to.be.false;
    }
    key('a', { ctrlKey: true });

    expect(sent).to.eql([]);
    expect(store.editor).to.equal(null);
  });

  describe('keys that belong to something else', () => {
    it('leaves a key pressed inside a hosted component to it', () => {
      insideHosted = true;

      key('ArrowDown');
      key('q');

      expect(sent).to.eql([]);
      expect(store.editor).to.equal(null);
    });

    it('takes the keyboard back from a custom editor on Escape, and on Tab moves on as well', () => {
      insideHosted = true;
      insideEditor = true;

      key('Escape');
      expect(sheetFocused).to.equal(1);
      expect(sent).to.eql([]);

      const tab = key('Tab');
      expect(sheetFocused).to.equal(2);
      expect(tab.defaultPrevented).to.be.true;
      expect(active()).to.eql({ col: 3, row: 2 });
    });

    it('takes the keyboard back on Enter, unless the editor used it for a new line', () => {
      insideHosted = true;
      insideEditor = true;

      key('Enter', {}, document.createElement('textarea'));
      expect(sheetFocused, 'a new line').to.equal(0);

      key('Enter');
      expect(sheetFocused).to.equal(1);
      expect(active(), 'the cell stays selected').to.eql({ col: 2, row: 2 });
    });

    it('leaves the key to leaving the sheet', () => {
      exits = true;

      key('Tab');

      expect(sent).to.eql([]);
    });

    it('lets Enter and Space press a button of the sheet, editing nothing', () => {
      key('Enter', {}, document.createElement('button'));
      const roleButton = document.createElement('div');
      roleButton.setAttribute('role', 'button');
      key(' ', {}, roleButton);

      expect(store.editor).to.equal(null);
    });
  });
});
