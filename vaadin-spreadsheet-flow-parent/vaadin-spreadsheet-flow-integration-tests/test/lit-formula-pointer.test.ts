/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { FormulaPointer } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/FormulaPointer.js';
import { openEditor } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/Interaction.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';

/**
 * Pointing at cells while a formula is written, on its own: the arrows, the
 * mouse and what is written, kept in the store the sheet draws both inputs
 * from.
 */
describe('FormulaPointer', () => {
  let store: SheetStore;
  let fieldFocused: boolean;
  let scrolled: Array<{ col: number; row: number }>;
  let pointing: FormulaPointer;

  /** Opens the cell editor on B2 with a text written in it, the edit begun as typing over the cell. */
  function editB2(text: string, caret = text.length, autoPercent = false): void {
    store.changeEdit(openEditor(store.interaction.edit, { col: 2, row: 2, initialValue: text, mode: 'enter' }, autoPercent));
    store.changeWriting({ text, caret, source: 'cell' });
  }

  /** Writes in the formula field, which has the keyboard. */
  function inField(text: string, caret = text.length): void {
    fieldFocused = true;
    store.changeWriting({ text, caret, source: 'field' });
  }

  function arrow(key: string, shiftKey = false, source: 'cell' | 'field' = 'cell'): CustomEvent {
    const { text, caret } = store.writing!;
    const e = new CustomEvent('arrow', { detail: { key, shiftKey, value: text, caret }, cancelable: true });
    pointing.arrow(e, source);
    return e;
  }

  const written = (): string | undefined => store.writing?.text;

  beforeEach(() => {
    store = new SheetStore(() => {});
    store.write('rows', 20);
    store.write('cols', 10);
    fieldFocused = false;
    scrolled = [];
    pointing = new FormulaPointer(store, {
      fieldHasFocus: () => fieldFocused,
      sheetName: () => 'Sheet1',
      scrollIntoView: (cell) => scrolled.push(cell)
    });
  });

  describe('where a formula is written', () => {
    it('is the cell editor while it holds a formula', () => {
      editB2('=1+');
      expect(pointing.source()).to.equal('cell');

      store.changeWriting({ text: '12', caret: 2, source: 'cell' });
      expect(pointing.source(), 'a number').to.equal(null);
    });

    it('is the formula field while it has the keyboard and holds a formula, the cell editor open or not', () => {
      editB2('=1+');
      inField('=2*');
      expect(pointing.source()).to.equal('field');

      store.changeWriting({ text: 'text', caret: 4, source: 'field' });
      expect(pointing.source(), 'no formula').to.equal(null);
    });

    it('is nowhere while nothing is edited', () => {
      expect(pointing.source()).to.equal(null);
    });
  });

  describe('the arrows', () => {
    it('put a reference to the next cell over into the formula after an operator', () => {
      editB2('=1+');

      const e = arrow('ArrowRight');

      expect(e.defaultPrevented).to.be.true;
      expect(store.writing).to.eql({ text: '=1+C2', caret: 5, source: 'cell' });
      expect(store.formula.pointer).to.eql({ anchor: { col: 3, row: 2 }, end: { col: 3, row: 2 }, span: { start: 3, end: 5 } });
      expect(store.draft).to.eql({ text: '=1+C2', caret: 5, source: 'cell' });
      expect(scrolled).to.eql([{ col: 3, row: 2 }]);
    });

    it('move the reference on from the cell pointed at, and Shift stretches it into a range', () => {
      editB2('=1+');
      arrow('ArrowRight');

      arrow('ArrowDown');
      expect(written()).to.equal('=1+C3');

      arrow('ArrowRight', true);
      expect(written()).to.equal('=1+C3:D3');
    });

    it('point from the cell pointed at last, past an operator typed since', () => {
      editB2('=1+');
      arrow('ArrowRight');
      pointing.typed('=1+C2*', 6, 'cell');

      arrow('ArrowDown');

      expect(written()).to.equal('=1+C2*C3');
    });

    it('leave a reference typed over by hand alone, the caret back where the pointed one ended', () => {
      editB2('=1+');
      arrow('ArrowRight');
      pointing.typed('=1+C', 4, 'cell');
      pointing.typed('=1+C3', 5, 'cell');

      expect(arrow('ArrowRight').defaultPrevented).to.be.false;
      expect(written()).to.equal('=1+C3');
    });

    it('leave what was pointed at alone once the caret is elsewhere', () => {
      editB2('=1+');
      arrow('ArrowRight');
      store.changeWriting({ text: '=1+C2', caret: 2, source: 'cell' });

      expect(arrow('ArrowRight').defaultPrevented).to.be.false;
      expect(written()).to.equal('=1+C2');
    });

    it('leave a formula alone where no reference can go, and anything that is not a formula', () => {
      editB2('=12');
      expect(arrow('ArrowRight').defaultPrevented, 'after a number').to.be.false;
      expect(written()).to.equal('=12');

      store.changeWriting({ text: '12', caret: 2, source: 'cell' });
      expect(arrow('ArrowRight').defaultPrevented, 'no formula').to.be.false;
    });

    it('point in the formula field too, with the caret where the reference ends', () => {
      editB2('=1+');
      inField('=1+');

      arrow('ArrowLeft', false, 'field');

      expect(store.writing).to.eql({ text: '=1+A2', caret: 5, source: 'field' });
    });

    it('take off the % a sign typed into a percentage cell was given, as the entry turns into a formula', () => {
      editB2('+%', 1, true);

      arrow('ArrowRight');

      expect(written()).to.equal('+C2');
      const edit = store.interaction.edit;
      expect(edit.kind === 'editing' && edit.autoPercent).to.be.false;
    });
  });

  describe('the mouse', () => {
    it('puts a reference to the pressed cell into the formula, and stretches it as the pointer goes over others', () => {
      editB2('=SUM(');

      pointing.press(4, 4, false, false, 'cell');
      expect(written()).to.equal('=SUM(D4');
      expect(store.formula.press).to.equal('pointing');

      expect(pointing.dragOver(5, 6)).to.be.true;
      expect(written()).to.equal('=SUM(D4:E6');
    });

    it('adds another reference after a comma with Ctrl, and stretches the last one with Shift', () => {
      editB2('=SUM(');
      pointing.press(4, 4, false, false, 'cell');

      pointing.press(6, 6, false, true, 'cell');
      expect(written()).to.equal('=SUM(D4,F6');

      pointing.press(7, 8, true, false, 'cell');
      expect(written()).to.equal('=SUM(D4,F6:G8');
    });

    it('replaces the reference the caret is in', () => {
      editB2('=A1+B1', 2);

      pointing.press(3, 3, false, false, 'cell');

      expect(written()).to.equal('=C3+B1');
    });

    it('goes over cells without pointing while no press points', () => {
      editB2('=SUM(');
      expect(pointing.dragOver(5, 6), 'nothing pointed at').to.be.false;

      arrow('ArrowRight');
      expect(pointing.dragOver(5, 6), 'pointed at with an arrow').to.be.false;
      expect(written()).to.equal('=SUM(C2');
    });

    it('ends a press that pointed on the release, and the click after it selects nothing', () => {
      editB2('=SUM(');
      pointing.press(4, 4, false, false, 'cell');

      expect(pointing.released()).to.be.true;
      expect(store.formula.press).to.equal('pointed');
      expect(pointing.clicked()).to.be.true;
      expect(store.formula.press).to.equal('none');
      expect(pointing.released(), 'no press pointing').to.be.false;
      expect(pointing.clicked(), 'no press that pointed').to.be.false;
    });
  });

  describe('what is written', () => {
    it('is kept as it is typed, where it is typed, the formula being written with it', () => {
      editB2('');

      pointing.typed('=A', 2, 'cell');
      expect(store.draft).to.eql({ text: '=A', caret: 2, source: 'cell' });

      pointing.typed('=A1', 3, 'field');
      expect(store.writing).to.eql({ text: '=A1', caret: 3, source: 'field' });
    });

    it('is kept once for the same text, caret and input', () => {
      editB2('=A1');
      const kept = store.writing;

      pointing.caretMoved('=A1', 3, 'cell');

      expect(store.writing).to.equal(kept);
    });

    it('keeps nothing of text typed into the formula field alone that is no formula', () => {
      fieldFocused = true;

      pointing.typed('abc', 3, 'field');

      expect(store.interaction.edit.kind).to.equal('idle');
    });

    it('drops what was pointed at when the caret leaves it', () => {
      editB2('=1+');
      arrow('ArrowRight');
      pointing.caretMoved('=1+C2', 5, 'cell');
      expect(store.formula.pointer, 'the caret still at its end').to.not.equal(null);

      pointing.caretMoved('=1+C2', 2, 'cell');
      expect(store.formula.pointer).to.equal(null);
      expect(store.writing).to.eql({ text: '=1+C2', caret: 2, source: 'cell' });
    });

    it('takes no caret from an editor on its way out', () => {
      pointing.caretMoved('=A1', 3, 'cell');

      expect(store.writing).to.equal(null);
    });

    it('ends with nothing pointed at, the editor keeping its text until it closes', () => {
      editB2('=1+');
      arrow('ArrowRight');

      pointing.end();

      expect(store.formula.pointer).to.equal(null);
      expect(store.formula.lastPointed).to.equal(null);
      expect(written()).to.equal('=1+C2');
    });

    it('ends a formula written in the formula field alone', () => {
      inField('=1+');
      arrow('ArrowRight', false, 'field');

      pointing.end();

      expect(store.interaction.edit.kind).to.equal('idle');
      expect(store.draft).to.equal(null);
    });
  });
});
