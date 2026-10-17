/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { attachSheet, type AttachedSheet } from './spreadsheet-harness.js';

/**
 * One way the sheet is drawn: what a handler of the sheet's own events
 * changes is drawn once, as the handler ends and before the server is told;
 * what the server changes, once it is all in. A repaint already asked for is
 * not drawn again once the sheet has been drawn.
 */
describe('Spreadsheet — one way to draw', () => {
  let ctx: AttachedSheet;
  let paints: number;

  beforeEach(() => {
    ctx = attachSheet();
    paints = 0;
    const draw = ctx.sheet.recomputeViewport.bind(ctx.sheet);
    ctx.sheet.recomputeViewport = () => {
      paints++;
      draw();
    };
  });

  afterEach(() => ctx.cleanup());

  /** Drag column B's edge 20px right; the drag ends on the page, not on the sheet. */
  function dragColumnEdge(): void {
    ctx.sheet
      .getColumnHeader()
      .dispatchEvent(
        new CustomEvent('column-resize-start', { detail: { col: 2, clientX: 100 }, bubbles: true, composed: true })
      );
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 120, bubbles: true }));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
  }

  it('has the header showing a width dragged as soon as the drag ends', () => {
    dragColumnEdge();

    expect(ctx.sheet.getColumnHeader().renderState.colWidths[1]).to.equal(70);
  });

  it('draws a drag across the page once, as it ends', () => {
    paints = 0;

    dragColumnEdge();

    expect(paints).to.equal(1);
  });
});
