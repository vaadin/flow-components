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
 * Slice 184 — `setSelectedCellAndRange` applies server-driven selection.
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
    ctx.sheet.showSelectedCell('A1', 1, 1, '=SUM(B1:B9)', true, false, false);
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
    // frame (slice 183), so wait for the frame before Lit's update.
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
    ctx.sheet.updateFormulaBar(null as any, 2, 2);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('B2');
  });

  it('prefers the name the server sent', () => {
    ctx.sheet.updateFormulaBar('Sales', 2, 2);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('Sales');
  });

  it('works out a two-letter column', () => {
    ctx.sheet.updateFormulaBar(null as any, 28, 5);

    expect(ctx.sheet.getFormulaBar().renderState.address).to.equal('AB5');
  });

  it('shows the formula behind a computed cell', () => {
    // The bar shows what the cell is, not what it displays. The server
    // sends the formula without its leading =, the way POI stores it.
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: '42', formulaValue: 'SUM(B1:B9)', cellStyle: 'cs0' } as any
    ]);
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
    ctx.sheet.updateFormulaBar(null as any, 2, 4);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal("'abc");
  });

  it('falls back to the displayed value when there is nothing else', () => {
    ctx.sheet.updateBottomRightCellValues([
      { row: 5, col: 2, value: 'plain', cellStyle: 'cs0' } as any
    ]);
    ctx.sheet.updateFormulaBar(null as any, 2, 5);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('plain');
  });

  it('empties the field for a cell the server has sent nothing for', () => {
    ctx.sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: '42', formulaValue: 'SUM(B1:B9)', cellStyle: 'cs0' } as any
    ]);
    ctx.sheet.updateFormulaBar(null as any, 2, 2);
    ctx.sheet.updateFormulaBar(null as any, 9, 9);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('');
  });

  it('prefers the formula over the unformatted value', () => {
    ctx.sheet.updateBottomRightCellValues([
      {
        row: 2, col: 2, value: '42',
        formulaValue: 'SUM(B1:B9)', originalValue: '42', cellStyle: 'cs0'
      } as any
    ]);
    ctx.sheet.updateFormulaBar(null as any, 2, 2);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('=SUM(B1:B9)');
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
    expect([...select!.options].map((o) => o.textContent!.trim())).to.deep.equal([
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
    expect([...select!.options].map((o) => o.value)).to.deep.equal(['Sales']);
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
    ctx.sheet.showSelectedCell('B2', 2, 2, '=1+1', true, false, false);

    const bar = ctx.sheet.getFormulaBar();
    await bar.updateComplete;
    expect(bar.shadowRoot!.querySelector('[part~="address-field"]')).to.not.be.null;
    expect(bar.shadowRoot!.querySelector('[part~="formula-field"]')).to.not.be.null;
    expect(bar.shadowRoot!.querySelector('.namedrangebox')).to.not.be.null;
  });
});
