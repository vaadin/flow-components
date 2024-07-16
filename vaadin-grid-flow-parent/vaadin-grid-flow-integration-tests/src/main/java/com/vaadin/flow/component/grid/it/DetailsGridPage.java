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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * Details grid uses the same component for all shown details. If the grid
 * itemDetailsDataGenerator is not correctly cleaned then there will be an
 * exception when navigating and all data is cleared.
 */
@Route("vaadin-grid/detailsGrid")
public class DetailsGridPage extends Div {

    Span text = new Span("wow");

    public DetailsGridPage() {
        Grid<Person> grid = new Grid<>(Person.class);

        grid.setItems(new Person("Jorma", 2018), new Person("Jarmo", 2018),
                new Person("Jethro", 2018));

        grid.setItemDetailsRenderer(new ComponentRenderer<>(person -> {
            text.setText(person.getFirstName());
            return text;
        }));

        RouterLink next = new RouterLink("next", DisabledGridPage.class);
        next.setId("next");
        add(grid, next);
    }
}
