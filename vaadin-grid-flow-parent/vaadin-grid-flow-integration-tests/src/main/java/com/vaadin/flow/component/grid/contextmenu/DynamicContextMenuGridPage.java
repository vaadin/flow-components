/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.contextmenu;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/dynamic-context-menu-grid")
public class DynamicContextMenuGridPage extends Div {

    public DynamicContextMenuGridPage() {
        gridWithDynamicContextMenu();
    }

    private void gridWithDynamicContextMenu() {
        final Grid<Person> grid = new Grid<>();
        grid.addColumn(Person::getFirstName).setHeader("Name").setId("Name-Id");
        grid.addColumn(Person::getAge).setHeader("Born").setId("Born-Id");

        grid.setItems(IntStream.range(0, 50)
                .mapToObj(i -> new Person("Person " + i, i)));

        GridContextMenu<Person> contextMenu = grid.addContextMenu();

        contextMenu.setDynamicContentHandler(person -> {
            if (person == null || person.getAge() < 30) {
                // do not open the context menu
                return false;
            }

            contextMenu.removeAll();
            contextMenu.addItem(person.getFirstName());
            return true;
        });

        grid.setId("grid-with-dynamic-context-menu");
        add(grid);
    }
}
