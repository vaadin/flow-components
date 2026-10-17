/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { SheetData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetData.js';
import { ViewState } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/ViewState.js';
import { attachSheet, serverBatch } from './spreadsheet-harness.js';

/**
 * What the client knows of the sheet the server shows, which a sheet shown
 * anew starts over, and what the view remembers between drawings.
 */
describe('Spreadsheet — what it knows of the sheet shown', () => {
  it('starts knowing nothing of it', () => {
    const data = new SheetData();

    expect([data.cells.size, data.serverSelectedCell, data.viewportAnnounced]).to.eql([0, null, false]);
  });

  it('gives each sheet shown cells of its own', () => {
    expect(new SheetData().cells).to.not.equal(new SheetData().cells);
  });

  it('forgets the cells and the cell the server selected when the server shows the sheet anew', () => {
    const ctx = attachSheet();
    ctx.sheet.updateBottomRightCellValues([
      { col: 2, row: 2, value: 'x', cellStyle: 'cs0', locked: false, needsMeasure: false, isPercentage: false }
    ]);
    ctx.sheet.setSelectedCellAndRange('', 2, 2, 2, 2, 2, 2, false);

    serverBatch(ctx, { reload: true });
    ctx.sent.length = 0;
    // From the cell the server no longer holds: a range of its own.
    ctx.sheet
      .getCellGrid()
      .dispatchEvent(
        new CustomEvent('cell-click', { detail: { col: 3, row: 3, shiftKey: true }, bubbles: true, composed: true })
      );

    expect(ctx.sheet.getCellData().size, 'cells').to.equal(0);
    expect(ctx.types(), 'selected cell').to.eql(['cellRangePainted']);
    ctx.cleanup();
  });
});

describe('Spreadsheet — what the view remembers between drawings', () => {
  it('starts with nothing to restore, keep in place or measure', () => {
    const view = new ViewState();

    expect(view).to.deep.include({
      pendingSheetScroll: null,
      rowAnchor: null,
      anchoredScrollTop: null,
      shownSheetName: null,
      styleWidthRatiosStale: false,
      pendingRatioReport: null
    });
    expect(view.pagedViewParts).to.eql({
      corner: { width: 0, height: 0 },
      frozen: { width: 0, columns: 0, height: 0, rows: 0 }
    });
    expect(view.measuredStyleIndexes).to.eql(new Set());
  });
});
