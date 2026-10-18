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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.tests.MockUIExtension;

/**
 * A cell's value as entered goes to the client only where it differs from the
 * value the cell shows, and never for a formula, which the client edits by its
 * formula: sent for every cell, it doubled what a sheet of text sent. The
 * client falls back to the value shown.
 */
class CellOriginalValueTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final Sheet sheet = workbook.createSheet();
    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setLocale(Locale.US);
        ui.add(spreadsheet);
    }

    private Cell cell(int column) {
        return sheet.createRow(0).createCell(column);
    }

    private Cell formatted(Cell cell, String format) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        cell.setCellStyle(style);
        return cell;
    }

    private CellData sent(Cell cell) {
        return spreadsheet.getCellValueManager().createCellDataForCell(cell);
    }

    @Test
    void text_goesAsItIsShown_only() {
        Cell cell = cell(0);
        cell.setCellValue("abc");

        assertEquals("abc", sent(cell).value);
        assertNull(sent(cell).originalValue);
    }

    @Test
    void aNumberShownAsItIs_goesAsItIsShown_only() {
        Cell cell = cell(0);
        cell.setCellValue(42);

        assertEquals("42", sent(cell).value);
        assertNull(sent(cell).originalValue);
    }

    @Test
    void aNumberShownRounded_goesWithItsFullValue() {
        Cell cell = formatted(cell(0), "0.00");
        cell.setCellValue(1.2345);

        assertEquals("1.23", sent(cell).value);
        assertEquals("1.2345", sent(cell).originalValue);
    }

    @Test
    void textWithALeadingApostrophe_goesWithIt() {
        Cell cell = cell(0);
        cell.setCellValue("abc");
        CellStyle quoted = workbook.createCellStyle();
        quoted.setQuotePrefixed(true);
        cell.setCellStyle(quoted);

        assertEquals("abc", sent(cell).value);
        assertEquals("'abc", sent(cell).originalValue);
    }

    @Test
    void aFormula_goesWithItsFormula_only() {
        cell(1).setCellValue(21);
        Cell cell = sheet.getRow(0).createCell(0);
        cell.setCellFormula("B1*2");

        CellData sent = sent(cell);

        assertEquals("B1*2", sent.formulaValue);
        assertEquals("42", sent.value);
        assertNull(sent.originalValue);
    }

    @Test
    void aZeroShownAsNothing_stillGoesWithTheZero() {
        sheet.setDisplayZeros(false);
        Cell cell = cell(0);
        cell.setCellValue(0);

        assertEquals("", sent(cell).value);
        assertEquals("0", sent(cell).originalValue);
    }

    @Test
    void aDate_goesAsItIsShown_only() {
        Cell cell = formatted(cell(0), "m/d/yy");
        cell.setCellValue(45000);

        assertNull(sent(cell).originalValue);
    }
}
