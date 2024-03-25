/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.action;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;

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
