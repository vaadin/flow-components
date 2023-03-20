
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/dialog-with-combo")
public class DialogWithComboBoxPage extends Div {

    public DialogWithComboBoxPage() {
        Dialog dialog = new Dialog();

        ComboBox<String> combo = new ComboBox<>();
        combo.setItems("foo", "bar");
        combo.setId("combo");

        Div info = new Div();
        info.setId("info");

        combo.getElement().addPropertyChangeListener("opened",
                event -> info.setText(String.valueOf(combo.isOpened())));
        dialog.add(combo);

        NativeButton button = new NativeButton("Show dialog",
                event -> dialog.open());
        button.setId("open-dialog");

        add(info, button);
    }
}
