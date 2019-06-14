/*
 * Copyright 2000-2019 Vaadin Ltd.
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
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("sorting")
@Theme(Lumo.class)
public class SortingPage extends Div {

    public SortingPage() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(new Person("B", 20), new Person("A", 30));
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> ageColumn = grid.addColumn(Person::getAge)
                .setHeader("Age");
        add(grid);

        List<GridSortOrder<Person>> sortByName = new GridSortOrderBuilder<Person>()
                .thenAsc(nameColumn).build();
        grid.sort(sortByName);

        NativeButton button = new NativeButton("Sort by age", e -> {
            List<GridSortOrder<Person>> sortByAge = new GridSortOrderBuilder<Person>()
                    .thenAsc(ageColumn).build();
            grid.sort(sortByAge);
        });
        button.setId("sort-by-age");
        add(button);
    }

}
