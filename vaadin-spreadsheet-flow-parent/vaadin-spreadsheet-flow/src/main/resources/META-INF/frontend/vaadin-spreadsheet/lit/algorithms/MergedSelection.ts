/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { CellRect } from './SelectionPaint.js';

/**
 * Growing a selection to whole merged regions.
 *
 * A merged region is one cell to the user, so a selection that touches any
 * part of one has to take all of it — drag across half a merge in Excel and
 * the whole merge comes with it. Selecting a range that ends inside a merge
 * and reporting the literal rectangle instead leaves the server writing to
 * cells the user cannot see.
 *
 * The rule is not "union the rectangle with every merge it intersects" done
 * once: growing to take in one merge can bring the rectangle's edge over a
 * second, so the sweep repeats until an pass changes nothing. Coordinates
 * are 1-based throughout, matching the wire.
 */

/** A merged region, as the server sends it. */
export interface MergedRect {
  col1: number;
  col2: number;
  row1: number;
  row2: number;
}

/** The merge covering a cell, or null when it is an ordinary cell. */
function mergeAt(regions: ReadonlyArray<MergedRect>, col: number, row: number): MergedRect | null {
  for (const region of regions) {
    if (col >= region.col1 && col <= region.col2 && row >= region.row1 && row <= region.row2) {
      return region;
    }
  }
  return null;
}

/**
 * The smallest rectangle that contains {@code selection} and every merged
 * region it touches.
 *
 * Returns the selection unchanged when it touches none, so the common case
 * costs one sweep of the edges and allocates nothing.
 */
export function expandToMergedRegions(selection: CellRect, regions: ReadonlyArray<MergedRect>): CellRect {
  if (regions.length === 0) {
    return selection;
  }

  let { c1, c2, r1, r2 } = selection;

  // Each pass walks the four edges, taking in any merge it meets. Taking
  // one in can push an edge over another, so keep sweeping until a pass
  // changes nothing. Bounded by the region count: every pass that changes
  // anything has absorbed at least one more merge.
  for (let pass = 0; pass <= regions.length; pass++) {
    let grew = false;

    const absorb = (region: MergedRect): void => {
      if (region.col1 < c1) {
        c1 = region.col1;
        grew = true;
      }
      if (region.col2 > c2) {
        c2 = region.col2;
        grew = true;
      }
      if (region.row1 < r1) {
        r1 = region.row1;
        grew = true;
      }
      if (region.row2 > r2) {
        r2 = region.row2;
        grew = true;
      }
    };

    for (let col = c1; col <= c2; col++) {
      const top = mergeAt(regions, col, r1);
      if (top !== null) {
        absorb(top);
      }
      const bottom = mergeAt(regions, col, r2);
      if (bottom !== null) {
        absorb(bottom);
      }
    }
    for (let row = r1; row <= r2; row++) {
      const left = mergeAt(regions, c1, row);
      if (left !== null) {
        absorb(left);
      }
      const right = mergeAt(regions, c2, row);
      if (right !== null) {
        absorb(right);
      }
    }

    if (!grew) {
      break;
    }
  }

  return { c1, c2, r1, r2 };
}
