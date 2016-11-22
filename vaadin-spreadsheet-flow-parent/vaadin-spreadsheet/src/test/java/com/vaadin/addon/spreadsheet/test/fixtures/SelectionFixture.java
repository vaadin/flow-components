package com.vaadin.addon.spreadsheet.test.fixtures;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class SelectionFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        for (CellReference cellRef : spreadsheet.getSelectedCellReferences()) {
            spreadsheet.createCell(cellRef.getRow(), cellRef.getCol(),
                    "SELECTED");
        }
        spreadsheet.refreshAllCellValues();
    }
}
