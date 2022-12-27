/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.listbox.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-list-box/multi-select")
public class MultiSelectListBoxPage extends Div {

    public MultiSelectListBoxPage() {
        MultiSelectListBox<String> listbox = new MultiSelectListBox<>();

        List<String> items = new LinkedList<>(
                Arrays.asList("foo", "bar", "baz", "qux"));
        listbox.setItems(new ListDataProvider<>(items));

        Span fromClientSpan = new Span();
        fromClientSpan.setId("from-client");

        Div valueChanges = new Div();
        valueChanges.add(new Text("value:"));
        valueChanges.setId("value-changes");

        listbox.addValueChangeListener(e -> {
            valueChanges.add(new Paragraph(formatValue(e.getValue())));
            fromClientSpan.setText(e.isFromClient() + "");
        });

        Set<String> valueToSet = new HashSet<>();
        valueToSet.add("bar");
        valueToSet.add("qux");
        NativeButton setValueButton = new NativeButton("set value bar qux",
                e -> listbox.setValue(valueToSet));
        setValueButton.setId("set-value");

        NativeButton refreshAllButton = new NativeButton("Refresh all items",
                e -> listbox.getListDataView().refreshAll());
        refreshAllButton.setId("refresh-all-items");

        NativeButton updateItemsButton = new NativeButton("Update items", e -> {
            items.add("quux");
            items.remove(0);
            listbox.getListDataView().refreshAll();
        });
        updateItemsButton.setId("update-items");

        NativeButton updateItemLabelGeneratorButton = new NativeButton(
                "Update labels", e -> listbox
                        .setItemLabelGenerator(item -> item + " (Updated)"));
        updateItemLabelGeneratorButton.setId("update-labels");

        add(listbox, setValueButton, refreshAllButton, updateItemsButton,
                updateItemLabelGeneratorButton,
                new Div(new Span("fromClient: "), fromClientSpan),
                valueChanges);
    }

    private String formatValue(Set<String> value) {
        return String.join(", ", value);
    }

}
