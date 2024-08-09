/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import org.apache.poi.ss.usermodel.Workbook;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

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
