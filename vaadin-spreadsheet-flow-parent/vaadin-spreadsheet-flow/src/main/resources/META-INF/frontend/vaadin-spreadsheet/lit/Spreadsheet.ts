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
} from './types/wire.js';
import type { SpreadsheetClientApi } from './types/rpc-server-to-client.js';
import {
  normalizedRange,
  selectRange,
  selectSingleCell,
  type CellRange,
  type SelectionState
} from './state/SelectionState.js';
import type {
  CellRangeSelectedData,
  CellSelectedData,
  CellValueEditedData,
  ColumnSelectedData,
  RowSelectedData,
  SpreadsheetEvent
} from './types/rpc-client-to-server.js';
import type { CellEditorState, PositionedOverlay, PositionedPopupButton } from './elements/types.js';
import {
  computeTotalGridSize,
  computeViewportBounds,
  FALLBACK_PPI,
  getDefaultRowHeightPx,
  convertPointsToPixel
} from './algorithms/ViewportMath.js';
import { toKey } from './algorithms/CellKeys.js';
import { serializeToTsv } from './algorithms/Tsv.js';
import { SpreadsheetCellGrid } from './elements/SpreadsheetCellGrid.js';
import {
  SpreadsheetColumnHeader,
  GROUPING_BUTTON_SIZE_PX,
  type PositionedGrouping
} from './elements/SpreadsheetColumnHeader.js';
import { SpreadsheetRowHeader } from './elements/SpreadsheetRowHeader.js';
import { SpreadsheetSheetTabs } from './elements/SpreadsheetSheetTabs.js';
import { SpreadsheetFormulaBar } from './elements/SpreadsheetFormulaBar.js';
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

  /**
   * The HTMLElement we dispatch server-bound events on (the
   * wrapper `<vaadin-spreadsheet>` from the Flow side, or a
   * plain test div). DOM is appended to {@link renderTarget}
   * — usually the same element, but a ShadowRoot when the
   * wrapper passes its `renderRoot`. Slice 162.
   */
  private readonly host: HTMLElement;

  /**
   * Where DOM gets appended. When the wrapper is a LitElement,
   * this is its shadow root (passed as the second constructor
   * arg). Otherwise it's the host element itself. Slice 162.
   */
  private readonly renderTarget: HTMLElement | ShadowRoot;

  /** Total grid dimensions. */
  private rows = 0;
  private cols = 0;

  /** Sheet navigation. */
  private sheetIndex = 0;
  private sheetNames: string[] = [];

  /** Selection state. */
  private selection: SelectionState = selectSingleCell(1, 1);

  /**
   * Cell-editor state (slice 152). Null when no editor is
   * active. When set, the cell-grid renders an editable
   * {@code <input>} at the cell's coordinates; the user types,
   * then Enter/Tab commits (slice 153) and Escape cancels.
   */
  private editor: CellEditorState | null = null;

  /**
   * Mouse-drag state (slice 155). True between mousedown on a
   * cell and the subsequent mouseup (anywhere). While true,
   * {@code cell-mouseover} events extend the active range from
   * the drag's anchor cell to the hovered cell.
   */
  private dragging = false;

  /**
   * Column-resize drag state (slice 164). Set on
   * {@code column-resize-start}, cleared on mouseup. While
   * non-null, document-level mousemove updates the column
   * width live.
   */
  private columnResize: {
    col: number;
    startWidth: number;
    startClientX: number;
  } | null = null;

  /**
   * Row-resize drag state (slice 164). Symmetric to
   * {@link columnResize}.
   */
  private rowResize: {
    row: number;
    startHeight: number;
    startClientY: number;
  } | null = null;

  /**
   * The "current extend position" for shift+arrow navigation
   * (slice 147). Starts equal to the active cell. Shift+arrow
   * moves this corner ±1; the resulting range is normalized
   * across (activeCell, extendCol/Row). Plain arrow / click
   * resets extendCol/Row to the new active cell so the next
   * shift+arrow starts fresh.
   */
  private extendCol = 1;
  private extendRow = 1;

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
  /**
   * Resource map decoded from {@link setResources} — overlay ID
   * → image URL. Used at render time to resolve IMAGE-type
   * overlay sources. Slice 160.
   */
  private resources: Record<string, string> = {};
  /**
   * Popup-button states from {@link setPopupButtons}. Slice 161.
   */
  private popupButtonStates: PopupButtonState[] = [];
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

  /**
   * Handle of the animation frame a coalesced repaint is waiting on, or
   * {@code null} when no repaint is pending. See {@link scheduleRender}.
   */
  private pendingRenderFrame: number | null = null;

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
   * The sheet-tab strip below the scroll container — renders
   * one tab per sheet name with the active tab highlighted.
   * Clicking a tab dispatches `sheetSelected` to the server
   * (slice 154).
   */
  private readonly sheetTabs: SpreadsheetSheetTabs;

  /**
   * Formula bar at the top of the spreadsheet — two text inputs
   * for the active cell's address + formula. Slice 175. Wired
   * by {@link updateFormulaBar} + {@link showSelectedCell}. The
   * documented parts {@code address-field} + {@code formula-field}
   * live on the input elements (see {@link SpreadsheetFormulaBar}).
   */
  private readonly formulaBar: SpreadsheetFormulaBar;

  /**
   * MutationObserver watching the spreadsheet host for class
   * changes. Slice 174 honors {@code Spreadsheet.HIDE_TABSHEET_STYLE}
   * ("hidetabsheet") + {@code Spreadsheet.HIDE_FUNCTION_BAR_STYLE}
   * ("hidefunctionbar") by toggling display on the internal
   * elements. The wrapper element is light DOM, so we can't use
   * a {@code :host-context()} CSS selector — class observation
   * is the portable path.
   */
  private hostClassObserver: MutationObserver | null = null;

  /**
   * Height of the column-header strip in pixels. {@code 0}
   * (default) means no header strip is rendered. The host opts
   * in by calling {@link setHeaderRowHeight}; matches Excel's
   * 18-22px range. Slice 143.
   */
  /**
   * Slice 180: defaults to 20 (matching the GWT-era visual)
   * because {@link displayRowColHeadings} defaults to true.
   * Setting {@code displayRowColHeadings = false} collapses to 0.
   */
  private headerRowHeight = 20;

  /**
   * Width of the row-header strip in pixels. {@code 0} (default)
   * means no row-header is rendered. The host opts in via
   * {@link setHeaderColumnWidth}; Excel uses 30-40px (wider
   * accommodates the largest row numbers without truncation).
   * Slice 144.
   */
  /** Slice 180: defaults to 40, symmetric with {@link headerRowHeight}. */
  private headerColumnWidth = 40;

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

  constructor(host: HTMLElement, renderTarget?: HTMLElement | ShadowRoot) {
    this.host = host;
    // The wrapper (vaadin-spreadsheet.js) calls
    // `new Spreadsheet(element, element.renderRoot)` so DOM
    // goes into the shadow root, not the light DOM. Tests
    // and direct callers can omit the second arg and let
    // DOM go into the host directly. Slice 162.
    this.renderTarget = renderTarget ?? host;
    this.cellGrid = new SpreadsheetCellGrid();
    this.columnHeader = new SpreadsheetColumnHeader();
    this.rowHeader = new SpreadsheetRowHeader();
    this.headerCorner = document.createElement('div');
    this.sheetTabs = new SpreadsheetSheetTabs();
    this.formulaBar = new SpreadsheetFormulaBar();
    // Host becomes a flex column so the scroll container takes
    // the available space + the sheet tabs sit below at their
    // intrinsic height. Slice 154. Existing host width/height
    // (set via setWidth/setHeight) are honored by flex.
    // When renderTarget is a ShadowRoot, flex still applies
    // on the HOST element (CSS reaches it via the host
    // selector); skip the style writes when host is a
    // ShadowRoot itself (e.g. test setups that pass a
    // ShadowRoot as the first arg — uncommon).
    this.host.style.display = 'flex';
    this.host.style.flexDirection = 'column';
    this.scrollContainer = document.createElement('div');
    this.scrollContainer.className = 'sheet-scroll';
    // CSS Grid layout inside the scroll container: 2 columns ×
    // 2 rows so each header sits in its own cell. Column 1 =
    // row-header (auto-sized to its intrinsic width); column
    // 2 = cell-grid (auto-sized to its intrinsic width). Same
    // for rows. Sticky positioning on each header pins it to
    // its viewport edge during scroll. Slice 144.
    // `flex:1` so the container takes the remaining vertical
    // space inside the flex-column host, leaving room for the
    // sheet tabs below. Slice 154.
    this.scrollContainer.style.cssText =
      'position:relative;overflow:auto;width:100%;flex:1;min-height:0;' +
      'display:grid;grid-template-columns:auto auto;' +
      'grid-template-rows:auto auto;' +
      // outline:none so the browser-default focus ring doesn't
      // render around the spreadsheet; we get focus via
      // tabindex=0 (slice 147) but draw selection feedback via
      // the .sheet-selection overlay instead.
      'outline:none;';
    // tabindex=0 makes the scroll container focusable so it can
    // receive keydown events for keyboard navigation (slice 147).
    this.scrollContainer.tabIndex = 0;
    // Header corner at (col 1, row 1) — sticky on both axes
    // so it overlays both header strips at their intersection.
    // Slice 145. Hidden when either header dimension is 0.
    this.headerCorner.className = 'sheet-header-corner';
    this.headerCorner.style.cssText =
      'grid-column:1;grid-row:1;' +
      'position:sticky;top:0;left:0;z-index:6;' +
      'background:var(--vaadin-spreadsheet-header-bg, #f4f4f4);' +
      'border-right:1px solid' +
      ' var(--vaadin-spreadsheet-header-border, #c0c0c0);' +
      'border-bottom:1px solid' +
      ' var(--vaadin-spreadsheet-header-border, #c0c0c0);' +
      'box-sizing:border-box;' +
      'width:0;height:0;';
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
    // Slice 175: formula bar sits ABOVE the scroll container
    // in the flex column. flex:0 so it takes its intrinsic
    // height. Appended to renderTarget first so it ends up
    // ahead of the scroll container in DOM order.
    this.formulaBar.style.flex = '0 0 auto';
    this.renderTarget.appendChild(this.formulaBar);
    this.formulaBar.addEventListener('sheet-address-changed', this.handleSheetAddressChanged as EventListener);
    this.renderTarget.appendChild(this.scrollContainer);
    // Sheet tabs sit below the scroll container in the flex
    // column. flex:0 so they take their intrinsic height,
    // never grow / shrink based on content.
    this.sheetTabs.style.flex = '0 0 auto';
    this.renderTarget.appendChild(this.sheetTabs);
    // Slice 174: honor `Spreadsheet.HIDE_TABSHEET_STYLE` ("hidetabsheet")
    // on the wrapper host. When the host has that class, the sheet
    // tabs strip hides. Observe the host's class attribute via
    // MutationObserver — the wrapper element is light DOM, so
    // CSS in our shadow root can't `:host-context()` it.
    this.applyHostHideClasses();
    this.hostClassObserver = new MutationObserver(() => this.applyHostHideClasses());
    this.hostClassObserver.observe(this.host, {
      attributes: true,
      attributeFilter: ['class']
    });
    // Sheet tab click → switch active sheet via spreadsheet-event
    // (slice 154).
    this.sheetTabs.addEventListener('sheet-tab-click', this.handleSheetTabClick as EventListener);
    this.scrollContainer.addEventListener('scroll', this.handleScroll);
    // Clipboard integration (slice 163). The browser routes
    // copy/cut/paste events to the focused element when the
    // user presses Ctrl+C / Ctrl+X / Ctrl+V. The scroll
    // container is the focus host (tabindex=0 from slice 147).
    this.scrollContainer.addEventListener('copy', this.handleCopy as EventListener);
    this.scrollContainer.addEventListener('cut', this.handleCut as EventListener);
    this.scrollContainer.addEventListener('paste', this.handlePaste as EventListener);
    // Cell-click delegation (slice 146): the cell-grid dispatches
    // a `cell-click` custom event with detail = { col, row,
    // shiftKey, ctrlKey, metaKey, altKey }. The connector updates
    // its selection + re-renders. Bubble + composed = true so the
    // event crosses the cell-grid's shadow DOM boundary.
    this.cellGrid.addEventListener('cell-click', this.handleCellClick as EventListener);
    // Cell editor lifecycle events (slice 152). The cell-grid
    // emits these from its shadow DOM; the connector tracks
    // editor state + dispatches cellValueEdited on commit.
    this.cellGrid.addEventListener('cell-doubleclick', this.handleCellDoubleClick as EventListener);
    this.cellGrid.addEventListener('cell-editor-commit', this.handleCellEditorCommit as EventListener);
    this.cellGrid.addEventListener('cell-editor-cancel', this.handleCellEditorCancel as EventListener);
    // Mouse drag selection (slice 155). The cell-grid emits
    // cell-mousedown + cell-mouseover; the connector watches
    // mouseup at the document level so a drag that ends
    // outside the spreadsheet still terminates cleanly.
    this.cellGrid.addEventListener('cell-mousedown', this.handleCellMouseDown as EventListener);
    this.cellGrid.addEventListener('cell-mouseover', this.handleCellMouseOver as EventListener);
    document.addEventListener('mouseup', this.handleDocumentMouseUp);
    // Popup-button click → dispatch popupButtonClick (slice
    // 161). The server's `onPopupButtonClick` opens the
    // associated popup component.
    this.cellGrid.addEventListener('popup-button-click', this.handlePopupButtonClick as EventListener);
    // Context-menu wiring (slice 166). Right-click on a cell
    // / column-header / row-header dispatches the matching
    // local event from the element; the connector forwards it
    // to the server as `contextMenuOpenOnSelection` /
    // `columnHeaderContextMenuOpen` / `rowHeaderContextMenuOpen`
    // after first updating the selection (Excel: right-click
    // on a cell outside the current selection selects that
    // cell, then opens the menu).
    this.cellGrid.addEventListener('cell-contextmenu', this.handleCellContextMenu as EventListener);
    this.columnHeader.addEventListener(
      'column-header-contextmenu',
      this.handleColumnHeaderContextMenu as EventListener
    );
    // Grouping outline (slice 167). The column header emits
    // `column-grouping-toggle` when the user clicks a +/-
    // button in the grouping gutter; the connector forwards
    // it as `groupingCollapsed` to the server.
    this.columnHeader.addEventListener('column-grouping-toggle', this.handleColumnGroupingToggle as EventListener);
    this.rowHeader.addEventListener('row-header-contextmenu', this.handleRowHeaderContextMenu as EventListener);
    // Header-drag resize (slice 164). The header elements
    // emit `column-resize-start` / `row-resize-start` from
    // their right-edge / bottom-edge mousedown handles; the
    // connector tracks the drag via document mousemove +
    // mouseup. Live-updates colWidths/rowHeights for the
    // current drag column/row.
    this.columnHeader.addEventListener('column-resize-start', this.handleColumnResizeStart as EventListener);
    this.rowHeader.addEventListener('row-resize-start', this.handleRowResizeStart as EventListener);
    document.addEventListener('mousemove', this.handleDocumentMouseMove);
    // Header click delegation (slice 148). Each header element
    // emits its own custom event with the col / row coordinate;
    // the connector translates them into whole-column / whole-
    // row range selections.
    this.columnHeader.addEventListener('column-header-click', this.handleColumnHeaderClick as EventListener);
    this.rowHeader.addEventListener('row-header-click', this.handleRowHeaderClick as EventListener);
    // Keyboard navigation (slice 147). Listen on the scroll
    // container so arrow keys + shift+arrow work whenever the
    // spreadsheet has focus. preventDefault() in the handler
    // stops the browser from scrolling the viewport via the
    // arrow keys.
    this.scrollContainer.addEventListener('keydown', this.handleKeyDown as EventListener);
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
      this.extendCol = col;
      this.extendRow = row;
    } else {
      this.selection = selectSingleCell(col, row);
      this.extendCol = col;
      this.extendRow = row;
    }
    this.recomputeViewport();
    this.dispatchSelectionEvent();
    // Hyperlink cells additionally dispatch `linkCellClicked`
    // after the normal selection event (slice 165). The
    // server's onLinkCellClicked handler opens the URL.
    const cellKey = toKey(col, row);
    if (this.hyperlinksTooltips[cellKey] !== undefined) {
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: {
            type: 'linkCellClicked',
            data: { row, column: col }
          },
          bubbles: true,
          composed: true
        })
      );
    }
  };

  /**
   * Column resize drag — start. Captures the dragged column's
   * current width + mouse position so {@link handleDocumentMouseMove}
   * can apply deltas. Slice 164.
   */
  private readonly handleColumnResizeStart = (e: CustomEvent<{ col: number; clientX: number }>): void => {
    const { col, clientX } = e.detail;
    const idx = col - 1;
    const startWidth = idx < this.colWidths.length ? this.colWidths[idx] : this.defColWidth;
    this.columnResize = { col, startWidth, startClientX: clientX };
  };

  /**
   * Row resize drag — start. Mirror of column variant.
   */
  private readonly handleRowResizeStart = (e: CustomEvent<{ row: number; clientY: number }>): void => {
    const { row, clientY } = e.detail;
    const idx = row - 1;
    // Note: rowHeights is in POINTS, not pixels. We convert
    // on render, but the resize delta is in pixels; convert
    // back when writing.
    const startHeight = idx < this.rowHeights.length ? this.rowHeights[idx] : this.defRowHeight;
    this.rowResize = { row, startHeight, startClientY: clientY };
  };

  /**
   * Document mousemove during a header-drag resize (slice 164).
   * Updates the dragged column's pixel width (or row's point
   * height) from the mouse delta + triggers a re-render. Fires
   * many times per second during drag; the per-update cost is
   * one array element write + one recomputeViewport.
   */
  private readonly handleDocumentMouseMove = (e: MouseEvent): void => {
    if (this.columnResize !== null) {
      const delta = e.clientX - this.columnResize.startClientX;
      const newWidth = Math.max(0, this.columnResize.startWidth + delta);
      const idx = this.columnResize.col - 1;
      // Convert colWidths to a mutable array if it's an Int32Array
      // (server-pushed shape) — the resize-drag writes into it.
      if (!Array.isArray(this.colWidths)) {
        this.colWidths = Array.from(this.colWidths);
      }
      (this.colWidths as number[])[idx] = newWidth;
      this.recomputeViewport();
      return;
    }
    if (this.rowResize !== null) {
      const deltaPx = e.clientY - this.rowResize.startClientY;
      // Convert pixel delta → point delta (1 pt = 96/72 px at default ppi).
      const deltaPt = (deltaPx * 72) / FALLBACK_PPI;
      const newHeight = Math.max(0, this.rowResize.startHeight + deltaPt);
      const idx = this.rowResize.row - 1;
      if (!Array.isArray(this.rowHeights)) {
        this.rowHeights = Array.from(this.rowHeights);
      }
      (this.rowHeights as number[])[idx] = newHeight;
      this.recomputeViewport();
    }
  };

  /**
   * Popup-button click handler (slice 161). Translates the
   * cell-grid's local event into a server-bound
   * `popupButtonClick` spreadsheet-event so the server can
   * open the popup component.
   */
  private readonly handlePopupButtonClick = (e: CustomEvent<{ col: number; row: number }>): void => {
    const { col, row } = e.detail;
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'popupButtonClick',
          data: { row, column: col }
        },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Right-click on a cell (slice 166). Excel behavior: if the
   * clicked cell is outside the current selection, move selection
   * to it first (firing a selection event), then dispatch
   * {@code contextMenuOpenOnSelection}. If inside the existing
   * selection, leave the selection alone and dispatch the event
   * with the clicked cell's coordinates.
   */
  private readonly handleCellContextMenu = (e: CustomEvent<{ col: number; row: number }>): void => {
    const { col, row } = e.detail;
    if (!isCellInSelection(col, row, this.selection)) {
      this.selection = selectSingleCell(col, row);
      this.extendCol = col;
      this.extendRow = row;
      this.recomputeViewport();
      this.dispatchSelectionEvent();
    }
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'contextMenuOpenOnSelection',
          data: { row, column: col }
        },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Right-click on a column header (slice 166). Selects the
   * whole column (Excel behavior) then dispatches
   * {@code columnHeaderContextMenuOpen}.
   */
  private readonly handleColumnHeaderContextMenu = (e: CustomEvent<{ col: number }>): void => {
    const { col } = e.detail;
    const rows = this.rows > 0 ? this.rows : 1;
    this.selection = {
      activeCell: { col, row: 1 },
      activeRange: normalizedRange(col, 1, col, rows),
      auxRanges: []
    };
    this.extendCol = col;
    this.extendRow = rows;
    this.recomputeViewport();
    this.dispatchSelectionEvent();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'columnHeaderContextMenuOpen',
          data: { columnIndex: col }
        },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Right-click on a row header (slice 166). Mirror of the column
   * variant — selects the whole row then dispatches
   * {@code rowHeaderContextMenuOpen}.
   */
  private readonly handleRowHeaderContextMenu = (e: CustomEvent<{ row: number }>): void => {
    const { row } = e.detail;
    const cols = this.cols > 0 ? this.cols : 1;
    this.selection = {
      activeCell: { col: 1, row },
      activeRange: normalizedRange(1, row, cols, row),
      auxRanges: []
    };
    this.extendCol = cols;
    this.extendRow = row;
    this.recomputeViewport();
    this.dispatchSelectionEvent();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'rowHeaderContextMenuOpen',
          data: { rowIndex: row }
        },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Column-grouping toggle (slice 167). Forwards the user's
   * +/- click in the grouping outline gutter as
   * {@code groupingCollapsed} for the server. Wire payload:
   * {@code cols: true}, {@code colIndex} carries the
   * grouping's {@code uniqueIndex} (its identity in the
   * grouping list), {@code collapsed} is the NEW state the
   * user is requesting.
   */
  private readonly handleColumnGroupingToggle = (e: CustomEvent<{ uniqueIndex: number; collapsed: boolean }>): void => {
    const { uniqueIndex, collapsed } = e.detail;
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'groupingCollapsed',
          data: { cols: true, colIndex: uniqueIndex, collapsed }
        },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Mouse drag — mousedown begin (slice 155). Starts a drag.
   * Sets the selection to the single cell at the mousedown
   * coord (anchor) + opens the dragging flag so subsequent
   * `cell-mouseover` events extend the range. Doesn't dispatch
   * `cellSelected` here — the existing `cell-click` handler
   * (which fires on mouseup) takes care of that for a no-drag
   * click. A real drag ends with a `cellRangeSelected` event
   * in {@link handleDocumentMouseUp}.
   */
  private readonly handleCellMouseDown = (
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
    this.dragging = true;
    if (shiftKey) {
      // Shift+mousedown: extend existing range to the
      // mousedown coord (treat the existing anchor as the
      // drag start). Standard Excel "shift-drag" behavior.
      const anchor = this.selection.activeCell;
      this.selection = selectRange(anchor.col, anchor.row, col, row);
      this.extendCol = col;
      this.extendRow = row;
    } else {
      this.selection = selectSingleCell(col, row);
      this.extendCol = col;
      this.extendRow = row;
    }
    this.recomputeViewport();
  };

  /**
   * Mouse drag — mouseover during drag (slice 155). Extends
   * the range from the active cell (anchor) to the hovered
   * cell. Mouseover events that fire while NOT dragging are
   * silently ignored — the cell-grid emits on every hover.
   */
  private readonly handleCellMouseOver = (e: CustomEvent<{ col: number; row: number }>): void => {
    if (!this.dragging) {
      return;
    }
    const { col, row } = e.detail;
    // Same as shift-arrow: keep activeCell as anchor, move
    // the extend corner.
    const anchor = this.selection.activeCell;
    this.selection = {
      activeCell: anchor,
      activeRange: normalizedRange(anchor.col, anchor.row, col, row),
      auxRanges: []
    };
    this.extendCol = col;
    this.extendRow = row;
    this.recomputeViewport();
  };

  /**
   * Mouse drag — mouseup end (slice 155). Closes the drag.
   * Dispatches a {@code cellRangeSelected} event when the
   * final range covers more than one cell (single-cell drag
   * is treated as a click, whose `cell-click` handler already
   * dispatched `cellSelected`).
   */
  private readonly handleDocumentMouseUp = (): void => {
    if (this.columnResize !== null) {
      // End of column-resize drag (slice 164). Dispatch
      // the columnResized event with the new pixel width
      // for the server's persistence + downstream rows
      // dependencies.
      const { col } = this.columnResize;
      const idx = col - 1;
      const newWidth = idx < this.colWidths.length ? this.colWidths[idx] : this.defColWidth;
      this.columnResize = null;
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: {
            type: 'columnResized',
            data: { colIndexes: [col], newWidths: [newWidth] }
          },
          bubbles: true,
          composed: true
        })
      );
      return;
    }
    if (this.rowResize !== null) {
      // End of row-resize drag (slice 164). rowHeights is
      // in points; the event payload also uses points.
      const { row } = this.rowResize;
      const idx = row - 1;
      const newHeight = idx < this.rowHeights.length ? this.rowHeights[idx] : this.defRowHeight;
      this.rowResize = null;
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: {
            type: 'rowsResized',
            data: { rowIndexes: [row], newHeights: [newHeight] }
          },
          bubbles: true,
          composed: true
        })
      );
      return;
    }
    if (!this.dragging) {
      return;
    }
    this.dragging = false;
    const range = this.selection.activeRange;
    if (range.c1 !== range.c2 || range.r1 !== range.r2) {
      // Multi-cell range — dispatch the server-bound
      // selection event so the server learns the final
      // dragged range.
      this.dispatchSelectionEvent();
    }
    // Single-cell drag (mousedown + mouseup on the same cell
    // with no mouseover-extension) is handled by the existing
    // `cell-click` handler — no double dispatch.
  };

  /**
   * Double-click on a cell opens the in-place editor at that
   * cell (slice 152). The connector loads the cell's existing
   * value (or empty string when there's no data) into the
   * editor state, then pushes a fresh render. The cell-grid's
   * `updated()` lifecycle focuses the input + places the caret
   * at the end.
   */
  private readonly handleCellDoubleClick = (e: CustomEvent<{ col: number; row: number }>): void => {
    const { col, row } = e.detail;
    if (this.isCellLockedForEdit(col, row)) {
      return;
    }
    const key = toKey(col, row);
    const cellData = this.cellDataByKey.get(key);
    const initialValue = cellData?.value ?? '';
    this.editor = { col, row, initialValue };
    this.recomputeViewport();
  };

  /**
   * Excel-style lock check: a cell is uneditable only when the
   * sheet is protected AND the cell's locked flag is true. POI
   * defaults every cell's locked flag to true, so a freshly
   * loaded workbook with sheet protection enabled blocks all
   * cells; unlocking a region in POI flips the per-cell flag.
   * When the sheet is unprotected, the flag is meaningless.
   * Cells without CellData entries default to locked because
   * that matches POI's behavior for empty cells in a protected
   * sheet.
   */
  private isCellLockedForEdit(col: number, row: number): boolean {
    if (!this.sheetProtected) {
      return false;
    }
    const cellData = this.cellDataByKey.get(toKey(col, row));
    return cellData?.locked ?? true;
  }

  /**
   * Cell-editor commit. Called when the user presses Enter,
   * Tab, or blurs the input. Reads the typed value, dispatches
   * `cellValueEdited` to the server, closes the editor. When
   * the commit key was Enter/Tab (slice 158), moves the active
   * cell in the canonical Excel direction:
   *   Enter           → row + 1
   *   Shift+Enter     → row - 1
   *   Tab             → col + 1
   *   Shift+Tab       → col - 1
   *   blur (no key)   → stay at the committed cell
   */
  private readonly handleCellEditorCommit = (
    e: CustomEvent<{
      col: number;
      row: number;
      value: string;
      key?: string;
      shiftKey?: boolean;
    }>
  ): void => {
    const { col, row, value, key, shiftKey } = e.detail;
    // Slice 181: optimistically update the client-side cell
    // data so the typed value is visible immediately. The
    // server's `cellsUpdated` callback may arrive much later
    // (or not at all in offline / TestBench scenarios); the
    // GWT-era code updated the cell display before the
    // round-trip — Excel users expect what they typed to
    // show up at once. Preserves the existing CellData
    // metadata when present (style index, locked flag).
    const cellKey = toKey(col, row);
    const previous = this.cellDataByKey.get(cellKey);
    this.cellDataByKey.set(cellKey, {
      col,
      row,
      value,
      cellStyle: previous?.cellStyle ?? 'cs0',
      locked: previous?.locked ?? false,
      needsMeasure: previous?.needsMeasure ?? false,
      isPercentage: previous?.isPercentage ?? false,
      ...(previous?.formulaValue !== undefined ? { formulaValue: previous.formulaValue } : {}),
      ...(previous?.originalValue !== undefined ? { originalValue: previous.originalValue } : {}),
      ...(previous?.textColor !== undefined ? { textColor: previous.textColor } : {})
    });
    // Close the editor first so the re-render removes the
    // input — important to do BEFORE dispatching the server
    // event so the editor doesn't briefly flash on top of the
    // server's response.
    this.editor = null;
    // Move the active cell to the next slot per Excel
    // convention (slice 158). For 'Enter' / 'Tab' the move
    // direction is fixed; blur leaves the selection at the
    // committed cell. Clamped to grid bounds.
    if (key === 'Enter' && this.rows > 0) {
      const nextRow = shiftKey ? Math.max(1, row - 1) : Math.min(this.rows, row + 1);
      this.selection = selectSingleCell(col, nextRow);
      this.extendCol = col;
      this.extendRow = nextRow;
    } else if (key === 'Tab' && this.cols > 0) {
      const nextCol = shiftKey ? Math.max(1, col - 1) : Math.min(this.cols, col + 1);
      this.selection = selectSingleCell(nextCol, row);
      this.extendCol = nextCol;
      this.extendRow = row;
    }
    this.recomputeViewport();
    const data: CellValueEditedData = { row, col, value };
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'cellValueEdited', data },
        bubbles: true,
        composed: true
      })
    );
    // If we moved the active cell, also dispatch the
    // selection event so the server stays in sync (slice
    // 150 contract — every selection write emits an event).
    if (key === 'Enter' || key === 'Tab') {
      this.dispatchSelectionEvent();
    }
  };

  /**
   * Cell-editor cancel (slice 152). User pressed Escape. Close
   * the editor without dispatching anything.
   */
  private readonly handleCellEditorCancel = (): void => {
    this.editor = null;
    this.recomputeViewport();
  };

  /**
   * Column-header click handler (slice 148). Selects the entire
   * column (rows 1..this.rows). The active cell becomes the
   * column's row 1. Shift-click extends from the existing anchor
   * column to the clicked column, selecting a whole-column
   * range. Updates {@code extendCol/Row} so subsequent
   * shift+arrow continues naturally.
   */
  private readonly handleColumnHeaderClick = (
    e: CustomEvent<{
      col: number;
      shiftKey: boolean;
      ctrlKey: boolean;
      metaKey: boolean;
      altKey: boolean;
    }>
  ): void => {
    const { col, shiftKey } = e.detail;
    const rows = this.rows > 0 ? this.rows : 1;
    if (shiftKey) {
      const anchor = this.selection.activeCell;
      this.selection = {
        activeCell: anchor,
        activeRange: normalizedRange(anchor.col, 1, col, rows),
        auxRanges: []
      };
      this.extendCol = col;
      this.extendRow = rows;
    } else {
      this.selection = {
        activeCell: { col, row: 1 },
        activeRange: normalizedRange(col, 1, col, rows),
        auxRanges: []
      };
      this.extendCol = col;
      this.extendRow = rows;
    }
    this.recomputeViewport();
    this.dispatchSelectionEvent();
  };

  /**
   * Row-header click handler (slice 148). Mirror of the column
   * variant — selects an entire row (cols 1..this.cols).
   */
  private readonly handleRowHeaderClick = (
    e: CustomEvent<{
      row: number;
      shiftKey: boolean;
      ctrlKey: boolean;
      metaKey: boolean;
      altKey: boolean;
    }>
  ): void => {
    const { row, shiftKey } = e.detail;
    const cols = this.cols > 0 ? this.cols : 1;
    if (shiftKey) {
      const anchor = this.selection.activeCell;
      this.selection = {
        activeCell: anchor,
        activeRange: normalizedRange(1, anchor.row, cols, row),
        auxRanges: []
      };
      this.extendCol = cols;
      this.extendRow = row;
    } else {
      this.selection = {
        activeCell: { col: 1, row },
        activeRange: normalizedRange(1, row, cols, row),
        auxRanges: []
      };
      this.extendCol = cols;
      this.extendRow = row;
    }
    this.recomputeViewport();
    this.dispatchSelectionEvent();
  };

  /**
   * Keyboard navigation handler (slice 147). Plain arrow keys
   * move the active cell ±1 in the requested direction, clamped
   * to {@code [1, rows]} / {@code [1, cols]}. Shift+arrow moves
   * the "extend position" (separate from active cell) and
   * recomputes the range as
   * {@code normalizedRange(activeCell, extendCol/Row)} — matches
   * Excel's behavior where the active cell stays fixed across a
   * sequence of shift+arrow presses.
   *
   * Other keys (Tab, Enter, Home, End, etc.) are deferred to
   * future slices.
   */
  private readonly handleKeyDown = (e: KeyboardEvent): void => {
    const key = e.key;
    const ctrlOrMeta = e.ctrlKey || e.metaKey;

    // Ctrl+A / Cmd+A — select the entire sheet (slice 159).
    if (ctrlOrMeta && (key === 'a' || key === 'A')) {
      if (this.rows <= 0 || this.cols <= 0) {
        return;
      }
      e.preventDefault();
      this.selection = {
        activeCell: { col: 1, row: 1 },
        activeRange: normalizedRange(1, 1, this.cols, this.rows),
        auxRanges: []
      };
      this.extendCol = this.cols;
      this.extendRow = this.rows;
      this.recomputeViewport();
      this.dispatchSelectionEvent();
      return;
    }

    // Ctrl+Z / Cmd+Z — undo. Pure pass-through to the server;
    // the server owns the undo stack + replies with the
    // resulting cell updates. Slice 159.
    if (ctrlOrMeta && (key === 'z' || key === 'Z') && !e.shiftKey) {
      e.preventDefault();
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: { type: 'onUndo', data: {} },
          bubbles: true,
          composed: true
        })
      );
      return;
    }

    // Ctrl+Y / Cmd+Shift+Z — redo. Cmd+Y is reserved on Mac
    // for "history menu" by some apps, so we also accept
    // Cmd+Shift+Z (the Mac convention) + Ctrl+Y (Windows).
    // Slice 159.
    if (ctrlOrMeta && (key === 'y' || key === 'Y' || ((key === 'z' || key === 'Z') && e.shiftKey))) {
      e.preventDefault();
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: { type: 'onRedo', data: {} },
          bubbles: true,
          composed: true
        })
      );
      return;
    }

    // F2 opens the cell editor at the active cell, loaded with
    // the cell's existing value (Excel convention). Slice 153.
    if (key === 'F2' && !this.editor) {
      if (this.rows <= 0 || this.cols <= 0) {
        return;
      }
      const { col, row } = this.selection.activeCell;
      if (this.isCellLockedForEdit(col, row)) {
        return;
      }
      e.preventDefault();
      const cellData = this.cellDataByKey.get(toKey(col, row));
      this.editor = {
        col,
        row,
        initialValue: cellData?.value ?? ''
      };
      this.recomputeViewport();
      return;
    }

    // Type-to-edit: any printable character key without
    // Ctrl/Meta/Alt modifiers opens the cell editor at the
    // active cell with the typed character as the initial
    // value. Excel convention — typing a letter directly
    // starts replacing the cell's content. Slice 153.
    if (!this.editor && key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey) {
      if (this.rows <= 0 || this.cols <= 0) {
        return;
      }
      const { col, row } = this.selection.activeCell;
      if (this.isCellLockedForEdit(col, row)) {
        return;
      }
      e.preventDefault();
      this.editor = { col, row, initialValue: key };
      this.recomputeViewport();
      return;
    }

    // Delete + Backspace clear the cell contents in the current
    // selection. Pure pass-through to the server — the connector
    // dispatches `deleteSelectedCells` and the server replies
    // with `update*CellValues` carrying the cleared cells. The
    // client doesn't optimistically clear locally so the
    // server's response is the single source of truth. Slice
    // 151.
    if (key === 'Delete' || key === 'Backspace') {
      if (this.rows <= 0 || this.cols <= 0) {
        return;
      }
      e.preventDefault();
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: { type: 'deleteSelectedCells', data: {} },
          bubbles: true,
          composed: true
        })
      );
      return;
    }
    // Map each navigation key to a (deltaCol, deltaRow) OR an
    // absolute coord. Modifier-key semantics:
    //   - shift+arrow : move "extend" corner (slice 147)
    //   - plain arrow : move active cell ±1
    //   - Tab / Enter : same as ArrowRight / ArrowDown
    //   - Home / End  : absolute col edge, same row
    //   - Ctrl+Home   : (1, 1)
    //   - Ctrl+End    : (cols, rows)
    // The handler resolves the next active-cell (or extend-cell)
    // through a single code path so the clamping + selection-
    // write is shared. Slice 149.
    const target = computeNavigationTarget(key, e, {
      activeCell: this.selection.activeCell,
      extendCol: this.extendCol,
      extendRow: this.extendRow,
      cols: this.cols,
      rows: this.rows
    });
    if (!target) {
      return;
    }
    if (this.rows <= 0 || this.cols <= 0) {
      return;
    }
    e.preventDefault();
    if (target.kind === 'extend') {
      this.extendCol = target.col;
      this.extendRow = target.row;
      const anchor = this.selection.activeCell;
      this.selection = {
        activeCell: anchor,
        activeRange: normalizedRange(anchor.col, anchor.row, target.col, target.row),
        auxRanges: []
      };
    } else {
      this.selection = selectSingleCell(target.col, target.row);
      this.extendCol = target.col;
      this.extendRow = target.row;
    }
    // Auto-scroll the new active cell (or extend cell) into
    // view BEFORE the render (slice 156). Excel scrolls the
    // viewport whenever keyboard nav moves the cursor off-
    // screen so the user can keep typing without losing
    // visual context. Sets scrollLeft/scrollTop directly; the
    // scroll listener triggers an additional recomputeViewport
    // automatically.
    this.scrollActiveOrExtendIntoView(target);
    this.recomputeViewport();
    this.dispatchSelectionEvent();
  };

  /**
   * Adjust {@code scrollContainer.scrollLeft / scrollTop} so the
   * given cell (either the new active cell or the extend cell
   * after shift+arrow) is visible. Sets scrollLeft/Top in-place;
   * the existing `scroll` listener triggers a fresh
   * recomputeViewport. Slice 156.
   *
   * Pure-ish: reads `this.colWidths / rowHeights / defRowHeight /
   * hiddenRowIndexes / hiddenColumnIndexes`. Side effect = scroll
   * adjustment.
   */
  private scrollActiveOrExtendIntoView(target: NavigationTarget): void {
    if (this.rows <= 0 || this.cols <= 0) {
      return;
    }
    const ppi = FALLBACK_PPI;
    const defaultRowHeightPx = getDefaultRowHeightPx(this.defRowHeight, ppi);
    const hiddenColSet = new Set(this.hiddenColumnIndexes);
    const hiddenRowSet = new Set(this.hiddenRowIndexes);
    const colWidth = (c: number): number => {
      if (hiddenColSet.has(c)) return 0;
      return c > this.colWidths.length ? this.defColWidth : this.colWidths[c - 1];
    };
    const rowHeight = (r: number): number => {
      if (hiddenRowSet.has(r)) return 0;
      return r > this.rowHeights.length ? defaultRowHeightPx : convertPointsToPixel(this.rowHeights[r - 1], ppi);
    };

    let left = 0;
    for (let c = 1; c < target.col; c++) {
      left += colWidth(c);
    }
    const width = colWidth(target.col);
    let top = 0;
    for (let r = 1; r < target.row; r++) {
      top += rowHeight(r);
    }
    const height = rowHeight(target.row);

    const viewWidth = this.scrollContainer.clientWidth;
    const viewHeight = this.scrollContainer.clientHeight;
    const curLeft = this.scrollContainer.scrollLeft;
    const curTop = this.scrollContainer.scrollTop;

    let newLeft = curLeft;
    if (left < curLeft) {
      newLeft = left;
    } else if (left + width > curLeft + viewWidth) {
      newLeft = Math.max(0, left + width - viewWidth);
    }

    let newTop = curTop;
    if (top < curTop) {
      newTop = top;
    } else if (top + height > curTop + viewHeight) {
      newTop = Math.max(0, top + height - viewHeight);
    }

    if (newLeft !== curLeft) {
      this.scrollContainer.scrollLeft = newLeft;
    }
    if (newTop !== curTop) {
      this.scrollContainer.scrollTop = newTop;
    }
  }

  /**
   * Copy handler (slice 163). Serializes the current selection
   * range to TSV via {@code serializeToTsv} + writes to the
   * browser's clipboard via {@code event.clipboardData}. No
   * server-bound event — the server doesn't track the
   * clipboard. Returns early when the editor is open (the
   * input's own copy handler takes over).
   */
  private readonly handleCopy = (e: ClipboardEvent): void => {
    if (this.editor !== null) {
      return;
    }
    const tsv = this.selectionToTsv();
    if (tsv === null) {
      return;
    }
    e.preventDefault();
    e.clipboardData?.setData('text/plain', tsv);
  };

  /**
   * Cut handler (slice 163). Same as copy + dispatches
   * {@code clearSelectedCellsOnCut} to the server so it can
   * clear the cells (the visual update comes back via
   * {@code update*CellValues}).
   */
  private readonly handleCut = (e: ClipboardEvent): void => {
    if (this.editor !== null) {
      return;
    }
    const tsv = this.selectionToTsv();
    if (tsv === null) {
      return;
    }
    e.preventDefault();
    e.clipboardData?.setData('text/plain', tsv);
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'clearSelectedCellsOnCut', data: {} },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Paste handler (slice 163). Reads the clipboard's text/plain
   * payload + forwards to the server via {@code onPaste}.
   * The server parses the TSV and writes cells starting at the
   * active cell.
   */
  private readonly handlePaste = (e: ClipboardEvent): void => {
    if (this.editor !== null) {
      return;
    }
    const text = e.clipboardData?.getData('text/plain') ?? '';
    if (text === '') {
      return;
    }
    e.preventDefault();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'onPaste', data: { text } },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Serialize the active range's cell values to TSV (tab-
   * separated values). Returns null when the selection's range
   * is invalid or no cells exist. Uses
   * {@link cellDataByKey} as the source of truth — cells
   * without data are emitted as empty strings.
   *
   * Slice 163.
   */
  private selectionToTsv(): string | null {
    const range = this.selection.activeRange;
    if (range.c1 > range.c2 || range.r1 > range.r2) {
      return null;
    }
    const rows: (string | null)[][] = [];
    for (let r = range.r1; r <= range.r2; r++) {
      const row: (string | null)[] = [];
      for (let c = range.c1; c <= range.c2; c++) {
        const data = this.cellDataByKey.get(toKey(c, r));
        row.push(data?.value ?? '');
      }
      rows.push(row);
    }
    return serializeToTsv(rows);
  }

  /**
   * Scroll-event handler: recompute the visible viewport from the
   * scroll container's current state + push a fresh render state
   * to the cell-grid. Also dispatches `onSheetScroll` to the
   * server so it can prefetch cell values for the newly-visible
   * range (slice 157). Bound as an arrow function so the
   * listener binding survives without explicit
   * {@code .bind(this)}.
   */
  private readonly handleScroll = (): void => {
    this.recomputeViewport();
    this.dispatchSheetScrollEvent();
  };

  /**
   * Dispatch `onSheetScroll` carrying the current viewport
   * bounds (firstRow/firstColumn/lastRow/lastColumn). The
   * server's `SpreadsheetHandler.onSheetScroll` uses these to
   * decide which cell values to push. Slice 157.
   *
   * No debouncing — scroll events naturally throttle via the
   * browser's scroll-tick coalescing (usually one per
   * requestAnimationFrame). If profiling reveals server-side
   * pressure, add a debounce here.
   */
  private dispatchSheetScrollEvent(): void {
    const viewport = this.cellGrid.renderState.viewport;
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'onSheetScroll',
          data: {
            firstRow: viewport.firstRowIndex,
            firstColumn: viewport.firstColumnIndex,
            lastRow: viewport.lastRowIndex,
            lastColumn: viewport.lastColumnIndex
          }
        },
        bubbles: true,
        composed: true
      })
    );
  }

  /**
   * Recompute the visible viewport from the scroll container's
   * current scroll position + client dimensions, then push the
   * resulting render state to the cell-grid. Called by the scroll
   * listener {@link handleScroll}, the {@link resizeObserver}, and
   * the wrapper element on data changes. Public so callers (the
   * Lit wrapper's {@code updated} cycle, integration tests) can
   * force a re-render without going through the scroll surface.
   */
  /**
   * Request a repaint on the next animation frame, coalescing repeated
   * calls into a single {@link recomputeViewport}.
   *
   * The server pushes cell values, styles and reload signals as several
   * separate RPC calls within one roundtrip. Painting on each one would
   * re-render the whole viewport many times over for what the user sees
   * as a single update, so the calls collapse into one frame.
   */
  private scheduleRender(): void {
    if (this.pendingRenderFrame !== null) {
      return;
    }
    this.pendingRenderFrame = requestAnimationFrame(() => {
      this.pendingRenderFrame = null;
      this.recomputeViewport();
    });
  }

  recomputeViewport(): void {
    this.applyRenderState({
      scrollLeft: this.scrollContainer.scrollLeft,
      scrollTop: this.scrollContainer.scrollTop,
      viewportWidth: this.scrollContainer.clientWidth,
      viewportHeight: this.scrollContainer.clientHeight
    });
  }

  /**
   * Release the scroll + resize listeners. Call this when the host
   * element is being detached + destroyed so the
   * {@link resizeObserver} doesn't keep a reference to the
   * scroll container after teardown.
   */
  dispose(): void {
    if (this.pendingRenderFrame !== null) {
      cancelAnimationFrame(this.pendingRenderFrame);
      this.pendingRenderFrame = null;
    }
    this.scrollContainer.removeEventListener('scroll', this.handleScroll);
    this.scrollContainer.removeEventListener('keydown', this.handleKeyDown as EventListener);
    this.scrollContainer.removeEventListener('copy', this.handleCopy as EventListener);
    this.scrollContainer.removeEventListener('cut', this.handleCut as EventListener);
    this.scrollContainer.removeEventListener('paste', this.handlePaste as EventListener);
    this.cellGrid.removeEventListener('cell-click', this.handleCellClick as EventListener);
    this.cellGrid.removeEventListener('cell-doubleclick', this.handleCellDoubleClick as EventListener);
    this.cellGrid.removeEventListener('cell-editor-commit', this.handleCellEditorCommit as EventListener);
    this.cellGrid.removeEventListener('cell-editor-cancel', this.handleCellEditorCancel as EventListener);
    this.columnHeader.removeEventListener('column-header-click', this.handleColumnHeaderClick as EventListener);
    this.rowHeader.removeEventListener('row-header-click', this.handleRowHeaderClick as EventListener);
    this.sheetTabs.removeEventListener('sheet-tab-click', this.handleSheetTabClick as EventListener);
    this.cellGrid.removeEventListener('cell-mousedown', this.handleCellMouseDown as EventListener);
    this.cellGrid.removeEventListener('cell-mouseover', this.handleCellMouseOver as EventListener);
    this.cellGrid.removeEventListener('popup-button-click', this.handlePopupButtonClick as EventListener);
    this.cellGrid.removeEventListener('cell-contextmenu', this.handleCellContextMenu as EventListener);
    this.columnHeader.removeEventListener(
      'column-header-contextmenu',
      this.handleColumnHeaderContextMenu as EventListener
    );
    this.columnHeader.removeEventListener('column-grouping-toggle', this.handleColumnGroupingToggle as EventListener);
    this.rowHeader.removeEventListener('row-header-contextmenu', this.handleRowHeaderContextMenu as EventListener);
    document.removeEventListener('mouseup', this.handleDocumentMouseUp);
    document.removeEventListener('mousemove', this.handleDocumentMouseMove);
    this.columnHeader.removeEventListener('column-resize-start', this.handleColumnResizeStart as EventListener);
    this.rowHeader.removeEventListener('row-resize-start', this.handleRowResizeStart as EventListener);
    this.resizeObserver.disconnect();
    if (this.hostClassObserver) {
      this.hostClassObserver.disconnect();
      this.hostClassObserver = null;
    }
    this.formulaBar.removeEventListener('sheet-address-changed', this.handleSheetAddressChanged as EventListener);
  }

  /**
   * Slice 174: read the spreadsheet host's class list + apply
   * the two public "hide bar" classes by toggling display on
   * the matching internal elements. The constants
   * {@link Spreadsheet#HIDE_FUNCTION_BAR_STYLE} ("hidefunctionbar")
   * and {@link Spreadsheet#HIDE_TABSHEET_STYLE} ("hidetabsheet")
   * are public JavaDoc-documented API; consumers call
   * {@code addClassName(HIDE_TABSHEET_STYLE)} on the host
   * (or use {@code setSheetSelectionBarVisible(false)} which
   * does the same) to suppress the corresponding strip.
   *
   * The function bar isn't built yet (deferred to slice 175);
   * the function-bar branch becomes meaningful when that
   * element exists.
   */
  private applyHostHideClasses(): void {
    const tabsHidden = this.host.classList.contains('hidetabsheet');
    this.sheetTabs.style.display = tabsHidden ? 'none' : '';
    const barHidden = this.host.classList.contains('hidefunctionbar');
    this.formulaBar.style.display = barHidden ? 'none' : '';
  }

  /**
   * Slice 175: dispatched when the user presses Enter in the
   * formula-bar address input. Forwards as the documented
   * {@code sheetAddressChanged} server-bound event.
   */
  private readonly handleSheetAddressChanged = (e: CustomEvent<{ value: string }>): void => {
    const { value } = e.detail;
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'sheetAddressChanged',
          data: { value }
        },
        bubbles: true,
        composed: true
      })
    );
  };

  /** Test-only accessor: the scroll container element. */
  getScrollContainer(): HTMLElement {
    return this.scrollContainer;
  }

  /**
   * Dispatch a {@code spreadsheet-event} on the host carrying
   * the appropriate server-bound event for the current selection
   * (slice 150). Picks one of {@code cellSelected /
   * cellRangeSelected / columnSelected / rowSelected} based on
   * the shape of {@code this.selection.activeRange}:
   *
   *  - whole column (r1=1, r2=rows, single col) → columnSelected
   *  - whole row    (c1=1, c2=cols, single row) → rowSelected
   *  - single cell  (c1=c2, r1=r2)              → cellSelected
   *  - otherwise                                → cellRangeSelected
   *
   * The wrapper subscribes to {@code spreadsheet-event} with a
   * {@code @DomEvent} listener on the server side; the detail's
   * {@code type} + {@code data} mirror the existing
   * SpreadsheetHandler dispatch signatures (preserved verbatim
   * from the GWT side as a wire contract).
   */
  private dispatchSelectionEvent(): void {
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: buildSelectionEvent(this.selection.activeCell, this.selection.activeRange, this.rows, this.cols),
        bubbles: true,
        composed: true
      })
    );
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
      // Slice 167: compute positioned column-groupings so the
      // header can render the outline gutter. Pre-resolving
      // pixel positions in the connector keeps the header
      // element independent of the full colWidths array.
      const colGroupings = computePositionedColGroupings(
        this.colGroupingData,
        this.colWidths,
        this.colGroupingInversed,
        this.defColWidth
      );
      const gutterPx = this.colGroupingMax * 16;
      this.columnHeader.style.width = `${state.totalGridWidthPx}px`;
      this.columnHeader.style.height = `${this.headerRowHeight + gutterPx}px`;
      this.columnHeader.renderState = {
        viewport: state.viewport,
        colX: state.colX,
        colWidths: state.colWidths,
        headerHeight: this.headerRowHeight,
        totalGridWidthPx: state.totalGridWidthPx,
        colGroupings,
        colGroupingMax: this.colGroupingMax,
        colGroupingInversed: this.colGroupingInversed,
        // Slice 173: feed the selected-columns set so the
        // header can emit `part="column-header
        // header-selected"` on cells inside the active
        // range. Pure derivation from
        // {@code selection.activeRange}.
        selectedColumns: selectedColumnSet(this.selection)
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
        // Slice 173: feed the selected-rows set so the
        // header can emit `part="row-header
        // header-selected"` on rows inside the active
        // range.
        selectedRows: selectedRowSet(this.selection)
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
      getRowHeight: rowHeightForIndex
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
      defaultColWidthPx: this.defColWidth
    });

    // Compute positioned overlays (slice 160) from the current
    // overlay map + resource map. Each overlay's pixel
    // position is anchor cell's left/top (cumulative col/row
    // widths/heights) + the overlay's dx/dy offset.
    const positionedOverlays = computePositionedOverlays(
      this.overlays,
      this.resources,
      colWidthForIndex,
      rowHeightForIndex
    );

    // Compute positioned popup buttons (slice 161). Each
    // button anchors at its cell's bottom-right corner.
    const positionedPopupButtons = computePositionedPopupButtons(
      this.popupButtonStates,
      colWidthForIndex,
      rowHeightForIndex
    );

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
      editor: this.editor,
      overlays: positionedOverlays,
      popupButtons: positionedPopupButtons,
      // Comment-bearing + hyperlink cell keys (slice 165).
      // The maps from setCellComments / setHyperlinksTooltips
      // are keyed by the cell-key format already, so a Set
      // of their keys is the right shape for the render.
      commentCells: new Set(Object.keys(this.cellComments)),
      hyperlinkCells: new Set(Object.keys(this.hyperlinksTooltips))
    };
  }

  /**
   * Test-only accessor: the cells currently known to the client, keyed by
   * {@code toKey(col, row)}.
   */
  getCellData(): ReadonlyMap<string, CellData> {
    return this.cellDataByKey;
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

  /** Test-only accessor: the sheet-tabs sub-element (slice 154). */
  getSheetTabs(): SpreadsheetSheetTabs {
    return this.sheetTabs;
  }

  /** Test-only accessor: the formula-bar sub-element (slice 175). */
  getFormulaBar(): SpreadsheetFormulaBar {
    return this.formulaBar;
  }

  /**
   * Push fresh state to the sheet tabs from the current
   * {@code sheetNames} + {@code sheetIndex} fields. Called from
   * the public setters when those change, AND from any caller
   * that wants to force a re-render. Slice 154.
   */
  private syncSheetTabs(): void {
    this.sheetTabs.renderState = {
      sheetNames: this.sheetNames,
      activeIndex: this.sheetIndex
    };
  }

  /**
   * Sheet-tab click handler — translates to a `sheetSelected`
   * spreadsheet-event for the server. The server is expected
   * to call back into {@code setSheetIndex} + push fresh data
   * via the cell value APIs, which the next render picks up.
   * Slice 154.
   */
  private readonly handleSheetTabClick = (e: CustomEvent<{ index: number }>): void => {
    const { index } = e.detail;
    // Optimistically reflect the click in the tab strip so the
    // active tab visually moves immediately, without waiting
    // for the server's response. The server's setSheetIndex
    // will overwrite this when it lands.
    this.sheetIndex = index;
    this.syncSheetTabs();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'sheetSelected',
          data: {
            sheetName: this.sheetNames[index - 1] ?? '',
            scrollLeft: this.scrollContainer.scrollLeft,
            scrollTop: this.scrollContainer.scrollTop
          }
        },
        bubbles: true,
        composed: true
      })
    );
  };

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
    // The wrapper calls this after batched state changes
    // (setRowH / setColW / setRows / setCols / ...) settle.
    // Recompute viewport bounds + push fresh render state.
    // Slice 162.
    this.recomputeViewport();
  }

  resize(): void {
    // The wrapper's `spreadsheetResizeObserver` calls this
    // on container size changes. We also have our own
    // ResizeObserver (slice 142) on the scroll container —
    // calling recomputeViewport here is idempotent + cheap.
    // Slice 162.
    this.recomputeViewport();
  }

  notifyStateChanges(_changedPropertyNames: string[], _firstUpdate: boolean): void {
    // TODO Phase 3.2 — fold the property batch into a single render pass.
  }

  // ====================================================================
  // 12 explicit RPC methods (server→client action messages)
  // ====================================================================

  updateBottomRightCellValues(cellData: CellData[]): void {
    this.storeCellData(cellData);
    this.scheduleRender();
  }
  updateTopLeftCellValues(cellData: CellData[]): void {
    this.storeCellData(cellData);
    this.scheduleRender();
  }
  updateTopRightCellValues(cellData: CellData[]): void {
    this.storeCellData(cellData);
    this.scheduleRender();
  }
  updateBottomLeftCellValues(cellData: CellData[]): void {
    this.storeCellData(cellData);
    this.scheduleRender();
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
  updateFormulaBar(possibleName: string, _col: number, _row: number): void {
    // Slice 175: update the address field with the server-supplied
    // name (cell address or named range). The formula field is
    // updated separately by {@link showSelectedCell}.
    this.formulaBar.renderState = {
      address: possibleName,
      formula: this.formulaBar.renderState.formula
    };
  }
  invalidCellAddress(): void {
    // TODO Phase 3.3 — flash the address bar to indicate invalid input.
  }
  showSelectedCell(
    name: string,
    _col: number,
    _row: number,
    cellValue: string,
    _formula: boolean,
    _locked: boolean,
    _initialSelection: boolean
  ): void {
    // Slice 175: server-pushed update of both formula-bar fields
    // — happens after the user clicks a cell or navigates with
    // arrow keys server-side.
    this.formulaBar.renderState = {
      address: name,
      formula: cellValue
    };
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
  cellsUpdated(updatedCellData: CellData[]): void {
    this.storeCellData(updatedCellData);
    this.scheduleRender();
  }
  refreshCellStyles(): void {
    this.scheduleRender();
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
    this.rowHeights = coerceNumberArray(rowHeightsInPoints);
  }
  setColW(colWidthsInPixels: number[] | Int32Array): void {
    this.colWidths = coerceNumberArray(colWidthsInPixels);
  }
  setHiddenColumnIndexes(indexes: number[]): void {
    this.hiddenColumnIndexes = coerceArray(indexes);
  }
  setHiddenRowIndexes(indexes: number[]): void {
    this.hiddenRowIndexes = coerceArray(indexes);
  }
  setLockedColumnIndexes(indexes: number[]): void {
    this.lockedColumnIndexes = coerceArray(indexes);
  }
  setLockedRowIndexes(indexes: number[]): void {
    this.lockedRowIndexes = coerceArray(indexes);
  }
  setSheetIndex(index: number): void {
    this.sheetIndex = index;
    // Update the sheet-tabs strip so the active tab moves to
    // the server's chosen sheet (slice 154).
    this.syncSheetTabs();
  }
  setSheetNames(names: string[]): void {
    this.sheetNames = coerceArray(names);
    // Re-render the sheet-tabs strip with the new names list
    // (slice 154).
    this.syncSheetTabs();
  }
  setVerticalScrollPositions(positions: number[]): void {
    this.verticalScrollPositions = coerceArray(positions);
  }
  setHorizontalScrollPositions(positions: number[]): void {
    this.horizontalScrollPositions = coerceArray(positions);
  }
  setCellStyleToCSSStyle(map: Record<number, string>): void {
    this.cellStyleToCSSStyle = coerceObject(map);
  }
  setRowIndexToStyleIndex(map: Record<number, number>): void {
    this.rowIndexToStyleIndex = coerceObject(map);
  }
  setColumnIndexToStyleIndex(map: Record<number, number>): void {
    this.columnIndexToStyleIndex = coerceObject(map);
  }
  setShiftedCellBorderStyles(cssRules: string[]): void {
    this.shiftedCellBorderStyles = coerceArray(cssRules);
  }
  setConditionalFormattingStyles(map: Record<number, string>): void {
    this.conditionalFormattingStyles = coerceObject(map);
  }
  setColGroupingData(groups: GroupingData[]): void {
    this.colGroupingData = coerceArray(groups);
  }
  setRowGroupingData(groups: GroupingData[]): void {
    this.rowGroupingData = coerceArray(groups);
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
  setReload(triggerReload: boolean): void {
    if (triggerReload) {
      this.scheduleRender();
    }
  }
  setSheetProtected(isProtected: boolean): void {
    this.sheetProtected = isProtected;
  }
  setMergedRegions(regions: MergedRegion[]): void {
    this.mergedRegions = coerceArray(regions);
  }
  setCellKeysToEditorIdMap(map: Record<string, string>): void {
    this.cellKeysToEditorIdMap = coerceObject(map);
  }
  setComponentIDtoCellKeysMap(map: Record<string, string>): void {
    this.componentIDtoCellKeysMap = coerceObject(map);
  }
  setCellComments(map: Record<string, string>): void {
    this.cellComments = coerceObject(map);
  }
  setCellCommentAuthors(map: Record<string, string>): void {
    this.cellCommentAuthors = coerceObject(map);
  }
  setVisibleCellComments(keys: string[]): void {
    this.visibleCellComments = coerceArray(keys);
  }
  setInvalidFormulaCells(keys: Set<string> | string[] | string): void {
    // Server may push as a JSON-encoded array; tolerate both.
    const arr = coerceArray(keys as unknown as string[] | string | null | undefined);
    this.invalidFormulaCells = new Set(arr);
  }
  setOverlays(map: Record<string, OverlayInfo>): void {
    this.overlays = coerceObject(map);
  }
  setShowCustomEditorOnFocus(show: boolean): void {
    this.showCustomEditorOnFocus = show;
  }
  setInvalidFormulaErrorMessage(message: string | null): void {
    this.invalidFormulaErrorMessage = message;
  }
  setNamedRanges(names: string[]): void {
    this.namedRanges = coerceArray(names);
  }
  setResources(_host: HTMLElement, jsonString: string): void {
    // Parse the JSON-serialized resource map (id → URL) so
    // overlay rendering can resolve image sources. The host
    // argument is unused — kept for signature compatibility
    // with the GWT API. Slice 160.
    if (!jsonString) {
      this.resources = {};
      return;
    }
    try {
      const parsed = JSON.parse(jsonString) as Record<string, string>;
      this.resources = parsed ?? {};
    } catch {
      // Malformed JSON — keep the previous map; the server
      // will retry on next push. Don't crash the connector.
      this.resources = {};
    }
  }
  setPopupButtons(popupButtons: PopupButtonState[]): void {
    // Store the raw popup-button list. The connector computes
    // positions at render time via `buildRenderState`. Slice
    // 161.
    this.popupButtonStates = coerceArray(popupButtons);
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
    // Slice 180: the server doesn't push explicit
    // headerRowHeight / headerColumnWidth — only this boolean.
    // When headings should be visible, give the header strips
    // sensible defaults so they actually render. When hidden,
    // collapse them. These match the GWT-era defaults
    // (~20 px tall, ~40 px wide) and can be customized via
    // the CSS custom properties on the host.
    if (visible) {
      if (this.headerRowHeight === 0) {
        this.headerRowHeight = 20;
      }
      if (this.headerColumnWidth === 0) {
        this.headerColumnWidth = 40;
      }
    } else {
      this.headerRowHeight = 0;
      this.headerColumnWidth = 0;
    }
    this.recomputeViewport();
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
    this.hyperlinksTooltips = coerceObject(tooltips);
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
  setCellAddedToSelectionAndSelectedCallback(callback: (event: unknown) => void): void {
    this.callbacks.CellAddedToSelectionAndSelected = callback;
  }
  setCellsAddedToRangeSelectionCallback(callback: (event: unknown) => void): void {
    this.callbacks.CellsAddedToRangeSelection = callback;
  }
  setRowSelectedCallback(callback: (event: unknown) => void): void {
    this.callbacks.RowSelected = callback;
  }
  setRowAddedToRangeSelectionCallback(callback: (event: unknown) => void): void {
    this.callbacks.RowAddedToRangeSelection = callback;
  }
  setColumnSelectedCallback(callback: (event: unknown) => void): void {
    this.callbacks.ColumnSelected = callback;
  }
  setColumnAddedToSelectionCallback(callback: (event: unknown) => void): void {
    this.callbacks.ColumnAddedToSelection = callback;
  }
  setSelectionIncreasePaintedCallback(callback: (event: unknown) => void): void {
    this.callbacks.SelectionIncreasePainted = callback;
  }
  setSelectionDecreasePaintedCallback(callback: (event: unknown) => void): void {
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
  setSetCellStyleWidthRatiosCallback(callback: (event: unknown) => void): void {
    this.callbacks.SetCellStyleWidthRatios = callback;
  }
  setProtectedCellWriteAttemptedCallback(callback: (event: unknown) => void): void {
    this.callbacks.ProtectedCellWriteAttempted = callback;
  }
  setOnPasteCallback(callback: (event: unknown) => void): void {
    this.callbacks.OnPaste = callback;
  }
  setClearSelectedCellsOnCutCallback(callback: (event: unknown) => void): void {
    this.callbacks.ClearSelectedCellsOnCut = callback;
  }
  setUpdateCellCommentCallback(callback: (event: unknown) => void): void {
    this.callbacks.UpdateCellComment = callback;
  }
  setOnConnectorInitCallback(callback: (event: unknown) => void): void {
    this.callbacks.OnConnectorInit = callback;
  }
  setContextMenuOpenOnSelectionCallback(callback: (event: unknown) => void): void {
    this.callbacks.ContextMenuOpenOnSelection = callback;
  }
  setActionOnCurrentSelectionCallback(callback: (event: unknown) => void): void {
    this.callbacks.ActionOnCurrentSelection = callback;
  }
  setRowHeaderContextMenuOpenCallback(callback: (event: unknown) => void): void {
    this.callbacks.RowHeaderContextMenuOpen = callback;
  }
  setActionOnRowHeaderCallback(callback: (event: unknown) => void): void {
    this.callbacks.ActionOnRowHeader = callback;
  }
  setColumnHeaderContextMenuOpenCallback(callback: (event: unknown) => void): void {
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

  onPopupButtonOpened(_row: number, _column: number, _contentId: string, _appId: string): void {
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

/**
 * Pure helper: build the positioned-popup-button list from the
 * server-pushed `PopupButtonState` list. Each button anchors at
 * the bottom-right corner of its target cell — pixel position =
 * (sum of widths 1..col + col width, sum of heights 1..row + row
 * height). Slice 161.
 */
export function computePositionedPopupButtons(
  states: ReadonlyArray<PopupButtonState>,
  colWidth: (col: number) => number,
  rowHeight: (row: number) => number
): PositionedPopupButton[] {
  return states.map((state) => {
    let left = 0;
    for (let c = 1; c <= state.col; c++) {
      left += colWidth(c);
    }
    let top = 0;
    for (let r = 1; r <= state.row; r++) {
      top += rowHeight(r);
    }
    return {
      col: state.col,
      row: state.row,
      leftPx: left,
      topPx: top,
      active: state.active
    };
  });
}

/**
 * Pure helper: build the positioned-overlay list from the server-
 * pushed `OverlayInfo` map + resource map. Each overlay's pixel
 * position is `anchor cell's left/top` (cumulative sums of col/
 * row sizes 1..anchor-1) + the overlay's `dx`/`dy` offset.
 *
 * Pure (no DOM, no instance state) so it's straightforward to
 * unit-test in isolation. Slice 160.
 */
export function computePositionedOverlays(
  overlays: Record<string, OverlayInfo>,
  resources: Record<string, string>,
  colWidth: (col: number) => number,
  rowHeight: (row: number) => number
): PositionedOverlay[] {
  const result: PositionedOverlay[] = [];
  for (const [id, info] of Object.entries(overlays)) {
    let left = 0;
    for (let c = 1; c < info.col; c++) {
      left += colWidth(c);
    }
    let top = 0;
    for (let r = 1; r < info.row; r++) {
      top += rowHeight(r);
    }
    const positioned: PositionedOverlay = {
      id,
      type: info.type,
      src: info.type === 'IMAGE' ? resources[id] : undefined,
      leftPx: left + info.dx,
      topPx: top + info.dy,
      widthPx: info.width,
      heightPx: info.height
    };
    result.push(positioned);
  }
  return result;
}

/**
 * Pure helper: classify a selection shape and build the matching
 * server-bound {@link SpreadsheetEvent}.
 *
 * Detection order (specific → general):
 *   1. whole column: r1=1, r2=rows, c1=c2 → {@code columnSelected}
 *   2. whole row:    c1=1, c2=cols, r1=r2 → {@code rowSelected}
 *   3. single cell:  c1=c2, r1=r2         → {@code cellSelected}
 *   4. otherwise:                          → {@code cellRangeSelected}
 *
 * Note: when `rows`/`cols` is 0 (uninitialized grid) the "whole
 * column / row" branches collapse to cellSelected since the
 * full-axis match can't succeed against a zero-width axis.
 *
 * Slice 150.
 */
/**
 * Coerce a value that may be either an array or a JSON-encoded
 * string into an array. The Flow runtime serializes server-side
 * property values to JSON strings when they reach the
 * {@code <vaadin-spreadsheet>} element's attribute lane, and they
 * arrive at the connector's setters as strings; the GWT-side
 * client used to JSON-parse them on the way in. Slice 179.
 */
export function coerceArray<T>(value: T[] | string | null | undefined): T[] {
  if (value == null) {
    return [];
  }
  if (Array.isArray(value)) {
    return value;
  }
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
  // Typed arrays (Int32Array, Float32Array) — already array-like;
  // convert to a real array for downstream `.map` etc. compatibility.
  if (typeof (value as any).length === 'number') {
    return Array.from(value as any);
  }
  return [];
}

/**
 * Object-shaped variant of {@link coerceArray}. The same JSON-string-
 * coming-from-Flow-attribute path applies to record/map setters.
 * Slice 179.
 */
export function coerceObject<V>(value: Record<string, V> | string | null | undefined): Record<string, V> {
  if (value == null) {
    return {};
  }
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return parsed && typeof parsed === 'object' ? parsed : {};
    } catch {
      return {};
    }
  }
  return value;
}

/**
 * Numeric-array variant. Typed arrays (Int32Array, Float32Array)
 * arrive intact when pushed via Flow's typed-array support; JSON
 * strings get parsed. Slice 179.
 */
export function coerceNumberArray(
  value: number[] | Int32Array | Float32Array | string | null | undefined
): number[] | Int32Array | Float32Array {
  if (value == null) {
    return [];
  }
  if (Array.isArray(value) || value instanceof Int32Array || value instanceof Float32Array) {
    return value;
  }
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
  return [];
}

/**
 * Pure helper: return the set of column indices that intersect
 * the active range of the selection. Slice 173 — fed to the
 * column-header element so each cell whose column is inside the
 * active range emits {@code part="column-header header-selected"}.
 */
export function selectedColumnSet(selection: SelectionState): Set<number> {
  const range = selection.activeRange;
  const min = Math.min(range.c1, range.c2);
  const max = Math.max(range.c1, range.c2);
  const set = new Set<number>();
  for (let c = min; c <= max; c++) {
    set.add(c);
  }
  return set;
}

/**
 * Pure helper: symmetric row variant of {@link selectedColumnSet}.
 * Slice 173.
 */
export function selectedRowSet(selection: SelectionState): Set<number> {
  const range = selection.activeRange;
  const min = Math.min(range.r1, range.r2);
  const max = Math.max(range.r1, range.r2);
  const set = new Set<number>();
  for (let r = min; r <= max; r++) {
    set.add(r);
  }
  return set;
}

/**
 * Pure helper: is the given (col, row) inside the active range OR
 * any aux range of the current selection? Slice 166 uses this for
 * "right-click outside selection → move selection to clicked cell"
 * Excel behavior.
 */
export function isCellInSelection(col: number, row: number, selection: SelectionState): boolean {
  const inRange = (range: CellRange): boolean => {
    const cMin = Math.min(range.c1, range.c2);
    const cMax = Math.max(range.c1, range.c2);
    const rMin = Math.min(range.r1, range.r2);
    const rMax = Math.max(range.r1, range.r2);
    return col >= cMin && col <= cMax && row >= rMin && row <= rMax;
  };
  if (inRange(selection.activeRange)) {
    return true;
  }
  for (const aux of selection.auxRanges) {
    if (inRange(aux)) {
      return true;
    }
  }
  return false;
}

/**
 * Pure helper: convert {@link GroupingData} entries into
 * {@link PositionedGrouping} entries with pre-computed pixel
 * positions. Slice 167.
 *
 * - {@code startPx}: pixel x-position of column {@code startIndex}
 *   (sum of widths of columns 1..startIndex-1).
 * - {@code endPx}: pixel x-position of the column AFTER
 *   {@code endIndex} (so the line spans the full data range).
 * - {@code buttonPx}: when {@code !inversed}, button is at
 *   {@code startPx - GROUPING_BUTTON_SIZE_PX}; when {@code inversed},
 *   button is at {@code endPx}.
 *
 * Both indices are 0-based in the wire format (server-side
 * `GroupingData.startIndex`/`endIndex` use POI's 0-based row/col
 * indices). The connector's `colWidths` array is 0-indexed too.
 */
export function computePositionedColGroupings(
  groupings: ReadonlyArray<GroupingData>,
  colWidths: ReadonlyArray<number>,
  inversed: boolean,
  defColWidthPx: number
): PositionedGrouping[] {
  const widthAt = (col0: number): number => (col0 < colWidths.length ? colWidths[col0] : defColWidthPx);
  const positions: PositionedGrouping[] = [];
  for (const g of groupings) {
    let startPx = 0;
    for (let i = 0; i < g.startIndex; i++) {
      startPx += widthAt(i);
    }
    let endPx = startPx;
    for (let i = g.startIndex; i <= g.endIndex; i++) {
      endPx += widthAt(i);
    }
    const buttonPx = inversed ? endPx : startPx - GROUPING_BUTTON_SIZE_PX;
    positions.push({
      uniqueIndex: g.uniqueIndex,
      level: g.level,
      startPx,
      endPx,
      buttonPx,
      collapsed: g.collapsed
    });
  }
  return positions;
}

export function buildSelectionEvent(
  activeCell: { col: number; row: number },
  range: CellRange,
  rows: number,
  cols: number
): SpreadsheetEvent {
  const isSingleCol = range.c1 === range.c2;
  const isSingleRow = range.r1 === range.r2;
  const isWholeCol = cols > 0 && rows > 0 && isSingleCol && range.r1 === 1 && range.r2 === rows;
  const isWholeRow = cols > 0 && rows > 0 && isSingleRow && range.c1 === 1 && range.c2 === cols;

  if (isWholeCol) {
    const data: ColumnSelectedData = {
      column: range.c1,
      firstRowIndex: range.r1
    };
    return { type: 'columnSelected', data };
  }
  if (isWholeRow) {
    const data: RowSelectedData = {
      row: range.r1,
      firstColumnIndex: range.c1
    };
    return { type: 'rowSelected', data };
  }
  if (isSingleCol && isSingleRow) {
    const data: CellSelectedData = {
      row: activeCell.row,
      column: activeCell.col,
      // Every TS-side selection write replaces the prior
      // range (we don't yet support non-coherent / Ctrl-
      // click auxRanges, slice 146 deferred). The wrapper
      // can still ignore this when re-applying state.
      oldSelectionRangeDiscarded: true
    };
    return { type: 'cellSelected', data };
  }
  const data: CellRangeSelectedData = {
    row1: range.r1,
    col1: range.c1,
    row2: range.r2,
    col2: range.c2
  };
  return { type: 'cellRangeSelected', data };
}

/**
 * Pure helper: given the pressed key + modifier state + current
 * selection + grid bounds, compute the next active cell (or the
 * next extend-cell when shift is held).
 *
 * Returns {@code null} when the key isn't a recognized navigation
 * key — the connector returns early without consuming the event.
 *
 * Slice 147 introduced the arrow keys; slice 149 extends with
 * Tab / Enter / Home / End / Ctrl+Home / Ctrl+End. Each navigation
 * key has the same modifier semantics:
 *   - plain  : move active cell, collapse range
 *   - shift  : move extend cell, keep active as anchor
 *   - ctrl   : absolute jumps (Ctrl+Home/End)
 *
 * Pure (no DOM, no instance state) so it's straightforward to
 * unit-test in isolation.
 */
export interface NavigationTarget {
  kind: 'move' | 'extend';
  col: number;
  row: number;
}

export function computeNavigationTarget(
  key: string,
  modifiers: { shiftKey: boolean; ctrlKey: boolean; metaKey: boolean },
  state: {
    activeCell: { col: number; row: number };
    extendCol: number;
    extendRow: number;
    cols: number;
    rows: number;
  }
): NavigationTarget | null {
  const { activeCell, extendCol, extendRow, cols, rows } = state;
  const ctrlOrMeta = modifiers.ctrlKey || modifiers.metaKey;
  // Arrow keys participate in extend (shift+arrow grows the
  // range, base = extendCell). Tab/Enter/Home/End always MOVE
  // the active cell, ignoring shift's "extend" meaning — they
  // use shift as a direction flag (shift+Tab = back, shift+
  // Enter = up) and base from activeCell.
  const isArrow = key === 'ArrowUp' || key === 'ArrowDown' || key === 'ArrowLeft' || key === 'ArrowRight';
  const isExtend = isArrow && modifiers.shiftKey;
  const baseCol = isExtend ? extendCol : activeCell.col;
  const baseRow = isExtend ? extendRow : activeCell.row;
  let nextCol: number | null = null;
  let nextRow: number | null = null;

  switch (key) {
    case 'ArrowUp':
      nextCol = baseCol;
      nextRow = baseRow - 1;
      break;
    case 'ArrowDown':
      nextCol = baseCol;
      nextRow = baseRow + 1;
      break;
    case 'ArrowLeft':
      nextCol = baseCol - 1;
      nextRow = baseRow;
      break;
    case 'ArrowRight':
      nextCol = baseCol + 1;
      nextRow = baseRow;
      break;
    case 'Enter':
      // Shift+Enter goes up, plain Enter goes down — both
      // move the active cell from its current position.
      nextCol = baseCol;
      nextRow = modifiers.shiftKey ? baseRow - 1 : baseRow + 1;
      break;
    case 'Tab':
      // Shift+Tab goes left, plain Tab goes right.
      nextCol = modifiers.shiftKey ? baseCol - 1 : baseCol + 1;
      nextRow = baseRow;
      break;
    case 'Home':
      // Ctrl+Home jumps to A1; plain Home goes to col 1 in
      // the current row.
      nextCol = 1;
      nextRow = ctrlOrMeta ? 1 : baseRow;
      break;
    case 'End':
      // Ctrl+End jumps to the last cell; plain End goes to
      // the last column in the current row.
      nextCol = cols;
      nextRow = ctrlOrMeta ? rows : baseRow;
      break;
    default:
      return null;
  }
  return {
    kind: isExtend ? 'extend' : 'move',
    col: Math.max(1, Math.min(cols, nextCol!)),
    row: Math.max(1, Math.min(rows, nextRow!))
  };
}
