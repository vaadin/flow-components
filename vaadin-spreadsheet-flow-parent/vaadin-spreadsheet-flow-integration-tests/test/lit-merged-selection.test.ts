/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { expandToMergedRegions } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/MergedSelection.js';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * A merged region is one cell to the user, so a selection that touches any
 * part of one takes all of it — drag across half a merge in Excel and the
 * whole merge comes with it.
 *
 * Reporting the literal rectangle instead leaves the server writing to
 * cells nobody can see: selecting C3:D4 over a merge at B2:C3 has to become
 * B2:D4, and anything written to the selection belongs in all of it.
 */

// B2:C3 merged.
const MERGE_B2_C3 = [{ c1: 2, c2: 3, r1: 2, r2: 3 }].map((r) => ({
  col1: r.c1,
  col2: r.c2,
  row1: r.r1,
  row2: r.r2
}));

describe('expandToMergedRegions', () => {
  it('leaves a selection that touches no merge alone', () => {
    const range = { c1: 5, c2: 6, r1: 5, r2: 6 };
    expect(expandToMergedRegions(range, MERGE_B2_C3)).to.eql(range);
  });

  it('returns the selection untouched when the sheet has no merges', () => {
    const range = { c1: 1, c2: 2, r1: 1, r2: 2 };
    expect(expandToMergedRegions(range, [])).to.equal(range);
  });

  it('grows a selection that ends inside a merge', () => {
    // C3:D4 clips the merge's bottom-right corner.
    expect(expandToMergedRegions({ c1: 3, c2: 4, r1: 3, r2: 4 }, MERGE_B2_C3)).to.eql({
      c1: 2,
      c2: 4,
      r1: 2,
      r2: 4
    });
  });

  it('grows a selection that starts inside a merge', () => {
    expect(expandToMergedRegions({ c1: 1, c2: 2, r1: 1, r2: 2 }, MERGE_B2_C3)).to.eql({
      c1: 1,
      c2: 3,
      r1: 1,
      r2: 3
    });
  });

  it('takes the whole merge for a single cell inside it', () => {
    expect(expandToMergedRegions({ c1: 3, c2: 3, r1: 3, r2: 3 }, MERGE_B2_C3)).to.eql({
      c1: 2,
      c2: 3,
      r1: 2,
      r2: 3
    });
  });

  it('leaves a selection that already contains the merge alone', () => {
    const range = { c1: 1, c2: 5, r1: 1, r2: 5 };
    expect(expandToMergedRegions(range, MERGE_B2_C3)).to.eql(range);
  });

  it('keeps sweeping when taking one merge in reaches another', () => {
    // A1:B1 and C2:D2 do not touch each other, but growing to take in the
    // first brings the edge over the second.
    const regions = [
      { col1: 1, col2: 2, row1: 1, row2: 1 },
      { col1: 3, col2: 4, row1: 2, row2: 2 }
    ];
    expect(expandToMergedRegions({ c1: 2, c2: 3, r1: 1, r2: 2 }, regions)).to.eql({
      c1: 1,
      c2: 4,
      r1: 1,
      r2: 2
    });
  });
});

function attachSheet(): {
  sheet: Spreadsheet;
  sent: { type: string; data: Record<string, unknown> }[];
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.style.cssText = 'width:600px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));
  sheet.setMergedRegions([{ id: 1, col1: 2, col2: 3, row1: 2, row2: 3 }] as any);
  sheet.recomputeViewport();
  const sent: { type: string; data: Record<string, unknown> }[] = [];
  host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));
  return {
    sheet,
    sent,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

function cellClick(
  sheet: Spreadsheet,
  col: number,
  row: number,
  modifiers: { shiftKey?: boolean; ctrlKey?: boolean } = {}
): void {
  sheet.getCellGrid().dispatchEvent(
    new CustomEvent('cell-click', {
      detail: { col, row, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false, ...modifiers },
      bubbles: true,
      composed: true
    })
  );
}

describe('Spreadsheet — selecting across a merged region', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('grows a shift-click range that ends inside the merge', () => {
    cellClick(ctx.sheet, 4, 4);
    cellClick(ctx.sheet, 3, 3, { shiftKey: true });

    expect(ctx.sheet.getSelection().activeRange).to.eql({ c1: 2, c2: 4, r1: 2, r2: 4 });
  });

  it('reports the grown range to the server, not the literal one', () => {
    cellClick(ctx.sheet, 4, 4);
    ctx.sent.length = 0;
    cellClick(ctx.sheet, 3, 3, { shiftKey: true });

    const range = ctx.sent.find((e) => e.type === 'cellRangeSelected');
    expect(range, 'no range reported').to.not.be.undefined;
    expect(range!.data).to.eql({ row1: 2, col1: 2, row2: 4, col2: 4 });
  });

  it('grows a drag selection that crosses the merge', () => {
    cellClick(ctx.sheet, 4, 4);
    ctx.sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-mousedown', {
        detail: { col: 4, row: 4, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false },
        bubbles: true,
        composed: true
      })
    );
    ctx.sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-mouseover', { detail: { col: 3, row: 3 }, bubbles: true, composed: true })
    );

    expect(ctx.sheet.getSelection().activeRange).to.eql({ c1: 2, c2: 4, r1: 2, r2: 4 });
  });

  it('leaves a range that touches no merge as it is', () => {
    cellClick(ctx.sheet, 5, 5);
    cellClick(ctx.sheet, 7, 7, { shiftKey: true });

    expect(ctx.sheet.getSelection().activeRange).to.eql({ c1: 5, c2: 7, r1: 5, r2: 7 });
  });
});
