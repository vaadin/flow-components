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
    parseTsv,
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

    it('emits NO trailing newline on the last row (matches GWT)', () => {
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

describe('Tsv — parseTsv', () => {
    it('parses a single cell', () => {
        expect(parseTsv('A')).to.deep.equal([['A']]);
    });

    it('parses a single row with tab separators', () => {
        expect(parseTsv('A\tB\tC')).to.deep.equal([['A', 'B', 'C']]);
    });

    it('parses a single column with newline separators', () => {
        expect(parseTsv('A\nB\nC')).to.deep.equal([['A'], ['B'], ['C']]);
    });

    it('parses an MxN grid', () => {
        expect(parseTsv('A\tB\tC\nD\tE\tF')).to.deep.equal([
            ['A', 'B', 'C'],
            ['D', 'E', 'F'],
        ]);
    });

    it('preserves empty cells as empty strings (not null)', () => {
        expect(parseTsv('A\t\tC')).to.deep.equal([['A', '', 'C']]);
    });

    it('normalizes Windows CRLF line endings to LF', () => {
        expect(parseTsv('A\tB\r\nC\tD')).to.deep.equal([
            ['A', 'B'],
            ['C', 'D'],
        ]);
    });

    it('normalizes classic-Mac CR line endings to LF', () => {
        expect(parseTsv('A\tB\rC\tD')).to.deep.equal([
            ['A', 'B'],
            ['C', 'D'],
        ]);
    });

    it('strips a single trailing newline (no phantom empty row)', () => {
        expect(parseTsv('A\tB\nC\tD\n')).to.deep.equal([
            ['A', 'B'],
            ['C', 'D'],
        ]);
    });

    it('returns an empty array on empty input (not [[]])', () => {
        expect(parseTsv('')).to.deep.equal([]);
    });
});

describe('Tsv — round-trip', () => {
    it('round-trips a representative MxN grid', () => {
        const original: (string | null)[][] = [
            ['Name', 'Age', 'City'],
            ['Alice', '30', 'Paris'],
            ['Bob', '', 'Berlin'],
            [null, '25', null],
        ];

        const tsv = serializeToTsv(original);
        const parsed = parseTsv(tsv);

        // Round-trip: null becomes '' on output, parses as '' on input.
        // The round-tripped form preserves shape but normalizes nulls.
        expect(parsed).to.deep.equal([
            ['Name', 'Age', 'City'],
            ['Alice', '30', 'Paris'],
            ['Bob', '', 'Berlin'],
            ['', '25', ''],
        ]);
    });
});
