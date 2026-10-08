/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

// Imported for its side effect too: it defines the internal elements.
import '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import {
  emptyCellGridRenderState,
  type CellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * A workbook's styles reach the page as CSS rules, and a workbook names its
 * own fonts: the server writes a font's name into its rule as it is. The
 * Lit grid put the rules into its style block as HTML, so a font named
 * `</style><img src=x onerror=...>` closed the block and ran its script.
 * The GWT client inserted each rule through the CSSOM, which reads no
 * markup.
 */
describe('SpreadsheetCellGrid — markup in a workbook style', () => {
  const FLAG = '__spreadsheetStyleInjection';
  let grid: SpreadsheetCellGrid;

  async function render(styles: Record<number, string>): Promise<void> {
    grid = new SpreadsheetCellGrid();
    const cell: CellData = {
      row: 1,
      col: 1,
      value: 'x',
      cellStyle: 'cs1',
      locked: false,
      needsMeasure: false,
      isPercentage: false
    };
    grid.renderState = {
      ...emptyCellGridRenderState(),
      viewport: {
        firstColumnIndex: 1,
        firstColumnPosition: 0,
        lastColumnIndex: 1,
        lastColumnPosition: 50,
        firstRowIndex: 1,
        firstRowPosition: 0,
        lastRowIndex: 1,
        lastRowPosition: 20
      },
      colX: [0],
      rowY: [0],
      colWidths: [50],
      rowHeights: [20],
      cellData: new Map([['col1 row1', cell]]),
      cellStyleToCSSStyle: styles
    } as CellGridRenderState;
    document.body.appendChild(grid);
    await grid.updateComplete;
  }

  afterEach(() => {
    grid.remove();
    delete (window as any)[FLAG];
  });

  it('keeps it text, so it runs nothing', async () => {
    const font = `X</style><img src=x onerror="window.${FLAG} = 1">`;
    await render({ 0: 'color: black;', 1: `font-family: var(--default-font-family);--default-font-family:${font},swiss;` });
    // Long enough for a broken image to report its error.
    await new Promise((resolve) => setTimeout(resolve, 300));

    expect(grid.shadowRoot!.querySelector('img')).to.be.null;
    expect((window as any)[FLAG]).to.be.undefined;
    const css = [...grid.shadowRoot!.querySelectorAll('style')].map((s) => s.textContent).join('\n');
    expect(css).to.contain(font);
  });

  it('still styles the cells', async () => {
    await render({ 0: 'color: black;', 1: 'font-size: 20pt;' });

    const cell = grid.shadowRoot!.querySelector<HTMLElement>('.cell.col1.row1')!;
    expect(getComputedStyle(cell).fontSize).to.equal('26.6667px');
  });
});
