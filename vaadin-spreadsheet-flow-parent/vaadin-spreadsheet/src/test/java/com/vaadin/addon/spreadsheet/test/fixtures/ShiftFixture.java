package com.vaadin.addon.spreadsheet.test.fixtures;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class ShiftFixture {

    public static class InsertRow implements SpreadsheetFixture {
        @Override
        public void loadFixture(Spreadsheet spreadsheet) {

            spreadsheet.setMaxRows(spreadsheet.getRows() + 1);

            spreadsheet.shiftRows(spreadsheet.getSelectedCellReference()
                    .getRow(), spreadsheet.getRows() - 1, 1);

            spreadsheet.refreshAllCellValues();
        }
    }

    public static class DeleteRow implements SpreadsheetFixture {
        @Override
        public void loadFixture(Spreadsheet spreadsheet) {

            spreadsheet.shiftRows(spreadsheet.getSelectedCellReference()
                    .getRow() + 1, spreadsheet.getRows() - 1, -1);

            spreadsheet.setMaxRows(spreadsheet.getRows() - 1);
            spreadsheet.refreshAllCellValues();
        }
    }
}
