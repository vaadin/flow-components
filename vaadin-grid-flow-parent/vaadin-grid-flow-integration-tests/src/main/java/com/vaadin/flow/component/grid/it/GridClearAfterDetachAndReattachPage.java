/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
