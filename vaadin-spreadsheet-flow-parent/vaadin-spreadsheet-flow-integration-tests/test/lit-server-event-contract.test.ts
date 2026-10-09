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
import { SERVER_EVENT_PAYLOAD_KEYS } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';

/**
 * The client half of the client-to-server payload contract.
 *
 * A key the connector sends but the server never reads makes the event
 * silently do nothing — no error on either side, and the symptom turns up
 * somewhere else entirely. `sheetSelected` sent `sheetName` while the server
 * read `sheetIndex`, so every sheet-tab click activated sheet 0 and the
 * failures looked like lost cell selections.
 *
 * `SERVER_EVENT_PAYLOAD_KEYS` is the single source of truth.
 * `ServerEventPayloadContractTest` holds the server to it from the other
 * side.
 */

type Dispatched = { type: string; data: Record<string, unknown> };

function attachSheet(): {
  host: HTMLElement;
  sheet: Spreadsheet;
  sent: Dispatched[];
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(20);
  sheet.setRows(200);
  sheet.setColW(new Array(20).fill(50));
  sheet.setRowH(new Array(200).fill(15));
  sheet.setHeaderRowHeight(20);
  sheet.setHeaderColumnWidth(40);
  sheet.setSheetNames(['One', 'Two']);
  sheet.setSheetIndex(1);
  sheet.recomputeViewport();

  const sent: Dispatched[] = [];
  host.addEventListener('spreadsheet-event', (e: Event) => {
    sent.push((e as CustomEvent).detail as Dispatched);
  });

  return {
    host,
    sheet,
    sent,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

/** Every payload seen so far carries exactly the keys the server reads. */
function expectConforms(sent: Dispatched[]): Set<string> {
  const seen = new Set<string>();
  for (const { type, data } of sent) {
    const expected = SERVER_EVENT_PAYLOAD_KEYS[type as keyof typeof SERVER_EVENT_PAYLOAD_KEYS];
    expect(expected, `'${type}' is not a declared server event`).to.not.be.undefined;
    expect(Object.keys(data ?? {}).sort(), `payload of '${type}'`).to.eql(
      [...expected].sort()
    );
    seen.add(type);
  }
  return seen;
}

function cellEl(sheet: Spreadsheet, col: number, row: number): HTMLElement {
  const el = sheet
    .getCellGrid()
    .shadowRoot!.querySelector<HTMLElement>(`.cell[data-col="${col}"][data-row="${row}"]`);
  expect(el, `cell ${col},${row}`).to.not.be.null;
  return el!;
}

function cellClick(
  sheet: Spreadsheet,
  col: number,
  row: number,
  modifiers: { shiftKey?: boolean; ctrlKey?: boolean; metaKey?: boolean }
): void {
  sheet.getCellGrid().dispatchEvent(
    new CustomEvent('cell-click', {
      detail: {
        col,
        row,
        shiftKey: false,
        ctrlKey: false,
        metaKey: false,
        altKey: false,
        ...modifiers
      },
      bubbles: true,
      composed: true
    })
  );
}

function mouse(el: HTMLElement, type: string, init: MouseEventInit = {}): void {
  el.dispatchEvent(new MouseEvent(type, { bubbles: true, composed: true, ...init }));
}

describe('Spreadsheet — client-to-server payload contract', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(async () => {
    ctx = attachSheet();
    await ctx.sheet.getCellGrid().updateComplete;
    ctx.sent.length = 0;
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('declares a payload for every event type it can send', () => {
    // Record<SpreadsheetEventType, …> makes this exhaustive at compile
    // time; the runtime check catches a hand-edited map.
    for (const [type, keys] of Object.entries(SERVER_EVENT_PAYLOAD_KEYS)) {
      expect(Array.isArray(keys), `${type} has no key list`).to.be.true;
      expect(new Set(keys).size, `${type} repeats a key`).to.equal(keys.length);
    }
  });

  it('conforms when a cell is clicked', () => {
    const cell = cellEl(ctx.sheet, 3, 4);
    mouse(cell, 'pointerdown');
    mouse(cell, 'mousedown');
    mouse(cell, 'mouseup');
    mouse(cell, 'click');

    expect(expectConforms(ctx.sent)).to.include('cellSelected');
  });

  it('conforms when a range is shift-extended', () => {
    cellClick(ctx.sheet, 2, 2, {});
    cellClick(ctx.sheet, 4, 5, { shiftKey: true });

    expect(expectConforms(ctx.sent)).to.include('cellRangeSelected');
  });

  it('conforms when cells are ctrl-added to the selection', () => {
    cellClick(ctx.sheet, 2, 2, {});
    cellClick(ctx.sheet, 4, 5, { ctrlKey: true });

    expect(expectConforms(ctx.sent)).to.include('cellAddedToSelectionAndSelected');
  });

  it('conforms when ctrl-shift extends the range', () => {
    cellClick(ctx.sheet, 2, 2, {});
    cellClick(ctx.sheet, 4, 5, { ctrlKey: true, shiftKey: true });

    expect(expectConforms(ctx.sent)).to.include('cellsAddedToRangeSelection');
  });

  it('conforms when the sheet scrolls', async () => {
    ctx.sheet.getScrollContainer().scrollTop = 600;
    ctx.sheet.getScrollContainer().dispatchEvent(new Event('scroll'));
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    expectConforms(ctx.sent);
  });

  it('conforms when a sheet tab is clicked', () => {
    ctx.sheet.getSheetTabs().dispatchEvent(
      new CustomEvent('sheet-tab-click', {
        detail: { index: 2 },
        bubbles: true,
        composed: true
      })
    );

    expect(expectConforms(ctx.sent)).to.include('sheetSelected');
  });

  it('names the sheet by its 0-based index, the way the server reads it', () => {
    ctx.sheet.getSheetTabs().dispatchEvent(
      new CustomEvent('sheet-tab-click', {
        detail: { index: 2 },
        bubbles: true,
        composed: true
      })
    );

    const selected = ctx.sent.find((e) => e.type === 'sheetSelected');
    expect(selected, 'no sheetSelected dispatched').to.not.be.undefined;
    expect(selected!.data.sheetIndex).to.equal(1);
  });

  it('sends where the sheet was scrolled to in whole pixels', () => {
    // A browser reports fractional scroll offsets on a scaled display, and
    // the server reads whole numbers: given 37.6 it refused the event, and
    // the sheet never switched.
    const scroller = ctx.sheet.getScrollContainer();
    Object.defineProperty(scroller, 'scrollTop', { get: () => 37.6, configurable: true });
    Object.defineProperty(scroller, 'scrollLeft', { get: () => 12.4, configurable: true });

    ctx.sheet.getSheetTabs().dispatchEvent(
      new CustomEvent('sheet-tab-click', { detail: { index: 2 }, bubbles: true, composed: true })
    );
    ctx.sheet.getSheetTabs().dispatchEvent(new CustomEvent('sheet-tab-add', { bubbles: true, composed: true }));

    const selected = ctx.sent.find((e) => e.type === 'sheetSelected');
    const created = ctx.sent.find((e) => e.type === 'sheetCreated');
    expect(selected!.data).to.include({ scrollTop: 38, scrollLeft: 12 });
    expect(created!.data).to.eql({ scrollLeft: 12, scrollTop: 38 });
  });

  it('conforms when a sheet is added', () => {
    ctx.sheet.getSheetTabs().dispatchEvent(
      new CustomEvent('sheet-tab-add', { bubbles: true, composed: true })
    );

    expect(expectConforms(ctx.sent)).to.include('sheetCreated');
  });

  it('conforms when an address is typed into the formula bar', () => {
    ctx.sheet.getFormulaBar().dispatchEvent(
      new CustomEvent('sheet-address-changed', {
        detail: { value: 'B7' },
        bubbles: true,
        composed: true
      })
    );

    expect(expectConforms(ctx.sent)).to.include('sheetAddressChanged');
  });

  it('conforms when a row header is clicked', () => {
    const header = ctx.sheet
      .getRowHeader()
      .shadowRoot!.querySelector<HTMLElement>('.row-header-cell');
    expect(header, 'no row header cell').to.not.be.null;
    mouse(header!, 'pointerdown');
    mouse(header!, 'mousedown');
    mouse(header!, 'click');

    expectConforms(ctx.sent);
  });

  it('conforms when a column header is clicked', () => {
    const header = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.col-header-cell');
    expect(header, 'no column header cell').to.not.be.null;
    mouse(header!, 'pointerdown');
    mouse(header!, 'mousedown');
    mouse(header!, 'click');

    expectConforms(ctx.sent);
  });
});
