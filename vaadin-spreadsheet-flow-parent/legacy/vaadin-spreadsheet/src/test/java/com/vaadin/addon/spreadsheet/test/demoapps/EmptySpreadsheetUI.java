package com.vaadin.addon.spreadsheet.test.demoapps;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("demo")
@Widgetset("com.vaadin.addon.spreadsheet.Widgetset")
public class EmptySpreadsheetUI extends UI {


    private Spreadsheet spreadsheet = null;

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeUndefined();
        setContent(layout);
        spreadsheet = new Spreadsheet();
        layout.addComponent(spreadsheet);
    }
}
