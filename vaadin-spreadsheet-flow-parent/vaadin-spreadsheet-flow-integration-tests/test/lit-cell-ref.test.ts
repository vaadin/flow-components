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
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/CellRef.js';

describe('CellRef — columnIndexToLetters', () => {
    it('writes a column number in letters, past Z as AA and on', () => {
        expect(columnIndexToLetters(1)).to.equal('A');
        expect(columnIndexToLetters(2)).to.equal('B');
        expect(columnIndexToLetters(26)).to.equal('Z');
        expect(columnIndexToLetters(27)).to.equal('AA');
        expect(columnIndexToLetters(52)).to.equal('AZ');
        expect(columnIndexToLetters(53)).to.equal('BA');
        expect(columnIndexToLetters(702)).to.equal('ZZ');
        expect(columnIndexToLetters(703)).to.equal('AAA');
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
