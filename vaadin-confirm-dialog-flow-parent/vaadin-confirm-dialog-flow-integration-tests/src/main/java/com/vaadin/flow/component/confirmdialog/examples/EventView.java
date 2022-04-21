package com.vaadin.flow.component.confirmdialog.examples;

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

        ConfirmDialog dialogCloseOnEsc = new ConfirmDialog();
        dialogCloseOnEsc.setCloseOnEsc(true);

        Button showDialogCloseOnEscButton = new Button("Show escape dialog",
                e -> dialogCloseOnEsc.open());
        showDialogCloseOnEscButton.setId("open-esc-dialog");

        ConfirmDialog dialogNoCloseOnEsc = new ConfirmDialog();
        dialogNoCloseOnEsc.setCloseOnEsc(false);

        Button showDialogNoCloseOnEscButton = new Button(
                "Show no escape dialog", e -> dialogNoCloseOnEsc.open());
        showDialogNoCloseOnEscButton.setId("open-no-esc-dialog");

        add(dialog, showDialogButton, dialogNoCloseOnEsc,
                showDialogCloseOnEscButton, dialogNoCloseOnEsc,
                showDialogNoCloseOnEscButton);
    }
}
