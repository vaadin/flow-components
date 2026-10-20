/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { ViewportBounds } from '../algorithms/ViewportMath.js';
import type { SelectionState } from '../state/SelectionState.js';
import type { CellData, MergedRegion } from '../types/wire.js';
import type { CellRect } from '../algorithms/SelectionPaint.js';

/**
 * Input state for the {@link SpreadsheetCellGrid} element. The
 * Spreadsheet connector builds a fresh instance on every render
 * cycle (scroll, selection change, data update) and assigns it to
 * the element's {@code renderState} property.
 *
 * Naming + structure follow the {@code renderState} convention used
 * by other Vaadin Flow components (see vaadin-grid-flow's
 * {@code gridConnector.ts}): single object input rather than dozens
 * of individual property setters.
 *
 * All coordinates are 1-based (the codebase-wide convention). Pixel
 * positions are 0-based and measured from the cell-grid's top-left
 * corner.
 */
export interface CellGridRenderState {
  /**
   * Which rows + columns are currently visible (output of
   * {@code computeViewportBounds}). The cell-grid iterates
   * {@code firstColumnIndex..lastColumnIndex} × {@code firstRowIndex
   * ..lastRowIndex} and renders one cell per coordinate.
   */
  viewport: ViewportBounds;

  /**
   * Cell content keyed by {@code toKey(col, row)} (the "col1 row1"
   * wire format from {@code CellKeys.toKey}). Coordinates without
   * an entry render as empty cells.
   */
  cellData: ReadonlyMap<string, CellData>;

  /**
   * Pixel x-offsets for visible columns, indexed by
   * {@code col - viewport.firstColumnIndex}. Pixel widths are the
   * differences between consecutive offsets, except for the last
   * column where the width comes from {@code colWidths}.
   */
  colX: ReadonlyArray<number>;

  /**
   * Pixel y-offsets for visible rows, indexed by
   * {@code row - viewport.firstRowIndex}. Pixel heights are the
   * differences between consecutive offsets, except for the last
   * row where the height comes from {@code rowHeights}.
   */
  rowY: ReadonlyArray<number>;

  /** Pixel width of each visible column, same indexing as {@code colX}. */
  colWidths: ReadonlyArray<number>;

  /** Pixel height of each visible row, same indexing as {@code rowY}. */
  rowHeights: ReadonlyArray<number>;

  /**
   * Cells whose value has been measured and really is too wide for its
   * column, by key. Not wire state: the server flags candidates with
   * {@code needsMeasure}, and the cell-grid measures those few before
   * replacing their value with hashes.
   */
  tooNarrowCells?: ReadonlySet<string>;

  /**
   * Cells the server gave a run-on width whose value, measured, fits its
   * column after all, by key. Not wire state: the server guesses by
   * character count, and the text is measured here, so these
   * are drawn as ordinary cells - a right-aligned number stays right.
   */
  fittingSpills?: ReadonlySet<string>;

  /**
   * Style-index → CSS-rule-body lookup. Each rendered cell carries
   * its style class (e.g. {@code "cs0"}) on its element; the
   * cell-grid emits a {@code <style>} block in its shadow DOM with
   * rules of the form {@code .cell.cs<N> { <body> }}. The body is
   * everything between the braces — declarations like
   * {@code "font-weight:bold;color:red;"}.
   *
   * Keys are integer style indices (Excel's per-cell-style table is
   * indexed). The {@code cellStyle} field on {@link CellData} stores
   * the corresponding {@code "cs<N>"} string.
   */
  cellStyleToCSSStyle: Readonly<Record<number, string>>;

  /**
   * Whether the sheet shows its gridlines. Turned off, the cells lose the
   * grid's borders but keep any the workbook gives them. Absent means shown.
   */
  displayGridlines?: boolean;

  /**
   * Style index a whole row is formatted with, by 1-based row. A cell of
   * that row that has no style of its own takes the row's, as it does in
   * Excel - a row filled yellow is yellow in its empty cells too.
   */
  rowIndexToStyleIndex?: Readonly<Record<number, number>>;

  /** Same as {@link rowIndexToStyleIndex}, by 1-based column. */
  columnIndexToStyleIndex?: Readonly<Record<number, number>>;

  /**
   * Cells that show a server-side component instead of their own text, as
   * cell key → the name of the slot that projects it.
   *
   * Covers both components placed with {@code Spreadsheet.setComponent} and
   * custom cell editors; the cell only needs to know which slot to render,
   * and the connector decides which of the two wins a cell that has both.
   */
  cellSlots: Readonly<Record<string, string>>;

  /**
   * Conditional-formatting rule bodies, keyed by rule index. A cell that
   * a rule matches carries {@code "cf<index>"} in its {@code cellStyle}.
   * Emitted after the {@code cs<N>} rules so a matching rule wins over the
   * cell's own style, and in ascending key order so a later rule wins over
   * an earlier one — the order Excel resolves them in.
   */
  conditionalFormattingStyles: Readonly<Record<number, string>>;

  /**
   * Complete CSS rules for cells whose borders come from a neighbour —
   * Excel draws a border once and both cells show it. Each entry already
   * carries its own selector, written against {@code .col<N>.row<N>}.
   * Emitted last: a shifted border overrides the cell's own edge.
   */
  shiftedCellBorderStyles: ReadonlyArray<string>;

  /** The sheet's merged regions, each drawn as one cell over those it spans. */
  mergedRegions: ReadonlyArray<MergedRegion>;

  /**
   * Where each of {@link mergedRegions} is drawn, by the same index. Absent,
   * a region is drawn only as far as its own quadrant shows it, and only
   * while its top-left cell is on screen.
   */
  mergedRegionBoxes?: ReadonlyArray<MergedRegionBox>;

  /**
   * Current selection state, which the grid draws.
   */
  selection: SelectionState;

  /**
   * Number of frozen rows at the top of the sheet (1-based count;
   * 0 = no row freeze). Rows {@code 1..verticalSplitPosition} render
   * in the top-row + corner quadrants and don't scroll vertically;
   * the main viewport's {@code firstRowIndex} starts above this
   * count.
   */
  verticalSplitPosition: number;

  /**
   * Symmetric column variant of {@link verticalSplitPosition}.
   * {@code 0} = no column freeze.
   */
  horizontalSplitPosition: number;

  /**
   * Pixel x-offsets for the frozen columns (cols
   * {@code 1..horizontalSplitPosition}). Indexed by
   * {@code col - 1} (col 1 → index 0). Empty when no column
   * freeze is active.
   */
  frozenColX: ReadonlyArray<number>;

  /** Pixel widths for the frozen columns, same indexing as {@link frozenColX}. */
  frozenColWidths: ReadonlyArray<number>;

  /** Pixel y-offsets for the frozen rows ({@code 1..verticalSplitPosition}). */
  frozenRowY: ReadonlyArray<number>;

  /** Pixel heights for the frozen rows, same indexing as {@link frozenRowY}. */
  frozenRowHeights: ReadonlyArray<number>;

  /**
   * Total pixel width of the entire (virtual) grid — the sum of all
   * non-hidden column widths. Used by the connector to size the
   * scroll surface so the host's horizontal scrollbar tracks reflect
   * actual scroll extent.
   */
  totalGridWidthPx: number;

  /** Symmetric vertical variant of {@link totalGridWidthPx}. */
  totalGridHeightPx: number;

  /**
   * How far the row numbers reach across the view and the column letters
   * down it, their outline gutters included: where the cells start. The
   * frozen panes are held that far in from the view's edges as the sheet
   * scrolls. Absent, there are no headers.
   */
  headerExtent?: { width: number; height: number };

  /**
   * Current horizontal scroll offset in pixels, as drawn. What is drawn at
   * grid level over a frozen cell - the editor, a note - is placed by it, and
   * so is how much of a merged region across the freeze still shows. The
   * frozen panes themselves are held in place by the browser.
   */
  scrollLeft: number;

  /** Symmetric vertical variant of {@link scrollLeft}. */
  scrollTop: number;

  /**
   * Cell-editor state. When non-null, the cell-grid
   * renders an editable {@code <input>} positioned over the
   * editor's target cell so the user can type a new value. When
   * null, no editor is rendered. The connector sets this on
   * double-click and clears it on commit / cancel.
   */
  editor: CellEditorState | null;

  /**
   * The references of the formula being written, tinted on the sheet in
   * their colours; the one the caret is in is
   * outlined. Absent or empty while no formula is being written.
   */
  formulaReferences?: ReadonlyArray<{ range: CellRect; color: string; atCaret: boolean }>;

  /**
   * Positioned sheet overlays (images, charts, components)
   * computed by the connector from {@code setOverlays} +
   * {@code setResources}. The cell-grid renders an absolutely-
   * positioned element per overlay. Empty when no overlays
   * are active.
   */
  overlays: ReadonlyArray<PositionedOverlay>;

  /**
   * Positioned popup-button anchors. The cell-grid
   * renders a small {@code <div class="popup-button">} in the
   * bottom-right corner of each anchor cell. Clicking dispatches
   * {@code popup-button-click}; the connector translates to
   * {@code popupButtonClick} for the server.
   */
  popupButtons: ReadonlyArray<PositionedPopupButton>;

  /**
   * The strip the fill handle is painting over, or null when no paint drag
   * is in progress. Rendered as a second, dashed selection outline.
   */
  paintRect?: CellRect | null;

  /**
   * Whether the sheet is used by touch: `select` draws the selection's
   * corner handle for a finger, and `fill` a fill handle on each side in its
   * place; `off`, or left out, draws it for a mouse.
   */
  touch?: 'off' | 'select' | 'fill';

  /** Comment text by cell key, for every cell that carries one. */
  commentText?: Readonly<Record<string, string>>;

  /** Comment author by cell key, where the workbook records one. */
  commentAuthors?: Readonly<Record<string, string>>;

  /**
   * Cells whose comment is shown without hovering — Excel's "show comment".
   */
  visibleCommentKeys?: ReadonlyArray<string>;
  /**
   * The cell whose comment is being written, as a cell key, or null when
   * none is. Its overlay shows a textarea instead of the note's text.
   */
  editingCommentKey?: string | null;

  /** Message shown when hovering a cell whose formula the server rejected. */
  invalidFormulaMessage?: string | null;

  /**
   * Cell keys (in {@link toKey} format — `"col<C> row<R>"`)
   * whose cell carries a comment. The cell-grid renders a
   * small yellow corner-triangle indicator on each. Empty
   * set when no cell has a comment.
   */
  commentCells: ReadonlySet<string>;
  /**
   * Cells whose formula the server could not evaluate, keyed by
   * {@code toKey(col, row)}. Each gets the documented
   * {@code invalid-triangle} indicator.
   */
  invalidFormulaCells: ReadonlySet<string>;

  /**
   * Cell keys whose cell is a clickable hyperlink. Click on
   * such a cell dispatches {@code linkCellClicked} INSTEAD OF
   * (well, in addition to) the normal {@code cellSelected}.
   * Empty set when no cells are hyperlinks.
   */
  hyperlinkCells: ReadonlySet<string>;
}

/**
 * A positioned popup-button anchor with the cell's pixel
 * coordinates pre-computed by the connector.
 */
export interface PositionedPopupButton {
  /** 1-based column of the anchor cell. */
  col: number;
  /** 1-based row of the anchor cell. */
  row: number;
  /** Pixel left of the anchor cell's bottom-right corner. */
  leftPx: number;
  /** Pixel top of the anchor cell's bottom-right corner. */
  topPx: number;
  /**
   * Height of the anchor cell's row: the Lumo theme draws the button as
   * tall as its cell.
   */
  cellHeightPx?: number;
  /** True when the popup is currently shown — affects the indicator style. */
  active: boolean;
}

/**
 * State for the in-place cell editor. The cell-grid
 * uses col/row to look up the cell's pixel position from its
 * existing {@code colX} / {@code rowY} / {@code colWidths} /
 * {@code rowHeights} arrays — no separate position computation.
 */
export interface CellEditorState {
  /** 1-based column of the cell being edited. */
  col: number;
  /** 1-based row of the cell being edited. */
  row: number;
  /**
   * The initial value placed in the input when the editor
   * opens. The user's typing happens in the input's local
   * state — the connector reads it back from the input when
   * the user commits.
   */
  initialValue: string;

  /**
   * Excel's two editing modes, which differ only in what the arrow keys
   * do.
   *
   * `enter` — the user started typing over the cell, so an arrow key
   * commits the value and moves the selection.
   * `edit` — the user opened the existing value with F2 or a
   * double-click, so an arrow key moves the caret through the text.
   *
   * Absent means `edit`, the more conservative of the two.
   */
  mode?: 'enter' | 'edit';
}

/**
 * A merged region's box in the sheet's own pixel coordinates - the distance
 * from its top-left corner, frozen rows and columns included - which every
 * quadrant of the cell grid places its cells by.
 */
export interface MergedRegionBox {
  left: number;
  top: number;
  width: number;
  height: number;
}

/**
 * A sheet overlay (image, chart, embedded component) with its
 * pixel position pre-computed by the connector. The cell-grid
 * just absolutely-positions each overlay at the supplied
 * coordinates — it doesn't know about anchor cells or offsets.
 */
export interface PositionedOverlay {
  /** Identifier from the server's `setOverlays` map. */
  id: string;
  /** 1-based column of the cell the overlay is anchored to. */
  col: number;
  /** 1-based row of the cell the overlay is anchored to. */
  row: number;
  /** Overlay kind — image or embedded component. */
  type: 'IMAGE' | 'COMPONENT';
  /**
   * For IMAGE: the image URL resolved from {@code setResources}.
   * For COMPONENT: undefined; the component is slotted in.
   */
  src: string | undefined;
  /** Pixel left of the overlay, relative to the cell-grid's origin. */
  leftPx: number;
  /** Pixel top of the overlay. */
  topPx: number;
  /** Pixel width. */
  widthPx: number;
  /** Pixel height. */
  heightPx: number;
}

/**
 * Helper: build an empty {@link CellGridRenderState} for tests +
 * for the connector's initial render before any state has been
 * pushed.
 */
export function emptyCellGridRenderState(): CellGridRenderState {
  return {
    viewport: {
      firstColumnIndex: 1,
      firstColumnPosition: 0,
      lastColumnIndex: 0,
      lastColumnPosition: 0,
      firstRowIndex: 1,
      firstRowPosition: 0,
      lastRowIndex: 0,
      lastRowPosition: 0
    },
    cellData: new Map(),
    colX: [],
    rowY: [],
    colWidths: [],
    rowHeights: [],
    cellStyleToCSSStyle: {},
    cellSlots: {},
    conditionalFormattingStyles: {},
    shiftedCellBorderStyles: [],
    mergedRegions: [],
    verticalSplitPosition: 0,
    horizontalSplitPosition: 0,
    frozenColX: [],
    frozenColWidths: [],
    frozenRowY: [],
    frozenRowHeights: [],
    totalGridWidthPx: 0,
    totalGridHeightPx: 0,
    scrollLeft: 0,
    scrollTop: 0,
    editor: null,
    overlays: [],
    popupButtons: [],
    paintRect: null,
    touch: 'off',
    commentText: {},
    commentAuthors: {},
    visibleCommentKeys: [],
    editingCommentKey: null,
    invalidFormulaMessage: null,
    commentCells: new Set(),
    invalidFormulaCells: new Set(),
    hyperlinkCells: new Set(),
    selection: {
      activeCell: { col: 1, row: 1 },
      activeRange: { c1: 1, c2: 1, r1: 1, r2: 1 },
      auxRanges: []
    }
  };
}
