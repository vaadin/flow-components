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
import { FormulaPointer } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/FormulaPointer.js';
import { PointerSelection } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/PointerSelection.js';
import { SelectionReporter } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/SelectionReporter.js';
import { openEditor } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/Interaction.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';
import type { SpreadsheetEvent } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';

/**
 * Selecting with the pointer, on its own: clicks, presses dragged out,
 * Shift and Ctrl with them, the column letters and row numbers, the fill
 * handle, and a press on a custom editor.
 */
describe('PointerSelection', () => {
  const NO_KEYS = { shiftKey: false, ctrlKey: false, metaKey: false };
  let store: SheetStore;
  let sent: SpreadsheetEvent[];
  let scroll: { top: number; left: number };
  let headerDrag: boolean;
  let editorCell: { col: number; row: number } | null;
  let selecting: PointerSelection;

  const types = (): string[] => sent.map((e) => e.type);
  const range = () => store.selection.activeRange;

  function press(col: number, row: number, keys: Partial<typeof NO_KEYS> = {}): CustomEvent {
    const e = new CustomEvent('cell-mousedown', { detail: { col, row, ...NO_KEYS, ...keys }, cancelable: true });
    selecting.press(e as CustomEvent<any>);
    return e;
  }

  function click(col: number, row: number, keys: Partial<typeof NO_KEYS> = {}): void {
    selecting.click({ col, row, ...NO_KEYS, ...keys });
  }

  beforeEach(() => {
    store = new SheetStore(() => {});
    store.write('rows', 20);
    store.write('cols', 10);
    sent = [];
    scroll = { top: 0, left: 0 };
    headerDrag = false;
    editorCell = null;
    const report = new SelectionReporter(store, { send: (e) => sent.push(e), selectedCellReported: () => {} });
    const pointing = new FormulaPointer(store, {
      fieldHasFocus: () => false,
      sheetName: () => 'Sheet1',
      scrollIntoView: () => {}
    });
    selecting = new PointerSelection(store, report, pointing, {
      scrollTop: () => scroll.top,
      scrollLeft: () => scroll.left,
      finishHeaderResize: () => headerDrag,
      cellOfEditor: () => editorCell,
      send: (e) => sent.push(e)
    });
  });

  describe('clicks', () => {
    it('select the cell clicked, and a link cell is reported as clicked after it', () => {
      store.write('hyperlinksTooltips', { [toKey(3, 4)]: 'https://vaadin.com' });

      click(2, 2);
      click(3, 4);

      expect(types()).to.eql(['cellSelected', 'cellSelected', 'linkCellClicked']);
      expect(sent[2].data).to.eql({ row: 4, column: 3 });
      expect(store.selection.activeCell).to.eql({ col: 3, row: 4 });
    });

    it('grow the range from the active cell with Shift', () => {
      click(2, 2);

      click(4, 4, { shiftKey: true });

      expect(range()).to.eql({ c1: 2, c2: 4, r1: 2, r2: 4 });
      expect(store.selection.activeCell).to.eql({ col: 2, row: 2 });
      expect(types()).to.eql(['cellSelected', 'cellRangeSelected']);
    });

    it('add the cell as a range of its own with Ctrl, and grow the last one with Ctrl and Shift, keeping the others', () => {
      click(2, 2);

      click(5, 5, { ctrlKey: true });
      expect(store.selection.auxRanges).to.have.length(1);

      click(6, 6, { metaKey: true, shiftKey: true });
      expect(range()).to.eql({ c1: 5, c2: 6, r1: 5, r2: 6 });
      expect(store.selection.auxRanges).to.have.length(1);
      expect(types()).to.eql(['cellSelected', 'cellAddedToSelectionAndSelected', 'cellsAddedToRangeSelection']);
    });
  });

  describe('presses', () => {
    it('drag out a range from the cell pressed, reported once on the release with the cell it started on', () => {
      click(2, 2);
      sent.length = 0;
      press(2, 2);
      expect(store.interaction.press).to.eql({ kind: 'select', crossed: false });

      selecting.over(4, 5);
      expect(store.interaction.press).to.eql({ kind: 'select', crossed: true });
      expect(range()).to.eql({ c1: 2, c2: 4, r1: 2, r2: 5 });
      expect(sent, 'nothing while dragging').to.eql([]);

      selecting.release();
      expect(store.interaction.press).to.equal(null);
      // With its own starting point, though it is the cell the server holds.
      expect(types()).to.eql(['cellRangePainted']);
    });

    it('report a shift-click range once, on the release, and the next shift-click again', () => {
      click(2, 2);
      sent.length = 0;

      press(4, 4, { shiftKey: true });
      selecting.release();
      click(4, 4, { shiftKey: true });
      expect(types(), 'the release only').to.eql(['cellRangeSelected']);

      press(5, 5, { shiftKey: true });
      selecting.release();
      click(5, 5, { shiftKey: true });
      expect(types()).to.eql(['cellRangeSelected', 'cellRangeSelected']);
    });

    it('let the click take the mark of a range reported on the release, and the next press drop it', () => {
      click(2, 2);
      press(4, 4, { shiftKey: true });
      selecting.release();
      expect(store.interaction.rangeReportedOnRelease).to.be.true;
      click(4, 4, { shiftKey: true });
      expect(store.interaction.rangeReportedOnRelease, 'taken by the click').to.be.false;

      press(5, 5, { shiftKey: true });
      // Released off the cells: no click follows.
      selecting.release();
      press(6, 6);
      expect(store.interaction.rangeReportedOnRelease, 'dropped by the next press').to.be.false;
    });

    it('leave the selection alone with Ctrl, starting no drag', () => {
      click(2, 2);

      press(5, 5, { ctrlKey: true });
      selecting.over(6, 6);

      expect(store.interaction.press).to.equal(null);
      expect(range()).to.eql({ c1: 2, c2: 2, r1: 2, r2: 2 });
    });

    it('point at the cell while a formula is written, selecting nothing', () => {
      store.changeEdit(openEditor(store.interaction.edit, { col: 2, row: 2, initialValue: '=', mode: 'enter' }, false));

      const e = press(4, 4);

      expect(e.defaultPrevented).to.be.true;
      expect(store.writing?.text).to.equal('=D4');
      expect(store.selection.activeCell).to.eql({ col: 1, row: 1 });
    });

    it('take a hover with no button down for nothing', () => {
      selecting.over(4, 4);

      expect(range()).to.eql({ c1: 1, c2: 1, r1: 1, r2: 1 });
      selecting.release();
      expect(sent).to.eql([]);
    });

    it('end a header drag on the release before anything else', () => {
      press(2, 2);
      selecting.over(3, 3);
      headerDrag = true;

      selecting.release();

      expect(sent).to.eql([]);
      expect(store.interaction.press, 'the press goes on').to.not.equal(null);
    });
  });

  describe('the fill handle', () => {
    it('paints from the range selected, and grows it on the release', () => {
      click(2, 2);
      sent.length = 0;
      selecting.fillStarted();

      selecting.over(2, 5);
      expect(store.paintRect()).to.eql({ c1: 2, c2: 2, r1: 3, r2: 5 });

      selecting.release();
      expect(sent).to.eql([{ type: 'selectionIncreasePainted', data: { r1: 2, c1: 2, r2: 5, c2: 2 } }]);
      expect(store.interaction.press).to.equal(null);
    });

    it('shrinks the range on the release, and does nothing for a drag that painted nothing', () => {
      store.select({ activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, c2: 2, r1: 2, r2: 5 }, auxRanges: [] });
      selecting.fillStarted();
      selecting.release();
      expect(sent, 'nothing painted').to.eql([]);

      selecting.fillStarted();
      selecting.over(2, 3);
      selecting.release();
      expect(sent).to.eql([{ type: 'selectionDecreasePainted', data: { row: 4, col: 2 } }]);
    });
  });

  describe('the handles of a selection by touch', () => {
    it('stretch the range from its active cell to the cell the finger goes to, reported on the release', () => {
      click(2, 2);
      sent.length = 0;
      selecting.stretchStarted();

      selecting.over(4, 5);
      expect(range()).to.eql({ c1: 2, c2: 4, r1: 2, r2: 5 });
      expect(store.selection.activeCell).to.eql({ col: 2, row: 2 });

      selecting.release();
      expect(sent).to.eql([
        {
          type: 'cellRangePainted',
          data: { selectedCellRow: 2, selectedCellColumn: 2, row1: 2, col1: 2, row2: 5, col2: 4 }
        }
      ]);
      expect(store.interaction.press).to.equal(null);
    });

    it('fill outward only: a drag back inside the range paints nothing, and clears nothing on the release', () => {
      store.select({ activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, c2: 2, r1: 2, r2: 5 }, auxRanges: [] });
      selecting.fillStarted(true);

      selecting.over(2, 3);
      expect(store.paintRect(), 'inside').to.equal(null);
      selecting.over(2, 7);
      expect(store.paintRect(), 'below').to.eql({ c1: 2, c2: 2, r1: 6, r2: 7 });
      selecting.over(2, 4);
      expect(store.paintRect(), 'inside again').to.equal(null);

      selecting.release();
      expect(sent).to.eql([]);
    });

    it('fill outward as the fill handle does', () => {
      click(2, 2);
      sent.length = 0;
      selecting.fillStarted(true);

      selecting.over(4, 2);
      selecting.release();

      expect(sent).to.eql([{ type: 'selectionIncreasePainted', data: { r1: 2, c1: 2, r2: 2, c2: 4 } }]);
    });
  });

  describe('the column letters and row numbers', () => {
    it('select the whole column, its first row on screen active', () => {
      scroll.top = 100;

      selecting.columnHeaderClicked({ col: 3, ...NO_KEYS });

      expect(range()).to.eql({ c1: 3, c2: 3, r1: 1, r2: 20 });
      // Rows of 20 pixels: the sixth has its top 100 pixels down.
      expect(store.selection.activeCell).to.eql({ col: 3, row: 6 });
      expect(types()).to.eql(['columnSelected']);
    });

    it('make row 1 active while rows are frozen, and pass over hidden rows', () => {
      store.write('verticalSplitPosition', 2);
      scroll.top = 100;
      selecting.columnHeaderClicked({ col: 3, ...NO_KEYS });
      scroll.top = 0;
      expect(store.selection.activeCell.row, 'frozen').to.equal(1);

      store.write('verticalSplitPosition', 0);
      store.write('hiddenRowIndexes', [1, 2]);
      selecting.columnHeaderClicked({ col: 4, ...NO_KEYS });
      expect(store.selection.activeCell.row, 'hidden').to.equal(3);
    });

    it('add the column with Ctrl, and grow across columns with Shift', () => {
      click(2, 2);

      selecting.columnHeaderClicked({ col: 5, ...NO_KEYS, ctrlKey: true });
      expect(store.selection.auxRanges).to.have.length(1);

      selecting.columnHeaderClicked({ col: 7, ...NO_KEYS, shiftKey: true });
      expect(range()).to.eql({ c1: 5, c2: 7, r1: 1, r2: 20 });
      // Grown from the cell the server holds, the top of the column added.
      expect(types()).to.eql(['cellSelected', 'columnAddedToSelection', 'cellRangeSelected']);
    });

    it('select the whole row, its first column on screen active, and grow across rows', () => {
      scroll.left = 130;

      selecting.rowHeaderClicked({ row: 4, ...NO_KEYS });
      expect(range()).to.eql({ c1: 1, c2: 10, r1: 4, r2: 4 });
      // Columns of 64 pixels: the third starts 128 pixels in, off screen
      // by 2; the fourth is the first with its left on screen.
      expect(store.selection.activeCell).to.eql({ col: 4, row: 4 });

      selecting.rowHeaderClicked({ row: 6, ...NO_KEYS, shiftKey: true });
      expect(range()).to.eql({ c1: 1, c2: 10, r1: 4, r2: 6 });

      selecting.rowHeaderClicked({ row: 9, ...NO_KEYS, metaKey: true });
      expect(types()).to.eql(['rowSelected', 'cellRangeSelected', 'rowAddedToRangeSelection']);
    });
  });

  describe('a press on a custom editor', () => {
    it('selects the cell the editor is on, once', () => {
      editorCell = { col: 4, row: 5 };

      selecting.editorPressed(new Event('pointerdown'));
      selecting.editorPressed(new Event('pointerdown'));

      expect(store.selection.activeCell).to.eql({ col: 4, row: 5 });
      expect(types()).to.eql(['cellSelected']);
    });

    it('selects nothing for a press elsewhere', () => {
      selecting.editorPressed(new Event('pointerdown'));

      expect(sent).to.eql([]);
    });
  });
});
