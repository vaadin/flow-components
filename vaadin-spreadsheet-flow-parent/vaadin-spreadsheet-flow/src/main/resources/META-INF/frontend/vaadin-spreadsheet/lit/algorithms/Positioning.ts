/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { GroupingData, MergedRegion, OverlayInfo, PopupButtonState } from '../types/wire.js';
import type { MergedRegionBox, PositionedOverlay, PositionedPopupButton } from '../elements/types.js';
import type { PositionedGrouping } from '../elements/headers/Grouping.js';
import { FALLBACK_PPI, freezeGap, pastFreeze } from './ViewportMath.js';

/**
 * Where the parts drawn over the sheet go in the cell grid's own pixels:
 * popup buttons, overlays (pictures, charts, components), merged regions,
 * and the column outline. Pure functions of the sheet's sizes.
 */

/**
 * Pure helper: build the positioned-popup-button list from the
 * server-pushed `PopupButtonState` list. Each button anchors at
 * the bottom-right corner of its target cell — pixel position =
 * (sum of widths 1..col + col width, sum of heights 1..row + row
 * height).
 */
export function computePositionedPopupButtons(
  states: ReadonlyArray<PopupButtonState>,
  colWidth: (col: number) => number,
  rowHeight: (row: number) => number,
  split: { cols: number; rows: number } = { cols: 0, rows: 0 }
): PositionedPopupButton[] {
  return states.map((state) => {
    let left = pastFreeze(state.col, split.cols);
    for (let c = 1; c <= state.col; c++) {
      left += colWidth(c);
    }
    let top = pastFreeze(state.row, split.rows);
    for (let r = 1; r <= state.row; r++) {
      top += rowHeight(r);
    }
    return {
      col: state.col,
      row: state.row,
      leftPx: left,
      topPx: top,
      cellHeightPx: rowHeight(state.row),
      active: state.active === true
    };
  });
}

/**
 * Pure helper: build the positioned-overlay list from the server-
 * pushed `OverlayInfo` map + resource map. Each overlay's pixel
 * position is `anchor cell's left/top` (cumulative sums of col/
 * row sizes 1..anchor-1) + the overlay's `dx`/`dy` offset.
 *
 * The two axes do not share a unit. POI measures anything vertical in
 * points, so `height` and `dy` are points while `width` and `dx` are
 * already pixels — the same split the row heights come through with.
 * Treating the vertical pair as pixels makes every overlay 25% too short
 * and seats it too high.
 *
 * The points are converted exactly, fractions kept, as a browser converts
 * `pt`. Cut down to whole pixels, a picture
 * sat up to a pixel high and a pixel short, and a chart drew itself for
 * a pixel less height.
 *
 * Pure (no DOM, no instance state) so it's straightforward to
 * unit-test in isolation.
 */
export function computePositionedOverlays(
  overlays: Record<string, OverlayInfo>,
  resources: Record<string, string>,
  colWidth: (col: number) => number,
  rowHeight: (row: number) => number,
  ppi: number = FALLBACK_PPI,
  split: { cols: number; rows: number } = { cols: 0, rows: 0 }
): PositionedOverlay[] {
  const result: PositionedOverlay[] = [];
  for (const [id, info] of Object.entries(overlays)) {
    let left = pastFreeze(info.col, split.cols);
    for (let c = 1; c < info.col; c++) {
      left += colWidth(c);
    }
    let top = pastFreeze(info.row, split.rows);
    for (let r = 1; r < info.row; r++) {
      top += rowHeight(r);
    }
    const positioned: PositionedOverlay = {
      col: info.col,
      row: info.row,
      id,
      type: info.type,
      src: info.type === 'IMAGE' ? resources[id] : undefined,
      leftPx: left + info.dx,
      topPx: top + (info.dy * ppi) / 72,
      widthPx: info.width,
      heightPx: (info.height * ppi) / 72
    };
    result.push(positioned);
  }
  return result;
}

/**
 * Where each merged region is drawn: its box in the sheet's own pixel
 * coordinates, frozen rows and columns included.
 *
 * Every region gets one, not only those whose top-left cell is on screen: a
 * merge whose first rows have scrolled away is still showing the rest. A
 * region that starts among the frozen rows or columns and runs past the split
 * is drawn pinned with them, the part beyond the
 * split shortened by how far the sheet has scrolled underneath.
 *
 * @param split the number of frozen rows and columns
 * @param scroll how far the sheet has scrolled
 */
export function computeMergedRegionBoxes(
  regions: ReadonlyArray<MergedRegion>,
  split: { rows: number; cols: number },
  scroll: { left: number; top: number },
  colWidthAt: (col: number) => number,
  rowHeightAt: (row: number) => number
): MergedRegionBox[] {
  let lastCol = split.cols;
  let lastRow = split.rows;
  for (const region of regions) {
    lastCol = Math.max(lastCol, region.col2);
    lastRow = Math.max(lastRow, region.row2);
  }
  // starts[n] is where row or column n begins; starts[1] is 0.
  const startsOf = (last: number, sizeAt: (index: number) => number): Float64Array => {
    const starts = new Float64Array(last + 2);
    for (let index = 1; index <= last; index++) {
      starts[index + 1] = starts[index] + sizeAt(index);
    }
    return starts;
  };
  const colStarts = startsOf(lastCol, colWidthAt);
  const rowStarts = startsOf(lastRow, rowHeightAt);
  // A region across the split keeps its frozen part pinned and shows what
  // has not scrolled away of the rest, which starts past the freeze line.
  const extent = (starts: Float64Array, from: number, to: number, frozen: number, scrolled: number): number => {
    const whole = starts[to + 1] - starts[from];
    if (from > frozen || to <= frozen) {
      return whole;
    }
    const pinned = starts[frozen + 1] - starts[from];
    const rest = whole - pinned - scrolled;
    return rest > 0 ? pinned + freezeGap(frozen) + rest : pinned;
  };
  return regions.map((region) => ({
    left: colStarts[region.col1] + pastFreeze(region.col1, split.cols),
    top: rowStarts[region.row1] + pastFreeze(region.row1, split.rows),
    width: extent(colStarts, region.col1, region.col2, split.cols, scroll.left),
    height: extent(rowStarts, region.row1, region.row2, split.rows, scroll.top)
  }));
}

/**
 * Pure helper: convert {@link GroupingData} entries into
 * {@link PositionedGrouping} entries - where each group's bracket runs and
 * where its button sits, along the axis the sizes are for.
 *
 * A group's summary row or column holds its button: by default the one after
 * the group, so the bracket runs from just inside the group's first header
 * ({@code startPaddingPx} in) to the middle of the next one, where the button
 * is. Inversed, the summary is the one before: the bracket runs from the
 * middle of that header to the group's end, and the button sits at its start.
 *
 * Both indices are 0-based in the wire format (server-side
 * `GroupingData.startIndex`/`endIndex` use POI's 0-based row/col
 * indices). The connector's `colWidths` array is 0-indexed too.
 *
 * @param startPaddingPx how far into its first header a group's bracket
 * starts - 5px for columns, 2px for rows
 * @param shiftPx how far back along the axis every bracket and button is
 * drawn - a pixel while the other outline gutter shows, measured without
 * its border
 */
export function computePositionedColGroupings(
  groupings: ReadonlyArray<GroupingData>,
  colWidths: ArrayLike<number>,
  inversed: boolean,
  defColWidthPx: number,
  startPaddingPx = 0,
  shiftPx = 0
): PositionedGrouping[] {
  const widthAt = (col0: number): number => {
    if (col0 < 0) {
      return 0;
    }
    return col0 < colWidths.length ? colWidths[col0] : defColWidthPx;
  };
  const positions: PositionedGrouping[] = [];
  for (const g of groupings) {
    let groupStart = 0;
    for (let i = 0; i < g.startIndex; i++) {
      groupStart += widthAt(i);
    }
    let groupEnd = groupStart;
    for (let i = g.startIndex; i <= g.endIndex; i++) {
      groupEnd += widthAt(i);
    }
    const summaryBefore = groupStart - widthAt(g.startIndex - 1) / 2;
    const summaryAfter = groupEnd + widthAt(g.endIndex + 1) / 2;
    positions.push({
      uniqueIndex: g.uniqueIndex,
      level: g.level,
      startPx: (inversed ? summaryBefore : groupStart + startPaddingPx) - shiftPx,
      endPx: (inversed ? groupEnd : summaryAfter) - shiftPx,
      buttonPx: (inversed ? summaryBefore : summaryAfter) - shiftPx,
      collapsed: g.collapsed
    });
  }
  return positions;
}
