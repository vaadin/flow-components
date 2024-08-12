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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/default-multi-sort-priority")
public class DefaultMultiSortPriorityPage extends Div {

    public DefaultMultiSortPriorityPage() {
        NativeButton setThenAdd = new NativeButton("Set priority then add",
                e -> {
                    Grid.setDefaultMultiSortPriority(MultiSortPriority.APPEND);

                    Grid<Person> grid = new Grid<>();
                    grid.setMultiSort(true);
                    grid.setId("multi-sort-priority-grid");
                    grid.setItems(new Person("Bob", 20), new Person("Ann", 30),
                            new Person("Ann", 25));

                    Column<Person> nameColumn = grid
                            .addColumn(Person::getFirstName)
                            .setHeader(new Span("Name"))
                            .setComparator(Person::getFirstName);

                    Column<Person> ageColumn = grid.addColumn(Person::getAge)
                            .setHeader("Age").setComparator(Person::getAge);

                    add(grid);

                    // Restore initial state
                    Grid.setDefaultMultiSortPriority(MultiSortPriority.PREPEND);
                });
        setThenAdd.setId("btn-set-add");
        add(setThenAdd);
    }
}
