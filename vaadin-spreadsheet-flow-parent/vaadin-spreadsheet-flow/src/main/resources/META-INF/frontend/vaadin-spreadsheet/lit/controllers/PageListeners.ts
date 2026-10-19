/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { ReactiveController } from 'lit';
import { listen } from '../dom/Listen.js';

/**
 * A listener on something the sheet does not own: the page's document. Made
 * with {@link onDocument}, which checks the handler against the kind of event;
 * after that it is only handed to `addEventListener`.
 */
export type PageListener = readonly [target: EventTarget, type: string, listener: (event: never) => void];

/** A listener on the page's document, whose handler takes the kind of event it listens for. */
export function onDocument<K extends keyof DocumentEventMap>(
  type: K,
  listener: (event: DocumentEventMap[K]) => void
): PageListener {
  return [document, type, listener];
}

/**
 * Listeners on the page, there while the sheet is in it. They hold the sheet:
 * taken off as it leaves, a sheet removed from the page can be collected.
 * Left on, every removed sheet stayed in memory, with its cells, and went on
 * handling every key press and mouse move in the page.
 */
export class PageListeners implements ReactiveController {
  private listening: AbortController | null = null;

  constructor(private readonly listeners: readonly PageListener[]) {}

  /** The sheet joined the page: listen, once however often it joins. */
  hostConnected(): void {
    if (this.listening !== null) {
      return;
    }
    this.listening = new AbortController();
    for (const [target, type, listener] of this.listeners) {
      listen(target, type, listener, this.listening.signal);
    }
  }

  /** The sheet left the page: listen no more. */
  hostDisconnected(): void {
    this.listening?.abort();
    this.listening = null;
  }
}
