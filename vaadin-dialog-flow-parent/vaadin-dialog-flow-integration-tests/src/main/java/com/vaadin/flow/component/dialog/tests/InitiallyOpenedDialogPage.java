/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd.
 *
 */
@Route("vaadin-dialog/initial-dialog-open")
public class InitiallyOpenedDialogPage extends Div {

    public InitiallyOpenedDialogPage() {
        Dialog dialog = new Dialog();
        Label label = new Label("Label inside dialog");
        label.setId("nested-component");
        dialog.add(label);
        dialog.open();
    }
}
