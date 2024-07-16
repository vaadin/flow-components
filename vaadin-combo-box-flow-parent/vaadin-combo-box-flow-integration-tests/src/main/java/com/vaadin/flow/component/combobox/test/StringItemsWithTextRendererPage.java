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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/string-items-text-renderer")
public class StringItemsWithTextRendererPage extends Div {

    public StringItemsWithTextRendererPage() {
        Div info = new Div();
        info.setId("info");
        ComboBox<String> comboBox = new ComboBox<>();

        comboBox.setRenderer(new TextRenderer<>());
        comboBox.setItems("foo", "bar");
        comboBox.setId("list");
        comboBox.addValueChangeListener(
                event -> info.setText(comboBox.getValue()));

        add(comboBox, info);
    }
}
