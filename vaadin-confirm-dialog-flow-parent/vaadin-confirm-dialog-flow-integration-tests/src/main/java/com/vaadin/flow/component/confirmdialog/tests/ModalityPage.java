/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.confirmdialog.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/modality")
public class ModalityPage extends Div {
    public ModalityPage() {
        ConfirmDialog dialog = new ConfirmDialog();

        Button addDialog = new Button("Add dialog to UI", e -> add(dialog));
        addDialog.setId("add-dialog");

        Button openDialog = new Button("Open dialog", e -> dialog.open());
        openDialog.setId("open-dialog");

        Span testClickResult = new Span();
        testClickResult.setId("test-click-result");
        Button testClick = new Button("Test click",
                event -> testClickResult.setText("Click event received"));
        testClick.setId("test-click");

        add(new Div(addDialog, openDialog));
        add(new Div(testClick, testClickResult));
    }
}
