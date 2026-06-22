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
    SpreadsheetActionDetails,
} from './types/wire.js';
import type { SpreadsheetClientApi } from './types/rpc-server-to-client.js';
import { selectSingleCell, type SelectionState } from './state/SelectionState.js';

/**
 * The new Spreadsheet client class — the replacement for the
 * GWT-compiled {@code Spreadsheet} class exported from
 * {@code spreadsheet-export.js}. The Lit wrapper at
 * {@code ../vaadin-spreadsheet.js} holds a reference to an instance of
 * this class as {@code this.api} once Phase 3.6 flips the import.
 *
 * Today this class is a **type-safe stub**. Every method on
 * {@link SpreadsheetClientApi} is implemented with a minimal placeholder
 * (typically just storing the argument into a private field, or a
 * {@code TODO} comment naming the phase that fills it in). The
 * TypeScript compiler enforces that every method the interface declares
 * is present here, so the team gradually fills in real implementations
 * without losing track of any.
 *
 * Implementation roadmap (per
 * {@code refactor-artifacts/TIER3_PLAN.md} § 6):
 *
 * - **Phase 3.2** — Cell grid + virtual scrolling. The
 *   `update*CellValues` / `setRowH` / `setColW` / `notifyStateChanges`
 *   methods get real implementations that build / recycle DOM.
 * - **Phase 3.3** — Editing + formula bar. `updateFormulaBar`,
 *   `showSelectedCell`, `setSelectedCellAndRange` get real
 *   implementations.
 * - **Phase 3.4** — Vaadin integration. `setResources` +
 *   custom-editor + popup-button methods get real implementations.
 * - **Phase 3.5** — Polish. Remaining setters + edge cases.
 *
 * Until Phase 3.6 (rollout), this class is NOT loaded by the Lit
 * wrapper — it exists alongside the GWT bundle. Only when the wrapper's
 * `import { Spreadsheet } from './spreadsheet-export.js'` flips to
 * `import { Spreadsheet } from './lit/Spreadsheet.js'` does this code
 * actually run.
 */
export class Spreadsheet implements SpreadsheetClientApi {
    // ====================================================================
    // Internal state — placeholder fields. The real Phase 3.2+ class
    // will replace these with subsystem-specific managers (cell grid,
    // virtual scroller, selection state, etc.).
    // ====================================================================

    /** The host element the spreadsheet renders into. */
    private readonly host: HTMLElement;

    /** Total grid dimensions. */
    private rows = 0;
    private cols = 0;

    /** Sheet navigation. */
    private sheetIndex = 0;
    private sheetNames: string[] = [];

    /** Selection state. */
    private selection: SelectionState = selectSingleCell(1, 1);

    /** Row / column geometry. */
    private rowHeights: Float32Array | number[] = [];
    private colWidths: Int32Array | number[] = [];
    private defRowHeight = 15; // points
    private defColWidth = 64; // pixels

    /** Hidden / locked indexes. */
    private hiddenRowIndexes: number[] = [];
    private hiddenColumnIndexes: number[] = [];
    private lockedRowIndexes: number[] = [];
    private lockedColumnIndexes: number[] = [];

    /** Merged regions. */
    private mergedRegions: MergedRegion[] = [];

    /** CSS styling state. */
    private cellStyleToCSSStyle: Record<number, string> = {};
    private rowIndexToStyleIndex: Record<number, number> = {};
    private columnIndexToStyleIndex: Record<number, number> = {};
    private shiftedCellBorderStyles: string[] = [];
    private conditionalFormattingStyles: Record<number, string> = {};

    /** Grouping (outline) state. */
    private colGroupingData: GroupingData[] = [];
    private rowGroupingData: GroupingData[] = [];
    private colGroupingMax = 0;
    private rowGroupingMax = 0;
    private colGroupingInversed = false;
    private rowGroupingInversed = false;

    /** Buffer + scroll state. */
    private rowBufferSize = 200;
    private columnBufferSize = 200;
    private verticalScrollPositions: number[] = [];
    private horizontalScrollPositions: number[] = [];

    /** Misc. */
    private sheetProtected = false;
    private showCustomEditorOnFocus = false;
    private invalidFormulaErrorMessage: string | null = null;
    private namedRanges: string[] = [];
    private cellComments: Record<string, string> = {};
    private cellCommentAuthors: Record<string, string> = {};
    private visibleCellComments: string[] = [];
    private invalidFormulaCells: Set<string> = new Set();
    private overlays: Record<string, OverlayInfo> = {};
    private cellKeysToEditorIdMap: Record<string, string> = {};
    private componentIDtoCellKeysMap: Record<string, string> = {};

    constructor(host: HTMLElement) {
        this.host = host;
    }

    // ====================================================================
    // Lifecycle (Phase 3.2 wires up the real DOM)
    // ====================================================================

    setWidth(width: string): void {
        this.host.style.width = width;
    }

    setHeight(height: string): void {
        this.host.style.height = height;
    }

    relayout(): void {
        // TODO Phase 3.2 — re-measure viewport, repopulate visible cells.
    }

    resize(): void {
        // TODO Phase 3.2 — recompute viewport bounds on container resize.
    }

    notifyStateChanges(_changedPropertyNames: string[], _firstUpdate: boolean): void {
        // TODO Phase 3.2 — fold the property batch into a single render pass.
    }

    // ====================================================================
    // 12 explicit RPC methods (server→client action messages)
    // ====================================================================

    updateBottomRightCellValues(_cellData: CellData[]): void {
        // TODO Phase 3.2 — update the main viewport's cell DOM.
    }
    updateTopLeftCellValues(_cellData: CellData[]): void {
        // TODO Phase 3.2 — update the top-left frozen quadrant.
    }
    updateTopRightCellValues(_cellData: CellData[]): void {
        // TODO Phase 3.2 — update the top-right frozen quadrant.
    }
    updateBottomLeftCellValues(_cellData: CellData[]): void {
        // TODO Phase 3.2 — update the bottom-left frozen quadrant.
    }
    updateFormulaBar(_possibleName: string, _col: number, _row: number): void {
        // TODO Phase 3.3 — update formula bar display + named-range dropdown.
    }
    invalidCellAddress(): void {
        // TODO Phase 3.3 — flash the address bar to indicate invalid input.
    }
    showSelectedCell(
        _name: string,
        _col: number,
        _row: number,
        _cellValue: string,
        _formula: boolean,
        _locked: boolean,
        _initialSelection: boolean
    ): void {
        // TODO Phase 3.3 — display the selected cell in the formula bar.
    }
    showActions(_actionDetails: SpreadsheetActionDetails[]): void {
        // TODO Phase 3.5 — show context menu / actions popup.
    }
    setSelectedCellAndRange(
        _name: string,
        _col: number,
        _row: number,
        _c1: number,
        _c2: number,
        _r1: number,
        _r2: number,
        _scroll: boolean
    ): void {
        // TODO Phase 3.2 — apply the server-supplied selection state.
    }
    cellsUpdated(_updatedCellData: CellData[]): void {
        // TODO Phase 3.2 — repaint affected cells.
    }
    refreshCellStyles(): void {
        // TODO Phase 3.2 — flush the cell-style cache + repaint.
    }
    editCellComment(_col: number, _row: number): void {
        // TODO Phase 3.5 — open the comment-edit dialog.
    }

    // ====================================================================
    // Property mirrors — store the value; downstream phases will USE
    // these fields when rendering / scrolling / responding to input.
    // ====================================================================

    setRowBufferSize(pixels: number): void {
        this.rowBufferSize = pixels;
    }
    setColumnBufferSize(pixels: number): void {
        this.columnBufferSize = pixels;
    }
    setRows(count: number): void {
        this.rows = count;
    }
    setCols(count: number): void {
        this.cols = count;
    }
    setDefRowH(heightInPoints: number): void {
        this.defRowHeight = heightInPoints;
    }
    setDefColW(widthInPixels: number): void {
        this.defColWidth = widthInPixels;
    }
    setRowH(rowHeightsInPoints: number[] | Float32Array): void {
        this.rowHeights = rowHeightsInPoints;
    }
    setColW(colWidthsInPixels: number[] | Int32Array): void {
        this.colWidths = colWidthsInPixels;
    }
    setHiddenColumnIndexes(indexes: number[]): void {
        this.hiddenColumnIndexes = indexes;
    }
    setHiddenRowIndexes(indexes: number[]): void {
        this.hiddenRowIndexes = indexes;
    }
    setLockedColumnIndexes(indexes: number[]): void {
        this.lockedColumnIndexes = indexes;
    }
    setLockedRowIndexes(indexes: number[]): void {
        this.lockedRowIndexes = indexes;
    }
    setSheetIndex(index: number): void {
        this.sheetIndex = index;
    }
    setSheetNames(names: string[]): void {
        this.sheetNames = names;
    }
    setVerticalScrollPositions(positions: number[]): void {
        this.verticalScrollPositions = positions;
    }
    setHorizontalScrollPositions(positions: number[]): void {
        this.horizontalScrollPositions = positions;
    }
    setCellStyleToCSSStyle(map: Record<number, string>): void {
        this.cellStyleToCSSStyle = map;
    }
    setRowIndexToStyleIndex(map: Record<number, number>): void {
        this.rowIndexToStyleIndex = map;
    }
    setColumnIndexToStyleIndex(map: Record<number, number>): void {
        this.columnIndexToStyleIndex = map;
    }
    setShiftedCellBorderStyles(cssRules: string[]): void {
        this.shiftedCellBorderStyles = cssRules;
    }
    setConditionalFormattingStyles(map: Record<number, string>): void {
        this.conditionalFormattingStyles = map;
    }
    setColGroupingData(groups: GroupingData[]): void {
        this.colGroupingData = groups;
    }
    setRowGroupingData(groups: GroupingData[]): void {
        this.rowGroupingData = groups;
    }
    setColGroupingMax(level: number): void {
        this.colGroupingMax = level;
    }
    setRowGroupingMax(level: number): void {
        this.rowGroupingMax = level;
    }
    setColGroupingInversed(inversed: boolean): void {
        this.colGroupingInversed = inversed;
    }
    setRowGroupingInversed(inversed: boolean): void {
        this.rowGroupingInversed = inversed;
    }
    setReload(_triggerReload: boolean): void {
        // TODO Phase 3.2 — trigger full re-render on reload signal.
    }
    setSheetProtected(isProtected: boolean): void {
        this.sheetProtected = isProtected;
    }
    setMergedRegions(regions: MergedRegion[]): void {
        this.mergedRegions = regions;
    }
    setCellKeysToEditorIdMap(map: Record<string, string>): void {
        this.cellKeysToEditorIdMap = map;
    }
    setComponentIDtoCellKeysMap(map: Record<string, string>): void {
        this.componentIDtoCellKeysMap = map;
    }
    setCellComments(map: Record<string, string>): void {
        this.cellComments = map;
    }
    setCellCommentAuthors(map: Record<string, string>): void {
        this.cellCommentAuthors = map;
    }
    setVisibleCellComments(keys: string[]): void {
        this.visibleCellComments = keys;
    }
    setInvalidFormulaCells(keys: Set<string>): void {
        this.invalidFormulaCells = keys;
    }
    setOverlays(map: Record<string, OverlayInfo>): void {
        this.overlays = map;
    }
    setShowCustomEditorOnFocus(show: boolean): void {
        this.showCustomEditorOnFocus = show;
    }
    setInvalidFormulaErrorMessage(message: string | null): void {
        this.invalidFormulaErrorMessage = message;
    }
    setNamedRanges(names: string[]): void {
        this.namedRanges = names;
    }
    setResources(_host: HTMLElement, _jsonString: string): void {
        // TODO Phase 3.4 — register image/resource map for overlays.
    }
    setPopupButtons(_popupButtons: PopupButtonState[]): void {
        // TODO Phase 3.5 — register popup-button anchors.
    }

    // ====================================================================
    // Popup-button lifecycle (Phase 3.5)
    // ====================================================================

    onPopupButtonOpened(
        _row: number,
        _column: number,
        _contentId: string,
        _appId: string
    ): void {
        // TODO Phase 3.5 — handle popup-button open event from server.
    }
    closePopup(_row: number, _column: number): void {
        // TODO Phase 3.5 — close active popup at coordinates.
    }
    addPopupButton(_rawState: string): void {
        // TODO Phase 3.5 — register a popup-button anchor.
    }
    removePopupButton(_rawState: string): void {
        // TODO Phase 3.5 — unregister a popup-button anchor.
    }

    // ====================================================================
    // Read accessors — used by tests + the renderer + the selection
    // controller (coming in slice 122).
    // ====================================================================

    getRows(): number {
        return this.rows;
    }
    getCols(): number {
        return this.cols;
    }
    getSheetIndex(): number {
        return this.sheetIndex;
    }
    getSheetNames(): readonly string[] {
        return this.sheetNames;
    }
    getSelection(): SelectionState {
        return this.selection;
    }
    getMergedRegions(): readonly MergedRegion[] {
        return this.mergedRegions;
    }
    isSheetProtected(): boolean {
        return this.sheetProtected;
    }
}
