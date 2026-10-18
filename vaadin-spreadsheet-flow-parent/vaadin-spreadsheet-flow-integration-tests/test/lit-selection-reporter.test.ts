/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { SelectionReporter } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/SelectionReporter.js';
import {
  type CellPosition,
  selectSingleCell
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';
import type { SpreadsheetEvent } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';

/**
 * Telling the server what the user selected, on its own: which event each
 * selection goes as, and the cell the server holds as selected after it.
 */
describe('SelectionReporter', () => {
  let store: SheetStore;
  let sent: SpreadsheetEvent[];
  let reported: CellPosition[];
  let clock: number;
  let report: SelectionReporter;

  /** Selects a range with the active cell given. */
  function selectRange(col: number, row: number, c1: number, r1: number, c2: number, r2: number): void {
    store.select({ activeCell: { col, row }, activeRange: { c1, r1, c2, r2 }, auxRanges: [] });
  }

  beforeEach(() => {
    store = new SheetStore(() => {});
    store.write('rows', 10);
    store.write('cols', 5);
    sent = [];
    reported = [];
    clock = 0;
    report = new SelectionReporter(
      store,
      {
        send: (event) => sent.push(event),
        selectedCellReported: (cell) => reported.push(cell)
      },
      () => clock
    );
  });

  describe('the selection', () => {
    it('reports one cell, which the server then holds as selected and the formula bar shows', () => {
      store.select(selectSingleCell(2, 3));

      report.selection();

      expect(sent).to.eql([{ type: 'cellSelected', data: { row: 3, column: 2, oldSelectionRangeDiscarded: true } }]);
      expect(store.sheetData.serverSelectedCell).to.eql({ col: 2, row: 3 });
      expect(reported).to.eql([{ col: 2, row: 3 }]);
    });

    it('reports a whole column and a whole row as such', () => {
      selectRange(2, 4, 2, 1, 2, 10);
      report.selection();
      selectRange(3, 6, 1, 6, 5, 6);
      report.selection();

      expect(sent).to.eql([
        { type: 'columnSelected', data: { column: 2, firstRowIndex: 4 } },
        { type: 'rowSelected', data: { row: 6, firstColumnIndex: 3 } }
      ]);
      expect(reported).to.eql([
        { col: 2, row: 4 },
        { col: 3, row: 6 }
      ]);
    });

    it('reports a range grown from the cell the server holds as selected as a range only', () => {
      store.select(selectSingleCell(2, 2));
      report.selection();
      sent.length = 0;

      selectRange(2, 2, 2, 2, 4, 4);
      report.selection();

      expect(sent).to.eql([{ type: 'cellRangeSelected', data: { row1: 2, col1: 2, row2: 4, col2: 4 } }]);
    });

    it('reports a range with its starting point when it has one of its own, or grows from another cell', () => {
      store.select(selectSingleCell(2, 2));
      report.selection();
      sent.length = 0;

      selectRange(2, 2, 2, 2, 4, 4);
      report.selection(true);
      selectRange(3, 3, 3, 3, 5, 5);
      report.selection();

      expect(sent).to.eql([
        {
          type: 'cellRangePainted',
          data: { selectedCellRow: 2, selectedCellColumn: 2, row1: 2, col1: 2, row2: 4, col2: 4 }
        },
        {
          type: 'cellRangePainted',
          data: { selectedCellRow: 3, selectedCellColumn: 3, row1: 3, col1: 3, row2: 5, col2: 5 }
        }
      ]);
      expect(store.sheetData.serverSelectedCell).to.eql({ col: 3, row: 3 });
    });

    it('reports a range grown from another cell of the column or the row the server holds with its starting point', () => {
      store.select(selectSingleCell(2, 2));
      report.selection();
      // Another row of the same column.
      selectRange(2, 5, 2, 5, 4, 6);
      report.selection();
      store.select(selectSingleCell(2, 2));
      report.selection();
      // Another column of the same row.
      selectRange(5, 2, 5, 2, 6, 3);
      report.selection();

      expect(sent.map((event) => event.type)).to.eql([
        'cellSelected',
        'cellRangePainted',
        'cellSelected',
        'cellRangePainted'
      ]);
      expect(sent[1].data).to.include({ selectedCellRow: 5, selectedCellColumn: 2 });
      expect(sent[3].data).to.include({ selectedCellRow: 2, selectedCellColumn: 5 });
    });

    it('reports a merge as its one cell', () => {
      store.write('mergedRegions', [{ id: 1, col1: 2, col2: 3, row1: 2, row2: 4 }]);
      store.selectCellOrMerge(3, 3);

      report.selection();

      expect(sent).to.eql([{ type: 'cellSelected', data: { row: 2, column: 2, oldSelectionRangeDiscarded: true } }]);
    });

    it('reports the active range of a selection in pieces as one more piece, and of a whole one as the selection', () => {
      store.select({
        activeCell: { col: 4, row: 4 },
        activeRange: { c1: 4, r1: 4, c2: 5, r2: 5 },
        auxRanges: [{ c1: 1, r1: 1, c2: 1, r2: 1 }]
      });
      report.range();
      store.select(selectSingleCell(2, 2));
      report.range();

      expect(sent.map((event) => event.type)).to.eql(['cellsAddedToRangeSelection', 'cellSelected']);
      expect(sent[0].data).to.eql({ row1: 4, col1: 4, row2: 5, col2: 5 });
    });
  });

  describe('what joins the selection', () => {
    it('reports a cell, a column and a row joining, each with the cell it selects', () => {
      report.cellAdded(3, 4);
      report.columnAdded(5, 2);
      report.rowAdded(6, 1);

      expect(sent).to.eql([
        { type: 'cellAddedToSelectionAndSelected', data: { row: 4, column: 3 } },
        { type: 'columnAddedToSelection', data: { column: 5, firstRowIndex: 2 } },
        { type: 'rowAddedToRangeSelection', data: { row: 6, firstColumnIndex: 1 } }
      ]);
      expect(reported).to.eql([
        { col: 3, row: 4 },
        { col: 5, row: 2 },
        { col: 1, row: 6 }
      ]);
      expect(store.sheetData.serverSelectedCell).to.eql({ col: 1, row: 6 });
    });

    it('reports a range joining without another cell selected', () => {
      report.rangeAdded({ c1: 2, r1: 3, c2: 4, r2: 5 });

      expect(sent).to.eql([{ type: 'cellsAddedToRangeSelection', data: { row1: 3, col1: 2, row2: 5, col2: 4 } }]);
      expect(store.sheetData.serverSelectedCell).to.equal(null);
      expect(reported).to.eql([]);
    });
  });

  it('reports the active cell moving inside the range, the range kept', () => {
    report.activeCellMoved({ col: 3, row: 2 });

    expect(sent).to.eql([{ type: 'cellSelected', data: { row: 2, column: 3, oldSelectionRangeDiscarded: false } }]);
    expect(store.sheetData.serverSelectedCell).to.eql({ col: 3, row: 2 });
  });

  it('reports writes to a locked cell once a second at most', () => {
    for (const at of [0, 500, 999, 1000, 1500, 2100]) {
      clock = at;
      report.protectedWrite();
    }

    expect(sent).to.have.length(3);
    expect(sent[0]).to.eql({ type: 'protectedCellWriteAttempted', data: {} });
    expect(reported).to.eql([]);
  });
});
