/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.listbox.demo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ListBox} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-list-box")
public class ListBoxView extends DemoView {

    @Override
    public void initView() {
        addListboxWithSelection();
        addComponentsBetween();
        addItemRenderer();

        addCard("Example object used in the demo");
    }

    private void addListboxWithSelection() {
        Label message = new Label("-");
        // begin-source-example
        // source-example-heading: ListBox and selection
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Bread", "Butter", "Milk");

        listBox.addValueChangeListener(event -> message.setText(String.format(
                "Selection changed from %s to %s, selection is from client: %s",
                event.getOldValue(), event.getValue(), event.isFromClient())));

        NativeButton button = new NativeButton("Select Milk",
                event -> listBox.setValue("Milk"));
        // end-source-example
        addCard("ListBox and selection", listBox, button, message)
                .setId("list-box-with-selection");
    }

    private void addComponentsBetween() {
        // begin-source-example
        // source-example-heading: Adding components between items
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Bread", "Butter", "Milk");

        // Adding components to the end:
        listBox.add(new H3("After all the items"));

        // Adding components after a specific item:
        listBox.addComponents("Butter", new H3("After butter"));

        // Adding components before a specific item:
        listBox.prependComponents("Bread", new H3("Before bread"));
        // end-source-example
        addCard("Adding components between items", listBox)
                .setId("list-box-with-components-between");
    }

    private void addItemRenderer() {
        // begin-source-example
        // source-example-heading: Using item renderer and disabling items
        ListBox<Item> listBox = new ListBox<>();
        listBox.setItems(getItems());

        listBox.setItemRenderer(item -> {
            Label name = new Label("Item: " + item.getName());
            Label stock = new Label("In stock: " + item.getStock());

            NativeButton button = new NativeButton("Buy", event -> {
                item.setStock(item.getStock() - 1);
                listBox.getDataProvider().refreshItem(item);
            });

            Div labels = new Div(name, stock);
            Div layout = new Div(labels, button);

            labels.getStyle().set("display", "flex")
                    .set("flexDirection", "column").set("marginRight", "10px");
            layout.getStyle().set("display", "flex").set("alignItems",
                    "center");

            return layout;
        });

        listBox.setItemEnabledProvider(item -> item.getStock() > 0);

        // end-source-example
        addCard("Using item renderer and disabling items", listBox)
                .setId("list-box-with-renderer");
    }

    private List<Item> getItems() {
        return Stream.of("Bread", "Butter", "Milk").map(name -> {
            Item item = new Item();
            item.setName(name);
            item.setStock((int) (Math.random() * 5) + 1);
            return item;
        }).collect(Collectors.toList());
    }

    // begin-source-example
    // source-example-heading: Example object used in the demo
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
    // end-source-example
}
