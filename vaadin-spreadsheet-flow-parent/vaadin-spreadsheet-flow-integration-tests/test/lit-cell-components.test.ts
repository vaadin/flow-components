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
import { computeCellSlots, visibleCellSlots } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/VirtualChildren.js';

/**
 * Server-side elements rendered inside a cell.
 *
 * `Spreadsheet.setComponent` puts a component in a cell, and a custom cell
 * editor attaches one too. Both arrive as Flow virtual children — the
 * element exists on the client but is not in the document until the client
 * places it. The cell shows it instead of its own text.
 */

/** A 3x3 sheet in view with no frozen panes. */
const VIEWPORT_3x3 = {
  viewport: {
    firstColumnIndex: 1,
    lastColumnIndex: 3,
    firstRowIndex: 1,
    lastRowIndex: 3
  },
  verticalSplitPosition: 0,
  horizontalSplitPosition: 0
};

function slotsFor(input: Partial<Parameters<typeof computeCellSlots>[0]>) {
  return computeCellSlots({
    componentIdToCellKey: {},
    cellKeyToEditorId: {},
    showCustomEditorOnFocus: false,
    activeCellKey: null,
    ...input
  });
}

describe('computeCellSlots — components', () => {
  it('gives a cell with a component its component slot', () => {
    // The server keeps the map as node id → cell key; rendering asks the
    // opposite question, once per visible cell.
    expect(slotsFor({ componentIdToCellKey: { '31': 'col2 row3' } })).to.deep.equal({
      'col2 row3': { nodeId: '31', slot: 'custom-component-31', kind: 'component' }
    });
  });

  it('assigns nothing when there is nothing to assign', () => {
    expect(slotsFor({})).to.deep.equal({});
  });

  it('keeps every distinct cell', () => {
    const slots = slotsFor({
      componentIdToCellKey: { '1': 'col1 row1', '2': 'col2 row2' }
    });
    expect(Object.keys(slots).sort()).to.deep.equal(['col1 row1', 'col2 row2']);
  });

  it('lets the later component win a contested cell', () => {
    // One slot per cell, so only one can be shown either way.
    const slots = slotsFor({
      componentIdToCellKey: { '1': 'col1 row1', '2': 'col1 row1' }
    });
    expect(slots['col1 row1'].nodeId).to.equal('2');
  });

  it('drops a component with no cell', () => {
    expect(slotsFor({ componentIdToCellKey: { '1': '' } })).to.deep.equal({});
  });
});

describe('computeCellSlots — custom editors', () => {
  it('shows every editor in its cell by default', () => {
    // setShowCustomEditorOnFocus(false) is the default: a cell with a
    // custom editor displays it, focused or not.
    const slots = slotsFor({ cellKeyToEditorId: { 'col2 row2': '77' } });
    expect(slots['col2 row2']).to.deep.equal({
      nodeId: '77',
      slot: 'custom-editor-77',
      kind: 'editor'
    });
  });

  it('shows only the selected cell\'s editor when asked to', () => {
    const slots = slotsFor({
      cellKeyToEditorId: { 'col1 row1': 'a', 'col2 row2': 'b' },
      showCustomEditorOnFocus: true,
      activeCellKey: 'col2 row2'
    });

    expect(Object.keys(slots)).to.deep.equal(['col2 row2']);
    expect(slots['col2 row2'].nodeId).to.equal('b');
  });

  it('shows no editor on focus when nothing is selected', () => {
    expect(
      slotsFor({
        cellKeyToEditorId: { 'col1 row1': 'a' },
        showCustomEditorOnFocus: true,
        activeCellKey: null
      })
    ).to.deep.equal({});
  });

  it('drops an editor entry with no node id', () => {
    expect(slotsFor({ cellKeyToEditorId: { 'col1 row1': '' } })).to.deep.equal({});
  });

  it('gives the editor the cell when it also holds a component', () => {
    // The editor is the interactive one, and it is applied last.
    const slots = slotsFor({
      componentIdToCellKey: { '31': 'col1 row1' },
      cellKeyToEditorId: { 'col1 row1': '77' }
    });

    expect(slots['col1 row1'].kind).to.equal('editor');
    expect(slots['col1 row1'].nodeId).to.equal('77');
  });

  it('leaves a component alone in a cell with no editor', () => {
    const slots = slotsFor({
      componentIdToCellKey: { '31': 'col1 row1' },
      cellKeyToEditorId: { 'col2 row2': '77' }
    });

    expect(slots['col1 row1'].kind).to.equal('component');
    expect(slots['col2 row2'].kind).to.equal('editor');
  });

  it('hides a component-free editor cell again when focus moves away', () => {
    const withFocus = slotsFor({
      cellKeyToEditorId: { 'col1 row1': 'a' },
      showCustomEditorOnFocus: true,
      activeCellKey: 'col1 row1'
    });
    const withoutFocus = slotsFor({
      cellKeyToEditorId: { 'col1 row1': 'a' },
      showCustomEditorOnFocus: true,
      activeCellKey: 'col2 row2'
    });

    expect(Object.keys(withFocus)).to.deep.equal(['col1 row1']);
    expect(withoutFocus).to.deep.equal({});
  });
});

describe('visibleCellSlots — only what the viewport renders', () => {
  it('returns the assignment for a visible cell', () => {
    const slots = slotsFor({ componentIdToCellKey: { '31': 'col2 row2' } });

    expect(visibleCellSlots(slots, VIEWPORT_3x3).map((a) => a.nodeId)).to.deep.equal([
      '31'
    ]);
  });

  it('includes a cell pinned in a frozen row', () => {
    // A frozen row stays rendered whatever the scroll position, in its
    // own quadrant. Counting only the scrolling viewport left an editor
    // pinned there with no element.
    const slots = slotsFor({ cellKeyToEditorId: { 'col2 row1': '77' } });
    const scrolledPast = {
      viewport: {
        firstColumnIndex: 1,
        lastColumnIndex: 3,
        firstRowIndex: 40,
        lastRowIndex: 60
      },
      verticalSplitPosition: 2,
      horizontalSplitPosition: 0
    };

    expect(visibleCellSlots(slots, scrolledPast).map((a) => a.nodeId)).to.deep.equal([
      '77'
    ]);
  });

  it('includes a cell pinned in a frozen column', () => {
    const slots = slotsFor({ cellKeyToEditorId: { 'col1 row45': '77' } });
    const scrolledRight = {
      viewport: {
        firstColumnIndex: 20,
        lastColumnIndex: 30,
        firstRowIndex: 40,
        lastRowIndex: 60
      },
      verticalSplitPosition: 0,
      horizontalSplitPosition: 2
    };

    expect(visibleCellSlots(slots, scrolledRight).map((a) => a.nodeId)).to.deep.equal([
      '77'
    ]);
  });

  it('includes the frozen corner', () => {
    const slots = slotsFor({ cellKeyToEditorId: { 'col1 row1': '77' } });
    const scrolledAway = {
      viewport: {
        firstColumnIndex: 20,
        lastColumnIndex: 30,
        firstRowIndex: 40,
        lastRowIndex: 60
      },
      verticalSplitPosition: 2,
      horizontalSplitPosition: 2
    };

    expect(visibleCellSlots(slots, scrolledAway).map((a) => a.nodeId)).to.deep.equal([
      '77'
    ]);
  });

  it('reports each assignment once when the quadrants overlap', () => {
    // With the sheet scrolled to the top, the frozen rows and the
    // viewport rows are the same rows.
    const slots = slotsFor({ cellKeyToEditorId: { 'col1 row1': '77' } });
    const atTop = {
      viewport: {
        firstColumnIndex: 1,
        lastColumnIndex: 3,
        firstRowIndex: 1,
        lastRowIndex: 3
      },
      verticalSplitPosition: 2,
      horizontalSplitPosition: 2
    };

    expect(visibleCellSlots(slots, atTop)).to.have.length(1);
  });

  it('skips one scrolled out of view', () => {
    // No slot is rendered for it, so parenting it would take it out of the
    // document for nothing.
    const slots = slotsFor({ componentIdToCellKey: { '31': 'col9 row9' } });

    expect(visibleCellSlots(slots, VIEWPORT_3x3)).to.deep.equal([]);
  });

  it("includes the cells on the viewport's last row and column, and none past them", () => {
    const slots = slotsFor({
      componentIdToCellKey: { a: 'col3 row1', b: 'col1 row3', c: 'col4 row1', d: 'col1 row4' }
    });

    expect(
      visibleCellSlots(slots, VIEWPORT_3x3)
        .map((s) => s.nodeId)
        .sort()
    ).to.deep.equal(['a', 'b']);
  });

  it('includes the last frozen row and column, and not the ones after them', () => {
    const slots = slotsFor({
      componentIdToCellKey: { a: 'col2 row45', b: 'col25 row2', c: 'col3 row45', d: 'col25 row3' }
    });
    const scrolled = {
      viewport: { firstColumnIndex: 20, lastColumnIndex: 30, firstRowIndex: 40, lastRowIndex: 60 },
      verticalSplitPosition: 2,
      horizontalSplitPosition: 2
    };

    expect(
      visibleCellSlots(slots, scrolled)
        .map((s) => s.nodeId)
        .sort()
    ).to.deep.equal(['a', 'b']);
  });

  it('returns nothing when no cell has an assignment', () => {
    expect(visibleCellSlots({}, VIEWPORT_3x3)).to.deep.equal([]);
  });

  it('returns every visible assignment, of either kind', () => {
    const slots = slotsFor({
      componentIdToCellKey: { a: 'col1 row1', c: 'col9 row1' },
      cellKeyToEditorId: { 'col3 row3': 'b' }
    });

    expect(
      visibleCellSlots(slots, VIEWPORT_3x3)
        .map((s) => s.nodeId)
        .sort()
    ).to.deep.equal(['a', 'b']);
  });
});

describe('Spreadsheet — components inside cells', () => {
  const originalVaadin = (window as any).Vaadin;

  function attachSheet(components: Record<string, HTMLElement>): {
    host: HTMLElement;
    sheet: Spreadsheet;
    cleanup: () => void;
  } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    (host as any).appId = 'ROOT';
    document.body.appendChild(host);
    // The wrapper renders into its own shadow root, and that boundary is
    // exactly what the forwarding slots exist to cross. A plain div host
    // would let a broken chain pass.
    const renderTarget = host.attachShadow({ mode: 'open' });
    (window as any).Vaadin = {
      Flow: {
        clients: { ROOT: { getByNodeId: (n: number) => components[String(n)] ?? null } }
      }
    };
    const sheet = new Spreadsheet(host, renderTarget);
    sheet.setRows(50);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(50).fill(15));
    return {
      host,
      sheet,
      cleanup: () => {
        sheet.dispose();
        host.remove();
        (window as any).Vaadin = originalVaadin;
      }
    };
  }

  function button(label: string): HTMLElement {
    const el = document.createElement('button');
    el.textContent = label;
    return el;
  }

  it('projects the component into its cell', async () => {
    const widget = button('Click me');
    const { host, sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const cell = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>('.cell.col2.row2')!;
    const slot = cell.querySelector('slot')!;
    expect(slot.getAttribute('name')).to.equal('custom-component-31');
    const assigned = slot.assignedElements({ flatten: true });
    expect(assigned).to.have.length(1);
    expect(assigned[0]).to.equal(widget);
    cleanup();
  });

  it('shows the component instead of the cell value', async () => {
    // The component is what the cell is for; rendering both would stack
    // them on top of each other.
    const { host, sheet, cleanup } = attachSheet({ '31': button('Click me') });
    sheet.updateBottomRightCellValues([
      { row: 2, col: 2, value: 'ignored', cellStyle: 'cs0' } as any
    ]);
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const cell = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>('.cell.col2.row2')!;
    expect(cell.textContent).to.not.contain('ignored');
    cleanup();
  });

  it('leaves cells without a component showing their value', async () => {
    const { host, sheet, cleanup } = attachSheet({ '31': button('Click me') });
    sheet.updateBottomRightCellValues([
      { row: 1, col: 1, value: 'plain', cellStyle: 'cs0' } as any
    ]);
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const cell = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>('.cell.col1.row1')!;
    expect(cell.textContent!.trim()).to.equal('plain');
    expect(cell.querySelector('slot')).to.be.null;
    cleanup();
  });

  it('takes the component back when the server removes it', async () => {
    const widget = button('Click me');
    const { host, sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.setComponentIDtoCellKeysMap({});
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(widget.parentElement).to.be.null;
    expect(widget.hasAttribute('slot')).to.be.false;
    cleanup();
  });

  it('does not re-parent the component when the sheet repaints', async () => {
    // Re-parenting rebuilds a live Vaadin component, losing its state —
    // half-typed text in a field, an open dropdown.
    const widget = button('Click me');
    const { host, sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    let moved = false;
    new MutationObserver(() => {
      moved = true;
    }).observe(host, { childList: true });

    sheet.recomputeViewport();
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(moved).to.be.false;
    cleanup();
  });

  it('releases the component when the sheet is disposed', async () => {
    const widget = button('Click me');
    const { host, sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    cleanup();

    expect(widget.parentElement).to.be.null;
  });
});

describe('Spreadsheet — custom cell editors', () => {
  const originalVaadin = (window as any).Vaadin;

  function attachSheet(editors: Record<string, HTMLElement>): {
    host: HTMLElement;
    sheet: Spreadsheet;
    cleanup: () => void;
  } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    (host as any).appId = 'ROOT';
    document.body.appendChild(host);
    // The wrapper renders into its own shadow root, and that boundary is
    // exactly what the forwarding slots exist to cross. A plain div host
    // would let a broken chain pass.
    const renderTarget = host.attachShadow({ mode: 'open' });
    (window as any).Vaadin = {
      Flow: {
        clients: { ROOT: { getByNodeId: (n: number) => editors[String(n)] ?? null } }
      }
    };
    const sheet = new Spreadsheet(host, renderTarget);
    sheet.setRows(50);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(50).fill(15));
    return {
      host,
      sheet,
      cleanup: () => {
        sheet.dispose();
        host.remove();
        (window as any).Vaadin = originalVaadin;
      }
    };
  }

  function field(): HTMLElement {
    return document.createElement('input');
  }

  function cell(sheet: Spreadsheet, selector: string): HTMLElement | null {
    return sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(selector);
  }

  it('shows the editor in its cell by default', async () => {
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const slot = cell(sheet, '.cell.col2.row2')!.querySelector('slot')!;
    expect(slot.getAttribute('name')).to.equal('custom-editor-77');
    const assigned = slot.assignedElements({ flatten: true });
    expect(assigned).to.have.length(1);
    expect(assigned[0]).to.equal(editor);
    cleanup();
  });

  it('commits an edit left for a custom editor and leaves the keyboard in the editor', async () => {
    // A field's input sits inside the field.
    const field = document.createElement('div');
    const fieldInput = document.createElement('input');
    field.append(fieldInput);
    const { host, sheet, cleanup } = attachSheet({ '77': field });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    const sent: string[] = [];
    host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail.type));
    sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { key: 'x', bubbles: true, composed: true, cancelable: true }));
    await sheet.getCellGrid().updateComplete;
    const cellEditor = cell(sheet, '.cell-editor') as HTMLInputElement;
    expect(sheet.getCellGrid().shadowRoot!.activeElement, 'editing').to.equal(cellEditor);

    fieldInput.focus();
    await sheet.getCellGrid().updateComplete;

    expect(sent).to.include('cellValueEdited');
    expect(document.activeElement).to.equal(fieldInput);
    cleanup();
  });

  it('pads a cell showing a custom editor all round', async () => {
    // The cell gets a class of its own, with two pixels of padding on every
    // side; the editor sits two pixels higher than in a plain cell, which
    // pads only its sides.
    const { sheet, cleanup } = attachSheet({ '77': field() });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const editorCell = cell(sheet, '.cell.col2.row2')!;
    expect(editorCell.classList.contains('custom-editor-cell')).to.be.true;
    expect(getComputedStyle(editorCell).padding).to.equal('2px');
    cleanup();
  });

  it('leaves the padding of a cell showing a component alone', async () => {
    const { sheet, cleanup } = attachSheet({ '31': document.createElement('span') });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const componentCell = cell(sheet, '.cell.col2.row2')!;
    expect(componentCell.classList.contains('custom-editor-cell')).to.be.false;
    expect(getComputedStyle(componentCell).padding).to.equal('0px 2px');
    cleanup();
  });

  it('holds the editor back until the cell is selected on focus mode', async () => {
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setShowCustomEditorOnFocus(true);
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    // Selection starts at A1.
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(cell(sheet, '.cell.col2.row2')!.querySelector('slot')).to.be.null;
    expect(editor.parentElement).to.be.null;
    cleanup();
  });

  it('brings the editor in when its cell is selected', async () => {
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setShowCustomEditorOnFocus(true);
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const slot = cell(sheet, '.cell.col2.row2')!.querySelector('slot')!;
    expect(slot.getAttribute('name')).to.equal('custom-editor-77');
    expect(editor.parentElement).to.equal(host);
    cleanup();
  });

  it('takes the editor back out when the selection moves away', async () => {
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setShowCustomEditorOnFocus(true);
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(editor.parentElement).to.be.null;
    expect(editor.hasAttribute('slot')).to.be.false;
    cleanup();
  });

  it('gives the editor the cell when a component is there too', async () => {
    const editor = field();
    const widget = document.createElement('span');
    const { host, sheet, cleanup } = attachSheet({ '77': editor, '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const slot = cell(sheet, '.cell.col2.row2')!.querySelector('slot')!;
    expect(slot.getAttribute('name')).to.equal('custom-editor-77');
    expect(editor.parentElement).to.equal(host);
    // The component keeps no slot it cannot project through.
    expect(widget.parentElement).to.be.null;
    cleanup();
  });

  it('does not re-parent the editor when the sheet repaints', async () => {
    // Re-parenting rebuilds the component and loses half-typed input.
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    let moved = false;
    new MutationObserver(() => {
      moved = true;
    }).observe(host, { childList: true });

    sheet.recomputeViewport();
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(moved).to.be.false;
    cleanup();
  });

  it('shows the editor of a merged cell', async () => {
    // The per-cell loop skips everything a merge covers, its anchor
    // included, so the merged overlay is the only element rendered for the
    // region — the slot has to be there or the editor has nowhere to go.
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setMergedRegions([{ col1: 2, row1: 2, col2: 4, row2: 3 }] as any);
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const merged = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>('.merged-cell')!;
    expect(merged).to.not.be.null;
    const slot = merged.querySelector('slot')!;
    expect(slot.getAttribute('name')).to.equal('custom-editor-77');
    const assigned = slot.assignedElements({ flatten: true });
    expect(assigned).to.have.length(1);
    expect(assigned[0]).to.equal(editor);
    cleanup();
  });

  it('shows a component in a merged cell', async () => {
    const widget = document.createElement('span');
    const { host, sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setMergedRegions([{ col1: 2, row1: 2, col2: 4, row2: 3 }] as any);
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const merged = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>('.merged-cell')!;
    expect(merged.querySelector('slot')!.getAttribute('name')).to.equal(
      'custom-component-31'
    );
    cleanup();
  });

  it('gives F2 to the custom editor instead of the inline one', async () => {
    // The editor IS the way to change such a cell, so the inline editor
    // has no business opening over it. Excel's F2 does the same.
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'F2', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(document.activeElement).to.equal(editor);
    expect(
      sheet.getCellGrid().shadowRoot!.querySelector('[part~="cell-input"]')
    ).to.be.null;
    cleanup();
  });

  it('gives Enter to the custom editor instead of moving down', async () => {
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(document.activeElement).to.equal(editor);
    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
    cleanup();
  });

  it('lets the character that opened the editor reach it', async () => {
    // The browser delivers the keypress to whatever holds the focus once
    // the handlers have run, so the editor must be focused without the
    // key being cancelled — otherwise the first character is swallowed.
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const event = new KeyboardEvent('keydown', {
      key: 'a',
      bubbles: true,
      composed: true,
      cancelable: true
    });
    sheet.getScrollContainer().dispatchEvent(event);

    expect(event.defaultPrevented).to.be.false;
    cleanup();
  });

  it('still cancels the key when the cell has no custom editor', async () => {
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const event = new KeyboardEvent('keydown', {
      key: 'a',
      bubbles: true,
      composed: true,
      cancelable: true
    });
    sheet.getScrollContainer().dispatchEvent(event);

    // The inline editor takes the character, so the browser must not also
    // type it somewhere.
    expect(event.defaultPrevented).to.be.true;
    cleanup();
  });

  it('gives a typed character to the custom editor too', async () => {
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'x', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(document.activeElement).to.equal(editor);
    cleanup();
  });

  it('still opens the inline editor on a cell with no custom editor', async () => {
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'F2', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(
      sheet.getCellGrid().shadowRoot!.querySelector('[part~="cell-input"]')
    ).to.not.be.null;
    cleanup();
  });

  it('opens the inline editor when the cell holds a component, not an editor', async () => {
    // A component in a cell is not an editor — the cell's value is still
    // edited the ordinary way.
    const widget = document.createElement('span');
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'F2', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(
      sheet.getCellGrid().shadowRoot!.querySelector('[part~="cell-input"]')
    ).to.not.be.null;
    cleanup();
  });

  it('leaves the selection alone when Enter is pressed in the editor', async () => {
    // The editor is a light child of the host and reaches the screen
    // through slots, so its keydown travels the flattened tree and lands
    // on the sheet's own listener. Treating it as a sheet shortcut moves
    // the selection down, which takes the editor off the sheet in the
    // middle of typing into it.
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    editor.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
    expect(editor.isConnected).to.be.true;
  });

  it('selects the cell when its editor is clicked', async () => {
    // An editor is usually wider than its cell, so a click aimed at the
    // cell often lands on the editor. The cell-grid's click delegation
    // cannot see it — the editor is projected in, not a descendant — so
    // without this the cell can never be selected with the mouse.
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col3 row4': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    editor.dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 4 });
    cleanup();
  });

  it('tells the server about a selection made through an editor', async () => {
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col3 row4': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    const events: any[] = [];
    host.addEventListener('spreadsheet-event', (e) =>
      events.push((e as CustomEvent).detail)
    );

    editor.dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, composed: true })
    );

    const selected = events.find((e) => e.type === 'cellSelected');
    expect(selected).to.not.be.undefined;
    expect(selected.data).to.include({ row: 4, column: 3 });
    cleanup();
  });

  it('reports the selection once, not once per path', async () => {
    // The editor is projected into its cell, so a pointer event on it
    // also travels through the cell in the flattened tree. If the
    // grid's own click delegation reports the cell as well, the server
    // sees the selection twice and re-runs onCustomEditorDisplayed for
    // every click.
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col3 row4': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    const selections: any[] = [];
    host.addEventListener('spreadsheet-event', (e) => {
      const d = (e as CustomEvent).detail;
      if (d.type === 'cellSelected') {
        selections.push(d.data);
      }
    });

    editor.dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, composed: true })
    );
    editor.dispatchEvent(
      new MouseEvent('mousedown', { bubbles: true, composed: true })
    );
    editor.dispatchEvent(
      new MouseEvent('click', { bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(selections).to.have.length(1);
    cleanup();
  });

  it('says nothing when the editor of the selected cell is clicked', async () => {
    // Re-reporting the selection the server already knows about would
    // bounce a needless round trip on every click into the editor.
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col3 row4': '77' });
    sheet.setSelectedCellAndRange('C4', 3, 4, 3, 3, 4, 4, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    const events: any[] = [];
    host.addEventListener('spreadsheet-event', (e) =>
      events.push((e as CustomEvent).detail)
    );

    editor.dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, composed: true })
    );

    expect(events.map((e) => e.type)).to.not.include('cellSelected');
    cleanup();
  });

  it('does not report a selection when the server focuses an editor', async () => {
    // The server focuses an editor when it displays one. Reporting that
    // back would move the selection to whatever the server was last
    // talking about, and the two would chase each other.
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col3 row4': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    const events: any[] = [];
    host.addEventListener('spreadsheet-event', (e) =>
      events.push((e as CustomEvent).detail)
    );

    editor.focus();

    expect(events.map((e) => e.type)).to.not.include('cellSelected');
    cleanup();
  });

  it('leaves the selection alone when an in-cell component is clicked', async () => {
    // Only editors stand in for their cell; a component in a cell is its
    // own thing.
    const widget = document.createElement('input');
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col3 row4' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    widget.dispatchEvent(
      new PointerEvent('pointerdown', { bubbles: true, composed: true })
    );

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 1, row: 1 });
    cleanup();
  });

  it('gives focus back to the sheet on Escape', async () => {
    // Escape is the way out of a custom editor. Without it the editor
    // keeps the keyboard and the sheet can never be driven again.
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    editor.focus();

    editor.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, composed: true })
    );

    expect(document.activeElement).to.not.equal(editor);
    // Escape leaves the selection where it is.
    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
    cleanup();
  });

  it('gives focus back and moves on for Tab', async () => {
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    editor.focus();

    editor.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Tab', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(document.activeElement).to.not.equal(editor);
    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 2 });
    cleanup();
  });

  // Enter in a custom editor gives the keyboard back to the sheet, once the
  // editor has had it: a text field has committed what was typed.

  /** An editor in B2, B2 selected and the editor focused. */
  async function typingInto(editor: HTMLElement) {
    const attached = attachSheet({ '77': editor });
    attached.sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    attached.sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    attached.sheet.recomputeViewport();
    await attached.sheet.getCellGrid().updateComplete;
    editor.focus();
    return attached;
  }

  function pressEnter(target: HTMLElement): void {
    target.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, composed: true, cancelable: true }));
  }

  it('gives focus back to the sheet on Enter, leaving the selection where it is', async () => {
    const editor = field();
    const { sheet, cleanup } = await typingInto(editor);

    pressEnter(editor);
    await sheet.getCellGrid().updateComplete;

    expect(document.activeElement).to.not.equal(editor);
    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
    cleanup();
  });

  it('keeps Enter in a text area, where it starts a new line', async () => {
    const editor = document.createElement('textarea');
    const { cleanup } = await typingInto(editor);

    pressEnter(editor);

    expect(document.activeElement).to.equal(editor);
    cleanup();
  });

  it('keeps Enter in rich text, where it starts a new line', async () => {
    const editor = document.createElement('div');
    editor.contentEditable = 'true';
    const { cleanup } = await typingInto(editor);

    pressEnter(editor);

    expect(document.activeElement).to.equal(editor);
    cleanup();
  });

  it('keeps an Enter that the editor used itself, as a list picks an item with it', async () => {
    const editor = field();
    editor.addEventListener('keydown', (e) => e.preventDefault());
    const { cleanup } = await typingInto(editor);

    pressEnter(editor);

    expect(document.activeElement).to.equal(editor);
    cleanup();
  });

  it('keeps other keys in the editor', async () => {
    const editor = field();
    const { cleanup } = await typingInto(editor);

    editor.dispatchEvent(new KeyboardEvent('keydown', { key: 'Delete', bubbles: true, composed: true }));

    expect(document.activeElement).to.equal(editor);
    cleanup();
  });

  it('keeps Enter inside an in-cell component', async () => {
    const widget = document.createElement('input');
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    widget.focus();

    pressEnter(widget);

    expect(document.activeElement).to.equal(widget);
    cleanup();
  });

  it('keeps Escape inside an in-cell component', async () => {
    // Only editors have a way out to give back — a component in a cell
    // is not something the sheet took the keyboard from.
    const widget = document.createElement('input');
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
    widget.focus();

    widget.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, composed: true })
    );

    expect(document.activeElement).to.equal(widget);
    cleanup();
  });

  it('leaves the selection alone for arrow keys in the editor', async () => {
    // Arrow keys move the caret inside a text field.
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    editor.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
    cleanup();
  });

  it('still moves the selection for a key pressed on the sheet', async () => {
    const editor = field();
    const { sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 3, row: 2 });
    cleanup();
  });

  it('leaves the sheet alone for a key inside an in-cell component', async () => {
    const widget = document.createElement('input');
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.setSelectedCellAndRange('B2', 2, 2, 2, 2, 2, 2, false);
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    widget.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'ArrowDown', bubbles: true, composed: true })
    );
    await sheet.getCellGrid().updateComplete;

    expect(sheet.getSelection().activeCell).to.deep.equal({ col: 2, row: 2 });
    cleanup();
  });

  it('releases the editor when the sheet is disposed', async () => {
    const editor = field();
    const { host, sheet, cleanup } = attachSheet({ '77': editor });
    sheet.setCellKeysToEditorIdMap({ 'col2 row2': '77' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    cleanup();

    expect(editor.parentElement).to.be.null;
  });
});

describe('Spreadsheet — the cell slots of a draw', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(10);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.setComponentIDtoCellKeysMap({ '7': 'col2 row2' });
    sheet.recomputeViewport();
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  it('keeps them from one draw to the next while the components stay in their cells', () => {
    const before = sheet.getCellGrid().renderState.cellSlots;

    sheet.getScrollContainer().scrollTop = 20;
    sheet.recomputeViewport();

    expect(sheet.getCellGrid().renderState.cellSlots).to.equal(before);
    expect(Object.keys(before)).to.eql(['col2 row2']);
  });

  it('works them out again when a component goes to another cell', () => {
    sheet.setComponentIDtoCellKeysMap({ '7': 'col3 row3' });
    sheet.recomputeViewport();

    expect(Object.keys(sheet.getCellGrid().renderState.cellSlots)).to.eql(['col3 row3']);
  });
});
