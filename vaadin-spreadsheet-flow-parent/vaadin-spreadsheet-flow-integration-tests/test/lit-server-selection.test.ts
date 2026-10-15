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
 * `setSelectedCellAndRange` applies server-driven selection.
 *
 * The server calls this after `Spreadsheet.setSelection` /
 * `setSelectionRange`, and after operations that move the selection on
 * the server (sheet change, row insert, undo). It was a no-op, so a
 * selection set from server code never reached the screen.
 *
 * The active cell is passed separately from the range corners because
 * it need not be a corner of the range — it cannot be derived.
 */

function attachSheet(): {
  sheet: Spreadsheet;
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  // Real geometry, so the scroll container actually overflows and a
  // scrollTop assignment sticks. 15pt rows are 20px at 96 ppi, so 400
  // rows are 8000px against a 300px viewport.
  sheet.setCols(60);
  sheet.setRows(400);
  sheet.setColW(new Array(60).fill(50));
  sheet.setRowH(new Array(400).fill(15));
  sheet.recomputeViewport();
  return {
    sheet,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

/**
 * The user clicks a cell: the client moves there at once, and the server's
 * answer, updateFormulaBar, names it afterwards. The server only ever
 * answers about the cell it has selected.
 */
function clickCell(sheet: Spreadsheet, col: number, row: number): void {
  sheet.getCellGrid().dispatchEvent(
    new CustomEvent('cell-click', {
      detail: { col, row, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false },
      bubbles: true,
      composed: true
    })
  );
}

describe('Spreadsheet server-driven selection', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('sets the active cell the server names', () => {
    ctx.sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);

    expect(ctx.sheet.getSelection().activeCell).to.deep.equal({
      col: 2,
      row: 2
    });
  });

  it('sets the range from the four corners', () => {
    ctx.sheet.setSelectedCellAndRange('A1', 1, 1, 1, 3, 1, 4, false);

    expect(ctx.sheet.getSelection().activeRange).to.deep.equal({
      c1: 1,
      r1: 1,
      c2: 3,
      r2: 4
    });
  });

  it('keeps an active cell that is not a corner of the range', () => {
    ctx.sheet.setSelectedCellAndRange('B2', 2, 2, 1, 3, 1, 3, false);

    const selection = ctx.sheet.getSelection();
    expect(selection.activeCell).to.deep.equal({ col: 2, row: 2 });
    expect(selection.activeRange).to.deep.equal({ c1: 1, r1: 1, c2: 3, r2: 3 });
  });

  it('normalizes corners given in reverse order', () => {
    ctx.sheet.setSelectedCellAndRange('C3', 3, 3, 3, 1, 3, 1, false);

    expect(ctx.sheet.getSelection().activeRange).to.deep.equal({
      c1: 1,
      r1: 1,
      c2: 3,
      r2: 3
    });
  });

  it('replaces a previous multi-range selection', () => {
    ctx.sheet.setSelectedCellAndRange('A1', 1, 1, 1, 2, 1, 2, false);
    ctx.sheet.setSelectedCellAndRange('D4', 4, 4, 4, 4, 4, 4, false);

    const selection = ctx.sheet.getSelection();
    expect(selection.activeCell).to.deep.equal({ col: 4, row: 4 });
    expect(selection.activeRange).to.deep.equal({ c1: 4, r1: 4, c2: 4, r2: 4 });
    expect(selection.auxRanges).to.have.length(0);
  });

  it('shows the address the server sent in the formula bar', () => {
    ctx.sheet.setSelectedCellAndRange('Sheet1!B7', 2, 7, 2, 2, 7, 7, false);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal(
      'Sheet1!B7'
    );
  });

  it('leaves the formula field alone when only the address changes', () => {
    // The server sends a formula the way POI stores it, without the
    // leading '='; the client is what puts it back.
    ctx.sheet.showSelectedCell('A1', 1, 1, 'SUM(B1:B9)', true, false, false);
    ctx.sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal(
      '=SUM(B1:B9)'
    );
  });
});

describe('Spreadsheet server-driven selection — scrolling', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('scrolls a far-off cell into view when the server asks', () => {
    ctx.sheet.setSelectedCellAndRange('A200', 1, 200, 1, 1, 200, 200, true);

    expect(ctx.sheet.getScrollContainer().scrollTop).to.be.greaterThan(0);
  });

  it('does not scroll when the server does not ask', () => {
    ctx.sheet.setSelectedCellAndRange('A200', 1, 200, 1, 1, 200, 200, false);

    expect(ctx.sheet.getScrollContainer().scrollTop).to.equal(0);
  });

  it('stays put when the target cell is already visible', () => {
    ctx.sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, true);

    expect(ctx.sheet.getScrollContainer().scrollTop).to.equal(0);
    expect(ctx.sheet.getScrollContainer().scrollLeft).to.equal(0);
  });
});

describe('Spreadsheet selection overlay — reports where the selection is', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function overlay(): Promise<Element | null> {
    // setSelectedCellAndRange coalesces its repaint into an animation
    // frame, so wait for the frame before Lit's update.
    await new Promise<void>((resolve) => {
      requestAnimationFrame(() => requestAnimationFrame(() => resolve()));
    });
    const grid = ctx.sheet.getCellGrid();
    await grid.updateComplete;
    return grid.shadowRoot!.querySelector('.sheet-selection');
  }

  it('carries the active cell coordinates', async () => {
    ctx.sheet.setSelectedCellAndRange('B5', 2, 5, 2, 2, 5, 5, false);

    const el = await overlay();
    expect(el).to.not.be.null;
    expect(el!.getAttribute('data-col')).to.equal('2');
    expect(el!.getAttribute('data-row')).to.equal('5');
  });

  it('reports the active cell, not a corner of the range', async () => {
    // Active cell deliberately in the middle of the range.
    ctx.sheet.setSelectedCellAndRange('B2', 2, 2, 1, 3, 1, 3, false);

    const el = await overlay();
    expect(el!.getAttribute('data-col')).to.equal('2');
    expect(el!.getAttribute('data-row')).to.equal('2');
  });

  it('follows the selection when it moves', async () => {
    ctx.sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);
    await overlay();
    ctx.sheet.setSelectedCellAndRange('C7', 3, 7, 3, 3, 7, 7, false);

    const el = await overlay();
    expect(el!.getAttribute('data-col')).to.equal('3');
    expect(el!.getAttribute('data-row')).to.equal('7');
  });
});

describe('showSelectedCell — the server moving the selection', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('moves the selection to the cell the server names', () => {
    // Sent from the server's address-change path — jumping by address
    // and following an internal hyperlink. Updating only the formula
    // bar left the highlight on the cell the user clicked while the
    // address field named the one the server had moved to.
    ctx.sheet.showSelectedCell('B6', 2, 6, '', false, false, false);

    expect(ctx.sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 6 });
  });

  it('collapses a range to that one cell', () => {
    ctx.sheet.setSelectedCellAndRange('A1', 1, 1, 1, 4, 1, 4, false);
    ctx.sheet.showSelectedCell('B6', 2, 6, '', false, false, false);

    expect(ctx.sheet.getSelection().activeRange).to.deep.equal({
      c1: 2, r1: 6, c2: 2, r2: 6
    });
  });

  it('scrolls a cell below the fold into view', () => {
    // Following an internal hyperlink from row 30 to row 10 moved the
    // selection but left the viewport where it was, so the user was
    // looking at a sheet with nothing selected on it.
    ctx.sheet.getScrollContainer().scrollTop = 4000;
    ctx.sheet.showSelectedCell('B10', 2, 10, '', false, false, false);

    expect(ctx.sheet.getScrollContainer().scrollTop).to.be.at.most(180);
  });

  it('scrolls a cell to the right of the fold into view', () => {
    ctx.sheet.showSelectedCell('AZ4', 52, 4, '', false, false, false);

    expect(ctx.sheet.getScrollContainer().scrollLeft).to.be.greaterThan(0);
  });

  it('leaves the view alone for the selection a sheet is shown with again', () => {
    // The sheet has just gone back to where it was scrolled, and the
    // selection it was left with, out of view, leaves it there.
    ctx.sheet.getScrollContainer().scrollTop = 4000;
    ctx.sheet.showSelectedCell('B10', 2, 10, '', false, false, true, true);

    expect(ctx.sheet.getScrollContainer().scrollTop).to.equal(4000);
    expect(ctx.sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 10 });
  });

  it('leaves the viewport alone for a cell already in view', () => {
    ctx.sheet.getScrollContainer().scrollTop = 400;
    ctx.sheet.showSelectedCell('B22', 2, 22, '', false, false, false);

    expect(ctx.sheet.getScrollContainer().scrollTop).to.equal(400);
  });

  it('puts the leading = back on a formula', () => {
    ctx.sheet.showSelectedCell('C3', 3, 3, 'A1+B2', true, false, false);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('=A1+B2');
  });

  it('leaves a plain value alone', () => {
    ctx.sheet.showSelectedCell('C3', 3, 3, "'text", false, false, false);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal("'text");
  });

  it('shows an empty formula cell as empty, not as a bare =', () => {
    ctx.sheet.showSelectedCell('C3', 3, 3, '', true, false, false);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('');
  });

  it('works out the address when the server sends no name', () => {
    ctx.sheet.showSelectedCell(null as any, 2, 6, '', false, false, false);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('B6');
  });

  it('repaints so the highlight follows', async () => {
    ctx.sheet.showSelectedCell('B6', 2, 6, '', false, false, false);
    await Promise.resolve();
    await ctx.sheet.getCellGrid().updateComplete;

    const overlay = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector('.sheet-selection')!;
    expect(overlay.getAttribute('data-col')).to.equal('2');
    expect(overlay.getAttribute('data-row')).to.equal('6');
  });
});

describe('Formula bar — the address field', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('shows the address the client works out when the server sends none', () => {
    // The server only names a selection that has a name — a named range,
    // or a sheet-qualified reference. For an ordinary cell it sends null
    // and leaves the address to the client, which is every selection a
    // user makes by clicking.
    clickCell(ctx.sheet, 2, 2);
    ctx.sheet.updateFormulaBar(null as any, 2, 2);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('B2');
  });

  it('prefers the name the server sent', () => {
    clickCell(ctx.sheet, 2, 2);
    ctx.sheet.updateFormulaBar('Sales', 2, 2);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('Sales');
  });

  it('works out a two-letter column', () => {
    clickCell(ctx.sheet, 28, 5);
    ctx.sheet.updateFormulaBar(null as any, 28, 5);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('AB5');
  });

  it('shows the formula behind a computed cell', () => {
    // The bar shows what the cell is, not what it displays. The server
    // sends the formula without its leading =, the way POI stores it.
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: '42', formulaValue: 'SUM(B1:B9)', cellStyle: 'cs0' } as any
    ]);
    clickCell(ctx.sheet, 2, 2);
    ctx.sheet.updateFormulaBar(null as any, 2, 2);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('=SUM(B1:B9)');
  });

  it('shows the unformatted value of a plain cell', () => {
    // originalValue keeps a text cell's leading apostrophe and a
    // number's full precision, both of which the displayed value has
    // already dropped.
    ctx.sheet.updateBottomRightCellValues([
      { row: 4, col: 2, value: 'abc', originalValue: "'abc", cellStyle: 'cs0' } as any
    ]);
    clickCell(ctx.sheet, 2, 4);
    ctx.sheet.updateFormulaBar(null as any, 2, 4);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal("'abc");
  });

  it('falls back to the displayed value when there is nothing else', () => {
    ctx.sheet.updateBottomRightCellValues([
      { row: 5, col: 2, value: 'plain', cellStyle: 'cs0' } as any
    ]);
    clickCell(ctx.sheet, 2, 5);
    ctx.sheet.updateFormulaBar(null as any, 2, 5);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('plain');
  });

  it('empties the field for a cell the server has sent nothing for', () => {
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: '42', formulaValue: 'SUM(B1:B9)', cellStyle: 'cs0' } as any
    ]);
    clickCell(ctx.sheet, 2, 2);
    ctx.sheet.updateFormulaBar(null as any, 2, 2);
    clickCell(ctx.sheet, 9, 9);
    ctx.sheet.updateFormulaBar(null as any, 9, 9);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('');
  });

  it('shows the new active cell when the server moves the selection, as the GWT client did', () => {
    // An address typed into the address field, or a named range picked
    // there, comes back this way; the field kept the old cell's value.
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: '42', formulaValue: 'SUM(B1:B9)', cellStyle: 'cs0' } as any,
      { row: 5, col: 3, value: 'hello', cellStyle: 'cs0' } as any
    ]);
    clickCell(ctx.sheet, 2, 2);
    ctx.sheet.updateFormulaBar(null as any, 2, 2);
    ctx.sheet.setSelectedCellAndRange('C5', 3, 5, 3, 3, 5, 5, true);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('hello');
  });

  it('leaves what is being typed when the server moves the selection mid-edit', () => {
    ctx.sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { key: 'x', bubbles: true, composed: true, cancelable: true }));
    ctx.sheet.setSelectedCellAndRange('C5', 3, 5, 3, 3, 5, 5, false);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('x');
  });

  it('keeps what the server sent for the same cell when it names its range', () => {
    // A cell this client has no data for yet: the server sends its value
    // with showSelectedCell, then the range it is in.
    ctx.sheet.showSelectedCell('C5', 3, 5, 'far away', false, false, false);
    ctx.sheet.setSelectedCellAndRange('Sales', 3, 5, 3, 4, 5, 6, false);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('far away');
  });

  it('prefers the formula over the unformatted value', () => {
    ctx.sheet.updateBottomRightCellValues([
      {
        row: 2, col: 2, value: '42',
        formulaValue: 'SUM(B1:B9)', originalValue: '42', cellStyle: 'cs0'
      } as any
    ]);
    clickCell(ctx.sheet, 2, 2);
    ctx.sheet.updateFormulaBar(null as any, 2, 2);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('=SUM(B1:B9)');
  });
});

describe('Formula bar — what the server says while the user moves on or writes', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: 'two', originalValue: 'two', cellStyle: 'cs0' } as any,
      { row: 3, col: 2, value: 'three', originalValue: 'three', cellStyle: 'cs0' } as any,
      { row: 5, col: 3, value: 'hello', originalValue: 'hello', cellStyle: 'cs0' } as any
    ]);
    clickCell(ctx.sheet, 2, 2);
    ctx.sheet.updateFormulaBar(null, 2, 2);
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function bar() {
    return ctx.sheet.getFormulaBar();
  }

  function key(target: EventTarget, k: string): void {
    target.dispatchEvent(new KeyboardEvent('keydown', { key: k, bubbles: true, composed: true, cancelable: true }));
  }

  async function formulaField(): Promise<HTMLInputElement> {
    await bar().updateComplete;
    return bar().shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
  }

  /** Types into the formula field as the user does: it has the keyboard, and an input event follows. */
  async function typeInField(value: string): Promise<HTMLInputElement> {
    const field = await formulaField();
    field.focus();
    field.value = value;
    field.setSelectionRange(value.length, value.length);
    field.dispatchEvent(new Event('input', { bubbles: true, composed: true }));
    return field;
  }

  /** Types into the cell editor, which the formula field mirrors. */
  function typeInEditor(value: string): void {
    key(ctx.sheet.getScrollContainer(), value[0]);
    ctx.sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-editor-input', { detail: { value, caret: value.length }, bubbles: true, composed: true })
    );
  }

  it('drops an answer about a cell the keyboard has already left, as the GWT client did', () => {
    key(ctx.sheet.getScrollContainer(), 'ArrowDown');
    // The answer about B2 the server sent before it heard of the move.
    ctx.sheet.updateFormulaBar('Old', 2, 2);

    expect(bar().renderState.address).to.equal('B3');

    ctx.sheet.updateFormulaBar(null, 2, 3);

    expect([bar().renderState.address, bar().renderState.formula]).to.eql(['B3', 'three']);
  });

  it('keeps what is typed into the cell editor when the server names the cell it is on', () => {
    typeInEditor('xyz');

    ctx.sheet.updateFormulaBar(null, 2, 2);
    ctx.sheet.showSelectedCell(null, 2, 2, 'two', false, false, false);

    expect(bar().renderState.formula).to.equal('xyz');
  });

  it('keeps what is typed into the formula field when the server names the cell it is on', async () => {
    const field = await typeInField('typed');
    // The cell changes underneath, and the server names it again.
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: 'TWO', originalValue: 'TWO', cellStyle: 'cs0' } as any
    ]);

    ctx.sheet.updateFormulaBar(null, 2, 2);
    await bar().updateComplete;
    const afterAnswer = field.value;
    ctx.sheet.showSelectedCell(null, 2, 2, 'TWO!', false, false, false);
    await bar().updateComplete;

    expect([afterAnswer, field.value]).to.eql(['typed', 'typed']);
  });

  it('shows a cell changed underneath a formula field with nothing typed in it', async () => {
    // Left as it was, the old value would go back into the cell when the
    // field is left.
    const field = await formulaField();
    field.focus();
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: 'TWO', originalValue: 'TWO', cellStyle: 'cs0' } as any
    ]);

    ctx.sheet.updateFormulaBar(null, 2, 2);
    await bar().updateComplete;

    expect(field.value).to.equal('TWO');
  });

  it('keeps the colours of a formula being written when the server names the cell it is on', () => {
    typeInEditor('=A1+C3');
    expect(bar().renderState.highlights?.references).to.have.length(2);

    ctx.sheet.updateFormulaBar(null, 2, 2);
    ctx.sheet.showSelectedCell(null, 2, 2, 'two', false, false, false);
    ctx.sheet.setSelectedCellAndRange(null as any, 2, 2, 2, 2, 2, 2, false);

    expect(bar().renderState.highlights?.references).to.have.length(2);
  });

  it('keeps the colours of a formula the field shows when the server names its cell again', async () => {
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: '1', formulaValue: 'A1', cellStyle: 'cs0' } as any
    ]);
    ctx.sheet.updateFormulaBar(null, 2, 2);
    (await formulaField()).focus();
    expect(bar().renderState.highlights?.references).to.have.length(1);

    ctx.sheet.updateFormulaBar(null, 2, 2);

    expect(bar().renderState.highlights?.references).to.have.length(1);
  });

  it('keeps the named ranges when the server names a cell', () => {
    ctx.sheet.setNamedRanges(['john']);

    ctx.sheet.updateFormulaBar(null, 2, 2);
    ctx.sheet.showSelectedCell(null, 3, 5, 'hello', false, false, false);

    expect(bar().renderState.namedRanges).to.eql(['john']);
  });

  it('shows the cell the server moves the selection to, over a formula typed in the field', async () => {
    // The field commits into the selected cell; the text written for B2
    // would otherwise go into C5.
    await typeInField('=A1');
    expect(bar().renderState.highlights).to.not.equal(null);

    ctx.sheet.setSelectedCellAndRange('C5', 3, 5, 3, 3, 5, 5, false);
    await bar().updateComplete;

    expect((await formulaField()).value).to.equal('hello');
    expect(bar().renderState.highlights ?? null).to.equal(null);
  });

  it('shows the cell showSelectedCell moves the selection to, over a formula typed in the field', async () => {
    await typeInField('=A1');

    ctx.sheet.showSelectedCell('C5', 3, 5, 'hello', false, false, false);
    await bar().updateComplete;

    expect((await formulaField()).value).to.equal('hello');
    expect(bar().renderState.highlights ?? null).to.equal(null);
  });
});

describe('Formula bar — whether the formula field is being written in', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('is written in only once typed into while it has the keyboard', async () => {
    const bar = ctx.sheet.getFormulaBar();
    await bar.updateComplete;
    const field = bar.shadowRoot!.querySelector<HTMLInputElement>('.formula')!;

    field.focus();
    const focused = bar.formulaFieldEdited();
    field.value = 'a';
    field.dispatchEvent(new Event('input', { bubbles: true, composed: true }));
    const typed = bar.formulaFieldEdited();
    field.blur();
    const left = bar.formulaFieldEdited();
    field.focus();
    const again = bar.formulaFieldEdited();

    expect([focused, typed, left, again]).to.eql([false, true, false, false]);
  });

  it('is written in once a reference is pointed into it', async () => {
    const bar = ctx.sheet.getFormulaBar();
    await bar.updateComplete;
    const field = bar.shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
    field.focus();

    bar.setFormulaFieldValue('=A1', 3);

    expect(bar.formulaFieldEdited()).to.equal(true);
  });

  it('is not written in when the sheet puts a value there', async () => {
    const bar = ctx.sheet.getFormulaBar();
    await bar.updateComplete;
    const field = bar.shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
    field.focus();

    bar.renderState = { ...bar.renderState, formula: 'from the sheet' };
    await bar.updateComplete;

    expect(bar.formulaFieldEdited()).to.equal(false);
  });
});

describe('Formula bar — jumping by address', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('gives the keyboard back to the sheet', () => {
    // Typing an address is a way of moving around the sheet. Leaving
    // the focus in the address field means the arrow keys that
    // naturally follow move the caret through the text just typed.
    ctx.sheet.getFormulaBar().dispatchEvent(
      new CustomEvent('sheet-address-changed', {
        detail: { value: 'B1:C2' },
        bubbles: true,
        composed: true
      })
    );

    expect(ctx.sheet.getScrollContainer().contains(document.activeElement)).to.be
      .true;
  });

  it('still tells the server where to go', () => {
    const events: any[] = [];
    const listener = (e: Event) => events.push((e as CustomEvent).detail);
    document.addEventListener('spreadsheet-event', listener, true);
    try {
      ctx.sheet.getFormulaBar().dispatchEvent(
        new CustomEvent('sheet-address-changed', {
          detail: { value: 'B1:C2' },
          bubbles: true,
          composed: true
        })
      );
    } finally {
      document.removeEventListener('spreadsheet-event', listener, true);
    }

    const jump = events.find((e) => e.type === 'sheetAddressChanged');
    expect(jump).to.not.be.undefined;
    expect(jump.data).to.deep.equal({ value: 'B1:C2' });
  });

  it('names an unnamed server selection by its active cell', () => {
    // Same as updateFormulaBar: the server names a selection only when
    // it has one, and leaves an ordinary one to the client.
    ctx.sheet.setSelectedCellAndRange(null as any, 2, 1, 2, 3, 1, 2, false);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('B1');
  });
});

describe('Formula bar — dragging out a range', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function drag(from: [number, number], to: [number, number]): void {
    const grid = ctx.sheet.getCellGrid();
    grid.dispatchEvent(
      new CustomEvent('cell-mousedown', {
        detail: {
          col: from[0], row: from[1],
          shiftKey: false, ctrlKey: false, metaKey: false, altKey: false
        },
        bubbles: true,
        composed: true
      })
    );
    grid.dispatchEvent(
      new CustomEvent('cell-mouseover', {
        detail: { col: to[0], row: to[1] },
        bubbles: true,
        composed: true
      })
    );
  }

  it('counts the range instead of naming a cell', () => {
    // Excel shows the size while the range is being dragged out; the
    // address comes back when the selection is finished.
    drag([8, 10], [9, 10]);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('1R x 2C');
  });

  it('keeps counting as the drag grows', () => {
    drag([8, 10], [10, 12]);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('3R x 3C');
  });

  it('counts a drag that runs backwards', () => {
    drag([10, 12], [8, 10]);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('3R x 3C');
  });

  it('shows 1R x 1C while the drag is still on one cell', () => {
    drag([8, 10], [8, 10]);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('1R x 1C');
  });

  it('says nothing on a hover with no button down', () => {
    // The grid emits cell-mouseover on every hover.
    clickCell(ctx.sheet, 2, 2);
    ctx.sheet.updateFormulaBar(null as any, 2, 2);
    ctx.sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-mouseover', {
        detail: { col: 9, row: 9 },
        bubbles: true,
        composed: true
      })
    );

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('B2');
  });

  it('gives the address back when the server answers the selection', () => {
    drag([8, 10], [9, 10]);
    ctx.sheet.updateFormulaBar(null as any, 8, 10);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('H10');
  });

  it('leaves the formula field alone while dragging', () => {
    ctx.sheet.updateBottomRightCellValues([
      { row: 10, col: 8, value: 'x', originalValue: 'x', cellStyle: 'cs0' } as any
    ]);
    clickCell(ctx.sheet, 8, 10);
    ctx.sheet.updateFormulaBar(null as any, 8, 10);
    drag([8, 10], [9, 10]);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('x');
  });
});

describe('Formula bar — named ranges', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function box(): Promise<HTMLSelectElement | null> {
    const bar = ctx.sheet.getFormulaBar();
    await bar.updateComplete;
    return bar.shadowRoot!.querySelector<HTMLSelectElement>('.namedrangebox');
  }

  it('offers one option per named range the workbook defines', async () => {
    ctx.sheet.setNamedRanges(['Sales', 'Costs']);

    const select = await box();
    expect(select).to.not.be.null;
    // The blank first entry is the picker's "nothing picked" state.
    expect([...select!.options].map((o) => o.textContent!.trim())).to.deep.equal([
      '',
      'Sales',
      'Costs'
    ]);
  });

  it('shows no dropdown when the workbook defines none', async () => {
    ctx.sheet.setNamedRanges([]);

    expect(await box()).to.be.null;
  });

  it('accepts the JSON-serialized list the server sends', async () => {
    ctx.sheet.setNamedRanges(JSON.stringify(['Sales']) as any);

    const select = await box();
    expect([...select!.options].map((o) => o.value)).to.deep.equal(['', 'Sales']);
  });

  it('navigates to the range that is picked', async () => {
    ctx.sheet.setNamedRanges(['Sales', 'Costs']);
    const select = await box();
    const received: any[] = [];
    ctx.sheet.getFormulaBar().addEventListener('sheet-address-changed', (e) =>
      received.push((e as CustomEvent).detail)
    );

    select!.value = 'Costs';
    select!.dispatchEvent(new Event('change', { bubbles: true, composed: true }));

    expect(received).to.deep.equal([{ value: 'Costs' }]);
  });

  it('keeps the address and formula fields alongside it', async () => {
    ctx.sheet.setNamedRanges(['Sales']);
    ctx.sheet.showSelectedCell('B2', 2, 2, '1+1', true, false, false);

    const bar = ctx.sheet.getFormulaBar();
    await bar.updateComplete;
    expect(bar.shadowRoot!.querySelector('[part~="address-field"]')).to.not.be.null;
    expect(bar.shadowRoot!.querySelector('[part~="formula-field"]')).to.not.be.null;
    expect(bar.shadowRoot!.querySelector('.namedrangebox')).to.not.be.null;
  });
});
