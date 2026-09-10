/*
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
   * Conditional-formatting rule bodies, keyed by rule index. A cell that
   * a rule matches carries {@code "cf<index>"} in its {@code cellStyle}.
   * Emitted after the {@code cs<N>} rules so a matching rule wins over the
   * cell's own style, and in ascending key order so a later rule wins over
   * an earlier one — the order Excel resolves them in.
   */
  /**
   * Server-side components rendered inside a cell, as cell key → Flow node
   * id. The cell shows the component instead of its own text.
   *
   * Inverted from the server's node-id-keyed map by the connector, because
   * rendering asks the question the other way round: given this cell, is
   * there a component in it?
   */
  cellComponents: Readonly<Record<string, string>>;

  conditionalFormattingStyles: Readonly<Record<number, string>>;

  /**
   * Complete CSS rules for cells whose borders come from a neighbour —
   * Excel draws a border once and both cells show it. Each entry already
   * carries its own selector, written against {@code .col<N>.row<N>}.
   * Emitted last: a shifted border overrides the cell's own edge.
   */
  shiftedCellBorderStyles: ReadonlyArray<string>;

  /**
   * Merged regions that overlap the visible viewport. Slice 137 will
   * render these as larger cells overlaying the underlying single-
   * cell grid; slice 132's minimal render ignores them.
   */
  mergedRegions: ReadonlyArray<MergedRegion>;

  /**
   * Current selection state. Slice 136 will render selection
   * overlays; slice 132's minimal render ignores it.
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
   * Current horizontal scroll offset in pixels — copied through
   * from the {@code applyRenderState} call so the cell-grid can
   * keep frozen quadrants visually pinned. The frozen-row and
   * frozen-corner quadrant wrappers apply
   * {@code transform:translateY(scrollTop)} to counter the scroll
   * container's vertical shift; the frozen-column and corner
   * wrappers apply {@code transform:translateX(scrollLeft)} to
   * counter the horizontal shift. Slice 141.
   */
  scrollLeft: number;

  /** Symmetric vertical variant of {@link scrollLeft}. */
  scrollTop: number;

  /**
   * Cell-editor state (slice 152). When non-null, the cell-grid
   * renders an editable {@code <input>} positioned over the
   * editor's target cell so the user can type a new value. When
   * null, no editor is rendered. The connector sets this on
   * double-click and clears it on commit / cancel.
   */
  editor: CellEditorState | null;

  /**
   * Positioned sheet overlays (images, charts, components)
   * computed by the connector from {@code setOverlays} +
   * {@code setResources}. The cell-grid renders an absolutely-
   * positioned element per overlay. Empty when no overlays
   * are active. Slice 160.
   */
  overlays: ReadonlyArray<PositionedOverlay>;

  /**
   * Positioned popup-button anchors (slice 161). The cell-grid
   * renders a small {@code <div class="popup-button">} in the
   * bottom-right corner of each anchor cell. Clicking dispatches
   * {@code popup-button-click}; the connector translates to
   * {@code popupButtonClick} for the server.
   */
  popupButtons: ReadonlyArray<PositionedPopupButton>;

  /**
   * Cell keys (in {@link toKey} format — `"col<C> row<R>"`)
   * whose cell carries a comment. The cell-grid renders a
   * small yellow corner-triangle indicator on each. Empty
   * set when no cell has a comment. Slice 165.
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
   * Empty set when no cells are hyperlinks. Slice 165.
   */
  hyperlinkCells: ReadonlySet<string>;
}

/**
 * A positioned popup-button anchor with the cell's pixel
 * coordinates pre-computed by the connector. Slice 161.
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
  /** True when the popup is currently shown — affects the indicator style. */
  active: boolean;
}

/**
 * State for the in-place cell editor (slice 152). The cell-grid
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
}

/**
 * A sheet overlay (image, chart, embedded component) with its
 * pixel position pre-computed by the connector. The cell-grid
 * just absolutely-positions each overlay at the supplied
 * coordinates — it doesn't know about anchor cells or offsets.
 * Slice 160.
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
   * For COMPONENT: undefined (rendered as a placeholder div
   * pending slice 161+ slot integration).
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
    cellComponents: {},
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
