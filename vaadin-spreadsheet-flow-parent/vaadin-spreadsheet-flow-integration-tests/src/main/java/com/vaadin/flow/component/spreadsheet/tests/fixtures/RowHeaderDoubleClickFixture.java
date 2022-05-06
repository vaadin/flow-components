package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.Cell;

public class RowHeaderDoubleClickFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        spreadsheet.addRowHeaderDoubleClickListener(
                new Spreadsheet.RowHeaderDoubleClickListener() {
                    @Override
                    public void onRowHeaderDoubleClick(
                            Spreadsheet.RowHeaderDoubleClickEvent event) {

                        Cell cell = spreadsheet.createCell(event.getRowIndex(),
                                0, "Double-click on row header");

                        spreadsheet.refreshCells(cell);
                    }
                });
    }
}
