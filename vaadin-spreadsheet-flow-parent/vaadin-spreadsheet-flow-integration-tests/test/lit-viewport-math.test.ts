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
    computeTotalGridSize,
    computeViewportBounds,
    convertPointsToPixel,
    FALLBACK_PPI,
    getDefaultRowHeightPx,
    type ViewportInput,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/ViewportMath.js';

describe('ViewportMath — unit conversion', () => {
    it('converts points to pixels at 96 ppi (Excel default)', () => {
        // 12 pt → 16 px (12 * 96 / 72 = 16)
        expect(convertPointsToPixel(12, 96)).to.equal(16);
        // 15 pt → 20 px (default row height in Excel)
        expect(convertPointsToPixel(15, 96)).to.equal(20);
    });

    it('truncates toward zero (matching Java BigDecimal.intValue)', () => {
        // 13.5 pt at 96 ppi → 18.0 px exactly (no truncation visible)
        expect(convertPointsToPixel(13.5, 96)).to.equal(18);
        // 14.7 pt → 19.6 px → truncates to 19, NOT rounds to 20
        expect(convertPointsToPixel(14.7, 96)).to.equal(19);
    });

    it('handles non-default PPI (retina, custom DPI)', () => {
        expect(convertPointsToPixel(12, 192)).to.equal(32);
        expect(convertPointsToPixel(12, 72)).to.equal(12);
    });

    it('exposes the 96 ppi fallback constant', () => {
        expect(FALLBACK_PPI).to.equal(96);
    });
});

describe('ViewportMath — getDefaultRowHeightPx', () => {
    it('converts 15-point Excel default to 20 px at 96 ppi', () => {
        expect(getDefaultRowHeightPx(15, 96)).to.equal(20);
    });

    it('falls back to 96 ppi when ppi is 0', () => {
        // 15 pt * 96 / 72 = 20
        expect(getDefaultRowHeightPx(15, 0)).to.equal(20);
    });

    it('falls back to 96 ppi when ppi is negative', () => {
        expect(getDefaultRowHeightPx(15, -1)).to.equal(20);
    });

    it('uses the supplied ppi when it is positive', () => {
        expect(getDefaultRowHeightPx(15, 192)).to.equal(40);
    });
});

describe('ViewportMath — computeViewportBounds — no freeze, simple scroll', () => {
    function buildInput(overrides: Partial<ViewportInput> = {}): ViewportInput {
        return {
            scrollLeft: 0,
            scrollTop: 0,
            scrollViewWidth: 300,
            scrollViewHeight: 200,
            horizontalSplitPosition: 0,
            verticalSplitPosition: 0,
            columnBufferSize: 0,
            rowBufferSize: 0,
            maxColumns: 100,
            maxRows: 100,
            definedRows: 50,
            defaultRowHeightPx: 20,
            getColWidthActual: () => 50, // uniform 50px
            getRowHeight: () => 20, // uniform 20px
            ...overrides,
        };
    }

    it('starts at column 1 / row 1 when scroll is 0', () => {
        const r = computeViewportBounds(buildInput());
        expect(r.firstColumnIndex).to.equal(1);
        expect(r.firstColumnPosition).to.equal(0);
        expect(r.firstRowIndex).to.equal(1);
        expect(r.firstRowPosition).to.equal(0);
    });

    it('extends last index until viewport is covered', () => {
        const r = computeViewportBounds(buildInput());
        // 300px wide viewport / 50px columns = 6 cols. Loop continues
        // while lastColumnPosition < scrollLeft + viewportW; lastColumnPosition
        // starts at 50 (one column's width) and adds 50 per iteration.
        // 50 → 100 → 150 → 200 → 250 → 300 (loop exits at >= 300).
        // That's 5 increments → lastColumnIndex = 6.
        expect(r.lastColumnIndex).to.equal(6);
        expect(r.lastColumnPosition).to.equal(300);
    });

    it('scrolls past visible columns when scrollLeft is large', () => {
        const r = computeViewportBounds(
            buildInput({ scrollLeft: 200, scrollTop: 0 })
        );
        // firstColumnPosition advances while < 200: 50, 100, 150, 200 (stop)
        // 4 increments → firstColumnIndex = 5
        expect(r.firstColumnIndex).to.equal(5);
        expect(r.firstColumnPosition).to.equal(200);
    });

    it('caps lastColumnIndex at maxColumns when sheet ends inside viewport', () => {
        const r = computeViewportBounds(
            buildInput({ maxColumns: 4, scrollViewWidth: 1000 })
        );
        expect(r.lastColumnIndex).to.equal(4);
    });

    it('caps lastRowIndex at maxRows when sheet ends inside viewport', () => {
        const r = computeViewportBounds(
            buildInput({ maxRows: 3, scrollViewHeight: 1000 })
        );
        expect(r.lastRowIndex).to.equal(3);
    });
});

describe('ViewportMath — computeViewportBounds — buffer', () => {
    it('applies the column buffer to widen the first/last range on both sides', () => {
        const baseInput: ViewportInput = {
            scrollLeft: 200,
            scrollTop: 0,
            scrollViewWidth: 100,
            scrollViewHeight: 100,
            horizontalSplitPosition: 0,
            verticalSplitPosition: 0,
            columnBufferSize: 50,
            rowBufferSize: 0,
            maxColumns: 100,
            maxRows: 100,
            definedRows: 50,
            defaultRowHeightPx: 20,
            getColWidthActual: () => 50,
            getRowHeight: () => 20,
        };
        const r = computeViewportBounds(baseInput);
        // Without buffer: firstColumnIndex = 5 (firstColumnPosition = 200 satisfies < 200 false).
        // With buffer 50: scrollLeft - buffer = 150; advance while < 150:
        //   50, 100, 150 (stop). 3 increments → firstColumnIndex = 4.
        expect(r.firstColumnIndex).to.equal(4);
        // Last extends while < scrollLeft + viewportW + buffer = 200 + 100 + 50 = 350.
        // lastColumnPosition starts at firstColumnPosition + colWidth = 150 + 50 = 200.
        // Then advances: 250, 300, 350 (stop). 3 increments → lastColumnIndex = 7.
        expect(r.lastColumnIndex).to.equal(7);
    });
});

describe('ViewportMath — computeViewportBounds — freeze panes', () => {
    function buildInput(overrides: Partial<ViewportInput> = {}): ViewportInput {
        return {
            scrollLeft: 0,
            scrollTop: 0,
            scrollViewWidth: 300,
            scrollViewHeight: 200,
            horizontalSplitPosition: 2, // freeze cols 1-2
            verticalSplitPosition: 0,
            columnBufferSize: 0,
            rowBufferSize: 0,
            maxColumns: 100,
            maxRows: 100,
            definedRows: 50,
            defaultRowHeightPx: 20,
            getColWidthActual: () => 50,
            getRowHeight: () => 20,
            ...overrides,
        };
    }

    it('starts the scroll pane at horizontalSplit + 1 when freeze is active', () => {
        const r = computeViewportBounds(buildInput());
        expect(r.firstColumnIndex).to.equal(3); // first non-frozen
        expect(r.firstColumnPosition).to.equal(0);
    });

    it('starts the scroll pane at verticalSplit + 1 for the row freeze', () => {
        const r = computeViewportBounds(buildInput({
            horizontalSplitPosition: 0,
            verticalSplitPosition: 3, // freeze rows 1-3
        }));
        expect(r.firstRowIndex).to.equal(4);
        expect(r.firstRowPosition).to.equal(0);
    });
});

describe('ViewportMath — computeViewportBounds — rows beyond definedRows', () => {
    it('uses defaultRowHeightPx for rows >= definedRows during advance', () => {
        const r = computeViewportBounds({
            scrollLeft: 0,
            scrollTop: 100, // scroll past first 5 rows
            scrollViewWidth: 100,
            scrollViewHeight: 200,
            horizontalSplitPosition: 0,
            verticalSplitPosition: 0,
            columnBufferSize: 0,
            rowBufferSize: 0,
            maxColumns: 100,
            maxRows: 100,
            definedRows: 3, // rows 1-3 have defined heights
            defaultRowHeightPx: 25, // rows 4+ use this
            getColWidthActual: () => 50,
            getRowHeight: () => 20, // for rows 1-3
        });
        // firstRowPosition advances while < 100, then firstRowIndex++.
        // The >= definedRows check is on the CURRENT firstRowIndex,
        // BEFORE the post-iter increment.
        // Iter 1: 0 < 100; idx 1 >= 3? no → getRowHeight(1)=20. pos=20, idx=2.
        // Iter 2: 20 < 100; idx 2 >= 3? no → 20. pos=40, idx=3.
        // Iter 3: 40 < 100; idx 3 >= 3? YES → default 25. pos=65, idx=4.
        // Iter 4: 65 < 100; idx 4 >= 3? YES → 25. pos=90, idx=5.
        // Iter 5: 90 < 100; idx 5 >= 3? YES → 25. pos=115, idx=6.
        //   115 < 100? no. Stop.
        expect(r.firstRowIndex).to.equal(6);
        expect(r.firstRowPosition).to.equal(115);
    });
});

describe('ViewportMath — computeTotalGridSize', () => {
    it('sums all defined row heights + column widths', () => {
        // 3 cols × widths 50/60/40 = 150 px wide.
        // 2 rows × heights 20/30 = 50 px tall.
        const result = computeTotalGridSize({
            rows: 2,
            cols: 3,
            definedRowHeightsPx: [20, 30],
            definedColWidthsPx: [50, 60, 40],
            hiddenRows: new Set(),
            hiddenColumns: new Set(),
            defaultRowHeightPx: 18,
            defaultColWidthPx: 64,
        });

        expect(result.totalWidthPx).to.equal(150);
        expect(result.totalHeightPx).to.equal(50);
    });

    it('uses defaults for rows + columns beyond the defined arrays', () => {
        // 5 rows total; defined heights cover rows 1..2; rows 3..5 use
        // the default 18px. Total = 20 + 30 + 3*18 = 104.
        // 4 cols total; defined widths cover col 1; cols 2..4 use default
        // 64px. Total = 50 + 3*64 = 242.
        const result = computeTotalGridSize({
            rows: 5,
            cols: 4,
            definedRowHeightsPx: [20, 30],
            definedColWidthsPx: [50],
            hiddenRows: new Set(),
            hiddenColumns: new Set(),
            defaultRowHeightPx: 18,
            defaultColWidthPx: 64,
        });

        expect(result.totalWidthPx).to.equal(242);
        expect(result.totalHeightPx).to.equal(104);
    });

    it('hidden rows contribute 0 to total height', () => {
        // 3 rows × heights 20/30/40. Row 2 hidden → total = 20 + 0 + 40 = 60.
        const result = computeTotalGridSize({
            rows: 3,
            cols: 1,
            definedRowHeightsPx: [20, 30, 40],
            definedColWidthsPx: [50],
            hiddenRows: new Set([2]),
            hiddenColumns: new Set(),
            defaultRowHeightPx: 18,
            defaultColWidthPx: 64,
        });

        expect(result.totalHeightPx).to.equal(60);
    });

    it('hidden columns contribute 0 to total width', () => {
        // 3 cols × widths 40/50/60. Cols 1 + 3 hidden → total = 0 + 50 + 0 = 50.
        const result = computeTotalGridSize({
            rows: 1,
            cols: 3,
            definedRowHeightsPx: [20],
            definedColWidthsPx: [40, 50, 60],
            hiddenRows: new Set(),
            hiddenColumns: new Set([1, 3]),
            defaultRowHeightPx: 18,
            defaultColWidthPx: 64,
        });

        expect(result.totalWidthPx).to.equal(50);
    });

    it('hidden rows beyond definedRowHeightsPx still contribute 0', () => {
        // 5 rows, defined heights cover 1..2 (20+30). Default 18.
        // Hide row 4 (beyond defined) → total = 20 + 30 + 18 + 0 + 18 = 86.
        const result = computeTotalGridSize({
            rows: 5,
            cols: 1,
            definedRowHeightsPx: [20, 30],
            definedColWidthsPx: [50],
            hiddenRows: new Set([4]),
            hiddenColumns: new Set(),
            defaultRowHeightPx: 18,
            defaultColWidthPx: 64,
        });

        expect(result.totalHeightPx).to.equal(86);
    });

    it('returns 0 for both dimensions when the grid is empty', () => {
        const result = computeTotalGridSize({
            rows: 0,
            cols: 0,
            definedRowHeightsPx: [],
            definedColWidthsPx: [],
            hiddenRows: new Set(),
            hiddenColumns: new Set(),
            defaultRowHeightPx: 18,
            defaultColWidthPx: 64,
        });

        expect(result.totalWidthPx).to.equal(0);
        expect(result.totalHeightPx).to.equal(0);
    });
});
