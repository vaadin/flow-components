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

  it('does not re-measure on a render that changed no style', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);
    const before = ctx.sent.filter((e) => e.type === 'setCellStyleWidthRatios').length;

    await settle(ctx.sheet);

    expect(ctx.sent.filter((e) => e.type === 'setCellStyleWidthRatios')).to.have.length(before);
  });

  it('leaves the sheet undisturbed — the probe is removed', async () => {
    ctx.sheet.setCellStyleToCSSStyle({ 0: 'font-size:10px;' });
    await settle(ctx.sheet);

    const strays = ctx.sheet.getCellGrid().shadowRoot!.querySelectorAll('span.cell');
    expect(strays).to.have.length(0);
  });
});
