package com.vaadin.addon.spreadsheet.test.fixtures;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.test.demoapps.TestexcelsheetUI;

public class CellMergeFixture extends UIFixture {

    public CellMergeFixture(TestexcelsheetUI ui) {
        super(ui);
    }

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        if (ui.currentSelection == null || ui.currentSelection.isEmpty()) {
            return;
        }

        int firstRow = Integer.MAX_VALUE;
        int lastRow = 0;
        int firstCol = Integer.MAX_VALUE;
        int lastCol = 0;

        for (CellReference cellRef : ui.currentSelection) {
            if (cellRef.getRow() < firstRow) {
                firstRow = cellRef.getRow();
            }
            if (cellRef.getRow() > lastRow) {
                lastRow = cellRef.getRow();
            }
            if (cellRef.getCol() < firstCol) {
                firstCol = cellRef.getCol();
            }
            if (cellRef.getCol() > lastCol) {
                lastCol = cellRef.getCol();
            }
        }

        CellRangeAddress cra = new CellRangeAddress(firstRow, lastRow,
                firstCol, lastCol);
        spreadsheet.addMergedRegion(cra);
    }
}
