/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
