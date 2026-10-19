/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.ColumnRendering;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/cell-contents")
public class GridCellContentsPage extends Div {

    public GridCellContentsPage() {
        createSmallGrid();
        createLargeGrid();
        createGridWithHiddenColumn();
        createEmptyGrid();
        createLazyColumnsGrid();
    }

    private void createEmptyGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(List.of());

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setId("empty-grid");

        add(grid);
    }

    private void createSmallGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(IntStream.range(0, 10)
                .mapToObj(i -> new Person("Person " + i, i))
                .collect(Collectors.toList()));

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setId("small-grid");

        add(grid);
    }

    private void createLargeGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(
                DataProvider
                        .fromCallbacks(
                                query -> IntStream
                                        .range(query.getOffset(),
                                                query.getOffset()
                                                        + query.getLimit())
                                        .mapToObj(index -> new Person(
                                                "Person " + index, index)),
                                query -> 200));

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setId("large-grid");

        add(grid);
    }

    private void createGridWithHiddenColumn() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(IntStream.range(0, 10)
                .mapToObj(i -> new Person("Person " + i, i))
                .collect(Collectors.toList()));

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age").setVisible(false);
        grid.addColumn(person -> "Email" + person.getAge()).setHeader("Email");

        grid.setId("hidden-column-grid");

        add(grid);
    }

    private void createLazyColumnsGrid() {
        Grid<Integer> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, 100).boxed().collect(Collectors.toList()));

        // Many wide columns in a narrow grid, so that most columns start
        // outside the horizontal viewport and their cells are not rendered
        for (int col = 0; col < 20; col++) {
            int column = col;
            grid.addColumn(row -> "R" + row + "C" + column)
                    .setHeader("Col " + column).setWidth("200px")
                    .setFlexGrow(0);
        }
        grid.setColumnRendering(ColumnRendering.LAZY);
        grid.setWidth("500px");

        grid.setId("lazy-columns-grid");

        add(grid);
    }
}
