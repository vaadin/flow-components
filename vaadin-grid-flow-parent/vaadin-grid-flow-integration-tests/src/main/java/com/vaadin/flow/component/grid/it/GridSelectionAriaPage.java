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

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/selection-aria")
public class GridSelectionAriaPage extends Div {

    public GridSelectionAriaPage() {
        createMultiSelectGrid();
        createNoneSelectGrid();
    }

    private void createMultiSelectGrid() {
        List<Person> people = new PeopleGenerator().generatePeople(10);

        Grid<Person> grid = new Grid<>();
        grid.setId("multi-selection");
        grid.setItems(people);
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.asMultiSelect().select(people.get(0), people.get(1));

        Div message = new Div();
        message.setId("multi-selection-message");
        grid.asMultiSelect()
                .addSelectionListener(event -> message.setText(String.format(
                        "%s -> %s, from client: %s",
                        getNames(event.getOldValue()),
                        getNames(event.getValue()), event.isFromClient())));

        NativeButton selectButton = new NativeButton("Select first five",
                event -> grid.asMultiSelect()
                        .select(people.subList(0, 5).toArray(new Person[0])));
        selectButton.setId("multi-selection-button");

        add(grid, selectButton, message);
    }

    private void createNoneSelectGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setId("none-selection");
        grid.setItems(new PeopleGenerator().generatePeople(10));
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.setSelectionMode(SelectionMode.NONE);
        add(grid);
    }

    private static List<String> getNames(Set<Person> people) {
        return people.stream().sorted(Comparator.comparingLong(Person::getId))
                .map(Person::getFirstName).toList();
    }
}
