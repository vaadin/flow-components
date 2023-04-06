/**
 * Copyright (C) 2000-2023 Vaadin Ltd
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
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/beangridpage")
public class BeanGridPage extends Div {

    public BeanGridPage() {
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setItems(new Person("Jorma", 2018), new Person("Jarvi", 33));
        grid.setItemDetailsRenderer(TemplateRenderer
                .<Person> of("<div>[[item.name]] [[item.age]]</div>")
                .withProperty("name", Person::getFirstName)
                .withProperty("age", Person::getAge));
        add(grid);
    }

}
