/*
 * Copyright 2000-2023 Vaadin Ltd.
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
        var grid = new Grid<Item>();
        grid.addColumn(Item::displayValue).setHeader("Display value");
        grid.setItems(Arrays.asList(new Item("1", "1"), new Item("2", "2")));
        grid.select(new Item("1", "INVALID"));

        var useMultiSelectCheckbox = new Checkbox("Use multi-select",
                event -> grid
                        .setSelectionMode(Boolean.TRUE.equals(event.getValue())
                                ? Grid.SelectionMode.MULTI
                                : Grid.SelectionMode.SINGLE));
        useMultiSelectCheckbox.setId("use-multi-select-checkbox");

        var updateSelectionButton = new Button("Update selection", e -> {
            if (grid.getSelectedItems().stream().map(Item::id)
                    .anyMatch("2"::equals)) {
                grid.deselect(new Item("2", "INVALID"));
            } else {
                grid.select(new Item("2", "INVALID"));
            }
        });
        updateSelectionButton.setId("update-selection-button");

        var addGridButton = new Button("Add grid", e -> {
            e.getSource().setVisible(false);
            useMultiSelectCheckbox.setVisible(false);
            add(updateSelectionButton, grid);
        });
        addGridButton.setId("add-grid-button");

        add(useMultiSelectCheckbox, addGridButton);
    }

    private record Item(String id, String displayValue) {

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
