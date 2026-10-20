/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import { rendered } from './spreadsheet-harness.js';

/**
 * The formula field and the cell editor are one edit.
 *
 * What is typed into the cell shows in the formula field as it is typed, and
 * Escape puts the cell's own value back there. Moving to the formula field
 * in the middle of an edit carries it on there: nothing is committed, the
 * cell editor stays, and what is typed in the formula field shows in it.
 * Enter or Tab there commits, Escape throws the edit away, and going back to
 * the cell editor carries on there.
 */

interface Ctx {
  host: HTMLElement;
  sheet: Spreadsheet;
  shadow: ShadowRoot;
  sent: Array<{ type: string; data: Record<string, unknown> }>;
  cleanup: () => void;
}

function attach(): Ctx {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const shadow = host.attachShadow({ mode: 'open' });
  const sheet = new Spreadsheet(host, shadow);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));
  sheet.recomputeViewport();
  const sent: Ctx['sent'] = [];
  host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));
  return {
    host,
    sheet,
    shadow,
    sent,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

function cellEditor(sheet: Spreadsheet): HTMLInputElement | null {
  return sheet.getCellGrid().shadowRoot!.querySelector<HTMLInputElement>('.cell-editor');
}

function formulaField(sheet: Spreadsheet): HTMLInputElement {
  return sheet.getFormulaBar().shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
}

function edits(ctx: Ctx): Array<Record<string, unknown>> {
  return ctx.sent.filter((e) => e.type === 'cellValueEdited').map((e) => e.data);
}

/** Type into a field the way a user does: its value changes, and it says so. */
function typeInto(input: HTMLInputElement, value: string): void {
  input.value = value;
  input.dispatchEvent(new InputEvent('input', { bubbles: true, composed: true }));
}

function press(input: HTMLInputElement, key: string): void {
  input.dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, composed: true, cancelable: true }));
}

async function settled(sheet: Spreadsheet): Promise<void> {
  await sheet.getCellGrid().updateComplete;
  await sheet.getFormulaBar().updateComplete;
}

/** Select a cell with a value of its own, from the server. */
async function select(ctx: Ctx, col: number, row: number, value?: string): Promise<void> {
  if (value !== undefined) {
    ctx.sheet.updateBottomRightCellValues([{ row, col, value, cellStyle: 'cs0' } as never]);
    ctx.sheet.recomputeViewport();
  }
  ctx.sheet.setSelectedCellAndRange('', col, row, col, col, row, row, false);
  await settled(ctx.sheet);
}

/** Start typing over the selected cell, as Excel's "enter" mode does. */
async function typeOver(ctx: Ctx, text: string): Promise<HTMLInputElement> {
  ctx.sheet.getScrollContainer().focus();
  ctx.sheet
    .getScrollContainer()
    .dispatchEvent(new KeyboardEvent('keydown', { key: text[0], bubbles: true, composed: true }));
  await settled(ctx.sheet);
  const input = cellEditor(ctx.sheet)!;
  if (text.length > 1) {
    typeInto(input, text);
    await settled(ctx.sheet);
  }
  return input;
}

describe('Spreadsheet — the formula field follows the cell editor', () => {
  let ctx: Ctx;

  beforeEach(() => {
    ctx = attach();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('shows the key that started the edit', async () => {
    await select(ctx, 2, 2, 'old');

    await typeOver(ctx, 'x');

    expect(formulaField(ctx.sheet).value).to.equal('x');
  });

  it('shows what is typed into the cell as it is typed', async () => {
    await select(ctx, 2, 2, 'old');

    await typeOver(ctx, 'xyz');

    expect(formulaField(ctx.sheet).value).to.equal('xyz');
  });

  it("puts the cell's own value back on Escape", async () => {
    await select(ctx, 2, 2, 'old');
    const input = await typeOver(ctx, 'xyz');

    press(input, 'Escape');
    await settled(ctx.sheet);

    expect(formulaField(ctx.sheet).value).to.equal('old');
  });
});

describe('Spreadsheet — an edit carried on in the formula field', () => {
  let ctx: Ctx;

  beforeEach(() => {
    ctx = attach();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  /** Type into the cell, then take the edit to the formula field. */
  async function carriedOver(text = 'xyz'): Promise<{ input: HTMLInputElement; field: HTMLInputElement }> {
    await select(ctx, 2, 2, 'old');
    const input = await typeOver(ctx, text);
    input.focus();
    const field = formulaField(ctx.sheet);
    field.focus();
    await settled(ctx.sheet);
    return { input, field };
  }

  it('commits nothing and keeps the cell editor', async () => {
    const { input } = await carriedOver();

    expect(edits(ctx)).to.be.empty;
    expect(cellEditor(ctx.sheet)).to.equal(input);
    expect(input.value).to.equal('xyz');
  });

  it('keeps the keyboard in the formula field when the sheet draws again', async () => {
    const { field } = await carriedOver();

    ctx.sheet.recomputeViewport();
    await settled(ctx.sheet);

    expect(ctx.sheet.getFormulaBar().shadowRoot!.activeElement).to.equal(field);
  });

  it('shows what is typed into the formula field in the cell editor', async () => {
    const { input, field } = await carriedOver('x');
    const before = input.getBoundingClientRect().width;

    typeInto(field, 'a much longer value than the cell');

    expect(input.value).to.equal('a much longer value than the cell');
    expect(input.getBoundingClientRect().width, 'and widens it').to.be.greaterThan(before);
  });

  it('commits the formula field with Enter, closes the cell editor and moves down', async () => {
    const { field } = await carriedOver();
    typeInto(field, '=1+2');

    press(field, 'Enter');
    await settled(ctx.sheet);

    expect(edits(ctx)).to.deep.equal([{ row: 2, col: 2, value: '=1+2' }]);
    expect(cellEditor(ctx.sheet)).to.be.null;
    expect(ctx.sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 3 });
  });

  it('throws the edit away with Escape and puts the cell value back', async () => {
    const { field } = await carriedOver();

    press(field, 'Escape');
    await settled(ctx.sheet);

    expect(edits(ctx)).to.be.empty;
    expect(cellEditor(ctx.sheet)).to.be.null;
    expect(formulaField(ctx.sheet).value).to.equal('old');
  });

  it('carries on in the cell editor when it is clicked again', async () => {
    const { input, field } = await carriedOver();
    typeInto(field, 'xyzw');

    input.focus();
    await settled(ctx.sheet);
    expect(edits(ctx), 'nothing committed going back').to.be.empty;
    expect(cellEditor(ctx.sheet)).to.equal(input);

    press(input, 'Enter');
    await settled(ctx.sheet);
    expect(edits(ctx)).to.deep.equal([{ row: 2, col: 2, value: 'xyzw' }]);
  });

  it('commits what it holds when it is left for somewhere else', async () => {
    const { field } = await carriedOver();
    typeInto(field, 'xyzw');

    ctx.sheet.getScrollContainer().focus();
    await settled(ctx.sheet);

    expect(edits(ctx)).to.deep.equal([{ row: 2, col: 2, value: 'xyzw' }]);
    expect(cellEditor(ctx.sheet)).to.be.null;
  });

  it('ends the edit when the address field is clicked instead, as leaving for anywhere else does', async () => {
    await select(ctx, 2, 2, 'old');
    const input = await typeOver(ctx, 'xyz');
    input.focus();

    ctx.sheet.getFormulaBar().shadowRoot!.querySelector<HTMLInputElement>('.address')!.focus();
    // The edit ends once the keyboard has settled there.
    await new Promise((resolve) => setTimeout(resolve, 10));
    await settled(ctx.sheet);

    expect(edits(ctx)).to.deep.equal([{ row: 2, col: 2, value: 'xyz' }]);
    expect(cellEditor(ctx.sheet)).to.be.null;
  });

  it('still commits the cell editor when it is left for somewhere other than the formula field', async () => {
    await select(ctx, 2, 2, 'old');
    const input = await typeOver(ctx, 'xyz');
    input.focus();

    ctx.sheet.getScrollContainer().focus();
    await settled(ctx.sheet);

    expect(edits(ctx)).to.deep.equal([{ row: 2, col: 2, value: 'xyz' }]);
    expect(cellEditor(ctx.sheet)).to.be.null;
  });
});

/**
 * What the cell shows once an edit is committed, before the server answers:
 * typed text goes in the cell at once, but a formula keeps showing the old
 * value until the server has worked it out - from the cell editor and from
 * the formula field alike.
 */
describe('Spreadsheet — the cell once an edit is committed', () => {
  let ctx: Ctx;

  beforeEach(() => {
    ctx = attach();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function shown(col: number, row: number): Promise<string> {
    ctx.sheet.recomputeViewport();
    await settled(ctx.sheet);
    return ctx.sheet.getCellGrid().shadowRoot!.querySelector(`.cell.col${col}.row${row}`)!.textContent!.trim();
  }

  it('shows text typed into the cell at once', async () => {
    await select(ctx, 2, 2, 'old');
    const input = await typeOver(ctx, 'new');

    press(input, 'Enter');

    expect(await shown(2, 2)).to.equal('new');
  });

  it('keeps the old value while the server works a formula out', async () => {
    await select(ctx, 2, 2, 'old');
    const input = await typeOver(ctx, '=1+2');

    press(input, 'Enter');

    expect(await shown(2, 2)).to.equal('old');
  });

  it('keeps a value committed in the cell, without moving on, in the formula field', async () => {
    await select(ctx, 2, 2, 'old');
    const input = await typeOver(ctx, 'new');

    input.dispatchEvent(new FocusEvent('blur'));
    await rendered(ctx.sheet);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('new');
  });

  it('keeps a value committed in the formula field, without moving on, there', async () => {
    await select(ctx, 2, 2, 'old');
    const field = formulaField(ctx.sheet);
    field.focus();
    typeInto(field, 'new');

    field.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Tab', shiftKey: true, bubbles: true, composed: true, cancelable: true })
    );
    await rendered(ctx.sheet);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('new');
  });

  it('shows text committed from the formula field at once', async () => {
    await select(ctx, 2, 2, 'old');
    const field = formulaField(ctx.sheet);
    field.focus();
    typeInto(field, 'new');

    press(field, 'Enter');

    expect(await shown(2, 2)).to.equal('new');
  });

  /** What the cell shows once the sheet has drawn, with no repaint asked for. */
  async function drawn(col: number, row: number): Promise<string> {
    await settled(ctx.sheet);
    await new Promise((resolve) => setTimeout(resolve, 0));
    await settled(ctx.sheet);
    return ctx.sheet.getCellGrid().shadowRoot!.querySelector(`.cell.col${col}.row${row}`)!.textContent!.trim();
  }

  it('shows text committed by leaving the formula field at once', async () => {
    await select(ctx, 2, 2, 'old');
    const field = formulaField(ctx.sheet);
    field.focus();
    typeInto(field, 'new');

    field.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));

    expect(await drawn(2, 2)).to.equal('new');
  });

  it('shows text committed with shift+Tab in the formula field at once', async () => {
    await select(ctx, 2, 2, 'old');
    const field = formulaField(ctx.sheet);
    field.focus();
    typeInto(field, 'new');

    field.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Tab', shiftKey: true, bubbles: true, composed: true, cancelable: true })
    );

    expect(await drawn(2, 2)).to.equal('new');
  });
});
