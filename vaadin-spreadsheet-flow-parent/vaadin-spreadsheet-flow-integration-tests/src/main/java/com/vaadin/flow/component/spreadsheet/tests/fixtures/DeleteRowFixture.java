package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class DeleteRowFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        spreadsheet.shiftRows(
                spreadsheet.getSelectedCellReference().getRow() + 1,
                spreadsheet.getRows() - 1, -1);

        spreadsheet.setMaxRows(spreadsheet.getRows() - 1);
        spreadsheet.refreshAllCellValues();
    }
}