package com.vaadin.flow.component.spreadsheet.action;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;

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
                CellRangeAddress selection = event.getCellRangeAddresses()
                        .get(0);
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
            LoggerFactory.getLogger(this.getClass()).warn(iae.getMessage());
        }
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        throw new UnsupportedOperationException(
                "Merge action can't be executed against a header range.");
    }

}
