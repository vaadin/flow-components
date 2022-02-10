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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/pre-multisorted")
public class PreMultiSortedPage extends Div {

    public PreMultiSortedPage() {
        Grid<Person> grid = new Grid<>(Person.class, true);

        setSizeFull();
        add(grid);
        grid.setPageSize(5);
        grid.setMultiSort(true);
        grid.setColumns("firstName", "lastName");

        List<GridSortOrder<Person>> sorting = new GridSortOrderBuilder<Person>()
                .thenAsc(grid.getColumns().get(0))
                .thenAsc(grid.getColumns().get(1)).build();
        grid.sort(sorting);

        List<Person> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Person person = new Person();
            person.setFirstName("First " + i);
            person.setLastName("Last " + i);
            items.add(person);
        }

        grid.setItems(items);
        grid.setHeightFull();
    }
}
