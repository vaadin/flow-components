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
    Cell fourPartDataFormatCell;
    Cell threePartDataFormatCell;
    Cell generalFormatCell;

    @Before
    public void init() {
        var ui = new UI();
        ui.setLocale(Locale.US);
        UI.setCurrent(ui);

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
    public void fourPartDataFormatCellWithPositiveNumber_getCellValue_formatIsCorrect() {
        fourPartDataFormatCell.setCellValue(12345);
        Assert.assertEquals("12,345",
                spreadsheet.getCellValue(fourPartDataFormatCell));
    }

    @Test
    public void fourPartDataFormatCellWithNegativeNumber_getCellValue_formatIsCorrect() {
        fourPartDataFormatCell.setCellValue(-12345);
        Assert.assertEquals("(12,345)",
                spreadsheet.getCellValue(fourPartDataFormatCell));
    }

    @Test
    public void fourPartDataFormatCellWithNumberZero_getCellValue_formatIsCorrect() {
        fourPartDataFormatCell.setCellValue(0);
        Assert.assertEquals("\"-\"",
                spreadsheet.getCellValue(fourPartDataFormatCell).trim());
    }

    @Test
    public void fourPartDataFormatCellTextValue_getCellValue_sameTextIsReturned() {
        fourPartDataFormatCell.setCellValue("text");
        Assert.assertEquals("text",
                spreadsheet.getCellValue(fourPartDataFormatCell).trim());
    }

    @Test
    public void threePartDataFormatCellWithPositiveNumber_getCellValue_formatIsCorrect() {
        threePartDataFormatCell.setCellValue(12345.6789);
        Assert.assertEquals("12,345.7",
                spreadsheet.getCellValue(threePartDataFormatCell));
    }

    @Test
    public void threePartDataFormatCellWithNegativeNumber_getCellValue_formatIsCorrect() {
        threePartDataFormatCell.setCellValue(-12345.6789);
        Assert.assertEquals("-12,345.7",
                spreadsheet.getCellValue(threePartDataFormatCell));
    }

    @Test
    public void threePartDataFormatCellWithNumberZero_getCellValue_formatIsCorrect() {
        threePartDataFormatCell.setCellValue(0);
        Assert.assertEquals("\"-\"",
                spreadsheet.getCellValue(threePartDataFormatCell).trim());
    }

    @Test
    public void threePartDataFormatCellTextValue_getCellValue_sameTextIsReturned() {
        threePartDataFormatCell.setCellValue("text");
        Assert.assertEquals("text",
                spreadsheet.getCellValue(threePartDataFormatCell).trim());
    }

    @Test
    public void generalFormatCellTextValue_getCellValue_textIsReturned() {
        generalFormatCell.setCellValue("text");
        Assert.assertEquals("text",
                spreadsheet.getCellValue(generalFormatCell).trim());
    }
}
