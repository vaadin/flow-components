/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { AxisSizes } from '../algorithms/AxisSizes.js';
import { formulaReferences, referenceAt } from '../algorithms/FormulaPointing.js';
import { Memo } from '../algorithms/Memo.js';
import { computePositionedColGroupings, computePositionedPopupButtons } from '../algorithms/Positioning.js';
import {
  computeTotalGridSize,
  convertPointsToPixel,
  FALLBACK_PPI,
  getDefaultRowHeightPx
} from '../algorithms/ViewportMath.js';
import { computeCellSlots, type CellSlotAssignment } from '../dom/VirtualChildren.js';
import type { PositionedGrouping } from '../elements/SpreadsheetColumnHeader.js';
import type { FormulaBarRenderState } from '../elements/SpreadsheetFormulaBar.js';
import type { PositionedPopupButton } from '../elements/types.js';
import type { PopupButtonState } from '../types/wire.js';
import { type FormulaBarInputs, formulaBarRenderState } from './FormulaBarState.js';
import { type CellRange, selectedColumnSet, selectedRowSet, type SelectionState } from './SelectionState.js';
import type { SheetModel } from './SheetModel.js';

/**
 * A sheet's row and column sizes as they are drawn, in pixels, with what
 * follows from them alone.
 */
export interface SheetSizes {
  rows: AxisSizes;
  columns: AxisSizes;
  totalWidthPx: number;
  totalHeightPx: number;
  /** The heights of the rows the server gave one, hidden or not. */
  rowHeightsPx: number[];
  /** The height of the rows past those. */
  defaultRowHeightPx: number;
}

/** The frozen columns' and rows' places and sizes, from the sheet's top left corner. */
export interface FrozenPanes {
  colX: number[];
  colWidths: number[];
  width: number;
  rowY: number[];
  rowHeights: number[];
  height: number;
}

/** Which element each cell shows, and the slot the cell grid draws for it. */
export interface CellSlots {
  assignments: Record<string, CellSlotAssignment>;
  slots: Record<string, string>;
}

/** A reference of the formula being written, as the cells show it. */
export interface DrawnReference {
  range: CellRange;
  color: string;
  atCaret: boolean;
}

/** The width a column is drawn with: none when hidden, the default past the last one the server sized. */
export function drawnColumnWidth(model: SheetModel, col: number): number {
  if (model.hiddenColumnIndexes.has(col)) {
    return 0;
  }
  return col > model.colW.length ? model.defColW : model.colW[col - 1];
}

/**
 * The height a row is drawn with, in pixels: none when hidden, the default
 * past the last one the server sized. POI measures rows in points, so this
 * is where they are converted.
 */
export function drawnRowHeight(model: SheetModel, row: number, ppi: number = FALLBACK_PPI): number {
  if (model.hiddenRowIndexes.has(row)) {
    return 0;
  }
  return row > model.rowH.length
    ? getDefaultRowHeightPx(model.defRowH, ppi)
    : convertPointsToPixel(model.rowH[row - 1], ppi);
}

/**
 * What the sheet draws that is worked out from the model and the selection
 * alone, kept until what it is worked out from changes. A scroll draws the
 * sheet every frame and changes none of them: worked out each frame, a whole
 * column selected on a sheet of a million rows made a set of a million rows
 * each frame.
 *
 * Each value names all it is worked out from: one left out, the value would
 * not change with it.
 */
export class Derived {
  private readonly sizes = new Memo<SheetSizes>();
  private readonly frozen = new Memo<FrozenPanes>();
  private readonly slots = new Memo<CellSlots>();
  private readonly comments = new Memo<ReadonlySet<string>>();
  private readonly links = new Memo<ReadonlySet<string>>();
  private readonly columnsSelected = new Memo<ReadonlySet<number>>();
  private readonly rowsSelected = new Memo<ReadonlySet<number>>();
  private readonly columnOutline = new Memo<PositionedGrouping[]>();
  private readonly rowOutline = new Memo<PositionedGrouping[]>();
  private readonly references = new Memo<DrawnReference[]>();
  private readonly buttons = new Memo<PositionedPopupButton[]>();
  private readonly bar = new Memo<FormulaBarRenderState>();

  /**
   * The sheet's row and column sizes as drawn. A sheet can have a million
   * rows: adding them all up on every scroll step cost more than the rest of
   * the draw. Rows past the ones the server gave a height to are the default
   * height, hidden or not.
   */
  sheetSizes(model: SheetModel): SheetSizes {
    return this.sizes.get(
      [
        model.rowH,
        model.colW,
        model.hiddenRowIndexes,
        model.hiddenColumnIndexes,
        model.defRowH,
        model.defColW,
        model.rows,
        model.cols
      ],
      () => computeSheetSizes(model)
    );
  }

  /** Where the frozen columns and rows are drawn, and how far they reach. */
  frozenPanes(model: SheetModel): FrozenPanes {
    return this.frozen.get(
      [
        model.horizontalSplitPosition,
        model.verticalSplitPosition,
        model.rowH,
        model.colW,
        model.hiddenRowIndexes,
        model.hiddenColumnIndexes,
        model.defRowH,
        model.defColW
      ],
      () => computeFrozenPanes(model)
    );
  }

  /** Which component or editor each cell shows; see {@link computeCellSlots}. */
  cellSlots(model: SheetModel, activeCellKey: string | null): CellSlots {
    return this.slots.get(
      [model.componentIDtoCellKeysMap, model.cellKeysToEditorIdMap, model.showCustomEditorOnFocus, activeCellKey],
      () => {
        const assignments = computeCellSlots({
          componentIdToCellKey: model.componentIDtoCellKeysMap,
          cellKeyToEditorId: model.cellKeysToEditorIdMap,
          showCustomEditorOnFocus: model.showCustomEditorOnFocus,
          activeCellKey
        });
        const slots = Object.fromEntries(Object.entries(assignments).map(([cellKey, a]) => [cellKey, a.slot]));
        return { assignments, slots };
      }
    );
  }

  /** The cells with a note, by their keys. */
  commentCells(model: SheetModel): ReadonlySet<string> {
    return this.comments.get([model.cellComments], () => new Set(Object.keys(model.cellComments)));
  }

  /** The cells with a link, by their keys. */
  hyperlinkCells(model: SheetModel): ReadonlySet<string> {
    return this.links.get([model.hyperlinksTooltips], () => new Set(Object.keys(model.hyperlinksTooltips)));
  }

  /** The columns any range of the selection takes in. */
  selectedColumns(selection: SelectionState): ReadonlySet<number> {
    return this.columnsSelected.get([selection], () => selectedColumnSet(selection));
  }

  /** The rows any range of the selection takes in. */
  selectedRows(selection: SelectionState): ReadonlySet<number> {
    return this.rowsSelected.get([selection], () => selectedRowSet(selection));
  }

  /** The column groups' brackets and buttons, in the column header's pixels. */
  columnGroupings(model: SheetModel): PositionedGrouping[] {
    const otherOutline = model.rowGroupingMax > 0;
    return this.columnOutline.get(
      [model.colGroupingData, model.colW, model.colGroupingInversed, model.defColW, otherOutline],
      () =>
        computePositionedColGroupings(
          model.colGroupingData,
          model.colW,
          model.colGroupingInversed,
          model.defColW,
          5,
          otherOutline ? 1 : 0
        )
    );
  }

  /** The row groups' brackets and buttons, from the same heights as the rows drawn. */
  rowGroupings(model: SheetModel): PositionedGrouping[] {
    const sizes = this.sheetSizes(model);
    const otherOutline = model.colGroupingMax > 0;
    return this.rowOutline.get([model.rowGroupingData, sizes, model.rowGroupingInversed, otherOutline], () =>
      computePositionedColGroupings(
        model.rowGroupingData,
        sizes.rowHeightsPx,
        model.rowGroupingInversed,
        sizes.defaultRowHeightPx,
        2,
        otherOutline ? 1 : 0
      )
    );
  }

  /** The references of the formula being written, coloured on the cells: none while no formula is. */
  formulaReferences(draft: { text: string; caret: number } | null, sheetName: string): DrawnReference[] {
    return this.references.get([draft, sheetName], () => {
      if (draft === null) {
        return [];
      }
      const refs = formulaReferences(draft.text, sheetName);
      const atCaret = referenceAt(refs, draft.caret);
      return refs.map((ref) => ({ range: ref.range, color: ref.color, atCaret: ref === atCaret }));
    });
  }

  /**
   * What the formula bar draws; see {@link formulaBarRenderState}. Kept while
   * nothing it is worked out from changes, so a scroll draws it no more.
   */
  formulaBar(input: {
    bar: FormulaBarInputs;
    writing: { text: string } | null;
    selection: SelectionState;
    growing: boolean;
    draft: { text: string } | null;
    sheetName: string;
  }): FormulaBarRenderState {
    return this.bar.get([input.bar, input.writing, input.selection, input.growing, input.draft, input.sheetName], () =>
      formulaBarRenderState(input)
    );
  }

  /** The popup buttons, each at the bottom right corner of its cell. */
  popupButtons(states: readonly PopupButtonState[], model: SheetModel): PositionedPopupButton[] {
    return this.buttons.get(
      [
        states,
        model.horizontalSplitPosition,
        model.verticalSplitPosition,
        model.rowH,
        model.colW,
        model.hiddenRowIndexes,
        model.hiddenColumnIndexes,
        model.defRowH,
        model.defColW
      ],
      () =>
        computePositionedPopupButtons(
          states,
          (col) => drawnColumnWidth(model, col),
          (row) => drawnRowHeight(model, row),
          { cols: model.horizontalSplitPosition, rows: model.verticalSplitPosition }
        )
    );
  }
}

function computeSheetSizes(model: SheetModel): SheetSizes {
  const ppi = FALLBACK_PPI;
  const defaultRowHeightPx = getDefaultRowHeightPx(model.defRowH, ppi);
  const rowHeightsPx = Array.from(model.rowH, (points) => convertPointsToPixel(points, ppi));
  const { hiddenRowIndexes: hiddenRows, hiddenColumnIndexes: hiddenColumns, colW: colWidths, defColW } = model;
  const colWidthAt = (col: number): number => {
    if (hiddenColumns.has(col)) {
      return 0;
    }
    return col > colWidths.length ? defColW : colWidths[col - 1];
  };
  const totals = computeTotalGridSize({
    rows: model.rows,
    cols: model.cols,
    definedRowHeightsPx: rowHeightsPx,
    definedColWidthsPx: Array.from(colWidths),
    hiddenRows,
    hiddenColumns,
    defaultRowHeightPx,
    defaultColWidthPx: defColW
  });
  return {
    rows: new AxisSizes(
      rowHeightsPx.length,
      (row) => (hiddenRows.has(row) ? 0 : rowHeightsPx[row - 1]),
      defaultRowHeightPx
    ),
    columns: new AxisSizes(Math.max(model.cols, colWidths.length), colWidthAt, defColW),
    totalWidthPx: totals.totalWidthPx,
    totalHeightPx: totals.totalHeightPx,
    rowHeightsPx,
    defaultRowHeightPx
  };
}

function computeFrozenPanes(model: SheetModel): FrozenPanes {
  const colX: number[] = [];
  const colWidths: number[] = [];
  let width = 0;
  for (let col = 1; col <= model.horizontalSplitPosition; col++) {
    const w = drawnColumnWidth(model, col);
    colX.push(width);
    colWidths.push(w);
    width += w;
  }
  const rowY: number[] = [];
  const rowHeights: number[] = [];
  let height = 0;
  for (let row = 1; row <= model.verticalSplitPosition; row++) {
    const h = drawnRowHeight(model, row);
    rowY.push(height);
    rowHeights.push(h);
    height += h;
  }
  return { colX, colWidths, width, rowY, rowHeights, height };
}
