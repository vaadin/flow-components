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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Writing the workbook to a file: the file written is read back, and a failure
 * to write it is the caller's to see.
 */
class SpreadsheetWriteTest {

    @TempDir
    Path folder;

    @Test
    void theWorkbookWritten_isReadBack() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Data").createRow(0).createCell(0)
                .setCellValue("written");
        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        File file = spreadsheet.write(folder.resolve("book").toString());

        assertEquals("book.xlsx", file.getName());
        assertEquals("written", spreadsheet.getWorkbook().getSheet("Data")
                .getRow(0).getCell(0).getStringCellValue());
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
}
