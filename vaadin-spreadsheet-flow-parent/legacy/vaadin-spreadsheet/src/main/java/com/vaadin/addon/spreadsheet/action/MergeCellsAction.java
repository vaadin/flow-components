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
 * Spreadsheet action for merging two or more cells.
 * 
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class MergeCellsAction extends SpreadsheetAction {

    public MergeCellsAction() {
        super("Merge cells");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        if (event.getCellRangeAddresses().size() == 1
                && event.getIndividualSelectedCells().size() == 0) {
            Sheet sheet = spreadsheet.getActiveSheet();
            if (isSheetProtected(sheet)) {
                CellRangeAddress selection = event.getCellRangeAddresses().get(
                        0);
                for (int r = selection.getFirstRow(); r <= selection
                        .getLastRow(); r++) {
                    Row row = sheet.getRow(r);
                    if (row != null) {
                        for (int c = selection.getFirstColumn(); c <= selection
                                .getLastColumn(); c++) {
                            Cell cell = row.getCell(c);
                            if (isCellLocked(cell)) {
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isApplicableForHeader(Spreadsheet spreasdheet,
            CellRangeAddress headerRange) {
        return false;
    }

    @Override
    public void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        CellRangeAddress region = event.getCellRangeAddresses().get(0);
        try {
            spreadsheet.addMergedRegion(region);
        } catch (IllegalArgumentException iae) {
            System.out.println(iae.getMessage());
        }
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        throw new UnsupportedOperationException(
                "Merge action can't be executed against a header range.");
    }

}
