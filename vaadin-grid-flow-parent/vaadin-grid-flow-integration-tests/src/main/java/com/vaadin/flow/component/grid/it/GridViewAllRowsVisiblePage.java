/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * Simple test view with all rows set to visible.
 */
@Route("vaadin-grid-it-demo/all-rows-visible")
public class GridViewAllRowsVisiblePage extends Div {

    /**
     * Sets up the test view.
     */
    public GridViewAllRowsVisiblePage() {
        Grid<Person> grid = new Grid<>();

        // When using allRowsVisible, all items are fetched and
        // Grid uses all the space needed to render everything.
        grid.setAllRowsVisible(true);

        List<Person> people = new PeopleGenerator().generatePeople(50);
        grid.setItems(people);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);

        grid.setId("grid-all-rows-visible");
        add(grid);
    }

}
