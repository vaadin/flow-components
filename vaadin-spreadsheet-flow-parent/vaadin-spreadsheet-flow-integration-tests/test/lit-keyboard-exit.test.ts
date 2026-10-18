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
import { KEYBOARD_EXIT_HINT } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/KeyboardExit.js';
import type { SpreadsheetActionDetails } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * Escape, then Tab or Shift+Tab, takes the keyboard out of the sheet. Tab and
 * Shift+Tab move the active cell, as in Excel, and neither client had another
 * way out, so a keyboard user could not leave the sheet (WCAG 2.1.2). The keys
 * are real key presses: the browser moving the focus is what is tested.
 */
describe('Spreadsheet — Escape, then Tab, leaves the sheet', () => {
  let before: HTMLButtonElement;
  let after: HTMLButtonElement;
  let host: HTMLElement;
  let sheet: Spreadsheet;

  /**
   * A sheet between two page buttons, with a row group, so the sheet has
   * buttons of its own on it: the grouping toggle and the outline levels.
   */
  function attach(hostClass = 'hidetabsheet hidefunctionbar'): void {
    before = document.createElement('button');
    before.textContent = 'Before';
    host = document.createElement('div');
    host.className = hostClass;
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    after = document.createElement('button');
    after.textContent = 'After';
    document.body.append(before, host, after);
    sheet = new Spreadsheet(host);
    sheet.setCols(10);
    sheet.setRows(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.setRowGroupingMax(1);
    sheet.setRowGroupingData([{ uniqueIndex: 1, level: 1, startIndex: 2, endIndex: 4, collapsed: false }]);
    sheet.recomputeViewport();
  }

  afterEach(() => {
    sheet.dispose();
    before.remove();
    host.remove();
    after.remove();
  });

  function cells(): HTMLElement {
    return sheet.getScrollContainer();
  }

  function activeCell(): { col: number; row: number } {
    return sheet.getSelection().activeCell;
  }

  function rowToggle(): HTMLButtonElement {
    return sheet.getRowHeader().shadowRoot!.querySelector<HTMLButtonElement>('.row-grouping-toggle')!;
  }

  function levelButton(): HTMLButtonElement {
    return sheet.getHeaderCorner().shadowRoot!.querySelector<HTMLButtonElement>('.level-header')!;
  }

  describe('with nothing around the cells', () => {
    beforeEach(async () => {
      attach();
      await sheet.getRowHeader().updateComplete;
      await (sheet.getHeaderCorner() as HTMLElement & { updateComplete: Promise<unknown> }).updateComplete;
      cells().focus();
    });

    it('leaves forwards with Escape, then Tab, past the buttons on the sheet', async () => {
      await sendKeys({ press: 'Escape' });
      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(after);
      expect(activeCell()).to.deep.equal({ col: 1, row: 1 });
    });

    it('leaves backwards with Escape, then Shift+Tab', async () => {
      await sendKeys({ press: 'Escape' });
      await sendKeys({ press: 'Shift+Tab' });

      expect(document.activeElement).to.equal(before);
      expect(activeCell()).to.deep.equal({ col: 1, row: 1 });
    });

    it('moves the active cell with Tab alone, as in Excel', async () => {
      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(cells());
      expect(activeCell()).to.deep.equal({ col: 2, row: 1 });
    });

    it('moves the active cell again once another key comes between Escape and Tab', async () => {
      await sendKeys({ press: 'Escape' });
      await sendKeys({ press: 'ArrowDown' });
      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(cells());
      expect(activeCell()).to.deep.equal({ col: 2, row: 2 });
    });

    it('moves the active cell again after a press on the sheet', async () => {
      await sendKeys({ press: 'Escape' });
      sheet.getCellGrid().dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, composed: true }));
      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(cells());
      expect(activeCell()).to.deep.equal({ col: 2, row: 1 });
    });

    it('moves the active cell again once the focus has been away', async () => {
      await sendKeys({ press: 'Escape' });
      before.focus();
      cells().focus();
      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(cells());
      expect(activeCell()).to.deep.equal({ col: 2, row: 1 });
    });

    it('does not take Escape that confirms an input method conversion as a way out', async () => {
      cells().dispatchEvent(
        new KeyboardEvent('keydown', { key: 'Escape', isComposing: true, bubbles: true, composed: true })
      );
      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(cells());
      expect(activeCell()).to.deep.equal({ col: 2, row: 1 });
    });

    it('keeps the keyboard in the sheet when Escape closes the cell editor', async () => {
      await sendKeys({ press: 'F2' });
      await sheet.getCellGrid().updateComplete;
      expect(sheet.getCellGrid().renderState.editor).to.not.equal(null);

      await sendKeys({ press: 'Escape' });
      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(cells());
      expect(activeCell()).to.deep.equal({ col: 2, row: 1 });
    });

    it('keeps the keyboard in the sheet when Escape closes the context menu', async () => {
      const action: SpreadsheetActionDetails = { caption: 'One', key: '1', type: 0, iconNodeId: 0 };
      sheet.setHasActions(true);
      sheet
        .getCellGrid()
        .dispatchEvent(
          new CustomEvent('cell-contextmenu', {
            detail: { col: 1, row: 1, clientX: 120, clientY: 90 },
            bubbles: true,
            composed: true
          })
        );
      sheet.showActions([action]);
      await sheet.getContextMenu().updateComplete;
      // The menu takes the keyboard as it opens; the cells take it back while it is still open.
      cells().focus();
      expect(sheet.getContextMenu().renderState.open).to.be.true;

      await sendKeys({ press: 'Escape' });
      await sendKeys({ press: 'Tab' });

      expect(sheet.getContextMenu().renderState.open).to.be.false;
      expect(document.activeElement).to.equal(cells());
      expect(activeCell()).to.deep.equal({ col: 2, row: 1 });
    });

    it('moves on from a button on the sheet with Tab, and leaves the active cell where it is', async () => {
      rowToggle().focus();

      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(after);
      expect(activeCell()).to.deep.equal({ col: 1, row: 1 });
    });

    it('moves back from a button on the sheet with Shift+Tab, to the cells', async () => {
      levelButton().focus();

      await sendKeys({ press: 'Shift+Tab' });

      expect(document.activeElement).to.equal(cells());
      expect(activeCell()).to.deep.equal({ col: 1, row: 1 });
    });

    it('is no Tab stop of its own: coming back, Shift+Tab reaches the last button on the sheet', async () => {
      after.focus();

      await sendKeys({ press: 'Shift+Tab' });

      expect(document.activeElement).to.equal(sheet.getRowHeader());
      expect(sheet.getRowHeader().shadowRoot!.activeElement).to.equal(rowToggle());
    });

    it('tells assistive technology the way out', () => {
      const hintId = cells().getAttribute('aria-describedby');
      const hint = host.querySelector(`[id="${hintId}"]`) as HTMLElement;

      expect(hint.textContent).to.equal(KEYBOARD_EXIT_HINT);
      expect(hint.hidden).to.be.true;
    });

    it('keeps the point it leaves from out of what assistive technology reads', () => {
      expect(host.querySelector('.keyboard-exit')!.getAttribute('aria-hidden')).to.equal('true');
    });

    it('gives each sheet a hint of its own', () => {
      const other = document.createElement('div');
      document.body.appendChild(other);
      const second = new Spreadsheet(other);
      try {
        expect(second.getScrollContainer().getAttribute('aria-describedby')).to.not.equal(
          cells().getAttribute('aria-describedby')
        );
      } finally {
        second.dispose();
        other.remove();
      }
    });
  });

  describe('with the formula bar and the sheet tabs', () => {
    beforeEach(async () => {
      attach('');
      sheet.setSheetNames(['Data', 'Totals']);
      sheet.setSheetIndex(1);
      sheet.notifyStateChanges(['sheetNames', 'sheetIndex'], false);
      await sheet.getSheetTabs().updateComplete;
      await sheet.getFormulaBar().updateComplete;
      cells().focus();
    });

    it('leaves forwards to the sheet tabs, whose first stop is the button that adds a sheet', async () => {
      await sendKeys({ press: 'Escape' });
      await sendKeys({ press: 'Tab' });

      expect(document.activeElement).to.equal(sheet.getSheetTabs());
      expect(sheet.getSheetTabs().shadowRoot!.activeElement!.getAttribute('aria-label')).to.equal('Add a sheet');
    });

    it('leaves backwards to the formula bar', async () => {
      await sendKeys({ press: 'Escape' });
      await sendKeys({ press: 'Shift+Tab' });

      expect(document.activeElement).to.equal(sheet.getFormulaBar());
    });
  });
});

/**
 * A pop-up opened with the pointer leaves the keyboard on the cells, and
 * Escape there closes it. That Escape is not a way out.
 */
describe('Spreadsheet — Escape that closes a pop-up', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    // A real shadow root: the popup's content is projected through a slot.
    sheet = new Spreadsheet(host, host.attachShadow({ mode: 'open' }));
    sheet.setCols(10);
    sheet.setRows(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.recomputeViewport();
    const content = document.createElement('div');
    content.textContent = 'filter';
    (window as any).Vaadin = { Flow: { clients: { ROOT: { getByNodeId: (n: number) => (n === 42 ? content : null) } } } };
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
    delete (window as any).Vaadin;
  });

  it('keeps the keyboard in the sheet', async () => {
    sheet.getScrollContainer().focus();
    sheet.onPopupButtonOpen(1, 4, '42', 'ROOT');
    expect(sheet.getPopupOverlay().hidden).to.be.false;

    await sendKeys({ press: 'Escape' });
    await sendKeys({ press: 'Tab' });
    // It fades out before it hides.
    sheet
      .getPopupOverlay()
      .getAnimations()
      .forEach((animation) => animation.finish());
    await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));

    expect(sheet.getPopupOverlay().hidden).to.be.true;
    expect(host.shadowRoot!.activeElement).to.equal(sheet.getScrollContainer());
    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 1 });
  });
});
