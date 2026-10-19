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

/** Where a formula is written: in the cell editor, or in the formula field. */
export type FormulaSource = 'cell' | 'field';

/** What pointing at cells for a formula needs of the sheet. */
export interface FormulaPointerHost {
  /** Whether the formula field has the keyboard. */
  fieldHasFocus(): boolean;
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
 * stays on the cell being edited. Also keeps what is written, from what the
 * two inputs report; the sheet draws it into both.
 */
export class FormulaPointer {
  constructor(private readonly store: SheetStore, private readonly host: FormulaPointerHost) {}

  /** Where a formula is being written now: the formula field, the cell editor, or nowhere. */
  source(): FormulaSource | null {
    const draft = this.store.draft;
    if (this.host.fieldHasFocus()) {
      return draft !== null && draft.source === 'field' ? 'field' : null;
    }
    return this.store.editor !== null && draft !== null ? 'cell' : null;
  }

  /**
   * Typed into the cell editor or the formula field. Typing ends pointing at
   * that reference: the next arrow after an operator points at a new one,
   * from the cell pointed at last.
   */
  typed(value: string, caret: number, source: FormulaSource): void {
    this.store.changeFormula({ pointer: null });
    this.write(value, caret, source);
  }

  /** The caret moved in the cell editor or the formula field, or the edit just began. */
  caretMoved(value: string, caret: number, source: FormulaSource): void {
    if (this.store.editor === null && !this.host.fieldHasFocus()) {
      // From an editor on its way out - the edit is over.
      return;
    }
    const pointer = this.store.formula.pointer;
    if (pointer !== null && caret !== pointer.span.end) {
      // Moved away from what was pointed at: the next point starts afresh.
      this.store.changeFormula({ pointer: null });
    }
    this.write(value, caret, source);
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
    const direction = keyDirection(key, false);
    if (direction === null || !isFormula(value) || model.rows <= 0 || model.cols <= 0) {
      return;
    }
    const pointer = formula.pointer !== null && caret === formula.pointer.span.end ? formula.pointer : null;
    if (pointer === null && !canPoint(value, caret)) {
      return;
    }
    e.preventDefault();
    const from = pointer?.end ?? formula.lastPointed ?? this.formulaCell();
    const next = moveActiveCell(from, null, direction, this.store.sheetShape());
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
    const writing = this.store.writing;
    if (writing === null) {
      return;
    }
    let { text: value, caret } = writing;
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
    const writing = this.store.writing;
    if (writing !== null) {
      const end = selectCellAt(col, row, this.store.model.mergedRegions).activeCell;
      this.pointAt(pointer.anchor, end, writing.text, pointer.span.end, pointer.span, this.source() ?? 'cell');
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

  /**
   * The formula is written or thrown away: nothing is pointed at any more,
   * and a formula written in the field alone is gone. An open editor keeps
   * its text until it closes.
   */
  end(): void {
    if (this.store.editor === null) {
      this.store.changeWriting(null);
    }
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
    this.write(placed.text, placed.end, source);
  }

  /**
   * Keeps what is written, unless it is what is kept already, or text in the
   * formula field alone that is no formula: that stays the field's.
   */
  private write(text: string, caret: number, source: FormulaSource): void {
    const writing = this.store.writing;
    if (writing !== null && writing.text === text && writing.caret === caret && writing.source === source) {
      return;
    }
    if (writing === null && this.store.editor === null && !isFormula(text)) {
      return;
    }
    this.store.changeWriting({ text, caret, source });
  }

  /** The cell the formula being written is for. */
  private formulaCell(): Cell {
    const editor = this.store.editor;
    return editor !== null ? { col: editor.col, row: editor.row } : this.store.selection.activeCell;
  }
}
