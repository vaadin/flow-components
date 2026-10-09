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
 * The documented `::part()` styling API, exercised the way an application
 * uses it.
 *
 * `::part()` crosses exactly one shadow boundary. Every documented part is
 * emitted inside a sub-element that has a shadow root of its own, so from
 * an application's stylesheet each one is two boundaries away and matches
 * nothing unless the sub-element re-exports it.
 *
 * These tests write real CSS against the host and read back the computed
 * style, rather than querying the parts through the shadow roots. Reaching
 * in is what let the whole API silently break: the parts were all present
 * and correctly named, and none of them could be styled.
 */

let hostSeq = 0;

function attachSheet(): {
  host: HTMLElement;
  sheet: Spreadsheet;
  hostSelector: string;
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.id = `part-export-host-${hostSeq++}`;
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  // The wrapper element renders into its own shadow root, which is the
  // boundary the parts have to cross.
  const renderTarget = host.attachShadow({ mode: 'open' });
  const sheet = new Spreadsheet(host, renderTarget);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));
  return {
    host,
    sheet,
    hostSelector: `#${host.id}`,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

/** Apply a rule the way an application would, and remove it afterwards. */
function withAppStyle(css: string, run: () => void): void {
  const style = document.createElement('style');
  style.textContent = css;
  document.head.appendChild(style);
  try {
    run();
  } finally {
    style.remove();
  }
}

describe('::part() reaches out of the nested shadow roots', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function paint(): Promise<void> {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;
    await ctx.sheet.getColumnHeader().updateComplete;
    await ctx.sheet.getRowHeader().updateComplete;
    await ctx.sheet.getFormulaBar().updateComplete;
  }

  it('styles a cell', async () => {
    await paint();
    const cell = ctx.sheet.getCellGrid().shadowRoot!.querySelector('.cell')!;

    withAppStyle(`${ctx.hostSelector}::part(cell) { color: rgb(1, 2, 3); }`, () => {
      expect(getComputedStyle(cell).color).to.equal('rgb(1, 2, 3)');
    });
  });

  it('styles a column header', async () => {
    await paint();
    const header = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector('.col-header-cell')!;

    withAppStyle(
      `${ctx.hostSelector}::part(column-header) { color: rgb(4, 5, 6); }`,
      () => {
        expect(getComputedStyle(header).color).to.equal('rgb(4, 5, 6)');
      }
    );
  });

  it('styles a row header', async () => {
    await paint();
    const header = ctx.sheet
      .getRowHeader()
      .shadowRoot!.querySelector('.row-header-cell')!;

    withAppStyle(`${ctx.hostSelector}::part(row-header) { color: rgb(7, 8, 9); }`, () => {
      expect(getComputedStyle(header).color).to.equal('rgb(7, 8, 9)');
    });
  });

  it('styles the address and formula fields', async () => {
    await paint();
    const bar = ctx.sheet.getFormulaBar().shadowRoot!;
    const address = bar.querySelector('[part~="address-field"]')!;
    const formula = bar.querySelector('[part~="formula-field"]')!;

    withAppStyle(
      `${ctx.hostSelector}::part(address-field) { color: rgb(10, 11, 12); }
       ${ctx.hostSelector}::part(formula-field) { color: rgb(13, 14, 15); }`,
      () => {
        expect(getComputedStyle(address).color).to.equal('rgb(10, 11, 12)');
        expect(getComputedStyle(formula).color).to.equal('rgb(13, 14, 15)');
      }
    );
  });

  it('styles a selected cell through cell-range', async () => {
    // cell-range is a second part on the same element, so it also proves
    // a multi-valued part attribute survives the export.
    ctx.sheet.setSelectedCellAndRange('A1', 1, 1, 1, 3, 1, 1, false);
    await paint();
    const ranged = ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector('.cell.col2.row1')!;

    withAppStyle(
      `${ctx.hostSelector}::part(cell-range) { color: rgb(16, 17, 18); }`,
      () => {
        expect(getComputedStyle(ranged).color).to.equal('rgb(16, 17, 18)');
      }
    );
  });

  it('styles a selected column header through header-selected', async () => {
    ctx.sheet.setSelectedCellAndRange('A1', 1, 1, 1, 1, 1, 1, false);
    await paint();
    const header = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector('.col-header-cell.col1')!;

    withAppStyle(
      `${ctx.hostSelector}::part(header-selected) { color: rgb(19, 20, 21); }`,
      () => {
        expect(getComputedStyle(header).color).to.equal('rgb(19, 20, 21)');
      }
    );
  });

  it('styles a sheet tab', async () => {
    ctx.sheet.setSheetNames(['One', 'Two']);
    ctx.sheet.setSheetIndex(1);
    await ctx.sheet.getSheetTabs().updateComplete;
    const tab = ctx.sheet.getSheetTabs().shadowRoot!.querySelector('.sheet-tab')!;

    withAppStyle(`${ctx.hostSelector}::part(tab) { color: rgb(22, 23, 24); }`, () => {
      expect(getComputedStyle(tab).color).to.equal('rgb(22, 23, 24)');
    });
  });

  it('leaves a part nobody exported alone', async () => {
    // Guards against exporting with a wildcard or over-broad list: a name
    // the component does not document must not become styleable by
    // accident.
    await paint();
    const cell = ctx.sheet.getCellGrid().shadowRoot!.querySelector('.cell')!;
    const before = getComputedStyle(cell).color;

    withAppStyle(
      `${ctx.hostSelector}::part(not-a-documented-part) { color: rgb(99, 98, 97); }`,
      () => {
        expect(getComputedStyle(cell).color).to.equal(before);
      }
    );
  });
});

describe('::part() reaches the parts documented with the Lit client', () => {
  let ctx: ReturnType<typeof attachSheet>;
  const originalVaadin = (window as any).Vaadin;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
    (window as any).Vaadin = originalVaadin;
  });

  /** Style one part the way an application would and read it back. */
  function expectStyleable(part: string, el: Element): void {
    withAppStyle(`${ctx.hostSelector}::part(${part}) { color: rgb(31, 32, 33); }`, () => {
      expect(getComputedStyle(el).color, part).to.equal('rgb(31, 32, 33)');
    });
  }

  it('styles the named-range box', async () => {
    ctx.sheet.setNamedRanges(['Totals']);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getFormulaBar().updateComplete;

    expectStyleable('named-range-box', ctx.sheet.getFormulaBar().shadowRoot!.querySelector('.namedrangebox')!);
  });

  it('styles the header corner and its outline level buttons', async () => {
    ctx.sheet.setColGroupingMax(1);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getHeaderCorner().updateComplete;

    expectStyleable('header-corner', ctx.sheet.getHeaderCorner());
    expectStyleable('outline-level-button', ctx.sheet.getHeaderCorner().shadowRoot!.querySelector('.level-header')!);
  });

  it('styles the field a sheet tab is renamed in', async () => {
    ctx.sheet.setSheetNames(['One', 'Two']);
    ctx.sheet.setSheetIndex(1);
    await ctx.sheet.getSheetTabs().updateComplete;
    ctx.sheet
      .getSheetTabs()
      .shadowRoot!.querySelector('.sheet-tab')!
      .dispatchEvent(new MouseEvent('dblclick', { bubbles: true, composed: true }));
    await ctx.sheet.getSheetTabs().updateComplete;

    expectStyleable('tab-rename', ctx.sheet.getSheetTabs().shadowRoot!.querySelector('[part~="tab-rename"]')!);
  });

  it('styles the box a comment is written in', async () => {
    ctx.sheet.setCellComments({ 'col2 row2': 'A note' });
    ctx.sheet.editCellComment(1, 1);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    expectStyleable('comment-edit', ctx.sheet.getCellGrid().shadowRoot!.querySelector('.comment-overlay-edit')!);
  });

  it("styles a comment's card, author and text", async () => {
    ctx.sheet.setCellComments({ 'col2 row2': 'A note' });
    ctx.sheet.setCellCommentAuthors({ 'col2 row2': 'Ann' });
    ctx.sheet.setVisibleCellComments(['col2 row2']);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    const grid = ctx.sheet.getCellGrid().shadowRoot!;
    expectStyleable('comment-overlay', grid.querySelector('.v-spreadsheet-comment-overlay')!);
    expectStyleable('comment-author', grid.querySelector('.comment-overlay-author')!);
    expectStyleable('comment-text', grid.querySelector('.comment-overlay-label')!);
  });

  it("styles the pop-up's card, header, close box and caption", () => {
    const content = document.createElement('div');
    (window as any).Vaadin = { Flow: { clients: { ROOT: { getByNodeId: () => content } } } };
    ctx.sheet.onPopupButtonOpen(1, 2, '7', 'ROOT');

    const overlay = ctx.sheet.getPopupOverlay();
    expectStyleable('popup-overlay', overlay);
    expectStyleable('popup-header', overlay.querySelector('.popup-header')!);
    expectStyleable('popup-close', overlay.querySelector('.popup-close')!);
    expectStyleable('popup-caption', overlay.querySelector('.header-caption')!);
  });

  it('styles the context menu and its items', async () => {
    ctx.sheet.showActions([{ caption: 'Insert row', key: '1', type: 0, iconNodeId: 0 }]);
    await ctx.sheet.getContextMenu().updateComplete;

    const menu = ctx.sheet.getContextMenu().shadowRoot!;
    expectStyleable('context-menu', menu.querySelector('.menu')!);
    expectStyleable('context-menu-item', menu.querySelector('.item')!);
  });
});

describe('exportparts — the declared lists', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('declares an export list on every sub-element that owns a shadow root', () => {
    for (const el of [
      ctx.sheet.getCellGrid(),
      ctx.sheet.getColumnHeader(),
      ctx.sheet.getRowHeader(),
      ctx.sheet.getFormulaBar(),
      ctx.sheet.getSheetTabs(),
      ctx.sheet.getContextMenu()
    ]) {
      expect(el.getAttribute('exportparts'), el.tagName).to.be.a('string').and.not
        .be.empty;
    }
  });

  it('exports every part name the cell grid emits', () => {
    // The list is written by hand, so it drifts the moment a part is added
    // without it. These are the names the styling docs promise.
    const exported = ctx.sheet.getCellGrid().getAttribute('exportparts')!.split(',');
    expect(exported).to.include.members([
      'cell',
      'cell-range',
      'cell-input',
      'comment-triangle',
      'invalid-triangle',
      'popup-button',
      'selection-corner',
      'comment-edit',
      'comment-overlay',
      'comment-author',
      'comment-text'
    ]);
  });

  it('exports the header and formula-bar names', () => {
    expect(
      ctx.sheet.getColumnHeader().getAttribute('exportparts')!.split(',')
    ).to.include.members(['column-header', 'header-selected']);
    expect(
      ctx.sheet.getRowHeader().getAttribute('exportparts')!.split(',')
    ).to.include.members(['row-header', 'header-selected']);
    expect(
      ctx.sheet.getFormulaBar().getAttribute('exportparts')!.split(',')
    ).to.include.members(['address-field', 'formula-field']);
    expect(
      ctx.sheet.getSheetTabs().getAttribute('exportparts')!.split(',')
    ).to.include.members(['tab', 'tab-selected']);
  });
});
