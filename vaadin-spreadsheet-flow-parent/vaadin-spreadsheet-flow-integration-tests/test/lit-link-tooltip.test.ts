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
import { LinkTooltip } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/LinkTooltip.js';
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

  it('stays away while a range is dragged out over a link cell', () => {
    cell(4, 4).dispatchEvent(new MouseEvent('mousedown', { bubbles: true, composed: true }));
    hover(2, 2);

    expect(tooltip().hidden).to.be.true;
  });

  it('stays away while the fill handle is dragged over a link cell', async () => {
    sheet.setSelectedCellAndRange('D4', 4, 4, 4, 4, 4, 4, false);
    await sheet.getCellGrid().updateComplete;
    sheet
      .getCellGrid()
      .shadowRoot!.querySelector('.selection-corner')!
      .dispatchEvent(new MouseEvent('mousedown', { bubbles: true, composed: true }));
    hover(2, 2);

    expect(tooltip().hidden).to.be.true;
  });

  it('comes back once the drag is over', () => {
    cell(4, 4).dispatchEvent(new MouseEvent('mousedown', { bubbles: true, composed: true }));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
    hover(2, 2);

    expect(tooltip().hidden).to.be.false;
  });

  it('shows nothing over a cell without a link', () => {
    hover(4, 4);

    expect(tooltip().hidden).to.be.true;
  });
});

describe('LinkTooltip — on its own', () => {
  let dragging: boolean;
  let tooltip: LinkTooltip;
  const box = new DOMRect(100, 100, 80, 20);

  beforeEach(() => {
    dragging = false;
    tooltip = new LinkTooltip({
      tooltipAt: (col, row) => (col === 2 && row === 3 ? 'https://vaadin.com' : undefined),
      dragging: () => dragging
    });
    document.body.appendChild(tooltip.element);
  });

  afterEach(() => {
    tooltip.element.remove();
  });

  it('starts hidden, as a popover of its own', () => {
    expect(tooltip.element.hidden).to.be.true;
    expect(tooltip.element.popover).to.equal('manual');
    expect(tooltip.element.className).to.equal('link-tooltip');
  });

  it("shows the link's target below the cell", () => {
    tooltip.show({ col: 2, row: 3, box });

    expect(tooltip.element.hidden).to.be.false;
    expect(tooltip.element.textContent).to.equal('https://vaadin.com');
    expect(tooltip.element.style.top).to.equal('120px');
  });

  it('hides over a cell without a link, without a box, and while dragging', () => {
    tooltip.show({ col: 2, row: 3, box });
    tooltip.show({ col: 1, row: 1, box });
    expect(tooltip.element.hidden).to.be.true;

    tooltip.show({ col: 2, row: 3, box });
    tooltip.show({ col: 2, row: 3 });
    expect(tooltip.element.hidden).to.be.true;

    dragging = true;
    tooltip.show({ col: 2, row: 3, box });
    expect(tooltip.element.hidden).to.be.true;
  });

  it('hides on demand, and hiding again changes nothing', () => {
    tooltip.show({ col: 2, row: 3, box });

    tooltip.hide();
    tooltip.hide();

    expect(tooltip.element.hidden).to.be.true;
  });
});

