/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

import '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import type { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import {
  type CellGridRenderState,
  emptyCellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';
import {
  type ColumnHeaderRenderState,
  emptyColumnHeaderRenderState,
  type SpreadsheetColumnHeader
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetColumnHeader.js';
import {
  emptyRowHeaderRenderState,
  type RowHeaderRenderState,
  type SpreadsheetRowHeader
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetRowHeader.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * The lists a sheet scrolls through - its rows and cells, its pictures, its
 * column letters and row numbers - keep the element of each item still in
 * view, keyed, as the GWT client moved what it had already drawn.
 */

const COLS = 10;
const ROWS = 10;

function cell(col: number, row: number): CellData {
  return { row, col, value: `${col}:${row}`, cellStyle: 'cs0', locked: false, needsMeasure: false, isPercentage: false };
}

/** The grid showing ROWS rows and COLS columns from the given ones, each 50 x 20 pixels. */
function gridAt(firstCol: number, firstRow: number, extra: Partial<CellGridRenderState> = {}): CellGridRenderState {
  const cellData = new Map<string, CellData>();
  for (let row = firstRow; row < firstRow + ROWS; row++) {
    for (let col = firstCol; col < firstCol + COLS; col++) {
      cellData.set(`col${col} row${row}`, cell(col, row));
    }
  }
  const lastCol = firstCol + COLS - 1;
  const lastRow = firstRow + ROWS - 1;
  return {
    ...emptyCellGridRenderState(),
    viewport: {
      firstColumnIndex: firstCol,
      firstColumnPosition: (firstCol - 1) * 50,
      lastColumnIndex: lastCol,
      lastColumnPosition: lastCol * 50,
      firstRowIndex: firstRow,
      firstRowPosition: (firstRow - 1) * 20,
      lastRowIndex: lastRow,
      lastRowPosition: lastRow * 20
    },
    cellData,
    colX: Array.from({ length: lastCol }, (_, i) => i * 50),
    rowY: Array.from({ length: lastRow }, (_, i) => i * 20),
    colWidths: new Array(lastCol).fill(50),
    rowHeights: new Array(lastRow).fill(20),
    ...extra
  };
}

function picture(id: string, col: number): NonNullable<CellGridRenderState['overlays']>[number] {
  return {
    id,
    col,
    row: 1,
    type: 'IMAGE',
    src: 'data:image/gif;base64,R0lGODlhAQABAAAAACw=',
    leftPx: (col - 1) * 50,
    topPx: 0,
    widthPx: 10,
    heightPx: 10
  };
}

function lettersAt(first: number): ColumnHeaderRenderState {
  return {
    ...emptyColumnHeaderRenderState(),
    viewport: {
      firstColumnIndex: first,
      firstColumnPosition: (first - 1) * 50,
      lastColumnIndex: first + COLS - 1,
      lastColumnPosition: 0,
      firstRowIndex: 1,
      firstRowPosition: 0,
      lastRowIndex: 0,
      lastRowPosition: 0
    },
    colX: Array.from({ length: COLS }, (_, i) => (first - 1 + i) * 50),
    colWidths: new Array(COLS).fill(50),
    headerHeight: 24
  };
}

function numbersAt(first: number): RowHeaderRenderState {
  return {
    ...emptyRowHeaderRenderState(),
    viewport: {
      firstColumnIndex: 1,
      firstColumnPosition: 0,
      lastColumnIndex: 0,
      lastColumnPosition: 0,
      firstRowIndex: first,
      firstRowPosition: (first - 1) * 20,
      lastRowIndex: first + ROWS - 1,
      lastRowPosition: 0
    },
    rowY: Array.from({ length: ROWS }, (_, i) => (first - 1 + i) * 20),
    rowHeights: new Array(ROWS).fill(20),
    headerWidth: 40
  };
}

async function attach<E extends HTMLElement & { renderState: unknown; updateComplete: Promise<boolean> }>(
  tag: string,
  state: unknown
): Promise<E> {
  const el = document.createElement(tag) as E;
  el.renderState = state;
  document.body.appendChild(el);
  await el.updateComplete;
  return el;
}

async function show<E extends { renderState: unknown; updateComplete: Promise<boolean> }>(el: E, state: unknown): Promise<void> {
  el.renderState = state;
  await el.updateComplete;
}

describe('Scrolled lists keep the elements still in view', () => {
  it('keeps a cell still in view as the sheet scrolls sideways', async () => {
    const grid = await attach<SpreadsheetCellGrid>('vaadin-spreadsheet-internal-cell-grid', gridAt(1, 1));
    const before = grid.shadowRoot!.querySelector('.cell.col5.row3');

    await show(grid, gridAt(3, 1));

    expect(grid.shadowRoot!.querySelector('.cell.col5.row3')).to.equal(before);
    grid.remove();
  });

  it('keeps a picture that stays when another goes', async () => {
    const grid = await attach<SpreadsheetCellGrid>(
      'vaadin-spreadsheet-internal-cell-grid',
      gridAt(1, 1, { overlays: [picture('a', 1), picture('b', 3)] })
    );
    const b = grid.shadowRoot!.querySelector('[data-overlay-id="b"]');

    await show(grid, gridAt(1, 1, { overlays: [picture('b', 3)] }));

    expect(grid.shadowRoot!.querySelector('[data-overlay-id="b"]')).to.equal(b);
    grid.remove();
  });

  it('keeps a column letter and its resize handle as the sheet scrolls sideways', async () => {
    const header = await attach<SpreadsheetColumnHeader>('vaadin-spreadsheet-internal-column-header', lettersAt(1));
    const letter = header.shadowRoot!.querySelector('.col-header-cell.col5');
    const handle = header.shadowRoot!.querySelector('.col-resize-handle[data-col="5"]');

    await show(header, lettersAt(3));

    expect(header.shadowRoot!.querySelector('.col-header-cell.col5')).to.equal(letter);
    expect(header.shadowRoot!.querySelector('.col-resize-handle[data-col="5"]')).to.equal(handle);
    header.remove();
  });

  it('keeps a row number and its resize handle as the sheet scrolls down', async () => {
    const header = await attach<SpreadsheetRowHeader>('vaadin-spreadsheet-internal-row-header', numbersAt(1));
    const number = header.shadowRoot!.querySelector('.row-header-cell.row5');
    const handle = header.shadowRoot!.querySelector('.row-resize-handle[data-row="5"]');

    await show(header, numbersAt(3));

    expect(header.shadowRoot!.querySelector('.row-header-cell.row5')).to.equal(number);
    expect(header.shadowRoot!.querySelector('.row-resize-handle[data-row="5"]')).to.equal(handle);
    header.remove();
  });
});

/** Every comment node in an element's shadow tree: lit-html's markers among them. */
function markers(el: HTMLElement): number {
  let count = 0;
  const walker = document.createTreeWalker(el.shadowRoot!, NodeFilter.SHOW_COMMENT);
  while (walker.nextNode()) {
    count++;
  }
  return count;
}

/**
 * Since lit-html 3.3.1, `repeat` leaves a comment behind for each item it
 * drops (https://github.com/lit/lit/issues/5298): every row, cell and header
 * scrolled past stayed in the page as one, without bound.
 */
describe('Scrolled lists leave nothing behind', () => {
  async function roundTrips<E extends HTMLElement & { renderState: unknown; updateComplete: Promise<boolean> }>(
    el: E,
    there: unknown,
    back: unknown
  ): Promise<void> {
    for (let i = 0; i < 5; i++) {
      await show(el, there);
      await show(el, back);
    }
  }

  it('as rows scroll past', async () => {
    const grid = await attach<SpreadsheetCellGrid>('vaadin-spreadsheet-internal-cell-grid', gridAt(1, 1));
    const before = markers(grid);

    await roundTrips(grid, gridAt(1, 8), gridAt(1, 1));

    expect(markers(grid)).to.equal(before);
    grid.remove();
  });

  it('as columns scroll past', async () => {
    const grid = await attach<SpreadsheetCellGrid>('vaadin-spreadsheet-internal-cell-grid', gridAt(1, 1));
    const before = markers(grid);

    await roundTrips(grid, gridAt(8, 1), gridAt(1, 1));

    expect(markers(grid)).to.equal(before);
    grid.remove();
  });

  it('as pictures come and go', async () => {
    const both = gridAt(1, 1, { overlays: [picture('a', 1), picture('b', 3)] });
    const grid = await attach<SpreadsheetCellGrid>('vaadin-spreadsheet-internal-cell-grid', both);
    const before = markers(grid);

    await roundTrips(grid, { ...both, overlays: [picture('b', 3)] }, both);

    expect(markers(grid)).to.equal(before);
    grid.remove();
  });

  it('as column letters scroll past', async () => {
    const header = await attach<SpreadsheetColumnHeader>('vaadin-spreadsheet-internal-column-header', lettersAt(1));
    const before = markers(header);

    await roundTrips(header, lettersAt(8), lettersAt(1));

    expect(markers(header)).to.equal(before);
    header.remove();
  });

  it('as row numbers scroll past', async () => {
    const header = await attach<SpreadsheetRowHeader>('vaadin-spreadsheet-internal-row-header', numbersAt(1));
    const before = markers(header);

    await roundTrips(header, numbersAt(8), numbersAt(1));

    expect(markers(header)).to.equal(before);
    header.remove();
  });
});
