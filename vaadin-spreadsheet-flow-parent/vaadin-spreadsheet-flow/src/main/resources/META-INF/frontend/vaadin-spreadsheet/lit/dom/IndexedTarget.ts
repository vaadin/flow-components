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
 * The element an event came from that carries a 1-based index: the nearest
 * one to the event's target matching `selector`, with the index it holds in
 * `data-<key>`. Null when the event came from none, or from one whose index
 * is not a number from 1 on.
 */
export function indexedTarget(e: Event, selector: string, key: string): { element: HTMLElement; index: number } | null {
  const target = e.target;
  if (!(target instanceof Element)) {
    return null;
  }
  const element = target.closest<HTMLElement>(selector);
  if (element === null) {
    return null;
  }
  const index = Number(element.dataset[key]);
  return Number.isFinite(index) && index >= 1 ? { element, index } : null;
}
