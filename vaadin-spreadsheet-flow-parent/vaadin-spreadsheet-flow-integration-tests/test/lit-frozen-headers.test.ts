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
 * Headers of frozen rows and columns.
 *
 * The cell-grid pins frozen cells in their own quadrants, but the header
 * strips only ever drew the scrolling viewport. Scroll a sheet with two
 * frozen columns and the columns stayed put while the letters naming
 * them slid away — and once column 1's header is gone, nothing can
 * select that column.
 */

function attachSheet(): { sheet: Spreadsheet; cleanup: () => void } {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(60);
  sheet.setRows(400);
  sheet.setColW(new Array(60).fill(50));
  sheet.setRowH(new Array(400).fill(15));
  sheet.setHeaderRowHeight(20);
  sheet.setHeaderColumnWidth(40);
  return {
    sheet,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

function colHeaders(sheet: Spreadsheet): string[] {
  return [
    ...sheet
      .getColumnHeader()
      .shadowRoot!.querySelectorAll<HTMLElement>('.col-header-cell')
  ].map((e) => e.dataset.col!);
}

function rowHeaders(sheet: Spreadsheet): string[] {
  return [
    ...sheet
      .getRowHeader()
      .shadowRoot!.querySelectorAll<HTMLElement>('.row-header-cell')
  ].map((e) => e.dataset.row!);
}

describe('Column header — frozen columns', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('keeps the frozen columns headed once the sheet is scrolled past them', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.getScrollContainer().scrollLeft = 1000;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    expect(colHeaders(ctx.sheet)).to.include.members(['1', '2']);
  });

  it('draws them in a strip that cancels the scroll out', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.getScrollContainer().scrollLeft = 1000;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    const strip = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip')!;
    expect(strip).to.not.be.null;
    expect(strip.style.transform).to.equal(
      `translateX(${ctx.sheet.getScrollContainer().scrollLeft}px)`
    );
  });

  it('puts the frozen headers in the strip, not the scrolling row', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    const strip = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip')!;
    const inStrip = [...strip.querySelectorAll<HTMLElement>('.col-header-cell')].map(
      (e) => e.dataset.col
    );
    expect(inStrip).to.deep.equal(['1', '2']);
  });

  it('draws no strip when nothing is frozen', async () => {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    expect(
      ctx.sheet.getColumnHeader().shadowRoot!.querySelector('.frozen-header-strip')
    ).to.be.null;
  });

  it('gives the frozen headers their resize handles too', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    const strip = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip')!;
    expect(strip.querySelectorAll('.col-resize-handle')).to.have.length(2);
  });
});

describe('Row header — frozen rows', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('keeps the frozen rows numbered once the sheet is scrolled past them', async () => {
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.getScrollContainer().scrollTop = 2000;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getRowHeader().updateComplete;

    expect(rowHeaders(ctx.sheet)).to.include.members(['1', '2']);
  });

  it('draws them in a strip that cancels the scroll out', async () => {
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.getScrollContainer().scrollTop = 2000;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getRowHeader().updateComplete;

    const strip = ctx.sheet
      .getRowHeader()
      .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip')!;
    expect(strip).to.not.be.null;
    expect(strip.style.transform).to.equal(
      `translateY(${ctx.sheet.getScrollContainer().scrollTop}px)`
    );
  });

  it('draws no strip when nothing is frozen', async () => {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getRowHeader().updateComplete;

    expect(
      ctx.sheet.getRowHeader().shadowRoot!.querySelector('.frozen-header-strip')
    ).to.be.null;
  });
});
