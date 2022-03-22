package com.vaadin.addon.spreadsheet.command;

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

import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.addon.spreadsheet.Spreadsheet;

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