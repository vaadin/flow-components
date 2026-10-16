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

type LoadEvent = { firstRow: number; firstColumn: number; lastRow: number; lastColumn: number };

/**
 * The cells the sheet asks the server for, against the ones it draws: more
 * than those, so that scrolling finds the next ones loaded. No server
 * answers in these tests, so each request counts as answered at once.
 */
describe('Spreadsheet — the cells it asks the server for', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let loads: LoadEvent[];

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
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  /** A sheet of 1,000 rows and 50 columns, and whatever it asks for after a while. */
  async function open(): Promise<void> {
    sheet.setCols(50);
    sheet.setRows(1000);
    sheet.setColW(new Array(50).fill(50));
    sheet.setRowH(new Array(1000).fill(15));
    sheet.recomputeViewport();
    await new Promise((resolve) => setTimeout(resolve, 150));
  }

  function drawn(): LoadEvent {
    const v = sheet.getCellGrid().renderState.viewport;
    return { firstRow: v.firstRowIndex, firstColumn: v.firstColumnIndex, lastRow: v.lastRowIndex, lastColumn: v.lastColumnIndex };
  }

  it('asks for rows past the ones it draws', async () => {
    await open();

    expect(Math.max(...loads.map((l) => l.lastRow))).to.be.greaterThan(drawn().lastRow);
  });
});
