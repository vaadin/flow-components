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

import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.tests.CapturedLogs;
import com.vaadin.flow.component.spreadsheet.tests.ExpectWarnings;
import com.vaadin.tests.MockUIExtension;

/**
 * Loading a sheet that POI cannot fully read: what it can read is loaded, and
 * what it cannot is reported.
 */
class SheetLoadingTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    @ExpectWarnings
    void aPartPoiCannotRead_isReported_andTheRestLoaded() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        sheet.addMergedRegion(CellRangeAddress.valueOf("B2:C3"));
        workbook.createName().setNameName("Later");
        workbook.getName("Later").setRefersToFormula("Sheet0!$A$1");
        // A frozen pane naming a cell that is not one
        sheet.getCTWorksheet().getSheetViews().getSheetViewArray(0).addNewPane()
                .setTopLeftCell("not a cell");

        Spreadsheet spreadsheet;
        List<CapturedLogs.Entry> logged;
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            spreadsheet = new Spreadsheet(workbook);
            logged = logs.entries();
        }

        assertEquals(1, logged.size());
        assertEquals("Could not load the frozen panes of the sheet",
                logged.get(0).message());
        assertEquals(1, spreadsheet.sheetModel().mergedRegions.get().size());
        assertTrue(spreadsheet.getElement().getProperty("namedRanges", "")
                .contains("Later"));
    }
}
