/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SpreadsheetTheme;
import com.vaadin.flow.component.spreadsheet.SpreadsheetFilterTable;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("spreadsheet-filter")
@PageTitle("Spreadsheet filter")
public class SpreadsheetFilterPage extends VerticalLayout {

    public SpreadsheetFilterPage() {
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setTheme(SpreadsheetTheme.LUMO);
        spreadsheet.setHeight("400px");

        spreadsheet.createCell(0, 0, "Column A");
        spreadsheet.createCell(0, 1, "Column B");
        spreadsheet.createCell(0, 2, "Column C");

        spreadsheet.createCell(1, 0, "Alpha");
        spreadsheet.createCell(1, 1, "Foo");
        spreadsheet.createCell(1, 2, "Alice");

        spreadsheet.createCell(2, 0, "Beta");
        spreadsheet.createCell(2, 1, "Bar");
        spreadsheet.createCell(2, 2, "Bob");

        spreadsheet.createCell(3, 0, "Gamma");
        spreadsheet.createCell(3, 1, "Baz");
        spreadsheet.createCell(3, 2, "Carol");

        CellRangeAddress range = new CellRangeAddress(0, 3, 0, 2);
        SpreadsheetFilterTable table = new SpreadsheetFilterTable(spreadsheet,
                range);
        spreadsheet.registerTable(table);
        spreadsheet.refreshAllCellValues();

        add(spreadsheet);
    }
}
