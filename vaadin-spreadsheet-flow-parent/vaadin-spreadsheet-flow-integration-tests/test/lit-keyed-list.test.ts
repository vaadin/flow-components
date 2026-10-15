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

import { keyedList } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/KeyedList.js';

/**
 * The keyed list the sheet renders its rows, cells and headers with: lit's
 * `repeat`, keyed the same way, but taking an item's markers with it when
 * the item leaves. Since lit-html 3.3.1 `repeat` leaves one comment behind
 * per item it drops (https://github.com/lit/lit/issues/5298).
 */
describe('keyedList', () => {
  let container: HTMLElement;

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    container.remove();
  });

  /** Render the list of numbers, each as a span keyed by itself, between two fixed elements. */
  function draw(items: number[], label = 'item'): void {
    render(
      html`<div id="list"><b>before</b>${keyedList(
        items,
        (item) => item,
        (item) => html`<span data-key=${item}>${label} ${item}</span>`
      )}<i>after</i></div>`,
      container
    );
  }

  function list(): HTMLElement {
    return container.querySelector('#list')!;
  }

  function spans(): HTMLElement[] {
    return Array.from(list().querySelectorAll('span'));
  }

  function keys(): string[] {
    return spans().map((span) => span.dataset.key!);
  }

  function comments(): number {
    return Array.from(list().childNodes).filter((node) => node.nodeType === Node.COMMENT_NODE).length;
  }

  it('renders the items in order, between what is around it', () => {
    draw([1, 2, 3]);

    expect(keys()).to.deep.equal(['1', '2', '3']);
    expect(list().firstElementChild!.tagName).to.equal('B');
    expect(list().lastElementChild!.tagName).to.equal('I');
  });

  it('keeps the element of an item that stays', () => {
    draw([1, 2, 3, 4, 5]);
    const three = spans()[2];

    draw([3, 4, 5, 6, 7]);

    expect(keys()).to.deep.equal(['3', '4', '5', '6', '7']);
    expect(spans()[0]).to.equal(three);
  });

  it('leaves no marker behind as items come and go', () => {
    draw([1, 2, 3, 4, 5]);
    const markers = comments();

    for (let i = 0; i < 10; i++) {
      draw([3, 4, 5, 6, 7]);
      draw([1, 2, 3, 4, 5]);
    }

    expect(comments()).to.equal(markers);
    expect(keys()).to.deep.equal(['1', '2', '3', '4', '5']);
  });

  it('leaves only its own marker once it is empty', () => {
    draw([]);
    const empty = comments();
    draw([1, 2, 3, 4, 5]);

    draw([]);

    expect(comments()).to.equal(empty);
    expect(spans()).to.be.empty;
  });

  it('moves the elements of items whose order changes', () => {
    draw([1, 2, 3, 4]);
    const [one, , , four] = spans();

    draw([4, 3, 2, 1]);

    expect(keys()).to.deep.equal(['4', '3', '2', '1']);
    expect(spans()[0]).to.equal(four);
    expect(spans()[3]).to.equal(one);
    expect(list().lastElementChild!.tagName).to.equal('I');
  });

  it('moves nothing that is already in its place', () => {
    draw([1, 2, 3]);
    let moved = 0;
    const observer = new MutationObserver((records) => {
      moved += records.filter((record) => record.type === 'childList').length;
    });
    observer.observe(list(), { childList: true });

    draw([1, 2, 3], 'row');
    observer.takeRecords().forEach(() => moved++);
    observer.disconnect();

    expect(moved).to.equal(0);
  });

  it('replaces what the expression held before it was a list', () => {
    const drawValue = (value: unknown): void => render(html`<div id="list">${value}</div>`, container);
    drawValue('plain text');

    drawValue(keyedList([1, 2], (item) => item, (item) => html`<span data-key=${item}></span>`));

    expect(list().textContent).to.not.include('plain text');
    expect(keys()).to.deep.equal(['1', '2']);
  });

  it('puts an item that comes in between others in its place', () => {
    draw([1, 3]);

    draw([1, 2, 3]);

    expect(keys()).to.deep.equal(['1', '2', '3']);
  });

  it('updates an item that stays in place', () => {
    draw([1, 2]);
    const two = spans()[1];

    draw([1, 2], 'row');

    expect(spans()[1]).to.equal(two);
    expect(two.textContent).to.equal('row 2');
  });

  it('renders after other content that goes and comes', () => {
    const drawWith = (head: boolean, items: number[]): void =>
      render(
        html`<div id="list">${head ? html`<b>head</b>` : ''}${keyedList(
          items,
          (item) => item,
          (item) => html`<span data-key=${item}></span>`
        )}</div>`,
        container
      );
    drawWith(true, [1, 2]);
    drawWith(false, [2, 3]);
    drawWith(true, [3, 4]);

    expect(keys()).to.deep.equal(['3', '4']);
    expect(list().firstElementChild!.tagName).to.equal('B');
  });
});
