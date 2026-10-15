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
 * Adds a listener for as long as a signal lasts: aborting the signal's
 * controller removes every listener added with it, so an owner releases all
 * of them in one call. The listener takes the event its target dispatches -
 * a CustomEvent with its detail, say - without a cast where it is added.
 */
export function listen<E extends Event>(
  target: EventTarget,
  type: string,
  listener: (event: E) => void,
  signal: AbortSignal
): void {
  target.addEventListener(type, listener as EventListener, { signal });
}
