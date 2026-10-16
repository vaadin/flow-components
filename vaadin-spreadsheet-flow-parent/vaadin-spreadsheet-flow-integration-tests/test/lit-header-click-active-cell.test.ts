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
 * A click on a column letter selects the column and makes active the first
 * row whose top is on screen, as GWT did, rather than row 1, which can be far
 * off screen; a row number likewise the first column on screen. With rows or
 * columns frozen, it is row or column 1.
 */

type Dispatched = { type: string; data: Record<string, unknown> };

describe('Spreadsheet — the cell a header click makes active', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let sent: Dispatched[];

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    // 100 rows of 20px, 20 columns of 50px.
    sheet.setRows(100);
    sheet.setCols(20);
    sheet.setColW(new Array(20).fill(50));
    sheet.setRowH(new Array(100).fill(15));
    sheet.recomputeViewport();
    sent = [];
    host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  function scrollTo(top: number, left: number): void {
    sheet.getScrollContainer().scrollTop = top;
    sheet.getScrollContainer().scrollLeft = left;
    sheet.recomputeViewport();
    sent.length = 0;
  }

  const KEYS = { shiftKey: false, ctrlKey: false, metaKey: false, altKey: false };

  function clickColumn(col: number, keys: Partial<typeof KEYS> = {}): void {
    sheet.getColumnHeader().dispatchEvent(
      new CustomEvent('column-header-click', { detail: { col, ...KEYS, ...keys }, bubbles: true, composed: true })
    );
  }

  function clickRow(row: number, keys: Partial<typeof KEYS> = {}): void {
    sheet.getRowHeader().dispatchEvent(
      new CustomEvent('row-header-click', { detail: { row, ...KEYS, ...keys }, bubbles: true, composed: true })
    );
  }

  it('makes row 1 active on a sheet not scrolled', () => {
    clickColumn(5);

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 5, row: 1 });
    expect(sent).to.deep.equal([{ type: 'columnSelected', data: { column: 5, firstRowIndex: 1 } }]);
  });

  it('makes the first row on screen active on a sheet scrolled down', () => {
    // Row 51 starts at 1,000px.
    scrollTo(1000, 0);

    clickColumn(5);

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 5, row: 51 });
    expect(sheet.getSelection().activeRange).to.deep.equal({ c1: 5, c2: 5, r1: 1, r2: 100 });
    expect(sent).to.deep.equal([{ type: 'columnSelected', data: { column: 5, firstRowIndex: 51 } }]);
  });

  it('passes over a row whose top is scrolled out of view', () => {
    scrollTo(1010, 0);

    clickColumn(5);

    expect(sheet.getSelection().activeCell.row).to.equal(52);
  });

  it('passes over hidden rows', () => {
    sheet.setHiddenRowIndexes([51, 52]);
    scrollTo(1000, 0);

    clickColumn(5);

    expect(sheet.getSelection().activeCell.row).to.equal(53);
  });

  it('makes row 1 active when rows are frozen', () => {
    sheet.setVerticalSplitPosition(2);
    scrollTo(1000, 0);

    clickColumn(5);

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 5, row: 1 });
  });

  it('adds the column with Ctrl, making the first row on screen active', () => {
    scrollTo(1000, 0);
    clickColumn(2);
    sent.length = 0;

    clickColumn(5, { ctrlKey: true });

    const selection = sheet.getSelection();
    expect(selection.activeCell).to.deep.equal({ col: 5, row: 51 });
    expect(selection.activeRange).to.deep.equal({ c1: 5, c2: 5, r1: 1, r2: 100 });
    expect(selection.auxRanges).to.deep.equal([{ c1: 2, c2: 2, r1: 1, r2: 100 }]);
    expect(sent).to.deep.equal([{ type: 'columnAddedToSelection', data: { column: 5, firstRowIndex: 51 } }]);
  });

  it('keeps the cell it extends from with Shift', () => {
    scrollTo(1000, 0);
    clickColumn(2);

    clickColumn(5, { shiftKey: true });

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 51 });
  });

  it('makes the first column on screen active on a sheet scrolled right', () => {
    // Column 11 starts at 500px.
    scrollTo(0, 500);

    clickRow(7);

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 11, row: 7 });
    expect(sheet.getSelection().activeRange).to.deep.equal({ c1: 1, c2: 20, r1: 7, r2: 7 });
    expect(sent).to.deep.equal([{ type: 'rowSelected', data: { row: 7, firstColumnIndex: 11 } }]);
  });

  it('passes over a column whose left edge is scrolled out of view, and hidden columns', () => {
    sheet.setHiddenColumnIndexes([12]);
    scrollTo(0, 510);

    clickRow(7);

    expect(sheet.getSelection().activeCell.col).to.equal(13);
  });

  it('makes column 1 active when columns are frozen', () => {
    sheet.setHorizontalSplitPosition(1);
    scrollTo(0, 500);

    clickRow(7);

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 7 });
  });

  it('adds the row with Ctrl, making the first column on screen active', () => {
    scrollTo(0, 500);

    clickRow(7, { metaKey: true });

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 11, row: 7 });
    expect(sent).to.deep.equal([{ type: 'rowAddedToRangeSelection', data: { row: 7, firstColumnIndex: 11 } }]);
  });
});
