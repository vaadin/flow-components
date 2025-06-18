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
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/events")
public class EventView extends Div {

    public EventView() {
        ConfirmDialog dialog = new ConfirmDialog();

        Button showDialogButton = new Button("Show dialog", e -> dialog.open());
        showDialogButton.setId("open-dialog");

        Button toggleCloseOnEscButton = new Button("Toggle close on Esc",
                e -> dialog.setCloseOnEsc(!dialog.isCloseOnEsc()));
        toggleCloseOnEscButton.setId("toggle-close-on-esc");

        add(dialog, showDialogButton, toggleCloseOnEscButton);
    }
}
