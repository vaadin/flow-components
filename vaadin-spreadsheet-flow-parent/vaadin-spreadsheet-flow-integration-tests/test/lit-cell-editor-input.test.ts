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
import {
  CellEditor,
  cellEditorInput,
  editorWidth
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/CellEditor.js';
import {
  type CellEditorState,
  type CellGridRenderState,
  emptyCellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';

/** Stands in for the cell grid: draws the editor in its shadow root as the grid's update would. */
class EditorHost extends HTMLElement {
  renderState: CellGridRenderState = emptyCellGridRenderState();
  theme: string | null = null;
  /** Every value measured, in its style; ten pixels a character. */
  readonly measured: Array<[string, string]> = [];
  readonly editor = new CellEditor(this, (cellStyle, value) => {
    this.measured.push([cellStyle, value]);
    return value.length * 10;
  });

  constructor() {
    super();
    this.attachShadow({ mode: 'open' });
  }

  /** One update: the drawing, and after it. */
  draw(): void {
    render(html`${this.editor.render()}<span hidden></span>`, this.shadowRoot!);
    this.editor.updated();
  }
}
customElements.define('test-cell-editor-host', EditorHost);

/**
 * The cell editor, on its own: its width, the input over the cell, its
 * keys, and fitting it to the text and giving it the keyboard.
 */
describe('CellEditor', () => {
  /** Columns 1 to 4 of 50 pixels and rows 1 to 3 of 20, the second column hidden. */
  function sheet(editor: Partial<CellEditorState> | null, overrides: Partial<CellGridRenderState> = {}): CellGridRenderState {
    const empty = emptyCellGridRenderState();
    return {
      ...empty,
      viewport: { ...empty.viewport, firstColumnIndex: 1, lastColumnIndex: 4, firstRowIndex: 1, lastRowIndex: 3 },
      colX: [0, 50, 50, 100],
      colWidths: [50, 0, 50, 50],
      rowY: [0, 20, 40],
      rowHeights: [20, 20, 20],
      editor: editor === null ? null : { col: 3, row: 2, initialValue: 'abc', mode: 'enter', ...editor },
      ...overrides
    };
  }

  describe('the width', () => {
    it('is its column and the gridline at its right for text that fits', () => {
      expect(editorWidth(sheet(null), 3, 2, 30)).to.equal(51);
    });

    it('takes in the columns after it, whole ones, until the text fits, and no more than are drawn', () => {
      expect(editorWidth(sheet(null), 3, 2, 60)).to.equal(101);
      expect(editorWidth(sheet(null), 3, 2, 500)).to.equal(101);
    });

    it('starts from a whole merged cell, and takes in the columns past its last one', () => {
      const state = sheet(null, {
        colWidths: [50, 0, 50, 70],
        mergedRegions: [{ col1: 1, row1: 1, col2: 3, row2: 1 }],
        mergedRegionBoxes: [{ left: 0, top: 0, width: 100, height: 20 }]
      });

      expect(editorWidth(state, 1, 1, 10)).to.equal(101);
      expect(editorWidth(state, 1, 1, 120)).to.equal(171);
    });
  });

  describe('the input', () => {
    function drawn(state: CellGridRenderState, lumo = false, fitWidth?: number): HTMLInputElement | null {
      const container = document.createElement('div');
      render(cellEditorInput(state, lumo, fitWidth), container);
      return container.querySelector('input');
    }

    it('is drawn over the cell being edited, named for it, on its first value', () => {
      const input = drawn(sheet({}))!;

      expect([input.className, input.getAttribute('part'), input.getAttribute('aria-label')]).to.eql([
        'cell-editor',
        'cell-input',
        'Edit cell C2'
      ]);
      expect(input.value).to.equal('abc');
      expect(input.style.cssText).to.equal('left: 50px; top: 20px; width: 51px; height: 20px;');
    });

    it('is as wide as measured, once measured', () => {
      expect(drawn(sheet({}), false, 77)!.style.width).to.equal('77px');
    });

    it("is dressed as its cell, and in the Lumo theme's font and text colour last", () => {
      const state = sheet({}, { cellStyleToCSSStyle: { 0: 'color: red' } });

      expect(drawn(state)!.style.color).to.equal('red');
      const lumo = drawn(state, true)!;
      expect(lumo.style.color).to.equal('var(--default-color)');
      expect(lumo.style.getPropertyValue('--default-font-family')).to.equal('var(--lumo-font-family)');
    });

    it('is not drawn while no cell is edited, nor for a cell out of view or hidden', () => {
      expect(cellEditorInput(sheet(null))).to.equal('');
      expect(cellEditorInput(sheet({ col: 9 }))).to.equal('');
      expect(cellEditorInput(sheet({ col: 2 }))).to.equal('');
      expect(cellEditorInput(sheet({}, { rowHeights: [20, 0, 20] }))).to.equal('');
    });
  });

  describe('on the grid', () => {
    let host: EditorHost;
    let heard: Array<{ type: string; detail: any }>;
    let take: Set<string>;
    const input = (): HTMLInputElement => host.shadowRoot!.querySelector<HTMLInputElement>('.cell-editor')!;

    function key(name: string, init: KeyboardEventInit = {}): KeyboardEvent {
      const e = new KeyboardEvent('keydown', { key: name, bubbles: true, composed: true, cancelable: true, ...init });
      input().dispatchEvent(e);
      return e;
    }

    beforeEach(() => {
      host = new EditorHost();
      document.body.append(host);
      heard = [];
      take = new Set();
      for (const type of ['cell-editor-commit', 'cell-editor-cancel', 'cell-editor-arrow', 'cell-editor-caret', 'cell-editor-input', 'keydown']) {
        host.addEventListener(type, (e) => {
          heard.push({ type, detail: (e as CustomEvent).detail });
          if (take.has(type)) {
            e.preventDefault();
          }
        });
      }
    });

    afterEach(() => {
      host.remove();
    });

    it('looks nothing up after a drawing with no editor open', () => {
      // The sheet draws on every scroll step; with no editor there is no
      // input to fit or to give the keyboard to.
      host.renderState = sheet(null);
      const root = host.shadowRoot!;
      const looked: string[] = [];
      const one = root.querySelector.bind(root);
      root.querySelector = ((selector: string) => {
        looked.push(selector);
        return one(selector);
      }) as ShadowRoot['querySelector'];

      host.draw();

      expect(looked).to.deep.equal([]);
    });

    it('fits an editor as it opens, and gives it the keyboard, the caret before a percent sign, saying where it is', () => {
      host.renderState = sheet({ initialValue: '5%' }, { cellData: new Map([['col3 row2', { col: 3, row: 2, value: '5%', cellStyle: 'cs7', locked: false, needsMeasure: false, isPercentage: true }]]) });

      host.draw();

      expect(host.measured).to.eql([['cs7', '5%']]);
      expect(input().style.width).to.equal('51px');
      expect(host.shadowRoot!.activeElement).to.equal(input());
      expect(input().selectionStart).to.equal(1);
      expect(heard).to.eql([{ type: 'cell-editor-caret', detail: { value: '5%', caret: 1 } }]);
    });

    it('keeps its width and leaves the keyboard alone as the sheet draws again, and starts over for the next edit', () => {
      host.renderState = sheet({});
      host.draw();
      input().value = 'abcdefgh';
      input().dispatchEvent(new Event('input'));
      expect(input().style.width).to.equal('101px');
      host.measured.length = 0;
      input().blur();
      heard.length = 0;

      host.draw();
      expect(input().style.width, 'on a drawing again').to.equal('101px');
      expect(host.shadowRoot!.activeElement, 'the keyboard not taken back').to.equal(null);
      expect(host.measured).to.eql([]);

      host.renderState = sheet(null);
      host.draw();
      host.renderState = sheet({});
      host.draw();
      expect(input().style.width, 'the next edit').to.equal('51px');
    });

    it('widens to what is typed, saying what it holds', () => {
      host.renderState = sheet({});
      host.draw();
      heard.length = 0;

      input().value = 'abcdefgh';
      input().setSelectionRange(4, 4);
      input().dispatchEvent(new Event('input'));

      expect(input().style.width).to.equal('101px');
      expect(heard).to.eql([{ type: 'cell-editor-input', detail: { col: 3, row: 2, value: 'abcdefgh', caret: 4 } }]);
    });

    it('takes a value typed elsewhere, with its caret, and fits it; gives what it holds; and nothing with none open', () => {
      host.renderState = sheet({});
      host.draw();

      host.editor.setValue('abcdefgh', 2);

      expect([input().value, input().selectionStart, input().style.width]).to.eql(['abcdefgh', 2, '101px']);
      expect(host.editor.value()).to.equal('abcdefgh');
      expect(host.editor.draft()).to.eql({ value: 'abcdefgh', caret: 2 });
      host.renderState = sheet(null);
      host.draw();
      host.editor.setValue('x');
      expect([host.editor.value(), host.editor.draft()]).to.eql([null, null]);
    });

    describe('its keys', () => {
      beforeEach(() => {
        host.renderState = sheet({});
        host.draw();
        heard.length = 0;
      });

      it('commit on Enter and Tab, with the key and Shift, and cancel on Escape, taking the keys', () => {
        const enter = key('Enter');
        const tab = key('Tab', { shiftKey: true });
        const escape = key('Escape');

        expect([enter.defaultPrevented, tab.defaultPrevented, escape.defaultPrevented]).to.eql([true, true, true]);
        expect(heard.map((h) => [h.type, h.detail.key, h.detail.shiftKey])).to.eql([
          ['cell-editor-commit', 'Enter', false],
          ['cell-editor-commit', 'Tab', true],
          ['cell-editor-cancel', undefined, undefined]
        ]);
        expect(heard[0].detail).to.include({ col: 3, row: 2, value: 'abc' });
      });

      it('commit and move on with an arrow over a value typed over the cell', () => {
        const down = key('ArrowDown');

        expect(down.defaultPrevented).to.be.true;
        expect(heard.map((h) => h.type)).to.eql(['cell-editor-arrow', 'cell-editor-commit']);
        // The caret where the editor opened it, at the end.
        expect(heard[0].detail).to.eql({ key: 'ArrowDown', shiftKey: false, value: 'abc', caret: 3 });
        expect(heard[1].detail.key).to.equal('ArrowDown');
      });

      it('move the caret with an arrow in a value opened to be changed, or in a formula', () => {
        host.renderState = sheet({ mode: 'edit' });
        host.draw();
        expect(key('ArrowLeft').defaultPrevented, 'a value opened to be changed').to.be.false;

        host.renderState = sheet({ mode: 'enter' });
        host.draw();
        host.editor.setValue('=A1');
        expect(key('ArrowLeft').defaultPrevented, 'a formula').to.be.false;
        expect(heard.filter((h) => h.type === 'cell-editor-commit')).to.eql([]);
      });

      it('leave an arrow the sheet takes to point at a cell to the sheet', () => {
        take.add('cell-editor-arrow');

        expect(key('ArrowRight').defaultPrevented).to.be.true;
        expect(heard.map((h) => h.type)).to.eql(['cell-editor-arrow']);
      });

      it('go no further than the editor, and leave the keys of an input method alone', () => {
        const typed = key('x');
        const converted = key('Enter', { isComposing: true });

        expect([typed.defaultPrevented, converted.defaultPrevented]).to.eql([false, false]);
        expect(heard).to.eql([]);
      });

      it('commit once: the editor going away after a key commits nothing more', () => {
        key('Enter');
        input().dispatchEvent(new FocusEvent('blur'));

        expect(heard.map((h) => h.type)).to.eql(['cell-editor-commit']);
      });

      it('commit again once a commit kept the edit going', () => {
        take.add('cell-editor-commit');
        key('Enter');
        input().dispatchEvent(new FocusEvent('blur'));

        expect(heard.map((h) => h.type)).to.eql(['cell-editor-commit', 'cell-editor-commit']);
      });
    });

    it('commits when left, saying where the keyboard went', () => {
      host.renderState = sheet({});
      host.draw();
      heard.length = 0;
      const elsewhere = document.createElement('input');

      input().dispatchEvent(new FocusEvent('blur', { relatedTarget: elsewhere }));

      expect(heard.map((h) => [h.type, h.detail.key, h.detail.relatedTarget])).to.eql([
        ['cell-editor-commit', undefined, elsewhere]
      ]);
    });

    it('says where the caret is as it moves', () => {
      host.renderState = sheet({});
      host.draw();
      heard.length = 0;
      input().setSelectionRange(1, 1);

      input().dispatchEvent(new KeyboardEvent('keyup', { key: 'ArrowLeft' }));
      input().dispatchEvent(new MouseEvent('mouseup'));

      expect(heard).to.eql([
        { type: 'cell-editor-caret', detail: { value: 'abc', caret: 1 } },
        { type: 'cell-editor-caret', detail: { value: 'abc', caret: 1 } }
      ]);
    });
  });
});
