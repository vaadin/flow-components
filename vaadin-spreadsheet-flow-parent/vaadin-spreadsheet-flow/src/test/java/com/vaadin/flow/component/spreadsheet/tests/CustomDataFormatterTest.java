/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.tests.MockUIExtension;

class CustomDataFormatterTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    Spreadsheet spreadsheet;
    Cell fourPartDataFormatCell;
    Cell threePartDataFormatCell;
    Cell generalFormatCell;

    @BeforeEach
    void init() {
        ui.setLocale(Locale.US);

        spreadsheet = new Spreadsheet();
        this.fourPartDataFormatCell = createFourPartDataFormatCell(spreadsheet);
        this.threePartDataFormatCell = createThreePartDataFormatCell(
                spreadsheet);
        this.generalFormatCell = createGeneralFormatCell(spreadsheet);
    }

    private Cell createFourPartDataFormatCell(Spreadsheet spreadsheet) {
        var cell = spreadsheet.createCell(1, 1, null);
        var cellStyle = spreadsheet.getWorkbook().createCellStyle();
        var dataFormat = spreadsheet.getWorkbook().createDataFormat();
        cell.setCellStyle(cellStyle);
        cellStyle.setDataFormat(dataFormat
                .getFormat("_(* #,##0_);_(* (#,##0);_(* \\\"-\\\"_);_(@_)"));
        return cell;
    }

    private Cell createThreePartDataFormatCell(Spreadsheet spreadsheet) {
        var cell = spreadsheet.createCell(2, 1, null);
        var cellStyle = spreadsheet.getWorkbook().createCellStyle();
        var dataFormat = spreadsheet.getWorkbook().createDataFormat();
        cell.setCellStyle(cellStyle);
        cellStyle.setDataFormat(
                dataFormat.getFormat("#,###.#;-#,###.#;\\\"-\\\""));
        return cell;
    }

    private Cell createGeneralFormatCell(Spreadsheet spreadsheet) {
        var cell = spreadsheet.createCell(3, 1, null);
        var cellStyle = spreadsheet.getWorkbook().createCellStyle();
        cell.setCellStyle(cellStyle);
        return cell;
    }

    @Test
    void fourPartDataFormatCellWithPositiveNumber_getCellValue_formatIsCorrect() {
        fourPartDataFormatCell.setCellValue(12345);
        Assertions.assertEquals("12,345",
                spreadsheet.getCellValue(fourPartDataFormatCell));
    }

    @Test
    void fourPartDataFormatCellWithNegativeNumber_getCellValue_formatIsCorrect() {
        fourPartDataFormatCell.setCellValue(-12345);
        Assertions.assertEquals("(12,345)",
                spreadsheet.getCellValue(fourPartDataFormatCell));
    }

    @Test
    void fourPartDataFormatCellWithNumberZero_getCellValue_formatIsCorrect() {
        fourPartDataFormatCell.setCellValue(0);
        Assertions.assertEquals("\"-\"",
                spreadsheet.getCellValue(fourPartDataFormatCell).trim());
    }

    @Test
    void fourPartDataFormatCellTextValue_getCellValue_sameTextIsReturned() {
        fourPartDataFormatCell.setCellValue("text");
        Assertions.assertEquals("text",
                spreadsheet.getCellValue(fourPartDataFormatCell).trim());
    }

    @Test
    void threePartDataFormatCellWithPositiveNumber_getCellValue_formatIsCorrect() {
        threePartDataFormatCell.setCellValue(12345.6789);
        Assertions.assertEquals("12,345.7",
                spreadsheet.getCellValue(threePartDataFormatCell));
    }

    @Test
    void threePartDataFormatCellWithNegativeNumber_getCellValue_formatIsCorrect() {
        threePartDataFormatCell.setCellValue(-12345.6789);
        Assertions.assertEquals("-12,345.7",
                spreadsheet.getCellValue(threePartDataFormatCell));
    }

    @Test
    void threePartDataFormatCellWithNumberZero_getCellValue_formatIsCorrect() {
        threePartDataFormatCell.setCellValue(0);
        Assertions.assertEquals("\"-\"",
                spreadsheet.getCellValue(threePartDataFormatCell).trim());
    }

    @Test
    void threePartDataFormatCellTextValue_getCellValue_sameTextIsReturned() {
        threePartDataFormatCell.setCellValue("text");
        Assertions.assertEquals("text",
                spreadsheet.getCellValue(threePartDataFormatCell).trim());
    }

    @Test
    void generalFormatCellTextValue_getCellValue_textIsReturned() {
        generalFormatCell.setCellValue("text");
        Assertions.assertEquals("text",
                spreadsheet.getCellValue(generalFormatCell).trim());
    }

    // The section a value picks decides how it is formatted: an empty section
    // shows nothing, and a section of only text shows that text.

    @Test
    void zeroInAFormatWithAnEmptyZeroSection_showsNothing() {
        Assertions.assertEquals("", formatted(0, "0;-0;;@"));
        Assertions.assertEquals("", formatted(0, "0;-0;"));
    }

    @Test
    void zeroInAFormatWithATextZeroSection_showsTheText() {
        Assertions.assertEquals("zero", formatted(0, "0;-0;\"zero\";@"));
        Assertions.assertEquals("zero", formatted(0, "0;-0;\"zero\""));
    }

    @Test
    void negativeInAFormatWithAnEmptyNegativeSection_showsNothing() {
        Assertions.assertEquals("", formatted(-5, "0;;0;@"));
        Assertions.assertEquals("", formatted(-5, "0;;0"));
        Assertions.assertEquals("", formatted(-5, "0;"));
    }

    @Test
    void negativeInAFormatWithATextNegativeSection_showsTheText() {
        Assertions.assertEquals("neg", formatted(-5, "0;\"neg\";0;@"));
        Assertions.assertEquals("neg", formatted(-5, "0;\"neg\";0"));
        Assertions.assertEquals("neg", formatted(-5, "0;\"neg\""));
    }

    @Test
    void positiveInAFormatWithATextPositiveSection_showsTheText() {
        Assertions.assertEquals("pos", formatted(5, "\"pos\";-0;0;@"));
        Assertions.assertEquals("pos", formatted(5, "\"pos\";-0"));
    }

    @Test
    void aFormatOfOnlyText_showsTheText_withAMinusForANegative() {
        Assertions.assertEquals("text", formatted(5, "\"text\""));
        Assertions.assertEquals("-text", formatted(-5, "\"text\""));
    }

    private String formatted(double value, String format) {
        var cell = spreadsheet.createCell(5, 1, value);
        var cellStyle = spreadsheet.getWorkbook().createCellStyle();
        cellStyle.setDataFormat(
                spreadsheet.getWorkbook().createDataFormat().getFormat(format));
        cell.setCellStyle(cellStyle);
        return spreadsheet.getCellValue(cell);
    }
}
