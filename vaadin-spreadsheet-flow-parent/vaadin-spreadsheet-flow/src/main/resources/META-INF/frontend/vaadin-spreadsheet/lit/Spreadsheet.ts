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
  SpreadsheetActionDetails
} from './types/wire.js';
import type { SpreadsheetClientApi } from './types/rpc-server-to-client.js';
import {
  addAuxRange,
  normalizedRange,
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
import type { CellEditorState, MergedRegionBox, PositionedOverlay, PositionedPopupButton } from './elements/types.js';
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
import {
  canPoint,
  formulaReferences,
  isFormula,
  placeReference,
  referenceAt,
  referenceText
} from './algorithms/FormulaPointing.js';
import {
  type Direction,
  isSingleMerge,
  moveActiveCell,
  moveRangeEdge,
  selectCellAt,
  type SheetShape,
  type SingleCellSelection,
  typesCharacter
} from './algorithms/KeyboardMoves.js';
import { resolveSheetRename } from './algorithms/SheetName.js';
import type { WalkDirection } from './algorithms/SelectionWalk.js';
import { serializeToTsv } from './algorithms/Tsv.js';
import { SpreadsheetCellGrid } from './elements/SpreadsheetCellGrid.js';
import { animateIn, animateOut, overlayAnimationStyles } from './dom/OverlayAnimation.js';
import {
  columnGroupingGutterPx,
  type PositionedGrouping,
  rowGroupingGutterPx,
  SpreadsheetColumnHeader
} from './elements/SpreadsheetColumnHeader.js';
import { SpreadsheetRowHeader } from './elements/SpreadsheetRowHeader.js';
import { SpreadsheetHeaderCorner } from './elements/SpreadsheetHeaderCorner.js';
import { SpreadsheetSheetTabs } from './elements/SpreadsheetSheetTabs.js';
import { SpreadsheetFormulaBar } from './elements/SpreadsheetFormulaBar.js';
import { emptyContextMenuRenderState, SpreadsheetContextMenu } from './elements/SpreadsheetContextMenu.js';
import { type LoadRange, ViewportLoader } from './state/ViewportLoader.js';
import type { CellGridRenderState } from './elements/types.js';

/**
 * Which way a key moves the selection: an arrow the way it points, Tab
 * right and Enter down - shift reversing those two. Null for any other key.
 */
export function keyDirection(key: string, shiftKey: boolean): Direction | null {
  switch (key) {
    case 'ArrowUp':
      return 'up';
    case 'ArrowDown':
      return 'down';
    case 'ArrowLeft':
      return 'left';
    case 'ArrowRight':
      return 'right';
    case 'Tab':
      return shiftKey ? 'left' : 'right';
    case 'Enter':
      return shiftKey ? 'up' : 'down';
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

/** The style index a cell's class list names - "cs5 r cf2" is 5 - or null. */
function styleIndexOf(cellStyle: string | undefined): number | null {
  const match = /(?:^|\s)cs(\d+)(?:\s|$)/.exec(cellStyle ?? '');
  return match ? Number(match[1]) : null;
}

/**
 * Height of the column headers, in pixels: the GWT client's 27, so a sheet
 * starts its first row where it always did.
 */
const DEFAULT_HEADER_ROW_HEIGHT = 27;

/**
 * Width of the row headers, in pixels: the GWT client's 50, which also fits
 * a six-digit row number.
 */
const DEFAULT_HEADER_COLUMN_WIDTH = 50;

/**
 * The client of the Spreadsheet component: the connector between the
 * {@code <vaadin-spreadsheet>} element and the server.
 *
 * It builds the sheet's parts - formula bar, headers, cell grid, sheet
 * tabs, context menu - and pushes one render state to each; it turns what
 * the user does into events for the server and applies what the server
 * sends. The element creates one per element, as its {@code $connector}.
 */
export class Spreadsheet implements SpreadsheetClientApi {
  // ====================================================================
  // Internal state
  // ====================================================================

  /**
   * The HTMLElement we dispatch server-bound events on (the
   * wrapper `<vaadin-spreadsheet>` from the Flow side, or a
   * plain test div). DOM is appended to {@link renderTarget}
   * — usually the same element, but a ShadowRoot when the
   * wrapper passes its `renderRoot`.
   */
  private readonly host: HTMLElement;

  /**
   * Where DOM gets appended. When the wrapper is a LitElement,
   * this is its shadow root (passed as the second constructor
   * arg). Otherwise it's the host element itself.
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
   * The cell a click or an arrow asked for when the selection is a merged
   * region: the arrows leave the merge in its column and row, as in the
   * GWT client. Null when the selection is not a merge.
   */
  private mergeEntry: { col: number; row: number } | null = null;

  /**
   * The formula being written and where its caret is, while one is: its
   * references are coloured, on the sheet and in the formula field.
   */
  private formulaDraft: { text: string; caret: number } | null = null;

  /**
   * The reference last pointed at in the formula being written - the
   * GWT client's point mode: where the pointed range starts and ends on
   * the sheet, and where its text is in the formula, which pointing again
   * replaces. Null once anything is typed.
   */
  private pointer: {
    anchor: { col: number; row: number };
    end: { col: number; row: number };
    span: { start: number; end: number };
  } | null = null;

  /**
   * The cell pointed at last in the formula being written. The next arrow
   * after an operator starts from it, as the GWT client's did for the rest
   * of the edit - not from the cell being edited.
   */
  private lastPointed: { col: number; row: number } | null = null;

  /** A drag pointing out a range with the mouse is under way. */
  private pointerDragging = false;

  /** The press pointed at a cell, so the click it ends in selects nothing. */
  private pointedByPress = false;

  /**
   * Cell-editor state. Null when no editor is
   * active. When set, the cell-grid renders an editable
   * {@code <input>} at the cell's coordinates; the user types,
   * then Enter/Tab commits and Escape cancels.
   */
  private editor: CellEditorState | null = null;

  /**
   * Whether the editor opened on a character typed into a percentage cell,
   * with the % it was given still to come off once the entry turns into a
   * formula. See {@link pointAt}.
   */
  private autoPercentEntry = false;

  /**
   * Mouse-drag state. True between mousedown on a
   * cell and the subsequent mouseup (anywhere). While true,
   * {@code cell-mouseover} events extend the active range from
   * the drag's anchor cell to the hovered cell.
   */
  private dragging = false;

  /**
   * Column-resize drag state. Set on
   * {@code column-resize-start}, cleared on mouseup. As in the GWT client,
   * the column keeps its width until the drag ends: a line and a tooltip
   * follow the pointer meanwhile, and the width it takes is from the
   * column's left edge, {@code edge}, to the pointer at {@code x}.
   */
  private columnResize: { col: number; edge: number; x: number; y: number } | null = null;

  /**
   * Row-resize drag state. Symmetric to
   * {@link columnResize}: the height runs from the row's top edge to the
   * pointer at {@code y}.
   */
  private rowResize: { row: number; edge: number; x: number; y: number } | null = null;

  /** Where a dragged header edge would go: a line across the whole sheet. */
  private readonly resizeLine: HTMLElement;

  /** The size a dragged header would take, by the pointer. */
  private readonly resizeTooltip: HTMLElement;

  /** A link cell's tooltip, shown while the pointer is over the cell. */
  private readonly linkTooltip: HTMLElement;

  /**
   * Over the sheet while a resize drag moves, so the resize cursor shows
   * wherever the pointer goes, as the GWT client's did. Not before the
   * pointer moves: the second click of a double-click, which fits the
   * column to its content, has to reach the handle.
   */
  private readonly resizeCover: HTMLElement;

  /** Row / column geometry. */
  private rowHeights: NumberList = [];
  private colWidths: NumberList = [];
  private defRowHeight = 15; // points
  private defColWidth = 64; // pixels

  // Hidden / locked indexes
  /*
   * Sets, not arrays: they are asked about one row or column at a time
   * inside loops over rows, and on a filtered table with tens of thousands
   * of hidden rows an array lookup made each arrow key take 200 ms.
   */
  private hiddenRows: ReadonlySet<number> = new Set();
  private hiddenColumns: ReadonlySet<number> = new Set();
  private lockedRows: ReadonlySet<number> = new Set();
  private lockedColumns: ReadonlySet<number> = new Set();

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
   * {@link resolveResources}.
   */
  private resourceKeys: string[] = [];
  /** The element the `resource-<id>` attributes live on. */
  private resourceHost: HTMLElement | null = null;
  /** The popup buttons the server has added, one per cell. */
  private popupButtonStates: PopupButtonState[] = [];
  private cellKeysToEditorIdMap: Record<string, string> = {};
  private componentIDtoCellKeysMap: Record<string, string> = {};
  private hyperlinksTooltips: Record<string, string> = {};

  /** Freeze pane split positions. */
  private verticalSplitPosition = 0;
  private horizontalSplitPosition = 0;

  /** Display flags. */
  private displayGridlines = true;
  private lockFormatColumns = false;
  private lockFormatRows = false;
  private hasActions = false;

  /** Display content. The status label, or null while there is none. */
  private infoLabelValue: string | null = null;

  /** Where each sheet was last scrolled to; see {@link setVerticalScrollPositions}. */
  private verticalScrollPositions: NumberList = [];
  private horizontalScrollPositions: NumberList = [];
  /** The offsets the next render scrolls to, for a sheet shown again. */
  private pendingSheetScroll: { top: number; left: number } | null = null;
  /** The sheet the last batch showed, by name, which is unique in a workbook. */
  private shownSheetName: string | null = null;

  /**
   * Cells currently known to the client, keyed by
   * {@code toKey(col, row)}. Filled by the {@code update*CellValues}
   * RPC methods and {@code cellsUpdated}, and by a committed edit until
   * the server answers.
   */
  private cellDataByKey: Map<string, CellData> = new Map();

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
  /**
   * Server-side components the sheet renders as overlays — charts, and
   * anything else added with {@code Spreadsheet.setComponent}. Flow creates
   * the elements but leaves placing them to us; they become light-DOM
   * children of the cell-grid so the slots in its shadow root can project
   * them over the sheet.
   */
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

  /**
   * The button whose press has just closed its own popup: the click that
   * follows is not to open it again. The GWT client's popup closed the
   * same way, its auto-hide taking the press on the button.
   */
  private popupClosedByItsButton: { row: number; col: number } | null = null;
  /** The popup whose button was pressed from the keyboard, until it opens. */
  private popupOpenedFromKeyboard: { row: number; col: number } | null = null;

  /**
   * Places the open popup again when its size changes: its content is
   * Flow's components, which render after they are attached, so the size
   * the popup opens at is seldom the one it settles at.
   */
  private readonly popupResizeObserver: ResizeObserver;

  /** Whether the cell styles have changed since the last width measurement. */
  private styleWidthRatiosStale = false;
  /** The style indexes last reported to the server with a width ratio. */
  private measuredStyleIndexes: ReadonlySet<number> = new Set();

  /** The listeners on the page while the sheet is in it; see {@link connect}. */
  private pageListeners: AbortController | null = null;

  /** Decides when the server is asked for the cells the sheet scrolls to. */
  private readonly viewportLoader = new ViewportLoader({
    drawnRange: () => this.drawnRange(),
    sheet: () => ({
      rows: this.rows,
      cols: this.cols,
      frozenRows: this.verticalSplitPosition,
      frozenCols: this.horizontalSplitPosition,
      rowPx: getDefaultRowHeightPx(this.defRowHeight, FALLBACK_PPI),
      colPx: this.defColWidth
    }),
    serverBusy: () => flowRequestInFlight(this.host),
    load: (range) => this.sendLoad(range)
  });

  /**
   * The fill-handle drag in progress: the selection it started from and the
   * strip it is currently painting. Null when no drag is running.
   */
  private painting: { from: CellRect; outcome: PaintOutcome } | null = null;

  /**
   * The column-header sub-element. Sibling of the cell-grid
   * inside the scroll container; renders the column letters
   * (A, B, ..., AA) above the cells. Hidden when
   * {@link headerRowHeight} is 0, as it is while the server hides
   * the headings.
   */
  private readonly columnHeader: SpreadsheetColumnHeader;

  /**
   * The row-header sub-element. Sibling of the cell-grid
   * inside the scroll container; renders the row numbers
   * (1, 2, 3, ...) at the left of the cells. Hidden when
   * {@link headerColumnWidth} is 0.
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
   * 0.
   */
  private readonly headerCorner: SpreadsheetHeaderCorner;

  /**
   * The sheet-tab strip below the scroll container — renders
   * one tab per sheet name with the active tab highlighted.
   * Clicking a tab dispatches `sheetSelected` to the server.
   */
  private readonly sheetTabs: SpreadsheetSheetTabs;

  /**
   * Formula bar at the top of the spreadsheet — two text inputs
   * for the active cell's address + formula. Wired
   * by {@link updateFormulaBar} + {@link showSelectedCell}. The
   * documented parts {@code address-field} + {@code formula-field}
   * live on the input elements (see {@link SpreadsheetFormulaBar}).
   */
  private readonly formulaBar: SpreadsheetFormulaBar;

  /**
   * MutationObserver watching the spreadsheet host for class
   * changes: {@code Spreadsheet.HIDE_TABSHEET_STYLE}
   * ("hidetabsheet") and {@code Spreadsheet.HIDE_FUNCTION_BAR_STYLE}
   * ("hidefunctionbar") hide the matching internal elements.
   */
  private hostClassObserver: MutationObserver | null = null;

  /**
   * Height of the column-header strip in pixels, its bottom border
   * included. {@code 0} when the sheet hides its headings, which
   * collapses the strip.
   */
  private headerRowHeight = DEFAULT_HEADER_ROW_HEIGHT;

  /**
   * Width of the row-header strip in pixels, its right border included.
   * {@code 0} when the sheet hides its headings.
   */
  private headerColumnWidth = DEFAULT_HEADER_COLUMN_WIDTH;

  /**
   * The component's own root inside {@link renderTarget}.
   *
   * Everything the spreadsheet draws lives in here, and this element —
   * not the host — owns the vertical layout. The host belongs to the
   * application: writing a layout onto it means an application that
   * positions the component its own way is silently overridden.
   */
  private readonly root: HTMLElement;
  /**
   * The scroll surface — wraps the cell-grid and provides the
   * scrollbars + viewport bounds. The user scrolls THIS element;
   * the connector listens for {@code scroll} events and recomputes
   * the visible viewport. The cell-grid's intrinsic size is set to
   * the total grid dimensions so the scrollbar track reflects the
   * full scroll extent.
   */
  private readonly scrollContainer: HTMLElement;

  /**
   * The headers' and frozen panes' sizes at the last render, which the
   * scroll container leaves out of the view it pages - see
   * {@link pagedViewInset}.
   */
  private pagedViewParts = {
    corner: { width: 0, height: 0 },
    frozen: { width: 0, columns: 0, height: 0, rows: 0 }
  };

  /**
   * Resize observer on the scroll container. Fires when the host's
   * box size changes — either initially when the host is attached
   * + given dimensions, or later when the parent layout changes
   * (window resize, container reflow). Each fire triggers
   * {@link recomputeViewport}, which pushes a fresh render state
   * with the new {@code clientWidth} / {@code clientHeight}. The
   * initial fire also serves as the first render, replacing the
   * empty default state assigned in the constructor.
   */
  private readonly resizeObserver: ResizeObserver;

  constructor(host: HTMLElement, renderTarget?: HTMLElement | ShadowRoot) {
    this.host = host;
    // The wrapper (vaadin-spreadsheet.js) calls
    // `new Spreadsheet(element, element.renderRoot)` so DOM
    // goes into the shadow root, not the light DOM. Tests
    // and direct callers can omit the second arg and let
    // DOM go into the host directly.
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
    // In the top layer while open, as the GWT client's popover was: no
    // edge of the sheet or of the page around it cuts it off. Absolutely
    // placed there, so in page coordinates and moving with the page. The
    // inset, margin and overflow undo the browser's popover defaults, as
    // the GWT client's overlay did: content taller than a set height shows
    // past it rather than scrolling. Drawn by the rules the overlay carries
    // - see popupOverlayRules.
    this.popupOverlay.popover = 'manual';
    // Takes the keyboard when it opens from it without a close box.
    this.popupOverlay.tabIndex = -1;
    this.popupOverlay.style.cssText = 'position:absolute;inset:auto;margin:0;overflow:visible;box-sizing:border-box;';
    this.popupResizeObserver = new ResizeObserver(() => this.positionPopupOverlay());
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
    // its viewport edge during scroll.
    // `flex:1` so the container takes the remaining vertical
    // space inside the flex-column host, leaving room for the
    // sheet tabs below.
    // height:0 with flex:1 1 0 is what keeps the sheet from sizing the
    // component. A column flex item still offers its content height
    // when the container's own height is indefinite, and the grid
    // inside is as tall as the whole sheet, so a spreadsheet with no
    // height became thousands of pixels tall. A stated height of 0 is
    // definite, so it offers nothing, and flex-grow still gives it
    // every pixel left over once the host does have a height. The GWT
    // client got this from absolutely positioned panes.
    // A sheet smaller than its view keeps to the top left corner: left to
    // stretch, the grid shares the room left over between the header track
    // and the cells', and the cells drop away from their headers.
    this.scrollContainer.style.cssText =
      'position:relative;overflow:auto;width:100%;flex:1 1 0;height:0;min-height:0;' +
      'display:grid;grid-template-columns:auto auto;' +
      'grid-template-rows:auto auto;align-content:start;justify-content:start;' +
      // outline:none so the browser-default focus ring doesn't
      // render around the spreadsheet; we get focus via
      // tabindex=0 but draw selection feedback via
      // the .sheet-selection overlay instead.
      'outline:none;';
    // tabindex=0 makes the scroll container focusable so it can
    // receive keydown events for keyboard navigation.
    this.scrollContainer.tabIndex = 0;
    // Header corner at (col 1, row 1) — sticky on both axes
    // so it overlays both header strips at their intersection.
    // Hidden when either header dimension is 0.
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
    // Formula bar sits ABOVE the scroll container
    // in the flex column. flex:0 so it takes its intrinsic
    // height. Appended to renderTarget first so it ends up
    // ahead of the scroll container in DOM order.
    this.formulaBar.style.flex = '0 0 auto';
    this.root.appendChild(this.formulaBar);
    this.root.appendChild(this.contextMenu);
    this.formulaBar.addEventListener('sheet-address-changed', this.handleSheetAddressChanged as EventListener);
    this.formulaBar.addEventListener('formula-commit', this.handleFormulaCommit as EventListener);
    this.formulaBar.addEventListener('formula-cancel', this.handleFormulaCancel);
    this.formulaBar.addEventListener('formula-input', this.handleFormulaInput as EventListener);
    this.formulaBar.addEventListener('formula-arrow', this.handleFormulaArrow as EventListener);
    this.formulaBar.addEventListener('formula-caret', this.handleDraftCaret as EventListener);
    this.root.appendChild(this.scrollContainer);
    // Sheet tabs sit below the scroll container in the flex
    // column. flex:0 so they take their intrinsic height,
    // never grow / shrink based on content.
    this.sheetTabs.style.flex = '0 0 auto';
    this.root.appendChild(this.sheetTabs);
    // Outside the scroll container, as the GWT client's popup was outside
    // the sheet: the keys typed into a filter's fields and the clicks on
    // its check boxes are the popup's, not the sheet's shortcuts. Beside
    // the context menu, the other overlay.
    this.contextMenu.after(this.popupOverlay);
    // What a header drag shows, as the GWT client showed it - see
    // showResizeGuide. Drawn by the host's stylesheet.
    this.resizeLine = document.createElement('div');
    this.resizeLine.className = 'resize-line';
    this.resizeLine.hidden = true;
    this.resizeCover = document.createElement('div');
    this.resizeCover.className = 'resize-cover';
    this.resizeCover.hidden = true;
    // In the top layer, as the GWT client's tooltip was outside the sheet,
    // so no edge cuts it off.
    this.resizeTooltip = document.createElement('div');
    this.resizeTooltip.className = 'resize-tooltip';
    this.resizeTooltip.popover = 'manual';
    this.resizeTooltip.hidden = true;
    this.linkTooltip = document.createElement('div');
    this.linkTooltip.className = 'link-tooltip';
    this.linkTooltip.popover = 'manual';
    this.linkTooltip.hidden = true;
    this.popupOverlay.after(this.resizeCover, this.resizeLine, this.resizeTooltip, this.linkTooltip);
    this.renderTarget.appendChild(this.root);
    // Honor `Spreadsheet.HIDE_TABSHEET_STYLE` ("hidetabsheet")
    // on the wrapper host. When the host has that class, the sheet
    // tabs strip hides. Observe the host's class attribute via
    // MutationObserver — the wrapper element is light DOM, so
    // CSS in our shadow root can't `:host-context()` it.
    this.applyHostHideClasses();
    this.applyHostTheme();
    this.hostClassObserver = new MutationObserver(() => {
      this.applyHostHideClasses();
      this.applyHostTheme();
    });
    // Sheet tab click → switch active sheet via spreadsheet-event.
    this.sheetTabs.addEventListener('sheet-tab-click', this.handleSheetTabClick as EventListener);
    this.sheetTabs.addEventListener('sheet-tabs-scroll', this.handleSheetTabsScroll as EventListener);
    this.sheetTabs.addEventListener('sheet-tab-add', this.handleSheetTabAdd);
    this.sheetTabs.addEventListener('sheet-tab-rename-start', this.handleSheetRenameStart as EventListener);
    this.sheetTabs.addEventListener('sheet-tab-rename', this.handleSheetRename as EventListener);
    this.sheetTabs.addEventListener('sheet-tab-rename-cancel', this.handleSheetRenameCancel);
    this.scrollContainer.addEventListener('scroll', this.handleScroll);
    this.scrollContainer.addEventListener('focusin', this.handleSheetFocusChange);
    this.scrollContainer.addEventListener('focusout', this.handleSheetFocusChange);
    // Clipboard integration. The browser routes
    // copy/cut/paste events to the focused element when the
    // user presses Ctrl+C / Ctrl+X / Ctrl+V. The scroll
    // container is the focus host (tabindex=0).
    this.scrollContainer.addEventListener('copy', this.handleCopy as EventListener);
    this.scrollContainer.addEventListener('cut', this.handleCut as EventListener);
    this.scrollContainer.addEventListener('paste', this.handlePaste as EventListener);
    // Cell-click delegation: the cell-grid dispatches
    // a `cell-click` custom event with detail = { col, row,
    // shiftKey, ctrlKey, metaKey, altKey }. The connector updates
    // its selection + re-renders. Bubble + composed = true so the
    // event crosses the cell-grid's shadow DOM boundary.
    this.cellGrid.addEventListener('cell-click', this.handleCellClick as EventListener);
    this.contextMenu.addEventListener('context-menu-action', this.handleContextMenuAction as EventListener);
    this.contextMenu.addEventListener('context-menu-dismiss', this.handleContextMenuDismiss);
    // Cell editor lifecycle events. The cell-grid
    // emits these from its shadow DOM; the connector tracks
    // editor state + dispatches cellValueEdited on commit.
    this.cellGrid.addEventListener('cell-doubleclick', this.handleCellDoubleClick as EventListener);
    this.cellGrid.addEventListener('cell-editor-commit', this.handleCellEditorCommit as EventListener);
    this.cellGrid.addEventListener('cell-editor-cancel', this.handleCellEditorCancel as EventListener);
    this.cellGrid.addEventListener('cell-editor-input', this.handleCellEditorInput as EventListener);
    this.cellGrid.addEventListener('cell-editor-arrow', this.handleEditorArrow as EventListener);
    this.cellGrid.addEventListener('cell-editor-caret', this.handleDraftCaret as EventListener);
    // Mouse drag selection. The cell-grid emits
    // cell-mousedown + cell-mouseover; the connector watches
    // mouseup at the document level so a drag that ends
    // outside the spreadsheet still terminates cleanly.
    this.cellGrid.addEventListener('cell-mousedown', this.handleCellMouseDown as EventListener);
    this.cellGrid.addEventListener('cell-mouseover', this.handleCellMouseOver as EventListener);
    this.cellGrid.addEventListener('grid-mouseleave', this.hideLinkTooltip);
    this.cellGrid.addEventListener('selection-paint-start', this.handleSelectionPaintStart);
    // Popup-button click → dispatch popupButtonClick. The
    // server's `onPopupButtonClick` opens the associated popup
    // component.
    this.cellGrid.addEventListener('popup-button-click', this.handlePopupButtonClick as EventListener);
    // Context-menu wiring. Right-click on a cell
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
    // Grouping outline. The column header emits
    // `column-grouping-toggle` when the user clicks a +/-
    // button in the grouping gutter; the connector forwards
    // it as `groupingCollapsed` to the server.
    this.columnHeader.addEventListener('column-grouping-toggle', this.handleColumnGroupingToggle as EventListener);
    this.rowHeader.addEventListener('row-grouping-toggle', this.handleRowGroupingToggle as EventListener);
    this.rowHeader.addEventListener('row-header-contextmenu', this.handleRowHeaderContextMenu as EventListener);
    // Header-drag resize. The header elements
    // emit `column-resize-start` / `row-resize-start` from
    // their right-edge / bottom-edge mousedown handles; the
    // connector tracks the drag via document mousemove +
    // mouseup. Live-updates colWidths/rowHeights for the
    // current drag column/row.
    this.columnHeader.addEventListener('column-resize-start', this.handleColumnResizeStart as EventListener);
    this.rowHeader.addEventListener('row-resize-start', this.handleRowResizeStart as EventListener);
    this.columnHeader.addEventListener('column-autofit', this.handleColumnAutofit as EventListener);
    this.rowHeader.addEventListener('row-autofit', this.handleRowAutofit as EventListener);
    // Header click delegation. Each header element
    // emits its own custom event with the col / row coordinate;
    // the connector translates them into whole-column / whole-
    // row range selections.
    this.columnHeader.addEventListener('column-header-click', this.handleColumnHeaderClick as EventListener);
    this.rowHeader.addEventListener('row-header-click', this.handleRowHeaderClick as EventListener);
    // Keyboard navigation. Listen on the scroll
    // container so arrow keys + shift+arrow work whenever the
    // spreadsheet has focus. preventDefault() in the handler
    // stops the browser from scrolling the viewport via the
    // arrow keys.
    this.scrollContainer.addEventListener('keydown', this.handleKeyDown as EventListener);
    this.scrollContainer.addEventListener('pointerdown', this.handleHostedEditorPointerDown);
    this.resizeObserver = new ResizeObserver(() => this.recomputeViewport());
    this.connect();
  }

  /**
   * Start listening to the page and observing the host. Called as the
   * element joins the page, and again whenever it is moved: these
   * listeners hold the connector, so {@link disconnect} releases them and a
   * sheet removed from the page can be collected. Without that every
   * removed sheet stayed in memory, with its cells, and went on handling
   * every key press and mouse move in the page.
   */
  connect(): void {
    if (this.pageListeners !== null) {
      return;
    }
    this.pageListeners = new AbortController();
    const options = { signal: this.pageListeners.signal };
    document.addEventListener('pointerdown', this.handleDocumentPointerDown, options);
    document.addEventListener('keydown', this.handleContextMenuKeyDown, options);
    document.addEventListener('mouseup', this.handleDocumentMouseUp, options);
    document.addEventListener('keyup', this.handleDocumentKeyUp, options);
    document.addEventListener('mousemove', this.handleDocumentMouseMove, options);
    this.resizeObserver.observe(this.scrollContainer);
    if (this.hostClassObserver !== null) {
      this.hostClassObserver.observe(this.host, { attributes: true, attributeFilter: ['class', 'theme'] });
      // The host's classes may have changed while it was out of the page.
      this.applyHostHideClasses();
      this.applyHostTheme();
    }
  }

  /** Stop listening to the page; see {@link connect}. */
  disconnect(): void {
    this.pageListeners?.abort();
    this.pageListeners = null;
    this.resizeObserver.disconnect();
    this.popupResizeObserver.disconnect();
    this.hostClassObserver?.disconnect();
    this.viewportLoader.dispose();
  }

  /**
   * Cell-click handler. Single click = move active cell + collapse
   * selection to that cell. Shift-click = extend the active range
   * to include the new anchor while keeping the existing active
   * cell as the range origin (Excel convention). Ctrl/Meta click
   * adds the cell to the selection as a range of its own, and
   * Ctrl/Meta+Shift extends the active range keeping the others.
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
    if (this.pointedByPress) {
      // The press pointed at the cell for a formula; it selects nothing.
      this.pointedByPress = false;
      return;
    }
    const { col, row, shiftKey, ctrlKey, metaKey } = e.detail;
    const ctrlOrMeta = ctrlKey || metaKey;
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
      this.selectCellOrMerge(col, row);
    }
    this.recomputeViewport();
    this.dispatchSelectionEvent();
    // Hyperlink cells additionally dispatch `linkCellClicked`
    // after the normal selection event. The
    // server's onLinkCellClicked handler opens the URL.
    const cellKey = toKey(col, row);
    if (this.hyperlinksTooltips[cellKey] !== undefined) {
      this.sendToServer({
        type: 'linkCellClicked',
        data: { row, column: col }
      });
    }
  };

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

  /**
   * Column resize drag — start. The header reports where the column starts
   * on screen; without that, the drag is taken to start on the column's
   * right edge.
   */
  private readonly handleColumnResizeStart = (
    e: CustomEvent<{ col: number; clientX: number; clientY?: number; left?: number }>
  ): void => {
    if (!this.canResizeColumn()) {
      return;
    }
    const { col, clientX, clientY = 0, left } = e.detail;
    const edge = left ?? clientX - this.colWidthPx(col);
    this.columnResize = { col, edge, x: clientX, y: clientY };
    this.showResizeGuide();
  };

  /**
   * Row resize drag — start. Mirror of column variant.
   */
  private readonly handleRowResizeStart = (
    e: CustomEvent<{ row: number; clientX?: number; clientY: number; top?: number }>
  ): void => {
    if (!this.canResizeRow()) {
      return;
    }
    const { row, clientX = 0, clientY, top } = e.detail;
    const edge = top ?? clientY - this.rowHeightPx(row);
    this.rowResize = { row, edge, x: clientX, y: clientY };
    this.showResizeGuide();
  };

  /**
   * Document mousemove during a header-drag resize: the line
   * and the tooltip follow the pointer, and the sheet stays as it is until
   * the drag ends, as in the GWT client.
   */
  private readonly handleDocumentMouseMove = (e: MouseEvent): void => {
    const resize = this.columnResize ?? this.rowResize;
    if (resize === null) {
      return;
    }
    resize.x = e.clientX;
    resize.y = e.clientY;
    this.resizeCover.hidden = false;
    this.showResizeGuide();
  };

  /**
   * The line and the tooltip of a resize drag, as the GWT client drew them:
   * a line across the whole sheet where the edge would go - no further
   * back than where the column or row starts - and the size it would make,
   * 10px right of the pointer and 25px above it. Nothing at all is
   * "Hide column" or "Hide row"; a row's height shows in points too.
   */
  private showResizeGuide(): void {
    const origin = this.root.getBoundingClientRect();
    const line = this.resizeLine;
    let text: string;
    let x: number;
    let y: number;
    if (this.columnResize !== null) {
      ({ x, y } = this.columnResize);
      const width = Math.max(0, Math.round(x - this.columnResize.edge));
      text = width > 0 ? `Width: ${width}px` : 'Hide column';
      const at = Math.max(x, this.columnResize.edge) - 1 - origin.left - this.root.clientLeft;
      line.style.cssText = `left:${at}px;top:0;width:1px;height:100%;`;
    } else if (this.rowResize !== null) {
      ({ x, y } = this.rowResize);
      const height = Math.max(0, Math.round(y - this.rowResize.edge));
      text = height > 0 ? `Height: ${height}px ≈ ${pixelsToPoints(height)}pt` : 'Hide row';
      const at = Math.max(y, this.rowResize.edge) - 1 - origin.top - this.root.clientTop;
      line.style.cssText = `left:0;top:${at}px;width:100%;height:1px;`;
    } else {
      return;
    }
    line.hidden = false;
    this.resizeCover.classList.toggle('rows', this.rowResize !== null);
    const tooltip = this.resizeTooltip;
    tooltip.textContent = text;
    tooltip.style.left = `${x + 10}px`;
    tooltip.style.top = `${y - 25}px`;
    tooltip.hidden = false;
    showInTopLayer(tooltip);
  }

  /**
   * A link cell's tooltip - the target the server named for it - as the
   * GWT client showed it while the pointer was over the cell: below the
   * cell, or above it when there is no room below. Any other cell, or a
   * drag in progress, takes it away.
   */
  private showLinkTooltip(at: { col: number; row: number; box?: DOMRect }): void {
    const text = this.hyperlinksTooltips[toKey(at.col, at.row)];
    if (text === undefined || at.box === undefined || this.dragging || this.painting !== null) {
      this.hideLinkTooltip();
      return;
    }
    const tooltip = this.linkTooltip;
    tooltip.textContent = text;
    tooltip.hidden = false;
    showInTopLayer(tooltip);
    const { left, top } = linkTooltipPosition(at.box, tooltip.getBoundingClientRect(), {
      width: window.innerWidth,
      height: window.innerHeight
    });
    tooltip.style.left = `${left}px`;
    tooltip.style.top = `${top}px`;
  }

  private readonly hideLinkTooltip = (): void => {
    if (this.linkTooltip.hidden) {
      return;
    }
    this.linkTooltip.hidden = true;
    hideFromTopLayer(this.linkTooltip);
  };

  /** Take the line, the tooltip and the cover of a resize drag away. */
  private hideResizeGuide(): void {
    this.resizeLine.hidden = true;
    this.resizeCover.hidden = true;
    this.resizeTooltip.hidden = true;
    hideFromTopLayer(this.resizeTooltip);
  }

  /**
   * Popup-button click handler. Translates the
   * cell-grid's local event into a server-bound
   * `popupButtonClick` spreadsheet-event so the server can
   * open the popup component.
   */
  private readonly handlePopupButtonClick = (
    e: CustomEvent<{ col: number; row: number; keyboard?: boolean }>
  ): void => {
    const { col, row, keyboard } = e.detail;
    const closed = this.popupClosedByItsButton;
    this.popupClosedByItsButton = null;
    if (closed !== null && closed.row === row && closed.col === col) {
      return;
    }
    this.popupOpenedFromKeyboard = keyboard === true ? { row, col } : null;
    this.sendToServer({
      type: 'popupButtonClick',
      data: { row, column: col }
    });
  };

  /**
   * Right-click on a cell. Excel behavior: if the
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
    // Without actions there is no menu, and the GWT client left the
    // selection alone.
    if (!this.hasActions) {
      return;
    }
    // Taken for the sheet's own menu: the browser's stays closed.
    e.preventDefault();
    this.rememberContextMenuPosition(e.detail);
    if (!isCellInSelection(col, row, this.selection)) {
      this.selectCellOrMerge(col, row);
      this.recomputeViewport();
      this.dispatchSelectionEvent();
    }
    this.sendToServer({
      type: 'contextMenuOpenOnSelection',
      data: { row, column: col }
    });
  };

  /**
   * Right-click on a column header: asks the server for the
   * column's actions with {@code columnHeaderContextMenuOpen}.
   */
  private readonly handleColumnHeaderContextMenu = (
    e: CustomEvent<{ col: number; clientX?: number; clientY?: number }>
  ): void => {
    const { col } = e.detail;
    // The menu acts on the column whatever is selected, so the selection
    // stays as it is, as in the GWT client - and there is no menu without
    // actions.
    if (!this.hasActions) {
      return;
    }
    // Taken for the sheet's own menu: the browser's stays closed.
    e.preventDefault();
    this.rememberContextMenuPosition(e.detail);
    this.sendToServer({
      type: 'columnHeaderContextMenuOpen',
      data: { columnIndex: col }
    });
  };

  /**
   * Right-click on a row header. Mirror of the column
   * variant — dispatches {@code rowHeaderContextMenuOpen}.
   */
  private readonly handleRowHeaderContextMenu = (
    e: CustomEvent<{ row: number; clientX?: number; clientY?: number }>
  ): void => {
    const { row } = e.detail;
    // As for a column: the selection stays, and no actions means no menu.
    if (!this.hasActions) {
      return;
    }
    // Taken for the sheet's own menu: the browser's stays closed.
    e.preventDefault();
    this.rememberContextMenuPosition(e.detail);
    this.sendToServer({
      type: 'rowHeaderContextMenuOpen',
      data: { rowIndex: row }
    });
  };

  /**
   * Column-grouping toggle. Forwards the user's
   * +/- click in the grouping outline gutter as
   * {@code groupingCollapsed} for the server. Wire payload:
   * {@code cols: true}, {@code colIndex} carries the
   * grouping's {@code uniqueIndex} (its identity in the
   * grouping list), {@code collapsed} is the NEW state the
   * user is requesting.
   */
  private readonly handleColumnGroupingToggle = (e: CustomEvent<{ uniqueIndex: number; collapsed: boolean }>): void => {
    const { uniqueIndex, collapsed } = e.detail;
    this.sendToServer({
      type: 'groupingCollapsed',
      data: { cols: true, colIndex: uniqueIndex, collapsed }
    });
  };

  private readonly handleRowGroupingToggle = (e: CustomEvent<{ uniqueIndex: number; collapsed: boolean }>): void => {
    const { uniqueIndex, collapsed } = e.detail;
    this.sendToServer({
      type: 'groupingCollapsed',
      data: { cols: false, colIndex: uniqueIndex, collapsed }
    });
  };

  /**
   * Mouse drag — mousedown begin. Starts a drag.
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
    // A formula is being written: the press points at the cell instead of
    // selecting it, and the keyboard stays where the formula is.
    const source = this.formulaSource();
    if (source !== null) {
      e.preventDefault();
      this.pointWithMouse(col, row, shiftKey, ctrlKey || metaKey, source);
      return;
    }
    if (ctrlKey || metaKey) {
      // Ctrl/Cmd-mousedown must leave the selection untouched. The
      // click that follows adds this cell as a further range, and it
      // needs the current active range to push aside — replacing the
      // selection here would wipe exactly what the Ctrl-click is meant
      // to keep. mousedown always precedes click for a real pointer.
      // Nor does it start a drag, as in the GWT client: a pointer that
      // drifted into the next cell before the button came up replaced
      // every range picked out with Ctrl with one rectangle.
      return;
    }
    this.dragging = true;
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
      this.selectCellOrMerge(col, row);
    }
    this.recomputeViewport();
  };

  /**
   * Mouse drag — mouseover during drag. Extends
   * the range from the active cell (anchor) to the hovered
   * cell. Mouseover events that fire while NOT dragging are
   * silently ignored — the cell-grid emits on every hover.
   */
  private readonly handleCellMouseOver = (e: CustomEvent<{ col: number; row: number; box?: DOMRect }>): void => {
    this.showLinkTooltip(e.detail);
    if (this.pointerDragging && this.pointer !== null) {
      const draft = this.formulaSource() === 'field' ? this.formulaBar.formulaFieldState() : this.editorDraft();
      if (draft !== null) {
        const end = selectCellAt(e.detail.col, e.detail.row, this.mergedRegions).activeCell;
        this.pointAt(
          this.pointer.anchor,
          end,
          draft.value,
          this.pointer.span.end,
          this.pointer.span,
          this.formulaSource() ?? 'cell'
        );
        this.recomputeViewport();
      }
      return;
    }
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
    // The ranges picked out before stay: a plain press has already let
    // them go, and a Shift-press drags out the range it extends.
    const anchor = this.selection.activeCell;
    this.selection = {
      activeCell: anchor,
      activeRange: this.rangeTo(anchor, col, row),
      auxRanges: this.selection.auxRanges
    };
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
   * Mouse drag — mouseup end. Closes the drag.
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
      // End of column-resize drag: the column takes the width
      // from its left edge to where the pointer was let go, here at once
      // and then on the server - when it is a different width. A width of
      // 0 hides it, as in the GWT client.
      const { col, edge, x } = this.columnResize;
      this.columnResize = null;
      this.hideResizeGuide();
      const width = Math.max(0, Math.round(x - edge));
      if (width === this.colWidthPx(col)) {
        return;
      }
      this.colWidths = withSize(this.colWidths, col, width, this.defColWidth);
      if (width === 0 && !this.hiddenColumns.has(col)) {
        this.hiddenColumns = new Set([...this.hiddenColumns, col]);
      }
      this.recomputeViewport();
      this.sendToServer({
        type: 'columnResized',
        data: { colIndexes: [col], newWidths: [width] }
      });
      return;
    }
    if (this.rowResize !== null) {
      // End of row-resize drag. Mirror of the column's;
      // rowHeights is in points, and so is the event payload.
      const { row, edge, y } = this.rowResize;
      this.rowResize = null;
      this.hideResizeGuide();
      const height = pixelsToPoints(Math.max(0, Math.round(y - edge)));
      const idx = row - 1;
      if (height === (idx < this.rowHeights.length ? this.rowHeights[idx] : this.defRowHeight)) {
        return;
      }
      this.rowHeights = withSize(this.rowHeights, row, height, this.defRowHeight);
      if (height === 0 && !this.hiddenRows.has(row)) {
        this.hiddenRows = new Set([...this.hiddenRows, row]);
      }
      this.recomputeViewport();
      this.sendToServer({
        type: 'rowsResized',
        data: { rowIndexes: [row], newHeights: [height] }
      });
      return;
    }
    if (this.pointerDragging) {
      this.pointerDragging = false;
      return;
    }
    if (!this.dragging) {
      return;
    }
    this.dragging = false;
    if (this.selectsRange()) {
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
   * cell. The connector loads the cell's existing
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
    this.autoPercentEntry = false;
    this.showInFormulaField(initialValue);
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
    return this.lockedColumns.has(col) && this.lockedRows.has(row);
  }

  /**
   * Cell-editor commit. Called when the user presses Enter,
   * Tab, or blurs the input. Reads the typed value, dispatches
   * `cellValueEdited` to the server, closes the editor. When
   * the commit key was Enter/Tab, moves the active
   * cell in the canonical Excel direction:
   * Enter           → row + 1
   * Shift+Enter     → row - 1
   * Tab             → col + 1
   * Shift+Tab       → col - 1
   * blur (no key)   → stay at the committed cell
   */
  private readonly handleCellEditorCommit = (
    e: CustomEvent<{
      col: number;
      row: number;
      value: string;
      key?: string;
      shiftKey?: boolean;
      relatedTarget?: EventTarget | null;
    }>
  ): void => {
    const { col, row, value, key, shiftKey, relatedTarget } = e.detail;
    // Left for the formula bar, the edit carries on in the formula field, as
    // in the GWT client: nothing is committed yet, and the editor stays
    // open. Gone on to the address field instead, it ends once the keyboard
    // has settled there, as the GWT client's deferred commit did.
    if (key === undefined && relatedTarget === this.formulaBar) {
      e.preventDefault();
      setTimeout(() => {
        if (this.editor?.col === col && this.editor.row === row && !this.formulaBar.formulaFieldHasFocus()) {
          this.commitCellEdit(col, row, this.cellGrid.editorValue() ?? value, undefined, false, false);
        }
      });
      return;
    }
    this.commitCellEdit(col, row, value, key, shiftKey === true, true);
  };

  /**
   * Commit an edit made in the cell editor: report the value, close the
   * editor and move on the way the key that ended the edit says - see
   * handleCellEditorCommit. The keyboard goes back to the sheet unless the
   * user has already put it somewhere.
   */
  private commitCellEdit(
    col: number,
    row: number,
    value: string,
    key: string | undefined,
    shiftKey: boolean,
    takeKeyboard: boolean
  ): void {
    this.showCommittedValue(col, row, value);
    // Close the editor first so the re-render removes the
    // input — important to do BEFORE dispatching the server
    // event so the editor doesn't briefly flash on top of the
    // server's response.
    this.editor = null;
    this.endFormulaPointing();
    if (takeKeyboard) {
      this.returnFocusToSheet();
    }
    // Move the active cell to the next slot per Excel
    // convention. For 'Enter' / 'Tab' the move
    // direction is fixed; blur leaves the selection at the
    // committed cell. Clamped to grid bounds.
    // Enter moves down, Tab right, shift reverses each; an arrow key
    // moves the way it points, which is what committing from Excel's
    // "enter" editing mode does.
    //
    // With a range selected the commit walks inside it, the same way
    // Enter and Tab do when nothing is being edited: filling a block
    // cell by cell is the reason the range is selected at all.
    const walkDirection = inSelectionWalkDirection(key, shiftKey);
    const walks = walkDirection !== null && this.rows > 0 && this.cols > 0 && this.selectsRange();
    // Moving on goes by the arrows' rules: over hidden rows and columns,
    // and out of a merge on its far side.
    const direction = walks || key === undefined ? null : keyDirection(key, shiftKey);
    const next =
      direction !== null && this.rows > 0 && this.cols > 0
        ? moveActiveCell({ col, row }, this.mergeEntry, direction, this.sheetShape())
        : null;
    if (next !== null) {
      this.applySingleSelection(next);
    }
    if (!walks) {
      this.recomputeViewport();
    }
    const data: CellValueEditedData = { row, col, value };
    this.sendToServer({ type: 'cellValueEdited', data });
    // If we moved the active cell, also dispatch the
    // selection event so the server stays in sync: every
    // selection write sends an event.
    if (next !== null) {
      this.dispatchSelectionEvent();
    }
    if (walks) {
      this.moveActiveCellInsideSelection(
        walkInsideSelection({
          range: this.selection.activeRange,
          col,
          row,
          direction: walkDirection!,
          hiddenColumns: this.hiddenColumns,
          hiddenRows: this.hiddenRows
        })
      );
    }
  }

  /**
   * Cell-editor cancel. User pressed Escape. Close
   * the editor without dispatching anything.
   */
  private readonly handleCellEditorCancel = (): void => {
    const editor = this.editor;
    this.editor = null;
    this.endFormulaPointing();
    // The formula field showed what was typed; it shows the cell again.
    if (editor !== null) {
      this.showInFormulaField(this.formulaFieldValueFor(editor.col, editor.row));
    }
    this.returnFocusToSheet();
    this.recomputeViewport();
  };

  /** Typed into the cell editor: the formula field shows it as it is typed. */
  private readonly handleCellEditorInput = (e: CustomEvent<{ value: string; caret?: number }>): void => {
    this.showInFormulaField(e.detail.value);
    // Typing ends pointing at that reference: the next arrow after an
    // operator points at a new one, from the cell pointed at last.
    this.pointer = null;
    this.updateFormulaDraft(e.detail.value, e.detail.caret ?? e.detail.value.length);
  };

  /**
   * Typed into the formula field while the edit started in the cell: the
   * cell editor shows it as it is typed, as the GWT client's did.
   */
  private readonly handleFormulaInput = (e: CustomEvent<{ value: string; caret?: number }>): void => {
    if (this.editor !== null) {
      this.cellGrid.setEditorValue(e.detail.value);
    }
    this.pointer = null;
    this.updateFormulaDraft(e.detail.value, e.detail.caret ?? e.detail.value.length);
  };

  /** The caret moved in the formula being written, or the edit just began. */
  private readonly handleDraftCaret = (e: CustomEvent<{ value: string; caret: number }>): void => {
    if (this.editor === null && !this.formulaBar.formulaFieldHasFocus()) {
      // From an editor on its way out - the edit is over.
      return;
    }
    const { value, caret } = e.detail;
    if (this.pointer !== null && caret !== this.pointer.span.end) {
      // Moved away from what was pointed at: the next point starts afresh.
      this.pointer = null;
    }
    this.updateFormulaDraft(value, caret);
  };

  /** An arrow in the cell editor: pointing at a cell, when a formula takes one there. */
  private readonly handleEditorArrow = (
    e: CustomEvent<{ key: string; shiftKey: boolean; value: string; caret: number }>
  ): void => {
    this.pointWithArrow(e, 'cell');
  };

  /** An arrow in the formula field: pointing, as in the cell editor. */
  private readonly handleFormulaArrow = (
    e: CustomEvent<{ key: string; shiftKey: boolean; value: string; caret: number }>
  ): void => {
    this.pointWithArrow(e, 'field');
  };

  /**
   * Point at a cell with an arrow while a formula is written, as the GWT
   * client let the user: after an operator, a bracket or a separator the
   * arrow puts a reference to the next cell over into the formula, and
   * further arrows move it on - from the cell being edited at first, then
   * from the cell pointed at last, even past an operator typed since.
   * Shift+arrow stretches it into a range from where it started. The
   * selection stays on the cell being edited.
   */
  private pointWithArrow(
    e: CustomEvent<{ key: string; shiftKey: boolean; value: string; caret: number }>,
    source: 'cell' | 'field'
  ): void {
    const { key, shiftKey, value, caret } = e.detail;
    if (!isFormula(value) || this.rows <= 0 || this.cols <= 0) {
      return;
    }
    const pointer = this.pointer !== null && caret === this.pointer.span.end ? this.pointer : null;
    if (pointer === null && !canPoint(value, caret)) {
      return;
    }
    e.preventDefault();
    const from = pointer?.end ?? this.lastPointed ?? this.formulaCell();
    const next = moveActiveCell(from, null, keyDirection(key, false)!, this.sheetShape());
    const end = next?.activeCell ?? from;
    const anchor = shiftKey && pointer !== null ? pointer.anchor : end;
    this.pointAt(anchor, end, value, caret, pointer?.span ?? null, source);
    this.scrollCellIntoView(end);
    this.recomputeViewport();
  }

  /**
   * Point at a cell with the mouse while a formula is written: a click puts
   * a reference to it into the formula - in place of the reference pointed
   * at a moment ago, or the one the caret is in, as the GWT client meant
   * to - a drag stretches it into a range, Shift stretches the last one to
   * the cell, and Ctrl or Cmd adds another after a comma.
   */
  private pointWithMouse(col: number, row: number, shiftKey: boolean, add: boolean, source: 'cell' | 'field'): void {
    const draft = source === 'field' ? this.formulaBar.formulaFieldState() : this.editorDraft();
    if (draft === null) {
      return;
    }
    let { value, caret } = draft;
    const cell = selectCellAt(col, row, this.mergedRegions).activeCell;
    const pointer = this.pointer !== null && caret === this.pointer.span.end ? this.pointer : null;
    let span: { start: number; end: number } | null;
    let anchor = cell;
    if (add) {
      const at = pointer?.span.end ?? caret;
      const comma = placeReference(value, at, null, ',');
      value = comma.text;
      caret = comma.end;
      span = null;
    } else if (pointer !== null) {
      span = pointer.span;
      anchor = shiftKey ? pointer.anchor : cell;
    } else {
      const inRef = canPoint(value, caret)
        ? null
        : referenceAt(formulaReferences(value, this.activeSheetName()), caret);
      span = inRef === null ? null : { start: inRef.start, end: inRef.end };
      if (shiftKey && inRef !== null) {
        anchor = { col: inRef.range.c1, row: inRef.range.r1 };
      }
    }
    this.pointAt(anchor, cell, value, caret, span, source);
    this.pointerDragging = true;
    this.pointedByPress = true;
    this.recomputeViewport();
  }

  /** Puts the reference to a pointed cell or range into the formula being written. */
  private pointAt(
    anchor: { col: number; row: number },
    end: { col: number; row: number },
    value: string,
    caret: number,
    span: { start: number; end: number } | null,
    source: 'cell' | 'field'
  ): void {
    if (this.autoPercentEntry && value.endsWith('%')) {
      // The % a sign typed into a percentage cell was given belongs to a
      // number. Pointing makes the entry a formula, where "+A1%" would take
      // a hundredth of A1: a GWT bug.
      value = value.slice(0, -1);
      caret = Math.min(caret, value.length);
    }
    this.autoPercentEntry = false;
    const placed = placeReference(value, caret, span, referenceText(anchor, end));
    this.pointer = { anchor, end, span: { start: placed.start, end: placed.end } };
    this.lastPointed = end;
    if (this.editor !== null) {
      this.cellGrid.setEditorValue(placed.text, source === 'cell' ? placed.end : undefined);
    }
    if (source === 'field') {
      this.formulaBar.setFormulaFieldValue(placed.text, placed.end);
    }
    this.showInFormulaField(placed.text);
    this.updateFormulaDraft(placed.text, placed.end);
  }

  /** Where a formula is being written now: the formula field, the cell editor, or nowhere. */
  private formulaSource(): 'cell' | 'field' | null {
    if (this.formulaBar.formulaFieldHasFocus()) {
      const field = this.formulaBar.formulaFieldState();
      return field !== null && isFormula(field.value) ? 'field' : null;
    }
    const draft = this.editorDraft();
    return draft !== null && isFormula(draft.value) ? 'cell' : null;
  }

  /** What the cell editor holds and where its caret is, while it is open. */
  private editorDraft(): { value: string; caret: number } | null {
    if (this.editor === null) {
      return null;
    }
    const input = this.cellGrid.shadowRoot?.querySelector<HTMLInputElement>('.cell-editor') ?? null;
    if (input === null) {
      return null;
    }
    return { value: input.value, caret: input.selectionStart ?? input.value.length };
  }

  /** The cell the formula being written is for. */
  private formulaCell(): { col: number; row: number } {
    return this.editor !== null ? { col: this.editor.col, row: this.editor.row } : this.selection.activeCell;
  }

  /** The name of the sheet shown. */
  private activeSheetName(): string {
    return this.sheetNames[this.sheetIndex - 1] ?? '';
  }

  /** Colours the references of the formula being written, or none when it is not one. */
  private updateFormulaDraft(text: string | null, caret: number): void {
    const draft = text !== null && isFormula(text) ? { text, caret } : null;
    const before = this.formulaDraft;
    if (draft === null && before === null) {
      return;
    }
    if (draft !== null && before !== null && draft.text === before.text && draft.caret === before.caret) {
      return;
    }
    this.formulaDraft = draft;
    const refs = draft === null ? [] : formulaReferences(draft.text, this.activeSheetName());
    this.formulaBar.renderState = {
      ...this.formulaBar.renderState,
      highlights:
        draft === null
          ? null
          : { text: draft.text, references: refs.map((ref) => ({ start: ref.start, end: ref.end, color: ref.color })) }
    };
    this.recomputeViewport();
  }

  /** The formula is written or thrown away: nothing is pointed at or coloured any more. */
  private endFormulaPointing(): void {
    this.pointer = null;
    this.lastPointed = null;
    this.pointerDragging = false;
    this.updateFormulaDraft(null, 0);
  }

  /** The references of the formula being written, as the cell grid colours them. */
  private formulaReferencesToDraw(): Array<{ range: CellRange; color: string; atCaret: boolean }> {
    const draft = this.formulaDraft;
    if (draft === null) {
      return [];
    }
    const refs = formulaReferences(draft.text, this.activeSheetName());
    const atCaret = referenceAt(refs, draft.caret);
    return refs.map((ref) => ({ range: ref.range, color: ref.color, atCaret: ref === atCaret }));
  }

  /** What the formula field shows - the cell editor's value, while it is open. */
  private showInFormulaField(value: string): void {
    this.formulaBar.renderState = { ...this.formulaBar.renderState, formula: value };
  }

  /**
   * Show a committed value in its cell at once. The server's
   * `cellsUpdated` callback may arrive much later (or not at all in offline
   * / TestBench scenarios); the GWT client updated the cell before the
   * round-trip, as Excel users expect - except for a formula, which kept
   * showing the old value until the server had worked it out. Preserves
   * the existing CellData metadata when present (style index, locked flag).
   */
  private showCommittedValue(col: number, row: number, value: string): void {
    if (value.startsWith('=') || value.startsWith('+')) {
      return;
    }
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
  }

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
   * Column-header click handler. Selects the entire
   * column (rows 1..this.rows). The active cell becomes the
   * column's row 1. Shift-click extends from the existing anchor
   * column to the clicked column, selecting a whole-column
   * range.
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
    if ((ctrlKey || metaKey) && !shiftKey) {
      // The whole column joins the selection as a further range.
      this.selection = addAuxRange(this.selection, col, 1, col, rows);
      this.recomputeViewport();
      const data: ColumnSelectedData = { column: col, firstRowIndex: 1 };
      this.sendToServer({ type: 'columnAddedToSelection', data });
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
   * Row-header click handler. Mirror of the column
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
    if ((ctrlKey || metaKey) && !shiftKey) {
      // The whole row joins the selection as a further range.
      this.selection = addAuxRange(this.selection, 1, row, cols, row);
      this.recomputeViewport();
      const data: RowSelectedData = { row, firstColumnIndex: 1 };
      this.sendToServer({ type: 'rowAddedToRangeSelection', data });
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
   * Keyboard navigation. Arrow keys, Tab and Enter move the active cell
   * over hidden rows and columns and out of merges; Shift with an arrow
   * grows the range from the active cell, which stays where it is, as in
   * Excel. Home and End go along the row, or with Ctrl to the sheet's first
   * and last cell; typing starts an edit.
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
    // The key that holds a shortcut - select all, undo, redo: Cmd on a Mac,
    // where Ctrl+A and Ctrl+Z did nothing in the GWT client, and Ctrl or
    // Cmd elsewhere.
    const shortcut = isMacPlatform() ? e.metaKey : ctrlOrMeta;

    // A cell showing a custom editor is edited through that editor, so
    // every key that would otherwise start editing hands it the focus
    // instead: F2, Enter, and any printable character.
    //
    // Deliberately without preventDefault. The character that opened the
    // editor should land in it, and the browser delivers a keypress to
    // whatever holds the focus once the handlers have run — which by then
    // is the editor.
    const startsEditing =
      !this.editor &&
      (((key === 'F2' || key === 'Enter') && !ctrlOrMeta && !e.altKey) || typesCharacter(e, isMacPlatform()));
    if (startsEditing && this.focusCustomEditor()) {
      return;
    }

    // Ctrl+A / Cmd+A — select the entire sheet.
    if (shortcut && (key === 'a' || key === 'A')) {
      if (this.rows <= 0 || this.cols <= 0) {
        return;
      }
      e.preventDefault();
      this.selection = {
        activeCell: { col: 1, row: 1 },
        activeRange: normalizedRange(1, 1, this.cols, this.rows),
        auxRanges: []
      };
      this.recomputeViewport();
      this.dispatchSelectionEvent();
      return;
    }

    // Ctrl+Z / Cmd+Z — undo. Pure pass-through to the server;
    // the server owns the undo stack + replies with the
    // resulting cell updates.
    if (shortcut && (key === 'z' || key === 'Z') && !e.shiftKey) {
      e.preventDefault();
      this.sendToServer({ type: 'onUndo', data: {} });
      return;
    }

    // Ctrl+Y / Cmd+Shift+Z — redo. Cmd+Y is reserved on Mac
    // for "history menu" by some apps, so we also accept
    // Cmd+Shift+Z (the Mac convention) + Ctrl+Y (Windows).
    if (shortcut && (key === 'y' || key === 'Y' || ((key === 'z' || key === 'Z') && e.shiftKey))) {
      e.preventDefault();
      this.sendToServer({ type: 'onRedo', data: {} });
      return;
    }

    // F2 opens the cell editor at the active cell, loaded with the
    // cell's existing value (Excel convention).
    //
    // So does Enter on a single cell, as it always has: the GWT client
    // treated the two keys as one case. With a range selected Enter
    // walks inside it instead, and on a cell the user cannot see there
    // is nothing to edit, so it falls through to moving on.
    const entersEditor =
      key === 'F2' ||
      (key === 'Enter' &&
        !this.selectsRange() &&
        !this.hiddenRows.has(this.selection.activeCell.row) &&
        !this.hiddenColumns.has(this.selection.activeCell.col));
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
      this.autoPercentEntry = false;
      this.showInFormulaField(this.editor.initialValue);
      this.recomputeViewport();
      return;
    }

    // Type-to-edit: a key that types a character opens the cell editor at
    // the active cell with that character as the initial value - Excel's
    // way: typing directly starts replacing the cell's content. A
    // character typed with AltGr or Option counts; see typesCharacter.
    if (!this.editor && typesCharacter(e, isMacPlatform())) {
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
      // Space opens it empty, as the GWT client did: it took Space on
      // keydown, as a key that starts an edit, and typed nothing.
      this.editor = {
        col,
        row,
        initialValue: key === ' ' ? '' : initialEditValue(key, this.cellDataByKey.get(toKey(col, row))),
        mode: 'enter'
      };
      this.autoPercentEntry = key !== ' ' && this.editor.initialValue !== key;
      this.showInFormulaField(this.editor.initialValue);
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
      this.sendToServer({ type: 'deleteSelectedCells', data: {} });
      return;
    }
    // With a range selected, Enter and Tab walk the active cell around
    // inside it instead of moving on and dropping it: Enter down the
    // column and on to the next, Tab along the row and on to the next,
    // both wrapping at the end, shift reversing. The range stays, so the
    // server hears a selection that did not discard its old range.
    const walkDirection = inSelectionWalkDirection(key, e.shiftKey);
    if (walkDirection !== null && this.rows > 0 && this.cols > 0 && this.selectsRange()) {
      e.preventDefault();
      const next = walkInsideSelection({
        range: this.selection.activeRange,
        col: this.selection.activeCell.col,
        row: this.selection.activeCell.row,
        direction: walkDirection,
        hiddenColumns: this.hiddenColumns,
        hiddenRows: this.hiddenRows
      });
      this.moveActiveCellInsideSelection(next);
      return;
    }

    // The arrows - and Tab and Enter, outside a range - step the way the
    // GWT client's did: over hidden rows and columns, onto a merge as a
    // whole and out of it on the far side, and not at all at the edge of
    // the sheet. Shift+arrow moves the side of the range away from the
    // active cell instead.
    const direction = keyDirection(key, e.shiftKey);
    if (direction !== null) {
      if (this.rows <= 0 || this.cols <= 0) {
        return;
      }
      e.preventDefault();
      if (e.shiftKey && key.startsWith('Arrow')) {
        const grown = moveRangeEdge(
          this.selection.activeRange,
          this.selection.activeCell,
          direction,
          this.sheetShape()
        );
        if (grown === null) {
          return;
        }
        this.selection = { activeCell: this.selection.activeCell, activeRange: grown.range, auxRanges: [] };
        this.extendingSelection = true;
        this.showRangeSizeInAddressField();
        this.scrollCellIntoView(grown.moved);
      } else {
        const next = moveActiveCell(this.selection.activeCell, this.mergeEntry, direction, this.sheetShape());
        if (next === null) {
          return;
        }
        this.moveSelectionTo(next);
      }
      this.recomputeViewport();
      this.dispatchSelectionEvent();
      return;
    }

    // Home / End go to the first / last column of the row, Ctrl+Home to
    // A1 and Ctrl+End to the last cell.
    const target = homeEndTarget(key, e.ctrlKey || e.metaKey, this.selection.activeCell, this.cols, this.rows);
    if (!target) {
      return;
    }
    if (this.rows <= 0 || this.cols <= 0) {
      return;
    }
    e.preventDefault();
    this.moveSelectionTo(selectCellAt(target.col, target.row, this.mergedRegions));
    this.recomputeViewport();
    this.dispatchSelectionEvent();
  };

  /**
   * Move the selection to one cell - or merge - from the keyboard, naming
   * it in the address field straight away rather than waiting for the
   * server to: the address field is how the user knows where the keyboard
   * has taken them. The cell is scrolled into view before the render, by
   * the GWT client's rule.
   */
  private moveSelectionTo(next: SingleCellSelection): void {
    this.extendingSelection = false;
    this.applySingleSelection(next);
    this.formulaBar.renderState = {
      ...this.formulaBar.renderState,
      address: cellCoordToAddress(next.activeCell)
    };
    this.scrollCellIntoView(next.activeCell);
  }

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
    this.scrollCellIntoView(next);
    this.recomputeViewport();
    const data: CellSelectedData = {
      row: next.row,
      column: next.col,
      oldSelectionRangeDiscarded: false
    };
    this.sendToServer({ type: 'cellSelected', data });
  }

  /**
   * Adjust {@code scrollContainer.scrollLeft / scrollTop} so the
   * given cell (either the new active cell or the extend cell
   * after shift+arrow) is in full view, by the GWT client's rule -
   * see {@link scrollToShow}. Sets scrollLeft/Top in-place; the
   * existing `scroll` listener triggers a fresh recomputeViewport.
   */
  private scrollCellIntoView(target: { col: number; row: number }): void {
    if (this.rows <= 0 || this.cols <= 0) {
      return;
    }
    const sc = this.scrollContainer;
    // The cells' part of the view: the header strips stick to its top and
    // left edges, and the cell grid starts where they end.
    const viewWidth = sc.clientWidth - this.cellGrid.offsetLeft;
    const viewHeight = sc.clientHeight - this.cellGrid.offsetTop;
    const left = scrollToShow(target.col, this.horizontalSplitPosition, this.cols, sc.scrollLeft, viewWidth, (c) =>
      this.colWidthPx(c)
    );
    const top = scrollToShow(target.row, this.verticalSplitPosition, this.rows, sc.scrollTop, viewHeight, (r) =>
      this.rowHeightPx(r)
    );
    if (left !== sc.scrollLeft) {
      sc.scrollLeft = left;
    }
    if (top !== sc.scrollTop) {
      sc.scrollTop = top;
    }
  }

  /**
   * Copy handler. Serializes the current selection
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
   * Cut handler. Same as copy + dispatches
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
    this.sendToServer({ type: 'clearSelectedCellsOnCut', data: {} });
  };

  /**
   * Paste handler. Reads the clipboard's text/plain
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
    this.sendToServer({ type: 'onPaste', data: { text } });
  };

  /**
   * Serialize the active range's cell values to TSV (tab-
   * separated values). Returns null when the selection's range
   * is invalid or no cells exist. Uses
   * {@link cellDataByKey} as the source of truth — cells
   * without data are emitted as empty strings.
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
   * Scroll-event handler: tell the loader where the sheet is, recompute
   * the visible viewport and push a fresh render state to the cell-grid.
   * The loader decides whether the server needs asking for cells now - see
   * {@link ViewportLoader}.
   */
  private readonly handleScroll = (): void => {
    this.viewportLoader.scrolled(this.scrollContainer.scrollTop, this.scrollContainer.scrollLeft);
    this.recomputeViewport();
    // The GWT client moved an open popup along with its cell.
    this.positionPopupOverlay();
  };

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
   * Loading itself is checked again on every repaint, not only on
   * scrolling: plenty of things resize the sheet without a scroll - a tab
   * revealing it, a split pane opening, the window resizing. A sheet that
   * came up 0 wide inside a hidden tab would otherwise keep the viewport it
   * measured while hidden, and the cells and popup buttons outside it
   * would never load. The {@link ViewportLoader} asks only for what is not
   * loaded yet.
   */
  private announceViewport(): void {
    if (this.rows <= 0 || this.cols <= 0) {
      // The sheet's size has not arrived yet. Announcing now would report a
      // viewport that means nothing and the server would load nothing.
      return;
    }
    if (this.drawnRange() === null) {
      // Nothing measured yet; try again after the next render.
      return;
    }
    if (!this.viewportAnnounced) {
      this.viewportAnnounced = true;
      this.sendToServer({ type: 'onConnectorInit', data: {} });
    }
    this.viewportLoader.check();
  }

  /**
   * Send an event to the server. Dispatched on the host alone, where Flow
   * listens: a bubbling event also reached the server component of a
   * spreadsheet this one sits in - as popup content, a custom editor or a
   * cell's component - which applied it to its own cells.
   */
  private sendToServer(detail: { type: string; data: unknown }): void {
    this.host.dispatchEvent(new CustomEvent('spreadsheet-event', { detail }));
  }

  /** The cells the grid draws, its drawing buffer included; null before anything is measured. */
  private drawnRange(): LoadRange | null {
    const viewport = this.cellGrid.renderState.viewport;
    if (viewport.lastRowIndex < viewport.firstRowIndex || viewport.lastColumnIndex < viewport.firstColumnIndex) {
      return null;
    }
    return {
      r1: viewport.firstRowIndex,
      c1: viewport.firstColumnIndex,
      r2: viewport.lastRowIndex,
      c2: viewport.lastColumnIndex
    };
  }

  /**
   * Ask the server to load the cells, comments, overlays and components of
   * a range. Its `onSheetScroll` loads what it has not sent before.
   */
  private sendLoad(range: LoadRange): void {
    this.sendToServer({
      type: 'onSheetScroll',
      data: {
        firstRow: range.r1,
        firstColumn: range.c1,
        lastRow: range.r2,
        lastColumn: range.c2
      }
    });
  }

  /**
   * Request a repaint once the current task is done, coalescing repeated
   * calls into a single {@link recomputeViewport}.
   *
   * The server pushes cell values, styles and reload signals as several
   * separate RPC calls within one roundtrip. Painting on each one would
   * re-render the whole viewport many times over for what the user sees
   * as a single update, so the calls collapse into one repaint.
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
   * Repaint now, dropping any repaint already queued.
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
    // Every style a cell wears, not only those the style map holds: a style
    // set on a cell straight through POI never reaches the map, and without
    // a ratio the server judges every value in it as fitting. Unstyled, it
    // measures as the default does.
    const styleIndexes = [
      ...new Set([...Object.keys(this.cellStyleToCSSStyle).map(Number), ...this.styleIndexesInUse()])
    ]
      .filter((index) => Number.isFinite(index))
      .sort((a, b) => a - b);
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

    this.measuredStyleIndexes = new Set(styleIndexes);
    this.sendToServer({
      type: 'setCellStyleWidthRatios',
      data: { styleIndexes, ratios }
    });
  }

  /** The style indexes the cells loaded so far wear, from their cs<N> class. */
  private styleIndexesInUse(): Set<number> {
    const indexes = new Set<number>();
    for (const cell of this.cellDataByKey.values()) {
      const index = styleIndexOf(cell.cellStyle);
      if (index !== null) {
        indexes.add(index);
      }
    }
    return indexes;
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
  recomputeViewport(): void {
    if (this.pendingSheetScroll !== null) {
      // A sheet shown again goes back to where it was scrolled. The grid is
      // sized for it first: against the last sheet's size the browser would
      // cut the offsets short.
      const { top, left } = this.pendingSheetScroll;
      this.pendingSheetScroll = null;
      this.applyRenderState({
        scrollLeft: 0,
        scrollTop: 0,
        viewportWidth: this.scrollContainer.clientWidth,
        viewportHeight: this.scrollContainer.clientHeight
      });
      this.scrollContainer.scrollTop = top;
      this.scrollContainer.scrollLeft = left;
    }
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
   * Stop listening for good: the page listeners and observers that
   * {@link disconnect} releases, and the listeners on the sheet's own
   * elements. The element only disconnects on detach, since it may be
   * attached again; tests dispose of the sheets they make.
   */
  dispose(): void {
    this.renderQueued = false;
    this.scrollContainer.removeEventListener('scroll', this.handleScroll);
    this.scrollContainer.removeEventListener('focusin', this.handleSheetFocusChange);
    this.scrollContainer.removeEventListener('focusout', this.handleSheetFocusChange);
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
    this.contextMenu.removeEventListener('context-menu-dismiss', this.handleContextMenuDismiss);
    this.cellGrid.removeEventListener('cell-doubleclick', this.handleCellDoubleClick as EventListener);
    this.cellGrid.removeEventListener('cell-editor-commit', this.handleCellEditorCommit as EventListener);
    this.cellGrid.removeEventListener('cell-editor-cancel', this.handleCellEditorCancel as EventListener);
    this.cellGrid.removeEventListener('cell-editor-input', this.handleCellEditorInput as EventListener);
    this.cellGrid.removeEventListener('cell-editor-arrow', this.handleEditorArrow as EventListener);
    this.cellGrid.removeEventListener('cell-editor-caret', this.handleDraftCaret as EventListener);
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
    this.cellGrid.removeEventListener('grid-mouseleave', this.hideLinkTooltip);
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
    this.columnHeader.removeEventListener('column-resize-start', this.handleColumnResizeStart as EventListener);
    this.rowHeader.removeEventListener('row-resize-start', this.handleRowResizeStart as EventListener);
    this.columnHeader.removeEventListener('column-autofit', this.handleColumnAutofit as EventListener);
    this.rowHeader.removeEventListener('row-autofit', this.handleRowAutofit as EventListener);
    this.disconnect();
    hideFromTopLayer(this.popupOverlay);
    this.hostClassObserver = null;
    this.formulaBar.removeEventListener('sheet-address-changed', this.handleSheetAddressChanged as EventListener);
    this.formulaBar.removeEventListener('formula-commit', this.handleFormulaCommit as EventListener);
    this.formulaBar.removeEventListener('formula-input', this.handleFormulaInput as EventListener);
    this.formulaBar.removeEventListener('formula-arrow', this.handleFormulaArrow as EventListener);
    this.formulaBar.removeEventListener('formula-caret', this.handleDraftCaret as EventListener);
    this.formulaBar.removeEventListener('formula-cancel', this.handleFormulaCancel);
  }

  /**
   * Read the spreadsheet host's class list + apply
   * the two public "hide bar" classes by toggling display on
   * the matching internal elements. The constants
   * {@link Spreadsheet#HIDE_FUNCTION_BAR_STYLE} ("hidefunctionbar")
   * and {@link Spreadsheet#HIDE_TABSHEET_STYLE} ("hidetabsheet")
   * are public JavaDoc-documented API; consumers call
   * {@code addClassName(HIDE_TABSHEET_STYLE)} on the host
   * (or use {@code setSheetSelectionBarVisible(false)} which
   * does the same) to suppress the corresponding strip.
   */
  private applyHostHideClasses(): void {
    const tabsHidden = this.host.classList.contains('hidetabsheet');
    this.sheetTabs.style.display = tabsHidden ? 'none' : '';
    const barHidden = this.host.classList.contains('hidefunctionbar');
    this.formulaBar.style.display = barHidden ? 'none' : '';
    this.applyPagedViewInset();
  }

  /**
   * Leaves the headers and frozen panes out of the view the browser pages,
   * so Page Up and Page Down move the cells as far as the GWT client's did.
   * Paging the whole scroll container, they moved them a header further.
   */
  private applyPagedViewInset(): void {
    const { corner, frozen } = this.pagedViewParts;
    const inset = pagedViewInset(corner, frozen, this.sheetTabs.style.display !== 'none');
    this.scrollContainer.style.scrollPaddingTop = `${inset.top}px`;
    this.scrollContainer.style.scrollPaddingLeft = `${inset.left}px`;
  }

  /**
   * The host's theme variant, on every internal element as well.
   *
   * `Spreadsheet.setTheme(SpreadsheetTheme.LUMO)` sets `theme="lumo"` on
   * the host. Each internal element draws in its own shadow root, where no
   * selector can see the host's attributes, so each carries the attribute
   * itself and draws its theme under `:host([theme~='lumo'])`.
   */
  private applyHostTheme(): void {
    const theme = this.host.getAttribute('theme');
    const themed = [
      this.formulaBar,
      this.contextMenu,
      this.headerCorner,
      this.columnHeader,
      this.rowHeader,
      this.cellGrid,
      this.sheetTabs
    ];
    for (const element of themed) {
      if (theme === null) {
        element.removeAttribute('theme');
      } else if (element.getAttribute('theme') !== theme) {
        element.setAttribute('theme', theme);
      }
    }
  }

  /**
   * Dispatched when the user presses Enter in the
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
    this.sendToServer({
      type: 'sheetAddressChanged',
      data: { value }
    });
  };

  /** Test-only accessor: the component's own root inside the host. */
  getRoot(): HTMLElement {
    return this.root;
  }

  /**
   * Tells the cell grid whether the sheet has the keyboard, which it shows
   * by dimming the selection when it does not - as the GWT client did, so
   * a sheet left for another field does not look like it still takes the
   * typing. Read once the focus has settled: moving between the sheet and
   * its own cell editor fires a focusout and a focusin, and only the
   * outcome matters.
   */
  private readonly handleSheetFocusChange = (): void => {
    queueMicrotask(() => {
      this.cellGrid.toggleAttribute('sheet-focused', this.scrollContainer.matches(':focus-within'));
    });
  };

  /** Test-only accessor: the scroll container element. */
  getScrollContainer(): HTMLElement {
    return this.scrollContainer;
  }

  /** Test-only accessor: the overlay an open popup button's content is in. */
  getPopupOverlay(): HTMLElement {
    return this.popupOverlay;
  }

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
    this.sendToServer({ type: 'cellAddedToSelectionAndSelected', data });
  }

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

  /** Tell the server a whole range joined a non-coherent selection. */
  private dispatchCellsAddedToRangeSelection(range: CellRange): void {
    const data: CellRangeSelectedData = {
      row1: range.r1,
      col1: range.c1,
      row2: range.r2,
      col2: range.c2
    };
    this.sendToServer({ type: 'cellsAddedToRangeSelection', data });
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
    this.sendToServer({ type: 'onColumnAutofit', data: { columnIndex: e.detail.col } });
  };

  /** Double-click on a row's resize handle — size it to its tallest value. */
  private readonly handleRowAutofit = (e: CustomEvent<{ row: number }>): void => {
    if (!this.canResizeRow()) {
      return;
    }
    this.sendToServer({ type: 'onRowAutofit', data: { rowIndex: e.detail.row } });
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
    this.sendToServer({ type: 'protectedCellWriteAttempted', data: {} });
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
      this.sendToServer({ type: 'selectionDecreasePainted', data: { row: r1, col: c1 } });
      return;
    }

    const grown = grownSelection(painting.from, painting.outcome.painted);
    if (!isNonEmpty(grown)) {
      return;
    }
    this.sendToServer({
      type: 'selectionIncreasePainted',
      data: { r1: grown.r1, c1: grown.c1, r2: grown.r2, c2: grown.c2 }
    });
  }

  /**
   * Dispatch a {@code spreadsheet-event} on the host carrying
   * the appropriate server-bound event for the current selection.
   * Picks one of {@code cellSelected /
   * cellRangeSelected / columnSelected / rowSelected} based on
   * the shape of {@code this.selection.activeRange}:
   *
   *  - whole column (r1=1, r2=rows, single col) → columnSelected
   *  - whole row    (c1=1, c2=cols, single row) → rowSelected
   *  - single cell  (c1=c2, r1=r2)              → cellSelected
   *  - otherwise                                → cellRangeSelected
   *
   * The server receives {@code spreadsheet-event} through a
   * {@code @DomEvent}; the detail's {@code type} + {@code data} keep the
   * GWT client's SpreadsheetHandler signatures as a wire contract.
   */
  private dispatchSelectionEvent(): void {
    const { activeCell } = this.selection;
    // A merge is one cell: the server hears that cell, as it did from the
    // GWT client, rather than a range it would offer to merge again.
    const range =
      this.selectsRange() || !isMultiCellRange(this.selection.activeRange)
        ? this.selection.activeRange
        : { c1: activeCell.col, c2: activeCell.col, r1: activeCell.row, r2: activeCell.row };
    const event = buildSelectionEvent(activeCell, range, this.rows, this.cols);
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
      this.sendToServer({ type: 'cellSelected', data: anchorData });
    }
    this.sendToServer(event);
  }

  // ====================================================================
  // Render pipeline — compose `CellGridRenderState` from
  // current state + push to the cell-grid sub-element.
  // ====================================================================

  /**
   * Recompute the visible viewport + push a fresh
   * {@code CellGridRenderState} to the cell-grid sub-element.
   *
   * The connector calls this in response to scroll events, viewport
   * resize, or any state change that affects rendering (data updates,
   * style changes, selection moves).
   *
   * @param opts.scrollLeft Horizontal scroll offset in pixels.
   * @param opts.scrollTop Vertical scroll offset in pixels.
   * @param opts.viewportWidth Viewport pixel width.
   * @param opts.viewportHeight Viewport pixel height.
   */
  applyRenderState(opts: {
    scrollLeft: number;
    scrollTop: number;
    viewportWidth: number;
    viewportHeight: number;
  }): void {
    const state = this.buildRenderState(opts);
    // Size the cell-grid to the full virtual grid so the scroll
    // surface's scrollWidth / scrollHeight reflect actual extent.
    // Cells render at absolute virtual coordinates inside; the
    // scroll container clips + scrolls them visually.
    this.cellGrid.style.width = `${state.totalGridWidthPx}px`;
    this.cellGrid.style.height = `${state.totalGridHeightPx}px`;
    this.cellGrid.renderState = state;
    const { col, row } = this.selection.activeCell;
    this.formulaBar.formulaLocked = this.isCellLockedForEdit(col, row);
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
    // Column-header strip. Sized + state-pushed only
    // when there is something to draw, so the default opt-out path
    // stays a no-op — important to avoid feedback loops with the
    // ResizeObserver under wtr parallel.
    //
    // A workbook can hide its headers and still group its columns; the
    // outline gutter lives in this strip, so it has to be drawn even
    // when the letters above it are not.
    //
    // The corner covers the outline gutters too - otherwise a grouped sheet
    // has an empty notch where the two gutters meet, and the level buttons
    // have nowhere to sit. Its size is how far the strips start from the
    // sheet's edges.
    const rowGutterPx = rowGroupingGutterPx(this.rowGroupingMax, this.headerColumnWidth > 0);
    const colGutterPx = columnGroupingGutterPx(this.colGroupingMax, this.headerRowHeight > 0);
    const cornerWidth = this.headerColumnWidth > 0 ? this.headerColumnWidth + rowGutterPx : rowGutterPx;
    const cornerHeight = this.headerRowHeight > 0 ? this.headerRowHeight + colGutterPx : colGutterPx;
    if (this.headerRowHeight > 0 || this.colGroupingMax > 0) {
      // Compute positioned column-groupings so the
      // header can render the outline gutter. Pre-resolving
      // pixel positions in the connector keeps the header
      // element independent of the full colWidths array.
      const colGroupings = computePositionedColGroupings(
        this.colGroupingData,
        this.colWidths,
        this.colGroupingInversed,
        this.defColWidth,
        5,
        this.rowGroupingMax > 0 ? 1 : 0
      );
      this.columnHeader.style.display = '';
      this.columnHeader.style.width = `${state.totalGridWidthPx}px`;
      this.columnHeader.style.height = `${this.headerRowHeight + colGutterPx}px`;
      this.columnHeader.renderState = {
        viewport: state.viewport,
        colX: state.colX,
        colWidths: state.colWidths,
        headerHeight: this.headerRowHeight,
        totalGridWidthPx: state.totalGridWidthPx,
        // Frozen columns stay on screen whatever the scroll position,
        // so their headers travel with them.
        horizontalSplitPosition: state.horizontalSplitPosition,
        frozenRows: state.verticalSplitPosition > 0,
        scrollTop: state.scrollTop,
        frozenColX: state.frozenColX,
        frozenColWidths: state.frozenColWidths,
        scrollLeft: state.scrollLeft,
        colGroupings,
        colGroupingMax: this.colGroupingMax,
        colGroupingInversed: this.colGroupingInversed,
        leadingPx: cornerWidth,
        // Feed the selected-columns set so the
        // header can emit `part="column-header
        // header-selected"` on cells inside the active
        // range. Pure derivation from
        // {@code selection.activeRange}.
        selectedColumns: selectedColumnSet(this.selection)
      };
    } else {
      // No headings and no outline: the strip goes altogether, border and
      // all, as the GWT client's did - also when they are turned off while
      // the sheet is up, rather than keep the size it was last given.
      this.columnHeader.style.display = 'none';
    }
    // Row-header strip. Same opt-in gate as the column
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
        defaultRowHeightPx,
        2,
        this.colGroupingMax > 0 ? 1 : 0
      );
      this.rowHeader.style.display = '';
      this.rowHeader.style.width = `${this.headerColumnWidth + rowGutterPx}px`;
      this.rowHeader.style.height = `${state.totalGridHeightPx}px`;
      this.rowHeader.renderState = {
        viewport: state.viewport,
        rowY: state.rowY,
        rowHeights: state.rowHeights,
        headerWidth: this.headerColumnWidth + rowGutterPx,
        totalGridHeightPx: state.totalGridHeightPx,
        // Feed the selected-rows set so the
        // header can emit `part="row-header
        // header-selected"` on rows inside the active
        // range.
        selectedRows: selectedRowSet(this.selection),
        rowGroupings,
        rowGroupingMax: this.rowGroupingMax,
        rowGroupingInversed: this.rowGroupingInversed,
        rowGroupingHeadings: this.headerColumnWidth > 0,
        leadingPx: cornerHeight,
        // Frozen rows stay on screen whatever the scroll position, so
        // their headers travel with them.
        verticalSplitPosition: state.verticalSplitPosition,
        frozenColumns: state.horizontalSplitPosition > 0,
        scrollLeft: state.scrollLeft,
        frozenRowY: state.frozenRowY,
        frozenRowHeights: state.frozenRowHeights,
        scrollTop: state.scrollTop
      };
    } else {
      this.rowHeader.style.display = 'none';
    }
    // Header corner. Sized only when BOTH header
    // dimensions are > 0 so it occupies its grid cell with
    // matching width + height. When either is 0, the corner
    // collapses to a 0×0 box and isn't visible.
    this.headerCorner.style.display = cornerWidth === 0 && cornerHeight === 0 ? 'none' : '';
    this.headerCorner.style.width = `${cornerWidth}px`;
    this.headerCorner.style.height = `${cornerHeight}px`;
    this.pagedViewParts = {
      corner: { width: cornerWidth, height: cornerHeight },
      frozen: {
        width: state.frozenColWidths.reduce((sum, w) => sum + w, 0),
        columns: state.horizontalSplitPosition,
        height: state.frozenRowHeights.reduce((sum, h) => sum + h, 0),
        rows: state.verticalSplitPosition
      }
    };
    this.applyPagedViewInset();
    this.headerCorner.renderState = {
      headerWidth: this.headerColumnWidth,
      headerHeight: this.headerRowHeight,
      colGroupingMax: this.colGroupingMax,
      rowGroupingMax: this.rowGroupingMax
    };
  }

  /**
   * Pure: build a {@code CellGridRenderState} from the current
   * internal state + the given scroll / viewport. Exposed for
   * tests to call without going through the DOM (the public
   * {@link applyRenderState} writes the result to the cell-grid;
   * this lets tests inspect it directly).
   */
  buildRenderState(opts: {
    scrollLeft: number;
    scrollTop: number;
    viewportWidth: number;
    viewportHeight: number;
  }): CellGridRenderState {
    const ppi = FALLBACK_PPI;
    const defaultRowHeightPx = getDefaultRowHeightPx(this.defRowHeight, ppi);
    const hiddenRowSet = this.hiddenRows;

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

    const hiddenColSet = this.hiddenColumns;
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
    //
    // The freeze line has a pixel of its own between the frozen columns or
    // rows and the rest, as the GWT client's frozen pane border did, so the
    // scrolling ones start one pixel past it. Drawn over their first pixel
    // instead, the first row after a freeze came out a pixel short.
    const split = { cols: this.horizontalSplitPosition, rows: this.verticalSplitPosition };
    const gapX = freezeGap(split.cols);
    const gapY = freezeGap(split.rows);
    const colX: number[] = [];
    const colWidths: number[] = [];
    let cumulativeX = frozenCumX + gapX + viewport.firstColumnPosition;
    for (let col = viewport.firstColumnIndex; col <= viewport.lastColumnIndex; col++) {
      const w = colWidthForIndex(col);
      colX.push(cumulativeX);
      colWidths.push(w);
      cumulativeX += w;
    }

    const rowY: number[] = [];
    const rowHeights: number[] = [];
    let cumulativeY = frozenCumY + gapY + viewport.firstRowPosition;
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
    const totals = computeTotalGridSize({
      rows: this.rows,
      cols: this.cols,
      definedRowHeightsPx,
      definedColWidthsPx: Array.from(this.colWidths),
      hiddenRows: hiddenRowSet,
      hiddenColumns: hiddenColSet,
      defaultRowHeightPx,
      defaultColWidthPx: this.defColWidth
    });
    const totalWidthPx = totals.totalWidthPx + gapX;
    const totalHeightPx = totals.totalHeightPx + gapY;

    // Compute positioned overlays from the current
    // overlay map + resource map. Each overlay's pixel
    // position is anchor cell's left/top (cumulative col/row
    // widths/heights) + the overlay's dx/dy offset.
    const positionedOverlays = computePositionedOverlays(
      this.overlays,
      this.resolveResources(),
      colWidthForIndex,
      rowHeightForIndex,
      ppi,
      split
    );

    const mergedRegionBoxes = computeMergedRegionBoxes(
      this.mergedRegions,
      split,
      { left: opts.scrollLeft, top: opts.scrollTop },
      colWidthForIndex,
      rowHeightForIndex
    );

    // Compute positioned popup buttons. Each
    // button anchors at its cell's bottom-right corner.
    const positionedPopupButtons = computePositionedPopupButtons(
      this.popupButtonStates,
      colWidthForIndex,
      rowHeightForIndex,
      split
    );

    return {
      viewport,
      cellData: this.cellDataByKey,
      colX,
      rowY,
      colWidths,
      rowHeights,
      cellStyleToCSSStyle: this.cellStyleToCSSStyle,
      displayGridlines: this.displayGridlines,
      rowIndexToStyleIndex: this.rowIndexToStyleIndex,
      columnIndexToStyleIndex: this.columnIndexToStyleIndex,
      cellSlots: Object.fromEntries(
        Object.entries(this.cellSlotAssignments()).map(([cellKey, a]) => [cellKey, a.slot])
      ),
      conditionalFormattingStyles: this.conditionalFormattingStyles,
      shiftedCellBorderStyles: this.shiftedCellBorderStyles,
      mergedRegions: this.mergedRegions,
      mergedRegionBoxes,
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
      formulaReferences: this.formulaReferencesToDraw(),
      overlays: positionedOverlays,
      popupButtons: positionedPopupButtons,
      paintRect: this.painting?.outcome.kind === 'none' ? null : this.painting?.outcome.painted ?? null,
      commentText: this.cellComments,
      commentAuthors: this.cellCommentAuthors,
      visibleCommentKeys: this.visibleCellComments,
      editingCommentKey: this.editingCommentKey,
      invalidFormulaMessage: this.invalidFormulaErrorMessage,
      // Comment-bearing + hyperlink cell keys.
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

  /** Test-only accessor: the column-header sub-element. */
  getColumnHeader(): SpreadsheetColumnHeader {
    return this.columnHeader;
  }

  /** Test-only accessor: the row-header sub-element. */
  getRowHeader(): SpreadsheetRowHeader {
    return this.rowHeader;
  }

  /** Test-only accessor: the header-corner box. */
  getHeaderCorner(): HTMLElement {
    return this.headerCorner;
  }

  /** Test-only accessor: the sheet-tabs sub-element. */
  getSheetTabs(): SpreadsheetSheetTabs {
    return this.sheetTabs;
  }

  /** Test-only accessor: the formula-bar sub-element. */
  getFormulaBar(): SpreadsheetFormulaBar {
    return this.formulaBar;
  }

  /**
   * Push fresh state to the sheet tabs from the current
   * {@code sheetNames} + {@code sheetIndex} fields. Called from
   * the public setters when those change, AND from any caller
   * that wants to force a re-render.
   */
  private syncSheetTabs(): void {
    this.sheetTabs.renderState = {
      sheetNames: this.sheetNames,
      activeIndex: this.sheetIndex,
      firstVisibleTab: this.firstVisibleTab,
      readOnly: this.workbookProtected,
      renamingIndex: this.renamingSheetIndex,
      infoLabel: this.infoLabelValue
    };
  }

  /**
   * A scroll button moved the strip.
   *
   * Nothing is reported: the GWT client told its own handler about the
   * new index, but the Flow server has no counterpart for it, so which
   * tab is leftmost is the client's business alone. Scrolling the strip
   * does not change the sheet.
   *
   * The end button goes to the leftmost tab that still shows the last
   * one, which the strip measured: as far as the strip can scroll
   * without showing empty space after the last tab.
   */
  private readonly handleSheetTabsScroll = (
    e: CustomEvent<{ direction: 'start' | 'backward' | 'forward' | 'end'; lastFirstTab?: number }>
  ): void => {
    const last = Math.max(0, this.sheetNames.length - 1);
    const moved = {
      start: 0,
      backward: this.firstVisibleTab - 1,
      forward: this.firstVisibleTab + 1,
      end: e.detail.lastFirstTab ?? last
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
  private readonly handleFormulaCommit = (
    e: CustomEvent<{ value: string; key?: string; shiftKey: boolean; relatedTarget?: EventTarget | null }>
  ): void => {
    const { value, key, shiftKey, relatedTarget } = e.detail;
    // Back to the cell editor the edit came from: it carries on there.
    if (key === undefined && this.editor !== null && relatedTarget === this.cellGrid) {
      return;
    }
    const { col, row } = this.selection.activeCell;
    if (this.rows <= 0 || this.cols <= 0) {
      return;
    }
    if (this.isCellLockedForEdit(col, row)) {
      this.reportProtectedCellWriteAttempt();
      return;
    }
    // An edit started in the cell ends here too: its editor closes, and
    // the cell shows the value, as it does when the edit ends there.
    const editorWasOpen = this.editor !== null;
    this.editor = null;
    this.endFormulaPointing();
    this.showCommittedValue(col, row, value);
    this.sendToServer({ type: 'cellValueEdited', data: { row, col, value } });
    // shift+Tab stays put: the GWT client passed the move along only
    // when shift was not held.
    const direction = key === undefined || (key === 'Tab' && shiftKey) ? null : keyDirection(key, false);
    if (direction === null) {
      if (editorWasOpen) {
        this.recomputeViewport();
      }
      return;
    }
    this.returnFocusToSheet();
    const next = moveActiveCell({ col, row }, this.mergeEntry, direction, this.sheetShape());
    if (next === null) {
      this.recomputeViewport();
      return;
    }
    this.applySingleSelection(next);
    this.scrollCellIntoView(next.activeCell);
    this.recomputeViewport();
    this.dispatchSelectionEvent();
  };

  /**
   * Escape in the formula field: put the cell's own value back. An edit
   * started in the cell is thrown away with it, and its editor closes.
   */
  private readonly handleFormulaCancel = (): void => {
    this.endFormulaPointing();
    const { col, row } = this.selection.activeCell;
    this.formulaBar.renderState = {
      ...this.formulaBar.renderState,
      formula: this.formulaFieldValueFor(col, row)
    };
    if (this.editor !== null) {
      this.editor = null;
      this.recomputeViewport();
    }
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
    this.sendToServer({ type: 'updateCellComment', data: { text, col: coord.col, row: coord.row } });
  };

  /**
   * An outline level button: show the sheet down to that level.
   *
   * The server expands everything shallower and collapses that level,
   * which hides whatever is nested under it.
   */
  private readonly handleLevelHeaderClick = (e: CustomEvent<{ cols: boolean; level: number }>): void => {
    this.sendToServer({ type: 'levelHeaderClicked', data: { cols: e.detail.cols, level: e.detail.level } });
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
    this.sendToServer({
      // The server counts sheets from 0; the strip renders from 1.
      type: 'sheetRenamed',
      data: { sheetIndex: index - 1, newName: outcome.name }
    });
  };

  /** The + button: ask the server for a new sheet. */
  private readonly handleSheetTabAdd = (): void => {
    if (this.workbookProtected) {
      return;
    }
    // The strip button took the focus on the way in. Adding a sheet is a
    // way of moving to it, so the keyboard belongs to the new sheet.
    this.scrollContainer.focus();
    this.sendToServer({
      type: 'sheetCreated',
      data: this.scrollOffsets()
    });
  };

  /**
   * Sheet-tab click handler — translates to a `sheetSelected`
   * spreadsheet-event for the server. The server is expected
   * to call back into {@code setSheetIndex} + push fresh data
   * via the cell value APIs, which the next render picks up.
   */
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
    this.sendToServer({
      type: 'sheetSelected',
      data: {
        // The server reads a 0-based tab index here; `index` is the
        // 1-based position the strip renders.
        sheetIndex: index - 1,
        ...this.scrollOffsets()
      }
    });
  };

  /**
   * Where the sheet is scrolled to, in whole pixels, as the server stores
   * it. A browser reports fractions on a scaled display, and the server
   * reads whole numbers.
   */
  private scrollOffsets(): { scrollLeft: number; scrollTop: number } {
    return {
      scrollLeft: Math.round(this.scrollContainer.scrollLeft),
      scrollTop: Math.round(this.scrollContainer.scrollTop)
    };
  }

  // ====================================================================
  // Lifecycle
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

  resize(): void {
    // The wrapper's `spreadsheetResizeObserver` calls this
    // on container size changes. We also have our own
    // ResizeObserver on the scroll container —
    // calling recomputeViewport here is idempotent + cheap.
    this.recomputeViewport();
  }

  notifyStateChanges(changedPropertyNames: string[], firstUpdate: boolean): void {
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
    // property has been forwarded. The first batch renders even when it is
    // empty, so the sheet is drawn once it is in the page.
    // A sheet shown again goes back to where it was scrolled. The sheet
    // shown can also get another index without changing, when a sheet
    // before it is deleted, hidden or shown again: it stays where it is.
    const index = this.sheetIndex - 1;
    const shownSheetName = this.sheetNames[index] ?? null;
    if (!firstUpdate && changedPropertyNames.includes('sheetIndex') && shownSheetName !== this.shownSheetName) {
      this.pendingSheetScroll = {
        top: this.verticalScrollPositions[index] ?? 0,
        left: this.horizontalScrollPositions[index] ?? 0
      };
    }
    this.shownSheetName = shownSheetName;
    if (changedPropertyNames.length > 0 || firstUpdate) {
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
   * its wire-format key. All four quadrants share the one map; the
   * cell-grid picks out what each pane draws.
   */
  private storeCellData(cellData: CellData[] | string): void {
    // Flow hands collections over as a JSON string on some lanes and as an
    // array on others. Iterating the string walks it character by
    // character, which quietly files one entry under
    // "colundefined rowundefined" and drops every value on the sheet.
    for (const cell of coerceArray<CellData>(cellData)) {
      this.cellDataByKey.set(toKey(cell.col, cell.row), cell);
      // A style no ratio has been measured for yet: measure again after
      // the next render.
      const index = styleIndexOf(cell.cellStyle);
      if (index !== null && !this.measuredStyleIndexes.has(index)) {
        this.styleWidthRatiosStale = true;
      }
    }
  }
  updateFormulaBar(possibleName: string | null, col: number, row: number): void {
    // The server only sends a name when the selection has one — a named
    // range, or a sheet-qualified reference. For an ordinary cell it
    // sends null and leaves the address to the client, which is the
    // common case: without this the address field sits empty for every
    // selection the user makes.
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
   *
   * `keepView` is set for the selection a sheet is shown with again: the
   * sheet has just gone back to where it was scrolled, and its selection
   * leaves it there, wherever the selected cell is.
   */
  showSelectedCell(
    name: string | null,
    col: number,
    row: number,
    cellValue: string,
    formula: boolean,
    _locked: boolean,
    _initialSelection: boolean,
    keepView = false
  ): void {
    // Read before the selection collapses to the one cell below: while
    // the user is still growing a selection the address field is
    // counting the block out, and this is the server answering the
    // anchor of the range they are in the middle of making.
    const sizeLabel = selectionSizeLabel(this.selection.activeRange);
    this.selection = selectSingleCell(col, row);
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
    if (!keepView) {
      this.scrollCellIntoView({ col, row });
    }
    this.scheduleRender();
  }
  showActions(actionDetails: SpreadsheetActionDetails[]): void {
    const actions = coerceArray<SpreadsheetActionDetails>(actionDetails);
    this.contextMenu.renderState = {
      open: true,
      x: this.lastContextMenuPosition.x,
      y: this.lastContextMenuPosition.y,
      // Arrives JSON-serialized through the Flow lane, like the
      // collection-shaped properties.
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
    // From inside the root's border, where the menu's space starts.
    const origin = this.root.getBoundingClientRect();
    this.lastContextMenuPosition = {
      x: (detail.clientX ?? 0) - origin.left - this.root.clientLeft,
      y: (detail.clientY ?? 0) - origin.top - this.root.clientTop
    };
  }

  /**
   * Close the context menu and tell the server, unless it is already closed.
   */
  private closeContextMenu(): void {
    if (!this.contextMenu.renderState.open) {
      return;
    }
    // The menu had the keyboard; the GWT client gave it back to where it
    // had been. A click elsewhere still moves it on, once this is done.
    const menuHadFocus = this.contextMenu.shadowRoot?.activeElement != null;
    this.contextMenu.renderState = emptyContextMenuRenderState();
    this.actionIcons.clear();
    if (menuHadFocus) {
      this.returnFocusToSheet();
    }
    this.sendToServer({ type: 'contextMenuClosed', data: {} });
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
    this.sendToServer({ type: eventType, data: { actionKey: key } });
  };

  /** Left or Tab in the menu closes it, as in the GWT client. */
  private readonly handleContextMenuDismiss = (): void => {
    this.closeContextMenu();
  };

  /** Any click outside the menu dismisses it, as a native menu would. */
  private readonly handleDocumentPointerDown = (e: Event): void => {
    const path = e.composedPath();
    // A click inside the popup belongs to the popup's content. A press on
    // the button that opened it closes it, as in the GWT client, and the
    // click that follows is then not to open it again.
    // The content is a light child of the host projected into the overlay,
    // so the path reaches the overlay through the slot — except before the
    // projection settles, hence the second check.
    const insidePopup = path.includes(this.popupOverlay) || path.some((target) => this.popupContents.owns(target));
    const open = this.openPopup;
    this.popupClosedByItsButton =
      open !== null && this.hitOpenPopupButton(path) ? { row: open.row, col: open.col } : null;
    if (open !== null && !insidePopup) {
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

  /** Whether the focus is in the popup: its card, or the content it shows. */
  private popupHasFocus(): boolean {
    let el: Element | null = document.activeElement;
    while (el?.shadowRoot?.activeElement) {
      el = el.shadowRoot.activeElement;
    }
    for (let node: Node | null = el; node !== null; ) {
      if (node === this.popupOverlay || this.popupContents.owns(node)) {
        return true;
      }
      node = node.parentNode instanceof ShadowRoot ? node.parentNode.host : node.parentNode;
    }
    return false;
  }

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
    const before = this.selection.activeCell;
    const moved = before.col !== col || before.row !== row;
    this.selection = {
      activeCell: { col, row },
      activeRange: normalizedRange(c1, r1, c2, r2),
      auxRanges: this.selection.auxRanges
    };
    this.formulaBar.renderState = {
      // As in updateFormulaBar: the server names a selection only when
      // it has a name, and leaves an ordinary one to the client. While
      // the user is still growing the selection, though, the count is
      // what they are reading, and this is the server echoing back the
      // range they are in the middle of making.
      address: this.extendingSelection
        ? selectionSizeLabel(this.selection.activeRange)
        : name ?? cellCoordToAddress({ col, row }),
      // A new active cell shows in the field, as the GWT client's
      // updateSelectedCellValues showed it - unless an edit is open, whose
      // text stays where the user is typing it. The same cell keeps what
      // the field has: the server may have just sent its value along with
      // showSelectedCell, for a cell this client holds no data for.
      formula:
        moved && this.editor === null && !this.formulaBar.formulaFieldHasFocus()
          ? this.formulaFieldValueFor(col, row)
          : this.formulaBar.renderState.formula,
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
   *
   * The server names the cell by its POI CellReference, which counts
   * from 0 where every other cell address here counts from 1.
   */
  editCellComment(col: number, row: number): void {
    this.editingCommentKey = toKey(col + 1, row + 1);
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
    this.hiddenColumns = new Set(coerceArray<number>(indexes));
  }
  setHiddenRowIndexes(indexes: number[]): void {
    this.hiddenRows = new Set(coerceArray<number>(indexes));
  }
  setLockedColumnIndexes(indexes: number[]): void {
    this.lockedColumns = new Set(coerceArray<number>(indexes));
  }
  setLockedRowIndexes(indexes: number[]): void {
    this.lockedRows = new Set(coerceArray<number>(indexes));
  }
  setSheetIndex(index: number): void {
    this.sheetIndex = index;
    // Update the sheet-tabs strip so the active tab moves to
    // the server's chosen sheet.
    this.syncSheetTabs();
  }
  setSheetNames(names: string[]): void {
    this.sheetNames = coerceArray(names);
    // Re-render the sheet-tabs strip with the new names list.
    this.syncSheetTabs();
  }
  /**
   * Where each sheet was last scrolled to, by visible sheet, as the server
   * keeps it: a sheet shown again goes back there. The GWT client meant to
   * and did not.
   */
  setVerticalScrollPositions(positions: number[] | string): void {
    this.verticalScrollPositions = coerceNumberArray(positions);
  }
  /** See {@link setVerticalScrollPositions}. */
  setHorizontalScrollPositions(positions: number[] | string): void {
    this.horizontalScrollPositions = coerceNumberArray(positions);
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
   * So clear the handshake latch and forget what was loaded, and let the
   * next completed viewport announce itself.
   */
  setReload(triggerReload: boolean): void {
    if (triggerReload) {
      // The server has thrown away everything it knew about the sheet —
      // a sheet switch, a new file, a reset. Our cached cell values are
      // the previous sheet's, and the server only ever sends cells that
      // have something in them, so a cell that is empty on the new sheet
      // is never mentioned again and the old value would show through.
      this.cellDataByKey.clear();
      this.viewportAnnounced = false;
      // What it was asked to load is gone too: load again, even the same
      // range.
      this.viewportLoader.reset();
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
    // Turned off or on from the server on its own, with no cell data to
    // carry a repaint along.
    this.scheduleRender();
  }
  setDisplayRowColHeadings(visible: boolean): void {
    // The server sends only this boolean, never the strips' sizes: shown,
    // they get their default sizes back; hidden, they collapse.
    if (visible) {
      if (this.headerRowHeight === 0) {
        this.headerRowHeight = DEFAULT_HEADER_ROW_HEIGHT;
      }
      if (this.headerColumnWidth === 0) {
        this.headerColumnWidth = DEFAULT_HEADER_COLUMN_WIDTH;
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
  /**
   * The server flips this on loading another workbook. The change alone is
   * the point: the property batch it comes in is drawn, as every batch is.
   */
  setWorkbookChangeToggle(_toggle: boolean): void {
    // See above.
  }

  // Display content
  /**
   * The status label at the right end of the sheet tabs. The server
   * clears the property for no label, which reaches here as null or
   * undefined; an empty string is a label with nothing in it.
   */
  setInfoLabelValue(label: string | null | undefined): void {
    this.infoLabelValue = label ?? null;
    this.syncSheetTabs();
  }
  setHyperlinksTooltips(tooltips: Record<string, string>): void {
    this.hyperlinksTooltips = coerceObject(tooltips);
  }
  /**
   * The id the server gives the element. The element's own id property
   * applies it: Lit wraps the native one.
   */
  setId(_id: string): void {
    // See above.
  }

  // ====================================================================
  // Popup-button lifecycle
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
    // Opened again while it shows, the GWT client's popup stayed put.
    const showing = this.openPopup !== null && this.openPopup.row === row && this.openPopup.col === column;
    this.openPopup = { row, col: column, nodeId };
    // A resolver per call rather than the sheet's own: the id belongs to
    // the application the button reports, not necessarily the sheet's.
    this.popupContents.setResolver(flowVirtualChildResolver(appId));
    this.popupContents.sync([nodeId]);
    const state = this.popupButtonStates.find((b) => b.row === row && b.col === column);
    const parts: Node[] = [popupOverlayRules()];
    if (!state?.headerHidden) {
      parts.push(this.buildPopupHeader(this.cellDataByKey.get(toKey(column, row))?.value ?? ''));
    }
    parts.push(namedSlot(popupContentSlotName(nodeId)));
    this.popupOverlay.replaceChildren(...parts);
    this.popupOverlay.hidden = false;
    showInTopLayer(this.popupOverlay);
    this.positionPopupOverlay();
    if (!showing) {
      animateIn(this.popupOverlay);
    }
    this.popupResizeObserver.observe(this.popupOverlay);
    this.scheduleRender();
    // Opened from the keyboard, the keyboard goes into it: to its close
    // box, or to the card itself, from where Tab reaches the content. With
    // the pointer the focus stays where it is.
    const fromKeyboard = this.popupOpenedFromKeyboard;
    this.popupOpenedFromKeyboard = null;
    if (fromKeyboard !== null && fromKeyboard.row === row && fromKeyboard.col === column) {
      const target = this.popupOverlay.querySelector<HTMLElement>('.popup-close') ?? this.popupOverlay;
      target.focus({ preventScroll: true });
    }
  }

  /**
   * The popup's header strip: its close box and, as in the GWT client, the
   * text of the cell the button is on - a filter's column title.
   *
   * Shown unless the button asked for {@code headerHidden}, which is what
   * {@code PopupButton.setHeaderHidden(true)} sets: a popup with no header
   * has to be dismissed by clicking away from it.
   */
  private buildPopupHeader(caption: string): HTMLElement {
    const header = document.createElement('div');
    header.className = 'popup-header';
    header.setAttribute('part', 'popup-header');
    const close = document.createElement('button');
    close.type = 'button';
    close.className = 'popup-close';
    close.setAttribute('part', 'popup-close');
    close.setAttribute('aria-label', 'Close');
    close.addEventListener('click', () => this.dismissPopup());
    const text = document.createElement('div');
    text.className = 'header-caption';
    text.setAttribute('part', 'popup-caption');
    text.textContent = caption;
    header.append(close, text);
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
    this.hidePopup(true);
  }

  /**
   * Take the popup down and tell the server. Used for every client-side
   * dismissal — clicking away, Escape, scrolling the anchor out of view.
   * Its content fades out with it unless asked to go first - see hidePopup.
   */
  private dismissPopup(releaseContentFirst = false): void {
    const closing = this.openPopup;
    if (closing === null) {
      return;
    }
    this.hidePopup(releaseContentFirst);
    this.sendToServer({
      type: 'popupClose',
      data: { row: closing.row, column: closing.col }
    });
  }

  /**
   * Hide the overlay and release its content, without telling the server.
   *
   * The card fades out and then goes, as the GWT client's did. What it held
   * fades with it when the user closed it; when the server did, or took
   * the button away, the content goes first and the empty card fades, which
   * is the order the GWT client released them in then.
   */
  private hidePopup(releaseContentFirst: boolean): void {
    const closing = this.openPopup;
    // The keyboard was in the popup: it goes back to the button, or to the
    // sheet when the button has gone, rather than to nowhere.
    const hadFocus = this.popupHasFocus();
    this.openPopup = null;
    this.popupResizeObserver.unobserve(this.popupOverlay);
    if (releaseContentFirst) {
      this.popupContents.clear();
    }
    if (hadFocus && closing !== null) {
      const stays = this.popupButtonStates.some((b) => b.row === closing.row && b.col === closing.col);
      const button = stays
        ? this.cellGrid.shadowRoot?.querySelector<HTMLElement>(
            `.popup-button[data-col="${closing.col}"][data-row="${closing.row}"]`
          )
        : null;
      (button ?? this.scrollContainer).focus({ preventScroll: true });
    }
    const overlay = this.popupOverlay;
    animateOut(overlay, () => {
      this.popupContents.clear();
      overlay.replaceChildren();
      overlay.hidden = true;
      hideFromTopLayer(overlay);
    });
    this.scheduleRender();
  }

  /** The sheet as the arrow keys see it. */
  private sheetShape(): SheetShape {
    return {
      cols: this.cols,
      rows: this.rows,
      mergedRegions: this.mergedRegions,
      hiddenColumns: this.hiddenColumns,
      hiddenRows: this.hiddenRows
    };
  }

  /**
   * Select one cell - or the whole merge it is part of, with the merge's
   * top-left cell active, as the GWT client did for a click.
   */
  private selectCellOrMerge(col: number, row: number): void {
    this.applySingleSelection(selectCellAt(col, row, this.mergedRegions));
  }

  private applySingleSelection(next: SingleCellSelection): void {
    this.selection = { activeCell: next.activeCell, activeRange: next.activeRange, auxRanges: [] };
    this.mergeEntry = next.entry;
  }

  /**
   * Whether a range is selected: more than one cell, and more than the one
   * merge the active cell is in, which is a single cell to the user.
   */
  private selectsRange(): boolean {
    const { activeRange, activeCell } = this.selection;
    return isMultiCellRange(activeRange) && !isSingleMerge(activeRange, activeCell, this.mergedRegions);
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
    if (this.hiddenColumns.has(col)) {
      return 0;
    }
    return col > this.colWidths.length ? this.defColWidth : this.colWidths[col - 1];
  }

  /**
   * Rendered height of a row in pixels. POI measures rows in points, so
   * this is the one place the conversion happens.
   */
  private rowHeightPx(row: number, ppi: number = FALLBACK_PPI): number {
    if (this.hiddenRows.has(row)) {
      return 0;
    }
    return row > this.rowHeights.length
      ? getDefaultRowHeightPx(this.defRowHeight, ppi)
      : convertPointsToPixel(this.rowHeights[row - 1], ppi);
  }

  /**
   * Place the overlay at its button by the GWT client's rule - see
   * {@link popupPlacement} - against the cell as it is drawn now and the
   * scrolling pane around it.
   *
   * While the cell is outside the rendered range the overlay stays where it
   * is, as the GWT client's did.
   */
  private positionPopupOverlay(): void {
    if (this.openPopup === null) {
      return;
    }
    const { row, col } = this.openPopup;
    const state = this.popupButtonStates.find((b) => b.row === row && b.col === col);
    const overlay = this.popupOverlay;
    overlay.style.width = state?.popupWidth ?? '';
    overlay.style.height = state?.popupHeight ?? '';
    const cell = this.cellGrid.cellClientRect(col, row);
    if (cell === null) {
      return;
    }
    // Measured at the page's corner, where nothing squeezes it: next to the
    // right edge of the page an overlay of no set width wraps to fit.
    overlay.style.left = '0px';
    overlay.style.top = '0px';
    const place = popupPlacement(cell, this.scrollingPaneClientRect(), overlay.offsetWidth, overlay.offsetHeight);
    overlay.style.left = `${place.left + window.scrollX}px`;
    overlay.style.top = `${place.top + window.scrollY}px`;
  }

  /**
   * The scrolling pane on screen: the cells under the column letters and
   * right of the row numbers, past the frozen rows and columns. The GWT
   * client's popups kept to this, its bottom-right pane, whose bottom is the
   * sheet's own.
   */
  private scrollingPaneClientRect(): { left: number; top: number; bottom: number } {
    const box = this.scrollContainer.getBoundingClientRect();
    const frozen = this.cellGrid.frozenExtent();
    // Past the freeze line's own pixel, where the scrolling cells start.
    return {
      left:
        box.left +
        this.scrollContainer.clientLeft +
        this.cellGrid.offsetLeft +
        frozen.width +
        freezeGap(this.horizontalSplitPosition),
      top:
        box.top +
        this.scrollContainer.clientTop +
        this.cellGrid.offsetTop +
        frozen.height +
        freezeGap(this.verticalSplitPosition),
      bottom: box.bottom
    };
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
    const state = coerceJson(rawState);
    if (state === null || typeof state.col !== 'number') {
      return;
    }
    this.popupButtonStates = [...this.popupButtonStates.filter((b) => !samePopupButtonCell(b, state)), state];
    this.scheduleRender();
  }

  /**
   * The button's cell is what identifies it; the rest of the state may
   * differ.
   *
   * The server takes a button away once its cell has scrolled out of view,
   * and when the sheet changes. The GWT client's popup closed with its
   * button and told the server it had.
   */
  removePopupButton(rawState: PopupButtonState | string): void {
    const state = coerceJson(rawState);
    if (state === null || typeof state.col !== 'number') {
      return;
    }
    // The button goes first, so that a popup closing with it does not give
    // the keyboard back to it.
    this.popupButtonStates = this.popupButtonStates.filter((b) => !samePopupButtonCell(b, state));
    if (this.openPopup !== null && this.openPopup.row === state.row && this.openPopup.col === state.col) {
      this.dismissPopup(true);
    }
    this.scheduleRender();
  }

  // ====================================================================
  // Read accessors
  // ====================================================================

  /**
   * Whether a request for cells, or for the server to hear where the sheet
   * stopped, may still be on its way. TestBench waits for this as well as for
   * Flow after a scroll: the loader decides on a timer that Flow cannot see.
   */
  isViewportLoadPending(): boolean {
    return this.viewportLoader.pending;
  }

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
 * Where a popup goes, by the GWT client's rule, in screen coordinates.
 *
 * Under the button's cell, its right edge on the cell's right edge - or
 * starting at that edge instead, when ending there would take it past the
 * left side of the scrolling pane. Above the cell when it runs past the
 * pane's bottom, and never above the pane's top: a popup too tall for
 * either side hangs from the top and runs past the bottom.
 */
export function popupPlacement(
  cell: { readonly top: number; readonly right: number; readonly bottom: number },
  pane: { readonly left: number; readonly top: number; readonly bottom: number },
  width: number,
  height: number
): { left: number; top: number } {
  let left = cell.right - width;
  if (left < pane.left) {
    left = cell.right;
  }
  let top = cell.bottom;
  if (top + height > pane.bottom) {
    top = cell.top - height;
  }
  if (top < pane.top) {
    top = pane.top;
  }
  return { left, top };
}

/**
 * Put a popover in the top layer. A no-op for one already there, or one
 * the browser cannot show - not in the document, or no popover support -
 * which then stays where the page puts it.
 */
function showInTopLayer(el: HTMLElement): void {
  try {
    if (el.isConnected && !el.matches(':popover-open')) {
      el.showPopover();
    }
  } catch {
    // Shown in place instead.
  }
}

/** Take a popover out of the top layer, if it is there. */
function hideFromTopLayer(el: HTMLElement): void {
  try {
    if (el.matches(':popover-open')) {
      el.hidePopover();
    }
  } catch {
    // Not in the top layer.
  }
}

/**
 * Where the scroll position along one axis goes to show the column or row
 * at a 1-based index, by the GWT client's rule. A frozen one, or one
 * already in full view, leaves it where it is. One before the first whole
 * one in view scrolls back past it by one more, and one after the last
 * whole one in view scrolls on by one more - so holding an arrow key
 * scrolls two at a time and keeps one in view beyond the one selected.
 *
 * `scroll` is the current scroll offset and `view` the length of the
 * cells' part of the view, frozen ones included; `sizeOf` is a column's
 * width or a row's height, 0 when it is hidden. The ones past the frozen
 * ones are drawn past the freeze line's own pixel.
 */
export function scrollToShow(
  index: number,
  frozen: number,
  count: number,
  scroll: number,
  view: number,
  sizeOf: (i: number) => number
): number {
  const gap = freezeGap(frozen);
  if (index <= frozen || index > count) {
    return scroll;
  }
  // Start of each one, from the start of the first: starts[i] for 1-based i.
  const starts: number[] = [0, 0];
  const startOf = (i: number): number => {
    while (starts.length <= i) {
      const last = starts.length - 1;
      starts.push(starts[last] + sizeOf(last));
    }
    return starts[i];
  };
  const frozenExtent = startOf(frozen + 1);
  // The first whole one in view past the frozen ones.
  let first = frozen + 1;
  while (first < count && startOf(first) - scroll < frozenExtent) {
    first++;
  }
  if (index < first) {
    let back = 0;
    for (let i = first - 1; i >= index - 1 && i > 0; i--) {
      back += sizeOf(i);
    }
    return Math.max(0, scroll - back);
  }
  // The last whole one in view.
  let last = first;
  while (last < count && startOf(last + 1) + sizeOf(last + 1) + gap - scroll <= view) {
    last++;
  }
  if (index > last) {
    let on = 0;
    for (let i = last + 1; i <= index + 1 && i <= count; i++) {
      on += sizeOf(i);
    }
    return scroll + on;
  }
  return scroll;
}

/**
 * Where a link cell's tooltip goes, in viewport pixels, by the GWT client's
 * rule for it: left-aligned under the cell; right-aligned with the cell
 * when it is wider than the cell and would run off the right of the window
 * but fits that way; and above the cell when there is no room below but
 * room above.
 */
export function linkTooltipPosition(
  cell: { left: number; top: number; right: number; bottom: number },
  size: { width: number; height: number },
  view: { width: number; height: number }
): { left: number; top: number } {
  let left = cell.left;
  const wider = size.width - (cell.right - cell.left);
  if (wider > 0 && view.width - left < size.width && left >= wider) {
    left -= wider;
  }
  let top = cell.bottom;
  if (view.height - cell.bottom < size.height && cell.top >= size.height) {
    top = cell.top - size.height;
  }
  return { left, top };
}

/** Whether the page runs on a Mac, where shortcuts take Cmd rather than Ctrl. */
export function isMacPlatform(): boolean {
  return /Mac|iPhone|iPad|iPod/.test(navigator.platform);
}

/** Pixels in points, as the GWT client converted the height of a row being dragged. */
function pixelsToPoints(px: number, ppi: number = FALLBACK_PPI): number {
  return (px * 72) / ppi;
}

/**
 * Column or row sizes with the one at a 1-based index changed. Those past
 * the end take the default, so the gap up to a size set past it is filled
 * with the default.
 */
function withSize(sizes: ArrayLike<number>, index: number, size: number, fallback: number): number[] {
  const out = Array.from(sizes);
  while (out.length < index - 1) {
    out.push(fallback);
  }
  out[index - 1] = size;
  return out;
}

/**
 * The popup's card, header and close box, as rules the popup carries.
 *
 * The overlay is the connector's own element in the host's tree, where no
 * internal element's stylesheet reaches, and a rule says what an inline
 * style cannot: "on hover", and "under the Lumo theme". The GWT client's
 * card is white, rounded and softly shadowed, with an 18px header whose
 * close box sits at its left, grey until pointed at, and whose caption is
 * centred; its Lumo popup put the close box at the right, under a rule, as
 * Lumo's cross, and the caption at the left in bold.
 */
function popupOverlayRules(): HTMLStyleElement {
  const rules = document.createElement('style');
  rules.textContent = `
    ${overlayAnimationStyles.cssText}
    .popup-overlay.animate-out {
      animation: overlay-animate-out 120ms;
    }

    .popup-overlay.animate-in {
      animation: overlay-animate-in 120ms;
    }
    .popup-overlay {
      outline: none;
      padding: 4px;
      border: 0;
      border-radius: 4px;
      color: #474747;
      background: var(--vaadin-spreadsheet-popup-background, var(--_vaadin-spreadsheet-popup-background, #fff));
      /* The edge is the last shadow, a hairline that takes no room. */
      box-shadow: 0 4px 10px 0 rgba(0, 0, 0, 0.1), 0 3px 5px 0 rgba(0, 0, 0, 0.05),
        0 0 0 1px var(--vaadin-spreadsheet-popup-border-color, var(--_vaadin-spreadsheet-popup-border-color, rgba(0, 0, 0, 0.09)));
    }
    .popup-header {
      position: relative;
      height: 18px;
      padding-bottom: 7px;
    }
    .popup-close {
      position: absolute;
      left: 0;
      top: 0;
      box-sizing: border-box;
      width: 18px;
      height: 18px;
      padding: 0 4px 0 0;
      border: 0;
      border-radius: 0 4px 0 4px;
      background: transparent;
      cursor: pointer;
      font-family: inherit;
      font-size: 21px;
      line-height: 18px;
      text-align: center;
      color: #999;
    }
    .popup-close::before {
      content: '\\00d7';
    }
    .popup-close:hover {
      color: #197de1;
    }
    .header-caption {
      margin: 0 18px;
      height: 18px;
      line-height: 18px;
      text-align: center;
      color: var(--lumo-body-text-color);
    }
    :host([theme~='lumo']) .popup-overlay {
      padding: var(--lumo-space-xs);
      border: 1px solid var(--vaadin-spreadsheet-popup-border-color, var(--_vaadin-spreadsheet-popup-border-color, rgba(0, 0, 0, 0.09)));
      border-radius: var(--lumo-border-radius-m);
      color: var(--lumo-body-text-color);
      box-shadow: var(--lumo-box-shadow-s);
    }
    :host([theme~='lumo']) .header-caption {
      margin-left: var(--lumo-space-xs);
      font-weight: bold;
      text-align: left;
    }
    :host([theme~='lumo']) .popup-header {
      padding-bottom: var(--lumo-space-xs);
      border-bottom: 1px solid var(--lumo-contrast-20pct);
      margin-bottom: var(--lumo-space-xs);
    }
    :host([theme~='lumo']) .popup-close {
      left: auto;
      right: 0;
      padding-right: var(--lumo-space-xs);
      border-radius: 0 var(--lumo-border-radius-m) 0 var(--lumo-border-radius-m);
      font-size: var(--lumo-font-size-xl);
      color: var(--lumo-contrast-50pct);
      cursor: var(--lumo-clickable-cursor);
    }
    :host([theme~='lumo']) .popup-close::before {
      content: var(--lumo-icons-cross);
      font-family: 'lumo-icons';
    }
    :host([theme~='lumo']) .popup-close:hover {
      color: var(--lumo-primary-color);
    }
  `;
  return rules;
}

/**
 * The pixel the freeze line takes between the frozen columns or rows and the
 * rest, as the GWT client's frozen pane border did: 1 when any are frozen.
 */
export function freezeGap(frozen: number): number {
  return frozen > 0 ? 1 : 0;
}

/**
 * How much further on a column or row is drawn for the freeze line before
 * it: the line's pixel when it lies past the frozen ones, nothing otherwise.
 */
export function pastFreeze(index: number, frozen: number): number {
  return index > frozen ? freezeGap(frozen) : 0;
}

/**
 * How far the headers and frozen panes reach into the scroll container's
 * view: the browser leaves that much out of the view it pages, with Page Up,
 * Page Down or a click in a scrollbar's track, given it as `scroll-padding`.
 * The GWT client paged its pane of scrolling cells, which started past the
 * headers, the frozen panes and the freeze line, and ran a pixel under the
 * top border of the sheet tabs; while the tabs show, a page here is a pixel
 * taller for it too.
 */
export function pagedViewInset(
  corner: { width: number; height: number },
  frozen: { width: number; columns: number; height: number; rows: number },
  tabsShown: boolean
): { top: number; left: number } {
  return {
    top: Math.max(0, corner.height + frozen.height + freezeGap(frozen.rows) - (tabsShown ? 1 : 0)),
    left: corner.width + frozen.width + freezeGap(frozen.columns)
  };
}

/**
 * Pure helper: build the positioned-popup-button list from the
 * server-pushed `PopupButtonState` list. Each button anchors at
 * the bottom-right corner of its target cell — pixel position =
 * (sum of widths 1..col + col width, sum of heights 1..row + row
 * height).
 */
export function computePositionedPopupButtons(
  states: ReadonlyArray<PopupButtonState>,
  colWidth: (col: number) => number,
  rowHeight: (row: number) => number,
  split: { cols: number; rows: number } = { cols: 0, rows: 0 }
): PositionedPopupButton[] {
  return states.map((state) => {
    let left = pastFreeze(state.col, split.cols);
    for (let c = 1; c <= state.col; c++) {
      left += colWidth(c);
    }
    let top = pastFreeze(state.row, split.rows);
    for (let r = 1; r <= state.row; r++) {
      top += rowHeight(r);
    }
    return {
      col: state.col,
      row: state.row,
      leftPx: left,
      topPx: top,
      cellHeightPx: rowHeight(state.row),
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

/** Two popup-button states describe the same button when they share a cell. */
function samePopupButtonCell(a: PopupButtonState, b: PopupButtonState): boolean {
  return a.col === b.col && a.row === b.row;
}

/**
 * Whether Flow is waiting for the server to answer the application the
 * host belongs to. False when there is no Flow client to ask.
 */
function flowRequestInFlight(host: HTMLElement): boolean {
  const clients = (
    window as unknown as { Vaadin?: { Flow?: { clients?: Record<string, { isActive?: () => boolean }> } } }
  ).Vaadin?.Flow?.clients;
  return clients?.[overlayComponentAppId(host)]?.isActive?.() === true;
}

/**
 * The Flow application the sheet belongs to.
 *
 * The server pushes it as an {@code appId} property on the host — the
 * component is registered against one running application, and looking a
 * virtual child up in the wrong one silently finds nothing.
 */
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
 * The points are converted exactly, fractions kept, as the GWT client
 * left them to the browser in `pt`. Cut down to whole pixels, a picture
 * sat up to a pixel high and a pixel short, and a chart drew itself for
 * a pixel less height.
 *
 * Pure (no DOM, no instance state) so it's straightforward to
 * unit-test in isolation.
 */
export function computePositionedOverlays(
  overlays: Record<string, OverlayInfo>,
  resources: Record<string, string>,
  colWidth: (col: number) => number,
  rowHeight: (row: number) => number,
  ppi: number = FALLBACK_PPI,
  split: { cols: number; rows: number } = { cols: 0, rows: 0 }
): PositionedOverlay[] {
  const result: PositionedOverlay[] = [];
  for (const [id, info] of Object.entries(overlays)) {
    let left = pastFreeze(info.col, split.cols);
    for (let c = 1; c < info.col; c++) {
      left += colWidth(c);
    }
    let top = pastFreeze(info.row, split.rows);
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
      topPx: top + (info.dy * ppi) / 72,
      widthPx: info.width,
      heightPx: (info.height * ppi) / 72
    };
    result.push(positioned);
  }
  return result;
}

/**
 * Coerce a value that may be either an array or a JSON-encoded
 * string into an array. The Flow runtime serializes server-side
 * property values to JSON strings when they reach the
 * {@code <vaadin-spreadsheet>} element's attribute lane, and they
 * arrive at the connector's setters as strings; the GWT-side
 * client used to JSON-parse them on the way in.
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
 * One object the server sends, as it is or as the JSON string Flow may send
 * it as; null for anything else.
 */
function coerceJson<T extends object>(value: T | string | null | undefined): T | null {
  if (value == null) {
    return null;
  }
  if (typeof value !== 'string') {
    return value;
  }
  try {
    const parsed: unknown = JSON.parse(value);
    return parsed !== null && typeof parsed === 'object' ? (parsed as T) : null;
  } catch {
    return null;
  }
}

/**
 * Object-shaped variant of {@link coerceArray}. The same JSON-string-
 * coming-from-Flow-attribute path applies to record/map setters.
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

/** The sizes the server sends: a plain array, or a typed one. */
type NumberList = number[] | Int32Array | Float32Array;

/**
 * Numeric-array variant. Typed arrays (Int32Array, Float32Array)
 * arrive intact when pushed via Flow's typed-array support; JSON
 * strings get parsed.
 */
export function coerceNumberArray(value: NumberList | string | null | undefined): NumberList {
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
 * the active range of the selection, fed to the
 * column-header element so each cell whose column is inside the
 * active range emits {@code part="column-header header-selected"}.
 */
export function selectedColumnSet(selection: SelectionState): Set<number> {
  // Every range's columns, as the GWT client marked them - not only the
  // last one added.
  const set = new Set<number>();
  for (const range of [selection.activeRange, ...selection.auxRanges]) {
    for (let c = Math.min(range.c1, range.c2); c <= Math.max(range.c1, range.c2); c++) {
      set.add(c);
    }
  }
  return set;
}

/**
 * Pure helper: symmetric row variant of {@link selectedColumnSet}.
 */
export function selectedRowSet(selection: SelectionState): Set<number> {
  const set = new Set<number>();
  for (const range of [selection.activeRange, ...selection.auxRanges]) {
    for (let r = Math.min(range.r1, range.r2); r <= Math.max(range.r1, range.r2); r++) {
      set.add(r);
    }
  }
  return set;
}

/**
 * Pure helper: is the given (col, row) inside the active range OR
 * any aux range of the current selection? A right-click outside the
 * selection moves the selection to the clicked cell, as in Excel.
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
 * Where each merged region is drawn: its box in the sheet's own pixel
 * coordinates, frozen rows and columns included.
 *
 * Every region gets one, not only those whose top-left cell is on screen: a
 * merge whose first rows have scrolled away is still showing the rest. A
 * region that starts among the frozen rows or columns and runs past the split
 * is drawn pinned with them, as the GWT client drew it, the part beyond the
 * split shortened by how far the sheet has scrolled underneath.
 *
 * @param split the number of frozen rows and columns
 * @param scroll how far the sheet has scrolled
 */
export function computeMergedRegionBoxes(
  regions: ReadonlyArray<MergedRegion>,
  split: { rows: number; cols: number },
  scroll: { left: number; top: number },
  colWidthAt: (col: number) => number,
  rowHeightAt: (row: number) => number
): MergedRegionBox[] {
  let lastCol = split.cols;
  let lastRow = split.rows;
  for (const region of regions) {
    lastCol = Math.max(lastCol, region.col2);
    lastRow = Math.max(lastRow, region.row2);
  }
  // starts[n] is where row or column n begins; starts[1] is 0.
  const startsOf = (last: number, sizeAt: (index: number) => number): Float64Array => {
    const starts = new Float64Array(last + 2);
    for (let index = 1; index <= last; index++) {
      starts[index + 1] = starts[index] + sizeAt(index);
    }
    return starts;
  };
  const colStarts = startsOf(lastCol, colWidthAt);
  const rowStarts = startsOf(lastRow, rowHeightAt);
  // A region across the split keeps its frozen part pinned and shows what
  // has not scrolled away of the rest, which starts past the freeze line.
  const extent = (starts: Float64Array, from: number, to: number, frozen: number, scrolled: number): number => {
    const whole = starts[to + 1] - starts[from];
    if (from > frozen || to <= frozen) {
      return whole;
    }
    const pinned = starts[frozen + 1] - starts[from];
    const rest = whole - pinned - scrolled;
    return rest > 0 ? pinned + freezeGap(frozen) + rest : pinned;
  };
  return regions.map((region) => ({
    left: colStarts[region.col1] + pastFreeze(region.col1, split.cols),
    top: rowStarts[region.row1] + pastFreeze(region.row1, split.rows),
    width: extent(colStarts, region.col1, region.col2, split.cols, scroll.left),
    height: extent(rowStarts, region.row1, region.row2, split.rows, scroll.top)
  }));
}

/**
 * Pure helper: convert {@link GroupingData} entries into
 * {@link PositionedGrouping} entries - where each group's bracket runs and
 * where its button sits, along the axis the sizes are for - as the GWT client
 * laid them out.
 *
 * A group's summary row or column holds its button: by default the one after
 * the group, so the bracket runs from just inside the group's first header
 * ({@code startPaddingPx} in) to the middle of the next one, where the button
 * is. Inversed, the summary is the one before: the bracket runs from the
 * middle of that header to the group's end, and the button sits at its start.
 *
 * Both indices are 0-based in the wire format (server-side
 * `GroupingData.startIndex`/`endIndex` use POI's 0-based row/col
 * indices). The connector's `colWidths` array is 0-indexed too.
 *
 * @param startPaddingPx how far into its first header a group's bracket
 * starts - the GWT client's 5px for columns, 2px for rows
 * @param shiftPx how far back along the axis every bracket and button is
 * drawn - a pixel while the other outline gutter shows, which
 * the GWT client measured without its border
 */
export function computePositionedColGroupings(
  groupings: ReadonlyArray<GroupingData>,
  colWidths: ArrayLike<number>,
  inversed: boolean,
  defColWidthPx: number,
  startPaddingPx = 0,
  shiftPx = 0
): PositionedGrouping[] {
  const widthAt = (col0: number): number => {
    if (col0 < 0) {
      return 0;
    }
    return col0 < colWidths.length ? colWidths[col0] : defColWidthPx;
  };
  const positions: PositionedGrouping[] = [];
  for (const g of groupings) {
    let groupStart = 0;
    for (let i = 0; i < g.startIndex; i++) {
      groupStart += widthAt(i);
    }
    let groupEnd = groupStart;
    for (let i = g.startIndex; i <= g.endIndex; i++) {
      groupEnd += widthAt(i);
    }
    const summaryBefore = groupStart - widthAt(g.startIndex - 1) / 2;
    const summaryAfter = groupEnd + widthAt(g.endIndex + 1) / 2;
    positions.push({
      uniqueIndex: g.uniqueIndex,
      level: g.level,
      startPx: (inversed ? summaryBefore : groupStart + startPaddingPx) - shiftPx,
      endPx: (inversed ? groupEnd : summaryAfter) - shiftPx,
      buttonPx: (inversed ? summaryBefore : summaryAfter) - shiftPx,
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
 */
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
      // A plain selection replaces every earlier range;
      // Ctrl-click sends events of its own.
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
 * Where Home and End take the active cell: the first or last column of its
 * row, or with Ctrl (Cmd on a Mac) the first cell of the sheet or the last.
 * Null for any other key.
 */
export function homeEndTarget(
  key: string,
  toSheetCorner: boolean,
  activeCell: { col: number; row: number },
  cols: number,
  rows: number
): { col: number; row: number } | null {
  if (key === 'Home') {
    return { col: 1, row: toSheetCorner ? 1 : activeCell.row };
  }
  if (key === 'End') {
    return { col: Math.max(1, cols), row: toSheetCorner ? Math.max(1, rows) : activeCell.row };
  }
  return null;
}
