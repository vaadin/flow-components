package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class InsertRowFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        spreadsheet.setMaxRows(spreadsheet.getRows() + 1);

        spreadsheet.shiftRows(spreadsheet.getSelectedCellReference().getRow(),
                spreadsheet.getRows() - 1, 1);

        spreadsheet.refreshAllCellValues();
    }
}
