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
import { rendered } from './spreadsheet-harness.js';

/**
 * The address and formula fields follow the selection at once, from the cells
 * the client has, as in GWT. Waiting for the server's answer had them trail
 * every click by a round trip: a third of a second on a slow network. No
 * server answers in these tests.
 */
describe('Spreadsheet — the formula bar follows the selection', () => {
  let host: HTMLDivElement;
  let sheet: Spreadsheet;

  function cell(col: number, row: number, value: string, extra: Partial<CellData> = {}): CellData {
    return { col, row, value, originalValue: value, cellStyle: 'cs0', locked: false, needsMeasure: false, isPercentage: false, ...extra } as CellData;
  }

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(20);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(20).fill(15));
    sheet.recomputeViewport();
    sheet.updateBottomRightCellValues([
      cell(2, 2, '7'),
      cell(3, 5, '42', { formulaValue: 'SUM(B1:B3)' }),
      cell(4, 7, 'hello'),
      cell(2, 3, '8')
    ]);
    sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  const NO_KEYS = { shiftKey: false, ctrlKey: false, metaKey: false, altKey: false };

  function clickCell(col: number, row: number, keys: Partial<typeof NO_KEYS> = {}): void {
    const detail = { col, row, ...NO_KEYS, ...keys };
    sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-mousedown', { detail, bubbles: true, composed: true }));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    sheet.getCellGrid().dispatchEvent(new CustomEvent('cell-click', { detail, bubbles: true, composed: true }));
  }

  function bar(): { address: string; formula: string } {
    const { address, formula } = sheet.getFormulaBar().renderState;
    return { address, formula };
  }

  it('names a clicked cell and shows its value', () => {
    clickCell(2, 2);

    expect(bar()).to.deep.equal({ address: 'B2', formula: '7' });
  });

  it('shows the formula of a clicked cell that has one', () => {
    clickCell(3, 5);

    expect(bar()).to.deep.equal({ address: 'C5', formula: '=SUM(B1:B3)' });
  });

  it('shows an empty formula field for a cell with nothing in it', () => {
    clickCell(4, 7);
    clickCell(6, 9);

    expect(bar()).to.deep.equal({ address: 'F9', formula: '' });
  });

  it('follows the arrow keys', () => {
    clickCell(2, 2);

    sheet.getScrollContainer().dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', bubbles: true, composed: true }));

    expect(bar()).to.deep.equal({ address: 'B3', formula: '8' });
  });

  it('names the cell a click on a column letter makes active', () => {
    sheet.getColumnHeader().dispatchEvent(
      new CustomEvent('column-header-click', { detail: { col: 4, ...NO_KEYS }, bubbles: true, composed: true })
    );

    expect(bar().address).to.equal('D1');
  });

  it('names a cell added with Ctrl-click', () => {
    clickCell(2, 2);

    clickCell(4, 7, { ctrlKey: true });

    expect(bar()).to.deep.equal({ address: 'D7', formula: 'hello' });
  });

  it('counts out a selection Shift is growing, also when the server has no cell of its own', () => {
    // Right after a reload the server holds no selected cell, and a range
    // grown from the active cell goes to it with that cell named.
    clickCell(2, 2);
    sheet.setReload(true);
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.setReload(true);

    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'ArrowDown', shiftKey: true, bubbles: true, composed: true })
    );

    expect(bar().address).to.equal('2R x 1C');
  });

  it('lets the server name a named range afterwards', async () => {
    clickCell(2, 2);

    sheet.updateFormulaBar('Totals', 2, 2);

    await rendered(sheet);
    expect(bar().address).to.equal('Totals');
  });
});
