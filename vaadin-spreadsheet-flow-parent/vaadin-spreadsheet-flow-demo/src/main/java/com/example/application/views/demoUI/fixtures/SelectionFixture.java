package com.example.application.views.demoUI.fixtures;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

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
