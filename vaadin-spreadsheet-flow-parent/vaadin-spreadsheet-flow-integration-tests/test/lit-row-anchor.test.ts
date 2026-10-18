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
 * Rows above the screen changing height, as the server makes rows of
 * components taller when it loads the components and lets them shrink when
 * it lets go of them: the first row with its top on screen stays where it
 * is. Moved, the rows on screen were others than those loaded, and loading
 * those moved them on again, a row each round trip.
 */
describe('Spreadsheet — rows above the screen changing height', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let loads: { firstRow: number; lastRow: number }[];

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    loads = [];
    host.addEventListener('spreadsheet-event', (e) => {
      const detail = (e as CustomEvent).detail;
      if (detail.type === 'onSheetScroll') {
        loads.push(detail.data);
      }
    });
    sheet = new Spreadsheet(host);
    sheet.setRows(1000);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    // 15 pt rows are 20 px.
    sheet.setRowH(heights({}));
    sheet.notifyStateChanges(['rows', 'cols', 'colW', 'rowH'], true);
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  /** Every row 15 pt high, but those given. */
  function heights(changed: Record<number, number>): number[] {
    const all = new Array(1000).fill(15);
    for (const [row, points] of Object.entries(changed)) {
      all[Number(row) - 1] = points;
    }
    return all;
  }

  function scrollTo(top: number): void {
    sheet.getScrollContainer().scrollTop = top;
    sheet.recomputeViewport();
  }

  /** As the server sends them. */
  function serverSends(rowH: number[]): void {
    sheet.setRowH(rowH);
    sheet.notifyStateChanges(['rowH'], false);
  }

  function scrollTop(): number {
    return sheet.getScrollContainer().scrollTop;
  }

  it('keeps the first row on screen where it is when rows above it grow', () => {
    // Row 101 at the top.
    scrollTo(2000);

    // Rows 50-52 from 20 to 40 px.
    serverSends(heights({ 50: 30, 51: 30, 52: 30 }));

    expect(scrollTop()).to.equal(2060);
  });

  it('keeps it where it is when rows above it shrink', () => {
    scrollTo(2000);

    serverSends(heights({ 50: 7.5, 51: 7.5, 52: 7.5 }));

    expect(scrollTop()).to.equal(1970);
  });

  it('keeps a row whose top is below the top of the screen as far below it', () => {
    // Row 101 is cut 10 px short; row 102 starts 10 px down.
    scrollTo(2010);

    serverSends(heights({ 50: 30, 51: 30, 52: 30 }));

    expect(scrollTop()).to.equal(2070);
  });

  it('keeps the row of the first of two changes that come before the sheet is drawn again', () => {
    scrollTo(2000);

    sheet.setRowH(heights({ 50: 30, 51: 30, 52: 30 }));
    sheet.setRowH(heights({ 50: 30, 51: 30, 52: 30, 60: 30 }));
    sheet.notifyStateChanges(['rowH'], false);

    expect(scrollTop()).to.equal(2080);
  });

  it('keeps it where it is at the bottom of the sheet, past where the sheet could scroll before', () => {
    const scroller = sheet.getScrollContainer();
    scrollTo(scroller.scrollHeight - scroller.clientHeight);
    const bottom = scrollTop();

    serverSends(heights({ 50: 30, 51: 30, 52: 30 }));

    expect(scrollTop()).to.equal(bottom + 60);
  });

  it('keeps no row in place once the user has scrolled on', () => {
    scrollTo(2000);

    sheet.setRowH(heights({ 50: 30, 51: 30, 52: 30 }));
    const scroller = sheet.getScrollContainer();
    scroller.scrollTop = 3000;
    scroller.dispatchEvent(new Event('scroll'));

    expect(scrollTop()).to.equal(3000);
  });

  it('moves nothing for rows below the top of the screen', () => {
    scrollTo(2000);

    serverSends(heights({ 101: 30, 150: 30 }));

    expect(scrollTop()).to.equal(2000);
  });

  it('moves nothing at the top of the sheet', () => {
    serverSends(heights({ 1: 30, 2: 30 }));

    expect(scrollTop()).to.equal(0);
  });

  it('keeps rows below frozen ones in place, which frozen rows growing do not move', () => {
    sheet.setVerticalSplitPosition(5);
    sheet.notifyStateChanges(['verticalSplitPosition'], false);
    scrollTo(2000);

    serverSends(heights({ 2: 30, 3: 30 }));
    expect(scrollTop(), 'frozen rows').to.equal(2000);
    serverSends(heights({ 2: 30, 3: 30, 50: 30 }));

    expect(scrollTop()).to.equal(2020);
  });

  it('goes back to where a sheet shown again was, not to the row of the sheet before', () => {
    sheet.setSheetNames(['One', 'Two']);
    sheet.setSheetIndex(1);
    sheet.notifyStateChanges(['sheetNames', 'sheetIndex'], false);
    scrollTo(2000);

    sheet.setVerticalScrollPositions([2000, 600]);
    sheet.setSheetIndex(2);
    sheet.setRowH(heights({ 50: 30 }));
    sheet.notifyStateChanges(['verticalScrollPositions', 'sheetIndex', 'rowH'], false);

    expect(scrollTop()).to.equal(600);
  });

  it('moves nothing for heights that come with a reload', () => {
    scrollTo(2000);

    sheet.setRowH(heights({ 50: 30 }));
    sheet.setReload(true);
    sheet.notifyStateChanges(['rowH', 'reload'], false);

    expect(scrollTop()).to.equal(2000);
  });

  it('asks for nothing more once rows above have grown', async () => {
    scrollTo(2000);
    // The cells scrolled to, then those past them.
    await new Promise((resolve) => setTimeout(resolve, 300));
    loads.length = 0;

    serverSends(heights({ 90: 30, 91: 30, 92: 30, 93: 30 }));
    await new Promise((resolve) => setTimeout(resolve, 300));

    expect(loads).to.be.empty;
  });

  it('asks for nothing more on a sheet with a component factory once rows of components above have grown', async () => {
    // It loads no margin: the rows drawn above the screen are all there is
    // above it, and fewer of them are drawn once they grow.
    sheet.setHasComponentFactory(true);
    scrollTo(2000);
    await new Promise((resolve) => setTimeout(resolve, 300));
    loads.length = 0;

    serverSends(heights({ 91: 30, 92: 30, 93: 30, 94: 30 }));
    await new Promise((resolve) => setTimeout(resolve, 300));

    expect(loads).to.be.empty;
  });

  it('takes a scroll after that for the user scrolling', async () => {
    scrollTo(2000);
    await new Promise((resolve) => setTimeout(resolve, 300));
    serverSends(heights({ 50: 30 }));
    await new Promise((resolve) => setTimeout(resolve, 100));
    loads.length = 0;

    sheet.getScrollContainer().scrollTop = 2100;
    await new Promise((resolve) => setTimeout(resolve, 300));

    expect(loads, 'told where it stopped').to.not.be.empty;
  });

  it('draws once when only rows below the top of the screen change height', () => {
    scrollTo(2000);
    let draws = 0;
    const draw = sheet.applyRenderState.bind(sheet);
    sheet.applyRenderState = (opts) => {
      draws++;
      draw(opts);
    };

    serverSends(heights({ 150: 30 }));

    expect(draws).to.equal(1);
  });
});
