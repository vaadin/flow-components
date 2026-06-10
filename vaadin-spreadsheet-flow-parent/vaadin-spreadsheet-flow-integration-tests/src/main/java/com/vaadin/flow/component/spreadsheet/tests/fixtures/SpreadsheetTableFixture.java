/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
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
        int rows = 5; // 1 header row + 4 data rows
        int columns = 5;
        int firstRow = 1;
        int firstColumn = 1;

        // Label every cell with its position within the table, e.g. "Cell 0:0".
        // This keeps each value distinct and makes it obvious which row and
        // column a filter value belongs to.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                spreadsheet.createCell(firstRow + row, firstColumn + col,
                        "Cell " + row + ":" + col);
            }
        }

        CellRangeAddress range = new CellRangeAddress(firstRow,
                firstRow + rows - 1, firstColumn, firstColumn + columns - 1);
        SpreadsheetTable table = new SpreadsheetFilterTable(spreadsheet, range);
        spreadsheet.registerTable(table);
        spreadsheet.refreshAllCellValues();
    }
}
