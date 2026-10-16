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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.tests.CapturedLogs;
import com.vaadin.flow.component.spreadsheet.tests.ExpectWarnings;

/**
 * Fitting a column to its content with {@link ColumnAutofitter}.
 *
 * <p>
 * Three concerns pinned:
 *
 * <ul>
 * <li>POI's {@code autoSizeColumn} can throw NPE — the autofitter catches and
 * silently aborts (no cache, no colW change).</li>
 * <li>Columns hosting a filter popup button reserve extra width
 * ({@code FILTER_BUTTON_PIXEL_WIDTH} + {@code FILTER_BUTTON_PIXEL_PADDING} = 16
 * px) — the autofitted width is bumped before being stored.</li>
 * <li>The autofitted-widths cache (WeakHashMap, per-CellReference) is consulted
 * by {@code refreshAutofittedColumnsInTable} to decide which columns to
 * re-autofit (only if the current width still matches the recorded
 * autofit).</li>
 * </ul>
 *
 * <p>
 * Direct field access on {@code Spreadsheet.sheetOverlayManager} works on a
 * Mockito mock — Mockito creates a real subclass instance and the
 * package-private field is writable from the test.
 */
class ColumnAutofitterTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Sheet sheet;
    @Mock
    private CellValueManager valueManager;
    @Mock
    private SheetOverlayManager overlayManager;
    @Mock
    private SpreadsheetHistoryManager historyManager;

    private ColumnAutofitter autofitter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        autofitter = new ColumnAutofitter(spreadsheet);
        // sheetOverlayManager is a final field; set via reflection so the
        // autofit() code path doesn't NPE on spreadsheet.sheetOverlayManager.
        java.lang.reflect.Field f = Spreadsheet.class
                .getDeclaredField("sheetOverlayManager");
        f.setAccessible(true);
        f.set(spreadsheet, overlayManager);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        lenient().when(spreadsheet.getCellValueManager())
                .thenReturn(valueManager);
        lenient().when(spreadsheet.getSpreadsheetHistoryManager())
                .thenReturn(historyManager);
        lenient().when(spreadsheet.getColW())
                .thenReturn(new int[] { 50, 60, 70 });
        lenient().when(spreadsheet.getFirstRow()).thenReturn(1);
        lenient().when(spreadsheet.getLastRow()).thenReturn(10);
        lenient().when(spreadsheet.getTablesForActiveSheet())
                .thenReturn(Collections.emptyList());
        lenient().when(sheet.getSheetName()).thenReturn("Sheet1");
        lenient().when(sheet.getColumnWidthInPixels(anyInt())).thenReturn(100f);
    }

    @Test
    void autofit_callsPoiAutoSizeColumn_andUpdatesColW() {
        autofitter.autofit(1);

        verify(sheet).autoSizeColumn(1);
        ArgumentCaptor<int[]> captor = ArgumentCaptor.forClass(int[].class);
        verify(spreadsheet).setColW(captor.capture());
        assertEquals(100, captor.getValue()[1]); // no filter padding
    }

    @Test
    void autofit_clearsCellCache_andLoadsRange() {
        autofitter.autofit(1);

        verify(valueManager).clearCacheForColumn(2); // 1-based
        verify(valueManager).loadCellData(1, 2, 10, 2);
    }

    @Test
    @ExpectWarnings
    void autofit_npeFromPoi_abortsWithAWarning_noColWUpdate() {
        // POI's autoSizeColumn can throw NPE where fonts are missing; the
        // autofitter reports it and returns without touching colW or the
        // cell value manager.
        org.mockito.Mockito.doThrow(new NullPointerException("poi")).when(sheet)
                .autoSizeColumn(1);

        List<CapturedLogs.Entry> logged;
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            autofitter.autofit(1);
            logged = logs.entries();
        }

        verify(spreadsheet, never()).setColW(any());
        verify(valueManager, never()).clearCacheForColumn(anyInt());
        assertEquals(List.of("Could not autofit column 2"),
                logged.stream().map(CapturedLogs.Entry::message).toList());
    }

    @Test
    void autofit_filterColumnWithPopupButton_addsExtraPixels() {
        // Pin the filter-padding contract: a column hosting a popup
        // button on a SpreadsheetFilterTable gets the autofit width +
        // FILTER_BUTTON_PIXEL_WIDTH (14) + FILTER_BUTTON_PIXEL_PADDING (2)
        // = +16px.
        SpreadsheetFilterTable filterTable = mock(SpreadsheetFilterTable.class);
        PopupButton button = mock(PopupButton.class);
        when(spreadsheet.getTablesForActiveSheet())
                .thenReturn(Arrays.asList(filterTable));
        when(filterTable.getPopupButton(any(CellReference.class)))
                .thenReturn(button);

        autofitter.autofit(1);

        ArgumentCaptor<int[]> captor = ArgumentCaptor.forClass(int[].class);
        verify(spreadsheet).setColW(captor.capture());
        assertEquals(116, captor.getValue()[1]); // 100 + 14 + 2
    }

    @Test
    void autofit_nonFilterTable_doesNotAddPadding() {
        SpreadsheetTable plainTable = mock(SpreadsheetTable.class);
        when(spreadsheet.getTablesForActiveSheet())
                .thenReturn(Arrays.asList(plainTable));

        autofitter.autofit(1);

        ArgumentCaptor<int[]> captor = ArgumentCaptor.forClass(int[].class);
        verify(spreadsheet).setColW(captor.capture());
        assertEquals(100, captor.getValue()[1]); // no padding
    }

    @Test
    void autofit_filterTableButNoPopupForColumn_doesNotAddPadding() {
        SpreadsheetFilterTable filterTable = mock(SpreadsheetFilterTable.class);
        when(spreadsheet.getTablesForActiveSheet())
                .thenReturn(Arrays.asList(filterTable));
        when(filterTable.getPopupButton(any(CellReference.class)))
                .thenReturn(null);

        autofitter.autofit(1);

        ArgumentCaptor<int[]> captor = ArgumentCaptor.forClass(int[].class);
        verify(spreadsheet).setColW(captor.capture());
        assertEquals(100, captor.getValue()[1]);
    }

    @Test
    void autofit_noOverlays_skipsOverlayManagerCalls() {
        when(overlayManager.hasOverlays()).thenReturn(false);

        autofitter.autofit(1);

        verify(overlayManager, never()).markReloadFromPoi();
        verify(overlayManager, never()).loadOrUpdate();
    }

    @Test
    void autofit_withOverlays_marksReload_andTriggersUpdate() {
        when(overlayManager.hasOverlays()).thenReturn(true);

        autofitter.autofit(1);

        verify(overlayManager).markReloadFromPoi();
        verify(overlayManager).loadOrUpdate();
    }

    @Test
    void onClientAutofit_recordsHistoryCommand_andRunsAutofit() {
        autofitter.onClientAutofit(1);

        verify(historyManager).addCommand(any());
        // Through the component, so an override of autofitColumn applies.
        verify(spreadsheet).autofitColumn(1);
    }

    @Test
    void refreshAutofittedColumnsInTable_emptyCache_doesNothing() {
        SpreadsheetFilterTable table = mock(SpreadsheetFilterTable.class);
        when(table.getSheet()).thenReturn(sheet);
        when(table.getFullTableRegion())
                .thenReturn(new CellRangeAddress(0, 5, 0, 3));

        autofitter.refreshAutofittedColumnsInTable(table);

        // No autofit calls because the cache is empty.
        verify(sheet, never()).autoSizeColumn(anyInt());
        verify(spreadsheet, never()).autofitColumn(anyInt());
    }

    @Test
    void refreshAutofittedColumnsInTable_widthChanged_skipsRefit() {
        // Pin: if the current column width differs from the recorded
        // autofit width, the column is NOT re-autofitted (user has
        // manually resized in the meantime).
        SpreadsheetFilterTable table = mock(SpreadsheetFilterTable.class);
        when(table.getSheet()).thenReturn(sheet);
        when(table.getFullTableRegion())
                .thenReturn(new CellRangeAddress(0, 5, 1, 1));

        // First: register col 1 in the cache via an autofit pass.
        when(sheet.getColumnWidthInPixels(1)).thenReturn(100f);
        autofitter.autofit(1);

        // Then: simulate user resize — current width != recorded width.
        when(sheet.getColumnWidthInPixels(1)).thenReturn(150f);
        org.mockito.Mockito.clearInvocations(sheet);

        autofitter.refreshAutofittedColumnsInTable(table);

        verify(sheet, never()).autoSizeColumn(anyInt());
        verify(spreadsheet, never()).autofitColumn(anyInt());
    }

    @Test
    void refreshAutofittedColumnsInTable_widthMatches_refitsColumn() {
        // Pin: if the current width matches the recorded autofit width,
        // the column IS re-autofitted (the user hasn't touched it).
        SpreadsheetFilterTable table = mock(SpreadsheetFilterTable.class);
        when(table.getSheet()).thenReturn(sheet);
        when(table.getFullTableRegion())
                .thenReturn(new CellRangeAddress(0, 5, 1, 1));

        // First autofit: caches width 100 for col 1.
        when(sheet.getColumnWidthInPixels(1)).thenReturn(100f);
        autofitter.autofit(1);
        // Current width still matches.
        org.mockito.Mockito.clearInvocations(sheet);
        when(sheet.getColumnWidthInPixels(1)).thenReturn(100f);

        autofitter.refreshAutofittedColumnsInTable(table);

        verify(spreadsheet).autofitColumn(1);
    }
}
