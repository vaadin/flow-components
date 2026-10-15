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

describe('expandToMergedRegions — against its definition', () => {
  /**
   * The definition: the smallest rectangle holding the selection and all
   * of every merge that overlaps it, found by taking merges in until none
   * is left half in.
   */
  function definition(
    selection: { c1: number; c2: number; r1: number; r2: number },
    regions: { col1: number; col2: number; row1: number; row2: number }[]
  ) {
    let { c1, c2, r1, r2 } = selection;
    for (let grew = true; grew; ) {
      grew = false;
      for (const m of regions) {
        const overlaps = m.col1 <= c2 && m.col2 >= c1 && m.row1 <= r2 && m.row2 >= r1;
        const inside = m.col1 >= c1 && m.col2 <= c2 && m.row1 >= r1 && m.row2 <= r2;
        if (overlaps && !inside) {
          c1 = Math.min(c1, m.col1);
          c2 = Math.max(c2, m.col2);
          r1 = Math.min(r1, m.row1);
          r2 = Math.max(r2, m.row2);
          grew = true;
        }
      }
    }
    return { c1, c2, r1, r2 };
  }

  /** A small deterministic generator, so a failure can be run again. */
  function random(seed: number): () => number {
    let x = seed;
    return () => {
      x = (x * 1103515245 + 12345) % 2147483648;
      return x / 2147483648;
    };
  }

  it('grows a long edge at a merge partway along it', () => {
    const regions = [{ col1: 10, col2: 11, row1: 50, row2: 52 }];
    expect(expandToMergedRegions({ c1: 1, c2: 10, r1: 1, r2: 1000 }, regions)).to.eql({
      c1: 1,
      c2: 11,
      r1: 1,
      r2: 1000
    });
  });

  it('leaves a merge just outside the selection alone', () => {
    const regions = [{ col1: 6, col2: 7, row1: 1, row2: 2 }];
    expect(expandToMergedRegions({ c1: 1, c2: 5, r1: 1, r2: 5 }, regions)).to.eql({ c1: 1, c2: 5, r1: 1, r2: 5 });
  });

  it('agrees with its definition on many sheets', () => {
    const next = random(7);
    const pick = (n: number) => 1 + Math.floor(next() * n);
    for (let sheet = 0; sheet < 400; sheet++) {
      // Merges never overlap one another: a workbook cannot hold two that do.
      const regions: { col1: number; col2: number; row1: number; row2: number }[] = [];
      for (let i = pick(12); i > 0; i--) {
        const col1 = pick(30);
        const row1 = pick(30);
        const m = { col1, col2: col1 + pick(4) - 1, row1, row2: row1 + pick(4) - 1 };
        const clashes = regions.some((o) => o.col1 <= m.col2 && o.col2 >= m.col1 && o.row1 <= m.row2 && o.row2 >= m.row1);
        if (!clashes) {
          regions.push(m);
        }
      }
      const c = pick(30);
      const r = pick(30);
      const selection = { c1: c, c2: c + pick(8) - 1, r1: r, r2: r + pick(8) - 1 };

      expect(expandToMergedRegions(selection, regions), JSON.stringify({ selection, regions })).to.eql(
        definition(selection, regions)
      );
    }
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
