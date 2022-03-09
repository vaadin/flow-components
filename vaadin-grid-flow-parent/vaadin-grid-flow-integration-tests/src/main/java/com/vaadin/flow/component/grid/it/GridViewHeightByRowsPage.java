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

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/height-by-rows")
public class GridViewHeightByRowsPage extends LegacyTestView {

    public GridViewHeightByRowsPage() {
        createHeightByRows();
    }

    private void createHeightByRows() {
        Grid<Person> grid = new Grid<>();

        // When using allRowsVisible, all items are fetched and
        // Grid uses all the space needed to render everything.
        grid.setAllRowsVisible(true);

        List<Person> people = createItems(50);
        grid.setItems(people);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);

        grid.setId("grid-height-by-rows");
        addCard("Height by Rows", "Using height by rows", grid);
    }
}
