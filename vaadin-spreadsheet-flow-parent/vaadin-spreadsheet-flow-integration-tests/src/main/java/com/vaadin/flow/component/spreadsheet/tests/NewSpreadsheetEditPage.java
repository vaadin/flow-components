/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;

@Route("vaadin-spreadsheet/new-spreadsheet-edit-page")
final public class NewSpreadsheetEditPage extends Div {

    private Spreadsheet spreadsheet;

    public NewSpreadsheetEditPage() {
        setSizeFull();

        var freezePaneButton = new Button("Freeze pane",
                e -> spreadsheet.createFreezePane(6, 6));
        freezePaneButton.setId("freeze-pane-button");

        spreadsheet = new Spreadsheet();
        spreadsheet.setSizeFull();

        add(spreadsheet, freezePaneButton);
    }
}
