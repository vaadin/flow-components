/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { type LaidOutParts, SheetLayout } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/SheetLayout.js';
import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import { SpreadsheetColumnHeader } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetColumnHeader.js';
import { SpreadsheetContextMenu } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetContextMenu.js';
import { SpreadsheetFormulaBar } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetFormulaBar.js';
import { SpreadsheetHeaderCorner } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetHeaderCorner.js';
import { SpreadsheetRowHeader } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetRowHeader.js';
import { SpreadsheetSheetTabs } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetSheetTabs.js';

/**
 * The sheet's own elements, laid out, on their own: where each part goes,
 * the scroll container as the keyboard's place, and following the host's
 * classes and theme.
 */
describe('SheetLayout', () => {
  let host: HTMLElement;
  let parts: LaidOutParts;
  let stripsChanged: number;
  let listening: AbortController;
  let layout: SheetLayout;

  /** Lets a mutation observer and a queued microtask run. */
  const settled = (): Promise<void> => new Promise((resolve) => setTimeout(resolve));

  beforeEach(() => {
    host = document.createElement('div');
    document.body.append(host);
    parts = {
      cellGrid: new SpreadsheetCellGrid(),
      columnHeader: new SpreadsheetColumnHeader(),
      rowHeader: new SpreadsheetRowHeader(),
      headerCorner: new SpreadsheetHeaderCorner(),
      formulaBar: new SpreadsheetFormulaBar(),
      sheetTabs: new SpreadsheetSheetTabs(),
      contextMenu: new SpreadsheetContextMenu()
    };
    stripsChanged = 0;
    listening = new AbortController();
    layout = new SheetLayout(host, parts, listening.signal, () => stripsChanged++);
    host.append(layout.root);
  });

  afterEach(() => {
    layout.dispose();
    listening.abort();
    host.remove();
  });

  it('puts the formula bar above the sheet and the tabs below it, the menu beside the formula bar', () => {
    expect(Array.from(layout.root.children)).to.eql([
      parts.formulaBar,
      parts.contextMenu,
      layout.scrollContainer,
      parts.sheetTabs
    ]);
  });

  it('puts the corner, the column letters, the row numbers and the cells in a grid that scrolls', () => {
    expect(Array.from(layout.scrollContainer.children)).to.eql([
      parts.headerCorner,
      parts.columnHeader,
      parts.rowHeader,
      parts.cellGrid
    ]);
    expect([parts.cellGrid.style.gridColumn, parts.cellGrid.style.gridRow]).to.eql(['2', '2']);
    expect([parts.rowHeader.style.gridColumn, parts.columnHeader.style.gridRow]).to.eql(['1', '1']);
    expect(getComputedStyle(layout.scrollContainer).overflow).to.equal('auto');
  });

  it('makes the scroll container the one control the keys go to, named as a spreadsheet', () => {
    const { scrollContainer } = layout;

    expect(scrollContainer.tabIndex).to.equal(0);
    expect(scrollContainer.getAttribute('role')).to.equal('application');
    expect(scrollContainer.getAttribute('aria-roledescription')).to.equal('spreadsheet');
    expect(scrollContainer.getAttribute('aria-label')).to.equal('Spreadsheet');
  });

  it('sizes the sheet, not the host', () => {
    layout.setWidth('300px');
    layout.setHeight('200px');

    expect([layout.root.style.width, layout.root.style.height]).to.eql(['300px', '200px']);
    expect(host.style.height).to.equal('');
  });

  describe("the host's classes and theme", () => {
    it('hide the tabs and the formula bar for the classes that ask for it, and say so', () => {
      host.classList.add('hidetabsheet');

      layout.followHost();

      expect(parts.sheetTabs.hidden).to.be.true;
      expect(parts.formulaBar.hidden).to.be.false;
      expect(stripsChanged).to.equal(1);
    });

    it('are followed as they change', async () => {
      layout.followHost();

      host.classList.add('hidefunctionbar');
      host.setAttribute('theme', 'lumo');
      await settled();

      expect(parts.formulaBar.hidden).to.be.true;
      for (const part of Object.values(parts)) {
        expect((part as HTMLElement).getAttribute('theme'), part.localName).to.equal('lumo');
      }

      host.removeAttribute('theme');
      await settled();
      expect(parts.cellGrid.hasAttribute('theme')).to.be.false;
    });

    it('are no longer followed once the layout is disposed of', async () => {
      layout.followHost();
      layout.dispose();

      host.classList.add('hidetabsheet');
      await settled();

      expect(parts.sheetTabs.hidden).to.be.false;
    });
  });

  it('tells the cells whether the sheet has the keyboard, once the focus has settled', async () => {
    layout.scrollContainer.focus();
    await settled();
    expect(parts.cellGrid.hasAttribute('sheet-focused')).to.be.true;

    layout.scrollContainer.blur();
    await settled();
    expect(parts.cellGrid.hasAttribute('sheet-focused')).to.be.false;
  });
});
