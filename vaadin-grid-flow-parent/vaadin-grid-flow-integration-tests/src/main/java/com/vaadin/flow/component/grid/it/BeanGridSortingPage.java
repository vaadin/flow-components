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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/bean-grid-sorting")
public class BeanGridSortingPage extends Div {

    public BeanGridSortingPage() {
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setItems(new Person("Person 1", 99), new Person("Person 2", 1111),
                new Person("Person 3", 1));
        grid.setColumns("firstName", "age");
        add(grid);
    }

}
