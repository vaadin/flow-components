/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { RenderScheduler } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/RenderScheduler.js';

/**
 * When the sheet is drawn: once for all asked for in a task, as the outermost
 * of the sheet's handlers ends, and before a handler tells the server.
 */
/** A scheduler of its own for each test, counting its drawings: one left asking cannot draw in another test. */
function counting(): { scheduler: RenderScheduler; draws: () => number } {
  let draws = 0;
  // As the sheet's drawing does, telling the scheduler as it starts.
  const scheduler: RenderScheduler = new RenderScheduler(() => {
    scheduler.drawn();
    draws++;
  });
  return { scheduler, draws: () => draws };
}

describe('Spreadsheet — when it is drawn', () => {
  let scheduler: RenderScheduler;
  let draws: () => number;

  beforeEach(() => {
    ({ scheduler, draws } = counting());
  });

  it('draws nothing until the task asking is done', () => {
    scheduler.schedule();

    expect(draws()).to.equal(0);
  });

  it('draws once for all asked for in a task', async () => {
    scheduler.schedule();
    scheduler.schedule();
    scheduler.schedule();
    await Promise.resolve();

    expect(draws()).to.equal(1);
  });

  it('draws again for what is asked for after a drawing', async () => {
    scheduler.schedule();
    await Promise.resolve();

    scheduler.schedule();
    await Promise.resolve();

    expect(draws()).to.equal(2);
  });

  it('draws nothing asked for before the sheet was drawn some other way', async () => {
    scheduler.schedule();

    scheduler.drawn();
    await Promise.resolve();

    expect(draws()).to.equal(0);
  });

  it('draws what a handler asked for as it ends', () => {
    scheduler.handle(() => scheduler.schedule());

    expect(draws()).to.equal(1);
  });

  it('draws nothing as a handler that asked for nothing ends', () => {
    scheduler.handle(() => {});

    expect(draws()).to.equal(0);
  });

  it('draws once, as the outermost handler ends, for handlers one inside another', () => {
    const atEachStep: number[] = [];

    scheduler.handle(() => {
      scheduler.schedule();
      scheduler.handle(() => scheduler.schedule());
      atEachStep.push(draws());
      scheduler.schedule();
    });

    expect([...atEachStep, draws()]).to.eql([0, 1]);
  });

  it('draws what a handler asked for before it tells the server', () => {
    const atSend: number[] = [];

    scheduler.handle(() => {
      scheduler.schedule();
      scheduler.beforeSend();
      atSend.push(draws());
    });

    expect([...atSend, draws()]).to.eql([1, 1]);
  });

  it('draws nothing for what the server tells the sheet before a send it makes', () => {
    scheduler.schedule();

    scheduler.beforeSend();

    expect(draws()).to.equal(0);
  });

  it('draws nothing before a send from a handler that asked for nothing', () => {
    scheduler.handle(() => scheduler.beforeSend());

    expect(draws()).to.equal(0);
  });

  it('draws once a handler that failed has ended', () => {
    expect(() =>
      scheduler.handle(() => {
        scheduler.schedule();
        throw new Error('failed');
      })
    ).to.throw('failed');

    expect(draws()).to.equal(1);
    scheduler.handle(() => scheduler.schedule());
    expect(draws(), 'a handler after it').to.equal(2);
  });

  it('draws nothing once the sheet has gone', async () => {
    scheduler.schedule();

    scheduler.cancel();
    await Promise.resolve();

    expect(draws()).to.equal(0);
  });
});
