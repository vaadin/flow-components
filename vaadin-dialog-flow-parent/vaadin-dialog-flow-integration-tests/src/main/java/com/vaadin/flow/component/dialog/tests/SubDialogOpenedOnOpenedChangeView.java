/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog/sub-dialog-opened-on-opened-change")
public class SubDialogOpenedOnOpenedChangeView extends Div {

    public SubDialogOpenedOnOpenedChangeView() {
        Span output = new Span();
        output.setId("output");

        Dialog mainDialog = new Dialog();
        mainDialog.setHeaderTitle("Main Dialog");
        mainDialog.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                Dialog subDialog = new Dialog();
                subDialog.setHeaderTitle("Sub-Dialog");
                subDialog.open();
            }
        });
        mainDialog.addDetachListener(ev -> output.setText("Detached"));

        Button closeMainDialogAndOpenSubDialogButton = new Button(
                "Close main dialog and open sub-dialog",
                ev -> mainDialog.close());
        closeMainDialogAndOpenSubDialogButton
                .setId("close-main-dialog-and-open-sub-dialog");
        mainDialog.add(closeMainDialogAndOpenSubDialogButton);

        Button openMainDialogButton = new Button("Open main dialog");
        openMainDialogButton.setId("open-main-dialog");
        openMainDialogButton.addClickListener(e -> mainDialog.open());

        add(openMainDialogButton, output);
    }
}
