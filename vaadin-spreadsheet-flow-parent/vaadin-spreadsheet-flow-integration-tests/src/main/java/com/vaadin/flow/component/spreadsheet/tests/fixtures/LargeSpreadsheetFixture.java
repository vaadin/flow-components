package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class LargeSpreadsheetFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.setMaxRows(1000);
        for (int r = 0; r < 1000; r++) {
            for (int c = 0; c < 52; c++) {
                spreadsheet.createCell(r, c, String.format("(%d, %d)", r, c));
            }
        }
        spreadsheet.reload();
    }

}
