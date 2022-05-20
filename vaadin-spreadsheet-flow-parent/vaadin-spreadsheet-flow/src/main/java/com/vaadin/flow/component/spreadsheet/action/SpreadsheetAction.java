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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.framework.Action;

/**
 * Abstract base class for Spreadsheet actions.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public abstract class SpreadsheetAction extends Action {

    public SpreadsheetAction(String caption) {
        super(caption);
    }

    /**
     * Returns true if this action is possible in the given spreadsheet for the
     * given selection.
     *
     * @param spreadsheet
     *            Target spreadsheet
     * @param event
     *            Selection event
     * @return true if it's possible to execute this action
     */
    public abstract boolean isApplicableForSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event);

    /**
     * Returns true if this action is possible for the given row/column header.
     *
     * @param spreadsheet
     *            Target spreadsheet
     * @param headerRange
     *            Target column/row header range
     * @return true if it's possible to execute this action
     */
    public abstract boolean isApplicableForHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange);

    /**
     * Execute this action on the given spreadsheet and selection.
     *
     * @param spreadsheet
     *            Target spreadsheet
     * @param event
     *            Selection event
     */
    public abstract void executeActionOnSelection(Spreadsheet spreadsheet,
            SelectionChangeEvent event);

    /**
     * Execute this action on the given spreadsheet and row/column header.
     *
     * @param spreadsheet
     *            Target spreadsheet
     * @param headerRange
     *            Target header range
     */
    public abstract void executeActionOnHeader(Spreadsheet spreadsheet,
            CellRangeAddress headerRange);

    /**
     * Returns the protection status of the given spreadsheet.
     *
     * @param spreadsheet
     *            Target spreadsheet
     * @return true if the given spreadsheet is protected
     */
    protected boolean isSheetProtected(Spreadsheet spreadsheet) {
        return spreadsheet.getActiveSheet().getProtect();
    }

    /**
     * Returns the protection status of the given sheet.
     *
     * @param sheet
     *            Target sheet
     * @return true if the given sheet is protected
     */
    protected boolean isSheetProtected(Sheet sheet) {
        return sheet.getProtect();
    }

    /**
     * Returns the locking status of the given cell.
     *
     * @param cell
     *            Target cell
     * @return true if the given cell is locked
     */
    protected boolean isCellLocked(Cell cell) {
        return cell.getSheet().getProtect() && cell.getCellStyle().getLocked();
    }

    /**
     * Returns the column header for the column at the given index.
     *
     * @param col
     *            Column index
     * @return Column header
     */
    protected String getColumnHeader(int col) {
        String h = "";
        while (col > 0) {
            h = (char) ('A' + (col - 1) % 26) + h;
            col = (col - 1) / 26;
        }
        return h;
    }
}
