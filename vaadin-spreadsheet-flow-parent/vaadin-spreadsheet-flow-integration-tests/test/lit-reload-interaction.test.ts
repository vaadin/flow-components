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
import type { SpreadsheetActionDetails } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';
import { rendered } from './spreadsheet-harness.js';

/**
 * A reload - another sheet shown, a new workbook, a sheet added, deleted or
 * hidden, panes frozen - makes the server forget the selection, and the
 * sheet may be a different one. Whatever the user was in the middle of ends
 * with it, and none of it is committed: a value, a note or a menu pick would
 * otherwise land on the sheet the server shows now, in the cell with the
 * same address.
 */
describe('Spreadsheet — a reload ends what the user was doing', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let sent: { type: string; data: Record<string, unknown> }[];

  beforeEach(async () => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setCols(10);
    sheet.setRows(20);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(20).fill(15));
    sheet.setHasActions(true);
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    sent = [];
    host.addEventListener('spreadsheet-event', (e: Event) => sent.push((e as CustomEvent).detail));
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  function grid(): ShadowRoot {
    return sheet.getCellGrid().shadowRoot!;
  }

  function types(): string[] {
    return sent.map((e) => e.type);
  }

  /** The element that has the keyboard, looking into the sheet's parts. */
  function focused(): Element | null {
    let el = document.activeElement;
    while (el?.shadowRoot?.activeElement) {
      el = el.shadowRoot.activeElement;
    }
    return el;
  }

  async function reload(): Promise<void> {
    sheet.setReload(true);
    await sheet.getCellGrid().updateComplete;
    await rendered(sheet);
  }

  function gridEvent(type: string, detail: Record<string, unknown>): void {
    sheet.getCellGrid().dispatchEvent(new CustomEvent(type, { detail, bubbles: true, composed: true }));
  }

  async function typeIntoCell(text: string): Promise<HTMLInputElement> {
    sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { key: text[0], bubbles: true, composed: true, cancelable: true }));
    await sheet.getCellGrid().updateComplete;
    const input = grid().querySelector<HTMLInputElement>('.cell-editor')!;
    input.value = text;
    input.setSelectionRange(text.length, text.length);
    input.dispatchEvent(new Event('input', { bubbles: true, composed: true }));
    return input;
  }

  it('closes the cell editor and commits nothing, though its input loses the keyboard', async () => {
    const input = await typeIntoCell('typed');
    input.focus();

    await reload();

    expect(sheet.getCellGrid().renderState.editor).to.equal(null);
    expect(grid().querySelector('.cell-editor')).to.equal(null);
    expect(types()).to.not.include('cellValueEdited');
    expect(focused()).to.equal(sheet.getScrollContainer());
  });

  it('ends pointing at cells, and the formula loses its colours', async () => {
    await typeIntoCell('=A1+C3');
    expect(sheet.getCellGrid().renderState.formulaReferences).to.have.length(2);

    await reload();

    expect(sheet.getCellGrid().renderState.formulaReferences).to.be.empty;
    expect(sheet.getFormulaBar().renderState.highlights ?? null).to.equal(null);
  });

  it('ends an edit in the formula field without committing it', async () => {
    await rendered(sheet);
    const field = sheet.getFormulaBar().shadowRoot!.querySelector<HTMLInputElement>('.formula')!;
    field.focus();
    field.value = 'typed';
    field.dispatchEvent(new Event('input', { bubbles: true, composed: true }));

    await reload();

    expect(types()).to.not.include('cellValueEdited');
    expect(focused()).to.equal(sheet.getScrollContainer());
  });

  it('closes a note being written without saving it', async () => {
    sheet.editCellComment(1, 1);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    const note = grid().querySelector<HTMLTextAreaElement>('.comment-overlay-edit')!;
    note.focus();
    note.value = 'half a note';

    await reload();

    expect(sheet.getCellGrid().renderState.editingCommentKey).to.equal(null);
    expect(types()).to.not.include('updateCellComment');
    expect(focused()).to.equal(sheet.getScrollContainer());
  });

  it('ends a drag: the cells the pointer crosses after it select nothing', async () => {
    gridEvent('cell-mousedown', { col: 2, row: 2, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false });

    await reload();
    gridEvent('cell-mouseover', { col: 5, row: 6 });
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

    expect(sheet.getSelection().activeRange).to.eql({ c1: 2, c2: 2, r1: 2, r2: 2 });
    expect(types()).to.not.include('cellRangeSelected');
    expect(types()).to.not.include('cellRangePainted');
  });

  it('ends a fill drag without painting', async () => {
    gridEvent('selection-paint-start', {});
    gridEvent('cell-mouseover', { col: 2, row: 6 });

    await reload();
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

    expect(types().filter((t) => t.startsWith('selection'))).to.be.empty;
    expect(sheet.getCellGrid().renderState.paintRect).to.equal(null);
  });

  it('ends a header edge drag without resizing, and takes its line away', async () => {
    sheet.getColumnHeader().dispatchEvent(
      new CustomEvent('column-resize-start', {
        detail: { col: 2, clientX: 100, clientY: 10, left: 50 },
        bubbles: true,
        composed: true
      })
    );
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 140, clientY: 10, bubbles: true }));

    await reload();
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));

    expect(types()).to.not.include('columnResized');
    const line = host.querySelector<HTMLElement>('.resize-line') ?? host.shadowRoot?.querySelector<HTMLElement>('.resize-line');
    expect(line!.hidden).to.be.true;
  });

  it('closes the context menu, so nothing is picked for the sheet now shown', async () => {
    const insertRow: SpreadsheetActionDetails = { caption: 'Insert row', key: 'insert-row', type: 0, iconNodeId: 0 };
    sheet.showActions([insertRow]);
    await sheet.getContextMenu().updateComplete;
    expect(sheet.getContextMenu().renderState.open).to.be.true;

    await reload();

    expect(sheet.getContextMenu().renderState.open).to.be.false;
  });

  it('leaves the keyboard where it was when it was outside the sheet', async () => {
    const outside = document.createElement('input');
    document.body.appendChild(outside);
    outside.focus();

    await reload();

    expect(document.activeElement).to.equal(outside);
    outside.remove();
  });

  it('stops counting a selection Shift was growing: the server names the cell again', async () => {
    sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', shiftKey: true, bubbles: true, composed: true, cancelable: true }));
    await rendered(sheet);
    expect(sheet.getFormulaBar().renderState.address).to.equal('2R x 1C');

    await reload();
    sheet.showSelectedCell(null, 1, 1, '', false, false, true);

    await rendered(sheet);
    expect(sheet.getFormulaBar().renderState.address).to.equal('A1');
  });

  it('lets the next click select again after a press that pointed at a cell', async () => {
    await typeIntoCell('=');
    gridEvent('cell-mousedown', { col: 4, row: 4, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false });

    await reload();
    gridEvent('cell-click', { col: 5, row: 5, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false });

    expect(sheet.getSelection().activeCell).to.eql({ col: 5, row: 5 });
  });

  it('takes a link tooltip away', async () => {
    sheet.setHyperlinksTooltips({ 'col3 row3': 'https://vaadin.com' });
    grid()
      .querySelector('.cell.col3.row3')!
      .dispatchEvent(new MouseEvent('mouseover', { bubbles: true, composed: true }));
    const tooltip = host.querySelector<HTMLElement>('.link-tooltip')!;
    expect(tooltip.hidden).to.be.false;

    await reload();

    expect(tooltip.hidden).to.be.true;
  });

  it('commits nothing for an editor that is not open', () => {
    // Its input losing the keyboard as it goes is not the user leaving it.
    gridEvent('cell-editor-commit', { col: 2, row: 2, value: 'stale' });

    expect(types()).to.not.include('cellValueEdited');
  });

  it('tells the server of no menu closing when none was open', async () => {
    await reload();

    expect(types()).to.not.include('contextMenuClosed');
  });
});
