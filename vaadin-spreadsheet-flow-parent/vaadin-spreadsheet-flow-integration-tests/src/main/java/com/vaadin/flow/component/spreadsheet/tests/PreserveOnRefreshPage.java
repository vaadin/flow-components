package com.vaadin.flow.component.spreadsheet.tests;

import java.io.IOException;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/preserve-on-refresh")
@PreserveOnRefresh
public class PreserveOnRefreshPage extends Div {

    public PreserveOnRefreshPage() throws IOException {
        setSizeFull();

        var stream = getClass().getResourceAsStream("/test_sheets/Bubble.xlsx");

        var spreadsheet = new Spreadsheet(stream);
        spreadsheet.setId("spreadsheet");
        add(spreadsheet);
    }

}
