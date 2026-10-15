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
 * The sheet's row and column sizes are worked out once and kept until one of
 * them changes: adding up a million rows on every scroll step cost more than
 * the rest of the draw.
 */
describe('Spreadsheet — the sizes the sheet is drawn with', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(100);
    sheet.setCols(20);
    sheet.setColW(new Array(20).fill(50));
    sheet.setRowH(new Array(100).fill(15));
    sheet.recomputeViewport();
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  /** What the sizes were last worked out as. */
  function kept(): unknown {
    return (sheet as unknown as { sizesCache: { sizes: unknown } | null }).sizesCache?.sizes;
  }

  function height(): number {
    return parseFloat(sheet.getCellGrid().style.height);
  }

  function width(): number {
    return parseFloat(sheet.getCellGrid().style.width);
  }

  it('keeps them from one draw to the next', () => {
    const before = kept();

    sheet.getScrollContainer().scrollTop = 300;
    sheet.recomputeViewport();

    expect(kept()).to.equal(before);
  });

  const changes: Array<[string, (s: Spreadsheet) => void, (s: Spreadsheet) => void, () => number, number]> = [
    // 100 rows of 20px; one twice as tall adds 20.
    ['the row heights', () => {}, (s) => s.setRowH([30, ...new Array(99).fill(15)]), height, 2020],
    ['the column widths', () => {}, (s) => s.setColW([150, ...new Array(19).fill(50)]), width, 1100],
    ['the hidden rows', () => {}, (s) => s.setHiddenRowIndexes([1, 2]), height, 1960],
    ['the hidden columns', () => {}, (s) => s.setHiddenColumnIndexes([1]), width, 950],
    ['the number of rows', () => {}, (s) => s.setRows(50), height, 1000],
    ['the number of columns', () => {}, (s) => s.setCols(10), width, 500],
    // Past the ones the server sized, 10 more rows at the default height:
    // 2000 + 10 x 20 to begin with, then 10 x 40.
    ['the default row height', (s) => s.setRows(110), (s) => s.setDefRowH(30), height, 2400],
    ['the default column width', (s) => s.setCols(22), (s) => s.setDefColW(100), width, 1200]
  ];
  for (const [what, setUp, change, measure, expected] of changes) {
    it(`works them out again when ${what} change`, () => {
      setUp(sheet);
      sheet.recomputeViewport();
      const before = kept();

      change(sheet);
      sheet.recomputeViewport();

      expect(kept()).to.not.equal(before);
      expect(measure()).to.equal(expected);
    });
  }

  it('draws from past the hidden rows a scroll has gone by', () => {
    sheet.setRows(200);
    sheet.setRowH(new Array(200).fill(15));
    sheet.setHiddenRowIndexes(Array.from({ length: 50 }, (_, i) => i + 1));
    sheet.recomputeViewport();

    sheet.getScrollContainer().scrollTop = 1000;
    sheet.recomputeViewport();

    // 1000 less the 200 drawn ahead of the view is 800: 40 rows of 20px
    // past the 50 hidden ones.
    expect(sheet.getCellGrid().renderState.viewport.firstRowIndex).to.equal(91);
  });

  it('draws from past the hidden columns a scroll has gone by', () => {
    sheet.setHiddenColumnIndexes([1, 2, 3, 4, 5]);
    sheet.recomputeViewport();

    sheet.getScrollContainer().scrollLeft = 400;
    sheet.recomputeViewport();

    // 400 less the 200 drawn ahead is 200: 4 columns of 50px past the 5 hidden.
    expect(sheet.getCellGrid().renderState.viewport.firstColumnIndex).to.equal(10);
  });

  it('finds the rows to draw far down a sheet of a million rows', () => {
    sheet.setRows(1_048_576);
    sheet.setRowH(new Array(1_048_576).fill(15));
    sheet.recomputeViewport();

    sheet.getScrollContainer().scrollTop = 20_000_000;
    sheet.recomputeViewport();

    const viewport = sheet.getCellGrid().renderState.viewport;
    // 20px rows: the millionth row starts at 19,999,980.
    expect(viewport.firstRowIndex).to.be.at.most(1_000_000);
    expect(viewport.lastRowIndex).to.be.at.least(1_000_001);
  });
});
