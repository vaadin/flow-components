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
 * What the sheet's view remembers from one drawing to the next. Where the
 * sheet is scrolled is not here: the scroll container holds that itself.
 */
export class ViewState {
  /** The offsets the next drawing scrolls to, for a sheet shown again. */
  pendingSheetScroll: { top: number; left: number } | null = null;

  /**
   * The first scrolling row with its top on screen when the server changed
   * the row heights, and how far below the top of the screen that top was:
   * the next drawing puts it back there.
   */
  rowAnchor: { row: number; below: number } | null = null;

  /** Where the sheet scrolled to keep that row in place, until its scroll event comes. */
  anchoredScrollTop: number | null = null;

  /** The sheet the last batch showed, by name, which is unique in a workbook. */
  shownSheetName: string | null = null;

  /**
   * The headers' and frozen panes' sizes at the last drawing, which the
   * scroll container leaves out of the view it pages.
   */
  pagedViewParts = {
    corner: { width: 0, height: 0 },
    frozen: { width: 0, columns: 0, height: 0, rows: 0 }
  };
}
