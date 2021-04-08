package com.example.application.views.demoUI.fixtures;

import org.apache.poi.ss.usermodel.Cell;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;


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

}
