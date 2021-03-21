import {VaadinSpreadsheet} from '../vaadin-spreadsheet.js';
import {fixture, html} from '@open-wc/testing';

const assert = chai.assert;

suite('vaadin-spreadsheet', () => {
  test('is defined', () => {
    const el = document.createElement('vaadin-spreadsheet');
    assert.instanceOf(el, VaadinSpreadsheet);
  });

  test('renders with default values', async () => {
    const el = await fixture(html`<vaadin-spreadsheet></vaadin-spreadsheet>`);
    assert.shadowDom.equal(
      el,
      `
      <h1>Hello, World!</h1>
      <button part="button">Click Count: 0</button>
      <slot></slot>
    `
    );
  });

  test('renders with a set name', async () => {
    const el = await fixture(html`<vaadin-spreadsheet name="Test"></vaadin-spreadsheet>`);
    assert.shadowDom.equal(
      el,
      `
      <h1>Hello, Test!</h1>
      <button part="button">Click Count: 0</button>
      <slot></slot>
    `
    );
  });

  test('handles a click', async () => {
    const el = await fixture(html`<vaadin-spreadsheet></vaadin-spreadsheet>`);
    const button = el.shadowRoot.querySelector('button');
    button.click();
    await el.updateComplete;
    assert.shadowDom.equal(
      el,
      `
      <h1>Hello, World!</h1>
      <button part="button">Click Count: 1</button>
      <slot></slot>
    `
    );
  });
});
