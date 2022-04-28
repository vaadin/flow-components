package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.Workbook;

public class RenameFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.getCell(0, 0);
        Workbook wb = spreadsheet.createCell(0, 0, "").getSheet().getWorkbook();
        wb.getSheet("new sheet name");
        spreadsheet.setSheetName(wb.getSheetIndex("new sheet name"),
                "new_sheet_REnamed");
    }
}
