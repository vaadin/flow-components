/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Type definitions for the client→server events dispatched by the
 * {@code <vaadin-spreadsheet>} Lit wrapper element.
 *
 * The wrapper re-emits every server-bound action as a
 * {@code CustomEvent} on the host element, where the event name matches
 * the typed dispatch handler on the server side
 * ({@link com.vaadin.flow.component.spreadsheet.SpreadsheetHandlerImpl}).
 *
 * On the WIRE the events travel through a single
 * {@code @DomEvent("spreadsheet-event")} carrying
 * {@code {type: string, data: JsonNode}} — see slice 116's Tier 3
 * examination. Individually-named events on the wrapper element are the
 * client-side DOM convenience; the JSON payload that crosses the wire
 * is built by the {@code SpreadsheetEventListener} on the server when it
 * receives the typed DOM event.
 *
 * The type discriminator below is keyed by the wrapper event name (one
 * of the 39 strings the wrapper dispatches today). New event types
 * added in Phase 3.2+ should extend this union — the
 * exhaustiveness-check pattern is enforced by `SpreadsheetEventDetail`
 * being a discriminated union.
 *
 * As of Phase 3.1 slice 120, the {@code data} shapes are mostly typed
 * as {@code Record<string, unknown>} pending the Phase 3.2 work that
 * wires the new client's per-event dispatch. The names and the
 * envelope shape are stable; the inner data fields will be sharpened
 * incrementally.
 */

/**
 * The complete set of event names the wrapper dispatches today.
 * Compile-time exhaustive — if a new event is added in Phase 3.2+ but
 * forgotten here, the dispatch switch in the new client won't compile.
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
  // Popup buttons (slice 128 — added after audit; the original
  // slice 120 survey scanned the wrapper's GENERAL event-dispatch
  // section + missed these two in the popup-button section)
  | 'popupButtonClick'
  | 'popupClose';

/**
 * Generic envelope for every event the wrapper dispatches. The data
 * shape varies per event; specific event interfaces below narrow it.
 */
export interface SpreadsheetEventDetail<
  T extends SpreadsheetEventType = SpreadsheetEventType,
  D = Record<string, unknown>
> {
  type: T;
  data: D;
}

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

/**
 * The discriminated-union map. Use as
 * {@code event: SpreadsheetEvent} where
 * {@code event.detail.type === 'cellSelected'} narrows
 * {@code event.detail.data} to {@code CellSelectedData}.
 *
 * Events whose data shape isn't yet typed default to
 * {@code SpreadsheetEventDetail<T>} with the generic
 * {@code Record<string, unknown>} payload — Phase 3.2+ work will
 * incrementally narrow those.
 */
export type SpreadsheetEvent =
  | { type: 'onSheetScroll'; data: OnSheetScrollData }
  | { type: 'cellSelected'; data: CellSelectedData }
  | { type: 'cellRangeSelected'; data: CellRangeSelectedData }
  | { type: 'cellAddedToSelectionAndSelected'; data: CellSelectedData }
  | { type: 'cellsAddedToRangeSelection'; data: CellRangeSelectedData }
  | { type: 'rowSelected'; data: RowSelectedData }
  | { type: 'rowAddedToRangeSelection'; data: RowSelectedData }
  | { type: 'columnSelected'; data: ColumnSelectedData }
  | { type: 'columnAddedToSelection'; data: ColumnSelectedData }
  | { type: 'cellRangePainted'; data: CellRangePaintedData }
  | { type: 'selectionIncreasePainted'; data: SelectionIncreasePaintedData }
  | { type: 'selectionDecreasePainted'; data: SelectionDecreasePaintedData }
  | { type: 'cellValueEdited'; data: CellValueEditedData }
  | { type: 'sheetSelected'; data: SheetSelectedData }
  | { type: 'sheetRenamed'; data: SheetRenamedData }
  | { type: 'sheetCreated'; data: SheetCreatedData }
  | { type: 'sheetAddressChanged'; data: { value: string } }
  | { type: 'linkCellClicked'; data: LinkCellClickedData }
  | { type: 'rowsResized'; data: RowsResizedData }
  | { type: 'columnResized'; data: ColumnResizedData }
  | { type: 'onRowAutofit'; data: { rowIndex: number } }
  | { type: 'onColumnAutofit'; data: { columnIndex: number } }
  | { type: 'setCellStyleWidthRatios'; data: { ratios: Record<number, number> } }
  | { type: 'onPaste'; data: OnPasteData }
  | { type: 'clearSelectedCellsOnCut'; data: Record<string, never> }
  | { type: 'onUndo'; data: Record<string, never> }
  | { type: 'onRedo'; data: Record<string, never> }
  | { type: 'onConnectorInit'; data: Record<string, never> }
  | { type: 'protectedCellWriteAttempted'; data: Record<string, never> }
  | { type: 'updateCellComment'; data: { row: number; col: number; text: string } }
  | { type: 'deleteSelectedCells'; data: Record<string, never> }
  | { type: 'groupingCollapsed'; data: GroupingCollapsedData }
  | { type: 'levelHeaderClicked'; data: LevelHeaderClickedData }
  | { type: 'contextMenuOpenOnSelection'; data: ContextMenuOpenData }
  | { type: 'rowHeaderContextMenuOpen'; data: { rowIndex: number } }
  | { type: 'columnHeaderContextMenuOpen'; data: { columnIndex: number } }
  | { type: 'actionOnCurrentSelection'; data: ActionData }
  | { type: 'actionOnRowHeader'; data: ActionData }
  | { type: 'actionOnColumnHeader'; data: ActionData }
  | { type: 'contextMenuClosed'; data: Record<string, never> }
  | { type: 'popupButtonClick'; data: PopupButtonEventData }
  | { type: 'popupClose'; data: PopupButtonEventData };

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
 * Helper: narrow an arbitrary CustomEvent dispatched by the wrapper to
 * a typed {@code SpreadsheetEvent}. Returns null if the event type
 * isn't recognized — callers handle the missing case.
 *
 * Use:
 *
 * <pre>
 *     element.addEventListener('cellSelected', (e) =&gt; {
 *         const detail = asSpreadsheetEvent(e);
 *         if (detail?.type === 'cellSelected') {
 *             // detail.data is CellSelectedData
 *         }
 *     });
 * </pre>
 */
export function asSpreadsheetEvent(event: Event): SpreadsheetEvent | null {
  if (!(event instanceof CustomEvent)) {
    return null;
  }
  const detail = event.detail as SpreadsheetEvent | null;
  if (!detail || typeof detail !== 'object' || typeof detail.type !== 'string') {
    return null;
  }
  return detail;
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
export const SERVER_EVENT_PAYLOAD_KEYS: Record<SpreadsheetEventType, readonly string[]> = {
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
