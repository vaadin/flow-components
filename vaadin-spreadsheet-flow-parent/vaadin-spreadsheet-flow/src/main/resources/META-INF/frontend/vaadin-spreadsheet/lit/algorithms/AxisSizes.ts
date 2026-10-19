/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * The sizes of a sheet's rows or columns, in whole pixels, with the total
 * before each one worked out once: where one starts, and which one a
 * distance reaches, are found without adding up every one before it. A
 * sheet can have a million rows, and it is drawn on every scroll step.
 *
 * Indexes are 1-based. The first `count` have the sizes given; every one
 * after them is `beyond` pixels.
 */
export class AxisSizes {
  /** The total of the first n, at n. */
  private readonly totals: Float64Array;

  constructor(readonly count: number, sizeAt: (index: number) => number, private readonly beyond: number) {
    this.totals = new Float64Array(count + 1);
    for (let index = 1; index <= count; index++) {
      this.totals[index] = this.totals[index - 1] + sizeAt(index);
    }
  }

  /** The size of one. */
  size(index: number): number {
    return index >= 1 && index <= this.count ? this.totals[index] - this.totals[index - 1] : this.beyond;
  }

  /** Where one starts: the total of all before it. */
  start(index: number): number {
    const before = Math.max(0, index - 1);
    return before <= this.count ? this.totals[before] : this.totals[this.count] + (before - this.count) * this.beyond;
  }

  /**
   * The first one, from `from` on, that starts at least `distance` further
   * on than `from` does: `from` itself for no distance. Ones of no size, a
   * hidden row, start where the next one does, so the first of them is
   * the one found.
   */
  reach(from: number, distance: number): number {
    if (distance <= 0) {
      return from;
    }
    const target = this.start(from) + distance;
    if (from <= this.count && this.totals[this.count] >= target) {
      // Among the sized ones: `low` starts short of the target and `high`
      // reaches it.
      let low = from;
      let high = this.count + 1;
      while (high - low > 1) {
        const middle = (low + high) >>> 1;
        if (this.start(middle) >= target) {
          high = middle;
        } else {
          low = middle;
        }
      }
      return high;
    }
    // Past them, every one is the same size; of none, the distance is
    // never covered, and the first past them is as far as there is.
    const first = Math.max(from, this.count + 1);
    if (this.beyond <= 0) {
      return first;
    }
    return first + Math.ceil((target - this.start(first)) / this.beyond);
  }

  /**
   * The one a point lies in, `distance` past where `from` starts: `from`
   * itself for a point at its start or before it. Ones of no size, a hidden
   * row, hold no point; the point is in the next one with a size.
   */
  holding(from: number, distance: number): number {
    let index = from;
    if (distance > 0) {
      index = this.reach(from, distance);
      if (this.start(index) > this.start(from) + distance) {
        // It starts past the point, which is in the one before it.
        index--;
      }
    }
    while (index <= this.count && this.size(index) <= 0) {
      index++;
    }
    return index;
  }
}
