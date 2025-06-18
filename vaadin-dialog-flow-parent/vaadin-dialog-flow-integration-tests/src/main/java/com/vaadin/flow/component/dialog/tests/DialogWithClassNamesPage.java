/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/dialog-class-names-test")
public class DialogWithClassNamesPage extends Div {

    private Dialog dialog;

    public DialogWithClassNamesPage() {
        dialog = new Dialog();
        dialog.addClassName("custom");

        NativeButton addClass = new NativeButton("Add class",
                event -> dialog.addClassName("added"));
        addClass.setId("add");
        dialog.add(addClass);

        NativeButton clearAllClass = new NativeButton("Clear all class",
                event -> dialog.getClassNames().clear());
        clearAllClass.setId("clear");
        dialog.add(clearAllClass);

        NativeButton open = new NativeButton("Open dialog",
                event -> dialog.open());
        open.setId("open");
        add(open);
    }

}
