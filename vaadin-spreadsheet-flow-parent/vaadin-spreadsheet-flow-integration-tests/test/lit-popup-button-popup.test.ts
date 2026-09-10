/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * Opening a popup button's popup.
 *
 * `PopupButton.openPopup()` calls `onPopupButtonOpen` on the sheet — and
 * that is the only path, because the server runs `openPopup()` itself when
 * it handles the button's click. The connector declared the method as
 * `onPopupButtonOpened`, which the server never calls, so no popup ever
 * opened from either direction.
 *
 * The content is a Flow virtual child: it exists on the client but is not
 * in the document until the connector places it, and it is resolved against
 * the application id the button reports, which need not be the sheet's.
 */

const APP = 'ROOT';

let hostSeq = 0;

function attachSheet(): {
  host: HTMLElement;
  sheet: Spreadsheet;
  sent: { type: string; data: Record<string, unknown> }[];
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.id = `popup-host-${hostSeq++}`;
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  // A real shadow root: the popup's content is a light child of the host
  // projected through a slot, and a slot only works inside a shadow tree.
  const sheet = new Spreadsheet(host, host.attachShadow({ mode: 'open' }));
  sheet.setCols(20);
  sheet.setRows(200);
  sheet.setColW(new Array(20).fill(50));
  // 15pt rows are 20px at 96ppi.
  sheet.setRowH(new Array(200).fill(15));
  sheet.recomputeViewport();

  const sent: { type: string; data: Record<string, unknown> }[] = [];
  host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));

  return {
    host,
    sheet,
    sent,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

/** Stand in for the Flow client that owns the popup's content element. */
function withFlowClient(nodes: Record<number, HTMLElement>, appId: string = APP): void {
  (window as any).Vaadin = {
    Flow: { clients: { [appId]: { getByNodeId: (n: number) => nodes[n] ?? null } } }
  };
}

function content(text: string): HTMLElement {
  const el = document.createElement('div');
  el.textContent = text;
  return el;
}

describe('Spreadsheet — popup-button popup', () => {
  let ctx: ReturnType<typeof attachSheet>;
  const originalVaadin = (window as any).Vaadin;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
    (window as any).Vaadin = originalVaadin;
  });

  it('keeps the overlay hidden until a popup is opened', () => {
    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
  });

  it('shows the content the server named', () => {
    const popup = content('filter');
    withFlowClient({ 42: popup });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const overlay = ctx.sheet.getPopupOverlay();
    expect(overlay.hidden).to.be.false;
    const slot = overlay.querySelector('slot')!;
    expect(slot.assignedElements({ flatten: true })).to.eql([popup]);
  });

  it('leaves the content in the host light DOM, where the server can reach it', () => {
    const popup = content('filter');
    withFlowClient({ 42: popup });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    expect(popup.parentElement).to.equal(ctx.host);
  });

  it('resolves the content against the application the button reports', () => {
    const popup = content('filter');
    withFlowClient({ 42: popup }, 'OTHER-APP');

    ctx.sheet.onPopupButtonOpen(1, 4, '42', 'OTHER-APP');

    expect(ctx.sheet.getPopupOverlay().querySelector('slot')!.assignedElements({ flatten: true })).to.eql([
      popup
    ]);
  });

  it('seats the overlay under its anchor cell', () => {
    withFlowClient({ 42: content('filter') });

    // Column 4 starts after three 50px columns; the overlay hangs below
    // row 1, which is 20px tall.
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const overlay = ctx.sheet.getPopupOverlay();
    expect(overlay.style.left).to.equal('150px');
    expect(overlay.style.top).to.equal('20px');
  });

  it('sizes the overlay from the button state when the server gave one', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.setPopupButtons([
      {
        col: 4,
        row: 1,
        active: true,
        headerHidden: false,
        popupWidth: '220px',
        popupHeight: '180px',
        sheet: 'Sheet1'
      }
    ]);

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const overlay = ctx.sheet.getPopupOverlay();
    expect(overlay.style.width).to.equal('220px');
    expect(overlay.style.height).to.equal('180px');
  });

  it('closes on the server request for that cell', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    ctx.sheet.closePopup(1, 4);

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
  });

  it('ignores a close for a cell whose popup is not the open one', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    ctx.sheet.closePopup(2, 9);

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.false;
  });

  it('tells the server nothing when the server closed it', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    ctx.sheet.closePopup(1, 4);

    expect(ctx.sent.filter((e) => e.type === 'popupClose')).to.be.empty;
  });

  it('closes and reports when the user clicks away', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    document.body.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, composed: true }));

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
    expect(ctx.sent.filter((e) => e.type === 'popupClose')).to.eql([
      { type: 'popupClose', data: { row: 1, column: 4 } }
    ]);
  });

  it('stays open when the click lands inside it', () => {
    const popup = content('filter');
    withFlowClient({ 42: popup });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    popup.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, composed: true }));

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.false;
  });

  it('closes and reports on Escape', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
    expect(ctx.sent.some((e) => e.type === 'popupClose')).to.be.true;
  });

  it('closes when the sheet scrolls out from under it', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    ctx.sheet.getScrollContainer().scrollTop = 400;
    ctx.sheet.getScrollContainer().dispatchEvent(new Event('scroll'));

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
  });

  it('releases the content when it closes, so reopening another button is clean', () => {
    const first = content('one');
    const second = content('two');
    withFlowClient({ 42: first, 43: second });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sheet.closePopup(1, 4);
    ctx.sheet.onPopupButtonOpen(3, 2, '43', APP);

    const assigned = ctx.sheet
      .getPopupOverlay()
      .querySelector('slot')!
      .assignedElements({ flatten: true });
    expect(assigned).to.eql([second]);
    expect(first.parentElement).to.be.null;
  });

  it('shows a close box in the popup header', () => {
    withFlowClient({ 42: content('filter') });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const close = ctx.sheet.getPopupOverlay().querySelector<HTMLElement>('.popup-close');
    expect(close, 'no close box').to.not.be.null;
    expect(close!.getAttribute('part')).to.equal('popup-close');
  });

  it('closes and reports from the close box', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    ctx.sheet.getPopupOverlay().querySelector<HTMLElement>('.popup-close')!.click();

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
    expect(ctx.sent.filter((e) => e.type === 'popupClose')).to.eql([
      { type: 'popupClose', data: { row: 1, column: 4 } }
    ]);
  });

  it('omits the header when the button asked for it hidden', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.setPopupButtons([
      {
        col: 4,
        row: 1,
        active: true,
        headerHidden: true,
        popupWidth: null,
        popupHeight: null,
        sheet: 'Sheet1'
      }
    ]);

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    expect(ctx.sheet.getPopupOverlay().querySelector('.popup-header')).to.be.null;
  });

  it('does nothing for a content id Flow cannot resolve', () => {
    withFlowClient({});

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const overlay = ctx.sheet.getPopupOverlay();
    expect(overlay.hidden).to.be.false;
    expect(overlay.querySelector('slot')!.assignedElements({ flatten: true })).to.be.empty;
  });
});
