/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Sending updated and deleted cells to the client with {@link CellMarkUpdater}.
 *
 * <p>
 * Pins the five orchestration paths the helper owns. The SUT is a pure
 * orchestrator — every assertion is on the SEQUENCE / CONDITIONAL DISPATCH of
 * calls into mocked collaborators (valueManager, styler, conditionalFormatter,
 * selectionManager, cellCommentLoader, evaluators, spreadsheet). Each test
 * names a specific 1-line regression in the comment.
 */
class CellMarkUpdaterTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private CellValueManager valueManager;
    @Mock
    private SpreadsheetStyleFactory styler;
    @Mock
    private CellSelectionManager selectionManager;
    @Mock
    private ConditionalFormatter conditionalFormatter;
    @Mock
    private CellCommentLoader cellCommentLoader;
    @Mock
    private FormulaEvaluator formulaEvaluator;
    @Mock
    private ConditionalFormattingEvaluator condFmtEvaluator;
    @Mock
    private Workbook workbook;
    @Mock
    private Sheet activeSheet;
    @Mock
    private Row row;
    @Mock
    private Cell cell;

    private CellMarkUpdater updater;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        lenient().when(spreadsheet.getCellValueManager())
                .thenReturn(valueManager);
        lenient().when(spreadsheet.getSpreadsheetStyleFactory())
                .thenReturn(styler);
        lenient().when(spreadsheet.getCellSelectionManager())
                .thenReturn(selectionManager);
        lenient().when(spreadsheet.getConditionalFormatter())
                .thenReturn(conditionalFormatter);
        lenient().when(spreadsheet.getFormulaEvaluator())
                .thenReturn(formulaEvaluator);
        lenient().when(spreadsheet.getConditionalFormattingEvaluator())
                .thenReturn(condFmtEvaluator);
        lenient().when(spreadsheet.getWorkbook()).thenReturn(workbook);
        lenient().when(workbook.getActiveSheetIndex()).thenReturn(0);
        lenient().when(workbook.getSheetAt(0)).thenReturn(activeSheet);

        // cellCommentLoader is a package-private field on Spreadsheet,
        // not exposed via a getter. The mock's field defaults to null,
        // so wire it manually via reflection — same pattern as
        // CellDeletionGuardTest.
        java.lang.reflect.Field f = Spreadsheet.class
                .getDeclaredField("cellCommentLoader");
        f.setAccessible(true);
        f.set(spreadsheet, cellCommentLoader);

        updater = new CellMarkUpdater(spreadsheet);
    }

    // ----- markCellAsUpdated -----------------------------------------------

    @Test
    void markCellAsUpdated_stylePropagateTrue_callsCellUpdatedAndCellStyleUpdated() {
        // Pin: cellStyleUpdated=true triggers BOTH the value-manager
        // cellUpdated AND the styler cellStyleUpdated(cell, true) call.
        // A regression that swallowed the styler call would fail.
        updater.markCellAsUpdated(cell, true);

        verify(valueManager).cellUpdated(cell);
        verify(styler).cellStyleUpdated(cell, true);
    }

    @Test
    void markCellAsUpdated_stylePropagateFalse_callsCellUpdatedOnly() {
        // Pin the opposite branch: cellStyleUpdated=false MUST NOT
        // touch the styler. A regression that always notified the
        // styler would be caught here.
        updater.markCellAsUpdated(cell, false);

        verify(valueManager).cellUpdated(cell);
        verify(styler, never()).cellStyleUpdated(
                org.mockito.ArgumentMatchers.any(Cell.class),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    // ----- markCellAsDeleted -----------------------------------------------

    @Test
    void markCellAsDeleted_stylePropagateTrue_dispatchesStylerAndRefreshes() {
        // Pin: cellStyleUpdated=true triggers the deleted path AND clears
        // the cell's style by its address, as a cell out of its row can no
        // longer be read, AND the update that sends the deletion.
        when(cell.getRowIndex()).thenReturn(2);
        when(cell.getColumnIndex()).thenReturn(3);

        updater.markCellAsDeleted(cell, true);

        verify(valueManager).cellDeleted(cell);
        verify(styler).clearCellStyle(2, 3);
        verify(spreadsheet).updateMarkedCells();
    }

    @Test
    void markCellAsDeleted_stylePropagateFalse_skipsStyler_stillRefreshes() {
        // Pin: the update fires regardless of cellStyleUpdated, but
        // the styler call is guarded by the flag. A regression that
        // tied the update to the flag would silently break the
        // refresh path for value-only deletes.
        updater.markCellAsDeleted(cell, false);

        verify(valueManager).cellDeleted(cell);
        verify(styler, never()).clearCellStyle(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
        verify(spreadsheet).updateMarkedCells();
    }

    // ----- deleteCell ------------------------------------------------------

    @Test
    void deleteCell_cellExists_dispatchesFullChain() {
        // Pin: cell != null path triggers row.removeCell(cell) →
        // styler.clearCellStyle(2, 3) → valueManager.cellDeleted(cell) →
        // spreadsheet.updateMarkedCells(). The cell's style is cleared by its
        // address once it is out of its row, so what it drew goes with it.
        when(activeSheet.getRow(2)).thenReturn(row);
        when(row.getCell(3)).thenReturn(cell);

        updater.deleteCell(2, 3);

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(styler, row,
                valueManager, spreadsheet);
        inOrder.verify(row).removeCell(cell);
        inOrder.verify(styler).clearCellStyle(2, 3);
        inOrder.verify(valueManager).cellDeleted(cell);
        inOrder.verify(spreadsheet).updateMarkedCells();
    }

    @Test
    void deleteCell_cellMissing_isNoOp() {
        // Pin: cell == null path SKIPS ALL FOUR dispatches. A regression
        // that removed the null guard would NPE on styler.cellStyleUpdated
        // — the test would fail with NPE rather than verification, but
        // would still fail.
        when(activeSheet.getRow(2)).thenReturn(row);
        when(row.getCell(3)).thenReturn(null);

        updater.deleteCell(2, 3);

        verify(styler, never()).clearCellStyle(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
        verify(row, never())
                .removeCell(org.mockito.ArgumentMatchers.any(Cell.class));
        verify(valueManager, never())
                .cellDeleted(org.mockito.ArgumentMatchers.any(Cell.class));
        verify(spreadsheet, never())
                .refreshCells(org.mockito.ArgumentMatchers.any(Cell.class));
    }

    @Test
    void deleteCell_resolvesViaActiveSheetIndex() {
        // Pin: deleteCell MUST resolve the sheet via
        // workbook.getActiveSheetIndex() — NOT a hardcoded 0. A
        // regression that always used sheet 0 would break operations
        // on the non-first sheet.
        when(workbook.getActiveSheetIndex()).thenReturn(3);
        Sheet otherSheet = org.mockito.Mockito.mock(Sheet.class);
        when(workbook.getSheetAt(3)).thenReturn(otherSheet);
        when(otherSheet.getRow(0)).thenReturn(row);
        when(row.getCell(0)).thenReturn(cell);

        updater.deleteCell(0, 0);

        verify(workbook).getSheetAt(3);
        verify(row).removeCell(cell);
    }

    // ----- updateMarkedCells -----------------------------------------------

    @Test
    void updateMarkedCells_dispatchesFiveCollaboratorsInOrder() {
        // Pin the 5-call orchestration shape:
        // 1. conditionalFormatter.createConditionalFormatterRules()
        // 2. valueManager.updateMarkedCellValues()
        // 3. selectionManager.reSelectSelectedCell()
        // 4. cellCommentLoader.load()
        // 5. spreadsheet.reloadVisibleCellContents(recreateEditors)
        // The order is load-bearing: conditional-formatting rules MUST
        // be rebuilt BEFORE valueManager pushes the values (which would
        // otherwise miss the updated styles), and reloadVisibleCellContents
        // MUST be last (it depends on the freshly-loaded value cache).
        updater.updateMarkedCells(false);

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(
                conditionalFormatter, valueManager, selectionManager,
                cellCommentLoader, spreadsheet);
        inOrder.verify(conditionalFormatter).createConditionalFormatterRules();
        inOrder.verify(valueManager).updateMarkedCellValues();
        inOrder.verify(selectionManager).reSelectSelectedCell();
        inOrder.verify(cellCommentLoader).load();
        inOrder.verify(spreadsheet).reloadVisibleCellContents(false);
    }

    @Test
    void updateMarkedCells_recreatingEditors_passesTheFlagOn() {
        // Row insert and delete move cells, and an editor is cached against
        // a cell key — one left in place would end up on the wrong cell.
        updater.updateMarkedCells(true);

        org.mockito.Mockito.verify(spreadsheet).reloadVisibleCellContents(true);
    }

    // ----- refreshAllCellValues --------------------------------------------

    @Test
    void refreshAllCellValues_viewportLoaded_clearsCachesAndReloadsAndReselects() {
        // Pin the loaded-viewport path: firstColumn != -1 (viewport
        // has been loaded at least once) so the reload happens. Order:
        // 1. formulaEvaluator.clearAllCachedResultValues
        // 2. condFmtEvaluator.clearAllCachedValues
        // 3. valueManager.clearCachedContent
        // 4. spreadsheet.updateRowAndColumnRangeCellData(1, 1, rows, cols)
        // 5. selectionManager.reSelectSelectedCell
        when(spreadsheet.getFirstColumn()).thenReturn(1);
        when(spreadsheet.getRows()).thenReturn(20);
        when(spreadsheet.getColumns()).thenReturn(10);

        updater.refreshAllCellValues();

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(
                formulaEvaluator, condFmtEvaluator, valueManager, spreadsheet,
                selectionManager);
        inOrder.verify(formulaEvaluator).clearAllCachedResultValues();
        inOrder.verify(condFmtEvaluator).clearAllCachedValues();
        inOrder.verify(valueManager).clearCachedContent();
        inOrder.verify(spreadsheet).updateRowAndColumnRangeCellData(eq(1),
                eq(1), eq(20), eq(10));
        inOrder.verify(selectionManager).reSelectSelectedCell();
    }

    @Test
    void refreshAllCellValues_viewportNotLoaded_clearsCaches_but_skipsReloadAndReselect() {
        // Pin: firstColumn == -1 (initial state — never scrolled / no
        // data yet) → clear caches, then EARLY RETURN. Removing the
        // early-return would force an unwanted reload before the
        // client has even requested cells, causing duplicate work
        // and a redundant selection event.
        when(spreadsheet.getFirstColumn()).thenReturn(-1);

        updater.refreshAllCellValues();

        verify(formulaEvaluator).clearAllCachedResultValues();
        verify(condFmtEvaluator).clearAllCachedValues();
        verify(valueManager).clearCachedContent();
        verify(spreadsheet, never()).updateRowAndColumnRangeCellData(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
        verify(selectionManager, never()).reSelectSelectedCell();
    }

    @Test
    void refreshAllCellValues_clearOrder_evaluatorsBeforeValueCache() {
        // Pin: the formula + conditional-formatting evaluator caches
        // are cleared BEFORE the value-manager cache. The order
        // matters because valueManager's `clearCachedContent` may
        // re-trigger formula evaluation in the next update cycle —
        // doing it before the evaluators are cleared would resurrect
        // stale formula results.
        when(spreadsheet.getFirstColumn()).thenReturn(-1);

        updater.refreshAllCellValues();

        org.mockito.InOrder inOrder = org.mockito.Mockito
                .inOrder(formulaEvaluator, condFmtEvaluator, valueManager);
        inOrder.verify(formulaEvaluator).clearAllCachedResultValues();
        inOrder.verify(condFmtEvaluator).clearAllCachedValues();
        inOrder.verify(valueManager).clearCachedContent();
    }
}
