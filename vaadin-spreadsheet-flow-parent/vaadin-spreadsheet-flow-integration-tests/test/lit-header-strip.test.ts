/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { html, type TemplateResult } from 'lit';
import {
  type HeaderAxis,
  type HeaderLabels,
  type HeaderLine,
  HeaderStrip
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/headers/HeaderStrip.js';
import type { PositionedGrouping } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/headers/Grouping.js';

const AXIS: HeaderAxis = {
  key: 'col',
  cellClass: 'test-cell',
  handleClass: 'test-handle',
  toggleClass: 'test-toggle',
  edge: 'left',
  events: {
    click: 'test-click',
    contextMenu: 'test-menu',
    resizeStart: 'test-resize',
    autofit: 'test-autofit',
    groupingToggle: 'test-toggled'
  }
};

/** A header strip drawing whatever it is handed, with the base's helpers. */
class TestStrip extends HeaderStrip {
  static properties = { drawing: { attribute: false } };
  protected axis: HeaderAxis = AXIS;
  declare drawing: TemplateResult;

  constructor() {
    super();
    this.drawing = html``;
  }

  render(): TemplateResult {
    return this.drawing;
  }

  labelsOf(
    line: HeaderLine,
    build: (index: number, start: number, size: number) => { cell: TemplateResult; handle: TemplateResult } | null
  ): HeaderLabels {
    return this.labels(line, build);
  }

  toggle(g: PositionedGrouping, style: string): TemplateResult {
    return this.groupingToggle(g, style);
  }

  stripOf(grouping: TemplateResult[], labels: Pick<HeaderLabels, 'cells' | 'handles'>): TemplateResult {
    return this.strip(grouping, labels, html`<i class="gap"></i>`, html`<b class="frozen"></b>`);
  }
}
customElements.define('test-header-strip', TestStrip);

/**
 * What the column letters and the row numbers do alike, on its own: the
 * events they fire, a label for each column or row, the grouping buttons,
 * and the strip they make.
 */
describe('HeaderStrip', () => {
  let strip: TestStrip;
  let heard: Array<{ type: string; detail: any }>;
  let take: boolean;

  /** A label of a column at the given place, its cell and its handle. */
  const label = (col: number, start: number, size: number) => ({
    cell: html`<div class="test-cell col${col}" data-col=${col} style="position:absolute;left:${start}px;width:${size}px;height:20px;">
      ${col}
    </div>`,
    handle: html`<div class="test-handle" data-col=${col} style="position:absolute;left:${start + size - 3}px;width:5px;height:20px;"></div>`
  });

  async function draw(template: TemplateResult): Promise<void> {
    strip.drawing = template;
    await strip.updateComplete;
  }

  const inStrip = (selector: string): HTMLElement => strip.shadowRoot!.querySelector<HTMLElement>(selector)!;

  beforeEach(async () => {
    strip = new TestStrip();
    strip.style.cssText = 'display:block;position:relative;height:40px;';
    document.body.append(strip);
    await strip.updateComplete;
    heard = [];
    take = false;
    for (const type of Object.values(AXIS.events)) {
      strip.addEventListener(type, (e) => {
        heard.push({ type, detail: (e as CustomEvent).detail });
        if (take) {
          e.preventDefault();
        }
      });
    }
  });

  afterEach(() => {
    strip.remove();
  });

  describe('the labels along a line', () => {
    const LINE: HeaderLine = {
      first: 4,
      last: 7,
      starts: [100, 150, 150, 200],
      sizes: [50, 0, 50, 50],
      frozen: 2,
      frozenStarts: [0, 40],
      frozenSizes: [40, 30]
    };

    it('are built for each column in view, at its place, keyed by it, none for one of no size', () => {
      const built: number[][] = [];
      const labels = strip.labelsOf(LINE, (col, start, size) => {
        built.push([col, start, size]);
        return label(col, start, size);
      });

      expect(built).to.eql([
        [4, 100, 50],
        [6, 150, 50],
        [7, 200, 50],
        [1, 0, 40],
        [2, 40, 30]
      ]);
      expect(labels.cells.map((cell) => cell.key)).to.eql([4, 6, 7]);
      expect(labels.handles.map((handle) => handle.key)).to.eql([4, 6, 7]);
    });

    it('keep the frozen ones apart, in order', () => {
      const labels = strip.labelsOf(LINE, label);

      expect(labels.frozenCells).to.have.length(2);
      expect(labels.frozenHandles).to.have.length(2);
      expect(labels.frozenCells[1].values).to.include(2);
    });

    it('leave out the ones not built, and are none with nothing frozen', () => {
      const labels = strip.labelsOf({ ...LINE, frozen: 0 }, (col, start, size) => (col === 6 ? null : label(col, start, size)));

      expect(labels.cells.map((cell) => cell.key)).to.eql([4, 7]);
      expect([labels.frozenCells, labels.frozenHandles]).to.eql([[], []]);
    });

    it('place a column they have no place or size for at 0, and leave it out', () => {
      const built: number[] = [];
      strip.labelsOf({ ...LINE, last: 9, frozen: 3 }, (col) => {
        built.push(col);
        return null;
      });

      expect(built).to.eql([4, 6, 7, 1, 2]);
    });
  });

  describe('the strip', () => {
    it('is the outline gutter, the labels and their handles, the gap, and the frozen strip over them, in that order', async () => {
      const labels = strip.labelsOf({ first: 1, last: 2, starts: [0, 50], sizes: [50, 50], frozen: 0, frozenStarts: [], frozenSizes: [] }, label);

      await draw(strip.stripOf([html`<u class="gutter"></u>`], labels));

      expect(Array.from(strip.shadowRoot!.children, (child) => child.className)).to.eql([
        'gutter',
        'test-cell col1',
        'test-cell col2',
        'test-handle',
        'test-handle',
        'gap',
        'frozen'
      ]);
    });

    it('keeps the element of a label still in view as the line scrolls', async () => {
      const line = (first: number): HeaderLine => ({
        first,
        last: first + 2,
        starts: [0, 50, 100],
        sizes: [50, 50, 50],
        frozen: 0,
        frozenStarts: [],
        frozenSizes: []
      });
      await draw(strip.stripOf([], strip.labelsOf(line(1), label)));
      const third = inStrip('.test-cell.col3');

      await draw(strip.stripOf([], strip.labelsOf(line(2), label)));

      expect(inStrip('.test-cell.col3')).to.equal(third);
    });
  });

  it('draws a grouping button that opens a closed group and closes an open one', async () => {
    await draw(html`${strip.toggle({ uniqueIndex: 7, level: 1, startPx: 0, endPx: 10, buttonPx: 20, collapsed: true }, 'left:3px;')}${strip.toggle(
      { uniqueIndex: 8, level: 2, startPx: 0, endPx: 10, buttonPx: 20, collapsed: false },
      'left:5px;'
    )}`);

    const [closed, open] = Array.from(strip.shadowRoot!.querySelectorAll<HTMLButtonElement>('button'));
    expect([closed.className, closed.type, closed.getAttribute('aria-expanded'), closed.textContent!.trim()]).to.eql([
      'test-toggle',
      'button',
      'false',
      '+'
    ]);
    expect([closed.dataset.uniqueIndex, closed.dataset.collapsed, closed.style.left]).to.eql(['7', 'true', '3px']);
    expect([open.getAttribute('aria-expanded'), open.dataset.collapsed, open.textContent!.trim()]).to.eql([
      'true',
      'false',
      '−'
    ]);
  });

  describe('the events', () => {
    beforeEach(async () => {
      const labels = strip.labelsOf({ first: 3, last: 4, starts: [10, 60], sizes: [50, 50], frozen: 0, frozenStarts: [], frozenSizes: [] }, label);
      await draw(
        strip.stripOf(
          [
            strip.toggle({ uniqueIndex: 5, level: 1, startPx: 0, endPx: 10, buttonPx: 20, collapsed: true }, ''),
            html`<button class="test-toggle" data-unique-index="none"></button>`
          ],
          labels
        )
      );
    });

    it('select the column of a header cell clicked, with the keys held', () => {
      inStrip('.test-cell.col4').dispatchEvent(
        new MouseEvent('click', { bubbles: true, composed: true, shiftKey: true, metaKey: true })
      );

      expect(heard).to.eql([
        { type: 'test-click', detail: { col: 4, shiftKey: true, ctrlKey: false, metaKey: true, altKey: false } }
      ]);
    });

    it('open or close a group from its button, the click going no further', () => {
      let reached = 0;
      document.body.addEventListener('click', () => reached++, { once: true });

      inStrip('.test-toggle').click();
      inStrip('.test-toggle[data-unique-index="none"]').click();

      expect(heard).to.eql([{ type: 'test-toggled', detail: { uniqueIndex: 5, collapsed: false } }]);
      expect(reached).to.equal(0);
    });

    it('start a resize from a handle pressed, measured from where its column starts, taking the press', () => {
      const press = new MouseEvent('mousedown', { bubbles: true, composed: true, cancelable: true, clientX: 105, clientY: 7 });

      inStrip('.test-handle[data-col="3"]').dispatchEvent(press);

      expect(press.defaultPrevented).to.be.true;
      expect(heard).to.eql([
        {
          type: 'test-resize',
          detail: { col: 3, clientX: 105, clientY: 7, left: inStrip('.test-cell.col3').getBoundingClientRect().left }
        }
      ]);
    });

    it('fit a column to its content from a handle double-clicked', () => {
      const twice = new MouseEvent('dblclick', { bubbles: true, composed: true, cancelable: true });

      inStrip('.test-handle[data-col="4"]').dispatchEvent(twice);

      expect(twice.defaultPrevented).to.be.true;
      expect(heard).to.eql([{ type: 'test-autofit', detail: { col: 4 } }]);
    });

    it("ask for the sheet's menu on a right-click, leaving the browser's unless the sheet takes it", () => {
      const first = new MouseEvent('contextmenu', { bubbles: true, composed: true, cancelable: true, clientX: 30, clientY: 9 });
      inStrip('.test-cell.col3').dispatchEvent(first);
      take = true;
      const second = new MouseEvent('contextmenu', { bubbles: true, composed: true, cancelable: true });
      inStrip('.test-cell.col4').dispatchEvent(second);

      expect(heard.map((h) => h.detail)).to.eql([
        { col: 3, clientX: 30, clientY: 9 },
        { col: 4, clientX: 0, clientY: 0 }
      ]);
      expect([first.defaultPrevented, second.defaultPrevented]).to.eql([false, true]);
    });

    it('fire nothing for what is no header cell, handle or button', () => {
      const gap = inStrip('.gap');
      for (const type of ['click', 'mousedown', 'dblclick', 'contextmenu']) {
        gap.dispatchEvent(new MouseEvent(type, { bubbles: true, composed: true, cancelable: true }));
      }
      inStrip('.test-cell.col3').dispatchEvent(new MouseEvent('mousedown', { bubbles: true, composed: true }));
      inStrip('.test-handle').dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, composed: true }));

      expect(heard).to.eql([]);
    });
  });
});
