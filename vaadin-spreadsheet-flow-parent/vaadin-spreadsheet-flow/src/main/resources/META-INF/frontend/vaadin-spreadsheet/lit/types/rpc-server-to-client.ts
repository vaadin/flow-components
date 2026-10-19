/**
 * @license
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
  SizeRuns,
  SpreadsheetActionDetails
} from './wire.js';

/**
 * The methods the connector exposes to the {@code <vaadin-spreadsheet>}
 * element ({@code vaadin-spreadsheet.js}), which keeps it as
 * {@code $connector}: each server-to-client RPC call goes to the method
 * of the same name, and each property the server sets to its
 * {@code set<Name>} method.
 *
 * The RPC methods at the top mirror
 * {@link com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc}.
 * The setters below receive the properties, which the server declares
 * as a {@code ClientProperty} each;
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
  updateFormulaBar(possibleName: string | null, col: number, row: number): void;
  invalidCellAddress(): void;
  showSelectedCell(
    name: string,
    col: number,
    row: number,
    cellValue: string,
    formula: boolean,
    locked: boolean,
    initialSelection: boolean,
    keepView?: boolean
  ): void;
  showActions(actionDetails: SpreadsheetActionDetails[] | null): void;
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
  cellsReplaced(cellData: CellData[]): void;
  refreshCellStyles(): void;
  editCellComment(col: number, row: number): void;

  // ====================================================================
  // Property mirrors (state lane) — each setter corresponds to one
  // ClientProperty<T> on the server side.
  // ====================================================================

  setRowBufferSize(pixels: number): void;
  setColumnBufferSize(pixels: number): void;
  setRows(count: number): void;
  setCols(count: number): void;

  // Sheet-level dimensions + visibility
  setDefRowH(heightInPoints: number): void;
  setDefColW(widthInPixels: number): void;
  setRowH(rowHeightsInPoints: number[] | Float32Array | SizeRuns | null): void;
  setColW(colWidthsInPixels: number[] | Int32Array | SizeRuns | null): void;
  setHiddenColumnIndexes(indexes: number[] | null): void;
  setHiddenRowIndexes(indexes: number[] | null): void;
  setLockedColumnIndexes(indexes: number[] | null): void;
  setLockedRowIndexes(indexes: number[] | null): void;

  // Sheet navigation
  setSheetIndex(index: number): void;
  setSheetNames(names: string[] | null): void;
  setVerticalScrollPositions(positions: number[] | null): void;
  setHorizontalScrollPositions(positions: number[] | null): void;

  // CSS styling
  setCellStyleToCSSStyle(map: Record<number, string> | null): void;
  setRowIndexToStyleIndex(map: Record<number, number> | null): void;
  setColumnIndexToStyleIndex(map: Record<number, number> | null): void;
  setShiftedCellBorderStyles(cssRules: string[] | null): void;
  setConditionalFormattingStyles(map: Record<number, string> | null): void;

  // Grouping (outline)
  setColGroupingData(groups: GroupingData[] | null): void;
  setRowGroupingData(groups: GroupingData[] | null): void;
  setColGroupingMax(level: number): void;
  setRowGroupingMax(level: number): void;
  setColGroupingInversed(inversed: boolean): void;
  setRowGroupingInversed(inversed: boolean): void;

  // Misc state
  setReload(triggerReload: boolean): void;
  setSheetProtected(isProtected: boolean): void;
  setWorkbookProtected(isProtected: boolean): void;
  setMergedRegions(regions: MergedRegion[] | null): void;
  setCellKeysToEditorIdMap(map: Record<string, string> | null): void;
  setComponentIDtoCellKeysMap(map: Record<string, string>): void;
  setCellComments(map: Record<string, string> | null): void;
  setCellCommentAuthors(map: Record<string, string> | null): void;
  setVisibleCellComments(keys: string[] | null): void;
  setInvalidFormulaCells(keys: string[] | null): void;
  setOverlays(map: Record<string, OverlayInfo> | null): void;
  setShowCustomEditorOnFocus(show: boolean): void;
  setHasComponentFactory(has: boolean): void;
  setInvalidFormulaErrorMessage(message: string | null): void;
  setNamedRanges(names: string[] | null): void;
  setResources(host: HTMLElement, keys: string[] | null): void;

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
  setHyperlinksTooltips(tooltips: Record<string, string> | null): void;

  // ====================================================================
  // Popup-button lifecycle
  // ====================================================================

  /**
   * Named for the server's call, not the past tense the earlier draft used:
   * {@code PopupButton.openPopup()} runs
   * {@code callJsFunction("onPopupButtonOpen", ...)}, and a client method
   * under any other name is simply never called.
   */
  onPopupButtonOpen(row: number, column: number, contentId: number, appId: string): void;
  closePopup(row: number, column: number): void;
  addPopupButton(state: PopupButtonState): void;
  removePopupButton(state: PopupButtonState): void;
}
