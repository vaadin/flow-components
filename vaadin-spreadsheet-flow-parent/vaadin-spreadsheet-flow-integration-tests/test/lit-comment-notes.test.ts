/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { html, render } from 'lit';
import { cellGridStyles } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/CellGridStyles.js';
import {
  buildCommentOverlays,
  COMMENT_GAP,
  CommentNotes,
  commentLineStyle,
  drawnNotes
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/CommentNotes.js';
import {
  type CellGridRenderState,
  emptyCellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';

/** Stands in for the cell grid: draws the notes in its shadow root as the grid's update would. */
class NotesHost extends HTMLElement {
  renderState: CellGridRenderState = emptyCellGridRenderState();
  updatesAsked = 0;
  readonly notes = new CommentNotes(this);

  constructor() {
    super();
    this.attachShadow({ mode: 'open' }).adoptedStyleSheets = [cellGridStyles.styleSheet!];
  }

  requestUpdate(): void {
    this.updatesAsked++;
  }

  /**
   * One update: before drawing, the drawing, and after it. The notes are
   * followed by something else, as in the grid, so a copy fading out at the
   * end of the shadow root is not theirs to clear.
   */
  draw(): void {
    this.notes.willUpdate();
    render(html`${this.notes.render()}<span hidden></span>`, this.shadowRoot!);
    this.notes.updated();
  }
}
customElements.define('test-comment-notes-host', NotesHost);

/**
 * The cell grid's notes, on their own: which show, how each is drawn and
 * placed, the ones that go fading out, and writing one.
 */
describe('CommentNotes', () => {
  /** Four columns of 100 pixels and ten rows of 20, notes on A1 and D2. */
  function sheet(overrides: Partial<CellGridRenderState> = {}): CellGridRenderState {
    const empty = emptyCellGridRenderState();
    return {
      ...empty,
      viewport: { ...empty.viewport, firstColumnIndex: 1, lastColumnIndex: 4, firstRowIndex: 1, lastRowIndex: 10 },
      colX: [0, 100, 200, 300],
      colWidths: [100, 100, 100, 100],
      rowY: [0, 20, 40, 60, 80, 100, 120, 140, 160, 180],
      rowHeights: [20, 20, 20, 20, 20, 20, 20, 20, 20, 20],
      commentText: { 'col1 row1': 'First', 'col4 row2': 'Fourth' },
      commentAuthors: { 'col1 row1': 'Ann' },
      ...overrides
    };
  }

  describe('the notes that show', () => {
    it("are the workbook's always-visible ones and the one under the pointer, each at its cell's box", () => {
      const notes = drawnNotes(sheet({ visibleCommentKeys: ['col4 row2'] }), 'col1 row1');

      expect(notes.map((note) => [note.key, note.comment])).to.eql([
        ['col4 row2', 'Fourth'],
        ['col1 row1', 'First']
      ]);
      expect(notes[1].coord).to.eql({ col: 1, row: 1 });
      expect(notes[0].geometry).to.eql({ left: 300, top: 20, width: 100, height: 20 });
    });

    it('are none for a cell under the pointer with no note and no error', () => {
      expect(drawnNotes(sheet(), 'col2 row2')).to.eql([]);
    });

    it("show a formula's error only while the pointer is on its cell, and only with a message", () => {
      const state = sheet({ invalidFormulaCells: new Set(['col2 row2']), invalidFormulaMessage: 'Bad formula' });

      expect(drawnNotes(state, 'col2 row2').map((note) => [note.key, note.showsError])).to.eql([['col2 row2', true]]);
      expect(drawnNotes({ ...state, visibleCommentKeys: ['col2 row2'] }, null)).to.eql([]);
      expect(drawnNotes({ ...state, invalidFormulaMessage: null }, 'col2 row2')).to.eql([]);
    });

    it('keep the note being written up, with no text yet and the pointer elsewhere', () => {
      const notes = drawnNotes(sheet({ editingCommentKey: 'col3 row3' }), null);

      expect(notes.map((note) => [note.key, note.editing, note.comment])).to.eql([['col3 row3', true, undefined]]);
    });

    it('leave out the note of a cell not drawn', () => {
      expect(drawnNotes(sheet({ visibleCommentKeys: ['col9 row9', 'col0 row1'], commentText: { 'col9 row9': 'x', 'col0 row1': 'y' } }), null)).to.eql(
        []
      );
    });
  });

  describe('the line to a note', () => {
    it("runs from the cell's corner up to the note's top left, turned up", () => {
      // From (100, 40) to the note's corner two pixels in, at (217, 27).
      const style = commentLineStyle(100, 40, 215, 25, 50);
      const length = Math.sqrt(117 * 117 + 13 * 13) + 1;

      expect(style).to.equal(
        `left:100px;top:40px;width:${length}px;transform:rotate(${-(Math.atan(13 / 117) * 180) / Math.PI}deg);`
      );
    });

    it("runs to the note's top right when the note had to go left of the cell", () => {
      const style = commentLineStyle(300, 40, 100, 25, 150);

      expect(style).to.contain(`rotate(${-180 + (Math.atan(13 / 50) * 180) / Math.PI}deg)`);
    });

    it('runs flat to a note level with the cell or below it, as long as the way down', () => {
      expect(commentLineStyle(100, 10, 120, 30, 50)).to.equal(`left:100px;top:10px;width:${Math.sqrt(22 * 22 + 22 * 22) + 1}px;transform:rotate(0deg);`);
      expect(commentLineStyle(300, 10, 100, 30, 150)).to.contain('rotate(-180deg)');
    });
  });

  describe('a note drawn', () => {
    function drawn(state: CellGridRenderState, hovered: string | null): HTMLElement {
      const container = document.createElement('div');
      render(buildCommentOverlays(state, drawnNotes(state, hovered), hovered), container);
      return container;
    }

    it('hangs right of its cell and above it, its line from the corner, carrying where its cell is', () => {
      const container = drawn(sheet(), 'col1 row1');

      const [line, card] = Array.from(container.children) as HTMLElement[];
      expect(line.className).to.equal('comment-overlay-line');
      expect(card.style.cssText).to.equal(`left: ${100 + COMMENT_GAP}px; top: ${-COMMENT_GAP}px; position: absolute;`);
      expect([card.dataset.cellLeft, card.dataset.cellRight, card.dataset.cellTop, card.dataset.key]).to.eql([
        '0',
        '100',
        '0',
        'col1 row1'
      ]);
      expect([card.dataset.quadrant, card.dataset.pinned]).to.eql(['main', 'false']);
      expect(card.querySelector('[part="comment-author"]')!.textContent).to.equal('Ann');
      expect(card.querySelector('[part="comment-text"]')!.textContent).to.equal('First');
    });

    it("is pinned when the workbook shows it always, and kept to the pane of its cell", () => {
      const card = drawn(
        sheet({ visibleCommentKeys: ['col1 row1'], horizontalSplitPosition: 1, frozenColX: [0], frozenColWidths: [100] }),
        null
      ).querySelector<HTMLElement>('.v-spreadsheet-comment-overlay')!;

      expect([card.dataset.pinned, card.dataset.quadrant]).to.eql(['true', 'left']);
    });

    it("shows a formula's error over its comment, set apart, and a text box for the note being written", () => {
      const state = sheet({
        commentText: { 'col1 row1': 'First' },
        invalidFormulaCells: new Set(['col1 row1']),
        invalidFormulaMessage: 'Bad formula',
        editingCommentKey: 'col1 row1'
      });

      const card = drawn(state, 'col1 row1').querySelector<HTMLElement>('.v-spreadsheet-comment-overlay')!;

      expect(Array.from(card.children, (child) => child.className)).to.eql([
        'comment-overlay-invalidformula',
        'comment-overlay-separator',
        'comment-overlay-author',
        'comment-overlay-edit'
      ]);
      const box = card.querySelector('textarea')!;
      expect([box.value, box.dataset.key, box.getAttribute('part')]).to.eql(['First', 'col1 row1', 'comment-edit']);
    });
  });

  describe('on the grid', () => {
    let container: HTMLElement;
    let host: NotesHost;
    const cards = (): HTMLElement[] =>
      Array.from(host.shadowRoot!.querySelectorAll<HTMLElement>('.v-spreadsheet-comment-overlay'));

    beforeEach(() => {
      container = document.createElement('div');
      // A little wider than the four columns, so the last one's corner is in the pane.
      container.style.cssText = 'position:relative;width:420px;height:200px;overflow:hidden;';
      host = new NotesHost();
      // As wide as a sheet's columns run, so a note has the room it takes.
      host.style.width = '1000px';
      host.renderState = sheet();
      container.append(host);
      document.body.append(container);
      host.notes.listen(host.shadowRoot!);
    });

    afterEach(() => {
      container.remove();
    });

    it('asks for a drawing only when the pointer comes to or leaves a cell that shows something', () => {
      host.notes.hover('col2 row2');
      host.notes.hover('col3 row3');
      expect(host.updatesAsked, 'between cells with nothing to show').to.equal(0);

      host.notes.hover('col1 row1');
      host.notes.hover('col1 row1');
      host.notes.hover(null);
      expect(host.updatesAsked).to.equal(2);

      host.renderState = sheet({ invalidFormulaCells: new Set(['col2 row2']) });
      host.notes.hover('col2 row2');
      expect(host.updatesAsked, "a formula's error").to.equal(3);
    });

    it('looks nothing up after a drawing with no note, now or before', () => {
      // The sheet draws on every scroll step; with no note there is nothing
      // to place.
      const root = host.shadowRoot!;
      const looked: string[] = [];
      const all = root.querySelectorAll.bind(root);
      const one = root.querySelector.bind(root);
      root.querySelectorAll = ((selector: string) => {
        looked.push(selector);
        return all(selector);
      }) as ShadowRoot['querySelectorAll'];
      root.querySelector = ((selector: string) => {
        looked.push(selector);
        return one(selector);
      }) as ShadowRoot['querySelector'];

      host.draw();
      host.draw();

      expect(looked).to.deep.equal([]);
    });

    it('drops a note into place again when it shows again after going', () => {
      host.notes.hover('col1 row1');
      host.draw();
      cards()[0].classList.remove('animate-in');
      host.notes.hover(null);
      host.draw();

      host.notes.hover('col1 row1');
      host.draw();

      const shown = cards().filter((card) => !card.classList.contains('animate-out'));
      expect(shown).to.have.length(1);
      expect(shown[0].classList.contains('animate-in')).to.be.true;
    });

    it('drops a note into place as it appears, and not again as the sheet draws', () => {
      host.notes.hover('col1 row1');
      host.draw();
      expect(cards()[0].classList.contains('animate-in')).to.be.true;
      cards()[0].classList.remove('animate-in');

      host.draw();

      expect(cards()).to.have.length(1);
      expect(cards()[0].classList.contains('animate-in')).to.be.false;
    });

    it('fades out a copy of a note that goes, which takes no keys, as the note itself goes', () => {
      host.notes.hover('col1 row1');
      host.draw();
      cards()[0].classList.remove('animate-in');

      host.notes.hover(null);
      host.draw();

      const [copy] = cards();
      expect(cards()).to.have.length(1);
      expect([copy.inert, copy.classList.contains('animate-out'), copy.dataset.key]).to.eql([true, true, 'col1 row1']);
    });

    it('lets a note still dropping into place, or one not shown, go without a copy', () => {
      host.notes.hover('col1 row1');
      host.draw();
      host.notes.hover(null);
      host.draw();
      expect(cards(), 'still dropping in').to.have.length(0);

      host.renderState = sheet({ visibleCommentKeys: ['col4 row2'], colX: [0, 100, 200, 500] });
      host.draw();
      const [hidden] = cards();
      expect(hidden.style.display, 'its cell out of the pane').to.equal('none');
      // Its opening animation called off as it was hidden.
      hidden.classList.remove('animate-in');
      host.renderState = sheet({ colX: [0, 100, 200, 500] });
      host.draw();
      expect(cards()).to.have.length(0);
    });

    it('places a note right of its cell, and left of it when it would run past the pane', () => {
      host.renderState = sheet({ visibleCommentKeys: ['col1 row1', 'col4 row2'] });
      host.draw();

      const [first, fourth] = cards();
      expect(first.style.left).to.equal(`${100 + COMMENT_GAP}px`);
      expect(fourth.style.left).to.equal(`${300 - COMMENT_GAP - fourth.offsetWidth}px`);
      // The line from the cell's corner turns back to the note on its left.
      const line = fourth.previousElementSibling as HTMLElement;
      expect(line.style.left).to.equal('400px');
      expect(parseFloat(line.style.transform.slice('rotate('.length))).to.be.below(-90);
    });

    it('stays right of its cell when it fits on neither side', () => {
      container.style.width = '300px';
      host.renderState = sheet({ visibleCommentKeys: ['col2 row2'], commentText: { 'col2 row2': 'A note too long to fit beside its cell on either side of it' } });
      host.draw();

      const [note] = cards();
      expect(note.offsetWidth > 100, 'a wide note').to.be.true;
      expect(note.style.left).to.equal(`${200 + COMMENT_GAP}px`);
    });

    it('moves up a note that would run past the foot of the pane', () => {
      host.renderState = sheet({
        commentText: { 'col1 row10': 'Low' },
        rowY: [0, 20, 40, 60, 80, 100, 120, 140, 160, 190]
      });
      host.notes.hover('col1 row10');
      host.draw();

      const [note] = cards();
      expect(190 - COMMENT_GAP + note.offsetHeight > 200, 'hanging past the foot').to.be.true;
      expect(parseFloat(note.style.top) + note.offsetHeight).to.equal(200 - 5);
    });

    it("hides a pinned note whose cell's corner has scrolled under the frozen columns, past the freeze line", () => {
      host.style.marginLeft = '50px';
      host.renderState = sheet({
        horizontalSplitPosition: 1,
        frozenColX: [0],
        frozenColWidths: [100],
        viewport: { ...sheet().viewport, firstColumnIndex: 2 },
        colX: [0, 100, 200],
        colWidths: [100, 100, 100],
        visibleCommentKeys: ['col2 row2'],
        commentText: { 'col2 row2': 'Under' }
      });
      host.draw();

      expect(cards()[0].style.display).to.equal('none');
    });

    it('keeps a note inside the pane at the top, and hides a pinned note whose cell has left it', () => {
      host.renderState = sheet({
        visibleCommentKeys: ['col1 row1', 'col2 row10'],
        commentText: { 'col1 row1': 'First', 'col2 row10': 'Far' },
        rowY: [0, 20, 40, 60, 80, 100, 120, 140, 160, 250]
      });
      host.draw();

      const [first, far] = cards();
      expect(first.style.top).to.equal('0px');
      expect(far.style.display).to.equal('none');
      expect((far.previousElementSibling as HTMLElement).style.display).to.equal('none');
    });

    it('gives the keyboard to the note being written, the caret after its text', () => {
      host.renderState = sheet({ editingCommentKey: 'col1 row1' });
      host.draw();

      const box = host.shadowRoot!.querySelector('textarea')!;
      expect(host.shadowRoot!.activeElement).to.equal(box);
      expect(box.selectionStart).to.equal('First'.length);

      box.setSelectionRange(2, 2);
      host.draw();
      expect(box.selectionStart, 'not again on the next drawing').to.equal(2);
    });

    it('reports the note as written when it is left, and ends it on Escape, keeping its keys from the sheet', () => {
      host.renderState = sheet({ editingCommentKey: 'col1 row1' });
      host.draw();
      const box = host.shadowRoot!.querySelector('textarea')!;
      const written: unknown[] = [];
      const keysReached: string[] = [];
      host.addEventListener('cell-comment-commit', (e) => written.push((e as CustomEvent).detail));
      container.addEventListener('keydown', (e) => keysReached.push(e.key));
      box.value = 'Changed';

      box.dispatchEvent(new KeyboardEvent('keydown', { key: 'a', bubbles: true, composed: true }));
      const escape = new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, composed: true, cancelable: true });
      box.dispatchEvent(escape);

      expect(keysReached).to.eql([]);
      expect(escape.defaultPrevented).to.be.true;
      expect(written).to.eql([{ key: 'col1 row1', text: 'Changed' }]);
      expect(host.shadowRoot!.activeElement).to.equal(null);
    });

    it('leaves the note open on an Escape that drops a conversion', () => {
      host.renderState = sheet({ editingCommentKey: 'col1 row1' });
      host.draw();
      const box = host.shadowRoot!.querySelector('textarea')!;

      box.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', isComposing: true, bubbles: true, composed: true }));

      expect(host.shadowRoot!.activeElement).to.equal(box);
    });
  });
});
