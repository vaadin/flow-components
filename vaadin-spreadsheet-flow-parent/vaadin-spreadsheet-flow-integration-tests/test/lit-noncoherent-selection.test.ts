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
import { isCellSelected } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';

/**
 * Ctrl/Cmd-click builds a non-coherent selection.
 *
 * `handleCellClick` read `shiftKey` and ignored `ctrlKey` / `metaKey`,
 * so Ctrl-clicking a second cell replaced the selection instead of
 * adding to it. Excel keeps both, and so did the GWT client.
 *
 * This is what SelectionIT's `testSelectionSingleCell`,
 * `testMultipleRectSelection`, `testComplicatedCellSelection` and
 * `testRowColumnMixed` exercise — every one of them Ctrl-clicks.
 */

function attachSheet(): { sheet: Spreadsheet; events: any[]; cleanup: () => void } {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));
  // Get the startup handshake (onConnectorInit + the initial onSheetScroll)
  // out of the way before any test starts counting events.
  sheet.recomputeViewport();

  const events: any[] = [];
  host.addEventListener('spreadsheet-event', (e) => events.push((e as CustomEvent).detail));

  return {
    sheet,
    events,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

function clickCell(
  sheet: Spreadsheet,
  col: number,
  row: number,
  mods: { shiftKey?: boolean; ctrlKey?: boolean; metaKey?: boolean } = {}
): void {
  // A real pointer fires mousedown BEFORE click, and the drag handler
  // reacts to mousedown. Dispatching only cell-click hides bugs that
  // live in that ordering — this helper reproduces the real sequence.
  sheet.getCellGrid().dispatchEvent(
    new CustomEvent('cell-mousedown', {
      detail: {
        col,
        row,
        shiftKey: false,
        ctrlKey: false,
        metaKey: false,
        altKey: false,
        ...mods
      },
      bubbles: true,
      composed: true
    })
  );
  document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
  sheet.getCellGrid().dispatchEvent(
    new CustomEvent('cell-click', {
      detail: {
        col,
        row,
        shiftKey: false,
        ctrlKey: false,
        metaKey: false,
        altKey: false,
        ...mods
      },
      bubbles: true,
      composed: true
    })
  );
}

describe('Spreadsheet non-coherent selection — Ctrl-click', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('keeps the first cell selected when a second is Ctrl-clicked', () => {
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 3, 3, { ctrlKey: true });

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 2, 2)).to.be.true;
    expect(isCellSelected(selection, 3, 3)).to.be.true;
  });

  it('makes the Ctrl-clicked cell the active one', () => {
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 3, 3, { ctrlKey: true });

    expect(ctx.sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 3 });
  });

  it('accumulates three separately Ctrl-clicked cells', () => {
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 3, 3, { ctrlKey: true });
    clickCell(ctx.sheet, 4, 4, { ctrlKey: true });

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 2, 2)).to.be.true;
    expect(isCellSelected(selection, 3, 3)).to.be.true;
    expect(isCellSelected(selection, 4, 4)).to.be.true;
    expect(isCellSelected(selection, 5, 5)).to.be.false;
  });

  it('treats Cmd-click the same as Ctrl-click', () => {
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 3, 3, { metaKey: true });

    expect(isCellSelected(ctx.sheet.getSelection(), 2, 2)).to.be.true;
    expect(isCellSelected(ctx.sheet.getSelection(), 3, 3)).to.be.true;
  });

  it('keeps a whole shift-extended range when a further cell is Ctrl-clicked', () => {
    clickCell(ctx.sheet, 2, 1);
    clickCell(ctx.sheet, 2, 3, { shiftKey: true });
    clickCell(ctx.sheet, 4, 3, { ctrlKey: true });

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 2, 1)).to.be.true;
    expect(isCellSelected(selection, 2, 2)).to.be.true;
    expect(isCellSelected(selection, 2, 3)).to.be.true;
    expect(isCellSelected(selection, 4, 3)).to.be.true;
  });

  it('a plain click still discards the whole previous selection', () => {
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 3, 3, { ctrlKey: true });
    clickCell(ctx.sheet, 5, 5);

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 5, 5)).to.be.true;
    expect(isCellSelected(selection, 2, 2)).to.be.false;
    expect(isCellSelected(selection, 3, 3)).to.be.false;
    expect(selection.auxRanges).to.have.length(0);
  });

  it('keeps the earlier ranges when the pointer drifts across a cell edge during a Ctrl-click', () => {
    // A hand is rarely still: the pointer often crosses into the next cell
    // between pressing and letting go. The press started a range drag, the
    // drift replaced every range picked out with Ctrl with one rectangle,
    // and the server was told to discard them. The GWT client started no
    // drag on a Ctrl, Cmd or Shift press.
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 4, 4, { ctrlKey: true });
    ctx.events.length = 0;

    const grid = ctx.sheet.getCellGrid();
    const detail = { col: 6, row: 6, shiftKey: false, ctrlKey: true, metaKey: false, altKey: false };
    grid.dispatchEvent(new CustomEvent('cell-mousedown', { detail, bubbles: true, composed: true }));
    grid.dispatchEvent(new CustomEvent('cell-mouseover', { detail: { col: 7, row: 6 }, bubbles: true, composed: true }));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    grid.dispatchEvent(new CustomEvent('cell-click', { detail, bubbles: true, composed: true }));

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 2, 2), 'B2').to.be.true;
    expect(isCellSelected(selection, 4, 4), 'D4').to.be.true;
    expect(isCellSelected(selection, 6, 6), 'F6').to.be.true;
    expect(isCellSelected(selection, 7, 6), 'G6').to.be.false;
    expect(ctx.events.map((e) => e.type)).to.eql(['cellAddedToSelectionAndSelected']);
  });

  it('keeps the earlier ranges while a Shift-press drags its range out', () => {
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 4, 4, { ctrlKey: true });

    const grid = ctx.sheet.getCellGrid();
    const detail = { col: 5, row: 5, shiftKey: true, ctrlKey: false, metaKey: false, altKey: false };
    grid.dispatchEvent(new CustomEvent('cell-mousedown', { detail, bubbles: true, composed: true }));
    grid.dispatchEvent(new CustomEvent('cell-mouseover', { detail: { col: 6, row: 6 }, bubbles: true, composed: true }));

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 2, 2), 'B2').to.be.true;
    expect(isCellSelected(selection, 6, 6), 'F6').to.be.true;
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
  });

  it('marks the cells of an earlier range as cell-range after a Ctrl-click', () => {
    clickCell(ctx.sheet, 2, 1);
    clickCell(ctx.sheet, 2, 3, { shiftKey: true });
    clickCell(ctx.sheet, 4, 3, { ctrlKey: true });

    const state = ctx.sheet.getCellGrid().renderState;
    expect(isCellSelected(state.selection, 2, 2)).to.be.true;
  });
});

describe('Spreadsheet non-coherent selection — server events', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('Ctrl-click reports the cell as added, not as a replacement', () => {
    clickCell(ctx.sheet, 2, 2);
    ctx.events.length = 0;
    clickCell(ctx.sheet, 3, 3, { ctrlKey: true });

    expect(ctx.events).to.have.length(1);
    expect(ctx.events[0].type).to.equal('cellAddedToSelectionAndSelected');
    // Coordinates only: the server reads no discard flag for this event,
    // so sending one would violate the payload contract.
    expect(ctx.events[0].data).to.deep.equal({ row: 3, column: 3 });
  });

  it('keeps Ctrl-added ranges when a plain shift-click extends the last one', () => {
    // Excel: shift extends the range you are on and leaves the ranges you
    // picked out earlier alone. Rebuilding the selection from scratch here
    // silently dropped them, so B1:B3 vanished the moment D3:E3 was drawn.
    clickCell(ctx.sheet, 2, 1);
    clickCell(ctx.sheet, 2, 3, { shiftKey: true });
    clickCell(ctx.sheet, 4, 3, { ctrlKey: true });
    clickCell(ctx.sheet, 5, 3, { shiftKey: true });

    const selection = ctx.sheet.getSelection();
    expect(selection.activeRange).to.eql({ c1: 4, c2: 5, r1: 3, r2: 3 });
    expect(selection.auxRanges).to.eql([{ c1: 2, c2: 2, r1: 1, r2: 3 }]);
  });

  it('reports a shift-click on a selection in pieces as an added range', () => {
    // The server hears a plain selection event as "the old range is
    // gone", and drops the cells earlier Ctrl-clicks put there with it.
    // Anything acting on the selection - a context-menu action, a
    // delete - then misses them.
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 3, 3, { ctrlKey: true });
    ctx.events.length = 0;
    clickCell(ctx.sheet, 4, 4, { shiftKey: true });

    // mouseup ends the drag and click follows it, so the range is
    // reported twice; what matters is that neither is a replacement.
    expect(new Set(ctx.events.map((e: any) => e.type))).to.eql(
      new Set(['cellsAddedToRangeSelection'])
    );
    expect(ctx.events[0].data).to.deep.equal({
      row1: 3,
      col1: 3,
      row2: 4,
      col2: 4
    });
  });

  it('still reports a whole selection when nothing was Ctrl-clicked', () => {
    clickCell(ctx.sheet, 2, 2);
    ctx.events.length = 0;
    clickCell(ctx.sheet, 4, 4, { shiftKey: true });

    // Once, though the press and the click both select it.
    expect(ctx.events.map((e: any) => e.type)).to.eql(['cellRangeSelected']);
  });

  it('Ctrl-shift-click reports the added range', () => {
    clickCell(ctx.sheet, 2, 2);
    ctx.events.length = 0;
    clickCell(ctx.sheet, 4, 5, { ctrlKey: true, shiftKey: true });

    expect(ctx.events).to.have.length(1);
    expect(ctx.events[0].type).to.equal('cellsAddedToRangeSelection');
    expect(ctx.events[0].data).to.deep.equal({
      row1: 2,
      col1: 2,
      row2: 5,
      col2: 4
    });
  });

  it('Ctrl-shift-click keeps ranges picked out by earlier Ctrl-clicks', () => {
    clickCell(ctx.sheet, 1, 1);
    clickCell(ctx.sheet, 3, 3, { ctrlKey: true });
    clickCell(ctx.sheet, 5, 5, { ctrlKey: true, shiftKey: true });

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 1, 1)).to.be.true;
    expect(isCellSelected(selection, 4, 4)).to.be.true;
  });

  it('survives the server echoing the selection back', () => {
    // The server answers cellAddedToSelectionAndSelected with
    // setSelectedCellAndRange. That must not wipe the ranges the
    // Ctrl-clicks built up, or the selection collapses to one cell the
    // moment the roundtrip completes.
    clickCell(ctx.sheet, 2, 2);
    clickCell(ctx.sheet, 3, 3, { ctrlKey: true });
    ctx.sheet.setSelectedCellAndRange('C3', 3, 3, 3, 3, 3, 3, false);

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 2, 2)).to.be.true;
    expect(isCellSelected(selection, 3, 3)).to.be.true;
  });

  it('a plain click still reports a normal cell selection', () => {
    clickCell(ctx.sheet, 2, 2);

    expect(ctx.events).to.have.length(1);
    expect(ctx.events[0].type).to.equal('cellSelected');
  });
});

function clickHeader(
  sheet: Spreadsheet,
  kind: 'column' | 'row',
  index: number,
  mods: { shiftKey?: boolean; ctrlKey?: boolean; metaKey?: boolean } = {}
): void {
  const el = kind === 'column' ? sheet.getColumnHeader() : sheet.getRowHeader();
  el.dispatchEvent(
    new CustomEvent(`${kind}-header-click`, {
      detail: {
        ...(kind === 'column' ? { col: index } : { row: index }),
        shiftKey: false,
        ctrlKey: false,
        metaKey: false,
        altKey: false,
        ...mods
      },
      bubbles: true,
      composed: true
    })
  );
}

describe('Spreadsheet non-coherent selection — whole rows and columns', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('Ctrl-clicking a second column keeps the first', () => {
    clickHeader(ctx.sheet, 'column', 2);
    clickHeader(ctx.sheet, 'column', 4, { ctrlKey: true });

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 2, 5)).to.be.true;
    expect(isCellSelected(selection, 4, 5)).to.be.true;
    expect(isCellSelected(selection, 3, 5)).to.be.false;
  });

  it('Ctrl-clicking a second row keeps the first', () => {
    clickHeader(ctx.sheet, 'row', 3);
    clickHeader(ctx.sheet, 'row', 5, { ctrlKey: true });

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 5, 3)).to.be.true;
    expect(isCellSelected(selection, 5, 5)).to.be.true;
    expect(isCellSelected(selection, 5, 4)).to.be.false;
  });

  it('shift-extending rows keeps a row added with Ctrl', () => {
    clickHeader(ctx.sheet, 'row', 3);
    clickHeader(ctx.sheet, 'row', 5, { ctrlKey: true });
    clickHeader(ctx.sheet, 'row', 7, { shiftKey: true });

    const selection = ctx.sheet.getSelection();
    for (const row of [3, 5, 6, 7]) {
      expect(isCellSelected(selection, 5, row), `row ${row}`).to.be.true;
    }
  });

  it('shift-extending columns keeps a column added with Ctrl', () => {
    clickHeader(ctx.sheet, 'column', 2);
    clickHeader(ctx.sheet, 'column', 4, { ctrlKey: true });
    clickHeader(ctx.sheet, 'column', 6, { shiftKey: true });

    const selection = ctx.sheet.getSelection();
    for (const col of [2, 4, 5, 6]) {
      expect(isCellSelected(selection, col, 5), `col ${col}`).to.be.true;
    }
  });

  it('a plain column click still discards the previous selection', () => {
    clickHeader(ctx.sheet, 'column', 2);
    clickHeader(ctx.sheet, 'column', 4, { ctrlKey: true });
    clickHeader(ctx.sheet, 'column', 6);

    const selection = ctx.sheet.getSelection();
    expect(isCellSelected(selection, 6, 5)).to.be.true;
    expect(isCellSelected(selection, 2, 5)).to.be.false;
    expect(selection.auxRanges).to.have.length(0);
  });

  it('Ctrl-clicking a column reports it as added', () => {
    clickHeader(ctx.sheet, 'column', 2);
    ctx.events.length = 0;
    clickHeader(ctx.sheet, 'column', 4, { ctrlKey: true });

    expect(ctx.events).to.have.length(1);
    expect(ctx.events[0].type).to.equal('columnAddedToSelection');
    expect(ctx.events[0].data).to.deep.equal({ column: 4, firstRowIndex: 1 });
  });

  it('Ctrl-clicking a row reports it as added', () => {
    clickHeader(ctx.sheet, 'row', 3);
    ctx.events.length = 0;
    clickHeader(ctx.sheet, 'row', 5, { ctrlKey: true });

    expect(ctx.events).to.have.length(1);
    expect(ctx.events[0].type).to.equal('rowAddedToRangeSelection');
    expect(ctx.events[0].data).to.deep.equal({ row: 5, firstColumnIndex: 1 });
  });
});
