package com.vaadin.addon.spreadsheet.test.fixtures;

import java.util.Set;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class CellMergeFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        Set<CellReference> seletecCells =spreadsheet.getSelectedCellReferences();
        if (seletecCells == null || seletecCells.isEmpty()) {
            return;
        }

        int firstRow = Integer.MAX_VALUE;
        int lastRow = 0;
        int firstCol = Integer.MAX_VALUE;
        int lastCol = 0;

        for (CellReference cellRef : seletecCells) {
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
