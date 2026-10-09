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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The visible range {@link ViewportBounds} remembers.
 *
 * <p>
 * Pins the four-index state-holder behavior: getters reflect last update,
 * {@code hasChanged} returns true only when at least one of the four 1-based
 * indices differs, {@code update} replaces all four atomically.
 *
 * <p>
 * Pure data structure — no fixture needed.
 */
class ViewportBoundsTest {

    private ViewportBounds bounds;

    @BeforeEach
    void setUp() {
        bounds = new ViewportBounds();
    }

    @Test
    void freshBounds_allFourIndicesAreZero() {
        // Pin: a never-updated ViewportBounds starts with primitive int
        // defaults (0). Spreadsheet.reloadActiveSheetData treats this as
        // "viewport not yet known" by calling update(-1, -1, -1, -1).
        assertEquals(0, bounds.getFirstRow());
        assertEquals(0, bounds.getLastRow());
        assertEquals(0, bounds.getFirstColumn());
        assertEquals(0, bounds.getLastColumn());
    }

    @Test
    void update_replacesAllFourIndices() {
        bounds.update(5, 20, 3, 10);
        assertEquals(5, bounds.getFirstRow());
        assertEquals(20, bounds.getLastRow());
        assertEquals(3, bounds.getFirstColumn());
        assertEquals(10, bounds.getLastColumn());
    }

    @Test
    void hasChanged_returnsTrueWhenFirstRowDiffers() {
        bounds.update(5, 20, 3, 10);
        assertTrue(bounds.hasChanged(6, 20, 3, 10));
    }

    @Test
    void hasChanged_returnsTrueWhenLastRowDiffers() {
        bounds.update(5, 20, 3, 10);
        assertTrue(bounds.hasChanged(5, 21, 3, 10));
    }

    @Test
    void hasChanged_returnsTrueWhenFirstColumnDiffers() {
        bounds.update(5, 20, 3, 10);
        assertTrue(bounds.hasChanged(5, 20, 4, 10));
    }

    @Test
    void hasChanged_returnsTrueWhenLastColumnDiffers() {
        bounds.update(5, 20, 3, 10);
        assertTrue(bounds.hasChanged(5, 20, 3, 11));
    }

    @Test
    void hasChanged_returnsFalseWhenAllFourMatch() {
        bounds.update(5, 20, 3, 10);
        assertFalse(bounds.hasChanged(5, 20, 3, 10));
    }

    @Test
    void hasChanged_onFreshBounds_returnsTrueForAnyNonZeroInput() {
        // Pin: a never-updated viewport differs from "any non-trivial bounds"
        // by definition. onSheetScroll uses this to decide whether to push
        // cell data on the first client scroll.
        assertTrue(bounds.hasChanged(1, 50, 1, 20));
    }

    @Test
    void update_canBeCalledRepeatedly() {
        bounds.update(1, 10, 1, 5);
        bounds.update(2, 11, 2, 6);
        assertEquals(2, bounds.getFirstRow());
        assertEquals(11, bounds.getLastRow());
        assertEquals(2, bounds.getFirstColumn());
        assertEquals(6, bounds.getLastColumn());
    }

    @Test
    void update_withNegativeOnes_marksViewportAsUnknown() {
        // Pin: Spreadsheet.reloadActiveSheetData calls update(-1, -1, -1, -1)
        // as the "viewport not yet known" sentinel.
        // setSpreadsheetComponentFactory
        // checks getFirstRow() != -1 to decide whether to load components now.
        bounds.update(5, 20, 3, 10);
        bounds.update(-1, -1, -1, -1);
        assertEquals(-1, bounds.getFirstRow());
        assertEquals(-1, bounds.getLastRow());
        assertEquals(-1, bounds.getFirstColumn());
        assertEquals(-1, bounds.getLastColumn());
    }
}
