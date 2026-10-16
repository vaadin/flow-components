/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { expect } from 'chai';
import {
  type LoadRange,
  ViewportLoader
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/ViewportLoader.js';

/** A clock that only moves when told to, running the timers that fall due. */
function fakeClock() {
  let now = 0;
  let nextId = 1;
  const timers: Array<{ at: number; fn: () => void; id: number }> = [];
  // Frames are drawn every 16 ms, at fixed times, as the screen refreshes.
  let frameInterval = 16;
  let nextFrame = frameInterval;
  let frameCallbacks: Array<(time: number) => void> = [];
  let framesRequested = 0;
  return {
    get framesRequested() {
      return framesRequested;
    },
    clock: {
      now: () => now,
      setTimeout: (fn: () => void, delay: number) => {
        const id = nextId++;
        timers.push({ at: now + delay, fn, id });
        return id;
      },
      clearTimeout: (id: unknown) => {
        const index = timers.findIndex((t) => t.id === id);
        if (index >= 0) {
          timers.splice(index, 1);
        }
      },
      requestFrame: (fn: (time: number) => void) => {
        framesRequested++;
        frameCallbacks.push(fn);
      }
    },
    /** Frames come this many ms apart from now on; Infinity for none at all. */
    setFrameInterval(ms: number) {
      frameInterval = ms;
      nextFrame = now + ms;
    },
    advance(ms: number) {
      const end = now + ms;
      for (;;) {
        while (nextFrame <= now) {
          nextFrame += frameInterval;
        }
        timers.sort((a, b) => a.at - b.at);
        const timer = timers[0];
        const frameAt = frameCallbacks.length > 0 ? nextFrame : Number.POSITIVE_INFINITY;
        const timerAt = timer === undefined ? Number.POSITIVE_INFINITY : timer.at;
        if (Math.min(frameAt, timerAt) > end) {
          break;
        }
        if (frameAt <= timerAt) {
          now = frameAt;
          const callbacks = frameCallbacks;
          frameCallbacks = [];
          callbacks.forEach((fn) => fn(now));
          nextFrame += frameInterval;
        } else {
          timers.shift();
          now = timer!.at;
          timer!.fn();
        }
      }
      now = end;
    }
  };
}

function setUp(sheet: Partial<{ rows: number; frozenRows: number; noMargin: boolean }> = {}) {
  const time = fakeClock();
  const loads: LoadRange[] = [];
  let drawn: LoadRange | null = { r1: 1, c1: 1, r2: 30, c2: 10 };
  let busy = false;
  let busyWhileLoading = false;
  const loader = new ViewportLoader(
    {
      drawnRange: () => drawn,
      sheet: () => ({
        rows: sheet.rows ?? 1000,
        cols: 50,
        frozenRows: sheet.frozenRows ?? 0,
        frozenCols: 0,
        rowPx: 20,
        colPx: 64,
        noMargin: sheet.noMargin ?? false
      }),
      serverBusy: () => busy,
      load: (range) => {
        loads.push(range);
        busy = busyWhileLoading;
      }
    },
    time.clock
  );
  return {
    loader,
    loads,
    time,
    show(range: LoadRange) {
      drawn = range;
    },
    /** As Flow does: busy from the moment a request goes out until it is answered. */
    serverAnswersAfterEachLoad() {
      busyWhileLoading = true;
    },
    answer() {
      busy = false;
    },
    setBusy(value: boolean) {
      busy = value;
    },
    /** Scroll to a row at a reading pace, then stop. */
    scrollTo(top: number, left = 0) {
      loader.scrolled(top, left);
      time.advance(200);
    }
  };
}

describe('ViewportLoader', () => {
  it('loads the drawn cells and a screen and a half below them at first', () => {
    const t = setUp();

    t.loader.check();

    expect(t.loads).to.eql([{ r1: 1, c1: 1, r2: 75, c2: 10 }]);
  });

  it('asks nothing while the sheet scrolls inside what was loaded, then says where it stopped', () => {
    // The server closes popups and lets go of pictures by the range it was
    // last given, as it did with the GWT client, which told it after every
    // scroll.
    const t = setUp();
    t.loader.check();
    t.scrollTo(0);

    t.loader.scrolled(400, 0);
    t.show({ r1: 21, c1: 1, r2: 50, c2: 10 });
    t.loader.check();
    expect(t.loads, 'asked while scrolling').to.have.length(1);

    t.time.advance(200);

    expect(t.loads).to.have.length(2);
    expect(t.loads[1]).to.eql({ r1: 6, c1: 1, r2: 95, c2: 10 });
  });

  it('asks nothing more once the server knows where the sheet stopped', () => {
    const t = setUp();
    t.loader.check();
    t.scrollTo(0);
    t.scrollTo(400);
    t.show({ r1: 21, c1: 1, r2: 50, c2: 10 });
    t.loader.check();

    t.loader.check();
    t.time.advance(1000);

    expect(t.loads).to.have.length(2);
  });

  it('asks at once, with its own request still out, for cells a layout change shows', () => {
    // A tab shown or a window resized redraws the sheet bigger before the
    // first cells are in. Waiting for them left Flow idle in between, and
    // TestBench saw half a table.
    const t = setUp();
    t.serverAnswersAfterEachLoad();
    t.loader.check();

    t.show({ r1: 1, c1: 1, r2: 60, c2: 20 });
    t.loader.check();

    expect(t.loads).to.have.length(2);
    expect(t.loads[1]).to.eql({ r1: 1, c1: 1, r2: 150, c2: 20 });
  });

  it('counts as pending while its request is out or it waits to decide', () => {
    const t = setUp();
    expect(t.loader.pending, 'before anything').to.be.false;

    t.loader.check();
    expect(t.loader.pending, 'with its request out').to.be.true;
    t.time.advance(50);
    expect(t.loader.pending, 'answered').to.be.false;

    t.loader.scrolled(400, 0);
    t.show({ r1: 21, c1: 1, r2: 50, c2: 10 });
    t.loader.check();
    expect(t.loader.pending, 'waiting for the sheet to stop').to.be.true;
    t.time.advance(400);
    expect(t.loader.pending, 'told where it stopped').to.be.false;
  });

  it('asks again once the drawn cells leave what was loaded, half a screen behind and a screen and a half ahead', () => {
    const t = setUp();
    t.loader.check();

    t.scrollTo(0);
    t.scrollTo(1200);
    t.show({ r1: 61, c1: 1, r2: 90, c2: 10 });
    t.loader.check();

    expect(t.loads[1]).to.eql({ r1: 46, c1: 1, r2: 135, c2: 10 });
  });

  it('loads the drawn cells alone on a sheet that loads no margin', () => {
    const t = setUp({ noMargin: true });

    t.loader.check();

    expect(t.loads).to.deep.equal([{ r1: 1, c1: 1, r2: 30, c2: 10 }]);
  });

  it('with no margin, holds back while the sheet scrolls, and asks for the drawn cells once it stops', () => {
    const t = setUp({ noMargin: true });
    t.loader.check();

    // Ten rows every 80 ms: past any margin before a load could come back.
    // The first scroll event's speed is not known yet, and it is asked for.
    for (let step = 1; step <= 8; step++) {
      t.loader.scrolled(step * 200, 0);
      t.time.advance(80);
      t.show({ r1: 1 + step * 10, c1: 1, r2: 30 + step * 10, c2: 10 });
      t.loader.check();
    }
    expect(t.loads, 'asked while scrolling').to.deep.equal([
      { r1: 1, c1: 1, r2: 30, c2: 10 },
      { r1: 11, c1: 1, r2: 40, c2: 10 }
    ]);

    t.time.advance(400);

    expect(t.loads[2]).to.deep.equal({ r1: 81, c1: 1, r2: 110, c2: 10 });
  });

  it('loads ahead upwards when scrolling up', () => {
    const t = setUp();
    t.scrollTo(10000);
    t.scrollTo(9980);
    t.show({ r1: 500, c1: 1, r2: 529, c2: 10 });

    t.loader.check();

    expect(t.loads[0]).to.eql({ r1: 455, c1: 1, r2: 544, c2: 10 });
  });

  it('loads columns ahead too when scrolling sideways', () => {
    const t = setUp();
    t.scrollTo(0, 0);
    t.scrollTo(0, 1200);
    t.show({ r1: 1, c1: 20, r2: 30, c2: 29 });

    t.loader.check();

    expect(t.loads[0]).to.eql({ r1: 1, c1: 15, r2: 75, c2: 44 });
  });

  it('asks one thing at a time, and then for where the sheet is, not where it was', () => {
    // Positions passed while the server answers are never loaded: a fast
    // scroll on a slow connection otherwise queued up every one of them.
    const t = setUp();
    t.serverAnswersAfterEachLoad();
    t.loader.check();

    t.loader.scrolled(2000, 0);
    t.time.advance(20);
    t.show({ r1: 101, c1: 1, r2: 130, c2: 10 });
    t.loader.check();
    t.loader.scrolled(4000, 0);
    t.time.advance(20);
    t.show({ r1: 201, c1: 1, r2: 230, c2: 10 });
    t.loader.check();
    expect(t.loads, 'asked while the server was busy').to.have.length(1);

    t.answer();
    t.time.advance(200);

    expect(t.loads).to.have.length(2);
    expect(t.loads[1].r1).to.equal(186);
  });

  it('holds back while the sheet moves faster than a load comes back, and asks once it stops', () => {
    const t = setUp();
    t.loader.check();

    // 400 pixels every 10 ms: far past any margin before a 200 ms load
    // could come back.
    for (let top = 400; top <= 4000; top += 400) {
      t.loader.scrolled(top, 0);
      t.time.advance(10);
    }
    t.show({ r1: 201, c1: 1, r2: 230, c2: 10 });
    t.loader.check();
    expect(t.loads, 'asked while flying past').to.have.length(1);

    t.time.advance(150);

    expect(t.loads).to.have.length(2);
    expect(t.loads[1].r1).to.equal(186);
  });

  // On a slow machine frames are 100-190 ms apart, and so are a scroll's
  // events: a fling counted as stopped between every two of them, and
  // loaded 11-47 times, each load slowing the next frames further.

  it('on a machine drawing slow frames, holds back while the sheet scrolls, and asks once it stops', () => {
    const t = setUp();
    t.loader.check();
    t.time.setFrameInterval(120);

    // 10 rows a frame: slow enough to load on the way on a fast machine.
    for (let step = 1; step <= 8; step++) {
      t.loader.scrolled(step * 200, 0);
      t.time.advance(120);
      t.show({ r1: 1 + step * 10, c1: 1, r2: 30 + step * 10, c2: 10 });
      t.loader.check();
    }
    expect(t.loads, 'asked while scrolling').to.have.length(1);

    t.time.advance(400);

    expect(t.loads).to.have.length(2);
    expect(t.loads[1].r1).to.be.lessThan(81);
    expect(t.loads[1].r2).to.be.greaterThan(110);
  });

  it('on a fast machine, asks on the way when the sheet scrolls slowly enough', () => {
    const t = setUp();
    t.loader.check();

    for (let step = 1; step <= 8; step++) {
      t.loader.scrolled(step * 200, 0);
      t.time.advance(80);
      t.show({ r1: 1 + step * 10, c1: 1, r2: 30 + step * 10, c2: 10 });
      t.loader.check();
    }

    expect(t.loads.length, 'asked while scrolling').to.be.greaterThan(1);
  });

  it('at a fast frame rate, waits 100 ms before the sheet counts as stopped', () => {
    // Two frames are 32 ms at 60 Hz: the pause between two turns of a wheel
    // would count as a stop, and each would ask the server again.
    const t = setUp();
    t.loader.check();
    t.scrollTo(0);
    t.loader.scrolled(400, 0);
    t.show({ r1: 21, c1: 1, r2: 50, c2: 10 });
    t.loader.check();

    t.time.advance(60);
    expect(t.loads, 'stopped after two frames').to.have.length(1);
    t.time.advance(100);

    expect(t.loads).to.have.length(2);
  });

  it('times the frames of a scroll afresh, after a pause', () => {
    // A frame timed from the last one of a scroll seconds ago would look
    // like a slow machine's, and hold back what a fast one loads on the way.
    const t = setUp();
    t.loader.check();
    t.scrollTo(0);
    t.time.advance(2000);

    t.loader.scrolled(1000, 0);
    t.time.advance(16);
    t.show({ r1: 51, c1: 1, r2: 80, c2: 10 });
    t.loader.check();

    expect(t.loads).to.have.length(2);
  });

  it('counts the time between slow frames as scrolling, not as a stop', () => {
    const t = setUp();
    t.loader.check();
    t.scrollTo(0);
    t.time.setFrameInterval(150);

    // Inside what was loaded: the only request would be the stop's.
    for (let step = 1; step <= 4; step++) {
      t.loader.scrolled(step * 40, 0);
      t.time.advance(140);
      t.show({ r1: 1 + step * 2, c1: 1, r2: 30 + step * 2, c2: 10 });
      t.loader.check();
    }
    expect(t.loads, 'stopped on the way').to.have.length(1);

    t.time.advance(400);

    expect(t.loads).to.have.length(2);
  });

  // In a production build six times slower, frames are 40-60 ms apart and a
  // fling's events about 60 ms: frames under 50 ms counted as a fast
  // machine's, and the fling loaded 10 times.

  /** Scroll on by ten rows in 100 ms, the two frames after it this many ms apart. */
  function scrollOn(t: ReturnType<typeof setUp>, step: number, frameMs: number) {
    t.time.setFrameInterval(frameMs);
    t.loader.scrolled(step * 200, 0);
    t.time.advance(100);
    t.show({ r1: 1 + step * 10, c1: 1, r2: 30 + step * 10, c2: 10 });
    t.loader.check();
  }

  it('on a machine drawing fewer than 30 frames a second, holds back while the sheet scrolls', () => {
    const t = setUp();
    t.loader.check();

    for (let step = 1; step <= 8; step++) {
      scrollOn(t, step, 45);
    }
    expect(t.loads, 'asked while scrolling').to.have.length(1);

    t.time.advance(400);

    expect(t.loads).to.have.length(2);
  });

  it('on a machine drawing 30 frames a second or more, asks on the way', () => {
    const t = setUp();
    t.loader.check();

    for (let step = 1; step <= 8; step++) {
      scrollOn(t, step, 28);
    }

    expect(t.loads.length, 'asked while scrolling').to.be.greaterThan(1);
  });

  it('takes a fast machine with one long frame, a load being drawn, for a fast machine still', () => {
    const t = setUp();
    t.loader.check();
    scrollOn(t, 1, 16);

    // A frame 100 ms long, the sheet scrolling on.
    t.time.setFrameInterval(100);
    t.loader.scrolled(1000, 0);
    t.time.advance(150);
    t.loader.scrolled(1100, 0);
    t.time.advance(51);
    t.show({ r1: 56, c1: 1, r2: 85, c2: 10 });
    t.loader.check();

    expect(t.loads, 'asked on the way').to.have.length(2);
  });

  it('takes a fast machine slowing down for a slow one from its second slow frame', () => {
    const t = setUp();
    t.loader.check();
    for (let step = 1; step <= 3; step++) {
      scrollOn(t, step, 16);
    }

    scrollOn(t, 4, 45);
    scrollOn(t, 5, 45);

    expect(t.loads, 'asked while scrolling').to.have.length(1);
  });

  it('counts a machine whose frames were not timed yet as a fast one', () => {
    const t = setUp();
    t.loader.check();
    t.time.setFrameInterval(Number.POSITIVE_INFINITY);

    t.loader.scrolled(200, 0);
    t.time.advance(100);
    t.loader.scrolled(400, 0);
    t.show({ r1: 51, c1: 1, r2: 80, c2: 10 });
    t.loader.check();

    expect(t.loads, 'asked on the way').to.have.length(2);
  });

  it('takes a slow machine with one short frame for a slow machine still', () => {
    const t = setUp();
    t.loader.check();

    for (let step = 1; step <= 8; step++) {
      scrollOn(t, step, step === 5 ? 28 : 45);
    }

    expect(t.loads, 'asked while scrolling').to.have.length(1);
  });

  it('counts the sheet as stopped a second after its last scroll event, with no frames drawn', () => {
    // A tab in the background draws no frames.
    const t = setUp();
    t.loader.check();
    t.scrollTo(0);
    t.time.setFrameInterval(Number.POSITIVE_INFINITY);
    t.loader.scrolled(400, 0);
    t.show({ r1: 21, c1: 1, r2: 50, c2: 10 });
    t.loader.check();

    t.time.advance(500);
    expect(t.loads, 'stopped without frames').to.have.length(1);
    t.time.advance(600);

    expect(t.loads).to.have.length(2);
  });

  it('times the round trips and loads a quarter screen ahead on a slow connection', () => {
    const t = setUp();
    t.serverAnswersAfterEachLoad();
    t.loader.check();
    t.time.advance(1000);
    t.answer();
    t.time.advance(30);
    expect(t.loader.loadRoundTrip).to.be.greaterThan(400);

    t.scrollTo(0);
    t.scrollTo(6000);
    t.show({ r1: 301, c1: 1, r2: 330, c2: 10 });
    t.loader.check();

    expect(t.loads[1]).to.eql({ r1: 301, c1: 1, r2: 338, c2: 10 });
  });

  it('counts a request no server was seen to answer as answered, untimed', () => {
    const t = setUp();
    t.loader.check();
    t.scrollTo(0);
    t.scrollTo(4000);
    t.show({ r1: 201, c1: 1, r2: 230, c2: 10 });

    t.loader.check();

    expect(t.loads).to.have.length(2);
    expect(t.loader.loadRoundTrip).to.equal(200);
  });

  it('stays inside the sheet and below its frozen rows', () => {
    const t = setUp({ rows: 40, frozenRows: 3 });
    t.show({ r1: 4, c1: 1, r2: 33, c2: 10 });

    t.loader.check();

    expect(t.loads[0]).to.eql({ r1: 4, c1: 1, r2: 40, c2: 10 });
  });

  it('forgets what was loaded on reset, and loads it again', () => {
    const t = setUp();
    t.loader.check();

    t.loader.reset();
    t.loader.check();

    expect(t.loads).to.have.length(2);
    expect(t.loads[1]).to.eql(t.loads[0]);
  });

  it('asks nothing before anything is drawn', () => {
    const t = setUp();
    t.show(null as unknown as LoadRange);

    t.loader.check();

    expect(t.loads).to.be.empty;
  });

  it('stops looking again once disposed', () => {
    const t = setUp();
    t.serverAnswersAfterEachLoad();
    t.loader.check();
    t.loader.scrolled(4000, 0);
    t.show({ r1: 201, c1: 1, r2: 230, c2: 10 });
    t.loader.check();

    t.loader.dispose();
    t.answer();
    t.time.advance(1000);

    expect(t.loads).to.have.length(1);
  });

  it('counts no frames once disposed', () => {
    const t = setUp();
    t.loader.dispose();

    t.loader.scrolled(400, 0);

    expect(t.time.framesRequested).to.equal(0);
  });

  it('asks at once while the server answers something else, for Flow to send next', () => {
    // Held back until the server was idle, the first load cost a round trip
    // of its own, and TestBench, which waits only for Flow, saw an idle
    // client with the cells not yet asked for.
    const t = setUp();
    t.setBusy(true);

    t.loader.check();

    expect(t.loads).to.have.length(1);
  });

  it('does not time a request that went out after another one', () => {
    const t = setUp();
    t.serverAnswersAfterEachLoad();
    t.setBusy(true);
    t.loader.check();

    t.time.advance(2000);
    t.answer();
    t.time.advance(30);

    expect(t.loader.loadRoundTrip).to.equal(200);
  });
});
