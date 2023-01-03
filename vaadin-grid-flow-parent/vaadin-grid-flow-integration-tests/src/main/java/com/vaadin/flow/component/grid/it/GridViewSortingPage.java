/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/sorting")
public class GridViewSortingPage extends LegacyTestView {

    public GridViewSortingPage() {
        createSorting();
    }

    private void createSorting() {
        Div messageDiv = new Div();
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.setSelectionMode(SelectionMode.NONE);

        Grid.Column<Person> nameColumn = grid
                .addColumn(Person::getFirstName, "firstName").setHeader("Name");
        Grid.Column<Person> ageColumn = grid.addColumn(Person::getAge, "age")
                .setHeader("Age");

        Comparator<Person> addressComparator = Comparator
                .comparing((Person person) -> person.getAddress().getNumber())
                .thenComparing(
                        (Person person) -> person.getAddress().getNumber());

        grid.addColumn(LitRenderer.<Person> of(
                "<div>${item.street}, number ${item.number}<br><small>${item.postalCode}</small></div>")
                .withProperty("street",
                        person -> person.getAddress().getStreet())
                .withProperty("number",
                        person -> person.getAddress().getNumber())
                .withProperty("postalCode",
                        person -> person.getAddress().getPostalCode()))
                .setSortProperty("street", "number")
                .setComparator(addressComparator).setHeader("Address");

        Checkbox multiSort = new Checkbox("Multiple column sorting enabled");
        multiSort.addValueChangeListener(
                event -> grid.setMultiSort(event.getValue()));

        Checkbox multiSortPriority = new Checkbox(
                "Multi-sort priority: append");
        multiSortPriority.addValueChangeListener(event -> grid.setMultiSort(
                grid.isMultiSort(), event.getValue() ? MultiSortPriority.APPEND
                        : MultiSortPriority.PREPEND));

        grid.addSortListener(event -> {
            String currentSortOrder = grid.getDataCommunicator()
                    .getBackEndSorting().stream()
                    .map(querySortOrder -> String.format(
                            "{sort property: %s, direction: %s}",
                            querySortOrder.getSorted(),
                            querySortOrder.getDirection()))
                    .collect(Collectors.joining(", "));
            messageDiv.setText(String.format(
                    "Current sort order: %s. Sort originates from the client: %s.",
                    currentSortOrder, event.isFromClient()));
        });

        // you can set the sort order from server-side with the grid.sort method
        NativeButton invertAllSortings = new NativeButton(
                "Invert all sort directions", event -> {
                    List<GridSortOrder<Person>> orderList = grid.getSortOrder();
                    List<GridSortOrder<Person>> newOrderList = new ArrayList<>(
                            orderList.size());
                    for (GridSortOrder<Person> sort : orderList) {
                        newOrderList.add(new GridSortOrder<>(sort.getSorted(),
                                sort.getDirection().getOpposite()));
                    }
                    grid.sort(newOrderList);
                });

        NativeButton resetAllSortings = new NativeButton("Reset all sortings",
                event -> grid.sort(null));

        NativeButton toggleFirstColumnAndReset = new NativeButton(
                "Toggle first column and reset", event -> {
                    Grid.Column<Person> firstColumn = grid.getColumns().stream()
                            .findFirst().get();
                    firstColumn.setVisible(!firstColumn.isVisible());
                    grid.sort(null);
                });

        NativeButton sortByTwoColumns = new NativeButton("Sort by 2 columns",
                event -> {
                    grid.sort(GridSortOrder.asc(ageColumn).thenDesc(nameColumn)
                            .build());
                });

        grid.setId("grid-sortable-columns");
        multiSort.setId("grid-multi-sort-toggle");
        multiSortPriority.setId("grid-multi-sort-priority-toggle");
        invertAllSortings.setId("grid-sortable-columns-invert-sortings");
        resetAllSortings.setId("grid-sortable-columns-reset-sortings");
        toggleFirstColumnAndReset.setId("grid-sortable-columns-toggle-first");
        sortByTwoColumns.setId("grid-sortable-columns-sort-by-two");
        messageDiv.setId("grid-sortable-columns-message");
        addCard("Sorting", "Grid with sortable columns", grid, multiSort,
                multiSortPriority, invertAllSortings, resetAllSortings,
                toggleFirstColumnAndReset, sortByTwoColumns, messageDiv);
    }
}
