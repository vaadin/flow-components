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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellDeletionHandler;

/**
 * Whether a deletion handler lets the default deletion run, as
 * {@link CellDeletionGuard} checks it.
 *
 * <p>
 * Three deletion-shape predicates plus an in-range helper. Each predicate
 * follows the "pass when nobody intercepts" contract — returns {@code true} if
 * the default deletion path should run, {@code false} only when the configured
 * handler accepted responsibility and returned false.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>Null selection or absent handler always passes (default path).</li>
 * <li>The active-sheet lookup goes through
 * {@code parent.spreadsheet.getWorkbook().getSheetAt(activeIndex)}.</li>
 * <li>Single-cell deletion passes when the cell doesn't exist on the resolved
 * row.</li>
 * <li>Handler return value is the predicate's return value (no inversion).</li>
 * </ul>
 */
class CellDeletionGuardTest {

    @Mock
    private CellValueManager parent;
    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Workbook workbook;
    @Mock
    private Sheet sheet;
    @Mock
    private Row row;
    @Mock
    private Cell cell;
    @Mock
    private CellDeletionHandler handler;
    @Mock
    private FormulaEvaluator formulaEvaluator;
    @Mock
    private DataFormatter dataFormatter;
    @Mock
    private ConditionalFormattingEvaluator condFormattingEvaluator;

    private CellDeletionGuard guard;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        guard = new CellDeletionGuard(parent);
        // CellValueManager.spreadsheet is final — assign via reflection so
        // the guard's active-sheet lookup resolves through the mock chain.
        java.lang.reflect.Field f = CellValueManager.class
                .getDeclaredField("spreadsheet");
        f.setAccessible(true);
        f.set(parent, spreadsheet);
        lenient().when(spreadsheet.getWorkbook()).thenReturn(workbook);
        lenient().when(workbook.getActiveSheetIndex()).thenReturn(0);
        lenient().when(workbook.getSheetAt(0)).thenReturn(sheet);
        lenient().when(parent.getFormulaEvaluator())
                .thenReturn(formulaEvaluator);
        lenient().when(parent.getDataFormatter()).thenReturn(dataFormatter);
        lenient().when(parent.getConditionalFormattingEvaluator())
                .thenReturn(condFormattingEvaluator);
    }

    // ----- isInRange --------------------------------------------------------

    @Test
    void isInRange_emptyRanges_returnsFalse() {
        CellReference ref = new CellReference(0, 0);
        assertFalse(guard.isInRange(ref, Collections.emptyList()));
    }

    @Test
    void isInRange_cellInsideSingleRange_returnsTrue() {
        CellReference ref = new CellReference(2, 2);
        CellRangeAddress range = new CellRangeAddress(0, 5, 0, 5);
        assertTrue(guard.isInRange(ref, Collections.singletonList(range)));
    }

    @Test
    void isInRange_cellOutsideAllRanges_returnsFalse() {
        CellReference ref = new CellReference(10, 10);
        CellRangeAddress r1 = new CellRangeAddress(0, 5, 0, 5);
        CellRangeAddress r2 = new CellRangeAddress(20, 25, 20, 25);
        assertFalse(guard.isInRange(ref, Arrays.asList(r1, r2)));
    }

    @Test
    void isInRange_cellInsideOneOfManyRanges_returnsTrue() {
        // Pin: the predicate stops at the first matching range.
        CellReference ref = new CellReference(22, 22);
        CellRangeAddress r1 = new CellRangeAddress(0, 5, 0, 5);
        CellRangeAddress r2 = new CellRangeAddress(20, 25, 20, 25);
        assertTrue(guard.isInRange(ref, Arrays.asList(r1, r2)));
    }

    // ----- passesDeletionCheck (single cell) -------------------------------

    @Test
    void passesDeletionCheck_nullReference_returnsTrue_noHandlerCall() {
        // Pin: null selection is treated as "default path runs". No
        // handler invocation regardless of whether a handler exists.
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);

        assertTrue(guard.passesDeletionCheck(null));

        verify(handler, never()).cellDeleted(any(), any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(), any(), any(), any());
    }

    @Test
    void passesDeletionCheck_noHandler_returnsTrue() {
        when(parent.getCustomCellDeletionHandler()).thenReturn(null);

        assertTrue(guard.passesDeletionCheck(new CellReference(0, 0)));
    }

    @Test
    void passesDeletionCheck_handlerExists_butRowMissing_returnsTrue() {
        // Pin: if the resolved row is null, the predicate returns true
        // (default path) — the handler is NEVER consulted because there's
        // no cell to delete.
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);
        when(sheet.getRow(0)).thenReturn(null);

        assertTrue(guard.passesDeletionCheck(new CellReference(0, 0)));

        verify(handler, never()).cellDeleted(any(), any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(), any(), any(), any());
    }

    @Test
    void passesDeletionCheck_handlerExists_butCellMissing_returnsTrue() {
        // Pin: row exists but the cell at the target column is null.
        // Same "no cell to delete" semantic — return true, skip handler.
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);
        when(sheet.getRow(0)).thenReturn(row);
        when(row.getCell((short) 0)).thenReturn(null);

        assertTrue(guard.passesDeletionCheck(new CellReference(0, 0)));

        verify(handler, never()).cellDeleted(any(), any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(), any(), any(), any());
    }

    @Test
    void passesDeletionCheck_handlerReturnsTrue_predicateReturnsTrue() {
        // Pin: handler return propagates directly to the predicate
        // (no inversion). True from handler → true from predicate.
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);
        when(sheet.getRow(0)).thenReturn(row);
        when(row.getCell((short) 0)).thenReturn(cell);
        when(handler.cellDeleted(cell, sheet, (short) 0, 0, formulaEvaluator,
                dataFormatter, condFormattingEvaluator)).thenReturn(true);

        assertTrue(guard.passesDeletionCheck(new CellReference(0, 0)));
    }

    @Test
    void passesDeletionCheck_handlerReturnsFalse_predicateReturnsFalse() {
        // Pin: handler return false → predicate false. This means the
        // handler took responsibility for the deletion and the default
        // path should be skipped.
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);
        when(sheet.getRow(0)).thenReturn(row);
        when(row.getCell((short) 0)).thenReturn(cell);
        when(handler.cellDeleted(cell, sheet, (short) 0, 0, formulaEvaluator,
                dataFormatter, condFormattingEvaluator)).thenReturn(false);

        assertFalse(guard.passesDeletionCheck(new CellReference(0, 0)));
    }

    // ----- passesIndividualDeletionCheck -----------------------------------

    @Test
    void passesIndividualDeletionCheck_emptyList_returnsTrue() {
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);

        assertTrue(
                guard.passesIndividualDeletionCheck(Collections.emptyList()));

        verify(handler, never()).individualSelectedCellsDeleted(any(), any(),
                any(), any(), any());
    }

    @Test
    void passesIndividualDeletionCheck_noHandler_returnsTrue() {
        when(parent.getCustomCellDeletionHandler()).thenReturn(null);
        List<CellReference> selection = Collections
                .singletonList(new CellReference(0, 0));

        assertTrue(guard.passesIndividualDeletionCheck(selection));
    }

    @Test
    void passesIndividualDeletionCheck_handlerReturn_propagates() {
        // Pin both directions: handler returning false → false; returning
        // true → true.
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);
        List<CellReference> selection = Arrays.asList(new CellReference(0, 0),
                new CellReference(1, 1));
        when(handler.individualSelectedCellsDeleted(selection, sheet,
                formulaEvaluator, dataFormatter, condFormattingEvaluator))
                .thenReturn(false);

        assertFalse(guard.passesIndividualDeletionCheck(selection));
    }

    // ----- passesRangeDeletionCheck ----------------------------------------

    @Test
    void passesRangeDeletionCheck_emptyList_returnsTrue() {
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);

        assertTrue(guard.passesRangeDeletionCheck(Collections.emptyList()));

        verify(handler, never()).cellRangeDeleted(any(), any(), any(), any(),
                any());
    }

    @Test
    void passesRangeDeletionCheck_noHandler_returnsTrue() {
        when(parent.getCustomCellDeletionHandler()).thenReturn(null);
        List<CellRangeAddress> ranges = Collections
                .singletonList(new CellRangeAddress(0, 5, 0, 5));

        assertTrue(guard.passesRangeDeletionCheck(ranges));
    }

    @Test
    void passesRangeDeletionCheck_handlerReturnsTrue_predicateReturnsTrue() {
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);
        List<CellRangeAddress> ranges = Arrays.asList(
                new CellRangeAddress(0, 5, 0, 5),
                new CellRangeAddress(10, 15, 10, 15));
        when(handler.cellRangeDeleted(ranges, sheet, formulaEvaluator,
                dataFormatter, condFormattingEvaluator)).thenReturn(true);

        assertTrue(guard.passesRangeDeletionCheck(ranges));
    }

    @Test
    void passesRangeDeletionCheck_handlerReturnsFalse_predicateReturnsFalse() {
        when(parent.getCustomCellDeletionHandler()).thenReturn(handler);
        List<CellRangeAddress> ranges = Collections
                .singletonList(new CellRangeAddress(0, 5, 0, 5));
        when(handler.cellRangeDeleted(ranges, sheet, formulaEvaluator,
                dataFormatter, condFormattingEvaluator)).thenReturn(false);

        assertFalse(guard.passesRangeDeletionCheck(ranges));
    }
}
