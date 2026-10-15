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

/**
 * Enter and Space on an outline button press it, as on any button. The sheet
 * used to take both keys for its active cell: Enter opened the cell's editor
 * and Space typed into it, and the button was never pressed.
 */
describe('Spreadsheet — outline buttons and the keyboard', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let sent: { type: string; data: Record<string, unknown> }[];

  beforeEach(async () => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setCols(10);
    sheet.setRows(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.setRowGroupingMax(1);
    sheet.setRowGroupingData([{ uniqueIndex: 1, level: 1, startIndex: 1, endIndex: 3, collapsed: false }]);
    sheet.recomputeViewport();
    await sheet.getRowHeader().updateComplete;
    sent = [];
    host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  function toggle(): HTMLButtonElement {
    return sheet.getRowHeader().shadowRoot!.querySelector<HTMLButtonElement>('.row-grouping-toggle')!;
  }

  function levelButton(level: number): HTMLButtonElement {
    return sheet
      .getHeaderCorner()
      .shadowRoot!.querySelector<HTMLButtonElement>(`.level-header[data-axis="rows"][data-level="${level}"]`)!;
  }

  function editorOpen(): boolean {
    return sheet.getCellGrid().renderState.editor != null;
  }

  function sentOf(type: string): Record<string, unknown>[] {
    return sent.filter((e) => e.type === type).map((e) => e.data);
  }

  it('collapses a group with Enter, and leaves the cell alone', async () => {
    toggle().focus();

    await sendKeys({ press: 'Enter' });

    expect(sentOf('groupingCollapsed')).to.deep.equal([{ cols: false, colIndex: 1, collapsed: true }]);
    expect(editorOpen()).to.be.false;
  });

  it('collapses a group with Space, and types nothing into the cell', async () => {
    toggle().focus();

    await sendKeys({ press: 'Space' });

    expect(sentOf('groupingCollapsed')).to.have.length(1);
    expect(editorOpen()).to.be.false;
  });

  it('shows a level with Enter, and leaves the cell alone', async () => {
    await sheet.getHeaderCorner().updateComplete;
    levelButton(1).focus();

    await sendKeys({ press: 'Enter' });

    expect(sentOf('levelHeaderClicked')).to.deep.equal([{ cols: false, level: 1 }]);
    expect(editorOpen()).to.be.false;
  });

  it('says whether its group is open', async () => {
    expect(toggle().getAttribute('aria-expanded')).to.equal('true');

    sheet.setRowGroupingData([{ uniqueIndex: 1, level: 1, startIndex: 1, endIndex: 3, collapsed: true }]);
    sheet.recomputeViewport();
    await sheet.getRowHeader().updateComplete;

    expect(toggle().getAttribute('aria-expanded')).to.equal('false');
  });

  it('says whether a column group is open as well', async () => {
    sheet.setColGroupingMax(1);
    sheet.setColGroupingData([{ uniqueIndex: 1, level: 1, startIndex: 1, endIndex: 3, collapsed: true }]);
    sheet.recomputeViewport();
    await sheet.getColumnHeader().updateComplete;

    const columnToggle = sheet.getColumnHeader().shadowRoot!.querySelector('.col-grouping-toggle')!;
    expect(columnToggle.getAttribute('aria-expanded')).to.equal('false');
  });

  it('still opens the editor with Enter on the sheet', async () => {
    sheet.getScrollContainer().focus();

    await sendKeys({ press: 'Enter' });

    expect(editorOpen()).to.be.true;
  });
});
