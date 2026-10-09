/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * The step of an arithmetic sequence, as {@link SequenceIncrement} computes it.
 *
 * <p>
 * Pins the arithmetic-progression detection that drives Excel autofill — "if
 * the user drags a fill handle and the selection has a clear pattern, extend
 * it; otherwise repeat the values literally." The class is pure logic on the
 * inputs, so the tests are direct (no POI fixture needed).
 *
 * <p>
 * Edge cases pinned: empty array, single value, repeated value, null entries,
 * negative increment, mixed-prefix strings, no-trailing-digits strings.
 */
class SequenceIncrementTest {

    // ----- numeric overload --------------------------------------------------

    @Test
    void numeric_simpleIntegerSequence_returnsCommonDifference() {
        assertEquals(Double.valueOf(2),
                SequenceIncrement.compute(new Double[] { 2d, 4d, 6d }));
    }

    @Test
    void numeric_decreasingSequence_returnsNegativeIncrement() {
        assertEquals(Double.valueOf(-3),
                SequenceIncrement.compute(new Double[] { 10d, 7d, 4d }));
    }

    @Test
    void numeric_constantSequence_returnsZero() {
        assertEquals(Double.valueOf(0),
                SequenceIncrement.compute(new Double[] { 5d, 5d, 5d }));
    }

    @Test
    void numeric_fractionalIncrement_returnsExactDouble() {
        assertEquals(Double.valueOf(0.5),
                SequenceIncrement.compute(new Double[] { 1d, 1.5d, 2d }));
    }

    @Test
    void numeric_nonConstantDifference_returnsNull() {
        assertNull(SequenceIncrement.compute(new Double[] { 1d, 2d, 4d }));
    }

    @Test
    void numeric_nullEntry_returnsNull() {
        assertNull(SequenceIncrement.compute(new Double[] { 1d, null, 3d }));
    }

    @Test
    void numeric_singleValue_returnsNull() {
        // No way to compute a difference from one value; null per the pinned
        // contract (the String overload diverges and returns 1 for a single
        // value, an Excel-autofill convention; the numeric overload does not).
        assertNull(SequenceIncrement.compute(new Double[] { 42d }));
    }

    @Test
    void numeric_emptyArray_returnsNull() {
        assertNull(SequenceIncrement.compute(new Double[0]));
    }

    // ----- string overload ---------------------------------------------------

    @Test
    void string_uniformPrefixAndDigits_returnsIncrement() {
        assertEquals(Double.valueOf(1),
                SequenceIncrement.compute(new String[] { "A1", "A2", "A3" }));
    }

    @Test
    void string_uniformPrefixStepOfTwo() {
        assertEquals(Double.valueOf(2), SequenceIncrement
                .compute(new String[] { "Item 2", "Item 4", "Item 6" }));
    }

    @Test
    void string_singleValueWithDigits_returnsOne() {
        // Pinned Excel-autofill convention: one cell of "A1" autofills as
        // "A1, A2, A3, ..." — increment 1.
        assertEquals(Double.valueOf(1),
                SequenceIncrement.compute(new String[] { "A1" }));
    }

    @Test
    void string_noTrailingDigits_returnsNull() {
        assertNull(SequenceIncrement.compute(new String[] { "foo", "bar" }));
    }

    @Test
    void string_mixedPrefix_returnsNull() {
        assertNull(
                SequenceIncrement.compute(new String[] { "A1", "B2", "A3" }));
    }

    @Test
    void string_nonConstantDifference_returnsNull() {
        assertNull(
                SequenceIncrement.compute(new String[] { "A1", "A2", "A4" }));
    }

    @Test
    void string_nullSecondEntry_returnsNull() {
        assertNull(
                SequenceIncrement.compute(new String[] { "A1", null, "A3" }));
    }
}
