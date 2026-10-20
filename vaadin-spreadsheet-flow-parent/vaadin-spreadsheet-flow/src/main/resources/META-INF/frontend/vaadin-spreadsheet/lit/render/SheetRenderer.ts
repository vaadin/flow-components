/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { toKey } from '../algorithms/CellKeys.js';
import { computeMergedRegionBoxes, computePositionedOverlays } from '../algorithms/Positioning.js';
import {
  computeViewportBounds,
  FALLBACK_PPI,
  freezeGap,
  indexAtPoint,
  pagedViewInset,
  scrollToShow
} from '../algorithms/ViewportMath.js';
import type { CellPanes } from '../controllers/DragScroll.js';
import type { CellSlotAssignment } from '../dom/VirtualChildren.js';
import type { SpreadsheetCellGrid } from '../elements/SpreadsheetCellGrid.js';
import type { SpreadsheetColumnHeader } from '../elements/SpreadsheetColumnHeader.js';
import { columnGroupingGutterPx, rowGroupingGutterPx } from '../elements/headers/Grouping.js';
import type { ContextMenuRenderState, SpreadsheetContextMenu } from '../elements/SpreadsheetContextMenu.js';
import type { SpreadsheetFormulaBar } from '../elements/SpreadsheetFormulaBar.js';
import type { SpreadsheetHeaderCorner } from '../elements/SpreadsheetHeaderCorner.js';
import type { SpreadsheetRowHeader } from '../elements/SpreadsheetRowHeader.js';
import type { SheetTabsRenderState, SpreadsheetSheetTabs } from '../elements/SpreadsheetSheetTabs.js';
import type { CellGridRenderState } from '../elements/types.js';
import { type CellSlots, drawnColumnWidth, drawnRowHeight, type SheetSizes } from '../state/Derived.js';
import type { SheetStore } from '../state/SheetStore.js';
import type { LoadRange } from '../state/ViewportLoader.js';
import { ViewState } from '../state/ViewState.js';
import type { PopupButtonState } from '../types/wire.js';

/** Height of the column headers, in pixels. */
const DEFAULT_HEADER_ROW_HEIGHT = 27;

/**
 * Width of the row headers, in pixels: 50, which fits a six-digit row
 * number.
 */
const DEFAULT_HEADER_COLUMN_WIDTH = 50;

/** A cell, by its column and row, 1-based. */
interface Cell {
  col: number;
  row: number;
}

/** Where the cells are scrolled to, and how much of them the scroll container shows. */
export interface ScrollView {
  scrollLeft: number;
  scrollTop: number;
  viewportWidth: number;
  viewportHeight: number;
}

/** The parts of the sheet that are drawn. */
export interface SheetParts {
  readonly scrollContainer: HTMLElement;
  readonly cellGrid: SpreadsheetCellGrid;
  readonly columnHeader: SpreadsheetColumnHeader;
  readonly rowHeader: SpreadsheetRowHeader;
  readonly headerCorner: SpreadsheetHeaderCorner;
  readonly formulaBar: SpreadsheetFormulaBar;
  readonly sheetTabs: SpreadsheetSheetTabs;
  readonly contextMenu: SpreadsheetContextMenu;
}

/** What drawing the sheet needs of the rest of it. */
export interface SheetRendererHost {
  /** A drawing starts, with everything asked for so far; see {@code RenderScheduler.drawn}. */
  drawingStarts(): void;
  /** The sheet is drawn: the server hears of it; see {@code ServerSync.drawn}. */
  drawn(): void;
  /** The name of the sheet shown. */
  sheetName(): string;
  /** What the sheet tabs draw. */
  tabs(): SheetTabsRenderState;
  /** What the context menu draws. */
  menu(): ContextMenuRenderState;
  /** The context menu has been handed what it draws. */
  menuDrawn(): void;
  /** Places the components the sheet hosts, for what was drawn. */
  placeHosted(drawn: CellGridRenderState, assignments: Readonly<Record<string, CellSlotAssignment>>): void;
  /** The popup buttons on the sheet. */
  popupButtons(): readonly PopupButtonState[];
}

/**
 * Draws the sheet: the cell grid, the headers and their corner, the formula
 * bar, the sheet tabs and the context menu, from the store and the scroll
 * position, in one go. Also keeps what the view remembers from one drawing
 * to the next: the row to keep in place, the scroll to restore.
 */
export class SheetRenderer {
  /** What the view remembers from one drawing to the next; see {@link ViewState}. */
  private readonly view = new ViewState();

  /** The element the `resource-<id>` attributes live on; see {@link useResourcesOf}. */
  private resourceHost: HTMLElement | null = null;

  constructor(
    private readonly store: SheetStore,
    private readonly parts: SheetParts,
    private readonly host: SheetRendererHost
  ) {}

  /**
   * Recompute the visible viewport from the scroll container's
   * current scroll position + client dimensions, then push the
   * resulting render state to the parts.
   */
  recompute(): void {
    this.host.drawingStarts();
    const { scrollContainer } = this.parts;
    if (this.view.pendingSheetScroll !== null) {
      // A sheet shown again goes back to where it was scrolled. The grid is
      // sized for it first: against the last sheet's size the browser would
      // cut the offsets short.
      const { top, left } = this.view.pendingSheetScroll;
      this.view.pendingSheetScroll = null;
      // The rows are another sheet's: there is no row to keep in place.
      this.view.rowAnchor = null;
      this.apply({
        scrollLeft: 0,
        scrollTop: 0,
        viewportWidth: scrollContainer.clientWidth,
        viewportHeight: scrollContainer.clientHeight
      });
      scrollContainer.scrollTop = top;
      scrollContainer.scrollLeft = left;
    }
    this.keepRowAnchored();
    this.apply(this.currentView());
    this.host.drawn();
  }

  /** Draws the sheet for a scroll position. */
  apply(opts: ScrollView): void {
    const { store, parts } = this;
    const { model, selection, interaction } = store;
    const slots = this.cellSlots();
    const state = this.build(opts, slots);
    // Size the cell-grid to the full virtual grid so the scroll
    // surface's scrollWidth / scrollHeight reflect actual extent.
    // Cells render at absolute virtual coordinates inside; the
    // scroll container clips + scrolls them visually.
    parts.cellGrid.style.width = `${state.totalGridWidthPx}px`;
    parts.cellGrid.style.height = `${state.totalGridHeightPx}px`;
    parts.cellGrid.renderState = state;
    const { col, row } = selection.activeCell;
    parts.formulaBar.formulaLocked = store.isCellLockedForEdit(col, row);
    const press = interaction.press;
    parts.formulaBar.renderState = store.derived.formulaBar({
      bar: store.formulaBar,
      writing: store.writing,
      selection,
      growing: interaction.extending || (press?.kind === 'select' && press.crossed),
      draft: store.draft,
      sheetName: this.host.sheetName()
    });
    this.drawWriting();
    parts.sheetTabs.renderState = this.host.tabs();
    parts.contextMenu.renderState = this.host.menu();
    this.host.menuDrawn();
    this.host.placeHosted(state, slots.assignments);
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
    const headerRowHeight = this.headerRowHeight();
    const headerColumnWidth = this.headerColumnWidth();
    const rowGutterPx = rowGroupingGutterPx(model.rowGroupingMax, headerColumnWidth > 0);
    const colGutterPx = columnGroupingGutterPx(model.colGroupingMax, headerRowHeight > 0);
    const { width: cornerWidth, height: cornerHeight } = this.headerExtent();
    if (headerRowHeight > 0 || model.colGroupingMax > 0) {
      // The outline's pixels are worked out here, which keeps the header
      // element independent of the full colWidths array.
      const colGroupings = store.derived.columnGroupings(model);
      parts.columnHeader.hidden = false;
      parts.columnHeader.style.width = `${state.totalGridWidthPx}px`;
      parts.columnHeader.style.height = `${headerRowHeight + colGutterPx}px`;
      parts.columnHeader.renderState = {
        viewport: state.viewport,
        colX: state.colX,
        colWidths: state.colWidths,
        headerHeight: headerRowHeight,
        totalGridWidthPx: state.totalGridWidthPx,
        // Frozen columns stay on screen whatever the scroll position,
        // so their headers travel with them.
        horizontalSplitPosition: state.horizontalSplitPosition,
        frozenRows: state.verticalSplitPosition > 0,
        scrollTop: state.scrollTop,
        frozenColX: state.frozenColX,
        frozenColWidths: state.frozenColWidths,
        colGroupings,
        colGroupingMax: model.colGroupingMax,
        colGroupingInversed: model.colGroupingInversed,
        leadingPx: cornerWidth,
        // Feed the selected-columns set so the
        // header can emit `part="column-header
        // header-selected"` on cells inside the active
        // range. Pure derivation from
        // {@code selection.activeRange}.
        selectedColumns: store.derived.selectedColumns(selection)
      };
    } else {
      // No headings and no outline: the strip goes altogether, border and
      // all - also when they are turned off while
      // the sheet is up, rather than keep the size it was last given.
      parts.columnHeader.hidden = true;
    }
    // Row-header strip. Same opt-in gate as the column
    // header, and the same reason for the grouping half of it.
    if (headerColumnWidth > 0 || model.rowGroupingMax > 0) {
      // Row groupings mirror the column ones; heights come from the same
      // point-to-pixel conversion the grid uses.
      const rowGroupings = store.derived.rowGroupings(model);
      parts.rowHeader.hidden = false;
      parts.rowHeader.style.width = `${headerColumnWidth + rowGutterPx}px`;
      parts.rowHeader.style.height = `${state.totalGridHeightPx}px`;
      parts.rowHeader.renderState = {
        viewport: state.viewport,
        rowY: state.rowY,
        rowHeights: state.rowHeights,
        headerWidth: headerColumnWidth + rowGutterPx,
        totalGridHeightPx: state.totalGridHeightPx,
        // Feed the selected-rows set so the
        // header can emit `part="row-header
        // header-selected"` on rows inside the active
        // range.
        selectedRows: store.derived.selectedRows(selection),
        rowGroupings,
        rowGroupingMax: model.rowGroupingMax,
        rowGroupingInversed: model.rowGroupingInversed,
        rowGroupingHeadings: headerColumnWidth > 0,
        leadingPx: cornerHeight,
        // Frozen rows stay on screen whatever the scroll position, so
        // their headers travel with them.
        verticalSplitPosition: state.verticalSplitPosition,
        frozenColumns: state.horizontalSplitPosition > 0,
        scrollLeft: state.scrollLeft,
        frozenRowY: state.frozenRowY,
        frozenRowHeights: state.frozenRowHeights
      };
    } else {
      parts.rowHeader.hidden = true;
    }
    // Header corner. Shown only when BOTH its dimensions are > 0, so it
    // fills its grid cell with matching width + height. With either at 0 -
    // the headings off and only one axis grouped - it is hidden: its border
    // would otherwise keep a 1px row or column of the grid, and every cell
    // would sit a pixel off.
    parts.headerCorner.hidden = cornerWidth === 0 || cornerHeight === 0;
    parts.headerCorner.style.width = `${cornerWidth}px`;
    parts.headerCorner.style.height = `${cornerHeight}px`;
    this.view.pagedViewParts = {
      corner: { width: cornerWidth, height: cornerHeight },
      frozen: {
        width: state.frozenColWidths.reduce((sum, w) => sum + w, 0),
        columns: state.horizontalSplitPosition,
        height: state.frozenRowHeights.reduce((sum, h) => sum + h, 0),
        rows: state.verticalSplitPosition
      }
    };
    this.insetPagedView();
    parts.headerCorner.renderState = {
      headerWidth: headerColumnWidth,
      headerHeight: headerRowHeight,
      colGroupingMax: model.colGroupingMax,
      rowGroupingMax: model.rowGroupingMax
    };
  }

  /**
   * Pure: build a {@code CellGridRenderState} from the store and the given
   * scroll / viewport, without writing anything.
   */
  build(opts: ScrollView, slots: CellSlots = this.cellSlots()): CellGridRenderState {
    const ppi = FALLBACK_PPI;
    const { store } = this;
    const model = store.model;
    const rowHeightForIndex = (row: number): number => drawnRowHeight(model, row, ppi);
    const colWidthForIndex = (col: number): number => drawnColumnWidth(model, col);

    const sizes = this.sheetSizes();
    const viewport = computeViewportBounds({
      scrollLeft: opts.scrollLeft,
      scrollTop: opts.scrollTop,
      scrollViewWidth: opts.viewportWidth,
      scrollViewHeight: opts.viewportHeight,
      horizontalSplitPosition: model.horizontalSplitPosition,
      verticalSplitPosition: model.verticalSplitPosition,
      columnBufferSize: model.columnBufferSize,
      rowBufferSize: model.rowBufferSize,
      maxColumns: model.cols,
      maxRows: model.rows,
      columns: sizes.columns,
      rows: sizes.rows
    });

    // Frozen columns 1..horizontalSplitPosition and rows
    // 1..verticalSplitPosition, 1-based and inclusive. Empty when nothing
    // is frozen.
    const frozen = store.derived.frozenPanes(model);

    // Build per-visible-index arrays for colX, rowY, widths, heights.
    //
    // These are the sheet's own coordinates: the distance from its left
    // edge, frozen columns included. The viewport reports how far into
    // the scrolling region the first visible column is, which is
    // measured from the split, so the frozen block's width goes back on
    // top. Without it the scrolling columns would be drawn from x 0,
    // underneath the frozen block that is held there - the first
    // columns after a freeze could not be seen at all, and a click on
    // one would land on a frozen cell.
    //
    // The freeze line has a pixel of its own between the frozen columns or
    // rows and the rest, the frozen pane's border, so the scrolling ones start one pixel past it. Drawn over their first pixel
    // instead, the first row after a freeze would come out a pixel short.
    const split = { cols: model.horizontalSplitPosition, rows: model.verticalSplitPosition };
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
      model.overlays,
      this.resources(),
      colWidthForIndex,
      rowHeightForIndex,
      ppi,
      split
    );

    const mergedRegionBoxes = computeMergedRegionBoxes(
      model.mergedRegions,
      split,
      { left: opts.scrollLeft, top: opts.scrollTop },
      colWidthForIndex,
      rowHeightForIndex
    );

    return {
      viewport,
      cellData: store.sheetData.cells,
      colX,
      rowY,
      colWidths,
      rowHeights,
      cellStyleToCSSStyle: model.cellStyleToCSSStyle,
      displayGridlines: model.displayGridlines,
      rowIndexToStyleIndex: model.rowIndexToStyleIndex,
      columnIndexToStyleIndex: model.columnIndexToStyleIndex,
      cellSlots: slots.slots,
      conditionalFormattingStyles: model.conditionalFormattingStyles,
      shiftedCellBorderStyles: model.shiftedCellBorderStyles,
      mergedRegions: model.mergedRegions,
      mergedRegionBoxes,
      selection: store.selection,
      verticalSplitPosition: model.verticalSplitPosition,
      horizontalSplitPosition: model.horizontalSplitPosition,
      frozenColX: frozen.colX,
      frozenColWidths: frozen.colWidths,
      frozenRowY: frozen.rowY,
      frozenRowHeights: frozen.rowHeights,
      totalGridWidthPx: totalWidthPx,
      totalGridHeightPx: totalHeightPx,
      headerExtent: this.headerExtent(),
      scrollLeft: opts.scrollLeft,
      scrollTop: opts.scrollTop,
      editor: store.editor,
      formulaReferences: store.derived.formulaReferences(store.draft, this.host.sheetName()),
      overlays: positionedOverlays,
      // Each at the bottom right corner of its cell.
      popupButtons: store.derived.popupButtons(this.host.popupButtons(), model),
      paintRect: store.paintRect(),
      touch: store.interaction.touch,
      commentText: model.cellComments,
      commentAuthors: model.cellCommentAuthors,
      visibleCommentKeys: model.visibleCellComments,
      editingCommentKey: store.interaction.comment,
      invalidFormulaMessage: model.invalidFormulaErrorMessage,
      commentCells: store.derived.commentCells(model),
      invalidFormulaCells: model.invalidFormulaCells,
      hyperlinkCells: store.derived.hyperlinkCells(model)
    };
  }

  /** The cells the grid draws, its drawing buffer included; null before anything is measured. */
  drawnRange(): LoadRange | null {
    const viewport = this.parts.cellGrid.renderState.viewport;
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
   * Which server-side element each cell shows, from the three inputs the
   * server pushes separately.
   *
   * Recomputed rather than cached: it depends on the selection, which
   * changes on every click, and the maps it reads are small. A draw works
   * it out once and uses it twice.
   */
  cellSlotAssignments(): Record<string, CellSlotAssignment> {
    return this.cellSlots().assignments;
  }

  /**
   * Adjust {@code scrollContainer.scrollLeft / scrollTop} so the
   * given cell (either the new active cell or the extend cell
   * after shift+arrow) is in full view - see {@link scrollToShow}. Sets scrollLeft/Top in-place; the
   * existing `scroll` listener triggers a fresh recompute.
   */
  scrollCellIntoView(target: Cell): void {
    const { store } = this;
    const { model } = store;
    if (!store.hasCells()) {
      return;
    }
    const sc = this.parts.scrollContainer;
    const { cellGrid } = this.parts;
    // The cells' part of the view: the header strips stick to its top and
    // left edges, and the cell grid starts where they end.
    const viewWidth = sc.clientWidth - cellGrid.offsetLeft;
    const viewHeight = sc.clientHeight - cellGrid.offsetTop;
    const left = scrollToShow(target.col, model.horizontalSplitPosition, model.cols, sc.scrollLeft, viewWidth, (c) =>
      store.colWidthPx(c)
    );
    const top = scrollToShow(target.row, model.verticalSplitPosition, model.rows, sc.scrollTop, viewHeight, (r) =>
      store.rowHeightPx(r)
    );
    if (left !== sc.scrollLeft) {
      sc.scrollLeft = left;
    }
    if (top !== sc.scrollTop) {
      sc.scrollTop = top;
    }
  }

  /**
   * The scroll container scrolled; whether the user did it. The sheet
   * keeping a row in place is not the user scrolling; when the user has
   * moved on, there is no row to keep where it was.
   */
  scrolled(): boolean {
    const anchored = this.view.anchoredScrollTop;
    this.view.anchoredScrollTop = null;
    const byUser = this.parts.scrollContainer.scrollTop !== anchored;
    if (byUser) {
      this.view.rowAnchor = null;
    }
    return byUser;
  }

  /**
   * Row heights are about to change. Rows above the screen grow and shrink
   * as their components are loaded and let go of; the first row with its
   * top on screen stays where it is, and so does what is on screen. Moved,
   * the rows on screen would be others than those loaded, and loading those
   * would move them on again, one request after another.
   */
  holdRowInPlace(): void {
    this.view.rowAnchor ??= this.firstRowWithTopOnScreen();
  }

  /** The rows are another sheet's now: there is no row to keep in place. */
  forgetRowAnchor(): void {
    this.view.rowAnchor = null;
  }

  /**
   * A batch of the server's changes is in, showing a sheet. A sheet shown
   * again goes back to where it was scrolled. The sheet shown can also get
   * another index without changing, when a sheet before it is deleted,
   * hidden or shown again: it stays where it is.
   *
   * @param name the sheet's name, which is unique in a workbook
   * @param scroll where the sheet was last scrolled to
   * @param switched whether the batch changed which sheet is shown
   */
  sheetShown(name: string | null, scroll: { top: number; left: number }, switched: boolean): void {
    if (switched && name !== this.view.shownSheetName) {
      this.view.pendingSheetScroll = scroll;
    }
    this.view.shownSheetName = name;
    this.parts.scrollContainer.setAttribute('aria-label', name || 'Spreadsheet');
  }

  /**
   * Leaves the headers and frozen panes out of the view the browser pages,
   * so Page Up and Page Down move the cells by a page of cells. Paging the
   * whole scroll container, they would move them a header further.
   */
  insetPagedView(): void {
    const { corner, frozen } = this.view.pagedViewParts;
    const inset = pagedViewInset(corner, frozen, !this.parts.sheetTabs.hidden);
    this.parts.scrollContainer.style.scrollPaddingTop = `${inset.top}px`;
    this.parts.scrollContainer.style.scrollPaddingLeft = `${inset.left}px`;
  }

  /**
   * The scrolling pane on screen: the cells under the column letters and
   * right of the row numbers, past the frozen rows and columns. Popups keep
   * to this pane, whose bottom is the sheet's own.
   */
  scrollingPaneClientRect(): { left: number; top: number; bottom: number } {
    const { scrolling } = this.cellPanes();
    return {
      left: scrolling.left,
      top: scrolling.top,
      bottom: this.parts.scrollContainer.getBoundingClientRect().bottom
    };
  }

  /**
   * Where the cells are on screen: all of them in view, the frozen ones
   * included, and the pane of those that scroll, past the frozen rows and
   * columns and the freeze line's own pixel. The scroll bars are not cells.
   */
  cellPanes(): CellPanes {
    const { scrollContainer, cellGrid } = this.parts;
    const { model } = this.store;
    const box = scrollContainer.getBoundingClientRect();
    const frozen = cellGrid.frozenExtent();
    const viewLeft = box.left + scrollContainer.clientLeft;
    const viewTop = box.top + scrollContainer.clientTop;
    const cells = {
      left: viewLeft + cellGrid.offsetLeft,
      top: viewTop + cellGrid.offsetTop,
      right: viewLeft + scrollContainer.clientWidth,
      bottom: viewTop + scrollContainer.clientHeight
    };
    return {
      cells,
      scrolling: {
        ...cells,
        left: cells.left + frozen.width + freezeGap(model.horizontalSplitPosition),
        top: cells.top + frozen.height + freezeGap(model.verticalSplitPosition)
      }
    };
  }

  /**
   * The cell at a point on the cells, as they are drawn and scrolled now. A
   * point off them is on the nearest column or row of the sheet.
   */
  cellAt(x: number, y: number): { col: number; row: number } {
    const { model } = this.store;
    const { scrollContainer } = this.parts;
    const { columns, rows } = this.sheetSizes();
    const { cells } = this.cellPanes();
    return {
      col: indexAtPoint(columns, model.horizontalSplitPosition, model.cols, x - cells.left, scrollContainer.scrollLeft),
      row: indexAtPoint(rows, model.verticalSplitPosition, model.rows, y - cells.top, scrollContainer.scrollTop)
    };
  }

  /** The resources' URLs are read from this element's `resource-<id>` attributes; see {@link resources}. */
  useResourcesOf(host: HTMLElement): void {
    this.resourceHost = host;
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
  resources(): Record<string, string> {
    const host = this.resourceHost;
    if (host === null) {
      return {};
    }
    const resolved: Record<string, string> = {};
    for (const key of this.store.model.resources) {
      const url = host.getAttribute(`resource-${key}`);
      if (url) {
        resolved[key] = url;
      }
    }
    return resolved;
  }

  /** Where the cells are scrolled to now, and how much of them shows. */
  private currentView(): ScrollView {
    const { scrollContainer } = this.parts;
    return {
      scrollLeft: scrollContainer.scrollLeft,
      scrollTop: scrollContainer.scrollTop,
      viewportWidth: scrollContainer.clientWidth,
      viewportHeight: scrollContainer.clientHeight
    };
  }

  /**
   * Height of the column-header strip in pixels, its bottom border
   * included. {@code 0} when the sheet hides its headings, which
   * collapses the strip.
   */
  private headerRowHeight(): number {
    return this.store.model.displayRowColHeadings ? DEFAULT_HEADER_ROW_HEIGHT : 0;
  }

  /**
   * Width of the row-header strip in pixels, its right border included.
   * {@code 0} when the sheet hides its headings.
   */
  private headerColumnWidth(): number {
    return this.store.model.displayRowColHeadings ? DEFAULT_HEADER_COLUMN_WIDTH : 0;
  }

  /**
   * How far the row numbers and their outline gutter reach across the sheet,
   * and the column letters and theirs down it: the corner where the two
   * meet, and where the cells start.
   */
  private headerExtent(): { width: number; height: number } {
    const { model } = this.store;
    const headerRowHeight = this.headerRowHeight();
    const headerColumnWidth = this.headerColumnWidth();
    const rowGutterPx = rowGroupingGutterPx(model.rowGroupingMax, headerColumnWidth > 0);
    const colGutterPx = columnGroupingGutterPx(model.colGroupingMax, headerRowHeight > 0);
    return {
      width: headerColumnWidth > 0 ? headerColumnWidth + rowGutterPx : rowGutterPx,
      height: headerRowHeight > 0 ? headerRowHeight + colGutterPx : colGutterPx
    };
  }

  /** The sheet's row and column sizes as drawn; see {@link Derived.sheetSizes}. */
  private sheetSizes(): SheetSizes {
    return this.store.derived.sheetSizes(this.store.model);
  }

  /** Which component or editor each cell shows, and the slot the grid draws for it. */
  private cellSlots(): CellSlots {
    const active = this.store.selection.activeCell;
    return this.store.derived.cellSlots(this.store.model, active === null ? null : toKey(active.col, active.row));
  }

  /**
   * Draws what is written into the input it is not written in, and into the
   * one it is written in where pointing at a cell changed it. The cell editor
   * and the formula field report what is typed into them and where the caret
   * goes, which the store keeps; neither is written to anywhere else.
   */
  private drawWriting(): void {
    const { cellGrid, formulaBar } = this.parts;
    const edit = this.store.interaction.edit;
    if (edit.kind === 'editing') {
      const shown = cellGrid.editorValue();
      if (shown !== null && shown !== edit.writing.text) {
        // The caret only where the editor has the keyboard: written in the
        // formula field, the text is shown in the editor and nothing moves
        // there.
        cellGrid.setEditorValue(edit.writing.text, edit.writing.source === 'cell' ? edit.writing.caret : undefined);
      }
    }
    const writing = this.store.writing;
    if (writing !== null && writing.source === 'field' && formulaBar.formulaFieldHasFocus()) {
      const field = formulaBar.formulaFieldState();
      if (field !== null && field.value !== writing.text) {
        formulaBar.setFormulaFieldValue(writing.text, writing.caret);
      }
    }
  }

  /** The first scrolling row whose top is on screen, and how far below the top of the screen. */
  private firstRowWithTopOnScreen(): { row: number; below: number } {
    const top = this.parts.scrollContainer.scrollTop;
    const rows = this.sheetSizes().rows;
    const origin = this.store.model.verticalSplitPosition + 1;
    const row = rows.reach(origin, top);
    return { row, below: rows.start(row) - rows.start(origin) - top };
  }

  /**
   * Puts the row {@link holdRowInPlace} keeps back where it was on screen,
   * now that the rows above it have their new heights.
   */
  private keepRowAnchored(): void {
    const anchor = this.view.rowAnchor;
    if (anchor === null) {
      return;
    }
    this.view.rowAnchor = null;
    const { scrollContainer } = this.parts;
    const rows = this.sheetSizes().rows;
    const top = rows.start(anchor.row) - rows.start(this.store.model.verticalSplitPosition + 1) - anchor.below;
    if (top === scrollContainer.scrollTop) {
      return;
    }
    // Sized for the new heights first: against the old ones the browser
    // would cut the offset short.
    this.apply(this.currentView());
    scrollContainer.scrollTop = top;
    this.view.anchoredScrollTop = scrollContainer.scrollTop;
  }
}
