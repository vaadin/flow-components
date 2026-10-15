/**
 * @license
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
 * second, so the sweep repeats until a pass changes nothing. Coordinates
 * are 1-based throughout, matching the wire.
 */

/** A merged region, as the server sends it. */
export interface MergedRect {
  col1: number;
  col2: number;
  row1: number;
  row2: number;
}

/**
 * The smallest rectangle that contains {@code selection} and every merged
 * region it touches.
 *
 * Returns the selection itself when the sheet has no merges.
 */
export function expandToMergedRegions(selection: CellRect, regions: ReadonlyArray<MergedRect>): CellRect {
  if (regions.length === 0) {
    return selection;
  }

  let { c1, c2, r1, r2 } = selection;

  // Each pass takes in every merge that is partly inside: one taken in can
  // reach another, so keep going until a pass takes in none. A merge that
  // is partly inside crosses an edge, so this is the merges the edges
  // meet, found without walking the edges cell by cell. Bounded by the
  // region count: every pass that changes anything has taken in one more.
  for (let pass = 0; pass <= regions.length; pass++) {
    let grew = false;
    for (const region of regions) {
      const overlaps = region.col1 <= c2 && region.col2 >= c1 && region.row1 <= r2 && region.row2 >= r1;
      if (!overlaps) {
        continue;
      }
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
    }
    if (!grew) {
      break;
    }
  }

  return { c1, c2, r1, r2 };
}
