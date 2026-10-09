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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Turning an address or coordinates into a range within the sheet with
 * {@link CellRangeNormalizer}.
 *
 * <p>
 * Pins the two normalization paths:
 *
 * <ul>
 * <li>{@code fromAddressString(String)} — POI-format parsing (0-based coords
 * from {@link org.apache.poi.ss.util.CellReference}), reorder corners, clamp to
 * {@code getRows() - 1} / {@code getCols() - 1}.</li>
 * <li>{@code fromCoordinates(int row1, int col1, int row2, int col2)} — 1-based
 * input, reorder corners, clamp to {@code getRows()} / {@code getCols()}, then
 * subtract one to land in POI's 0-based space.</li>
 * </ul>
 *
 * <p>
 * The same observable result is reached via slightly different inner arithmetic
 * in the two overloads. The tests pin the END result and the specific clamping
 * shape so a future refactor that unifies the inner paths can be verified
 * behavior-preserving.
 */
class CellRangeNormalizerTest {

    @Mock
    private Spreadsheet spreadsheet;

    private CellRangeNormalizer normalizer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Default sheet bounds: 10 rows x 10 columns. Override per test
        // when a smaller / larger sheet is needed.
        when(spreadsheet.getRows()).thenReturn(10);
        when(spreadsheet.getCols()).thenReturn(10);
        normalizer = new CellRangeNormalizer(spreadsheet);
    }

    // ----- fromAddressString -----------------------------------------------

    @Test
    void fromAddressString_orderedRange_returnsRangeUnchanged() {
        // Pin: "B3:C5" parses to (r=2..4, c=1..2) in 0-based POI coords.
        CellRangeAddress range = normalizer.fromAddressString("B3:C5");

        assertEquals(2, range.getFirstRow());
        assertEquals(4, range.getLastRow());
        assertEquals(1, range.getFirstColumn());
        assertEquals(2, range.getLastColumn());
    }

    @Test
    void fromAddressString_singleCell_returnsSingleCellRange() {
        // Pin: "A1:A1" produces a 1x1 range at (0,0). The parser doesn't
        // special-case single cells — it just runs the corner-reorder
        // logic which is idempotent for equal inputs.
        CellRangeAddress range = normalizer.fromAddressString("A1:A1");

        assertEquals(0, range.getFirstRow());
        assertEquals(0, range.getLastRow());
        assertEquals(0, range.getFirstColumn());
        assertEquals(0, range.getLastColumn());
    }

    @Test
    void fromAddressString_reversedRange_isReorderedMinFirst() {
        // Pin the corner-reorder shape: "C5:B3" — second corner is
        // top-left, first is bottom-right. The normalizer MUST swap them
        // so the resulting CellRangeAddress has min-row/col as first and
        // max-row/col as last. A regression that skipped the swap would
        // produce an inverted range with first > last.
        CellRangeAddress range = normalizer.fromAddressString("C5:B3");

        assertEquals(2, range.getFirstRow());
        assertEquals(4, range.getLastRow());
        assertEquals(1, range.getFirstColumn());
        assertEquals(2, range.getLastColumn());
    }

    @Test
    void fromAddressString_rowBeyondSheet_clampsToLastRow() {
        // Pin: "A1:A15" against a 10-row sheet clamps lastRow to 9
        // (getRows() - 1). The string-overload clamps directly with
        // getRows() - 1 — note this differs from the int-overload's
        // clamp-then-subtract.
        when(spreadsheet.getRows()).thenReturn(10);
        CellRangeAddress range = normalizer.fromAddressString("A1:A15");

        assertEquals(0, range.getFirstRow());
        assertEquals(9, range.getLastRow());
    }

    @Test
    void fromAddressString_columnBeyondSheet_clampsToLastColumn() {
        // Pin: "A1:Z1" against a 10-column sheet clamps lastColumn to 9.
        // CellReference parses "Z" → col index 25; the >= getCols() guard
        // triggers and the value is set to getCols() - 1 = 9.
        when(spreadsheet.getCols()).thenReturn(10);
        CellRangeAddress range = normalizer.fromAddressString("A1:Z1");

        assertEquals(0, range.getFirstColumn());
        assertEquals(9, range.getLastColumn());
    }

    @Test
    void fromAddressString_bothCornersBeyondSheet_clampsBoth() {
        // Pin: when BOTH corners exceed the sheet, BOTH are clamped to
        // the same upper bound. The result collapses to a 1x1 range at
        // the sheet's bottom-right corner.
        when(spreadsheet.getRows()).thenReturn(5);
        when(spreadsheet.getCols()).thenReturn(5);
        CellRangeAddress range = normalizer.fromAddressString("J10:K15");

        assertEquals(4, range.getFirstRow());
        assertEquals(4, range.getLastRow());
        assertEquals(4, range.getFirstColumn());
        assertEquals(4, range.getLastColumn());
    }

    @Test
    void fromAddressString_atExactSheetBoundary_doesNotClampLastRow() {
        // Pin: the clamp guard is `>= getRows()`, NOT `> getRows()`. So
        // row index 9 (== getRows() - 1) is INSIDE the sheet (returns 9)
        // but index 10 (== getRows()) triggers the clamp to 9. Boundary
        // case worth pinning — a regression to `>` would let row 10
        // through and produce an out-of-bounds range.
        when(spreadsheet.getRows()).thenReturn(10);
        CellRangeAddress inside = normalizer.fromAddressString("A1:A10");
        assertEquals(9, inside.getLastRow());

        CellRangeAddress outside = normalizer.fromAddressString("A1:A11");
        assertEquals(9, outside.getLastRow());
    }

    @Test
    void fromAddressString_noColon_throwsArrayIndexOutOfBounds() {
        // Pin the documented input contract: a string without ':' has
        // only one element in the split, so split[1] throws AIOOBE. The
        // SUT does NOT pre-validate. Removing the assumption (e.g.
        // adding null-safe parsing) would change the failure mode. Tests
        // pin the current contract; if a future change adds validation,
        // update this test.
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> normalizer.fromAddressString("A1"));
    }

    // ----- fromCoordinates --------------------------------------------------

    @Test
    void fromCoordinates_orderedRange_returnsZeroBasedRange() {
        // Pin: 1-based input (3, 2, 5, 3) → POI 0-based (2, 1, 4, 2).
        // The normalizer subtracts one on emit. A regression that
        // forgot the -1 would produce a range one row/col too far.
        CellRangeAddress range = normalizer.fromCoordinates(3, 2, 5, 3);

        assertEquals(2, range.getFirstRow());
        assertEquals(4, range.getLastRow());
        assertEquals(1, range.getFirstColumn());
        assertEquals(2, range.getLastColumn());
    }

    @Test
    void fromCoordinates_reversedRange_isReorderedMinFirst() {
        // Pin: row1 > row2 and col1 > col2 — the corners are swapped
        // before the -1 emit. The output is identical to passing them
        // ordered.
        CellRangeAddress range = normalizer.fromCoordinates(5, 3, 3, 2);

        assertEquals(2, range.getFirstRow());
        assertEquals(4, range.getLastRow());
        assertEquals(1, range.getFirstColumn());
        assertEquals(2, range.getLastColumn());
    }

    @Test
    void fromCoordinates_singleCell_returnsSingleCellRange() {
        CellRangeAddress range = normalizer.fromCoordinates(1, 1, 1, 1);

        assertEquals(0, range.getFirstRow());
        assertEquals(0, range.getLastRow());
        assertEquals(0, range.getFirstColumn());
        assertEquals(0, range.getLastColumn());
    }

    @Test
    void fromCoordinates_rowBeyondSheet_clampsToGetRows_thenSubtractsOne() {
        // Pin: the int-overload clamps with `>= getRows()` to `getRows()`,
        // then subtracts one on emit. So row 15 against a 10-row sheet
        // is clamped to 10, then emitted as 9 — same observable end
        // result as the string overload, but a different inner shape.
        // Pin both halves so a refactor unifying the two overloads is
        // verifiable.
        when(spreadsheet.getRows()).thenReturn(10);
        CellRangeAddress range = normalizer.fromCoordinates(1, 1, 15, 1);

        assertEquals(0, range.getFirstRow());
        assertEquals(9, range.getLastRow());
    }

    @Test
    void fromCoordinates_columnBeyondSheet_clampsToGetCols_thenSubtractsOne() {
        when(spreadsheet.getCols()).thenReturn(10);
        CellRangeAddress range = normalizer.fromCoordinates(1, 1, 1, 26);

        assertEquals(0, range.getFirstColumn());
        assertEquals(9, range.getLastColumn());
    }

    @Test
    void fromCoordinates_atExactSheetBoundary_doesNotClampLastRow() {
        // Pin: the int-overload's guard is `>= getRows()`. A 1-based
        // row of 10 against a 10-row sheet (getRows()==10) triggers the
        // guard (10 >= 10 is true) and clamps to 10, then emits 9. A
        // 1-based row of 11 also clamps to 10, emits 9. Same result.
        // What ISN'T the same: a 1-based row of 9 (== getRows() - 1)
        // does NOT trigger the guard (9 >= 10 is false), goes through
        // unchanged, emits 8.
        when(spreadsheet.getRows()).thenReturn(10);
        CellRangeAddress atBoundary = normalizer.fromCoordinates(1, 1, 10, 1);
        assertEquals(9, atBoundary.getLastRow());

        CellRangeAddress oneBelow = normalizer.fromCoordinates(1, 1, 9, 1);
        assertEquals(8, oneBelow.getLastRow());
    }

    @Test
    void fromCoordinates_bothCornersBeyondSheet_collapsesToCorner() {
        when(spreadsheet.getRows()).thenReturn(5);
        when(spreadsheet.getCols()).thenReturn(5);
        CellRangeAddress range = normalizer.fromCoordinates(10, 10, 15, 15);

        assertEquals(4, range.getFirstRow());
        assertEquals(4, range.getLastRow());
        assertEquals(4, range.getFirstColumn());
        assertEquals(4, range.getLastColumn());
    }

    // ----- cross-overload consistency --------------------------------------

    @Test
    void bothOverloads_orderedRangeInBounds_produceSameResult() {
        // Pin: for inputs within bounds and already ordered, both
        // overloads produce the same range. Pinning this contract makes
        // a future refactor that unifies the two paths easier to verify.
        CellRangeAddress fromString = normalizer.fromAddressString("B3:C5");
        CellRangeAddress fromInts = normalizer.fromCoordinates(3, 2, 5, 3);

        assertEquals(fromString.getFirstRow(), fromInts.getFirstRow());
        assertEquals(fromString.getLastRow(), fromInts.getLastRow());
        assertEquals(fromString.getFirstColumn(), fromInts.getFirstColumn());
        assertEquals(fromString.getLastColumn(), fromInts.getLastColumn());
    }

    @Test
    void bothOverloads_clampingAtSheetEdge_produceSameResult() {
        // Pin: even with the different internal arithmetic, both
        // overloads converge on the same clamped output when given
        // equivalent out-of-bounds input. Catches a regression where
        // one overload accidentally drops the -1 on the int side.
        when(spreadsheet.getRows()).thenReturn(10);
        when(spreadsheet.getCols()).thenReturn(10);

        CellRangeAddress fromString = normalizer.fromAddressString("A1:Z100");
        CellRangeAddress fromInts = normalizer.fromCoordinates(1, 1, 100, 26);

        assertEquals(fromString.getFirstRow(), fromInts.getFirstRow());
        assertEquals(fromString.getLastRow(), fromInts.getLastRow());
        assertEquals(fromString.getFirstColumn(), fromInts.getFirstColumn());
        assertEquals(fromString.getLastColumn(), fromInts.getLastColumn());
    }
}
