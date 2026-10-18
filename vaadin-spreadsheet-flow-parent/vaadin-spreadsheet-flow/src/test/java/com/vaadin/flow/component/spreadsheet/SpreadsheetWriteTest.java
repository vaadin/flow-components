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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * Writing the workbook to a file: the file holds the workbook, which stays the
 * one shown with what the application set on it, and a failure to write it is
 * the caller's to see.
 */
class SpreadsheetWriteTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @TempDir
    Path folder;

    /** A workbook of one sheet, "Data", whose A1 says "written". */
    private static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Data").createRow(0).createCell(0)
                .setCellValue("written");
        return workbook;
    }

    /** The client reports the rows and columns it shows, as on a scroll. */
    private static void clientShowsRows(Spreadsheet spreadsheet) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20,"
                        + " \"lastColumn\": 10}");
    }

    @Test
    void aFileNameWithoutAnExtension_isGivenTheWorkbooksOwn()
            throws IOException {
        Spreadsheet spreadsheet = new Spreadsheet(workbook());

        File file = spreadsheet.write(folder.resolve("book").toString());

        assertEquals("book.xlsx", file.getName());
    }

    @Test
    void aWorkbookThatCannotBeWritten_isReportedToTheCaller() {
        IOException full = new IOException("no room left");
        XSSFWorkbook workbook = new XSSFWorkbook() {
            @Override
            protected void commit() throws IOException {
                throw full;
            }
        };
        workbook.createSheet();
        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        IOException reported = assertThrows(IOException.class,
                () -> spreadsheet.write(folder.resolve("book").toString()));
        assertEquals("no room left", reported.getMessage());
    }

    @Test
    void theWorkbookWritten_staysTheOneShown() throws IOException {
        XSSFWorkbook workbook = workbook();
        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        File file = spreadsheet.write(folder.resolve("book").toString());

        assertSame(workbook, spreadsheet.getWorkbook());
        try (Workbook written = WorkbookFactory.create(file)) {
            assertEquals("written", written.getSheet("Data").getRow(0)
                    .getCell(0).getStringCellValue());
        }
    }

    @Test
    void aPopupButtonSetOnTheSheet_staysAfterTheWorkbookIsWritten()
            throws IOException {
        Spreadsheet spreadsheet = new Spreadsheet(workbook());
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
        clientShowsRows(spreadsheet);
        PopupButton button = new PopupButton();
        spreadsheet.setPopup("A2", button);
        ui.fakeClientCommunication();

        spreadsheet.write(folder.resolve("book").toString());
        ui.fakeClientCommunication();
        clientShowsRows(spreadsheet);
        ui.fakeClientCommunication();

        assertEquals(spreadsheet.getElement(), button.getElement().getParent());
    }

    @Test
    void aCellMarkedAsAnInvalidFormula_staysMarkedAfterTheWorkbookIsWritten()
            throws IOException {
        Spreadsheet spreadsheet = new Spreadsheet(workbook());
        spreadsheet.markInvalidFormula(2, 3);

        spreadsheet.write(folder.resolve("book").toString());

        assertTrue(spreadsheet.isMarkedAsInvalidFormula(2, 3));
    }
}
