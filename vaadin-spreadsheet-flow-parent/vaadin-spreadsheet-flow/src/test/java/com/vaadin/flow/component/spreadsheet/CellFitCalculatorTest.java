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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Whether a value fits its column, as {@link CellFitCalculator} estimates it
 * from the width ratios the client reports.
 *
 * <p>
 * Pure-function calculator with one piece of stateful memory (the per-style
 * width-ratio map sent from the client). Pins:
 *
 * <ul>
 * <li>Null map (initial state) → {@code doesValueFit} returns true (caller must
 * assume fit until the client reports).</li>
 * <li>Map present but no entry for the cell's style → still true.</li>
 * <li>Entry present → BigDecimal compare of {@code ratio * value.length} vs
 * {@code cellWidthPixels - 4} (the 4px is for 2px left + 2px right cell
 * padding).</li>
 * <li>{@code onWidthRatioUpdate} replaces the map wholesale, not a putAll merge
 * — matches the original CellValueManager behavior.</li>
 * </ul>
 */
class CellFitCalculatorTest {

    private CellFitCalculator calculator;
    private Cell cell;
    private CellStyle style;

    @BeforeEach
    void setUp() {
        calculator = new CellFitCalculator();
        cell = mock(Cell.class);
        style = mock(CellStyle.class);
        when(cell.getCellStyle()).thenReturn(style);
        when(style.getIndex()).thenReturn((short) 7);
    }

    @Test
    void doesValueFit_nullMap_returnsTrue() {
        // Pin the initial-state contract: before the client has reported
        // any ratios, the calculator assumes everything fits.
        assertTrue(calculator.doesValueFit(cell, "12345", 50));
    }

    @Test
    void doesValueFit_emptyMap_returnsTrue() {
        // Pin: even after onWidthRatioUpdate with an empty map, the
        // per-style lookup returns null and the calculator assumes fit.
        calculator.onWidthRatioUpdate(new HashMap<>());
        assertTrue(calculator.doesValueFit(cell, "12345", 50));
    }

    @Test
    void doesValueFit_styleNotInMap_returnsTrue() {
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(99, 5.0f); // different style index
        calculator.onWidthRatioUpdate(ratios);

        assertTrue(calculator.doesValueFit(cell, "12345", 50));
    }

    @Test
    void doesValueFit_valueWidthEqualsColumnWidthMinus4_returnsTrue() {
        // Pin the inclusive boundary: ratio=2, value length=5 → string
        // pixels = 10. columnWidth-4 = 14-4 = 10. compareTo(<=) → true.
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(7, 2.0f);
        calculator.onWidthRatioUpdate(ratios);

        assertTrue(calculator.doesValueFit(cell, "12345", 14));
    }

    @Test
    void doesValueFit_valueWidthExceedsColumnWidthMinus4_returnsFalse() {
        // Pin: ratio=3, value length=5 → string pixels = 15.
        // columnWidth-4 = 14-4 = 10. 15 > 10 → false.
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(7, 3.0f);
        calculator.onWidthRatioUpdate(ratios);

        assertFalse(calculator.doesValueFit(cell, "12345", 14));
    }

    @Test
    void doesValueFit_fourPixelPaddingIsCritical() {
        // Pin the 4px padding constant explicitly: ratio=1, value length=10
        // → string pixels = 10. With columnWidth=13: 13-4=9, 10 > 9 →
        // false. With columnWidth=14: 14-4=10, 10 ≤ 10 → true.
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(7, 1.0f);
        calculator.onWidthRatioUpdate(ratios);

        assertFalse(calculator.doesValueFit(cell, "1234567890", 13));
        assertTrue(calculator.doesValueFit(cell, "1234567890", 14));
    }

    @Test
    void doesValueFit_emptyString_alwaysFits() {
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(7, 100.0f);
        calculator.onWidthRatioUpdate(ratios);

        // length 0 → string pixels 0 → fits regardless of width.
        assertTrue(calculator.doesValueFit(cell, "", 5));
    }

    @Test
    void doesValueFit_zeroRatio_alwaysFits() {
        // Pin: ratio=0 means every cell is treated as zero-width-per-char.
        // String pixels = 0 → fits always (≤ any non-negative column width).
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(7, 0.0f);
        calculator.onWidthRatioUpdate(ratios);

        assertTrue(calculator.doesValueFit(cell, "very long string", 5));
    }

    @Test
    void onWidthRatioUpdate_replacesWholesale_doesNotMerge() {
        // Pin: subsequent calls REPLACE the map. Prior entries are dropped.
        // Load-bearing because the client may have a different snapshot
        // each round and partial-merge would leak stale ratios.
        HashMap<Integer, Float> first = new HashMap<>();
        first.put(1, 1.0f);
        first.put(7, 1.0f);
        calculator.onWidthRatioUpdate(first);

        HashMap<Integer, Float> second = new HashMap<>();
        second.put(2, 1.0f);
        calculator.onWidthRatioUpdate(second);

        // Style index 7 is no longer in the map.
        assertNull(calculator.getRatioForStyleIndex(7));
        assertEquals(1.0f, calculator.getRatioForStyleIndex(2));
    }

    @Test
    void getRatioForStyleIndex_nullMap_returnsNull() {
        // Pin the initial state: the lookup returns null without NPE.
        assertNull(calculator.getRatioForStyleIndex(7));
    }

    @Test
    void getRatioForStyleIndex_missingEntry_returnsNull() {
        calculator.onWidthRatioUpdate(new HashMap<>());
        assertNull(calculator.getRatioForStyleIndex(7));
    }

    @Test
    void getRatioForStyleIndex_existingEntry_returnsRatio() {
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(7, 1.5f);
        calculator.onWidthRatioUpdate(ratios);

        assertEquals(1.5f, calculator.getRatioForStyleIndex(7));
    }

    @Test
    void doesValueFit_usesCellStyleIndex_notArbitraryStyle() {
        // Pin: the lookup keys off cell.getCellStyle().getIndex() —
        // changing the cell's style changes the lookup.
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(7, 10.0f); // huge ratio
        ratios.put(99, 0.1f); // tiny ratio
        calculator.onWidthRatioUpdate(ratios);

        // Style index 7 → 10*5 = 50 > 14-4=10 → false.
        assertFalse(calculator.doesValueFit(cell, "12345", 14));

        // Switch to style 99 → 0.1*5 = 0.5 ≤ 10 → true.
        when(style.getIndex()).thenReturn((short) 99);
        assertTrue(calculator.doesValueFit(cell, "12345", 14));
    }
}
