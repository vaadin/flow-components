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
    parseArrayOfCellData,
    parseArrayOfGroupingData,
    parseArrayOfIntegers,
    parseArrayOfMergedRegion,
    parseArrayOfPopupButtonState,
    parseArrayOfSpreadsheetActionDetails,
    parseArrayOfStrings,
    parseFloat32Array,
    parseInt32Array,
    parseMapNumberNumber,
    parseMapNumberString,
    parseMapStringOverlayInfo,
    parseMapStringString,
    parsePopupButtonState,
    parseSetNumber,
    parseSetString,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/wire/parser.js';

/**
 * Wire-format parser tests. The expected JSON inputs match the snapshots
 * pinned by `DtoWireFormatTest.java` (slice 106) — same Jackson output,
 * same JSON.parse semantics.
 */

describe('wire-parser — empty-input handling', () => {
    it('returns [] for arrays on null / "" / "null" input', () => {
        expect(parseArrayOfCellData(null)).to.deep.equal([]);
        expect(parseArrayOfCellData('')).to.deep.equal([]);
        expect(parseArrayOfCellData('null')).to.deep.equal([]);
        expect(parseArrayOfCellData(undefined)).to.deep.equal([]);
    });

    it('returns null for maps on null / "" / "null" input', () => {
        expect(parseMapStringString(null)).to.be.null;
        expect(parseMapStringString('')).to.be.null;
        expect(parseMapStringString('null')).to.be.null;
        expect(parseMapNumberNumber(null)).to.be.null;
        expect(parseMapStringOverlayInfo(null)).to.be.null;
    });

    it('returns empty Sets on null input', () => {
        expect([...parseSetNumber(null)]).to.deep.equal([]);
        expect([...parseSetString(null)]).to.deep.equal([]);
    });

    it('returns zero-length typed arrays on null input', () => {
        expect(parseInt32Array(null).length).to.equal(0);
        expect(parseFloat32Array(null).length).to.equal(0);
    });

    it('parsePopupButtonState returns null on empty input', () => {
        expect(parsePopupButtonState(null)).to.be.null;
        expect(parsePopupButtonState('')).to.be.null;
        expect(parsePopupButtonState('null')).to.be.null;
    });
});

describe('wire-parser — CellData', () => {
    it('parses the all-fields-populated snapshot from DtoWireFormatTest', () => {
        // Matches DtoWireFormatTest.cellData_allNonDefaultFields_serializeAlphabetically.
        const raw =
            '{"cellStyle":"cs7","col":5,"formulaValue":"=A1+1","isPercentage":true,' +
            '"locked":true,"needsMeasure":true,"originalValue":"raw","row":3,' +
            '"textColor":"#ff0000","value":"hello"}';

        const arr = parseArrayOfCellData('[' + raw + ']');

        expect(arr).to.have.length(1);
        expect(arr[0]).to.deep.equal({
            row: 3,
            col: 5,
            value: 'hello',
            formulaValue: '=A1+1',
            originalValue: 'raw',
            cellStyle: 'cs7',
            textColor: '#ff0000',
            locked: true,
            needsMeasure: true,
            isPercentage: true,
        });
    });

    it('parses the fresh-instance snapshot (cellStyle and null value present)', () => {
        // Matches DtoWireFormatTest.cellData_freshInstance_emitsCellStyleAndNullValue.
        const raw = '{"cellStyle":"cs0","value":null}';

        const arr = parseArrayOfCellData('[' + raw + ']');

        expect(arr).to.have.length(1);
        expect(arr[0].cellStyle).to.equal('cs0');
        expect(arr[0].value).to.be.null;
    });
});

describe('wire-parser — MergedRegion', () => {
    it('parses the wire snapshot from DtoWireFormatTest', () => {
        // Matches DtoWireFormatTest.mergedRegion_allFields_serializeAlphabetically.
        const raw =
            '[{"col1":2,"col2":4,"id":99,"row1":3,"row2":5}]';

        const arr = parseArrayOfMergedRegion(raw);

        expect(arr).to.deep.equal([{ col1: 2, col2: 4, id: 99, row1: 3, row2: 5 }]);
    });
});

describe('wire-parser — SpreadsheetActionDetails', () => {
    it('preserves int type field through JSON.parse (matches server)', () => {
        // Matches DtoWireFormatTest.spreadsheetActionDetails_intTypeAndIconNodeId_serializeAsNumbers.
        const raw =
            '[{"caption":"Cut","iconNodeId":42,"key":"cut","type":1}]';

        const arr = parseArrayOfSpreadsheetActionDetails(raw);

        expect(arr).to.have.length(1);
        expect(arr[0].type).to.equal(1);
        expect(typeof arr[0].type).to.equal('number');
        expect(arr[0].iconNodeId).to.equal(42);
        expect(typeof arr[0].iconNodeId).to.equal('number');
    });
});

describe('wire-parser — PopupButtonState', () => {
    it('parses the all-fields snapshot from DtoWireFormatTest', () => {
        // Matches DtoWireFormatTest.popupButtonState_serializesOnlyServerSideFields.
        const raw =
            '{"active":true,"col":2,"headerHidden":true,"popupHeight":"100px",' +
            '"popupWidth":"200px","row":3,"sheet":"Sheet1"}';

        const state = parsePopupButtonState(raw);

        expect(state).to.deep.equal({
            active: true,
            col: 2,
            headerHidden: true,
            popupHeight: '100px',
            popupWidth: '200px',
            row: 3,
            sheet: 'Sheet1',
        });
    });
});

describe('wire-parser — GroupingData', () => {
    it('parses the all-fields snapshot from DtoWireFormatTest', () => {
        // Matches DtoWireFormatTest.groupingData_allFields_serializeAlphabetically.
        const raw =
            '[{"collapsed":true,"endIndex":10,"level":1,"startIndex":5,"uniqueIndex":7}]';

        const arr = parseArrayOfGroupingData(raw);

        expect(arr).to.deep.equal([
            {
                collapsed: true,
                endIndex: 10,
                level: 1,
                startIndex: 5,
                uniqueIndex: 7,
            },
        ]);
    });
});

describe('wire-parser — OverlayInfo map', () => {
    it('parses the IMAGE-default snapshot from DtoWireFormatTest', () => {
        // Matches DtoWireFormatTest.overlayInfo_freshInstance_emitsTypeIMAGE.
        const raw = '{"overlay1":{"type":"IMAGE"}}';

        const map = parseMapStringOverlayInfo(raw);

        expect(map).to.not.be.null;
        expect(map!.overlay1.type).to.equal('IMAGE');
    });

    it('parses the COMPONENT variant with full geometry', () => {
        // Matches DtoWireFormatTest.overlayInfo_componentVariant_emitsEnumName.
        const raw =
            '{"overlay1":{"col":2,"dx":1.0,"dy":2.0,"height":50.0,"row":3,' +
            '"type":"COMPONENT","width":100.0}}';

        const map = parseMapStringOverlayInfo(raw);

        expect(map).to.not.be.null;
        expect(map!.overlay1).to.deep.equal({
            col: 2,
            dx: 1.0,
            dy: 2.0,
            height: 50.0,
            row: 3,
            type: 'COMPONENT',
            width: 100.0,
        });
    });
});

describe('wire-parser — generic shape helpers', () => {
    it('parseArrayOfStrings round-trips a string array', () => {
        expect(parseArrayOfStrings('["a","b","c"]')).to.deep.equal(['a', 'b', 'c']);
    });

    it('parseArrayOfIntegers round-trips a number array', () => {
        expect(parseArrayOfIntegers('[1,2,3]')).to.deep.equal([1, 2, 3]);
    });

    it('parseInt32Array produces a typed array', () => {
        const arr = parseInt32Array('[1,2,3,4]');

        expect(arr).to.be.an.instanceOf(Int32Array);
        expect(arr.length).to.equal(4);
        expect(arr[3]).to.equal(4);
    });

    it('parseFloat32Array produces a typed array', () => {
        const arr = parseFloat32Array('[1.5,2.5,3.5]');

        expect(arr).to.be.an.instanceOf(Float32Array);
        expect(arr.length).to.equal(3);
    });

    it('parseMapNumberString coerces JSON string keys to numbers', () => {
        const map = parseMapNumberString('{"1":"one","2":"two"}');

        expect(map).to.not.be.null;
        expect(map!.get(1)).to.equal('one');
        expect(map!.get(2)).to.equal('two');
        expect(map!.size).to.equal(2);
    });

    it('parseMapNumberNumber coerces both keys and values', () => {
        const map = parseMapNumberNumber('{"1":10,"2":20}');

        expect(map!.get(1)).to.equal(10);
        expect(map!.get(2)).to.equal(20);
    });

    it('parseSetNumber dedups input values', () => {
        const s = parseSetNumber('[1,2,2,3,3,3]');

        expect(s.size).to.equal(3);
        expect([...s].sort()).to.deep.equal([1, 2, 3]);
    });

    it('parseSetString dedups input values', () => {
        const s = parseSetString('["a","b","a","c"]');

        expect(s.size).to.equal(3);
    });
});
