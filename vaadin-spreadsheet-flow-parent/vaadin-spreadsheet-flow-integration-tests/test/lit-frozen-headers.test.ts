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

/**
 * A freeze pane splits the sheet in four, and the three quadrants that
 * are not the frozen corner have to start where the frozen block ends.
 * Drawn from zero instead, the scrolling columns end up underneath the
 * frozen ones that are pinned there: the first columns after a freeze
 * cannot be seen at all, and a click aimed at one of them lands on a
 * frozen cell, which is somebody else's data.
 */
describe('Frozen panes — where the scrolling cells start', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function cell(col: number, row: number): HTMLElement | null {
    return ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>(`.cell.col${col}.row${row}`);
  }

  function cellLeft(col: number, row: number): number {
    return cell(col, row)!.getBoundingClientRect().left;
  }

  it('starts the scrolling columns past the frozen ones', async () => {
    // Two 50px columns frozen, so column 3 starts 100px in.
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    expect(cellLeft(3, 5) - cellLeft(1, 5)).to.equal(100);
  });

  it('starts the scrolling rows below the frozen ones', async () => {
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    const top = (col: number, row: number): number =>
      cell(col, row)!.getBoundingClientRect().top;
    // 15pt rows are 20px at 96 ppi, so two frozen rows are 40px.
    expect(top(1, 3) - top(1, 1)).to.equal(40);
  });

  it('lines the frozen rows up over the scrolling columns', async () => {
    // The top quadrant shares its column positions with the main one,
    // so a frozen row's cell sits directly above the scrolling cell in
    // the same column.
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    expect(cellLeft(4, 1)).to.equal(cellLeft(4, 5));
  });

  it('keeps them lined up once the sheet is scrolled', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.getScrollContainer().scrollLeft = 500;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    const first = ctx.sheet.getCellGrid().renderState.viewport
      .firstColumnIndex;
    expect(cellLeft(first, 1)).to.equal(cellLeft(first, 5));
    expect(cellLeft(first, 5)).to.be.at.least(cellLeft(1, 5) + 100);
  });

  it('heads the scrolling columns where they actually are', async () => {
    // The header reads the same positions, so a column's letter has to
    // stay over the column itself.
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;
    await ctx.sheet.getColumnHeader().updateComplete;

    const header = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.col-header-cell.col3')!;
    expect(Math.round(header.getBoundingClientRect().left)).to.equal(
      Math.round(cellLeft(3, 5))
    );
  });
});
