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
 * What the sheet asks of the Flow client that owns its element.
 */

/**
 * The Flow application the sheet belongs to.
 *
 * The server pushes it as an {@code appId} property on the host — the
 * component is registered against one running application, and looking a
 * virtual child up in the wrong one silently finds nothing.
 */
export function overlayComponentAppId(host: HTMLElement): string {
  return String((host as unknown as { appId?: string }).appId ?? '');
}

/**
 * Whether Flow is waiting for the server to answer the application the
 * host belongs to. False when there is no Flow client to ask.
 */
export function flowRequestInFlight(host: HTMLElement): boolean {
  const clients = (
    window as unknown as { Vaadin?: { Flow?: { clients?: Record<string, { isActive?: () => boolean }> } } }
  ).Vaadin?.Flow?.clients;
  return clients?.[overlayComponentAppId(host)]?.isActive?.() === true;
}
