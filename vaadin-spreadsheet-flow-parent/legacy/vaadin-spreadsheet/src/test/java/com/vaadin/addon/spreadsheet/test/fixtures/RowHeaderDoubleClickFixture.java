package com.vaadin.addon.spreadsheet.test.fixtures;

import org.apache.poi.ss.usermodel.Cell;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class RowHeaderDoubleClickFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        spreadsheet.addRowHeaderDoubleClickListener(
            new Spreadsheet.RowHeaderDoubleClickListener() {
                @Override
                public void onRowHeaderDoubleClick(
                    Spreadsheet.RowHeaderDoubleClickEvent event) {

                    Cell cell = spreadsheet.createCell(event.getRowIndex(), 0,
                        "Double-click on row header");

                    spreadsheet.refreshCells(cell);
                }
            });
    }
}
