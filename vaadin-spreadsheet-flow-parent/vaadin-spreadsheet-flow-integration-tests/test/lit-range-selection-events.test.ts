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
 * A range selection reaches the server as one event. The server keeps one
 * selected cell, the starting point of its ranges: a range grown from it - a
 * shift-click, Shift+arrow - is `cellRangeSelected` alone, and a range with a
 * starting point of its own - a drag, select all - or grown from a cell the
 * server does not hold is `cellRangePainted`, which carries the point. Told
 * the point first, as a `cellSelected`, the server would report a selection
 * of that one cell in between to every selection listener.
 */

interface Sent {
  type: string;
  data: Record<string, unknown>;
}

function attachSheet(): { sheet: Spreadsheet; sent: Sent[]; cleanup: () => void } {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));
  // The startup handshake goes out before any test counts events.
  sheet.recomputeViewport();
  const sent: Sent[] = [];
  host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));
  return {
    sheet,
    sent,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

const NO_KEYS = { shiftKey: false, ctrlKey: false, metaKey: false, altKey: false };

function gridEvent(sheet: Spreadsheet, type: string, detail: Record<string, unknown>): void {
  sheet.getCellGrid().dispatchEvent(new CustomEvent(type, { detail, bubbles: true, composed: true }));
}

/** A click as a pointer makes it: press, release, click. */
function click(sheet: Spreadsheet, col: number, row: number, shiftKey = false): void {
  gridEvent(sheet, 'cell-mousedown', { col, row, ...NO_KEYS, shiftKey });
  document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
  gridEvent(sheet, 'cell-click', { col, row, ...NO_KEYS, shiftKey });
}

/** A press on one cell, dragged over others and let go on the last. */
function drag(sheet: Spreadsheet, from: [number, number], ...over: Array<[number, number]>): void {
  gridEvent(sheet, 'cell-mousedown', { col: from[0], row: from[1], ...NO_KEYS });
  for (const [col, row] of over) {
    gridEvent(sheet, 'cell-mouseover', { col, row });
  }
  document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
}

function key(sheet: Spreadsheet, name: string, shiftKey = false): void {
  sheet
    .getScrollContainer()
    .dispatchEvent(new KeyboardEvent('keydown', { key: name, shiftKey, bubbles: true, cancelable: true }));
}

/** The selection events sent, by type: what the server's selection listeners hear about. */
function selectionTypes(sent: Sent[]): string[] {
  return sent
    .map((e) => e.type)
    .filter((type) => ['cellSelected', 'cellRangeSelected', 'cellRangePainted', 'columnSelected'].includes(type));
}

describe('Spreadsheet — a range selection is one event on the server', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('grows a range from the clicked cell with cellRangeSelected alone', () => {
    click(ctx.sheet, 2, 2);
    ctx.sent.length = 0;

    click(ctx.sheet, 4, 5, true);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangeSelected']);
    expect(ctx.sent[0].data).to.deep.equal({ row1: 2, col1: 2, row2: 5, col2: 4 });
  });

  it('grows a range with every Shift+arrow by cellRangeSelected alone', () => {
    click(ctx.sheet, 2, 2);
    ctx.sent.length = 0;

    key(ctx.sheet, 'ArrowDown', true);
    key(ctx.sheet, 'ArrowDown', true);
    key(ctx.sheet, 'ArrowRight', true);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangeSelected', 'cellRangeSelected', 'cellRangeSelected']);
    expect(ctx.sent[ctx.sent.length - 1].data).to.deep.equal({ row1: 2, col1: 2, row2: 4, col2: 3 });
  });

  it('sends a drag with the cell it started on, in cellRangePainted', () => {
    click(ctx.sheet, 7, 7);
    ctx.sent.length = 0;

    drag(ctx.sheet, [2, 2], [3, 3], [4, 5]);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangePainted']);
    expect(ctx.sent[0].data).to.deep.equal({
      selectedCellRow: 2,
      selectedCellColumn: 2,
      row1: 2,
      col1: 2,
      row2: 5,
      col2: 4
    });
  });

  it('sends a drag with its starting point even from the selected cell', () => {
    click(ctx.sheet, 2, 2);
    ctx.sent.length = 0;

    drag(ctx.sheet, [2, 2], [3, 3]);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangePainted']);
  });

  it('selects all with A1 as its starting point, even when the server holds A1 already', () => {
    click(ctx.sheet, 1, 1);
    ctx.sent.length = 0;

    // Ctrl+A off a Mac: the platform decides which key the shortcut takes.
    Object.defineProperty(navigator, 'platform', { value: 'Win32', configurable: true });
    try {
      ctx.sheet
        .getScrollContainer()
        .dispatchEvent(new KeyboardEvent('keydown', { key: 'a', ctrlKey: true, bubbles: true, cancelable: true }));
    } finally {
      delete (navigator as { platform?: string }).platform;
    }

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangePainted']);
    expect(ctx.sent[0].data).to.deep.equal({
      selectedCellRow: 1,
      selectedCellColumn: 1,
      row1: 1,
      col1: 1,
      row2: 10,
      col2: 10
    });
  });

  it('carries the starting point of a range grown from a cell the server does not hold', () => {
    // Nothing selected on the server yet: no click, no word from it.
    key(ctx.sheet, 'ArrowDown', true);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangePainted']);
    expect(ctx.sent[0].data).to.deep.equal({
      selectedCellRow: 1,
      selectedCellColumn: 1,
      row1: 1,
      col1: 1,
      row2: 2,
      col2: 1
    });
  });

  it('grows a range alone from a cell the server selected', () => {
    ctx.sheet.showSelectedCell(null, 3, 3, '', false, false, false);
    ctx.sent.length = 0;

    key(ctx.sheet, 'ArrowRight', true);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangeSelected']);
  });

  it('grows a range alone from the active cell of a range the server selected', () => {
    ctx.sheet.setSelectedCellAndRange('', 4, 4, 4, 5, 4, 6, false);
    ctx.sent.length = 0;

    key(ctx.sheet, 'ArrowDown', true);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangeSelected']);
  });

  it('carries the starting point again once a reload has cleared the server', () => {
    click(ctx.sheet, 2, 2);
    ctx.sheet.setReload(true);
    ctx.sent.length = 0;

    click(ctx.sheet, 4, 4, true);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangePainted']);
  });

  it('grows a column selection from a shift-clicked header with cellRangeSelected alone', () => {
    const header = (col: number, shiftKey: boolean): void => {
      ctx.sheet
        .getColumnHeader()
        .dispatchEvent(
          new CustomEvent('column-header-click', { detail: { col, ...NO_KEYS, shiftKey }, bubbles: true, composed: true })
        );
    };
    header(2, false);
    ctx.sent.length = 0;

    header(4, true);

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangeSelected']);
    expect(ctx.sent[0].data).to.deep.equal({ row1: 1, col1: 2, row2: 10, col2: 4 });
  });

  it('reports a range a shift-press selects once, though the click selects it again', () => {
    click(ctx.sheet, 2, 2);
    ctx.sent.length = 0;

    gridEvent(ctx.sheet, 'cell-mousedown', { col: 5, row: 3, ...NO_KEYS, shiftKey: true });
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    gridEvent(ctx.sheet, 'cell-click', { col: 5, row: 3, ...NO_KEYS, shiftKey: true });

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangeSelected']);
  });

  it('reports a shift-click that comes on its own after the release has been reported', async () => {
    click(ctx.sheet, 2, 2);
    click(ctx.sheet, 4, 4, true);
    // The next press, as a test or an assistive tool may make it: a click
    // with no press of its own, once the earlier one is over.
    await new Promise((resolve) => setTimeout(resolve));
    ctx.sent.length = 0;

    gridEvent(ctx.sheet, 'cell-click', { col: 6, row: 6, ...NO_KEYS, shiftKey: true });

    expect(selectionTypes(ctx.sent)).to.deep.equal(['cellRangeSelected']);
  });
});
