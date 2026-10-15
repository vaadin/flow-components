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

/**
 * Cells and headers are not text to select, as in the GWT client: a drag
 * across them highlighted their text along with the cells. What is typed
 * into a field can still be selected.
 */
describe('Spreadsheet — cells and headers are not text', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;

  beforeEach(async () => {
    host = document.createElement('div');
    host.style.cssText = 'position:fixed;left:0;top:0;width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setCols(10);
    sheet.setRows(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    const cells = [];
    for (let row = 1; row <= 6; row++) {
      for (let col = 1; col <= 4; col++) {
        cells.push({
          col,
          row,
          value: `text ${col},${row}`,
          cellStyle: 'cs0',
          locked: false,
          needsMeasure: false,
          isPercentage: false
        });
      }
    }
    sheet.updateBottomRightCellValues(cells);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
  });

  afterEach(async () => {
    sheet.dispose();
    host.remove();
    document.getSelection()?.removeAllRanges();
    await resetMouse();
  });

  function centre(el: Element): [number, number] {
    const box = el.getBoundingClientRect();
    return [Math.round(box.left + box.width / 2), Math.round(box.top + box.height / 2)];
  }

  function cell(col: number, row: number): HTMLElement {
    return sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(`div.col${col}.row${row}.cell`)!;
  }

  it('selects no text on a drag from a row header across the cells', async () => {
    const rowHeader = sheet.getRowHeader().shadowRoot!.querySelector('.row-header-cell.row2')!;
    await sendMouse({ type: 'move', position: centre(rowHeader) });
    await sendMouse({ type: 'down' });
    await sendMouse({ type: 'move', position: centre(cell(3, 5)) });
    await sendMouse({ type: 'up' });

    expect(document.getSelection()!.toString()).to.equal('');
  });

  it('keeps the headers from being selected as well', async () => {
    const rowHeader = sheet.getRowHeader().shadowRoot!.querySelector('.row-header-cell')!;
    const columnHeader = sheet.getColumnHeader().shadowRoot!.querySelector('.col-header-cell')!;

    expect([getComputedStyle(rowHeader).userSelect, getComputedStyle(columnHeader).userSelect]).to.eql([
      'none',
      'none'
    ]);
  });

  it('still selects what is typed into the cell editor', async () => {
    cell(2, 2).dispatchEvent(new MouseEvent('dblclick', { bubbles: true, composed: true }));
    await sheet.getCellGrid().updateComplete;
    const input = sheet.getCellGrid().shadowRoot!.querySelector<HTMLInputElement>('.cell-editor')!;

    input.select();

    expect(input.selectionEnd! - input.selectionStart!).to.equal(input.value.length);
    expect(input.value.length).to.be.greaterThan(0);
  });
});
