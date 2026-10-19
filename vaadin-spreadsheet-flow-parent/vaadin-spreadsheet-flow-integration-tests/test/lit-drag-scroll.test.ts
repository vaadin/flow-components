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
import { AxisSizes } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/AxisSizes.js';
import { indexAtPoint } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/ViewportMath.js';
import { DragScroll } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/DragScroll.js';

/** Timers that only run when told to, a 50 ms step at a time. */
function fakeTimers() {
  let nextId = 1;
  const running = new Map<number, () => void>();
  return {
    timers: {
      setInterval: (fn: () => void, ms: number) => {
        expect(ms, 'the step').to.equal(50);
        const id = nextId++;
        running.set(id, fn);
        return id;
      },
      clearInterval: (id: unknown) => {
        running.delete(id as number);
      }
    },
    /** Runs this many steps of the timers running. */
    steps(count: number) {
      for (let i = 0; i < count; i++) {
        for (const fn of [...running.values()]) {
          fn();
        }
      }
    },
    get running() {
      return running.size;
    }
  };
}

/**
 * Scrolling the cells while a drag holds the pointer past their edge, on its
 * own: a sheet of 100 rows of 20px and 30 columns of 50px, the cells on
 * screen from (40, 30) to (440, 230), 400 by 200 pixels.
 */
describe('DragScroll — the cells scroll while a drag holds the pointer past their edge', () => {
  const cells = { left: 40, top: 30, right: 440, bottom: 230 };
  const rows = new AxisSizes(100, () => 20, 20);
  const columns = new AxisSizes(30, () => 50, 50);
  let frozenRows: number;
  let frozenColumns: number;
  let scroll: { left: number; top: number };
  let dragging: boolean;
  let reached: Array<{ col: number; row: number }>;
  let time: ReturnType<typeof fakeTimers>;
  let drag: DragScroll;

  /** Where the scrolling rows and columns start: past the frozen ones and the freeze line. */
  const scrollingTop = () => cells.top + (frozenRows > 0 ? rows.start(frozenRows + 1) + 1 : 0);
  const scrollingLeft = () => cells.left + (frozenColumns > 0 ? columns.start(frozenColumns + 1) + 1 : 0);
  const maxTop = () => rows.start(101) - (cells.bottom - scrollingTop());
  const maxLeft = () => columns.start(31) - (cells.right - scrollingLeft());

  beforeEach(() => {
    frozenRows = 0;
    frozenColumns = 0;
    scroll = { left: 0, top: 0 };
    dragging = false;
    reached = [];
    time = fakeTimers();
    drag = new DragScroll(
      {
        dragging: () => dragging,
        panes: () => ({ cells, scrolling: { ...cells, left: scrollingLeft(), top: scrollingTop() } }),
        scrolled: () => ({ ...scroll, maxLeft: maxLeft(), maxTop: maxTop() }),
        scrollTo: (left, top) => {
          scroll = { left: Math.min(Math.max(0, left), maxLeft()), top: Math.min(Math.max(0, top), maxTop()) };
        },
        cellAt: (x, y) => ({
          col: indexAtPoint(columns, frozenColumns, 30, x - cells.left, scroll.left),
          row: indexAtPoint(rows, frozenRows, 100, y - cells.top, scroll.top)
        }),
        over: (col, row) => reached.push({ col, row })
      },
      time.timers
    );
  });

  /** A press on the cells at a point, which the drag starts from. */
  function pressAt(x: number, y: number): void {
    drag.pressed(x, y);
    dragging = true;
  }

  it('scrolls down by half of how far the pointer is past the bottom edge, every 50 ms', () => {
    pressAt(100, 100);

    drag.moved(100, 270);

    expect(drag.scrolling()).to.be.true;
    expect(scroll.top, 'before the first step').to.equal(0);
    time.steps(1);
    expect(scroll.top).to.equal(20);
    time.steps(2);
    expect(scroll.top).to.equal(60);
  });

  it('scrolls faster the further past the edge the pointer is', () => {
    pressAt(100, 100);

    drag.moved(100, 330);
    time.steps(1);

    expect(scroll.top).to.equal(50);
  });

  it('scrolls a pixel a step for a pointer just past the edge', () => {
    pressAt(100, 100);

    drag.moved(100, 231);
    time.steps(3);

    expect(scroll.top).to.equal(3);
  });

  it('takes the drag to the cell at the bottom edge as the cells scroll, once each', () => {
    pressAt(100, 100);

    drag.moved(100, 231);
    time.steps(1);
    // Scrolled 1px, the edge's last pixel, 199px down, is 200px into the rows: row 11.
    expect(reached).to.eql([{ col: 2, row: 11 }]);
    time.steps(19);
    // Scrolled 20px: 219px, row 11 still.
    expect(reached).to.eql([{ col: 2, row: 11 }]);
    time.steps(1);
    expect(reached).to.eql([
      { col: 2, row: 11 },
      { col: 2, row: 12 }
    ]);
  });

  it('scrolls right past the right edge, and both ways past a corner', () => {
    pressAt(100, 100);

    drag.moved(480, 250);
    time.steps(1);

    expect(scroll).to.eql({ left: 20, top: 10 });
  });

  it('stops when the pointer comes back over the cells', () => {
    pressAt(100, 100);
    drag.moved(100, 270);
    time.steps(1);

    drag.moved(100, 200);

    expect(drag.scrolling(), 'at once').to.be.false;
    time.steps(3);
    expect(time.running).to.equal(0);
    expect(scroll.top).to.equal(20);
  });

  it('stops when the button comes up', () => {
    pressAt(100, 100);
    drag.moved(100, 270);

    drag.stop();
    time.steps(3);

    expect(drag.scrolling()).to.be.false;
    expect(scroll.top).to.equal(0);
  });

  it('stops when the drag ends some other way', () => {
    pressAt(100, 100);
    drag.moved(100, 270);

    dragging = false;
    time.steps(1);

    expect(time.running).to.equal(0);
    expect(scroll.top).to.equal(0);
    expect(reached).to.eql([]);
  });

  it('does nothing for a pointer moved without a drag', () => {
    drag.pressed(100, 100);

    drag.moved(100, 270);

    expect(drag.scrolling()).to.be.false;
    time.steps(3);
    expect(scroll.top).to.equal(0);
  });

  it('does not scroll on past the end of the sheet', () => {
    scroll.top = maxTop();
    pressAt(100, 100);

    drag.moved(100, 270);

    expect(drag.scrolling()).to.be.false;
    expect(time.running).to.equal(0);
  });

  it('scrolls back past the top edge, the drag taken to the cell at the edge', () => {
    scroll.top = 200;
    pressAt(100, 150);

    drag.moved(100, -10);
    time.steps(1);

    expect(scroll.top).to.equal(180);
    // The top edge is 180px into the rows: row 10.
    expect(reached).to.eql([{ col: 2, row: 10 }]);
  });

  it('does not scroll back past the start, nor up when not scrolled', () => {
    pressAt(100, 150);

    drag.moved(100, -10);

    expect(drag.scrolling()).to.be.false;
    expect(scroll.top).to.equal(0);
  });

  it('takes the drag to the edge again each time the pointer goes past it', () => {
    pressAt(100, 100);
    drag.moved(100, 231);
    time.steps(1);
    // Back over the cells, where the pointer goes over other cells.
    drag.moved(100, 150);

    drag.moved(100, 231);
    time.steps(1);

    // Row 11 is at the edge both times, and the drag is taken there again.
    expect(reached).to.eql([
      { col: 2, row: 11 },
      { col: 2, row: 11 }
    ]);
  });

  it('starts each drag afresh, from where it was pressed', () => {
    frozenRows = 3;
    scroll.top = 100;
    // A drag from the frozen rows that comes onto the rest, let go with
    // no move after.
    pressAt(100, 55);
    drag.moved(100, 120);
    dragging = false;
    scroll.top = 100;

    // The next, from the frozen rows, held above them.
    pressAt(100, 55);
    drag.moved(100, 10);

    expect(drag.scrolling()).to.be.false;
  });

  describe('with three rows frozen', () => {
    beforeEach(() => {
      // 60px of frozen rows and the freeze line's pixel: the scrolling rows start at 91.
      frozenRows = 3;
    });

    it('scrolls back toward the frozen rows, then takes the drag onto the frozen row under the pointer', () => {
      scroll.top = 40;
      pressAt(100, 150);

      // Over frozen row 2, 36px above the scrolling rows: 18px a step.
      drag.moved(100, 55);
      time.steps(3);

      expect(scroll.top).to.equal(0);
      expect(reached.map((cell) => cell.row)).to.eql([5, 4, 2]);
      expect(time.running, 'nothing left to scroll').to.equal(0);
    });

    it('keeps a drag that started on the frozen rows among them, the rest not scrolled', () => {
      scroll.top = 100;
      pressAt(100, 55);

      drag.moved(100, 10);
      time.steps(2);

      expect(drag.scrolling()).to.be.false;
      expect(scroll.top).to.equal(100);
    });

    it('scrolls the rest back to the frozen rows when a drag from those comes onto them', () => {
      scroll.top = 100;
      pressAt(100, 55);

      drag.moved(100, 120);

      expect(scroll.top).to.equal(0);
      // 29px into the scrolling rows, which start at row 4: row 5.
      expect(reached).to.eql([{ col: 2, row: 5 }]);
    });

    it('scrolls back up a drag from the frozen rows that has been on the rest', () => {
      pressAt(100, 55);
      drag.moved(100, 120);
      drag.moved(100, 270);
      time.steps(2);
      expect(scroll.top).to.equal(40);

      drag.moved(100, 55);
      time.steps(1);

      expect(scroll.top).to.equal(22);
    });
  });

  describe('with two columns frozen', () => {
    beforeEach(() => {
      // 100px of frozen columns and the freeze line's pixel: the scrolling columns start at 141.
      frozenColumns = 2;
    });

    it('scrolls back toward the frozen columns, then takes the drag onto the frozen column under the pointer', () => {
      scroll.left = 30;
      pressAt(300, 100);

      // Over frozen column 2, 36px left of the scrolling columns: 18px a step.
      drag.moved(105, 100);
      time.steps(2);

      expect(scroll.left).to.equal(0);
      // 12px into column 3, the first that scrolls, then onto column 2 under the pointer.
      expect(reached.map((cell) => cell.col)).to.eql([3, 2]);
    });

    it('scrolls the rest back to the frozen columns when a drag from those comes onto them', () => {
      scroll.left = 120;
      pressAt(105, 100);

      drag.moved(200, 100);

      expect(scroll.left).to.equal(0);
      // 59px into the scrolling columns, which start at column 3: column 4.
      expect(reached).to.eql([{ col: 4, row: 4 }]);
    });
  });
});

describe('Spreadsheet — a drag held past the edge scrolls the cells', () => {
  function attach(rowsFrozen = 0): { sheet: Spreadsheet; cleanup: () => void } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:200px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    const sheet = new Spreadsheet(host);
    sheet.setRows(100);
    sheet.setCols(30);
    sheet.setColW(new Array(30).fill(50));
    sheet.setRowH(new Array(100).fill(15));
    if (rowsFrozen > 0) {
      sheet.setVerticalSplitPosition(rowsFrozen);
    }
    sheet.recomputeViewport();
    return {
      sheet,
      cleanup: () => {
        sheet.dispose();
        host.remove();
      }
    };
  }

  const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

  /** Presses the button on an element of the cells, at its middle. */
  function pressOn(element: HTMLElement): { x: number; y: number } {
    const box = element.getBoundingClientRect();
    const at = { clientX: box.left + box.width / 2, clientY: box.top + box.height / 2 };
    element.dispatchEvent(new PointerEvent('pointerdown', { ...at, bubbles: true, composed: true }));
    element.dispatchEvent(new MouseEvent('mousedown', { ...at, button: 0, bubbles: true, composed: true }));
    return { x: at.clientX, y: at.clientY };
  }

  /** Moves the pointer to 30px below the sheet. */
  function moveBelow(sheet: Spreadsheet, x: number): MouseEventInit {
    const at = { clientX: x, clientY: sheet.getScrollContainer().getBoundingClientRect().bottom + 30 };
    document.dispatchEvent(new MouseEvent('mousemove', at));
    return at;
  }

  /** Holds the pointer 30px below the sheet for a while, then lets go; how far the cells scrolled. */
  async function holdBelow(sheet: Spreadsheet, x: number): Promise<{ held: number; after: number }> {
    const container = sheet.getScrollContainer();
    const at = moveBelow(sheet, x);
    await wait(300);
    const held = container.scrollTop;
    document.dispatchEvent(new MouseEvent('mouseup', at));
    await wait(150);
    return { held, after: container.scrollTop };
  }

  /** The row of column B whose cell takes in the last pixel of the view, at its bottom edge. */
  async function rowAtBottomEdge(sheet: Spreadsheet): Promise<number> {
    await sheet.getCellGrid().updateComplete;
    const container = sheet.getScrollContainer();
    const edge = container.getBoundingClientRect().top + container.clientTop + container.clientHeight - 1;
    const cells = sheet.getCellGrid().shadowRoot!.querySelectorAll<HTMLElement>('.cell.col2');
    for (const cell of cells) {
      const box = cell.getBoundingClientRect();
      if (box.height > 0 && box.top <= edge && box.bottom > edge) {
        return Number(/\brow(\d+)\b/.exec(cell.className)![1]);
      }
    }
    throw new Error('no cell at the bottom edge');
  }

  it('selects on into the rows scrolled into view, up to the one at the edge, and stops on the release', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const cell = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('.cell.col2.row2')!;
      const inViewBefore = await rowAtBottomEdge(sheet);
      const { x } = pressOn(cell);

      const { held, after } = await holdBelow(sheet, x);

      expect(held).to.be.above(0);
      expect(after, 'no more after the release').to.equal(held);
      const range = sheet.getSelection().activeRange;
      expect([range.c1, range.r1, range.c2]).to.eql([2, 2, 2]);
      expect(range.r2).to.be.above(inViewBefore);
      expect(range.r2).to.equal(await rowAtBottomEdge(sheet));
    } finally {
      cleanup();
    }
  });

  it('leaves the drag at the edge while the cells scroll, whatever the pointer seems to go over', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const grid = sheet.getCellGrid().shadowRoot!;
      const cell = grid.querySelector<HTMLElement>('.cell.col2.row2')!;
      const third = grid.querySelector<HTMLElement>('.cell.col2.row3')!;
      const { x } = pressOn(cell);
      const at = moveBelow(sheet, x);
      await wait(150);
      const reached = sheet.getSelection().activeRange.r2;
      expect(reached).to.be.above(3);

      expect(third.isConnected).to.be.true;
      third.dispatchEvent(new MouseEvent('mouseover', { bubbles: true, composed: true }));

      expect(sheet.getSelection().activeRange.r2).to.equal(reached);
      document.dispatchEvent(new MouseEvent('mouseup', at));
    } finally {
      cleanup();
    }
  });

  it('stops when the sheet is taken out of the page during the drag', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const { x } = pressOn(sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('.cell.col2.row2')!);
      moveBelow(sheet, x);
      await wait(100);
      const container = sheet.getScrollContainer();
      expect(container.scrollTop).to.be.above(0);

      sheet.disconnect();
      const left = container.scrollTop;
      await wait(150);

      expect(container.scrollTop).to.equal(left);
    } finally {
      cleanup();
    }
  });

  it('scrolls the rows back to the frozen ones when a drag from those comes onto them', async () => {
    const { sheet, cleanup } = attach(3);
    try {
      const container = sheet.getScrollContainer();
      container.scrollTop = 300;
      sheet.recomputeViewport();
      await sheet.getCellGrid().updateComplete;
      const frozen = Array.from(
        sheet.getCellGrid().shadowRoot!.querySelectorAll<HTMLElement>('.cell.col2.row2')
      ).find((cell) => cell.getBoundingClientRect().height > 0)!;
      const { x } = pressOn(frozen);

      moveBelow(sheet, x);
      await wait(150);

      // Back to the start, and from there on down: not on from 300px.
      expect(container.scrollTop).to.be.above(0);
      expect(container.scrollTop).to.be.below(300);
      document.dispatchEvent(new MouseEvent('mouseup'));
    } finally {
      cleanup();
    }
  });

  it('leaves the drag to the pointer at the end of the sheet, where there is nothing to scroll', async () => {
    const { sheet, cleanup } = attach();
    try {
      const container = sheet.getScrollContainer();
      container.scrollTop = container.scrollHeight;
      sheet.recomputeViewport();
      await sheet.getCellGrid().updateComplete;
      const grid = sheet.getCellGrid().shadowRoot!;
      const { x } = pressOn(grid.querySelector<HTMLElement>('.cell.col2.row95')!);
      const at = moveBelow(sheet, x);
      await wait(100);

      grid.querySelector<HTMLElement>('.cell.col2.row97')!.dispatchEvent(
        new MouseEvent('mouseover', { bubbles: true, composed: true })
      );

      expect(sheet.getSelection().activeRange.r2).to.equal(97);
      document.dispatchEvent(new MouseEvent('mouseup', at));
    } finally {
      cleanup();
    }
  });

  it('scrolls right for a drag held right of the sheet', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const { y } = pressOn(sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('.cell.col2.row2')!);
      const container = sheet.getScrollContainer();
      const at = { clientX: container.getBoundingClientRect().right + 30, clientY: y };
      document.dispatchEvent(new MouseEvent('mousemove', at));
      await wait(200);

      expect(container.scrollLeft).to.be.above(0);
      expect(sheet.getSelection().activeRange.c2).to.be.above(2);
      document.dispatchEvent(new MouseEvent('mouseup', at));
    } finally {
      cleanup();
    }
  });

  it('scrolls for the fill handle dragged past the edge too', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const handle = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('.selection-corner')!;
      const { x } = pressOn(handle);

      const { held, after } = await holdBelow(sheet, x);

      expect(held).to.be.above(0);
      expect(after).to.equal(held);
    } finally {
      cleanup();
    }
  });
});
