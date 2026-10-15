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
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import { popupPlacement } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/Placement.js';
import type {
  CellData,
  PopupButtonState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

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

function buttonState(
  col: number,
  row: number,
  size: { popupWidth?: string; popupHeight?: string } = {}
): PopupButtonState {
  return {
    col,
    row,
    active: false,
    headerHidden: false,
    popupWidth: size.popupWidth ?? null,
    popupHeight: size.popupHeight ?? null,
    sheet: 'Sheet1'
  };
}

function cellData(col: number, row: number, value: string): CellData {
  return { col, row, value, cellStyle: 'cs0', locked: false, needsMeasure: false, isPercentage: false };
}

/**
 * Where a cell is on screen: the sheet's columns are 50px and its rows
 * 20px, counted from the cell grid's corner.
 */
function cellBox(sheet: Spreadsheet, col: number, row: number): { left: number; right: number; top: number; bottom: number } {
  const grid = sheet.getCellGrid().getBoundingClientRect();
  return {
    left: grid.left + (col - 1) * 50,
    right: grid.left + col * 50,
    top: grid.top + (row - 1) * 20,
    bottom: grid.top + row * 20
  };
}

/** The top of the scrolling pane: under the column letters. */
function paneTop(sheet: Spreadsheet): number {
  return sheet.getColumnHeader().getBoundingClientRect().bottom;
}

/** The last row whose bottom edge is inside the scroll container. */
function lastWholeRow(sheet: Spreadsheet): number {
  const bottom = sheet.getScrollContainer().getBoundingClientRect().bottom;
  return Math.floor((bottom - sheet.getCellGrid().getBoundingClientRect().top) / 20);
}

function scrollSheet(sheet: Spreadsheet, top: number): void {
  const container = sheet.getScrollContainer();
  container.scrollTop = top;
  container.dispatchEvent(new Event('scroll'));
}

/** Where the popup comes to rest, once it has dropped into place. */
function restingRect(sheet: Spreadsheet): DOMRect {
  const overlay = sheet.getPopupOverlay();
  overlay.getAnimations().forEach((animation) => animation.finish());
  return overlay.getBoundingClientRect();
}

/** The names of the animations running on an element. */
function animationNames(el: Element): string[] {
  return (el.getAnimations() as CSSAnimation[]).map((animation) => animation.animationName);
}

/** Run an element's animations to their end, and let their end events land. */
async function settle(el: HTMLElement): Promise<void> {
  el.getAnimations().forEach((animation) => animation.finish());
  await frames(2);
}

function frames(count: number): Promise<void> {
  return new Promise((resolve) => {
    const step = (left: number): void => {
      if (left === 0) {
        resolve();
      } else {
        requestAnimationFrame(() => step(left - 1));
      }
    };
    step(count);
  });
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

  it("hangs below its cell with its right edge on the cell's right edge, as the GWT client did", () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 1, { popupWidth: '120px' }));

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const overlay = restingRect(ctx.sheet);
    const cell = cellBox(ctx.sheet, 4, 1);
    expect(overlay.right).to.be.closeTo(cell.right, 0.5);
    expect(overlay.top).to.be.closeTo(cell.bottom, 0.5);
  });

  it("starts at the cell's right edge when it would reach past the sheet's left side", () => {
    withFlowClient({ 42: content('filter') });
    // Column 1 is 50px wide, so a 120px popup ending at its right edge
    // would start under the row numbers.
    ctx.sheet.addPopupButton(buttonState(1, 1, { popupWidth: '120px' }));

    ctx.sheet.onPopupButtonOpen(1, 1, '42', APP);

    expect(restingRect(ctx.sheet).left).to.be.closeTo(cellBox(ctx.sheet, 1, 1).right, 0.5);
  });

  it("measures the sheet's left side from the frozen columns, not the row numbers", () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    // Column 3 ends 150px in, and a pixel more past the freeze line's own;
    // 120px back from there is inside the two frozen columns, and the GWT
    // client's scrolling pane starts after them.
    ctx.sheet.addPopupButton(buttonState(3, 1, { popupWidth: '120px' }));

    ctx.sheet.onPopupButtonOpen(1, 3, '42', APP);

    const drawn = ctx.sheet.getCellGrid().cellClientRect(3, 1)!;
    expect(drawn.right).to.equal(cellBox(ctx.sheet, 3, 1).right + 1);
    expect(restingRect(ctx.sheet).left).to.be.closeTo(drawn.right, 0.5);
  });

  it('opens above its cell when there is no room below it', () => {
    withFlowClient({ 42: content('filter') });
    const row = lastWholeRow(ctx.sheet);
    ctx.sheet.addPopupButton(buttonState(4, row, { popupWidth: '120px', popupHeight: '100px' }));

    ctx.sheet.onPopupButtonOpen(row, 4, '42', APP);

    expect(restingRect(ctx.sheet).bottom).to.be.closeTo(
      cellBox(ctx.sheet, 4, row).top,
      0.5
    );
  });

  it('never rises above the top of the scrolling pane', () => {
    withFlowClient({ 42: content('filter') });
    // Taller than the room on either side of row 2.
    ctx.sheet.addPopupButton(buttonState(4, 2, { popupWidth: '120px', popupHeight: '400px' }));

    ctx.sheet.onPopupButtonOpen(2, 4, '42', APP);

    expect(restingRect(ctx.sheet).top).to.be.closeTo(paneTop(ctx.sheet), 0.5);
  });

  it("is drawn in the top layer, as the GWT client's popover was, so the sheet's edges cannot cut it off", async () => {
    withFlowClient({ 42: content('filter') });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    expect(ctx.sheet.getPopupOverlay().matches(':popover-open'), 'open').to.be.true;

    ctx.sheet.closePopup(1, 4);
    await settle(ctx.sheet.getPopupOverlay());
    expect(ctx.sheet.getPopupOverlay().matches(':popover-open'), 'closed').to.be.false;
  });

  it('places itself again when its content grows after it opened', async () => {
    const popup = content('filter');
    withFlowClient({ 42: popup });
    const row = lastWholeRow(ctx.sheet) - 4;
    ctx.sheet.addPopupButton(buttonState(4, row, { popupWidth: '120px' }));
    ctx.sheet.onPopupButtonOpen(row, 4, '42', APP);
    expect(restingRect(ctx.sheet).top, 'below at first').to.be.closeTo(
      cellBox(ctx.sheet, 4, row).bottom,
      0.5
    );

    // Flow components render after they are attached, so the popup's
    // real size arrives a moment after it opened. Grown just past the room
    // below the cell, it has to go above it.
    const overlay = ctx.sheet.getPopupOverlay();
    const cell = cellBox(ctx.sheet, 4, row);
    const chrome = overlay.getBoundingClientRect().height - popup.getBoundingClientRect().height;
    const grown = ctx.sheet.getScrollContainer().getBoundingClientRect().bottom - cell.bottom + 5;
    expect(grown, 'room above').to.be.at.most(cell.top - paneTop(ctx.sheet));
    popup.style.height = `${grown - chrome}px`;
    await frames(2);

    expect(restingRect(ctx.sheet).bottom).to.be.closeTo(
      cellBox(ctx.sheet, 4, row).top,
      0.5
    );
  });

  it('sizes the overlay from the button state when the server gave one', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton({
      col: 4,
      row: 1,
      active: true,
      headerHidden: false,
      popupWidth: '220px',
      popupHeight: '180px',
      sheet: 'Sheet1'
    });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const overlay = ctx.sheet.getPopupOverlay();
    expect(overlay.style.width).to.equal('220px');
    expect(overlay.style.height).to.equal('180px');
  });

  it("shows content taller than the height the server set past it, as the GWT client's overlay did", () => {
    const popup = content('filter');
    popup.style.height = '300px';
    withFlowClient({ 42: popup });
    ctx.sheet.addPopupButton(buttonState(4, 1, { popupWidth: '120px', popupHeight: '100px' }));

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    // A popover scrolls its overflow unless told otherwise; the GWT
    // client's overlay set it visible.
    const overlay = ctx.sheet.getPopupOverlay();
    expect(getComputedStyle(overlay).overflowY).to.equal('visible');
    expect(popup.getBoundingClientRect().bottom).to.be.greaterThan(overlay.getBoundingClientRect().bottom);
  });

  it("drops into place as it opens, as the GWT client's popup did", () => {
    withFlowClient({ 42: content('filter') });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const overlay = ctx.sheet.getPopupOverlay();
    expect(overlay.classList.contains('animate-in')).to.be.true;
    expect(animationNames(overlay)).to.deep.equal(['overlay-animate-in']);
  });

  it('does not drop in again when the server reopens the popup that is open', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    await settle(ctx.sheet.getPopupOverlay());

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    expect(animationNames(ctx.sheet.getPopupOverlay())).to.be.empty;
  });

  it("drops in again for another cell's popup", async () => {
    withFlowClient({ 42: content('one'), 43: content('two') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    await settle(ctx.sheet.getPopupOverlay());

    ctx.sheet.onPopupButtonOpen(3, 2, '43', APP);

    expect(animationNames(ctx.sheet.getPopupOverlay())).to.deep.equal(['overlay-animate-in']);
  });

  it('closes on the server request for that cell', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    ctx.sheet.closePopup(1, 4);
    await settle(ctx.sheet.getPopupOverlay());

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

  it('closes and reports when the user clicks away', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    document.body.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, composed: true }));
    await settle(ctx.sheet.getPopupOverlay());

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
    expect(ctx.sent.filter((e) => e.type === 'popupClose')).to.eql([
      { type: 'popupClose', data: { row: 1, column: 4 } }
    ]);
  });

  /** Press and click a popup button, as a mouse does. */
  async function clickButton(col: number, row: number): Promise<void> {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;
    const button = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>(`.popup-button[data-col="${col}"][data-row="${row}"]`)!;
    button.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, composed: true }));
    button.dispatchEvent(new MouseEvent('click', { bubbles: true, composed: true }));
  }

  it('closes and reports when its own button is clicked, as the GWT client did', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 1));
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    await clickButton(4, 1);
    await settle(ctx.sheet.getPopupOverlay());

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
    expect(ctx.sent.map((e) => e.type)).to.eql(['popupClose']);
  });

  it('asks the server to open it again on the next click of its button', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 1));
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    await clickButton(4, 1);
    ctx.sent.length = 0;

    await clickButton(4, 1);

    expect(ctx.sent).to.eql([{ type: 'popupButtonClick', data: { row: 1, column: 4 } }]);
  });

  it("opens another cell's popup from its button while one is open", async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 1));
    ctx.sheet.addPopupButton(buttonState(6, 1));
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    await clickButton(6, 1);

    expect(ctx.sent.map((e) => e.type)).to.eql(['popupClose', 'popupButtonClick']);
  });

  it('keeps the keys typed inside it away from the sheet, as the GWT client did', () => {
    const field = document.createElement('input');
    withFlowClient({ 42: field });
    ctx.sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    // An arrow key in a filter's search field or on one of its check
    // boxes is the popup's, not a move of the sheet's selection.
    field.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', bubbles: true, composed: true }));

    expect(ctx.sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
  });

  it('stays open when the click lands inside it', () => {
    const popup = content('filter');
    withFlowClient({ 42: popup });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    popup.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, composed: true }));

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.false;
  });

  it('closes and reports on Escape', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await settle(ctx.sheet.getPopupOverlay());

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
    expect(ctx.sent.some((e) => e.type === 'popupClose')).to.be.true;
  });

  it('follows its cell as the sheet scrolls and stays open, as the GWT client did', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 6, { popupWidth: '120px', popupHeight: '60px' }));
    ctx.sheet.onPopupButtonOpen(6, 4, '42', APP);
    ctx.sent.length = 0;

    scrollSheet(ctx.sheet, 40);

    const overlay = ctx.sheet.getPopupOverlay();
    expect(overlay.hidden).to.be.false;
    expect(restingRect(ctx.sheet).top).to.be.closeTo(cellBox(ctx.sheet, 4, 6).bottom, 0.5);
    expect(ctx.sent.filter((e) => e.type === 'popupClose')).to.be.empty;
  });

  it('closes and reports when the server takes its button away, as the GWT client did', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 1));
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    // What the server does once the button's cell has scrolled out of
    // view, or the sheet has changed. The GWT client's popup closed with
    // its button and said so.
    ctx.sheet.removePopupButton(buttonState(4, 1));
    await settle(ctx.sheet.getPopupOverlay());

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
    expect(ctx.sent.filter((e) => e.type === 'popupClose')).to.eql([
      { type: 'popupClose', data: { row: 1, column: 4 } }
    ]);
  });

  it("keeps the open popup when the server takes another cell's button away", () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 1));
    ctx.sheet.addPopupButton(buttonState(5, 1));
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    ctx.sheet.removePopupButton(buttonState(5, 1));

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.false;
  });

  it('waits at the top of the pane while its cell scrolls up out of sight', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 3, { popupWidth: '120px', popupHeight: '60px' }));
    ctx.sheet.onPopupButtonOpen(3, 4, '42', APP);

    // Row 3 ends 60px down, so 100px of scrolling takes it above the pane.
    scrollSheet(ctx.sheet, 100);

    expect(restingRect(ctx.sheet).top).to.be.closeTo(paneTop(ctx.sheet), 0.5);
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

  it('draws the popup as the GWT client did: a white card with a soft shadow', () => {
    withFlowClient({ 42: content('filter') });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const style = getComputedStyle(ctx.sheet.getPopupOverlay());
    expect(`${style.borderTopLeftRadius} ${style.paddingTop} ${style.color}`).to.equal('4px 4px rgb(71, 71, 71)');
    expect(style.boxShadow).to.contain('rgba(0, 0, 0, 0.1) 0px 4px 10px 0px');
  });

  it('edges the card with a hairline of shadow rather than a border, which took no room', () => {
    withFlowClient({ 42: content('filter') });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const style = getComputedStyle(ctx.sheet.getPopupOverlay());
    expect(style.borderTopWidth).to.equal('0px');
    expect(style.boxShadow).to.contain('rgba(0, 0, 0, 0.09) 0px 0px 0px 1px');
  });

  it("captions the header with the text of the button's cell, as the GWT client did", () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.cellsUpdated([cellData(4, 1, 'Column 1')]);

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const caption = ctx.sheet.getPopupOverlay().querySelector<HTMLElement>('.popup-header .header-caption');
    expect(caption, 'no caption').to.not.be.null;
    expect(caption!.textContent).to.equal('Column 1');
    expect(caption!.getAttribute('part')).to.equal('popup-caption');
  });

  it('centres the caption between 18px margins on an 18px line', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.cellsUpdated([cellData(4, 1, 'Column 1')]);

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const style = getComputedStyle(ctx.sheet.getPopupOverlay().querySelector('.header-caption')!);
    expect(`${style.marginLeft} ${style.marginRight} ${style.height} ${style.lineHeight} ${style.textAlign}`).to.equal(
      '18px 18px 18px 18px center'
    );
  });

  it('leaves the caption empty for a cell with no value', () => {
    withFlowClient({ 42: content('filter') });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    expect(ctx.sheet.getPopupOverlay().querySelector('.header-caption')!.textContent).to.equal('');
  });

  it('puts the close box at the left of an 18px header, in grey', () => {
    withFlowClient({ 42: content('filter') });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    const overlay = ctx.sheet.getPopupOverlay();
    const header = overlay.querySelector<HTMLElement>('.popup-header')!;
    const close = overlay.querySelector<HTMLElement>('.popup-close')!;
    expect(header.getBoundingClientRect().height).to.equal(18 + 7);
    expect(close.getBoundingClientRect().left).to.equal(header.getBoundingClientRect().left);
    expect(getComputedStyle(close).color).to.equal('rgb(153, 153, 153)');
    expect(getComputedStyle(close).fontSize).to.equal('21px');
  });

  it('closes and reports from the close box', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    ctx.sent.length = 0;

    ctx.sheet.getPopupOverlay().querySelector<HTMLElement>('.popup-close')!.click();
    await settle(ctx.sheet.getPopupOverlay());

    expect(ctx.sheet.getPopupOverlay().hidden).to.be.true;
    expect(ctx.sent.filter((e) => e.type === 'popupClose')).to.eql([
      { type: 'popupClose', data: { row: 1, column: 4 } }
    ]);
  });

  it('omits the header when the button asked for it hidden', () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton({
      col: 4,
      row: 1,
      active: true,
      headerHidden: true,
      popupWidth: null,
      popupHeight: null,
      sheet: 'Sheet1'
    });

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

  // Closed once it has dropped into place, the GWT client's popup faded out
  // over 120ms and then went.
  it('fades out as it closes, taking no clicks, and then goes', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    const overlay = ctx.sheet.getPopupOverlay();
    await settle(overlay);

    ctx.sheet.closePopup(1, 4);

    expect(animationNames(overlay)).to.deep.equal(['overlay-animate-out']);
    expect(overlay.hidden, 'still there while it fades').to.be.false;
    expect(overlay.matches(':popover-open')).to.be.true;
    expect(getComputedStyle(overlay).pointerEvents).to.equal('none');
    await settle(overlay);
    expect(overlay.hidden).to.be.true;
    expect(overlay.matches(':popover-open')).to.be.false;
  });

  it('finishes dropping into place before it goes, without a fade, when closed as it opens', async () => {
    withFlowClient({ 42: content('filter') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    const overlay = ctx.sheet.getPopupOverlay();

    ctx.sheet.closePopup(1, 4);

    expect(animationNames(overlay)).to.deep.equal(['overlay-animate-in']);
    expect(overlay.hidden).to.be.false;
    await settle(overlay);
    expect(overlay.hidden).to.be.true;
  });

  it('fades out with what it holds when the user closes it', async () => {
    const filter = content('filter');
    withFlowClient({ 42: filter });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    const overlay = ctx.sheet.getPopupOverlay();
    await settle(overlay);

    overlay.querySelector<HTMLElement>('.popup-close')!.click();

    expect(filter.assignedSlot, 'shown while the card fades').to.not.be.null;
    await settle(overlay);
    expect(filter.isConnected, 'released once it has gone').to.be.false;
  });

  it('lets what it holds go first when the server closes it, and fades the empty card', async () => {
    const filter = content('filter');
    withFlowClient({ 42: filter });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    const overlay = ctx.sheet.getPopupOverlay();
    await settle(overlay);

    ctx.sheet.closePopup(1, 4);

    expect(filter.isConnected).to.be.false;
    expect(animationNames(overlay)).to.deep.equal(['overlay-animate-out']);
  });

  it('lets what it holds go first when the server takes its button away', async () => {
    const filter = content('filter');
    withFlowClient({ 42: filter });
    ctx.sheet.addPopupButton(buttonState(4, 1));
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    await settle(ctx.sheet.getPopupOverlay());

    ctx.sheet.removePopupButton(buttonState(4, 1));

    expect(filter.isConnected).to.be.false;
  });

  it('stays open when opened again while it fades', async () => {
    const filter = content('filter');
    withFlowClient({ 42: filter, 43: content('other') });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);
    const overlay = ctx.sheet.getPopupOverlay();
    await settle(overlay);
    overlay.querySelector<HTMLElement>('.popup-close')!.click();
    await frames(1);

    ctx.sheet.onPopupButtonOpen(1, 5, '43', APP);
    await settle(overlay);

    expect(overlay.hidden).to.be.false;
    expect(overlay.matches(':popover-open')).to.be.true;
    expect(overlay.classList.contains('animate-out')).to.be.false;
    expect(overlay.querySelector('slot')!.assignedElements({ flatten: true }).map((el) => el.textContent)).to.deep.equal([
      'other'
    ]);
  });
});

describe("popupPlacement — the GWT client's rule", () => {
  const pane = { left: 100, top: 50, bottom: 400 };

  it('hangs under the cell with the right edges together', () => {
    expect(popupPlacement({ top: 100, right: 300, bottom: 120 }, pane, 150, 80)).to.deep.equal({ left: 150, top: 120 });
  });

  it("starts at the cell's right edge when ending there would cross the pane's left side", () => {
    expect(popupPlacement({ top: 100, right: 200, bottom: 120 }, pane, 150, 80)).to.deep.equal({ left: 200, top: 120 });
  });

  it("keeps the right edges together when it starts exactly on the pane's left side", () => {
    expect(popupPlacement({ top: 100, right: 250, bottom: 120 }, pane, 150, 80)).to.deep.equal({ left: 100, top: 120 });
  });

  it("goes above the cell when it would run past the pane's bottom", () => {
    expect(popupPlacement({ top: 350, right: 300, bottom: 370 }, pane, 150, 80)).to.deep.equal({ left: 150, top: 270 });
  });

  it("stays below when it ends exactly on the pane's bottom", () => {
    expect(popupPlacement({ top: 100, right: 300, bottom: 120 }, pane, 150, 280)).to.deep.equal({ left: 150, top: 120 });
  });

  it("hangs from the pane's top when it fits neither below nor above", () => {
    expect(popupPlacement({ top: 150, right: 300, bottom: 170 }, pane, 150, 300)).to.deep.equal({ left: 150, top: 50 });
  });
});

/**
 * The popup button is a button to assistive technology and the keyboard, as
 * the GWT client's was: it takes the focus, and Enter or Space press it. A
 * popup opened from the keyboard takes the keyboard, and gives it back to
 * its button when it closes.
 */
describe('Spreadsheet — the popup button and the keyboard', () => {
  let ctx: ReturnType<typeof attachSheet>;
  const originalVaadin = (window as any).Vaadin;

  beforeEach(async () => {
    ctx = attachSheet();
    withFlowClient({ 42: content('filter') });
    ctx.sheet.addPopupButton(buttonState(4, 1));
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;
    ctx.sent.length = 0;
  });

  afterEach(() => {
    ctx.cleanup();
    (window as any).Vaadin = originalVaadin;
  });

  function button(): HTMLElement {
    return ctx.sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('.popup-button[data-col="4"][data-row="1"]')!;
  }

  function focused(): Element | null {
    let el = document.activeElement;
    while (el?.shadowRoot?.activeElement) {
      el = el.shadowRoot.activeElement;
    }
    return el;
  }

  function clicks(): number {
    return ctx.sent.filter((e) => e.type === 'popupButtonClick').length;
  }

  it('is found on its cell, and nowhere else', () => {
    expect(ctx.sheet.getCellGrid().popupButton(4, 1) === button()).to.be.true;
    expect(ctx.sheet.getCellGrid().popupButton(5, 1)).to.equal(null);
  });

  it('is a button that takes the focus', () => {
    expect(button().getAttribute('role')).to.equal('button');
    expect(button().tabIndex).to.equal(0);
  });

  it('is pressed with Enter', async () => {
    button().focus();

    await sendKeys({ press: 'Enter' });

    expect(ctx.sent).to.deep.include({ type: 'popupButtonClick', data: { row: 1, column: 4 } });
  });

  it('is pressed with Space', async () => {
    button().focus();

    await sendKeys({ press: 'Space' });

    expect(clicks()).to.equal(1);
  });

  it('keeps the keys from the sheet, which would edit the cell with them', async () => {
    button().focus();

    await sendKeys({ press: 'Enter' });
    await sendKeys({ press: 'Space' });

    expect(ctx.sheet.getCellGrid().renderState.editor ?? null).to.equal(null);
  });

  it('takes the keyboard into a popup it opened', async () => {
    button().focus();
    await sendKeys({ press: 'Enter' });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    expect(focused()).to.equal(ctx.sheet.getPopupOverlay().querySelector('.popup-close'));
  });

  it('takes the keyboard to the card when the popup has no header', async () => {
    ctx.sheet.addPopupButton({ ...buttonState(4, 1), headerHidden: true });
    button().focus();
    await sendKeys({ press: 'Enter' });

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    expect(focused()).to.equal(ctx.sheet.getPopupOverlay());
  });

  it('leaves the focus where it is for a popup opened with the pointer', () => {
    ctx.sheet.getScrollContainer().focus();
    button().dispatchEvent(new MouseEvent('click', { bubbles: true, composed: true }));

    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    expect(focused()).to.equal(ctx.sheet.getScrollContainer());
  });

  it('gives the keyboard back to its button when the popup closes', async () => {
    button().focus();
    await sendKeys({ press: 'Enter' });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    await sendKeys({ press: 'Escape' });

    expect(ctx.sent).to.deep.include({ type: 'popupClose', data: { row: 1, column: 4 } });
    expect(focused()).to.equal(button());
  });

  it('gives the keyboard to the sheet when its button went with the popup', async () => {
    button().focus();
    await sendKeys({ press: 'Enter' });
    ctx.sheet.onPopupButtonOpen(1, 4, '42', APP);

    ctx.sheet.removePopupButton(buttonState(4, 1));
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    expect(focused()).to.equal(ctx.sheet.getScrollContainer());
  });
});
