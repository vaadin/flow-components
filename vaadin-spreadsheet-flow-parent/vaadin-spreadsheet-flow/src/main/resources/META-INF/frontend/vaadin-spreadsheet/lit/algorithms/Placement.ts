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
 * Where the sheet's floating parts go on screen: a popup button's popup and
 * a link cell's tooltip. Pure functions of boxes in viewport pixels.
 */

/**
 * Where a popup goes, in screen coordinates.
 *
 * Under the button's cell, its right edge on the cell's right edge - or
 * starting at that edge instead, when ending there would take it past the
 * left side of the scrolling pane. Above the cell when it runs past the
 * pane's bottom, and never above the pane's top: a popup too tall for
 * either side hangs from the top and runs past the bottom.
 */
export function popupPlacement(
  cell: { readonly top: number; readonly right: number; readonly bottom: number },
  pane: { readonly left: number; readonly top: number; readonly bottom: number },
  width: number,
  height: number
): { left: number; top: number } {
  let left = cell.right - width;
  if (left < pane.left) {
    left = cell.right;
  }
  let top = cell.bottom;
  if (top + height > pane.bottom) {
    top = cell.top - height;
  }
  if (top < pane.top) {
    top = pane.top;
  }
  return { left, top };
}

/**
 * Where a link cell's tooltip goes, in viewport pixels: left-aligned under the cell; right-aligned with the cell
 * when it is wider than the cell and would run off the right of the window
 * but fits that way; and above the cell when there is no room below but
 * room above.
 */
export function linkTooltipPosition(
  cell: { left: number; top: number; right: number; bottom: number },
  size: { width: number; height: number },
  view: { width: number; height: number }
): { left: number; top: number } {
  let left = cell.left;
  const wider = size.width - (cell.right - cell.left);
  if (wider > 0 && view.width - left < size.width && left >= wider) {
    left -= wider;
  }
  let top = cell.bottom;
  if (view.height - cell.bottom < size.height && cell.top >= size.height) {
    top = cell.top - size.height;
  }
  return { left, top };
}

/**
 * Where the command a selection made by touch offers goes, in viewport
 * pixels: centred over the selection and 5px above it - or 5px below it
 * when above would take it past `cellsTop`, the top of the cells on screen -
 * and kept inside the window across.
 */
export function touchCommandPosition(
  selection: { left: number; top: number; right: number; bottom: number },
  size: { width: number; height: number },
  cellsTop: number,
  viewWidth: number
): { left: number; top: number } {
  const centred = (selection.left + selection.right) / 2 - size.width / 2;
  const left = Math.max(0, Math.min(centred, viewWidth - size.width));
  const above = selection.top - 5 - size.height;
  return { left, top: above < cellsTop ? selection.bottom + 5 : above };
}
