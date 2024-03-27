/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.action;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.command.RowInsertOrDeleteCommand;

/**
 * Spreadsheet action for inserting a new row to the sheet.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class InsertNewRowAction extends SpreadsheetAction {

    public InsertNewRowAction() {
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
                setCaption("Insert new row");
                return true;
            }
        }
        return false;
    }

    @Override
    public void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        throw new UnsupportedOperationException(
                "Insert new row action can't be executed against a selection.");
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        RowInsertOrDeleteCommand command = new RowInsertOrDeleteCommand(
                spreadsheet, headerRange);
        command.insertNewRow();
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
    }
}
