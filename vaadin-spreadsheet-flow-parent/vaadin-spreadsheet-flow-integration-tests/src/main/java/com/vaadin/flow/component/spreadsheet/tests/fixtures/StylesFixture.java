package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetStyleFactory;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class StylesFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        Cell c;
        CellStyle cellStyle;
        Font font;
        SpreadsheetStyleFactory sssf = spreadsheet.getSpreadsheetStyleFactory();

        c = spreadsheet.createCell(0, 0, "Styles");
        Workbook wb = c.getSheet().getWorkbook();

        c = spreadsheet.createCell(1, 0, "hcenter");
        cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        c = spreadsheet.createCell(1, 1, "right align");
        cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        c = spreadsheet.createCell(2, 0, "blue bottom");
        cellStyle = wb.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THICK);
        cellStyle.setBottomBorderColor(IndexedColors.BLUE.getIndex());
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        c = spreadsheet.createCell(2, 1, "back green");
        cellStyle = wb.createCellStyle();
        cellStyle.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        c = spreadsheet.createCell(3, 0, "red text");
        font = wb.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        c = spreadsheet.createCell(3, 1, "bold text");
        font = wb.createFont();
        font.setBold(true);
        cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        c = spreadsheet.createCell(3, 2, "italic");
        font = wb.createFont();
        font.setItalic(true);
        cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        int column = 0;
        for (short size : new Short[] { 8, 10, 12, 14 }) {

            c = spreadsheet.createCell(4, column, "Size " + size);
            font = wb.createFont();
            font.setFontHeightInPoints(size);
            cellStyle = wb.createCellStyle();
            cellStyle.setFont(font);
            c.setCellStyle(cellStyle);
            sssf.cellStyleUpdated(c, true);
            column++;
        }

        c = spreadsheet.createCell(5, 1,
                "default aligned should overflow by default to other cells");
        c = spreadsheet.createCell(6, 1,
                "right aligned that should overflow by default to other cells");
        cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);
        c = spreadsheet.createCell(7, 1,
                "center aligned text that should overflow by default to other cells");
        cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        c = spreadsheet.createCell(8, 1,
                "wrapping long text will wrap to multiple lines");
        cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(true);
        c.setCellStyle(cellStyle);
        sssf.cellStyleUpdated(c, true);

        spreadsheet.refreshAllCellValues();
    }
}