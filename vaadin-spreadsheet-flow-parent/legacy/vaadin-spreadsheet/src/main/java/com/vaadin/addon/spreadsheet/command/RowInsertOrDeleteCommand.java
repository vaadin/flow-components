package com.vaadin.addon.spreadsheet.command;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.addon.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import java.util.*;

/**
 * Command to insert or delete a row
 */
public class RowInsertOrDeleteCommand extends SpreadsheetCommand  {

    private final int row;
    private boolean wasDeleted = false;
    private RowData rowData;

    public RowInsertOrDeleteCommand(Spreadsheet spreadsheet, CellRangeAddress headerRange) {
        super(spreadsheet);
        row = headerRange.getFirstRow();
        rowData = new RowData(spreadsheet);
    }

    @Override
    public void execute() {
        if(wasDeleted) {
            insertNewRow();
        } else {
            deleteRow();
        }
    }

    @Override
    public CellReference getSelectedCellReference() {
        return new CellReference(row, 0);
    }

    @Override
    public CellRangeAddress getPaintedCellRange() {
        return new CellRangeAddress(row, row, 0, spreadsheet.getLastColumn());
    }

    public void insertNewRow() {
        wasDeleted = false;
        int rows = spreadsheet.getRows();
        spreadsheet.shiftRows(row, (rows - 1), 1, true,
                true);
        spreadsheet.setMaxRows(rows + 1);
        restoreOldCellValues();
        spreadsheet.refreshAllCellValues();
    }

    public void deleteRow() {
        wasDeleted = true;
        int rows = spreadsheet.getRows();
        captureOldValues();
        if (row + 1 > rows - 1) {
            // if removed last row, just delete it and make sheet smaller
            spreadsheet.deleteRows(row, row);
        } else {
            spreadsheet.shiftRows(row + 1, (rows - 1), -1, true,
                    true);
        }
        spreadsheet.setMaxRows(rows - 1);
    }

    private void captureOldValues() {
        rowData.copy(this.row);
    }

    private void restoreOldCellValues() {
        if (rowData.isCopied()) {
            Row row = getSheet().getRow(this.row);
            if (row == null) {
                row = getSheet().createRow(this.row);
            }
            rowData.writeTo(row);

            if (!spreadsheet.isRerenderPending()) {
                ArrayList<Cell> modifiedCells = new ArrayList<Cell>();
                for (Cell cell : row) {
                    if (cell != null) {
                        modifiedCells.add(cell);
                    }
                }
                spreadsheet.refreshCells(modifiedCells);
            }
        }
    }


}
