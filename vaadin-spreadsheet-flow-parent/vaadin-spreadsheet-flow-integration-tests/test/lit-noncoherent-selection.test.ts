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
 * Slice 185 — Ctrl/Cmd-click builds a non-coherent selection.
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
  host.style.cssText = 'width:400px;height:300px;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));

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
    expect(ctx.events[0].data).to.deep.equal({
      row: 3,
      column: 3,
      oldSelectionRangeDiscarded: false
    });
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

  it('a plain click still reports a normal cell selection', () => {
    clickCell(ctx.sheet, 2, 2);

    expect(ctx.events).to.have.length(1);
    expect(ctx.events[0].type).to.equal('cellSelected');
  });
});
