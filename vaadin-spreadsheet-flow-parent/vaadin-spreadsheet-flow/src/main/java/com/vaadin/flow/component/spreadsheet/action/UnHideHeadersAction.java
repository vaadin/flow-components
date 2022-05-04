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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;

/**
 * Spreadsheet action for showing hidden columns or rows.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class UnHideHeadersAction extends SpreadsheetAction {

    private int unhideHeaderIndex = 0;

    public UnHideHeadersAction() {
        super("");
    }

    @Override
    public boolean isApplicableForSelection(Spreadsheet spreadhseet,
            SelectionChangeEvent event) {
        return false;
    }

    @Override
    public boolean isApplicableForHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        Sheet activeSheet = spreadsheet.getActiveSheet();
        if (!isSheetProtected(activeSheet)) {
            if (headerRange.isFullColumnRange()) {
                int index = headerRange.getFirstColumn();
                if (!activeSheet.isColumnHidden(index)) {
                    if (index > 0 && activeSheet.isColumnHidden(index - 1)) {
                        setCaption("Unhide column " + getColumnHeader(index));
                        unhideHeaderIndex = index - 1;
                        return true;
                    } else if (activeSheet.isColumnHidden(index + 1)) {
                        setCaption(
                                "Unhide column " + getColumnHeader(index + 2));
                        unhideHeaderIndex = index + 1;
                        return true;
                    }
                } else {
                    setCaption("Unhide column " + getColumnHeader(index + 1));
                    unhideHeaderIndex = index;
                    return true;
                }
            } else if (headerRange.isFullRowRange()) {
                int index = headerRange.getFirstRow();
                if (!spreadsheet.isRowHidden(index)) {
                    if (index > 0 && spreadsheet.isRowHidden(index - 1)) {
                        setCaption("Unhide row " + (index));
                        unhideHeaderIndex = index - 1;
                        return true;
                    } else if (spreadsheet.isRowHidden(index + 1)) {
                        setCaption("Unhide row " + (index + 2));
                        unhideHeaderIndex = index + 1;
                        return true;
                    }
                } else {
                    setCaption("Unhide row " + (index + 1));
                    unhideHeaderIndex = index;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event) {
        throw new UnsupportedOperationException(
                "Show row/column action can't be executed against a selection.");
    }

    @Override
    public void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange) {
        if (headerRange.isFullColumnRange()) {
            spreadsheet.setColumnHidden(unhideHeaderIndex, false);
        } else if (headerRange.isFullRowRange()) {
            spreadsheet.setRowHidden(unhideHeaderIndex, false);
        }
    }

}
