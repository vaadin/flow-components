/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { attachSheet, rendered, serverBatch, type AttachedSheet } from './spreadsheet-harness.js';

/**
 * The styles the server sends for whole rows and columns, for conditional
 * formats and for the borders cells draw on others, as the cells wear them
 * once the server's properties have reached the sheet.
 */
describe('Spreadsheet — the styles the server sends, on the cells', () => {
  let ctx: AttachedSheet;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => ctx.cleanup());

  /** B2 with a value and the given style classes, and the properties that come with it. */
  async function sendB2(cellStyle: string, properties: Record<string, unknown>): Promise<CSSStyleDeclaration> {
    ctx.sheet.updateBottomRightCellValues([
      { col: 2, row: 2, value: 'x', cellStyle, locked: false, needsMeasure: false, isPercentage: false }
    ]);
    serverBatch(ctx, {
      cellStyleToCSSStyle: { 0: 'color: rgb(0, 0, 0);', 1: 'color: rgb(0, 128, 0);' },
      ...properties
    });
    await rendered(ctx.sheet);
    return getComputedStyle(ctx.sheet.getCellGrid().shadowRoot!.querySelector('.cell.col2.row2')!);
  }

  it('dresses a cell with no style of its own in the style of its row', async () => {
    const style = await sendB2('cs0', { rowIndexToStyleIndex: { 2: 1 } });

    expect(style.color).to.equal('rgb(0, 128, 0)');
  });

  it('dresses a cell with no style of its own in the style of its column', async () => {
    const style = await sendB2('cs0', { columnIndexToStyleIndex: { 2: 1 } });

    expect(style.color).to.equal('rgb(0, 128, 0)');
  });

  it('shows a conditional format the cell matches over its own style', async () => {
    const style = await sendB2('cs1 cf3', { conditionalFormattingStyles: { 3: 'color: rgb(0, 0, 255);' } });

    expect(style.color).to.equal('rgb(0, 0, 255)');
  });

  it('draws a border another cell draws on it', async () => {
    const style = await sendB2('cs0', {
      shiftedCellBorderStyles: ['.col2.row2 { border-right: 2px solid rgb(255, 0, 0); }']
    });

    expect([style.borderRightWidth, style.borderRightColor]).to.eql(['2px', 'rgb(255, 0, 0)']);
  });
});
