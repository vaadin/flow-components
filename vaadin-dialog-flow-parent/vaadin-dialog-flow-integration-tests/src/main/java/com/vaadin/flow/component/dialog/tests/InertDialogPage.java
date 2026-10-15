/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/inert-dialog")
public class InertDialogPage extends Div {
    public InertDialogPage() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);
        add(dialog);

        NativeButton setInert = new NativeButton("Set inert", event -> {
            ElementUtil.setInert(dialog.getElement(), true);
        });
        setInert.setId("set-inert");
        dialog.add(setInert);

        dialog.open();
    }
}
