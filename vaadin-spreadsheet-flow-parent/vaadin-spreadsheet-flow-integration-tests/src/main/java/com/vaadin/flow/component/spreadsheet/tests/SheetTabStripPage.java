/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;

/**
 * A workbook with more sheets than its sheet tab strip has room for, and
 * buttons that set and clear the status label at the end of the strip.
 */
@Route("vaadin-spreadsheet/sheet-tab-strip")
public class SheetTabStripPage extends Div {

    /** The text the status label shows once it is set. */
    public static final String STATUS_LABEL = "Status label";

    public SheetTabStripPage() {
        Spreadsheet spreadsheet = createSpreadsheet();
        // Narrower than the tabs of the workbook's eleven visible sheets, so
        // the strip scrolls whatever the size of the window.
        spreadsheet.setWidth("700px");
        spreadsheet.setHeight("400px");

        // The sheet the server has active, which the tab the client marks as
        // selected does not prove.
        Span activeSheet = new Span(
                spreadsheet.getActiveSheet().getSheetName());
        activeSheet.setId("active-sheet");
        spreadsheet.addSheetChangeListener(event -> activeSheet
                .setText(event.getNewSheet().getSheetName()));

        NativeButton setStatusLabel = new NativeButton("Set status label",
                event -> spreadsheet.setStatusLabelValue(STATUS_LABEL));
        setStatusLabel.setId("set-status-label");
        NativeButton clearStatusLabel = new NativeButton("Clear status label",
                event -> spreadsheet.setStatusLabelValue(null));
        clearStatusLabel.setId("clear-status-label");

        add(new Div(setStatusLabel, clearStatusLabel, activeSheet),
                spreadsheet);
    }

    private static Spreadsheet createSpreadsheet() {
        try (InputStream workbook = SheetTabStripPage.class
                .getResourceAsStream("/test_sheets/multiple_sheets.xlsx")) {
            return new Spreadsheet(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
