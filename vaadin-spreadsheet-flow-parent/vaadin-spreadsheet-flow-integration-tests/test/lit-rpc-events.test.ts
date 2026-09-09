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
    asSpreadsheetEvent,
    type PopupButtonEventData,
    type SpreadsheetEvent,
    type SpreadsheetEventType,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';

/**
 * Slice 128 added `popupButtonClick` + `popupClose` to the
 * `SpreadsheetEventType` union (the slice 120 survey missed them
 * because they were dispatched from the wrapper's popup-button
 * section, not the general event-dispatch section). These tests
 * pin both the literal-string union AND the typed payload shape.
 */
describe('rpc-client-to-server — popup-button events (slice 128)', () => {
    it('popupButtonClick is part of SpreadsheetEventType', () => {
        // Compile-time check: assigning the literal to the union
        // forces tsc to verify the type union includes it. If the
        // union ever loses 'popupButtonClick', this fails to compile.
        const t: SpreadsheetEventType = 'popupButtonClick';
        expect(t).to.equal('popupButtonClick');
    });

    it('popupClose is part of SpreadsheetEventType', () => {
        const t: SpreadsheetEventType = 'popupClose';
        expect(t).to.equal('popupClose');
    });

    it('PopupButtonEventData carries {row, column}', () => {
        const data: PopupButtonEventData = { row: 3, column: 5 };
        expect(data.row).to.equal(3);
        expect(data.column).to.equal(5);
    });

    it('SpreadsheetEvent discriminates popupButtonClick by type', () => {
        const evt: SpreadsheetEvent = {
            type: 'popupButtonClick',
            data: { row: 2, column: 4 },
        };
        // TypeScript narrows evt.data to PopupButtonEventData via
        // the discriminated union. Verify access compiles.
        if (evt.type === 'popupButtonClick') {
            expect(evt.data.row).to.equal(2);
            expect(evt.data.column).to.equal(4);
        }
    });

    it('SpreadsheetEvent discriminates popupClose by type', () => {
        const evt: SpreadsheetEvent = {
            type: 'popupClose',
            data: { row: 7, column: 11 },
        };
        if (evt.type === 'popupClose') {
            expect(evt.data.row).to.equal(7);
            expect(evt.data.column).to.equal(11);
        }
    });
});

describe('rpc-client-to-server — asSpreadsheetEvent narrower', () => {
    // The wrapper's `createEvent(type, data)` returns
    //   new CustomEvent('spreadsheet-event', { detail: { type, data } })
    // — i.e. ALL events use the outer name 'spreadsheet-event' and
    // the discriminator lives in detail.type. asSpreadsheetEvent
    // reads detail.type, not event.type. These tests build the
    // CustomEvent's detail in the wrapper-emitted shape.

    it('narrows a popupButtonClick spreadsheet-event to the typed shape', () => {
        const ce = new CustomEvent('spreadsheet-event', {
            detail: { type: 'popupButtonClick', data: { row: 1, column: 1 } },
        });
        const narrowed = asSpreadsheetEvent(ce);
        expect(narrowed).to.not.be.null;
        expect(narrowed?.type).to.equal('popupButtonClick');
    });

    it('narrows a popupClose spreadsheet-event to the typed shape', () => {
        const ce = new CustomEvent('spreadsheet-event', {
            detail: { type: 'popupClose', data: { row: 5, column: 3 } },
        });
        const narrowed = asSpreadsheetEvent(ce);
        expect(narrowed?.type).to.equal('popupClose');
    });

    it('returns null when detail is missing the type field', () => {
        const ce = new CustomEvent('spreadsheet-event', { detail: {} });
        expect(asSpreadsheetEvent(ce)).to.be.null;
    });

    it('returns null for a non-CustomEvent', () => {
        const e = new Event('spreadsheet-event');
        expect(asSpreadsheetEvent(e)).to.be.null;
    });
});
