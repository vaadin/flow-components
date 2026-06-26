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
import {
    selectRange,
    selectSingleCell,
    type SelectionState,
} from './state/SelectionState.js';
import {
    computeTotalGridSize,
    computeViewportBounds,
    FALLBACK_PPI,
    getDefaultRowHeightPx,
    convertPointsToPixel,
} from './algorithms/ViewportMath.js';
import { toKey } from './algorithms/CellKeys.js';
import { SpreadsheetCellGrid } from './elements/SpreadsheetCellGrid.js';
import { SpreadsheetColumnHeader } from './elements/SpreadsheetColumnHeader.js';
import { SpreadsheetRowHeader } from './elements/SpreadsheetRowHeader.js';
import type { CellGridRenderState } from './elements/types.js';

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
    private workbookProtected = false;
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
    private hyperlinksTooltips: Record<string, string> = {};

    /** Freeze pane split positions. */
    private verticalSplitPosition = 0;
    private horizontalSplitPosition = 0;

    /** Display flags. */
    private displayGridlines = true;
    private displayRowColHeadings = true;
    private lockFormatColumns = false;
    private lockFormatRows = false;
    private hasActions = false;
    private workbookChangeToggle = false;

    /** Display content. */
    private infoLabelValue = '';
    private elementId = '';
    private elementClass = '';

    /**
     * Event-callback registry. Keyed by callback name (e.g.
     * "GroupingCollapsed"). All callback setters write into this map;
     * the Phase 3.3+ event-dispatch code will read from it.
     */
    private callbacks: Record<string, (event: unknown) => void> = {};

    /**
     * Cells currently known to the client, keyed by
     * {@code toKey(col, row)}. Populated by the
     * {@code update*CellValues} RPC methods (slice 134 wires the
     * accessors; the methods themselves stay TODO until later in
     * Phase 3.2 — the connector still needs to dispatch per-quadrant).
     */
    private cellDataByKey: Map<string, CellData> = new Map();

    /** The cell-grid sub-element. Created in the constructor + appended to the host. */
    private readonly cellGrid: SpreadsheetCellGrid;

    /**
     * The column-header sub-element. Sibling of the cell-grid
     * inside the scroll container; renders the column letters
     * (A, B, ..., AA) above the cells. Hidden when
     * {@link headerRowHeight} is 0 (the default until a host
     * calls {@code setHeaderRowHeight}). Slice 143.
     */
    private readonly columnHeader: SpreadsheetColumnHeader;

    /**
     * The row-header sub-element. Sibling of the cell-grid
     * inside the scroll container; renders the row numbers
     * (1, 2, 3, ...) at the left of the cells. Hidden when
     * {@link headerColumnWidth} is 0. Slice 144.
     */
    private readonly rowHeader: SpreadsheetRowHeader;

    /**
     * The header corner box — small gray square where the column
     * + row headers meet at the top-left. Plain {@code <div>}
     * since it has no content; sticky-positioned at (0, 0) of
     * the scroll container so it overlays both header strips at
     * their intersection. Sized to
     * {@code headerColumnWidth × headerRowHeight} on every
     * {@link applyRenderState}; hidden when either dimension is
     * 0. Slice 145.
     */
    private readonly headerCorner: HTMLElement;

    /**
     * Height of the column-header strip in pixels. {@code 0}
     * (default) means no header strip is rendered. The host opts
     * in by calling {@link setHeaderRowHeight}; matches Excel's
     * 18-22px range. Slice 143.
     */
    private headerRowHeight = 0;

    /**
     * Width of the row-header strip in pixels. {@code 0} (default)
     * means no row-header is rendered. The host opts in via
     * {@link setHeaderColumnWidth}; Excel uses 30-40px (wider
     * accommodates the largest row numbers without truncation).
     * Slice 144.
     */
    private headerColumnWidth = 0;

    /**
     * The scroll surface — wraps the cell-grid and provides the
     * scrollbars + viewport bounds. The user scrolls THIS element;
     * the connector listens for {@code scroll} events and recomputes
     * the visible viewport. The cell-grid's intrinsic size is set to
     * the total grid dimensions so the scrollbar track reflects the
     * full scroll extent (slice 140).
     */
    private readonly scrollContainer: HTMLElement;

    /**
     * Resize observer on the scroll container. Fires when the host's
     * box size changes — either initially when the host is attached
     * + given dimensions, or later when the parent layout changes
     * (window resize, container reflow). Each fire triggers
     * {@link recomputeViewport}, which pushes a fresh render state
     * with the new {@code clientWidth} / {@code clientHeight}. The
     * initial fire also serves as the first render, replacing the
     * empty default state assigned in the constructor (slice 142).
     */
    private readonly resizeObserver: ResizeObserver;

    constructor(host: HTMLElement) {
        this.host = host;
        this.cellGrid = new SpreadsheetCellGrid();
        this.columnHeader = new SpreadsheetColumnHeader();
        this.rowHeader = new SpreadsheetRowHeader();
        this.headerCorner = document.createElement('div');
        this.scrollContainer = document.createElement('div');
        this.scrollContainer.className = 'sheet-scroll';
        // CSS Grid layout: 2 columns × 2 rows so each header sits in
        // its own cell. Column 1 = row-header (auto-sized to its
        // intrinsic width); column 2 = cell-grid (auto-sized to its
        // intrinsic width). Same for rows. Sticky positioning on
        // each header pins it to its viewport edge during scroll.
        // Slice 144.
        this.scrollContainer.style.cssText =
            'position:relative;overflow:auto;width:100%;height:100%;'
            + 'display:grid;grid-template-columns:auto auto;'
            + 'grid-template-rows:auto auto;';
        // Header corner at (col 1, row 1) — sticky on both axes
        // so it overlays both header strips at their intersection.
        // Slice 145. Hidden when either header dimension is 0.
        this.headerCorner.className = 'sheet-header-corner';
        this.headerCorner.style.cssText =
            'grid-column:1;grid-row:1;'
            + 'position:sticky;top:0;left:0;z-index:6;'
            + 'background:var(--vaadin-spreadsheet-header-bg, #f4f4f4);'
            + 'border-right:1px solid'
            + ' var(--vaadin-spreadsheet-header-border, #c0c0c0);'
            + 'border-bottom:1px solid'
            + ' var(--vaadin-spreadsheet-header-border, #c0c0c0);'
            + 'box-sizing:border-box;'
            + 'width:0;height:0;';
        // Column-header at (col 2, row 1) — top strip above cells.
        this.columnHeader.style.gridColumn = '2';
        this.columnHeader.style.gridRow = '1';
        // Row-header at (col 1, row 2) — left strip alongside cells.
        this.rowHeader.style.gridColumn = '1';
        this.rowHeader.style.gridRow = '2';
        // Cell-grid at (col 2, row 2).
        this.cellGrid.style.gridColumn = '2';
        this.cellGrid.style.gridRow = '2';
        this.scrollContainer.appendChild(this.headerCorner);
        this.scrollContainer.appendChild(this.columnHeader);
        this.scrollContainer.appendChild(this.rowHeader);
        this.scrollContainer.appendChild(this.cellGrid);
        this.host.appendChild(this.scrollContainer);
        this.scrollContainer.addEventListener('scroll', this.handleScroll);
        // Cell-click delegation (slice 146): the cell-grid dispatches
        // a `cell-click` custom event with detail = { col, row,
        // shiftKey, ctrlKey, metaKey, altKey }. The connector updates
        // its selection + re-renders. Bubble + composed = true so the
        // event crosses the cell-grid's shadow DOM boundary.
        this.cellGrid.addEventListener(
            'cell-click',
            this.handleCellClick as EventListener
        );
        this.resizeObserver = new ResizeObserver(() => this.recomputeViewport());
        this.resizeObserver.observe(this.scrollContainer);
    }

    /**
     * Cell-click handler. Single click = move active cell + collapse
     * selection to that cell. Shift-click = extend the active range
     * to include the new anchor while keeping the existing active
     * cell as the range origin (Excel convention). Ctrl/Meta click
     * is reserved for non-coherent selection (auxRanges) — deferred
     * to a future slice. Slice 146.
     */
    private readonly handleCellClick = (
        e: CustomEvent<{
            col: number;
            row: number;
            shiftKey: boolean;
            ctrlKey: boolean;
            metaKey: boolean;
            altKey: boolean;
        }>
    ): void => {
        const { col, row, shiftKey } = e.detail;
        if (shiftKey) {
            // Extend the active range from the existing active cell
            // (= the anchor) to the clicked coordinate.
            const anchor = this.selection.activeCell;
            this.selection = selectRange(anchor.col, anchor.row, col, row);
        } else {
            this.selection = selectSingleCell(col, row);
        }
        this.recomputeViewport();
    };

    /**
     * Scroll-event handler: recompute the visible viewport from the
     * scroll container's current state + push a fresh render state
     * to the cell-grid. Bound as an arrow function so the listener
     * binding survives without explicit {@code .bind(this)}.
     */
    private readonly handleScroll = (): void => {
        this.recomputeViewport();
    };

    /**
     * Recompute the visible viewport from the scroll container's
     * current scroll position + client dimensions, then push the
     * resulting render state to the cell-grid. Called by the scroll
     * listener {@link handleScroll}, the {@link resizeObserver}, and
     * the wrapper element on data changes. Public so callers (the
     * Lit wrapper's {@code updated} cycle, integration tests) can
     * force a re-render without going through the scroll surface.
     */
    recomputeViewport(): void {
        this.applyRenderState({
            scrollLeft: this.scrollContainer.scrollLeft,
            scrollTop: this.scrollContainer.scrollTop,
            viewportWidth: this.scrollContainer.clientWidth,
            viewportHeight: this.scrollContainer.clientHeight,
        });
    }

    /**
     * Release the scroll + resize listeners. Call this when the host
     * element is being detached + destroyed so the
     * {@link resizeObserver} doesn't keep a reference to the
     * scroll container after teardown.
     */
    dispose(): void {
        this.scrollContainer.removeEventListener('scroll', this.handleScroll);
        this.cellGrid.removeEventListener(
            'cell-click',
            this.handleCellClick as EventListener
        );
        this.resizeObserver.disconnect();
    }

    /** Test-only accessor: the scroll container element. */
    getScrollContainer(): HTMLElement {
        return this.scrollContainer;
    }

    // ====================================================================
    // Render pipeline (slice 134) — compose `CellGridRenderState` from
    // current state + push to the cell-grid sub-element. The full
    // event-driven trigger flow (scroll handler → applyRenderState)
    // wires up when the wrapper flips imports in Phase 3.6.
    // ====================================================================

    /**
     * Recompute the visible viewport + push a fresh
     * {@code CellGridRenderState} to the cell-grid sub-element.
     *
     * The connector calls this in response to scroll events, viewport
     * resize, or any state change that affects rendering (data updates,
     * style changes, selection moves). Slice 134 ships the composition
     * + push; slice 135+ extend each branch (styling, selection,
     * merged-cell overlays, frozen panes).
     *
     * @param opts.scrollLeft Horizontal scroll offset in pixels.
     * @param opts.scrollTop Vertical scroll offset in pixels.
     * @param opts.viewportWidth Viewport pixel width.
     * @param opts.viewportHeight Viewport pixel height.
     * @param opts.ppi Pixels-per-inch for point→pixel row-height
     *                 conversion. Defaults to 96 (Excel's standard).
     *                 Callers that have measured the host's actual PPI
     *                 should pass it for visual stability across
     *                 high-DPR displays.
     */
    applyRenderState(opts: {
        scrollLeft: number;
        scrollTop: number;
        viewportWidth: number;
        viewportHeight: number;
        ppi?: number;
    }): void {
        const state = this.buildRenderState(opts);
        // Size the cell-grid to the full virtual grid so the scroll
        // surface's scrollWidth / scrollHeight reflect actual extent.
        // Cells render at absolute virtual coordinates inside; the
        // scroll container clips + scrolls them visually.
        this.cellGrid.style.width = `${state.totalGridWidthPx}px`;
        this.cellGrid.style.height = `${state.totalGridHeightPx}px`;
        this.cellGrid.renderState = state;
        // Column-header strip (slice 143). Sized + state-pushed only
        // when headerRowHeight > 0 so the default opt-out path stays
        // a no-op — important to avoid feedback loops with the
        // ResizeObserver under wtr parallel.
        if (this.headerRowHeight > 0) {
            this.columnHeader.style.width = `${state.totalGridWidthPx}px`;
            this.columnHeader.style.height = `${this.headerRowHeight}px`;
            this.columnHeader.renderState = {
                viewport: state.viewport,
                colX: state.colX,
                colWidths: state.colWidths,
                headerHeight: this.headerRowHeight,
                totalGridWidthPx: state.totalGridWidthPx,
            };
        }
        // Row-header strip (slice 144). Same opt-in gate as column
        // header — headerColumnWidth must be > 0 to render.
        if (this.headerColumnWidth > 0) {
            this.rowHeader.style.width = `${this.headerColumnWidth}px`;
            this.rowHeader.style.height = `${state.totalGridHeightPx}px`;
            this.rowHeader.renderState = {
                viewport: state.viewport,
                rowY: state.rowY,
                rowHeights: state.rowHeights,
                headerWidth: this.headerColumnWidth,
                totalGridHeightPx: state.totalGridHeightPx,
            };
        }
        // Header corner (slice 145). Sized only when BOTH header
        // dimensions are > 0 so it occupies its grid cell with
        // matching width + height. When either is 0, the corner
        // collapses to a 0×0 box and isn't visible.
        if (this.headerRowHeight > 0 && this.headerColumnWidth > 0) {
            this.headerCorner.style.width = `${this.headerColumnWidth}px`;
            this.headerCorner.style.height = `${this.headerRowHeight}px`;
        } else {
            this.headerCorner.style.width = '0px';
            this.headerCorner.style.height = '0px';
        }
    }

    /**
     * Pure: build a {@code CellGridRenderState} from the current
     * internal state + the given scroll / viewport / PPI. Exposed for
     * tests to call without going through the DOM (the public
     * {@link applyRenderState} writes the result to the cell-grid;
     * this lets tests inspect it directly).
     */
    buildRenderState(opts: {
        scrollLeft: number;
        scrollTop: number;
        viewportWidth: number;
        viewportHeight: number;
        ppi?: number;
    }): CellGridRenderState {
        const ppi = opts.ppi ?? FALLBACK_PPI;
        const defaultRowHeightPx = getDefaultRowHeightPx(this.defRowHeight, ppi);
        const hiddenRowSet = new Set(this.hiddenRowIndexes);

        const definedRowsLength = this.rowHeights.length;
        const rowHeightForIndex = (row: number): number => {
            if (hiddenRowSet.has(row)) {
                return 0;
            }
            if (row > definedRowsLength) {
                return defaultRowHeightPx;
            }
            return convertPointsToPixel(this.rowHeights[row - 1], ppi);
        };

        const hiddenColSet = new Set(this.hiddenColumnIndexes);
        const colWidthForIndex = (col: number): number => {
            if (hiddenColSet.has(col)) {
                return 0;
            }
            if (col > this.colWidths.length) {
                return this.defColWidth;
            }
            return this.colWidths[col - 1];
        };

        const viewport = computeViewportBounds({
            scrollLeft: opts.scrollLeft,
            scrollTop: opts.scrollTop,
            scrollViewWidth: opts.viewportWidth,
            scrollViewHeight: opts.viewportHeight,
            horizontalSplitPosition: this.horizontalSplitPosition,
            verticalSplitPosition: this.verticalSplitPosition,
            columnBufferSize: this.columnBufferSize,
            rowBufferSize: this.rowBufferSize,
            maxColumns: this.cols,
            maxRows: this.rows,
            definedRows: definedRowsLength,
            defaultRowHeightPx,
            getColWidthActual: colWidthForIndex,
            getRowHeight: rowHeightForIndex,
        });

        // Build per-visible-index arrays for colX, rowY, widths, heights.
        const colX: number[] = [];
        const colWidths: number[] = [];
        let cumulativeX = viewport.firstColumnPosition;
        for (let col = viewport.firstColumnIndex; col <= viewport.lastColumnIndex; col++) {
            const w = colWidthForIndex(col);
            colX.push(cumulativeX);
            colWidths.push(w);
            cumulativeX += w;
        }

        const rowY: number[] = [];
        const rowHeights: number[] = [];
        let cumulativeY = viewport.firstRowPosition;
        for (let row = viewport.firstRowIndex; row <= viewport.lastRowIndex; row++) {
            const h = rowHeightForIndex(row);
            rowY.push(cumulativeY);
            rowHeights.push(h);
            cumulativeY += h;
        }

        // Frozen columns: cols 1..horizontalSplitPosition (1-based,
        // inclusive). Empty when no freeze is active.
        const frozenColX: number[] = [];
        const frozenColWidths: number[] = [];
        let frozenCumX = 0;
        for (let col = 1; col <= this.horizontalSplitPosition; col++) {
            const w = colWidthForIndex(col);
            frozenColX.push(frozenCumX);
            frozenColWidths.push(w);
            frozenCumX += w;
        }

        // Frozen rows: rows 1..verticalSplitPosition.
        const frozenRowY: number[] = [];
        const frozenRowHeights: number[] = [];
        let frozenCumY = 0;
        for (let row = 1; row <= this.verticalSplitPosition; row++) {
            const h = rowHeightForIndex(row);
            frozenRowY.push(frozenCumY);
            frozenRowHeights.push(h);
            frozenCumY += h;
        }

        // Total grid dimensions in pixels — used by the scroll surface
        // to establish scroll extent. Row heights are passed as points
        // converted via the active PPI; column widths come through
        // as-is (already pixels).
        const definedRowHeightsPx: number[] = [];
        for (let row = 1; row <= definedRowsLength; row++) {
            definedRowHeightsPx.push(convertPointsToPixel(this.rowHeights[row - 1], ppi));
        }
        const { totalWidthPx, totalHeightPx } = computeTotalGridSize({
            rows: this.rows,
            cols: this.cols,
            definedRowHeightsPx,
            definedColWidthsPx: Array.from(this.colWidths),
            hiddenRows: hiddenRowSet,
            hiddenColumns: hiddenColSet,
            defaultRowHeightPx,
            defaultColWidthPx: this.defColWidth,
        });

        return {
            viewport,
            cellData: this.cellDataByKey,
            colX,
            rowY,
            colWidths,
            rowHeights,
            cellStyleToCSSStyle: this.cellStyleToCSSStyle,
            mergedRegions: this.mergedRegions,
            selection: this.selection,
            verticalSplitPosition: this.verticalSplitPosition,
            horizontalSplitPosition: this.horizontalSplitPosition,
            frozenColX,
            frozenColWidths,
            frozenRowY,
            frozenRowHeights,
            totalGridWidthPx: totalWidthPx,
            totalGridHeightPx: totalHeightPx,
            scrollLeft: opts.scrollLeft,
            scrollTop: opts.scrollTop,
        };
    }

    /** Test-only accessor: the cell-grid sub-element this connector owns. */
    getCellGrid(): SpreadsheetCellGrid {
        return this.cellGrid;
    }

    /** Test-only accessor: the column-header sub-element (slice 143). */
    getColumnHeader(): SpreadsheetColumnHeader {
        return this.columnHeader;
    }

    /** Test-only accessor: the row-header sub-element (slice 144). */
    getRowHeader(): SpreadsheetRowHeader {
        return this.rowHeader;
    }

    /** Test-only accessor: the header-corner box (slice 145). */
    getHeaderCorner(): HTMLElement {
        return this.headerCorner;
    }

    /**
     * Set the column-header strip's height in pixels. {@code 0}
     * (default) means no header is rendered — useful for headless
     * use cases and as a clean default. Excel's typical header is
     * 18-22 px. Slice 143.
     */
    setHeaderRowHeight(pixels: number): void {
        this.headerRowHeight = pixels;
    }

    /** Current column-header strip height in pixels. Slice 143. */
    getHeaderRowHeight(): number {
        return this.headerRowHeight;
    }

    /**
     * Set the row-header strip's width in pixels. {@code 0}
     * (default) disables the strip. Excel uses 30-40px; wider
     * accommodates the largest row numbers without truncation.
     * Slice 144.
     */
    setHeaderColumnWidth(pixels: number): void {
        this.headerColumnWidth = pixels;
    }

    /** Current row-header strip width in pixels. Slice 144. */
    getHeaderColumnWidth(): number {
        return this.headerColumnWidth;
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

    updateBottomRightCellValues(cellData: CellData[]): void {
        this.storeCellData(cellData);
    }
    updateTopLeftCellValues(cellData: CellData[]): void {
        this.storeCellData(cellData);
    }
    updateTopRightCellValues(cellData: CellData[]): void {
        this.storeCellData(cellData);
    }
    updateBottomLeftCellValues(cellData: CellData[]): void {
        this.storeCellData(cellData);
    }

    /**
     * Stash each cell's data into {@link cellDataByKey} keyed by
     * its wire-format key. The Phase 3.2-cont. quadrant-aware rendering
     * (top-left / top-right / bottom-left / bottom-right cell-grid
     * sub-trees) can iterate the map at render time; until then, all
     * four quadrants share one map (the cell-grid's renderState picks
     * out the visible subset).
     */
    private storeCellData(cellData: CellData[]): void {
        for (const cell of cellData) {
            this.cellDataByKey.set(toKey(cell.col, cell.row), cell);
        }
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

    setWorkbookProtected(isProtected: boolean): void {
        this.workbookProtected = isProtected;
    }

    // Freeze pane split positions
    setVerticalSplitPosition(value: number): void {
        this.verticalSplitPosition = value;
    }
    setHorizontalSplitPosition(value: number): void {
        this.horizontalSplitPosition = value;
    }

    // Display flags
    setDisplayGridlines(visible: boolean): void {
        this.displayGridlines = visible;
    }
    setDisplayRowColHeadings(visible: boolean): void {
        this.displayRowColHeadings = visible;
    }
    setLockFormatColumns(locked: boolean): void {
        this.lockFormatColumns = locked;
    }
    setLockFormatRows(locked: boolean): void {
        this.lockFormatRows = locked;
    }
    setHasActions(hasActions: boolean): void {
        this.hasActions = hasActions;
    }
    setWorkbookChangeToggle(toggle: boolean): void {
        this.workbookChangeToggle = toggle;
    }

    // Display content
    setInfoLabelValue(label: string): void {
        this.infoLabelValue = label;
    }
    setHyperlinksTooltips(tooltips: Record<string, string>): void {
        this.hyperlinksTooltips = tooltips;
    }
    setId(id: string): void {
        this.elementId = id;
    }
    setClass(className: string): void {
        this.elementClass = className;
    }

    // ====================================================================
    // Event-callback registration (Phase 3.3+ dispatch)
    //
    // Each setter stores the callback in `this.callbacks` keyed by its
    // event name. Future phases (cell-input, scroll, selection) read
    // from this map to dispatch user events back to the server. The
    // wrapper element subscribes by passing a function that
    // re-dispatches each callback's argument as a CustomEvent.
    // ====================================================================

    setGroupingCollapsedCallback(callback: (event: unknown) => void): void {
        this.callbacks.GroupingCollapsed = callback;
    }
    setContextMenuClosedCallback(callback: (event: unknown) => void): void {
        this.callbacks.ContextMenuClosed = callback;
    }
    setLevelHeaderClickedCallback(callback: (event: unknown) => void): void {
        this.callbacks.LevelHeaderClicked = callback;
    }
    setOnSheetScrollCallback(callback: (event: unknown) => void): void {
        this.callbacks.OnSheetScroll = callback;
    }
    setSheetAddressChangedCallback(callback: (event: unknown) => void): void {
        this.callbacks.SheetAddressChanged = callback;
    }
    setCellSelectedCallback(callback: (event: unknown) => void): void {
        this.callbacks.CellSelected = callback;
    }
    setCellRangeSelectedCallback(callback: (event: unknown) => void): void {
        this.callbacks.CellRangeSelected = callback;
    }
    setCellAddedToSelectionAndSelectedCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.CellAddedToSelectionAndSelected = callback;
    }
    setCellsAddedToRangeSelectionCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.CellsAddedToRangeSelection = callback;
    }
    setRowSelectedCallback(callback: (event: unknown) => void): void {
        this.callbacks.RowSelected = callback;
    }
    setRowAddedToRangeSelectionCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.RowAddedToRangeSelection = callback;
    }
    setColumnSelectedCallback(callback: (event: unknown) => void): void {
        this.callbacks.ColumnSelected = callback;
    }
    setColumnAddedToSelectionCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.ColumnAddedToSelection = callback;
    }
    setSelectionIncreasePaintedCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.SelectionIncreasePainted = callback;
    }
    setSelectionDecreasePaintedCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.SelectionDecreasePainted = callback;
    }
    setCellValueEditedCallback(callback: (event: unknown) => void): void {
        this.callbacks.CellValueEdited = callback;
    }
    setSheetSelectedCallback(callback: (event: unknown) => void): void {
        this.callbacks.SheetSelected = callback;
    }
    setSheetRenamedCallback(callback: (event: unknown) => void): void {
        this.callbacks.SheetRenamed = callback;
    }
    setSheetCreatedCallback(callback: (event: unknown) => void): void {
        this.callbacks.SheetCreated = callback;
    }
    setCellRangePaintedCallback(callback: (event: unknown) => void): void {
        this.callbacks.CellRangePainted = callback;
    }
    setDeleteSelectedCellsCallback(callback: (event: unknown) => void): void {
        this.callbacks.DeleteSelectedCells = callback;
    }
    setLinkCellClickedCallback(callback: (event: unknown) => void): void {
        this.callbacks.LinkCellClicked = callback;
    }
    setRowsResizedCallback(callback: (event: unknown) => void): void {
        this.callbacks.RowsResized = callback;
    }
    setColumnResizedCallback(callback: (event: unknown) => void): void {
        this.callbacks.ColumnResized = callback;
    }
    setOnRowAutofitCallback(callback: (event: unknown) => void): void {
        this.callbacks.OnRowAutofit = callback;
    }
    setOnColumnAutofitCallback(callback: (event: unknown) => void): void {
        this.callbacks.OnColumnAutofit = callback;
    }
    setOnUndoCallback(callback: (event: unknown) => void): void {
        this.callbacks.OnUndo = callback;
    }
    setOnRedoCallback(callback: (event: unknown) => void): void {
        this.callbacks.OnRedo = callback;
    }
    setSetCellStyleWidthRatiosCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.SetCellStyleWidthRatios = callback;
    }
    setProtectedCellWriteAttemptedCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.ProtectedCellWriteAttempted = callback;
    }
    setOnPasteCallback(callback: (event: unknown) => void): void {
        this.callbacks.OnPaste = callback;
    }
    setClearSelectedCellsOnCutCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.ClearSelectedCellsOnCut = callback;
    }
    setUpdateCellCommentCallback(callback: (event: unknown) => void): void {
        this.callbacks.UpdateCellComment = callback;
    }
    setOnConnectorInitCallback(callback: (event: unknown) => void): void {
        this.callbacks.OnConnectorInit = callback;
    }
    setContextMenuOpenOnSelectionCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.ContextMenuOpenOnSelection = callback;
    }
    setActionOnCurrentSelectionCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.ActionOnCurrentSelection = callback;
    }
    setRowHeaderContextMenuOpenCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.RowHeaderContextMenuOpen = callback;
    }
    setActionOnRowHeaderCallback(callback: (event: unknown) => void): void {
        this.callbacks.ActionOnRowHeader = callback;
    }
    setColumnHeaderContextMenuOpenCallback(
        callback: (event: unknown) => void
    ): void {
        this.callbacks.ColumnHeaderContextMenuOpen = callback;
    }
    setActionOnColumnHeaderCallback(callback: (event: unknown) => void): void {
        this.callbacks.ActionOnColumnHeader = callback;
    }
    setPopupButtonClickCallback(callback: (event: unknown) => void): void {
        this.callbacks.PopupButtonClick = callback;
    }
    setPopupCloseCallback(callback: (event: unknown) => void): void {
        this.callbacks.PopupClose = callback;
    }

    /**
     * Legacy escape hatch — see interface javadoc. The GWT-era wrapper
     * reaches into this property to read `definedRowHeights` directly.
     * The new TS implementation MUST NOT rely on this; we expose it
     * as `undefined` for runtime safety.
     */
    readonly spreadsheetWidget = undefined;

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
