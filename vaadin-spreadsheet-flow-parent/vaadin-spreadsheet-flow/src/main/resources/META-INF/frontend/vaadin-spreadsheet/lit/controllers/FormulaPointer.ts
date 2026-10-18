/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import {
  canPoint,
  formulaReferences,
  isFormula,
  placeReference,
  referenceAt,
  referenceText
} from '../algorithms/FormulaPointing.js';
import { keyDirection, moveActiveCell, selectCellAt } from '../algorithms/KeyboardMoves.js';
import type { SheetStore } from '../state/SheetStore.js';

/** A cell, by its column and row, 1-based. */
interface Cell {
  col: number;
  row: number;
}

/** A text being written, and where its caret is. */
export interface WrittenText {
  value: string;
  caret: number;
}

/** Where a formula is written: in the cell editor, or in the formula field. */
export type FormulaSource = 'cell' | 'field';

/** What pointing at cells for a formula needs of the sheet. */
export interface FormulaPointerHost {
  /** The cell editor's text and caret. */
  editorText(): WrittenText | null;
  /** Writes the cell editor's text, and puts its caret at the place given, if one is. */
  setEditorText(text: string, caret?: number): void;
  /** The formula field's text and caret; null while the field is not drawn. */
  fieldText(): WrittenText | null;
  /** Whether the formula field has the keyboard. */
  fieldHasFocus(): boolean;
  /** Writes the formula field's text, with its caret at the place given. */
  setFieldText(text: string, caret: number): void;
  /** The name of the sheet shown, which a reference without a sheet of its own is on. */
  sheetName(): string;
  /** Brings a cell into view. */
  scrollIntoView(cell: Cell): void;
}

/**
 * Pointing at cells while a formula is written, in the cell editor or the
 * formula field, as Excel has it: after an operator, a bracket or a
 * separator an arrow or a click puts a reference to a cell into the
 * formula, and further arrows, a drag or Shift stretch it. The selection
 * stays on the cell being edited. Also keeps the formula being written,
 * whose references are coloured on the cells and in the formula field.
 */
export class FormulaPointer {
  constructor(private readonly store: SheetStore, private readonly host: FormulaPointerHost) {}

  /** Where a formula is being written now: the formula field, the cell editor, or nowhere. */
  source(): FormulaSource | null {
    if (this.host.fieldHasFocus()) {
      const field = this.host.fieldText();
      return field !== null && isFormula(field.value) ? 'field' : null;
    }
    const draft = this.editorDraft();
    return draft !== null && isFormula(draft.value) ? 'cell' : null;
  }

  /**
   * Typed into the cell editor or the formula field. Typing ends pointing at
   * that reference: the next arrow after an operator points at a new one,
   * from the cell pointed at last.
   */
  typed(value: string, caret: number): void {
    this.store.changeFormula({ pointer: null });
    this.updateDraft(value, caret);
  }

  /** The caret moved in the formula being written, or the edit just began. */
  caretMoved(value: string, caret: number): void {
    if (this.store.editor === null && !this.host.fieldHasFocus()) {
      // From an editor on its way out - the edit is over.
      return;
    }
    const pointer = this.store.formula.pointer;
    if (pointer !== null && caret !== pointer.span.end) {
      // Moved away from what was pointed at: the next point starts afresh.
      this.store.changeFormula({ pointer: null });
    }
    this.updateDraft(value, caret);
  }

  /**
   * Point at a cell with an arrow while a formula is written: after an
   * operator, a bracket or a separator the
   * arrow puts a reference to the next cell over into the formula, and
   * further arrows move it on - from the cell being edited at first, then
   * from the cell pointed at last, even past an operator typed since.
   * Shift+arrow stretches it into a range from where it started. The
   * selection stays on the cell being edited.
   */
  arrow(e: CustomEvent<{ key: string; shiftKey: boolean; value: string; caret: number }>, source: FormulaSource): void {
    const { key, shiftKey, value, caret } = e.detail;
    const { model, formula } = this.store;
    if (!isFormula(value) || model.rows <= 0 || model.cols <= 0) {
      return;
    }
    const pointer = formula.pointer !== null && caret === formula.pointer.span.end ? formula.pointer : null;
    if (pointer === null && !canPoint(value, caret)) {
      return;
    }
    e.preventDefault();
    const from = pointer?.end ?? formula.lastPointed ?? this.formulaCell();
    const next = moveActiveCell(from, null, keyDirection(key, false)!, this.store.sheetShape());
    const end = next?.activeCell ?? from;
    const anchor = shiftKey && pointer !== null ? pointer.anchor : end;
    this.pointAt(anchor, end, value, caret, pointer?.span ?? null, source);
    this.host.scrollIntoView(end);
  }

  /**
   * Point at a cell with the mouse while a formula is written: a click puts
   * a reference to it into the formula - in place of the reference pointed
   * at a moment ago, or the one the caret is in - a drag stretches it into a range, Shift stretches the last one to
   * the cell, and Ctrl or Cmd adds another after a comma.
   */
  press(col: number, row: number, shiftKey: boolean, add: boolean, source: FormulaSource): void {
    const draft = source === 'field' ? this.host.fieldText() : this.editorDraft();
    if (draft === null) {
      return;
    }
    let { value, caret } = draft;
    const { formula } = this.store;
    const cell = selectCellAt(col, row, this.store.model.mergedRegions).activeCell;
    const pointer = formula.pointer !== null && caret === formula.pointer.span.end ? formula.pointer : null;
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
      const inRef = canPoint(value, caret) ? null : referenceAt(formulaReferences(value, this.host.sheetName()), caret);
      span = inRef === null ? null : { start: inRef.start, end: inRef.end };
      if (shiftKey && inRef !== null) {
        anchor = { col: inRef.range.c1, row: inRef.range.r1 };
      }
    }
    this.pointAt(anchor, cell, value, caret, span, source);
    this.store.changeFormula({ press: 'pointing' });
  }

  /**
   * The pointer went over a cell. While a press points, the reference
   * pointed at stretches to it; whether it did.
   */
  dragOver(col: number, row: number): boolean {
    const pointer = this.store.formula.pointer;
    if (this.store.formula.press !== 'pointing' || pointer === null) {
      return false;
    }
    const source = this.source();
    const draft = source === 'field' ? this.host.fieldText() : this.editorDraft();
    if (draft !== null) {
      const end = selectCellAt(col, row, this.store.model.mergedRegions).activeCell;
      this.pointAt(pointer.anchor, end, draft.value, pointer.span.end, pointer.span, source ?? 'cell');
    }
    return true;
  }

  /** The button came up. A press that pointed is over, and the click it ends in is to come; whether it pointed. */
  released(): boolean {
    if (this.store.formula.press !== 'pointing') {
      return false;
    }
    this.store.changeFormula({ press: 'pointed' });
    return true;
  }

  /** A click on a cell. The press it ends pointed at the cell for a formula, and selects nothing; whether it did. */
  clicked(): boolean {
    if (this.store.formula.press === 'none') {
      return false;
    }
    this.store.changeFormula({ press: 'none' });
    return true;
  }

  /** Colours the references of the formula being written, or none when it is not one. */
  updateDraft(text: string | null, caret: number): void {
    const draft = text !== null && isFormula(text) ? { text, caret } : null;
    const before = this.store.formula.draft;
    if (draft === null && before === null) {
      return;
    }
    if (draft !== null && before !== null && draft.text === before.text && draft.caret === before.caret) {
      return;
    }
    this.store.changeFormula({ draft });
  }

  /** The formula is written or thrown away: nothing is pointed at or coloured any more. */
  end(): void {
    this.updateDraft(null, 0);
    this.store.changeFormula({ pointer: null, lastPointed: null });
  }

  /** Puts the reference to a pointed cell or range into the formula being written. */
  private pointAt(
    anchor: Cell,
    end: Cell,
    value: string,
    caret: number,
    span: { start: number; end: number } | null,
    source: FormulaSource
  ): void {
    const edit = this.store.interaction.edit;
    if (edit.kind === 'editing' && edit.autoPercent && value.endsWith('%')) {
      // The % a sign typed into a percentage cell was given belongs to a
      // number. Pointing makes the entry a formula, where "+A1%" would take
      // a hundredth of A1. It goes with the editor that entry opened: a %
      // in a formula written once that editor has closed is the formula's.
      value = value.slice(0, -1);
      caret = Math.min(caret, value.length);
    }
    if (edit.kind === 'editing' && edit.autoPercent) {
      this.store.changeEdit({ ...edit, autoPercent: false });
    }
    const placed = placeReference(value, caret, span, referenceText(anchor, end));
    this.store.changeFormula({
      pointer: { anchor, end, span: { start: placed.start, end: placed.end } },
      lastPointed: end
    });
    if (this.store.editor !== null) {
      this.host.setEditorText(placed.text, source === 'cell' ? placed.end : undefined);
    }
    if (source === 'field') {
      this.host.setFieldText(placed.text, placed.end);
    }
    this.store.showInFormulaField(placed.text);
    this.updateDraft(placed.text, placed.end);
  }

  /** What the cell editor holds and where its caret is, while it is open. */
  private editorDraft(): WrittenText | null {
    return this.store.editor === null ? null : this.host.editorText();
  }

  /** The cell the formula being written is for. */
  private formulaCell(): Cell {
    const editor = this.store.editor;
    return editor !== null ? { col: editor.col, row: editor.row } : this.store.selection.activeCell;
  }
}
