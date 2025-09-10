// @ts-nocheck
import { Debouncer } from '@vaadin/component-base/src/debounce.js';
import { microTask } from '@vaadin/component-base/src/async.js';

export class GridViewportUpdateDebouncer {
  #grid: HTMLElement;
  #debouncer: Debouncer;

  constructor(grid: HTMLElement) {
    this.#grid = grid;
    this.#grid.__updateVisibleRows = (...args) => {
      if (this.#debouncer?.isActive()) {
        return;
      }

      Object.getPrototypeOf(this.#grid).__updateVisibleRows.call(this.#grid, ...args);
    };
  }

  debounce() {
    this.#debouncer = Debouncer.debounce(this.#debouncer, microTask, () => {
      this.#grid.__updateVisibleRows();
    });
  }

  flush() {
    this.#debouncer?.flush();
  }
}
