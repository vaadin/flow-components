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
  emptyColumnHeaderRenderState,
  SpreadsheetColumnHeader
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetColumnHeader.js';
import {
  emptyRowHeaderRenderState,
  SpreadsheetRowHeader
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetRowHeader.js';
import { headerStripStyles } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/headers/HeaderStripStyles.js';

/**
 * The look the column letters and the row numbers share, on its own: one
 * stylesheet under each header's own rules, giving both the same grouping
 * buttons, frozen strip and selected labels.
 */
describe('HeaderStripStyles', () => {
  const GROUP = { uniqueIndex: 1, level: 1, startPx: 0, endPx: 40, buttonPx: 60, collapsed: false };
  let letters: SpreadsheetColumnHeader;
  let numbers: SpreadsheetRowHeader;

  const inside = (header: HTMLElement, selector: string): CSSStyleDeclaration =>
    getComputedStyle(header.shadowRoot!.querySelector(selector)!);

  beforeEach(async () => {
    const viewport = { ...emptyColumnHeaderRenderState().viewport, lastColumnIndex: 2, lastRowIndex: 2 };
    letters = new SpreadsheetColumnHeader();
    letters.renderState = {
      ...emptyColumnHeaderRenderState(),
      viewport,
      colX: [50, 100],
      colWidths: [50, 50],
      headerHeight: 27,
      totalGridWidthPx: 150,
      horizontalSplitPosition: 1,
      frozenColX: [0],
      frozenColWidths: [50],
      colGroupings: [GROUP],
      colGroupingMax: 1,
      selectedColumns: new Set([2])
    };
    numbers = new SpreadsheetRowHeader();
    numbers.renderState = {
      ...emptyRowHeaderRenderState(),
      viewport,
      rowY: [20, 40],
      rowHeights: [20, 20],
      headerWidth: 70,
      totalGridHeightPx: 60,
      verticalSplitPosition: 1,
      frozenRowY: [0],
      frozenRowHeights: [20],
      rowGroupings: [GROUP],
      rowGroupingMax: 1,
      selectedRows: new Set([2])
    };
    document.body.append(letters, numbers);
    await Promise.all([letters.updateComplete, numbers.updateComplete]);
  });

  afterEach(() => {
    letters.remove();
    numbers.remove();
  });

  it("comes first in both headers, under each one's own rules", () => {
    for (const styles of [SpreadsheetColumnHeader.styles, SpreadsheetRowHeader.styles] as unknown[][]) {
      expect(styles[0]).to.equal(headerStripStyles);
      expect(styles).to.have.length(2);
    }
  });

  it('draws the same round grouping button in both', () => {
    for (const [header, selector] of [
      [letters, '.col-grouping-toggle'],
      [numbers, '.row-grouping-toggle']
    ] as const) {
      const toggle = inside(header, selector);
      expect([toggle.width, toggle.height, toggle.borderRadius, toggle.color, toggle.lineHeight], selector).to.eql([
        '12px',
        '12px',
        '50%',
        'rgb(255, 255, 255)',
        '11px'
      ]);
    }
  });

  it('lays the frozen strip over the scrolling labels in both, letting the pointer through but to its labels', () => {
    for (const header of [letters, numbers]) {
      const strip = inside(header, '.frozen-header-strip');
      expect([strip.position, strip.zIndex, strip.pointerEvents]).to.eql(['absolute', '2', 'none']);
      expect(inside(header, '.frozen-header-strip > [part]').pointerEvents).to.equal('auto');
    }
  });

  it('tints the selected labels alike, each marked along its own edge', () => {
    const letter = inside(letters, '[part~="header-selected"]');
    const number = inside(numbers, '[part~="header-selected"]');

    // The documented default of --vaadin-spreadsheet-header-selected-bg.
    expect([letter.backgroundColor, number.backgroundColor]).to.eql(['rgb(230, 237, 244)', 'rgb(230, 237, 244)']);
    expect([letter.borderBottomWidth, number.borderRightWidth]).to.eql(['2px', '2px']);
  });

  it('sticks each header to its own edge, as a block in the header font', () => {
    const [col, row] = [getComputedStyle(letters), getComputedStyle(numbers)];

    expect([col.position, col.display, row.position, row.display]).to.eql(['sticky', 'block', 'sticky', 'block']);
    expect(col.fontFamily).to.equal(row.fontFamily);
    expect([col.top, row.left]).to.eql(['0px', '0px']);
  });
});
