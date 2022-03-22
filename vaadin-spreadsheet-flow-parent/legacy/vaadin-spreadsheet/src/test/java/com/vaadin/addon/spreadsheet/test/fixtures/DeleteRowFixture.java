package com.vaadin.addon.spreadsheet.test.fixtures;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class DeleteRowFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        spreadsheet.shiftRows(spreadsheet.getSelectedCellReference()
                .getRow() + 1, spreadsheet.getRows() - 1, -1);

        spreadsheet.setMaxRows(spreadsheet.getRows() - 1);
        spreadsheet.refreshAllCellValues();
    }
}