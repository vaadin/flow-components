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

import java.util.Set;
import java.util.function.IntPredicate;

import org.junit.jupiter.api.Test;

/**
 * How far text runs past its own column.
 *
 * <p>
 * A spreadsheet carries text on across empty neighbours instead of clipping it
 * at the column edge. The Lit client clipped every cell, which the Phase 3.0
 * baselines show as "File open" where the GWT reference reads "File opened".
 */
class CellOverflowCalculatorTest {

    /** Four 50px columns. */
    private static final int[] WIDTHS = { 50, 50, 50, 50 };

    private static final IntPredicate NOTHING_BESIDE_IT = col -> false;

    private static IntPredicate occupied(Integer... columns) {
        Set<Integer> taken = Set.of(columns);
        return taken::contains;
    }

    @Test
    void textThatFits_doesNotSpill() {
        assertEquals(0, CellOverflowCalculator.spillWidth(0, 46, WIDTHS, 0,
                NOTHING_BESIDE_IT));
    }

    @Test
    void textSpillsAcrossAsManyEmptyColumnsAsItNeeds() {
        // 120px of text in a 50px column, 4px of which is padding, needs
        // 74px more: two 50px columns cover it.
        assertEquals(150, CellOverflowCalculator.spillWidth(0, 120, WIDTHS, 0,
                NOTHING_BESIDE_IT));
    }

    @Test
    void textStopsAtTheFirstColumnHoldingAValue() {
        assertEquals(100, CellOverflowCalculator.spillWidth(0, 120, WIDTHS, 0,
                occupied(2)));
    }

    @Test
    void textBlockedByTheNextColumn_keepsToItsOwn() {
        // Still too wide, so still drawn clipped with an ellipsis - in the
        // one column it has. The GWT client read "Wednesd..." here.
        assertEquals(50, CellOverflowCalculator.spillWidth(0, 120, WIDTHS, 0,
                occupied(1)));
    }

    @Test
    void rightAlignedTextRunsOnToTheRightLikeAnyOther() {
        // Excel runs these leftwards. The GWT client let a value too wide for
        // its column overflow its cell whatever the alignment, so the text
        // started at the cell's left edge and ran on to the right; the
        // calculator no longer knows the alignment at all.
        assertEquals(150, CellOverflowCalculator.spillWidth(0, 120, WIDTHS, 0,
                NOTHING_BESIDE_IT));
    }

    @Test
    void textStopsAtTheFrozenSplit() {
        // Two frozen columns, so column index 1 is the last frozen one. Its
        // text must not run into column 2, which scrolls away underneath.
        assertEquals(50, CellOverflowCalculator.spillWidth(1, 120, WIDTHS, 2,
                NOTHING_BESIDE_IT));
    }

    @Test
    void textSpillsFreelyInsideTheFrozenBlock() {
        assertEquals(150, CellOverflowCalculator.spillWidth(0, 120, WIDTHS, 3,
                NOTHING_BESIDE_IT));
    }

    @Test
    void textSpillsFreelyOutsideTheFrozenBlock() {
        assertEquals(150, CellOverflowCalculator.spillWidth(1, 120, WIDTHS, 1,
                NOTHING_BESIDE_IT));
    }

    @Test
    void textStopsAtTheLastColumnOfTheSheet() {
        assertEquals(100, CellOverflowCalculator.spillWidth(2, 200, WIDTHS, 0,
                NOTHING_BESIDE_IT));
    }

    @Test
    void textInTheLastColumn_keepsToItsOwn() {
        assertEquals(50, CellOverflowCalculator.spillWidth(3, 200, WIDTHS, 0,
                NOTHING_BESIDE_IT));
    }

    @Test
    void neighboursOwnWidthsAreCounted_notTheCellsOwn() {
        int[] widths = { 50, 10, 10, 200 };
        assertEquals(270, CellOverflowCalculator.spillWidth(0, 130, widths, 0,
                NOTHING_BESIDE_IT));
    }

    @Test
    void oneColumnIsEnoughWhenTheTextOnlyJustOvershoots() {
        assertEquals(100, CellOverflowCalculator.spillWidth(0, 47, WIDTHS, 0,
                NOTHING_BESIDE_IT));
    }

    @Test
    void aColumnOutsideTheSheetSpillsNowhere() {
        assertEquals(0, CellOverflowCalculator.spillWidth(9, 200, WIDTHS, 0,
                NOTHING_BESIDE_IT));
        assertEquals(0, CellOverflowCalculator.spillWidth(-1, 200, WIDTHS, 0,
                NOTHING_BESIDE_IT));
    }
}
