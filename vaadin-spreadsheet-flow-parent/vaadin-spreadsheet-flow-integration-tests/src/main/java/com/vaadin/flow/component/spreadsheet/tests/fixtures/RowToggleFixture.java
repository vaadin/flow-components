package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.List;

public class RowToggleFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        List<Integer> rowIndexes = new ArrayList<Integer>();

        for (CellReference cellRef : spreadsheet.getSelectedCellReferences()) {
            if (!rowIndexes.contains(cellRef.getRow())) {
                rowIndexes.add(cellRef.getRow());
            }
        }

        for (Integer row : rowIndexes) {
            spreadsheet.setRowHidden(row, !spreadsheet.isRowHidden(row));
        }

        spreadsheet.refreshAllCellValues();
    }
}
