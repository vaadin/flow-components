package com.vaadin.addon.spreadsheet.demo.action;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.SpreadsheetAction;

public class HideHeaderAction extends SpreadsheetAction {

    public HideHeaderAction() {
        super("");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        return false;
    }

    @Override
    public boolean isApplicableForHeader(Spreadsheet spreadhseet,
            CellRangeAddress headerRange) {
        Sheet activeSheet = spreadhseet.getActiveSheet();
        if (!isSheetProtected(activeSheet)) {
            if (headerRange.isFullColumnRange()) {
                int index = headerRange.getFirstColumn();
                if (!activeSheet.isColumnHidden(index)) {
                    setCaption("Hide column " + (getColumnHeader(index + 1)));
                }
                return true;
            } else if (headerRange.isFullRowRange()) {
                int index = headerRange.getFirstRow();
                if (!spreadhseet.isRowHidden(index)) {
                    setCaption("Hide row " + (index + 1));
                }
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
        if (headerRange.isFullColumnRange()) {
            spreadsheet.setColumnHidden(headerRange.getFirstColumn(), true);
        } else if (headerRange.isFullRowRange()) {
            spreadsheet.setRowHidden(headerRange.getFirstRow(), true);
        }
    }

}
