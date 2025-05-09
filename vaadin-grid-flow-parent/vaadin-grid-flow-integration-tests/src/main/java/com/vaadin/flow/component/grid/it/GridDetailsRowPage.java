/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-details-row")
public class GridDetailsRowPage extends Div {

    private List<Person> items = new ArrayList<>();
    private Grid<Person> grid = new Grid<>();

    private static record Person(int id, String name) {
    }

    public GridDetailsRowPage() {
        items.add(new Person(0, "Person 0"));
        items.add(new Person(1, "Person 1"));
        items.add(new Person(2, "Person 2"));

        ListDataProvider<Person> dataProvider = new ListDataProvider<>(items) {
            @Override
            public Object getId(Person person) {
                return person.id();
            };
        };
        grid.setItems(dataProvider);
        grid.setSelectionMode(SelectionMode.NONE);
        grid.addColumn(person -> person.name()).setHeader("Name");
        grid.setItemDetailsRenderer(
                new ComponentRenderer<>(person -> new Button(person.name())));

        NativeButton updatePerson2 = new NativeButton("Update person 2", e -> {
            Person updatedPerson = new Person(2, "Updated Person 2");
            items.set(2, updatedPerson);
            grid.getDataProvider().refreshItem(updatedPerson);
        });
        updatePerson2.setId("update-person-2");

        NativeButton removePerson2 = new NativeButton("Remove person 2", e -> {
            items.remove(2);
            grid.getDataProvider().refreshAll();
        });
        removePerson2.setId("remove-person-2");

        setFirstAndSecondItemsVisible();

        NativeButton openDetails = new NativeButton("Open details", e -> {
            setFirstAndSecondItemsVisible();
        });

        add(grid, openDetails, updatePerson2, removePerson2);
    }

    public void setFirstAndSecondItemsVisible() {
        grid.setDetailsVisible(items.get(0), true);
        grid.setDetailsVisible(items.get(1), true);
    }
}
