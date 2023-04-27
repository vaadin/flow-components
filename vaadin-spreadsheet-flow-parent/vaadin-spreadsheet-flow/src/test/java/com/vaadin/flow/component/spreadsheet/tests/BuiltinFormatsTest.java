package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Locale;

public class BuiltinFormatsTest {

    Cell cell;
    Spreadsheet spreadsheet;
    CellStyle cellStyle;
    DataFormat dataFormat;

    @Before
    public void init() {
        setupWithLocale(Locale.US);
    }

    private void setupWithLocale(Locale locale) {
        var ui = new UI();
        ui.setLocale(locale);
        UI.setCurrent(ui);

        spreadsheet = new Spreadsheet();

        cellStyle = spreadsheet.getWorkbook().createCellStyle();
        dataFormat = spreadsheet.getWorkbook().createDataFormat();

        cell = spreadsheet.createCell(0, 0, null);
        cell.setCellStyle(cellStyle);
    }

    @Test
    public void cellWithNumberValue_testNumberFormats() {
        cell.setCellValue(12345);

        cellStyle.setDataFormat(dataFormat.getFormat("# ##0"));
        Assert.assertEquals("12 345", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("0"));
        Assert.assertEquals("12345", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("0.00"));
        Assert.assertEquals("12345.00", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345.67);
        cellStyle.setDataFormat(dataFormat.getFormat("0.00"));
        Assert.assertEquals("12345.67", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0"));
        Assert.assertEquals("12,345", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
        Assert.assertEquals("12,345.00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("$#,##0_);($#,##0)"));
        Assert.assertEquals("$12,345", spreadsheet.getCellValue(cell));

        cell.setCellValue(-12345);
        cellStyle.setDataFormat(dataFormat.getFormat("$#,##0_);($#,##0)"));
        Assert.assertEquals("($12,345)", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle.setDataFormat(dataFormat.getFormat("$#,##0_);[Red]($#,##0)"));
        Assert.assertEquals("$12,345", spreadsheet.getCellValue(cell));

        cell.setCellValue(-12345);
        cellStyle.setDataFormat(dataFormat.getFormat("$#,##0_);[Red]($#,##0)"));
        Assert.assertEquals("($12,345)", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle
                .setDataFormat(dataFormat.getFormat("$#,##0.00_);($#,##0.00)"));
        Assert.assertEquals("$12,345.00", spreadsheet.getCellValue(cell));

        cell.setCellValue(-12345);
        cellStyle
                .setDataFormat(dataFormat.getFormat("$#,##0.00_);($#,##0.00)"));
        Assert.assertEquals("($12,345.00)", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle.setDataFormat(
                dataFormat.getFormat("$#,##0.00_);[Red]($#,##0.00)"));
        Assert.assertEquals("$12,345.00", spreadsheet.getCellValue(cell));

        cell.setCellValue(-12345);
        cellStyle.setDataFormat(
                dataFormat.getFormat("$#,##0.00_);[Red]($#,##0.00)"));
        Assert.assertEquals("($12,345.00)", spreadsheet.getCellValue(cell));

        cell.setCellValue(.7525);
        cellStyle.setDataFormat(dataFormat.getFormat("0%"));
        Assert.assertEquals("75%", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("0.00%"));
        Assert.assertEquals("75.25%", spreadsheet.getCellValue(cell));

        cell.setCellValue(12345);
        cellStyle.setDataFormat(dataFormat.getFormat("0.00E+00"));
        Assert.assertEquals("1.23E+04", spreadsheet.getCellValue(cell));
    }

    @Test
    public void cellWithDateValue_testDateFormats() {
        cell.setCellValue(LocalDateTime.of(2022, 10, 31, 12, 0));

        cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy"));
        Assert.assertEquals("10/31/22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("d-mmm-yy"));
        Assert.assertEquals("31-Oct-22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("d-mmm"));
        Assert.assertEquals("31-Oct", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("mmm-yy"));
        Assert.assertEquals("Oct-22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm AM/PM"));
        Assert.assertEquals("12:00 PM", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm:ss AM/PM"));
        Assert.assertEquals("12:00:00 PM", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm"));
        Assert.assertEquals("12:00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm:ss"));
        Assert.assertEquals("12:00:00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy h:mm"));
        Assert.assertEquals("10/31/22 12:00", spreadsheet.getCellValue(cell));
    }

    @Test
    public void cellWithDateValue_withGermanLocale_testDateFormats() {
        setupWithLocale(Locale.GERMAN);

        cell.setCellValue(LocalDateTime.of(2022, 10, 31, 12, 0));

        cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy"));
        Assert.assertEquals("10/31/22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("d-mmm-yy"));
        Assert.assertEquals("31-Okt.-22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("d-mmm"));
        Assert.assertEquals("31-Okt.", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("mmm-yy"));
        Assert.assertEquals("Okt.-22", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm AM/PM"));
        Assert.assertEquals("12:00 PM", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm:ss AM/PM"));
        Assert.assertEquals("12:00:00 PM", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm"));
        Assert.assertEquals("12:00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("h:mm:ss"));
        Assert.assertEquals("12:00:00", spreadsheet.getCellValue(cell));

        cellStyle.setDataFormat(dataFormat.getFormat("m/d/yy h:mm"));
        Assert.assertEquals("10/31/22 12:00", spreadsheet.getCellValue(cell));
    }
}
