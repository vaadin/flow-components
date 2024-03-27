/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/selection")
public class GridViewSelectionPage extends LegacyTestView {

    public GridViewSelectionPage() {
        createMultiSelect();
        createNoneSelect();
    }

    private void createMultiSelect() {
        Div messageDiv = new Div();
        List<Person> people = getItems();
        Grid<Person> grid = new Grid<>();
        grid.setItems(people);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.MULTI);

        grid.asMultiSelect().addSelectionListener(event -> {
            List<Person> previousSelectionSorted = event.getOldValue().stream()
                    .sorted(Comparator.comparingLong(Person::getId))
                    .collect(Collectors.toList());
            List<Person> newSelectionSorted = event.getValue().stream()
                    .sorted(Comparator.comparingLong(Person::getId))
                    .collect(Collectors.toList());

            messageDiv.setText(String.format(
                    "Selection changed from %s to %s, selection is from client: %s",
                    previousSelectionSorted, newSelectionSorted,
                    event.isFromClient()));
        });

        // You can pre-select items
        grid.asMultiSelect().select(people.get(0), people.get(1));

        NativeButton selectBtn = new NativeButton("Select first five persons");
        selectBtn.addClickListener(event -> grid.asMultiSelect()
                .select(people.subList(0, 5).toArray(new Person[5])));
        NativeButton deselectBtn = new NativeButton("Deselect all");
        deselectBtn
                .addClickListener(event -> grid.asMultiSelect().deselectAll());
        NativeButton selectAllBtn = new NativeButton("Select all");
        selectAllBtn.addClickListener(
                event -> ((GridMultiSelectionModel<Person>) grid
                        .getSelectionModel()).selectAll());
        grid.setId("multi-selection");
        selectBtn.setId("multi-selection-button");
        messageDiv.setId("multi-selection-message");
        addCard("Selection", "Grid Multi Selection", grid,
                new HorizontalLayout(selectBtn, deselectBtn, selectAllBtn),
                messageDiv);
    }

    private void createNoneSelect() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);
        grid.setId("none-selection");
        addCard("Selection", "Grid with No Selection Enabled", grid);
    }
}
