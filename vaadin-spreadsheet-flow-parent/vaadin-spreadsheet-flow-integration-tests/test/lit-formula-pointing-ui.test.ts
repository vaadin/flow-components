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

/**
 * Writing a formula by pointing at cells, as the GWT client let the user:
 * after an operator the arrows and the mouse put references into the
 * formula, and the formula's references are coloured on the sheet and in
 * the formula field. The Lit client had none of it - an arrow committed
 * "=SUM(" and a click took the selection away.
 */
describe('Spreadsheet — pointing at cells while writing a formula', () => {
  let sheet: Spreadsheet;
  let host: HTMLElement;
  let events: { type: string; data: Record<string, unknown> }[];

  beforeEach(async () => {
    host = document.createElement('div');
    host.style.cssText = 'width:600px;height:400px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(30);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(30).fill(15));
    sheet.setSheetNames(['Sheet1']);
    sheet.setSheetIndex(1);
    sheet.setSelectedCellAndRange('C3', 3, 3, 3, 3, 3, 3, false);
    sheet.recomputeViewport();
    events = [];
    host.addEventListener('spreadsheet-event', (e: Event) => events.push((e as CustomEvent).detail));
    await sheet.getCellGrid().updateComplete;
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  const grid = () => sheet.getCellGrid().shadowRoot!;
  const editor = () => grid().querySelector<HTMLInputElement>('.cell-editor')!;

  async function startTyping(text: string): Promise<HTMLInputElement> {
    sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { key: text[0], bubbles: true, composed: true, cancelable: true }));
    await sheet.getCellGrid().updateComplete;
    const input = editor();
    if (text.length > 1) {
      input.value = text;
      input.setSelectionRange(text.length, text.length);
      input.dispatchEvent(new Event('input', { bubbles: true, composed: true }));
    }
    return input;
  }

  function press(input: HTMLElement, key: string, shiftKey = false): KeyboardEvent {
    const e = new KeyboardEvent('keydown', { key, shiftKey, bubbles: true, composed: true, cancelable: true });
    input.dispatchEvent(e);
    input.dispatchEvent(new KeyboardEvent('keyup', { key, shiftKey, bubbles: true, composed: true }));
    return e;
  }

  function cell(col: number, row: number): HTMLElement {
    return grid().querySelector<HTMLElement>(`.cell.col${col}.row${row}`)!;
  }

  function mouse(type: string, target: HTMLElement, init: MouseEventInit = {}): MouseEvent {
    const e = new MouseEvent(type, { bubbles: true, composed: true, cancelable: true, ...init });
    target.dispatchEvent(e);
    return e;
  }

  it('drops the % a percentage cell gave a typed + once a cell is pointed at', async () => {
    // A + typed into a percentage cell opens on "+%", ready for a number.
    // Pointing made it "+C4%", which takes a hundredth of C4: a GWT bug.
    sheet.updateBottomRightCellValues([
      { col: 3, row: 3, value: '5%', isPercentage: true, cellStyle: 'cs0', locked: false, needsMeasure: false }
    ] as never);
    const input = await startTyping('+');
    expect(input.value).to.equal('+%');

    press(input, 'ArrowDown');

    expect(input.value).to.equal('+C4');
  });

  it('drops the % a percentage cell gave a typed + when the edit carries on in the formula field', async () => {
    sheet.updateBottomRightCellValues([
      { col: 3, row: 3, value: '5%', isPercentage: true, cellStyle: 'cs0', locked: false, needsMeasure: false }
    ] as never);
    const input = await startTyping('+');
    input.focus();
    await sheet.getFormulaBar().updateComplete;
    const field = sheet.getFormulaBar().shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
    field.focus();
    field.setSelectionRange(1, 1);

    press(field, 'ArrowDown');

    expect([editor() !== null, field.value]).to.eql([true, '+C4']);
  });

  it('keeps the % of a formula written in the field once a percentage entry is over', async () => {
    // The % a percentage cell gave a typed + went with that entry; one
    // typed into a formula afterwards is the formula's own.
    sheet.updateBottomRightCellValues([
      { col: 3, row: 3, value: '5%', isPercentage: true, cellStyle: 'cs0', locked: false, needsMeasure: false }
    ] as never);
    const input = await startTyping('+');
    press(input, 'Escape');
    await sheet.getFormulaBar().updateComplete;
    const field = sheet.getFormulaBar().shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
    field.focus();
    field.value = '=SUM()%';
    field.setSelectionRange(5, 5);
    field.dispatchEvent(new Event('input', { bubbles: true, composed: true }));

    press(field, 'ArrowDown');

    expect(field.value).to.equal('=SUM(C4)%');
  });

  it('keeps a % typed after the first cell pointed at in a percentage entry', async () => {
    sheet.updateBottomRightCellValues([
      { col: 3, row: 3, value: '5%', isPercentage: true, cellStyle: 'cs0', locked: false, needsMeasure: false }
    ] as never);
    const input = await startTyping('+');
    press(input, 'ArrowDown');
    // "+C4", then "*%" typed, the caret put back between them.
    input.value = '+C4*%';
    input.setSelectionRange(4, 4);
    input.dispatchEvent(new Event('input', { bubbles: true, composed: true }));

    // On from the cell pointed at last.
    press(input, 'ArrowDown');

    expect(input.value).to.equal('+C4*C5%');
  });

  it('keeps the % for a number typed into a percentage cell', async () => {
    sheet.updateBottomRightCellValues([
      { col: 3, row: 3, value: '5%', isPercentage: true, cellStyle: 'cs0', locked: false, needsMeasure: false }
    ] as never);

    const input = await startTyping('7');

    expect(input.value).to.equal('7%');
  });

  it('puts a reference to the next cell over into the formula on an arrow after =', async () => {
    const input = await startTyping('=');

    expect(press(input, 'ArrowDown').defaultPrevented).to.be.true;

    expect(input.value).to.equal('=C4');
    expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 3 });
  });

  it('moves the reference on with further arrows', async () => {
    const input = await startTyping('=SUM(');
    press(input, 'ArrowDown');
    press(input, 'ArrowDown');
    press(input, 'ArrowRight');

    expect(input.value).to.equal('=SUM(D5');
  });

  it('stretches the reference into a range with Shift', async () => {
    const input = await startTyping('=SUM(');
    press(input, 'ArrowDown');
    press(input, 'ArrowDown', true);
    press(input, 'ArrowRight', true);

    expect(input.value).to.equal('=SUM(C4:D5');
  });

  it('goes on from the cell pointed at last after an operator is typed, as the GWT client did', async () => {
    const input = await startTyping('=');
    press(input, 'ArrowDown');
    press(input, 'ArrowDown');
    input.value = '=C5+';
    input.setSelectionRange(4, 4);
    input.dispatchEvent(new Event('input', { bubbles: true, composed: true }));
    press(input, 'ArrowLeft');

    expect(input.value).to.equal('=C5+B5');
  });

  it('starts from the edited cell again in the next edit', async () => {
    let input = await startTyping('=');
    press(input, 'ArrowDown');
    press(input, 'Escape');
    await sheet.getCellGrid().updateComplete;
    input = await startTyping('=');
    press(input, 'ArrowDown');

    expect(input.value).to.equal('=C4');
  });

  it('neither points nor commits on an arrow after a reference, as the GWT client did', async () => {
    const input = await startTyping('=A1');

    expect(press(input, 'ArrowDown').defaultPrevented).to.be.false;

    expect(input.value).to.equal('=A1');
    expect(editor()).to.not.be.null;
    expect(events.filter((e) => e.type === 'cellValueEdited')).to.be.empty;
  });

  it('still commits a plain value on an arrow while typing over the cell', async () => {
    const input = await startTyping('42');
    press(input, 'ArrowDown');

    expect(events.filter((e) => e.type === 'cellValueEdited')).to.have.length(1);
  });

  it('puts a reference to a clicked cell into the formula, keeping the editor and the selection', async () => {
    const input = await startTyping('=');

    const down = mouse('mousedown', cell(5, 7));
    mouse('mouseup', cell(5, 7));
    mouse('click', cell(5, 7));

    expect(down.defaultPrevented, 'the press must not take the keyboard from the editor').to.be.true;
    expect(input.value).to.equal('=E7');
    expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 3 });
    expect(events.filter((e) => e.type === 'cellSelected')).to.be.empty;
  });

  it('selects with the click after the one a pointing press ends in, once the formula is written', async () => {
    const input = await startTyping('=');
    mouse('mousedown', cell(5, 7));
    mouse('mouseup', cell(5, 7));
    mouse('click', cell(5, 7));
    press(input, 'Enter');
    await sheet.getCellGrid().updateComplete;

    events.length = 0;
    mouse('mousedown', cell(6, 9));
    mouse('mouseup', cell(6, 9));
    mouse('click', cell(6, 9));

    expect(sheet.getSelection().activeCell).to.eql({ col: 6, row: 9 });
    expect(events.filter((e) => e.type === 'cellSelected').map((e) => e.data)).to.eql([
      { row: 9, column: 6, oldSelectionRangeDiscarded: true }
    ]);
  });

  it('stretches the clicked reference into a range by dragging', async () => {
    const input = await startTyping('=SUM(');
    mouse('mousedown', cell(5, 7));
    mouse('mouseover', cell(6, 9));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

    expect(input.value).to.equal('=SUM(E7:F9');
  });

  it('stops stretching the reference once the button is up', async () => {
    const input = await startTyping('=SUM(');
    mouse('mousedown', cell(5, 7));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    mouse('mouseover', cell(6, 9));

    expect(input.value).to.equal('=SUM(E7');
  });

  it('replaces the clicked reference with the next click', async () => {
    const input = await startTyping('=');
    mouse('mousedown', cell(5, 7));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    mouse('mousedown', cell(2, 2));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

    expect(input.value).to.equal('=B2');
  });

  it('adds another reference after a comma with Ctrl or Cmd', async () => {
    const input = await startTyping('=SUM(');
    mouse('mousedown', cell(5, 7));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    mouse('mousedown', cell(2, 2), { ctrlKey: true });
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

    expect(input.value).to.equal('=SUM(E7,B2');
  });

  it('replaces the reference the caret is right after, which the GWT client appended to', async () => {
    const input = await startTyping('=A1');
    mouse('mousedown', cell(2, 2));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

    expect(input.value).to.equal('=B2');
  });

  it('commits the formula written by pointing on Enter', async () => {
    const input = await startTyping('=SUM(');
    press(input, 'ArrowDown');
    input.value = input.value + ')';
    input.dispatchEvent(new Event('input', { bubbles: true, composed: true }));
    press(input, 'Enter');

    const edits = events.filter((e) => e.type === 'cellValueEdited').map((e) => e.data);
    expect(edits).to.eql([{ row: 3, col: 3, value: '=SUM(C4)' }]);
  });

  it('colours the references on the sheet, the one at the caret ringed', async () => {
    const input = await startTyping('=A1+B2:C4');
    input.setSelectionRange(2, 2);
    input.dispatchEvent(new KeyboardEvent('keyup', { key: 'ArrowLeft', bubbles: true, composed: true }));
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const marks = Array.from(grid().querySelectorAll<HTMLElement>('.formula-reference'));
    expect(marks).to.have.length(2);
    expect(marks[0].style.background).to.contain('rgba(48, 144, 240, 0.25)');
    expect(marks[0].style.boxShadow).to.contain('rgba(48, 144, 240, 0.75)');
    expect(marks[1].style.background).to.contain('rgba(236, 100, 100, 0.25)');
    expect(marks[1].style.boxShadow).to.equal('');
    // B2:C4 covers two columns and three rows of 50px by 20px cells.
    expect(marks[1].getBoundingClientRect().width).to.equal(100);
    expect(marks[1].getBoundingClientRect().height).to.equal(60);
  });

  it('colours the references in the formula field too', async () => {
    await startTyping('=A1+B2');
    await sheet.getFormulaBar().updateComplete;

    const spans = Array.from(
      sheet.getFormulaBar().shadowRoot!.querySelectorAll<HTMLElement>('.formula-highlights > span')
    );
    expect(spans.map((s) => s.textContent)).to.eql(['=', 'A1', '+', 'B2']);
    expect(spans[1].style.backgroundColor).to.equal('rgba(48, 144, 240, 0.25)');
    expect(spans[3].style.backgroundColor).to.equal('rgba(236, 100, 100, 0.25)');
  });

  it('writes the formula into the field as text, never as markup', async () => {
    await startTyping('="<b>bold</b>"&A1');
    await sheet.getFormulaBar().updateComplete;

    const layer = sheet.getFormulaBar().shadowRoot!.querySelector('.formula-highlights')!;
    expect(layer.querySelector('b')).to.be.null;
    expect(layer.textContent).to.equal('="<b>bold</b>"&A1');
  });

  it('takes the colours away once the formula is written', async () => {
    const input = await startTyping('=A1+B2');
    press(input, 'Enter');
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    await sheet.getFormulaBar().updateComplete;

    expect(grid().querySelectorAll('.formula-reference')).to.have.length(0);
    expect(sheet.getFormulaBar().shadowRoot!.querySelector('.formula-highlights')).to.be.null;
  });

  it('takes the colours away when the formula is thrown away', async () => {
    const input = await startTyping('=A1+B2');
    press(input, 'Escape');
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(grid().querySelectorAll('.formula-reference')).to.have.length(0);
  });

  it('points from the formula field as well', async () => {
    await sheet.getFormulaBar().updateComplete;
    const field = sheet.getFormulaBar().shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
    field.focus();
    field.value = '=';
    field.setSelectionRange(1, 1);
    field.dispatchEvent(new Event('input', { bubbles: true, composed: true }));

    const e = press(field, 'ArrowRight');

    expect(e.defaultPrevented).to.be.true;
    expect(field.value).to.equal('=D3');
  });
});
