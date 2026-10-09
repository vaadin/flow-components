/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Filling cells down, up, right and left with {@link DirectionalShiftOperator}.
 *
 * <p>
 * Four structurally-similar methods (shiftRowsDown / shiftRowsUp /
 * shiftColumnsRight / shiftColumnsLeft) plus two close-but-different clear-cell
 * helpers. Pins:
 *
 * <ul>
 * <li>The source-range iteration uses 1-based indices throughout; POI calls
 * subtract 1.</li>
 * <li>Each (source cell, dest cell) pair routes through
 * {@code cellValueShifter.shift(source, dest, false, increment)}.</li>
 * <li>Missing destination rows / cells are auto-created; missing SOURCE rows
 * trigger {@code cellValueManager.removeCells} for the dest row instead of a
 * per-cell shift.</li>
 * <li>The asymmetric clear ordering — row variants reset style FIRST
 * (clearCell), column variants clear value FIRST (clearCellReversed). Pinned
 * verbatim because the original CellSelectionShifter preserved this ordering
 * and it may matter for style-cache invalidation.</li>
 * </ul>
 *
 * <p>
 * Mocks all collaborators. The static {@code RangeSequenceDetector} calls reach
 * into the mocked sheet — since {@code Sheet.getRow} defaults to null on the
 * mock, the static returns null too (no sequence detected). This means the
 * {@code cellValueShifter.shift} stubs all see a null sequenceIncrement, which
 * is fine for the dispatch-level invariants we're pinning.
 */
class DirectionalShiftOperatorTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private CellValueShifter cellValueShifter;
    @Mock
    private CellSelectionManager selectionManager;
    @Mock
    private CellValueManager valueManager;
    @Mock
    private SpreadsheetStyleFactory styleFactory;
    @Mock
    private Workbook workbook;
    @Mock
    private Sheet sheet;
    @Mock
    private Row sourceRow;
    @Mock
    private Row destRow;
    @Mock
    private Cell sourceCell;
    @Mock
    private Cell destCell;

    private DirectionalShiftOperator op;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        op = new DirectionalShiftOperator(spreadsheet, cellValueShifter);
        lenient().when(spreadsheet.getCellSelectionManager())
                .thenReturn(selectionManager);
        lenient().when(spreadsheet.getCellValueManager())
                .thenReturn(valueManager);
        lenient().when(spreadsheet.getSpreadsheetStyleFactory())
                .thenReturn(styleFactory);
        lenient().when(spreadsheet.getWorkbook()).thenReturn(workbook);
        lenient().when(workbook.getActiveSheetIndex()).thenReturn(0);
        lenient().when(workbook.getSheetAt(0)).thenReturn(sheet);
    }

    private void selectionRange(int r1, int r2, int c1, int c2) {
        // CellRangeAddress is 0-based; the operator does +1 to coordinates.
        when(selectionManager.getSelectedCellRange())
                .thenReturn(new CellRangeAddress(r1, r2, c1, c2));
    }

    // ----- shiftRowsDown ----------------------------------------------------

    @Test
    void shiftRowsDown_simpleDuplication_callsShifterPerCell() {
        // Pin: source range row 0..0 (1-based: row 1), col 0..0.
        // newLastRow=2 means shift the source row down to fill rows 2..2
        // (POI 1..1).
        selectionRange(0, 0, 0, 0);
        when(sheet.getRow(0)).thenReturn(sourceRow); // source row 1
        when(sheet.getRow(1)).thenReturn(destRow); // dest row 2
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(destRow.getCell(0)).thenReturn(destCell);

        op.shiftRowsDown(2);

        verify(cellValueShifter).shift(eq(sourceCell), eq(destCell), eq(false),
                isNull());
    }

    @Test
    void shiftRowsDown_nullDestRow_createsRow() {
        // Pin: when the destination row doesn't exist, it gets created.
        selectionRange(0, 0, 0, 0);
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sheet.getRow(1)).thenReturn(null);
        when(sheet.createRow(1)).thenReturn(destRow);
        when(sourceRow.getCell(0)).thenReturn(sourceCell);

        op.shiftRowsDown(2);

        verify(sheet).createRow(1);
        verify(cellValueShifter).shift(eq(sourceCell), any(), eq(false), any());
    }

    @Test
    void shiftRowsDown_nullSourceCell_destExists_clearsCellWithStyleFirst() {
        // Pin clearCell ordering: style reset → cellStyleUpdated(true) →
        // setCellValue(null) → cellDeleted. Style-first matters for
        // row variants (clearCell). Style update fires with true (custom
        // borders refresh).
        selectionRange(0, 0, 0, 0);
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sheet.getRow(1)).thenReturn(destRow);
        when(sourceRow.getCell(0)).thenReturn(null);
        when(destRow.getCell(0)).thenReturn(destCell);

        op.shiftRowsDown(2);

        InOrder inOrder = Mockito.inOrder(destCell, styleFactory, valueManager);
        inOrder.verify(destCell).setCellStyle(isNull());
        inOrder.verify(styleFactory).cellStyleUpdated(destCell, true);
        inOrder.verify(destCell).setCellValue((String) null);
        inOrder.verify(valueManager).cellDeleted(destCell);
        verify(cellValueShifter, never()).shift(any(), any(), anyBoolean(),
                any());
    }

    @Test
    void shiftRowsDown_nullSourceRow_callsRemoveCellsOnDestRowRange() {
        // Pin: a null source row means "the entire dest row range
        // gets removeCells". The shifter is not called.
        selectionRange(0, 0, 0, 2); // row 1, cols 1..3
        when(sheet.getRow(0)).thenReturn(null); // source row missing

        op.shiftRowsDown(2);

        verify(valueManager).removeCells(2, 1, 2, 3, true);
        verify(cellValueShifter, never()).shift(any(), any(), anyBoolean(),
                any());
    }

    @Test
    void shiftRowsDown_singleSourceRow_fillsMultipleDestRows() {
        // Pin: a single-row source can fill many destination rows when
        // newLastRow extends beyond just one. The increment from r2 to
        // r1+1 is (r2-r1+1) = 1; new rows continue stepping by 1.
        selectionRange(0, 0, 0, 0); // source row 1
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sheet.getRow(1)).thenReturn(destRow); // dest row 2
        when(sheet.getRow(2)).thenReturn(destRow); // dest row 3
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(destRow.getCell(0)).thenReturn(destCell);

        op.shiftRowsDown(3); // fill rows 2 and 3

        verify(cellValueShifter, times(2)).shift(eq(sourceCell), eq(destCell),
                eq(false), any());
    }

    // ----- shiftRowsUp ------------------------------------------------------

    @Test
    void shiftRowsUp_simpleDuplication_callsShifterPerCell() {
        // Pin: shift row 3 up to row 2 (newFirstRow=2). Mirrors shiftRowsDown
        // but moving up.
        selectionRange(2, 2, 0, 0); // source row 3
        when(sheet.getRow(2)).thenReturn(sourceRow); // source row 3
        when(sheet.getRow(1)).thenReturn(destRow); // dest row 2
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(destRow.getCell(0)).thenReturn(destCell);

        op.shiftRowsUp(2);

        verify(cellValueShifter).shift(eq(sourceCell), eq(destCell), eq(false),
                any());
    }

    @Test
    void shiftRowsUp_nullSourceRow_callsRemoveCells() {
        selectionRange(2, 2, 0, 0);
        when(sheet.getRow(2)).thenReturn(null);

        op.shiftRowsUp(2);

        verify(valueManager).removeCells(2, 1, 2, 1, true);
    }

    @Test
    void shiftRowsUp_singleSourceRow_fillsMultipleDestRows() {
        // Pin the multi-row up-shift: source row 3 (1-based: 3 / POI: 2)
        // fills both rows 2 AND 1 when newFirstRow=1. The stepping (r2-r1+1
        // = 1 here) moves up by one each iteration.
        selectionRange(2, 2, 0, 0); // source row 3
        when(sheet.getRow(2)).thenReturn(sourceRow); // source row 3
        when(sheet.getRow(1)).thenReturn(destRow); // dest row 2
        when(sheet.getRow(0)).thenReturn(destRow); // dest row 1
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(destRow.getCell(0)).thenReturn(destCell);

        op.shiftRowsUp(1); // fill rows 2 and 1

        verify(cellValueShifter, times(2)).shift(eq(sourceCell), eq(destCell),
                eq(false), any());
    }

    // ----- Sequence increment dispatch -------------------------------------

    @Test
    void shiftRowsDown_passesColumnIncrementFromRangeSequenceDetector_toShifter() {
        // Pin the dispatch tie: shiftRowsDown MUST forward the value
        // returned by RangeSequenceDetector.getColumnIncrement(sheet, c, r1,
        // r2) to cellValueShifter.shift as the 4th arg. The prior matcher
        // form (any()) would pass even if the operator hardcoded null.
        selectionRange(0, 0, 0, 0); // r1=1, r2=1, c1=1, c2=1
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sheet.getRow(1)).thenReturn(destRow);
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(destRow.getCell(0)).thenReturn(destCell);

        try (MockedStatic<RangeSequenceDetector> detectorStatic = mockStatic(
                RangeSequenceDetector.class)) {
            detectorStatic.when(() -> RangeSequenceDetector
                    .getColumnIncrement(sheet, 1, 1, 1)).thenReturn(7.5);

            op.shiftRowsDown(2);

            verify(cellValueShifter).shift(sourceCell, destCell, false, 7.5);
        }
    }

    @Test
    void shiftRowsUp_passesColumnIncrementFromRangeSequenceDetector_toShifter() {
        selectionRange(2, 2, 0, 0); // r1=3, r2=3, c1=1
        when(sheet.getRow(2)).thenReturn(sourceRow);
        when(sheet.getRow(1)).thenReturn(destRow);
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(destRow.getCell(0)).thenReturn(destCell);

        try (MockedStatic<RangeSequenceDetector> detectorStatic = mockStatic(
                RangeSequenceDetector.class)) {
            detectorStatic.when(() -> RangeSequenceDetector
                    .getColumnIncrement(sheet, 1, 3, 3)).thenReturn(-2.0);

            op.shiftRowsUp(2);

            verify(cellValueShifter).shift(sourceCell, destCell, false, -2.0);
        }
    }

    @Test
    void shiftColumnsRight_passesRowIncrementFromRangeSequenceDetector_toShifter() {
        // Pin column-shift dispatch tie: shiftColumnsRight uses
        // getRowIncrement (NOT getColumnIncrement). Mixing the two would
        // compute the wrong sequence delta and corrupt autofill values.
        selectionRange(0, 0, 0, 0); // r1=1, c1=1, c2=1
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(sourceRow.getCell(1)).thenReturn(destCell);

        try (MockedStatic<RangeSequenceDetector> detectorStatic = mockStatic(
                RangeSequenceDetector.class)) {
            detectorStatic.when(
                    () -> RangeSequenceDetector.getRowIncrement(sheet, 1, 1, 1))
                    .thenReturn(3.0);

            op.shiftColumnsRight(2);

            verify(cellValueShifter).shift(sourceCell, destCell, false, 3.0);
        }
    }

    @Test
    void shiftColumnsLeft_passesRowIncrementFromRangeSequenceDetector_toShifter() {
        selectionRange(0, 0, 1, 1); // r1=1, c1=2
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sourceRow.getCell(1)).thenReturn(sourceCell);
        when(sourceRow.getCell(0)).thenReturn(destCell);

        try (MockedStatic<RangeSequenceDetector> detectorStatic = mockStatic(
                RangeSequenceDetector.class)) {
            detectorStatic.when(
                    () -> RangeSequenceDetector.getRowIncrement(sheet, 1, 2, 2))
                    .thenReturn(-1.5);

            op.shiftColumnsLeft(1);

            verify(cellValueShifter).shift(sourceCell, destCell, false, -1.5);
        }
    }

    // ----- shiftColumnsRight ------------------------------------------------

    @Test
    void shiftColumnsRight_simpleDuplication_callsShifterPerCell() {
        // Pin: source range col 0..0 (1-based: col 1), row 0..0.
        // newRightMostColumn=2 means shift source col into col 2.
        selectionRange(0, 0, 0, 0);
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(sourceRow.getCell(1)).thenReturn(destCell);

        op.shiftColumnsRight(2);

        verify(cellValueShifter).shift(eq(sourceCell), eq(destCell), eq(false),
                any());
    }

    @Test
    void shiftColumnsRight_nullDestCell_createsCell() {
        selectionRange(0, 0, 0, 0);
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sourceRow.getCell(0)).thenReturn(sourceCell);
        when(sourceRow.getCell(1)).thenReturn(null);
        when(sourceRow.createCell(1)).thenReturn(destCell);

        op.shiftColumnsRight(2);

        verify(sourceRow).createCell(1);
        verify(cellValueShifter).shift(eq(sourceCell), eq(destCell), eq(false),
                any());
    }

    @Test
    void shiftColumnsRight_nullSourceCell_destExists_clearsCellValueFirst() {
        // Pin clearCellReversed ordering: setCellValue(null) → cellDeleted →
        // setCellStyle(null) → cellStyleUpdated(true). Value-first matters
        // for column variants. Pinned verbatim because original
        // CellSelectionShifter preserved this asymmetry; may matter for
        // style-cache invalidation.
        selectionRange(0, 0, 0, 0);
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sourceRow.getCell(0)).thenReturn(null); // source missing
        when(sourceRow.getCell(1)).thenReturn(destCell); // dest present

        op.shiftColumnsRight(2);

        InOrder inOrder = Mockito.inOrder(destCell, styleFactory, valueManager);
        inOrder.verify(destCell).setCellValue((String) null);
        inOrder.verify(valueManager).cellDeleted(destCell);
        inOrder.verify(destCell).setCellStyle(isNull());
        inOrder.verify(styleFactory).cellStyleUpdated(destCell, true);
        verify(cellValueShifter, never()).shift(any(), any(), anyBoolean(),
                any());
    }

    @Test
    void shiftColumnsRight_nullSourceRow_skipped_noShift_noRemove() {
        // Pin: shiftColumnsRight skips entire rows with null Row — unlike
        // shiftRowsDown, no removeCells call fires. The column variants
        // don't have the symmetric null-source-row guard.
        selectionRange(0, 0, 0, 0);
        when(sheet.getRow(0)).thenReturn(null);

        op.shiftColumnsRight(2);

        verify(cellValueShifter, never()).shift(any(), any(), anyBoolean(),
                any());
        verify(valueManager, never()).removeCells(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    // ----- shiftColumnsLeft -------------------------------------------------

    @Test
    void shiftColumnsLeft_simpleDuplication_callsShifterPerCell() {
        // Source col 2 (POI 1), shift to col 1 (POI 0). newLeftMostColumn=1.
        selectionRange(0, 0, 1, 1); // source col 2
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sourceRow.getCell(1)).thenReturn(sourceCell); // source POI 1
        when(sourceRow.getCell(0)).thenReturn(destCell); // dest POI 0

        op.shiftColumnsLeft(1);

        verify(cellValueShifter).shift(eq(sourceCell), eq(destCell), eq(false),
                any());
    }

    @Test
    void shiftColumnsLeft_nullSourceCell_destExists_usesClearCellReversed() {
        // Same asymmetric clear as shiftColumnsRight.
        selectionRange(0, 0, 1, 1);
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sourceRow.getCell(1)).thenReturn(null);
        when(sourceRow.getCell(0)).thenReturn(destCell);

        op.shiftColumnsLeft(1);

        InOrder inOrder = Mockito.inOrder(destCell, valueManager, styleFactory);
        inOrder.verify(destCell).setCellValue((String) null);
        inOrder.verify(valueManager).cellDeleted(destCell);
        inOrder.verify(destCell).setCellStyle(isNull());
        inOrder.verify(styleFactory).cellStyleUpdated(destCell, true);
    }

    // ----- Cross-cutting: row vs column clear ordering ----------------------

    @Test
    void rowShift_clearCell_vs_columnShift_clearCellReversed_orderingDiffers() {
        // Pin the load-bearing asymmetry: BOTH clear paths end in the
        // same four operations, but the ORDER differs. Row variants:
        // style → cellStyleUpdated → value → cellDeleted. Column variants:
        // value → cellDeleted → style → cellStyleUpdated. Pinned across
        // both methods in one test to make the asymmetry impossible to
        // miss.
        selectionRange(0, 0, 0, 0);
        when(sheet.getRow(0)).thenReturn(sourceRow);
        when(sheet.getRow(1)).thenReturn(destRow);
        when(sourceRow.getCell(0)).thenReturn(null);
        when(destRow.getCell(0)).thenReturn(destCell);

        op.shiftRowsDown(2);

        // Row-variant order: style first, value after.
        InOrder rowOrder = Mockito.inOrder(destCell);
        rowOrder.verify(destCell).setCellStyle(isNull());
        rowOrder.verify(destCell).setCellValue((String) null);

        // Reset for column variant.
        Mockito.reset(destCell, valueManager, styleFactory);
        Cell colDestCell = mock(Cell.class);
        selectionRange(0, 0, 0, 0);
        when(sourceRow.getCell(0)).thenReturn(null);
        when(sourceRow.getCell(1)).thenReturn(colDestCell);

        op.shiftColumnsRight(2);

        // Column-variant order: value first, style after.
        InOrder colOrder = Mockito.inOrder(colDestCell);
        colOrder.verify(colDestCell).setCellValue((String) null);
        colOrder.verify(colDestCell).setCellStyle(isNull());
    }
}
