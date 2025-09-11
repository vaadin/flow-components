// @ts-nocheck
import { Debouncer } from '@vaadin/component-base/src/debounce.js';
import { microTask } from '@vaadin/component-base/src/async.js';

export class GridRowUpdateBatcher {
  #grid;
  #isActive = false;

  constructor(grid) {
    this.#grid = grid;
    this.#grid.__updateVisibleRows = (...args) => {
      if (this.#isActive) {
        return;
      }

      Object.getPrototypeOf(this.#grid).__updateVisibleRows.call(this.#grid, ...args);
    };
  }

  batch(callback) {
    const queue = [];

    this.#isActive = true;
    callback((...args) => queue.push(args));
    this.#isActive = false;

    queue.forEach((args) => this.#grid.__updateVisibleRows(...args));
  }
}
