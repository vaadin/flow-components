package com.vaadin.addon.spreadsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.event.Action;

public abstract class SpreadsheetAction extends Action {

    public SpreadsheetAction(String caption) {
        super(caption);
    }

    public boolean isSheetProtected(Spreadsheet spreadsheet) {
        return spreadsheet.getActiveSheet().getProtect();
    }

    public boolean isSheetProtected(Sheet sheet) {
        return sheet.getProtect();
    }

    public boolean isCellLocked(Cell cell) {
        return cell.getSheet().getProtect() && cell.getCellStyle().getLocked();
    }

    public static String getColumnHeader(int col) {
        String h = "";
        while (col > 0) {
            h = (char) ('A' + (col - 1) % 26) + h;
            col = (col - 1) / 26;
        }
        return h;
    }

    /**
     * Returns true if this action is possible in the spreadsheet for the for
     * the given selection.
     * 
     * @param event
     * @return
     */
    public abstract boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event);

    /**
     * Returns true if this action is possible for the given row/column header.
     * 
     * @param headerRange
     * @return
     */
    public abstract boolean isApplicableForHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange);

    /**
     * Execute this action on the given spreadsheet and selection.
     * 
     * @param spreadsheet
     * @param event
     */
    public abstract void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event);

    /**
     * Execute this action on the given spreadsheet and row/column header.
     * 
     * @param spreadsheet
     * @param headerRange
     */
    public abstract void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange);
}
