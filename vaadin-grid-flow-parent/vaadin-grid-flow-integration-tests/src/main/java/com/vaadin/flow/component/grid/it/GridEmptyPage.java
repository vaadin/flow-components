/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.UUID;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-empty")
public class GridEmptyPage extends Div {

    public GridEmptyPage() {
        Grid<Person> grid = new Grid<>();
        grid.setId("empty-grid");

        grid.addColumn(Person::getFirstName)
                .setKey(UUID.randomUUID().toString()).setHeader("First name")
                .setSortable(true);

        add(grid);

        final Button clearCache = new Button("Clear cache",
                event -> grid.getElement().executeJs("this.clearCache()"));
        clearCache.setId("clear-cache-button");
        add(clearCache);
    }

}
