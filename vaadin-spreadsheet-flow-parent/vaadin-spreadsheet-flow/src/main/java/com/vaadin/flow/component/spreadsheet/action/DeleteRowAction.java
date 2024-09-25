/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.action;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.command.RowInsertOrDeleteCommand;

/**
 * Spreadsheet action for deleting a single row.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class DeleteRowAction extends SpreadsheetAction {

    public DeleteRowAction() {
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
                setCaption("Delete row " + (headerRange.getFirstRow() + 1));
                return true;
            }
        }
        return false;
    }

    @Override
    public void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        throw new UnsupportedOperationException(
                "Delete row action can't be executed against a selection.");
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        RowInsertOrDeleteCommand command = new RowInsertOrDeleteCommand(
                spreadsheet, headerRange);
        command.deleteRow();
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
    }

}
