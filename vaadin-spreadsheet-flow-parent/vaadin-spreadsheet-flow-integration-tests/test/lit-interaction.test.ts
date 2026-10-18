/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import {
  closeEditor,
  draftOf,
  type Edit,
  formulaOf,
  type FormulaWriting,
  IDLE,
  NO_FORMULA,
  openEditor,
  withFormula,
  withWriting,
  type Writing,
  writingOf
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/Interaction.js';

/**
 * What the user is in the middle of on the sheet: an edit, with what is
 * written in the cell editor or the formula field and the cells pointed at
 * for a formula, ended whole.
 */
describe('Spreadsheet — what the user is in the middle of', () => {
  const EDITOR = { col: 2, row: 2, initialValue: '=', mode: 'enter' as const };
  const POINTING: FormulaWriting = {
    pointer: { anchor: { col: 1, row: 1 }, end: { col: 1, row: 1 }, span: { start: 1, end: 3 } },
    lastPointed: { col: 1, row: 1 },
    press: 'none'
  };
  const IN_FIELD: Writing = { text: '=A1', caret: 3, source: 'field' };

  it('starts with nothing under way', () => {
    expect(IDLE).to.deep.equal({ edit: { kind: 'idle' }, press: null, comment: null, extending: false });
    expect(NO_FORMULA).to.deep.equal({ pointer: null, lastPointed: null, press: 'none' });
  });

  it('has nothing written, and nothing pointed at, while nothing is edited', () => {
    expect(formulaOf({ kind: 'idle' })).to.equal(NO_FORMULA);
    expect(writingOf({ kind: 'idle' })).to.equal(null);
    expect(draftOf({ kind: 'idle' })).to.equal(null);
  });

  it('opens the cell editor on its first text, with the caret at its end, keeping what was pointed at', () => {
    const edit = openEditor({ kind: 'formulaField', writing: IN_FIELD, formula: POINTING }, EDITOR, true);

    expect(edit).to.deep.equal({
      kind: 'editing',
      editor: EDITOR,
      autoPercent: true,
      writing: { text: '=', caret: 1, source: 'cell' },
      formula: POINTING
    });
  });

  it('opens the cell editor on nothing pointed at', () => {
    expect(formulaOf(openEditor({ kind: 'idle' }, EDITOR, false))).to.equal(NO_FORMULA);
  });

  it('keeps the editor open whatever is written in it and whatever is pointed at', () => {
    const editing = openEditor({ kind: 'idle' }, EDITOR, false);

    expect(withFormula(editing, NO_FORMULA).kind).to.equal('editing');
    expect(writingOf(withWriting(editing, { text: '', caret: 0, source: 'cell' }))).to.eql({
      text: '',
      caret: 0,
      source: 'cell'
    });
    expect(withWriting(editing, null), 'nothing said of what is written').to.equal(editing);
  });

  it('takes the formula being written from what is written, while it is a formula', () => {
    const editing = openEditor({ kind: 'idle' }, { ...EDITOR, initialValue: '12' }, false);
    expect(draftOf(editing), 'a number').to.equal(null);

    const formula = withWriting(editing, IN_FIELD);
    expect(draftOf(formula)).to.equal(writingOf(formula));
  });

  it('writes a formula in the formula field alone, with no editor open', () => {
    expect(withWriting({ kind: 'idle' }, IN_FIELD)).to.deep.equal({
      kind: 'formulaField',
      writing: IN_FIELD,
      formula: NO_FORMULA
    });
  });

  it('keeps nothing of other text typed into the formula field alone', () => {
    expect(withWriting({ kind: 'idle' }, { text: 'abc', caret: 3, source: 'field' })).to.deep.equal({ kind: 'idle' });
  });

  const leftOver: Array<[string, Partial<FormulaWriting>]> = [
    ['a reference pointed at', { pointer: POINTING.pointer }],
    ['a cell pointed at last', { lastPointed: { col: 1, row: 1 } }],
    ['a press that pointed', { press: 'pointed' }]
  ];
  for (const [what, formula] of leftOver) {
    it(`keeps writing in the formula field while ${what} is left`, () => {
      const edit = withFormula({ kind: 'idle' }, { ...NO_FORMULA, ...formula });

      expect(edit).to.deep.equal({ kind: 'formulaField', writing: null, formula: { ...NO_FORMULA, ...formula } });
    });
  }

  it('writes nothing once the formula field has nothing left in it', () => {
    const field: Edit = { kind: 'formulaField', writing: IN_FIELD, formula: POINTING };

    expect(withFormula(field, NO_FORMULA).kind, 'the formula still written').to.equal('formulaField');
    expect(withWriting(withFormula(field, NO_FORMULA), null)).to.deep.equal({ kind: 'idle' });
  });

  it('closes the cell editor with its text, leaving what is left of the pointing to the field', () => {
    const editing = withWriting(openEditor({ kind: 'idle' }, EDITOR, true), IN_FIELD);

    expect(closeEditor(withFormula(editing, { ...NO_FORMULA, press: 'pointed' }))).to.deep.equal({
      kind: 'formulaField',
      writing: null,
      formula: { ...NO_FORMULA, press: 'pointed' }
    });
    expect(closeEditor(editing), 'nothing left').to.deep.equal({ kind: 'idle' });
  });
});
