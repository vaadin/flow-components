/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
