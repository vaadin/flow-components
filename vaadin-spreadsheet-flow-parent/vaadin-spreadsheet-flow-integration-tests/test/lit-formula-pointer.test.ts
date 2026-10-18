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
  FormulaPointer,
  type WrittenText
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/FormulaPointer.js';
import { openEditor } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/Interaction.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';

/**
 * Pointing at cells while a formula is written, on its own: the arrows, the
 * mouse and the formula kept for its colours, against a store and stand-ins
 * for the cell editor and the formula field.
 */
describe('FormulaPointer', () => {
  let store: SheetStore;
  let editorText: WrittenText | null;
  let fieldText: WrittenText | null;
  let fieldFocused: boolean;
  let scrolled: Array<{ col: number; row: number }>;
  let pointing: FormulaPointer;

  /** Opens the cell editor on B2 with a text and its caret, the edit begun as typing over the cell. */
  function editB2(value: string, caret = value.length, autoPercent = false): void {
    store.changeEdit(openEditor(store.interaction.edit, { col: 2, row: 2, initialValue: '', mode: 'enter' }, autoPercent));
    editorText = { value, caret };
  }

  function arrow(key: string, shiftKey = false, source: 'cell' | 'field' = 'cell'): CustomEvent {
    const written = source === 'cell' ? editorText! : fieldText!;
    const e = new CustomEvent('arrow', { detail: { key, shiftKey, value: written.value, caret: written.caret }, cancelable: true });
    pointing.arrow(e, source);
    return e;
  }

  beforeEach(() => {
    store = new SheetStore(() => {});
    store.write('rows', 20);
    store.write('cols', 10);
    editorText = null;
    fieldText = null;
    fieldFocused = false;
    scrolled = [];
    pointing = new FormulaPointer(store, {
      editorText: () => editorText,
      setEditorText: (text, caret) => (editorText = { value: text, caret: caret ?? editorText?.caret ?? 0 }),
      fieldText: () => fieldText,
      fieldHasFocus: () => fieldFocused,
      setFieldText: (text, caret) => (fieldText = { value: text, caret }),
      sheetName: () => 'Sheet1',
      scrollIntoView: (cell) => scrolled.push(cell)
    });
  });

  describe('where a formula is written', () => {
    it('is the cell editor while it holds a formula', () => {
      editB2('=1+');
      expect(pointing.source()).to.equal('cell');

      editorText = { value: '12', caret: 2 };
      expect(pointing.source(), 'a number').to.equal(null);
    });

    it('is the formula field while it has the keyboard and holds a formula, the cell editor open or not', () => {
      editB2('=1+');
      fieldFocused = true;
      fieldText = { value: '=2*', caret: 3 };
      expect(pointing.source()).to.equal('field');

      fieldText = { value: 'text', caret: 4 };
      expect(pointing.source(), 'no formula').to.equal(null);
    });

    it('is nowhere while nothing is edited', () => {
      editorText = { value: '=1+', caret: 3 };

      expect(pointing.source()).to.equal(null);
    });
  });

  describe('the arrows', () => {
    it('put a reference to the next cell over into the formula after an operator', () => {
      editB2('=1+');

      const e = arrow('ArrowRight');

      expect(e.defaultPrevented).to.be.true;
      expect(editorText).to.eql({ value: '=1+C2', caret: 5 });
      expect(store.formulaBar.fieldText).to.equal('=1+C2');
      expect(store.formula.pointer).to.eql({ anchor: { col: 3, row: 2 }, end: { col: 3, row: 2 }, span: { start: 3, end: 5 } });
      expect(store.formula.draft).to.eql({ text: '=1+C2', caret: 5 });
      expect(scrolled).to.eql([{ col: 3, row: 2 }]);
    });

    it('move the reference on from the cell pointed at, and Shift stretches it into a range', () => {
      editB2('=1+');
      arrow('ArrowRight');

      arrow('ArrowDown');
      expect(editorText!.value).to.equal('=1+C3');

      arrow('ArrowRight', true);
      expect(editorText!.value).to.equal('=1+C3:D3');
    });

    it('point from the cell pointed at last, past an operator typed since', () => {
      editB2('=1+');
      arrow('ArrowRight');
      editorText = { value: '=1+C2*', caret: 6 };
      pointing.typed('=1+C2*', 6);

      arrow('ArrowDown');

      expect(editorText!.value).to.equal('=1+C2*C3');
    });

    it('leave a reference typed over by hand alone, the caret back where the pointed one ended', () => {
      editB2('=1+');
      arrow('ArrowRight');
      editorText = { value: '=1+C', caret: 4 };
      pointing.typed('=1+C', 4);
      editorText = { value: '=1+C3', caret: 5 };
      pointing.typed('=1+C3', 5);

      expect(arrow('ArrowRight').defaultPrevented).to.be.false;
      expect(editorText.value).to.equal('=1+C3');
    });

    it('leave what was pointed at alone once the caret is elsewhere', () => {
      editB2('=1+');
      arrow('ArrowRight');
      editorText = { value: '=1+C2', caret: 2 };

      expect(arrow('ArrowRight').defaultPrevented).to.be.false;
      expect(editorText.value).to.equal('=1+C2');
    });

    it('leave a formula alone where no reference can go, and anything that is not a formula', () => {
      editB2('=12');
      expect(arrow('ArrowRight').defaultPrevented, 'after a number').to.be.false;
      expect(editorText!.value).to.equal('=12');

      editorText = { value: '12', caret: 2 };
      expect(arrow('ArrowRight').defaultPrevented, 'no formula').to.be.false;
    });

    it('point in the formula field too, writing the cell editor along with it', () => {
      editB2('=1+');
      fieldFocused = true;
      fieldText = { value: '=1+', caret: 3 };

      arrow('ArrowLeft', false, 'field');

      expect(fieldText).to.eql({ value: '=1+A2', caret: 5 });
      expect(editorText!.value).to.equal('=1+A2');
    });

    it("take off the % a sign typed into a percentage cell was given, as the entry turns into a formula", () => {
      editB2('+%', 1, true);

      arrow('ArrowRight');

      expect(editorText!.value).to.equal('+C2');
      const edit = store.interaction.edit;
      expect(edit.kind === 'editing' && edit.autoPercent).to.be.false;
    });
  });

  describe('the mouse', () => {
    it('puts a reference to the pressed cell into the formula, and stretches it as the pointer goes over others', () => {
      editB2('=SUM(');

      pointing.press(4, 4, false, false, 'cell');
      expect(editorText!.value).to.equal('=SUM(D4');
      expect(store.formula.press).to.equal('pointing');

      expect(pointing.dragOver(5, 6)).to.be.true;
      expect(editorText!.value).to.equal('=SUM(D4:E6');
    });

    it('adds another reference after a comma with Ctrl, and stretches the last one with Shift', () => {
      editB2('=SUM(');
      pointing.press(4, 4, false, false, 'cell');

      pointing.press(6, 6, false, true, 'cell');
      expect(editorText!.value).to.equal('=SUM(D4,F6');

      pointing.press(7, 8, true, false, 'cell');
      expect(editorText!.value).to.equal('=SUM(D4,F6:G8');
    });

    it('replaces the reference the caret is in', () => {
      editB2('=A1+B1', 2);

      pointing.press(3, 3, false, false, 'cell');

      expect(editorText!.value).to.equal('=C3+B1');
    });

    it('goes over cells without pointing while no press points', () => {
      editB2('=SUM(');
      expect(pointing.dragOver(5, 6), 'nothing pointed at').to.be.false;

      arrow('ArrowRight');
      expect(pointing.dragOver(5, 6), 'pointed at with an arrow').to.be.false;
      expect(editorText!.value).to.equal('=SUM(C2');
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

  describe('the formula being written', () => {
    it('is kept while it is a formula, and dropped when the text is not one', () => {
      pointing.updateDraft('=A1', 3);
      expect(store.formula.draft).to.eql({ text: '=A1', caret: 3 });

      const kept = store.formula.draft;
      pointing.updateDraft('=A1', 3);
      expect(store.formula.draft, 'the same text and caret').to.equal(kept);

      pointing.updateDraft('12', 2);
      expect(store.formula.draft).to.equal(null);
    });

    it('drops what was pointed at when typed into, and when the caret leaves it', () => {
      editB2('=1+');
      arrow('ArrowRight');
      pointing.caretMoved('=1+C2', 5);
      expect(store.formula.pointer, 'the caret still at its end').to.not.equal(null);

      pointing.caretMoved('=1+C2', 2);
      expect(store.formula.pointer).to.equal(null);
      expect(store.formula.draft).to.eql({ text: '=1+C2', caret: 2 });
    });

    it('takes no caret from an editor on its way out', () => {
      pointing.caretMoved('=A1', 3);

      expect(store.formula.draft).to.equal(null);
    });

    it('ends with nothing pointed at or coloured', () => {
      editB2('=1+');
      arrow('ArrowRight');

      pointing.end();

      expect(store.formula.draft).to.equal(null);
      expect(store.formula.pointer).to.equal(null);
      expect(store.formula.lastPointed).to.equal(null);
    });
  });
});
