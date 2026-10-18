/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { isFormula } from '../algorithms/FormulaPointing.js';
import type { CellRect, PaintOutcome } from '../algorithms/SelectionPaint.js';
import type { CellEditorState } from '../elements/types.js';

/**
 * What the user is in the middle of on the sheet: writing in a cell or in the
 * formula field, a press on the cells, a note. Replaced whole on each change,
 * and ended whole with {@link IDLE}.
 *
 * A press and a note stay beside the edit rather than in it: a press on
 * another cell comes while an edit is open, and ends it once the editor has
 * lost the keyboard; the server can open a note while an edit is open, and
 * the edit ends as the note takes the keyboard. Folded into the edit, the edit
 * would end at once and commit nothing.
 */

export interface Cell {
  readonly col: number;
  readonly row: number;
}

/** A reference pointed at: where it starts and ends on the sheet, and where its text is in the formula. */
export interface Pointer {
  readonly anchor: Cell;
  readonly end: Cell;
  readonly span: { readonly start: number; readonly end: number };
}

/**
 * The text written in the cell editor or the formula field, and where its
 * caret is: the one copy of it. The cell editor and the formula field both
 * show it, the one it is not written in as it is written.
 */
export interface Writing {
  readonly text: string;
  readonly caret: number;
  /** Where it is written: the cell editor, or the formula field. */
  readonly source: 'cell' | 'field';
}

/** Pointing at cells while a formula is written, in the cell editor or the formula field. */
export interface FormulaWriting {
  /** The reference pointed at last, which pointing again replaces; null once anything is typed. */
  readonly pointer: Pointer | null;
  /**
   * The cell pointed at last, for the rest of the edit: the next arrow after
   * an operator starts from it, not from the cell being edited.
   */
  readonly lastPointed: Cell | null;
  /**
   * A press on the cells that pointed at one: 'pointing' until the button
   * comes up, then 'pointed' until the click it ends in, which selects
   * nothing.
   */
  readonly press: 'none' | 'pointing' | 'pointed';
}

/** No formula being written. */
export const NO_FORMULA: FormulaWriting = { pointer: null, lastPointed: null, press: 'none' };

/**
 * Writing in the sheet: nothing, the cell editor open - with what is written
 * in it, or in the formula field beside it - or a formula written in the
 * formula field alone. What else is typed into the formula field alone is the
 * field's until it is committed.
 */
export type Edit =
  | { readonly kind: 'idle' }
  | {
      readonly kind: 'editing';
      readonly editor: CellEditorState;
      /**
       * Whether the editor opened on a character typed into a percentage cell,
       * with the % it was given still to come off once the entry turns into a
       * formula.
       */
      readonly autoPercent: boolean;
      /** What is written, in the cell editor or the formula field. */
      readonly writing: Writing;
      readonly formula: FormulaWriting;
    }
  | {
      readonly kind: 'formulaField';
      /** The formula written in the field; null once only what was pointed at is left. */
      readonly writing: Writing | null;
      readonly formula: FormulaWriting;
    };

/**
 * A press on the cells, from the press to the release anywhere on the page:
 * dragging out a range - `crossed` once the pointer has gone into another
 * cell - or dragging the fill handle from the selection it started with.
 */
export type CellPress =
  | { readonly kind: 'select'; readonly crossed: boolean }
  | { readonly kind: 'fill'; readonly from: CellRect; readonly outcome: PaintOutcome };

export interface InteractionState {
  readonly edit: Edit;
  readonly press: CellPress | null;
  /** The cell whose note is being written, by its key. */
  readonly comment: string | null;
  /**
   * Whether Shift is held growing the selection. While it is, the address
   * field counts the block out - "3R x 3C" - rather than naming a cell, as
   * Excel's name box does, whatever the server says of the selection.
   */
  readonly extending: boolean;
}

const NO_EDIT: Edit = { kind: 'idle' };

/** Nothing under way. */
export const IDLE: InteractionState = { edit: NO_EDIT, press: null, comment: null, extending: false };

/** Pointing at cells for the formula being written: none while nothing is. */
export function formulaOf(edit: Edit): FormulaWriting {
  return edit.kind === 'idle' ? NO_FORMULA : edit.formula;
}

/** What is written, in the cell editor or the formula field; null while nothing is. */
export function writingOf(edit: Edit): Writing | null {
  return edit.kind === 'idle' ? null : edit.writing;
}

/**
 * The formula being written: what is written, while it is a formula. Its
 * references are coloured on the cells and in the formula field.
 */
export function draftOf(edit: Edit): Writing | null {
  const writing = writingOf(edit);
  return writing !== null && isFormula(writing.text) ? writing : null;
}

/**
 * The edit with the pointing changed. The editor stays as it is; without
 * one, no formula left and nothing pointed at leaves nothing being written.
 */
export function withFormula(edit: Edit, formula: FormulaWriting): Edit {
  if (edit.kind === 'editing') {
    return { ...edit, formula };
  }
  return fieldEdit(writingOf(edit), formula);
}

/**
 * The edit with what is written changed. The editor stays open whatever is
 * written in it; in the formula field alone only a formula is kept.
 */
export function withWriting(edit: Edit, writing: Writing | null): Edit {
  if (edit.kind === 'editing') {
    return writing === null ? edit : { ...edit, writing };
  }
  return fieldEdit(writing, formulaOf(edit));
}

/** The cell editor opened on its first text, with the caret at its end; what was pointed at is kept. */
export function openEditor(edit: Edit, editor: CellEditorState, autoPercent: boolean): Edit {
  const writing: Writing = { text: editor.initialValue, caret: editor.initialValue.length, source: 'cell' };
  return { kind: 'editing', editor, autoPercent, writing, formula: formulaOf(edit) };
}

/** The cell editor closed, its text with it, and what is left of what was pointed at. */
export function closeEditor(edit: Edit): Edit {
  return fieldEdit(null, formulaOf(edit));
}

/** A formula written in the formula field alone, or nothing once nothing is left of it. */
function fieldEdit(writing: Writing | null, formula: FormulaWriting): Edit {
  const kept = writing !== null && isFormula(writing.text) ? writing : null;
  const empty = kept === null && formula.pointer === null && formula.lastPointed === null && formula.press === 'none';
  return empty ? NO_EDIT : { kind: 'formulaField', writing: kept, formula };
}
