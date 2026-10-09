/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { sendKeys } from '@web/test-runner-commands';

import {
  Spreadsheet,
  pagedViewInset
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * Page Up and Page Down, as the GWT client paged.
 *
 * Neither client handles the keys: the browser pages the view it scrolls.
 * The GWT client's was its pane of scrolling cells. The Lit client's scroll
 * container holds the headers and frozen panes too, pinned over its edges,
 * so the browser paged them in as well and moved the cells a header further:
 * 489px where the GWT client moved them 463px.
 */
const NO_FREEZE = { width: 0, columns: 0, height: 0, rows: 0 };

describe('pagedViewInset — what the browser leaves out of a page', () => {
  it('leaves out the headers, less the pixel the GWT pane ran under the tabs', () => {
    expect(pagedViewInset({ width: 50, height: 27 }, NO_FREEZE, true)).to.eql({ top: 26, left: 50 });
  });

  it('keeps that pixel with the tabs hidden, where the pane reached the bottom', () => {
    expect(pagedViewInset({ width: 50, height: 27 }, NO_FREEZE, false)).to.eql({ top: 27, left: 50 });
  });

  it('leaves out the frozen panes and the freeze line too', () => {
    expect(pagedViewInset({ width: 50, height: 27 }, { width: 120, columns: 2, height: 60, rows: 3 }, true)).to.eql({
      top: 27 + 60 + 1 - 1,
      left: 50 + 120 + 1
    });
  });

  it('leaves out nothing without headers or a freeze', () => {
    expect(pagedViewInset({ width: 0, height: 0 }, NO_FREEZE, true)).to.eql({ top: 0, left: 0 });
  });
});

describe('Spreadsheet — the view the browser pages', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let reference: HTMLElement | null = null;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setCols(60);
    sheet.setRows(400);
    sheet.setColW(new Array(60).fill(50));
    sheet.setRowH(new Array(400).fill(15));
    sheet.recomputeViewport();
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
    reference?.remove();
    reference = null;
  });

  function inset(): string {
    const style = getComputedStyle(sheet.getScrollContainer());
    return `${style.scrollPaddingTop} ${style.scrollPaddingLeft}`;
  }

  /** Waits until a smooth scroll has come to rest. */
  async function settled(el: HTMLElement): Promise<number> {
    let last = -1;
    for (let frames = 0; frames < 120; frames++) {
      await new Promise((resolve) => requestAnimationFrame(resolve));
      if (el.scrollTop === last && last > 0) {
        return last;
      }
      last = el.scrollTop;
    }
    return el.scrollTop;
  }

  it('leaves the headers out of it', () => {
    // The headers are 27px tall and 50px wide, and the pane starts a pixel
    // under the tabs.
    expect(inset()).to.equal('26px 50px');
  });

  it('gives the pixel back when the sheet hides its tabs', async () => {
    host.classList.add('hidetabsheet');
    // The host's classes are watched, not polled.
    await Promise.resolve();

    expect(inset()).to.equal('27px 50px');
  });

  it('leaves the frozen panes and the freeze line out of it too', () => {
    sheet.setVerticalSplitPosition(2);
    sheet.setHorizontalSplitPosition(1);
    sheet.recomputeViewport();

    // Rows of 15pt are 20px.
    expect(inset()).to.equal(`${27 + 2 * 20 + 1 - 1}px ${50 + 50 + 1}px`);
  });

  it('pages the cells as far as a pane as tall as the GWT client\'s would', async () => {
    const scroller = sheet.getScrollContainer();
    // The GWT client's pane: the scroll container's view less the headers,
    // and a pixel under the tabs.
    reference = document.createElement('div');
    reference.tabIndex = 0;
    reference.style.cssText = `height:${scroller.clientHeight - 26}px;width:200px;overflow:auto;`;
    reference.innerHTML = '<div style="height:6000px"></div>';
    document.body.appendChild(reference);

    reference.focus();
    await sendKeys({ press: 'PageDown' });
    const expected = await settled(reference);
    scroller.focus();
    await sendKeys({ press: 'PageDown' });

    expect(expected).to.be.greaterThan(0);
    expect(await settled(scroller)).to.equal(expected);
  });
});
