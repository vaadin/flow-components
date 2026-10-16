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
 * The invalid-formula marks of each sheet, kept by
 * {@link InvalidFormulaTracker}.
 *
 * <p>
 * Pins the per-active-sheet semantics of the invalid-formula key set:
 *
 * <ul>
 * <li>{@code mark} / {@code isMarked} / {@code removeMark} consult the
 * workbook's active sheet index on every call — a sheet switch hides marks made
 * under the previous sheet.</li>
 * <li>{@code shiftActiveSheetRows} re-keys marks on the active sheet after
 * {@code shiftRows(startRow, endRow, n)}: marks on the shifted rows move by
 * {@code n}, marks on the rows the shift overwrites are dropped, in both
 * directions, and the rest stay.</li>
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

    // ----- shiftActiveSheetRows: shifted rows move -------------------------

    @Test
    void shiftActiveSheetRows_rowInsideShiftRange_isTranslatedByN() {
        // Mark at POI row 4 (= toKey-row 5). Rows 2..10 move down by 3,
        // so the new POI row is 4+3=7, toKey row 8.
        tracker.mark(3, 5);

        tracker.shiftActiveSheetRows(2, 10, 3);

        assertFalse(tracker.isMarked(3, 5), "Original key MUST be replaced");
        assertTrue(tracker.isMarked(3, 8),
                "Shifted key MUST be present at row + n");
    }

    @Test
    void shiftActiveSheetRows_rowOutsideShiftRange_isKeptUnchanged() {
        // Mark at POI row 100 (toKey-row 101). Rows 2..10 move down by 3
        // and land on 5..13, so row 100 is not touched.
        tracker.mark(3, 101);

        tracker.shiftActiveSheetRows(2, 10, 3);

        assertTrue(tracker.isMarked(3, 101),
                "Row below the rows the shift lands on MUST be untouched");
    }

    @Test
    void shiftActiveSheetRows_rowAboveTheShift_isKeptUnchanged() {
        // Mark at POI row 0 (toKey-row 1). Rows 5..10 move down by 3,
        // so row 0 is not touched.
        tracker.mark(1, 1);

        tracker.shiftActiveSheetRows(5, 10, 3);

        assertTrue(tracker.isMarked(1, 1));
    }

    @Test
    void shiftActiveSheetRows_negativeN_firstShiftedRow_isTranslatedByN() {
        // Rows 5..10 move up by 3: POI row 5 (toKey 6) lands on row 2
        // (toKey 3).
        tracker.mark(3, 6);

        tracker.shiftActiveSheetRows(5, 10, -3);

        assertFalse(tracker.isMarked(3, 6));
        assertTrue(tracker.isMarked(3, 3));
    }

    @Test
    void shiftActiveSheetRows_positiveN_firstShiftedRow_isTranslatedByN() {
        // Rows 5..10 move down by 3: POI row 5 (toKey 6) lands on row 8
        // (toKey 9).
        tracker.mark(3, 6);

        tracker.shiftActiveSheetRows(5, 10, 3);

        assertFalse(tracker.isMarked(3, 6));
        assertTrue(tracker.isMarked(3, 9),
                "Positive n shifts row 5 to row 8 (toKey 9)");
    }

    // ----- shiftActiveSheetRows: overwritten rows are dropped ---------------

    @Test
    void shiftActiveSheetRows_negativeN_rowTheShiftLandsOn_isDropped() {
        // Rows 5..10 move up by 3 and land on 2..7, so POI rows 2..4 are
        // overwritten. The mark at POI row 4 (toKey 5) goes with its row.
        tracker.mark(3, 5);

        tracker.shiftActiveSheetRows(5, 10, -3);

        assertFalse(tracker.isMarked(3, 5),
                "Mark on an overwritten row MUST be dropped");
        assertFalse(tracker.isMarked(3, 2),
                "The mark MUST NOT move along with the shifted rows");
    }

    @Test
    void shiftActiveSheetRows_negativeN_rowAboveTheOverwrittenRows_isKept() {
        // Rows 5..10 move up by 3, overwriting POI rows 2..4. POI row 1
        // (toKey 2), just above them, is not touched.
        tracker.mark(3, 2);

        tracker.shiftActiveSheetRows(5, 10, -3);

        assertTrue(tracker.isMarked(3, 2),
                "Row above the overwritten rows MUST be kept");
    }

    @Test
    void shiftActiveSheetRows_positiveN_rowTheShiftLandsOn_isDropped() {
        // Rows 5..10 move down by 3 and land on 8..13, so POI rows 11..13
        // are overwritten. The mark at POI row 12 (toKey 13) goes with its
        // row.
        tracker.mark(3, 13);

        tracker.shiftActiveSheetRows(5, 10, 3);

        assertFalse(tracker.isMarked(3, 13),
                "Mark on an overwritten row MUST be dropped");
        assertFalse(tracker.isMarked(3, 16),
                "The mark MUST NOT move along with the shifted rows");
    }

    // ----- shiftActiveSheetRows: mixed + sheet scope -----------------------

    @Test
    void shiftActiveSheetRows_mixedKeys_appliesPerKeyClassification() {
        // Each key is classified on its own. Rows 2..10 move down by 3:
        // key (3, 5) — POI row 4 — is shifted → row 4+3=7 → toKey row 8.
        // key (1, 101) — POI row 100 — is below the shift → keep.
        // key (2, 1) — POI row 0 — is above the shift → keep.
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
