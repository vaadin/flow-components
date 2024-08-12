/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.flow.component.spreadsheet.SpreadsheetTable;

public class SpreadsheetTableFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        int maxColumns = 5;
        int maxRows = 5;

        for (int column = 1; column < maxColumns + 1; column++) {
            spreadsheet.createCell(1, column, "Column " + column);
        }

        for (int row = 2; row < maxRows + 2; row++) {
            for (int col = 1; col < maxColumns + 1; col++) {
                spreadsheet.createCell(row, col, row + col);
            }
        }
        CellRangeAddress range = new CellRangeAddress(1, maxRows, 1,
                maxColumns);
        SpreadsheetTable table = new SpreadsheetFilterTable(spreadsheet, range);
        spreadsheet.registerTable(table);
        spreadsheet.refreshAllCellValues();
    }
}
