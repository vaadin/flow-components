package com.vaadin.addon.spreadsheet.action;

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

import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
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
                && (event.getIndividualSelectedCells().size() == 0)) {
            if (event.getCellRangeAddresses().size() == 1) {
                List<CellRangeAddress> cras = event.getCellRangeAddresses();
                CellRangeAddress cra = cras.get(0);
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
