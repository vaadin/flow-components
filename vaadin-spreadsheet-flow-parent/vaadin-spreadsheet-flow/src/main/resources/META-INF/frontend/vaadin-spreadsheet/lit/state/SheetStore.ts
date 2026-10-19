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
import { isSingleMerge, selectCellAt, type SheetShape, type SingleCellSelection } from '../algorithms/KeyboardMoves.js';
import { expandToMergedRegions } from '../algorithms/MergedSelection.js';
import type { CellRect } from '../algorithms/SelectionPaint.js';
import { isMultiCellRange } from '../algorithms/SelectionWalk.js';
import { FALLBACK_PPI } from '../algorithms/ViewportMath.js';
import type { CellEditorState } from '../elements/types.js';
import { Derived, drawnColumnWidth, drawnRowHeight } from './Derived.js';
import {
  type CellPress,
  draftOf,
  type Edit,
  formulaOf,
  type FormulaWriting,
  IDLE,
  type InteractionState,
  type TouchUse,
  withFormula,
  withWriting,
  type Writing,
  writingOf
} from './Interaction.js';
import {
  type CellRange,
  initialSheetSelection,
  normalizedRange,
  pickCell,
  replaceRange,
  type SelectionState,
  type SheetSelection
} from './SelectionState.js';
import { type FormulaBarInputs, NO_FORMULA_BAR_INPUTS } from './FormulaBarState.js';
import { SheetData } from './SheetData.js';
import {
  initialSheetModel,
  type SheetModel,
  type SheetPropertyName,
  type SheetPropertyValue,
  withProperty,
  withValues
} from './SheetModel.js';

/**
 * What changes as the sheet is used, and the changes that have it drawn:
 * what the server mirrors onto the element, the cells it sent, the cells
 * selected, and what the user is in the middle of. Each part is replaced
 * whole, and only through the changes here.
 */
export class SheetStore {
  /** See {@link model}. */
  private sheetModel: SheetModel = initialSheetModel();

  /** See {@link sheetSelection}. */
  private selected: SheetSelection = initialSheetSelection();

  /** See {@link interaction}. */
  private ongoing: InteractionState = IDLE;

  /** See {@link sheetData}. */
  private session = new SheetData();

  /** See {@link formulaBar}. */
  private bar: FormulaBarInputs = NO_FORMULA_BAR_INPUTS;

  /** What the sheet draws that is worked out from the model and the selection alone; see {@link Derived}. */
  readonly derived = new Derived();

  /**
   * @param scheduleRender asks for the sheet to be drawn once the handler
   *   making a change ends; see {@link RenderScheduler}.
   */
  constructor(private readonly scheduleRender: () => void) {}

  /**
   * What the server mirrors onto the element, each property by its name on
   * the wire; see {@link SheetModel}. Replaced whole on each change.
   */
  get model(): SheetModel {
    return this.sheetModel;
  }

  /**
   * The cells selected and the cell a merge was entered by; see
   * {@link SheetSelection}. Changed through {@link select} and {@link pick}.
   */
  get sheetSelection(): SheetSelection {
    return this.selected;
  }

  /** The cells selected. */
  get selection(): SelectionState {
    return this.selected.cells;
  }

  /**
   * The cell a click or an arrow asked for when the selection is a merged
   * region: the arrows leave the merge in its column and row. Null when
   * the selection is not a merge.
   */
  get mergeEntry(): { col: number; row: number } | null {
    return this.selected.mergeEntry;
  }

  /**
   * What the user is in the middle of on the sheet: an edit and the formula
   * written in it, a press on the cells, a note; see {@link InteractionState}.
   * Changed through {@link interact}, which draws the change.
   */
  get interaction(): InteractionState {
    return this.ongoing;
  }

  /** The cell editor while it is open: the cell-grid draws an input on its cell. */
  get editor(): CellEditorState | null {
    const edit = this.ongoing.edit;
    return edit.kind === 'editing' ? edit.editor : null;
  }

  /** Pointing at cells for the formula being written, in the cell editor or the formula field. */
  get formula(): FormulaWriting {
    return formulaOf(this.ongoing.edit);
  }

  /** What is written, in the cell editor or the formula field; see {@link Writing}. */
  get writing(): Writing | null {
    return writingOf(this.ongoing.edit);
  }

  /** The formula being written: what is written, while it is a formula. */
  get draft(): Writing | null {
    return draftOf(this.ongoing.edit);
  }

  /**
   * What the client knows of the sheet the server shows: its cells, and the
   * cell the server holds as selected. Replaced whole by {@link resetSheetData}.
   */
  get sheetData(): SheetData {
    return this.session;
  }

  /**
   * What the formula bar shows besides what the selection and the formula
   * being written say; see {@link FormulaBarInputs}. Drawn in the render
   * with the rest of the sheet.
   */
  get formulaBar(): FormulaBarInputs {
    return this.bar;
  }

  /**
   * Takes a property as the server sends it; see {@link SHEET_PROPERTIES}.
   * It is drawn with the rest of the batch it comes in, once the batch is
   * all in: the sheet's {@code notifyStateChanges} has it drawn.
   */
  write<K extends SheetPropertyName>(name: K, value: SheetPropertyValue<K>): void {
    this.sheetModel = withProperty(this.sheetModel, name, value);
  }

  /**
   * Changes the model with values already read in: the client's own, which
   * it holds until the server answers. Drawn as the handler making the
   * change ends.
   */
  change(values: Partial<SheetModel>): void {
    this.sheetModel = withValues(this.sheetModel, values);
    this.scheduleRender();
  }

  /**
   * The server threw away what it knew of the sheet: what the client knows
   * of it starts over. The server only ever sends cells that have something
   * in them, so a cell empty on the sheet shown now is never mentioned again
   * and a value kept from before would show through.
   */
  resetSheetData(): void {
    this.session = new SheetData();
  }

  /**
   * Select one cell - or the whole merge it is part of, with the merge's
   * top-left cell active, as for a click.
   */
  selectCellOrMerge(col: number, row: number): void {
    this.pick(selectCellAt(col, row, this.sheetModel.mergedRegions));
  }

  /** Picks one cell or merge; see {@link pickCell}. */
  pick(next: SingleCellSelection): void {
    this.changeSelection(pickCell(next));
  }

  /**
   * Selects cells, with the merge still entered by the same cell; see
   * {@link replaceRange}. Drawn as the handler ends, before the server is
   * told.
   */
  select(cells: SelectionState): void {
    this.changeSelection(replaceRange(this.selected, cells));
  }

  /**
   * Changes what the user is in the middle of. Drawn as the handler ends,
   * before the server is told.
   */
  interact(interaction: InteractionState): void {
    this.ongoing = interaction;
    this.scheduleRender();
  }

  changeEdit(edit: Edit): void {
    this.interact({ ...this.ongoing, edit });
  }

  /** Changes what is written; see {@link withWriting}. */
  changeWriting(writing: Writing | null): void {
    this.changeEdit(withWriting(this.ongoing.edit, writing));
  }

  /** Changes the pointing at cells for the formula being written; see {@link withFormula}. */
  changeFormula(change: Partial<FormulaWriting>): void {
    this.changeEdit(withFormula(this.ongoing.edit, { ...this.formula, ...change }));
  }

  changePress(press: CellPress | null): void {
    this.interact({ ...this.ongoing, press });
  }

  /** The press just released reported its range, or that is over; see {@link InteractionState.rangeReportedOnRelease}. */
  markRangeReportedOnRelease(reported: boolean): void {
    if (this.ongoing.rangeReportedOnRelease !== reported) {
      this.interact({ ...this.ongoing, rangeReportedOnRelease: reported });
    }
  }

  /** Shift held growing the selection, or let go; see {@link InteractionState.extending}. */
  extendSelection(extending: boolean): void {
    this.interact({ ...this.ongoing, extending });
  }

  /** The sheet used by touch, or not; see {@link TouchUse}. */
  useTouch(touch: TouchUse): void {
    if (this.ongoing.touch !== touch) {
      this.interact({ ...this.ongoing, touch });
    }
  }

  /** What the server named the selection; null leaves the address field naming the cell. */
  nameSelection(name: string | null): void {
    this.changeFormulaBar({ selectionName: name });
  }

  /**
   * What the formula field shows while nobody types in it. Shown again even
   * when it is what the field was last told: the field may hold text typed
   * since, as after an edit thrown away. Each change replaces the inputs,
   * the same text included, and the field is written again from a new render
   * state.
   */
  showInFormulaField(text: string): void {
    this.changeFormulaBar({ fieldText: text });
  }

  /** The named ranges the server sent, offered beside the address field. */
  setNamedRanges(names: ReadonlyArray<string>): void {
    this.changeFormulaBar({ namedRanges: names });
  }

  /** Whether the sheet has any cells: none before the server sends its size. */
  hasCells(): boolean {
    return this.sheetModel.rows > 0 && this.sheetModel.cols > 0;
  }

  /** The sheet as the arrow keys see it. */
  sheetShape(): SheetShape {
    const model = this.sheetModel;
    return {
      cols: model.cols,
      rows: model.rows,
      mergedRegions: model.mergedRegions,
      hiddenColumns: model.hiddenColumnIndexes,
      hiddenRows: model.hiddenRowIndexes
    };
  }

  /**
   * Whether a press is being dragged over the cells: out into a range, from
   * the fill handle, or pointing at cells for the formula being written.
   */
  dragging(): boolean {
    return this.ongoing.press !== null || this.formula.press === 'pointing';
  }

  /** The strip a fill drag paints, while one does. */
  paintRect(): CellRect | null {
    const press = this.ongoing.press;
    return press?.kind === 'fill' && press.outcome.kind !== 'none' ? press.outcome.painted : null;
  }

  /**
   * Whether a range is selected: more than one cell, and more than the one
   * merge the active cell is in, which is a single cell to the user.
   */
  selectsRange(): boolean {
    const { activeRange, activeCell } = this.selection;
    return isMultiCellRange(activeRange) && !isSingleMerge(activeRange, activeCell, this.sheetModel.mergedRegions);
  }

  /**
   * The range from the anchor to a target cell, grown to take in whole
   * merged regions.
   *
   * A merged region is one cell to the user, so a selection that ends
   * inside one has to take all of it; reporting the literal rectangle
   * leaves the server writing to cells nobody can see.
   */
  rangeTo(anchor: { col: number; row: number }, col: number, row: number): CellRange {
    const range = normalizedRange(anchor.col, anchor.row, col, row);
    const grown = expandToMergedRegions(
      { c1: range.c1, c2: range.c2, r1: range.r1, r2: range.r2 },
      this.sheetModel.mergedRegions
    );
    return { c1: grown.c1, c2: grown.c2, r1: grown.r1, r2: grown.r2 };
  }

  /**
   * Rendered width of a column in pixels — 0 when it is hidden, the
   * sheet's default past the last defined column.
   */
  colWidthPx(col: number): number {
    return drawnColumnWidth(this.sheetModel, col);
  }

  /**
   * Rendered height of a row in pixels. POI measures rows in points, so
   * this is the one place the conversion happens.
   */
  rowHeightPx(row: number, ppi: number = FALLBACK_PPI): number {
    return drawnRowHeight(this.sheetModel, row, ppi);
  }

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
  isCellLockedForEdit(col: number, row: number): boolean {
    const model = this.sheetModel;
    if (!model.sheetProtected) {
      return false;
    }
    const cellData = this.session.cells.get(toKey(col, row));
    if (cellData !== undefined) {
      return cellData.locked === true;
    }
    return model.lockedColumnIndexes.has(col) && model.lockedRowIndexes.has(row);
  }

  /**
   * What the formula field shows for a cell.
   *
   * The bar shows what the cell *is*, not what it displays: the formula
   * behind a computed cell, and otherwise the value before formatting.
   * The unformatted value is what carries a text cell's leading
   * apostrophe and a number's full precision, both of which the
   * displayed value has already dropped. The server sends that value only
   * where it differs from the one shown.
   *
   * The server sends a formula without its leading `=`, the way POI
   * stores it.
   */
  formulaFieldValueFor(col: number, row: number): string {
    const data = this.session.cells.get(toKey(col, row));
    if (data === undefined) {
      return '';
    }
    const formula = data.formulaValue;
    if (formula !== undefined && formula !== null && formula !== '') {
      return `=${formula}`;
    }
    return data.originalValue ?? data.value ?? '';
  }

  private changeSelection(selection: SheetSelection): void {
    this.selected = selection;
    this.scheduleRender();
  }

  private changeFormulaBar(change: Partial<FormulaBarInputs>): void {
    this.bar = { ...this.bar, ...change };
    this.scheduleRender();
  }
}
