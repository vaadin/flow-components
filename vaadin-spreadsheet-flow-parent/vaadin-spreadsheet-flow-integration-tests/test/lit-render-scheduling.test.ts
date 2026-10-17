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
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * Server-pushed updates must repaint.
 *
 * Before this slice the four `update*CellValues` methods stored cell
 * data and never painted, `cellsUpdated` did nothing at all, and
 * `refreshCellStyles` / `setReload` were empty. The data arrived and
 * then waited for an unrelated trigger (a scroll, a resize, some other
 * property write) to reach the screen.
 *
 * These tests pin two things: that each of those callbacks paints, and
 * that a batch of them paints only once.
 */

function cell(col: number, row: number, value: string): CellData {
    return {
        col,
        row,
        value,
        cellStyle: 'cs0',
        locked: false,
        needsMeasure: false,
        isPercentage: false,
    };
}

async function attachSheet(): Promise<{
    sheet: Spreadsheet;
    /** Repaints counted since the sheet settled after attach. */
    paints: () => number;
    cleanup: () => void;
}> {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    const sheet = new Spreadsheet(host);

    // Count repaints by wrapping the public entry point the scheduler
    // calls. Spying beats asserting on rendered DOM here: the point of
    // the slice is *that* a paint is requested, and how many times.
    let count = 0;
    const original = sheet.recomputeViewport.bind(sheet);
    sheet.recomputeViewport = () => {
        count += 1;
        original();
    };

    // The constructor's ResizeObserver paints once when the host is
    // first observed. Let that settle and start counting from zero, so
    // the assertions below measure only what the server pushes cause.
    await afterFrame();
    count = 0;

    return {
        sheet,
        paints: () => count,
        cleanup: () => {
            sheet.dispose();
            host.remove();
        },
    };
}

/** Wait long enough for a scheduled animation frame to have run. */
function afterFrame(): Promise<void> {
    return new Promise((resolve) => {
        requestAnimationFrame(() => requestAnimationFrame(() => resolve()));
    });
}

describe('Spreadsheet render scheduling — server pushes repaint', () => {
    let ctx: Awaited<ReturnType<typeof attachSheet>>;

    beforeEach(async () => {
        ctx = await attachSheet();
    });

    afterEach(() => {
        ctx.cleanup();
    });

    it('cellsUpdated stores the new values and repaints', async () => {
        ctx.sheet.cellsUpdated([cell(1, 1, '42')]);
        await afterFrame();

        expect(ctx.paints()).to.equal(1);
        expect(ctx.sheet.getCellData().get('col1 row1')?.value).to.equal('42');
    });

    it('cellsReplaced keeps only the cells it is given, and repaints', async () => {
        ctx.sheet.cellsUpdated([cell(1, 1, 'old'), cell(9, 90, 'far away')]);
        await afterFrame();
        const before = ctx.paints();

        ctx.sheet.cellsReplaced([cell(1, 1, 'fresh')]);
        await afterFrame();

        expect(ctx.paints()).to.equal(before + 1);
        expect(ctx.sheet.getCellData().get('col1 row1')?.value).to.equal(
            'fresh'
        );
        expect(ctx.sheet.getCellData().has('col9 row90')).to.equal(false);
    });

    it('cellsUpdated overwrites the value already held for a cell', async () => {
        ctx.sheet.updateBottomRightCellValues([cell(1, 1, 'stale')]);
        await afterFrame();

        ctx.sheet.cellsUpdated([cell(1, 1, 'fresh')]);
        await afterFrame();

        expect(ctx.sheet.getCellData().get('col1 row1')?.value).to.equal(
            'fresh'
        );
    });

    [
        'updateTopLeftCellValues',
        'updateTopRightCellValues',
        'updateBottomLeftCellValues',
        'updateBottomRightCellValues',
    ].forEach((method) => {
        it(`${method} repaints`, async () => {
            (
                ctx.sheet[method as keyof Spreadsheet] as (
                    data: CellData[]
                ) => void
            ).call(ctx.sheet, [cell(1, 1, 'x')]);
            await afterFrame();

            expect(ctx.paints()).to.equal(1);
        });
    });

    it('refreshCellStyles repaints', async () => {
        ctx.sheet.refreshCellStyles();
        await afterFrame();

        expect(ctx.paints()).to.equal(1);
    });

    it('setReload(true) repaints', async () => {
        ctx.sheet.setReload(true);
        await afterFrame();

        expect(ctx.paints()).to.equal(1);
    });

    it('setDisplayGridlines repaints', async () => {
        // Turned off from the server on its own, with no cell data to carry
        // a repaint along - an application's setGridlinesVisible(false).
        ctx.sheet.setDisplayGridlines(false);
        await afterFrame();

        expect(ctx.paints()).to.equal(1);
    });

    it('setReload(false) does not repaint', async () => {
        ctx.sheet.setReload(false);
        await afterFrame();

        expect(ctx.paints()).to.equal(0);
    });
});

describe('Spreadsheet render scheduling — when the repaint lands', () => {
    let ctx: Awaited<ReturnType<typeof attachSheet>>;

    beforeEach(async () => {
        ctx = await attachSheet();
    });

    afterEach(() => {
        ctx.cleanup();
    });

    it('repaints within the same turn, not on the next frame', async () => {
        // Everything that queues a repaint is server-driven, and the
        // caller has already been told the round trip finished. Waiting
        // for a frame leaves the DOM behind the answer; a microtask still
        // folds a whole response into one render.
        ctx.sheet.updateBottomRightCellValues([cell(1, 1, 'fresh')]);

        await Promise.resolve();
        await ctx.sheet.getCellGrid().updateComplete;

        expect(
            ctx.sheet.getCellGrid().shadowRoot!.querySelector('.cell.col1.row1')!
                .textContent!.trim()
        ).to.equal('fresh');
    });

    it('folds every push in one response into a single repaint', async () => {
        const grid = ctx.sheet.getCellGrid();
        let renders = 0;
        const originalUpdate = grid.updated.bind(grid);
        (grid as any).updated = (changed: any) => {
            renders += 1;
            return originalUpdate(changed);
        };

        // Four quadrant pushes, as one response delivers them.
        ctx.sheet.updateTopLeftCellValues([cell(1, 1, 'a')]);
        ctx.sheet.updateTopRightCellValues([cell(2, 1, 'b')]);
        ctx.sheet.updateBottomLeftCellValues([cell(1, 2, 'c')]);
        ctx.sheet.updateBottomRightCellValues([cell(2, 2, 'd')]);

        await Promise.resolve();
        await grid.updateComplete;
        await afterFrame();

        expect(renders).to.equal(1);
    });
});

describe('Spreadsheet render scheduling — end of a server batch', () => {
    let ctx: Awaited<ReturnType<typeof attachSheet>>;

    beforeEach(async () => {
        ctx = await attachSheet();
    });

    afterEach(() => {
        ctx.cleanup();
    });

    it('has painted by the time notifyStateChanges returns', async () => {
        // This runs while the browser is still processing the server's
        // response, so painting here keeps the rule callers rely on: once a
        // round trip is finished, the sheet shows its result. A deferred
        // paint leaves the DOM a frame behind, and every test that acts and
        // then reads is racing it.
        ctx.sheet.updateBottomRightCellValues([cell(1, 1, 'fresh')]);
        ctx.sheet.notifyStateChanges(['cellStyleToCSSStyle'], false);
        await ctx.sheet.getCellGrid().updateComplete;

        const painted = ctx.sheet
            .getCellGrid()
            .shadowRoot!.querySelector('.cell.col1.row1')!;
        expect(painted.textContent!.trim()).to.equal('fresh');
    });

    it('drops a frame already scheduled rather than painting twice', async () => {
        ctx.sheet.updateBottomRightCellValues([cell(1, 1, 'fresh')]);
        let renders = 0;
        const grid = ctx.sheet.getCellGrid();
        const originalUpdate = grid.updated.bind(grid);
        (grid as any).updated = (changed: any) => {
            renders += 1;
            return originalUpdate(changed);
        };

        ctx.sheet.notifyStateChanges(['cellStyleToCSSStyle'], false);
        await grid.updateComplete;
        const afterFlush = renders;
        await afterFrame();

        expect(renders).to.equal(afterFlush);
    });

    it('paints nothing when the batch changed nothing', async () => {
        ctx.sheet.updateBottomRightCellValues([cell(1, 1, 'fresh')]);
        await afterFrame();
        const before = ctx.sheet
            .getCellGrid()
            .shadowRoot!.querySelector('.cell.col1.row1')!.textContent;

        ctx.sheet.notifyStateChanges([], false);

        expect(
            ctx.sheet.getCellGrid().shadowRoot!.querySelector('.cell.col1.row1')!
                .textContent
        ).to.equal(before);
    });
});

describe('Spreadsheet render scheduling — coalescing', () => {
    let ctx: Awaited<ReturnType<typeof attachSheet>>;

    beforeEach(async () => {
        ctx = await attachSheet();
    });

    afterEach(() => {
        ctx.cleanup();
    });

    it('a batch of pushes in one roundtrip paints once', async () => {
        ctx.sheet.updateTopLeftCellValues([cell(1, 1, 'a')]);
        ctx.sheet.updateTopRightCellValues([cell(2, 1, 'b')]);
        ctx.sheet.updateBottomLeftCellValues([cell(1, 2, 'c')]);
        ctx.sheet.updateBottomRightCellValues([cell(2, 2, 'd')]);
        ctx.sheet.refreshCellStyles();
        ctx.sheet.setReload(true);
        await afterFrame();

        expect(ctx.paints()).to.equal(1);
    });

    it('every cell from a coalesced batch is still stored', async () => {
        ctx.sheet.updateTopLeftCellValues([cell(1, 1, 'a')]);
        ctx.sheet.updateBottomRightCellValues([cell(2, 2, 'd')]);
        await afterFrame();

        const data = ctx.sheet.getCellData();
        expect(data.get('col1 row1')?.value).to.equal('a');
        expect(data.get('col2 row2')?.value).to.equal('d');
    });

    it('a later push after the frame paints again', async () => {
        ctx.sheet.cellsUpdated([cell(1, 1, 'first')]);
        await afterFrame();
        ctx.sheet.cellsUpdated([cell(1, 1, 'second')]);
        await afterFrame();

        expect(ctx.paints()).to.equal(2);
    });
});

describe('Spreadsheet render scheduling — lifecycle', () => {
    it('dispose cancels a repaint that has not run yet', async () => {
        const ctx = await attachSheet();

        ctx.sheet.cellsUpdated([cell(1, 1, 'x')]);
        // Dispose in the same task, before the frame fires.
        ctx.cleanup();
        await afterFrame();

        expect(ctx.paints()).to.equal(0);
    });
});

describe('Spreadsheet render scheduling — property batches', () => {
  let ctx: Awaited<ReturnType<typeof attachSheet>>;

  beforeEach(async () => {
    ctx = await attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('repaints after the wrapper reports a property batch', async () => {
    // The wrapper forwards each changed property and then calls this once.
    // Server-pushed state that is not a cell value has no other repaint
    // trigger.
    ctx.sheet.setOverlays({
      i1: { type: 'IMAGE', col: 2, row: 2, width: 40, height: 30, dx: 0, dy: 0 }
    });
    ctx.sheet.notifyStateChanges(['overlays'], false);
    await afterFrame();

    expect(ctx.paints()).to.equal(1);
  });

  it('folds a whole batch into one repaint', async () => {
    ctx.sheet.notifyStateChanges(['overlays', 'mergedRegions', 'cellComments'], false);
    await afterFrame();

    expect(ctx.paints()).to.equal(1);
  });

  it('does not repaint when nothing changed', async () => {
    ctx.sheet.notifyStateChanges([], false);
    await afterFrame();

    expect(ctx.paints()).to.equal(0);
  });

  it('the overlay reaches the cell grid once the batch is reported', async () => {
    ctx.sheet.setOverlays({
      i1: { type: 'IMAGE', col: 2, row: 2, width: 40, height: 30, dx: 0, dy: 0 }
    });
    ctx.sheet.notifyStateChanges(['overlays'], false);
    await afterFrame();

    expect(ctx.sheet.getCellGrid().renderState.overlays).to.have.length(1);
  });
});

describe('Spreadsheet startup handshake', () => {
  function attachRaw(): {
    host: HTMLElement;
    sheet: Spreadsheet;
    events: any[];
    cleanup: () => void;
  } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    const events: any[] = [];
    host.addEventListener('spreadsheet-event', (e) =>
      events.push((e as CustomEvent).detail)
    );
    const sheet = new Spreadsheet(host);
    return { host, sheet, events, cleanup: () => { sheet.dispose(); host.remove(); } };
  }

  function giveGeometry(sheet: Spreadsheet): void {
    sheet.setCols(10);
    sheet.setRows(10);
    sheet.setColW(new Array(10).fill(50));
    sheet.setRowH(new Array(10).fill(15));
  }

  it('tells the server it is live and where the viewport starts', () => {
    // The server loads overlays, hyperlinks, comments, tables, popup buttons
    // and custom editors only from loadCells, and loadCells runs off
    // onSheetScroll. Without this handshake none of that content ever loads.
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.recomputeViewport();

    const types = ctx.events.map((e) => e.type);
    expect(types).to.include('onConnectorInit');
    expect(types).to.include('onSheetScroll');
    ctx.cleanup();
  });

  it('announces the connector before the viewport', () => {
    // onConnectorInit marks the next scroll as a full reload; arriving after
    // the scroll would leave the server diffing against bounds it never saw.
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.recomputeViewport();

    const types = ctx.events.map((e) => e.type);
    expect(types.indexOf('onConnectorInit')).to.be.lessThan(
      types.indexOf('onSheetScroll')
    );
    ctx.cleanup();
  });

  it('reports the measured viewport bounds', () => {
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.recomputeViewport();

    const scroll = ctx.events.find((e) => e.type === 'onSheetScroll');
    expect(scroll.data.firstRow).to.be.greaterThan(0);
    expect(scroll.data.firstColumn).to.be.greaterThan(0);
    expect(scroll.data.lastRow).to.be.greaterThanOrEqual(scroll.data.firstRow);
    ctx.cleanup();
  });

  it('announces only once, however many repaints follow', () => {
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.recomputeViewport();
    ctx.sheet.recomputeViewport();
    ctx.sheet.recomputeViewport();

    const inits = ctx.events.filter((e) => e.type === 'onConnectorInit');
    expect(inits).to.have.length(1);
    ctx.cleanup();
  });

  it('says nothing more while the visible range stays put', () => {
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.recomputeViewport();
    ctx.events.length = 0;

    ctx.sheet.recomputeViewport();
    ctx.sheet.recomputeViewport();

    expect(ctx.events.filter((e) => e.type === 'onSheetScroll')).to.be.empty;
    ctx.cleanup();
  });

  it('announces the wider viewport when the sheet grows', () => {
    // Only scrolling dispatches a viewport of its own, and plenty of
    // things resize the sheet without one — a tab revealing it, a split
    // pane opening, the window resizing. A sheet that came up 0 wide
    // inside a hidden tab kept the viewport it measured while hidden, so
    // the cells and popup buttons outside it never loaded.
    const ctx = attachRaw();
    // Narrow enough that the first measurement cannot reach the far
    // columns, and wide enough afterwards that it plainly can.
    ctx.host.style.width = '80px';
    ctx.sheet.setCols(60);
    ctx.sheet.setRows(60);
    ctx.sheet.setColW(new Array(60).fill(50));
    ctx.sheet.setRowH(new Array(60).fill(15));
    ctx.sheet.recomputeViewport();
    const first = ctx.events.find((e) => e.type === 'onSheetScroll');
    expect(first, 'nothing announced at all').to.not.be.undefined;
    ctx.events.length = 0;

    ctx.host.style.width = '1200px';
    ctx.sheet.recomputeViewport();

    const second = ctx.events.find((e) => e.type === 'onSheetScroll');
    expect(second, 'the wider viewport was never announced').to.not.be.undefined;
    expect(second.data.lastColumn).to.be.greaterThan(first.data.lastColumn);
    ctx.cleanup();
  });

  it('does not repeat the handshake when it re-announces', () => {
    const ctx = attachRaw();
    ctx.host.style.width = '80px';
    ctx.sheet.setCols(60);
    ctx.sheet.setRows(60);
    ctx.sheet.setColW(new Array(60).fill(50));
    ctx.sheet.setRowH(new Array(60).fill(15));
    ctx.sheet.recomputeViewport();
    ctx.events.length = 0;

    ctx.host.style.width = '1200px';
    ctx.sheet.recomputeViewport();

    expect(ctx.events.filter((e) => e.type === 'onConnectorInit')).to.be.empty;
    ctx.cleanup();
  });

  it('announces again when the server says it dropped the sheet state', async () => {
    // reloadActiveSheetData clears the server's overlay/comment/hyperlink
    // maps and resets its viewport bounds to -1, then raises the reload
    // flag. Only onSheetScroll makes it call loadCells again, so without a
    // second announcement the sheet keeps its cells and loses everything
    // else until the user happens to scroll.
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.recomputeViewport();
    ctx.events.length = 0;

    ctx.sheet.setReload(true);
    await afterFrame();

    const types = ctx.events.map((e) => e.type);
    expect(types).to.include('onConnectorInit');
    expect(types).to.include('onSheetScroll');
    expect(types.indexOf('onConnectorInit')).to.be.lessThan(
      types.indexOf('onSheetScroll')
    );
    ctx.cleanup();
  });

  it('drops the cached cell values when the server drops the sheet', async () => {
    // The server only ever sends cells that have something in them, so a
    // cell that is empty on the new sheet is never mentioned again. Keeping
    // the old value showed the previous sheet's content through the new one.
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.updateBottomRightCellValues([
      { col: 1, row: 1, value: 'sheet0 value', cellStyle: 'cs0' }
    ] as any);
    expect(ctx.sheet.getCellData().size).to.be.greaterThan(0);

    ctx.sheet.setReload(true);
    await afterFrame();

    expect(ctx.sheet.getCellData().get('col1 row1')).to.be.undefined;
    ctx.cleanup();
  });

  it('keeps the cached cell values when the flag is lowered', async () => {
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.updateBottomRightCellValues([
      { col: 1, row: 1, value: 'still here', cellStyle: 'cs0' }
    ] as any);

    ctx.sheet.setReload(false);
    await afterFrame();

    expect(ctx.sheet.getCellData().get('col1 row1')?.value).to.equal('still here');
    ctx.cleanup();
  });

  it('says nothing when the reload flag is lowered rather than raised', async () => {
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.recomputeViewport();
    ctx.events.length = 0;

    ctx.sheet.setReload(false);
    await afterFrame();

    expect(ctx.events).to.have.length(0);
    ctx.cleanup();
  });

  it('re-announces once per reload, not once per following repaint', async () => {
    const ctx = attachRaw();
    giveGeometry(ctx.sheet);
    ctx.sheet.recomputeViewport();
    ctx.events.length = 0;

    ctx.sheet.setReload(true);
    await afterFrame();
    ctx.sheet.recomputeViewport();
    ctx.sheet.recomputeViewport();

    const inits = ctx.events.filter((e) => e.type === 'onConnectorInit');
    expect(inits).to.have.length(1);
    ctx.cleanup();
  });

  it('waits for a measurable viewport before announcing', () => {
    // A zero-size host measures nothing; announcing then would tell the
    // server an empty range and it would load nothing.
    const host = document.createElement('div');
    host.style.cssText = 'width:0px;height:0px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    const events: any[] = [];
    host.addEventListener('spreadsheet-event', (e) =>
      events.push((e as CustomEvent).detail)
    );
    const sheet = new Spreadsheet(host);
    sheet.recomputeViewport();

    expect(events.map((e) => e.type)).to.not.include('onConnectorInit');
    sheet.dispose();
    host.remove();
  });
});
