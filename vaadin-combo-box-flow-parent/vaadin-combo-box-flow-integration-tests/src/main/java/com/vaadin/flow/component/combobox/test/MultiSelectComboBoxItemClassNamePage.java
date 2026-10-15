/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/item-class-name")
public class MultiSelectComboBoxItemClassNamePage extends Div {

    public MultiSelectComboBoxItemClassNamePage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems("foo", "bar", "baz");

        // Make component wider, so that we can fit multiple chips
        comboBox.setWidth("300px");

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

        NativeButton setValue = new NativeButton("Set value", e -> {
            comboBox.select("foo", "bar");
        });
        setValue.setId("set-value");

        add(comboBox, setClassNameGenerator, resetClassNameGenerator, setValue);
    }
}
