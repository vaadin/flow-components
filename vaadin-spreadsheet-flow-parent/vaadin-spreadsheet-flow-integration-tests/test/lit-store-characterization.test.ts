/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import {
  afterFrames,
  attachSheet,
  editorInput,
  pressOnSheet,
  rendered,
  serverBatch,
  type AttachedSheet
} from './spreadsheet-harness.js';

/**
 * What the client does today in the order it does it, pinned before its state
 * moves into one store: the parts are drawn before the server is told, an
 * edit is committed once whatever interrupts it, and what a reload resets.
 */

function cellEvent(attached: AttachedSheet, type: string, detail: Record<string, unknown>): void {
  attached.sheet.getCellGrid().dispatchEvent(
    new CustomEvent(type, {
      detail: { shiftKey: false, ctrlKey: false, metaKey: false, altKey: false, ...detail },
      bubbles: true,
      composed: true
    })
  );
}

/** Selects a cell, 1-based, and types over it, as Excel's "enter" mode. */
async function typeOver(attached: AttachedSheet, col: number, row: number, text: string): Promise<HTMLInputElement> {
  attached.sheet.setSelectedCellAndRange('', col, row, col, col, row, row, false);
  pressOnSheet(attached.sheet, text[0]);
  await attached.sheet.getCellGrid().updateComplete;
  const input = editorInput(attached.sheet)!;
  input.value = text;
  input.dispatchEvent(new Event('input', { bubbles: true, composed: true }));
  return input;
}

function press(target: HTMLElement, key: string, shiftKey = false): KeyboardEvent {
  const event = new KeyboardEvent('keydown', { key, shiftKey, bubbles: true, composed: true, cancelable: true });
  target.dispatchEvent(event);
  return event;
}

describe('Spreadsheet store characterization — drawn before the server is told', () => {
  let ctx: AttachedSheet;

  afterEach(() => ctx.cleanup());

  it('asks for the rows a jump scrolls to before it reports the cell, at once', async () => {
    // 200 rows of 20 px in a host 300 px high: the last rows are far below.
    ctx = attachSheet({ rows: 200 });
    await afterFrames();
    ctx.sheet.setSelectedCellAndRange('', 1, 1, 1, 1, 1, 1, false);
    ctx.sent.length = 0;

    pressOnSheet(ctx.sheet, 'End', { ctrlKey: true });

    expect(ctx.types()).to.eql(['onSheetScroll', 'cellSelected']);
  });

  it('reports a move at once and the rows it scrolls to later, when they are loaded', async () => {
    ctx = attachSheet({ rows: 200 });
    await afterFrames();
    ctx.sheet.setSelectedCellAndRange('', 1, 12, 1, 1, 12, 12, false);
    ctx.sent.length = 0;

    // Rows 13 to 18: the last of them below the screen, scrolled to.
    for (let row = 13; row <= 18; row++) {
      pressOnSheet(ctx.sheet, 'ArrowDown');
    }
    expect(ctx.types(), 'at once').to.eql(new Array(6).fill('cellSelected'));
    expect(ctx.sheet.getScrollContainer().scrollTop, 'scrolled').to.be.greaterThan(0);
    await new Promise((resolve) => setTimeout(resolve, 300));

    expect(ctx.types().slice(6)).to.eql(['onSheetScroll']);
  });

  it('has the cell grid showing the new cell when a move is reported', () => {
    ctx = attachSheet();
    ctx.sheet.setSelectedCellAndRange('', 2, 2, 2, 2, 2, 2, false);
    let drawn: unknown = null;
    ctx.host.addEventListener('spreadsheet-event', (e) => {
      if ((e as CustomEvent).detail.type === 'cellSelected') {
        drawn = ctx.sheet.getCellGrid().renderState.selection?.activeCell;
      }
    });

    pressOnSheet(ctx.sheet, 'ArrowDown');

    expect(drawn).to.eql({ col: 2, row: 3 });
  });

  it('has the editor closed in the cell grid when an edit is reported', async () => {
    ctx = attachSheet();
    const input = await typeOver(ctx, 2, 2, '123');
    let editorAtCommit: unknown = 'not reported';
    ctx.host.addEventListener('spreadsheet-event', (e) => {
      if ((e as CustomEvent).detail.type === 'cellValueEdited') {
        editorAtCommit = ctx.sheet.getCellGrid().renderState.editor;
      }
    });

    press(input, 'Enter');

    expect(editorAtCommit).to.equal(null);
  });

  it('has the editor closed in the cell grid when an edit left for the address field is reported', async () => {
    ctx = attachSheet();
    const input = await typeOver(ctx, 2, 2, '123');
    input.focus();
    await ctx.sheet.getFormulaBar().updateComplete;
    let editorAtCommit: unknown = 'not reported';
    ctx.host.addEventListener('spreadsheet-event', (e) => {
      if ((e as CustomEvent).detail.type === 'cellValueEdited') {
        editorAtCommit = ctx.sheet.getCellGrid().renderState.editor;
      }
    });

    // The edit ends once the keyboard has settled there.
    ctx.sheet.getFormulaBar().shadowRoot!.querySelector<HTMLInputElement>('.address')!.focus();
    await new Promise((resolve) => setTimeout(resolve));

    expect(editorAtCommit).to.equal(null);
  });

  it('has the cell grid showing a reference pointed at with an arrow as soon as the key is handled', async () => {
    ctx = attachSheet();
    const input = await typeOver(ctx, 3, 3, '=');

    press(input, 'ArrowDown');

    expect(input.value).to.equal('=C4');
    expect(ctx.sheet.getCellGrid().renderState.formulaReferences).to.not.be.empty;
  });

  it('has the cell grid showing the cells a fill adds as soon as it is dragged over a cell', () => {
    ctx = attachSheet();
    ctx.sheet.setSelectedCellAndRange('', 2, 2, 2, 2, 2, 2, false);
    ctx.sheet.getCellGrid().dispatchEvent(new CustomEvent('selection-paint-start', { bubbles: true, composed: true }));

    cellEvent(ctx, 'cell-mouseover', { col: 2, row: 5 });

    // B3:B5, past B2 it is dragged from.
    expect(ctx.sheet.getCellGrid().renderState.paintRect).to.eql({ c1: 2, c2: 2, r1: 3, r2: 5 });
  });
});

describe('Spreadsheet store characterization — an edit committed once', () => {
  let ctx: AttachedSheet;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => ctx.cleanup());

  function edits(): unknown[] {
    return ctx.sent.filter((e) => e.type === 'cellValueEdited').map((e) => e.data);
  }

  it('commits an edit once, for its cell, when another cell is pressed', async () => {
    const input = await typeOver(ctx, 2, 2, 'x');
    input.focus();
    ctx.sent.length = 0;

    // The press comes first, the focus leaving the editor after it.
    cellEvent(ctx, 'cell-mousedown', { col: 4, row: 4 });
    input.blur();
    await rendered(ctx.sheet);

    expect(edits()).to.eql([{ row: 2, col: 2, value: 'x' }]);
  });

  it('commits an edit when the server opens a note for typing', async () => {
    const input = await typeOver(ctx, 2, 2, 'x');
    input.focus();
    ctx.sent.length = 0;

    ctx.sheet.editCellComment(3, 3);
    await rendered(ctx.sheet);
    const note = ctx.sheet.getCellGrid().shadowRoot!.querySelector<HTMLTextAreaElement>('.comment-overlay-edit');
    note!.focus();
    await rendered(ctx.sheet);

    expect(edits()).to.eql([{ row: 2, col: 2, value: 'x' }]);
  });
});

describe('Spreadsheet store characterization — what a reload resets', () => {
  let ctx: AttachedSheet;

  afterEach(() => ctx.cleanup());

  it('reports a shift-click from the cell the server selected as a range grown from it', () => {
    ctx = attachSheet();
    ctx.sheet.setSelectedCellAndRange('', 2, 2, 2, 2, 2, 2, false);
    ctx.sent.length = 0;

    cellEvent(ctx, 'cell-click', { col: 4, row: 4, shiftKey: true });

    expect(ctx.types()).to.eql(['cellRangeSelected']);
  });

  it('reports a shift-click after a reload as a range with its own starting point', () => {
    ctx = attachSheet();
    ctx.sheet.setSelectedCellAndRange('', 2, 2, 2, 2, 2, 2, false);
    serverBatch(ctx, { reload: true });
    expect(ctx.types().slice(-2), 'introduced again').to.eql(['onConnectorInit', 'onSheetScroll']);
    ctx.sent.length = 0;

    cellEvent(ctx, 'cell-click', { col: 4, row: 4, shiftKey: true });

    // The server has forgotten the cell it selected.
    expect(ctx.types()).to.eql(['cellRangePainted']);
  });

  it('keeps the cell a merge was entered by when the server says the merge is selected', () => {
    ctx = attachSheet();
    ctx.sheet.setMergedRegions([{ col1: 1, col2: 2, row1: 1, row2: 2 }] as never);
    ctx.sheet.recomputeViewport();
    ctx.sheet.setSelectedCellAndRange('', 3, 1, 3, 3, 1, 1, false);
    // Into the merge A1:B2 from C1: entered by B1.
    pressOnSheet(ctx.sheet, 'ArrowLeft');

    ctx.sheet.setSelectedCellAndRange('', 1, 1, 1, 2, 1, 2, false);
    pressOnSheet(ctx.sheet, 'ArrowDown');

    expect(ctx.sheet.getSelection().activeCell).to.eql({ col: 2, row: 3 });
  });

  it('keeps the rows on screen where they are for heights that come after a reload in a batch', () => {
    ctx = attachSheet({ rows: 300, rowH: 15 });
    const scroller = ctx.sheet.getScrollContainer();
    scroller.scrollTop = 2000;
    scroller.dispatchEvent(new Event('scroll'));
    const before = scroller.scrollTop;
    const heights = new Array(300).fill(15);
    heights[49] = 30;

    serverBatch(ctx, { reload: true, rowH: heights });

    // Row 50, above the screen, grew from 15 to 30 pt, 20 to 40 px: the rows
    // on screen stay where they are.
    expect(scroller.scrollTop).to.equal(before + 20);
  });
});
