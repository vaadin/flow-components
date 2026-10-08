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
 * Put a popover in the top layer, where no edge of the sheet or of the page
 * around it cuts it off. A no-op for one already there, or one the browser
 * cannot show - not in the document, or no popover support - which then
 * stays where the page puts it.
 */
export function showInTopLayer(el: HTMLElement): void {
  try {
    if (el.isConnected && !el.matches(':popover-open')) {
      el.showPopover();
    }
  } catch {
    // Shown in place instead.
  }
}

/** Take a popover out of the top layer, if it is there. */
export function hideFromTopLayer(el: HTMLElement): void {
  try {
    if (el.matches(':popover-open')) {
      el.hidePopover();
    }
  } catch {
    // Not in the top layer.
  }
}
