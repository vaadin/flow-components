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

package com.vaadin.flow.component.grid.it;

import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/clean-grid-items-after-detach-and-reattach")
public class GridClearAfterDetachAndReattachPage extends Div {

    public static final String GRID_ID = "person-grid-id";
    public static final String CLEAR_BUTTON_ID = "clear-button-id";

    public static final int GRID_ROW_COUNT = 150;

    public GridClearAfterDetachAndReattachPage() {
        Div gridContainer = new Div();
        gridContainer.setWidthFull();
        Grid<Person> grid = new Grid<>();
        grid.addColumn(Person::getFirstName);
        grid.addColumn(Person::getLastName);
        grid.addColumn(Person::getAge);
        final List<Person> persons = new PeopleGenerator()
                .generatePeople(GRID_ROW_COUNT);
        grid.setItems(persons);
        grid.setId(GRID_ID);

        NativeButton clearGrid = new NativeButton("Clear Grid", click -> {
            // Remove grid from it's container and add it again
            gridContainer.removeAll();
            gridContainer.add(grid);
            // Clear the underlying data
            grid.setItems(Collections.emptyList());
        });
        clearGrid.setId(CLEAR_BUTTON_ID);

        gridContainer.add(grid);
        add(gridContainer);
        add(clearGrid);
    }
}
