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
 *
 */

package com.vaadin.flow.component.listbox.test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * View for {@link ListBox} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-list-box-demo")
public class ListBoxViewDemoPage extends Div {

    public ListBoxViewDemoPage() {
        addListboxWithSelection();
        addComponentsBetween();
        addItemRenderer();
        addDisabledListBox();
        addItemLabelGenerator();
    }

    private void addListboxWithSelection() {
        Label message = new Label("-");
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Bread", "Butter", "Milk");

        listBox.addValueChangeListener(event -> message.setText(String.format(
                "Selection changed from %s to %s, selection is from client: %s",
                event.getOldValue(), event.getValue(), event.isFromClient())));

        NativeButton button = new NativeButton("Select Milk",
                event -> listBox.setValue("Milk"));
        addCard("ListBox and selection", listBox, button, message)
                .setId("list-box-with-selection");
    }

    private void addComponentsBetween() {
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Bread", "Butter", "Milk");

        // Adding components to the end:
        listBox.add(new H3("After all the items"));

        // Adding components after a specific item:
        listBox.addComponents("Butter", new H3("After butter"));

        // Adding components before a specific item:
        listBox.prependComponents("Bread", new H3("Before bread"));
        addCard("Adding components between items", listBox)
                .setId("list-box-with-components-between");
    }

    private void addItemLabelGenerator() {
        ListBox<Item> listBox = new ListBox<>();
        listBox.setItems(getItems());

        listBox.setItemLabelGenerator(Item::getName);

        addCard("Using item label generator", listBox)
                .setId("list-box-with-item-label-generator");
    }

    private void addItemRenderer() {
        ListBox<Item> listBox = new ListBox<>();
        ListBoxListDataView<Item> listDataView = listBox.setItems(getItems());

        listBox.setRenderer(new ComponentRenderer<>(item -> {
            Label name = new Label("Item: " + item.getName());
            Label stock = new Label("In stock: " + item.getStock());

            NativeButton button = new NativeButton("Buy", event -> {
                item.setStock(item.getStock() - 1);
                listDataView.refreshItem(item);
            });

            Div labels = new Div(name, stock);
            Div layout = new Div(labels, button);

            labels.getStyle().set("display", "flex")
                    .set("flexDirection", "column").set("marginRight", "10px");
            layout.getStyle().set("display", "flex").set("alignItems",
                    "center");

            return layout;
        }));

        listBox.setItemEnabledProvider(item -> item.getStock() > 0);

        addCard("Using item renderer and disabling items", listBox)
                .setId("list-box-with-renderer");
    }

    private void addDisabledListBox() {
        Label message = new Label("-");
        message.setId("message-label");

        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Bread", "Butter", "Milk");
        listBox.setEnabled(false);
        listBox.addValueChangeListener(event -> message.setText(String.format(
                "Selection changed from %s to %s, selection is from client: %s",
                event.getOldValue(), event.getValue(), event.isFromClient())));

        NativeButton button = new NativeButton("Select Milk",
                event -> listBox.setValue("Milk"));

        Label note = new Label(
                "Note! Even though updating from the client doesn't work, "
                        + "the server may push a new status for the component.");

        addCard("Disabled ListBox and selection", listBox, button, message,
                note).setId("disabled-list-box");
    }

    private List<Item> getItems() {
        return Stream.of("Bread", "Butter", "Milk").map(name -> {
            Item item = new Item();
            item.setName(name);
            item.setStock((int) (Math.random() * 5) + 1);
            return item;
        }).collect(Collectors.toList());
    }

    public static class Item {

        private String name;
        private int stock;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }

    private Component addCard(String title, Component... components) {
        return addCard(title, null, components);
    }

    private Component addCard(String title, String description,
            Component... components) {
        if (description != null) {
            title = title + ": " + description;
        }
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
        return layout;
    }
}
