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

/** Right-click a cell the way the cell-grid reports it. */
function rightClickCell(sheet: Spreadsheet, col: number, row: number): void {
  sheet.getCellGrid().dispatchEvent(
    new CustomEvent('cell-contextmenu', {
      detail: { col, row, clientX: 120, clientY: 90 },
      bubbles: true,
      composed: true
    })
  );
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
    expect(el.style.left).to.equal('120px');
    expect(el.style.top).to.equal('90px');
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

    expect(await items(ctx.sheet)).to.have.length(0);
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

    expect(await items(ctx.sheet)).to.have.length(0);
    expect(ctx.events.map((e) => e.type)).to.deep.equal(['contextMenuClosed']);
  });

  it('closes on Escape', async () => {
    ctx.sheet.showActions([action('Insert row', 'insert-row', 0)]);
    document.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true })
    );

    expect(await items(ctx.sheet)).to.have.length(0);
  });

  it('does not report a close when nothing was open', () => {
    ctx.events.length = 0;
    document.dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, composed: true })
    );

    expect(ctx.events).to.have.length(0);
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
