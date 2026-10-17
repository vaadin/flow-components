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
import { spreadsheetStyles } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet-styles.js';

/**
 * Dragging a header's edge, as the GWT client had it.
 *
 * The sheet stays as it is while the edge is dragged: a line across the
 * whole sheet shows where the edge would go, and a tooltip by the pointer
 * the size it would make - "Hide column" or "Hide row" for none. Let go,
 * the column takes the width from its left edge to the pointer, or the row
 * the height from its top edge, and the server is told when that is a
 * different size.
 */

interface Sheet {
  host: HTMLElement;
  sheet: Spreadsheet;
  shadow: ShadowRoot;
  sent: Array<{ type: string; data: Record<string, unknown> }>;
  cleanup: () => void;
}

/** Five 50px columns and ten 15pt (20px) rows, drawn with the host's stylesheet. */
async function attach(theme: string | null = null, tokens = ''): Promise<Sheet> {
  const host = document.createElement('div');
  host.style.cssText = `width:600px;height:300px;display:flex;flex-direction:column;${tokens}`;
  if (theme !== null) {
    host.setAttribute('theme', theme);
  }
  document.body.appendChild(host);
  const shadow = host.attachShadow({ mode: 'open' });
  shadow.adoptedStyleSheets = [spreadsheetStyles.styleSheet!];
  const sheet = new Spreadsheet(host, shadow);
  sheet.setCols(5);
  sheet.setRows(10);
  sheet.setColW([50, 50, 50, 50, 50]);
  sheet.setRowH(new Array(10).fill(15));
  sheet.recomputeViewport();
  await sheet.getColumnHeader().updateComplete;
  await sheet.getRowHeader().updateComplete;
  await sheet.getCellGrid().updateComplete;
  const sent: Sheet['sent'] = [];
  host.addEventListener('spreadsheet-event', (e: Event) => sent.push((e as CustomEvent).detail));
  return {
    host,
    sheet,
    shadow,
    sent,
    cleanup: () => {
      document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
      sheet.dispose();
      host.remove();
    }
  };
}

function columnHandle(sheet: Spreadsheet, col: number): HTMLElement {
  return sheet.getColumnHeader().shadowRoot!.querySelector<HTMLElement>(`.col-resize-handle[data-col="${col}"]`)!;
}

function rowHandle(sheet: Spreadsheet, row: number): HTMLElement {
  return sheet.getRowHeader().shadowRoot!.querySelector<HTMLElement>(`.row-resize-handle[data-row="${row}"]`)!;
}

function columnLetter(sheet: Spreadsheet, col: number): DOMRect {
  return sheet.getColumnHeader().shadowRoot!.querySelector(`.col-header-cell.col${col}`)!.getBoundingClientRect();
}

function rowNumber(sheet: Spreadsheet, row: number): DOMRect {
  return sheet.getRowHeader().shadowRoot!.querySelector(`.row-header-cell.row${row}`)!.getBoundingClientRect();
}

/** Press on a handle, the way the pointer does. */
function press(handle: HTMLElement, x: number, y: number): void {
  handle.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, composed: true, clientX: x, clientY: y }));
}

function move(x: number, y: number): void {
  document.dispatchEvent(new MouseEvent('mousemove', { bubbles: true, clientX: x, clientY: y }));
}

function release(x: number, y: number): void {
  document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, clientX: x, clientY: y }));
}

/** Press an arrow key on the sheet. */
function arrow(sheet: Spreadsheet, key: string): void {
  sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, composed: true }));
}

function line(ctx: Sheet): HTMLElement {
  return ctx.shadow.querySelector<HTMLElement>('.resize-line')!;
}

function tooltip(ctx: Sheet): HTMLElement {
  return ctx.shadow.querySelector<HTMLElement>('.resize-tooltip')!;
}

function cover(ctx: Sheet): HTMLElement {
  return ctx.shadow.querySelector<HTMLElement>('.resize-cover')!;
}

/** The root's box inside its border, which the line spans. */
function inside(ctx: Sheet): { left: number; top: number; width: number; height: number } {
  const root = ctx.sheet.getRoot();
  const box = root.getBoundingClientRect();
  return { left: box.left + root.clientLeft, top: box.top + root.clientTop, width: root.clientWidth, height: root.clientHeight };
}

describe('Spreadsheet — dragging a column edge, as the GWT client did', () => {
  let ctx: Sheet;

  beforeEach(async () => {
    ctx = await attach();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  /** Take column B's edge and drag it to a point. */
  function dragB(x: number, y = 0): { left: number; y: number } {
    const b = columnLetter(ctx.sheet, 2);
    const handle = columnHandle(ctx.sheet, 2).getBoundingClientRect();
    // A mouse event's coordinates are whole pixels.
    const grabY = Math.floor(handle.top + handle.height / 2);
    press(columnHandle(ctx.sheet, 2), handle.left + handle.width / 2, grabY);
    move(x, y || grabY);
    return { left: b.left, y: y || grabY };
  }

  it('shows a line where the edge would go, across the whole sheet', () => {
    const { left } = dragB(columnLetter(ctx.sheet, 2).left + 80);

    const box = line(ctx).getBoundingClientRect();
    const sheet = inside(ctx);
    expect([box.left, box.top, box.width, box.height]).to.deep.equal([left + 79, sheet.top, 1, sheet.height]);
    expect(getComputedStyle(line(ctx)).backgroundColor).to.equal('rgb(25, 125, 225)');
  });

  it('shows the width it would take by the pointer, on the GWT client\'s tooltip', () => {
    const { left, y } = dragB(columnLetter(ctx.sheet, 2).left + 80);

    const tip = tooltip(ctx);
    expect(tip.textContent).to.equal('Width: 80px');
    expect(tip.matches(':popover-open')).to.be.true;
    const box = tip.getBoundingClientRect();
    expect([box.left, box.top]).to.deep.equal([left + 80 + 10, y - 25]);
    const style = getComputedStyle(tip);
    expect(style.backgroundColor).to.equal('rgba(50, 50, 50, 0.9)');
    expect(style.color).to.equal('rgb(255, 255, 255)');
    expect(style.padding).to.equal('5px 9px');
    expect(style.borderRadius).to.equal('3px');
    expect(style.fontSize).to.equal('14px');
    expect(style.boxShadow).to.equal('rgba(0, 0, 0, 0.2) 0px 2px 12px 0px');
    expect(style.pointerEvents).to.equal('none');
  });

  it('measures from where the column starts, wherever the handle was taken', () => {
    const b = columnLetter(ctx.sheet, 2);
    const handle = columnHandle(ctx.sheet, 2).getBoundingClientRect();

    // Taken at the handle's far left, a pixel or two inside the column.
    press(columnHandle(ctx.sheet, 2), handle.left, handle.top + 2);

    expect(tooltip(ctx).textContent).to.equal(`Width: ${Math.round(handle.left - b.left)}px`);
  });

  it('leaves the column as it is while the edge is dragged', async () => {
    dragB(columnLetter(ctx.sheet, 2).left + 80);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    expect(columnLetter(ctx.sheet, 2).width).to.equal(50);
    expect(ctx.sheet.getCellGrid().renderState.colWidths[1]).to.equal(50);
  });

  it('offers to hide the column, and keeps the line at its left edge, short of it', () => {
    const { left } = dragB(columnLetter(ctx.sheet, 2).left - 30);

    expect(tooltip(ctx).textContent).to.equal('Hide column');
    expect(line(ctx).getBoundingClientRect().left).to.equal(left - 1);
  });

  it('takes the width to the pointer when let go, and tells the server', async () => {
    const { left, y } = dragB(columnLetter(ctx.sheet, 2).left + 80);

    release(left + 80, y);
    await ctx.sheet.getColumnHeader().updateComplete;

    expect(ctx.sent.filter((e) => e.type === 'columnResized').map((e) => e.data)).to.deep.equal([
      { colIndexes: [2], newWidths: [80] }
    ]);
    expect(columnLetter(ctx.sheet, 2).width).to.equal(80);
    expect(line(ctx).hidden).to.be.true;
    expect(tooltip(ctx).matches(':popover-open')).to.be.false;
  });

  it('hides the column when let go short of its left edge', async () => {
    const { left, y } = dragB(columnLetter(ctx.sheet, 2).left - 30);

    release(left - 30, y);
    await ctx.sheet.getColumnHeader().updateComplete;

    expect(ctx.sent.filter((e) => e.type === 'columnResized').map((e) => e.data)).to.deep.equal([
      { colIndexes: [2], newWidths: [0] }
    ]);
    expect(sheetColumnLetters(ctx)).to.deep.equal(['A', 'C', 'D', 'E']);
  });

  it('has the arrows step over the column hidden so, as over any hidden one', () => {
    const { left, y } = dragB(columnLetter(ctx.sheet, 2).left - 30);
    release(left - 30, y);
    ctx.sheet.setSelectedCellAndRange('', 1, 1, 1, 1, 1, 1, false);

    arrow(ctx.sheet, 'ArrowRight');

    expect(ctx.sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 1 });
  });

  it('tells the server once: a second release is no drag', () => {
    const { left, y } = dragB(columnLetter(ctx.sheet, 2).left + 80);

    release(left + 80, y);
    move(left + 120, y);
    release(left + 120, y);

    expect(ctx.sent.filter((e) => e.type === 'columnResized')).to.have.length(1);
    expect(line(ctx).hidden).to.be.true;
  });

  it('tells the server nothing when let go at the width the column has', () => {
    const { left, y } = dragB(columnLetter(ctx.sheet, 2).left + 50);

    release(left + 50, y);

    expect(ctx.sent.filter((e) => e.type === 'columnResized')).to.be.empty;
  });

  it('shows the resize cursor wherever the pointer goes once it moves', () => {
    const handle = columnHandle(ctx.sheet, 2).getBoundingClientRect();
    press(columnHandle(ctx.sheet, 2), handle.left + 2, handle.top + 2);
    expect(cover(ctx).hidden, 'not before it moves').to.be.true;

    move(handle.left + 40, handle.top + 100);

    const sheet = inside(ctx);
    const over = ctx.shadow.elementFromPoint(sheet.left + 200, sheet.top + 150);
    expect(over).to.equal(cover(ctx));
    expect(getComputedStyle(cover(ctx)).cursor).to.equal('col-resize');
    release(handle.left + 40, handle.top + 100);
    expect(cover(ctx).hidden).to.be.true;
  });

  it('leaves the handle to a double-click, which fits the column to its content', () => {
    const handle = columnHandle(ctx.sheet, 2);
    const box = handle.getBoundingClientRect();
    const x = box.left + box.width / 2;
    const y = box.top + box.height / 2;

    for (let click = 1; click <= 2; click++) {
      press(handle, x, y);
      // What the second click lands on is what gets the double-click.
      expect(ctx.shadow.elementFromPoint(x, y)).to.not.equal(cover(ctx));
      release(x, y);
    }
    handle.dispatchEvent(new MouseEvent('dblclick', { bubbles: true, composed: true, clientX: x, clientY: y }));

    expect(ctx.sent.filter((e) => e.type === 'onColumnAutofit')).to.have.length(1);
  });

  /** A point on the handle a pixel inside column B, where a click lands, in whole pixels. */
  function clickPoint(): { x: number; y: number } {
    const handle = columnHandle(ctx.sheet, 2).getBoundingClientRect();
    return { x: Math.round(columnLetter(ctx.sheet, 2).left) + 45, y: Math.round(handle.top + handle.height / 2) };
  }

  it('resizes nothing when the handle is only clicked, as the GWT client did', async () => {
    const { x, y } = clickPoint();

    press(columnHandle(ctx.sheet, 2), x, y);
    release(x, y);
    await ctx.sheet.getColumnHeader().updateComplete;

    expect(ctx.sent.filter((e) => e.type === 'columnResized')).to.be.empty;
    expect(columnLetter(ctx.sheet, 2).width).to.equal(50);
    expect(line(ctx).hidden).to.be.true;
  });

  it('takes a move event that leaves the pointer where it was pressed for no move', () => {
    const { x, y } = clickPoint();

    press(columnHandle(ctx.sheet, 2), x, y);
    move(x, y);

    expect(cover(ctx).hidden, 'the second click of a double-click goes to the handle').to.be.true;
    release(x, y);
    expect(ctx.sent.filter((e) => e.type === 'columnResized')).to.be.empty;
  });

  it('fits the column on a double-click without resizing it on the clicks first', () => {
    const handle = columnHandle(ctx.sheet, 2);
    const { x, y } = clickPoint();

    for (let click = 1; click <= 2; click++) {
      press(handle, x, y);
      release(x, y);
    }
    handle.dispatchEvent(new MouseEvent('dblclick', { bubbles: true, composed: true, clientX: x, clientY: y }));

    expect(
      ctx.sent.filter((e) => e.type === 'columnResized' || e.type === 'onColumnAutofit').map((e) => e.type)
    ).to.deep.equal(['onColumnAutofit']);
  });

  it('takes the width to the pointer when a drag comes back to where it was taken', () => {
    const { x, y } = clickPoint();

    press(columnHandle(ctx.sheet, 2), x, y);
    move(x + 30, y);
    move(x, y);
    release(x, y);

    expect(ctx.sent.filter((e) => e.type === 'columnResized').map((e) => e.data)).to.deep.equal([
      { colIndexes: [2], newWidths: [Math.round(x - columnLetter(ctx.sheet, 2).left)] }
    ]);
  });
});

function sheetColumnLetters(ctx: Sheet): string[] {
  return Array.from(
    ctx.sheet.getColumnHeader().shadowRoot!.querySelectorAll('.col-header-cell'),
    (letter) => letter.textContent!.trim()
  );
}

describe('Spreadsheet — dragging a row edge, as the GWT client did', () => {
  let ctx: Sheet;

  beforeEach(async () => {
    ctx = await attach();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  /** Take row 3's edge and drag it to a point. */
  function drag3(y: number): { top: number; x: number } {
    const three = rowNumber(ctx.sheet, 3);
    const handle = rowHandle(ctx.sheet, 3).getBoundingClientRect();
    const x = handle.left + handle.width / 2;
    press(rowHandle(ctx.sheet, 3), x, handle.top + handle.height / 2);
    move(x, y);
    return { top: three.top, x };
  }

  it('shows the height in pixels and in points', () => {
    drag3(rowNumber(ctx.sheet, 3).top + 19);

    expect(tooltip(ctx).textContent).to.equal('Height: 19px ≈ 14.25pt');
  });

  it('shows a line where the edge would go, across the whole sheet', () => {
    // A pointer is at a whole pixel; the row may start between two.
    const y = Math.round(rowNumber(ctx.sheet, 3).top + 24);
    drag3(y);

    const box = line(ctx).getBoundingClientRect();
    const sheet = inside(ctx);
    expect([box.left, box.top, box.width, box.height]).to.deep.equal([sheet.left, y - 1, sheet.width, 1]);
  });

  it('offers to hide the row above its top edge', () => {
    const { top } = drag3(rowNumber(ctx.sheet, 3).top - 6);

    expect(tooltip(ctx).textContent).to.equal('Hide row');
    expect(line(ctx).getBoundingClientRect().top).to.equal(top - 1);
  });

  it('hides the row when let go above its top edge, and the arrows step over it', () => {
    const { top, x } = drag3(rowNumber(ctx.sheet, 3).top - 6);
    release(x, top - 6);
    ctx.sheet.setSelectedCellAndRange('', 1, 2, 1, 1, 2, 2, false);

    arrow(ctx.sheet, 'ArrowDown');

    expect(ctx.sent.filter((e) => e.type === 'rowsResized').map((e) => e.data)).to.deep.equal([
      { rowIndexes: [3], newHeights: [0] }
    ]);
    expect(ctx.sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 4 });
  });

  it('takes the height to the pointer, in points, when let go', async () => {
    const { top, x } = drag3(rowNumber(ctx.sheet, 3).top + 24);

    release(x, top + 24);
    await ctx.sheet.getRowHeader().updateComplete;

    expect(ctx.sent.filter((e) => e.type === 'rowsResized').map((e) => e.data)).to.deep.equal([
      { rowIndexes: [3], newHeights: [18] }
    ]);
    expect(rowNumber(ctx.sheet, 3).height).to.equal(24);
  });

  it('shows the row resize cursor once the pointer moves', () => {
    drag3(rowNumber(ctx.sheet, 3).top + 24);

    expect(getComputedStyle(cover(ctx)).cursor).to.equal('row-resize');
  });

  it('resizes nothing when the handle is only clicked, as the GWT client did', () => {
    const handle = rowHandle(ctx.sheet, 3).getBoundingClientRect();
    const x = Math.round(handle.left + handle.width / 2);
    const y = Math.round(rowNumber(ctx.sheet, 3).top) + 16;

    press(rowHandle(ctx.sheet, 3), x, y);
    release(x, y);

    expect(ctx.sent.filter((e) => e.type === 'rowsResized')).to.be.empty;
    expect(rowNumber(ctx.sheet, 3).height).to.equal(20);
  });
});

describe('Spreadsheet — dragging a header edge in the Lumo theme', () => {
  const TOKENS =
    '--lumo-primary-color: rgb(0, 0, 200);--lumo-contrast-50pct: rgba(50, 50, 50, 0.5);' +
    '--lumo-box-shadow-s: 0 2px 4px rgba(2, 2, 2, 0.2);--lumo-base-color: rgb(251, 252, 253);' +
    '--lumo-space-xs: 3px;--lumo-space-s: 7px;--lumo-font-size-s: 13px;';
  let ctx: Sheet;

  beforeEach(async () => {
    ctx = await attach('lumo', TOKENS);
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('draws the line in the primary colour and the tooltip in Lumo\'s', () => {
    const handle = columnHandle(ctx.sheet, 2).getBoundingClientRect();
    press(columnHandle(ctx.sheet, 2), handle.left + 2, handle.top + 2);

    expect(getComputedStyle(line(ctx)).backgroundColor).to.equal('rgb(0, 0, 200)');
    const style = getComputedStyle(tooltip(ctx));
    expect(style.backgroundColor).to.equal('rgba(50, 50, 50, 0.5)');
    expect(style.color).to.equal('rgb(251, 252, 253)');
    expect(style.padding).to.equal('3px 7px');
    expect(style.fontSize).to.equal('13px');
    expect(style.boxShadow).to.equal('rgba(2, 2, 2, 0.2) 0px 2px 4px 0px');
  });
});
