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
 * Slice 183 — server-pushed updates must repaint.
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
    host.style.cssText = 'width:400px;height:300px;';
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

    it('setReload(false) does not repaint', async () => {
        ctx.sheet.setReload(false);
        await afterFrame();

        expect(ctx.paints()).to.equal(0);
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
