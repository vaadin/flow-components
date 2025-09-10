// @ts-nocheck
import { Debouncer } from '@vaadin/component-base/src/debounce.js';
import { microTask } from '@vaadin/component-base/src/async.js';

export class GridViewportUpdateDebouncer {
  #grid;
  #debouncer;
  #queue = [];

  constructor(grid) {
    this.#grid = grid;
    this.#grid.__updateVisibleRows = (...args) => {
      if (this.#queue.length > 0) {
        this.debounce(...args);
        return;
      }

      Object.getPrototypeOf(this.#grid).__updateVisibleRows.call(this.#grid, ...args);
    };
  }

  debounce(startIndex = 0, endIndex = this.#grid.size - 1) {
    this.#queue.push([startIndex, endIndex]);

    this.#debouncer = Debouncer.debounce(this.#debouncer, microTask, () => {
      if (this.#queue.length === 0) {
        return;
      }

      const start = Math.min(...this.#queue.map((args) => args[0]));
      const end = Math.max(...this.#queue.map((args) => args[1]));
      this.#queue = [];
      this.#grid.__updateVisibleRows(start, end);
    });
  }

  flush() {
    this.#debouncer?.flush();
  }
}
