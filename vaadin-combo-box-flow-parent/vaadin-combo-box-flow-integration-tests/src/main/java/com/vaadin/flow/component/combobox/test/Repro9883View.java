/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/9883.
 * <p>
 * The items fit into a single page (30 items, page size 50), so the combo box
 * filters on the client and the server never sees the filter. With
 * {@code setFocusSelectedItem(true)}, the server resolves the selected item's
 * index against the unfiltered list and the client applies it to the filtered
 * list, which focuses a different item and commits it as the value on close.
 * <p>
 * "Banana 5" is at index 15 in the full list and at index 5 among the "Banana"
 * items.
 */
@Route("repro-9883")
public class Repro9883View extends Div {

    private static final String SELECTED = "Banana 5";

    public Repro9883View() {
        add(new Paragraph("Type 'Banana' into a combo box (replacing the "
                + "current text), then click outside to close the dropdown."));

        // Failing case: focusSelectedItem enabled
        add(comboBoxSection("broken", true,
                "setFocusSelectedItem(true): focuses and commits 'Banana 15'"));

        // Control: same setup with the feature off
        add(comboBoxSection("control", false,
                "setFocusSelectedItem(false): keeps 'Banana 5'"));
    }

    private Div comboBoxSection(String id, boolean focusSelectedItem,
            String description) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId(id);
        comboBox.setItems(items());
        comboBox.setFocusSelectedItem(focusSelectedItem);
        comboBox.setValue(SELECTED);

        Span value = new Span(comboBox.getValue());
        value.setId(id + "-value");
        comboBox.addValueChangeListener(
                event -> value.setText(String.valueOf(event.getValue())));

        NativeButton reset = new NativeButton("Reset value",
                event -> comboBox.setValue(SELECTED));
        reset.setId(id + "-reset");

        return new Div(new Paragraph(description), comboBox,
                new Div(new Span("Server-side value: "), value), reset);
    }

    private static List<String> items() {
        List<String> items = new ArrayList<>();
        IntStream.range(0, 10).forEach(i -> items.add("Apple " + i));
        IntStream.range(0, 20).forEach(i -> items.add("Banana " + i));
        return items;
    }
}
