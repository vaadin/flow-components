package com.vaadin.addon.spreadsheet.test.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.test.demoapps.TestexcelsheetUI;

public class ColumnToggleFixture extends UIFixture {

    public ColumnToggleFixture(TestexcelsheetUI ui) {
        super(ui);
    }

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        List<Integer> columnIndexes = new ArrayList<Integer>();

        for (CellReference cellRef : ui.currentSelection) {
            if (!columnIndexes.contains((int) cellRef.getCol())) {
                columnIndexes.add((int) cellRef.getCol());
            }
        }

        for (Integer col : columnIndexes) {
            spreadsheet.setColumnHidden(col, !spreadsheet.isColumnHidden(col));
        }

        spreadsheet.refreshAllCellValues();
    }
}
