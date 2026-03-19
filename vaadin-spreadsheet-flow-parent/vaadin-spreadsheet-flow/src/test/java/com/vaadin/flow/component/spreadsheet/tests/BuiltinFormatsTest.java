/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.time.LocalDateTime;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.tests.MockUIExtension;

class BuiltinFormatsTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    Cell cell;
    Spreadsheet spreadsheet;
    CellStyle cellStyle;
    DataFormat dataFormat;

    @BeforeEach
    void init() {
        setupWithLocale(Locale.US);
    }

    private void setupWithLocale(Locale locale) {
        ui.setLocale(locale);

        spreadsheet = new Spreadsheet();

        cellStyle = spreadsheet.getWorkbook().createCellStyle();
        dataFormat = spreadsheet.getWorkbook().createDataFormat();

        cell = spreadsheet.createCell(0, 0, null);
        cell.setCellStyle(cellStyle);
    }

    @Test
    void cellWithNumberValue_testNumberFormats() {
        cell.setCellValue(12345);

        cellStyle.setDataFormat(dataFormat.getFormat("# ##0"));
        Assertions.assertEquals("12 345", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("0"));
        Assertions.assertEquals("12345", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("0.00"));
        Assertions.assertEquals("12345.00", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345.67);
        cellStyle.setDataFormat(dataFormat.getFormat("0.00"));
        Assertions.assertEquals("12345.67", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0"));
        Assertions.assertEquals("12,345", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
        Assertions.assertEquals("12,345.00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("$#,##0_);($#,##0)"));
        Assertions.assertEquals("$12,345", spreadsheet.getCellValue(cell));

        cell.setCellValue(-12345);
        cellStyle.setDataFormat(dataFormat.getFormat("$#,##0_);($#,##0)"));
        Assertions.assertEquals("($12,345)", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle.setDataFormat(dataFormat.getFormat("$#,##0_);[Red]($#,##0)"));
        Assertions.assertEquals("$12,345", spreadsheet.getCellValue(cell));

        cell.setCellValue(-12345);
        cellStyle.setDataFormat(dataFormat.getFormat("$#,##0_);[Red]($#,##0)"));
        Assertions.assertEquals("($12,345)", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle
                .setDataFormat(dataFormat.getFormat("$#,##0.00_);($#,##0.00)"));
        Assertions.assertEquals("$12,345.00", spreadsheet.getCellValue(cell));

        cell.setCellValue(-12345);
        cellStyle
                .setDataFormat(dataFormat.getFormat("$#,##0.00_);($#,##0.00)"));
        Assertions.assertEquals("($12,345.00)", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle.setDataFormat(
                dataFormat.getFormat("$#,##0.00_);[Red]($#,##0.00)"));
        Assertions.assertEquals("$12,345.00", spreadsheet.getCellValue(cell));

        cell.setCellValue(-12345);
        cellStyle.setDataFormat(
                dataFormat.getFormat("$#,##0.00_);[Red]($#,##0.00)"));
        Assertions.assertEquals("($12,345.00)", spreadsheet.getCellValue(cell));

        cell.setCellValue(.7525);
        cellStyle.setDataFormat(dataFormat.getFormat("0%"));
        Assertions.assertEquals("75%", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("0.00%"));
        Assertions.assertEquals("75.25%", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle.setDataFormat(dataFormat.getFormat("0.00E+00"));
        Assertions.assertEquals("1.23E+04", spreadsheet.getCellValue(cell));
    }

    @Test
    void cellWithDateValue_testDateFormats() {
        cell.setCellValue(LocalDateTime.of(2022, 10, 31, 12, 0));

        cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy"));
        Assertions.assertEquals("10/31/22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("d-mmm-yy"));
        Assertions.assertEquals("31-Oct-22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("d-mmm"));
        Assertions.assertEquals("31-Oct", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("mmm-yy"));
        Assertions.assertEquals("Oct-22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm AM/PM"));
        Assertions.assertEquals("12:00 PM", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm:ss AM/PM"));
        Assertions.assertEquals("12:00:00 PM", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm"));
        Assertions.assertEquals("12:00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm:ss"));
        Assertions.assertEquals("12:00:00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy h:mm"));
        Assertions.assertEquals("10/31/22 12:00",
                spreadsheet.getCellValue(cell));
    }

    @Test
    void cellWithDateValue_withGermanLocale_testDateFormats() {
        setupWithLocale(Locale.GERMAN);

        cell.setCellValue(LocalDateTime.of(2022, 10, 31, 12, 0));

        cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy"));
        Assertions.assertEquals("10/31/22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("d-mmm-yy"));
        Assertions.assertEquals("31-Okt.-22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("d-mmm"));
        Assertions.assertEquals("31-Okt.", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("mmm-yy"));
        Assertions.assertEquals("Okt.-22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm AM/PM"));
        Assertions.assertEquals("12:00 PM", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm:ss AM/PM"));
        Assertions.assertEquals("12:00:00 PM", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm"));
        Assertions.assertEquals("12:00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm:ss"));
        Assertions.assertEquals("12:00:00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy h:mm"));
        Assertions.assertEquals("10/31/22 12:00",
                spreadsheet.getCellValue(cell));
    }
}
