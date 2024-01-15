/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.Objects;

@Route("vaadin-grid/select-item-with-identical-id")
public class SelectItemWithIdenticalIdPage extends Div {

    public SelectItemWithIdenticalIdPage() {
        Grid<Item> grid = new Grid<>();
        grid.addColumn(Item::getDisplayValue).setHeader("Display value");
        grid.setItems(Arrays.asList(new Item("1", "1"), new Item("2", "2")));

        Checkbox useMultiSelectCheckbox = new Checkbox("Use multi-select",
                event -> grid
                        .setSelectionMode(Boolean.TRUE.equals(event.getValue())
                                ? Grid.SelectionMode.MULTI
                                : Grid.SelectionMode.SINGLE));
        useMultiSelectCheckbox.setId("use-multi-select-checkbox");

        Button selectItem2Button = new Button("Select item 2",
                e -> grid.select(new Item("2", "INVALID")));
        selectItem2Button.setId("select-item-2-button");

        Button deselectItem1Button = new Button("Deselect item 1",
                e -> grid.deselect(new Item("1", "INVALID")));
        deselectItem1Button.setId("deselect-item-1-button");

        Button addGridWithPreselectionButton = new Button("Add grid", e -> {
            grid.select(new Item("1", "INVALID"));
            e.getSource().setVisible(false);
            add(grid);
        });
        addGridWithPreselectionButton
                .setId("add-grid-with-preselection-button");

        add(useMultiSelectCheckbox, addGridWithPreselectionButton,
                selectItem2Button, deselectItem1Button);
    }

    private class Item {

        private String id;

        private String displayValue;

        public Item(String id, String displayValue) {
            this.id = id;
            this.displayValue = displayValue;
        }

        public String getId() {
            return id;
        }

        public String getDisplayValue() {
            return displayValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Item item = (Item) o;
            return id.equals(item.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
