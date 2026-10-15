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
 * Copy puts the selected range on the clipboard as tab-separated text, cut
 * does that and asks the server to clear it, and paste hands the server
 * what is on the clipboard. While a cell is being edited all three are the
 * editor's.
 */
describe('Spreadsheet — the clipboard', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let sent: { type: string; data: Record<string, unknown> }[];

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:300px;height:200px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(5);
    sheet.setCols(5);
    sheet.setColW([50, 50, 50, 50, 50]);
    sheet.setRowH([15, 15, 15, 15, 15]);
    sheet.updateBottomRightCellValues([
      { col: 1, row: 1, value: 'A1', cellStyle: 'cs0' },
      { col: 2, row: 1, value: null, cellStyle: 'cs0' },
      { col: 1, row: 2, value: 'A2', cellStyle: 'cs0' },
      { col: 4, row: 4, value: 'D4', cellStyle: 'cs0' }
    ]);
    sent = [];
    host.addEventListener('spreadsheet-event', (e: Event) => sent.push((e as CustomEvent).detail));
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  function click(col: number, row: number, keys: { shiftKey?: boolean; ctrlKey?: boolean } = {}): void {
    sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-click', {
        detail: { col, row, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false, ...keys },
        bubbles: true,
        composed: true
      })
    );
  }

  /** Fires a clipboard event at the sheet; returns it and its clipboard. */
  function clipboard(type: 'copy' | 'cut' | 'paste', text?: string): { event: ClipboardEvent; data: DataTransfer } {
    const data = new DataTransfer();
    if (text !== undefined) {
      data.setData('text/plain', text);
    }
    const event = new ClipboardEvent(type, { clipboardData: data, bubbles: true, cancelable: true });
    sheet.getScrollContainer().dispatchEvent(event);
    return { event, data };
  }

  /** What the clipboard told the server. */
  function clipboardEvents(): { type: string; data: Record<string, unknown> }[] {
    return sent.filter((e) => e.type === 'clearSelectedCellsOnCut' || e.type === 'onPaste');
  }

  function edit(col: number, row: number): void {
    sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-doubleclick', { detail: { col, row }, bubbles: true, composed: true })
    );
  }

  it('copies the selected range, cells without a value as empty, and takes the copy over', () => {
    click(1, 1);
    click(2, 2, { shiftKey: true });

    const { event, data } = clipboard('copy');

    expect(data.getData('text/plain')).to.equal('A1\t\nA2\t');
    expect(event.defaultPrevented).to.be.true;
    expect(clipboardEvents()).to.be.empty;
  });

  it('copies the active range alone of a selection with ranges added to it', () => {
    click(1, 1);
    click(4, 4, { ctrlKey: true });

    const { data } = clipboard('copy');

    expect(data.getData('text/plain')).to.equal('D4');
  });

  it('cuts: copies and asks the server to clear the range', () => {
    click(1, 2);

    const { event, data } = clipboard('cut');

    expect(data.getData('text/plain')).to.equal('A2');
    expect(event.defaultPrevented).to.be.true;
    expect(clipboardEvents().map((e) => e.type)).to.eql(['clearSelectedCellsOnCut']);
  });

  it('pastes: hands the server the text', () => {
    const { event } = clipboard('paste', 'x\ty');

    expect(clipboardEvents()).to.eql([{ type: 'onPaste', data: { text: 'x\ty' } }]);
    expect(event.defaultPrevented).to.be.true;
  });

  it('pastes nothing when the clipboard holds no text, and leaves the event alone', () => {
    const { event } = clipboard('paste', '');

    expect(clipboardEvents()).to.be.empty;
    expect(event.defaultPrevented).to.be.false;
  });

  it('leaves copy, cut and paste to the editor while a cell is edited', () => {
    edit(1, 1);

    const copy = clipboard('copy');
    const cut = clipboard('cut');
    const paste = clipboard('paste', 'x');

    expect([copy.event.defaultPrevented, cut.event.defaultPrevented, paste.event.defaultPrevented]).to.eql([
      false,
      false,
      false
    ]);
    expect(copy.data.getData('text/plain')).to.equal('');
    expect(clipboardEvents()).to.be.empty;
  });
});
