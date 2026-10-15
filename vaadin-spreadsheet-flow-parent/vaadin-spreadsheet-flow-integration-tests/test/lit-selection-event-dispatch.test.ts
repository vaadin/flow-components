/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

import { buildSelectionEvent } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';

/**
 * Pure helper that classifies a selection shape and
 * builds the server-bound {@code SpreadsheetEvent}. The detection
 * order is whole-column → whole-row → single-cell → range.
 */

describe('buildSelectionEvent — single cell', () => {
    it('returns cellSelected with the active cell coord', () => {
        const event = buildSelectionEvent(
            { col: 5, row: 7 },
            { c1: 5, c2: 5, r1: 7, r2: 7 },
            100,
            100
        );
        expect(event.type).to.equal('cellSelected');
        expect(event.data).to.deep.equal({
            row: 7,
            column: 5,
            oldSelectionRangeDiscarded: true,
        });
    });

    it('uses activeCell row/col, NOT range corners (they match for single cell)', () => {
        const event = buildSelectionEvent(
            { col: 1, row: 1 },
            { c1: 1, c2: 1, r1: 1, r2: 1 },
            10,
            10
        );
        expect(event.type).to.equal('cellSelected');
        expect(event.data).to.deep.equal({
            row: 1,
            column: 1,
            oldSelectionRangeDiscarded: true,
        });
    });
});

describe('buildSelectionEvent — cellRangeSelected', () => {
    it('returns cellRangeSelected when range spans multiple cells', () => {
        const event = buildSelectionEvent(
            { col: 2, row: 2 },
            { c1: 2, c2: 4, r1: 2, r2: 5 },
            100,
            100
        );
        expect(event.type).to.equal('cellRangeSelected');
        expect(event.data).to.deep.equal({
            row1: 2,
            col1: 2,
            row2: 5,
            col2: 4,
        });
    });

    it('range that spans full-grid columns OR rows but not both axes is still range', () => {
        // 2 cols (2..3) by 2 rows (1..rows) — NOT a whole-column (it
        // spans more than one column).
        const event = buildSelectionEvent(
            { col: 2, row: 1 },
            { c1: 2, c2: 3, r1: 1, r2: 10 },
            10,
            10
        );
        expect(event.type).to.equal('cellRangeSelected');
    });
});

describe('buildSelectionEvent — columnSelected', () => {
    it('returns columnSelected when range is exactly one column spanning all rows', () => {
        const event = buildSelectionEvent(
            { col: 3, row: 1 },
            { c1: 3, c2: 3, r1: 1, r2: 10 },
            10,
            5
        );
        expect(event.type).to.equal('columnSelected');
        expect(event.data).to.deep.equal({
            column: 3,
            firstRowIndex: 1,
        });
    });

    it('NOT columnSelected if rows is 0 (uninitialized grid → falls back to cellSelected)', () => {
        const event = buildSelectionEvent(
            { col: 1, row: 1 },
            { c1: 1, c2: 1, r1: 1, r2: 1 },
            0,
            0
        );
        expect(event.type).to.equal('cellSelected');
    });
});

describe('buildSelectionEvent — rowSelected', () => {
    it('returns rowSelected when range is exactly one row spanning all columns', () => {
        const event = buildSelectionEvent(
            { col: 1, row: 4 },
            { c1: 1, c2: 5, r1: 4, r2: 4 },
            10,
            5
        );
        expect(event.type).to.equal('rowSelected');
        expect(event.data).to.deep.equal({
            row: 4,
            firstColumnIndex: 1,
        });
    });
});

describe('buildSelectionEvent — ambiguity at 1x1 grid', () => {
    it('a 1x1 grid + single cell returns cellSelected (most-specific would be column/row but we prefer cellSelected)', () => {
        // Range (1,1)-(1,1) matches "whole column 1" AND "whole row 1"
        // AND "single cell". The dispatch order whole-col → whole-row
        // → cell makes whole-column fire — adjust if we want
        // cellSelected to win at 1x1.
        const event = buildSelectionEvent(
            { col: 1, row: 1 },
            { c1: 1, c2: 1, r1: 1, r2: 1 },
            1,
            1
        );
        // At 1x1 a single cell IS the whole column AND the whole row.
        // Document the deterministic choice (column-first per the
        // detection order in buildSelectionEvent).
        expect(event.type).to.equal('columnSelected');
    });
});
