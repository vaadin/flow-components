package com.vaadin.flow.component.confirmdialog.tests;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/shortcuts")
public class ShortcutsPage extends Div {
    public ShortcutsPage() {
        ConfirmDialog dialog = new ConfirmDialog();

        Button shortcutButton = new Button("Confirm");
        shortcutButton.setId("shortcut-button");
        shortcutButton.addClickShortcut(Key.KEY_X);
        shortcutButton.addClickListener(e -> dialog.close());
        dialog.add(shortcutButton);

        Button openDialog = new Button("Open");
        openDialog.setId("open-dialog-button");
        openDialog.addClickListener(e -> dialog.open());
        add(openDialog);
    }
}
