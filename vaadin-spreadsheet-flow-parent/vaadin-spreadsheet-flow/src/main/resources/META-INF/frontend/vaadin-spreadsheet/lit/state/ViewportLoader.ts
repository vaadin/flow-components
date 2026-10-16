/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/** A 1-based, inclusive range of cells. */
export interface LoadRange {
  r1: number;
  c1: number;
  r2: number;
  c2: number;
}

/** What the loader needs from the sheet it loads for. */
export interface ViewportLoaderHost {
  /** The cells drawn now, the drawing buffer included; null before anything is measured. */
  drawnRange(): LoadRange | null;
  /** The sheet's size, its frozen panes, and the height and width of a default row and column in pixels. */
  sheet(): { rows: number; cols: number; frozenRows: number; frozenCols: number; rowPx: number; colPx: number };
  /** Whether the server is answering a request right now. */
  serverBusy(): boolean;
  /** Ask the server for the cells of a range. */
  load(range: LoadRange): void;
}

/** Clock, timers and frames, replaceable in tests. */
export interface ViewportLoaderClock {
  now(): number;
  setTimeout(callback: () => void, delay: number): unknown;
  clearTimeout(handle: unknown): void;
  /** Call back as the next frame is drawn, with the frame's time. */
  requestFrame(callback: (time: number) => void): void;
}

const realClock: ViewportLoaderClock = {
  now: () => performance.now(),
  setTimeout: (callback, delay) => setTimeout(callback, delay),
  clearTimeout: (handle) => clearTimeout(handle as ReturnType<typeof setTimeout>),
  requestFrame: (callback) => {
    requestAnimationFrame(callback);
  }
};

/** A first guess at a load's round trip, before one has been timed. */
const FIRST_ROUND_TRIP_MS = 200;

/** Round trips slower than this count as a slow connection. */
const SLOW_ROUND_TRIP_MS = 400;

/** How often to look again while waiting for the server or for scrolling to slow down. */
const RECHECK_MS = 30;

/** Scroll positions older than this do not count towards the scrolling speed. */
const SPEED_WINDOW_MS = 150;

/** With no scroll event for this long, and for two frames, the sheet has stopped. */
const STOPPED_AFTER_MS = 100;

/** Frames drawn without a scroll event, besides {@link STOPPED_AFTER_MS}, before the sheet has stopped. */
const FRAMES_TO_STOP = 2;

/** However long frames take, the sheet has stopped this long after its last scroll event. */
const LONGEST_WAIT_TO_STOP_MS = 1000;

/**
 * A machine drawing a scroll's frames further apart than this, fewer than 30
 * a second, cannot draw cells loaded on the way.
 */
const SLOW_FRAME_MS = 1000 / 30;

/** How many of a scroll's last frames are timed to tell a slow machine. */
const FRAMES_TIMED = 3;

/**
 * Decides when the server is asked for cells as the sheet scrolls, and for
 * which range.
 *
 * Every request makes the server load the range's cells, comments,
 * overlays and components, so asking for each row as it scrolls into view
 * - about 80 requests and 2.5 MB for one fast scroll over a thousand rows -
 * made a slow connection unusable: the cells arrived seconds after the
 * scrolling stopped. The rules:
 * <ul>
 * <li>Ask only when the drawn cells are not all loaded, and then load a
 * margin ahead in the direction of scrolling, so a steady scroll needs a
 * request only every screen or so.
 * <li>Once the sheet stops, tell the server where, even when its cells are
 * all loaded. The server uses the
 * range it was last given for more than cells: it closes the popup of a
 * button scrolled out of it, and lets go of pictures and components.
 * <li>One request of its own at a time. While the server answers, the sheet
 * may move on, and only the latest position is asked for: positions passed
 * while waiting are never loaded.
 * <li>A request is not held back while the server answers something else,
 * nor, when the sheet is not scrolling, while it answers our own: Flow keeps
 * it and sends it as soon as that answer is in. Waiting for the server to go
 * idle instead cost a round trip at startup, and left Flow idle in between,
 * so TestBench, which waits for Flow, read a sheet that was still loading.
 * Such a request is not timed.
 * <li>No request while the sheet moves faster than a load comes back, timed
 * from the round trips so far: the cells would arrive after the user has
 * scrolled past them. The request goes out once the scrolling slows down
 * or stops.
 * <li>On a slow connection the margin shrinks to a quarter screen, so what
 * is on screen arrives sooner.
 * <li>On a slow machine nothing is loaded until the scrolling stops, as in
 * GWT: a fling loaded 10-49 times, each load slowing the next frames
 * further. A machine is slow while it draws a scroll's frames fewer than 30
 * a second; six times slower than a fast one, they were 40-60 ms apart. Its
 * scroll events come as far apart as its frames, so a scroll stops once two
 * frames are drawn without a scroll event, as well as 100 ms: counted in
 * milliseconds alone, a fling with frames 100-190 ms apart stopped between
 * every two of them.
 * </ul>
 */
export class ViewportLoader {
  private loaded: LoadRange | null = null;
  /** The drawn range the last request was made for. */
  private reportedDrawn: LoadRange | null = null;
  private inFlightSince: number | null = null;
  /** Whether the request in flight was seen to keep the server busy, so its round trip means something. */
  private sawBusy = false;
  /** Whether the request in flight went out at once rather than after another one, so its round trip is its own. */
  private timed = false;
  private roundTrip = FIRST_ROUND_TRIP_MS;
  private samples: Array<{ t: number; top: number; left: number }> = [];
  private rowDirection = 0;
  private columnDirection = 0;
  private timer: unknown = null;
  /** Frames drawn since the last scroll event, counted until {@link FRAMES_TO_STOP}. */
  private framesSinceScroll = 0;
  private countingFrames = false;
  private lastFrameAt: number | null = null;
  /** How long the last {@link FRAMES_TIMED} frames of a scroll took, in ms. */
  private readonly frameTimes: number[] = [];
  private disposed = false;

  constructor(private readonly host: ViewportLoaderHost, private readonly clock: ViewportLoaderClock = realClock) {}

  /** The sheet scrolled to these offsets, in pixels. */
  scrolled(top: number, left: number): void {
    const now = this.clock.now();
    const last = this.samples[this.samples.length - 1];
    if (last !== undefined) {
      if (top !== last.top) {
        this.rowDirection = Math.sign(top - last.top);
        this.columnDirection = left !== last.left ? Math.sign(left - last.left) : 0;
      } else if (left !== last.left) {
        this.columnDirection = Math.sign(left - last.left);
        this.rowDirection = 0;
      }
    }
    this.samples.push({ t: now, top, left });
    while (this.samples.length > 2 && now - this.samples[0].t > SPEED_WINDOW_MS) {
      this.samples.shift();
    }
    this.framesSinceScroll = 0;
    this.countFrames();
  }

  /**
   * Load what the sheet shows if it is not loaded yet, or tell the server
   * where the sheet stopped - when now is the time. Called whenever the
   * drawn range may have changed.
   */
  check(): void {
    this.noteAnswered();
    if (this.inFlightSince !== null) {
      // Ours is still out: back once it is answered, to time it - and,
      // while the sheet scrolls, to ask then for wherever it has got to.
      this.checkLater();
      if (this.moving()) {
        return;
      }
    }
    const drawn = this.host.drawnRange();
    if (drawn === null) {
      return;
    }
    const loaded = this.isLoaded(drawn);
    if (loaded && sameRange(drawn, this.reportedDrawn)) {
      return;
    }
    if (loaded && this.moving()) {
      // Nothing to load: the server hears where the sheet stops.
      this.checkLater();
      return;
    }
    const target = this.target(drawn);
    if (!loaded && this.tooFast(drawn, target)) {
      this.checkLater();
      return;
    }
    this.timed = this.inFlightSince === null && !this.host.serverBusy();
    this.loaded = target;
    this.reportedDrawn = drawn;
    this.inFlightSince = this.clock.now();
    this.host.load(target);
    // Flow sends the request as the event is dispatched, or as soon as the
    // answer it waits for is in.
    this.sawBusy = this.host.serverBusy();
    // Back once the server has answered, to time the round trip and catch
    // up with where the sheet has gone meanwhile.
    this.checkLater();
  }

  /** Forget what was loaded: the server has thrown away what it sent. */
  reset(): void {
    this.loaded = null;
    this.reportedDrawn = null;
    this.inFlightSince = null;
    this.cancelTimer();
  }

  /** Stop looking again. */
  dispose(): void {
    this.disposed = true;
    this.cancelTimer();
  }

  /** How long a load takes, smoothed over the round trips so far, in ms. */
  get loadRoundTrip(): number {
    return this.roundTrip;
  }

  /**
   * Whether a request may still be on its way: one is out, or the loader
   * waits to decide. Flow does not know about the latter, so TestBench asks
   * here before it takes a scroll as done.
   */
  get pending(): boolean {
    return this.timer !== null;
  }

  private isLoaded(range: LoadRange): boolean {
    const loaded = this.loaded;
    return (
      loaded !== null &&
      loaded.r1 <= range.r1 &&
      loaded.c1 <= range.c1 &&
      loaded.r2 >= range.r2 &&
      loaded.c2 >= range.c2
    );
  }

  /** The drawn range plus the margin to load with it, within the sheet. */
  private target(drawn: LoadRange): LoadRange {
    const sheet = this.host.sheet();
    const rows = drawn.r2 - drawn.r1 + 1;
    const cols = drawn.c2 - drawn.c1 + 1;
    const slow = this.roundTrip > SLOW_ROUND_TRIP_MS;
    // A screen and a half ahead and half a screen behind on the axis that
    // scrolls; a quarter screen ahead on a slow connection.
    const ahead = (n: number) => (slow ? Math.ceil(n / 4) : n + Math.ceil(n / 2));
    const behind = (n: number) => (slow ? 0 : Math.ceil(n / 2));
    const down = this.rowDirection >= 0;
    const rowsBack = down ? behind(rows) : ahead(rows);
    const rowsAhead = down ? ahead(rows) : behind(rows);
    const right = this.columnDirection > 0;
    const left = this.columnDirection < 0;
    const colsBack = left ? ahead(cols) : right ? behind(cols) : 0;
    const colsAhead = right ? ahead(cols) : left ? behind(cols) : 0;
    return {
      r1: Math.max(sheet.frozenRows + 1, drawn.r1 - rowsBack),
      c1: Math.max(sheet.frozenCols + 1, drawn.c1 - colsBack),
      r2: Math.min(sheet.rows, drawn.r2 + rowsAhead),
      c2: Math.min(sheet.cols, drawn.c2 + colsAhead)
    };
  }

  /**
   * Whether the sheet may still be moving: a scroll event came in only just
   * now, or fewer than two frames ago.
   */
  private moving(): boolean {
    const last = this.samples[this.samples.length - 1];
    if (last === undefined) {
      return false;
    }
    const since = this.clock.now() - last.t;
    return since <= STOPPED_AFTER_MS || (this.framesSinceScroll < FRAMES_TO_STOP && since <= LONGEST_WAIT_TO_STOP_MS);
  }

  /** Count the frames drawn after the last scroll event, and time them. */
  private countFrames(): void {
    if (this.countingFrames || this.disposed) {
      return;
    }
    this.countingFrames = true;
    this.lastFrameAt = null;
    this.clock.requestFrame(this.frameDrawn);
  }

  private readonly frameDrawn = (time: number): void => {
    if (this.lastFrameAt !== null) {
      this.frameTimes.push(time - this.lastFrameAt);
      if (this.frameTimes.length > FRAMES_TIMED) {
        this.frameTimes.shift();
      }
    }
    this.lastFrameAt = time;
    this.framesSinceScroll++;
    if (this.framesSinceScroll < FRAMES_TO_STOP) {
      this.clock.requestFrame(this.frameDrawn);
    } else {
      this.countingFrames = false;
    }
  };

  /** Whether the sheet moves past the target's margin before a load could come back. */
  private tooFast(drawn: LoadRange, target: LoadRange): boolean {
    const first = this.samples[0];
    const last = this.samples[this.samples.length - 1];
    if (first === undefined || last === undefined || first === last || !this.moving()) {
      return false;
    }
    if (this.slowMachine()) {
      return true;
    }
    const sheet = this.host.sheet();
    const elapsed = Math.max(1, last.t - first.t);
    const rowsMoved = ((Math.abs(last.top - first.top) / elapsed) * this.roundTrip) / sheet.rowPx;
    const colsMoved = ((Math.abs(last.left - first.left) / elapsed) * this.roundTrip) / sheet.colPx;
    const rowMargin = this.rowDirection < 0 ? drawn.r1 - target.r1 : target.r2 - drawn.r2;
    const colMargin = this.columnDirection < 0 ? drawn.c1 - target.c1 : target.c2 - drawn.c2;
    return rowsMoved > Math.max(rowMargin, 1) || (colsMoved > 0 && colsMoved > Math.max(colMargin, 1));
  }

  /**
   * Whether the machine draws a scroll's frames too slowly to draw cells
   * loaded on the way. The lower median of the last frames: one long frame,
   * a load being drawn, does not make a fast machine slow, nor one short
   * frame a slow machine fast.
   */
  private slowMachine(): boolean {
    const sorted = [...this.frameTimes].sort((a, b) => a - b);
    // Until a scroll's frames are timed, a machine counts as fast.
    return (sorted[Math.floor((sorted.length - 1) / 2)] ?? 0) > SLOW_FRAME_MS;
  }

  /**
   * Time the request in flight, once the server is idle again. A request
   * never seen to keep the server busy - none is when there is no server to
   * ask - counts as answered, untimed.
   */
  private noteAnswered(): void {
    if (this.inFlightSince === null) {
      return;
    }
    if (this.host.serverBusy()) {
      this.sawBusy = true;
      return;
    }
    if (this.sawBusy && this.timed) {
      this.roundTrip = (this.roundTrip + (this.clock.now() - this.inFlightSince)) / 2;
    }
    this.inFlightSince = null;
  }

  private checkLater(): void {
    if (this.timer !== null) {
      return;
    }
    this.timer = this.clock.setTimeout(() => {
      this.timer = null;
      this.check();
    }, RECHECK_MS);
  }

  private cancelTimer(): void {
    if (this.timer !== null) {
      this.clock.clearTimeout(this.timer);
      this.timer = null;
    }
  }
}

function sameRange(a: LoadRange, b: LoadRange | null): boolean {
  return b !== null && a.r1 === b.r1 && a.c1 === b.c1 && a.r2 === b.r2 && a.c2 === b.c2;
}
