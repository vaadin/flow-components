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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;

/**
 * Spreadsheet action for breaking a merged cell into its constituents.
 * 
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class UnMergeCellsAction extends SpreadsheetAction {

    public UnMergeCellsAction() {
        super("Unmerge cells");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        if (event.getSelectedCellMergedRegion() != null
                && event.getIndividualSelectedCells().size() == 0
                && event.getCellRangeAddresses().size() == 0) {
            Sheet sheet = spreadsheet.getActiveSheet();
            if (isSheetProtected(sheet)) {
                CellRangeAddress mergedCell = event
                        .getSelectedCellMergedRegion();

                Row row = sheet.getRow(mergedCell.getFirstRow());
                if (row != null) {
                    Cell cell = row.getCell(mergedCell.getFirstColumn());
                    if (isCellLocked(cell)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
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
        CellRangeAddress selectedCellReference = event
                .getSelectedCellMergedRegion();
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (selectedCellReference.getFirstColumn() == mergedRegion
                    .getFirstColumn()
                    && selectedCellReference.getFirstRow() == mergedRegion
                            .getFirstRow()) {
                spreadsheet.removeMergedRegion(i);
            }
        }
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        throw new UnsupportedOperationException(
                "Unmerge action can't be executed against a header range.");
    }

}
