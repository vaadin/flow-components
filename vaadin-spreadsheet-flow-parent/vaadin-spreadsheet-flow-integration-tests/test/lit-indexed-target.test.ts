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
  cellTarget,
  indexedTarget
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/IndexedTarget.js';

describe('indexedTarget — the indexed element an event came from', () => {
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement('div');
    document.body.appendChild(root);
  });

  afterEach(() => {
    root.remove();
  });

  /** The event that reached `root` from `target`. */
  function eventFrom(target: EventTarget): Event {
    let seen: Event | null = null;
    root.addEventListener('probe', (e) => (seen = e), { once: true });
    target.dispatchEvent(new Event('probe', { bubbles: true }));
    return seen!;
  }

  function cell(col: string | null): { cell: HTMLElement; inner: HTMLElement } {
    const el = document.createElement('div');
    el.className = 'header-cell';
    if (col !== null) {
      el.dataset.col = col;
    }
    const inner = document.createElement('span');
    el.appendChild(inner);
    root.appendChild(el);
    return { cell: el, inner };
  }

  it('finds the nearest matching element and the index it holds', () => {
    const { cell: el, inner } = cell('3');

    const at = indexedTarget(eventFrom(inner), '.header-cell', 'col');

    expect(at?.element).to.equal(el);
    expect(at?.index).to.equal(3);
  });

  it('finds nothing outside a matching element', () => {
    cell('3');
    const other = document.createElement('div');
    root.appendChild(other);

    expect(indexedTarget(eventFrom(other), '.header-cell', 'col')).to.equal(null);
  });

  it('finds nothing for an event from no element', () => {
    const text = document.createTextNode('x');
    root.appendChild(text);

    expect(indexedTarget(eventFrom(text), '.header-cell', 'col')).to.equal(null);
  });

  for (const [what, value] of [
    ['index 0', '0'],
    ['a negative index', '-2'],
    ['an index that is no number', 'x'],
    ['an endless index', 'Infinity'],
    ['no index at all', null]
  ] as const) {
    it(`finds nothing for ${what}`, () => {
      const { inner } = cell(value);

      expect(indexedTarget(eventFrom(inner), '.header-cell', 'col')).to.equal(null);
    });
  }
});

describe('cellTarget — the cell an event came from', () => {
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement('div');
    document.body.appendChild(root);
  });

  afterEach(() => {
    root.remove();
  });

  /** The event that reached `root` from `target`. */
  function eventFrom(target: EventTarget): Event {
    let seen: Event | null = null;
    root.addEventListener('probe', (e) => (seen = e), { once: true });
    target.dispatchEvent(new Event('probe', { bubbles: true }));
    return seen!;
  }

  /** A cell holding a marker, at the column and row given. */
  function cellAt(col: string | null, row: string | null): { cell: HTMLElement; marker: HTMLElement } {
    const el = document.createElement('div');
    el.className = 'cell';
    if (col !== null) {
      el.dataset.col = col;
    }
    if (row !== null) {
      el.dataset.row = row;
    }
    const marker = document.createElement('div');
    el.appendChild(marker);
    root.appendChild(el);
    return { cell: el, marker };
  }

  it('finds the cell from something inside it, with its column and row', () => {
    const { cell, marker } = cellAt('3', '7');

    expect(cellTarget(eventFrom(marker))).to.eql({ cell, col: 3, row: 7 });
  });

  it('finds nothing outside a cell, nor for an event from no element', () => {
    cellAt('3', '7');
    const text = document.createTextNode('x');
    root.appendChild(text);

    expect(cellTarget(eventFrom(root))).to.equal(null);
    expect(cellTarget(eventFrom(text))).to.equal(null);
  });

  for (const [what, col, row] of [
    ['column 0', '0', '7'],
    ['row 0', '3', '0'],
    ['a row that is no number', '3', 'x'],
    ['an endless row', '3', 'Infinity'],
    ['no row at all', '3', null],
    ['no column at all', null, '7']
  ] as const) {
    it(`finds nothing for a cell at ${what}`, () => {
      const { marker } = cellAt(col, row);

      expect(cellTarget(eventFrom(marker))).to.equal(null);
    });
  }
});
