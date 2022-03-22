package com.vaadin.addon.spreadsheet.test.demoapps;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.SpreadsheetTable;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("demo")
@Widgetset("com.vaadin.addon.spreadsheet.Widgetset")
public class TabsheetTestUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final Spreadsheet sheet = new Spreadsheet();
        sheet.setSizeFull();

        CellRangeAddress range = new CellRangeAddress(1, 4, 1, 4);

        final SpreadsheetTable table = new SpreadsheetTable(sheet, range);
        sheet.registerTable(table);

        TabSheet tabsheet = new TabSheet();
        tabsheet.setSizeFull();
        tabsheet.addTab(new Label("First"), "First tab");
        tabsheet.addTab(sheet, "Spreadsheet");
        tabsheet.addTab(new Label("Third"), "Third tab");

        setContent(tabsheet);
    }
}