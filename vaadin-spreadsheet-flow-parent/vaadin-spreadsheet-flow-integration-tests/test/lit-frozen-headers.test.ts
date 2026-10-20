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
 * Headers of frozen rows and columns.
 *
 * The cell-grid pins frozen cells in their own quadrants, but the header
 * strips only ever drew the scrolling viewport. Scroll a sheet with two
 * frozen columns and the columns stayed put while the letters naming
 * them slid away — and once column 1's header is gone, nothing can
 * select that column.
 */

function attachSheet(): { sheet: Spreadsheet; cleanup: () => void } {
  const host = document.createElement('div');
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setCols(60);
  sheet.setRows(400);
  sheet.setColW(new Array(60).fill(50));
  sheet.setRowH(new Array(400).fill(15));
  return {
    sheet,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

function colHeaders(sheet: Spreadsheet): string[] {
  return [
    ...sheet
      .getColumnHeader()
      .shadowRoot!.querySelectorAll<HTMLElement>('.col-header-cell')
  ].map((e) => e.dataset.col!);
}

function rowHeaders(sheet: Spreadsheet): string[] {
  return [
    ...sheet
      .getRowHeader()
      .shadowRoot!.querySelectorAll<HTMLElement>('.row-header-cell')
  ].map((e) => e.dataset.row!);
}

describe('Column header — frozen columns', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('keeps the frozen columns headed once the sheet is scrolled past them', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.getScrollContainer().scrollLeft = 1000;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    expect(colHeaders(ctx.sheet)).to.include.members(['1', '2']);
  });

  it('holds the frozen letters in place as the sheet scrolls, before the scroll is drawn', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;
    const letter = (): number =>
      ctx.sheet
        .getColumnHeader()
        .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip .col-header-cell.col1')!
        .getBoundingClientRect().left;
    const before = letter();

    ctx.sheet.getScrollContainer().scrollLeft = 1000;

    expect(letter()).to.equal(before);
  });

  it('puts the frozen headers in the strip, not the scrolling row', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    const strip = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip')!;
    const inStrip = [...strip.querySelectorAll<HTMLElement>('.col-header-cell')].map(
      (e) => e.dataset.col
    );
    expect(inStrip).to.deep.equal(['1', '2']);
  });

  it('draws no strip when nothing is frozen', async () => {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    expect(
      ctx.sheet.getColumnHeader().shadowRoot!.querySelector('.frozen-header-strip')
    ).to.be.null;
  });

  it('gives the frozen headers their resize handles too', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;

    const strip = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip')!;
    expect(strip.querySelectorAll('.col-resize-handle')).to.have.length(2);
  });
});

describe('Row header — frozen rows', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('keeps the frozen rows numbered once the sheet is scrolled past them', async () => {
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.getScrollContainer().scrollTop = 2000;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getRowHeader().updateComplete;

    expect(rowHeaders(ctx.sheet)).to.include.members(['1', '2']);
  });

  it('holds the frozen numbers in place as the sheet scrolls, before the scroll is drawn', async () => {
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getRowHeader().updateComplete;
    const number = (): number =>
      ctx.sheet
        .getRowHeader()
        .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip .row-header-cell.row1')!
        .getBoundingClientRect().top;
    const before = number();

    ctx.sheet.getScrollContainer().scrollTop = 2000;

    expect(number()).to.equal(before);
  });

  it('draws no strip when nothing is frozen', async () => {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getRowHeader().updateComplete;

    expect(
      ctx.sheet.getRowHeader().shadowRoot!.querySelector('.frozen-header-strip')
    ).to.be.null;
  });
});

/**
 * The browser scrolls the sheet before the page hears of the scroll, and
 * shows the scroll straight away. So it is the browser that holds the frozen
 * panes, their headers and the freeze line in place, in the same frame. Moved
 * back by the sheet once the scroll has been handled, they would show off
 * their edge in between, the further the faster the scroll.
 */
describe('Frozen panes — held in place while the sheet scrolls', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function draw(): Promise<void> {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;
    await ctx.sheet.getRowHeader().updateComplete;
    await ctx.sheet.getCellGrid().updateComplete;
  }

  const cells = (): ShadowRoot => ctx.sheet.getCellGrid().shadowRoot!;
  const letters = (): ShadowRoot => ctx.sheet.getColumnHeader().shadowRoot!;
  const numbers = (): ShadowRoot => ctx.sheet.getRowHeader().shadowRoot!;

  /** Where each of them is on screen, its top left corner. */
  function where(parts: Record<string, () => Element | null>): Record<string, string> {
    const at: Record<string, string> = {};
    for (const [name, find] of Object.entries(parts)) {
      const box = find()?.getBoundingClientRect();
      at[name] = box === undefined ? 'not drawn' : `${box.left} ${box.top}`;
    }
    return at;
  }

  /**
   * Scrolls the sheet, and asserts that the parts are where they were: at
   * once, before the scroll is handled, and once it has been drawn.
   */
  async function expectHeldOnScroll(
    parts: Record<string, () => Element | null>,
    top: number,
    left: number
  ): Promise<void> {
    const before = where(parts);
    expect(Object.values(before), 'all drawn').to.not.include('not drawn');
    const scroller = ctx.sheet.getScrollContainer();

    scroller.scrollTop = top;
    scroller.scrollLeft = left;

    expect([scroller.scrollTop, scroller.scrollLeft], 'scrolled').to.eql([top, left]);
    expect(where(parts), 'before the scroll is handled').to.deep.equal(before);
    await draw();
    expect(where(parts), 'once the scroll is drawn').to.deep.equal(before);
  }

  it('holds the frozen rows, their line and the gap among the numbers as the sheet scrolls down', async () => {
    ctx.sheet.setVerticalSplitPosition(2);
    await draw();

    await expectHeldOnScroll(
      {
        'a frozen row': () => cells().querySelector('.cell.col3.row1'),
        'the freeze line': () => cells().querySelector('.quadrant-wrapper-top .freeze-line'),
        'the gap among the numbers': () => numbers().querySelector('.freeze-gap:not(.slid)')
      },
      600,
      0
    );
  });

  it('holds the frozen columns, their line and the gap among the letters as the sheet scrolls right', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    await draw();

    await expectHeldOnScroll(
      {
        'a frozen column': () => cells().querySelector('.cell.col1.row3'),
        'the freeze line': () => cells().querySelector('.quadrant-wrapper-left .freeze-line'),
        'the gap among the letters': () => letters().querySelector('.freeze-gap')
      },
      0,
      1000
    );
  });

  it('holds the corner, its headers and the lines through them as the sheet scrolls both ways', async () => {
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.setHorizontalSplitPosition(2);
    await draw();

    await expectHeldOnScroll(
      {
        'the frozen corner': () => cells().querySelector('.cell.col1.row1'),
        'the line under it': () => cells().querySelector('.quadrant-wrapper-corner .freeze-line'),
        'its letter': () => letters().querySelector('.frozen-header-strip .col-header-cell.col1'),
        'its number': () => numbers().querySelector('.frozen-header-strip .row-header-cell.row1'),
        'the line through the letters': () => letters().querySelector('.frozen-header-strip > .freeze-line'),
        'the line through the numbers': () => numbers().querySelector('.frozen-header-strip > .freeze-line')
      },
      600,
      1000
    );
  });

  it('holds the frozen panes just inside the headers, their outline gutters included', async () => {
    ctx.sheet.setColGroupingMax(2);
    ctx.sheet.setRowGroupingMax(1);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.setHorizontalSplitPosition(2);
    await draw();
    const scroller = ctx.sheet.getScrollContainer();
    // How far the corner is from the letters' bottom and the numbers' right edge.
    const offHeaders = (): number[] => {
      const corner = cells().querySelector('.cell.col1.row1')!.getBoundingClientRect();
      return [
        corner.top - ctx.sheet.getColumnHeader().getBoundingClientRect().bottom,
        corner.left - ctx.sheet.getRowHeader().getBoundingClientRect().right
      ];
    };

    scroller.scrollTop = 600;
    scroller.scrollLeft = 1000;

    expect(offHeaders(), 'before the scroll is handled').to.eql([0, 0]);
    await draw();
    expect(offHeaders(), 'once the scroll is drawn').to.eql([0, 0]);
  });

  it('holds the frozen panes at the edges of the view when the sheet shows no headers', async () => {
    ctx.sheet.setDisplayRowColHeadings(false);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.setHorizontalSplitPosition(2);
    await draw();
    const scroller = ctx.sheet.getScrollContainer();
    // How far the corner is from the view's top left corner.
    const offView = (): number[] => {
      const view = scroller.getBoundingClientRect();
      const corner = cells().querySelector('.cell.col1.row1')!.getBoundingClientRect();
      return [corner.left - view.left - scroller.clientLeft, corner.top - view.top - scroller.clientTop];
    };

    scroller.scrollTop = 600;
    scroller.scrollLeft = 1000;

    expect(offView(), 'before the scroll is handled').to.eql([0, 0]);
    await draw();
    expect(offView(), 'once the scroll is drawn').to.eql([0, 0]);
  });
});

/**
 * A freeze pane splits the sheet in four, and the three quadrants that
 * are not the frozen corner have to start where the frozen block ends.
 * Drawn from zero instead, the scrolling columns end up underneath the
 * frozen ones that are pinned there: the first columns after a freeze
 * cannot be seen at all, and a click aimed at one of them lands on a
 * frozen cell, which is somebody else's data.
 */
describe('Frozen panes — where the scrolling cells start', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  function cell(col: number, row: number): HTMLElement | null {
    return ctx.sheet
      .getCellGrid()
      .shadowRoot!.querySelector<HTMLElement>(`.cell.col${col}.row${row}`);
  }

  function cellLeft(col: number, row: number): number {
    return cell(col, row)!.getBoundingClientRect().left;
  }

  it('starts the scrolling columns past the frozen ones and the freeze line', async () => {
    // Two 50px columns frozen, so column 3 starts 100px in, past the
    // freeze line's own pixel.
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    expect(cellLeft(3, 5) - cellLeft(1, 5)).to.equal(100 + 1);
  });

  it('starts the scrolling rows below the frozen ones and the freeze line', async () => {
    // Drawn over the line instead, the first row after a freeze would come
    // out a pixel shorter.
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    const top = (col: number, row: number): number =>
      cell(col, row)!.getBoundingClientRect().top;
    // 15pt rows are 20px at 96 ppi, so two frozen rows are 40px.
    expect(top(1, 3) - top(1, 1)).to.equal(40 + 1);
  });

  it('leaves the freeze line a pixel of its own, under no cell', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    const grid = ctx.sheet.getCellGrid().shadowRoot!;
    const [rowsLine, colsLine] = ['rows', 'columns'].map((along) =>
      [...grid.querySelectorAll<HTMLElement>('.freeze-line')]
        .map((l) => l.getBoundingClientRect())
        .find((b) => (along === 'rows' ? b.height === 1 : b.width === 1))!
    );
    const frozen = cell(2, 2)!.getBoundingClientRect();
    const scrolling = cell(3, 3)!.getBoundingClientRect();
    expect(rowsLine.top).to.equal(frozen.bottom);
    expect(scrolling.top).to.equal(rowsLine.bottom);
    expect(colsLine.left).to.equal(frozen.right);
    expect(scrolling.left).to.equal(colsLine.right);
  });

  it('lines the frozen rows up over the scrolling columns', async () => {
    // The top quadrant shares its column positions with the main one,
    // so a frozen row's cell sits directly above the scrolling cell in
    // the same column.
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    expect(cellLeft(4, 1)).to.equal(cellLeft(4, 5));
  });

  it('keeps them lined up once the sheet is scrolled', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.getScrollContainer().scrollLeft = 500;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;

    const first = ctx.sheet.getCellGrid().renderState.viewport
      .firstColumnIndex;
    expect(cellLeft(first, 1)).to.equal(cellLeft(first, 5));
    expect(cellLeft(first, 5)).to.be.at.least(cellLeft(1, 5) + 100);
  });

  it('heads the scrolling columns where they actually are', async () => {
    // The header reads the same positions, so a column's letter has to
    // stay over the column itself.
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getCellGrid().updateComplete;
    await ctx.sheet.getColumnHeader().updateComplete;

    const header = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.col-header-cell.col3')!;
    expect(Math.round(header.getBoundingClientRect().left)).to.equal(
      Math.round(cellLeft(3, 5))
    );
  });
});

/**
 * The frozen headers stay put while the rest scroll under them. The row
 * numbers had no background of their own, so a frozen row's number was
 * drawn over the scrolled one beneath it - 2 over 42.
 */
describe('Frozen panes — headers that cover what scrolls under them', () => {
  let sheet: Spreadsheet;
  let host: HTMLElement;

  beforeEach(async () => {
    host = document.createElement('div');
    host.style.cssText = 'width:600px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(100);
    sheet.setCols(30);
    sheet.setColW(new Array(30).fill(50));
    sheet.setRowH(new Array(100).fill(15));
    sheet.setVerticalSplitPosition(3);
    sheet.setHorizontalSplitPosition(2);
    sheet.recomputeViewport();
    const scroller = sheet.getScrollContainer();
    scroller.scrollTop = 400;
    scroller.scrollLeft = 400;
    scroller.dispatchEvent(new Event('scroll'));
    sheet.recomputeViewport();
    await sheet.getRowHeader().updateComplete;
    await sheet.getColumnHeader().updateComplete;
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  /** The header cell drawn on top at the middle of a frozen one. */
  function onTop(root: ShadowRoot, frozen: HTMLElement): Element | null {
    const box = frozen.getBoundingClientRect();
    return root.elementFromPoint(box.x + box.width / 2, box.y + box.height / 2);
  }

  it('covers the scrolled row numbers under a frozen one', () => {
    const root = sheet.getRowHeader().shadowRoot!;
    const frozen = root.querySelector<HTMLElement>('.frozen-header-strip .row-header-cell.row2')!;

    expect(getComputedStyle(frozen).backgroundColor).to.equal('rgb(250, 250, 250)');
    expect(onTop(root, frozen)?.closest('.row-header-cell')).to.equal(frozen);
  });

  it('covers the scrolled column letters under a frozen one', () => {
    const root = sheet.getColumnHeader().shadowRoot!;
    const frozen = root.querySelector<HTMLElement>('.frozen-header-strip .col-header-cell.col2')!;

    expect(getComputedStyle(frozen).backgroundImage).to.not.equal('none');
    expect(onTop(root, frozen)?.closest('.col-header-cell')).to.equal(frozen);
  });
});

/**
 * The line along a freeze, drawn through the headers and the outline gutters
 * as well as among the cells: it runs from the top of the column gutter and
 * from the left of the row gutter.
 */
describe('Frozen panes — the freeze line through the headers', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function settle(): Promise<void> {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;
    await ctx.sheet.getRowHeader().updateComplete;
    await ctx.sheet.getCellGrid().updateComplete;
  }

  function lines(): DOMRect[] {
    return [...ctx.sheet.getCellGrid().shadowRoot!.querySelectorAll<HTMLElement>('.freeze-line')].map((l) =>
      l.getBoundingClientRect()
    );
  }

  /** The freeze lines in a header strip's frozen part, top to bottom or left to right. */
  function headerLines(header: HTMLElement): HTMLElement[] {
    return [...header.shadowRoot!.querySelectorAll<HTMLElement>('.frozen-header-strip > .freeze-line')];
  }

  it('carries the line up through the letters and the gutter when rows are frozen too', async () => {
    ctx.sheet.setColGroupingMax(1);
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.setVerticalSplitPosition(2);
    await settle();

    const header = ctx.sheet.getColumnHeader();
    const [letters] = headerLines(header);
    const pane = header.shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip > .freeze-gutter-pane')!;
    const strip = header.getBoundingClientRect();
    // On the pixel the line among the cells is on: in the gutter the right
    // border of the frozen gutter pane, bordered along the bottom as the
    // gutter is; then down the letters over the strip's bottom border, in
    // the line's colour.
    const cellLine = lines().find((b) => b.width === 1)!;
    const paneBox = pane.getBoundingClientRect();
    expect(`${paneBox.right - 1} ${paneBox.top}`).to.equal(`${cellLine.left} ${strip.top}`);
    expect(getComputedStyle(pane).borderRight).to.equal('1px solid rgb(106, 106, 106)');
    expect(getComputedStyle(pane).borderBottom).to.equal('1px solid rgb(199, 199, 199)');
    const box = letters.getBoundingClientRect();
    expect(`${box.left} ${box.top} ${box.bottom}`).to.equal(`${cellLine.left} ${paneBox.bottom} ${strip.bottom}`);
    expect(getComputedStyle(letters).backgroundColor).to.equal('rgb(106, 106, 106)');
  });

  it('leaves the letters a see-through gap at the freeze line when only columns are frozen', async () => {
    // While no rows are frozen, no line runs through the letters. The pixel
    // at the freeze line is see-through: the sheet's white at rest, and the
    // letters that scroll across it as the sheet scrolls. The gutter's own
    // pane keeps its line.
    ctx.sheet.setColGroupingMax(1);
    ctx.sheet.setHorizontalSplitPosition(2);
    await settle();

    const header = ctx.sheet.getColumnHeader();
    expect(headerLines(header), 'no line through the letters').to.be.empty;
    const pane = header.shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip > .freeze-gutter-pane')!;
    expect(getComputedStyle(pane).borderRight, 'the gutter keeps its line').to.equal('1px solid rgb(106, 106, 106)');
    const gap = header.shadowRoot!.querySelector<HTMLElement>('.freeze-gap')!;
    const cellLine = lines().find((b) => b.width === 1)!;
    expect(getComputedStyle(gap).backgroundColor).to.equal('rgb(255, 255, 255)');
    expect(`${gap.getBoundingClientRect().left} ${gap.getBoundingClientRect().width}`).to.equal(`${cellLine.left} 1`);

    ctx.sheet.getScrollContainer().scrollLeft = 105;
    await settle();

    const box = gap.getBoundingClientRect();
    expect(box.left, 'pinned with the frozen letters').to.equal(cellLine.left);
    // A scrolled letter now lies across it, and is drawn over it.
    const under = [...header.shadowRoot!.querySelectorAll<HTMLElement>('.col-header-cell')].filter((c) => {
      const b = c.getBoundingClientRect();
      return b.left <= box.left && box.right <= b.right;
    });
    expect(under, 'a letter across the gap').to.not.be.empty;
    expect(getComputedStyle(gap.closest('.frozen-header-under')!).zIndex, 'under the letters').to.equal('-1');
  });

  it('carries the line across the numbers and the gutter when columns are frozen too', async () => {
    ctx.sheet.setRowGroupingMax(1);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.setHorizontalSplitPosition(2);
    await settle();

    const header = ctx.sheet.getRowHeader();
    const [numbers] = headerLines(header);
    const pane = header.shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip > .freeze-gutter-pane')!;
    const strip = header.getBoundingClientRect();
    const cellLine = lines().find((b) => b.height === 1)!;
    const paneBox = pane.getBoundingClientRect();
    expect(`${paneBox.bottom - 1} ${paneBox.left}`).to.equal(`${cellLine.top} ${strip.left}`);
    expect(getComputedStyle(pane).borderBottom).to.equal('1px solid rgb(106, 106, 106)');
    expect(getComputedStyle(pane).borderRight).to.equal('1px solid rgb(199, 199, 199)');
    const box = numbers.getBoundingClientRect();
    expect(`${box.top} ${box.left} ${box.right}`).to.equal(`${cellLine.top} ${paneBox.right} ${strip.right}`);
    expect(getComputedStyle(numbers).backgroundColor).to.equal('rgb(106, 106, 106)');
  });

  it('leaves the numbers a see-through gap at the freeze line when only rows are frozen', async () => {
    ctx.sheet.setRowGroupingMax(1);
    ctx.sheet.setVerticalSplitPosition(2);
    await settle();

    const header = ctx.sheet.getRowHeader();
    expect(headerLines(header), 'no line through the numbers').to.be.empty;
    const pane = header.shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip > .freeze-gutter-pane')!;
    expect(getComputedStyle(pane).borderBottom, 'the gutter keeps its line').to.equal('1px solid rgb(106, 106, 106)');
    const gap = header.shadowRoot!.querySelector<HTMLElement>('.freeze-gap')!;
    const cellLine = lines().find((b) => b.height === 1)!;
    expect(getComputedStyle(gap).backgroundColor).to.equal('rgb(255, 255, 255)');
    expect(`${gap.getBoundingClientRect().top} ${gap.getBoundingClientRect().height}`).to.equal(`${cellLine.top} 1`);

    ctx.sheet.getScrollContainer().scrollTop = 105;
    await settle();

    const box = gap.getBoundingClientRect();
    expect(box.top, 'pinned with the frozen numbers').to.equal(cellLine.top);
    // A scrolled number now lies across it, and is drawn over it.
    const under = [...header.shadowRoot!.querySelectorAll<HTMLElement>('.row-header-cell')].filter((c) => {
      const b = c.getBoundingClientRect();
      return b.top <= box.top && box.bottom <= b.bottom;
    });
    expect(under, 'a number across the gap').to.not.be.empty;
    expect(getComputedStyle(gap.closest('.frozen-header-under')!).zIndex, 'under the numbers').to.equal('-1');
  });

  it('shows the frozen columns\' line sliding up into the letters\' gap as the sheet scrolls down', async () => {
    // As the sheet scrolls down, the frozen columns move up under the
    // letters, and their line shows through the gap from the bottom up.
    ctx.sheet.setHorizontalSplitPosition(2);
    await settle();
    ctx.sheet.getScrollContainer().scrollTop = 10;
    await settle();

    const header = ctx.sheet.getColumnHeader();
    const [risen] = headerLines(header);
    const strip = header.getBoundingClientRect();
    const box = risen.getBoundingClientRect();
    const cellLine = lines().find((b) => b.width === 1)!;
    expect(`${box.left} ${box.width}`).to.equal(`${cellLine.left} 1`);
    expect(`${box.top} ${box.bottom}`).to.equal(`${strip.bottom - 10} ${strip.bottom}`);
    expect(getComputedStyle(risen).backgroundColor).to.equal('rgb(106, 106, 106)');
  });

  it('shows the frozen rows\' line sliding left into the numbers\' gap as the sheet scrolls right', async () => {
    // The line stays under the numbers, which are drawn over it.
    ctx.sheet.setVerticalSplitPosition(2);
    await settle();
    ctx.sheet.getScrollContainer().scrollLeft = 20;
    await settle();

    const header = ctx.sheet.getRowHeader();
    const slid = header.shadowRoot!.querySelector<HTMLElement>('.freeze-gap.slid')!;
    const strip = header.getBoundingClientRect();
    const box = slid.getBoundingClientRect();
    const cellLine = lines().find((b) => b.height === 1)!;
    expect(`${box.top} ${box.height}`).to.equal(`${cellLine.top} 1`);
    expect(`${box.left} ${box.right}`).to.equal(`${strip.right - 20} ${strip.right}`);
    expect(getComputedStyle(slid).backgroundColor).to.equal('rgb(106, 106, 106)');
    expect(getComputedStyle(slid.closest('.frozen-header-under')!).zIndex, 'under the numbers').to.equal('-1');
  });

  it('draws the dotted level lines over the freeze line in the gutters', async () => {
    // The level lines lie over the frozen strip, so a dot that falls on the
    // freeze line is drawn on it.
    ctx.sheet.setRowGroupingMax(1);
    ctx.sheet.setColGroupingMax(1);
    ctx.sheet.setVerticalSplitPosition(2);
    ctx.sheet.setHorizontalSplitPosition(2);
    await settle();

    for (const [header, separator] of [
      [ctx.sheet.getRowHeader(), '.row-grouping-separator'],
      [ctx.sheet.getColumnHeader(), '.col-grouping-separator'],
    ] as const) {
      const root = header.shadowRoot!;
      const strip = root.querySelector<HTMLElement>('.frozen-header-strip')!;
      const dots = root.querySelector<HTMLElement>(separator)!;
      expect(Number(getComputedStyle(dots).zIndex), separator).to.be.greaterThan(Number(getComputedStyle(strip).zIndex));
    }
  });

  it('keeps the line with the frozen columns as the sheet scrolls', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    ctx.sheet.setVerticalSplitPosition(2);
    await settle();
    const before = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.freeze-line')!
      .getBoundingClientRect().left;

    ctx.sheet.getScrollContainer().scrollLeft = 500;
    await settle();

    const after = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.freeze-line')!
      .getBoundingClientRect().left;
    expect(after).to.equal(before);
  });

  it('draws no line in the headers when nothing is frozen', async () => {
    await settle();

    expect(ctx.sheet.getColumnHeader().shadowRoot!.querySelector('.freeze-line')).to.be.null;
    expect(ctx.sheet.getRowHeader().shadowRoot!.querySelector('.freeze-line')).to.be.null;
  });
});

describe('Frozen panes — the scroll area stays the size of the sheet', () => {
  // What keeps the frozen headers in place must not reach past the end of
  // the sheet: every scroll to the bottom would make the scroll area longer,
  // into empty space.
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function scrollTo(top: number, left: number): Promise<void> {
    const pane = ctx.sheet.getScrollContainer();
    pane.scrollTop = top;
    pane.scrollLeft = left;
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getRowHeader().updateComplete;
    await ctx.sheet.getColumnHeader().updateComplete;
  }

  it('keeps the sheet as tall as it is when scrolled to the bottom with rows frozen', async () => {
    ctx.sheet.setVerticalSplitPosition(5);
    await scrollTo(0, 0);
    const pane = ctx.sheet.getScrollContainer();
    const tall = pane.scrollHeight;

    await scrollTo(tall, 0);
    await scrollTo(tall, 0);

    expect(pane.scrollHeight).to.equal(tall);
  });

  it('keeps the sheet as wide as it is when scrolled to the end with columns frozen', async () => {
    ctx.sheet.setHorizontalSplitPosition(3);
    await scrollTo(0, 0);
    const pane = ctx.sheet.getScrollContainer();
    const wide = pane.scrollWidth;

    await scrollTo(0, wide);
    await scrollTo(0, wide);

    expect(pane.scrollWidth).to.equal(wide);
  });
});

/**
 * The active cell's letter and number carry a marker along the edge that
 * faces the cells, drawn over the header's own border. Clipping the whole
 * header to keep the frozen strip from growing the scroll area cut that
 * pixel off, and the baseline screenshots caught it.
 */
describe('Headers — the active marker over the border', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  async function draw(): Promise<void> {
    ctx.sheet.recomputeViewport();
    await ctx.sheet.getColumnHeader().updateComplete;
    await ctx.sheet.getRowHeader().updateComplete;
  }

  /** What is drawn on the header's last pixel along the cells, at the middle of the given letter. */
  function atLettersEdge(letter: HTMLElement): Element | null {
    const header = ctx.sheet.getColumnHeader();
    const box = letter.getBoundingClientRect();
    return header.shadowRoot!.elementFromPoint(box.left + box.width / 2, header.getBoundingClientRect().bottom - 0.5);
  }

  function atNumbersEdge(number: HTMLElement): Element | null {
    const header = ctx.sheet.getRowHeader();
    const box = number.getBoundingClientRect();
    return header.shadowRoot!.elementFromPoint(header.getBoundingClientRect().right - 0.5, box.top + box.height / 2);
  }

  it('draws the active letter over the bottom border of the letters', async () => {
    await draw();

    const a = ctx.sheet.getColumnHeader().shadowRoot!.querySelector<HTMLElement>('.col-header-cell.col1')!;
    expect(a.getAttribute('part')).to.include('header-selected');
    expect(atLettersEdge(a)).to.equal(a);
  });

  it('draws a frozen active letter over it too', async () => {
    ctx.sheet.setHorizontalSplitPosition(2);
    await draw();

    const a = ctx.sheet
      .getColumnHeader()
      .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip .col-header-cell.col1')!;
    expect(atLettersEdge(a)).to.equal(a);
  });

  it('draws the active number over the right border of the numbers', async () => {
    await draw();

    const one = ctx.sheet.getRowHeader().shadowRoot!.querySelector<HTMLElement>('.row-header-cell.row1')!;
    expect(one.getAttribute('part')).to.include('header-selected');
    expect(atNumbersEdge(one)).to.equal(one);
  });

  it('draws a frozen active number over it too', async () => {
    ctx.sheet.setVerticalSplitPosition(2);
    await draw();

    const one = ctx.sheet
      .getRowHeader()
      .shadowRoot!.querySelector<HTMLElement>('.frozen-header-strip .row-header-cell.row1')!;
    expect(atNumbersEdge(one)).to.equal(one);
  });
});
