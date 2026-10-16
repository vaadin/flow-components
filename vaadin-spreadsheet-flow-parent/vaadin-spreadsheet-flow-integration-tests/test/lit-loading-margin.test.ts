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
import '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet.js';

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

  it('asks for the cells it draws first, and for those past them after', async () => {
    // On a slow network, the first screen came 0.3-1 s after GWT's while
    // the margin came along.
    await open();

    expect(loads[0]).to.eql(drawn());
    expect(loads[1].lastRow).to.be.greaterThan(drawn().lastRow);
  });

  // The server makes and sends a component or an editor for each cell it
  // loads that has one: with a margin, a sheet opened with 2.5 times GWT's.

  it('on a sheet whose server has a component factory, asks for the cells it draws alone', async () => {
    sheet.setHasComponentFactory(true);

    await open();

    expect(loads).to.deep.equal([drawn()]);
  });

  it('asks for rows past them again once the factory is gone', async () => {
    sheet.setHasComponentFactory(true);
    sheet.setHasComponentFactory(false);

    await open();

    expect(Math.max(...loads.map((l) => l.lastRow))).to.be.greaterThan(drawn().lastRow);
  });
});

describe('<vaadin-spreadsheet> — the component factory flag', () => {
  type Element = HTMLElement & { $connector: Spreadsheet; updateComplete: Promise<boolean>; hasComponentFactory: boolean };

  it('passes on what the server says about its component factory', async () => {
    const el = document.createElement('vaadin-spreadsheet') as Element;
    document.body.appendChild(el);
    await el.updateComplete;
    const received: boolean[] = [];
    const connector = el.$connector;
    const original = connector.setHasComponentFactory.bind(connector);
    connector.setHasComponentFactory = (has: boolean) => {
      received.push(has);
      original(has);
    };

    // As Flow sets it.
    el.hasComponentFactory = true;
    await el.updateComplete;

    expect(received).to.eql([true]);
    el.remove();
    connector.dispose();
  });
});
