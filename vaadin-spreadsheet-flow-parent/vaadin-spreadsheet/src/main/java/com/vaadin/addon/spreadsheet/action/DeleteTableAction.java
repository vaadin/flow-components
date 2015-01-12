package com.vaadin.addon.spreadsheet.action;

import java.util.List;

import org.apache.poi.hssf.record.cf.CellRangeUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.SpreadsheetTable;

/**
 * Spreadsheet action for deleting a SpreadsheetTable.
 * 
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class DeleteTableAction extends SpreadsheetAction {

    public DeleteTableAction() {
        super("");
    }

    protected SpreadsheetTable tableToDelete;

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        if (!spreadsheet.getActiveSheet().getProtect()
                && (event.getIndividualSelectedCells().length == 0)) {
            if (event.getCellRangeAddresses().length == 1) {
                CellRangeAddress[] cras = event.getCellRangeAddresses();
                CellRangeAddress cra = cras[0];
                List<SpreadsheetTable> tablesForActiveSheet = spreadsheet
                        .getTablesForActiveSheet();
                for (SpreadsheetTable table : tablesForActiveSheet) {
                    if (CellRangeUtil
                            .intersect(cra, table.getFullTableRegion()) != CellRangeUtil.NO_INTERSECTION) {
                        setCaption("Delete Table "
                                + table.getFullTableRegion().formatAsString());
                        tableToDelete = table;
                        return true;
                    }
                }
            } else {
                CellReference selectedCellReference = event
                        .getSelectedCellReference();
                List<SpreadsheetTable> tablesForActiveSheet = spreadsheet
                        .getTablesForActiveSheet();
                for (SpreadsheetTable table : tablesForActiveSheet) {
                    if (table.getFullTableRegion().isInRange(
                            selectedCellReference.getRow(),
                            selectedCellReference.getCol())) {
                        setCaption("Delete Table "
                                + table.getFullTableRegion().formatAsString());
                        tableToDelete = table;
                        return true;
                    }
                }
            }
        }
        tableToDelete = null;
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
        if (tableToDelete != null) {
            spreadsheet.deleteTable(tableToDelete);
            tableToDelete = null;
        }
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        throw new UnsupportedOperationException(
                "Delete table action can't be executed against a header range.");
    }

}
