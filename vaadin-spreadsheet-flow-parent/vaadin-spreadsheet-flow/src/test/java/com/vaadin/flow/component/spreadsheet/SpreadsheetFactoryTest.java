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
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

class SpreadsheetFactoryTest {

    @Mock
    private Spreadsheet spreadsheet;

    private final SheetClientModel model = new SheetClientModel(
            new Element("vaadin-spreadsheet"));

    private XSSFWorkbook workbook;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        when(spreadsheet.sheetModel()).thenReturn(model);
    }

    @Test
    void loadFreezePane_bothSplits_setsBothPositions() {
        // POI: createFreezePane(colSplit, rowSplit)
        // colSplit → POI verticalSplitPosition (columns)
        // rowSplit → POI horizontalSplitPosition (rows)
        Sheet sheet = workbook.createSheet();
        sheet.createFreezePane(5, 4);

        when(spreadsheet.getActiveSheet()).thenReturn(sheet);

        SpreadsheetFactory.loadFreezePane(spreadsheet);

        // Spreadsheet swaps the naming:
        // POI verticalSplit (cols=5) → Spreadsheet horizontalSplitPosition
        // POI horizontalSplit (rows=4) → Spreadsheet verticalSplitPosition
        assertEquals(5, model.horizontalSplitPosition.get());
        assertEquals(4, model.verticalSplitPosition.get());
    }

    @Test
    void loadFreezePane_onlyFrozenRows_resetsHorizontalToZero() {
        // Only frozen rows (POI horizontalSplit), no frozen columns
        Sheet sheet = workbook.createSheet();
        sheet.createFreezePane(0, 3);

        when(spreadsheet.getActiveSheet()).thenReturn(sheet);

        SpreadsheetFactory.loadFreezePane(spreadsheet);

        assertEquals(0, model.horizontalSplitPosition.get());
        assertEquals(3, model.verticalSplitPosition.get());
    }

    @Test
    void loadFreezePane_onlyFrozenColumns_resetsVerticalToZero() {
        // Only frozen columns (POI verticalSplit), no frozen rows
        Sheet sheet = workbook.createSheet();
        sheet.createFreezePane(2, 0);

        when(spreadsheet.getActiveSheet()).thenReturn(sheet);

        SpreadsheetFactory.loadFreezePane(spreadsheet);

        assertEquals(2, model.horizontalSplitPosition.get());
        assertEquals(0, model.verticalSplitPosition.get());
    }

    @Test
    void loadFreezePane_noFreezePane_resetsBothToZero() {
        Sheet sheet = workbook.createSheet();

        when(spreadsheet.getActiveSheet()).thenReturn(sheet);

        SpreadsheetFactory.loadFreezePane(spreadsheet);

        assertEquals(0, model.horizontalSplitPosition.get());
        assertEquals(0, model.verticalSplitPosition.get());
    }

    @Test
    void loadFreezePane_switchingSheets_doesNotBleedState() {
        // Simulate switching from a sheet with both splits to one with
        // only frozen columns — the vertical split (frozen rows) must
        // be reset to 0 and not retain the previous sheet's value.
        Sheet bothSplits = workbook.createSheet();
        bothSplits.createFreezePane(5, 4);

        Sheet onlyColumns = workbook.createSheet();
        onlyColumns.createFreezePane(2, 0);

        // Load first sheet (both splits)
        when(spreadsheet.getActiveSheet()).thenReturn(bothSplits);
        SpreadsheetFactory.loadFreezePane(spreadsheet);

        assertEquals(5, model.horizontalSplitPosition.get());
        assertEquals(4, model.verticalSplitPosition.get());

        Mockito.reset(spreadsheet);
        when(spreadsheet.sheetModel()).thenReturn(model);

        // Switch to second sheet (only columns) — vertical must reset
        when(spreadsheet.getActiveSheet()).thenReturn(onlyColumns);
        SpreadsheetFactory.loadFreezePane(spreadsheet);

        assertEquals(2, model.horizontalSplitPosition.get());
        assertEquals(0, model.verticalSplitPosition.get());
    }
}
