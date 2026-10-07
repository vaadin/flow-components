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
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import type { SpreadsheetActionDetails } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * Slice 191 — the right-click context menu.
 *
 * `showActions` was a no-op, so right-clicking dispatched the open event to
 * the server, the server answered with the actions it offers, and nothing
 * appeared. 13 IT failures look for a context menu that never opened.
 *
 * The server owns the menu's contents; the client reports where the click
 * landed and what the user picked.
 */

function action(
  caption: string,
  key: string,
  type: number
): SpreadsheetActionDetails {
  return { caption, key, type, iconNodeId: 0 };
}

function attachSheet(): {
  sheet: Spreadsheet;
  host: HTMLElement;
  events: any[];
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));

  const events: any[] = [];
  host.addEventListener('spreadsheet-event', (e) =>
    events.push((e as CustomEvent).detail)
  );

  return {
    sheet,
    host,
    events,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

async function items(sheet: Spreadsheet): Promise<HTMLElement[]> {
  const menu = sheet.getContextMenu();
  await menu.updateComplete;
  return Array.from(
    menu.shadowRoot!.querySelectorAll<HTMLElement>('.item')
  );
}

/**
 * The items left once the menu has finished closing: it fades out first, as
 * the GWT client's did.
 */
async function itemsOnceClosed(sheet: Spreadsheet): Promise<HTMLElement[]> {
  const menu = sheet.getContextMenu();
  await menu.updateComplete;
  menu.shadowRoot!.querySelector<HTMLElement>('.menu')?.getAnimations().forEach((a) => a.finish());
  await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
  return items(sheet);
}

/** Right-click a cell the way the cell-grid reports it. */
function rightClickCell(sheet: Spreadsheet, col: number, row: number, clientX = 120, clientY = 90): void {
  sheet.getCellGrid().dispatchEvent(
    new CustomEvent('cell-contextmenu', {
      detail: { col, row, clientX, clientY },
      bubbles: true,
      composed: true
    })
  );
}

function menuPanel(sheet: Spreadsheet): HTMLElement {
  return sheet.getContextMenu().shadowRoot!.querySelector<HTMLElement>('.menu')!;
}

/** The element of the menu that has the keyboard, if any. */
function focusedInMenu(sheet: Spreadsheet): Element | null {
  return sheet.getContextMenu().shadowRoot!.activeElement;
}

function press(sheet: Spreadsheet, key: string): KeyboardEvent {
  const target = (focusedInMenu(sheet) as HTMLElement | null) ?? menuPanel(sheet);
  const event = new KeyboardEvent('keydown', { key, bubbles: true, composed: true, cancelable: true });
  target.dispatchEvent(event);
  return event;
}

describe('Spreadsheet context menu — opening', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('shows nothing until the server offers actions', async () => {
    rightClickCell(ctx.sheet, 2, 2);

    expect(await items(ctx.sheet)).to.have.length(0);
  });

  it('shows one item per action the server offers', async () => {
    ctx.sheet.showActions([
      action('Insert row', 'insert-row', 0),
      action('Delete row', 'delete-row', 0)
    ]);

    const rendered = await items(ctx.sheet);
    expect(rendered.map((i) => i.textContent!.trim())).to.deep.equal([
      'Insert row',
      'Delete row'
    ]);
  });

  it('opens where the right-click landed', async () => {
    rightClickCell(ctx.sheet, 2, 2);
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);

    const menu = ctx.sheet.getContextMenu();
    await menu.updateComplete;
    const el = menu.shadowRoot!.querySelector<HTMLElement>('.menu')!;
    // Where it comes to rest, once it has dropped into place.
    el.getAnimations().forEach((animation) => animation.finish());
    // Where it is drawn, not what is written on it: the style is in the
    // sheet's coordinate space, and only the drawn position is the
    // contract a user's click depends on.
    const rect = el.getBoundingClientRect();
    expect(Math.round(rect.x)).to.equal(120);
    expect(Math.round(rect.y)).to.equal(90);
  });

  it('accepts the JSON-serialized payload the server actually sends', async () => {
    // Flow serializes the action list, so it arrives as a string rather
    // than an array — the same lane the collection-shaped property
    // pushes come through.
    ctx.sheet.showActions(
      JSON.stringify([action('Insert row', 'insert-row', 0)]) as any
    );

    const rendered = await items(ctx.sheet);
    expect(rendered.map((i) => i.textContent!.trim())).to.deep.equal([
      'Insert row'
    ]);
  });

  it('shows nothing when the server offers no actions', async () => {
    ctx.sheet.showActions([]);

    expect(await items(ctx.sheet)).to.have.length(0);
  });
});

describe('Spreadsheet context menu — where it lands', () => {
  let ctx: ReturnType<typeof attachSheet>;

  afterEach(() => {
    ctx.cleanup();
  });

  /** The menu where it comes to rest, once it has dropped into place. */
  async function panel(): Promise<HTMLElement> {
    const menu = ctx.sheet.getContextMenu();
    await menu.updateComplete;
    const el = menu.shadowRoot!.querySelector<HTMLElement>('.menu')!;
    el.getAnimations().forEach((animation) => animation.finish());
    return el;
  }

  it('lands on the point that was clicked', async () => {
    ctx = attachSheet();
    rightClickCell(ctx.sheet, 2, 2);
    ctx.sheet.showActions([action('Test', '1', 0)]);

    const rect = (await panel()).getBoundingClientRect();
    expect(Math.round(rect.x)).to.equal(120);
    expect(Math.round(rect.y)).to.equal(90);
  });

  it('lands on the point that was clicked when the sheet has a border', async () => {
    // The menu is placed inside the sheet's root, whose border the
    // stylesheet draws: measured from the outside of it, the menu landed a
    // border's width right of and below the click.
    ctx = attachSheet();
    ctx.sheet.getRoot().style.border = '5px solid black';
    rightClickCell(ctx.sheet, 2, 2);
    ctx.sheet.showActions([action('Test', '1', 0)]);

    const rect = (await panel()).getBoundingClientRect();
    expect(Math.round(rect.x)).to.equal(120);
    expect(Math.round(rect.y)).to.equal(90);
  });

  it('opens to the left of the point when it would cross the right edge of the window, as the GWT client did', async () => {
    ctx = attachSheet();
    const x = document.documentElement.clientWidth - 10;
    rightClickCell(ctx.sheet, 2, 2, x, 90);
    ctx.sheet.showActions([action('A rather long action caption', '1', 0)]);

    const rect = (await panel()).getBoundingClientRect();
    expect(Math.round(rect.right)).to.equal(x);
    expect(Math.round(rect.y)).to.equal(90);
  });

  it('moves up to fit when it would cross the bottom of the window, as the GWT client did', async () => {
    ctx = attachSheet();
    const y = document.documentElement.clientHeight - 5;
    rightClickCell(ctx.sheet, 2, 2, 120, y);
    ctx.sheet.showActions([action('One', '1', 0), action('Two', '2', 0)]);

    const rect = (await panel()).getBoundingClientRect();
    expect(Math.round(rect.bottom)).to.equal(document.documentElement.clientHeight);
    expect(Math.round(rect.x)).to.equal(120);
  });

  it('lands there inside a transformed ancestor too', async () => {
    // A dialog overlay is transformed, which makes it the containing
    // block for a fixed box. Placing the menu at raw viewport
    // coordinates then puts it somewhere else entirely, and a click
    // aimed at one item lands on its neighbour.
    ctx = attachSheet();
    ctx.host.style.transform = 'translate(30px, 40px)';

    rightClickCell(ctx.sheet, 2, 2);
    ctx.sheet.showActions([action('Test', '1', 0)]);

    const rect = (await panel()).getBoundingClientRect();
    expect(Math.round(rect.x)).to.equal(120);
    expect(Math.round(rect.y)).to.equal(90);
  });
});

describe('Spreadsheet context menu — picking an item', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('reports a cell action against the current selection', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    ctx.events.length = 0;
    (await items(ctx.sheet))[0].click();

    expect(ctx.events).to.have.length(1);
    expect(ctx.events[0].type).to.equal('actionOnCurrentSelection');
    expect(ctx.events[0].data).to.deep.equal({ actionKey: 'insert-row' });
  });

  it('reports a row action against the row header', async () => {
    ctx.sheet.showActions([action('Hide row', 'hide-row', 1)]);
    ctx.events.length = 0;
    (await items(ctx.sheet))[0].click();

    expect(ctx.events[0].type).to.equal('actionOnRowHeader');
    expect(ctx.events[0].data).to.deep.equal({ actionKey: 'hide-row' });
  });

  it('reports a column action against the column header', async () => {
    ctx.sheet.showActions([action('Hide column', 'hide-col', 2)]);
    ctx.events.length = 0;
    (await items(ctx.sheet))[0].click();

    expect(ctx.events[0].type).to.equal('actionOnColumnHeader');
    expect(ctx.events[0].data).to.deep.equal({ actionKey: 'hide-col' });
  });

  it('closes once an item is picked', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    (await items(ctx.sheet))[0].click();

    expect(await itemsOnceClosed(ctx.sheet)).to.have.length(0);
  });
});

describe("Spreadsheet context menu — the keyboard, as the GWT client's menu had it", () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(async () => {
    ctx = attachSheet();
    ctx.sheet.getScrollContainer().focus();
    rightClickCell(ctx.sheet, 2, 2);
    ctx.sheet.showActions([action('One', '1', 0), action('Two', '2', 0), action('Three', '3', 0)]);
    await items(ctx.sheet);
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function inHand(): string | null {
    const el = focusedInMenu(ctx.sheet);
    return el instanceof HTMLElement && el.classList.contains('item') ? el.textContent!.trim() : null;
  }

  it('takes the keyboard when it opens, with no item in hand yet', () => {
    expect(focusedInMenu(ctx.sheet)).to.equal(menuPanel(ctx.sheet));
    expect(inHand()).to.be.null;
  });

  it('takes the first item on Down, then goes down the list and round', () => {
    press(ctx.sheet, 'ArrowDown');
    expect(inHand()).to.equal('One');
    press(ctx.sheet, 'ArrowDown');
    press(ctx.sheet, 'ArrowDown');
    expect(inHand()).to.equal('Three');
    press(ctx.sheet, 'ArrowDown');
    expect(inHand()).to.equal('One');
  });

  it('takes the first item on Up, then goes up the list and round', () => {
    press(ctx.sheet, 'ArrowUp');
    expect(inHand()).to.equal('One');
    press(ctx.sheet, 'ArrowUp');
    expect(inHand()).to.equal('Three');
  });

  it('takes the first item on Enter when none is in hand, and picks nothing', () => {
    ctx.events.length = 0;

    press(ctx.sheet, 'Enter');

    expect(inHand()).to.equal('One');
    expect(ctx.events.filter((e) => e.type === 'actionOnCurrentSelection')).to.be.empty;
  });

  it('picks the item in hand on Enter', () => {
    press(ctx.sheet, 'ArrowDown');
    press(ctx.sheet, 'ArrowDown');
    ctx.events.length = 0;

    press(ctx.sheet, 'Enter');

    expect(ctx.events.filter((e) => e.type === 'actionOnCurrentSelection').map((e) => e.data)).to.deep.equal([
      { actionKey: '2' }
    ]);
  });

  it('closes on Left once an item is in hand', async () => {
    press(ctx.sheet, 'ArrowLeft');
    expect(inHand(), 'the first Left takes the first item').to.equal('One');

    press(ctx.sheet, 'ArrowLeft');

    expect(await itemsOnceClosed(ctx.sheet)).to.have.length(0);
  });

  it('closes on Tab', async () => {
    press(ctx.sheet, 'Tab');

    expect(await itemsOnceClosed(ctx.sheet)).to.have.length(0);
  });

  it('keeps the arrow keys away from the sheet while it is open', () => {
    const before = ctx.sheet.getSelection().activeCell;

    press(ctx.sheet, 'ArrowDown');
    press(ctx.sheet, 'ArrowRight');

    expect(ctx.sheet.getSelection().activeCell).to.deep.equal(before);
  });

  it('hands the keyboard back to the sheet when it closes', async () => {
    press(ctx.sheet, 'Escape');
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await items(ctx.sheet);

    expect(ctx.sheet.getRoot().getRootNode() === document ? document.activeElement : (ctx.sheet.getRoot().getRootNode() as Document).activeElement).to.equal(
      ctx.sheet.getScrollContainer()
    );
  });

  it('takes the item under the pointer in hand, as the GWT client did', async () => {
    const [, two] = await items(ctx.sheet);

    two.dispatchEvent(new PointerEvent('pointerover', { bubbles: true, composed: true }));

    expect(inHand()).to.equal('Two');
  });
});

describe('Spreadsheet context menu — dismissing', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('closes on a click elsewhere and tells the server', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    ctx.events.length = 0;
    document.dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, composed: true })
    );
    // Taken now: the new sheet's own start-up events land while the menu
    // fades.
    const reported = ctx.events.map((e) => e.type);

    expect(await itemsOnceClosed(ctx.sheet)).to.have.length(0);
    expect(reported).to.deep.equal(['contextMenuClosed']);
  });

  it('closes on Escape', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    document.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true })
    );

    expect(await itemsOnceClosed(ctx.sheet)).to.have.length(0);
  });

  it('does not report a close when nothing was open', () => {
    ctx.events.length = 0;
    document.dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, composed: true })
    );

    expect(ctx.events).to.have.length(0);
  });

  /** The menu, open and dropped into place. */
  async function settledMenu(): Promise<HTMLElement> {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0), action('Delete row', 'delete-row', 0)]);
    await items(ctx.sheet);
    const panel = menuPanel(ctx.sheet);
    panel.getAnimations().forEach((a) => a.finish());
    await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    return panel;
  }

  function closeByEscape(): void {
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
  }

  it('fades out as it closes, as the GWT client\'s menu did, taking no clicks', async () => {
    const panel = await settledMenu();

    closeByEscape();
    await ctx.sheet.getContextMenu().updateComplete;

    expect(menuPanel(ctx.sheet)).to.equal(panel);
    expect((panel.getAnimations() as CSSAnimation[]).map((a) => a.animationName)).to.deep.equal([
      'overlay-animate-out'
    ]);
    expect(getComputedStyle(panel).pointerEvents).to.equal('none');
    expect((await items(ctx.sheet)).map((item) => item.textContent!.trim())).to.deep.equal([
      'Insert row',
      'Delete row'
    ]);
    expect(await itemsOnceClosed(ctx.sheet)).to.have.length(0);
  });

  it('is closed for the sheet while it fades', async () => {
    await settledMenu();
    ctx.events.length = 0;

    closeByEscape();
    await ctx.sheet.getContextMenu().updateComplete;
    document.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, composed: true }));

    expect(ctx.events.map((e) => e.type)).to.deep.equal(['contextMenuClosed']);
  });

  it('stays open when opened again while it fades', async () => {
    await settledMenu();
    closeByEscape();
    await ctx.sheet.getContextMenu().updateComplete;

    ctx.sheet.showActions([action('Hide column', 'hide-col', 2)]);

    const after = await itemsOnceClosed(ctx.sheet);
    expect(after.map((item) => item.textContent!.trim())).to.deep.equal(['Hide column']);
    expect(menuPanel(ctx.sheet).classList.contains('animate-out')).to.be.false;
  });

  it('stops listening once disposed', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    ctx.cleanup();
    ctx.events.length = 0;
    document.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true })
    );

    expect(ctx.events).to.have.length(0);
    // cleanup() already ran; make afterEach a no-op.
    ctx.cleanup = () => {};
  });
});

/**
 * Action icons.
 *
 * `SpreadsheetAction.setIcon` attaches the icon as a virtual child and the
 * server sends its Flow node id on the action. The connector ignored it, so
 * every menu item rendered as bare text however the application decorated
 * it.
 */
describe('Spreadsheet — context-menu action icons', () => {
  const originalVaadin = (window as any).Vaadin;

  function attachWithShadow(): { host: HTMLElement; sheet: Spreadsheet; cleanup: () => void } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    // The icon is projected through two shadow roots — the host's and the
    // menu's — so the host needs a real one.
    const sheet = new Spreadsheet(host, host.attachShadow({ mode: 'open' }));
    sheet.setCols(10);
    sheet.setRows(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    return {
      host,
      sheet,
      cleanup: () => {
        sheet.dispose();
        host.remove();
      }
    };
  }

  function withIcons(nodes: Record<number, HTMLElement>): void {
    (window as any).Vaadin = {
      Flow: { clients: { '': { getByNodeId: (n: number) => nodes[n] ?? null } } }
    };
  }

  function icon(name: string): HTMLElement {
    const el = document.createElement('vaadin-icon');
    el.setAttribute('icon', name);
    return el;
  }

  let ctx: ReturnType<typeof attachWithShadow>;

  beforeEach(() => {
    ctx = attachWithShadow();
  });

  afterEach(() => {
    ctx.cleanup();
    (window as any).Vaadin = originalVaadin;
  });

  it('shows the icon component the action carries', async () => {
    const academy = icon('vaadin:academy-cap');
    withIcons({ 7: academy });

    ctx.sheet.showActions([
      { caption: 'Vaadin icon', type: 0, key: '1', iconNodeId: 7 }
    ] as any);
    await ctx.sheet.getContextMenu().updateComplete;
    await ctx.sheet.getContextMenu().updateComplete;

    const slot = ctx.sheet
      .getContextMenu()
      .shadowRoot!.querySelector<HTMLSlotElement>('slot.item-icon');
    expect(slot, 'no icon slot rendered').to.not.be.null;
    expect(slot!.assignedElements({ flatten: true })).to.eql([academy]);
  });

  it('renders no icon slot for an action without one', async () => {
    withIcons({});

    ctx.sheet.showActions([
      { caption: 'Plain', type: 0, key: '1', iconNodeId: 0 }
    ] as any);
    await ctx.sheet.getContextMenu().updateComplete;

    expect(ctx.sheet.getContextMenu().shadowRoot!.querySelector('slot.item-icon')).to.be.null;
  });

  it('places one icon per action that has one', async () => {
    withIcons({ 7: icon('vaadin:academy-cap'), 8: icon('lumo:angle-up') });

    ctx.sheet.showActions([
      { caption: 'Vaadin icon', type: 0, key: '1', iconNodeId: 7 },
      { caption: 'Plain', type: 0, key: '2', iconNodeId: 0 },
      { caption: 'Lumo icon', type: 0, key: '3', iconNodeId: 8 }
    ] as any);
    await ctx.sheet.getContextMenu().updateComplete;
    await ctx.sheet.getContextMenu().updateComplete;

    const slots = ctx.sheet.getContextMenu().shadowRoot!.querySelectorAll('slot.item-icon');
    expect(slots).to.have.length(2);
    expect(ctx.host.querySelectorAll('vaadin-icon')).to.have.length(2);
  });

  it('keeps the caption alongside the icon', async () => {
    withIcons({ 7: icon('vaadin:academy-cap') });

    ctx.sheet.showActions([
      { caption: 'Vaadin icon', type: 0, key: '1', iconNodeId: 7 }
    ] as any);
    await ctx.sheet.getContextMenu().updateComplete;

    const item = ctx.sheet.getContextMenu().shadowRoot!.querySelector('.item')!;
    expect(item.textContent!.trim()).to.equal('Vaadin icon');
  });

  it('releases the icons when the menu closes', async () => {
    const academy = icon('vaadin:academy-cap');
    withIcons({ 7: academy });

    ctx.sheet.showActions([
      { caption: 'Vaadin icon', type: 0, key: '1', iconNodeId: 7 }
    ] as any);
    await ctx.sheet.getContextMenu().updateComplete;
    await ctx.sheet.getContextMenu().updateComplete;
    expect(academy.parentElement).to.equal(ctx.host);

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await ctx.sheet.getContextMenu().updateComplete;

    expect(academy.parentElement).to.be.null;
  });

  it('accepts the action list as the JSON string Flow sends', async () => {
    withIcons({ 7: icon('vaadin:academy-cap') });

    ctx.sheet.showActions(
      JSON.stringify([{ caption: 'Vaadin icon', type: 0, key: '1', iconNodeId: 7 }]) as any
    );
    await ctx.sheet.getContextMenu().updateComplete;
    await ctx.sheet.getContextMenu().updateComplete;

    expect(
      ctx.sheet.getContextMenu().shadowRoot!.querySelector<HTMLSlotElement>('slot.item-icon')!
        .assignedElements({ flatten: true })
    ).to.have.length(1);
  });
});

/**
 * The menu, as the GWT client drew it: a white card with a soft shadow,
 * items in 27px rows, the one under the pointer on blue with light type.
 * The Lit menu had a plain grey border and a grey highlight.
 */
describe('Spreadsheet context menu — how it looks', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('is a white card with rounded corners and a soft shadow', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    await items(ctx.sheet);

    const menu = getComputedStyle(ctx.sheet.getContextMenu().shadowRoot!.querySelector('.menu')!);
    expect(`${menu.borderTopLeftRadius} ${menu.paddingTop} ${menu.color}`).to.equal('4px 4px rgb(71, 71, 71)');
    expect(menu.boxShadow).to.contain('rgba(0, 0, 0, 0.1) 0px 4px 10px 0px');
  });

  it('edges the card with a hairline of shadow rather than a border, as the GWT client did', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    await items(ctx.sheet);

    const menu = getComputedStyle(menuPanel(ctx.sheet));
    expect(menu.borderTopWidth).to.equal('0px');
    expect(menu.boxShadow).to.contain('rgba(0, 0, 0, 0.09) 0px 0px 0px 1px');
  });

  it("drops into place as it opens, as the GWT client's menu did", async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    await items(ctx.sheet);

    const panel = menuPanel(ctx.sheet);
    expect(panel.classList.contains('animate-in')).to.be.true;
    expect((panel.getAnimations() as CSSAnimation[]).map((a) => a.animationName)).to.deep.equal([
      'overlay-animate-in'
    ]);
  });

  it('does not drop in again when the actions come again while it is open', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    await items(ctx.sheet);
    // Run it out and let the class come off, so that a second drop would
    // show as a new animation.
    menuPanel(ctx.sheet).getAnimations().forEach((animation) => animation.finish());
    await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    expect(menuPanel(ctx.sheet).classList.contains('animate-in'), 'the first drop is over').to.be.false;

    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    await items(ctx.sheet);

    expect(menuPanel(ctx.sheet).getAnimations()).to.be.empty;
  });

  it('darkens the item being pressed, as the GWT client did', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    const [item] = await items(ctx.sheet);
    menuPanel(ctx.sheet).getAnimations().forEach((animation) => animation.finish());
    const box = item.getBoundingClientRect();

    await sendMouse({ type: 'move', position: [Math.round(box.left + 10), Math.round(box.top + box.height / 2)] });
    await sendMouse({ type: 'down' });
    try {
      const tint = getComputedStyle(item, '::before');
      expect(`${tint.backgroundColor} ${tint.opacity}`).to.equal('rgb(9, 87, 166) 0.15');
      expect(`${tint.width} ${tint.height}`).to.equal(`${box.width}px ${box.height}px`);
    } finally {
      await sendMouse({ type: 'up' });
      await resetMouse();
    }
  });

  it('sets its items in 27px rows', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    const [item] = await items(ctx.sheet);

    const style = getComputedStyle(item);
    expect(style.lineHeight).to.equal('27px');
    expect(`${style.paddingLeft} ${style.paddingRight}`).to.equal('10px 20px');
  });

  it('puts the item in hand on blue, in light type', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    const [item] = await items(ctx.sheet);

    item.focus();

    const style = getComputedStyle(item);
    expect(style.backgroundImage).to.contain('linear-gradient');
    expect(style.color).to.equal('rgb(236, 242, 248)');
  });
});
