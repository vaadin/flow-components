/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

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
        spreadsheet.deleteSheetWithPOIIndex(
                c.getSheet().getWorkbook().getSheetIndex("dontSee2"));
    }

}
