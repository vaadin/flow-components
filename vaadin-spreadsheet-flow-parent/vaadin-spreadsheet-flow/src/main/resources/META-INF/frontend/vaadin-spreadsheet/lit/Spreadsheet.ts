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
  addAuxRange,
  normalizedRange,
  selectRange,
  selectSingleCell,
  type CellRange,
  type SelectionState
} from './state/SelectionState.js';
import type {
  CellRangeSelectedData,
  CellAddedToSelectionData,
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
import { initialEditValue } from './algorithms/PercentageEntry.js';
import { cellCoordToAddress } from './algorithms/CellRef.js';
import { parseColRow, toKey } from './algorithms/CellKeys.js';
import {
  SlottedVirtualChildren,
  actionIconSlotName,
  cellComponentSlotName,
  cellEditorSlotName,
  flowVirtualChildResolver,
  namedSlot,
  overlayComponentSlotName,
  popupContentSlotName
} from './dom/VirtualChildren.js';
import {
  computePaintRectangle,
  grownSelection,
  isNonEmpty,
  type CellRect,
  type PaintOutcome
} from './algorithms/SelectionPaint.js';
import { expandToMergedRegions } from './algorithms/MergedSelection.js';
import { isMultiCellRange, walkInsideSelection } from './algorithms/SelectionWalk.js';
import { resolveSheetRename } from './algorithms/SheetName.js';
import type { WalkDirection } from './algorithms/SelectionWalk.js';
import { serializeToTsv } from './algorithms/Tsv.js';
import { SpreadsheetCellGrid } from './elements/SpreadsheetCellGrid.js';
import {
  SpreadsheetColumnHeader,
  GROUPING_BUTTON_SIZE_PX,
  type PositionedGrouping,
  GROUPING_LEVEL_HEIGHT_PX
} from './elements/SpreadsheetColumnHeader.js';
import { SpreadsheetRowHeader } from './elements/SpreadsheetRowHeader.js';
import { SpreadsheetHeaderCorner } from './elements/SpreadsheetHeaderCorner.js';
import { SpreadsheetSheetTabs } from './elements/SpreadsheetSheetTabs.js';
import { SpreadsheetFormulaBar } from './elements/SpreadsheetFormulaBar.js';
import { emptyContextMenuRenderState, SpreadsheetContextMenu } from './elements/SpreadsheetContextMenu.js';
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
/**
 * Where the selection goes when an edit is committed with a key.
 *
 * Enter moves down and Tab right, each reversed by shift; an arrow key
 * moves the way it points. Returns null for a commit that should leave the
 * selection where it is — a blur, or a key with no direction.
 */
export function commitStep(key: string | undefined, shiftKey: boolean): { dCol: number; dRow: number } | null {
  const back = shiftKey ? -1 : 1;
  switch (key) {
    case 'Enter':
      return { dCol: 0, dRow: back };
    case 'Tab':
      return { dCol: back, dRow: 0 };
    case 'ArrowUp':
      return { dCol: 0, dRow: -1 };
    case 'ArrowDown':
      return { dCol: 0, dRow: 1 };
    case 'ArrowLeft':
      return { dCol: -1, dRow: 0 };
    case 'ArrowRight':
      return { dCol: 1, dRow: 0 };
    default:
      return null;
  }
}

/**
 * Which way Enter or Tab walks the active cell inside a selected range.
 *
 * Only those two keys walk: an arrow key means "leave this selection and
 * go there", which is a different gesture. Returns null for anything else.
 */
export function inSelectionWalkDirection(key: string | undefined, shiftKey: boolean): WalkDirection | null {
  if (key === 'Enter') {
    return shiftKey ? 'up' : 'down';
  }
  if (key === 'Tab') {
    return shiftKey ? 'left' : 'right';
  }
  return null;
}

/**
 * Sample the cell-style width measurement uses: ten digits, matching what
 * the GWT client measured, so the ratios keep the same meaning server-side.
 */
const WIDTH_PROBE_TEXT = '5555555555';

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
  // The message a cell with a formula the workbook cannot make sense of
  // shows on hover. Held here rather than waited for: the server only
  // pushes it when an application replaces the default, and until then a
  // null message means the cell explains nothing at all.
  private invalidFormulaErrorMessage: string | null = 'Invalid formula';
  private namedRanges: string[] = [];
  private cellComments: Record<string, string> = {};
  private cellCommentAuthors: Record<string, string> = {};
  private visibleCellComments: string[] = [];
  private invalidFormulaCells: Set<string> = new Set();
  private overlays: Record<string, OverlayInfo> = {};
  /**
   * Overlay IDs the server has published a resource for, from
   * {@link setResources}. The URLs themselves are not kept: they live on
   * the host as `resource-<id>` attributes and are read at render time by
   * {@link resolveResources}. Slice 160.
   */
  private resourceKeys: string[] = [];
  /** The element the `resource-<id>` attributes live on. */
  private resourceHost: HTMLElement | null = null;
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
  /**
   * Whether a coalesced repaint is already queued.
   *
   * The repaint runs on a microtask, not an animation frame. Everything
   * that queues one is server-driven — a property push or one of the
   * cell-value RPCs — and those arrive several to a response. A microtask
   * still folds the whole response into one render, but runs before the
   * browser leaves the turn that processed it, so the sheet is never a
   * frame behind the answer a caller has already been given. Scrolling
   * does not come through here: the scroll handler repaints directly.
   */
  private renderQueued = false;

  /**
   * Whether the server has been told the connector is live and where the
   * viewport starts. See {@link announceViewport}.
   */
  private viewportAnnounced = false;

  /** The context menu shown on right-click. Empty until the server answers. */
  private readonly contextMenu: SpreadsheetContextMenu;

  /**
   * Viewport position of the most recent right-click, so `showActions` can
   * place the menu where the user clicked. The contextmenu event and the
   * server's answer arrive in separate turns.
   */
  private lastContextMenuPosition: { x: number; y: number } = { x: 0, y: 0 };

  /** The cell-grid sub-element. Created in the constructor + appended to the host. */
  private readonly cellGrid: SpreadsheetCellGrid;
  /**
   * Server-side components the sheet renders as overlays — charts, and
   * anything else added with {@code Spreadsheet.setComponent}. Flow creates
   * the elements but leaves placing them to us; they become light-DOM
   * children of the cell-grid so the slots in its shadow root can project
   * them over the sheet.
   */
  /**
   * Leftmost sheet tab currently shown. The strip scrolls by whole
   * tabs; the scroll buttons move this and the server is told, so it
   * can restore the position on the next load.
   */
  private firstVisibleTab = 0;

  /**
   * The tab whose name the user is typing, 1-based, or 0 when none is.
   * Client-side only: the server hears about a rename once, when it is
   * finished.
   */
  private renamingSheetIndex = 0;

  /**
   * The cell whose comment is being written, as a cell key, or null when
   * none is. The server opens this with editCellComment and hears back
   * once, when the note is finished.
   */
  private editingCommentKey: string | null = null;

  /**
   * Whether the user is holding shift and growing the selection.
   *
   * While they are, the address field counts the block out - "3R x 3C" -
   * rather than naming a cell, which is what Excel's name box does. It
   * has to survive the server echoing the selection straight back, or
   * the count would be replaced by the anchor's address before anyone
   * could read it.
   */
  private extendingSelection = false;
  private readonly overlayComponents: SlottedVirtualChildren;
  /**
   * Server-side components rendered inside a cell. Same mechanism as
   * {@link overlayComponents} and the same host, kept apart because the two
   * project through differently named slots. A component is either an
   * overlay or in a cell, never both, so the two id sets never overlap.
   */
  private readonly cellComponents: SlottedVirtualChildren;
  /**
   * Custom cell editors. A third holder rather than a flag on
   * {@link cellComponents}, because a cell can carry both and each needs
   * its own slot: swapping one for the other must not disturb the other's
   * element.
   */
  private readonly cellEditors: SlottedVirtualChildren;

  /**
   * Content of the popup a popup button opens. The server attaches it as a
   * virtual child of the popup button, so it needs placing like any other
   * hosted component; it goes in its own holder because it is shown in an
   * overlay rather than in the grid.
   */
  private readonly popupContents: SlottedVirtualChildren;

  /**
   * Icons on context-menu actions. Server-side components attached to the
   * action, so they need placing like any other; a fourth holder because
   * they live in the context menu's shadow root rather than the grid's.
   */
  private readonly actionIcons: SlottedVirtualChildren;

  /** The overlay the open popup's content is shown in. */
  private readonly popupOverlay: HTMLElement;

  /** The popup that is open, or null when none is. */
  private openPopup: { row: number; col: number; nodeId: string } | null = null;

  /** Whether the cell styles have changed since the last width measurement. */
  private styleWidthRatiosStale = false;

  /** The viewport bounds last reported to the server, as "r1,c1,r2,c2". */
  private announcedBounds = '';

  /**
   * The fill-handle drag in progress: the selection it started from and the
   * strip it is currently painting. Null when no drag is running.
   */
  private painting: { from: CellRect; outcome: PaintOutcome } | null = null;

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
  private readonly headerCorner: SpreadsheetHeaderCorner;

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
  /**
   * The component's own root inside {@link renderTarget}.
   *
   * Everything the spreadsheet draws lives in here, and this element —
   * not the host — owns the vertical layout. The host belongs to the
   * application: writing a layout onto it means an application that
   * positions the component its own way is silently overridden.
   */
  private readonly root: HTMLElement;
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
    this.headerCorner = new SpreadsheetHeaderCorner();
    this.sheetTabs = new SpreadsheetSheetTabs();
    this.formulaBar = new SpreadsheetFormulaBar();
    this.contextMenu = new SpreadsheetContextMenu();
    const resolveVirtualChild = (nodeId: string): HTMLElement | null =>
      flowVirtualChildResolver(overlayComponentAppId(host))(nodeId);
    // The components live in the host's light DOM, where an application's
    // own components belong — findable by an ordinary document query, the
    // way every other Vaadin component is. The slots that show them are in
    // the cell-grid's shadow root, so each gets a forwarding slot.
    this.overlayComponents = new SlottedVirtualChildren(
      host,
      this.cellGrid,
      resolveVirtualChild,
      overlayComponentSlotName
    );
    this.cellComponents = new SlottedVirtualChildren(host, this.cellGrid, resolveVirtualChild, cellComponentSlotName);
    this.cellEditors = new SlottedVirtualChildren(host, this.cellGrid, resolveVirtualChild, cellEditorSlotName);
    // The popup overlay is our own element inside the connector's shadow
    // root, one boundary from the host's light DOM, so the slot it renders
    // projects the content directly — no forwarder, hence the same element
    // for both hosts.
    this.popupOverlay = document.createElement('div');
    this.popupOverlay.className = 'popup-overlay';
    this.popupOverlay.setAttribute('part', 'popup-overlay');
    this.popupOverlay.hidden = true;
    this.popupOverlay.style.cssText =
      'position:absolute;z-index:20;box-sizing:border-box;' +
      'background:var(--vaadin-spreadsheet-popup-background, #fff);' +
      'border:1px solid var(--vaadin-spreadsheet-popup-border-color, #c0c0c0);';
    this.actionIcons = new SlottedVirtualChildren(host, this.contextMenu, resolveVirtualChild, actionIconSlotName);
    this.popupContents = new SlottedVirtualChildren(host, this.popupOverlay, resolveVirtualChild, popupContentSlotName);
    // A flex column holding the formula bar, the sheet, and the tabs.
    // `flex:1 1 auto` fills a host that has a definite height and
    // content-sizes against one that does not; a percentage height
    // would resolve to `auto` against a host whose own height is a
    // percentage, and the sheet would grow to the height of the whole
    // grid. Set on the element rather than in the stylesheet because
    // this is the component's own element and its sizing has to hold
    // wherever the connector is used, stylesheet or not.
    this.root = document.createElement('div');
    this.root.className = 'spreadsheet-root';
    // min-height is the floor the GWT client had on .v-spreadsheet: a
    // spreadsheet nobody has given a height to is still a spreadsheet,
    // not a one-line strip.
    this.root.style.cssText =
      'display:flex;flex-direction:column;width:100%;flex:1 1 auto;' +
      // position:relative so the context menu, which is a child, is
      // placed in the sheet's own coordinate space rather than the
      // viewport's.
      'min-height:100px;box-sizing:border-box;position:relative;';
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
    // height:0 with flex:1 1 0 is what keeps the sheet from sizing the
    // component. A column flex item still offers its content height
    // when the container's own height is indefinite, and the grid
    // inside is as tall as the whole sheet, so a spreadsheet with no
    // height became thousands of pixels tall. A stated height of 0 is
    // definite, so it offers nothing, and flex-grow still gives it
    // every pixel left over once the host does have a height. The GWT
    // client got this from absolutely positioned panes.
    this.scrollContainer.style.cssText =
      'position:relative;overflow:auto;width:100%;flex:1 1 0;height:0;min-height:0;' +
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
      'grid-column:1;grid-row:1;' + 'position:sticky;top:0;left:0;z-index:6;' + 'width:0;height:0;';
    this.headerCorner.addEventListener('level-header-click', this.handleLevelHeaderClick as EventListener);
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
    this.root.appendChild(this.formulaBar);
    this.root.appendChild(this.contextMenu);
    this.formulaBar.addEventListener('sheet-address-changed', this.handleSheetAddressChanged as EventListener);
    this.formulaBar.addEventListener('formula-commit', this.handleFormulaCommit as EventListener);
    this.formulaBar.addEventListener('formula-cancel', this.handleFormulaCancel);
    this.root.appendChild(this.scrollContainer);
    // Sheet tabs sit below the scroll container in the flex
    // column. flex:0 so they take their intrinsic height,
    // never grow / shrink based on content.
    this.sheetTabs.style.flex = '0 0 auto';
    this.root.appendChild(this.sheetTabs);
    // Inside the scroll container so it scrolls with the sheet and is
    // clipped by it, the way the button it belongs to is.
    this.scrollContainer.appendChild(this.popupOverlay);
    this.renderTarget.appendChild(this.root);
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
    this.sheetTabs.addEventListener('sheet-tabs-scroll', this.handleSheetTabsScroll as EventListener);
    this.sheetTabs.addEventListener('sheet-tab-add', this.handleSheetTabAdd);
    this.sheetTabs.addEventListener('sheet-tab-rename-start', this.handleSheetRenameStart as EventListener);
    this.sheetTabs.addEventListener('sheet-tab-rename', this.handleSheetRename as EventListener);
    this.sheetTabs.addEventListener('sheet-tab-rename-cancel', this.handleSheetRenameCancel);
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
    this.contextMenu.addEventListener('context-menu-action', this.handleContextMenuAction as EventListener);
    document.addEventListener('pointerdown', this.handleDocumentPointerDown);
    document.addEventListener('keydown', this.handleContextMenuKeyDown);
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
    this.cellGrid.addEventListener('selection-paint-start', this.handleSelectionPaintStart);
    document.addEventListener('mouseup', this.handleDocumentMouseUp);
    document.addEventListener('keyup', this.handleDocumentKeyUp);
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
    this.cellGrid.addEventListener('cell-comment-commit', this.handleCellCommentCommit as EventListener);
    this.columnHeader.addEventListener(
      'column-header-contextmenu',
      this.handleColumnHeaderContextMenu as EventListener
    );
    // Grouping outline (slice 167). The column header emits
    // `column-grouping-toggle` when the user clicks a +/-
    // button in the grouping gutter; the connector forwards
    // it as `groupingCollapsed` to the server.
    this.columnHeader.addEventListener('column-grouping-toggle', this.handleColumnGroupingToggle as EventListener);
    this.rowHeader.addEventListener('row-grouping-toggle', this.handleRowGroupingToggle as EventListener);
    this.rowHeader.addEventListener('row-header-contextmenu', this.handleRowHeaderContextMenu as EventListener);
    // Header-drag resize (slice 164). The header elements
    // emit `column-resize-start` / `row-resize-start` from
    // their right-edge / bottom-edge mousedown handles; the
    // connector tracks the drag via document mousemove +
    // mouseup. Live-updates colWidths/rowHeights for the
    // current drag column/row.
    this.columnHeader.addEventListener('column-resize-start', this.handleColumnResizeStart as EventListener);
    this.rowHeader.addEventListener('row-resize-start', this.handleRowResizeStart as EventListener);
    this.columnHeader.addEventListener('column-autofit', this.handleColumnAutofit as EventListener);
    this.rowHeader.addEventListener('row-autofit', this.handleRowAutofit as EventListener);
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
    this.scrollContainer.addEventListener('pointerdown', this.handleHostedEditorPointerDown);
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
    const { col, row, shiftKey, ctrlKey, metaKey } = e.detail;
    const ctrlOrMeta = ctrlKey || metaKey;
    this.extendCol = col;
    this.extendRow = row;
    if (ctrlOrMeta && shiftKey) {
      // Extend the active range, keeping the ranges already picked
      // out with earlier Ctrl-clicks.
      const anchor = this.selection.activeCell;
      const extended = {
        activeCell: anchor,
        activeRange: this.rangeTo(anchor, col, row),
        auxRanges: []
      };
      this.selection = { ...extended, auxRanges: this.selection.auxRanges };
      this.recomputeViewport();
      this.dispatchCellsAddedToRangeSelection(extended.activeRange);
      return;
    }
    if (ctrlOrMeta) {
      // Non-coherent selection: the clicked cell joins the selection
      // as its own range and becomes active; the previous active
      // range is kept as an aux range.
      this.selection = addAuxRange(this.selection, col, row, col, row);
      this.recomputeViewport();
      this.dispatchCellAddedToSelection(col, row);
      return;
    }
    if (shiftKey) {
      // Extend the active range from the existing active cell
      // (= the anchor) to the clicked coordinate. Ranges picked out
      // with earlier Ctrl-clicks stay: shift extends the last range,
      // it does not start the selection over.
      const anchor = this.selection.activeCell;
      this.selection = {
        activeCell: anchor,
        activeRange: this.rangeTo(anchor, col, row),
        auxRanges: this.selection.auxRanges
      };
      this.recomputeViewport();
      this.reportRangeSelection();
      return;
    } else {
      this.selection = selectSingleCell(col, row);
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
  /**
   * Whether the user may change column widths.
   *
   * A protected sheet can still allow formatting; `lockFormatColumns` is
   * the workbook's own flag for that, and the server sends it precisely so
   * the client can refuse the drag rather than let it happen and be undone
   * by the next repaint.
   */
  private canResizeColumn(): boolean {
    return !this.sheetProtected || !this.lockFormatColumns;
  }

  /** Whether the user may change row heights. See {@link canResizeColumn}. */
  private canResizeRow(): boolean {
    return !this.sheetProtected || !this.lockFormatRows;
  }

  private readonly handleColumnResizeStart = (e: CustomEvent<{ col: number; clientX: number }>): void => {
    if (!this.canResizeColumn()) {
      return;
    }
    const { col, clientX } = e.detail;
    const idx = col - 1;
    const startWidth = idx < this.colWidths.length ? this.colWidths[idx] : this.defColWidth;
    this.columnResize = { col, startWidth, startClientX: clientX };
  };

  /**
   * Row resize drag — start. Mirror of column variant.
   */
  private readonly handleRowResizeStart = (e: CustomEvent<{ row: number; clientY: number }>): void => {
    if (!this.canResizeRow()) {
      return;
    }
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
  private readonly handleCellContextMenu = (
    e: CustomEvent<{ col: number; row: number; clientX?: number; clientY?: number }>
  ): void => {
    const { col, row } = e.detail;
    this.rememberContextMenuPosition(e.detail);
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
  private readonly handleColumnHeaderContextMenu = (
    e: CustomEvent<{ col: number; clientX?: number; clientY?: number }>
  ): void => {
    const { col } = e.detail;
    this.rememberContextMenuPosition(e.detail);
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
  private readonly handleRowHeaderContextMenu = (
    e: CustomEvent<{ row: number; clientX?: number; clientY?: number }>
  ): void => {
    const { row } = e.detail;
    this.rememberContextMenuPosition(e.detail);
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

  private readonly handleRowGroupingToggle = (e: CustomEvent<{ uniqueIndex: number; collapsed: boolean }>): void => {
    const { uniqueIndex, collapsed } = e.detail;
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'groupingCollapsed',
          data: { cols: false, colIndex: uniqueIndex, collapsed }
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
    const { col, row, shiftKey, ctrlKey, metaKey } = e.detail;
    this.dragging = true;
    this.extendCol = col;
    this.extendRow = row;
    if (ctrlKey || metaKey) {
      // Ctrl/Cmd-mousedown must leave the selection untouched. The
      // click that follows adds this cell as a further range, and it
      // needs the current active range to push aside — replacing the
      // selection here would wipe exactly what the Ctrl-click is meant
      // to keep. mousedown always precedes click for a real pointer.
      return;
    }
    if (shiftKey) {
      // Shift+mousedown: extend existing range to the
      // mousedown coord (treat the existing anchor as the
      // drag start). Standard Excel "shift-drag" behavior.
      // Aux ranges survive, as on a shift-click.
      const anchor = this.selection.activeCell;
      this.selection = {
        activeCell: anchor,
        activeRange: this.rangeTo(anchor, col, row),
        auxRanges: this.selection.auxRanges
      };
    } else {
      this.selection = selectSingleCell(col, row);
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
    if (this.painting !== null) {
      const { col, row } = e.detail;
      this.painting = {
        from: this.painting.from,
        outcome: computePaintRectangle(this.painting.from, col, row)
      };
      this.recomputeViewport();
      return;
    }
    if (!this.dragging) {
      return;
    }
    const { col, row } = e.detail;
    // Same as shift-arrow: keep activeCell as anchor, move
    // the extend corner.
    const anchor = this.selection.activeCell;
    this.selection = {
      activeCell: anchor,
      activeRange: this.rangeTo(anchor, col, row),
      auxRanges: []
    };
    this.extendCol = col;
    this.extendRow = row;
    this.showRangeSizeInAddressField();
    this.recomputeViewport();
  };

  /**
   * While a range is being dragged out, the address field counts it
   * rather than naming a cell — Excel's "3R x 2C". The count is what
   * the user needs mid-drag; the address comes back when the server
   * answers the finished selection.
   */
  private showRangeSizeInAddressField(): void {
    this.formulaBar.renderState = {
      ...this.formulaBar.renderState,
      address: selectionSizeLabel(this.selection.activeRange)
    };
  }

  /**
   * Mouse drag — mouseup end (slice 155). Closes the drag.
   * Dispatches a {@code cellRangeSelected} event when the
   * final range covers more than one cell (single-cell drag
   * is treated as a click, whose `cell-click` handler already
   * dispatched `cellSelected`).
   */
  private readonly handleDocumentMouseUp = (): void => {
    if (this.painting !== null) {
      this.finishPainting();
      return;
    }
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
      this.reportRangeSelection();
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
      this.reportProtectedCellWriteAttempt();
      return;
    }
    // Seeded with what the cell IS, not what it shows — the formula
    // behind a computed cell, and the unformatted value otherwise, which
    // is what carries a text cell's leading apostrophe. The GWT client
    // seeded the inline editor from the formula field for the same
    // reason.
    const initialValue = this.formulaFieldValueFor(col, row);
    // Opening the existing value to change it: arrows move the caret.
    this.editor = { col, row, initialValue, mode: 'edit' };
    this.recomputeViewport();
  };

  /**
   * Excel-style lock check: a cell is uneditable only when the
   * sheet is protected AND the cell's locked flag is true. POI
   * defaults every cell's locked flag to true, so a freshly
   * loaded workbook with sheet protection enabled blocks all
   * cells; unlocking a region in POI flips the per-cell flag.
   * When the sheet is unprotected, the flag is meaningless.
   *
   * A cell the server has sent no data for — an empty cell — has no flag of
   * its own, so its row and column styles decide, and it is locked only
   * when **both** are. Defaulting such a cell to locked instead makes every
   * empty cell uneditable on a sheet that protects only some rows or
   * columns, which is most protected sheets.
   */
  private isCellLockedForEdit(col: number, row: number): boolean {
    if (!this.sheetProtected) {
      return false;
    }
    const cellData = this.cellDataByKey.get(toKey(col, row));
    if (cellData !== undefined) {
      return cellData.locked;
    }
    return this.lockedColumnIndexes.includes(col) && this.lockedRowIndexes.includes(row);
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
      // Not carried over: how far the old text ran into its neighbours says
      // nothing about how far the newly typed text should. The server sends
      // the new width with its answer.
      overflowWidth: 0,
      ...(previous?.formulaValue !== undefined ? { formulaValue: previous.formulaValue } : {}),
      ...(previous?.originalValue !== undefined ? { originalValue: previous.originalValue } : {}),
      ...(previous?.textColor !== undefined ? { textColor: previous.textColor } : {})
    });
    // Close the editor first so the re-render removes the
    // input — important to do BEFORE dispatching the server
    // event so the editor doesn't briefly flash on top of the
    // server's response.
    this.editor = null;
    this.returnFocusToSheet();
    // Move the active cell to the next slot per Excel
    // convention (slice 158). For 'Enter' / 'Tab' the move
    // direction is fixed; blur leaves the selection at the
    // committed cell. Clamped to grid bounds.
    // Enter moves down, Tab right, shift reverses each; an arrow key
    // moves the way it points, which is what committing from Excel's
    // "enter" editing mode does.
    //
    // With a range selected the commit walks inside it, the same way
    // Enter and Tab do when nothing is being edited: filling a block
    // cell by cell is the reason the range is selected at all.
    const walkDirection = inSelectionWalkDirection(key, shiftKey === true);
    const walks =
      walkDirection !== null && this.rows > 0 && this.cols > 0 && isMultiCellRange(this.selection.activeRange);
    const step = walks ? null : commitStep(key, shiftKey === true);
    if (step !== null && this.rows > 0 && this.cols > 0) {
      const nextCol = Math.min(this.cols, Math.max(1, col + step.dCol));
      const nextRow = Math.min(this.rows, Math.max(1, row + step.dRow));
      this.selection = selectSingleCell(nextCol, nextRow);
      this.extendCol = nextCol;
      this.extendRow = nextRow;
    }
    if (!walks) {
      this.recomputeViewport();
    }
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
    if (step !== null) {
      this.dispatchSelectionEvent();
    }
    if (walks) {
      this.moveActiveCellInsideSelection(
        walkInsideSelection({
          range: this.selection.activeRange,
          col,
          row,
          direction: walkDirection!,
          hiddenColumns: new Set(this.hiddenColumnIndexes),
          hiddenRows: new Set(this.hiddenRowIndexes)
        })
      );
    }
  };

  /**
   * Cell-editor cancel (slice 152). User pressed Escape. Close
   * the editor without dispatching anything.
   */
  private readonly handleCellEditorCancel = (): void => {
    this.editor = null;
    this.returnFocusToSheet();
    this.recomputeViewport();
  };

  /**
   * Put the keyboard back on the sheet.
   *
   * Closing the editor takes its input out of the document and the focus
   * lands on the body, where the sheet's own key handling — arrows,
   * Ctrl+Z, Ctrl+C — cannot see it. Excel leaves the keyboard on the grid
   * after every edit, and so must we, or the next shortcut goes nowhere.
   *
   * Not done while a hosted component holds the focus: a custom editor is
   * a component the application owns and taking its focus away mid-edit
   * would be wrong.
   */
  private returnFocusToSheet(): void {
    const active = this.host.shadowRoot?.activeElement ?? document.activeElement;
    if (active !== null && this.cellEditors.owns(active)) {
      return;
    }
    this.scrollContainer.focus();
  }

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
    const { col, shiftKey, ctrlKey, metaKey } = e.detail;
    const rows = this.rows > 0 ? this.rows : 1;
    this.extendCol = col;
    this.extendRow = rows;
    if ((ctrlKey || metaKey) && !shiftKey) {
      // The whole column joins the selection as a further range.
      this.selection = addAuxRange(this.selection, col, 1, col, rows);
      this.recomputeViewport();
      const data: ColumnSelectedData = { column: col, firstRowIndex: 1 };
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: { type: 'columnAddedToSelection', data },
          bubbles: true,
          composed: true
        })
      );
      return;
    }
    if (shiftKey) {
      // Extend across columns, keeping ranges added with Ctrl.
      const anchor = this.selection.activeCell;
      this.selection = {
        activeCell: anchor,
        activeRange: normalizedRange(anchor.col, 1, col, rows),
        auxRanges: this.selection.auxRanges
      };
    } else {
      this.selection = {
        activeCell: { col, row: 1 },
        activeRange: normalizedRange(col, 1, col, rows),
        auxRanges: []
      };
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
    const { row, shiftKey, ctrlKey, metaKey } = e.detail;
    const cols = this.cols > 0 ? this.cols : 1;
    this.extendCol = cols;
    this.extendRow = row;
    if ((ctrlKey || metaKey) && !shiftKey) {
      // The whole row joins the selection as a further range.
      this.selection = addAuxRange(this.selection, 1, row, cols, row);
      this.recomputeViewport();
      const data: RowSelectedData = { row, firstColumnIndex: 1 };
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: { type: 'rowAddedToRangeSelection', data },
          bubbles: true,
          composed: true
        })
      );
      return;
    }
    if (shiftKey) {
      // Extend across rows, keeping ranges added with Ctrl.
      const anchor = this.selection.activeCell;
      this.selection = {
        activeCell: anchor,
        activeRange: normalizedRange(1, anchor.row, cols, row),
        auxRanges: this.selection.auxRanges
      };
    } else {
      this.selection = {
        activeCell: { col: 1, row },
        activeRange: normalizedRange(1, row, cols, row),
        auxRanges: []
      };
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
    if (this.eventCameFromHostedComponent(e)) {
      // The key belongs to the component the user is typing into. Acting
      // on it as a sheet shortcut moves the selection out from under a
      // custom editor mid-edit, which takes the editor off the sheet.
      //
      // Two keys are the way back out of a custom editor, so they are the
      // sheet's after all: Escape returns the focus, and Tab returns it
      // and then moves on to the next cell.
      const fromEditor = this.eventCameFromCellEditor(e);
      if (!fromEditor || (e.key !== 'Escape' && e.key !== 'Tab')) {
        return;
      }
      this.scrollContainer.focus();
      if (e.key === 'Escape') {
        return;
      }
      e.preventDefault();
    }
    const key = e.key;
    const ctrlOrMeta = e.ctrlKey || e.metaKey;

    // A cell showing a custom editor is edited through that editor, so
    // every key that would otherwise start editing hands it the focus
    // instead: F2, Enter, and any printable character.
    //
    // Deliberately without preventDefault. The character that opened the
    // editor should land in it, and the browser delivers a keypress to
    // whatever holds the focus once the handlers have run — which by then
    // is the editor.
    const startsEditing =
      !this.editor && !ctrlOrMeta && !e.altKey && (key === 'F2' || key === 'Enter' || key.length === 1);
    if (startsEditing && this.focusCustomEditor()) {
      return;
    }

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

    // F2 opens the cell editor at the active cell, loaded with the
    // cell's existing value (Excel convention). Slice 153.
    //
    // So does Enter on a single cell, as it always has: the GWT client
    // treated the two keys as one case. With a range selected Enter
    // walks inside it instead, and on a cell the user cannot see there
    // is nothing to edit, so it falls through to moving on.
    const entersEditor =
      key === 'F2' ||
      (key === 'Enter' &&
        !isMultiCellRange(this.selection.activeRange) &&
        !this.hiddenRowIndexes.includes(this.selection.activeCell.row) &&
        !this.hiddenColumnIndexes.includes(this.selection.activeCell.col));
    if (entersEditor && !this.editor) {
      if (this.rows <= 0 || this.cols <= 0) {
        return;
      }
      const { col, row } = this.selection.activeCell;
      if (this.isCellLockedForEdit(col, row)) {
        this.reportProtectedCellWriteAttempt();
        return;
      }
      e.preventDefault();
      this.editor = {
        col,
        row,
        initialValue: this.formulaFieldValueFor(col, row),
        // F2 opens the existing value, so arrows move the caret.
        mode: 'edit'
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
        this.reportProtectedCellWriteAttempt();
        return;
      }
      e.preventDefault();
      // Typing over the cell: an arrow commits and moves on.
      // A digit typed into a percentage cell means that many percent, so
      // the editor opens on "5%" rather than "5" - see PercentageEntry.
      this.editor = {
        col,
        row,
        initialValue: initialEditValue(key, this.cellDataByKey.get(toKey(col, row))),
        mode: 'enter'
      };
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
    // With a range selected, Enter and Tab walk the active cell around
    // inside it instead of moving on and dropping it: Enter down the
    // column and on to the next, Tab along the row and on to the next,
    // both wrapping at the end, shift reversing. The range stays, so the
    // server hears a selection that did not discard its old range.
    const walkDirection = inSelectionWalkDirection(key, e.shiftKey);
    if (walkDirection !== null && this.rows > 0 && this.cols > 0 && isMultiCellRange(this.selection.activeRange)) {
      e.preventDefault();
      const next = walkInsideSelection({
        range: this.selection.activeRange,
        col: this.selection.activeCell.col,
        row: this.selection.activeCell.row,
        direction: walkDirection,
        hiddenColumns: new Set(this.hiddenColumnIndexes),
        hiddenRows: new Set(this.hiddenRowIndexes)
      });
      this.moveActiveCellInsideSelection(next);
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
        activeRange: this.rangeTo(anchor, target.col, target.row),
        auxRanges: []
      };
      this.extendingSelection = true;
      this.showRangeSizeInAddressField();
    } else {
      this.extendingSelection = false;
      this.selection = selectSingleCell(target.col, target.row);
      this.extendCol = target.col;
      this.extendRow = target.row;
      // Name the cell straight away rather than waiting for the server
      // to say so: the address field is how the user knows where the
      // keyboard has taken them.
      this.formulaBar.renderState = {
        ...this.formulaBar.renderState,
        address: cellCoordToAddress({ col: target.col, row: target.row })
      };
    }
    // Auto-scroll the new active cell (or extend cell) into
    // view BEFORE the render (slice 156). Excel scrolls the
    // viewport whenever keyboard nav moves the cursor off-
    // screen so the user can keep typing without losing
    // visual context. Sets scrollLeft/scrollTop directly; the
    // scroll listener triggers an additional recomputeViewport
    // automatically.
    this.scrollCellIntoView(target);
    this.recomputeViewport();
    this.dispatchSelectionEvent();
  };

  /**
   * Put the active cell somewhere else inside the range that is already
   * selected, keeping the range itself.
   *
   * The server is told with the plain `cellSelected` event, but with
   * `oldSelectionRangeDiscarded` false - that flag is the whole
   * difference between "the user picked this cell" and "the user is
   * walking through the block they picked earlier".
   */
  private moveActiveCellInsideSelection(next: { col: number; row: number }): void {
    this.selection = {
      activeCell: next,
      activeRange: this.selection.activeRange,
      auxRanges: this.selection.auxRanges
    };
    this.extendCol = next.col;
    this.extendRow = next.row;
    this.scrollCellIntoView(next);
    this.recomputeViewport();
    const data: CellSelectedData = {
      row: next.row,
      column: next.col,
      oldSelectionRangeDiscarded: false
    };
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'cellSelected', data },
        bubbles: true,
        composed: true
      })
    );
  }

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
  private scrollCellIntoView(target: { col: number; row: number }): void {
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
    // Excel drops an open filter popup as soon as the sheet moves under
    // it; keeping it would leave it pointing at the wrong cell.
    this.dismissPopup();
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
  /**
   * Complete the startup handshake, once the viewport is known.
   *
   * The server only loads overlays, hyperlinks, comments, tables, popup
   * buttons and custom editors from `loadCells`, and `loadCells` runs off
   * `onSheetScroll`. Nothing else calls it. Until the client reports a
   * viewport, none of that content exists — which looks like a sheet that
   * renders its cells and nothing else.
   *
   * `onConnectorInit` first, so the server marks the next scroll as a full
   * reload rather than diffing against viewport bounds it has never seen.
   *
   * The handshake runs once: {@link setReload} clears the latch whenever
   * the server discards its sheet state, and the next completed viewport
   * introduces itself again.
   *
   * The announcement itself repeats, though, every time the visible range
   * actually changes. Only scrolling dispatches a viewport of its own, and
   * plenty of things resize the sheet without one — a tab revealing it, a
   * split pane opening, the window resizing. A sheet that came up 0 wide
   * inside a hidden tab would otherwise keep the viewport it measured
   * while hidden, and the cells and popup buttons outside it would never
   * load.
   */
  private announceViewport(): void {
    if (this.rows <= 0 || this.cols <= 0) {
      // The sheet's size has not arrived yet. Announcing now would report a
      // viewport that means nothing and the server would load nothing.
      return;
    }
    const viewport = this.cellGrid.renderState.viewport;
    if (viewport.lastRowIndex < viewport.firstRowIndex || viewport.lastColumnIndex < viewport.firstColumnIndex) {
      // Nothing measured yet; try again after the next render.
      return;
    }
    const bounds = [
      viewport.firstRowIndex,
      viewport.firstColumnIndex,
      viewport.lastRowIndex,
      viewport.lastColumnIndex
    ].join(',');
    if (this.viewportAnnounced && bounds === this.announcedBounds) {
      return;
    }
    if (!this.viewportAnnounced) {
      this.viewportAnnounced = true;
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: { type: 'onConnectorInit', data: {} },
          bubbles: true,
          composed: true
        })
      );
    }
    this.announcedBounds = bounds;
    this.dispatchSheetScrollEvent();
  }

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
    if (this.renderQueued) {
      return;
    }
    this.renderQueued = true;
    queueMicrotask(() => {
      if (!this.renderQueued) {
        return;
      }
      this.renderQueued = false;
      this.recomputeViewport();
    });
  }

  /**
   * Repaint now, dropping any frame already scheduled.
   *
   * The counterpart to {@link scheduleRender}: coalescing is right for
   * scrolling, where the next frame is the soonest anything can be seen
   * anyway, and wrong at the end of a server round trip, where the caller
   * has been told the work is done.
   */
  private flushRender(): void {
    this.renderQueued = false;
    this.recomputeViewport();
  }

  /**
   * Tell the server how wide one character is in each cell style.
   *
   * A cell under the General format shows as many digits as fit and falls
   * back to scientific notation when they do not, and the server decides
   * that: it knows the column width in pixels but not the font, so it needs
   * a pixels-per-character ratio per style index. Without one it assumes
   * everything fits and no number is ever shortened.
   *
   * Measured the way the GWT client measured it — ten digits in a probe
   * carrying the style's class, divided by ten — so the server's existing
   * arithmetic keeps its meaning.
   */
  private reportCellStyleWidthRatios(): void {
    const styleIndexes = Object.keys(this.cellStyleToCSSStyle)
      .map(Number)
      .filter((index) => Number.isFinite(index));
    const root = this.cellGrid.shadowRoot;
    if (styleIndexes.length === 0 || root === null) {
      return;
    }

    const probe = document.createElement('span');
    // Out of flow and off-screen so measuring never disturbs the sheet.
    // `width:auto` overrides nothing in `.cell` but guards a theme that
    // sets one.
    probe.style.cssText = 'position:absolute;left:-10000px;top:0;visibility:hidden;width:auto;white-space:nowrap;';
    probe.textContent = WIDTH_PROBE_TEXT;
    root.appendChild(probe);
    const ratios = styleIndexes.map((index) => {
      probe.className = `cell cs${index}`;
      return probe.clientWidth / WIDTH_PROBE_TEXT.length;
    });
    probe.remove();

    if (ratios.every((ratio) => ratio === 0)) {
      // Nothing was laid out — the sheet is hidden, or the styles have not
      // been applied yet. A zero ratio would tell the server every value
      // fits in no space at all, so say nothing and measure again later.
      this.styleWidthRatiosStale = true;
      return;
    }

    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'setCellStyleWidthRatios',
          data: { styleIndexes, ratios }
        },
        bubbles: true,
        composed: true
      })
    );
  }

  recomputeViewport(): void {
    this.applyRenderState({
      scrollLeft: this.scrollContainer.scrollLeft,
      scrollTop: this.scrollContainer.scrollTop,
      viewportWidth: this.scrollContainer.clientWidth,
      viewportHeight: this.scrollContainer.clientHeight
    });
    this.announceViewport();
    if (this.styleWidthRatiosStale) {
      this.styleWidthRatiosStale = false;
      // Measured once the grid has painted the rules that were just
      // handed to it, not against the ones it is replacing.
      void this.cellGrid.updateComplete.then(() => this.reportCellStyleWidthRatios());
    }
  }

  /**
   * Release the scroll + resize listeners. Call this when the host
   * element is being detached + destroyed so the
   * {@link resizeObserver} doesn't keep a reference to the
   * scroll container after teardown.
   */
  dispose(): void {
    this.renderQueued = false;
    this.scrollContainer.removeEventListener('scroll', this.handleScroll);
    this.scrollContainer.removeEventListener('keydown', this.handleKeyDown as EventListener);
    this.scrollContainer.removeEventListener('pointerdown', this.handleHostedEditorPointerDown);
    this.scrollContainer.removeEventListener('copy', this.handleCopy as EventListener);
    this.scrollContainer.removeEventListener('cut', this.handleCut as EventListener);
    this.scrollContainer.removeEventListener('paste', this.handlePaste as EventListener);
    this.cellGrid.removeEventListener('cell-click', this.handleCellClick as EventListener);
    this.overlayComponents.clear();
    this.cellComponents.clear();
    this.cellEditors.clear();
    this.popupContents.clear();
    this.actionIcons.clear();
    this.contextMenu.removeEventListener('context-menu-action', this.handleContextMenuAction as EventListener);
    document.removeEventListener('pointerdown', this.handleDocumentPointerDown);
    document.removeEventListener('keydown', this.handleContextMenuKeyDown);
    this.cellGrid.removeEventListener('cell-doubleclick', this.handleCellDoubleClick as EventListener);
    this.cellGrid.removeEventListener('cell-editor-commit', this.handleCellEditorCommit as EventListener);
    this.cellGrid.removeEventListener('cell-editor-cancel', this.handleCellEditorCancel as EventListener);
    this.columnHeader.removeEventListener('column-header-click', this.handleColumnHeaderClick as EventListener);
    this.rowHeader.removeEventListener('row-header-click', this.handleRowHeaderClick as EventListener);
    this.sheetTabs.removeEventListener('sheet-tab-click', this.handleSheetTabClick as EventListener);
    this.sheetTabs.removeEventListener('sheet-tabs-scroll', this.handleSheetTabsScroll as EventListener);
    this.sheetTabs.removeEventListener('sheet-tab-add', this.handleSheetTabAdd);
    this.sheetTabs.removeEventListener('sheet-tab-rename-start', this.handleSheetRenameStart as EventListener);
    this.sheetTabs.removeEventListener('sheet-tab-rename', this.handleSheetRename as EventListener);
    this.sheetTabs.removeEventListener('sheet-tab-rename-cancel', this.handleSheetRenameCancel);
    this.cellGrid.removeEventListener('cell-mousedown', this.handleCellMouseDown as EventListener);
    this.cellGrid.removeEventListener('cell-mouseover', this.handleCellMouseOver as EventListener);
    this.cellGrid.removeEventListener('selection-paint-start', this.handleSelectionPaintStart);
    this.cellGrid.removeEventListener('popup-button-click', this.handlePopupButtonClick as EventListener);
    this.cellGrid.removeEventListener('cell-contextmenu', this.handleCellContextMenu as EventListener);
    this.cellGrid.removeEventListener('cell-comment-commit', this.handleCellCommentCommit as EventListener);
    this.columnHeader.removeEventListener(
      'column-header-contextmenu',
      this.handleColumnHeaderContextMenu as EventListener
    );
    this.columnHeader.removeEventListener('column-grouping-toggle', this.handleColumnGroupingToggle as EventListener);
    this.rowHeader.removeEventListener('row-grouping-toggle', this.handleRowGroupingToggle as EventListener);
    this.rowHeader.removeEventListener('row-header-contextmenu', this.handleRowHeaderContextMenu as EventListener);
    document.removeEventListener('mouseup', this.handleDocumentMouseUp);
    document.removeEventListener('keyup', this.handleDocumentKeyUp);
    document.removeEventListener('mousemove', this.handleDocumentMouseMove);
    this.columnHeader.removeEventListener('column-resize-start', this.handleColumnResizeStart as EventListener);
    this.rowHeader.removeEventListener('row-resize-start', this.handleRowResizeStart as EventListener);
    this.columnHeader.removeEventListener('column-autofit', this.handleColumnAutofit as EventListener);
    this.rowHeader.removeEventListener('row-autofit', this.handleRowAutofit as EventListener);
    this.resizeObserver.disconnect();
    if (this.hostClassObserver) {
      this.hostClassObserver.disconnect();
      this.hostClassObserver = null;
    }
    this.formulaBar.removeEventListener('sheet-address-changed', this.handleSheetAddressChanged as EventListener);
    this.formulaBar.removeEventListener('formula-commit', this.handleFormulaCommit as EventListener);
    this.formulaBar.removeEventListener('formula-cancel', this.handleFormulaCancel);
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
    // Typing an address is a way of moving around the sheet, so the
    // keyboard goes back to the sheet once it has moved. Leaving the
    // focus in the address field means the arrow keys that naturally
    // follow move the caret through the text just typed.
    this.scrollContainer.focus();
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

  /** Test-only accessor: the component's own root inside the host. */
  getRoot(): HTMLElement {
    return this.root;
  }

  /** Test-only accessor: the scroll container element. */
  getScrollContainer(): HTMLElement {
    return this.scrollContainer;
  }

  /** Test-only accessor: the overlay an open popup button's content is in. */
  getPopupOverlay(): HTMLElement {
    return this.popupOverlay;
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
  /**
   * Tell the server a cell joined a non-coherent selection. Distinct
   * from {@link dispatchSelectionEvent}, which reports a selection that
   * replaces whatever came before.
   */
  private dispatchCellAddedToSelection(col: number, row: number): void {
    const data: CellAddedToSelectionData = {
      row,
      column: col
    };
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'cellAddedToSelectionAndSelected', data },
        bubbles: true,
        composed: true
      })
    );
  }

  /** Tell the server a whole range joined a non-coherent selection. */
  /**
   * Tell the server about the active range, as a whole selection or as
   * one more piece of a selection that is already in pieces.
   *
   * An ordinary selection event says the old range is gone, and the
   * server drops the cells earlier Ctrl-clicks put there along with it.
   * Anything then acting on the selection - a context-menu action, a
   * delete - misses them. The GWT client drew no selection outline once
   * a Ctrl-click had broken the selection up, and used that as the same
   * signal.
   */
  private reportRangeSelection(): void {
    if (this.selection.auxRanges.length > 0) {
      this.dispatchCellsAddedToRangeSelection(this.selection.activeRange);
      return;
    }
    this.dispatchSelectionEvent();
  }

  private dispatchCellsAddedToRangeSelection(range: CellRange): void {
    const data: CellRangeSelectedData = {
      row1: range.r1,
      col1: range.c1,
      row2: range.r2,
      col2: range.c2
    };
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'cellsAddedToRangeSelection', data },
        bubbles: true,
        composed: true
      })
    );
  }

  /**
   * Double-click on a column's resize handle — size it to its widest
   * value. Only the server can measure that: it holds every value in the
   * column, including the ones outside the viewport.
   */
  private readonly handleColumnAutofit = (e: CustomEvent<{ col: number }>): void => {
    if (!this.canResizeColumn()) {
      return;
    }
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'onColumnAutofit', data: { columnIndex: e.detail.col } },
        bubbles: true,
        composed: true
      })
    );
  };

  /** Double-click on a row's resize handle — size it to its tallest value. */
  private readonly handleRowAutofit = (e: CustomEvent<{ row: number }>): void => {
    if (!this.canResizeRow()) {
      return;
    }
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'onRowAutofit', data: { rowIndex: e.detail.row } },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Tell the server the user tried to change a cell they may not change.
   *
   * The client refuses the edit on its own; the server is told so it can
   * say why — the default handler shows the workbook's "this cell is
   * protected" notification. Silently swallowing the keystroke leaves the
   * user with no idea why nothing happened.
   */
  private reportProtectedCellWriteAttempt(): void {
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'protectedCellWriteAttempted', data: {} },
        bubbles: true,
        composed: true
      })
    );
  }

  /**
   * Mousedown on the fill handle. The drag that follows paints rather than
   * selects, so the current range is remembered as the one being grown or
   * shrunk.
   */
  private readonly handleSelectionPaintStart = (): void => {
    const { c1, c2, r1, r2 } = this.selection.activeRange;
    this.painting = { from: { c1, c2, r1, r2 }, outcome: { kind: 'none' } };
  };

  /**
   * Release of a fill-handle drag: tell the server what to copy or clear.
   *
   * The two directions are different operations, and the server takes them
   * through different events — growing reports the whole new rectangle,
   * shrinking reports only the corner the selection retreats to.
   */
  private finishPainting(): void {
    const painting = this.painting;
    this.painting = null;
    this.recomputeViewport();
    if (painting === null || painting.outcome.kind === 'none') {
      return;
    }

    if (painting.outcome.kind === 'decrease') {
      const { r1, c1 } = painting.outcome.painted;
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: { type: 'selectionDecreasePainted', data: { row: r1, col: c1 } },
          bubbles: true,
          composed: true
        })
      );
      return;
    }

    const grown = grownSelection(painting.from, painting.outcome.painted);
    if (!isNonEmpty(grown)) {
      return;
    }
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'selectionIncreasePainted',
          data: { r1: grown.r1, c1: grown.c1, r2: grown.r2, c2: grown.c2 }
        },
        bubbles: true,
        composed: true
      })
    );
  }

  private dispatchSelectionEvent(): void {
    const event = buildSelectionEvent(this.selection.activeCell, this.selection.activeRange, this.rows, this.cols);
    if (event.type === 'cellRangeSelected') {
      // `onCellRangeSelected` takes only the four corners and reads the
      // anchor from the selection the server already holds — unlike its
      // sibling `onCellRangePainted`, which is given a starting point. The
      // GWT client established that anchor on mousedown, before the drag
      // produced a range. Without it the server dereferences a null
      // selectedCellReference.
      const anchor = this.selection.activeCell;
      const anchorData: CellSelectedData = {
        row: anchor.row,
        column: anchor.col,
        oldSelectionRangeDiscarded: true
      };
      this.host.dispatchEvent(
        new CustomEvent('spreadsheet-event', {
          detail: { type: 'cellSelected', data: anchorData },
          bubbles: true,
          composed: true
        })
      );
    }
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: event,
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
    // The slots that project these live in the template we just handed the
    // cell-grid, so the components have to be parented before the browser
    // resolves them. sync() is idempotent, so the repaint on every scroll
    // tick costs a set comparison and nothing else.
    this.overlayComponents.sync(state.overlays.filter((o) => o.type === 'COMPONENT').map((o) => o.id));
    // Only the cells on screen render a slot, so only those elements are
    // placed — one off-screen would be parented with nowhere to project.
    const visible = visibleCellSlots(this.cellSlotAssignments(), state);
    this.cellComponents.sync(visible.filter((a) => a.kind === 'component').map((a) => a.nodeId));
    this.cellEditors.sync(visible.filter((a) => a.kind === 'editor').map((a) => a.nodeId));
    // Column-header strip (slice 143). Sized + state-pushed only
    // when there is something to draw, so the default opt-out path
    // stays a no-op — important to avoid feedback loops with the
    // ResizeObserver under wtr parallel.
    //
    // A workbook can hide its headers and still group its columns; the
    // outline gutter lives in this strip, so it has to be drawn even
    // when the letters above it are not.
    if (this.headerRowHeight > 0 || this.colGroupingMax > 0) {
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
        // Frozen columns stay on screen whatever the scroll position,
        // so their headers travel with them.
        horizontalSplitPosition: state.horizontalSplitPosition,
        frozenColX: state.frozenColX,
        frozenColWidths: state.frozenColWidths,
        scrollLeft: state.scrollLeft,
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
    // Row-header strip (slice 144). Same opt-in gate as the column
    // header, and the same reason for the grouping half of it.
    if (this.headerColumnWidth > 0 || this.rowGroupingMax > 0) {
      // Row groupings mirror the column ones; heights come from the same
      // point-to-pixel conversion the grid uses.
      const ppi = FALLBACK_PPI;
      const defaultRowHeightPx = getDefaultRowHeightPx(this.defRowHeight, ppi);
      const rowHeightsPx: number[] = [];
      for (let r = 0; r < this.rowHeights.length; r++) {
        rowHeightsPx.push(convertPointsToPixel(this.rowHeights[r], ppi));
      }
      const rowGroupings = computePositionedColGroupings(
        this.rowGroupingData,
        rowHeightsPx,
        this.rowGroupingInversed,
        defaultRowHeightPx
      );
      const rowGutterPx = this.rowGroupingMax * 16;
      this.rowHeader.style.width = `${this.headerColumnWidth + rowGutterPx}px`;
      this.rowHeader.style.height = `${state.totalGridHeightPx}px`;
      this.rowHeader.renderState = {
        viewport: state.viewport,
        rowY: state.rowY,
        rowHeights: state.rowHeights,
        headerWidth: this.headerColumnWidth + rowGutterPx,
        totalGridHeightPx: state.totalGridHeightPx,
        // Slice 173: feed the selected-rows set so the
        // header can emit `part="row-header
        // header-selected"` on rows inside the active
        // range.
        selectedRows: selectedRowSet(this.selection),
        rowGroupings,
        rowGroupingMax: this.rowGroupingMax,
        rowGroupingInversed: this.rowGroupingInversed,
        // Frozen rows stay on screen whatever the scroll position, so
        // their headers travel with them.
        verticalSplitPosition: state.verticalSplitPosition,
        frozenRowY: state.frozenRowY,
        frozenRowHeights: state.frozenRowHeights,
        scrollTop: state.scrollTop
      };
    }
    // Header corner (slice 145). Sized only when BOTH header
    // dimensions are > 0 so it occupies its grid cell with
    // matching width + height. When either is 0, the corner
    // collapses to a 0×0 box and isn't visible.
    //
    // The outline gutters are part of the strips beside it, so the corner
    // covers them too - otherwise a grouped sheet has an empty notch where
    // the two gutters meet, and the level buttons have nowhere to sit.
    const rowGutterPx = this.rowGroupingMax * GROUPING_LEVEL_HEIGHT_PX;
    const colGutterPx = this.colGroupingMax * GROUPING_LEVEL_HEIGHT_PX;
    const cornerWidth = this.headerColumnWidth > 0 ? this.headerColumnWidth + rowGutterPx : rowGutterPx;
    const cornerHeight = this.headerRowHeight > 0 ? this.headerRowHeight + colGutterPx : colGutterPx;
    this.headerCorner.style.width = `${cornerWidth}px`;
    this.headerCorner.style.height = `${cornerHeight}px`;
    this.headerCorner.renderState = {
      headerWidth: this.headerColumnWidth,
      headerHeight: this.headerRowHeight,
      colGroupingMax: this.colGroupingMax,
      rowGroupingMax: this.rowGroupingMax
    };
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

    // Build per-visible-index arrays for colX, rowY, widths, heights.
    //
    // These are the sheet's own coordinates: the distance from its left
    // edge, frozen columns included. The viewport reports how far into
    // the scrolling region the first visible column is, which is
    // measured from the split, so the frozen block's width goes back on
    // top. Without it the scrolling columns were drawn from x 0,
    // underneath the frozen block that is pinned there - the first
    // columns after a freeze could not be seen at all, and a click on
    // one landed on a frozen cell.
    const colX: number[] = [];
    const colWidths: number[] = [];
    let cumulativeX = frozenCumX + viewport.firstColumnPosition;
    for (let col = viewport.firstColumnIndex; col <= viewport.lastColumnIndex; col++) {
      const w = colWidthForIndex(col);
      colX.push(cumulativeX);
      colWidths.push(w);
      cumulativeX += w;
    }

    const rowY: number[] = [];
    const rowHeights: number[] = [];
    let cumulativeY = frozenCumY + viewport.firstRowPosition;
    for (let row = viewport.firstRowIndex; row <= viewport.lastRowIndex; row++) {
      const h = rowHeightForIndex(row);
      rowY.push(cumulativeY);
      rowHeights.push(h);
      cumulativeY += h;
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
      this.resolveResources(),
      colWidthForIndex,
      rowHeightForIndex,
      ppi
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
      rowIndexToStyleIndex: this.rowIndexToStyleIndex,
      columnIndexToStyleIndex: this.columnIndexToStyleIndex,
      cellSlots: Object.fromEntries(
        Object.entries(this.cellSlotAssignments()).map(([cellKey, a]) => [cellKey, a.slot])
      ),
      conditionalFormattingStyles: this.conditionalFormattingStyles,
      shiftedCellBorderStyles: this.shiftedCellBorderStyles,
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
      paintRect: this.painting?.outcome.kind === 'none' ? null : this.painting?.outcome.painted ?? null,
      commentText: this.cellComments,
      commentAuthors: this.cellCommentAuthors,
      visibleCommentKeys: this.visibleCellComments,
      editingCommentKey: this.editingCommentKey,
      invalidFormulaMessage: this.invalidFormulaErrorMessage,
      // Comment-bearing + hyperlink cell keys (slice 165).
      // The maps from setCellComments / setHyperlinksTooltips
      // are keyed by the cell-key format already, so a Set
      // of their keys is the right shape for the render.
      commentCells: new Set(Object.keys(this.cellComments)),
      invalidFormulaCells: this.invalidFormulaCells,
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

  /** Test-only accessor: resource id to currently resolved URL. */
  getResources(): Readonly<Record<string, string>> {
    return this.resolveResources();
  }

  /** Test-only accessor: the context menu shown on right-click. */
  getContextMenu(): SpreadsheetContextMenu {
    return this.contextMenu;
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
      activeIndex: this.sheetIndex,
      firstVisibleTab: this.firstVisibleTab,
      readOnly: this.workbookProtected,
      renamingIndex: this.renamingSheetIndex
    };
  }

  /**
   * Sheet-tab click handler — translates to a `sheetSelected`
   * spreadsheet-event for the server. The server is expected
   * to call back into {@code setSheetIndex} + push fresh data
   * via the cell value APIs, which the next render picks up.
   * Slice 154.
   */
  /**
   * A scroll button moved the strip.
   *
   * Nothing is reported: the GWT client told its own handler about the
   * new index, but the Flow server has no counterpart for it, so which
   * tab is leftmost is the client's business alone. Scrolling the strip
   * does not change the sheet.
   */
  private readonly handleSheetTabsScroll = (
    e: CustomEvent<{ direction: 'start' | 'backward' | 'forward' | 'end' }>
  ): void => {
    const last = Math.max(0, this.sheetNames.length - 1);
    const moved = {
      start: 0,
      backward: this.firstVisibleTab - 1,
      forward: this.firstVisibleTab + 1,
      end: last
    }[e.detail.direction];
    const next = Math.min(last, Math.max(0, moved));
    if (next === this.firstVisibleTab) {
      return;
    }
    this.firstVisibleTab = next;
    this.syncSheetTabs();
  };

  /**
   * Shift let go: the selection is made, so the address field goes back
   * to naming the cell the user started from.
   */
  private readonly handleDocumentKeyUp = (e: KeyboardEvent): void => {
    if (e.key !== 'Shift' || !this.extendingSelection) {
      return;
    }
    this.extendingSelection = false;
    this.formulaBar.renderState = {
      ...this.formulaBar.renderState,
      address: cellCoordToAddress(this.selection.activeCell)
    };
  };

  /**
   * A value typed into the formula field.
   *
   * It belongs to the cell the sheet has selected, so it is reported as
   * an ordinary edit of that cell. Enter then moves on to the next cell
   * down and Tab to the next one right, the way finishing an edit in
   * the cell itself does; shift+Tab commits without moving, which is
   * what the GWT client did. Clicking away commits too and leaves the
   * selection alone - that is how a value typed here reaches the sheet
   * when the user goes straight back to the cell.
   */
  private readonly handleFormulaCommit = (e: CustomEvent<{ value: string; key?: string; shiftKey: boolean }>): void => {
    const { value, key, shiftKey } = e.detail;
    const { col, row } = this.selection.activeCell;
    if (this.rows <= 0 || this.cols <= 0) {
      return;
    }
    if (this.isCellLockedForEdit(col, row)) {
      this.reportProtectedCellWriteAttempt();
      return;
    }
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'cellValueEdited', data: { row, col, value } },
        bubbles: true,
        composed: true
      })
    );
    // shift+Tab stays put: the GWT client passed the move along only
    // when shift was not held.
    const step = key === 'Tab' && shiftKey ? null : commitStep(key, false);
    if (step === null) {
      return;
    }
    this.returnFocusToSheet();
    const nextCol = Math.min(this.cols, Math.max(1, col + step.dCol));
    const nextRow = Math.min(this.rows, Math.max(1, row + step.dRow));
    this.selection = selectSingleCell(nextCol, nextRow);
    this.extendCol = nextCol;
    this.extendRow = nextRow;
    this.scrollCellIntoView({ col: nextCol, row: nextRow });
    this.recomputeViewport();
    this.dispatchSelectionEvent();
  };

  /** Escape in the formula field: put the cell's own value back. */
  private readonly handleFormulaCancel = (): void => {
    const { col, row } = this.selection.activeCell;
    this.formulaBar.renderState = {
      ...this.formulaBar.renderState,
      formula: this.formulaFieldValueFor(col, row)
    };
    this.returnFocusToSheet();
  };

  /**
   * A finished comment.
   *
   * The note shows what was typed straight away - the server sends the
   * comments back, and waiting for that leaves the old note on screen
   * in the meantime. A note emptied out is a note removed, which is
   * what the server makes of empty text.
   */
  private readonly handleCellCommentCommit = (e: CustomEvent<{ key?: string; text: string }>): void => {
    const { key, text } = e.detail;
    // Reported once. Finishing the note puts the keyboard back on the
    // sheet, which takes the focus off the textarea and asks again,
    // and the textarea is still there until the next render.
    if (this.editingCommentKey === null) {
      return;
    }
    this.editingCommentKey = null;
    const coord = key === undefined ? null : parseColRow(key);
    if (coord === null || coord.col < 1 || coord.row < 1) {
      this.scheduleRender();
      return;
    }
    this.cellComments = { ...this.cellComments, [key as string]: text };
    this.scheduleRender();
    this.returnFocusToSheet();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'updateCellComment', data: { text, col: coord.col, row: coord.row } },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * An outline level button: show the sheet down to that level.
   *
   * The server expands everything shallower and collapses that level,
   * which hides whatever is nested under it.
   */
  private readonly handleLevelHeaderClick = (e: CustomEvent<{ cols: boolean; level: number }>): void => {
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'levelHeaderClicked', data: { cols: e.detail.cols, level: e.detail.level } },
        bubbles: true,
        composed: true
      })
    );
  };

  /** Double-click on the active tab: show its name in an input. */
  private readonly handleSheetRenameStart = (e: CustomEvent<{ index: number }>): void => {
    if (this.workbookProtected) {
      return;
    }
    this.renamingSheetIndex = e.detail.index;
    this.syncSheetTabs();
  };

  /** Escape: leave the sheet called what it was called. */
  private readonly handleSheetRenameCancel = (): void => {
    this.renamingSheetIndex = 0;
    this.syncSheetTabs();
    this.returnFocusToSheet();
  };

  /**
   * A finished rename.
   *
   * A name the workbook would refuse, one that is what the sheet is
   * already called, and one another sheet has taken all leave the tab
   * as it was and the server none the wiser. Anything else is shown on
   * the tab straight away - the server will send the names back, and
   * waiting for that leaves the old name sitting there in the meantime.
   */
  private readonly handleSheetRename = (e: CustomEvent<{ index: number; name: string }>): void => {
    const { index, name } = e.detail;
    this.renamingSheetIndex = 0;
    if (index < 1 || index > this.sheetNames.length) {
      this.syncSheetTabs();
      return;
    }
    const outcome = resolveSheetRename({
      proposed: name,
      index: index - 1,
      names: this.sheetNames
    });
    if (outcome.kind === 'revert') {
      this.syncSheetTabs();
      this.returnFocusToSheet();
      return;
    }
    const renamed = [...this.sheetNames];
    renamed[index - 1] = outcome.name;
    this.sheetNames = renamed;
    this.syncSheetTabs();
    this.returnFocusToSheet();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          // The server counts sheets from 0; the strip renders from 1.
          type: 'sheetRenamed',
          data: { sheetIndex: index - 1, newName: outcome.name }
        },
        bubbles: true,
        composed: true
      })
    );
  };

  /** The + button: ask the server for a new sheet. */
  private readonly handleSheetTabAdd = (): void => {
    if (this.workbookProtected) {
      return;
    }
    // The strip button took the focus on the way in. Adding a sheet is a
    // way of moving to it, so the keyboard belongs to the new sheet.
    this.scrollContainer.focus();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'sheetCreated',
          data: {
            scrollLeft: this.scrollContainer.scrollLeft,
            scrollTop: this.scrollContainer.scrollTop
          }
        },
        bubbles: true,
        composed: true
      })
    );
  };

  private readonly handleSheetTabClick = (e: CustomEvent<{ index: number }>): void => {
    const { index } = e.detail;
    // Clicking a tab focused it. Switching sheets is navigation, so the
    // keyboard goes back to the grid — the arrow keys that naturally follow
    // must move the selection, not the tab strip. This holds for a click on
    // the already-active tab too, which is otherwise a no-op.
    this.scrollContainer.focus();
    if (index === this.sheetIndex) {
      // Clicking the tab that is already active selects nothing — Excel
      // does nothing here either. Reporting it anyway makes the server run
      // `setActiveSheetIndex` on the sheet it is already showing, and that
      // goes through `reloadActiveSheetData`, which throws away every
      // overlay, comment, hyperlink, table and popup button `loadCells`
      // had just loaded.
      return;
    }
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
            // The server reads a 0-based tab index here; `index` is the
            // 1-based position the strip renders.
            sheetIndex: index - 1,
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

  /** The sheet's width, not the component's. See {@link setHeight}. */
  setWidth(width: string): void {
    this.root.style.width = width;
  }

  /**
   * Size the sheet, not the component.
   *
   * The wrapper asks for 100% as it starts the connector up, which is
   * the sheet filling whatever room the component has. Writing that on
   * the host instead threw away the size the application had given the
   * component with its own setHeight, and the sheet then took the
   * height of the page rather than the 250px it was asked for. The GWT
   * client put this on the widget, and the host was never its to
   * touch.
   */
  setHeight(height: string): void {
    this.root.style.height = height;
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

  notifyStateChanges(changedPropertyNames: string[], _firstUpdate: boolean): void {
    // The wrapper calls this once per property batch, after forwarding every
    // changed property. Without a repaint here, server-pushed state that is
    // not a cell value — overlays, popup buttons, comments, merged regions,
    // style maps, grouping, hidden rows and columns — reaches the connector
    // and never reaches the screen.
    //
    // Rendered now rather than on the next animation frame. This runs at
    // the end of the batch, while the browser is still processing the
    // server's response, so painting here keeps the rule a caller can rely
    // on: once a round trip is finished, the sheet shows its result. A
    // deferred paint leaves the DOM a frame behind — invisible to a person,
    // but every test that acts and then reads is racing it.
    //
    // Still one render per batch: this is called once, after every changed
    // property has been forwarded.
    if (changedPropertyNames.length > 0) {
      this.flushRender();
    }
  }

  // ====================================================================
  // 12 explicit RPC methods (server→client action messages)
  // ====================================================================

  updateBottomRightCellValues(cellData: CellData[] | string): void {
    this.storeCellData(cellData);
    this.scheduleRender();
  }
  updateTopLeftCellValues(cellData: CellData[] | string): void {
    this.storeCellData(cellData);
    this.scheduleRender();
  }
  updateTopRightCellValues(cellData: CellData[] | string): void {
    this.storeCellData(cellData);
    this.scheduleRender();
  }
  updateBottomLeftCellValues(cellData: CellData[] | string): void {
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
  private storeCellData(cellData: CellData[] | string): void {
    // Flow hands collections over as a JSON string on some lanes and as an
    // array on others. Iterating the string walks it character by
    // character, which quietly files one entry under
    // "colundefined rowundefined" and drops every value on the sheet.
    for (const cell of coerceArray<CellData>(cellData)) {
      this.cellDataByKey.set(toKey(cell.col, cell.row), cell);
    }
  }
  updateFormulaBar(possibleName: string | null, col: number, row: number): void {
    // The server only sends a name when the selection has one — a named
    // range, or a sheet-qualified reference. For an ordinary cell it
    // sends null and leaves the address to the client, which is the
    // common case: without this the address field sits empty for every
    // selection the user makes. Slice 175.
    this.formulaBar.renderState = {
      // While the user is still growing a selection the address field
      // counts the block out, and this is the server naming the anchor
      // of the range they are in the middle of making.
      address: this.extendingSelection
        ? selectionSizeLabel(this.selection.activeRange)
        : possibleName ?? cellCoordToAddress({ col, row }),
      formula: this.formulaFieldValueFor(col, row),
      namedRanges: this.namedRanges
    };
  }

  /**
   * What the formula field shows for a cell.
   *
   * The bar shows what the cell *is*, not what it displays: the formula
   * behind a computed cell, and otherwise the value before formatting.
   * The unformatted value is what carries a text cell's leading
   * apostrophe and a number's full precision, both of which the
   * displayed value has already dropped.
   *
   * The server sends a formula without its leading `=`, the way POI
   * stores it.
   */
  private formulaFieldValueFor(col: number, row: number): string {
    const data = this.cellDataByKey.get(toKey(col, row));
    if (data === undefined) {
      return '';
    }
    const formula = data.formulaValue;
    if (formula !== undefined && formula !== null && formula !== '') {
      return `=${formula}`;
    }
    return data.originalValue ?? data.value ?? '';
  }
  /**
   * The server could not make sense of the address that was typed.
   *
   * The address field goes back to the cell the selection is actually on,
   * as the GWT client did. Leaving the typed text would have the field
   * naming a cell nobody is standing on, and the next Enter would send it
   * again.
   */
  invalidCellAddress(): void {
    this.formulaBar.revertAddress();
  }
  /**
   * The server moved the selection to a single cell.
   *
   * Sent from the server's address-change path — jumping by address,
   * and following an internal hyperlink — so the selection collapses
   * to that one cell, as the GWT client did. Range selections never
   * come through here, so nothing a drag produced is disturbed.
   *
   * Updating only the formula bar, as this did before, left the
   * highlight behind on the cell the user had clicked while the
   * address field named the one the server had moved to.
   *
   * `cellValue` is the formula without its leading `=` when
   * `formula` is set — that is how POI stores it and how the server
   * sends it.
   */
  showSelectedCell(
    name: string | null,
    col: number,
    row: number,
    cellValue: string,
    formula: boolean,
    _locked: boolean,
    _initialSelection: boolean
  ): void {
    // Read before the selection collapses to the one cell below: while
    // the user is still growing a selection the address field is
    // counting the block out, and this is the server answering the
    // anchor of the range they are in the middle of making.
    const sizeLabel = selectionSizeLabel(this.selection.activeRange);
    this.selection = selectSingleCell(col, row);
    this.extendCol = col;
    this.extendRow = row;
    this.formulaBar.renderState = {
      address: this.extendingSelection ? sizeLabel : name ?? cellCoordToAddress({ col, row }),
      formula: formula && cellValue !== '' ? `=${cellValue}` : cellValue,
      namedRanges: this.namedRanges
    };
    // The server moves the selection for the user: a hyperlink followed,
    // an address typed into the address field, a named range picked, a
    // sheet operation landing somewhere else. Somewhere the user cannot
    // see is not somewhere they have been taken, so bring it into view.
    // A cell that is already visible stays where it is.
    this.scrollCellIntoView({ col, row });
    this.scheduleRender();
  }
  showActions(actionDetails: SpreadsheetActionDetails[]): void {
    const actions = coerceArray<SpreadsheetActionDetails>(actionDetails);
    this.contextMenu.renderState = {
      open: true,
      x: this.lastContextMenuPosition.x,
      y: this.lastContextMenuPosition.y,
      // Arrives JSON-serialized through the Flow lane, like the
      // collection-shaped property pushes slice 179 handles.
      actions
    };
    // An action can carry an icon component; 0 means it has none. The
    // slots that show them are in the menu's template, so place the
    // elements only once that has rendered.
    const iconIds = actions
      .filter((action) => Number(action.iconNodeId) > 0)
      .map((action) => String(action.iconNodeId));
    void this.contextMenu.updateComplete.then(() => this.actionIcons.sync(iconIds));
  }

  /**
   * Note where a right-click landed. The menu is placed here once the server
   * answers with the actions it offers.
   */
  private rememberContextMenuPosition(detail: { clientX?: number; clientY?: number }): void {
    // Kept relative to the sheet's own box, because that is the space
    // the menu is placed in. Viewport coordinates stop meaning the
    // viewport as soon as an ancestor has a transform - a dialog
    // overlay has one while it animates - and the menu then opens
    // somewhere other than where the user clicked.
    const origin = this.root.getBoundingClientRect();
    this.lastContextMenuPosition = {
      x: (detail.clientX ?? 0) - origin.left,
      y: (detail.clientY ?? 0) - origin.top
    };
  }

  /**
   * Close the context menu and tell the server, unless it is already closed.
   */
  private closeContextMenu(): void {
    if (!this.contextMenu.renderState.open) {
      return;
    }
    this.contextMenu.renderState = emptyContextMenuRenderState();
    this.actionIcons.clear();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: 'contextMenuClosed', data: {} },
        bubbles: true,
        composed: true
      })
    );
  }

  /** An item was picked: report it against whatever the menu was opened on. */
  private readonly handleContextMenuAction = (e: CustomEvent<{ key: string; type: number }>): void => {
    const { key, type } = e.detail;
    // 0 = cell, 1 = row, 2 = column, matching SpreadsheetActionDetails.
    const eventType =
      type === 1 ? 'actionOnRowHeader' : type === 2 ? 'actionOnColumnHeader' : 'actionOnCurrentSelection';
    this.contextMenu.renderState = emptyContextMenuRenderState();
    this.actionIcons.clear();
    // The menu item had the focus; picking it is done, so the keyboard
    // goes back to the sheet.
    this.returnFocusToSheet();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: { type: eventType, data: { actionKey: key } },
        bubbles: true,
        composed: true
      })
    );
  };

  /** Any click outside the menu dismisses it, as a native menu would. */
  private readonly handleDocumentPointerDown = (e: Event): void => {
    const path = e.composedPath();
    // A click inside the popup belongs to the popup's content; one on the
    // button that opened it is the server's to interpret, and closing here
    // would race the reopen it triggers.
    // The content is a light child of the host projected into the overlay,
    // so the path reaches the overlay through the slot — except before the
    // projection settles, hence the second check.
    const insidePopup = path.includes(this.popupOverlay) || path.some((target) => this.popupContents.owns(target));
    if (this.openPopup !== null && !insidePopup && !this.hitOpenPopupButton(path)) {
      this.dismissPopup();
    }
    if (!this.contextMenu.renderState.open) {
      return;
    }
    if (path.includes(this.contextMenu)) {
      return;
    }
    this.closeContextMenu();
  };

  /** Whether an event path passes through the button of the open popup. */
  private hitOpenPopupButton(path: EventTarget[]): boolean {
    const open = this.openPopup;
    if (open === null) {
      return false;
    }
    return path.some(
      (target) =>
        target instanceof Element &&
        target.classList.contains('popup-button') &&
        target.getAttribute('data-col') === String(open.col) &&
        target.getAttribute('data-row') === String(open.row)
    );
  }

  private readonly handleContextMenuKeyDown = (e: Event): void => {
    if ((e as KeyboardEvent).key !== 'Escape') {
      return;
    }
    this.dismissPopup();
    this.closeContextMenu();
  };
  setSelectedCellAndRange(
    name: string,
    col: number,
    row: number,
    c1: number,
    c2: number,
    r1: number,
    r2: number,
    scroll: boolean
  ): void {
    // The server is authoritative here: this arrives from
    // Spreadsheet.setSelection / setSelectionRange and after operations
    // that move the selection server-side (sheet change, row insert,
    // undo). The active cell is (col, row); it need not be a corner of
    // the range, so the range cannot be derived from it.
    // Keep any aux ranges. The server reports the active cell and the
    // active range only; it has no opinion about the other ranges of a
    // non-coherent (Ctrl-clicked) selection. Clearing them here undoes
    // the Ctrl-click the moment the server echoes the selection back.
    // A plain click has already cleared them client-side, so preserving
    // the current list is also correct for the replace case.
    this.selection = {
      activeCell: { col, row },
      activeRange: normalizedRange(c1, r1, c2, r2),
      auxRanges: this.selection.auxRanges
    };
    // Shift+arrow grows the selection from its far corner, so that
    // corner has to follow a selection the server made. Left where it
    // was, the first shift+arrow after any server-driven selection
    // stretched the range back to wherever the user had last been.
    const far = normalizedRange(c1, r1, c2, r2);
    this.extendCol = far.c2;
    this.extendRow = far.r2;
    this.formulaBar.renderState = {
      // As in updateFormulaBar: the server names a selection only when
      // it has a name, and leaves an ordinary one to the client. While
      // the user is still growing the selection, though, the count is
      // what they are reading, and this is the server echoing back the
      // range they are in the middle of making.
      address: this.extendingSelection
        ? selectionSizeLabel(this.selection.activeRange)
        : name ?? cellCoordToAddress({ col, row }),
      formula: this.formulaBar.renderState.formula,
      namedRanges: this.namedRanges
    };
    if (scroll) {
      this.scrollCellIntoView({ col, row });
    }
    this.scheduleRender();
  }
  cellsUpdated(updatedCellData: CellData[]): void {
    this.storeCellData(updatedCellData);
    this.scheduleRender();
  }
  refreshCellStyles(): void {
    this.scheduleRender();
  }
  /**
   * Write the comment on a cell.
   *
   * The server calls this after its Insert comment / Edit comment
   * action has made sure there is one, so the note is already in the
   * comment map; all that is left is to open it for typing.
   */
  editCellComment(col: number, row: number): void {
    this.editingCommentKey = toKey(col, row);
    this.scheduleRender();
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
    // The server sizes General-format numbers against how wide a digit is
    // in each style, and only the client can measure that.
    this.styleWidthRatiosStale = true;
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
  /**
   * The server signals that it has thrown away its per-sheet state.
   *
   * `Spreadsheet.reloadActiveSheetData` clears the overlay, comment,
   * hyperlink and custom-component maps and resets its viewport bounds to
   * -1 before raising this flag. Everything `loadCells` provides is gone on
   * the server side until the client reports its viewport again, and
   * `loadCells` only runs off `onSheetScroll`. Repainting alone leaves a
   * sheet that shows its cells and nothing else — no images, no charts, no
   * comment markers — for as long as the user does not scroll.
   *
   * So clear the handshake latch and let the next completed viewport
   * announce itself.
   */
  /**
   * Whether an event was raised inside a server-side component the sheet
   * is hosting — a custom editor, an in-cell component, a chart overlay.
   *
   * Those components are light children of the host and reach the screen
   * through slots, so their events travel the flattened tree and arrive at
   * the sheet's own listeners. They are not the sheet's to act on.
   */
  private eventCameFromHostedComponent(e: Event): boolean {
    for (const target of e.composedPath()) {
      if (this.cellEditors.owns(target) || this.cellComponents.owns(target) || this.overlayComponents.owns(target)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Clicking a custom editor selects the cell it belongs to.
   *
   * An editor is usually wider than its cell, so a click aimed at the cell
   * often lands on the editor instead — and the cell-grid's own click
   * delegation cannot see it, because the editor is projected in rather
   * than a descendant of the cell. Without this, selecting such a cell by
   * mouse is impossible, and the server never learns the editor is showing.
   *
   * Bound to pointerdown rather than focus on purpose: the server focuses
   * an editor itself when it displays one, and reporting that back as a
   * selection would move the selection to whatever the server was last
   * talking about — the feedback loop the GWT client guarded against with
   * its `userInitiatedFocus` flag.
   */
  private readonly handleHostedEditorPointerDown = (e: Event): void => {
    const cell = this.cellShowingEditorFor(e);
    if (cell === null) {
      return;
    }
    if (this.selection.activeCell.col === cell.col && this.selection.activeCell.row === cell.row) {
      return;
    }
    this.selection = selectSingleCell(cell.col, cell.row);
    this.extendCol = cell.col;
    this.extendRow = cell.row;
    this.recomputeViewport();
    this.dispatchSelectionEvent();
  };

  /** The cell whose custom editor an event came out of, if any. */
  private cellShowingEditorFor(e: Event): { col: number; row: number } | null {
    if (!this.eventCameFromCellEditor(e)) {
      return null;
    }
    for (const [cellKey, assignment] of Object.entries(this.cellSlotAssignments())) {
      if (assignment.kind !== 'editor') {
        continue;
      }
      const element = this.cellEditors.get(assignment.nodeId);
      if (element === null || !e.composedPath().includes(element)) {
        continue;
      }
      const parsed = parseColRow(cellKey);
      return { col: parsed.col, row: parsed.row };
    }
    return null;
  }

  /** Whether an event came out of a custom cell editor specifically. */
  private eventCameFromCellEditor(e: Event): boolean {
    for (const target of e.composedPath()) {
      if (this.cellEditors.owns(target)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Hand the keyboard to the active cell's custom editor, if it has one.
   *
   * A cell showing a custom editor has no use for the inline editor: the
   * editor IS the way to change the value, so a key that would normally
   * start editing focuses it instead. Excel's F2 on such a cell does the
   * same thing.
   *
   * @returns true when a custom editor took the focus.
   */
  private focusCustomEditor(): boolean {
    const active = this.selection.activeCell;
    const assignment = this.cellSlotAssignments()[toKey(active.col, active.row)];
    if (assignment === undefined || assignment.kind !== 'editor') {
      return false;
    }
    const element = this.cellEditors.get(assignment.nodeId);
    if (element === null) {
      return false;
    }
    element.focus();
    return true;
  }

  /**
   * Which server-side element each cell shows, from the three inputs the
   * server pushes separately.
   *
   * Recomputed rather than cached: it depends on the selection, which
   * changes on every click, and the maps it reads are small.
   */
  private cellSlotAssignments(): Record<string, CellSlotAssignment> {
    const active = this.selection.activeCell;
    return computeCellSlots({
      componentIdToCellKey: this.componentIDtoCellKeysMap,
      cellKeyToEditorId: this.cellKeysToEditorIdMap,
      showCustomEditorOnFocus: this.showCustomEditorOnFocus,
      activeCellKey: active === null ? null : toKey(active.col, active.row)
    });
  }

  setReload(triggerReload: boolean): void {
    if (triggerReload) {
      // The server has thrown away everything it knew about the sheet —
      // a sheet switch, a new file, a reset. Our cached cell values are
      // the previous sheet's, and the server only ever sends cells that
      // have something in them, so a cell that is empty on the new sheet
      // is never mentioned again and the old value would show through.
      this.cellDataByKey.clear();
      this.viewportAnnounced = false;
      // The bounds it holds are gone too — re-announce even if they come
      // out identical.
      this.announcedBounds = '';
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
    // Refresh the bar directly: the named-range list arrives on the property
    // lane, which repaints the grid but does not rebuild the formula bar.
    this.formulaBar.renderState = {
      ...this.formulaBar.renderState,
      namedRanges: this.namedRanges
    };
  }
  setResources(host: HTMLElement, keys: string[] | string): void {
    // The server sends the resource KEYS here and puts each URL on the host
    // as a `resource-<key>` attribute, because the URLs are Flow stream
    // resources minted per element rather than plain strings
    // (Spreadsheet.setResource). Resolving them means reading the
    // attributes, not parsing a map out of this argument.
    this.resourceHost = host;
    this.resourceKeys = coerceArray<string>(keys);
    this.scheduleRender();
  }

  /**
   * Read the current URL for every published resource id.
   *
   * Deliberately re-read on every render rather than cached when the key
   * list arrives. Scrolling an overlay out of view unregisters its stream
   * resource, and scrolling back mints a fresh one under the same id: the
   * attribute changes while the key list does not, so there is no property
   * push to cache on. A cached URL would point at a registration the
   * server has already dropped, and the image would 404.
   */
  private resolveResources(): Record<string, string> {
    const host = this.resourceHost;
    if (host === null) {
      return {};
    }
    const resolved: Record<string, string> = {};
    for (const key of this.resourceKeys) {
      const url = host.getAttribute(`resource-${key}`);
      if (url) {
        resolved[key] = url;
      }
    }
    return resolved;
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

  /**
   * Show a popup button's content.
   *
   * Called by {@code PopupButton.openPopup()} — which the server also runs
   * when it handles the button's own click, so this is the only path that
   * ever opens a popup, whether the user clicked the button or application
   * code asked for it.
   *
   * The content arrives as a Flow node id: the server attached it with
   * {@code appendVirtualChild}, so the element exists on the client but is
   * not in the document until we place it. `appId` names the Flow
   * application to resolve it against — this is the popup button's own
   * element, which may belong to a different application than the sheet
   * when several are on the page.
   */
  onPopupButtonOpen(row: number, column: number, contentId: string, appId: string): void {
    const nodeId = String(contentId);
    this.openPopup = { row, col: column, nodeId };
    // A resolver per call rather than the sheet's own: the id belongs to
    // the application the button reports, not necessarily the sheet's.
    this.popupContents.setResolver(flowVirtualChildResolver(appId));
    this.popupContents.sync([nodeId]);
    const state = this.popupButtonStates.find((b) => b.row === row && b.col === column);
    const parts: Node[] = [];
    if (!state?.headerHidden) {
      parts.push(this.buildPopupHeader());
    }
    parts.push(namedSlot(popupContentSlotName(nodeId)));
    this.popupOverlay.replaceChildren(...parts);
    this.popupOverlay.hidden = false;
    this.positionPopupOverlay();
    this.scheduleRender();
  }

  /**
   * The popup's header strip and its close box.
   *
   * Shown unless the button asked for {@code headerHidden}, which is what
   * {@code PopupButton.setHeaderHidden(true)} sets: a popup with no header
   * has to be dismissed by clicking away from it.
   */
  private buildPopupHeader(): HTMLElement {
    const header = document.createElement('div');
    header.className = 'popup-header';
    header.setAttribute('part', 'popup-header');
    header.style.cssText = 'display:flex;justify-content:flex-end;align-items:center;';
    const close = document.createElement('button');
    close.type = 'button';
    close.className = 'popup-close';
    close.setAttribute('part', 'popup-close');
    close.setAttribute('aria-label', 'Close');
    close.textContent = '\u00d7';
    close.style.cssText = 'border:0;background:transparent;cursor:pointer;font:inherit;line-height:1;';
    close.addEventListener('click', () => this.dismissPopup());
    header.appendChild(close);
    return header;
  }

  /**
   * Hide the open popup at the given cell.
   *
   * A no-op when the popup that is open belongs to another cell: the
   * server closes popups one by one and a stale close must not take down
   * the one the user just opened.
   */
  closePopup(row: number, column: number): void {
    if (this.openPopup === null || this.openPopup.row !== row || this.openPopup.col !== column) {
      return;
    }
    this.hidePopup();
  }

  /**
   * Take the popup down and tell the server. Used for every client-side
   * dismissal — clicking away, Escape, scrolling the anchor out of view.
   */
  private dismissPopup(): void {
    const closing = this.openPopup;
    if (closing === null) {
      return;
    }
    this.hidePopup();
    this.host.dispatchEvent(
      new CustomEvent('spreadsheet-event', {
        detail: {
          type: 'popupClose',
          data: { row: closing.row, column: closing.col }
        },
        bubbles: true,
        composed: true
      })
    );
  }

  /** Hide the overlay and release its content, without telling the server. */
  private hidePopup(): void {
    this.openPopup = null;
    this.popupContents.clear();
    this.popupOverlay.replaceChildren();
    this.popupOverlay.hidden = true;
    this.scheduleRender();
  }

  /**
   * The range from the anchor to a target cell, grown to take in whole
   * merged regions.
   *
   * A merged region is one cell to the user, so a selection that ends
   * inside one has to take all of it; reporting the literal rectangle
   * leaves the server writing to cells nobody can see.
   */
  private rangeTo(anchor: { col: number; row: number }, col: number, row: number): CellRange {
    const range = normalizedRange(anchor.col, anchor.row, col, row);
    const grown = expandToMergedRegions({ c1: range.c1, c2: range.c2, r1: range.r1, r2: range.r2 }, this.mergedRegions);
    return { c1: grown.c1, c2: grown.c2, r1: grown.r1, r2: grown.r2 };
  }

  /**
   * Rendered width of a column in pixels — 0 when it is hidden, the
   * sheet's default past the last defined column.
   */
  private colWidthPx(col: number): number {
    if (this.hiddenColumnIndexes.includes(col)) {
      return 0;
    }
    return col > this.colWidths.length ? this.defColWidth : this.colWidths[col - 1];
  }

  /**
   * Rendered height of a row in pixels. POI measures rows in points, so
   * this is the one place the conversion happens.
   */
  private rowHeightPx(row: number, ppi: number = FALLBACK_PPI): number {
    if (this.hiddenRowIndexes.includes(row)) {
      return 0;
    }
    return row > this.rowHeights.length
      ? getDefaultRowHeightPx(this.defRowHeight, ppi)
      : convertPointsToPixel(this.rowHeights[row - 1], ppi);
  }

  /**
   * Seat the overlay under its button, in the scroll container's
   * coordinates — the same space the buttons themselves are positioned in,
   * so it tracks the sheet as it scrolls.
   */
  private positionPopupOverlay(): void {
    if (this.openPopup === null) {
      return;
    }
    const { row, col } = this.openPopup;
    let left = 0;
    for (let c = 1; c < col; c++) {
      left += this.colWidthPx(c);
    }
    let top = 0;
    for (let r = 1; r <= row; r++) {
      top += this.rowHeightPx(r);
    }
    const state = this.popupButtonStates.find((b) => b.row === row && b.col === col);
    this.popupOverlay.style.left = `${left}px`;
    this.popupOverlay.style.top = `${top}px`;
    this.popupOverlay.style.width = state?.popupWidth ?? '';
    this.popupOverlay.style.height = state?.popupHeight ?? '';
  }
  /**
   * A popup (filter) button was attached to a cell.
   *
   * These arrive one at a time over the RPC lane rather than as a list
   * property, because on the server each is a real component with its
   * own lifecycle. The connector keeps them in the same list the
   * renderer already positions, keyed by their cell.
   */
  addPopupButton(rawState: PopupButtonState | string): void {
    const state = coerceObject<unknown>(rawState) as unknown as PopupButtonState;
    if (state === null || typeof state.col !== 'number') {
      return;
    }
    this.popupButtonStates = [...this.popupButtonStates.filter((b) => !samePopupButtonCell(b, state)), state];
    this.scheduleRender();
  }

  /** The button's cell is what identifies it; the rest of the state may differ. */
  removePopupButton(rawState: PopupButtonState | string): void {
    const state = coerceObject<unknown>(rawState) as unknown as PopupButtonState;
    if (state === null || typeof state.col !== 'number') {
      return;
    }
    this.popupButtonStates = this.popupButtonStates.filter((b) => !samePopupButtonCell(b, state));
    this.scheduleRender();
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
 * What a cell shows in place of its own text: a server-side component, or
 * the custom editor for that cell.
 */
export interface CellSlotAssignment {
  /** Flow node id of the element to project. */
  nodeId: string;
  /** Slot name the cell renders and the element is assigned to. */
  slot: string;
  /** Which holder owns the element. */
  kind: 'component' | 'editor';
}

/**
 * Decide, per cell, which server-side element the sheet should show.
 *
 * Two sources feed this. `setComponent` puts a component in a cell
 * outright. A custom editor is attached to a cell too, but
 * `showCustomEditorOnFocus` decides when it appears: by default every
 * editor is shown in its cell, and when the flag is set only the selected
 * cell's editor is.
 *
 * Editors are applied after components, so an editor wins a cell that has
 * both — the same order the GWT client mounted them in, and the one that
 * makes sense: the editor is the interactive thing.
 */
export function computeCellSlots(input: {
  componentIdToCellKey: Readonly<Record<string, string>>;
  cellKeyToEditorId: Readonly<Record<string, string>>;
  showCustomEditorOnFocus: boolean;
  activeCellKey: string | null;
}): Record<string, CellSlotAssignment> {
  const assignments: Record<string, CellSlotAssignment> = {};

  for (const [nodeId, cellKey] of Object.entries(input.componentIdToCellKey)) {
    if (cellKey) {
      // Two components claiming one cell cannot both be shown; the later
      // entry wins, matching what a single slot per cell can render.
      assignments[cellKey] = {
        nodeId,
        slot: cellComponentSlotName(nodeId),
        kind: 'component'
      };
    }
  }

  for (const [cellKey, nodeId] of Object.entries(input.cellKeyToEditorId)) {
    if (!nodeId) {
      continue;
    }
    if (input.showCustomEditorOnFocus && cellKey !== input.activeCellKey) {
      continue;
    }
    assignments[cellKey] = {
      nodeId,
      slot: cellEditorSlotName(nodeId),
      kind: 'editor'
    };
  }

  return assignments;
}

/**
 * The subset of {@link computeCellSlots} whose cells the sheet renders.
 *
 * A cell off screen renders no slot, so parenting its element would take
 * it out of the document for nothing. Scrolling back renders the slot
 * again and the next repaint places it.
 *
 * "On screen" is not just the scrolling viewport: a frozen row or column
 * stays rendered whatever the scroll position, in its own quadrant. An
 * editor pinned in a frozen row was never placed while only the viewport
 * counted.
 */
export function visibleCellSlots(
  assignments: Readonly<Record<string, CellSlotAssignment>>,
  state: {
    viewport: {
      firstRowIndex: number;
      lastRowIndex: number;
      firstColumnIndex: number;
      lastColumnIndex: number;
    };
    verticalSplitPosition: number;
    horizontalSplitPosition: number;
  }
): CellSlotAssignment[] {
  const { viewport } = state;
  const rows = new Set<number>();
  for (let row = 1; row <= state.verticalSplitPosition; row++) {
    rows.add(row);
  }
  for (let row = viewport.firstRowIndex; row <= viewport.lastRowIndex; row++) {
    rows.add(row);
  }
  const cols = new Set<number>();
  for (let col = 1; col <= state.horizontalSplitPosition; col++) {
    cols.add(col);
  }
  for (let col = viewport.firstColumnIndex; col <= viewport.lastColumnIndex; col++) {
    cols.add(col);
  }

  const visible: CellSlotAssignment[] = [];
  for (const row of rows) {
    for (const col of cols) {
      const assignment = assignments[toKey(col, row)];
      if (assignment !== undefined) {
        visible.push(assignment);
      }
    }
  }
  return visible;
}

/**
 * The Flow application the sheet belongs to.
 *
 * The server pushes it as an {@code appId} property on the host — the
 * component is registered against one running application, and looking a
 * virtual child up in the wrong one silently finds nothing.
 */
/** Two popup-button states describe the same button when they share a cell. */
function samePopupButtonCell(a: PopupButtonState, b: PopupButtonState): boolean {
  return a.col === b.col && a.row === b.row;
}

export function overlayComponentAppId(host: HTMLElement): string {
  return String((host as unknown as { appId?: string }).appId ?? '');
}

/**
 * Pure helper: build the positioned-overlay list from the server-
 * pushed `OverlayInfo` map + resource map. Each overlay's pixel
 * position is `anchor cell's left/top` (cumulative sums of col/
 * row sizes 1..anchor-1) + the overlay's `dx`/`dy` offset.
 *
 * The two axes do not share a unit. POI measures anything vertical in
 * points, so `height` and `dy` are points while `width` and `dx` are
 * already pixels — the same split the row heights come through with.
 * Treating the vertical pair as pixels makes every overlay 25% too short
 * and seats it too high.
 *
 * Pure (no DOM, no instance state) so it's straightforward to
 * unit-test in isolation. Slice 160.
 */
export function computePositionedOverlays(
  overlays: Record<string, OverlayInfo>,
  resources: Record<string, string>,
  colWidth: (col: number) => number,
  rowHeight: (row: number) => number,
  ppi: number = FALLBACK_PPI
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
      col: info.col,
      row: info.row,
      id,
      type: info.type,
      src: info.type === 'IMAGE' ? resources[id] : undefined,
      leftPx: left + info.dx,
      topPx: top + convertPointsToPixel(info.dy, ppi),
      widthPx: info.width,
      heightPx: convertPointsToPixel(info.height, ppi)
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

/**
 * How Excel's name box counts a block being selected: rows first, then
 * columns, as in "3R x 3C".
 */
export function selectionSizeLabel(range: CellRange): string {
  const rows = Math.abs(range.r2 - range.r1) + 1;
  const cols = Math.abs(range.c2 - range.c1) + 1;
  return `${rows}R x ${cols}C`;
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
