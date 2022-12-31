package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetTable;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/tabsheet")
public class SpreadsheetTabSheetPage extends Div {

    public SpreadsheetTabSheetPage() {
        super();
        setSizeFull();

        final Spreadsheet sheet = new Spreadsheet();
        sheet.setSizeFull();

        CellRangeAddress range = new CellRangeAddress(1, 4, 1, 4);

        final SpreadsheetTable table = new SpreadsheetTable(sheet, range);
        sheet.registerTable(table);

        var sheetWrapper = new Div(sheet);
        sheetWrapper.setHeight("400px");

        TabSheet tabsheet = new TabSheet();
        tabsheet.setSizeFull();
        tabsheet.add("First tab", new Span("First"));
        var sheetTab = tabsheet.add("Spreadsheet", sheetWrapper);
        tabsheet.add("Third tab", new Span("Third"));

        // Since V23 TabSheet doesn't detach tab content on tab change, we need
        // to do it manually to make this test case work.
        tabsheet.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == sheetTab) {
                sheetWrapper.add(sheet);
            } else {
                sheetWrapper.removeAll();
            }
        });

        add(tabsheet);

    }
}
