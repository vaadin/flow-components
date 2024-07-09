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
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route(value = "vaadin-grid/beangridpage")
@Theme(Lumo.class)
public class BeanGridPage extends Div {

    public BeanGridPage() {
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setItems(new Person("Jorma", 2018));
        add(grid);
    }

}
