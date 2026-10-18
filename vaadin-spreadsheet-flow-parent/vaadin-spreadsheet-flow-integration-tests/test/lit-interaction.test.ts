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
  type Edit,
  formulaOf,
  type FormulaWriting,
  IDLE,
  NO_FORMULA,
  openEditor,
  withFormula
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/Interaction.js';

/**
 * What the user is in the middle of on the sheet: an edit, with the formula
 * written in it in the cell editor or the formula field, ended whole.
 */
describe('Spreadsheet — what the user is in the middle of', () => {
  const EDITOR = { col: 2, row: 2, initialValue: '=', mode: 'enter' as const };
  const WRITING: FormulaWriting = {
    draft: { text: '=A1', caret: 3 },
    pointer: { anchor: { col: 1, row: 1 }, end: { col: 1, row: 1 }, span: { start: 1, end: 3 } },
    lastPointed: { col: 1, row: 1 },
    press: 'none'
  };

  it('starts with nothing under way', () => {
    expect(IDLE).to.deep.equal({ edit: { kind: 'idle' }, press: null, comment: null, extending: false });
    expect(NO_FORMULA).to.deep.equal({ draft: null, pointer: null, lastPointed: null, press: 'none' });
  });

  it('has no formula being written while nothing is edited', () => {
    expect(formulaOf({ kind: 'idle' })).to.equal(NO_FORMULA);
  });

  it('opens the cell editor with the formula written so far', () => {
    const edit = openEditor({ kind: 'formulaField', formula: WRITING }, EDITOR, true);

    expect(edit).to.deep.equal({ kind: 'editing', editor: EDITOR, autoPercent: true, formula: WRITING });
  });

  it('opens the cell editor on nothing written', () => {
    expect(formulaOf(openEditor({ kind: 'idle' }, EDITOR, false))).to.equal(NO_FORMULA);
  });

  it('keeps the editor open as the formula written in it changes, even to nothing', () => {
    const editing = openEditor({ kind: 'idle' }, EDITOR, false);

    const changed = withFormula(editing, NO_FORMULA);

    expect(changed).to.deep.equal({ kind: 'editing', editor: EDITOR, autoPercent: false, formula: NO_FORMULA });
  });

  it('writes a formula in the formula field alone, with no editor open', () => {
    expect(withFormula({ kind: 'idle' }, WRITING)).to.deep.equal({ kind: 'formulaField', formula: WRITING });
  });

  const leftOver: Array<[string, Partial<FormulaWriting>]> = [
    ['a draft', { draft: { text: '=', caret: 1 } }],
    ['a reference pointed at', { pointer: WRITING.pointer }],
    ['a cell pointed at last', { lastPointed: { col: 1, row: 1 } }],
    ['a press that pointed', { press: 'pointed' }]
  ];
  for (const [what, formula] of leftOver) {
    it(`keeps writing in the formula field while ${what} is left`, () => {
      const edit = withFormula({ kind: 'idle' }, { ...NO_FORMULA, ...formula });

      expect(edit.kind).to.equal('formulaField');
    });
  }

  it('writes nothing once the formula field has nothing left in it', () => {
    const field: Edit = { kind: 'formulaField', formula: WRITING };

    expect(withFormula(field, NO_FORMULA)).to.deep.equal({ kind: 'idle' });
  });

  it('closes the cell editor, leaving what is left of the formula to the field', () => {
    const editing = openEditor({ kind: 'idle' }, EDITOR, true);

    expect(closeEditor(withFormula(editing, { ...NO_FORMULA, press: 'pointed' }))).to.deep.equal({
      kind: 'formulaField',
      formula: { ...NO_FORMULA, press: 'pointed' }
    });
    expect(closeEditor(editing), 'nothing left').to.deep.equal({ kind: 'idle' });
  });
});
