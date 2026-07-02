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

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/column-toggle")
public class GridColumnTogglePage extends Div {

    public GridColumnTogglePage() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(List.of(
                new Person("John", "Doe", "john@example.com", 30, null, null),
                new Person("Jane", "Roe", "jane@example.com", 25, null, null)));

        // Marking columns as hideable makes the grid render its column toggle
        // button automatically; no extra component or wrapper is needed.
        Column<Person> firstName = grid.addColumn(Person::getFirstName)
                .setHeader("First name").setHideable(true);
        grid.addColumn(Person::getLastName).setHeader("Last name")
                .setHideable(true);
        grid.addColumn(Person::getEmail).setHeader("Email").setHideable(true);

        // Not hideable (the default), so excluded from the toggle menu.
        grid.addColumn(Person::getAge).setHeader("Age");

        // Reflects the server-side visibility state and event source so the IT
        // can verify the client toggle reached the server.
        Div status = new Div();
        status.setId("status");
        firstName.addVisibilityChangedListener(event -> status
                .setText(String.format("firstName visible=%s fromClient=%s",
                        event.isVisible(), event.isFromClient())));

        // Flips hideable on every column at runtime, so the IT can verify the
        // toggle button automatically hides itself when no column is hideable.
        NativeButton toggleHideable = new NativeButton("Toggle hideable",
                event -> {
                    boolean hideable = !firstName.isHideable();
                    grid.getColumns()
                            .forEach(column -> column.setHideable(hideable));
                });
        toggleHideable.setId("toggle-hideable");

        add(grid, toggleHideable, status);
    }
}
