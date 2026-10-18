/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { isMultiCellRange } from '../algorithms/SelectionWalk.js';
import { buildSelectionEvent, type CellPosition, type CellRange, selectedCellSetBy } from '../state/SelectionState.js';
import type { SheetStore } from '../state/SheetStore.js';
import type {
  CellAddedToSelectionData,
  CellRangePaintedData,
  CellRangeSelectedData,
  CellSelectedData,
  ColumnSelectedData,
  RowSelectedData,
  SpreadsheetEvent
} from '../types/rpc-client-to-server.js';

/**
 * The shortest time between two reports of a write to a locked cell, as the
 * GWT client kept it: the server's ProtectedEditListener promises one event a
 * second at most.
 */
const PROTECTED_WRITE_REPORT_INTERVAL_MS = 1000;

/** What telling the server of the selection needs of the sheet. */
export interface SelectionReporterHost {
  /** Sends an event to the server. */
  send(event: SpreadsheetEvent): void;
  /** The server was told another cell is selected: the formula bar shows it at once. */
  selectedCellReported(cell: CellPosition): void;
}

/**
 * Tells the server what the user selected, and keeps the cell the server
 * holds as selected: the cell the server grows a range from.
 */
export class SelectionReporter {
  /** When the server was last told of a write to a locked cell, if ever. */
  private protectedWriteReportedAt: number | null = null;

  constructor(
    private readonly store: Pick<SheetStore, 'model' | 'selection' | 'sheetData' | 'selectsRange'>,
    private readonly host: SelectionReporterHost,
    private readonly now: () => number = () => performance.now()
  ) {}

  /**
   * Tell the server the selection, replacing whatever came before.
   *
   * Picks one of {@code cellSelected / cellRangeSelected / columnSelected /
   * rowSelected} based on the shape of the active range:
   *
   *  - whole column (r1=1, r2=rows, single col) → columnSelected
   *  - whole row    (c1=1, c2=cols, single row) → rowSelected
   *  - single cell  (c1=c2, r1=r2)              → cellSelected
   *  - otherwise                                → cellRangeSelected
   *
   * The server receives {@code spreadsheet-event} through a
   * {@code @DomEvent}; the detail's {@code type} + {@code data} follow the
   * server's SpreadsheetHandler signatures as a wire contract.
   *
   * A range goes in one event, as in earlier versions. Grown from the cell
   * the server holds as selected - a shift-click, Shift+arrow - it is
   * `cellRangeSelected`, which keeps that cell. With a starting point of its
   * own - a drag, select all - or grown from a cell the server does not
   * hold, it is `cellRangePainted`, which carries the starting point:
   * `cellRangeSelected` would leave the server's selected cell elsewhere, or
   * null. Telling the server the starting point first, in a `cellSelected`,
   * made it report a selection of that one cell in between, which every
   * selection listener of the application saw.
   *
   * @param startsAnew whether the range has a starting point of its own,
   *   even one that is the selected cell already
   */
  selection(startsAnew = false): void {
    const { activeCell, activeRange } = this.store.selection;
    // A merge is one cell: the server hears that cell, rather than a range
    // it would offer to merge again.
    const range =
      this.store.selectsRange() || !isMultiCellRange(activeRange)
        ? activeRange
        : { c1: activeCell.col, c2: activeCell.col, r1: activeCell.row, r2: activeCell.row };
    const event = buildSelectionEvent(activeCell, range, this.store.model.rows, this.store.model.cols);
    const held = this.store.sheetData.serverSelectedCell;
    const grownFromHeld = held !== null && held.col === activeCell.col && held.row === activeCell.row;
    if (event.type === 'cellRangeSelected' && (startsAnew || !grownFromHeld)) {
      const data: CellRangePaintedData = {
        selectedCellRow: activeCell.row,
        selectedCellColumn: activeCell.col,
        ...event.data
      };
      this.send({ type: 'cellRangePainted', data });
      return;
    }
    this.send(event);
  }

  /**
   * Tell the server about the active range, as a whole selection or as
   * one more piece of a selection that is already in pieces.
   *
   * An ordinary selection event says the old range is gone, and the
   * server drops the cells earlier Ctrl-clicks put there along with it.
   * Anything then acting on the selection - a context-menu action, a
   * delete - misses them.
   */
  range(startsAnew = false): void {
    const { activeRange, auxRanges } = this.store.selection;
    if (auxRanges.length > 0) {
      this.rangeAdded(activeRange);
      return;
    }
    this.selection(startsAnew);
  }

  /**
   * Tell the server a cell joined a non-coherent selection. Distinct
   * from {@link selection}, which reports a selection that replaces
   * whatever came before.
   */
  cellAdded(col: number, row: number): void {
    const data: CellAddedToSelectionData = { row, column: col };
    this.send({ type: 'cellAddedToSelectionAndSelected', data });
  }

  /** Tell the server a whole range joined a non-coherent selection. */
  rangeAdded(range: CellRange): void {
    const data: CellRangeSelectedData = {
      row1: range.r1,
      col1: range.c1,
      row2: range.r2,
      col2: range.c2
    };
    this.send({ type: 'cellsAddedToRangeSelection', data });
  }

  /** Tell the server a whole column joined the selection, its cell in the row given selected. */
  columnAdded(col: number, row: number): void {
    const data: ColumnSelectedData = { column: col, firstRowIndex: row };
    this.send({ type: 'columnAddedToSelection', data });
  }

  /** Tell the server a whole row joined the selection, its cell in the column given selected. */
  rowAdded(row: number, col: number): void {
    const data: RowSelectedData = { row, firstColumnIndex: col };
    this.send({ type: 'rowAddedToRangeSelection', data });
  }

  /**
   * Tell the server the active cell moved inside the range selected, the
   * range kept.
   *
   * The server is told with the plain `cellSelected` event, but with
   * `oldSelectionRangeDiscarded` false - that flag is the whole
   * difference between "the user picked this cell" and "the user is
   * walking through the block they picked earlier".
   */
  activeCellMoved(cell: CellPosition): void {
    const data: CellSelectedData = {
      row: cell.row,
      column: cell.col,
      oldSelectionRangeDiscarded: false
    };
    this.send({ type: 'cellSelected', data });
  }

  /**
   * Tell the server the user tried to change a cell they may not change.
   *
   * The client refuses the edit on its own; the server is told so it can
   * say why — the default handler shows the workbook's "this cell is
   * protected" notification. Silently swallowing the keystroke leaves the
   * user with no idea why nothing happened.
   */
  protectedWrite(): void {
    // A burst of keys into a locked cell is one attempt, not a notification
    // per key: the attempts within a second of the last report go unsaid.
    const now = this.now();
    const last = this.protectedWriteReportedAt;
    if (last !== null && now - last < PROTECTED_WRITE_REPORT_INTERVAL_MS) {
      return;
    }
    this.protectedWriteReportedAt = now;
    this.send({ type: 'protectedCellWriteAttempted', data: {} });
  }

  /** Sends an event, keeping the cell it tells the server is selected. */
  private send(event: SpreadsheetEvent): void {
    this.host.send(event);
    const selected = selectedCellSetBy(event);
    if (selected !== null) {
      this.store.sheetData.serverSelectedCell = selected;
      this.host.selectedCellReported(selected);
    }
  }
}
