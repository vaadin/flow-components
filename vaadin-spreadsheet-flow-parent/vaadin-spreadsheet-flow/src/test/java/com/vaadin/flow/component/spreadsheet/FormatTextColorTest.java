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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * The text colour a number format such as {@code [Red]} gives its cell, which
 * the client draws the value in.
 */
class FormatTextColorTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final Sheet sheet = workbook.createSheet();
    private Spreadsheet spreadsheet;

    private Cell cell(int row, double value, String format) {
        Cell cell = sheet.createRow(row).createCell(0);
        cell.setCellValue(value);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        cell.setCellStyle(style);
        return cell;
    }

    private String textColorOf(Cell cell) {
        return spreadsheet.getCellValueManager()
                .createCellDataForCell(cell).textColor;
    }

    @Test
    void aFormatWithAColour_givesTheValuesItFormatsThatColour() {
        Cell red = cell(0, 42, "[Red]0.00");
        Cell redNegative = cell(1, -42, "0.00;[Red]-0.00");
        Cell positive = cell(2, 42, "0.00;[Red]-0.00");
        Cell plain = cell(3, 42, "0.00");
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setLocale(Locale.US);
        ui.add(spreadsheet);

        assertEquals("ff0000", textColorOf(red));
        assertEquals("ff0000", textColorOf(redNegative));
        assertNull(textColorOf(positive));
        assertNull(textColorOf(plain));
    }
}
