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
import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-list-box/list-box-retain-value")
public class ListBoxRetainValuePage extends VerticalLayout {

    public ListBoxRetainValuePage() {
        List<String> listBoxItems = new LinkedList<>(
                Arrays.asList("1", "2", "3", "4"));

        ListBox<String> listBox = new ListBox<>();
        listBox.setItems(listBoxItems);
        listBox.setValue("2");

        Div value = new Div();
        value.setId("list-box-value");
        value.setText(listBox.getValue());

        listBox.addValueChangeListener(e -> value.setText(e.getValue()));

        Button addButton = new Button("add");
        addButton.setId("add-button");
        addButton.addClickListener(event -> {
            remove(listBox);
            add(listBox);
            value.setText(listBox.getValue());
        });

        NativeButton refreshAllButton = new NativeButton("Refresh all items",
                e -> listBox.getListDataView().refreshAll());
        refreshAllButton.setId("refresh-all-items");

        NativeButton updateItemsButton = new NativeButton("Update items", e -> {
            listBoxItems.remove(1);
            listBoxItems.add("5");
            listBox.getListDataView().refreshAll();
        });
        updateItemsButton.setId("update-items");

        NativeButton updateItemLabelGeneratorButton = new NativeButton(
                "Update labels", e -> listBox
                        .setItemLabelGenerator(item -> item + " (Updated)"));
        updateItemLabelGeneratorButton.setId("update-labels");

        add(listBox, addButton, refreshAllButton, updateItemsButton,
                updateItemLabelGeneratorButton, value);
    }
}
