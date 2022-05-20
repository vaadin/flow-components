package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.util.CellReference;

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
