/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type {
  CellData,
  GroupingData,
  MergedRegion,
  OverlayInfo,
  PopupButtonState,
  SpreadsheetActionDetails
} from './wire.js';

/**
 * The complete set of methods the new Spreadsheet client must expose to
 * the Lit wrapper element ({@code vaadin-spreadsheet.js}). The wrapper
 * keeps a reference to an instance of this type as {@code this.api} and
 * dispatches every property change + RPC call through it.
 *
 * This interface IS the contract the Phase 3.6 swap turns on: today
 * {@code this.api} is an instance of the GWT-compiled
 * {@code Spreadsheet} class (from {@code spreadsheet-export.js}); when
 * the new client ships, {@code this.api} becomes an instance of a TS
 * class that implements this interface.
 *
 * Method shapes derived from the existing wrapper's call sites
 * (lines 174-749 of {@code vaadin-spreadsheet.js} as of slice 118).
 * The 12 RPC methods at the top mirror
 * {@link com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc}
 * (the typed façade clarified in slice 108) — they're the server→client
 * action messages. The setters below are the property-mirror lane (each
 * one corresponds to a {@link MirroredProperty} on the server).
 */
export interface SpreadsheetClientApi {
  // ====================================================================
  // Initial setup / lifecycle
  // ====================================================================

  /** Set the spreadsheet's width as a CSS dimension string. */
  setWidth(width: string): void;
  /** Set the spreadsheet's height as a CSS dimension string. */
  setHeight(height: string): void;
  /** Trigger a full relayout (called after the initial state batch). */
  relayout(): void;
  /** Re-measure on container size change. */
  resize(): void;
  /**
   * Inform the client that the given properties just changed in this
   * Lit-update cycle. Called after the wrapper has finished applying
   * the property batch.
   */
  notifyStateChanges(changedPropertyNames: string[], firstUpdate: boolean): void;

  // ====================================================================
  // 12 explicit RPC methods (server→client action messages)
  // ====================================================================

  updateBottomRightCellValues(cellData: CellData[]): void;
  updateTopLeftCellValues(cellData: CellData[]): void;
  updateTopRightCellValues(cellData: CellData[]): void;
  updateBottomLeftCellValues(cellData: CellData[]): void;
  updateFormulaBar(possibleName: string, col: number, row: number): void;
  invalidCellAddress(): void;
  showSelectedCell(
    name: string,
    col: number,
    row: number,
    cellValue: string,
    formula: boolean,
    locked: boolean,
    initialSelection: boolean
  ): void;
  showActions(actionDetails: SpreadsheetActionDetails[]): void;
  setSelectedCellAndRange(
    name: string,
    col: number,
    row: number,
    c1: number,
    c2: number,
    r1: number,
    r2: number,
    scroll: boolean
  ): void;
  cellsUpdated(updatedCellData: CellData[]): void;
  refreshCellStyles(): void;
  editCellComment(col: number, row: number): void;

  // ====================================================================
  // Property mirrors (state lane) — each setter corresponds to one
  // MirroredProperty<T> on the server side.
  // ====================================================================

  setRowBufferSize(pixels: number): void;
  setColumnBufferSize(pixels: number): void;
  setRows(count: number): void;
  setCols(count: number): void;

  // Sheet-level dimensions + visibility
  setDefRowH(heightInPoints: number): void;
  setDefColW(widthInPixels: number): void;
  setRowH(rowHeightsInPoints: number[] | Float32Array): void;
  setColW(colWidthsInPixels: number[] | Int32Array): void;
  setHiddenColumnIndexes(indexes: number[]): void;
  setHiddenRowIndexes(indexes: number[]): void;
  setLockedColumnIndexes(indexes: number[]): void;
  setLockedRowIndexes(indexes: number[]): void;

  // Sheet navigation
  setSheetIndex(index: number): void;
  setSheetNames(names: string[]): void;
  setVerticalScrollPositions(positions: number[]): void;
  setHorizontalScrollPositions(positions: number[]): void;

  // CSS styling
  setCellStyleToCSSStyle(map: Record<number, string>): void;
  setRowIndexToStyleIndex(map: Record<number, number>): void;
  setColumnIndexToStyleIndex(map: Record<number, number>): void;
  setShiftedCellBorderStyles(cssRules: string[]): void;
  setConditionalFormattingStyles(map: Record<number, string>): void;

  // Grouping (outline)
  setColGroupingData(groups: GroupingData[]): void;
  setRowGroupingData(groups: GroupingData[]): void;
  setColGroupingMax(level: number): void;
  setRowGroupingMax(level: number): void;
  setColGroupingInversed(inversed: boolean): void;
  setRowGroupingInversed(inversed: boolean): void;

  // Misc state
  setReload(triggerReload: boolean): void;
  setSheetProtected(isProtected: boolean): void;
  setWorkbookProtected(isProtected: boolean): void;
  setMergedRegions(regions: MergedRegion[]): void;
  setCellKeysToEditorIdMap(map: Record<string, string>): void;
  setComponentIDtoCellKeysMap(map: Record<string, string>): void;
  setCellComments(map: Record<string, string>): void;
  setCellCommentAuthors(map: Record<string, string>): void;
  setVisibleCellComments(keys: string[]): void;
  setInvalidFormulaCells(keys: Set<string>): void;
  setOverlays(map: Record<string, OverlayInfo>): void;
  setShowCustomEditorOnFocus(show: boolean): void;
  setInvalidFormulaErrorMessage(message: string | null): void;
  setNamedRanges(names: string[]): void;
  setResources(host: HTMLElement, jsonString: string): void;
  setPopupButtons(popupButtons: PopupButtonState[]): void;

  // Freeze pane split positions
  setVerticalSplitPosition(value: number): void;
  setHorizontalSplitPosition(value: number): void;

  // Display flags
  setDisplayGridlines(visible: boolean): void;
  setDisplayRowColHeadings(visible: boolean): void;
  setLockFormatColumns(locked: boolean): void;
  setLockFormatRows(locked: boolean): void;
  setHasActions(hasActions: boolean): void;
  setWorkbookChangeToggle(toggle: boolean): void;

  // Display content
  setInfoLabelValue(label: string): void;
  setHyperlinksTooltips(tooltips: Record<string, string>): void;
  setId(id: string): void;
  setClass(className: string): void;

  // ====================================================================
  // Event-callback registration
  //
  // Each setter wires up one client→server event. The wrapper element
  // re-dispatches each callback's argument as a `CustomEvent` on its
  // host. Event payload shapes vary per callback — see
  // `rpc-client-to-server.ts` for the 17 strongly-typed payload
  // interfaces. Callbacks fire client-side in response to user input.
  //
  // Naming: most callbacks are `setXCallback` where X names the
  // event. A few use `setOnXCallback` matching the GWT-side handler
  // method name.
  // ====================================================================

  setGroupingCollapsedCallback(callback: (event: unknown) => void): void;
  setContextMenuClosedCallback(callback: (event: unknown) => void): void;
  setLevelHeaderClickedCallback(callback: (event: unknown) => void): void;
  setOnSheetScrollCallback(callback: (event: unknown) => void): void;
  setSheetAddressChangedCallback(callback: (event: unknown) => void): void;
  setCellSelectedCallback(callback: (event: unknown) => void): void;
  setCellRangeSelectedCallback(callback: (event: unknown) => void): void;
  setCellAddedToSelectionAndSelectedCallback(callback: (event: unknown) => void): void;
  setCellsAddedToRangeSelectionCallback(callback: (event: unknown) => void): void;
  setRowSelectedCallback(callback: (event: unknown) => void): void;
  setRowAddedToRangeSelectionCallback(callback: (event: unknown) => void): void;
  setColumnSelectedCallback(callback: (event: unknown) => void): void;
  setColumnAddedToSelectionCallback(callback: (event: unknown) => void): void;
  setSelectionIncreasePaintedCallback(callback: (event: unknown) => void): void;
  setSelectionDecreasePaintedCallback(callback: (event: unknown) => void): void;
  setCellValueEditedCallback(callback: (event: unknown) => void): void;
  setSheetSelectedCallback(callback: (event: unknown) => void): void;
  setSheetRenamedCallback(callback: (event: unknown) => void): void;
  setSheetCreatedCallback(callback: (event: unknown) => void): void;
  setCellRangePaintedCallback(callback: (event: unknown) => void): void;
  setDeleteSelectedCellsCallback(callback: (event: unknown) => void): void;
  setLinkCellClickedCallback(callback: (event: unknown) => void): void;
  setRowsResizedCallback(callback: (event: unknown) => void): void;
  setColumnResizedCallback(callback: (event: unknown) => void): void;
  setOnRowAutofitCallback(callback: (event: unknown) => void): void;
  setOnColumnAutofitCallback(callback: (event: unknown) => void): void;
  setOnUndoCallback(callback: (event: unknown) => void): void;
  setOnRedoCallback(callback: (event: unknown) => void): void;
  setSetCellStyleWidthRatiosCallback(callback: (event: unknown) => void): void;
  setProtectedCellWriteAttemptedCallback(callback: (event: unknown) => void): void;
  setOnPasteCallback(callback: (event: unknown) => void): void;
  setClearSelectedCellsOnCutCallback(callback: (event: unknown) => void): void;
  setUpdateCellCommentCallback(callback: (event: unknown) => void): void;
  setOnConnectorInitCallback(callback: (event: unknown) => void): void;
  setContextMenuOpenOnSelectionCallback(callback: (event: unknown) => void): void;
  setActionOnCurrentSelectionCallback(callback: (event: unknown) => void): void;
  setRowHeaderContextMenuOpenCallback(callback: (event: unknown) => void): void;
  setActionOnRowHeaderCallback(callback: (event: unknown) => void): void;
  setColumnHeaderContextMenuOpenCallback(callback: (event: unknown) => void): void;
  setActionOnColumnHeaderCallback(callback: (event: unknown) => void): void;
  setPopupButtonClickCallback(callback: (event: unknown) => void): void;
  setPopupCloseCallback(callback: (event: unknown) => void): void;

  // ====================================================================
  // Internal-widget escape hatch (legacy)
  //
  // The GWT-era wrapper reads `api.spreadsheetWidget.sheetWidget.
  // definedRowHeights` directly to time the initial render. This
  // bypasses the typed API surface and is only present for
  // GWT-blob-compat. The new TS implementation MUST NOT rely on this
  // — Phase 3.6 removes it. Until then, the type is permissive.
  // ====================================================================

  spreadsheetWidget?: {
    sheetWidget?: {
      definedRowHeights?: unknown;
      [key: string]: unknown;
    };
  };

  // ====================================================================
  // Popup-button lifecycle
  // ====================================================================

  onPopupButtonOpened(row: number, column: number, contentId: string, appId: string): void;
  closePopup(row: number, column: number): void;
  addPopupButton(rawState: string): void;
  removePopupButton(rawState: string): void;
}
