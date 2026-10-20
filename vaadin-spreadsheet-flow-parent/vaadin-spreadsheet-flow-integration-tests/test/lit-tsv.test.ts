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
    serializeToTsv,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/Tsv.js';

describe('Tsv — serializeToTsv', () => {
    it('serializes a 1x1 grid', () => {
        expect(serializeToTsv([['A']])).to.equal('A');
    });

    it('serializes a 1xN grid with tab separators', () => {
        expect(serializeToTsv([['A', 'B', 'C']])).to.equal('A\tB\tC');
    });

    it('serializes an Nx1 grid with newline separators', () => {
        expect(serializeToTsv([['A'], ['B'], ['C']])).to.equal('A\nB\nC');
    });

    it('serializes an MxN grid', () => {
        const grid = [
            ['A', 'B', 'C'],
            ['D', 'E', 'F'],
        ];

        expect(serializeToTsv(grid)).to.equal('A\tB\tC\nD\tE\tF');
    });

    it('emits null cells as empty strings', () => {
        expect(serializeToTsv([['A', null, 'C']])).to.equal('A\t\tC');
        expect(serializeToTsv([[null, null, null]])).to.equal('\t\t');
    });

    it('preserves empty-string cells exactly', () => {
        expect(serializeToTsv([['A', '', 'C']])).to.equal('A\t\tC');
    });

    it('emits NO trailing newline on the last row', () => {
        const grid = [
            ['A', 'B'],
            ['C', 'D'],
        ];

        const tsv = serializeToTsv(grid);

        expect(tsv.endsWith('\n')).to.be.false;
    });

    it('returns empty string on empty input', () => {
        expect(serializeToTsv([])).to.equal('');
    });
});

/**
 * A cell holding a tab, a line break or a double quote goes in double
 * quotes, its own quotes doubled, as spreadsheets put such cells on the
 * clipboard. Written as it was, a cell with a line break pasted as two rows.
 */
describe('Tsv — quoting cells that would break the grid', () => {
    it('quotes a cell with a line break', () => {
        expect(serializeToTsv([['a\nb', 'c']])).to.equal('"a\nb"\tc');
    });

    it('quotes a cell with a tab', () => {
        expect(serializeToTsv([['a\tb']])).to.equal('"a\tb"');
    });

    it('quotes a cell with a quote, and doubles the quote', () => {
        expect(serializeToTsv([['say "hi"']])).to.equal('"say ""hi"""');
    });

    it('leaves every other cell as it is', () => {
        expect(serializeToTsv([['plain', '1,5', '']])).to.equal('plain\t1,5\t');
    });
});
