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

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/basic")
public class GridBasicPage extends Div {

    public GridBasicPage() {
        createBasicGrid();
        createNoHeaderGrid();
        createLazyLoadingGrid();
    }

    private void createBasicGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(new PeopleGenerator().generatePeople(200));
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        grid.setId("basic");
        add(grid);
    }

    private void createNoHeaderGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(new PeopleGenerator().generatePeople(10));
        grid.addColumn(Person::getFirstName);
        grid.addColumn(Person::getAge);
        grid.setId("noHeader");
        add(grid);
    }

    private void createLazyLoadingGrid() {
        Grid<Person> grid = new Grid<>();
        PeopleGenerator generator = new PeopleGenerator();
        grid.setItems(DataProvider.fromCallbacks(
                query -> IntStream
                        .range(query.getOffset(),
                                query.getOffset() + query.getLimit())
                        .mapToObj(index -> generator.createPerson(index + 1)),
                query -> 100 * 1000 * 1000));
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        grid.setId("lazy-loading");
        add(grid);
    }
}
