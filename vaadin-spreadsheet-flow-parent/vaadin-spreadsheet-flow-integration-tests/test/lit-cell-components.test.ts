/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import {
  Spreadsheet,
  invertCellComponentMap,
  visibleCellComponentIds
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import { emptyCellGridRenderState } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';

/**
 * Components the server renders inside a cell.
 *
 * `Spreadsheet.setComponent` attaches the component as a Flow virtual child
 * and tells the client which cell it belongs to. The cell shows the
 * component instead of its own text.
 */

describe('invertCellComponentMap — node id keys to cell keys', () => {
  it('turns the server map round', () => {
    // The server keeps it as node id → cell key; rendering asks the
    // opposite question, once per visible cell.
    expect(invertCellComponentMap({ '31': 'col2 row3' })).to.deep.equal({
      'col2 row3': '31'
    });
  });

  it('handles an empty map', () => {
    expect(invertCellComponentMap({})).to.deep.equal({});
  });

  it('keeps every distinct cell', () => {
    expect(
      invertCellComponentMap({ '1': 'col1 row1', '2': 'col2 row2' })
    ).to.deep.equal({ 'col1 row1': '1', 'col2 row2': '2' });
  });

  it('lets the later component win a contested cell', () => {
    // One slot per cell, so only one can be shown either way.
    expect(
      invertCellComponentMap({ '1': 'col1 row1', '2': 'col1 row1' })
    ).to.deep.equal({ 'col1 row1': '2' });
  });

  it('drops an entry with no cell', () => {
    expect(invertCellComponentMap({ '1': '' })).to.deep.equal({});
  });
});

describe('visibleCellComponentIds — only what the viewport renders', () => {
  function stateWith(
    cellComponents: Record<string, string>,
    viewport: Partial<ReturnType<typeof emptyCellGridRenderState>['viewport']> = {}
  ) {
    const base = emptyCellGridRenderState();
    return {
      ...base,
      cellComponents,
      viewport: {
        ...base.viewport,
        firstColumnIndex: 1,
        lastColumnIndex: 3,
        firstRowIndex: 1,
        lastRowIndex: 3,
        ...viewport
      }
    };
  }

  it('returns the component in a visible cell', () => {
    expect(
      visibleCellComponentIds(stateWith({ 'col2 row2': '31' }))
    ).to.deep.equal(['31']);
  });

  it('skips a component scrolled out of view', () => {
    // No slot is rendered for it, so parenting it would take it out of the
    // document for nothing.
    expect(
      visibleCellComponentIds(stateWith({ 'col9 row9': '31' }))
    ).to.deep.equal([]);
  });

  it('returns nothing when no cell has a component', () => {
    expect(visibleCellComponentIds(stateWith({}))).to.deep.equal([]);
  });

  it('returns every visible component', () => {
    const ids = visibleCellComponentIds(
      stateWith({ 'col1 row1': 'a', 'col3 row3': 'b', 'col9 row1': 'c' })
    );
    expect(ids.sort()).to.deep.equal(['a', 'b']);
  });
});

describe('Spreadsheet — components inside cells', () => {
  const originalVaadin = (window as any).Vaadin;

  function attachSheet(components: Record<string, HTMLElement>): {
    sheet: Spreadsheet;
    cleanup: () => void;
  } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;';
    (host as any).appId = 'ROOT';
    document.body.appendChild(host);
    (window as any).Vaadin = {
      Flow: {
        clients: { ROOT: { getByNodeId: (n: number) => components[String(n)] ?? null } }
      }
    };
    const sheet = new Spreadsheet(host);
    sheet.setRows(50);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(50).fill(15));
    return {
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
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    const cell = sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>('.cell.col2.row2')!;
    const slot = cell.querySelector('slot')!;
    expect(slot.getAttribute('name')).to.equal('custom-component-31');
    const assigned = slot.assignedElements();
    expect(assigned).to.have.length(1);
    expect(assigned[0]).to.equal(widget);
    cleanup();
  });

  it('shows the component instead of the cell value', async () => {
    // The component is what the cell is for; rendering both would stack
    // them on top of each other.
    const { sheet, cleanup } = attachSheet({ '31': button('Click me') });
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
    const { sheet, cleanup } = attachSheet({ '31': button('Click me') });
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

  it('accepts the JSON-serialized map the server sends', async () => {
    const widget = button('Click me');
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap(
      JSON.stringify({ '31': 'col2 row2' }) as any
    );
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(widget.getAttribute('slot')).to.equal('custom-component-31');
    cleanup();
  });

  it('takes the component back when the server removes it', async () => {
    const widget = button('Click me');
    const { sheet, cleanup } = attachSheet({ '31': widget });
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
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    let moved = false;
    new MutationObserver(() => {
      moved = true;
    }).observe(sheet.getCellGrid(), { childList: true });

    sheet.recomputeViewport();
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    expect(moved).to.be.false;
    cleanup();
  });

  it('releases the component when the sheet is disposed', async () => {
    const widget = button('Click me');
    const { sheet, cleanup } = attachSheet({ '31': widget });
    sheet.setComponentIDtoCellKeysMap({ '31': 'col2 row2' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;

    cleanup();

    expect(widget.parentElement).to.be.null;
  });
});
