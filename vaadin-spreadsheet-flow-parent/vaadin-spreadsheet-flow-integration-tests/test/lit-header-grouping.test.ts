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
  COLUMN_GROUPING_LEVEL_PX,
  columnGroupingGutterPx,
  GROUPING_BUTTON_SIZE_PX,
  ROW_GROUPING_LEVEL_PX,
  rowGroupingGutterPx
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/headers/Grouping.js';

/**
 * The outline gutters, in the GWT client's sizes: 18px column levels after a
 * 3px margin, 15px row levels after a pixel, and one level more for the
 * button that opens every group - while the headings show.
 */
describe('outline gutter sizes', () => {
  it('are a level of 18 pixels across the columns and of 15 down the rows, with a button of 12', () => {
    expect([COLUMN_GROUPING_LEVEL_PX, ROW_GROUPING_LEVEL_PX, GROUPING_BUTTON_SIZE_PX]).to.eql([18, 15, 12]);
  });

  it('is nothing for a sheet without groups', () => {
    expect(columnGroupingGutterPx(0, true)).to.equal(0);
    expect(rowGroupingGutterPx(0, true)).to.equal(0);
  });

  it('has a level per depth and one more with headings', () => {
    expect(columnGroupingGutterPx(2, true)).to.equal(3 + 3 * 18);
    expect(rowGroupingGutterPx(2, true)).to.equal(1 + 3 * 15);
  });

  it('has a level per depth only without headings', () => {
    expect(columnGroupingGutterPx(2, false)).to.equal(3 + 2 * 18);
    expect(rowGroupingGutterPx(2, false)).to.equal(1 + 2 * 15);
  });
});
