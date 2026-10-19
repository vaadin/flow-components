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

type Range = { firstRow: number; firstColumn: number; lastRow: number; lastColumn: number };
type LoadEvent = Range & { drawnFirstRow: number; drawnFirstColumn: number; drawnLastRow: number; drawnLastColumn: number };

/** The range a request asks to load. */
function loadedBy(l: LoadEvent): Range {
  return { firstRow: l.firstRow, firstColumn: l.firstColumn, lastRow: l.lastRow, lastColumn: l.lastColumn };
}

/** The cells drawn a request tells the server of. */
function drawnBy(l: LoadEvent): Range {
  return { firstRow: l.drawnFirstRow, firstColumn: l.drawnFirstColumn, lastRow: l.drawnLastRow, lastColumn: l.drawnLastColumn };
}

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

  function drawn(): Range {
    const v = sheet.getCellGrid().renderState.viewport;
    return { firstRow: v.firstRowIndex, firstColumn: v.firstColumnIndex, lastRow: v.lastRowIndex, lastColumn: v.lastColumnIndex };
  }

  it('asks for rows past the ones it draws', async () => {
    await open();

    expect(Math.max(...loads.map((l) => l.lastRow))).to.be.greaterThan(drawn().lastRow);
  });

  it('asks for the cells it draws and a quarter screen below them, in one request', async () => {
    // A sheet is complete after one round trip, as with GWT; the margin
    // comes with the first scroll.
    await open();

    const shown = drawn();
    const quarter = Math.ceil((shown.lastRow - shown.firstRow + 1) / 4);
    expect(loads.map(loadedBy)).to.eql([{ ...shown, lastRow: shown.lastRow + quarter }]);
    // It tells the server what is drawn.
    expect(loads.map(drawnBy)).to.eql([shown]);
  });

  /** Scroll the sheet down to the pixel, as the user does. */
  function scrollTo(top: number): void {
    const container = sheet.getScrollContainer();
    container.scrollTop = top;
    container.dispatchEvent(new Event('scroll'));
  }

  it('tells the server where a scroll stopped soon after', async () => {
    await open();
    loads = [];

    scrollTo(300);
    await new Promise((resolve) => setTimeout(resolve, 400));

    expect(loads).to.not.be.empty;
  });

  it('tells the server nothing once out of the page', async () => {
    await open();
    loads = [];

    // A row: inside what was loaded, the server hears of it once the
    // scroll stops.
    scrollTo(15);
    sheet.disconnect();
    await new Promise((resolve) => setTimeout(resolve, 400));

    expect(loads).to.be.empty;
  });

  it('tells the server where a scroll stopped as soon once moved in the page', async () => {
    // Moving the element takes it out of the page and puts it back.
    await open();
    sheet.disconnect();
    sheet.connect();
    loads = [];

    scrollTo(300);
    await new Promise((resolve) => setTimeout(resolve, 400));

    expect(loads).to.not.be.empty;
  });

  // The server makes and sends a component or an editor for each cell it
  // loads that has one: with a margin, a sheet opened with 2.5 times GWT's.

  it('on a sheet whose server has a component factory, asks for the cells it draws alone', async () => {
    sheet.setHasComponentFactory(true);

    await open();

    expect(loads.map(loadedBy)).to.deep.equal([drawn()]);
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
