package com.vaadin.addon.spreadsheet.test.fixtures;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class SheetsFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.createNewSheet("newSheet1", 20, 20);
        spreadsheet.createNewSheet("dontSee", 20, 20);
        spreadsheet.createNewSheet("newSheet2", 20, 20);
        spreadsheet.createNewSheet("dontSee2", 20, 20);

        spreadsheet.deleteSheet(2);
        Cell c = spreadsheet.createCell(0, 0, "");
        spreadsheet.deleteSheetWithPOIIndex(c.getSheet().getWorkbook()
                .getSheetIndex("dontSee2"));
    }

    public static class Rename1 implements SpreadsheetFixture {
        @Override
        public void loadFixture(Spreadsheet spreadsheet) {
            spreadsheet.getCell(0, 0);
            Workbook wb = spreadsheet.createCell(0, 0, "").getSheet()
                    .getWorkbook();
            wb.getSheet("new sheet name");
            spreadsheet.setSheetName(wb.getSheetIndex("new sheet name"),
                    "new_sheet_REnamed");
        }
    }
}
