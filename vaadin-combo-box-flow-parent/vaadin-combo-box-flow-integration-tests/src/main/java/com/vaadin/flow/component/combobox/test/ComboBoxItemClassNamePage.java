/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/item-class-name")
public class ComboBoxItemClassNamePage extends Div {

    public ComboBoxItemClassNamePage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("foo", "bar", "baz");

        NativeButton setClassNameGenerator = new NativeButton(
                "Set class name generator", e -> {
                    comboBox.setClassNameGenerator(item -> "item-" + item);
                });
        setClassNameGenerator.setId("set-generator");

        NativeButton resetClassNameGenerator = new NativeButton(
                "Reset class name generator", e -> {
                    comboBox.setClassNameGenerator(item -> null);
                });
        resetClassNameGenerator.setId("reset-generator");

        add(comboBox, setClassNameGenerator, resetClassNameGenerator);
    }
}
