/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog/content")
public class DialogContentView extends HorizontalLayout {

    private Dialog dialog = new Dialog();

    public DialogContentView() {
        var closeButton = new Button("Close dialog", e -> {
            dialog.close();
        });
        closeButton.setId("close-button");

        var addContentButton = new Button("Add content", e -> {
            dialog.add(closeButton);
        });
        addContentButton.setId("add-content-button");

        var openButton = new Button("Open dialog", e -> {
            dialog.open();
        });
        openButton.setId("open-button");

        // It's crucial that the dialog itself is added to the UI for this test
        add(dialog, addContentButton, openButton);
    }
}
