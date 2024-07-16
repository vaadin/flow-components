/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

@Route("vaadin-dialog/close-listener-reopen-dialog")
public class CloseListenerReopenDialogPage extends Div {

    public CloseListenerReopenDialogPage() {
        Dialog dialog = new Dialog();
        NativeButton close = new NativeButton("Close dialog",
                event -> dialog.close());
        close.setId("close");
        dialog.add(close);
        Registration registration = dialog.addDialogCloseActionListener(
                event -> addInfo("main", "Main dialog is closed"));
        NativeButton open = new NativeButton("Open dialog",
                event -> dialog.open());
        open.setId("open");
        add(open);

        NativeButton button = new NativeButton("Remove close listener",
                event -> registration.remove());
        button.setId("remove");
        add(button);

        Dialog subDialog = new Dialog();
        subDialog.add(new Text("Subdialog"));
        dialog.add(subDialog);
        subDialog.addDialogCloseActionListener(event -> {
            addInfo("sub", "Subdialog is closed");
            subDialog.close();
        });

        NativeButton openSubDialog = new NativeButton("Open subdialog",
                event -> subDialog.open());
        openSubDialog.setId("open-sub");
        dialog.add(openSubDialog);
    }

    private void addInfo(String style, String text) {
        Div div = new Div();
        div.setText(text);
        div.addClassName(style);
        add(div);
    }
}
