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
 * Cell comments — the yellow notes.
 *
 * The connector stored the comment text and rendered the little triangle
 * that says a cell has one, but never the note itself: hovering showed
 * nothing, and a comment the workbook marks "always visible" showed
 * nothing either. The invalid-formula message travelled the same path and
 * was equally invisible.
 *
 * The class names are GWT's, because applications style them.
 */

const A1 = 'col1 row1';
const B2 = 'col2 row2';

function attachSheet(): {
  sheet: Spreadsheet;
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));
  sheet.recomputeViewport();
  return {
    sheet,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

async function paint(sheet: Spreadsheet): Promise<void> {
  sheet.recomputeViewport();
  await sheet.getCellGrid().updateComplete;
}

function overlays(sheet: Spreadsheet): HTMLElement[] {
  return Array.from(
    sheet.getCellGrid().shadowRoot!.querySelectorAll<HTMLElement>('.v-spreadsheet-comment-overlay')
  );
}

function labels(sheet: Spreadsheet): string[] {
  return Array.from(
    sheet.getCellGrid().shadowRoot!.querySelectorAll('.comment-overlay-label')
  ).map((el) => el.textContent!.trim());
}

/** Hover a cell the way the pointer does — through the grid's own handler. */
async function hover(sheet: Spreadsheet, col: number, row: number): Promise<void> {
  const cell = sheet
    .getCellGrid()
    .shadowRoot!.querySelector<HTMLElement>(`.cell[data-col="${col}"][data-row="${row}"]`);
  expect(cell, `no cell ${col},${row}`).to.not.be.null;
  cell!.dispatchEvent(new MouseEvent('mouseover', { bubbles: true, composed: true }));
  await sheet.getCellGrid().updateComplete;
}

async function leave(sheet: Spreadsheet): Promise<void> {
  sheet.getCellGrid().dispatchEvent(new MouseEvent('mouseleave'));
  await sheet.getCellGrid().updateComplete;
}

describe('Spreadsheet — cell comment overlays', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(async () => {
    ctx = attachSheet();
    await paint(ctx.sheet);
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('shows nothing when no cell carries a comment', () => {
    expect(overlays(ctx.sheet)).to.be.empty;
  });

  it('shows an always-visible comment without hovering', async () => {
    ctx.sheet.setCellComments({ [A1]: 'Always Visible Comment.' });
    ctx.sheet.setVisibleCellComments([A1]);
    await paint(ctx.sheet);

    expect(labels(ctx.sheet)).to.eql(['Always Visible Comment.']);
  });

  it('keeps a comment hidden until its cell is hovered', async () => {
    ctx.sheet.setCellComments({ [B2]: 'first cell comment' });
    await paint(ctx.sheet);
    expect(overlays(ctx.sheet)).to.be.empty;

    await hover(ctx.sheet, 2, 2);

    expect(labels(ctx.sheet)).to.eql(['first cell comment']);
  });

  it('hides the hovered comment once the pointer leaves the sheet', async () => {
    ctx.sheet.setCellComments({ [B2]: 'first cell comment' });
    await paint(ctx.sheet);
    await hover(ctx.sheet, 2, 2);

    await leave(ctx.sheet);

    expect(overlays(ctx.sheet)).to.be.empty;
  });

  it('shows nothing for a hovered cell that has no comment', async () => {
    ctx.sheet.setCellComments({ [B2]: 'first cell comment' });
    await paint(ctx.sheet);

    await hover(ctx.sheet, 5, 5);

    expect(overlays(ctx.sheet)).to.be.empty;
  });

  it('names the author when the workbook records one', async () => {
    ctx.sheet.setCellComments({ [A1]: 'a note' });
    ctx.sheet.setCellCommentAuthors({ [A1]: 'Ada' });
    ctx.sheet.setVisibleCellComments([A1]);
    await paint(ctx.sheet);

    const author = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector('.comment-overlay-author');
    expect(author, 'no author line').to.not.be.null;
    expect(author!.textContent!.trim()).to.equal('Ada');
  });

  it('shows an always-visible comment and a hovered one at once', async () => {
    ctx.sheet.setCellComments({ [A1]: 'pinned', [B2]: 'hovered' });
    ctx.sheet.setVisibleCellComments([A1]);
    await paint(ctx.sheet);

    await hover(ctx.sheet, 2, 2);

    expect(labels(ctx.sheet).sort()).to.eql(['hovered', 'pinned']);
  });

  it('does not show the same comment twice when the pinned cell is hovered', async () => {
    ctx.sheet.setCellComments({ [A1]: 'pinned' });
    ctx.sheet.setVisibleCellComments([A1]);
    await paint(ctx.sheet);

    await hover(ctx.sheet, 1, 1);

    expect(labels(ctx.sheet)).to.eql(['pinned']);
  });

  it('shows the invalid-formula message on hover', async () => {
    ctx.sheet.setInvalidFormulaErrorMessage('Invalid formula');
    ctx.sheet.setInvalidFormulaCells([A1]);
    await paint(ctx.sheet);

    await hover(ctx.sheet, 1, 1);

    const message = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector('.comment-overlay-invalidformula');
    expect(message, 'no invalid-formula message').to.not.be.null;
    expect(message!.textContent!.trim()).to.equal('Invalid formula');
  });

  it('keeps the invalid-formula message hidden until hovered', async () => {
    ctx.sheet.setInvalidFormulaErrorMessage('Invalid formula');
    ctx.sheet.setInvalidFormulaCells([A1]);
    await paint(ctx.sheet);

    expect(overlays(ctx.sheet)).to.be.empty;
  });

  it('accepts the comment map as the JSON string Flow sends', async () => {
    ctx.sheet.setCellComments(JSON.stringify({ [A1]: 'from the wire' }) as any);
    ctx.sheet.setVisibleCellComments(JSON.stringify([A1]) as any);
    await paint(ctx.sheet);

    expect(labels(ctx.sheet)).to.eql(['from the wire']);
  });

  it('drops the overlay when the comment is removed', async () => {
    ctx.sheet.setCellComments({ [A1]: 'a note' });
    ctx.sheet.setVisibleCellComments([A1]);
    await paint(ctx.sheet);
    expect(overlays(ctx.sheet)).to.have.length(1);

    ctx.sheet.setCellComments({});
    ctx.sheet.setVisibleCellComments([]);
    await paint(ctx.sheet);

    expect(overlays(ctx.sheet)).to.be.empty;
  });
});
