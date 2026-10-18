/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { ReactiveController, ReactiveControllerHost } from 'lit';
import type {
  CellData,
  GroupingData,
  MergedRegion,
  OverlayInfo,
  PopupButtonState,
  SizeRuns,
  SpreadsheetActionDetails
} from './types/wire.js';
import type { Json, SpreadsheetClientApi } from './types/rpc-server-to-client.js';
import {
  initialSheetSelection,
  pickCell,
  replaceRange,
  type SheetSelection,
  addAuxRange,
  buildSelectionEvent,
  type CellPosition,
  type CellRange,
  isCellInSelection,
  normalizedRange,
  selectedCellSetBy,
  selectionSizeLabel,
  type SelectionState,
  selectSingleCell
} from './state/SelectionState.js';
import type {
  SpreadsheetEvent,
  CellRangePaintedData,
  CellRangeSelectedData,
  CellAddedToSelectionData,
  CellSelectedData,
  CellValueEditedData,
  ColumnSelectedData,
  RowSelectedData
} from './types/rpc-client-to-server.js';
import type { CellEditorState } from './elements/types.js';
import {
  computeViewportBounds,
  FALLBACK_PPI,
  freezeGap,
  getDefaultRowHeightPx,
  pagedViewInset,
  scrollToShow,
  withSize
} from './algorithms/ViewportMath.js';
import { type CellSlots, Derived, drawnColumnWidth, drawnRowHeight, type SheetSizes } from './state/Derived.js';
import { RenderScheduler } from './state/RenderScheduler.js';
import {
  type CellPress,
  closeEditor,
  type Edit,
  formulaOf,
  type FormulaWriting,
  IDLE,
  type InteractionState,
  openEditor,
  withFormula
} from './state/Interaction.js';
import { SheetData } from './state/SheetData.js';
import { ViewState } from './state/ViewState.js';
import {
  initialSheetModel,
  type SHEET_PROPERTIES,
  type SheetModel,
  type SheetPropertyName,
  type SheetPropertyValue,
  withProperty,
  withValues
} from './state/SheetModel.js';
import { initialEditValue } from './algorithms/PercentageEntry.js';
import { cellCoordToAddress } from './algorithms/CellRef.js';
import { parseColRow, toKey } from './algorithms/CellKeys.js';
import {
  actionIconSlotName,
  cellComponentSlotName,
  cellEditorSlotName,
  type CellSlotAssignment,
  flowVirtualChildResolver,
  overlayComponentSlotName,
  SlottedVirtualChildren,
  visibleCellSlots
} from './dom/VirtualChildren.js';
import {
  computePaintRectangle,
  grownSelection,
  isNonEmpty,
  type CellRect,
  type PaintOutcome
} from './algorithms/SelectionPaint.js';
import { expandToMergedRegions } from './algorithms/MergedSelection.js';
import { inSelectionWalkDirection, isMultiCellRange, walkInsideSelection } from './algorithms/SelectionWalk.js';
import {
  canPoint,
  formulaReferences,
  isFormula,
  placeReference,
  referenceAt,
  referenceText
} from './algorithms/FormulaPointing.js';
import {
  homeEndTarget,
  isSingleMerge,
  keyDirection,
  moveActiveCell,
  moveRangeEdge,
  selectCellAt,
  type SheetShape,
  type SingleCellSelection,
  typesCharacter
} from './algorithms/KeyboardMoves.js';
import { SpreadsheetCellGrid } from './elements/SpreadsheetCellGrid.js';
import { listen } from './dom/Listen.js';
import { listenToClipboard } from './controllers/Clipboard.js';
import { type PageListener, PageListeners } from './controllers/PageListeners.js';
import { SheetTabs } from './controllers/SheetTabs.js';
import { ContextMenu } from './controllers/ContextMenu.js';
import { HeaderResize } from './controllers/HeaderResize.js';
import { Popup } from './controllers/Popup.js';
import { listenForLongPress } from './dom/LongPress.js';
import { hideFromTopLayer, showInTopLayer } from './dom/TopLayer.js';
import { linkTooltipPosition } from './algorithms/Placement.js';
import { computeMergedRegionBoxes, computePositionedOverlays } from './algorithms/Positioning.js';
import { flowRequestInFlight, overlayComponentAppId } from './dom/FlowClient.js';
import { isIOS, isMacPlatform } from './dom/Platform.js';
import { coerceArray, coerceJson } from './wire/Coerce.js';
import {
  columnGroupingGutterPx,
  rowGroupingGutterPx,
  SpreadsheetColumnHeader
} from './elements/SpreadsheetColumnHeader.js';
import { SpreadsheetRowHeader } from './elements/SpreadsheetRowHeader.js';
import { SpreadsheetHeaderCorner } from './elements/SpreadsheetHeaderCorner.js';
import { SpreadsheetSheetTabs } from './elements/SpreadsheetSheetTabs.js';
import { type FormulaBarRenderState, SpreadsheetFormulaBar } from './elements/SpreadsheetFormulaBar.js';
import { SpreadsheetContextMenu } from './elements/SpreadsheetContextMenu.js';
import { type LoadRange, ViewportLoader } from './state/ViewportLoader.js';
import type { CellGridRenderState } from './elements/types.js';

/**
 * Sample the cell-style width measurement uses: ten digits, as earlier
 * versions measured, so the ratios keep the same meaning server-side.
 */
const WIDTH_PROBE_TEXT = '5555555555';

/**
 * The shortest time between two reports of a write to a locked cell, as the
 * GWT client kept it: the server's ProtectedEditListener promises one event a
 * second at most.
 */
const PROTECTED_WRITE_REPORT_INTERVAL_MS = 1000;

/** The style index a cell's class list names - "cs5 r cf2" is 5 - or null. */
function styleIndexOf(cellStyle: string | undefined): number | null {
  const match = /(?:^|\s)cs(\d+)(?:\s|$)/.exec(cellStyle ?? '');
  return match ? Number(match[1]) : null;
}

/**
 * Height of the column headers, in pixels: 27, so a sheet starts its first
 * row where it always did.
 */
const DEFAULT_HEADER_ROW_HEIGHT = 27;

/**
 * Width of the row headers, in pixels: 50, as in earlier versions, which
 * also fits a six-digit row number.
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
/** The server methods Flow gives the sheet's host element; see the server's {@code Spreadsheet#clipboardText}. */
type ServerHost = HTMLElement & {
  $server?: { clipboardText(col1: number, row1: number, col2: number, row2: number): Promise<string | null> };
};

/**
 * Whether a key pressed in a custom editor takes the keyboard back to the
 * sheet. Enter does, unless the editor used it: for a new line in a text
 * area or in rich text, or to pick from a list it has open.
 */
function leavesEditor(e: KeyboardEvent): boolean {
  if (e.key === 'Escape' || e.key === 'Tab') {
    return true;
  }
  if (e.key !== 'Enter' || e.defaultPrevented) {
    return false;
  }
  const target = e.composedPath()[0];
  return !(target instanceof HTMLTextAreaElement) && !(target instanceof HTMLElement && target.isContentEditable);
}

export class Spreadsheet implements SpreadsheetClientApi, ReactiveController {
  /**
   * What the sheet asks about the platform when it is made. Tests, which do
   * not run on iOS, stand in for it here.
   */
  static platform = { isIOS };

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

  /**
   * What the server mirrors onto the element, each property by its name on
   * the wire; see {@link SheetModel}. Replaced whole on each change.
   */
  private model: SheetModel = initialSheetModel();

  /**
   * The cells selected and the cell a merge was entered by; see
   * {@link SheetSelection}. Changed through {@link select} and {@link pick}.
   */
  private sheetSelection: SheetSelection = initialSheetSelection();

  /** The cells selected. */
  private get selection(): SelectionState {
    return this.sheetSelection.cells;
  }

  /**
   * Whether the release of the press under way reported the range it
   * selected. A shift-click is a press, which selects the range, and a
   * click: reported on both, the server heard the same range twice.
   */
  private rangeReportedOnRelease = false;

  /**
   * The cell a click or an arrow asked for when the selection is a merged
   * region: the arrows leave the merge in its column and row. Null when
   * the selection is not a merge.
   */
  private get mergeEntry(): { col: number; row: number } | null {
    return this.sheetSelection.mergeEntry;
  }

  /**
   * What the user is in the middle of on the sheet: an edit and the formula
   * written in it, a press on the cells, a note; see {@link InteractionState}.
   * Changed through {@link interact}, which draws the change.
   */
  private interaction: InteractionState = IDLE;

  /** The cell editor while it is open: the cell-grid draws an input on its cell. */
  private get editor(): CellEditorState | null {
    const edit = this.interaction.edit;
    return edit.kind === 'editing' ? edit.editor : null;
  }

  /** The formula being written, in the cell editor or the formula field. */
  private get formula(): FormulaWriting {
    return formulaOf(this.interaction.edit);
  }

  /** A link cell's tooltip, shown while the pointer is over the cell. */
  private readonly linkTooltip: HTMLElement;

  /** Resizing columns and rows by their header edges; see {@link HeaderResize}. */
  private readonly headerResize: HeaderResize;

  /** What the sheet draws that is worked out from the model and the selection alone; see {@link Derived}. */
  private readonly derived = new Derived();

  /** The element the `resource-<id>` attributes live on; see {@link setResources}. */
  private resourceHost: HTMLElement | null = null;

  /**
   * What the client knows of the sheet the server shows: its cells, the cell
   * the server holds as selected, and whether the server was told where the
   * sheet is. Replaced whole on a reload; see {@link setReload}.
   */
  private sheetData = new SheetData();

  /**
   * What the view remembers from one drawing to the next: the row to keep in
   * place, the scroll to restore, the cell style widths measured.
   */
  private readonly view = new ViewState();

  /** When the sheet is drawn; see {@link RenderScheduler}. */
  private readonly renders = new RenderScheduler(() => this.recomputeViewport());

  /** The context menu shown on right-click. Empty until the server answers. */
  private readonly contextMenu: SpreadsheetContextMenu;
  /** The context menu's state and what it does; see {@link ContextMenu}. */
  private readonly menu: ContextMenu;
  /** The popup buttons and the popup one opens; see {@link Popup}. */
  private readonly popup: Popup;

  /** The cell-grid sub-element. Created in the constructor + appended to the host. */
  private readonly cellGrid: SpreadsheetCellGrid;

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
   * Icons on context-menu actions. Server-side components attached to the
   * action, so they need placing like any other; a fourth holder because
   * they live in the context menu's shadow root rather than the grid's.
   */
  private readonly actionIcons: SlottedVirtualChildren;

  /**
   * The parts that follow the sheet into the page and out of it, in the
   * order they join it; see {@link connect}.
   */
  private readonly parts: ReactiveController[];
  /** The listeners on the sheet's own elements, released by {@link dispose}. */
  private readonly ownListeners = new AbortController();

  /** Decides when the server is asked for the cells the sheet scrolls to. */
  private readonly viewportLoader = new ViewportLoader({
    drawnRange: () => this.drawnRange(),
    sheet: () => ({
      rows: this.model.rows,
      cols: this.model.cols,
      frozenRows: this.model.verticalSplitPosition,
      frozenCols: this.model.horizontalSplitPosition,
      rowPx: getDefaultRowHeightPx(this.model.defRowH, FALLBACK_PPI),
      colPx: this.model.defColW,
      noMargin: this.model.hasComponentFactory
    }),
    serverBusy: () => flowRequestInFlight(this.host),
    load: (range) => this.sendLoad(range)
  });

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
  /** The sheet tabs' state and what they do; see {@link SheetTabs}. */
  private readonly tabs: SheetTabs;

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
   * ("hidefunctionbar") hide the matching internal elements. It watches
   * the host in the page and out of it, until {@link dispose}: a change
   * made while the host is out of the page is there when it comes back.
   */
  private readonly hostClassObserver: MutationObserver;

  /**
   * Height of the column-header strip in pixels, its bottom border
   * included. {@code 0} when the sheet hides its headings, which
   * collapses the strip.
   */
  private get headerRowHeight(): number {
    return this.model.displayRowColHeadings ? DEFAULT_HEADER_ROW_HEIGHT : 0;
  }

  /**
   * Width of the row-header strip in pixels, its right border included.
   * {@code 0} when the sheet hides its headings.
   */
  private get headerColumnWidth(): number {
    return this.model.displayRowColHeadings ? DEFAULT_HEADER_COLUMN_WIDTH : 0;
  }

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
   * Resize observer on the scroll container. Fires when the host's
   * box size changes — either initially when the host is attached
   * + given dimensions, or later when the parent layout changes
   * (window resize, container reflow). Each fire triggers
   * {@link recomputeViewport}, which pushes a fresh render state
   * with the new {@code clientWidth} / {@code clientHeight}. The
   * initial fire also serves as the first render, replacing the
   * empty default state assigned in the constructor.
   *
   * The only one needed: the scroll container changes size with the
   * host, and also when the formula bar, the tabs or the headings come
   * and go.
   */
  private readonly resizeObserver: ResizeObserver;
  /** Stops the long press opening the menu, on iOS; see listenForLongPress. */
  private readonly stopLongPress: () => void;

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
    this.actionIcons = new SlottedVirtualChildren(host, this.contextMenu, resolveVirtualChild, actionIconSlotName);
    this.popup = new Popup(
      host,
      resolveVirtualChild,
      this.cellGrid,
      {
        cellValue: (col, row) => this.sheetData.cells.get(toKey(col, row))?.value ?? '',
        paneClientRect: () => this.scrollingPaneClientRect(),
        focusSheet: () => this.scrollContainer.focus({ preventScroll: true }),
        scheduleRender: () => this.scheduleRender(),
        send: (event) => this.sendToServer(event)
      },
      this.ownListeners.signal
    );
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
    // min-height is a floor: a spreadsheet nobody has given a height to is still a spreadsheet,
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
    // every pixel left over once the host does have a height.
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
      'outline:none;' +
      // Cells and headers are not text to select: a drag selects cells, and a long press on a phone opens the sheet's
      // menu rather than the browser's. Fields typed into still select.
      'user-select:none;-webkit-user-select:none;-webkit-touch-callout:none;';
    // tabindex=0 makes the scroll container focusable so it can
    // receive keydown events for keyboard navigation.
    this.scrollContainer.tabIndex = 0;
    // Header corner at (col 1, row 1) — sticky on both axes
    // so it overlays both header strips at their intersection.
    // Hidden when either header dimension is 0.
    this.headerCorner.className = 'sheet-header-corner';
    this.headerCorner.style.cssText =
      'grid-column:1;grid-row:1;' + 'position:sticky;top:0;left:0;z-index:6;' + 'width:0;height:0;';
    this.on(this.headerCorner, 'level-header-click', this.handleLevelHeaderClick);
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
    this.on(this.formulaBar, 'sheet-address-changed', this.handleSheetAddressChanged);
    this.on(this.formulaBar, 'formula-commit', this.handleFormulaCommit);
    this.on(this.formulaBar, 'formula-cancel', this.handleFormulaCancel);
    this.on(this.formulaBar, 'formula-input', this.handleFormulaInput);
    this.on(this.formulaBar, 'formula-arrow', this.handleFormulaArrow);
    this.on(this.formulaBar, 'formula-caret', this.handleDraftCaret);
    this.root.appendChild(this.scrollContainer);
    // Sheet tabs sit below the scroll container in the flex
    // column. flex:0 so they take their intrinsic height,
    // never grow / shrink based on content.
    this.sheetTabs.style.flex = '0 0 auto';
    this.root.appendChild(this.sheetTabs);
    // Outside the scroll container: the keys typed into a filter's fields and the clicks on
    // its check boxes are the popup's, not the sheet's shortcuts. Beside
    // the context menu, the other overlay.
    this.contextMenu.after(this.popup.overlay);
    this.headerResize = new HeaderResize(
      this.root,
      { columnHeader: this.columnHeader, rowHeader: this.rowHeader },
      {
        sheetProtected: () => this.model.sheetProtected,
        colWidthPx: (col) => this.colWidthPx(col),
        rowHeightPx: (row) => this.rowHeightPx(row),
        rowHeightPt: (row) => (row - 1 < this.model.rowH.length ? this.model.rowH[row - 1] : this.model.defRowH),
        // A column or row dragged to nothing is hidden, as the server will
        // have it. One already hidden has no edge to drag.
        setColumnWidth: (col, width) => {
          this.change({ colW: withSize(this.model.colW, col, width, this.model.defColW) });
          if (width === 0) {
            this.change({ hiddenColumnIndexes: new Set([...this.model.hiddenColumnIndexes, col]) });
          }
        },
        setRowHeight: (row, height) => {
          this.change({ rowH: withSize(this.model.rowH, row, height, this.model.defRowH) });
          if (height === 0) {
            this.change({ hiddenRowIndexes: new Set([...this.model.hiddenRowIndexes, row]) });
          }
        },
        send: (event) => this.sendToServer(event)
      },
      this.ownListeners.signal
    );
    this.linkTooltip = document.createElement('div');
    this.linkTooltip.className = 'link-tooltip';
    this.linkTooltip.popover = 'manual';
    this.linkTooltip.hidden = true;
    this.popup.overlay.after(...this.headerResize.elements(), this.linkTooltip);
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
    this.hostClassObserver.observe(this.host, { attributes: true, attributeFilter: ['class', 'theme'] });
    // Sheet tab click → switch active sheet via spreadsheet-event.
    this.tabs = new SheetTabs(
      this.sheetTabs,
      {
        scrollOffsets: () => this.scrollOffsets(),
        focusSheet: () => this.scrollContainer.focus(),
        returnFocusToSheet: () => this.returnFocusToSheet(),
        send: (event) => this.sendToServer(event)
      },
      this.ownListeners.signal
    );
    this.on(this.scrollContainer, 'scroll', this.handleScroll);
    this.on(this.scrollContainer, 'focusin', this.handleSheetFocusChange);
    this.on(this.scrollContainer, 'focusout', this.handleSheetFocusChange);
    listenToClipboard(
      this.scrollContainer,
      {
        isEditing: () => this.editor !== null,
        activeRange: () => this.selection.activeRange,
        valueAt: (col, row) => this.sheetData.cells.get(toKey(col, row))?.value,
        hasCells: (range) => this.viewportLoader.hasLoaded(range),
        copyLater: (range) => this.copyLater(range),
        send: (event) => this.sendToServer(event)
      },
      this.ownListeners.signal
    );
    // Cell-click delegation: the cell-grid dispatches
    // a `cell-click` custom event with detail = { col, row,
    // shiftKey, ctrlKey, metaKey, altKey }. The connector updates
    // its selection + re-renders. Bubble + composed = true so the
    // event crosses the cell-grid's shadow DOM boundary.
    this.on(this.cellGrid, 'cell-click', this.handleCellClick);
    // Cell editor lifecycle events. The cell-grid
    // emits these from its shadow DOM; the connector tracks
    // editor state + dispatches cellValueEdited on commit.
    this.on(this.cellGrid, 'cell-doubleclick', this.handleCellDoubleClick);
    this.on(this.cellGrid, 'cell-editor-commit', this.handleCellEditorCommit);
    this.on(this.cellGrid, 'cell-editor-cancel', this.handleCellEditorCancel);
    this.on(this.cellGrid, 'cell-editor-input', this.handleCellEditorInput);
    this.on(this.cellGrid, 'cell-editor-arrow', this.handleEditorArrow);
    this.on(this.cellGrid, 'cell-editor-caret', this.handleDraftCaret);
    // Mouse drag selection. The cell-grid emits
    // cell-mousedown + cell-mouseover; the connector watches
    // mouseup at the document level so a drag that ends
    // outside the spreadsheet still terminates cleanly.
    this.on(this.cellGrid, 'cell-mousedown', this.handleCellMouseDown);
    this.on(this.cellGrid, 'cell-mouseover', this.handleCellMouseOver);
    this.on(this.cellGrid, 'grid-mouseleave', this.hideLinkTooltip);
    this.on(this.cellGrid, 'selection-paint-start', this.handleSelectionPaintStart);
    // Popup-button click → dispatch popupButtonClick. The
    // server's `onPopupButtonClick` opens the associated popup
    // component.
    // Context-menu wiring. Right-click on a cell
    // / column-header / row-header dispatches the matching
    // local event from the element; the connector forwards it
    // to the server as `contextMenuOpenOnSelection` /
    // `columnHeaderContextMenuOpen` / `rowHeaderContextMenuOpen`
    // after first updating the selection (Excel: right-click
    // on a cell outside the current selection selects that
    // cell, then opens the menu).
    this.menu = new ContextMenu(
      this.contextMenu,
      this.actionIcons,
      this.root,
      { cells: this.cellGrid, columnHeader: this.columnHeader, rowHeader: this.rowHeader },
      {
        // Drawn before the server hears of the cell, as from a handler of
        // the sheet's own.
        selectForMenu: (col, row) =>
          this.renders.handle(() => {
            if (!isCellInSelection(col, row, this.selection)) {
              this.selectCellOrMerge(col, row);
              this.dispatchSelectionEvent();
            }
          }),
        returnFocusToSheet: () => this.returnFocusToSheet(),
        send: (event) => this.sendToServer(event)
      },
      this.ownListeners.signal
    );
    this.on(this.cellGrid, 'cell-comment-commit', this.handleCellCommentCommit);
    // Grouping outline. The column header emits
    // `column-grouping-toggle` when the user clicks a +/-
    // button in the grouping gutter; the connector forwards
    // it as `groupingCollapsed` to the server.
    this.on(this.columnHeader, 'column-grouping-toggle', this.handleColumnGroupingToggle);
    this.on(this.rowHeader, 'row-grouping-toggle', this.handleRowGroupingToggle);
    // Header-drag resize. The header elements
    // emit `column-resize-start` / `row-resize-start` from
    // their right-edge / bottom-edge mousedown handles; the
    // connector tracks the drag via document mousemove +
    // mouseup. Live-updates colWidths/rowHeights for the
    // current drag column/row.
    // Header click delegation. Each header element
    // emits its own custom event with the col / row coordinate;
    // the connector translates them into whole-column / whole-
    // row range selections.
    this.on(this.columnHeader, 'column-header-click', this.handleColumnHeaderClick);
    this.on(this.rowHeader, 'row-header-click', this.handleRowHeaderClick);
    // Keyboard navigation. Listen on the scroll
    // container so arrow keys + shift+arrow work whenever the
    // spreadsheet has focus. preventDefault() in the handler
    // stops the browser from scrolling the viewport via the
    // arrow keys.
    this.on(this.scrollContainer, 'keydown', this.handleKeyDown);
    this.on(this.scrollContainer, 'pointerdown', this.handleHostedEditorPointerDown);
    // Safari on iOS opens no menu on a long press, so it is made a
    // right-click there, on the cells and the headers alike.
    this.stopLongPress = Spreadsheet.platform.isIOS() ? listenForLongPress(this.scrollContainer) : () => {};
    this.resizeObserver = new ResizeObserver(() => this.recomputeViewport());
    this.parts = [
      this.viewportLoader,
      new PageListeners(
        this.handlingAll([
          [document, 'pointerdown', this.handleDocumentPointerDown],
          [document, 'keydown', this.handleContextMenuKeyDown],
          [document, 'mouseup', this.handleDocumentMouseUp],
          [document, 'keyup', this.handleDocumentKeyUp],
          [document, 'mousemove', this.handleDocumentMouseMove]
        ])
      ),
      {
        hostConnected: () => this.resizeObserver.observe(this.scrollContainer),
        hostDisconnected: () => this.resizeObserver.disconnect()
      },
      this.popup
    ];
    if (isControllerHost(host)) {
      // Lit connects the sheet now, the element being in the page, and as
      // the element joins and leaves it after.
      host.addController(this);
    } else {
      this.connect();
    }
  }

  /**
   * The sheet joined the page, or came back to it after being moved: its
   * parts listen to the page and observe the host, once however often it
   * joins. A {@code <vaadin-spreadsheet>} has Lit call this, the sheet being
   * one of its controllers.
   */
  connect(): void {
    for (const part of this.parts) {
      part.hostConnected?.();
    }
  }

  /** The sheet left the page: its parts stop listening to it; see {@link connect}. */
  disconnect(): void {
    for (const part of this.parts) {
      part.hostDisconnected?.();
    }
  }

  /** Lit's: the element joined the page. */
  hostConnected(): void {
    this.connect();
  }

  /** Lit's: the element left the page. */
  hostDisconnected(): void {
    this.disconnect();
  }

  /** Listen to one of the sheet's own elements until {@link dispose}. */
  private on<E extends Event>(target: EventTarget, type: string, listener: (event: E) => void): void {
    listen(target, type, this.handling(listener), this.ownListeners.signal);
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
    if (this.formula.press !== 'none') {
      // The press pointed at the cell for a formula; it selects nothing.
      this.changeFormula({ press: 'none' });
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
      this.select({ ...extended, auxRanges: this.selection.auxRanges });
      this.dispatchCellsAddedToRangeSelection(extended.activeRange);
      return;
    }
    if (ctrlOrMeta) {
      // Non-coherent selection: the clicked cell joins the selection
      // as its own range and becomes active; the previous active
      // range is kept as an aux range.
      this.select(addAuxRange(this.selection, col, row, col, row));
      this.dispatchCellAddedToSelection(col, row);
      return;
    }
    if (shiftKey) {
      // Extend the active range from the existing active cell
      // (= the anchor) to the clicked coordinate. Ranges picked out
      // with earlier Ctrl-clicks stay: shift extends the last range,
      // it does not start the selection over.
      const anchor = this.selection.activeCell;
      this.select({
        activeCell: anchor,
        activeRange: this.rangeTo(anchor, col, row),
        auxRanges: this.selection.auxRanges
      });
      if (!this.rangeReportedOnRelease) {
        this.reportRangeSelection();
      }
      return;
    } else {
      this.selectCellOrMerge(col, row);
    }
    this.dispatchSelectionEvent();
    // Hyperlink cells additionally dispatch `linkCellClicked`
    // after the normal selection event. The
    // server's onLinkCellClicked handler opens the URL.
    const cellKey = toKey(col, row);
    if (this.model.hyperlinksTooltips[cellKey] !== undefined) {
      this.sendToServer({
        type: 'linkCellClicked',
        data: { row, column: col }
      });
    }
  };

  /** The pointer moved on the page: a header drag follows it. */
  private readonly handleDocumentMouseMove = (e: MouseEvent): void => {
    this.headerResize.moved(e);
  };

  /**
   * A link cell's tooltip - the target the server named for it - while
   * the pointer is over the cell: below the
   * cell, or above it when there is no room below. Any other cell, or a
   * drag in progress, takes it away.
   */
  private showLinkTooltip(at: { col: number; row: number; box?: DOMRect }): void {
    const text = this.model.hyperlinksTooltips[toKey(at.col, at.row)];
    const dragging = this.interaction.press !== null;
    if (text === undefined || at.box === undefined || dragging) {
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
      // Nor does it start a drag: a pointer that drifted into the next
      // cell before the button came up would replace every range picked
      // out with Ctrl with one rectangle.
      return;
    }
    this.changePress({ kind: 'select', crossed: false });
    if (shiftKey) {
      // Shift+mousedown: extend existing range to the
      // mousedown coord (treat the existing anchor as the
      // drag start). Standard Excel "shift-drag" behavior.
      // Aux ranges survive, as on a shift-click.
      const anchor = this.selection.activeCell;
      this.select({
        activeCell: anchor,
        activeRange: this.rangeTo(anchor, col, row),
        auxRanges: this.selection.auxRanges
      });
    } else {
      this.selectCellOrMerge(col, row);
    }
  };

  /**
   * Mouse drag — mouseover during drag. Extends
   * the range from the active cell (anchor) to the hovered
   * cell. Mouseover events that fire while NOT dragging are
   * silently ignored — the cell-grid emits on every hover.
   */
  private readonly handleCellMouseOver = (e: CustomEvent<{ col: number; row: number; box?: DOMRect }>): void => {
    this.showLinkTooltip(e.detail);
    const pointer = this.formula.pointer;
    if (this.formula.press === 'pointing' && pointer !== null) {
      const draft = this.formulaSource() === 'field' ? this.formulaBar.formulaFieldState() : this.editorDraft();
      if (draft !== null) {
        const end = selectCellAt(e.detail.col, e.detail.row, this.model.mergedRegions).activeCell;
        this.pointAt(pointer.anchor, end, draft.value, pointer.span.end, pointer.span, this.formulaSource() ?? 'cell');
      }
      return;
    }
    const press = this.interaction.press;
    if (press?.kind === 'fill') {
      const { col, row } = e.detail;
      this.changePress({ ...press, outcome: computePaintRectangle(press.from, col, row) });
      return;
    }
    if (press?.kind !== 'select') {
      return;
    }
    if (!press.crossed) {
      this.changePress({ kind: 'select', crossed: true });
    }
    const { col, row } = e.detail;
    // Same as shift-arrow: keep activeCell as anchor, move
    // the extend corner.
    // The ranges picked out before stay: a plain press has already let
    // them go, and a Shift-press drags out the range it extends.
    const anchor = this.selection.activeCell;
    this.select({
      activeCell: anchor,
      activeRange: this.rangeTo(anchor, col, row),
      auxRanges: this.selection.auxRanges
    });
    this.showRangeSizeInAddressField();
  };

  /**
   * While a range is being dragged out, the address field counts it
   * rather than naming a cell — Excel's "3R x 2C". The count is what
   * the user needs mid-drag; the address comes back when the server
   * answers the finished selection.
   */
  private showRangeSizeInAddressField(): void {
    this.changeFormulaBar({ address: selectionSizeLabel(this.selection.activeRange) });
  }

  /**
   * Mouse drag — mouseup end. Closes the drag.
   * Dispatches a {@code cellRangeSelected} event when the
   * final range covers more than one cell (single-cell drag
   * is treated as a click, whose `cell-click` handler already
   * dispatched `cellSelected`).
   */
  private readonly handleDocumentMouseUp = (): void => {
    const press = this.interaction.press;
    if (press?.kind === 'fill') {
      this.finishPainting(press);
      return;
    }
    if (this.headerResize.finish()) {
      return;
    }
    if (this.formula.press === 'pointing') {
      // The press that pointed is over; the click it ends in is to come.
      this.changeFormula({ press: 'pointed' });
      return;
    }
    if (press === null) {
      return;
    }
    this.changePress(null);
    if (press.kind === 'select' && this.selectsRange()) {
      // Multi-cell range — dispatch the server-bound
      // selection event so the server learns the final
      // dragged range: with the cell the drag started on, when it went
      // into other cells. One that stayed in its cell was a shift-click,
      // which grows the range from the selected cell.
      this.reportRangeSelection(press.crossed);
      // The click that may follow the release reports nothing more.
      this.rangeReportedOnRelease = true;
      setTimeout(() => (this.rangeReportedOnRelease = false));
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
    // is what carries a text cell's leading apostrophe.
    const initialValue = this.formulaFieldValueFor(col, row);
    // Opening the existing value to change it: arrows move the caret.
    this.changeEdit(openEditor(this.interaction.edit, { col, row, initialValue, mode: 'edit' }, false));
    this.showInFormulaField(initialValue);
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
    if (!this.model.sheetProtected) {
      return false;
    }
    const cellData = this.sheetData.cells.get(toKey(col, row));
    if (cellData !== undefined) {
      return cellData.locked === true;
    }
    return this.model.lockedColumnIndexes.has(col) && this.model.lockedRowIndexes.has(row);
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
    // An editor that has been closed commits nothing: its input losing the
    // keyboard as it goes is not the user leaving it.
    if (this.editor?.col !== col || this.editor.row !== row) {
      return;
    }
    // Left for the formula bar, the edit carries on in the formula field:
    // nothing is committed yet, and the editor stays open. Gone on to the
    // address field instead, it ends once the keyboard has settled there.
    if (key === undefined && relatedTarget === this.formulaBar) {
      e.preventDefault();
      setTimeout(() =>
        this.renders.handle(() => {
          if (this.editor?.col === col && this.editor.row === row && !this.formulaBar.formulaFieldHasFocus()) {
            this.commitCellEdit(col, row, this.cellGrid.editorValue() ?? value, undefined, false, false);
          }
        })
      );
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
    this.endFormulaPointing();
    this.changeEdit(closeEditor(this.interaction.edit));
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
    const walks = walkDirection !== null && this.model.rows > 0 && this.model.cols > 0 && this.selectsRange();
    // Moving on goes by the arrows' rules: over hidden rows and columns,
    // and out of a merge on its far side.
    const direction = walks || key === undefined ? null : keyDirection(key, shiftKey);
    const next =
      direction !== null && this.model.rows > 0 && this.model.cols > 0
        ? moveActiveCell({ col, row }, this.mergeEntry, direction, this.sheetShape())
        : null;
    if (next !== null) {
      this.pick(next);
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
          hiddenColumns: this.model.hiddenColumnIndexes,
          hiddenRows: this.model.hiddenRowIndexes
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
    this.endFormulaPointing();
    this.changeEdit(closeEditor(this.interaction.edit));
    // The formula field showed what was typed; it shows the cell again.
    if (editor !== null) {
      this.showInFormulaField(this.formulaFieldValueFor(editor.col, editor.row));
    }
    this.returnFocusToSheet();
  };

  /** Typed into the cell editor: the formula field shows it as it is typed. */
  private readonly handleCellEditorInput = (e: CustomEvent<{ value: string; caret?: number }>): void => {
    this.showInFormulaField(e.detail.value);
    // Typing ends pointing at that reference: the next arrow after an
    // operator points at a new one, from the cell pointed at last.
    this.changeFormula({ pointer: null });
    this.updateFormulaDraft(e.detail.value, e.detail.caret ?? e.detail.value.length);
  };

  /**
   * Typed into the formula field while the edit started in the cell: the
   * cell editor shows it as it is typed.
   */
  private readonly handleFormulaInput = (e: CustomEvent<{ value: string; caret?: number }>): void => {
    if (this.editor !== null) {
      this.cellGrid.setEditorValue(e.detail.value);
    }
    this.changeFormula({ pointer: null });
    this.updateFormulaDraft(e.detail.value, e.detail.caret ?? e.detail.value.length);
  };

  /** The caret moved in the formula being written, or the edit just began. */
  private readonly handleDraftCaret = (e: CustomEvent<{ value: string; caret: number }>): void => {
    if (this.editor === null && !this.formulaBar.formulaFieldHasFocus()) {
      // From an editor on its way out - the edit is over.
      return;
    }
    const { value, caret } = e.detail;
    if (this.formula.pointer !== null && caret !== this.formula.pointer.span.end) {
      // Moved away from what was pointed at: the next point starts afresh.
      this.changeFormula({ pointer: null });
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
   * Point at a cell with an arrow while a formula is written: after an
   * operator, a bracket or a separator the
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
    if (!isFormula(value) || this.model.rows <= 0 || this.model.cols <= 0) {
      return;
    }
    const pointer =
      this.formula.pointer !== null && caret === this.formula.pointer.span.end ? this.formula.pointer : null;
    if (pointer === null && !canPoint(value, caret)) {
      return;
    }
    e.preventDefault();
    const from = pointer?.end ?? this.formula.lastPointed ?? this.formulaCell();
    const next = moveActiveCell(from, null, keyDirection(key, false)!, this.sheetShape());
    const end = next?.activeCell ?? from;
    const anchor = shiftKey && pointer !== null ? pointer.anchor : end;
    this.pointAt(anchor, end, value, caret, pointer?.span ?? null, source);
    this.scrollCellIntoView(end);
  }

  /**
   * Point at a cell with the mouse while a formula is written: a click puts
   * a reference to it into the formula - in place of the reference pointed
   * at a moment ago, or the one the caret is in - a drag stretches it into a range, Shift stretches the last one to
   * the cell, and Ctrl or Cmd adds another after a comma.
   */
  private pointWithMouse(col: number, row: number, shiftKey: boolean, add: boolean, source: 'cell' | 'field'): void {
    const draft = source === 'field' ? this.formulaBar.formulaFieldState() : this.editorDraft();
    if (draft === null) {
      return;
    }
    let { value, caret } = draft;
    const cell = selectCellAt(col, row, this.model.mergedRegions).activeCell;
    const pointer =
      this.formula.pointer !== null && caret === this.formula.pointer.span.end ? this.formula.pointer : null;
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
    this.changeFormula({ press: 'pointing' });
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
    const edit = this.interaction.edit;
    if (edit.kind === 'editing' && edit.autoPercent && value.endsWith('%')) {
      // The % a sign typed into a percentage cell was given belongs to a
      // number. Pointing makes the entry a formula, where "+A1%" would take
      // a hundredth of A1. It goes with the editor that entry opened: a %
      // in a formula written once that editor has closed is the formula's.
      value = value.slice(0, -1);
      caret = Math.min(caret, value.length);
    }
    if (edit.kind === 'editing' && edit.autoPercent) {
      this.changeEdit({ ...edit, autoPercent: false });
    }
    const placed = placeReference(value, caret, span, referenceText(anchor, end));
    this.changeFormula({ pointer: { anchor, end, span: { start: placed.start, end: placed.end } }, lastPointed: end });
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
    return this.editor === null ? null : this.cellGrid.editorDraft();
  }

  /** The cell the formula being written is for. */
  private formulaCell(): { col: number; row: number } {
    return this.editor !== null ? { col: this.editor.col, row: this.editor.row } : this.selection.activeCell;
  }

  /** The name of the sheet shown. */
  private activeSheetName(): string {
    return this.tabs.shownSheetName();
  }

  /** Colours the references of the formula being written, or none when it is not one. */
  private updateFormulaDraft(text: string | null, caret: number): void {
    const draft = text !== null && isFormula(text) ? { text, caret } : null;
    const before = this.formula.draft;
    if (draft === null && before === null) {
      return;
    }
    if (draft !== null && before !== null && draft.text === before.text && draft.caret === before.caret) {
      return;
    }
    this.changeFormula({ draft });
    const refs = draft === null ? [] : formulaReferences(draft.text, this.activeSheetName());
    this.changeFormulaBar({
      highlights:
        draft === null
          ? null
          : { text: draft.text, references: refs.map((ref) => ({ start: ref.start, end: ref.end, color: ref.color })) }
    });
  }

  /** The formula is written or thrown away: nothing is pointed at or coloured any more. */
  private endFormulaPointing(): void {
    this.updateFormulaDraft(null, 0);
    this.changeFormula({ pointer: null, lastPointed: null });
  }

  /**
   * Changes what the formula bar shows. Every change goes through here, so
   * what one does not name stays as it is - the colours of a formula being
   * written among it.
   */
  private changeFormulaBar(change: Partial<FormulaBarRenderState>): void {
    this.formulaBar.renderState = { ...this.formulaBar.renderState, ...change };
  }

  /** What the formula field shows - the cell editor's value, while it is open. */
  private showInFormulaField(value: string): void {
    this.changeFormulaBar({ formula: value });
  }

  /**
   * Shows the value of a cell the server named in the formula field, unless
   * the user is writing one: in the cell editor, which the field mirrors,
   * or in the field itself, for the cell they are still on. A formula being
   * written in the field whose text this replaces is over, and so are its
   * colours.
   */
  private showServerValueInFormulaField(value: string, sameCell: boolean): void {
    if (this.editor !== null || (sameCell && this.formulaBar.formulaFieldEdited())) {
      return;
    }
    if (this.formula.draft !== null && this.formula.draft.text !== value) {
      this.endFormulaPointing();
    }
    this.showInFormulaField(value);
  }

  /**
   * Show a committed value in its cell at once. The server's
   * `cellsUpdated` callback may arrive much later (or not at all in offline
   * / TestBench scenarios); the cell is updated before the round-trip, as
   * Excel users expect - except for a formula, which keeps showing the old
   * value until the server has worked it out. Preserves
   * the existing CellData metadata when present (style index, locked flag).
   */
  private showCommittedValue(col: number, row: number, value: string): void {
    if (value.startsWith('=') || value.startsWith('+')) {
      return;
    }
    const cellKey = toKey(col, row);
    const previous = this.sheetData.cells.get(cellKey);
    this.sheetData.cells.set(cellKey, {
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
    // Drawn whichever way the edit was committed: leaving the formula field
    // asks for no repaint of its own.
    this.scheduleRender();
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
   * column (rows 1..this.model.rows). The active cell becomes the
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
    const rows = this.model.rows > 0 ? this.model.rows : 1;
    const row = this.firstRowOnScreen();
    if ((ctrlKey || metaKey) && !shiftKey) {
      // The whole column joins the selection as a further range.
      this.select(addAuxRange(this.selection, col, 1, col, rows, { col, row }));
      const data: ColumnSelectedData = { column: col, firstRowIndex: row };
      this.sendToServer({ type: 'columnAddedToSelection', data });
      return;
    }
    if (shiftKey) {
      // Extend across columns, keeping ranges added with Ctrl.
      const anchor = this.selection.activeCell;
      this.select({
        activeCell: anchor,
        activeRange: normalizedRange(anchor.col, 1, col, rows),
        auxRanges: this.selection.auxRanges
      });
    } else {
      this.select({
        activeCell: { col, row },
        activeRange: normalizedRange(col, 1, col, rows),
        auxRanges: []
      });
    }
    this.dispatchSelectionEvent();
  };

  /**
   * The row a click on a column letter makes active: the first one whose top
   * is on screen, as in GWT, or row 1 when rows are frozen. Row 1 of a sheet
   * scrolled far down is off screen, and the keys went there.
   */
  private firstRowOnScreen(): number {
    if (this.model.verticalSplitPosition > 0 || this.model.rows <= 0) {
      return 1;
    }
    let row = this.sheetSizes().rows.reach(1, this.scrollContainer.scrollTop);
    while (row < this.model.rows && this.model.hiddenRowIndexes.has(row)) {
      row++;
    }
    return row;
  }

  /** The column a click on a row number makes active; see {@link firstRowOnScreen}. */
  private firstColumnOnScreen(): number {
    if (this.model.horizontalSplitPosition > 0 || this.model.cols <= 0) {
      return 1;
    }
    let col = this.sheetSizes().columns.reach(1, this.scrollContainer.scrollLeft);
    while (col < this.model.cols && this.model.hiddenColumnIndexes.has(col)) {
      col++;
    }
    return col;
  }

  /**
   * Row-header click handler. Mirror of the column
   * variant — selects an entire row (cols 1..this.model.cols).
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
    const cols = this.model.cols > 0 ? this.model.cols : 1;
    const col = this.firstColumnOnScreen();
    if ((ctrlKey || metaKey) && !shiftKey) {
      // The whole row joins the selection as a further range.
      this.select(addAuxRange(this.selection, 1, row, cols, row, { col, row }));
      const data: RowSelectedData = { row, firstColumnIndex: col };
      this.sendToServer({ type: 'rowAddedToRangeSelection', data });
      return;
    }
    if (shiftKey) {
      // Extend across rows, keeping ranges added with Ctrl.
      const anchor = this.selection.activeCell;
      this.select({
        activeCell: anchor,
        activeRange: normalizedRange(1, anchor.row, cols, row),
        auxRanges: this.selection.auxRanges
      });
    } else {
      this.select({
        activeCell: { col, row },
        activeRange: normalizedRange(1, row, cols, row),
        auxRanges: []
      });
    }
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
    // Enter and Space on a button of the sheet's own - an outline button, a
    // popup's close box - press that button, as on any button, rather than
    // editing the active cell.
    if ((e.key === 'Enter' || e.key === ' ') && pressesButton(e)) {
      return;
    }
    if (this.eventCameFromHostedComponent(e)) {
      // The key belongs to the component the user is typing into. Acting
      // on it as a sheet shortcut moves the selection out from under a
      // custom editor mid-edit, which takes the editor off the sheet.
      //
      // Three keys are the way back out of a custom editor, so they are the
      // sheet's after all: Escape returns the focus, Tab returns it and then
      // moves on to the next cell, and Enter returns it once the editor has
      // had it, leaving the cell selected, as in GWT.
      const fromEditor = this.eventCameFromCellEditor(e);
      if (!fromEditor || !leavesEditor(e)) {
        return;
      }
      this.scrollContainer.focus();
      if (e.key !== 'Tab') {
        return;
      }
      e.preventDefault();
    }
    const key = e.key;
    const ctrlOrMeta = e.ctrlKey || e.metaKey;
    // The key that holds a shortcut - select all, undo, redo: Cmd on a Mac,
    // where Ctrl+A and Ctrl+Z do nothing, and Ctrl or Cmd elsewhere.
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
      if (this.model.rows <= 0 || this.model.cols <= 0) {
        return;
      }
      e.preventDefault();
      this.select({
        activeCell: { col: 1, row: 1 },
        activeRange: normalizedRange(1, 1, this.model.cols, this.model.rows),
        auxRanges: []
      });
      this.dispatchSelectionEvent(true);
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
    // So does Enter on a single cell, as it always has. With a range
    // selected Enter
    // walks inside it instead, and on a cell the user cannot see there
    // is nothing to edit, so it falls through to moving on.
    const entersEditor =
      key === 'F2' ||
      (key === 'Enter' &&
        !this.selectsRange() &&
        !this.model.hiddenRowIndexes.has(this.selection.activeCell.row) &&
        !this.model.hiddenColumnIndexes.has(this.selection.activeCell.col));
    if (entersEditor && !this.editor) {
      if (this.model.rows <= 0 || this.model.cols <= 0) {
        return;
      }
      const { col, row } = this.selection.activeCell;
      if (this.isCellLockedForEdit(col, row)) {
        this.reportProtectedCellWriteAttempt();
        return;
      }
      e.preventDefault();
      const editor: CellEditorState = {
        col,
        row,
        initialValue: this.formulaFieldValueFor(col, row),
        // F2 opens the existing value, so arrows move the caret.
        mode: 'edit'
      };
      this.changeEdit(openEditor(this.interaction.edit, editor, false));
      this.showInFormulaField(editor.initialValue);
      return;
    }

    // Type-to-edit: a key that types a character opens the cell editor at
    // the active cell with that character as the initial value - Excel's
    // way: typing directly starts replacing the cell's content. A
    // character typed with AltGr or Option counts; see typesCharacter.
    if (!this.editor && typesCharacter(e, isMacPlatform())) {
      if (this.model.rows <= 0 || this.model.cols <= 0) {
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
      // Space opens it empty: it starts an edit and types nothing.
      const editor: CellEditorState = {
        col,
        row,
        initialValue: key === ' ' ? '' : initialEditValue(key, this.sheetData.cells.get(toKey(col, row))),
        mode: 'enter'
      };
      this.changeEdit(openEditor(this.interaction.edit, editor, key !== ' ' && editor.initialValue !== key));
      this.showInFormulaField(editor.initialValue);
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
      if (this.model.rows <= 0 || this.model.cols <= 0) {
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
    if (walkDirection !== null && this.model.rows > 0 && this.model.cols > 0 && this.selectsRange()) {
      e.preventDefault();
      const next = walkInsideSelection({
        range: this.selection.activeRange,
        col: this.selection.activeCell.col,
        row: this.selection.activeCell.row,
        direction: walkDirection,
        hiddenColumns: this.model.hiddenColumnIndexes,
        hiddenRows: this.model.hiddenRowIndexes
      });
      this.moveActiveCellInsideSelection(next);
      return;
    }

    // The arrows - and Tab and Enter, outside a range - step over hidden
    // rows and columns, onto a merge as a
    // whole and out of it on the far side, and not at all at the edge of
    // the sheet. Shift+arrow moves the side of the range away from the
    // active cell instead.
    const direction = keyDirection(key, e.shiftKey);
    if (direction !== null) {
      if (this.model.rows <= 0 || this.model.cols <= 0) {
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
        this.select({ activeCell: this.selection.activeCell, activeRange: grown.range, auxRanges: [] });
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
      this.dispatchSelectionEvent();
      return;
    }

    // Home / End go to the first / last column of the row, Ctrl+Home to
    // A1 and Ctrl+End to the last cell.
    const target = homeEndTarget(
      key,
      e.ctrlKey || e.metaKey,
      this.selection.activeCell,
      this.model.cols,
      this.model.rows
    );
    if (!target) {
      return;
    }
    if (this.model.rows <= 0 || this.model.cols <= 0) {
      return;
    }
    e.preventDefault();
    this.moveSelectionTo(selectCellAt(target.col, target.row, this.model.mergedRegions));
    this.dispatchSelectionEvent();
  };

  /**
   * Move the selection to one cell - or merge - from the keyboard. The cell
   * is scrolled into view before the render - see {@link scrollToShow} - and
   * named in the address field as the server hears of it.
   */
  private moveSelectionTo(next: SingleCellSelection): void {
    this.extendingSelection = false;
    this.pick(next);
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
    this.select({
      activeCell: next,
      activeRange: this.selection.activeRange,
      auxRanges: this.selection.auxRanges
    });
    this.scrollCellIntoView(next);
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
   * after shift+arrow) is in full view - see {@link scrollToShow}. Sets scrollLeft/Top in-place; the
   * existing `scroll` listener triggers a fresh recomputeViewport.
   */
  private scrollCellIntoView(target: { col: number; row: number }): void {
    if (this.model.rows <= 0 || this.model.cols <= 0) {
      return;
    }
    const sc = this.scrollContainer;
    // The cells' part of the view: the header strips stick to its top and
    // left edges, and the cell grid starts where they end.
    const viewWidth = sc.clientWidth - this.cellGrid.offsetLeft;
    const viewHeight = sc.clientHeight - this.cellGrid.offsetTop;
    const left = scrollToShow(
      target.col,
      this.model.horizontalSplitPosition,
      this.model.cols,
      sc.scrollLeft,
      viewWidth,
      (c) => this.colWidthPx(c)
    );
    const top = scrollToShow(
      target.row,
      this.model.verticalSplitPosition,
      this.model.rows,
      sc.scrollTop,
      viewHeight,
      (r) => this.rowHeightPx(r)
    );
    if (left !== sc.scrollLeft) {
      sc.scrollLeft = left;
    }
    if (top !== sc.scrollTop) {
      sc.scrollTop = top;
    }
  }

  /**
   * Scroll-event handler: tell the loader where the sheet is, recompute
   * the visible viewport and push a fresh render state to the cell-grid.
   * The loader decides whether the server needs asking for cells now - see
   * {@link ViewportLoader}.
   */
  private readonly handleScroll = (): void => {
    const anchored = this.view.anchoredScrollTop;
    this.view.anchoredScrollTop = null;
    // The sheet keeping a row in place is not the user scrolling.
    if (this.scrollContainer.scrollTop !== anchored) {
      // The user has moved on: no row to keep where it was.
      this.view.rowAnchor = null;
      this.viewportLoader.scrolled(this.scrollContainer.scrollTop, this.scrollContainer.scrollLeft);
    }
    this.recomputeViewport();
    // An open popup moves along with its cell.
    this.popup.reposition();
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
    if (this.model.rows <= 0 || this.model.cols <= 0) {
      // The sheet's size has not arrived yet. Announcing now would report a
      // viewport that means nothing and the server would load nothing.
      return;
    }
    if (this.drawnRange() === null) {
      // Nothing measured yet; try again after the next render.
      return;
    }
    if (!this.sheetData.viewportAnnounced) {
      this.sheetData.viewportAnnounced = true;
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
  private sendToServer(detail: SpreadsheetEvent): void {
    this.renders.beforeSend();
    this.host.dispatchEvent(new CustomEvent('spreadsheet-event', { detail }));
    const selected = selectedCellSetBy(detail);
    if (selected !== null) {
      this.sheetData.serverSelectedCell = selected;
      this.showSelectedCellInFormulaBar(selected);
    }
  }

  /**
   * Names the selected cell in the address field and shows what it holds in
   * the formula field, from the cells the client has, as GWT did on every
   * click and key. Waiting for the server's answer, which only confirms them,
   * had both fields trail every click by a round trip: a third of a second
   * on a slow network. The answer still names a named range, and fills in a
   * cell not loaded yet.
   *
   * Nothing can have been written in the formula field for the cell yet: the
   * selection has only just been made.
   */
  /**
   * Puts the server's text for a range on the clipboard once it comes; see
   * {@link listenToClipboard}. Asked for and written now, while the copy is
   * the user's, with the text to follow. False when there is no server to
   * ask or no clipboard to write to later.
   */
  private copyLater(range: CellRange): boolean {
    const server = (this.host as ServerHost).$server;
    if (server === undefined || typeof ClipboardItem === 'undefined' || navigator.clipboard?.write === undefined) {
      return false;
    }
    const text = server.clipboardText(range.c1, range.r1, range.c2, range.r2).then((value) => {
      if (value === null) {
        throw new Error('The range is no longer selected');
      }
      return new Blob([value], { type: 'text/plain' });
    });
    // The selection had changed, or the browser would not write: the
    // clipboard keeps what it had. The browser may refuse without reading
    // the text, so its refusal is handled here too.
    text.catch(() => undefined);
    navigator.clipboard.write([new ClipboardItem({ 'text/plain': text })]).catch(() => undefined);
    return true;
  }

  private showSelectedCellInFormulaBar(cell: CellPosition): void {
    this.changeFormulaBar({
      address: this.extendingSelection ? selectionSizeLabel(this.selection.activeRange) : cellCoordToAddress(cell)
    });
    this.showServerValueInFormulaField(this.formulaFieldValueFor(cell.col, cell.row), false);
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
    this.renders.schedule();
  }

  /**
   * Wraps a handler of the sheet's own events: what it changes is drawn as
   * the outermost handler running ends, and before anything it tells the
   * server; see {@link RenderScheduler}.
   */
  private handling<E extends Event>(listener: (event: E) => void): (event: E) => void {
    return (event) => this.renders.handle(() => listener(event));
  }

  /** Each listener wrapped as a handler of the sheet's; see {@link handling}. */
  private handlingAll(listeners: readonly PageListener[]): PageListener[] {
    return listeners.map(([target, type, listener]) => [target, type, this.handling(listener)]);
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
   * Measured as ten digits in a probe carrying the style's class, divided
   * by ten, as earlier versions measured it, so the server's existing
   * arithmetic keeps its meaning.
   */
  private reportCellStyleWidthRatios(): void {
    // Every style a cell wears, not only those the style map holds: a style
    // set on a cell straight through POI never reaches the map, and without
    // a ratio the server judges every value in it as fitting. Unstyled, it
    // measures as the default does.
    const styleIndexes = [
      ...new Set([...Object.keys(this.model.cellStyleToCSSStyle).map(Number), ...this.styleIndexesInUse()])
    ]
      .filter((index) => Number.isFinite(index))
      .sort((a, b) => a - b);
    if (styleIndexes.length === 0) {
      return;
    }
    const ratios = this.cellGrid
      .textWidths(
        styleIndexes.map((index) => `cs${index}`),
        WIDTH_PROBE_TEXT
      )
      .map((width) => width / WIDTH_PROBE_TEXT.length);

    if (ratios.every((ratio) => ratio === 0)) {
      // Nothing was laid out — the sheet is hidden, or the styles have not
      // been applied yet. A zero ratio would tell the server every value
      // fits in no space at all, so say nothing and measure again later.
      this.view.styleWidthRatiosStale = true;
      return;
    }

    this.view.measuredStyleIndexes = new Set(styleIndexes);
    this.sendToServer({
      type: 'setCellStyleWidthRatios',
      data: { styleIndexes, ratios }
    });
  }

  /** The style indexes the cells loaded so far wear, from their cs<N> class. */
  private styleIndexesInUse(): Set<number> {
    const indexes = new Set<number>();
    for (const cell of this.sheetData.cells.values()) {
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
    this.renders.drawn();
    if (this.view.pendingSheetScroll !== null) {
      // A sheet shown again goes back to where it was scrolled. The grid is
      // sized for it first: against the last sheet's size the browser would
      // cut the offsets short.
      const { top, left } = this.view.pendingSheetScroll;
      this.view.pendingSheetScroll = null;
      // The rows are another sheet's: there is no row to keep in place.
      this.view.rowAnchor = null;
      this.applyRenderState({
        scrollLeft: 0,
        scrollTop: 0,
        viewportWidth: this.scrollContainer.clientWidth,
        viewportHeight: this.scrollContainer.clientHeight
      });
      this.scrollContainer.scrollTop = top;
      this.scrollContainer.scrollLeft = left;
    }
    this.keepRowAnchored();
    this.applyRenderState({
      scrollLeft: this.scrollContainer.scrollLeft,
      scrollTop: this.scrollContainer.scrollTop,
      viewportWidth: this.scrollContainer.clientWidth,
      viewportHeight: this.scrollContainer.clientHeight
    });
    if (this.view.styleWidthRatiosStale && this.view.pendingRatioReport === null) {
      // Measured once the grid has painted the rules that were just
      // handed to it, not against the ones it is replacing - and so
      // against whatever else changed while it waited.
      this.view.pendingRatioReport = this.cellGrid.updateComplete.then(() => {
        this.view.pendingRatioReport = null;
        this.view.styleWidthRatiosStale = false;
        this.reportCellStyleWidthRatios();
      });
    }
    // The viewport goes after the ratios, as in earlier versions: the
    // server builds the cells it sends against the ratios, and builds the
    // ones in view again when new ratios arrive - before, the first screen
    // of every workbook went out twice. Both still go in one request.
    if (this.view.pendingRatioReport !== null) {
      void this.view.pendingRatioReport.then(() => this.announceViewport());
      return;
    }
    this.announceViewport();
  }

  /**
   * Stop listening for good: the page listeners and observers that
   * {@link disconnect} releases, and the listeners on the sheet's own
   * elements. The element only disconnects on detach, since it may be
   * attached again; tests dispose of the sheets they make.
   */
  dispose(): void {
    this.renders.cancel();
    this.ownListeners.abort();
    this.stopLongPress();
    this.overlayComponents.clear();
    this.cellComponents.clear();
    this.cellEditors.clear();
    this.popup.dispose();
    this.actionIcons.clear();
    this.disconnect();
    this.hostClassObserver.disconnect();
  }

  /**
   * Read the spreadsheet host's class list + apply
   * the two public "hide bar" classes by hiding the matching
   * internal elements. The constants
   * {@link Spreadsheet#HIDE_FUNCTION_BAR_STYLE} ("hidefunctionbar")
   * and {@link Spreadsheet#HIDE_TABSHEET_STYLE} ("hidetabsheet")
   * are public JavaDoc-documented API; consumers call
   * {@code addClassName(HIDE_TABSHEET_STYLE)} on the host
   * (or use {@code setSheetSelectionBarVisible(false)} which
   * does the same) to suppress the corresponding strip.
   */
  private applyHostHideClasses(): void {
    this.sheetTabs.hidden = this.host.classList.contains('hidetabsheet');
    this.formulaBar.hidden = this.host.classList.contains('hidefunctionbar');
    this.applyPagedViewInset();
  }

  /**
   * Leaves the headers and frozen panes out of the view the browser pages,
   * so Page Up and Page Down move the cells by a page of cells. Paging the
   * whole scroll container, they would move them a header further.
   */
  private applyPagedViewInset(): void {
    const { corner, frozen } = this.view.pagedViewParts;
    const inset = pagedViewInset(corner, frozen, !this.sheetTabs.hidden);
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
   * by dimming the selection when it does not, so a sheet left for another field does not look like it still takes the
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
    return this.popup.overlay;
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
   * delete - misses them.
   */
  private reportRangeSelection(startsAnew = false): void {
    if (this.selection.auxRanges.length > 0) {
      this.dispatchCellsAddedToRangeSelection(this.selection.activeRange);
      return;
    }
    this.dispatchSelectionEvent(startsAnew);
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
   * Tell the server the user tried to change a cell they may not change.
   *
   * The client refuses the edit on its own; the server is told so it can
   * say why — the default handler shows the workbook's "this cell is
   * protected" notification. Silently swallowing the keystroke leaves the
   * user with no idea why nothing happened.
   */
  private reportProtectedCellWriteAttempt(): void {
    // A burst of keys into a locked cell is one attempt, not a notification
    // per key: the attempts within a second of the last report go unsaid.
    const now = performance.now();
    const last = this.view.protectedWriteReportedAt;
    if (last !== null && now - last < PROTECTED_WRITE_REPORT_INTERVAL_MS) {
      return;
    }
    this.view.protectedWriteReportedAt = now;
    this.sendToServer({ type: 'protectedCellWriteAttempted', data: {} });
  }

  /**
   * Mousedown on the fill handle. The drag that follows paints rather than
   * selects, so the current range is remembered as the one being grown or
   * shrunk.
   */
  private readonly handleSelectionPaintStart = (): void => {
    const { c1, c2, r1, r2 } = this.selection.activeRange;
    this.changePress({ kind: 'fill', from: { c1, c2, r1, r2 }, outcome: { kind: 'none' } });
  };

  /**
   * Release of a fill-handle drag: tell the server what to copy or clear.
   *
   * The two directions are different operations, and the server takes them
   * through different events — growing reports the whole new rectangle,
   * shrinking reports only the corner the selection retreats to.
   */
  private finishPainting(painting: { from: CellRect; outcome: PaintOutcome }): void {
    this.changePress(null);
    if (painting.outcome.kind === 'none') {
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
   * {@code @DomEvent}; the detail's {@code type} + {@code data} follow the
   * server's SpreadsheetHandler signatures as a wire contract.
   *
   * A range goes in one event, as in earlier versions. Grown from the cell
   * the server holds as selected - a shift-click, Shift+arrow - it is
   * `cellRangeSelected`, which keeps that cell. With a starting point of its
   * own - a drag, select all - or grown from a cell the server does not
   * hold, it is `cellRangePainted`, which carries the starting point:
   * `cellRangeSelected` would leave the server's selected cell elsewhere, or
   * null. Telling the server the starting point first, in a `cellSelected`,
   * made it report a selection of that one cell in between, which every
   * selection listener of the application saw.
   *
   * @param startsAnew whether the range has a starting point of its own,
   *   even one that is the selected cell already
   */
  private dispatchSelectionEvent(startsAnew = false): void {
    const { activeCell } = this.selection;
    // A merge is one cell: the server hears that cell, rather than a range
    // it would offer to merge again.
    const range =
      this.selectsRange() || !isMultiCellRange(this.selection.activeRange)
        ? this.selection.activeRange
        : { c1: activeCell.col, c2: activeCell.col, r1: activeCell.row, r2: activeCell.row };
    const event = buildSelectionEvent(activeCell, range, this.model.rows, this.model.cols);
    const held = this.sheetData.serverSelectedCell;
    const grownFromHeld = held !== null && held.col === activeCell.col && held.row === activeCell.row;
    if (event.type === 'cellRangeSelected' && (startsAnew || !grownFromHeld)) {
      const data: CellRangePaintedData = {
        selectedCellRow: activeCell.row,
        selectedCellColumn: activeCell.col,
        ...event.data
      };
      this.sendToServer({ type: 'cellRangePainted', data });
      return;
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
  /** The sheet's row and column sizes as drawn; see {@link Derived.sheetSizes}. */
  private sheetSizes(): SheetSizes {
    return this.derived.sheetSizes(this.model);
  }

  applyRenderState(opts: {
    scrollLeft: number;
    scrollTop: number;
    viewportWidth: number;
    viewportHeight: number;
  }): void {
    const slots = this.cellSlots();
    const state = this.buildRenderState(opts, slots);
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
    const visible = visibleCellSlots(slots.assignments, state);
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
    const rowGutterPx = rowGroupingGutterPx(this.model.rowGroupingMax, this.headerColumnWidth > 0);
    const colGutterPx = columnGroupingGutterPx(this.model.colGroupingMax, this.headerRowHeight > 0);
    const cornerWidth = this.headerColumnWidth > 0 ? this.headerColumnWidth + rowGutterPx : rowGutterPx;
    const cornerHeight = this.headerRowHeight > 0 ? this.headerRowHeight + colGutterPx : colGutterPx;
    if (this.headerRowHeight > 0 || this.model.colGroupingMax > 0) {
      // The outline's pixels are worked out here, which keeps the header
      // element independent of the full colWidths array.
      const colGroupings = this.derived.columnGroupings(this.model);
      this.columnHeader.hidden = false;
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
        colGroupingMax: this.model.colGroupingMax,
        colGroupingInversed: this.model.colGroupingInversed,
        leadingPx: cornerWidth,
        // Feed the selected-columns set so the
        // header can emit `part="column-header
        // header-selected"` on cells inside the active
        // range. Pure derivation from
        // {@code selection.activeRange}.
        selectedColumns: this.derived.selectedColumns(this.selection)
      };
    } else {
      // No headings and no outline: the strip goes altogether, border and
      // all - also when they are turned off while
      // the sheet is up, rather than keep the size it was last given.
      this.columnHeader.hidden = true;
    }
    // Row-header strip. Same opt-in gate as the column
    // header, and the same reason for the grouping half of it.
    if (this.headerColumnWidth > 0 || this.model.rowGroupingMax > 0) {
      // Row groupings mirror the column ones; heights come from the same
      // point-to-pixel conversion the grid uses.
      const rowGroupings = this.derived.rowGroupings(this.model);
      this.rowHeader.hidden = false;
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
        selectedRows: this.derived.selectedRows(this.selection),
        rowGroupings,
        rowGroupingMax: this.model.rowGroupingMax,
        rowGroupingInversed: this.model.rowGroupingInversed,
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
      this.rowHeader.hidden = true;
    }
    // Header corner. Sized only when BOTH header
    // dimensions are > 0 so it occupies its grid cell with
    // matching width + height. When either is 0, the corner
    // collapses to a 0×0 box and isn't visible.
    this.headerCorner.hidden = cornerWidth === 0 && cornerHeight === 0;
    this.headerCorner.style.width = `${cornerWidth}px`;
    this.headerCorner.style.height = `${cornerHeight}px`;
    this.view.pagedViewParts = {
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
      colGroupingMax: this.model.colGroupingMax,
      rowGroupingMax: this.model.rowGroupingMax
    };
  }

  /**
   * Pure: build a {@code CellGridRenderState} from the current
   * internal state + the given scroll / viewport. Exposed for
   * tests to call without going through the DOM (the public
   * {@link applyRenderState} writes the result to the cell-grid;
   * this lets tests inspect it directly).
   */
  buildRenderState(
    opts: {
      scrollLeft: number;
      scrollTop: number;
      viewportWidth: number;
      viewportHeight: number;
    },
    slots: CellSlots = this.cellSlots()
  ): CellGridRenderState {
    const ppi = FALLBACK_PPI;
    const model = this.model;
    const rowHeightForIndex = (row: number): number => drawnRowHeight(model, row, ppi);
    const colWidthForIndex = (col: number): number => drawnColumnWidth(model, col);

    const sizes = this.sheetSizes();
    const viewport = computeViewportBounds({
      scrollLeft: opts.scrollLeft,
      scrollTop: opts.scrollTop,
      scrollViewWidth: opts.viewportWidth,
      scrollViewHeight: opts.viewportHeight,
      horizontalSplitPosition: this.model.horizontalSplitPosition,
      verticalSplitPosition: this.model.verticalSplitPosition,
      columnBufferSize: this.model.columnBufferSize,
      rowBufferSize: this.model.rowBufferSize,
      maxColumns: this.model.cols,
      maxRows: this.model.rows,
      columns: sizes.columns,
      rows: sizes.rows
    });

    // Frozen columns 1..horizontalSplitPosition and rows
    // 1..verticalSplitPosition, 1-based and inclusive. Empty when nothing
    // is frozen.
    const frozen = this.derived.frozenPanes(model);

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
    // rows and the rest, the frozen pane's border, so the scrolling ones start one pixel past it. Drawn over their first pixel
    // instead, the first row after a freeze came out a pixel short.
    const split = { cols: this.model.horizontalSplitPosition, rows: this.model.verticalSplitPosition };
    const gapX = freezeGap(split.cols);
    const gapY = freezeGap(split.rows);
    const colX: number[] = [];
    const colWidths: number[] = [];
    let cumulativeX = frozen.width + gapX + viewport.firstColumnPosition;
    for (let col = viewport.firstColumnIndex; col <= viewport.lastColumnIndex; col++) {
      const w = colWidthForIndex(col);
      colX.push(cumulativeX);
      colWidths.push(w);
      cumulativeX += w;
    }

    const rowY: number[] = [];
    const rowHeights: number[] = [];
    let cumulativeY = frozen.height + gapY + viewport.firstRowPosition;
    for (let row = viewport.firstRowIndex; row <= viewport.lastRowIndex; row++) {
      const h = rowHeightForIndex(row);
      rowY.push(cumulativeY);
      rowHeights.push(h);
      cumulativeY += h;
    }

    // Total grid dimensions in pixels — used by the scroll surface
    // to establish scroll extent.
    const totalWidthPx = sizes.totalWidthPx + gapX;
    const totalHeightPx = sizes.totalHeightPx + gapY;

    // Compute positioned overlays from the current
    // overlay map + resource map. Each overlay's pixel
    // position is anchor cell's left/top (cumulative col/row
    // widths/heights) + the overlay's dx/dy offset.
    const positionedOverlays = computePositionedOverlays(
      this.model.overlays,
      this.resolveResources(),
      colWidthForIndex,
      rowHeightForIndex,
      ppi,
      split
    );

    const mergedRegionBoxes = computeMergedRegionBoxes(
      this.model.mergedRegions,
      split,
      { left: opts.scrollLeft, top: opts.scrollTop },
      colWidthForIndex,
      rowHeightForIndex
    );

    return {
      viewport,
      cellData: this.sheetData.cells,
      colX,
      rowY,
      colWidths,
      rowHeights,
      cellStyleToCSSStyle: this.model.cellStyleToCSSStyle,
      displayGridlines: this.model.displayGridlines,
      rowIndexToStyleIndex: this.model.rowIndexToStyleIndex,
      columnIndexToStyleIndex: this.model.columnIndexToStyleIndex,
      cellSlots: slots.slots,
      conditionalFormattingStyles: this.model.conditionalFormattingStyles,
      shiftedCellBorderStyles: this.model.shiftedCellBorderStyles,
      mergedRegions: this.model.mergedRegions,
      mergedRegionBoxes,
      selection: this.selection,
      verticalSplitPosition: this.model.verticalSplitPosition,
      horizontalSplitPosition: this.model.horizontalSplitPosition,
      frozenColX: frozen.colX,
      frozenColWidths: frozen.colWidths,
      frozenRowY: frozen.rowY,
      frozenRowHeights: frozen.rowHeights,
      totalGridWidthPx: totalWidthPx,
      totalGridHeightPx: totalHeightPx,
      scrollLeft: opts.scrollLeft,
      scrollTop: opts.scrollTop,
      editor: this.editor,
      formulaReferences: this.derived.formulaReferences(this.formula.draft, this.activeSheetName()),
      overlays: positionedOverlays,
      // Each at the bottom right corner of its cell.
      popupButtons: this.derived.popupButtons(this.popup.buttons(), model),
      paintRect: this.paintRect(),
      commentText: this.model.cellComments,
      commentAuthors: this.model.cellCommentAuthors,
      visibleCommentKeys: this.model.visibleCellComments,
      editingCommentKey: this.interaction.comment,
      invalidFormulaMessage: this.model.invalidFormulaErrorMessage,
      commentCells: this.derived.commentCells(model),
      invalidFormulaCells: this.model.invalidFormulaCells,
      hyperlinkCells: this.derived.hyperlinkCells(model)
    };
  }

  /**
   * Test-only accessor: the cells currently known to the client, keyed by
   * {@code toKey(col, row)}.
   */
  getCellData(): ReadonlyMap<string, CellData> {
    return this.sheetData.cells;
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
   * Shift let go: the selection is made, so the address field goes back
   * to naming the cell the user started from.
   */
  private readonly handleDocumentKeyUp = (e: KeyboardEvent): void => {
    if (e.key !== 'Shift' || !this.extendingSelection) {
      return;
    }
    this.extendingSelection = false;
    this.changeFormulaBar({ address: cellCoordToAddress(this.selection.activeCell) });
  };

  /**
   * A value typed into the formula field.
   *
   * It belongs to the cell the sheet has selected, so it is reported as
   * an ordinary edit of that cell. Enter then moves on to the next cell
   * down and Tab to the next one right, the way finishing an edit in
   * the cell itself does; shift+Tab commits without moving. Clicking away commits too and leaves the
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
    if (this.model.rows <= 0 || this.model.cols <= 0) {
      return;
    }
    if (this.isCellLockedForEdit(col, row)) {
      this.reportProtectedCellWriteAttempt();
      return;
    }
    // An edit started in the cell ends here too: its editor closes, and
    // the cell shows the value, as it does when the edit ends there.
    this.endFormulaPointing();
    this.changeEdit(closeEditor(this.interaction.edit));
    this.showCommittedValue(col, row, value);
    this.sendToServer({ type: 'cellValueEdited', data: { row, col, value } });
    // shift+Tab stays put.
    const direction = key === undefined || (key === 'Tab' && shiftKey) ? null : keyDirection(key, false);
    if (direction === null) {
      return;
    }
    this.returnFocusToSheet();
    const next = moveActiveCell({ col, row }, this.mergeEntry, direction, this.sheetShape());
    if (next === null) {
      return;
    }
    this.pick(next);
    this.scrollCellIntoView(next.activeCell);
    this.dispatchSelectionEvent();
  };

  /**
   * Escape in the formula field: put the cell's own value back. An edit
   * started in the cell is thrown away with it, and its editor closes.
   */
  private readonly handleFormulaCancel = (): void => {
    this.endFormulaPointing();
    const { col, row } = this.selection.activeCell;
    this.showInFormulaField(this.formulaFieldValueFor(col, row));
    this.changeEdit(closeEditor(this.interaction.edit));
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
    if (this.interaction.comment === null) {
      return;
    }
    this.interact({ ...this.interaction, comment: null });
    const coord = key === undefined ? null : parseColRow(key);
    if (coord === null || coord.col < 1 || coord.row < 1) {
      return;
    }
    this.change({ cellComments: { ...this.model.cellComments, [key as string]: text } });
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
   * height of the page rather than the 250px it was asked for. The host
   * is the application's to size.
   */
  setHeight(height: string): void {
    this.root.style.height = height;
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
    const index = this.tabs.sheetIndex - 1;
    const shownSheetName = this.tabs.sheetNames[index] ?? null;
    if (!firstUpdate && changedPropertyNames.includes('sheetIndex') && shownSheetName !== this.view.shownSheetName) {
      this.view.pendingSheetScroll = {
        top: this.model.verticalScrollPositions[index] ?? 0,
        left: this.model.horizontalScrollPositions[index] ?? 0
      };
    }
    this.view.shownSheetName = shownSheetName;
    if (changedPropertyNames.length > 0 || firstUpdate) {
      this.recomputeViewport();
    }
  }

  // ====================================================================
  // 12 explicit RPC methods (server→client action messages)
  // ====================================================================

  updateBottomRightCellValues(cellData: CellData[] | string): void {
    this.storeCellData(cellData);
  }
  updateTopLeftCellValues(cellData: CellData[] | string): void {
    this.storeCellData(cellData);
  }
  updateTopRightCellValues(cellData: CellData[] | string): void {
    this.storeCellData(cellData);
  }
  updateBottomLeftCellValues(cellData: CellData[] | string): void {
    this.storeCellData(cellData);
  }

  /**
   * Stash each cell's data into the sheet's cells, keyed by its
   * wire-format key. All four quadrants share the one map; the
   * cell-grid picks out what each pane draws.
   */
  private storeCellData(cellData: CellData[] | string): void {
    // Flow hands collections over as a JSON string on some lanes and as an
    // array on others. Iterating the string walks it character by
    // character, which quietly files one entry under
    // "colundefined rowundefined" and drops every value on the sheet.
    for (const cell of coerceArray<CellData>(cellData)) {
      this.sheetData.cells.set(toKey(cell.col, cell.row), cell);
      // A style no ratio has been measured for yet: measure again after
      // the next render.
      const index = styleIndexOf(cell.cellStyle);
      if (index !== null && !this.view.measuredStyleIndexes.has(index)) {
        this.view.styleWidthRatiosStale = true;
      }
    }
    this.scheduleRender();
  }
  updateFormulaBar(possibleName: string | null, col: number, row: number): void {
    // The answer about a cell the user has already left - the keyboard
    // moves on without waiting for the server - is out of date.
    const active = this.selection.activeCell;
    if (active.col !== col || active.row !== row) {
      return;
    }
    // The server only sends a name when the selection has one — a named
    // range, or a sheet-qualified reference. For an ordinary cell it
    // sends null and leaves the address to the client, which is the
    // common case: without this the address field sits empty for every
    // selection the user makes.
    this.changeFormulaBar({
      // While the user is still growing a selection the address field
      // counts the block out, and this is the server naming the anchor
      // of the range they are in the middle of making.
      address: this.extendingSelection
        ? selectionSizeLabel(this.selection.activeRange)
        : possibleName ?? cellCoordToAddress({ col, row })
    });
    this.showServerValueInFormulaField(this.formulaFieldValueFor(col, row), true);
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
    const data = this.sheetData.cells.get(toKey(col, row));
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
   * The address field goes back to the cell the selection is actually on.
   * Leaving the typed text would have the field
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
   * to that one cell. Range selections never
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
    const before = this.selection.activeCell;
    this.select(selectSingleCell(col, row));
    this.sheetData.serverSelectedCell = { col, row };
    this.changeFormulaBar({ address: this.extendingSelection ? sizeLabel : name ?? cellCoordToAddress({ col, row }) });
    this.showServerValueInFormulaField(
      formula && cellValue !== '' ? `=${cellValue}` : cellValue,
      before.col === col && before.row === row
    );
    // The server moves the selection for the user: a hyperlink followed,
    // an address typed into the address field, a named range picked, a
    // sheet operation landing somewhere else. Somewhere the user cannot
    // see is not somewhere they have been taken, so bring it into view.
    // A cell that is already visible stays where it is.
    if (!keepView) {
      this.scrollCellIntoView({ col, row });
    }
  }
  /** Any click outside the menu dismisses it, as a native menu would. */
  private readonly handleDocumentPointerDown = (e: Event): void => {
    const path = e.composedPath();
    this.popup.pressed(path);
    this.menu.closeUnlessPressedIn(path);
  };

  private readonly handleContextMenuKeyDown = (e: Event): void => {
    if ((e as KeyboardEvent).key !== 'Escape') {
      return;
    }
    this.popup.dismiss();
    this.menu.close();
  };
  showActions(actionDetails: Json<SpreadsheetActionDetails[]>): void {
    this.menu.showActions(coerceArray<SpreadsheetActionDetails>(actionDetails));
  }
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
    this.select({
      activeCell: { col, row },
      activeRange: normalizedRange(c1, r1, c2, r2),
      auxRanges: this.selection.auxRanges
    });
    this.sheetData.serverSelectedCell = { col, row };
    this.changeFormulaBar({
      // As in updateFormulaBar: the server names a selection only when
      // it has a name, and leaves an ordinary one to the client. While
      // the user is still growing the selection, though, the count is
      // what they are reading, and this is the server echoing back the
      // range they are in the middle of making.
      address: this.extendingSelection
        ? selectionSizeLabel(this.selection.activeRange)
        : name ?? cellCoordToAddress({ col, row })
    });
    // A new active cell shows in the field. The same cell keeps what the
    // field has: the server may have just sent its value along with
    // showSelectedCell, for a cell this client holds no data for.
    if (moved) {
      this.showServerValueInFormulaField(this.formulaFieldValueFor(col, row), false);
    }
    if (scroll) {
      this.scrollCellIntoView({ col, row });
    }
  }
  cellsUpdated(updatedCellData: CellData[]): void {
    this.storeCellData(updatedCellData);
  }
  /**
   * The cells to have, in place of every cell there is: the server worked
   * them all out again and sent those shown, and any other kept here would
   * be out of date. Those are sent again when they are shown.
   */
  cellsReplaced(cellData: CellData[]): void {
    this.sheetData.cells.clear();
    this.storeCellData(cellData);
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
    this.interact({ ...this.interaction, comment: toKey(col + 1, row + 1) });
  }

  // ====================================================================
  // Property mirrors - what the server sets on the element. Those that
  // only hold a value are written through the table in state/SheetModel.ts.
  // ====================================================================

  /**
   * Takes a property as the server sends it; see {@link SHEET_PROPERTIES}.
   * It is drawn with the rest of the batch it comes in, once the batch is
   * all in; see {@link notifyStateChanges}.
   */
  private write<K extends SheetPropertyName>(name: K, value: SheetPropertyValue<K>): void {
    this.model = withProperty(this.model, name, value);
  }

  /**
   * Changes the model with values already read in: the client's own, which
   * it holds until the server answers. Drawn as the handler making the
   * change ends; see {@link handling}.
   */
  private change(values: Partial<SheetModel>): void {
    this.model = withValues(this.model, values);
    this.scheduleRender();
  }

  setRowBufferSize(pixels: number): void {
    this.write('rowBufferSize', pixels);
  }
  setColumnBufferSize(pixels: number): void {
    this.write('columnBufferSize', pixels);
  }
  setRows(count: number): void {
    this.write('rows', count);
  }
  setCols(count: number): void {
    this.write('cols', count);
  }
  setDefRowH(heightInPoints: number): void {
    this.write('defRowH', heightInPoints);
  }
  setDefColW(widthInPixels: number): void {
    this.write('defColW', widthInPixels);
  }
  /**
   * Row heights from the server. Rows above the screen grow and shrink as
   * their components are loaded and let go of; the first row with its top
   * on screen stays where it is, and so does what is on screen. Moved, the
   * rows on screen were others than those loaded, and loading those moved
   * them on again: 8 requests after one jump, to GWT's 3.
   */
  setRowH(rowHeightsInPoints: Json<number[] | Float32Array | SizeRuns>): void {
    this.view.rowAnchor ??= this.firstRowWithTopOnScreen();
    this.write('rowH', rowHeightsInPoints);
  }

  /** The first scrolling row whose top is on screen, and how far below the top of the screen. */
  private firstRowWithTopOnScreen(): { row: number; below: number } {
    const top = this.scrollContainer.scrollTop;
    const rows = this.sheetSizes().rows;
    const origin = this.model.verticalSplitPosition + 1;
    const row = rows.reach(origin, top);
    return { row, below: rows.start(row) - rows.start(origin) - top };
  }

  /**
   * Puts the row {@link setRowH} keeps back where it was on screen, now that
   * the rows above it have their new heights.
   */
  private keepRowAnchored(): void {
    const anchor = this.view.rowAnchor;
    if (anchor === null) {
      return;
    }
    this.view.rowAnchor = null;
    const rows = this.sheetSizes().rows;
    const top = rows.start(anchor.row) - rows.start(this.model.verticalSplitPosition + 1) - anchor.below;
    if (top === this.scrollContainer.scrollTop) {
      return;
    }
    // Sized for the new heights first: against the old ones the browser
    // would cut the offset short.
    this.applyRenderState({
      scrollLeft: this.scrollContainer.scrollLeft,
      scrollTop: this.scrollContainer.scrollTop,
      viewportWidth: this.scrollContainer.clientWidth,
      viewportHeight: this.scrollContainer.clientHeight
    });
    this.scrollContainer.scrollTop = top;
    this.view.anchoredScrollTop = this.scrollContainer.scrollTop;
  }
  setColW(colWidthsInPixels: Json<number[] | Int32Array | SizeRuns>): void {
    this.write('colW', colWidthsInPixels);
  }
  setHiddenColumnIndexes(indexes: Json<number[]>): void {
    this.write('hiddenColumnIndexes', indexes);
  }
  setHiddenRowIndexes(indexes: Json<number[]>): void {
    this.write('hiddenRowIndexes', indexes);
  }
  setLockedColumnIndexes(indexes: Json<number[]>): void {
    this.write('lockedColumnIndexes', indexes);
  }
  setLockedRowIndexes(indexes: Json<number[]>): void {
    this.write('lockedRowIndexes', indexes);
  }
  setSheetIndex(index: number): void {
    this.tabs.setSheetIndex(index);
  }
  setSheetNames(names: Json<string[]>): void {
    this.tabs.setSheetNames(coerceArray(names));
  }
  /**
   * Where each sheet was last scrolled to, by visible sheet, as the server
   * keeps it: a sheet shown again goes back there.
   */
  setVerticalScrollPositions(positions: Json<number[]>): void {
    this.write('verticalScrollPositions', positions);
  }
  /** See {@link setVerticalScrollPositions}. */
  setHorizontalScrollPositions(positions: Json<number[]>): void {
    this.write('horizontalScrollPositions', positions);
  }
  setCellStyleToCSSStyle(map: Json<Record<number, string>>): void {
    this.write('cellStyleToCSSStyle', map);
    // The server sizes General-format numbers against how wide a digit is
    // in each style, and only the client can measure that.
    this.view.styleWidthRatiosStale = true;
  }
  setRowIndexToStyleIndex(map: Json<Record<number, number>>): void {
    this.write('rowIndexToStyleIndex', map);
  }
  setColumnIndexToStyleIndex(map: Json<Record<number, number>>): void {
    this.write('columnIndexToStyleIndex', map);
  }
  setShiftedCellBorderStyles(cssRules: Json<string[]>): void {
    this.write('shiftedCellBorderStyles', cssRules);
  }
  setConditionalFormattingStyles(map: Json<Record<number, string>>): void {
    this.write('conditionalFormattingStyles', map);
  }
  setColGroupingData(groups: Json<GroupingData[]>): void {
    this.write('colGroupingData', groups);
  }
  setRowGroupingData(groups: Json<GroupingData[]>): void {
    this.write('rowGroupingData', groups);
  }
  setColGroupingMax(level: number): void {
    this.write('colGroupingMax', level);
  }
  setRowGroupingMax(level: number): void {
    this.write('rowGroupingMax', level);
  }
  setColGroupingInversed(inversed: boolean): void {
    this.write('colGroupingInversed', inversed);
  }
  setRowGroupingInversed(inversed: boolean): void {
    this.write('rowGroupingInversed', inversed);
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
   * talking about, in a loop.
   */
  private readonly handleHostedEditorPointerDown = (e: Event): void => {
    const cell = this.cellShowingEditorFor(e);
    if (cell === null) {
      return;
    }
    if (this.selection.activeCell.col === cell.col && this.selection.activeCell.row === cell.row) {
      return;
    }
    this.select(selectSingleCell(cell.col, cell.row));
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
   * changes on every click, and the maps it reads are small. A draw works
   * it out once and uses it twice.
   */
  private cellSlotAssignments(): Record<string, CellSlotAssignment> {
    return this.cellSlots().assignments;
  }

  /** Which component or editor each cell shows, and the slot the grid draws for it. */
  private cellSlots(): CellSlots {
    const active = this.selection.activeCell;
    return this.derived.cellSlots(this.model, active === null ? null : toKey(active.col, active.row));
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
      // So is the cell it holds as selected, until it says which one it has
      // now, and what it was told of where the sheet is.
      this.sheetData = new SheetData();
      // Its row heights are those of whatever it shows now.
      this.view.rowAnchor = null;
      // What it was asked to load is gone too: load again, even the same
      // range.
      this.viewportLoader.reset();
      // So has the selection, and the sheet may be another one: a value, a
      // note or a menu pick still under way would land on whatever sheet
      // the server shows now.
      this.abandonInteraction();
      this.scheduleRender();
    }
  }

  /**
   * Ends whatever the user is in the middle of on the sheet, committing
   * none of it: an edit in a cell or in the formula field, pointing at
   * cells for a formula, a note being written, a drag, the context menu.
   * The keyboard goes back to the sheet if it was in any of them.
   */
  private abandonInteraction(): void {
    const hadKeyboard = this.host.matches(':focus-within');
    // The formula's colours in the formula field go with it.
    this.updateFormulaDraft(null, 0);
    this.interact(IDLE);
    this.formulaBar.abandonFormulaEdit();
    this.headerResize.cancel();
    this.extendingSelection = false;
    this.menu.close();
    this.hideLinkTooltip();
    if (hadKeyboard) {
      this.returnFocusToSheet();
    }
  }
  setSheetProtected(isProtected: boolean): void {
    this.write('sheetProtected', isProtected);
  }
  setMergedRegions(regions: Json<MergedRegion[]>): void {
    this.write('mergedRegions', regions);
  }
  setCellKeysToEditorIdMap(map: Json<Record<string, string>>): void {
    this.write('cellKeysToEditorIdMap', map);
  }
  setComponentIDtoCellKeysMap(map: Record<string, string>): void {
    this.write('componentIDtoCellKeysMap', map);
  }
  setCellComments(map: Json<Record<string, string>>): void {
    this.write('cellComments', map);
  }
  setCellCommentAuthors(map: Json<Record<string, string>>): void {
    this.write('cellCommentAuthors', map);
  }
  setVisibleCellComments(keys: Json<string[]>): void {
    this.write('visibleCellComments', keys);
  }
  setInvalidFormulaCells(keys: Json<string[]>): void {
    this.write('invalidFormulaCells', keys);
  }
  setOverlays(map: Json<Record<string, OverlayInfo>>): void {
    this.write('overlays', map);
  }
  setShowCustomEditorOnFocus(show: boolean): void {
    this.write('showCustomEditorOnFocus', show);
  }
  setHasComponentFactory(has: boolean): void {
    this.write('hasComponentFactory', has);
  }
  setInvalidFormulaErrorMessage(message: string | null): void {
    this.write('invalidFormulaErrorMessage', message);
  }
  setNamedRanges(names: Json<string[]>): void {
    this.changeFormulaBar({ namedRanges: coerceArray(names) });
  }
  setResources(host: HTMLElement, keys: string[] | string): void {
    // The server sends the resource KEYS here and puts each URL on the host
    // as a `resource-<key>` attribute, because the URLs are Flow stream
    // resources minted per element rather than plain strings
    // (Spreadsheet.setResource). Resolving them means reading the
    // attributes, not parsing a map out of this argument.
    this.resourceHost = host;
    this.write('resources', keys);
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
    for (const key of this.model.resources) {
      const url = host.getAttribute(`resource-${key}`);
      if (url) {
        resolved[key] = url;
      }
    }
    return resolved;
  }
  setWorkbookProtected(isProtected: boolean): void {
    this.tabs.setWorkbookProtected(isProtected);
  }

  // Freeze pane split positions
  setVerticalSplitPosition(value: number): void {
    this.write('verticalSplitPosition', value);
  }
  setHorizontalSplitPosition(value: number): void {
    this.write('horizontalSplitPosition', value);
  }

  // Display flags
  setDisplayGridlines(visible: boolean): void {
    this.write('displayGridlines', visible);
    // Turned off or on from the server on its own, with no cell data to
    // carry a repaint along.
    this.scheduleRender();
  }
  setDisplayRowColHeadings(visible: boolean): void {
    // The server sends only this boolean, never the strips' sizes: shown,
    // they get their default sizes back; hidden, they collapse. They are
    // drawn with the rest of the batch this came in, as every property is,
    // not halfway through it.
    this.write('displayRowColHeadings', visible);
  }
  setLockFormatColumns(locked: boolean): void {
    this.headerResize.setLockFormatColumns(locked);
  }
  setLockFormatRows(locked: boolean): void {
    this.headerResize.setLockFormatRows(locked);
  }
  setHasActions(hasActions: boolean): void {
    this.menu.setHasActions(hasActions);
  }
  /**
   * The server flips this on loading a workbook, the same one again
   * included. The change alone is the point: the property batch it comes in
   * is drawn, as every batch is, and the style width ratios are measured and
   * sent again. The server keeps them with the workbook, and the same styles
   * bring no change of the style map that would say so.
   */
  setWorkbookChangeToggle(_toggle: boolean): void {
    this.view.measuredStyleIndexes = new Set();
    this.view.styleWidthRatiosStale = true;
    this.scheduleRender();
  }

  // Display content
  /**
   * The status label at the right end of the sheet tabs. The server
   * clears the property for no label, which reaches here as null or
   * undefined; an empty string is a label with nothing in it.
   */
  setInfoLabelValue(label: string | null | undefined): void {
    this.tabs.setInfoLabel(label ?? null);
  }
  setHyperlinksTooltips(tooltips: Json<Record<string, string>>): void {
    this.write('hyperlinksTooltips', tooltips);
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

  /** The sheet as the arrow keys see it. */
  private sheetShape(): SheetShape {
    return {
      cols: this.model.cols,
      rows: this.model.rows,
      mergedRegions: this.model.mergedRegions,
      hiddenColumns: this.model.hiddenColumnIndexes,
      hiddenRows: this.model.hiddenRowIndexes
    };
  }

  /**
   * Select one cell - or the whole merge it is part of, with the merge's
   * top-left cell active, as for a click.
   */
  private selectCellOrMerge(col: number, row: number): void {
    this.pick(selectCellAt(col, row, this.model.mergedRegions));
  }

  /** Picks one cell or merge; see {@link pickCell}. */
  private pick(next: SingleCellSelection): void {
    this.changeSelection(pickCell(next));
  }

  /**
   * Selects cells, with the merge still entered by the same cell; see
   * {@link replaceRange}. Drawn as the handler ends, before the server is
   * told; see {@link handling}.
   */
  private select(cells: SelectionState): void {
    this.changeSelection(replaceRange(this.sheetSelection, cells));
  }

  private changeSelection(selection: SheetSelection): void {
    this.sheetSelection = selection;
    this.scheduleRender();
  }

  /**
   * Changes what the user is in the middle of. Drawn as the handler ends,
   * before the server is told; see {@link handling}.
   */
  private interact(interaction: InteractionState): void {
    this.interaction = interaction;
    this.scheduleRender();
  }

  private changeEdit(edit: Edit): void {
    this.interact({ ...this.interaction, edit });
  }

  /** Changes the formula being written; see {@link withFormula}. */
  private changeFormula(change: Partial<FormulaWriting>): void {
    this.changeEdit(withFormula(this.interaction.edit, { ...this.formula, ...change }));
  }

  private changePress(press: CellPress | null): void {
    this.interact({ ...this.interaction, press });
  }

  /** The strip a fill drag paints, while one does. */
  private paintRect(): CellRect | null {
    const press = this.interaction.press;
    return press?.kind === 'fill' && press.outcome.kind !== 'none' ? press.outcome.painted : null;
  }

  /**
   * Whether a range is selected: more than one cell, and more than the one
   * merge the active cell is in, which is a single cell to the user.
   */
  private selectsRange(): boolean {
    const { activeRange, activeCell } = this.selection;
    return isMultiCellRange(activeRange) && !isSingleMerge(activeRange, activeCell, this.model.mergedRegions);
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
    const grown = expandToMergedRegions(
      { c1: range.c1, c2: range.c2, r1: range.r1, r2: range.r2 },
      this.model.mergedRegions
    );
    return { c1: grown.c1, c2: grown.c2, r1: grown.r1, r2: grown.r2 };
  }

  /**
   * Rendered width of a column in pixels — 0 when it is hidden, the
   * sheet's default past the last defined column.
   */
  private colWidthPx(col: number): number {
    return drawnColumnWidth(this.model, col);
  }

  /**
   * Rendered height of a row in pixels. POI measures rows in points, so
   * this is the one place the conversion happens.
   */
  private rowHeightPx(row: number, ppi: number = FALLBACK_PPI): number {
    return drawnRowHeight(this.model, row, ppi);
  }

  /**
   * The scrolling pane on screen: the cells under the column letters and
   * right of the row numbers, past the frozen rows and columns. Popups keep
   * to this pane, whose bottom is the sheet's own.
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
        freezeGap(this.model.horizontalSplitPosition),
      top:
        box.top +
        this.scrollContainer.clientTop +
        this.cellGrid.offsetTop +
        frozen.height +
        freezeGap(this.model.verticalSplitPosition),
      bottom: box.bottom
    };
  }
  /** Show a popup button's content; see {@link Popup.open}. */
  onPopupButtonOpen(row: number, column: number, contentId: number | string, appId: string): void {
    this.popup.open(row, column, contentId, appId);
  }

  /** Hide the open popup at the given cell; see {@link Popup.close}. */
  closePopup(row: number, column: number): void {
    this.popup.close(row, column);
  }

  /** A popup (filter) button was attached to a cell. */
  addPopupButton(rawState: PopupButtonState | string): void {
    const state = coerceJson(rawState);
    if (state !== null && typeof state.col === 'number') {
      this.popup.addButton(state);
    }
  }

  /** A popup button was taken away, and its popup closes with it. */
  removePopupButton(rawState: PopupButtonState | string): void {
    const state = coerceJson(rawState);
    if (state !== null && typeof state.col === 'number') {
      this.popup.removeButton(state);
    }
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
    return this.model.rows;
  }
  getCols(): number {
    return this.model.cols;
  }
  getSheetIndex(): number {
    return this.tabs.sheetIndex;
  }
  getSheetNames(): readonly string[] {
    return this.tabs.sheetNames;
  }
  getSelection(): SelectionState {
    return this.selection;
  }
  getMergedRegions(): readonly MergedRegion[] {
    return this.model.mergedRegions;
  }
  isSheetProtected(): boolean {
    return this.model.sheetProtected;
  }
}

/** Whether the host is a Lit element, which tells its controllers as it joins and leaves the page. */
function isControllerHost(host: HTMLElement): host is HTMLElement & ReactiveControllerHost {
  return typeof (host as Partial<ReactiveControllerHost>).addController === 'function';
}

/** Whether a key goes to a button, which the key itself is pressing. */
function pressesButton(e: KeyboardEvent): boolean {
  const target = e.composedPath()[0];
  return target instanceof HTMLElement && (target.localName === 'button' || target.getAttribute('role') === 'button');
}
