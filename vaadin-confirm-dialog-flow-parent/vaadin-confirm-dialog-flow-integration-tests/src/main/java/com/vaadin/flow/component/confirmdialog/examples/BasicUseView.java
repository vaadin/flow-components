package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route
public class BasicUseView extends Div {

    boolean expanded = true;
    public BasicUseView() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("My header");
        confirmDialog.setText("Here is my text");
        Button showDialogButton = new Button("Show dialog", e -> confirmDialog.open());
        add(confirmDialog, showDialogButton);
    }
}
