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
import type { PopupButtonState } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * What assistive technology is told about the sheet and its fields. Each one
 * has a name, so that a screen reader does not announce an unnamed edit field
 * or button.
 */
describe('Spreadsheet — the names assistive technology is told', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;

  beforeEach(async () => {
    host = document.createElement('div');
    host.style.cssText = 'width:600px;height:400px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(10);
    sheet.setCols(5);
    sheet.setColW(new Array(5).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.setSheetNames(['Data', 'Totals']);
    sheet.setSheetIndex(1);
    sheet.notifyStateChanges(['sheetNames', 'sheetIndex'], false);
    await sheet.getCellGrid().updateComplete;
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  function inFormulaBar(selector: string): HTMLElement | null {
    return sheet.getFormulaBar().shadowRoot!.querySelector<HTMLElement>(selector);
  }

  function inGrid(selector: string): HTMLElement | null {
    return sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(selector);
  }

  it('names the name box and the formula bar', async () => {
    await sheet.getFormulaBar().updateComplete;

    expect(inFormulaBar('[part~="address-field"]')!.getAttribute('aria-label')).to.equal('Name box');
    expect(inFormulaBar('[part~="formula-field"]')!.getAttribute('aria-label')).to.equal('Formula bar');
  });

  it('names the picker of named ranges', async () => {
    sheet.setNamedRanges(['Budget', 'Costs']);
    sheet.recomputeViewport();
    await sheet.getFormulaBar().updateComplete;

    expect(inFormulaBar('[part~="named-range-box"]')!.getAttribute('aria-label')).to.equal('Named ranges');
  });

  it('names the cell editor after the cell it edits', async () => {
    sheet.setSelectedCellAndRange('', 2, 3, 2, 2, 3, 3, false);
    sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { key: 'F2', bubbles: true, composed: true }));
    await sheet.getCellGrid().updateComplete;

    expect(inGrid('[part~="cell-input"]')!.getAttribute('aria-label')).to.equal('Edit cell B3');
  });

  it('names a pop-up button', async () => {
    const button: PopupButtonState = {
      col: 2,
      row: 2,
      active: false,
      headerHidden: false,
      popupWidth: null,
      popupHeight: null,
      sheet: 'Data'
    };
    sheet.addPopupButton(button);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(inGrid('[part~="popup-button"]')!.getAttribute('aria-label')).to.equal('Open pop-up');
  });

  it('is one control that takes its keys for itself, a spreadsheet', () => {
    const scroll = sheet.getScrollContainer();

    expect(scroll.getAttribute('role')).to.equal('application');
    expect(scroll.getAttribute('aria-roledescription')).to.equal('spreadsheet');
  });

  it('is named after the sheet shown', () => {
    expect(sheet.getScrollContainer().getAttribute('aria-label')).to.equal('Data');

    sheet.setSheetIndex(2);
    sheet.notifyStateChanges(['sheetIndex'], false);

    expect(sheet.getScrollContainer().getAttribute('aria-label')).to.equal('Totals');
  });
});
