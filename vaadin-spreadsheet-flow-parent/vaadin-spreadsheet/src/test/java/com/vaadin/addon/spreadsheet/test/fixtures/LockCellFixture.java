package com.vaadin.addon.spreadsheet.test.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.test.demoapps.TestexcelsheetUI;

public class LockCellFixture extends UIFixture {

    public LockCellFixture(TestexcelsheetUI ui) {
        super(ui);
    }

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        spreadsheet.setActiveSheetProtected("pwd");
        Cell cell = spreadsheet.createCell(0, 0, "");
        Workbook wb = cell.getSheet().getWorkbook();

        List<Cell> updatedCells = new ArrayList<Cell>();
        for (CellReference cellRef : ui.currentSelection) {
            cell = spreadsheet.getCell(cellRef.getRow(), cellRef.getCol());
            if (cell == null) {
                cell = spreadsheet.createCell(cellRef.getRow(),
                        cellRef.getCol(), "unlocked");
            }
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setLocked(false);
            cell.setCellStyle(cellStyle);
            updatedCells.add(cell);
        }

        spreadsheet.refreshCells(updatedCells);
    }
}
