/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/selected-items-on-top")
public class MultiSelectComboBoxSelectedItemsOnTopPage extends Div {
    public MultiSelectComboBoxSelectedItemsOnTopPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        NativeButton setSelectedOnTop = new NativeButton("Set selected on top",
                e -> {
                    comboBox.setSelectedItemsOnTop(true);
                });
        setSelectedOnTop.setId("set-selected-on-top");

        NativeButton unsetSelectedOnTop = new NativeButton(
                "Un-set selected on top", e -> {
                    comboBox.setSelectedItemsOnTop(false);
                });
        unsetSelectedOnTop.setId("unset-selected-on-top");

        add(comboBox);
        add(new Div(setSelectedOnTop, unsetSelectedOnTop));
    }
}
