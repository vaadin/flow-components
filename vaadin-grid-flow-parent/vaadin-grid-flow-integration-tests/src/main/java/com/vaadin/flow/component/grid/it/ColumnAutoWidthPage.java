/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
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
    public ColumnAutoWidthPage() {
        Grid<Person> grid = new Grid<>();
        grid.getStyle().set("--lumo-font-family",
                "Arial, Helvetica, sans-serif");
        grid.setItems(new Person("Jorma", 2018));

        grid.addComponentColumn(
                person -> new NativeButton(person.getFirstName()))
                .setAutoWidth(true).setHeader("A").setFlexGrow(0);
        grid.addColumn(Person::getId).setHeader("B");
        grid.addColumn(
                person -> "This is some length of text to check auto column width.")
                .setAutoWidth(true).setHeader("C").setFlexGrow(0);
        grid.addColumn(person -> "-").setAutoWidth(true)
                .setHeader("Some long text for column header").setFlexGrow(0);

        add(grid);

        NativeButton updateItems = new NativeButton("Update items", event -> {
            grid.setItems(new Person("A longer firstname", 2018));
            grid.recalculateColumnWidths();
        });
        updateItems.setId("update-items");
        add(updateItems);
    }

}
