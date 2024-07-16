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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/column-auto-width")
public class ColumnAutoWidthPage extends Div {

    public static final String GRID_ID = "auto-width-grid";

    public ColumnAutoWidthPage() {
        Grid<Person> grid = new Grid<>();
        grid.setId(GRID_ID);
        grid.getStyle().set("--lumo-font-family",
                "Arial, Helvetica, sans-serif");
        grid.setItems(new Person("Jorma", 2018));

        grid.addComponentColumn(person -> {
            NativeButton button = new NativeButton("F");
            return button;
        }).setAutoWidth(true).setHeader("A").setFlexGrow(0);
        grid.addColumn(Person::getId).setHeader("B");
        grid.addColumn(
                person -> "This is some length of text to check auto column width.")
                .setAutoWidth(true).setHeader("C").setFlexGrow(0);
        grid.addColumn(person -> "-").setAutoWidth(true)
                .setHeader("Some long text for column header").setFlexGrow(0);

        add(grid);
    }

}
