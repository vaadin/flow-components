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
  computePaintRectangle,
  grownSelection,
  isNonEmpty
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/SelectionPaint.js';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * The fill handle — the square at the bottom-right of the selection that
 * copies its values over neighbouring cells, or clears them when dragged
 * back over itself.
 *
 * The connector never reported the drag, so the handle was inert: it
 * rendered, it could be grabbed, and nothing happened.
 */

const SELECTION = { c1: 2, c2: 4, r1: 3, r2: 6 };

describe('SelectionPaint — the strip a drag paints', () => {
  it('paints nothing while the pointer is still on the corner cell', () => {
    expect(computePaintRectangle(SELECTION, 4, 6)).to.eql({ kind: 'none' });
  });

  it('grows downwards past the bottom edge', () => {
    expect(computePaintRectangle(SELECTION, 4, 9)).to.eql({
      kind: 'increase',
      painted: { c1: 2, c2: 4, r1: 7, r2: 9 }
    });
  });

  it('grows upwards past the top edge', () => {
    expect(computePaintRectangle(SELECTION, 3, 1)).to.eql({
      kind: 'increase',
      painted: { c1: 2, c2: 4, r1: 1, r2: 2 }
    });
  });

  it('grows rightwards past the right edge', () => {
    expect(computePaintRectangle(SELECTION, 7, 4)).to.eql({
      kind: 'increase',
      painted: { c1: 5, c2: 7, r1: 3, r2: 6 }
    });
  });

  it('grows leftwards past the left edge', () => {
    expect(computePaintRectangle(SELECTION, 1, 4)).to.eql({
      kind: 'increase',
      painted: { c1: 1, c2: 1, r1: 3, r2: 6 }
    });
  });

  it('locks to the axis the drag favours', () => {
    // Four columns right, one row down: the horizontal move wins and the
    // strip stays on the selection's own rows.
    expect(computePaintRectangle(SELECTION, 8, 7)).to.eql({
      kind: 'increase',
      painted: { c1: 5, c2: 8, r1: 3, r2: 6 }
    });
  });

  it('shrinks from the bottom when dragged back up inside', () => {
    expect(computePaintRectangle(SELECTION, 4, 4)).to.eql({
      kind: 'decrease',
      painted: { c1: 2, c2: 4, r1: 5, r2: 6 }
    });
  });

  it('shrinks from the right when dragged back left inside', () => {
    expect(computePaintRectangle(SELECTION, 2, 6)).to.eql({
      kind: 'decrease',
      painted: { c1: 3, c2: 4, r1: 3, r2: 6 }
    });
  });

  it('never shrinks a selection away entirely', () => {
    // Dragging to the top-left cell still leaves the first row selected.
    const outcome = computePaintRectangle(SELECTION, 2, 3);
    expect(outcome.kind).to.equal('decrease');
    expect((outcome as any).painted.r1).to.be.greaterThan(SELECTION.r1);
  });

  it('unions the painted strip into the selection when it grows', () => {
    expect(grownSelection(SELECTION, { c1: 2, c2: 4, r1: 7, r2: 9 })).to.eql({
      c1: 2,
      c2: 4,
      r1: 3,
      r2: 9
    });
  });

  it('recognises an empty rectangle', () => {
    expect(isNonEmpty({ c1: 2, c2: 1, r1: 1, r2: 1 })).to.be.false;
    expect(isNonEmpty({ c1: 1, c2: 1, r1: 1, r2: 1 })).to.be.true;
  });
});

type Dispatched = { type: string; data: Record<string, unknown> };

function attachSheet(): {
  sheet: Spreadsheet;
  sent: Dispatched[];
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(10);
  sheet.setRows(20);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(20).fill(15));
  sheet.recomputeViewport();

  const sent: Dispatched[] = [];
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

describe('Spreadsheet — dragging the fill handle', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(async () => {
    ctx = attachSheet();
    // B3:D6 selected, the fill handle on D6.
    ctx.sheet.setSelectedCellAndRange('B3', 2, 3, 2, 4, 3, 6, false);
    await ctx.sheet.getCellGrid().updateComplete;
    ctx.sent.length = 0;
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function grabHandle(): void {
    const corner = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>('.selection-corner');
    expect(corner, 'no fill handle rendered').to.not.be.null;
    corner!.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, composed: true }));
  }

  function dragOver(col: number, row: number): void {
    ctx.sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-mouseover', {
        detail: { col, row },
        bubbles: true,
        composed: true
      })
    );
  }

  function release(): void {
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
  }

  function paints(sent: Dispatched[]): Dispatched[] {
    return sent.filter((e) => e.type.startsWith('selection'));
  }

  it('reports the grown rectangle after dragging down', () => {
    grabHandle();
    dragOver(4, 9);
    release();

    expect(paints(ctx.sent)).to.eql([
      { type: 'selectionIncreasePainted', data: { r1: 3, c1: 2, r2: 9, c2: 4 } }
    ]);
  });

  it('reports the corner it retreats to after dragging back inside', () => {
    grabHandle();
    dragOver(4, 4);
    release();

    expect(paints(ctx.sent)).to.eql([
      { type: 'selectionDecreasePainted', data: { row: 5, col: 2 } }
    ]);
  });

  it('reports nothing when released without moving', () => {
    grabHandle();
    release();

    expect(paints(ctx.sent)).to.be.empty;
  });

  it('stops painting once the button is up, and selects nothing after', () => {
    grabHandle();
    dragOver(4, 9);
    release();
    ctx.sent.length = 0;
    dragOver(4, 12);
    release();

    expect(paints(ctx.sent)).to.be.empty;
    expect(ctx.sheet.getSelection().activeRange).to.eql({ c1: 2, c2: 4, r1: 3, r2: 6 });
  });

  it('does not extend the selection the way a plain drag would', () => {
    grabHandle();
    dragOver(4, 9);

    // The selection is untouched until the server answers; only the
    // preview outline moves.
    expect(ctx.sheet.getSelection().activeRange).to.eql({ c1: 2, c2: 4, r1: 3, r2: 6 });
  });

  it('shows the paint outline while dragging and hides it after', async () => {
    const outline = () =>
      ctx.sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('.paintmode')!;
    expect(outline().style.display).to.equal('none');

    grabHandle();
    dragOver(4, 9);
    await ctx.sheet.getCellGrid().updateComplete;
    expect(outline().style.display).to.not.equal('none');

    release();
    await ctx.sheet.getCellGrid().updateComplete;
    expect(outline().style.display).to.equal('none');
  });

  it('keeps the outline element in the tree when nothing is painted', () => {
    expect(ctx.sheet.getCellGrid().shadowRoot!.querySelector('.paintmode')).to.not.be.null;
  });

  it('draws the outline, the handle and the drag preview in their default colours', async () => {
    // Blue for what is selected; green for what a drag will fill - the
    // handle and its preview share it.
    const root = ctx.sheet.getCellGrid().shadowRoot!;
    expect(getComputedStyle(root.querySelector('.sheet-selection .s-top')!).backgroundColor).to.equal(
      'rgb(25, 125, 225)'
    );
    const handle = getComputedStyle(root.querySelector('.selection-corner')!);
    expect(handle.backgroundColor).to.equal('rgb(64, 181, 39)');
    expect(`${handle.width} ${handle.height}`).to.equal('6px 6px');
    expect(`${handle.outlineWidth} ${handle.outlineStyle} ${handle.outlineColor}`).to.equal(
      '2px solid rgb(255, 255, 255)'
    );

    grabHandle();
    dragOver(4, 9);
    await ctx.sheet.getCellGrid().updateComplete;
    const preview = getComputedStyle(root.querySelector('.paintmode')!);
    expect(`${preview.borderTopWidth} ${preview.borderTopStyle} ${preview.borderTopColor}`).to.equal(
      '2px solid rgb(64, 181, 39)'
    );
    expect(preview.backgroundColor).to.equal('rgba(235, 247, 233, 0.8)');
    release();
  });

  it('colours the handle with the fill colour property', () => {
    const grid = ctx.sheet.getCellGrid();
    grid.style.setProperty('--vaadin-spreadsheet-selection-border-color', 'rgb(1, 2, 3)');

    const handle = grid.shadowRoot!.querySelector<HTMLElement>('.selection-corner')!;
    expect(getComputedStyle(handle).backgroundColor).to.equal('rgb(1, 2, 3)');
  });

  it('puts the handle where a pointer actually lands on it', () => {
    // A hit test, not a DOM query. The handle rendered and could be found
    // by a selector while the cell beneath it won every real click: cells
    // carry z-index 1 and the handle had none, so a drag selected cells
    // instead of filling them. Dispatching on the element directly — which
    // the tests above do — cannot see that.
    const corner = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>('.selection-corner')!;
    const box = corner.getBoundingClientRect();
    const topmost = ctx.sheet
      .getCellGrid()
      .shadowRoot!.elementFromPoint(box.x + box.width / 2, box.y + box.height / 2);

    expect(topmost, 'nothing under the handle at all').to.not.be.null;
    expect(topmost!.classList.contains('selection-corner'), `hit ${topmost!.className} instead`)
      .to.be.true;
  });
});

/**
 * The selection while the sheet does not have the keyboard.
 *
 * The selection is dimmed whenever the sheet loses focus, so that a sheet
 * the user has left for another field does not look like it still takes
 * the typing.
 */
describe('Spreadsheet — the selection while the sheet is not focused', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(async () => {
    ctx = attachSheet();
    ctx.sheet.setSelectedCellAndRange('B3', 2, 3, 2, 3, 2, 3, false);
    await ctx.sheet.getCellGrid().updateComplete;
  });

  afterEach(() => {
    ctx.cleanup();
  });

  /**
   * How strongly the outline and the handle are drawn: dimmed as one.
   * Dimmed piece by piece, the outline would be darker where its sides
   * cross each other and the handle.
   */
  function opacity(): string {
    const root = ctx.sheet.getCellGrid().shadowRoot!;
    const overlay = root.querySelector('.sheet-selection')!;
    for (const piece of overlay.querySelectorAll('.s-top, .selection-corner')) {
      expect(getComputedStyle(piece).opacity, 'each piece at full strength within').to.equal('1');
    }
    return getComputedStyle(overlay).opacity;
  }

  /** Long enough for the focus change to be read. */
  function settle(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 0));
  }

  it('dims the selection until the sheet has the keyboard', async () => {
    expect(opacity()).to.equal('0.6');

    ctx.sheet.getScrollContainer().focus();
    await settle();

    expect(opacity()).to.equal('1');
  });

  it('dims it again when the keyboard goes elsewhere', async () => {
    const elsewhere = document.createElement('input');
    document.body.appendChild(elsewhere);
    ctx.sheet.getScrollContainer().focus();
    await settle();

    elsewhere.focus();
    await settle();

    expect(opacity()).to.equal('0.6');
    elsewhere.remove();
  });

  it('keeps it bright while the sheet\'s own cell editor has the keyboard', async () => {
    ctx.sheet.getScrollContainer().focus();
    await settle();

    ctx.sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-doubleclick', { detail: { col: 2, row: 3 }, bubbles: true, composed: true })
    );
    await ctx.sheet.getCellGrid().updateComplete;
    await settle();

    const editor = ctx.sheet.getCellGrid().shadowRoot!.querySelector('.cell-editor');
    expect(editor, 'the editor opened').to.not.be.null;
    expect(ctx.sheet.getCellGrid().shadowRoot!.activeElement).to.equal(editor);
    expect(opacity()).to.equal('1');
  });
});

/**
 * The outline stays above the cells as one layer. Dimmed as a whole, the
 * overlay is a layer of its own, and at the cells' level the cells would
 * cover the outline and the handle.
 */
describe('Spreadsheet — the dimmed selection above the cells', () => {
  it('draws the outline and the handle over the cells under them', async () => {
    const ctx = attachSheet();
    ctx.sheet.setSelectedCellAndRange('B3', 2, 3, 2, 3, 2, 3, false);
    await ctx.sheet.getCellGrid().updateComplete;
    const root = ctx.sheet.getCellGrid().shadowRoot!;

    for (const piece of root.querySelectorAll<HTMLElement>('.sheet-selection .s-top, .sheet-selection .selection-corner')) {
      // Hit-testable for the probe: the outline lets clicks through.
      piece.style.pointerEvents = 'auto';
      const box = piece.getBoundingClientRect();
      const onTop = root.elementFromPoint(box.left + box.width / 2, box.top + box.height / 2);
      expect(onTop, piece.className).to.equal(piece);
    }
    ctx.cleanup();
  });
});
