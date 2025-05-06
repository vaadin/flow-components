/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class CustomDataFormatterTest {
    Spreadsheet spreadsheet;
    Cell cell;

    @Before
    public void init() {
        var ui = new UI();
        ui.setLocale(Locale.US);
        UI.setCurrent(ui);

        spreadsheet = new Spreadsheet();
        cell = spreadsheet.createCell(1, 1, null);

        var cellStyle = spreadsheet.getWorkbook().createCellStyle();
        var dataFormat = spreadsheet.getWorkbook().createDataFormat();
        cell.setCellStyle(cellStyle);
        cellStyle.setDataFormat(dataFormat
                .getFormat("_(* #,##0_);_(* (#,##0);_(* \\\"-\\\"_);_(@_)"));
    }

    @Test
    public void cellWithPositiveNumber_getCellValue_formatIsCorrect() {
        cell.setCellValue(12345);
        Assert.assertEquals("12,345", spreadsheet.getCellValue(cell));
    }

    @Test
    public void cellWithNegativeNumber_getCellValue_formatIsCorrect() {
        cell.setCellValue(-12345);
        Assert.assertEquals("(12,345)", spreadsheet.getCellValue(cell));
    }

    @Test
    public void cellWithNumberZero_getCellValue_formatIsCorrect() {
        cell.setCellValue(0);
        Assert.assertEquals("\"-\"", spreadsheet.getCellValue(cell).trim());
    }

    @Test
    public void cellTextValue_getCellValue_sameTextIsReturned() {
        cell.setCellValue("text");
        Assert.assertEquals("text", spreadsheet.getCellValue(cell).trim());
    }
}
