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
  host: HTMLElement;
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
    host,
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

  it('has a message to show without the server sending one', async () => {
    // The server only pushes this string when an application replaces the
    // default, so the connector has to carry one. Without it every
    // invalid formula hovered silently and explained nothing.
    ctx.sheet.setInvalidFormulaCells([A1]);
    await paint(ctx.sheet);

    await hover(ctx.sheet, 1, 1);

    const message = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector('.comment-overlay-invalidformula');
    expect(message, 'no invalid-formula message').to.not.be.null;
    expect(message!.textContent!.trim()).to.equal('Invalid formula');
  });

  it('lets the server replace the default message', async () => {
    ctx.sheet.setInvalidFormulaErrorMessage('Virheellinen kaava');
    ctx.sheet.setInvalidFormulaCells([A1]);
    await paint(ctx.sheet);

    await hover(ctx.sheet, 1, 1);

    const message = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector('.comment-overlay-invalidformula');
    expect(message!.textContent!.trim()).to.equal('Virheellinen kaava');
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

/**
 * A cell comment is written in the note itself. The server's Insert
 * comment and Edit comment actions make sure there is a comment and then
 * ask the client to open it; the connector had that call stubbed out with
 * a TODO, so the note could be created and never written.
 */
describe('Spreadsheet — writing a cell comment', () => {
  let ctx: ReturnType<typeof attachSheet>;
  let events: any[];

  beforeEach(() => {
    ctx = attachSheet();
    events = [];
    ctx.host.addEventListener('spreadsheet-event', (e: Event) =>
      events.push((e as CustomEvent).detail)
    );
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function note(): Promise<HTMLTextAreaElement | null> {
    await paint(ctx.sheet);
    return ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLTextAreaElement>('.comment-overlay-edit');
  }

  it('opens the note for typing when the server asks', async () => {
    ctx.sheet.setCellComments({ [A1]: 'a note' });
    ctx.sheet.editCellComment(0, 0);

    const el = await note();
    expect(el, 'no comment editor').to.not.be.null;
    expect(el!.value).to.equal('a note');
  });

  it('opens an empty note on a cell that has none yet', async () => {
    ctx.sheet.editCellComment(1, 2);

    expect((await note())!.value).to.equal('');
  });

  function escape(el: HTMLElement, isComposing: boolean): void {
    el.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', isComposing, bubbles: true, composed: true, cancelable: true })
    );
  }

  it('leaves the note on Escape', async () => {
    ctx.sheet.editCellComment(1, 2);
    const el = (await note())!;
    el.focus();

    escape(el, false);

    expect(ctx.sheet.getCellGrid().shadowRoot!.activeElement).to.not.equal(el);
  });

  it('stays in the note on the Escape that drops a conversion', async () => {
    // The key is the input method's: it drops the text being composed.
    ctx.sheet.editCellComment(1, 2);
    const el = (await note())!;
    el.focus();

    escape(el, true);

    expect(ctx.sheet.getCellGrid().shadowRoot!.activeElement).to.equal(el);
  });

  it('stays up without being hovered', async () => {
    // An ordinary note only shows on hover; the one being written has
    // to stay where the user is typing.
    ctx.sheet.editCellComment(0, 0);

    expect(await note()).to.not.be.null;
  });

  it('tells the server what was written when the note is left', async () => {
    ctx.sheet.editCellComment(1, 2);
    const el = (await note())!;
    el.value = 'Check this figure';
    events.length = 0;
    el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));

    expect(events).to.have.length(1);
    expect(events[0].type).to.equal('updateCellComment');
    expect(events[0].data).to.eql({ text: 'Check this figure', col: 2, row: 3 });
  });

  it('opens the note on the cell the server names, counting from 0 as POI does', async () => {
    // The server passes its CellReference's column and row as they are.
    // Read as counting from 1, the note for B3 opened on A2 and what was
    // typed into it was saved there.
    ctx.sheet.editCellComment(1, 2);
    const el = (await note())!;
    el.value = 'On B3';
    events.length = 0;
    el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));

    expect(events[0].data).to.eql({ text: 'On B3', col: 2, row: 3 });
  });

  it('shows the note without waiting for the server', async () => {
    ctx.sheet.editCellComment(0, 0);
    const el = (await note())!;
    el.value = 'Typed';
    el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));
    await paint(ctx.sheet);

    await hover(ctx.sheet, 1, 1);
    expect(labels(ctx.sheet)).to.eql(['Typed']);
  });

  it('closes the note once it is written', async () => {
    ctx.sheet.editCellComment(0, 0);
    const el = (await note())!;
    el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));

    expect(await note()).to.be.null;
  });

  it('reports the note once, not again as it closes', async () => {
    ctx.sheet.editCellComment(0, 0);
    const el = (await note())!;
    el.value = 'Once';
    events.length = 0;
    el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));
    el.dispatchEvent(new FocusEvent('focusout', { bubbles: true, composed: true }));

    expect(events.map((e) => e.type)).to.eql(['updateCellComment']);
  });

  it('takes a click inside the note, so the cell under it is not selected', async () => {
    // The card lets the pointer through to the cells so a note shown on
    // hover never gets in the way. The note being written has to take it:
    // a click meant to place the caret otherwise selected the cell
    // underneath, and the note lost the focus and closed.
    ctx.sheet.editCellComment(0, 0);
    const el = (await note())!;
    const box = el.getBoundingClientRect();

    const hit = ctx.sheet
      .getCellGrid()
      .shadowRoot!.elementFromPoint(box.left + box.width / 2, box.top + box.height / 2);

    expect(hit === el, `hit ${hit?.className}`).to.be.true;
  });

  it('keeps the keys out of the sheet underneath', async () => {
    // Without stopping them, typing a note would also be typing into
    // the cell the sheet has selected.
    ctx.sheet.editCellComment(0, 0);
    let reachedSheet = false;
    ctx.sheet
      .getScrollContainer()
      .addEventListener('keydown', () => {
        reachedSheet = true;
      });
    (await note())!.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'a', bubbles: true, composed: true })
    );

    expect(reachedSheet).to.be.false;
  });
});

/**
 * The comment card, as the GWT client drew it.
 *
 * The Lit client showed comments as yellow Excel-style tooltips. The GWT
 * client drew a white card with a soft shadow, the author in small bold
 * type above the text, and a rule between an error and the comment below it.
 */
describe('Spreadsheet — the comment card', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(async () => {
    ctx = attachSheet();
    await paint(ctx.sheet);
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function root(): ShadowRoot {
    return ctx.sheet.getCellGrid().shadowRoot!;
  }

  it('is a white card with rounded corners and a soft shadow', async () => {
    ctx.sheet.setCellComments({ [A1]: 'a note' });
    ctx.sheet.setVisibleCellComments([A1]);
    await paint(ctx.sheet);

    const style = getComputedStyle(overlays(ctx.sheet)[0]);
    expect(style.backgroundColor).to.equal('rgb(255, 255, 255)');
    expect(style.color).to.equal('rgb(71, 71, 71)');
    expect(`${style.borderTopLeftRadius} ${style.paddingTop}`).to.equal('4px 7px');
    expect(style.boxShadow).to.contain('rgba(0, 0, 0, 0.1) 0px 4px 10px 0px');
  });

  it('writes the author small and bold above the text', async () => {
    ctx.sheet.setCellComments({ [A1]: 'a note' });
    ctx.sheet.setCellCommentAuthors({ [A1]: 'Ugur' });
    ctx.sheet.setVisibleCellComments([A1]);
    await paint(ctx.sheet);

    const author = getComputedStyle(root().querySelector('.comment-overlay-author')!);
    expect(`${author.fontSize} ${author.fontWeight} ${author.paddingBottom}`).to.equal('11px 700 7px');
  });

  it('keeps a long comment to 168 by 140 pixels', async () => {
    ctx.sheet.setCellComments({ [A1]: 'a note' });
    ctx.sheet.setVisibleCellComments([A1]);
    await paint(ctx.sheet);

    const label = getComputedStyle(root().querySelector('.comment-overlay-label')!);
    expect(`${label.maxWidth} ${label.maxHeight}`).to.equal('168px 140px');
  });

  it('rules an error off from the comment below it, in red', async () => {
    ctx.sheet.setCellComments({ [A1]: 'a note' });
    ctx.sheet.setInvalidFormulaCells([A1]);
    await paint(ctx.sheet);

    await hover(ctx.sheet, 1, 1);

    expect(root().querySelector('.comment-overlay-separator'), 'a rule between them').to.not.be.null;
    expect(getComputedStyle(root().querySelector('.comment-overlay-invalidformula')!).color).to.equal(
      'rgb(237, 71, 59)'
    );
  });

  it('draws no rule under an error with no comment', async () => {
    ctx.sheet.setInvalidFormulaCells([A1]);
    await paint(ctx.sheet);

    await hover(ctx.sheet, 1, 1);

    expect(root().querySelector('.comment-overlay-separator')).to.be.null;
  });
});
