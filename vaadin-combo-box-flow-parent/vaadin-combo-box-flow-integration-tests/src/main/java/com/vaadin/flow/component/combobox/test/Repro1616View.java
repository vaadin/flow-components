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

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1616.
 *
 * Mutating the selected item's label and calling refreshAll() on the data
 * provider reportedly updates the label in the overlay but leaves the
 * selected-item caption in the input stale.
 */
@Route("repro-1616")
public class Repro1616View extends Div {

    public Repro1616View() {
        Item item = new Item(1, "a");

        ComboBox<Item> comboBox = new ComboBox<>();
        comboBox.setItems(item);
        comboBox.setItemLabelGenerator(it -> it.name);
        comboBox.setValue(item);

        Span serverValue = new Span();
        serverValue.setId("server-value");
        serverValue.setText("server value label: "
                + (comboBox.getValue() == null ? "null"
                        : comboBox.getValue().name));

        NativeButton refreshAll = new NativeButton("mutate + refreshAll",
                event -> {
                    item.name = item.name + "a";
                    comboBox.getDataProvider().refreshAll();
                    serverValue.setText(
                            "server value label: " + comboBox.getValue().name);
                });
        refreshAll.setId("refresh-all-button");

        NativeButton refreshItem = new NativeButton("mutate + refreshItem",
                event -> {
                    item.name = item.name + "b";
                    comboBox.getDataProvider().refreshItem(item);
                    serverValue.setText(
                            "server value label: " + comboBox.getValue().name);
                });
        refreshItem.setId("refresh-item-button");

        add(comboBox, refreshAll, refreshItem, serverValue);
    }

    public static class Item implements Serializable {
        public long id;
        public String name;

        public Item(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return id == ((Item) o).id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
