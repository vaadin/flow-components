/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import {
  cellGridLumoStyles,
  cellGridStyles
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/CellGridStyles.js';
import {
  type CellGridRenderState,
  emptyCellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';

/**
 * The cell grid's stylesheet, on its own: the rules written once for
 * several things - the selection's sides, the two corner markers - and the
 * Lumo rules coming after the base ones.
 */
describe('CellGridStyles', () => {
  let grid: SpreadsheetCellGrid;

  /** Three columns of 60 pixels and three rows of 20, B2:C3 selected, a note on A1 and a bad formula on A2. */
  function sheet(): CellGridRenderState {
    const empty = emptyCellGridRenderState();
    return {
      ...empty,
      viewport: { ...empty.viewport, firstColumnIndex: 1, lastColumnIndex: 3, firstRowIndex: 1, lastRowIndex: 3 },
      colX: [0, 60, 120],
      colWidths: [60, 60, 60],
      rowY: [0, 20, 40],
      rowHeights: [20, 20, 20],
      totalGridWidthPx: 180,
      totalGridHeightPx: 60,
      commentCells: new Set(['col1 row1']),
      invalidFormulaCells: new Set(['col1 row2']),
      selection: { activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, c2: 3, r1: 2, r2: 3 }, auxRanges: [] }
    };
  }

  const inGrid = (selector: string): HTMLElement => grid.shadowRoot!.querySelector<HTMLElement>(selector)!;

  beforeEach(async () => {
    grid = new SpreadsheetCellGrid();
    grid.renderState = sheet();
    document.body.append(grid);
    await grid.updateComplete;
  });

  afterEach(() => {
    grid.remove();
  });

  it('are the base rules and then the Lumo ones', () => {
    const styles = SpreadsheetCellGrid.styles as unknown[];

    expect(styles).to.include(cellGridStyles);
    expect(styles.indexOf(cellGridLumoStyles)).to.equal(styles.indexOf(cellGridStyles) + 1);
  });

  it("draw the selection's four sides two pixels wide along its edges, in the selection colour, over the cells", () => {
    const outline = inGrid('.sheet-selection');
    const { width, height } = outline.getBoundingClientRect();
    const box = (side: string) => {
      const el = inGrid(`.sheet-selection .${side}`);
      return [el.offsetLeft, el.offsetTop, el.offsetWidth, el.offsetHeight];
    };

    expect(box('s-top')).to.eql([0, 0, width, 2]);
    expect(box('s-bottom')).to.eql([0, height - 2, width, 2]);
    expect(box('s-left')).to.eql([0, 0, 2, height]);
    expect(box('s-right')).to.eql([width - 2, 0, 2, height]);
    for (const side of ['s-top', 's-bottom', 's-left', 's-right']) {
      const style = getComputedStyle(inGrid(`.sheet-selection .${side}`));
      expect([style.position, style.backgroundColor, style.zIndex], side).to.eql([
        'absolute',
        'rgb(25, 125, 225)',
        '2'
      ]);
    }
  });

  it('put the selection in one layer over the cells that the pointer goes through', () => {
    const style = getComputedStyle(inGrid('.sheet-selection'));

    expect([style.position, style.zIndex, style.pointerEvents]).to.eql(['absolute', '2', 'none']);
  });

  it('draw both corner markers as the same small triangle in the top right, a note amber and a bad formula red', () => {
    for (const [selector, colour] of [
      ['.comment-indicator', 'rgb(255, 207, 22)'],
      ['.invalidformula-indicator', 'rgb(237, 71, 59)']
    ]) {
      const marker = inGrid(selector);
      const style = getComputedStyle(marker);
      const cell = marker.parentElement!;

      expect([marker.offsetWidth, marker.offsetHeight], selector).to.eql([9, 9]);
      expect(marker.offsetLeft + 9, selector).to.equal(cell.clientWidth);
      expect([style.top, style.clipPath, style.zIndex, style.pointerEvents], selector).to.eql([
        '0px',
        'polygon(0px 0px, 100% 0px, 100% 100%)',
        '5',
        'none'
      ]);
      expect(style.backgroundColor, selector).to.equal(colour);
    }
  });

  it("let a Lumo rule as specific as a base one win, coming after it", async () => {
    grid.style.setProperty('--lumo-space-xs', '7px');
    expect(getComputedStyle(inGrid('.cell.col1.row1')).paddingLeft, 'without Lumo').to.equal('2px');

    grid.setAttribute('theme', 'lumo');
    await grid.updateComplete;

    expect(getComputedStyle(inGrid('.cell.col1.row1')).paddingLeft).to.equal('7px');
  });
});
