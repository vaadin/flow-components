/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { PageListeners } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/PageListeners.js';

/**
 * Listeners on the page, there while the sheet is in it, and gone once it has
 * left: they hold the sheet.
 */
describe('Spreadsheet — listeners on the page', () => {
  let target: EventTarget;
  let heard: string[];
  let listeners: PageListeners;

  beforeEach(() => {
    target = new EventTarget();
    heard = [];
    listeners = new PageListeners([
      [target, 'one', () => heard.push('one')],
      [target, 'two', (e: Event) => heard.push(e.type)]
    ]);
  });

  function dispatchBoth(): void {
    target.dispatchEvent(new Event('one'));
    target.dispatchEvent(new Event('two'));
  }

  it('listens to nothing before the sheet joins the page', () => {
    dispatchBoth();

    expect(heard).to.be.empty;
  });

  it('listens to each while the sheet is in the page', () => {
    listeners.hostConnected();

    dispatchBoth();

    expect(heard).to.eql(['one', 'two']);
  });

  it('listens once however often the sheet joins the page', () => {
    listeners.hostConnected();
    listeners.hostConnected();

    dispatchBoth();

    expect(heard).to.eql(['one', 'two']);
  });

  it('listens no more once the sheet has left the page', () => {
    listeners.hostConnected();
    listeners.hostDisconnected();

    dispatchBoth();

    expect(heard).to.be.empty;
  });

  it('listens no more once the sheet has left the page, however often it joined', () => {
    listeners.hostConnected();
    listeners.hostConnected();
    listeners.hostDisconnected();

    dispatchBoth();

    expect(heard).to.be.empty;
  });

  it('listens again once the sheet is back in the page', () => {
    listeners.hostConnected();
    listeners.hostDisconnected();
    listeners.hostConnected();

    dispatchBoth();

    expect(heard).to.eql(['one', 'two']);
  });
});
