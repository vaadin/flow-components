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
  type CellPosition,
  type CellRange,
  isCellInSelection,
  normalizedRange,
  type SelectionState,
  selectSingleCell
} from './state/SelectionState.js';
import type { SpreadsheetEvent } from './types/rpc-client-to-server.js';
import type { CellEditorState } from './elements/types.js';
import { FALLBACK_PPI, getDefaultRowHeightPx, withSize } from './algorithms/ViewportMath.js';
import type { CellSlots } from './state/Derived.js';
import { RenderScheduler } from './state/RenderScheduler.js';
import { IDLE, type InteractionState } from './state/Interaction.js';
import type { SheetData } from './state/SheetData.js';
import { SheetStore } from './state/SheetStore.js';
import { type SheetModel } from './state/SheetModel.js';
import { toKey } from './algorithms/CellKeys.js';
import { flowVirtualChildResolver } from './dom/VirtualChildren.js';
import { SpreadsheetCellGrid } from './elements/SpreadsheetCellGrid.js';
import { listen } from './dom/Listen.js';
import { listenToClipboard } from './controllers/Clipboard.js';
import { onDocument, type PageListener, PageListeners } from './controllers/PageListeners.js';
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
import { KeyboardNavigation } from './controllers/KeyboardNavigation.js';
import { PointerSelection } from './controllers/PointerSelection.js';
import { FormulaPointer } from './controllers/FormulaPointer.js';
import { Outline } from './controllers/Outline.js';
import { type ScrollView, SheetRenderer } from './render/SheetRenderer.js';
import { SheetLayout } from './dom/SheetLayout.js';
import { listenForLongPress } from './dom/LongPress.js';
import { flowRequestInFlight, overlayComponentAppId } from './dom/FlowClient.js';
import { isIOS, isMacPlatform } from './dom/Platform.js';
import { coerceArray, coerceJson } from './wire/Coerce.js';
import { SpreadsheetColumnHeader } from './elements/SpreadsheetColumnHeader.js';
import { SpreadsheetRowHeader } from './elements/SpreadsheetRowHeader.js';
import { SpreadsheetHeaderCorner } from './elements/SpreadsheetHeaderCorner.js';
import { SpreadsheetSheetTabs } from './elements/SpreadsheetSheetTabs.js';
import { SpreadsheetFormulaBar } from './elements/SpreadsheetFormulaBar.js';
import { SpreadsheetContextMenu } from './elements/SpreadsheetContextMenu.js';
import { ViewportLoader } from './state/ViewportLoader.js';
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

/** A press or a click on a cell, with the keys held. */
type CellPressEvent = CustomEvent<{
  col: number;
  row: number;
  shiftKey: boolean;
  ctrlKey: boolean;
  metaKey: boolean;
  altKey: boolean;
}>;

/** A click on a column letter or a row number, with the keys held. */
type HeaderClickEvent<T> = CustomEvent<T & { shiftKey: boolean; ctrlKey: boolean; metaKey: boolean; altKey: boolean }>;

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
    scrollIntoView: (cell) => this.renderer.scrollCellIntoView(cell)
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
    moveInsideSelection: (cell) => this.keyboard.moveInsideSelection(cell),
    scrollIntoView: (cell) => this.renderer.scrollCellIntoView(cell),
    scheduleRender: () => this.scheduleRender(),
    send: (event) => this.sendToServer(event)
  });

  /** Selecting with the pointer; see {@link PointerSelection}. */
  private readonly pointerSelection = new PointerSelection(this.store, this.report, this.pointing, {
    scrollTop: () => this.scrollContainer.scrollTop,
    scrollLeft: () => this.scrollContainer.scrollLeft,
    finishHeaderResize: () => this.headerResize.finish(),
    cellOfEditor: (e) => this.hosted.cellOfEditor(e),
    send: (event) => this.sendToServer(event)
  });

  /** The keyboard on the sheet; see {@link KeyboardNavigation}. */
  private readonly keyboard = new KeyboardNavigation(this.store, this.report, this.edit, {
    raisedInside: (e) => this.hosted.raisedInside(e),
    raisedInEditor: (e) => this.hosted.raisedInEditor(e),
    focusEditorOf: (col, row) => this.hosted.focusEditorOf(col, row),
    exits: (e) => this.keyboardExit.takes(e),
    focusSheet: () => this.scrollContainer.focus(),
    isMac: () => isMacPlatform(),
    scrollIntoView: (cell) => this.renderer.scrollCellIntoView(cell),
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

  /**
   * What the client knows of the sheet the server shows: its cells, and the
   * cell the server holds as selected. Replaced whole on a reload; see
   * {@link setReload}.
   */
  private get sheetData(): SheetData {
    return this.store.sheetData;
  }

  /** Draws the sheet; see {@link SheetRenderer}. */
  private readonly renderer: SheetRenderer;

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
    drawnRange: () => this.renderer.drawnRange(),
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
    load: (range, drawn) => this.sync.load(range, drawn)
  });

  /** Keeps the server in step with what the sheet shows; see {@link ServerSync}. */
  private readonly sync = new ServerSync(
    {
      size: () => this.model,
      cellStyles: () => this.model.cellStyleToCSSStyle,
      cells: () => this.sheetData.cells,
      drawnRange: () => this.renderer.drawnRange(),
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
   * (A, B, ..., AA) above the cells. Hidden while the server hides
   * the headings and groups no columns; see {@link SheetRenderer}.
   */
  private readonly columnHeader: SpreadsheetColumnHeader;

  /**
   * The row-header sub-element. Sibling of the cell-grid
   * inside the scroll container; renders the row numbers
   * (1, 2, 3, ...) at the left of the cells. Hidden while the server
   * hides the headings and groups no rows; see {@link SheetRenderer}.
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

  /** The sheet's own elements, laid out; see {@link SheetLayout}. */
  private readonly layout: SheetLayout;

  /** The component's own root inside {@link renderTarget}; see {@link SheetLayout.root}. */
  private readonly root: HTMLElement;
  /** The scroll container: the headers and the cells; see {@link SheetLayout.scrollContainer}. */
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
      { cellSlotAssignments: () => this.renderer.cellSlotAssignments() }
    );
    this.popup = new Popup(
      host,
      resolveVirtualChild,
      this.cellGrid,
      {
        cellValue: (col, row) => this.sheetData.cells.get(toKey(col, row))?.value ?? '',
        paneClientRect: () => this.renderer.scrollingPaneClientRect(),
        focusSheet: () => this.scrollContainer.focus({ preventScroll: true }),
        scheduleRender: () => this.scheduleRender(),
        send: (event) => this.sendToServer(event)
      },
      this.ownListeners.signal
    );
    this.layout = new SheetLayout(
      host,
      {
        cellGrid: this.cellGrid,
        columnHeader: this.columnHeader,
        rowHeader: this.rowHeader,
        headerCorner: this.headerCorner,
        formulaBar: this.formulaBar,
        sheetTabs: this.sheetTabs,
        contextMenu: this.contextMenu
      },
      this.ownListeners.signal,
      () => this.renderer.insetPagedView()
    );
    this.root = this.layout.root;
    this.scrollContainer = this.layout.scrollContainer;
    this.renderer = new SheetRenderer(
      this.store,
      {
        scrollContainer: this.scrollContainer,
        cellGrid: this.cellGrid,
        columnHeader: this.columnHeader,
        rowHeader: this.rowHeader,
        headerCorner: this.headerCorner,
        formulaBar: this.formulaBar,
        sheetTabs: this.sheetTabs,
        contextMenu: this.contextMenu
      },
      {
        drawingStarts: () => this.renders.drawn(),
        drawn: () => this.sync.drawn(),
        sheetName: () => this.activeSheetName(),
        tabs: () => this.tabs.renderState(),
        menu: () => this.menu.renderState(),
        menuDrawn: () => this.menu.drawn(),
        placeHosted: (drawn, assignments) => this.hosted.place(drawn, assignments),
        popupButtons: () => this.popup.buttons()
      }
    );
    this.on(this.formulaBar, 'sheet-address-changed', this.handleSheetAddressChanged);
    this.on(this.formulaBar, 'formula-commit', (e: FormulaCommitEvent) => this.edit.fieldCommitted(e.detail));
    this.on(this.formulaBar, 'formula-cancel', () => this.edit.fieldCancelled());
    // What is typed is kept, and drawn into the other input; see SheetRenderer.
    this.on(this.formulaBar, 'formula-input', (e: InputEvent) =>
      this.pointing.typed(e.detail.value, e.detail.caret ?? e.detail.value.length, 'field')
    );
    this.on(this.formulaBar, 'formula-arrow', (e: FormulaArrowEvent) => this.pointing.arrow(e, 'field'));
    this.on(this.formulaBar, 'formula-caret', (e: CaretEvent) =>
      this.pointing.caretMoved(e.detail.value, e.detail.caret, 'field')
    );
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
    // Honor `Spreadsheet.HIDE_TABSHEET_STYLE` ("hidetabsheet") and the theme
    // on the wrapper host; see SheetLayout.
    this.layout.followHost();
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
    this.on(this.cellGrid, 'cell-click', (e: CellPressEvent) => this.pointerSelection.click(e.detail));
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
    this.on(this.cellGrid, 'cell-mousedown', (e: CellPressEvent) => this.pointerSelection.press(e));
    this.on(this.cellGrid, 'cell-mouseover', (e: CustomEvent<{ col: number; row: number; box?: DOMRect }>) => {
      this.linkTooltip.show(e.detail);
      this.pointerSelection.over(e.detail.col, e.detail.row);
    });
    this.on(this.cellGrid, 'grid-mouseleave', this.linkTooltip.hide);
    this.on(this.cellGrid, 'selection-paint-start', () => this.pointerSelection.fillStarted());
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
    this.on(this.columnHeader, 'column-header-click', (e: HeaderClickEvent<{ col: number }>) =>
      this.pointerSelection.columnHeaderClicked(e.detail)
    );
    this.on(this.rowHeader, 'row-header-click', (e: HeaderClickEvent<{ row: number }>) =>
      this.pointerSelection.rowHeaderClicked(e.detail)
    );
    // Keyboard navigation. Listen on the scroll
    // container so arrow keys + shift+arrow work whenever the
    // spreadsheet has focus. preventDefault() in the handler
    // stops the browser from scrolling the viewport via the
    // arrow keys.
    this.on(this.scrollContainer, 'keydown', (e: KeyboardEvent) => this.keyboard.keyDown(e));
    this.on(this.scrollContainer, 'pointerdown', (e: Event) => this.pointerSelection.editorPressed(e));
    // Safari on iOS opens no menu on a long press, so it is made a
    // right-click there, on the cells and the headers alike.
    this.stopLongPress = Spreadsheet.platform.isIOS() ? listenForLongPress(this.scrollContainer) : () => {};
    this.resizeObserver = new ResizeObserver(() => this.recomputeViewport());
    this.parts = [
      this.viewportLoader,
      new PageListeners(
        this.handlingAll([
          onDocument('pointerdown', this.handleDocumentPointerDown),
          onDocument('keydown', this.handleContextMenuKeyDown),
          onDocument('mouseup', () => this.pointerSelection.release()),
          onDocument('keyup', (e) => this.keyboard.keyUp(e)),
          onDocument('mousemove', this.handleDocumentMouseMove)
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

  /** The pointer moved on the page: a header drag follows it. */
  private readonly handleDocumentMouseMove = (e: MouseEvent): void => {
    this.headerResize.moved(e);
  };

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
   * Scroll-event handler: tell the loader where the sheet is, recompute
   * the visible viewport and push a fresh render state to the cell-grid.
   * The loader decides whether the server needs asking for cells now - see
   * {@link ViewportLoader}.
   */
  private readonly handleScroll = (): void => {
    // The sheet keeping a row in place is not the user scrolling.
    if (this.renderer.scrolled()) {
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
    this.renderer.recompute();
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
    this.layout.dispose();
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

  /** Test-only accessor: the scroll container element. */
  getScrollContainer(): HTMLElement {
    return this.scrollContainer;
  }

  /** Test-only accessor: the overlay an open popup button's content is in. */
  getPopupOverlay(): HTMLElement {
    return this.popup.overlay;
  }

  // ====================================================================
  // Render pipeline — compose `CellGridRenderState` from
  // current state + push to the cell-grid sub-element.
  // ====================================================================

  applyRenderState(opts: ScrollView): void {
    this.renderer.apply(opts);
  }

  /**
   * Pure: build a {@code CellGridRenderState} from the current
   * internal state + the given scroll / viewport. Exposed for
   * tests to call without going through the DOM (the public
   * {@link applyRenderState} writes the result to the cell-grid;
   * this lets tests inspect it directly).
   */
  buildRenderState(opts: ScrollView, slots?: CellSlots): CellGridRenderState {
    return this.renderer.build(opts, slots);
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
    return this.renderer.resources();
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
    this.layout.setWidth(width);
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
    this.layout.setHeight(height);
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
    this.renderer.sheetShown(
      this.tabs.sheetNames[index] ?? null,
      {
        top: this.model.verticalScrollPositions[index] ?? 0,
        left: this.model.horizontalScrollPositions[index] ?? 0
      },
      !firstUpdate && changedPropertyNames.includes('sheetIndex')
    );
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
      this.renderer.scrollCellIntoView({ col, row });
    }
  }
  /** Any click outside the menu dismisses it, as a native menu would. */
  private readonly handleDocumentPointerDown = (e: Event): void => {
    const path = e.composedPath();
    this.popup.pressed(path);
    this.menu.closeUnlessPressedIn(path);
  };

  private readonly handleContextMenuKeyDown = (e: KeyboardEvent): void => {
    if (e.key !== 'Escape') {
      return;
    }
    this.popup.dismiss();
    this.menu.close();
  };
  showActions(actionDetails: SpreadsheetActionDetails[] | null): void {
    this.menu.showActions(coerceArray<SpreadsheetActionDetails>(actionDetails));
  }
  setSelectedCellAndRange(
    name: string | null,
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
      this.renderer.scrollCellIntoView({ col, row });
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
    this.renderer.holdRowInPlace();
    this.store.write('rowH', rowHeightsInPoints);
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
      this.renderer.forgetRowAnchor();
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
    this.renderer.useResourcesOf(host);
    this.store.write('resources', keys);
    this.scheduleRender();
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
