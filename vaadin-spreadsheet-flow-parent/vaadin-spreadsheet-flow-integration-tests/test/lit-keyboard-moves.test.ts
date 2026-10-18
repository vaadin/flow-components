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
  composing,
  isSingleMerge,
  mergeAt,
  moveActiveCell,
  moveRangeEdge,
  selectCellAt,
  type SheetShape
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/KeyboardMoves.js';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * The arrow keys by the GWT client's rules (SelectionHandler): a merge is
 * one cell - entered as a whole, left on its far side in the column or row
 * it was entered by - and hidden rows and columns are stepped over.
 */

/** B4:D6, as in merged_cells.xlsx. */
const B4_D6 = { col1: 2, col2: 4, row1: 4, row2: 6 };

function shape(over: Partial<SheetShape> = {}): SheetShape {
  return {
    cols: 20,
    rows: 20,
    mergedRegions: [B4_D6],
    hiddenColumns: new Set<number>(),
    hiddenRows: new Set<number>(),
    ...over
  };
}

describe('KeyboardMoves — selecting a cell', () => {
  it('selects an ordinary cell by itself', () => {
    expect(selectCellAt(1, 1, [B4_D6])).to.eql({
      activeCell: { col: 1, row: 1 },
      activeRange: { c1: 1, c2: 1, r1: 1, r2: 1 },
      entry: null
    });
  });

  it('selects the whole merge for any cell in it, its top-left cell active', () => {
    expect(selectCellAt(3, 5, [B4_D6])).to.eql({
      activeCell: { col: 2, row: 4 },
      activeRange: { c1: 2, c2: 4, r1: 4, r2: 6 },
      entry: { col: 3, row: 5 }
    });
  });

  it('finds the merge a cell is in', () => {
    expect(mergeAt([B4_D6], 4, 6)).to.equal(B4_D6);
    expect(mergeAt([B4_D6], 5, 6)).to.be.null;
  });

  it('tells a lone merge from a range', () => {
    expect(isSingleMerge({ c1: 2, c2: 4, r1: 4, r2: 6 }, { col: 2, row: 4 }, [B4_D6])).to.be.true;
    expect(isSingleMerge({ c1: 1, c2: 4, r1: 4, r2: 6 }, { col: 2, row: 4 }, [B4_D6])).to.be.false;
    expect(isSingleMerge({ c1: 1, c2: 2, r1: 1, r2: 1 }, { col: 1, row: 1 }, [B4_D6])).to.be.false;
  });
});

describe('KeyboardMoves — moving the active cell', () => {
  it('steps to the next cell', () => {
    expect(moveActiveCell({ col: 5, row: 5 }, null, 'down', shape())!.activeCell).to.eql({ col: 5, row: 6 });
    expect(moveActiveCell({ col: 5, row: 5 }, null, 'up', shape())!.activeCell).to.eql({ col: 5, row: 4 });
    expect(moveActiveCell({ col: 5, row: 5 }, null, 'right', shape())!.activeCell).to.eql({ col: 6, row: 5 });
    expect(moveActiveCell({ col: 6, row: 5 }, null, 'left', shape())!.activeCell).to.eql({ col: 5, row: 5 });
  });

  it('goes nowhere at the edge of the sheet', () => {
    expect(moveActiveCell({ col: 1, row: 1 }, null, 'up', shape())).to.be.null;
    expect(moveActiveCell({ col: 1, row: 1 }, null, 'left', shape())).to.be.null;
    expect(moveActiveCell({ col: 20, row: 20 }, null, 'down', shape())).to.be.null;
    expect(moveActiveCell({ col: 20, row: 20 }, null, 'right', shape())).to.be.null;
  });

  it('steps over hidden rows and columns', () => {
    const s = shape({ hiddenRows: new Set([11, 12]), hiddenColumns: new Set([8]) });
    expect(moveActiveCell({ col: 1, row: 10 }, null, 'down', s)!.activeCell).to.eql({ col: 1, row: 13 });
    expect(moveActiveCell({ col: 1, row: 13 }, null, 'up', s)!.activeCell).to.eql({ col: 1, row: 10 });
    expect(moveActiveCell({ col: 7, row: 1 }, null, 'right', s)!.activeCell).to.eql({ col: 9, row: 1 });
    expect(moveActiveCell({ col: 9, row: 1 }, null, 'left', s)!.activeCell).to.eql({ col: 7, row: 1 });
  });

  it('goes nowhere when nothing but hidden rows lie beyond', () => {
    const s = shape({ hiddenRows: new Set([19, 20]) });
    expect(moveActiveCell({ col: 1, row: 18 }, null, 'down', s)).to.be.null;
  });

  it('selects a merge as a whole when it moves into one', () => {
    const next = moveActiveCell({ col: 1, row: 5 }, null, 'right', shape())!;
    expect(next.activeCell).to.eql({ col: 2, row: 4 });
    expect(next.activeRange).to.eql({ c1: 2, c2: 4, r1: 4, r2: 6 });
    expect(next.entry).to.eql({ col: 2, row: 5 });
  });

  it('leaves a merge on its far side, in the row it was entered by', () => {
    // In from A5, out to E5 - not E4, where the merge starts.
    expect(moveActiveCell({ col: 2, row: 4 }, { col: 2, row: 5 }, 'right', shape())!.activeCell).to.eql({
      col: 5,
      row: 5
    });
    expect(moveActiveCell({ col: 2, row: 4 }, { col: 4, row: 6 }, 'left', shape())!.activeCell).to.eql({
      col: 1,
      row: 6
    });
  });

  it('leaves a merge on its far side, in the column it was entered by', () => {
    expect(moveActiveCell({ col: 2, row: 4 }, { col: 3, row: 4 }, 'down', shape())!.activeCell).to.eql({
      col: 3,
      row: 7
    });
    expect(moveActiveCell({ col: 2, row: 4 }, { col: 4, row: 6 }, 'up', shape())!.activeCell).to.eql({
      col: 4,
      row: 3
    });
  });

  it('leaves a clicked merge from its top-left cell', () => {
    expect(moveActiveCell({ col: 2, row: 4 }, null, 'down', shape())!.activeCell).to.eql({ col: 2, row: 7 });
    expect(moveActiveCell({ col: 2, row: 4 }, null, 'right', shape())!.activeCell).to.eql({ col: 5, row: 4 });
  });

  it('forgets an entry cell that is not in the merge', () => {
    // A stale entry from some other merge does not steer the way out.
    expect(moveActiveCell({ col: 2, row: 4 }, { col: 9, row: 9 }, 'down', shape())!.activeCell).to.eql({
      col: 2,
      row: 7
    });
  });

  it('goes from one merge straight into the next', () => {
    const s = shape({ mergedRegions: [B4_D6, { col1: 5, col2: 6, row1: 5, row2: 5 }] });
    const next = moveActiveCell({ col: 2, row: 4 }, { col: 2, row: 5 }, 'right', s)!;
    expect(next.activeRange).to.eql({ c1: 5, c2: 6, r1: 5, r2: 5 });
  });
});

describe('KeyboardMoves — Shift+arrow', () => {
  const none = shape({ mergedRegions: [] });

  it('grows the range away from the active cell', () => {
    const a1 = { c1: 1, c2: 1, r1: 1, r2: 1 };
    expect(moveRangeEdge(a1, { col: 1, row: 1 }, 'right', none)!.range).to.eql({ c1: 1, c2: 2, r1: 1, r2: 1 });
    expect(moveRangeEdge(a1, { col: 1, row: 1 }, 'down', none)!.range).to.eql({ c1: 1, c2: 1, r1: 1, r2: 2 });
  });

  it('takes the far side back toward the active cell', () => {
    const range = { c1: 1, c2: 3, r1: 1, r2: 1 };
    expect(moveRangeEdge(range, { col: 1, row: 1 }, 'left', none)!.range).to.eql({ c1: 1, c2: 2, r1: 1, r2: 1 });
  });

  it('grows past the active cell once the range is one column wide', () => {
    const range = { c1: 3, c2: 3, r1: 1, r2: 1 };
    expect(moveRangeEdge(range, { col: 3, row: 1 }, 'left', none)!.range).to.eql({ c1: 2, c2: 3, r1: 1, r2: 1 });
  });

  it('moves the left side when the active cell is on the right', () => {
    const range = { c1: 2, c2: 4, r1: 1, r2: 1 };
    expect(moveRangeEdge(range, { col: 4, row: 1 }, 'right', none)!.range).to.eql({ c1: 3, c2: 4, r1: 1, r2: 1 });
    expect(moveRangeEdge(range, { col: 4, row: 1 }, 'left', none)!.range).to.eql({ c1: 1, c2: 4, r1: 1, r2: 1 });
  });

  it('steps over hidden columns and rows', () => {
    const s = shape({ mergedRegions: [], hiddenColumns: new Set([2]), hiddenRows: new Set([2, 3]) });
    const a1 = { c1: 1, c2: 1, r1: 1, r2: 1 };
    expect(moveRangeEdge(a1, { col: 1, row: 1 }, 'right', s)!.range).to.eql({ c1: 1, c2: 3, r1: 1, r2: 1 });
    expect(moveRangeEdge(a1, { col: 1, row: 1 }, 'down', s)!.range).to.eql({ c1: 1, c2: 1, r1: 1, r2: 4 });
  });

  it('takes in a merge it reaches, and goes on past it the next time', () => {
    // merged_cells.xlsx from A4: Shift+Right takes in B4:D6, Shift+Down
    // then reaches row 7 - the GWT client's A4:D7.
    const first = moveRangeEdge({ c1: 1, c2: 1, r1: 4, r2: 4 }, { col: 1, row: 4 }, 'right', shape())!;
    expect(first.range).to.eql({ c1: 1, c2: 4, r1: 4, r2: 6 });
    const second = moveRangeEdge(first.range, { col: 1, row: 4 }, 'down', shape())!;
    expect(second.range).to.eql({ c1: 1, c2: 4, r1: 4, r2: 7 });
  });

  it('lets go of a whole merge when the side it is on moves back', () => {
    // A4:E6 holds B4:D6; taking the right side back from E lets go of the
    // merge altogether rather than cutting it.
    const range = { c1: 1, c2: 5, r1: 4, r2: 6 };
    expect(moveRangeEdge(range, { col: 1, row: 4 }, 'left', shape())!.range).to.eql({
      c1: 1,
      c2: 4,
      r1: 4,
      r2: 6
    });
    expect(moveRangeEdge({ c1: 1, c2: 4, r1: 4, r2: 6 }, { col: 1, row: 4 }, 'left', shape())!.range).to.eql({
      c1: 1,
      c2: 1,
      r1: 4,
      r2: 6
    });
  });

  it('takes the far side in by one where a merge lies wholly inside the range', () => {
    // A1:D10 holding B4:D6, the active cell at the bottom: Shift+Down takes
    // the top side to row 2. The merge further down is not in its way.
    const range = { c1: 1, c2: 4, r1: 1, r2: 10 };
    expect(moveRangeEdge(range, { col: 1, row: 10 }, 'down', shape())!.range).to.eql({ c1: 1, c2: 4, r1: 2, r2: 10 });
    // And across: A4:F6, the active cell on the right, Shift+Right.
    const across = { c1: 1, c2: 6, r1: 4, r2: 6 };
    expect(moveRangeEdge(across, { col: 6, row: 4 }, 'right', shape())!.range).to.eql({ c1: 2, c2: 6, r1: 4, r2: 6 });
  });

  it('grows a merge that is the whole selection either way', () => {
    const merge = { c1: 2, c2: 4, r1: 4, r2: 6 };
    expect(moveRangeEdge(merge, { col: 2, row: 4 }, 'left', shape())!.range).to.eql({ c1: 1, c2: 4, r1: 4, r2: 6 });
    expect(moveRangeEdge(merge, { col: 2, row: 4 }, 'right', shape())!.range).to.eql({ c1: 2, c2: 5, r1: 4, r2: 6 });
    expect(moveRangeEdge(merge, { col: 2, row: 4 }, 'up', shape())!.range).to.eql({ c1: 2, c2: 4, r1: 3, r2: 6 });
  });

  it('goes nowhere at the edge of the sheet', () => {
    expect(moveRangeEdge({ c1: 1, c2: 1, r1: 1, r2: 1 }, { col: 1, row: 1 }, 'up', none)).to.be.null;
  });

  it('says which side moved, for scrolling it into view', () => {
    expect(moveRangeEdge({ c1: 1, c2: 1, r1: 3, r2: 3 }, { col: 1, row: 3 }, 'right', none)!.moved).to.eql({
      col: 2,
      row: 3
    });
    expect(moveRangeEdge({ c1: 1, c2: 1, r1: 3, r2: 3 }, { col: 1, row: 3 }, 'up', none)!.moved).to.eql({
      col: 1,
      row: 2
    });
  });
});

describe('Spreadsheet — the keyboard and merged cells', () => {
  let sheet: Spreadsheet;
  let host: HTMLElement;
  let events: any[];

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:600px;height:400px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(20);
    sheet.setCols(20);
    sheet.setColW(new Array(20).fill(50));
    sheet.setRowH(new Array(20).fill(15));
    sheet.setMergedRegions([{ id: 1, ...B4_D6 }]);
    sheet.recomputeViewport();
    events = [];
    host.addEventListener('spreadsheet-event', (e) => events.push((e as CustomEvent).detail));
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  function press(key: string, init: KeyboardEventInit = {}): void {
    sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, composed: true, cancelable: true, ...init }));
  }

  function click(col: number, row: number): void {
    sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-click', {
        detail: { col, row, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false },
        bubbles: true,
        composed: true
      })
    );
  }

  it('selects the whole merge on a click, as the GWT client did', () => {
    click(2, 4);

    expect(sheet.getSelection().activeRange).to.eql({ c1: 2, c2: 4, r1: 4, r2: 6 });
  });

  it('tells the server about the cell, not a range, for a merge', () => {
    click(2, 4);

    expect(events.map((e) => e.type)).to.eql(['cellSelected']);
    expect(events[0].data).to.include({ row: 4, column: 2 });
  });

  it('crosses a merge in two presses and comes out in the row it went in by', () => {
    // merged_cells.xlsx: A4, Right, Right, Down ends on E5 in the GWT client.
    click(1, 5);
    press('ArrowRight');
    expect(sheet.getSelection().activeRange).to.eql({ c1: 2, c2: 4, r1: 4, r2: 6 });
    press('ArrowRight');
    expect(sheet.getSelection().activeCell).to.eql({ col: 5, row: 5 });
  });

  it('opens the editor on Enter, as on any single cell', async () => {
    click(2, 4);
    press('Enter');
    await sheet.getCellGrid().updateComplete;

    expect(sheet.getCellGrid().shadowRoot!.querySelector('.cell-editor')).to.not.be.null;
    expect(sheet.getSelection().activeCell).to.eql({ col: 2, row: 4 });
  });

  it('moves out of the merge when an edit in it is committed', async () => {
    click(2, 4);
    press('x');
    await sheet.getCellGrid().updateComplete;
    const input = sheet.getCellGrid().shadowRoot!.querySelector<HTMLInputElement>('.cell-editor')!;
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, composed: true, cancelable: true }));

    expect(sheet.getSelection().activeCell).to.eql({ col: 2, row: 7 });
  });

  it('leaves the selection alone at the edge of the sheet', () => {
    click(1, 1);
    press('ArrowUp');

    expect(sheet.getSelection().activeCell).to.eql({ col: 1, row: 1 });
    expect(events.filter((e) => e.type === 'cellSelected')).to.have.length(1);
  });

  it('steps over a hidden row', () => {
    sheet.setHiddenRowIndexes([2]);
    click(1, 1);
    press('ArrowDown');

    expect(sheet.getSelection().activeCell).to.eql({ col: 1, row: 3 });
  });

  it('grows a selection past a merge it took in, as the GWT client did', () => {
    click(1, 4);
    press('ArrowRight', { shiftKey: true });
    press('ArrowDown', { shiftKey: true });

    expect(sheet.getSelection().activeRange).to.eql({ c1: 1, c2: 4, r1: 4, r2: 7 });
  });
});

/**
 * A key an input method composes with is the input method's: the Enter that
 * confirms a conversion must not commit the cell.
 */
describe('composing — keys that belong to an input method', () => {
  it('takes a key pressed while text is being composed', () => {
    expect(composing({ isComposing: true, keyCode: 13 })).to.be.true;
  });

  it("takes Safari's confirming Enter, sent after the composition with key code 229", () => {
    expect(composing({ isComposing: false, keyCode: 229 })).to.be.true;
  });

  it('leaves a plain key alone', () => {
    expect(composing({ isComposing: false, keyCode: 13 })).to.be.false;
    expect(composing({})).to.be.false;
  });
});
