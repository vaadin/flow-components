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
    parseColRow,
    toKey,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/CellKeys.js';

describe('CellKeys — toKey', () => {
    it('formats (1, 1) as "col1 row1"', () => {
        expect(toKey(1, 1)).to.equal('col1 row1');
    });

    it('handles multi-digit columns + rows', () => {
        expect(toKey(27, 100)).to.equal('col27 row100');
        expect(toKey(1000, 99999)).to.equal('col1000 row99999');
    });

    it('matches the wire-format string the server emits via SpreadsheetUtil.toKey', () => {
        // The server side emits exactly this string for cell keys
        // (col + " row" + row, with the single space between them).
        // External code — the testbench module's CSS selectors, the
        // server's MirroredProperty serializers — depends on the
        // format.
        expect(toKey(5, 3)).to.equal('col5 row3');
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
