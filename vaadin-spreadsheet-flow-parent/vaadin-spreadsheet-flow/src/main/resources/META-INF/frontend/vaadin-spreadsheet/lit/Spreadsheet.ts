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
import type { SpreadsheetClientApi } from './types/rpc-server-to-client.js';
import {
  addAuxRange,
  type CellPosition,
  type CellRange,
  isCellInSelection,
  normalizedRange,
  type SelectionState,
  selectSingleCell
} from './state/SelectionState.js';
import type { SpreadsheetEvent } from './types/rpc-client-to-server.js';
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
import { IDLE, type InteractionState } from './state/Interaction.js';
import type { SheetData } from './state/SheetData.js';
import { SheetStore } from './state/SheetStore.js';
import { ViewState } from './state/ViewState.js';
import { type SheetModel } from './state/SheetModel.js';
import { toKey } from './algorithms/CellKeys.js';
import { type CellSlotAssignment, flowVirtualChildResolver } from './dom/VirtualChildren.js';
import {
  computePaintRectangle,
  grownSelection,
  isNonEmpty,
  type CellRect,
  type PaintOutcome
} from './algorithms/SelectionPaint.js';
import { inSelectionWalkDirection, walkInsideSelection } from './algorithms/SelectionWalk.js';
import {
  homeEndTarget,
  keyDirection,
  moveActiveCell,
  moveRangeEdge,
  selectCellAt,
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
import { KeyboardExit } from './controllers/KeyboardExit.js';
import { LinkTooltip } from './controllers/LinkTooltip.js';
import { HostedComponents } from './controllers/HostedComponents.js';
import { SelectionReporter } from './controllers/SelectionReporter.js';
import { ServerSync } from './controllers/ServerSync.js';
import { EditSession } from './controllers/EditSession.js';
import { FormulaPointer } from './controllers/FormulaPointer.js';
import { Outline } from './controllers/Outline.js';
import { listenForLongPress } from './dom/LongPress.js';
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
import { SpreadsheetFormulaBar } from './elements/SpreadsheetFormulaBar.js';
import { SpreadsheetContextMenu } from './elements/SpreadsheetContextMenu.js';
import { type LoadRange, ViewportLoader } from './state/ViewportLoader.js';
import type { CellGridRenderState } from './elements/types.js';

/** An arrow in the cell editor or the formula field, with the text written and its caret. */
type FormulaArrowEvent = CustomEvent<{ key: string; shiftKey: boolean; value: string; caret: number }>;

/** The caret moved in the cell editor or the formula field. */
type CaretEvent = CustomEvent<{ value: string; caret: number }>;

/** Typed into the cell editor or the formula field. */
type InputEvent = CustomEvent<{ value: string; caret?: number }>;

/** A cell, double-clicked. */
type CellEvent = CustomEvent<{ col: number; row: number }>;

/** The cell editor's edit ended by a key or by leaving it. */
type CellEditorCommitEvent = CustomEvent<{
  col: number;
  row: number;
  value: string;
  key?: string;
  shiftKey?: boolean;
  relatedTarget?: EventTarget | null;
}>;

/** The formula field's edit ended by a key or by leaving it. */
type FormulaCommitEvent = CustomEvent<{
  value: string;
  key?: string;
  shiftKey: boolean;
  relatedTarget?: EventTarget | null;
}>;

/** A note finished, for the cell of the key. */
type NoteCommitEvent = CustomEvent<{ key?: string; text: string }>;

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
   * What changes as the sheet is used, and the changes that have it drawn;
   * see {@link SheetStore}.
   */
  private readonly store = new SheetStore(() => this.scheduleRender());

  /** Pointing at cells while a formula is written; see {@link FormulaPointer}. */
  private readonly pointing = new FormulaPointer(this.store, {
    fieldHasFocus: () => this.formulaBar.formulaFieldHasFocus(),
    sheetName: () => this.activeSheetName(),
    scrollIntoView: (cell) => this.scrollCellIntoView(cell)
  });

  /** Tells the server what the user selected; see {@link SelectionReporter}. */
  private readonly report = new SelectionReporter(this.store, {
    send: (event) => this.sendToServer(event),
    selectedCellReported: (cell) => this.showSelectedCellInFormulaBar(cell)
  });

  /** Editing cells, in the cell editor and the formula field; see {@link EditSession}. */
  private readonly edit = new EditSession(this.store, this.report, this.pointing, {
    fieldHasFocus: () => this.formulaBar.formulaFieldHasFocus(),
    fieldEdited: () => this.formulaBar.formulaFieldEdited(),
    isFormulaBar: (target) => target === this.formulaBar,
    isCellGrid: (target) => target === this.cellGrid,
    handle: (work) => this.renders.handle(work),
    returnFocusToSheet: () => this.returnFocusToSheet(),
    moveInsideSelection: (cell) => this.moveActiveCellInsideSelection(cell),
    scrollIntoView: (cell) => this.scrollCellIntoView(cell),
    scheduleRender: () => this.scheduleRender(),
    send: (event) => this.sendToServer(event)
  });

  /** What the server mirrors onto the element; see {@link SheetStore.model}. */
  private get model(): SheetModel {
    return this.store.model;
  }

  /** The cells selected. */
  private get selection(): SelectionState {
    return this.store.selection;
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
    return this.store.mergeEntry;
  }

  /** What the user is in the middle of on the sheet; see {@link SheetStore.interaction}. */
  private get interaction(): InteractionState {
    return this.store.interaction;
  }

  /** The cell editor while it is open: the cell-grid draws an input on its cell. */
  private get editor(): CellEditorState | null {
    return this.store.editor;
  }

  /** A link cell's tooltip, shown while the pointer is over the cell; see {@link LinkTooltip}. */
  private readonly linkTooltip = new LinkTooltip({
    tooltipAt: (col, row) => this.model.hyperlinksTooltips[toKey(col, row)],
    dragging: () => this.interaction.press !== null
  });

  /** Resizing columns and rows by their header edges; see {@link HeaderResize}. */
  private readonly headerResize: HeaderResize;

  /** The element the `resource-<id>` attributes live on; see {@link setResources}. */
  private resourceHost: HTMLElement | null = null;

  /**
   * What the client knows of the sheet the server shows: its cells, and the
   * cell the server holds as selected. Replaced whole on a reload; see
   * {@link setReload}.
   */
  private get sheetData(): SheetData {
    return this.store.sheetData;
  }

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
  /** Escape, then Tab, leaving the sheet; see {@link KeyboardExit}. */
  private readonly keyboardExit: KeyboardExit;

  /** The cell-grid sub-element. Created in the constructor + appended to the host. */
  private readonly cellGrid: SpreadsheetCellGrid;

  /**
   * The server-side components the sheet hosts: charts and other overlays,
   * components in cells, custom editors, and the icons of menu actions.
   */
  private readonly hosted: HostedComponents;

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
    load: (range) => this.sync.load(range)
  });

  /** Keeps the server in step with what the sheet shows; see {@link ServerSync}. */
  private readonly sync = new ServerSync(
    {
      size: () => this.model,
      cellStyles: () => this.model.cellStyleToCSSStyle,
      cells: () => this.sheetData.cells,
      drawnRange: () => this.drawnRange(),
      textWidths: (cellStyles, text) => this.cellGrid.textWidths(cellStyles, text),
      painted: () => this.cellGrid.updateComplete,
      scheduleRender: () => this.scheduleRender(),
      send: (event) => this.sendToServer(event)
    },
    this.viewportLoader
  );

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
    this.hosted = new HostedComponents(
      host,
      { cellGrid: this.cellGrid, contextMenu: this.contextMenu },
      resolveVirtualChild,
      { cellSlotAssignments: () => this.cellSlotAssignments() }
    );
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
    // To assistive technology the sheet is one control that takes its keys
    // for itself, rather than a page to read, named after the sheet shown.
    this.scrollContainer.setAttribute('role', 'application');
    this.scrollContainer.setAttribute('aria-roledescription', 'spreadsheet');
    this.scrollContainer.setAttribute('aria-label', 'Spreadsheet');
    // Header corner at (col 1, row 1) — sticky on both axes
    // so it overlays both header strips at their intersection.
    // Hidden when either header dimension is 0.
    this.headerCorner.className = 'sheet-header-corner';
    this.headerCorner.style.cssText =
      'grid-column:1;grid-row:1;' + 'position:sticky;top:0;left:0;z-index:6;' + 'width:0;height:0;';
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
    this.on(this.formulaBar, 'formula-commit', (e: FormulaCommitEvent) => this.edit.fieldCommitted(e.detail));
    this.on(this.formulaBar, 'formula-cancel', () => this.edit.fieldCancelled());
    // What is typed is kept, and drawn into the other input; see drawWriting.
    this.on(this.formulaBar, 'formula-input', (e: InputEvent) =>
      this.pointing.typed(e.detail.value, e.detail.caret ?? e.detail.value.length, 'field')
    );
    this.on(this.formulaBar, 'formula-arrow', (e: FormulaArrowEvent) => this.pointing.arrow(e, 'field'));
    this.on(this.formulaBar, 'formula-caret', (e: CaretEvent) =>
      this.pointing.caretMoved(e.detail.value, e.detail.caret, 'field')
    );
    this.root.appendChild(this.scrollContainer);
    // Sheet tabs sit below the scroll container in the flex
    // column. flex:0 so they take their intrinsic height,
    // never grow / shrink based on content.
    this.sheetTabs.style.flex = '0 0 auto';
    this.root.appendChild(this.sheetTabs);
    this.keyboardExit = new KeyboardExit(
      this.scrollContainer,
      { showsMenuOrPopup: () => this.menu.isOpen() || this.popup.isOpen() },
      this.ownListeners.signal
    );
    this.scrollContainer.after(...this.keyboardExit.elements());
    // Outside the scroll container: the keys typed into a filter's fields and the clicks on
    // its check boxes are the popup's, not the sheet's shortcuts. Beside
    // the context menu, the other overlay.
    this.contextMenu.after(this.popup.overlay);
    this.headerResize = new HeaderResize(
      this.root,
      { columnHeader: this.columnHeader, rowHeader: this.rowHeader },
      {
        sheetProtected: () => this.model.sheetProtected,
        colWidthPx: (col) => this.store.colWidthPx(col),
        rowHeightPx: (row) => this.store.rowHeightPx(row),
        rowHeightPt: (row) => (row - 1 < this.model.rowH.length ? this.model.rowH[row - 1] : this.model.defRowH),
        // A column or row dragged to nothing is hidden, as the server will
        // have it. One already hidden has no edge to drag.
        setColumnWidth: (col, width) => {
          this.store.change({ colW: withSize(this.model.colW, col, width, this.model.defColW) });
          if (width === 0) {
            this.store.change({ hiddenColumnIndexes: new Set([...this.model.hiddenColumnIndexes, col]) });
          }
        },
        setRowHeight: (row, height) => {
          this.store.change({ rowH: withSize(this.model.rowH, row, height, this.model.defRowH) });
          if (height === 0) {
            this.store.change({ hiddenRowIndexes: new Set([...this.model.hiddenRowIndexes, row]) });
          }
        },
        send: (event) => this.sendToServer(event)
      },
      this.ownListeners.signal
    );
    this.popup.overlay.after(...this.headerResize.elements(), this.linkTooltip.element);
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
        send: (event) => this.sendToServer(event),
        scheduleRender: () => this.scheduleRender(),
        handle: (work) => this.renders.handle(work)
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
    this.on(this.cellGrid, 'cell-doubleclick', (e: CellEvent) => this.edit.openExisting(e.detail.col, e.detail.row));
    this.on(this.cellGrid, 'cell-editor-commit', (e: CellEditorCommitEvent) => this.edit.editorCommitted(e));
    this.on(this.cellGrid, 'cell-editor-cancel', () => this.edit.editorCancelled());
    this.on(this.cellGrid, 'cell-editor-input', (e: InputEvent) =>
      this.pointing.typed(e.detail.value, e.detail.caret ?? e.detail.value.length, 'cell')
    );
    this.on(this.cellGrid, 'cell-editor-arrow', (e: FormulaArrowEvent) => this.pointing.arrow(e, 'cell'));
    this.on(this.cellGrid, 'cell-editor-caret', (e: CaretEvent) =>
      this.pointing.caretMoved(e.detail.value, e.detail.caret, 'cell')
    );
    // Mouse drag selection. The cell-grid emits
    // cell-mousedown + cell-mouseover; the connector watches
    // mouseup at the document level so a drag that ends
    // outside the spreadsheet still terminates cleanly.
    this.on(this.cellGrid, 'cell-mousedown', this.handleCellMouseDown);
    this.on(this.cellGrid, 'cell-mouseover', this.handleCellMouseOver);
    this.on(this.cellGrid, 'grid-mouseleave', this.linkTooltip.hide);
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
      this.hosted.actionIcons,
      this.root,
      { cells: this.cellGrid, columnHeader: this.columnHeader, rowHeader: this.rowHeader },
      {
        // The menu's handlers run as the sheet's own: the cell is drawn
        // before the server hears of it.
        selectForMenu: (col, row) => {
          if (!isCellInSelection(col, row, this.selection)) {
            this.store.selectCellOrMerge(col, row);
            this.report.selection();
          }
        },
        returnFocusToSheet: () => this.returnFocusToSheet(),
        send: (event) => this.sendToServer(event),
        scheduleRender: () => this.scheduleRender(),
        handle: (work) => this.renders.handle(work)
      },
      this.ownListeners.signal
    );
    this.on(this.cellGrid, 'cell-comment-commit', (e: NoteCommitEvent) => this.edit.noteCommitted(e.detail));
    // The outline's +/- and level buttons, which the server acts on.
    new Outline(
      { columnHeader: this.columnHeader, rowHeader: this.rowHeader, headerCorner: this.headerCorner },
      (event) => this.sendToServer(event),
      this.ownListeners.signal
    );
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
    if (this.pointing.clicked()) {
      // The press pointed at the cell for a formula; it selects nothing.
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
        activeRange: this.store.rangeTo(anchor, col, row),
        auxRanges: []
      };
      this.store.select({ ...extended, auxRanges: this.selection.auxRanges });
      this.report.rangeAdded(extended.activeRange);
      return;
    }
    if (ctrlOrMeta) {
      // Non-coherent selection: the clicked cell joins the selection
      // as its own range and becomes active; the previous active
      // range is kept as an aux range.
      this.store.select(addAuxRange(this.selection, col, row, col, row));
      this.report.cellAdded(col, row);
      return;
    }
    if (shiftKey) {
      // Extend the active range from the existing active cell
      // (= the anchor) to the clicked coordinate. Ranges picked out
      // with earlier Ctrl-clicks stay: shift extends the last range,
      // it does not start the selection over.
      const anchor = this.selection.activeCell;
      this.store.select({
        activeCell: anchor,
        activeRange: this.store.rangeTo(anchor, col, row),
        auxRanges: this.selection.auxRanges
      });
      if (!this.rangeReportedOnRelease) {
        this.report.range();
      }
      return;
    } else {
      this.store.selectCellOrMerge(col, row);
    }
    this.report.selection();
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
    const source = this.pointing.source();
    if (source !== null) {
      e.preventDefault();
      this.pointing.press(col, row, shiftKey, ctrlKey || metaKey, source);
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
    this.store.changePress({ kind: 'select', crossed: false });
    if (shiftKey) {
      // Shift+mousedown: extend existing range to the
      // mousedown coord (treat the existing anchor as the
      // drag start). Standard Excel "shift-drag" behavior.
      // Aux ranges survive, as on a shift-click.
      const anchor = this.selection.activeCell;
      this.store.select({
        activeCell: anchor,
        activeRange: this.store.rangeTo(anchor, col, row),
        auxRanges: this.selection.auxRanges
      });
    } else {
      this.store.selectCellOrMerge(col, row);
    }
  };

  /**
   * Mouse drag — mouseover during drag. Extends
   * the range from the active cell (anchor) to the hovered
   * cell. Mouseover events that fire while NOT dragging are
   * silently ignored — the cell-grid emits on every hover.
   */
  private readonly handleCellMouseOver = (e: CustomEvent<{ col: number; row: number; box?: DOMRect }>): void => {
    this.linkTooltip.show(e.detail);
    if (this.pointing.dragOver(e.detail.col, e.detail.row)) {
      return;
    }
    const press = this.interaction.press;
    if (press?.kind === 'fill') {
      const { col, row } = e.detail;
      this.store.changePress({ ...press, outcome: computePaintRectangle(press.from, col, row) });
      return;
    }
    if (press?.kind !== 'select') {
      return;
    }
    if (!press.crossed) {
      this.store.changePress({ kind: 'select', crossed: true });
    }
    const { col, row } = e.detail;
    // Same as shift-arrow: keep activeCell as anchor, move
    // the extend corner.
    // The ranges picked out before stay: a plain press has already let
    // them go, and a Shift-press drags out the range it extends.
    const anchor = this.selection.activeCell;
    this.store.select({
      activeCell: anchor,
      activeRange: this.store.rangeTo(anchor, col, row),
      auxRanges: this.selection.auxRanges
    });
  };

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
    if (this.pointing.released()) {
      // The press that pointed is over; the click it ends in is to come.
      return;
    }
    if (press === null) {
      return;
    }
    this.store.changePress(null);
    if (press.kind === 'select' && this.store.selectsRange()) {
      // Multi-cell range — dispatch the server-bound
      // selection event so the server learns the final
      // dragged range: with the cell the drag started on, when it went
      // into other cells. One that stayed in its cell was a shift-click,
      // which grows the range from the selected cell.
      this.report.range(press.crossed);
      // The click that may follow the release reports nothing more.
      this.rangeReportedOnRelease = true;
      setTimeout(() => (this.rangeReportedOnRelease = false));
    }
    // Single-cell drag (mousedown + mouseup on the same cell
    // with no mouseover-extension) is handled by the existing
    // `cell-click` handler — no double dispatch.
  };

  /**
   * Draws what is written into the input it is not written in, and into the
   * one it is written in where pointing at a cell changed it. The cell editor
   * and the formula field report what is typed into them and where the caret
   * goes, which the store keeps; neither is written to anywhere else.
   */
  private drawWriting(): void {
    const edit = this.interaction.edit;
    if (edit.kind === 'editing') {
      const shown = this.cellGrid.editorValue();
      if (shown !== null && shown !== edit.writing.text) {
        // The caret only where the editor has the keyboard: written in the
        // formula field, the text is shown in the editor and nothing moves
        // there.
        this.cellGrid.setEditorValue(
          edit.writing.text,
          edit.writing.source === 'cell' ? edit.writing.caret : undefined
        );
      }
    }
    const writing = this.store.writing;
    if (writing !== null && writing.source === 'field' && this.formulaBar.formulaFieldHasFocus()) {
      const field = this.formulaBar.formulaFieldState();
      if (field !== null && field.value !== writing.text) {
        this.formulaBar.setFormulaFieldValue(writing.text, writing.caret);
      }
    }
  }

  /** The name of the sheet shown. */
  private activeSheetName(): string {
    return this.tabs.shownSheetName();
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
    if (this.hosted.isEditor(active)) {
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
      this.store.select(addAuxRange(this.selection, col, 1, col, rows, { col, row }));
      this.report.columnAdded(col, row);
      return;
    }
    if (shiftKey) {
      // Extend across columns, keeping ranges added with Ctrl.
      const anchor = this.selection.activeCell;
      this.store.select({
        activeCell: anchor,
        activeRange: normalizedRange(anchor.col, 1, col, rows),
        auxRanges: this.selection.auxRanges
      });
    } else {
      this.store.select({
        activeCell: { col, row },
        activeRange: normalizedRange(col, 1, col, rows),
        auxRanges: []
      });
    }
    this.report.selection();
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
      this.store.select(addAuxRange(this.selection, 1, row, cols, row, { col, row }));
      this.report.rowAdded(row, col);
      return;
    }
    if (shiftKey) {
      // Extend across rows, keeping ranges added with Ctrl.
      const anchor = this.selection.activeCell;
      this.store.select({
        activeCell: anchor,
        activeRange: normalizedRange(1, anchor.row, cols, row),
        auxRanges: this.selection.auxRanges
      });
    } else {
      this.store.select({
        activeCell: { col, row },
        activeRange: normalizedRange(1, row, cols, row),
        auxRanges: []
      });
    }
    this.report.selection();
  };

  /**
   * Keyboard navigation. Arrow keys, Tab and Enter move the active cell
   * over hidden rows and columns and out of merges; Shift with an arrow
   * grows the range from the active cell, which stays where it is, as in
   * Excel. Home and End go along the row, or with Ctrl to the sheet's first
   * and last cell; typing starts an edit. Escape, then Tab, leaves the
   * sheet; see {@link KeyboardExit}.
   */
  private readonly handleKeyDown = (e: KeyboardEvent): void => {
    // Enter and Space on a button of the sheet's own - an outline button, a
    // popup's close box - press that button, as on any button, rather than
    // editing the active cell.
    if ((e.key === 'Enter' || e.key === ' ') && pressesButton(e)) {
      return;
    }
    if (this.hosted.raisedInside(e)) {
      // The key belongs to the component the user is typing into. Acting
      // on it as a sheet shortcut moves the selection out from under a
      // custom editor mid-edit, which takes the editor off the sheet.
      //
      // Three keys are the way back out of a custom editor, so they are the
      // sheet's after all: Escape returns the focus, Tab returns it and then
      // moves on to the next cell, and Enter returns it once the editor has
      // had it, leaving the cell selected, as in GWT.
      const fromEditor = this.hosted.raisedInEditor(e);
      if (!fromEditor || !leavesEditor(e)) {
        return;
      }
      this.scrollContainer.focus();
      if (e.key !== 'Tab') {
        return;
      }
      e.preventDefault();
    } else if (this.keyboardExit.takes(e)) {
      return;
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
    const { activeCell } = this.selection;
    if (startsEditing && this.hosted.focusEditorOf(activeCell.col, activeCell.row)) {
      return;
    }

    // Ctrl+A / Cmd+A — select the entire sheet.
    if (shortcut && (key === 'a' || key === 'A')) {
      if (this.model.rows <= 0 || this.model.cols <= 0) {
        return;
      }
      e.preventDefault();
      this.store.select({
        activeCell: { col: 1, row: 1 },
        activeRange: normalizedRange(1, 1, this.model.cols, this.model.rows),
        auxRanges: []
      });
      this.report.selection(true);
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
        !this.store.selectsRange() &&
        !this.model.hiddenRowIndexes.has(this.selection.activeCell.row) &&
        !this.model.hiddenColumnIndexes.has(this.selection.activeCell.col));
    if (entersEditor && !this.editor) {
      if (this.model.rows <= 0 || this.model.cols <= 0) {
        return;
      }
      const { col, row } = this.selection.activeCell;
      // F2 opens the existing value, so arrows move the caret.
      if (this.edit.openExisting(col, row)) {
        e.preventDefault();
      }
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
      if (this.edit.openTyping(col, row, key)) {
        e.preventDefault();
      }
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
    if (walkDirection !== null && this.model.rows > 0 && this.model.cols > 0 && this.store.selectsRange()) {
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
          this.store.sheetShape()
        );
        if (grown === null) {
          return;
        }
        this.store.select({ activeCell: this.selection.activeCell, activeRange: grown.range, auxRanges: [] });
        this.store.extendSelection(true);
        this.scrollCellIntoView(grown.moved);
      } else {
        const next = moveActiveCell(this.selection.activeCell, this.mergeEntry, direction, this.store.sheetShape());
        if (next === null) {
          return;
        }
        this.moveSelectionTo(next);
      }
      this.report.selection();
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
    this.report.selection();
  };

  /**
   * Move the selection to one cell - or merge - from the keyboard. The cell
   * is scrolled into view before the render - see {@link scrollToShow} - and
   * named in the address field as the server hears of it.
   */
  private moveSelectionTo(next: SingleCellSelection): void {
    this.store.extendSelection(false);
    this.store.pick(next);
    this.scrollCellIntoView(next.activeCell);
  }

  /**
   * Put the active cell somewhere else inside the range that is already
   * selected, keeping the range itself.
   *
   * The server hears that only the active cell moved; see
   * {@link SelectionReporter.activeCellMoved}.
   */
  private moveActiveCellInsideSelection(next: { col: number; row: number }): void {
    this.store.select({
      activeCell: next,
      activeRange: this.selection.activeRange,
      auxRanges: this.selection.auxRanges
    });
    this.scrollCellIntoView(next);
    this.report.activeCellMoved(next);
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
      (c) => this.store.colWidthPx(c)
    );
    const top = scrollToShow(
      target.row,
      this.model.verticalSplitPosition,
      this.model.rows,
      sc.scrollTop,
      viewHeight,
      (r) => this.store.rowHeightPx(r)
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
   * Send an event to the server. Dispatched on the host alone, where Flow
   * listens: a bubbling event also reached the server component of a
   * spreadsheet this one sits in - as popup content, a custom editor or a
   * cell's component - which applied it to its own cells. What is drawn is
   * drawn first; see {@link RenderScheduler}. The selection goes through
   * {@link report}, which keeps the cell the server holds as selected.
   */
  private sendToServer(detail: SpreadsheetEvent): void {
    this.renders.beforeSend();
    this.host.dispatchEvent(new CustomEvent('spreadsheet-event', { detail }));
  }

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
  private showSelectedCellInFormulaBar(cell: CellPosition): void {
    // The address field names the cell until the server names the selection.
    this.store.nameSelection(null);
    this.edit.showServerValue(this.store.formulaFieldValueFor(cell.col, cell.row), false);
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
    this.sync.drawn();
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
    this.hosted.clear();
    this.popup.dispose();
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
   * Mousedown on the fill handle. The drag that follows paints rather than
   * selects, so the current range is remembered as the one being grown or
   * shrunk.
   */
  private readonly handleSelectionPaintStart = (): void => {
    const { c1, c2, r1, r2 } = this.selection.activeRange;
    this.store.changePress({ kind: 'fill', from: { c1, c2, r1, r2 }, outcome: { kind: 'none' } });
  };

  /**
   * Release of a fill-handle drag: tell the server what to copy or clear.
   *
   * The two directions are different operations, and the server takes them
   * through different events — growing reports the whole new rectangle,
   * shrinking reports only the corner the selection retreats to.
   */
  private finishPainting(painting: { from: CellRect; outcome: PaintOutcome }): void {
    this.store.changePress(null);
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
    return this.store.derived.sheetSizes(this.model);
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
    this.formulaBar.formulaLocked = this.store.isCellLockedForEdit(col, row);
    const press = this.interaction.press;
    this.formulaBar.renderState = this.store.derived.formulaBar({
      bar: this.store.formulaBar,
      writing: this.store.writing,
      selection: this.selection,
      growing: this.interaction.extending || (press?.kind === 'select' && press.crossed),
      draft: this.store.draft,
      sheetName: this.activeSheetName()
    });
    this.drawWriting();
    this.sheetTabs.renderState = this.tabs.renderState();
    this.contextMenu.renderState = this.menu.renderState();
    this.menu.drawn();
    this.hosted.place(state, slots.assignments);
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
      const colGroupings = this.store.derived.columnGroupings(this.model);
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
        selectedColumns: this.store.derived.selectedColumns(this.selection)
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
      const rowGroupings = this.store.derived.rowGroupings(this.model);
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
        selectedRows: this.store.derived.selectedRows(this.selection),
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
    // Header corner. Shown only when BOTH its dimensions are > 0, so it
    // fills its grid cell with matching width + height. With either at 0 -
    // the headings off and only one axis grouped - it is hidden: its border
    // would otherwise keep a 1px row or column of the grid, and every cell
    // would sit a pixel off.
    this.headerCorner.hidden = cornerWidth === 0 || cornerHeight === 0;
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
    const frozen = this.store.derived.frozenPanes(model);

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
      formulaReferences: this.store.derived.formulaReferences(this.store.draft, this.activeSheetName()),
      overlays: positionedOverlays,
      // Each at the bottom right corner of its cell.
      popupButtons: this.store.derived.popupButtons(this.popup.buttons(), model),
      paintRect: this.store.paintRect(),
      commentText: this.model.cellComments,
      commentAuthors: this.model.cellCommentAuthors,
      visibleCommentKeys: this.model.visibleCellComments,
      editingCommentKey: this.interaction.comment,
      invalidFormulaMessage: this.model.invalidFormulaErrorMessage,
      commentCells: this.store.derived.commentCells(model),
      invalidFormulaCells: this.model.invalidFormulaCells,
      hyperlinkCells: this.store.derived.hyperlinkCells(model)
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
    if (e.key !== 'Shift' || !this.interaction.extending) {
      return;
    }
    this.store.extendSelection(false);
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
    this.scrollContainer.setAttribute('aria-label', shownSheetName || 'Spreadsheet');
    if (changedPropertyNames.length > 0 || firstUpdate) {
      this.recomputeViewport();
    }
  }

  // ====================================================================
  // 12 explicit RPC methods (server→client action messages)
  // ====================================================================

  updateBottomRightCellValues(cellData: CellData[]): void {
    this.sync.store(cellData);
  }
  updateTopLeftCellValues(cellData: CellData[]): void {
    this.sync.store(cellData);
  }
  updateTopRightCellValues(cellData: CellData[]): void {
    this.sync.store(cellData);
  }
  updateBottomLeftCellValues(cellData: CellData[]): void {
    this.sync.store(cellData);
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
    // common case. While the user is still growing a selection the
    // address field counts the block out whatever the server names it;
    // see addressFieldText.
    this.store.nameSelection(possibleName);
    this.edit.showServerValue(this.store.formulaFieldValueFor(col, row), true);
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
    const before = this.selection.activeCell;
    this.store.select(selectSingleCell(col, row));
    this.sheetData.serverSelectedCell = { col, row };
    this.store.nameSelection(name);
    this.edit.showServerValue(
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
  showActions(actionDetails: SpreadsheetActionDetails[] | null): void {
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
    this.store.select({
      activeCell: { col, row },
      activeRange: normalizedRange(c1, r1, c2, r2),
      auxRanges: this.selection.auxRanges
    });
    this.sheetData.serverSelectedCell = { col, row };
    // As in updateFormulaBar: the server names a selection only when it has
    // a name, and leaves an ordinary one to the client.
    this.store.nameSelection(name);
    // A new active cell shows in the field. The same cell keeps what the
    // field has: the server may have just sent its value along with
    // showSelectedCell, for a cell this client holds no data for.
    if (moved) {
      this.edit.showServerValue(this.store.formulaFieldValueFor(col, row), false);
    }
    if (scroll) {
      this.scrollCellIntoView({ col, row });
    }
  }
  cellsUpdated(updatedCellData: CellData[]): void {
    this.sync.store(updatedCellData);
  }
  /** The cells to have, in place of every cell there is; see {@link ServerSync.replace}. */
  cellsReplaced(cellData: CellData[]): void {
    this.sync.replace(cellData);
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
    this.store.interact({ ...this.interaction, comment: toKey(col + 1, row + 1) });
  }

  // ====================================================================
  // Property mirrors - what the server sets on the element. Those that
  // only hold a value are written through the table in state/SheetModel.ts.
  // ====================================================================

  setRowBufferSize(pixels: number): void {
    this.store.write('rowBufferSize', pixels);
  }
  setColumnBufferSize(pixels: number): void {
    this.store.write('columnBufferSize', pixels);
  }
  setRows(count: number): void {
    this.store.write('rows', count);
  }
  setCols(count: number): void {
    this.store.write('cols', count);
  }
  setDefRowH(heightInPoints: number): void {
    this.store.write('defRowH', heightInPoints);
  }
  setDefColW(widthInPixels: number): void {
    this.store.write('defColW', widthInPixels);
  }
  /**
   * Row heights from the server. Rows above the screen grow and shrink as
   * their components are loaded and let go of; the first row with its top
   * on screen stays where it is, and so does what is on screen. Moved, the
   * rows on screen were others than those loaded, and loading those moved
   * them on again: 8 requests after one jump, to GWT's 3.
   */
  setRowH(rowHeightsInPoints: number[] | Float32Array | SizeRuns | null): void {
    this.view.rowAnchor ??= this.firstRowWithTopOnScreen();
    this.store.write('rowH', rowHeightsInPoints);
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
  setColW(colWidthsInPixels: number[] | Int32Array | SizeRuns | null): void {
    this.store.write('colW', colWidthsInPixels);
  }
  setHiddenColumnIndexes(indexes: number[] | null): void {
    this.store.write('hiddenColumnIndexes', indexes);
  }
  setHiddenRowIndexes(indexes: number[] | null): void {
    this.store.write('hiddenRowIndexes', indexes);
  }
  setLockedColumnIndexes(indexes: number[] | null): void {
    this.store.write('lockedColumnIndexes', indexes);
  }
  setLockedRowIndexes(indexes: number[] | null): void {
    this.store.write('lockedRowIndexes', indexes);
  }
  setSheetIndex(index: number): void {
    this.tabs.setSheetIndex(index);
  }
  setSheetNames(names: string[] | null): void {
    this.tabs.setSheetNames(coerceArray(names));
  }
  /**
   * Where each sheet was last scrolled to, by visible sheet, as the server
   * keeps it: a sheet shown again goes back there.
   */
  setVerticalScrollPositions(positions: number[] | null): void {
    this.store.write('verticalScrollPositions', positions);
  }
  /** See {@link setVerticalScrollPositions}. */
  setHorizontalScrollPositions(positions: number[] | null): void {
    this.store.write('horizontalScrollPositions', positions);
  }
  setCellStyleToCSSStyle(map: Record<number, string> | null): void {
    this.store.write('cellStyleToCSSStyle', map);
    this.sync.restyled();
  }
  setRowIndexToStyleIndex(map: Record<number, number> | null): void {
    this.store.write('rowIndexToStyleIndex', map);
  }
  setColumnIndexToStyleIndex(map: Record<number, number> | null): void {
    this.store.write('columnIndexToStyleIndex', map);
  }
  setShiftedCellBorderStyles(cssRules: string[] | null): void {
    this.store.write('shiftedCellBorderStyles', cssRules);
  }
  setConditionalFormattingStyles(map: Record<number, string> | null): void {
    this.store.write('conditionalFormattingStyles', map);
  }
  setColGroupingData(groups: GroupingData[] | null): void {
    this.store.write('colGroupingData', groups);
  }
  setRowGroupingData(groups: GroupingData[] | null): void {
    this.store.write('rowGroupingData', groups);
  }
  setColGroupingMax(level: number): void {
    this.store.write('colGroupingMax', level);
  }
  setRowGroupingMax(level: number): void {
    this.store.write('rowGroupingMax', level);
  }
  setColGroupingInversed(inversed: boolean): void {
    this.store.write('colGroupingInversed', inversed);
  }
  setRowGroupingInversed(inversed: boolean): void {
    this.store.write('rowGroupingInversed', inversed);
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
    const cell = this.hosted.cellOfEditor(e);
    if (cell === null) {
      return;
    }
    if (this.selection.activeCell.col === cell.col && this.selection.activeCell.row === cell.row) {
      return;
    }
    this.store.select(selectSingleCell(cell.col, cell.row));
    this.report.selection();
  };

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
    return this.store.derived.cellSlots(this.model, active === null ? null : toKey(active.col, active.row));
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
      // now.
      this.store.resetSheetData();
      // Its row heights are those of whatever it shows now.
      this.view.rowAnchor = null;
      // What it was told of where the sheet is, and asked to load, is gone
      // too: the sheet introduces itself again, and loads again even the
      // same range.
      this.sync.reset();
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
    // What was written goes with it, and the formula's colours with that.
    this.store.interact(IDLE);
    this.formulaBar.abandonFormulaEdit();
    this.headerResize.cancel();
    this.menu.close();
    this.linkTooltip.hide();
    if (hadKeyboard) {
      this.returnFocusToSheet();
    }
  }
  setSheetProtected(isProtected: boolean): void {
    this.store.write('sheetProtected', isProtected);
  }
  setMergedRegions(regions: MergedRegion[] | null): void {
    this.store.write('mergedRegions', regions);
  }
  setCellKeysToEditorIdMap(map: Record<string, string> | null): void {
    this.store.write('cellKeysToEditorIdMap', map);
  }
  setComponentIDtoCellKeysMap(map: Record<string, string>): void {
    this.store.write('componentIDtoCellKeysMap', map);
  }
  setCellComments(map: Record<string, string> | null): void {
    this.store.write('cellComments', map);
  }
  setCellCommentAuthors(map: Record<string, string> | null): void {
    this.store.write('cellCommentAuthors', map);
  }
  setVisibleCellComments(keys: string[] | null): void {
    this.store.write('visibleCellComments', keys);
  }
  setInvalidFormulaCells(keys: string[] | null): void {
    this.store.write('invalidFormulaCells', keys);
  }
  setOverlays(map: Record<string, OverlayInfo> | null): void {
    this.store.write('overlays', map);
  }
  setShowCustomEditorOnFocus(show: boolean): void {
    this.store.write('showCustomEditorOnFocus', show);
  }
  setHasComponentFactory(has: boolean): void {
    this.store.write('hasComponentFactory', has);
  }
  setInvalidFormulaErrorMessage(message: string | null): void {
    this.store.write('invalidFormulaErrorMessage', message);
  }
  setNamedRanges(names: string[] | null): void {
    this.store.setNamedRanges(coerceArray(names));
  }
  setResources(host: HTMLElement, keys: string[] | null): void {
    // The server sends the resource KEYS here and puts each URL on the host
    // as a `resource-<key>` attribute, because the URLs are Flow stream
    // resources minted per element rather than plain strings
    // (Spreadsheet.setResource). Resolving them means reading the
    // attributes, not parsing a map out of this argument.
    this.resourceHost = host;
    this.store.write('resources', keys);
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
    this.store.write('verticalSplitPosition', value);
  }
  setHorizontalSplitPosition(value: number): void {
    this.store.write('horizontalSplitPosition', value);
  }

  // Display flags
  setDisplayGridlines(visible: boolean): void {
    this.store.write('displayGridlines', visible);
    // Turned off or on from the server on its own, with no cell data to
    // carry a repaint along.
    this.scheduleRender();
  }
  setDisplayRowColHeadings(visible: boolean): void {
    // The server sends only this boolean, never the strips' sizes: shown,
    // they get their default sizes back; hidden, they collapse. They are
    // drawn with the rest of the batch this came in, as every property is,
    // not halfway through it.
    this.store.write('displayRowColHeadings', visible);
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
    this.sync.workbookLoaded();
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
  setHyperlinksTooltips(tooltips: Record<string, string> | null): void {
    this.store.write('hyperlinksTooltips', tooltips);
  }
  // ====================================================================
  // Popup-button lifecycle
  // ====================================================================

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
  addPopupButton(sent: PopupButtonState): void {
    const state = coerceJson(sent);
    if (state !== null && typeof state.col === 'number') {
      this.popup.addButton(state);
    }
  }

  /** A popup button was taken away, and its popup closes with it. */
  removePopupButton(sent: PopupButtonState): void {
    const state = coerceJson(sent);
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
