package com.vaadin.addon.spreadsheet.demo.action;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.SpreadsheetAction;

public class DeleteRowAction extends SpreadsheetAction {

    public DeleteRowAction() {
        super("");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        return false;
    }

    @Override
    public boolean isApplicableForHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        if (!isSheetProtected(spreadsheet)) {
            if (headerRange.isFullRowRange()) {
                setCaption("Delete row " + (headerRange.getFirstRow() + 1));
                return true;
            }
        }
        return false;
    }

    @Override
    public void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        // TODO throw error
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        int rows = spreadsheet.getRows();
        int deletedRowIndex = headerRange.getFirstRow();
        if (deletedRowIndex + 1 > rows - 1) {
            // if removed last row, just delete it and make sheet smaller
            spreadsheet.removeRows(deletedRowIndex, deletedRowIndex);
        } else {
            spreadsheet.shiftRows(deletedRowIndex + 1, (rows - 1), -1, true,
                    true);
        }
        spreadsheet.setMaximumRows(rows - 1);
    }

}
