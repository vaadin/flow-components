/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/set-items-later")
public class SetItemsLaterPage extends VerticalLayout {

    public SetItemsLaterPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        NativeButton button = new NativeButton(
                "Click me to add items to the combobox",
                event -> comboBox.setItems("foo", "bar"));
        button.setId("set-items-button");
        add(comboBox, button);
    }

}
