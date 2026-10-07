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

    expect(sheet.getColumnHeader().getBoundingClientRect().height).to.equal(0);
    expect(sheet.getRowHeader().getBoundingClientRect().width).to.equal(0);
    expect(sheet.getCellGrid().offsetTop).to.equal(0);
    expect(sheet.getCellGrid().offsetLeft).to.equal(0);
  });

  it('brings them back at their default sizes when turned on again', () => {
    sheet.setDisplayRowColHeadings(false);
    sheet.setDisplayRowColHeadings(true);

    expect(sheet.getColumnHeader().getBoundingClientRect().height).to.equal(27);
    expect(sheet.getRowHeader().getBoundingClientRect().width).to.equal(50);
  });
});
