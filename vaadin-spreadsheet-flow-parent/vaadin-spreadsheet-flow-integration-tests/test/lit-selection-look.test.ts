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
 * What a selection looks like: the tint over a range, and a selection of
 * several ranges - picked out with Ctrl-clicks - drawn without the outline
 * and the fill handle.
 */
describe('Spreadsheet — how a selection looks', () => {
  let sheet: Spreadsheet;
  let host: HTMLElement;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:600px;height:400px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(10);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.recomputeViewport();
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

  async function grid(): Promise<ShadowRoot> {
    await sheet.getCellGrid().updateComplete;
    return sheet.getCellGrid().shadowRoot!;
  }

  function cell(root: ShadowRoot, col: number, row: number): HTMLElement {
    return root.querySelector<HTMLElement>(`.cell.col${col}.row${row}`)!;
  }

  it("tints the range's cells light blue, leaving the active cell clear", async () => {
    // rgba(75, 155, 235, 0.1) over white is rgb(237, 245, 253), and still
    // lets nine tenths of a fill through.
    click(2, 2);
    click(3, 3, { shiftKey: true });
    const root = await grid();

    expect(getComputedStyle(cell(root, 3, 3)).boxShadow).to.contain('rgba(75, 155, 235, 0.1)');
    expect(getComputedStyle(cell(root, 2, 2)).boxShadow).to.equal('none');
  });

  it('leaves the outline box itself clear, so it tints nothing on its own', async () => {
    // In a frozen pane the box lies over the active cell, which stays
    // white.
    click(2, 2);
    click(3, 3, { shiftKey: true });
    const root = await grid();

    expect(getComputedStyle(root.querySelector('.sheet-selection')!).backgroundColor).to.equal('rgba(0, 0, 0, 0)');
  });

  it('marks the active cell of several ranges with a thin black outline', async () => {
    click(2, 2);
    click(4, 4, { ctrlKey: true });
    const root = await grid();

    const active = cell(root, 4, 4);
    expect(active.classList.contains('selected-cell-highlight')).to.be.true;
    const style = getComputedStyle(active);
    expect(`${style.outlineWidth} ${style.outlineStyle} ${style.outlineColor}`).to.equal('1px solid rgb(34, 34, 34)');
    expect(style.outlineOffset).to.equal('-2px');
  });

  it('draws no range outline and no fill handle for several ranges', async () => {
    click(2, 2);
    click(4, 4, { ctrlKey: true });
    const root = await grid();

    const overlay = root.querySelector<HTMLElement>('.sheet-selection')!;
    expect(overlay.classList.contains('multi-range')).to.be.true;
    for (const part of Array.from(overlay.children)) {
      expect(getComputedStyle(part).display, part.className).to.equal('none');
    }
  });

  it('heads the columns and rows of every range', async () => {
    click(2, 2);
    click(4, 4, { ctrlKey: true });
    sheet.recomputeViewport();
    await sheet.getColumnHeader().updateComplete;
    await sheet.getRowHeader().updateComplete;

    const lit = (root: ShadowRoot, selector: string): string[] =>
      Array.from(root.querySelectorAll(`${selector}[part~="header-selected"]`)).map((el) => el.textContent!.trim());
    expect(lit(sheet.getColumnHeader().shadowRoot!, '.col-header-cell')).to.eql(['B', 'D']);
    expect(lit(sheet.getRowHeader().shadowRoot!, '.row-header-cell')).to.eql(['2', '4']);
  });

  it('draws the outline again, without the black one, once one range is selected', async () => {
    click(2, 2);
    click(4, 4, { ctrlKey: true });
    click(5, 5);
    const root = await grid();

    expect(root.querySelector('.selected-cell-highlight')).to.be.null;
    expect(root.querySelector('.sheet-selection')!.classList.contains('multi-range')).to.be.false;
  });

  it('takes a Ctrl-click on a Mac as the Ctrl-click it is', async () => {
    // The browser reports it as a right-click with the left button, and
    // fires no click.
    click(2, 2);
    const root = await grid();
    cell(root, 4, 4).dispatchEvent(
      new MouseEvent('contextmenu', { button: 0, ctrlKey: true, bubbles: true, composed: true, cancelable: true })
    );

    expect(sheet.getSelection().activeCell).to.eql({ col: 4, row: 4 });
    expect(sheet.getSelection().auxRanges).to.eql([{ c1: 2, c2: 2, r1: 2, r2: 2 }]);
  });
});

/**
 * A selected cell in hidden columns or rows, as scrolled_frozen.xlsx opens
 * with A1 selected and columns A:B and rows 1:2 hidden. An outline drawn for
 * such a cell would be a sliver on the gridline, with two pixels of fill
 * handle.
 */
describe('Spreadsheet — a selection out of sight', () => {
  let sheet: Spreadsheet;
  let host: HTMLElement;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:600px;height:400px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(10);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.setHiddenColumnIndexes([1, 2]);
    sheet.setHiddenRowIndexes([1, 2]);
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  async function selection(): Promise<HTMLElement> {
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    return sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('.sheet-selection')!;
  }

  it('draws nothing for a selected cell that is hidden', async () => {
    sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);

    const box = await selection();

    expect(box.querySelector('.s-top, .s-left, .s-right, .s-bottom')).to.be.null;
    expect(box.querySelector('.selection-corner')).to.be.null;
  });

  it('still says where the selection is', async () => {
    sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);

    const box = await selection();

    expect(`${box.dataset.col} ${box.dataset.row}`).to.equal('1 1');
  });

  it('draws the part of a range that shows when its first cell is hidden', async () => {
    sheet.setSelectedCellAndRange('A1', 1, 1, 1, 4, 1, 4, false);

    const box = await selection();

    expect(box.querySelectorAll('.s-top, .s-left, .s-right, .s-bottom')).to.have.length(4);
    expect(box.querySelector('.selection-corner')).to.not.be.null;
    // C3:D4 shows: two columns and two rows.
    expect(`${box.style.width} ${box.style.height}`).to.equal('101px 41px');
  });
});
