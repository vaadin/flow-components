/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { CellData, GroupingData, MergedRegion, OverlayInfo, SpreadsheetActionDetails } from './wire.js';

/**
 * The methods the connector exposes to the {@code <vaadin-spreadsheet>}
 * element ({@code vaadin-spreadsheet.js}), which keeps it as
 * {@code $connector}: each server-to-client RPC call goes to the method
 * of the same name, and each property the server sets to its
 * {@code set<Name>} method.
 *
 * The RPC methods at the top mirror
 * {@link com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc}.
 * The setters below receive the properties, which the server sets
 * directly or through a {@link MirroredProperty};
 * {@code ClientApiContractTest} checks that each one has a setter.
 */
export interface SpreadsheetClientApi {
  // ====================================================================
  // Initial setup / lifecycle
  // ====================================================================

  /** Set the spreadsheet's width as a CSS dimension string. */
  setWidth(width: string): void;
  /** Set the spreadsheet's height as a CSS dimension string. */
  setHeight(height: string): void;
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

  // ====================================================================
  // Popup-button lifecycle
  // ====================================================================

  /**
   * Named for the server's call, not the past tense the earlier draft used:
   * {@code PopupButton.openPopup()} runs
   * {@code callJsFunction("onPopupButtonOpen", ...)}, and a client method
   * under any other name is simply never called.
   */
  onPopupButtonOpen(row: number, column: number, contentId: string, appId: string): void;
  closePopup(row: number, column: number): void;
  addPopupButton(rawState: string): void;
  removePopupButton(rawState: string): void;
}
