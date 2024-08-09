/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import org.apache.poi.ss.usermodel.Cell;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

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
