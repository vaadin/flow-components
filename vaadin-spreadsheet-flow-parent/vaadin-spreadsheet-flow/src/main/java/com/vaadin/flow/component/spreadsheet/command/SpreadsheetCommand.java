/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.command;

import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Abstract base class for Spreadsheet commands.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
public abstract class SpreadsheetCommand implements Command {

    protected int activeSheetIndex;
    protected final Spreadsheet spreadsheet;

    /**
     * Creates a new command targeting the given spreadsheet.
     *
     * @param spreadsheet
     *            The target spreadsheet
     */
    public SpreadsheetCommand(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        activeSheetIndex = spreadsheet.getActiveSheetIndex();
    }

    /**
     * Returns the currently active sheet.
     *
     * @return active sheet
     */
    protected Sheet getSheet() {
        return spreadsheet.getActiveSheet();
    }

    @Override
    public int getActiveSheetIndex() {
        return activeSheetIndex;
    }

    @Override
    public void setActiveSheetIndex(int activeSheetIndex) {
        this.activeSheetIndex = activeSheetIndex;
    }

}
