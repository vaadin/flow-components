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
  host.style.cssText = 'width:400px;height:300px;';
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

  it('leaves the formula field alone', () => {
    // Only showSelectedCell knows the formula; the address arriving must
    // not blank it.
    ctx.sheet.showSelectedCell('A1', 1, 1, '=SUM(B1:B9)', true, false, false);
    ctx.sheet.updateFormulaBar(null as any, 1, 1);

    expect(ctx.sheet.getFormulaBar().renderState.formula).to.equal('=SUM(B1:B9)');
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
