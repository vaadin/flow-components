package com.vaadin.addon.spreadsheet.demo.action;

import java.util.List;

import org.apache.poi.hssf.record.cf.CellRangeUtil;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.SpreadsheetAction;
import com.vaadin.addon.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.addon.spreadsheet.SpreadsheetTable;

public class InsertTableAction extends SpreadsheetAction {

    public InsertTableAction() {
        super("");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        if (!spreadsheet.getActiveSheet().getProtect()
                && event.getIndividualSelectedCells().length == 0
                && event.getCellRangeAddresses().length == 1) {
            CellRangeAddress[] cras = event.getCellRangeAddresses();
            CellRangeAddress cra = cras[0];
            List<SpreadsheetTable> tablesForActiveSheet = spreadsheet
                    .getTablesForActiveSheet();
            // check that the table doesn't contain a table that intersects with
            // the current selection
            for (SpreadsheetTable table : tablesForActiveSheet) {
                if (CellRangeUtil.intersect(cra, table.getFullTableRegion()) != CellRangeUtil.NO_INTERSECTION) {
                    return false;
                }
            }
            if (cra.getFirstRow() != cra.getLastRow()) {
                setCaption("Create Table on " + cra.formatAsString());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isApplicableForHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        return false;
    }

    @Override
    public void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        // spreadsheet.getActiveSheet().setAutoFilter(
        // event.getCellRangeAddresses()[0]);
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(spreadsheet,
                spreadsheet.getActiveSheet(), event.getCellRangeAddresses()[0]);
        spreadsheet.addTableToMemory(table);
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        // TODO throw error
    }

}
