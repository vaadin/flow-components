/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class LockCellFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        spreadsheet.setActiveSheetProtected("pwd");
        Cell cell = spreadsheet.createCell(0, 0, "");
        Workbook wb = cell.getSheet().getWorkbook();

        List<Cell> updatedCells = new ArrayList<Cell>();
        for (CellReference cellRef : spreadsheet.getSelectedCellReferences()) {
            cell = spreadsheet.getCell(cellRef.getRow(), cellRef.getCol());
            if (cell == null) {
                cell = spreadsheet.createCell(cellRef.getRow(),
                        cellRef.getCol(), "");
            }

            boolean wasLocked = spreadsheet.isCellLocked(cell.getAddress());

            CellStyle cellStyle = wb.createCellStyle();
            // Toggle cell locked state
            cellStyle.setLocked(!wasLocked);
            cell.setCellValue(cellStyle.getLocked() ? "locked" : "unlocked");
            cell.setCellStyle(cellStyle);
            updatedCells.add(cell);
        }

        spreadsheet.refreshCells(updatedCells);
    }
}
