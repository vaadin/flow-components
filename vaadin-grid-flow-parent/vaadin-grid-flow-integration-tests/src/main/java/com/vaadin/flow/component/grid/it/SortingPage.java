/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/sorting")
public class SortingPage extends Div {

    public SortingPage() {
        createSortingGrid();
        createInitiallyHiddenGrid();
    }

    private void createSortingGrid() {
        Grid<Person> grid = createGrid("sorting-grid", "sort-by-age");

        NativeButton btRm = new NativeButton("detach", evt -> remove(grid));
        btRm.setId("btn-detach");
        NativeButton btattach = new NativeButton("attach", evt -> add(grid));
        btattach.setId("btn-attach");
        add(btRm, btattach, grid);

    }

    private void createInitiallyHiddenGrid() {
        Grid<Person> grid = createGrid("hidden-grid", "sort-hidden-by-age");

        grid.setMaxHeight("0px");
        grid.getStyle().set("display", "none");

        NativeButton showGridBtn = new NativeButton("Show grid", e -> {
            grid.getStyle().set("display", "block");
            grid.getStyle().remove("max-height");
        });
        showGridBtn.setId("show-hidden-grid");

        add(grid, showGridBtn);
    }

    private Grid<Person> createGrid(String gridId, String sortBtnId) {
        Grid<Person> grid = new Grid<>();
        grid.setMultiSort(true);
        grid.setId(gridId);
        grid.setItems(new Person("B", 20), new Person("A", 30));

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader(new Span("Name"));
        Column<Person> ageColumn = grid.addColumn(Person::getAge)
                .setHeader("Age");

        // Needed to check that sorter is rendered in component header after
        // adding new header row
        grid.appendHeaderRow();

        List<GridSortOrder<Person>> sortByName = new GridSortOrderBuilder<Person>()
                .thenAsc(nameColumn).build();
        grid.sort(sortByName);

        NativeButton button = new NativeButton(
                sortBtnId.equals("sort-hidden-by-age") ? "Sort hidden by age"
                        : "Sort by age",
                e -> {
                    List<GridSortOrder<Person>> sortByAge = new GridSortOrderBuilder<Person>()
                            .thenAsc(ageColumn).build();
                    grid.sort(sortByAge);
                });
        button.setId(sortBtnId);

        NativeButton reOrder = new NativeButton("Re-order", e -> {
            grid.setColumnOrder(ageColumn, nameColumn);
        });
        reOrder.setId("reorder-button");

        NativeButton changeHeaderText = new NativeButton("Change header text",
                e -> {
                    ageColumn.setHeader("Age (updated)");
                });
        changeHeaderText.setId("change-header-text");

        NativeButton changeHeaderTextComponent = new NativeButton(
                "Change header text component", e -> {
                    ageColumn.setHeader(new Span("Age (updated)"));
                });
        changeHeaderTextComponent.setId("change-header-text-component");

        NativeButton clearButton = new NativeButton("Clear items",
                e -> grid.setItems(new ArrayList<Person>()));
        clearButton.setId("clear-items");

        add(button, reOrder, changeHeaderText, changeHeaderTextComponent,
                clearButton);

        return grid;
    }
}
