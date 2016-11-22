package com.vaadin.addon.spreadsheet.test.fixtures;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class InsertRowFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        spreadsheet.setMaxRows(spreadsheet.getRows() + 1);

        spreadsheet.shiftRows(spreadsheet.getSelectedCellReference()
                .getRow(), spreadsheet.getRows() - 1, 1);

        spreadsheet.refreshAllCellValues();
    }
}
