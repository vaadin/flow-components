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
 * A sheet shown again goes back to where it was scrolled. The server keeps
 * each sheet's offsets, taken from the sheet selection event, and sends them
 * in the batch that switches the sheet.
 */
describe('Spreadsheet — each sheet remembers where it was scrolled', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setCols(60);
    sheet.setRows(400);
    sheet.setColW(new Array(60).fill(50));
    sheet.setRowH(new Array(400).fill(15));
    sheet.setSheetNames(['One', 'Two']);
    sheet.setSheetIndex(1);
    sheet.notifyStateChanges(['cols', 'rows', 'colW', 'rowH', 'sheetNames', 'sheetIndex'], true);
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  /** As the server switches: the offsets it keeps, then the sheet. */
  function serverShows(index: number, top: number[], left: number[]): void {
    sheet.setVerticalScrollPositions(top);
    sheet.setHorizontalScrollPositions(left);
    sheet.setSheetIndex(index);
    sheet.notifyStateChanges(['verticalScrollPositions', 'horizontalScrollPositions', 'sheetIndex'], false);
  }

  it('goes back to where a sheet was when it is shown again', () => {
    const scroller = sheet.getScrollContainer();
    scroller.scrollTop = 600;

    // Leaving sheet one: the server keeps its 600 and shows sheet two,
    // never scrolled.
    serverShows(2, [600, 0], [0, 0]);
    expect(scroller.scrollTop).to.equal(0);

    // Back to sheet one, after sheet two was scrolled to 300.
    serverShows(1, [600, 300], [0, 0]);
    expect(scroller.scrollTop).to.equal(600);
  });

  it('stays there when the selection it was left with comes, out of view', () => {
    serverShows(2, [600, 0], [0, 0]);
    serverShows(1, [600, 0], [0, 0]);

    // The server's answer to where the sheet is: its selection, A1.
    sheet.showSelectedCell('A1', 1, 1, '', false, false, true, true);

    expect(sheet.getScrollContainer().scrollTop).to.equal(600);
  });

  it('goes back across too', () => {
    serverShows(2, [0, 0], [450, 0]);
    serverShows(1, [0, 0], [450, 0]);

    expect(sheet.getScrollContainer().scrollLeft).to.equal(450);
  });

  it('asks for the cells where the sheet goes back to, not where it was', () => {
    const asked: Array<{ firstRow: number; lastRow: number }> = [];
    host.addEventListener('spreadsheet-event', (e) => {
      const detail = (e as CustomEvent).detail;
      if (detail.type === 'onSheetScroll') {
        asked.push(detail.data);
      }
    });

    sheet.setReload(true);
    serverShows(2, [0, 2000], [0, 0]);

    // Rows of 15pt are 20px: 2000 pixels down is row 101. One request, for
    // there - not one for the top of the sheet and another after it.
    expect(asked).to.have.length(1);
    expect(asked[0].firstRow).to.be.above(50);
    expect(asked[0].firstRow).to.be.at.most(101);
    expect(asked[0].lastRow).to.be.at.least(101);
  });

  it('leaves a sheet where it is when a sheet before it goes', () => {
    // Sheet two, left at 150 once, is shown and scrolled to 600. The server
    // deletes or hides sheet one: sheet two is now the first, still shown.
    serverShows(2, [0, 150], [0, 0]);
    const scroller = sheet.getScrollContainer();
    scroller.scrollTop = 600;

    sheet.setSheetNames(['Two']);
    serverShows(1, [150], [0]);

    expect(scroller.scrollTop).to.equal(600);
  });

  it('leaves a sheet where it is when it is renamed', () => {
    serverShows(2, [0, 150], [0, 0]);
    const scroller = sheet.getScrollContainer();
    scroller.scrollTop = 600;

    sheet.setSheetNames(['One', 'Renamed']);
    sheet.notifyStateChanges(['sheetNames'], false);

    expect(scroller.scrollTop).to.equal(600);
  });

  it('does not scroll on the first batch', () => {
    const fresh = document.createElement('div');
    fresh.style.cssText = host.style.cssText;
    document.body.appendChild(fresh);
    const other = new Spreadsheet(fresh);
    try {
      other.setRows(400);
      other.setCols(10);
      other.setRowH(new Array(400).fill(15));
      other.setVerticalScrollPositions([600]);
      other.setSheetIndex(1);
      other.notifyStateChanges(['rows', 'cols', 'rowH', 'verticalScrollPositions', 'sheetIndex'], true);

      expect(other.getScrollContainer().scrollTop).to.equal(0);
    } finally {
      other.dispose();
      fresh.remove();
    }
  });
});
