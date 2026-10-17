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
 * When the sheet is drawn. A drawing asked for comes once the task asking is
 * done, folding all asked for in it into one: the server sends several
 * changes to a response. One asked for by a handler of the sheet's own events
 * comes as the outermost handler running ends, and before it tells the
 * server anything: the parts show the change by the time the event has been
 * handled, and the server hears of what is on screen.
 *
 * The drawing calls {@link drawn} as it starts: drawn, the sheet has nothing
 * left to draw of what was asked for.
 */
export class RenderScheduler {
  private queued = false;
  private depth = 0;

  constructor(private readonly draw: () => void) {}

  /** Asks for a drawing; see the class description. */
  schedule(): void {
    // Asked for already, one microtask is enough. Only for the time: a second
    // would find the sheet drawn by the first, and draw nothing.
    if (this.queued) {
      return;
    }
    this.queued = true;
    // A microtask, not an animation frame: it still folds a response into one
    // drawing, and the sheet is never a frame behind the answer a caller has
    // already been given.
    queueMicrotask(() => {
      if (this.queued) {
        this.draw();
      }
    });
  }

  /** The sheet is being drawn, with all asked for so far. */
  drawn(): void {
    this.queued = false;
  }

  /** Runs a handler of the sheet's own events, drawing as the outermost ends. */
  handle(work: () => void): void {
    this.depth++;
    try {
      work();
    } finally {
      this.depth--;
      if (this.depth === 0 && this.queued) {
        this.draw();
      }
    }
  }

  /**
   * About to tell the server something: from a handler, what it asked for is
   * drawn first. Told while the server's own changes come in, nothing is: they
   * are drawn once they are all in.
   */
  beforeSend(): void {
    if (this.depth > 0 && this.queued) {
      this.draw();
    }
  }

  /** Asks for no drawing any more: the sheet has gone. */
  cancel(): void {
    this.queued = false;
  }
}
