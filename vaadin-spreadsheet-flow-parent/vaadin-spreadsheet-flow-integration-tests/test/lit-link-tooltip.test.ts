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
import { linkTooltipPosition } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/Placement.js';
import { spreadsheetStyles } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet-styles.js';

/**
 * A link cell's tooltip: the target the server names for it, shown while
 * the pointer is over the cell, as the GWT client showed it.
 */
describe('linkTooltipPosition — the GWT client rule', () => {
  const cell = { left: 100, top: 100, right: 150, bottom: 120 };
  const view = { width: 1000, height: 800 };

  it('hangs below the cell, from its left edge', () => {
    expect(linkTooltipPosition(cell, { width: 200, height: 30 }, view)).to.eql({ left: 100, top: 120 });
  });

  it("lines up with the cell's right edge when it would run off the right", () => {
    const nearRight = { left: 900, top: 100, right: 950, bottom: 120 };
    expect(linkTooltipPosition(nearRight, { width: 200, height: 30 }, view)).to.eql({ left: 750, top: 120 });
  });

  it('stays left-aligned when it fits neither way', () => {
    const narrowView = { width: 220, height: 800 };
    expect(linkTooltipPosition(cell, { width: 200, height: 30 }, narrowView).left).to.equal(100);
  });

  it('goes above the cell when there is no room below', () => {
    const nearBottom = { left: 100, top: 780, right: 150, bottom: 800 };
    expect(linkTooltipPosition(nearBottom, { width: 200, height: 30 }, view)).to.eql({ left: 100, top: 750 });
  });

  it('stays below when there is no room above either', () => {
    const tallTooltip = { width: 200, height: 790 };
    expect(linkTooltipPosition(cell, tallTooltip, view).top).to.equal(120);
  });
});

describe('Spreadsheet — the tooltip of a link cell', () => {
  let sheet: Spreadsheet;
  let host: HTMLElement;
  let shadow: ShadowRoot;

  beforeEach(async () => {
    host = document.createElement('div');
    host.style.cssText = 'width:600px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    // Drawn with the host's stylesheet, where the tooltip's look is.
    shadow = host.attachShadow({ mode: 'open' });
    shadow.adoptedStyleSheets = [spreadsheetStyles.styleSheet!];
    sheet = new Spreadsheet(host, shadow);
    sheet.setRows(10);
    sheet.setCols(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
    sheet.setHyperlinksTooltips({ 'col2 row2': 'https://vaadin.com' });
    sheet.recomputeViewport();
    await sheet.getCellGrid().updateComplete;
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  function cell(col: number, row: number): HTMLElement {
    return sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(`.cell.col${col}.row${row}`)!;
  }

  function hover(col: number, row: number): void {
    cell(col, row).dispatchEvent(new MouseEvent('mouseover', { bubbles: true, composed: true }));
  }

  function tooltip(): HTMLElement {
    return shadow.querySelector<HTMLElement>('.link-tooltip')!;
  }

  it('shows the link target below the cell while the pointer is over it', () => {
    hover(2, 2);

    expect(tooltip().hidden).to.be.false;
    expect(tooltip().textContent).to.equal('https://vaadin.com');
    const box = cell(2, 2).getBoundingClientRect();
    const at = tooltip().getBoundingClientRect();
    expect(Math.round(at.left)).to.equal(Math.round(box.left));
    expect(Math.round(at.top)).to.equal(Math.round(box.bottom));
  });

  it('is drawn as the GWT client drew it: light text on a dark card', () => {
    hover(2, 2);

    const style = getComputedStyle(tooltip());
    expect(style.backgroundColor).to.equal('rgba(50, 50, 50, 0.9)');
    expect(style.color).to.equal('rgb(255, 255, 255)');
    expect(style.pointerEvents).to.equal('none');
  });

  it('goes when the pointer moves to a cell without a link', () => {
    hover(2, 2);
    hover(3, 2);

    expect(tooltip().hidden).to.be.true;
  });

  it('goes when the pointer leaves the sheet', () => {
    hover(2, 2);
    sheet.getCellGrid().dispatchEvent(new MouseEvent('mouseleave'));

    expect(tooltip().hidden).to.be.true;
  });

  it('shows nothing over a cell without a link', () => {
    hover(4, 4);

    expect(tooltip().hidden).to.be.true;
  });
});
