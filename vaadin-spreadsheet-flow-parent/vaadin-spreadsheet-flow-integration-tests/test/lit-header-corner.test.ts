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
  SpreadsheetHeaderCorner,
  type HeaderCornerRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetHeaderCorner.js';

/**
 * The corner where the header strips meet, laid out as the GWT client's:
 * the square beside the letters and above the numbers, shaded as the
 * letters are; a box above it for the column outline levels and one beside
 * it for the row levels, each bordered off from the gutters; and, where
 * the gutters meet, a box over the dotted lines between their levels. The
 * Lit client drew one flat square, the level numbers a few pixels off.
 */
describe('SpreadsheetHeaderCorner — as the GWT client laid it out', () => {
  // The GWT client's sizes: 50px numbers and 27px letters, a 15px gutter
  // column per row level and an 18px gutter row per column level, one more
  // of each, after 1px and 3px.
  const ROW_GUTTER = 1 + 2 * 15;
  const COL_GUTTER = 3 + 2 * 18;
  let corner: SpreadsheetHeaderCorner;

  async function attach(state: Partial<HeaderCornerRenderState>): Promise<SpreadsheetHeaderCorner> {
    corner = new SpreadsheetHeaderCorner();
    const full: HeaderCornerRenderState = {
      headerWidth: 50,
      headerHeight: 27,
      colGroupingMax: 0,
      rowGroupingMax: 0,
      ...state
    };
    const rowGutter = full.rowGroupingMax > 0 ? ROW_GUTTER : 0;
    const colGutter = full.colGroupingMax > 0 ? COL_GUTTER : 0;
    corner.style.cssText = `position:absolute;left:0;top:0;width:${full.headerWidth + rowGutter}px;height:${
      full.headerHeight + colGutter
    }px;`;
    corner.renderState = full;
    document.body.appendChild(corner);
    await corner.updateComplete;
    return corner;
  }

  afterEach(() => {
    corner.remove();
  });

  function part(selector: string): HTMLElement {
    return corner.shadowRoot!.querySelector<HTMLElement>(selector)!;
  }

  /** Where an element is, from the corner's own top left (its border box). */
  function at(el: Element): { left: number; top: number; width: number; height: number } {
    const box = el.getBoundingClientRect();
    const origin = corner.getBoundingClientRect();
    return { left: box.left - origin.left, top: box.top - origin.top, width: box.width, height: box.height };
  }

  it('shades the square from the header colour down, as the letters are', async () => {
    await attach({});

    const square = part('.corner');
    // The letters' gradient, blended in sRGB as theirs is.
    expect(getComputedStyle(square).backgroundImage).to.match(/^linear-gradient\(in srgb, rgb\(250, 250, 250\) 2%, .+ 98%\)$/);
    // Inside the host's right and bottom border.
    expect(at(square)).to.eql({ left: 0, top: 0, width: 49, height: 26 });
  });

  it('puts the square past both gutters on a grouped sheet', async () => {
    await attach({ colGroupingMax: 1, rowGroupingMax: 1 });

    expect(at(part('.corner'))).to.eql({ left: ROW_GUTTER, top: COL_GUTTER, width: 49, height: 26 });
  });

  it('boxes the column levels off from the gutter beside them', async () => {
    await attach({ colGroupingMax: 1, rowGroupingMax: 1 });

    const box = part('.col-group-summary');
    expect(at(box)).to.eql({ left: ROW_GUTTER, top: 0, width: 49, height: COL_GUTTER });
    expect(getComputedStyle(box).borderBottom).to.equal('1px solid rgb(199, 199, 199)');
  });

  it('boxes the row levels off from the gutter above them', async () => {
    await attach({ colGroupingMax: 1, rowGroupingMax: 1 });

    const box = part('.row-group-summary');
    expect(at(box)).to.eql({ left: 0, top: COL_GUTTER, width: ROW_GUTTER, height: 26 });
    expect(getComputedStyle(box).borderRight).to.equal('1px solid rgb(199, 199, 199)');
  });

  it('boxes off where the gutters meet, over the dotted lines that run in under it', async () => {
    await attach({ colGroupingMax: 1, rowGroupingMax: 1 });

    const box = part('.grouping-corner');
    const style = getComputedStyle(box);
    expect(at(box)).to.eql({ left: 0, top: 0, width: ROW_GUTTER, height: COL_GUTTER });
    expect(`${style.borderRight} | ${style.borderBottom}`).to.equal(
      '1px solid rgb(199, 199, 199) | 1px solid rgb(199, 199, 199)'
    );
    expect(style.backgroundColor).to.equal('rgb(250, 250, 250)');
    expect(Number(style.zIndex)).to.be.greaterThan(0);
  });

  it('draws no box where the gutters meet when only one axis is grouped', async () => {
    await attach({ colGroupingMax: 1 });

    expect(part('.grouping-corner')).to.be.null;
    expect(part('.row-group-summary')).to.be.null;
  });

  it('stacks the column levels at the right, 4px in, as the GWT client did', async () => {
    await attach({ colGroupingMax: 1, rowGroupingMax: 1 });

    const buttons = [...corner.shadowRoot!.querySelectorAll('[data-axis="cols"]')].map(at);
    // The box is 49px wide inside the host's border.
    expect(buttons.map((b) => `${b.left},${b.top}`)).to.eql([
      `${ROW_GUTTER + 49 - 4 - 11},0`,
      `${ROW_GUTTER + 49 - 4 - 11},18`
    ]);
  });

  it('writes a column level in a span set to the top of its line, as the GWT client did', async () => {
    await attach({ colGroupingMax: 1 });

    const span = corner.shadowRoot!.querySelector('[data-axis="cols"] > span')!;
    expect(getComputedStyle(span).verticalAlign).to.equal('text-top');
  });

  it('lines the row levels up 4px apart in a line as tall as the letters, as the GWT client did', async () => {
    await attach({ colGroupingMax: 1, rowGroupingMax: 1 });

    const box = part('.row-group-summary');
    const buttons = [...box.querySelectorAll('[data-axis="rows"]')];
    expect(buttons.map((b) => at(b).left)).to.eql([4, 19]);
    expect(getComputedStyle(box).lineHeight).to.equal('27px');
    expect(getComputedStyle(buttons[0]).display).to.equal('inline-block');
    // Nothing between them: a space would push them apart.
    const text = [...box.childNodes].filter((n) => n.nodeType === Node.TEXT_NODE && n.textContent!.trim() === '');
    expect(text.every((n) => n.textContent === '')).to.be.true;
  });

  it('runs the dotted lines from the corner\'s edges, as the GWT client\'s did', async () => {
    await attach({ colGroupingMax: 1, rowGroupingMax: 1 });

    const cols = part('.level-separator.cols');
    const rows = part('.level-separator.rows');
    expect(at(cols).left).to.equal(0);
    expect(at(cols).top).to.equal(18);
    expect(getComputedStyle(cols).borderBottomStyle).to.equal('dotted');
    expect(at(rows).top).to.equal(0);
    expect(at(rows).left).to.equal(15);
    expect(getComputedStyle(rows).borderRightStyle).to.equal('dotted');
    // Across the corner and its border, at an odd length: along an even
    // one the browser doubles the first dot and shifts the rest.
    expect(at(cols).width).to.equal(ROW_GUTTER + 50);
    expect(at(rows).height).to.equal(COL_GUTTER + 27 + 1);
  });

  it('draws nothing inside without headings', async () => {
    await attach({ headerWidth: 0, headerHeight: 0, colGroupingMax: 1, rowGroupingMax: 1 });

    expect(corner.shadowRoot!.querySelector('div, button')).to.be.null;
  });
});
