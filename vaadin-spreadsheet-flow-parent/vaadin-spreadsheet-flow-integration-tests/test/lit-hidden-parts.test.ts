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
 * The sheet's parts are hidden with the `hidden` attribute, and each keeps
 * itself out of sight then: its own `:host` display wins over the browser's
 * rule for the attribute, so without one of its own a hidden part showed
 * wherever the page had no global rule for it.
 */
describe('Spreadsheet — its parts while hidden', () => {
  const tags = [
    'vaadin-spreadsheet-internal-cell-grid',
    'vaadin-spreadsheet-internal-column-header',
    'vaadin-spreadsheet-internal-row-header',
    'vaadin-spreadsheet-internal-header-corner',
    'vaadin-spreadsheet-internal-formula-bar',
    'vaadin-spreadsheet-internal-sheet-tabs',
    'vaadin-spreadsheet-internal-context-menu'
  ];

  // The elements are defined by the connector's module.
  expect(Spreadsheet).to.exist;

  for (const tag of tags) {
    it(`keeps ${tag} out of sight`, async () => {
      const el = document.createElement(tag) as HTMLElement & { updateComplete: Promise<boolean> };
      document.body.appendChild(el);
      await el.updateComplete;
      const shown = getComputedStyle(el).display;

      el.hidden = true;

      expect([shown === 'none', getComputedStyle(el).display]).to.eql([false, 'none']);
      el.remove();
    });
  }

  it('hides the headers and their corner with the attribute when the headings are off', () => {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    const sheet = new Spreadsheet(host);
    sheet.setRows(10);
    sheet.setCols(10);
    sheet.setDisplayRowColHeadings(false);
    sheet.recomputeViewport();

    expect([sheet.getColumnHeader().hidden, sheet.getRowHeader().hidden, sheet.getHeaderCorner().hidden]).to.eql([
      true,
      true,
      true
    ]);
    sheet.dispose();
    host.remove();
  });
});
