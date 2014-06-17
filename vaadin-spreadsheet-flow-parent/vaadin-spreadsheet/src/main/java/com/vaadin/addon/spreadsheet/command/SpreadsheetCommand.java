package com.vaadin.addon.spreadsheet.command;

import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public abstract class SpreadsheetCommand implements Command {

    protected int activeSheetIndex;
    protected final Spreadsheet spreadsheet;

    public SpreadsheetCommand(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        activeSheetIndex = spreadsheet.getActiveSheetIndex();
    }

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