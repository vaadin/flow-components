/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Characterization tests for {@link InvalidFormulaTracker}.
 *
 * <p>
 * Pins the per-active-sheet semantics of the invalid-formula key set:
 *
 * <ul>
 * <li>{@code mark} / {@code isMarked} / {@code removeMark} consult the
 * workbook's active sheet index on every call — a sheet switch hides marks made
 * under the previous sheet.</li>
 * <li>{@code clearAll} empties the per-sheet map across all sheets.</li>
 * <li>{@code shiftActiveSheetRows} re-keys marks on the active sheet based on
 * the same row-removed / row-shifted predicates the original code on
 * {@code Spreadsheet} used. Three sub-paths: drop (removed rows), translate
 * (rows within [first, last]), keep (rows outside the shift range). The
 * {@code n < 0} guard makes the drop path up-shift-only.</li>
 * </ul>
 */
class InvalidFormulaTrackerTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Workbook workbook;

    private InvalidFormulaTracker tracker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(spreadsheet.getWorkbook()).thenReturn(workbook);
        // Default active sheet: 0. Override per-test for sheet-switch tests.
        lenient().when(workbook.getActiveSheetIndex()).thenReturn(0);
        tracker = new InvalidFormulaTracker(spreadsheet);
    }

    // ----- mark + isMarked happy path --------------------------------------

    @Test
    void freshTracker_isMarkedReturnsFalse_forAnyCoord() {
        // Pin: an unused tracker reports false for any (col, row).
        assertFalse(tracker.isMarked(1, 1));
        assertFalse(tracker.isMarked(100, 100));
    }

    @Test
    void mark_thenIsMarked_returnsTrue_forSameCoord() {
        tracker.mark(3, 5);

        assertTrue(tracker.isMarked(3, 5));
    }

    @Test
    void mark_thenIsMarked_returnsFalse_forDifferentCoord() {
        // Pin: marks are coord-specific. (3, 5) being marked must NOT
        // make (3, 6) or (4, 5) report true.
        tracker.mark(3, 5);

        assertFalse(tracker.isMarked(3, 6),
                "Row-shifted neighbor must remain unmarked");
        assertFalse(tracker.isMarked(4, 5),
                "Column-shifted neighbor must remain unmarked");
    }

    @Test
    void mark_sameCoordTwice_isIdempotent_thenSingleRemoveClearsMark() {
        // Pin: the underlying set semantics — duplicate mark is a no-op,
        // a single removeMark clears the entry. A regression that used
        // a multi-set (or a counter) would NOT have the entry gone
        // after one removeMark.
        tracker.mark(3, 5);
        tracker.mark(3, 5);

        tracker.removeMark(3, 5);

        assertFalse(tracker.isMarked(3, 5),
                "After one removeMark, mark MUST be cleared");
    }

    // ----- per-sheet isolation ----------------------------------------------

    @Test
    void mark_onSheet0_isNotVisible_afterActiveSheetSwitchesToSheet1() {
        // Pin: the map is keyed by active sheet index. Marks made on
        // sheet 0 must NOT leak to sheet 1.
        when(workbook.getActiveSheetIndex()).thenReturn(0);
        tracker.mark(3, 5);

        when(workbook.getActiveSheetIndex()).thenReturn(1);

        assertFalse(tracker.isMarked(3, 5),
                "Mark on sheet 0 MUST NOT be visible from sheet 1");
    }

    @Test
    void mark_onTwoSheets_remainsVisible_perSheet_afterSwitch() {
        // Pin: marks accumulate independently per sheet. Switching back
        // and forth must NOT lose marks.
        when(workbook.getActiveSheetIndex()).thenReturn(0);
        tracker.mark(1, 1);
        when(workbook.getActiveSheetIndex()).thenReturn(1);
        tracker.mark(2, 2);

        when(workbook.getActiveSheetIndex()).thenReturn(0);
        assertTrue(tracker.isMarked(1, 1));
        assertFalse(tracker.isMarked(2, 2));

        when(workbook.getActiveSheetIndex()).thenReturn(1);
        assertTrue(tracker.isMarked(2, 2));
        assertFalse(tracker.isMarked(1, 1));
    }

    // ----- removeMark -------------------------------------------------------

    @Test
    void removeMark_onUnmarkedCoord_isNoOp() {
        // Pin: removeMark for a coord that was never marked does not
        // throw and does not affect other marks.
        tracker.mark(1, 1);

        tracker.removeMark(99, 99);

        assertTrue(tracker.isMarked(1, 1),
                "Unrelated removeMark MUST NOT clear other marks");
    }

    @Test
    void removeMark_onSheetWithNoMarks_isNoOp_andDoesNotThrow() {
        // Pin: removeMark on a sheet that's never been touched (no map
        // entry yet) is a no-op — the SUT checks containsKey before
        // remove. A regression that called .get(...).remove() without
        // the guard would NPE.
        when(workbook.getActiveSheetIndex()).thenReturn(7);
        org.junit.jupiter.api.Assertions
                .assertDoesNotThrow(() -> tracker.removeMark(1, 1));
    }

    @Test
    void isMarked_onSheetWithNoMarks_returnsFalse_withoutCreatingEntry() {
        // Pin: isMarked on a never-touched sheet returns false WITHOUT
        // creating an entry. After the call, mark+isMarked on the same
        // sheet still works (the absence of side-effect creation is
        // observable indirectly through behavior consistency).
        when(workbook.getActiveSheetIndex()).thenReturn(5);

        assertFalse(tracker.isMarked(1, 1));

        // After the false read, a subsequent mark+check still works.
        tracker.mark(1, 1);
        assertTrue(tracker.isMarked(1, 1));
    }

    // ----- clearAll ---------------------------------------------------------

    @Test
    void clearAll_emptiesMarksAcrossAllSheets() {
        when(workbook.getActiveSheetIndex()).thenReturn(0);
        tracker.mark(1, 1);
        when(workbook.getActiveSheetIndex()).thenReturn(1);
        tracker.mark(2, 2);

        tracker.clearAll();

        when(workbook.getActiveSheetIndex()).thenReturn(0);
        assertFalse(tracker.isMarked(1, 1));
        when(workbook.getActiveSheetIndex()).thenReturn(1);
        assertFalse(tracker.isMarked(2, 2));
    }

    @Test
    void clearAll_onEmptyTracker_doesNotThrow() {
        org.junit.jupiter.api.Assertions
                .assertDoesNotThrow(() -> tracker.clearAll());
    }

    // ----- shiftActiveSheetRows: empty / no-op branches --------------------

    @Test
    void shiftActiveSheetRows_onSheetWithNoMarks_isNoOp() {
        // Pin the `original == null` guard inside the SUT — a sheet
        // with no marks must NOT throw and must NOT create an entry.
        when(workbook.getActiveSheetIndex()).thenReturn(0);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> tracker.shiftActiveSheetRows(2, 5, 3));

        assertFalse(tracker.isMarked(1, 1),
                "No marks before shift, none after");
    }

    // ----- shiftActiveSheetRows: translate branch --------------------------

    @Test
    void shiftActiveSheetRows_rowInsideShiftRange_isTranslatedByN() {
        // Pin the numberOfRowsAboveWasChanged branch. Pre-shift mark at
        // POI row 4 (= toKey-row 5). Shift rows [first=2, last=10] by
        // n=3 (down by 3). row=4 is in [2, 10], so the new POI row is
        // 4+3=7, new toKey row 8. After the shift, mark at (col=3,
        // toKey-row 5) is gone and (col=3, toKey-row 8) is present.
        tracker.mark(3, 5);

        tracker.shiftActiveSheetRows(2, 10, 3);

        assertFalse(tracker.isMarked(3, 5), "Original key MUST be replaced");
        assertTrue(tracker.isMarked(3, 8),
                "Shifted key MUST be present at row + n");
    }

    @Test
    void shiftActiveSheetRows_rowOutsideShiftRange_isKeptUnchanged() {
        // Pin the keep branch. Mark at POI row 100 (toKey-row 101).
        // Shift rows [2, 10] by 3 — row 100 is outside [2, 10], so it
        // is kept as-is.
        tracker.mark(3, 101);

        tracker.shiftActiveSheetRows(2, 10, 3);

        assertTrue(tracker.isMarked(3, 101),
                "Row above the shift range MUST be untouched");
    }

    @Test
    void shiftActiveSheetRows_rowBelowFirst_isKeptUnchanged() {
        // Pin: row below `first` (and not in the dropped range when
        // n < 0) is kept. Mark at POI row 0 (toKey-row 1). Shift
        // [first=5, last=10] by n=3 (down) — row 0 is outside [5, 10]
        // AND below the shift range. Kept.
        tracker.mark(1, 1);

        tracker.shiftActiveSheetRows(5, 10, 3);

        assertTrue(tracker.isMarked(1, 1));
    }

    // ----- shiftActiveSheetRows: drop branch (n < 0 only) ------------------

    @Test
    void shiftActiveSheetRows_negativeN_rowInRemovedRange_isDropped() {
        // Pin the rowWasRemoved branch: `n < 0 && first+n < row &&
        // row <= first`. With first=5, n=-3 the dropped POI rows are
        // {3, 4, 5} (toKey rows 4, 5, 6). Mark at toKey row 5 (POI 4)
        // MUST be dropped. The translation branch does NOT fire
        // because rowWasRemoved is checked FIRST.
        tracker.mark(3, 5);

        tracker.shiftActiveSheetRows(5, 10, -3);

        assertFalse(tracker.isMarked(3, 5),
                "Mark on a removed row MUST be dropped");
    }

    @Test
    void shiftActiveSheetRows_negativeN_rowAtFirstBoundary_isDropped() {
        // Pin the inclusive upper bound: row == first IS in the
        // dropped range. With first=5, n=-3, toKey row 6 (POI 5 ==
        // first) is dropped.
        tracker.mark(3, 6);

        tracker.shiftActiveSheetRows(5, 10, -3);

        assertFalse(tracker.isMarked(3, 6),
                "Row == first MUST be dropped (inclusive upper bound)");
    }

    @Test
    void shiftActiveSheetRows_negativeN_rowAtFirstPlusN_isNotDropped_butTranslated() {
        // Pin the exclusive lower bound: row == first+n is NOT in the
        // dropped range (the predicate is `first+n < row`). With
        // first=5, n=-3, POI row 2 (toKey 3) is NOT dropped. Since
        // row 2 < first=5, the numberOfRowsAboveWasChanged check
        // (`first <= row && row <= last`) is false too. So the key is
        // KEPT.
        tracker.mark(3, 3);

        tracker.shiftActiveSheetRows(5, 10, -3);

        assertTrue(tracker.isMarked(3, 3),
                "Row at first+n (exclusive lower bound) MUST be kept");
    }

    @Test
    void shiftActiveSheetRows_positiveN_doesNotDropRowsAtFirst() {
        // Pin the `n < 0` guard on the drop predicate. With n > 0 the
        // drop branch is ALWAYS false — even for rows in (first+n,
        // first]. So a positive n only translates / keeps, never drops.
        tracker.mark(3, 6); // POI 5 == first

        tracker.shiftActiveSheetRows(5, 10, 3);

        // POI 5 is in [5, 10], so it's translated: new POI row = 5+3 =
        // 8, new toKey row = 9.
        assertFalse(tracker.isMarked(3, 6));
        assertTrue(tracker.isMarked(3, 9),
                "Positive n shifts row 5 to row 8 (toKey 9)");
    }

    // ----- shiftActiveSheetRows: mixed + sheet scope -----------------------

    @Test
    void shiftActiveSheetRows_mixedKeys_appliesPerKeyClassification() {
        // Pin: each key in the set is classified independently. In one
        // call we expect:
        // key (3, 5) — POI row 4 — in [first=2, last=10] → translate
        // to row 4+3=7 → toKey row 8.
        // key (1, 101) — POI row 100 — outside [2, 10] → keep.
        // key (2, 1) — POI row 0 — outside [2, 10], below first → keep.
        tracker.mark(3, 5);
        tracker.mark(1, 101);
        tracker.mark(2, 1);

        tracker.shiftActiveSheetRows(2, 10, 3);

        assertFalse(tracker.isMarked(3, 5));
        assertTrue(tracker.isMarked(3, 8));
        assertTrue(tracker.isMarked(1, 101));
        assertTrue(tracker.isMarked(2, 1));
    }

    @Test
    void shiftActiveSheetRows_onlyAffectsActiveSheet_otherSheetUntouched() {
        // Pin: the shift only re-keys the active sheet's entries. A
        // mark on a different sheet must remain at its original key.
        when(workbook.getActiveSheetIndex()).thenReturn(0);
        tracker.mark(3, 5);
        when(workbook.getActiveSheetIndex()).thenReturn(1);
        tracker.mark(3, 5);

        // Active sheet is 1; shift only affects sheet 1.
        tracker.shiftActiveSheetRows(2, 10, 3);

        // Sheet 1: row 5 (POI 4) was translated to row 8.
        assertFalse(tracker.isMarked(3, 5));
        assertTrue(tracker.isMarked(3, 8));

        // Sheet 0: unchanged.
        when(workbook.getActiveSheetIndex()).thenReturn(0);
        assertTrue(tracker.isMarked(3, 5),
                "Non-active sheet MUST NOT be re-keyed by the shift");
    }
}
