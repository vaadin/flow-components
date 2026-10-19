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
    cellCoordToAddress,
    columnIndexToLetters,
    parseCellRefRange,
    parseSingleCell,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/CellRef.js';

describe('CellRef — parseSingleCell', () => {
    it('parses simple single-letter columns', () => {
        expect(parseSingleCell('A1')).to.deep.equal({ col: 1, row: 1 });
        expect(parseSingleCell('B1')).to.deep.equal({ col: 2, row: 1 });
        expect(parseSingleCell('Z1')).to.deep.equal({ col: 26, row: 1 });
    });

    it('parses multi-letter columns in base-26', () => {
        expect(parseSingleCell('AA1')).to.deep.equal({ col: 27, row: 1 });
        expect(parseSingleCell('AZ1')).to.deep.equal({ col: 52, row: 1 });
        expect(parseSingleCell('BA1')).to.deep.equal({ col: 53, row: 1 });
        expect(parseSingleCell('ZZ1')).to.deep.equal({ col: 702, row: 1 });
        expect(parseSingleCell('AAA1')).to.deep.equal({ col: 703, row: 1 });
    });

    it('parses multi-digit rows', () => {
        expect(parseSingleCell('A1')).to.deep.equal({ col: 1, row: 1 });
        expect(parseSingleCell('A99')).to.deep.equal({ col: 1, row: 99 });
        expect(parseSingleCell('A1234567')).to.deep.equal({
            col: 1,
            row: 1234567,
        });
    });

    it('tolerates lowercase input by uppercasing internally', () => {
        expect(parseSingleCell('a1')).to.deep.equal({ col: 1, row: 1 });
        expect(parseSingleCell('aa1')).to.deep.equal({ col: 27, row: 1 });
    });

    it('returns {0, 0} on null input (no exception)', () => {
        expect(parseSingleCell(null)).to.deep.equal({ col: 0, row: 0 });
    });

    it('returns {0, 0} on empty string', () => {
        expect(parseSingleCell('')).to.deep.equal({ col: 0, row: 0 });
    });
});

describe('CellRef — columnIndexToLetters', () => {
    it('inverts the parseSingleCell column math', () => {
        expect(columnIndexToLetters(1)).to.equal('A');
        expect(columnIndexToLetters(2)).to.equal('B');
        expect(columnIndexToLetters(26)).to.equal('Z');
        expect(columnIndexToLetters(27)).to.equal('AA');
        expect(columnIndexToLetters(52)).to.equal('AZ');
        expect(columnIndexToLetters(53)).to.equal('BA');
        expect(columnIndexToLetters(702)).to.equal('ZZ');
        expect(columnIndexToLetters(703)).to.equal('AAA');
    });

    it('round-trips parseSingleCell for representative values', () => {
        const addresses = ['A1', 'Z9', 'AA1', 'AZ52', 'BA53', 'ZZ702', 'AAA703'];
        for (const addr of addresses) {
            const coord = parseSingleCell(addr);
            const letters = columnIndexToLetters(coord.col);
            // We can't compare addresses directly because the test addresses
            // have arbitrary rows; compare the column part only.
            expect(letters + coord.row).to.equal(addr);
        }
    });

    it('returns empty string for col <= 0', () => {
        expect(columnIndexToLetters(0)).to.equal('');
        expect(columnIndexToLetters(-1)).to.equal('');
    });
});

describe('CellRef — cellCoordToAddress', () => {
    it('formats a coord as the canonical address', () => {
        expect(cellCoordToAddress({ col: 1, row: 1 })).to.equal('A1');
        expect(cellCoordToAddress({ col: 27, row: 42 })).to.equal('AA42');
    });

    it('omits the row for zero', () => {
        expect(cellCoordToAddress({ col: 1, row: 0 })).to.equal('A');
    });
});

describe('CellRef — parseCellRefRange', () => {
    const SHEET = 'Sheet1';

    it('parses single-cell refs', () => {
        const result = parseCellRefRange('A1', SHEET);

        expect(result).to.deep.equal({
            col1: 1,
            col2: 1,
            row1: 1,
            row2: 1,
        });
    });

    it('parses ranges', () => {
        const result = parseCellRefRange('A1:B3', SHEET);

        expect(result).to.deep.equal({
            col1: 1,
            col2: 2,
            row1: 1,
            row2: 3,
        });
    });

    it('strips the sheet prefix when it matches the active sheet', () => {
        const result = parseCellRefRange('Sheet1!A1', SHEET);

        expect(result).to.deep.equal({
            col1: 1,
            col2: 1,
            row1: 1,
            row2: 1,
        });
    });

    it('returns null when the sheet prefix is for another sheet', () => {
        const result = parseCellRefRange('Sheet2!A1', SHEET);

        expect(result).to.be.null;
    });

    it('parses sheet-qualified ranges when the sheet matches', () => {
        const result = parseCellRefRange('Sheet1!A1:C3', SHEET);

        expect(result).to.deep.equal({
            col1: 1,
            col2: 3,
            row1: 1,
            row2: 3,
        });
    });
});
