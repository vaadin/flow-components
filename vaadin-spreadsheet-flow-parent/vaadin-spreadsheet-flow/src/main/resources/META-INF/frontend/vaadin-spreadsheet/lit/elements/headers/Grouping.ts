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
 * A grouping outline entry with pre-computed pixel positions.
 * The connector pre-computes {@code startPx} +
 * {@code endPx} + {@code buttonPx} from {@code colWidths} /
 * {@code rowHeights} so the header doesn't need access to the
 * full width array.
 */
export interface PositionedGrouping {
  /** Unique-within-the-grouping-list identifier (matches server). */
  uniqueIndex: number;
  /** Grouping level (1-based; level-1 is the outermost). */
  level: number;
  /** Where the group's bracket starts, its tick included. */
  startPx: number;
  /** Where the group's bracket ends. */
  endPx: number;
  /**
   * Centre of the group's button: over the middle of the summary column -
   * the one after the group, or before it when the grouping is inversed.
   */
  buttonPx: number;
  /** Current collapse state — drives the +/- glyph. */
  collapsed: boolean;
}

/** Height of one column outline level in the gutter. */
export const COLUMN_GROUPING_LEVEL_PX = 18;
/** Width of one row outline level in the gutter. */
export const ROW_GROUPING_LEVEL_PX = 15;
/** Diameter of a group's round expand/collapse button. */
export const GROUPING_BUTTON_SIZE_PX = 12;

/**
 * Height of the column outline gutter: a level
 * per depth of grouping and, while the headings show, one more for the level
 * button that expands everything. Nothing when no column is grouped.
 */
export function columnGroupingGutterPx(maxLevel: number, headings: boolean): number {
  return maxLevel <= 0 ? 0 : 3 + (maxLevel + (headings ? 1 : 0)) * COLUMN_GROUPING_LEVEL_PX;
}

/** Width of the row outline gutter, likewise. */
export function rowGroupingGutterPx(maxLevel: number, headings: boolean): number {
  return maxLevel <= 0 ? 0 : 1 + (maxLevel + (headings ? 1 : 0)) * ROW_GROUPING_LEVEL_PX;
}
