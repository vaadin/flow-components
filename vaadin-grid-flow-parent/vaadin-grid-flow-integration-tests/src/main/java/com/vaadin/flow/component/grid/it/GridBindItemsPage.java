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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;

@Route("vaadin-grid/grid-bind-items")
public class GridBindItemsPage extends Div {

    public static final String GRID_ID = "test-grid";
    public static final String ADD_ITEM_BUTTON = "add-item-button";
    public static final String REMOVE_ITEM_BUTTON = "remove-item-button";
    public static final String ITEM_COUNT_SPAN = "item-count";

    private final ListSignal<Person> itemsSignal;

    public GridBindItemsPage() {
        // Create list signal and add initial items
        itemsSignal = new ListSignal<>();
        itemsSignal.insertLast(new Person("Alice", "Smith", "alice@example.com",
                25, null, null));
        itemsSignal.insertLast(new Person("Bob", "Johnson", "bob@example.com",
                30, null, null));
        itemsSignal.insertLast(new Person("Charlie", "Brown",
                "charlie@example.com", 35, null, null));

        // Create and configure grid
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setId(GRID_ID);
        grid.setColumns("firstName", "lastName", "email", "age");
        grid.bindItems(itemsSignal);

        // Item count display
        Span itemCount = new Span(String.valueOf(itemsSignal.peek().size()));
        itemCount.setId(ITEM_COUNT_SPAN);

        // Button to add a new item
        Button addItemButton = new Button("Add Item", event -> {
            int currentSize = itemsSignal.peek().size();
            itemsSignal.insertLast(new Person("New Person " + (currentSize + 1),
                    "Lastname", "new@example.com", 20, null, null));

            // Update display
            itemCount.setText(String.valueOf(itemsSignal.peek().size()));
        });
        addItemButton.setId(ADD_ITEM_BUTTON);

        // Button to remove the last item
        Button removeItemButton = new Button("Remove Last Item", event -> {
            var currentItems = itemsSignal.peek();
            if (!currentItems.isEmpty()) {
                itemsSignal.remove(currentItems.getLast());

                // Update display
                itemCount.setText(String.valueOf(itemsSignal.peek().size()));
            }
        });
        removeItemButton.setId(REMOVE_ITEM_BUTTON);

        add(grid, new Div(new Span("Item Count: "), itemCount), addItemButton,
                removeItemButton);
    }
}
