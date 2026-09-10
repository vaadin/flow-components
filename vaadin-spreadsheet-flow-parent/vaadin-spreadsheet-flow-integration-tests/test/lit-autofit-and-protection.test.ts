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
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * Two server-bound reports the connector never sent.
 *
 * Autofit: double-clicking a header's resize handle sizes the row or column
 * to its content. Only the server can measure that — it holds every value in
 * the line, including the ones outside the viewport.
 *
 * Protection: the client refuses an edit to a locked cell on its own, but the
 * server has to be told so it can explain why. Swallowing the keystroke in
 * silence leaves the user with no idea what happened.
 */

type Dispatched = { type: string; data: Record<string, unknown> };

function cell(col: number, row: number, locked: boolean): CellData {
  return {
    col,
    row,
    value: 'x',
    cellStyle: 'cs0',
    locked,
    needsMeasure: false,
    isPercentage: false
  } as CellData;
}

function attachSheet(): {
  sheet: Spreadsheet;
  sent: Dispatched[];
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
  sheet.setHeaderRowHeight(20);
  sheet.setHeaderColumnWidth(40);
  sheet.recomputeViewport();

  const sent: Dispatched[] = [];
  host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));

  return {
    sheet,
    sent,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

describe('Spreadsheet — autofit from a header double-click', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(async () => {
    ctx = attachSheet();
    await ctx.sheet.getColumnHeader().updateComplete;
    await ctx.sheet.getRowHeader().updateComplete;
    ctx.sent.length = 0;
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function handle(header: HTMLElement, selector: string): HTMLElement {
    const el = header.shadowRoot!.querySelector<HTMLElement>(selector);
    expect(el, `no ${selector}`).to.not.be.null;
    return el!;
  }

  it('asks the server to autofit the column', () => {
    handle(ctx.sheet.getColumnHeader(), '.col-resize-handle[data-col="3"]').dispatchEvent(
      new MouseEvent('dblclick', { bubbles: true, composed: true })
    );

    expect(ctx.sent).to.eql([{ type: 'onColumnAutofit', data: { columnIndex: 3 } }]);
  });

  it('asks the server to autofit the row', () => {
    handle(ctx.sheet.getRowHeader(), '.row-resize-handle[data-row="4"]').dispatchEvent(
      new MouseEvent('dblclick', { bubbles: true, composed: true })
    );

    expect(ctx.sent).to.eql([{ type: 'onRowAutofit', data: { rowIndex: 4 } }]);
  });

  it('ignores a double-click on the header cell itself', () => {
    handle(ctx.sheet.getColumnHeader(), '.col-header-cell').dispatchEvent(
      new MouseEvent('dblclick', { bubbles: true, composed: true })
    );

    expect(ctx.sent.filter((e) => e.type === 'onColumnAutofit')).to.be.empty;
  });
});

describe('Spreadsheet — a write to a protected cell', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(async () => {
    ctx = attachSheet();
    ctx.sheet.setSheetProtected(true);
    ctx.sheet.updateBottomRightCellValues([cell(2, 2, true), cell(3, 3, false)]);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;
    ctx.sent.length = 0;
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function typeInto(sheet: Spreadsheet, col: number, row: number, key: string): void {
    sheet.setSelectedCellAndRange('', col, row, col, col, row, row, false);
    sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, composed: true }));
  }

  function protectedReports(sent: Dispatched[]): Dispatched[] {
    return sent.filter((e) => e.type === 'protectedCellWriteAttempted');
  }

  it('reports a printable character typed into a locked cell', () => {
    typeInto(ctx.sheet, 2, 2, 'a');

    expect(protectedReports(ctx.sent)).to.have.length(1);
  });

  it('reports F2 on a locked cell', () => {
    typeInto(ctx.sheet, 2, 2, 'F2');

    expect(protectedReports(ctx.sent)).to.have.length(1);
  });

  it('reports a double-click on a locked cell', () => {
    ctx.sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-doubleclick', {
        detail: { col: 2, row: 2 },
        bubbles: true,
        composed: true
      })
    );

    expect(protectedReports(ctx.sent)).to.have.length(1);
  });

  it('opens no editor on the locked cell', async () => {
    typeInto(ctx.sheet, 2, 2, 'a');
    await ctx.sheet.getCellGrid().updateComplete;

    expect(ctx.sheet.getCellGrid().shadowRoot!.querySelector('[part~="cell-input"]')).to.be.null;
  });

  it('says nothing when the cell is editable', () => {
    typeInto(ctx.sheet, 3, 3, 'a');

    expect(protectedReports(ctx.sent)).to.be.empty;
  });

  it('still opens the editor on an editable cell', async () => {
    typeInto(ctx.sheet, 3, 3, 'a');
    await ctx.sheet.getCellGrid().updateComplete;

    expect(ctx.sheet.getCellGrid().shadowRoot!.querySelector('[part~="cell-input"]')).to.not.be
      .null;
  });
});
