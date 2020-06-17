package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("set-items-later")
public class SetItemsLaterPage extends VerticalLayout {

    public SetItemsLaterPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        NativeButton button = new NativeButton("Click me to add items to the combobox",
                event -> comboBox.setItems("foo", "bar"));
        button.setId("set-items-button");
        add(comboBox, button);
    }

}
