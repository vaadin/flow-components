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
    caretPosition,
    initialEditValue
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/PercentageEntry.js';

/**
 * Typing a number into a percentage cell.
 *
 * Without this, typing `5` into a cell reading `19%` stored 5 and the
 * percentage format drew it as `500.00%` — a hundredfold error from one
 * keystroke.
 */

describe('initialEditValue — opening the editor on a typed character', () => {
    const percentByFormat = { isPercentage: true, value: '19.00%' };
    const percentByValueOnly = { isPercentage: false, value: '19%' };
    const plain = { isPercentage: false, value: '19' };

    it('adds the sign when a digit is typed into a percentage cell', () => {
        expect(initialEditValue('5', percentByFormat)).to.equal('5%');
    });

    it('trusts the format even when the text does not show a sign', () => {
        // A percentage cell can display as "19,00 %" or with the sign
        // stripped by a custom format; the format is the reliable half.
        expect(initialEditValue('5', { isPercentage: true, value: '19' })).to.equal('5%');
    });

    it('trusts a displayed sign even when the format flag is missing', () => {
        expect(initialEditValue('5', percentByValueOnly)).to.equal('5%');
    });

    it('leaves a plain cell alone', () => {
        expect(initialEditValue('5', plain)).to.equal('5');
    });

    it('leaves an empty cell alone', () => {
        expect(initialEditValue('5', undefined)).to.equal('5');
    });

    it('carries a leading sign, which the caret rule makes work', () => {
        expect(initialEditValue('-', percentByFormat)).to.equal('-%');
        expect(initialEditValue('+', percentByFormat)).to.equal('+%');
    });

    it('does not touch a letter — that is text, not a number', () => {
        expect(initialEditValue('a', percentByFormat)).to.equal('a');
        expect(initialEditValue('=', percentByFormat)).to.equal('=');
    });
});

describe('caretPosition — where typing continues', () => {
    it('sits before a trailing percent sign', () => {
        // Otherwise typing 5 then 3 gives "5%3" instead of "53%".
        expect(caretPosition('5%')).to.equal(1);
        expect(caretPosition('-%')).to.equal(1);
        expect(caretPosition('19.00%')).to.equal(5);
    });

    it('sits at the end of anything else', () => {
        expect(caretPosition('5')).to.equal(1);
        expect(caretPosition('')).to.equal(0);
        expect(caretPosition('hello')).to.equal(5);
    });
});
