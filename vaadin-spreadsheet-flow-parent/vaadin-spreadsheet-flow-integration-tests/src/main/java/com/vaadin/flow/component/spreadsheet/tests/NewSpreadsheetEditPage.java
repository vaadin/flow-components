package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/new-spreadsheet-edit-page")
public class NewSpreadsheetEditPage extends Div {

    private Spreadsheet spreadsheet;

    public NewSpreadsheetEditPage() {
        setSizeFull();

        spreadsheet = new Spreadsheet();
        spreadsheet.setSizeFull();

        add(spreadsheet);
    }
}
