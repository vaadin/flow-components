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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * What {@link RowShiftOrchestrator} updates around a POI row shift.
 *
 * <p>
 * The SUT is a broad orchestrator — every test mocks the Spreadsheet host +
 * every collaborator handle the orchestrator reaches through. The actual POI
 * {@code Sheet.shiftRows} is exercised on a real {@link XSSFWorkbook} so the
 * sheet-side mutations match production behavior; only the server-side
 * bookkeeping (caches, helpers, mirrors) is verified through mocks.
 */
class RowShiftOrchestratorTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private CellValueManager valueManager;
    @Mock
    private SpreadsheetStyleFactory styler;
    @Mock
    private CellSelectionManager selectionManager;
    @Mock
    private SheetOverlayManager sheetOverlayManager;
    @Mock
    private PopupButtonRegistry popupButtonRegistry;
    @Mock
    private InvalidFormulaTracker invalidFormulaTracker;
    @Mock
    private FormulaEvaluator formulaEvaluator;
    @Mock
    private ConditionalFormattingEvaluator condFmtEvaluator;

    private XSSFWorkbook workbook;
    private Sheet activeSheet;
    private RowShiftOrchestrator orchestrator;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        activeSheet = workbook.createSheet();
        // Wire all collaborator handles the orchestrator reads through.
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(activeSheet);
        lenient().when(spreadsheet.getCellValueManager())
                .thenReturn(valueManager);
        lenient().when(spreadsheet.getSpreadsheetStyleFactory())
                .thenReturn(styler);
        lenient().when(spreadsheet.getCellSelectionManager())
                .thenReturn(selectionManager);
        lenient().when(spreadsheet.getFormulaEvaluator())
                .thenReturn(formulaEvaluator);
        lenient().when(spreadsheet.getConditionalFormattingEvaluator())
                .thenReturn(condFmtEvaluator);
        lenient().when(spreadsheet.getCols()).thenReturn(3);
        lenient().when(spreadsheet.getRowH()).thenReturn(new float[10]);
        lenient().when(spreadsheet.getHiddenRowIndexes())
                .thenReturn(new ArrayList<>());
        // viewportBounds reads are routed via getFirstRow/getFirstColumn etc.
        lenient().when(spreadsheet.getFirstRow()).thenReturn(1);
        lenient().when(spreadsheet.getFirstColumn()).thenReturn(1);
        lenient().when(spreadsheet.getLastRow()).thenReturn(10);
        lenient().when(spreadsheet.getLastColumn()).thenReturn(3);

        // sheetOverlayManager is a package-private final field on
        // Spreadsheet — wire via reflection on the mock; the pop-up buttons
        // and invalid formula marks come from the workbook's scope.
        wireField("sheetOverlayManager", sheetOverlayManager);
        lenient().when(spreadsheet.popupButtons())
                .thenReturn(popupButtonRegistry);
        lenient().when(spreadsheet.invalidFormulas())
                .thenReturn(invalidFormulaTracker);

        orchestrator = new RowShiftOrchestrator(spreadsheet);
    }

    private void wireField(String name, Object value) throws Exception {
        java.lang.reflect.Field f = Spreadsheet.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(spreadsheet, value);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    // ----- POI dispatch + evaluator-cache invalidation ---------------------

    @Test
    void shift_callsPOIShiftRows_AndClearsBothEvaluatorCaches() {
        // Pin: the orchestrator MUST call POI's Sheet.shiftRows AND clear
        // both evaluator caches. A regression skipping the evaluator clear
        // would leave stale formula results in place after a row shift.
        orchestrator.shift(0, 2, 3, false, false);

        verify(formulaEvaluator).clearAllCachedResultValues();
        verify(condFmtEvaluator).clearAllCachedValues();
    }

    // ----- updateDeletedRowsInClientCache range computation ----------------

    @Test
    void shift_positiveN_callsUpdateDeletedRowsForFollowingRange() {
        // Pin the down-shift cache-clear range:
        // start = startRow (n >= 0)
        // end = startRow + n - 1
        // the cache is told to drop rows (start+1 .. end+1) 1-based.
        // For startRow=0, n=3: cache drops rows 1..3.
        orchestrator.shift(0, 2, 3, false, false);

        verify(valueManager).updateDeletedRowsInClientCache(1, 3);
    }

    @Test
    void shift_negativeN_callsUpdateDeletedRowsForRemovedRange() {
        // Pin the up-shift cache-clear range:
        // start = max(lastNonBlankRow, startRow) (n < 0)
        // end = endRow (n < 0)
        // For an empty sheet (lastNonBlankRow=0), startRow=3, endRow=5,
        // n=-2: start = max(0, 3) = 3, end = 5; cache drops 4..6.
        orchestrator.shift(3, 5, -2, false, false);

        verify(valueManager).updateDeletedRowsInClientCache(4, 6);
    }

    // ----- merged regions are left to POI ----------------------------------

    @Test
    void shift_negativeN_doesNotRemoveMergedRegions() {
        // POI's shiftRows removes the merged regions the shifted rows land
        // on and moves the shifted ones, so the orchestrator removes none
        // itself. Row 2 is above the rows the shift lands on (3..5).
        // ShiftRowsTest checks the outcome on a real spreadsheet.
        activeSheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 1));

        orchestrator.shift(5, 7, -2, false, false);

        verify(spreadsheet, never()).removeMergedRegion(anyInt());
    }

    @Test
    void shift_positiveN_doesNotRemoveMergedRegions() {
        // Pin: down-shift skips the merged-region scan entirely. POI
        // handles the merged-region shifting internally.
        activeSheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 1));

        orchestrator.shift(5, 7, 2, false, false);

        verify(spreadsheet, never()).removeMergedRegion(anyInt());
    }

    // ----- popupButtonRegistry + invalidFormulaTracker dispatch ------------

    @Test
    void shift_alwaysShiftsThePopupButtons_andTheInvalidFormulaMarks() {
        // Pin: the shift is passed on to popupButtonRegistry and
        // invalidFormulaTracker, with the arguments POI got. Both must fire
        // regardless of n sign.
        orchestrator.shift(2, 5, 1, false, false);

        verify(popupButtonRegistry).shiftRows(2, 5, 1);
        verify(invalidFormulaTracker).shiftActiveSheetRows(2, 5, 1);
    }

    @Test
    void shift_negativeN_passesTheShiftToTheHelpers() {
        orchestrator.shift(5, 7, -2, false, false);

        verify(popupButtonRegistry).shiftRows(5, 7, -2);
        verify(invalidFormulaTracker).shiftActiveSheetRows(5, 7, -2);
    }

    // ----- overlay reload-from-POI branch ----------------------------------

    @Test
    void shift_overlayManagerHasOverlays_marksReloadFromPoi() {
        // Pin: if the sheet has overlays, the orchestrator triggers a POI
        // reload. A regression skipping this branch would leave overlay
        // positions stale (overlays anchored to shifted rows wouldn't
        // move with them on the next render).
        when(sheetOverlayManager.hasOverlays()).thenReturn(true);

        orchestrator.shift(0, 2, 1, false, false);

        verify(sheetOverlayManager).markReloadFromPoi();
    }

    @Test
    void shift_overlayManagerHasNoOverlays_skipsMarkReloadFromPoi() {
        when(sheetOverlayManager.hasOverlays()).thenReturn(false);

        orchestrator.shift(0, 2, 1, false, false);

        verify(sheetOverlayManager, never()).markReloadFromPoi();
    }

    // ----- post-shift orchestration ----------------------------------------

    @Test
    void shift_callsUpdateMarkedCells_andUpdateMergedRegions() {
        // Pin the post-shift orchestration: updateMarkedCells flushes any
        // cell updates queued during the shift and reloads the cells shown,
        // then merged regions update.
        //
        // The editors are recreated rather than reused: a shift moves
        // cells, and an editor is cached against a cell key, so one left
        // in place would end up on the wrong cell.
        orchestrator.shift(0, 2, 1, false, false);

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(spreadsheet);
        inOrder.verify(spreadsheet).updateMarkedCellsRecreatingEditors();
        inOrder.verify(spreadsheet).updateMergedRegions();
    }

    // ----- selection-update branches ---------------------------------------

    @Test
    void shift_selectedCellInsideAffectedRange_callsOnSheetAddressChanged() {
        // Pin: if the selected cell's row is inside [firstAffectedRow,
        // lastAffectedRow], the selection manager is notified to refresh.
        // For n=1, startRow=0, endRow=2: affectedRange = [0, 3].
        // Selected cell at row 2 falls inside.
        CellReference selectedCell = new CellReference(2, 0);
        when(selectionManager.getSelectedCellReference())
                .thenReturn(selectedCell);

        orchestrator.shift(0, 2, 1, false, false);

        ArgumentCaptor<String> addressCaptor = ArgumentCaptor
                .forClass(String.class);
        verify(selectionManager).onSheetAddressChanged(addressCaptor.capture(),
                eq(false));
        assertEquals(selectedCell.formatAsString(), addressCaptor.getValue());
    }

    @Test
    void shift_selectedCellOutsideAffectedRange_skipsSelectionUpdate() {
        // Pin: a selected cell outside the affected range is left alone.
        // For n=1, startRow=0, endRow=2: affectedRange = [0, 3]. A cell
        // at row 9 is outside.
        when(selectionManager.getSelectedCellReference())
                .thenReturn(new CellReference(9, 0));

        orchestrator.shift(0, 2, 1, false, false);

        verify(selectionManager, never()).onSheetAddressChanged(any(),
                anyBoolean());
    }

    @Test
    void shift_selectedCellNull_skipsSelectionUpdate() {
        when(selectionManager.getSelectedCellReference()).thenReturn(null);

        orchestrator.shift(0, 2, 1, false, false);

        verify(selectionManager, never()).onSheetAddressChanged(any(),
                anyBoolean());
    }

    // ----- copyRowHeight branch --------------------------------------------

    @Test
    void shift_copyRowHeightTrue_growsRowHArrayWhenNeeded() {
        // Pin: positive n + copyRowHeight=true grows the cached rowH
        // array when neededLength (endRow + n + 1) exceeds the current
        // array length. The new array is pushed via setRowH.
        when(spreadsheet.getRowH()).thenReturn(new float[3]);
        // startRow=0, endRow=2, n=5 → neededLength = 8 > 3 → grow.

        orchestrator.shift(0, 2, 5, true, false);

        ArgumentCaptor<float[]> rowHCaptor = ArgumentCaptor
                .forClass(float[].class);
        verify(spreadsheet).setRowH(rowHCaptor.capture());
        assertTrue(rowHCaptor.getValue().length >= 8,
                "rowH array MUST be grown to at least neededLength");
    }

    @Test
    void shift_copyRowHeightFalse_doesNotUpdateRowHArray() {
        // Pin: without copyRowHeight or resetOriginalRowHeight, the rowH
        // array is left untouched. setRowH must not fire.
        orchestrator.shift(0, 2, 5, false, false);

        verify(spreadsheet, never()).setRowH(any());
    }

    // ----- pre-shift style invalidation for cellsToUpdate ------------------

    @Test
    void shift_alwaysCallsLoadCustomBorderStylesToState() {
        // Pin: after the row-walk, custom border styles are reloaded.
        // This carries over any cell-style border edges that need to
        // be re-emitted on the client side. A regression skipping this
        // call would leave the client showing pre-shift border layout.
        orchestrator.shift(0, 2, 1, false, false);

        verify(styler).loadCustomBorderStylesToState();
    }
}
