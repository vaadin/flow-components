/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Type definitions for the client→server events.
 *
 * The connector sends every server-bound action as one
 * {@code spreadsheet-event} CustomEvent on the {@code <vaadin-spreadsheet>}
 * element, with a {@code {type, data}} detail. The server's
 * {@code @DomEvent("spreadsheet-event")} receives both, and
 * {@code SpreadsheetEventListener} calls the matching method of
 * {@link com.vaadin.flow.component.spreadsheet.SpreadsheetHandlerImpl}.
 *
 * Some {@code data} shapes are typed below; the others are
 * {@code Record<string, unknown>}. {@link SERVER_EVENT_PAYLOAD_KEYS} lists
 * the keys the server reads from each.
 */

/**
 * Every event type the connector sends. `lit-server-event-contract.test.ts`
 * checks that each event sent is one of these.
 */
export type SpreadsheetEventType =
  // Grouping (outline) interactions
  | 'groupingCollapsed'
  | 'levelHeaderClicked'
  // Context menu
  | 'contextMenuOpenOnSelection'
  | 'rowHeaderContextMenuOpen'
  | 'columnHeaderContextMenuOpen'
  | 'actionOnCurrentSelection'
  | 'actionOnRowHeader'
  | 'actionOnColumnHeader'
  | 'contextMenuClosed'
  // Sheet lifecycle
  | 'sheetSelected'
  | 'sheetRenamed'
  | 'sheetCreated'
  | 'sheetAddressChanged'
  // Selection
  | 'cellSelected'
  | 'cellRangeSelected'
  | 'cellAddedToSelectionAndSelected'
  | 'cellsAddedToRangeSelection'
  | 'rowSelected'
  | 'rowAddedToRangeSelection'
  | 'columnSelected'
  | 'columnAddedToSelection'
  | 'cellRangePainted'
  | 'selectionIncreasePainted'
  | 'selectionDecreasePainted'
  // Cell content
  | 'cellValueEdited'
  | 'deleteSelectedCells'
  | 'linkCellClicked'
  | 'updateCellComment'
  // Resize + autofit
  | 'rowsResized'
  | 'columnResized'
  | 'onRowAutofit'
  | 'onColumnAutofit'
  | 'setCellStyleWidthRatios'
  // Scroll
  | 'onSheetScroll'
  // History
  | 'onUndo'
  | 'onRedo'
  // Copy / paste
  | 'onPaste'
  | 'clearSelectedCellsOnCut'
  // Init + protection
  | 'onConnectorInit'
  | 'protectedCellWriteAttempted'
  // Popup buttons
  | 'popupButtonClick'
  | 'popupClose';

// ====================================================================
// Per-event data shapes — typed for the events whose argument list is
// stable per the existing SpreadsheetHandler.java. The data field is
// the dispatch arguments in named-object form.
// ====================================================================

export interface OnSheetScrollData {
  firstRow: number;
  firstColumn: number;
  lastRow: number;
  lastColumn: number;
}

export interface CellSelectedData {
  row: number;
  column: number;
  /** When true, the previous selection range is replaced (not extended). */
  oldSelectionRangeDiscarded: boolean;
}

/**
 * A cell joining a non-coherent (Ctrl-clicked) selection. Deliberately not
 * {@link CellSelectedData}: the server reads only the coordinates here, and
 * sending it a flag it never looks at is how a real key mismatch hides.
 */
export interface CellAddedToSelectionData {
  row: number;
  column: number;
}

export interface CellRangeSelectedData {
  row1: number;
  col1: number;
  row2: number;
  col2: number;
}

export interface RowSelectedData {
  row: number;
  firstColumnIndex: number;
}

export interface ColumnSelectedData {
  column: number;
  firstRowIndex: number;
}

export interface CellRangePaintedData {
  selectedCellRow: number;
  selectedCellColumn: number;
  row1: number;
  col1: number;
  row2: number;
  col2: number;
}

export interface SelectionIncreasePaintedData {
  r1: number;
  c1: number;
  r2: number;
  c2: number;
}

export interface SelectionDecreasePaintedData {
  row: number;
  col: number;
}

export interface CellValueEditedData {
  row: number;
  col: number;
  value: string;
}

export interface SheetSelectedData {
  /** 0-based POI sheet index. */
  sheetIndex: number;
  scrollLeft: number;
  scrollTop: number;
}

export interface SheetRenamedData {
  /** 0-based POI sheet index. */
  sheetIndex: number;
  newName: string;
}

export interface SheetCreatedData {
  scrollLeft: number;
  scrollTop: number;
}

export interface LinkCellClickedData {
  /** 1-based row index. */
  row: number;
  /** 1-based column index. */
  column: number;
}

export interface RowsResizedData {
  /** Resized rows, 1-based. */
  rowIndexes: number[];
  /** New height in points, positionally paired with `rowIndexes`. */
  newHeights: number[];
}

export interface ColumnResizedData {
  /** Resized columns, 1-based. */
  colIndexes: number[];
  /** New width in pixels, positionally paired with `colIndexes`. */
  newWidths: number[];
}

export interface OnPasteData {
  /** Pasted text — typically TSV from the clipboard. */
  text: string;
}

export interface GroupingCollapsedData {
  cols: boolean;
  colIndex: number;
  collapsed: boolean;
}

export interface LevelHeaderClickedData {
  cols: boolean;
  level: number;
}

export interface ActionData {
  actionKey: string;
}

export interface ContextMenuOpenData {
  row: number;
  column: number;
}

/** No data: the event itself is the message. */
export type NoData = Record<string, never>;

/**
 * The data each event carries, by its type. Every type in
 * {@link SpreadsheetEventType} has an entry, and
 * {@link SERVER_EVENT_PAYLOAD_KEYS} names only keys these declare.
 */
export interface SpreadsheetEventData {
  onSheetScroll: OnSheetScrollData;
  cellSelected: CellSelectedData;
  cellRangeSelected: CellRangeSelectedData;
  cellAddedToSelectionAndSelected: CellAddedToSelectionData;
  cellsAddedToRangeSelection: CellRangeSelectedData;
  rowSelected: RowSelectedData;
  rowAddedToRangeSelection: RowSelectedData;
  columnSelected: ColumnSelectedData;
  columnAddedToSelection: ColumnSelectedData;
  cellRangePainted: CellRangePaintedData;
  selectionIncreasePainted: SelectionIncreasePaintedData;
  selectionDecreasePainted: SelectionDecreasePaintedData;
  cellValueEdited: CellValueEditedData;
  sheetSelected: SheetSelectedData;
  sheetRenamed: SheetRenamedData;
  sheetCreated: SheetCreatedData;
  sheetAddressChanged: { value: string };
  linkCellClicked: LinkCellClickedData;
  rowsResized: RowsResizedData;
  columnResized: ColumnResizedData;
  onRowAutofit: { rowIndex: number };
  onColumnAutofit: { columnIndex: number };
  setCellStyleWidthRatios: CellStyleWidthRatiosData;
  onPaste: OnPasteData;
  clearSelectedCellsOnCut: NoData;
  onUndo: NoData;
  onRedo: NoData;
  onConnectorInit: NoData;
  protectedCellWriteAttempted: NoData;
  updateCellComment: { row: number; col: number; text: string };
  deleteSelectedCells: NoData;
  groupingCollapsed: GroupingCollapsedData;
  levelHeaderClicked: LevelHeaderClickedData;
  contextMenuOpenOnSelection: ContextMenuOpenData;
  rowHeaderContextMenuOpen: { rowIndex: number };
  columnHeaderContextMenuOpen: { columnIndex: number };
  actionOnCurrentSelection: ActionData;
  actionOnRowHeader: ActionData;
  actionOnColumnHeader: ActionData;
  contextMenuClosed: NoData;
  popupButtonClick: PopupButtonEventData;
  popupClose: PopupButtonEventData;
}

/**
 * An event for the server: its type, and the data that type carries. The
 * connector sends nothing else, so the compiler holds every dispatch to its
 * type's data.
 */
export type SpreadsheetEvent = {
  [T in SpreadsheetEventType]: { type: T; data: SpreadsheetEventData[T] };
}[SpreadsheetEventType];

/**
 * Payload for {@code setCellStyleWidthRatios}: how wide one character is in
 * each cell style, as two lists in step - the style indexes and their
 * pixels-per-character ratios.
 */
export interface CellStyleWidthRatiosData {
  styleIndexes: number[];
  ratios: number[];
}

/**
 * Payload for {@code popupButtonClick} and {@code popupClose}.
 *
 * Server-side handler shape (see
 * `Spreadsheet.java:onPopupButtonClick` + `onPopupClose`): both
 * read `(row, column)` via `toInt(pars, 0)` + `toInt(pars, 1)`.
 * 1-based, matching the rest of the API.
 */
export interface PopupButtonEventData {
  row: number;
  column: number;
}

/**
 * The payload key names the server reads for each event type — the single
 * source of truth for the client-to-server wire.
 *
 * Two guards check the two halves against this map, because nothing else
 * does: the connector can dispatch a key the server never reads and the
 * event silently does nothing. `sheetSelected` shipped for a while sending
 * `sheetName` while the server read `sheetIndex`, so every sheet-tab click
 * activated sheet 0.
 *
 * - `ServerEventPayloadContractTest` (Java) checks that
 *   `SpreadsheetEventListener` reads exactly these keys.
 * - `lit-server-event-contract.test.ts` (wtr) checks that every payload the
 *   connector dispatches carries exactly these keys.
 *
 * An empty list means the event carries no payload.
 */
export const SERVER_EVENT_PAYLOAD_KEYS: {
  readonly [T in SpreadsheetEventType]: ReadonlyArray<
    SpreadsheetEventData[T] extends NoData ? never : keyof SpreadsheetEventData[T] & string
  >;
} = {
  onConnectorInit: [],
  onSheetScroll: ['firstRow', 'firstColumn', 'lastRow', 'lastColumn'],
  sheetAddressChanged: ['value'],
  cellSelected: ['row', 'column', 'oldSelectionRangeDiscarded'],
  cellRangeSelected: ['row1', 'col1', 'row2', 'col2'],
  cellAddedToSelectionAndSelected: ['row', 'column'],
  cellsAddedToRangeSelection: ['row1', 'col1', 'row2', 'col2'],
  rowSelected: ['row', 'firstColumnIndex'],
  rowAddedToRangeSelection: ['row', 'firstColumnIndex'],
  columnSelected: ['column', 'firstRowIndex'],
  columnAddedToSelection: ['column', 'firstRowIndex'],
  selectionIncreasePainted: ['r1', 'c1', 'r2', 'c2'],
  selectionDecreasePainted: ['row', 'col'],
  cellValueEdited: ['row', 'col', 'value'],
  sheetSelected: ['sheetIndex', 'scrollLeft', 'scrollTop'],
  sheetRenamed: ['sheetIndex', 'newName'],
  sheetCreated: ['scrollLeft', 'scrollTop'],
  cellRangePainted: ['selectedCellRow', 'selectedCellColumn', 'row1', 'col1', 'row2', 'col2'],
  deleteSelectedCells: [],
  linkCellClicked: ['row', 'column'],
  rowsResized: ['rowIndexes', 'newHeights'],
  columnResized: ['colIndexes', 'newWidths'],
  onRowAutofit: ['rowIndex'],
  onColumnAutofit: ['columnIndex'],
  onUndo: [],
  onRedo: [],
  setCellStyleWidthRatios: ['styleIndexes', 'ratios'],
  protectedCellWriteAttempted: [],
  onPaste: ['text'],
  clearSelectedCellsOnCut: [],
  updateCellComment: ['text', 'col', 'row'],
  groupingCollapsed: ['cols', 'colIndex', 'collapsed'],
  levelHeaderClicked: ['cols', 'level'],
  popupButtonClick: ['row', 'column'],
  popupClose: ['row', 'column'],
  contextMenuOpenOnSelection: ['row', 'column'],
  actionOnCurrentSelection: ['actionKey'],
  rowHeaderContextMenuOpen: ['rowIndex'],
  actionOnRowHeader: ['actionKey'],
  columnHeaderContextMenuOpen: ['columnIndex'],
  actionOnColumnHeader: ['actionKey'],
  contextMenuClosed: []
};
