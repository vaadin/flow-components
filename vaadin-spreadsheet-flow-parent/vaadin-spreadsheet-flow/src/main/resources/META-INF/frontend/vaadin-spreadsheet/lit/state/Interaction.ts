/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

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

/** A formula being written, in the cell editor or the formula field. Pointing at cells is part of it. */
export interface FormulaWriting {
  /** The formula and where its caret is; null while the text is no formula. */
  readonly draft: { readonly text: string; readonly caret: number } | null;
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
export const NO_FORMULA: FormulaWriting = { draft: null, pointer: null, lastPointed: null, press: 'none' };

/**
 * Writing in the sheet: nothing, the cell editor open - with the formula
 * written in it, or in the formula field beside it - or a formula written in
 * the formula field alone.
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
      readonly formula: FormulaWriting;
    }
  | { readonly kind: 'formulaField'; readonly formula: FormulaWriting };

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
}

const NO_EDIT: Edit = { kind: 'idle' };

/** Nothing under way. */
export const IDLE: InteractionState = { edit: NO_EDIT, press: null, comment: null };

/** The formula being written, in the editor or the field: none while nothing is. */
export function formulaOf(edit: Edit): FormulaWriting {
  return edit.kind === 'idle' ? NO_FORMULA : edit.formula;
}

/**
 * The edit with the formula written changed. The editor stays as it is;
 * without one, a formula with nothing left in it leaves nothing being
 * written.
 */
export function withFormula(edit: Edit, formula: FormulaWriting): Edit {
  if (edit.kind === 'editing') {
    return { ...edit, formula };
  }
  const empty =
    formula.draft === null && formula.pointer === null && formula.lastPointed === null && formula.press === 'none';
  return empty ? NO_EDIT : { kind: 'formulaField', formula };
}

/** The cell editor opened, the formula written so far kept. */
export function openEditor(edit: Edit, editor: CellEditorState, autoPercent: boolean): Edit {
  return { kind: 'editing', editor, autoPercent, formula: formulaOf(edit) };
}

/** The cell editor closed, with what is left of the formula written. */
export function closeEditor(edit: Edit): Edit {
  return withFormula(NO_EDIT, formulaOf(edit));
}
