/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/component-column-height")
public class ComponentColumnWithHeightPage extends Div {
    private List<Person> persons = new ArrayList<>();

    public ComponentColumnWithHeightPage() {
        Grid<Person> grid = new Grid<>();
        grid.addComponentColumn(person -> {
            Div div = new Div();
            div.setText(person.getFirstName());
            div.setHeight("50px");
            return div;
        });

        for (int i = 0; i < 50; i++) {
            persons.add(new Person("Name", -1));
        }
        grid.setItems(persons);

        NativeButton add = new NativeButton("Add person");
        add.addClickListener(e -> {
            persons.add(new Person("Name", -1));
            grid.setItems(persons);
        });

        add(grid, add);
    }
}
