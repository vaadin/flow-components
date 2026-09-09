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
    isCellRef,
    parseCellReferences,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/FormulaRefs.js';

const SHEET_NAMES = ['Sheet1', 'Sheet2', 'Sheet3'];

describe('FormulaRefs — isCellRef', () => {
    it('accepts plain single-cell refs', () => {
        expect(isCellRef('A1', SHEET_NAMES)).to.be.true;
        expect(isCellRef('Z99', SHEET_NAMES)).to.be.true;
        expect(isCellRef('AA1', SHEET_NAMES)).to.be.true;
        expect(isCellRef('ZZZ1234567', SHEET_NAMES)).to.be.true;
    });

    it('accepts range refs', () => {
        expect(isCellRef('A1:B3', SHEET_NAMES)).to.be.true;
        expect(isCellRef('A1:A1', SHEET_NAMES)).to.be.true;
        expect(isCellRef('ZZZ100:AAA200', SHEET_NAMES)).to.be.true;
    });

    it('accepts sheet-qualified refs (single and range)', () => {
        expect(isCellRef('Sheet1!A1', SHEET_NAMES)).to.be.true;
        expect(isCellRef('Sheet2!B5:D10', SHEET_NAMES)).to.be.true;
        expect(isCellRef('Sheet3!Z99', SHEET_NAMES)).to.be.true;
    });

    it('rejects refs prefixed with an unknown sheet', () => {
        expect(isCellRef('UnknownSheet!A1', SHEET_NAMES)).to.be.false;
    });

    it('rejects non-cell-ref tokens', () => {
        expect(isCellRef('SUM', SHEET_NAMES)).to.be.false;
        expect(isCellRef('=', SHEET_NAMES)).to.be.false;
        expect(isCellRef('+', SHEET_NAMES)).to.be.false;
        expect(isCellRef('', SHEET_NAMES)).to.be.false;
    });

    it('rejects column-only or row-only tokens', () => {
        expect(isCellRef('A', SHEET_NAMES)).to.be.false;
        expect(isCellRef('99', SHEET_NAMES)).to.be.false;
    });

    it('returns false when no sheet names are provided', () => {
        expect(isCellRef('A1', [])).to.be.false;
    });

    it('escapes regex metacharacters in sheet names', () => {
        // A sheet named "Q1.2026" — the `.` would match any char if not
        // escaped, so `Q1X2026!A1` would falsely match.
        const sheets = ['Q1.2026'];
        expect(isCellRef('Q1.2026!A1', sheets)).to.be.true;
        expect(isCellRef('Q1X2026!A1', sheets)).to.be.false;
    });
});

describe('FormulaRefs — parseCellReferences', () => {
    it('extracts a single ref from a trivial formula', () => {
        expect(parseCellReferences('=A1', SHEET_NAMES)).to.deep.equal(['A1']);
    });

    it('extracts multiple refs in document order', () => {
        expect(parseCellReferences('=A1+B2', SHEET_NAMES)).to.deep.equal([
            'A1',
            'B2',
        ]);
    });

    it('preserves range tokens as single strings (not split on colon)', () => {
        expect(
            parseCellReferences('=SUM(A1:B3)', SHEET_NAMES)
        ).to.deep.equal(['A1:B3']);
    });

    it('extracts sheet-qualified refs', () => {
        expect(
            parseCellReferences('=Sheet1!A1+Sheet2!B2', SHEET_NAMES)
        ).to.deep.equal(['Sheet1!A1', 'Sheet2!B2']);
    });

    it('mixes sheet-qualified refs and plain refs', () => {
        expect(
            parseCellReferences('=A1+Sheet2!B5:D10', SHEET_NAMES)
        ).to.deep.equal(['A1', 'Sheet2!B5:D10']);
    });

    it('returns duplicates in their document positions', () => {
        // The formula bar's color cycle relies on positional ordering;
        // duplicates getting deduplicated would change the visible
        // numbering.
        expect(
            parseCellReferences('=A1+A1', SHEET_NAMES)
        ).to.deep.equal(['A1', 'A1']);
    });

    it('ignores function names and operators', () => {
        expect(
            parseCellReferences('=IF(A1>0,SUM(B2:C5),0)', SHEET_NAMES)
        ).to.deep.equal(['A1', 'B2:C5']);
    });

    it('handles formulas without an equals prefix', () => {
        expect(parseCellReferences('A1+B2', SHEET_NAMES)).to.deep.equal([
            'A1',
            'B2',
        ]);
    });

    it('returns empty array on empty / no-refs formula', () => {
        expect(parseCellReferences('', SHEET_NAMES)).to.deep.equal([]);
        expect(parseCellReferences('=1+2', SHEET_NAMES)).to.deep.equal([]);
        expect(
            parseCellReferences('=SUM(5,10)', SHEET_NAMES)
        ).to.deep.equal([]);
    });

    it('returns empty array when no sheet names provided', () => {
        expect(parseCellReferences('=A1+B2', [])).to.deep.equal([]);
    });
});
