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
  return {
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
      }
    },
    advance(ms: number) {
      const end = now + ms;
      for (;;) {
        timers.sort((a, b) => a.at - b.at);
        const next = timers[0];
        if (next === undefined || next.at > end) {
          break;
        }
        timers.shift();
        now = next.at;
        next.fn();
      }
      now = end;
    }
  };
}

/** A sheet of 1,000 rows by 50 columns, showing 30 rows by 10 columns. */
function setUp(sheet: Partial<{ rows: number; frozenRows: number }> = {}) {
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
        colPx: 64
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

  it('asks nothing while the drawn cells stay inside what was loaded', () => {
    const t = setUp();
    t.loader.check();

    t.scrollTo(0);
    t.scrollTo(400);
    t.show({ r1: 21, c1: 1, r2: 50, c2: 10 });
    t.loader.check();

    expect(t.loads).to.have.length(1);
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
    t.setBusy(true);
    t.loader.check();

    t.loader.dispose();
    t.setBusy(false);
    t.time.advance(1000);

    expect(t.loads).to.be.empty;
  });
});
