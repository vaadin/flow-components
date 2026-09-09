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
