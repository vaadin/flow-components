/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/** A cell, by its column and row, 1-based. */
interface Cell {
  col: number;
  row: number;
}

/** A box on screen, in client pixels; its right and bottom are just past it. */
export interface ScreenBox {
  readonly left: number;
  readonly top: number;
  readonly right: number;
  readonly bottom: number;
}

/** Where the cells are on screen. */
export interface CellPanes {
  /** The cells in view, the frozen ones included. */
  readonly cells: ScreenBox;
  /** The cells that scroll: past the frozen rows and columns and the freeze line. */
  readonly scrolling: ScreenBox;
}

/** How far the cells are scrolled, and how far they can be. */
export interface ScrollState {
  readonly left: number;
  readonly top: number;
  readonly maxLeft: number;
  readonly maxTop: number;
}

/** What scrolling the cells during a drag needs of the sheet. */
export interface DragScrollHost {
  /** Whether a press is being dragged over the cells. */
  dragging(): boolean;
  /** Where the cells are on screen now. */
  panes(): CellPanes;
  /** How far the cells are scrolled now. */
  scrolled(): ScrollState;
  /** Scrolls the cells to here, kept within how far they can be. */
  scrollTo(left: number, top: number): void;
  /** The cell at a point on the cells. */
  cellAt(x: number, y: number): Cell;
  /** The drag reaches a cell, as the pointer going over the cell takes it. */
  over(col: number, row: number): void;
}

/** The timer the cells scroll on, replaceable in tests. */
export interface DragScrollTimers {
  setInterval(step: () => void, ms: number): unknown;
  clearInterval(id: unknown): void;
}

const realTimers: DragScrollTimers = {
  setInterval: (step, ms) => window.setInterval(step, ms),
  clearInterval: (id) => window.clearInterval(id as number)
};

/** How often the cells scroll on while a drag holds the pointer past their edge, in ms. */
const STEP_MS = 50;

/** A value kept between two others. */
function keep(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

/**
 * How far the cells scroll in one step along one axis, for a pointer at `at`
 * against the scrolling cells from `start` to just before `end`: half of how
 * far the pointer is past an edge, at least a pixel, toward that edge, and
 * nothing for a pointer over them. They scroll back, toward `start`, only
 * when `back` allows, and on only when `on` does.
 */
function stepPast(at: number, start: number, end: number, back: boolean, on: boolean): number {
  if (at < start) {
    return back ? -Math.max(1, Math.trunc((start - at) / 2)) : 0;
  }
  if (at >= end) {
    return on ? Math.max(1, Math.trunc((at - end) / 2)) : 0;
  }
  return 0;
}

/**
 * Scrolling the cells while a drag holds the pointer past their edge: a
 * range dragged out, the fill handle dragged, or a reference pointed at for
 * a formula, reaching cells out of view. Every 50 ms the cells scroll by
 * half of how far the pointer is past the edge - the further, the faster -
 * and the drag reaches the cell at the edge, as with the GWT client.
 *
 * Toward the frozen rows or columns, the cells scroll back only for a drag
 * that has been on the scrolling ones: one that starts on the frozen ones
 * and stays there selects among them. When such a drag comes onto the
 * scrolling ones, those scroll back next to the frozen ones first, so that
 * the range it drags out shows whole.
 */
export class DragScroll {
  /** Where the last press on the page was, which a drag starts from. */
  private press: { x: number; y: number } | null = null;
  /** Where the pointer is. */
  private pointer = { x: 0, y: 0 };
  /**
   * Whether the drag may scroll the cells back, across and down: it has been
   * on the scrolling ones. Null until a drag moves.
   */
  private back: { x: boolean; y: boolean } | null = null;
  /** The timer the cells scroll on, while they do. */
  private timer: unknown = null;
  /** The cell the scrolling took the drag to last. */
  private reached: Cell | null = null;

  constructor(private readonly host: DragScrollHost, private readonly timers: DragScrollTimers = realTimers) {}

  /** A press on the page, which a drag may start from. */
  pressed(x: number, y: number): void {
    this.stop();
    this.press = { x, y };
  }

  /**
   * Whether the cells are scrolling for a drag. Until they stop, the drag
   * reaches the cell at their edge, not one the pointer goes over.
   */
  scrolling(): boolean {
    return this.timer !== null;
  }

  /** The pointer moved on the page. */
  moved(x: number, y: number): void {
    if (!this.host.dragging()) {
      this.stop();
      return;
    }
    this.pointer = { x, y };
    const panes = this.host.panes();
    const { scrolling } = panes;
    if (this.back === null) {
      // Pressed on the frozen rows or columns, the drag scrolls nothing back
      // past them until it has been on the scrolling ones.
      const from = this.press ?? this.pointer;
      this.back = { x: from.x >= scrolling.left, y: from.y >= scrolling.top };
    }
    this.cross(panes);
    const step = this.step(scrolling, this.host.scrolled());
    if (step.x !== 0 || step.y !== 0) {
      this.start();
    } else {
      this.halt();
    }
  }

  /** The drag is over: the button came up, or the sheet left the page. */
  stop(): void {
    this.halt();
    this.back = null;
  }

  /**
   * A drag from the frozen rows or columns comes onto the scrolling ones:
   * they scroll back next to the frozen ones, and the drag reaches the cell
   * now under the pointer.
   */
  private cross(panes: CellPanes): void {
    const back = this.back!;
    const { scrolling } = panes;
    const scroll = this.host.scrolled();
    let { left, top } = scroll;
    if (!back.y && this.pointer.y >= scrolling.top) {
      back.y = true;
      top = 0;
    }
    if (!back.x && this.pointer.x >= scrolling.left) {
      back.x = true;
      left = 0;
    }
    if (left !== scroll.left || top !== scroll.top) {
      this.host.scrollTo(left, top);
      this.reached = null;
      this.reach(panes);
    }
  }

  /** How far the cells scroll in a step, across and down, for where the pointer is. */
  private step(scrolling: ScreenBox, scroll: ScrollState): { x: number; y: number } {
    const back = this.back!;
    const { x, y } = this.pointer;
    return {
      x: stepPast(x, scrolling.left, scrolling.right, back.x && scroll.left > 0, scroll.left < scroll.maxLeft),
      y: stepPast(y, scrolling.top, scrolling.bottom, back.y && scroll.top > 0, scroll.top < scroll.maxTop)
    };
  }

  /** The cells start scrolling, unless they are. */
  private start(): void {
    if (this.timer === null) {
      this.reached = null;
      this.timer = this.timers.setInterval(this.tick, STEP_MS);
    }
  }

  /** The cells stop scrolling, if they were. */
  private halt(): void {
    if (this.timer !== null) {
      this.timers.clearInterval(this.timer);
      this.timer = null;
    }
  }

  /** A step: the cells scroll on, and the drag reaches the cell at their edge. */
  private readonly tick = (): void => {
    if (!this.host.dragging()) {
      this.stop();
      return;
    }
    const panes = this.host.panes();
    const scroll = this.host.scrolled();
    const step = this.step(panes.scrolling, scroll);
    if (step.x !== 0 || step.y !== 0) {
      this.host.scrollTo(scroll.left + step.x, scroll.top + step.y);
    }
    this.reach(panes);
    const next = this.step(panes.scrolling, this.host.scrolled());
    if (next.x === 0 && next.y === 0) {
      this.halt();
    }
  };

  /**
   * The drag reaches the cell at the pointer, the point kept to the cells -
   * and to the scrolling ones on an axis they still scroll back on, so that
   * the drag goes onto the frozen ones only once the rest are back at their
   * start. A cell reached already is not reached again.
   */
  private reach({ cells, scrolling }: CellPanes): void {
    const back = this.back!;
    const scroll = this.host.scrolled();
    const { x, y } = this.pointer;
    const left = back.x && scroll.left > 0 && x < scrolling.left ? scrolling.left : cells.left;
    const top = back.y && scroll.top > 0 && y < scrolling.top ? scrolling.top : cells.top;
    const cell = this.host.cellAt(keep(x, left, scrolling.right - 1), keep(y, top, scrolling.bottom - 1));
    if (this.reached !== null && cell.col === this.reached.col && cell.row === this.reached.row) {
      return;
    }
    this.reached = cell;
    this.host.over(cell.col, cell.row);
  }
}
