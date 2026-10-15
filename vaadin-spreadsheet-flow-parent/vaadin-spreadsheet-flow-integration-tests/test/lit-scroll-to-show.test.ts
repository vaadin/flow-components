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
import { scrollToShow } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/ViewportMath.js';

/**
 * Scrolling a cell into view, by the GWT client's rule: a frozen cell or one
 * in full view leaves the sheet where it is; past the last whole row in view
 * the sheet scrolls on to show one row more, and before the first whole row
 * back to show one row more - two rows at a time while an arrow key is held.
 */
describe('scrollToShow — the GWT client rule along one axis', () => {
  // Twenty 20px rows; five whole rows fit a 100px view.
  const twenty = (i: number): number => (i >= 1 && i <= 20 ? 20 : 0);

  it('leaves the scroll alone for a row in full view', () => {
    expect(scrollToShow(5, 0, 20, 0, 100, twenty)).to.equal(0);
  });

  it('scrolls on to show the row and one more when the row is past the view', () => {
    // Rows 6 and 7 come into view.
    expect(scrollToShow(6, 0, 20, 0, 100, twenty)).to.equal(40);
  });

  it('counts a row cut off by the bottom edge as out of view', () => {
    // 110px shows rows 1-5 whole and half of row 6.
    expect(scrollToShow(6, 0, 20, 0, 110, twenty)).to.equal(40);
  });

  it('scrolls back to show the row and one more when the row is above the view', () => {
    // Scrolled 200px: row 11 is the first whole row. Row 9 brings 8, 9 and 10 back.
    expect(scrollToShow(9, 0, 20, 200, 100, twenty)).to.equal(140);
  });

  it('never scrolls back past the start', () => {
    expect(scrollToShow(1, 0, 20, 10, 100, twenty)).to.equal(0);
  });

  it('leaves a frozen row to its pane', () => {
    expect(scrollToShow(2, 3, 20, 200, 100, twenty)).to.equal(200);
  });

  it('keeps a row clear of the frozen rows above it', () => {
    // Rows 1-3 frozen (60px). Scrolled 100px, the first whole scrolling row
    // is the one starting at 160 (row 9), so row 8 is under the frozen rows.
    expect(scrollToShow(8, 3, 20, 100, 200, twenty)).to.equal(60);
  });

  it('stops at the last row', () => {
    // Scrolled 280px, row 20 is cut off by the bottom edge. It brings only
    // itself into view; there is no row 21 to show.
    expect(scrollToShow(20, 0, 20, 280, 100, twenty)).to.equal(300);
  });

  it('skips hidden rows, which take no room', () => {
    const someHidden = (i: number): number => (i === 6 ? 0 : twenty(i));
    // Row 7 is past the view; rows 7 and 8 come into view (hidden 6 adds nothing).
    expect(scrollToShow(7, 0, 20, 0, 100, someHidden)).to.equal(40);
  });

  it('counts the freeze line\'s own pixel before the scrolling rows', () => {
    // Rows 1-3 frozen (60px), then the line's pixel: row 8 ends at 161, a
    // pixel past a 160px view, so it is cut off and the sheet scrolls on to
    // show it and row 9.
    expect(scrollToShow(8, 3, 20, 0, 160, twenty)).to.equal(40);
    // A pixel more of view and row 8 is whole.
    expect(scrollToShow(8, 3, 20, 0, 161, twenty)).to.equal(0);
  });
});

describe('Spreadsheet — keyboard moves keep the cell in full view', () => {
  function attach(rowsFrozen = 0): { sheet: Spreadsheet; cleanup: () => void } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:200px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    const sheet = new Spreadsheet(host);
    sheet.setRows(100);
    sheet.setCols(30);
    sheet.setColW(new Array(30).fill(50));
    sheet.setRowH(new Array(100).fill(15));
    if (rowsFrozen > 0) {
      sheet.setVerticalSplitPosition(rowsFrozen);
    }
    sheet.recomputeViewport();
    return {
      sheet,
      cleanup: () => {
        sheet.dispose();
        host.remove();
      }
    };
  }

  function press(sheet: Spreadsheet, key: string): void {
    sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, composed: true }));
  }

  /** The active cell's box against the cells' part of the view. */
  async function activeCellInView(sheet: Spreadsheet): Promise<{ top: number; bottom: number }> {
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    const { col, row } = sheet.getSelection().activeCell;
    const grid = sheet.getCellGrid();
    const cell = Array.from(grid.shadowRoot!.querySelectorAll<HTMLElement>(`.cell.col${col}.row${row}`)).find(
      (c) => c.getBoundingClientRect().height > 0
    )!;
    const scroller = sheet.getScrollContainer().getBoundingClientRect();
    const header = sheet.getColumnHeader().getBoundingClientRect();
    const sc = sheet.getScrollContainer();
    const viewBottom = scroller.top + sc.clientTop + sc.clientHeight;
    const box = cell.getBoundingClientRect();
    return { top: box.top - header.bottom, bottom: viewBottom - box.bottom };
  }

  it('shows the cell below the column headers and above the bottom edge on every step down', async () => {
    const { sheet, cleanup } = attach();
    try {
      for (let step = 0; step < 30; step++) {
        press(sheet, 'ArrowDown');
        const gaps = await activeCellInView(sheet);
        expect(gaps.top, `top gap at step ${step}`).to.be.at.least(0);
        expect(gaps.bottom, `bottom gap at step ${step}`).to.be.at.least(0);
      }
    } finally {
      cleanup();
    }
  });

  it('brings the row below the cell into view too when it scrolls, as the GWT client did', async () => {
    // Two rows at a time: the press that scrolls leaves a whole 20px row
    // below the active cell, and the next press moves onto it without
    // scrolling.
    const { sheet, cleanup } = attach();
    try {
      const sc = sheet.getScrollContainer();
      for (let step = 0; step < 30 && sc.scrollTop === 0; step++) {
        press(sheet, 'ArrowDown');
      }
      expect(sc.scrollTop).to.be.above(0);
      expect((await activeCellInView(sheet)).bottom).to.be.at.least(20);

      const scrolled = sc.scrollTop;
      press(sheet, 'ArrowDown');
      expect(sc.scrollTop).to.equal(scrolled);
    } finally {
      cleanup();
    }
  });

  it('shows the cell below the frozen rows on the way back up', async () => {
    const { sheet, cleanup } = attach(3);
    try {
      for (let step = 0; step < 30; step++) {
        press(sheet, 'ArrowDown');
      }
      for (let step = 0; step < 25; step++) {
        press(sheet, 'ArrowUp');
        const { row } = sheet.getSelection().activeCell;
        const gaps = await activeCellInView(sheet);
        if (row > 3) {
          // Three frozen 20px rows sit between the headers and the scrolling rows.
          expect(gaps.top, `top gap at row ${row}`).to.be.at.least(60);
        }
      }
    } finally {
      cleanup();
    }
  });
});
