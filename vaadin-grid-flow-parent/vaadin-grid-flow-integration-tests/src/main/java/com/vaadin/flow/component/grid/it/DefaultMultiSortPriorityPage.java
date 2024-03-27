/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
