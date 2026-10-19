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
 * Reporting how wide a character is in each cell style.
 *
 * A General-format number shows as many digits as fit the column and falls
 * back to scientific notation when they do not. The server decides that and
 * needs a pixels-per-character ratio per style index, because it knows the
 * column width but not the font. The connector never sent them, so the
 * server assumed everything fits and no number was ever shortened.
 */

type Dispatched = { type: string; data: Record<string, unknown> };

function attachSheet(): {
  sheet: Spreadsheet;
  sent: Dispatched[];
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host, host.attachShadow({ mode: 'open' }));
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));

  const sent: Dispatched[] = [];
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

/** The connector measures after the grid has painted the style rules. */
async function settle(sheet: Spreadsheet): Promise<void> {
  sheet.recomputeViewport();
  await sheet.getCellGrid().updateComplete;
  await sheet.getCellGrid().updateComplete;
  await new Promise((resolve) => setTimeout(resolve, 0));
}

function ratioReport(sent: Dispatched[]): Dispatched | undefined {
  return sent.filter((e) => e.type === 'setCellStyleWidthRatios').at(-1);
}

describe('Spreadsheet — cell-style width ratios', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('reports a ratio for every style the server defined', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;', 1: 'font-size:20px;' });
    await settle(ctx.sheet);

    const report = ratioReport(ctx.sent);
    expect(report, 'no ratios reported').to.not.be.undefined;
    expect(report!.data.styleIndexes).to.eql([0, 1]);
    expect(report!.data.ratios).to.have.length(2);
  });

  it('measures a wider ratio for a larger font', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;', 1: 'font-size:30px;' });
    await settle(ctx.sheet);

    const ratios = ratioReport(ctx.sent)!.data.ratios as number[];
    expect(ratios[1]).to.be.greaterThan(ratios[0]);
  });

  it('reports a plausible pixels-per-character figure', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:16px;' });
    await settle(ctx.sheet);

    const ratios = ratioReport(ctx.sent)!.data.ratios as number[];
    // A digit is narrower than the font size and wider than a pixel.
    expect(ratios[0]).to.be.greaterThan(1);
    expect(ratios[0]).to.be.lessThan(16);
  });

  it('says nothing when the server defined no styles', async () => {
    ctx.sheet.setCellStyleToCSSStyle({});
    await settle(ctx.sheet);

    expect(ratioReport(ctx.sent)).to.be.undefined;
  });

  it('re-measures when the styles change', async () => {
    // The default style's size in the shape the server writes it: through
    // --default-font-size, which is what the cell grid's base rule reads.
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size: var(--default-font-size);--default-font-size:10px;' });
    await settle(ctx.sheet);
    const first = ratioReport(ctx.sent)!.data.ratios as number[];

    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size: var(--default-font-size);--default-font-size:30px;' });
    await settle(ctx.sheet);
    const second = ratioReport(ctx.sent)!.data.ratios as number[];

    expect(second[0]).to.be.greaterThan(first[0]);
  });

  it('reports the ratios before the viewport, so the server builds the first cells once', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);

    // The server builds the cells it sends against the ratios, and builds
    // the ones in view again when new ratios come: after the viewport,
    // they doubled the first screen of every workbook.
    const types = ctx.sent.map((e) => e.type);
    expect(types.indexOf('setCellStyleWidthRatios'), 'no ratios').to.be.at.least(0);
    expect(types.indexOf('setCellStyleWidthRatios')).to.be.lessThan(types.indexOf('onConnectorInit'));
    expect(types.indexOf('setCellStyleWidthRatios')).to.be.lessThan(types.indexOf('onSheetScroll'));
  });

  it('reports new ratios before the next viewport of a sheet already shown', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);
    ctx.sheet.setReload(true);
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:12px;', 1: 'font-size:20px;' });
    ctx.sent.length = 0;

    await settle(ctx.sheet);

    const types = ctx.sent.map((e) => e.type);
    expect(types.indexOf('setCellStyleWidthRatios'), 'no ratios').to.be.at.least(0);
    expect(types.indexOf('setCellStyleWidthRatios')).to.be.lessThan(types.indexOf('onSheetScroll'));
  });

  it('reports the ratios and the viewport in the same task, so one request carries both', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    ctx.sheet.recomputeViewport();
    // Microtasks only: Flow sends what was queued once the task is over.
    for (let i = 0; i < 10; i++) {
      await Promise.resolve();
    }

    const types = ctx.sent.map((e) => e.type);
    expect(types).to.include('setCellStyleWidthRatios');
    expect(types).to.include('onSheetScroll');
  });

  it('measures once for repaints that come before the grid has painted, and the styles they bring', async () => {
    // Style 2 has all along the size style 1 is given while the first
    // measurement waits for the paint.
    ctx.sheet.setCellStyleToCSSStyle({ 1: 'font-size:10px;', 2: 'font-size:30px;' });
    ctx.sheet.recomputeViewport();
    ctx.sheet.setCellStyleToCSSStyle({ 1: 'font-size:30px;', 2: 'font-size:30px;' });
    // Drawn as the sheet schedules it, and not recomputed before the paint.
    for (let i = 0; i < 3; i++) {
      await ctx.sheet.getCellGrid().updateComplete;
      await new Promise((resolve) => setTimeout(resolve, 0));
    }

    const reports = ctx.sent.filter((e) => e.type === 'setCellStyleWidthRatios');
    expect(reports).to.have.length(1);
    const [changed, constant] = reports[0].data.ratios as number[];
    expect(changed).to.equal(constant);
    const types = ctx.sent.map((e) => e.type);
    expect(types.indexOf('onConnectorInit'), 'the sheet introduced after the ratios').to.be.greaterThan(
      types.indexOf('setCellStyleWidthRatios')
    );
  });

  it('tells the server where the sheet is at once while no measurement waits', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);
    ctx.sent.length = 0;

    // Further down than the rows loaded so far, so there is a load to ask for.
    ctx.sheet.setRows(500);
    ctx.sheet.setRowH(new Array(500).fill(15));
    ctx.sheet.getScrollContainer().scrollTop = 4000;
    ctx.sheet.recomputeViewport();

    expect(ctx.sent.map((e) => e.type)).to.include('onSheetScroll');
  });

  it('reports the ratios again for a workbook loaded anew, its styles unchanged', async () => {
    // The server keeps the ratios with the workbook, and a reload or the same
    // styles in the next workbook bring no style change: only the toggle
    // says the workbook is a new one.
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;', 3: 'font-size:20px;' });
    await settle(ctx.sheet);
    const first = ratioReport(ctx.sent)!.data;
    ctx.sent.length = 0;

    ctx.sheet.setWorkbookChangeToggle(true);
    await settle(ctx.sheet);

    expect(ratioReport(ctx.sent)?.data).to.deep.equal(first);
  });

  it('reports the ratios again for every workbook loaded anew', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);
    ctx.sheet.setWorkbookChangeToggle(true);
    await settle(ctx.sheet);
    ctx.sent.length = 0;

    ctx.sheet.setWorkbookChangeToggle(false);
    await settle(ctx.sheet);

    expect(ctx.sent.filter((e) => e.type === 'setCellStyleWidthRatios')).to.have.length(1);
  });

  it('does not re-measure on a render that changed no style', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);
    const before = ctx.sent.filter((e) => e.type === 'setCellStyleWidthRatios').length;

    await settle(ctx.sheet);

    expect(ctx.sent.filter((e) => e.type === 'setCellStyleWidthRatios')).to.have.length(before);
  });

  it('also measures a style a cell wears that came with no rule of its own', async () => {
    // A style set on a cell straight through POI never reaches the style
    // map, and the server then judged every value in it as fitting: a date
    // too wide for its column was clipped instead of running on. The GWT
    // client measured the overflow itself; this client measures the style.
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    ctx.sheet.cellsUpdated([
      { row: 2, col: 3, value: '1999-03-19', cellStyle: 'cs1 r', locked: false, needsMeasure: false, isPercentage: false } as any,
    ]);
    await settle(ctx.sheet);

    const report = ratioReport(ctx.sent);
    expect(report!.data.styleIndexes).to.eql([0, 1]);
    // Unstyled, it measures as the default does.
    expect(report!.data.ratios[1]).to.equal(report!.data.ratios[0]);
  });

  it('measures again when a cell brings a style not yet measured', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);
    const before = ctx.sent.filter((e) => e.type === 'setCellStyleWidthRatios').length;

    ctx.sheet.cellsUpdated([
      { row: 1, col: 1, value: 'x', cellStyle: 'cs4', locked: false, needsMeasure: false, isPercentage: false } as any,
    ]);
    await settle(ctx.sheet);

    const reports = ctx.sent.filter((e) => e.type === 'setCellStyleWidthRatios');
    expect(reports).to.have.length(before + 1);
    expect(reports.at(-1)!.data.styleIndexes).to.eql([0, 4]);
  });

  it('leaves the sheet undisturbed — the probe is removed', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);

    const strays = ctx.sheet.getCellGrid().shadowRoot!.querySelectorAll('span.cell');
    expect(strays).to.have.length(0);
  });
});
