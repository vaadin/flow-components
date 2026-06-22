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
    HEADER_COL,
    HEADER_NONE,
    HEADER_ROW,
    headerType,
    parseColRow,
    parseHeaderIndex,
    toCssKey,
    toKey,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/algorithms/CellKeys.js';

describe('CellKeys — toKey', () => {
    it('formats (1, 1) as "col1 row1"', () => {
        expect(toKey(1, 1)).to.equal('col1 row1');
    });

    it('handles multi-digit columns + rows', () => {
        expect(toKey(27, 100)).to.equal('col27 row100');
        expect(toKey(1000, 99999)).to.equal('col1000 row99999');
    });

    it('preserves byte-equivalence with GWT SheetWidget.toKey', () => {
        // Confirms the wire-format string is verbatim what the server
        // emits via SpreadsheetUtil.toKey(col, row).
        expect(toKey(5, 3)).to.equal('col5 row3');
    });
});

describe('CellKeys — toCssKey', () => {
    it('formats (1, 1) as ".col1.row1"', () => {
        expect(toCssKey(1, 1)).to.equal('.col1.row1');
    });

    it('handles multi-digit values', () => {
        expect(toCssKey(27, 100)).to.equal('.col27.row100');
    });
});

describe('CellKeys — parseColRow', () => {
    it('inverts toKey for single-digit coordinates', () => {
        expect(parseColRow('col1 row1')).to.deep.equal({ col: 1, row: 1 });
    });

    it('inverts toKey for multi-digit coordinates', () => {
        expect(parseColRow('col27 row100')).to.deep.equal({ col: 27, row: 100 });
        expect(parseColRow('col1000 row99999'))
            .to.deep.equal({ col: 1000, row: 99999 });
    });

    it('ignores characters after the second space (GWT flag-stop)', () => {
        // GWT version: once we've seen 2 spaces (entering "row" digits then
        // a separator), the scanner breaks. Anything after is ignored.
        // This is how cells with extra classes like "col1 row1 cell-range"
        // still parse correctly.
        expect(parseColRow('col5 row3 extra-class')).to.deep.equal({
            col: 5,
            row: 3,
        });
    });

    it('returns (0, 0) for an empty string', () => {
        expect(parseColRow('')).to.deep.equal({ col: 0, row: 0 });
    });

    it('returns (0, 0) when input has no digits', () => {
        expect(parseColRow('col row')).to.deep.equal({ col: 0, row: 0 });
    });

    it('returns (col, 0) when input has only the column part', () => {
        expect(parseColRow('col5')).to.deep.equal({ col: 5, row: 0 });
    });

    it('round-trips toKey ↔ parseColRow across a range of coordinates', () => {
        for (const col of [1, 7, 26, 27, 100, 1000]) {
            for (const row of [1, 10, 99, 100, 50000]) {
                const key = toKey(col, row);
                const parsed = parseColRow(key);
                expect(parsed, key).to.deep.equal({ col, row });
            }
        }
    });
});

describe('CellKeys — headerType', () => {
    it('classifies "rh5" as a row header (HEADER_ROW = 1)', () => {
        expect(headerType('rh5')).to.equal(HEADER_ROW);
        expect(HEADER_ROW).to.equal(1);
    });

    it('classifies "ch3" as a column header (HEADER_COL = 2)', () => {
        expect(headerType('ch3')).to.equal(HEADER_COL);
        expect(HEADER_COL).to.equal(2);
    });

    it('classifies cell keys ("col1 row1") as HEADER_NONE', () => {
        // GWT version: only str[0..1] matter. "col..." → first='c' but
        // second='o' (NOT 'h') → returns 0.
        expect(headerType('col1 row1')).to.equal(HEADER_NONE);
        expect(HEADER_NONE).to.equal(0);
    });

    it('classifies "row5" as HEADER_NONE (row keys must start "rh", not "row")', () => {
        // QUIRK from GWT: "row5" returns 0 because str[1] is 'o', not 'h'.
        // Header keys are "rh..."; cell keys are "col... row...". Preserved
        // verbatim for byte-equivalence.
        expect(headerType('row5')).to.equal(HEADER_NONE);
    });

    it('returns HEADER_NONE for empty / single-char input', () => {
        expect(headerType('')).to.equal(HEADER_NONE);
        expect(headerType('r')).to.equal(HEADER_NONE);
        expect(headerType('c')).to.equal(HEADER_NONE);
    });

    it('returns HEADER_NONE for unrelated strings', () => {
        expect(headerType('foo')).to.equal(HEADER_NONE);
        expect(headerType('hello world')).to.equal(HEADER_NONE);
    });
});

describe('CellKeys — parseHeaderIndex', () => {
    it('extracts the index from a row header "rh5"', () => {
        expect(parseHeaderIndex('rh5')).to.equal(5);
    });

    it('extracts the index from a column header "ch12"', () => {
        expect(parseHeaderIndex('ch12')).to.equal(12);
    });

    it('extracts multi-digit indexes from "rh1024"', () => {
        expect(parseHeaderIndex('rh1024')).to.equal(1024);
    });

    it('returns 0 for strings with no digits', () => {
        expect(parseHeaderIndex('rh')).to.equal(0);
        expect(parseHeaderIndex('foo')).to.equal(0);
        expect(parseHeaderIndex('')).to.equal(0);
    });

    it('accumulates ALL digit runs (QUIRK)', () => {
        // GWT scanner accumulates digits wherever they appear:
        // "rh1ch2" → 1*10 + 2 = 12. The upstream presumably never
        // calls with multi-run input, but preserved verbatim.
        expect(parseHeaderIndex('rh1ch2')).to.equal(12);
        expect(parseHeaderIndex('ch5rh10')).to.equal(510);
    });
});
