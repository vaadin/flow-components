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

  // The server no longer sends a selection to the client in case it is
  // copied - 10 MB for one column of 100,000 rows. The client copies the
  // cells it has, and asks the server for the text of a range it does not
  // have, which goes on the clipboard when it comes.
  describe('a range the client does not have', () => {
    type Server = { clipboardText(...range: number[]): Promise<string | null> };
    let written: ClipboardItem[];

    beforeEach(() => {
      written = [];
      Object.defineProperty(navigator, 'clipboard', {
        configurable: true,
        value: {
          write: (items: ClipboardItem[]) => {
            written.push(...items);
            return Promise.resolve();
          }
        }
      });
    });

    afterEach(() => {
      delete (navigator as unknown as Record<string, unknown>).clipboard;
    });

    /** As Flow gives the host its server's methods; returns the ranges asked for. */
    function serverAnswers(text: string | null, log: string[] = []): number[][] {
      const asked: number[][] = [];
      (host as HTMLElement & { $server?: Server }).$server = {
        clipboardText: (...range: number[]) => {
          asked.push(range);
          log.push('text');
          return Promise.resolve(text);
        }
      };
      return asked;
    }

    it('asks the server for its text, and puts that on the clipboard when it comes', async () => {
      const asked = serverAnswers('from the server');
      click(1, 1);
      click(2, 3, { shiftKey: true });

      const { event, data } = clipboard('copy');

      expect(event.defaultPrevented).to.be.true;
      expect(data.getData('text/plain'), 'nothing at once').to.equal('');
      // Columns, then rows.
      expect(asked).to.eql([[1, 1, 2, 3]]);
      const blob = await written[0].getType('text/plain');
      expect(await blob.text()).to.equal('from the server');
    });

    it('copies a range the client has at once, without asking the server', async () => {
      const asked = serverAnswers('from the server');
      sheet.recomputeViewport();
      // The first screen, then the cells past it, both answered.
      await new Promise((resolve) => setTimeout(resolve, 150));
      click(1, 1);
      click(2, 2, { shiftKey: true });

      const { data } = clipboard('copy');

      expect(data.getData('text/plain')).to.equal('A1\t\nA2\t');
      expect(asked).to.be.empty;
      expect(written).to.be.empty;
    });

    it('cuts: asks the server for the text before asking it to clear the range', () => {
      const log: string[] = [];
      serverAnswers('A2', log);
      host.addEventListener('spreadsheet-event', (e: Event) => {
        if ((e as CustomEvent).detail.type === 'clearSelectedCellsOnCut') {
          log.push('clear');
        }
      });
      click(1, 2);

      clipboard('cut');

      expect(log).to.eql(['text', 'clear']);
    });

    it('leaves the clipboard as it was when the range is no longer selected on the server', async () => {
      serverAnswers(null);
      click(1, 1);

      clipboard('copy');

      let refused = false;
      await written[0].getType('text/plain').catch(() => {
        refused = true;
      });
      expect(refused).to.be.true;
    });

    it('leaves no refusal of the browser unhandled', async () => {
      serverAnswers(null);
      Object.defineProperty(navigator, 'clipboard', {
        configurable: true,
        value: { write: () => Promise.reject(new Error('refused')) }
      });
      const unhandled: unknown[] = [];
      const listener = (e: PromiseRejectionEvent) => {
        unhandled.push(e.reason);
        e.preventDefault();
      };
      window.addEventListener('unhandledrejection', listener);
      try {
        click(1, 1);

        clipboard('copy');
        await new Promise((resolve) => setTimeout(resolve, 50));

        expect(unhandled).to.be.empty;
      } finally {
        window.removeEventListener('unhandledrejection', listener);
      }
    });

    it('copies the cells it has when the browser cannot write the clipboard later', () => {
      const asked = serverAnswers('from the server');
      Object.defineProperty(navigator, 'clipboard', { configurable: true, value: undefined });
      click(1, 1);

      const { data } = clipboard('copy');

      expect(data.getData('text/plain')).to.equal('A1');
      expect(asked).to.be.empty;
    });

    it('copies the cells it has when the browser has no clipboard items', () => {
      const asked = serverAnswers('from the server');
      const items = Object.getOwnPropertyDescriptor(window, 'ClipboardItem')!;
      delete (window as unknown as Record<string, unknown>).ClipboardItem;
      try {
        click(1, 1);

        const { data } = clipboard('copy');

        expect(data.getData('text/plain')).to.equal('A1');
        expect(asked).to.be.empty;
      } finally {
        Object.defineProperty(window, 'ClipboardItem', items);
      }
    });

    it('copies the cells it has when there is no server to ask', () => {
      click(1, 1);

      const { data } = clipboard('copy');

      expect(data.getData('text/plain')).to.equal('A1');
      expect(written).to.be.empty;
    });
  });
});
