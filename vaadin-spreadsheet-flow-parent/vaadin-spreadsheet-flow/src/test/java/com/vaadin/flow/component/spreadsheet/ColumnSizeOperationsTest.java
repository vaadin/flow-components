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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Setting column widths and hiding columns with {@link ColumnSizeOperations}.
 *
 * <p>
 * Sibling of {@link RowSizeOperations} but with tighter coupling — the
 * operations maintain TWO synchronized states ({@code hiddenColumnIndexes}
 * 1-based list + {@code colW} 0-based array), they downcast to
 * {@code XSSFSheet} for column-helper cleanup, and they call into
 * {@code CellValueManager} for cache invalidation. Pins:
 *
 * <ul>
 * <li>Width=0 routes through applyHidden — no direct setWidth path.</li>
 * <li>Width=positive on a hidden column unhides it (POI + the 1-based
 * list).</li>
 * <li>Both state collections (hiddenColumnIndexes + colW) get fresh-copy writes
 * on every operation.</li>
 * <li>XSSF cleanColumns is called only when the sheet is an XSSFSheet.</li>
 * <li>CellValueManager.clearCacheForColumn + loadCellData fire on width change
 * and on un-hide.</li>
 * </ul>
 */
class ColumnSizeOperationsTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private XSSFSheet sheet;
    @Mock
    private ColumnHelper columnHelper;
    @Mock
    private CellValueManager valueManager;

    private ColumnSizeOperations ops;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ops = new ColumnSizeOperations(spreadsheet);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        lenient().when(spreadsheet.getCellValueManager())
                .thenReturn(valueManager);
        lenient().when(spreadsheet.getHiddenColumnIndexes())
                .thenReturn(new ArrayList<>());
        lenient().when(spreadsheet.getColW())
                .thenReturn(new int[] { 50, 60, 70 });
        lenient().when(spreadsheet.getFirstRow()).thenReturn(1);
        lenient().when(spreadsheet.getLastRow()).thenReturn(10);
        lenient().when(sheet.getColumnHelper()).thenReturn(columnHelper);
    }

    @Test
    void setWidth_positive_setsColW_andCallsSheetSetColumnWidth() {
        ops.setWidth(1, 88);

        ArgumentCaptor<int[]> colCaptor = ArgumentCaptor.forClass(int[].class);
        verify(spreadsheet).setColW(colCaptor.capture());
        assertEquals(88, colCaptor.getValue()[1]);
        // Pin: POI gets the width via SpreadsheetUtil.pixel2WidthUnits.
        // 88 px is roundtripped through pixel2WidthUnits so the assertion
        // tracks the public conversion function. A regression that skipped
        // the conversion (e.g. passed pixels directly) would fail here
        // because the int value differs.
        verify(sheet).setColumnWidth(eqInt(1),
                eqInt(SpreadsheetUtil.pixel2WidthUnits(88)));
    }

    @Test
    void setWidth_positive_doesNotReloadStyles() {
        // Pin: only the width=0 path triggers reloadSheetStyles. Positive
        // widths skip the reload — the client applies the new width
        // through the colW property mirror.
        ops.setWidth(1, 88);

        verify(spreadsheet, never()).reloadSheetStyles(anyBoolean(),
                anyBoolean());
    }

    @Test
    void setWidth_zero_callsApplyHidden_andReloadsStyles() {
        ops.setWidth(1, 0);

        verify(sheet).setColumnHidden(1, true);
        verify(spreadsheet).reloadSheetStyles(false, true);
    }

    @Test
    void setWidth_positive_clearsCellCache_andLoadsRange() {
        ops.setWidth(1, 88);

        verify(valueManager).clearCacheForColumn(2); // 1-based column 2
        verify(valueManager).loadCellData(1, 2, 10, 2);
    }

    @Test
    void setWidth_positive_unhidesPreviouslyHiddenColumn() {
        // Pin: setting width to a positive value on a column that POI
        // considers hidden also clears the hidden state both in POI AND
        // in the spreadsheet's hidden-index list.
        ArrayList<Integer> hidden = new ArrayList<>();
        hidden.add(2); // 1-based equivalent of column index 1
        when(spreadsheet.getHiddenColumnIndexes()).thenReturn(hidden);
        when(sheet.isColumnHidden(1)).thenReturn(true);

        ops.setWidth(1, 88);

        verify(sheet).setColumnHidden(1, false);
        // hiddenColumnIndexes list passed to setter has 2 removed.
        ArgumentCaptor<ArrayList<Integer>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setHiddenColumnIndexes(captor.capture());
        assertFalse(captor.getValue().contains(2),
                "Previously hidden index should be removed");
    }

    @Test
    void setWidth_buildsFreshHiddenListAndColW_doesNotMutatePrior() {
        // Pin defensive-copy on BOTH state collections: the operation
        // copies the hidden list AND the colW array before mutation,
        // so the original references are untouched.
        ArrayList<Integer> originalHidden = new ArrayList<>();
        originalHidden.add(99);
        int[] originalColW = { 50, 60, 70 };
        when(spreadsheet.getHiddenColumnIndexes()).thenReturn(originalHidden);
        when(spreadsheet.getColW()).thenReturn(originalColW);

        ops.setWidth(0, 88);

        // Original arrays / lists unchanged.
        assertEquals(50, originalColW[0]);
        assertTrue(originalHidden.contains(99));
        assertEquals(1, originalHidden.size());
    }

    @Test
    void setWidth_onXSSFSheet_callsCleanColumns() {
        // Pin: the XSSFSheet downcast + cleanColumns is part of the
        // positive-width contract. The setUp uses XSSFSheet, so this
        // exercises the instanceof branch directly.
        ops.setWidth(1, 88);

        verify(columnHelper).cleanColumns();
    }

    @Test
    void setWidths_anyZero_reloadsStylesWithTrue() {
        Map<Integer, Integer> widths = new HashMap<>();
        widths.put(0, 50);
        widths.put(1, 0); // hidden

        ops.setWidths(widths);

        verify(spreadsheet).reloadSheetStyles(false, true);
    }

    @Test
    void setWidths_allPositive_skipsReloadStyles() {
        Map<Integer, Integer> widths = new HashMap<>();
        widths.put(0, 50);
        widths.put(1, 60);

        ops.setWidths(widths);

        verify(spreadsheet, never()).reloadSheetStyles(anyBoolean(),
                anyBoolean());
    }

    @Test
    void setHidden_true_addsToHiddenList_andZeroesColW() {
        ops.setHidden(1, true);

        verify(sheet).setColumnHidden(1, true);
        ArgumentCaptor<ArrayList<Integer>> hiddenCaptor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setHiddenColumnIndexes(hiddenCaptor.capture());
        assertTrue(hiddenCaptor.getValue().contains(2)); // 1-based

        ArgumentCaptor<int[]> colCaptor = ArgumentCaptor.forClass(int[].class);
        verify(spreadsheet).setColW(colCaptor.capture());
        assertEquals(0, colCaptor.getValue()[1]);

        verify(spreadsheet).reloadSheetStyles(false, true);
    }

    @Test
    void setHidden_alreadyHidden_doesNotDuplicate() {
        // Pin: if the column index is already in the hidden list, calling
        // setHidden(true) again does NOT add a duplicate entry. The
        // colW[idx] also stays 0.
        ArrayList<Integer> alreadyHidden = new ArrayList<>();
        alreadyHidden.add(2);
        when(spreadsheet.getHiddenColumnIndexes()).thenReturn(alreadyHidden);

        ops.setHidden(1, true);

        ArgumentCaptor<ArrayList<Integer>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setHiddenColumnIndexes(captor.capture());
        // Still exactly one occurrence of 2.
        assertEquals(1, captor.getValue().stream().filter(v -> v == 2).count());
    }

    @Test
    void setHidden_false_removesFromHiddenList_andRestoresColW() {
        // Pin: un-hide restores the colW from the POI sheet's column
        // width AND clears + reloads the cell cache.
        ArrayList<Integer> hidden = new ArrayList<>();
        hidden.add(2);
        when(spreadsheet.getHiddenColumnIndexes()).thenReturn(hidden);
        when(sheet.getColumnWidthInPixels(1)).thenReturn(75f);

        ops.setHidden(1, false);

        verify(sheet).setColumnHidden(1, false);
        ArgumentCaptor<int[]> colCaptor = ArgumentCaptor.forClass(int[].class);
        verify(spreadsheet).setColW(colCaptor.capture());
        assertEquals(75, colCaptor.getValue()[1]);

        verify(valueManager).clearCacheForColumn(2);
        verify(valueManager).loadCellData(1, 2, 10, 2);
        verify(spreadsheet).reloadSheetStyles(false, true);
    }

    @Test
    void setHidden_falseOnNotHiddenColumn_isMostlyNoOp() {
        // Pin: setHidden(false) on a column that isn't actually in the
        // hidden list still calls setColumnHidden(false) on POI but the
        // colW restore + cache invalidation branch is skipped.
        ops.setHidden(1, false);

        verify(sheet).setColumnHidden(1, false);
        verify(valueManager, never()).clearCacheForColumn(2);
        verify(valueManager, never()).loadCellData(anyInt(), anyInt(), anyInt(),
                anyInt());
        // reloadSheetStyles still fires (the setHidden() outer always
        // reloads, regardless of the no-op branch inside applyHidden).
        verify(spreadsheet).reloadSheetStyles(false, true);
    }

    @Test
    void setColumnsHidden_appliesPerColumn_reloadsOnce() {
        Map<Integer, Boolean> cols = new HashMap<>();
        cols.put(0, true);
        cols.put(1, false);

        ops.setColumnsHidden(cols);

        verify(sheet).setColumnHidden(0, true);
        verify(sheet).setColumnHidden(1, false);
        verify(spreadsheet, times(1)).reloadSheetStyles(false, true);
    }

    @Test
    void setDefaultWidthPixels_setsSpreadsheetDefColW_only() {
        // Pin: defaultColW is the simplest path — just delegates to the
        // spreadsheet's setDefColW. No POI sheet update, no reload.
        ops.setDefaultWidthPixels(80);

        verify(spreadsheet).setDefColW(80);
        verify(sheet, never()).setColumnWidth(anyInt(), anyInt());
        verify(spreadsheet, never()).reloadSheetStyles(anyBoolean(),
                anyBoolean());
    }

    @Test
    void setWidth_oneBasedListIndexing_isUsed() {
        // Pin: the hidden list uses 1-based indexing (index+1). Setting
        // width on POI column index 1 checks for "2" in the hidden list,
        // not "1".
        ArrayList<Integer> hidden = new ArrayList<>();
        hidden.add(1); // 1-based equivalent of POI col index 0
        when(spreadsheet.getHiddenColumnIndexes()).thenReturn(hidden);

        ops.setWidth(1, 88); // POI col 1 → 1-based 2

        // The existing index 1 is NOT removed (corresponds to a different
        // column).
        ArgumentCaptor<ArrayList<Integer>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setHiddenColumnIndexes(captor.capture());
        assertTrue(captor.getValue().contains(1));
    }

    // ----- helpers ----------------------------------------------------------

    private static int eqInt(int value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
