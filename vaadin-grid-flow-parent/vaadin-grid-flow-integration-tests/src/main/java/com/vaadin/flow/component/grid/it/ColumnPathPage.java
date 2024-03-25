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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/column-path")
public class ColumnPathPage extends Div {

    public ColumnPathPage() {
        Grid<Person> grid = new Grid<>();

        grid.setItems(new Person("Person 1", null, null, 42, null, null),
                new Person("Person 2", null, null, 42, null, null));

        grid.addColumn(Person::getFirstName).setHeader("Using path");
        grid.addColumn(TemplateRenderer.<Person> of("[[item.firstName]]")
                .withProperty("firstName", Person::getFirstName))
                .setHeader("Using template");
        grid.addColumn(Person::getFirstName)
                .setHeader("Using template because of editor")
                .setEditorComponent(new TextField());

        add(grid);
    }

}
