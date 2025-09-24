/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/multi-select")
public class MultiSelectComboBoxMultiSelectPage extends Div {
    public MultiSelectComboBoxMultiSelectPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);
        // Make component wider, so that we can fit multiple chips
        comboBox.setWidth("500px");

        NativeButton selectItems = new NativeButton("Select a few items", e -> {
            comboBox.select("Item 1", "Item 2", "Item 3");
        });
        selectItems.setId("select-items");

        NativeButton deselectItems = new NativeButton("Deselect a few items",
                e -> {
                    comboBox.deselect("Item 1", "Item 2");
                });
        deselectItems.setId("deselect-items");

        NativeButton deselectAll = new NativeButton("Deselect all", e -> {
            comboBox.deselectAll();
        });
        deselectAll.setId("deselect-all");

        Span eventValue = new Span();
        eventValue.setId("event-value");
        Span eventOrigin = new Span();
        eventOrigin.setId("event-origin");
        comboBox.addSelectionListener(e -> {
            String combinedValues = String.join(",", e.getValue());

            eventValue.setText(combinedValues);
            eventOrigin.setText(e.isFromClient() ? "client" : "server");
        });

        add(comboBox);
        add(new Div(selectItems, deselectItems, deselectAll));
        add(new Div(new Span("Event value: "), eventValue));
        add(new Div(new Span("Event origin: "), eventOrigin));
    }
}
