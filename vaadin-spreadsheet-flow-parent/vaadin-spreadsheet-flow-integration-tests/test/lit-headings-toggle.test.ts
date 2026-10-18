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

/**
 * Spreadsheet.setRowColHeadingsVisible while the sheet is up. Turned off,
 * the strips kept the size they were last given and stayed on screen.
 */
describe('Spreadsheet — turning the headings off and on', () => {
  let sheet: Spreadsheet;
  let host: HTMLElement;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:600px;height:400px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(10);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.recomputeViewport();
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  /** The server's batch of properties ends, as the wrapper ends it. */
  function endBatch(names: string[]): void {
    sheet.notifyStateChanges(names, false);
  }

  it('shows both strips to begin with', () => {
    expect(sheet.getColumnHeader().getBoundingClientRect().height).to.equal(27);
    expect(sheet.getRowHeader().getBoundingClientRect().width).to.equal(50);
  });

  it('starts the cells right at the headers when the sheet is smaller than its view', () => {
    // Ten 20px rows and ten 50px columns leave room over on both axes.
    expect(sheet.getCellGrid().offsetTop).to.equal(27);
    expect(sheet.getCellGrid().offsetLeft).to.equal(50);
  });

  it('collapses both strips when the headings are turned off', () => {
    sheet.setDisplayRowColHeadings(false);
    endBatch(['displayRowColHeadings']);

    expect(sheet.getColumnHeader().getBoundingClientRect().height).to.equal(0);
    expect(sheet.getRowHeader().getBoundingClientRect().width).to.equal(0);
    expect(sheet.getCellGrid().offsetTop).to.equal(0);
    expect(sheet.getCellGrid().offsetLeft).to.equal(0);
  });

  it('starts the cells at the top when the headings are off and only rows are grouped', () => {
    // The row outline gutter stays; nothing is over the cells, and the
    // corner, with no height of its own, took a 1px row with its border.
    sheet.setRowGroupingMax(1);
    sheet.setRowGroupingData([{ uniqueIndex: 1, level: 1, startIndex: 1, endIndex: 3, collapsed: false }]);
    sheet.setDisplayRowColHeadings(false);
    endBatch(['displayRowColHeadings', 'rowGroupingMax', 'rowGroupingData']);

    expect(sheet.getCellGrid().offsetTop).to.equal(0);
    expect(sheet.getRowHeader().getBoundingClientRect().width).to.be.greaterThan(0);
    expect(sheet.getCellGrid().offsetLeft).to.equal(sheet.getRowHeader().getBoundingClientRect().width);
  });

  it('starts the cells at the left when the headings are off and only columns are grouped', () => {
    sheet.setColGroupingMax(1);
    sheet.setColGroupingData([{ uniqueIndex: 1, level: 1, startIndex: 1, endIndex: 3, collapsed: false }]);
    sheet.setDisplayRowColHeadings(false);
    endBatch(['displayRowColHeadings', 'colGroupingMax', 'colGroupingData']);

    expect(sheet.getCellGrid().offsetLeft).to.equal(0);
    expect(sheet.getColumnHeader().getBoundingClientRect().height).to.be.greaterThan(0);
    expect(sheet.getCellGrid().offsetTop).to.equal(sheet.getColumnHeader().getBoundingClientRect().height);
  });

  it('brings them back at their default sizes when turned on again', () => {
    sheet.setDisplayRowColHeadings(false);
    endBatch(['displayRowColHeadings']);
    sheet.setDisplayRowColHeadings(true);
    endBatch(['displayRowColHeadings']);

    expect(sheet.getColumnHeader().getBoundingClientRect().height).to.equal(27);
    expect(sheet.getRowHeader().getBoundingClientRect().width).to.equal(50);
  });

  it('draws a batch that turns them off once, at its end, with everything else in it', () => {
    // Drawn halfway through, the sheet was measured with the widths the
    // batch had not set yet, and its viewport told to the server so.
    let draws = 0;
    const draw = sheet.recomputeViewport.bind(sheet);
    sheet.recomputeViewport = () => {
      draws++;
      draw();
    };

    sheet.setDisplayRowColHeadings(false);
    sheet.setColW(new Array(10).fill(80));
    endBatch(['displayRowColHeadings', 'colW']);

    expect(draws).to.equal(1);
    expect(sheet.getCellGrid().offsetLeft).to.equal(0);
  });
});
