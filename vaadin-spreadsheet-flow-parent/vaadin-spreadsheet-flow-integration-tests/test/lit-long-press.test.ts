/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { resetMouse, sendMouse } from '@web/test-runner-commands';
import { listenForLongPress, LONG_PRESS_MS } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/LongPress.js';
import { isIOS } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/Platform.js';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

function wait(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** One touch event, with one finger per point given. */
function touch(target: Element, type: string, points: Array<{ x: number; y: number }>): void {
  const touches = points.map(
    (p, i) => new Touch({ identifier: i + 1, target, clientX: p.x, clientY: p.y, screenX: p.x, screenY: p.y })
  );
  target.dispatchEvent(
    new TouchEvent(type, {
      touches: type === 'touchend' || type === 'touchcancel' ? [] : touches,
      targetTouches: type === 'touchend' || type === 'touchcancel' ? [] : touches,
      changedTouches: touches,
      bubbles: true,
      composed: true,
      cancelable: true
    })
  );
}

describe('isIOS — where Safari opens no menu on a long press', () => {
  it('knows an iPhone', () => {
    expect(
      isIOS({
        userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148',
        maxTouchPoints: 5
      })
    ).to.be.true;
  });

  it('knows an iPad that presents itself as a Mac, by its touch points', () => {
    const ua = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 Version/18.0 Safari/605.1.15';
    expect(isIOS({ userAgent: ua, maxTouchPoints: 5 })).to.be.true;
    expect(isIOS({ userAgent: ua, maxTouchPoints: 0 })).to.be.false;
  });

  it('leaves Android alone, whose browsers open the menu themselves', () => {
    expect(
      isIOS({
        userAgent: 'Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36',
        maxTouchPoints: 5
      })
    ).to.be.false;
  });
});

/**
 * A finger that stays put opens the menu at the point pressed, as the GWT
 * client's polyfill did on iOS. Anything else a finger does is not a long
 * press.
 */
describe('listenForLongPress', () => {
  const DELAY = 40;
  let host: HTMLElement;
  let inner: HTMLElement;
  let menus: MouseEvent[];
  let pressed: EventTarget[];
  let stop: () => void;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'position:fixed;left:0;top:0;width:200px;height:200px;';
    document.body.appendChild(host);
    // Pressed inside a shadow tree, as a cell of the grid is.
    const root = host.attachShadow({ mode: 'open' });
    inner = document.createElement('div');
    inner.style.cssText = 'width:200px;height:200px;';
    root.appendChild(inner);
    menus = [];
    pressed = [];
    host.addEventListener('contextmenu', (e) => {
      if (!e.isTrusted) {
        menus.push(e as MouseEvent);
        pressed.push(e.composedPath()[0]);
      }
      e.preventDefault();
    });
    stop = listenForLongPress(host, DELAY);
  });

  afterEach(async () => {
    stop();
    host.remove();
    await resetMouse();
  });

  it('opens a menu at the point pressed once the finger has stayed put', async () => {
    touch(inner, 'touchstart', [{ x: 30, y: 40 }]);

    await wait(DELAY + 30);

    expect(menus).to.have.length(1);
    expect(pressed[0]).to.equal(inner);
    expect([menus[0].clientX, menus[0].clientY, menus[0].button]).to.eql([30, 40, 2]);
  });

  it('opens nothing for a finger that lifts first', async () => {
    touch(inner, 'touchstart', [{ x: 30, y: 40 }]);
    touch(inner, 'touchend', [{ x: 30, y: 40 }]);

    await wait(DELAY + 30);

    expect(menus).to.be.empty;
  });

  it('opens nothing for a finger that moves away, as for a scroll', async () => {
    touch(inner, 'touchstart', [{ x: 30, y: 40 }]);
    touch(inner, 'touchmove', [{ x: 30, y: 60 }]);

    await wait(DELAY + 30);

    expect(menus).to.be.empty;
  });

  it('still opens the menu for a finger that only trembles', async () => {
    touch(inner, 'touchstart', [{ x: 30, y: 40 }]);
    touch(inner, 'touchmove', [{ x: 33, y: 44 }]);

    await wait(DELAY + 30);

    expect(menus).to.have.length(1);
  });

  it('opens nothing for two fingers', async () => {
    touch(inner, 'touchstart', [
      { x: 30, y: 40 },
      { x: 90, y: 40 }
    ]);

    await wait(DELAY + 30);

    expect(menus).to.be.empty;
  });

  it('opens nothing when a system menu came for the press, so there is one menu', async () => {
    touch(inner, 'touchstart', [{ x: 30, y: 40 }]);
    await sendMouse({ type: 'click', position: [30, 40], button: 'right' });

    await wait(DELAY + 30);

    expect(menus).to.be.empty;
  });

  it('listens no more once stopped', async () => {
    touch(inner, 'touchstart', [{ x: 30, y: 40 }]);
    stop();

    await wait(DELAY + 30);

    expect(menus).to.be.empty;
  });
});

/**
 * On iOS a long press on a cell opens the sheet's menu for it, as a
 * right-click does elsewhere. Elsewhere the browser does that itself.
 */
describe('Spreadsheet — a long press on iOS', () => {
  const realPlatform = Spreadsheet.platform;
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let sent: { type: string; data: Record<string, unknown> }[];

  function attach(ios: boolean): void {
    Spreadsheet.platform = { isIOS: () => ios };
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setCols(10);
    sheet.setRows(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.setHasActions(true);
    sheet.recomputeViewport();
    sent = [];
    host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));
  }

  afterEach(() => {
    Spreadsheet.platform = realPlatform;
    sheet.dispose();
    host.remove();
  });

  async function pressCell(col: number, row: number): Promise<void> {
    await sheet.getCellGrid().updateComplete;
    const cell = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(`div.col${col}.row${row}.cell`)!;
    const box = cell.getBoundingClientRect();
    touch(cell, 'touchstart', [{ x: box.left + 5, y: box.top + 5 }]);
    await wait(LONG_PRESS_MS + 100);
  }

  function menuRequests(): Record<string, unknown>[] {
    return sent.filter((e) => e.type === 'contextMenuOpenOnSelection').map((e) => e.data);
  }

  it('asks for the menu of the cell pressed', async () => {
    attach(true);

    await pressCell(3, 2);

    expect(menuRequests()).to.have.length(1);
    expect(sheet.getSelection().activeCell).to.eql({ col: 3, row: 2 });
  });

  it('leaves a long press to the browser elsewhere', async () => {
    attach(false);

    await pressCell(3, 2);

    expect(menuRequests()).to.be.empty;
  });

  it('keeps a long press from selecting the text of the cells', async () => {
    attach(true);
    await sheet.getCellGrid().updateComplete;
    const cell = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('div.col1.row1.cell')!;

    expect(getComputedStyle(cell).userSelect).to.equal('none');
  });
});
