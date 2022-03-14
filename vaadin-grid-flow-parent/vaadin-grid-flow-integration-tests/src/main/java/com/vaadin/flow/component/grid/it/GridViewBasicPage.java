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

import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo")
public class GridViewBasicPage extends LegacyTestView {

    public GridViewBasicPage() {
        createBasicUsage();
        createNoHeaderGrid();
        createCallBackDataProvider();
        createDisabledGrid();
    }

    private void createBasicUsage() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setId("basic");

        addCard("Grid Basics", grid);
    }

    private void createNoHeaderGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        grid.addColumn(Person::getFirstName);
        grid.addColumn(Person::getAge);

        grid.setId("noHeader");

        addCard("Grid Basic with no header", grid);
    }

    private void createCallBackDataProvider() {
        Grid<Person> grid = new Grid<>();

        /*
         * This Data Provider doesn't load all items into the memory right away.
         * Grid will request only the data that should be shown in its current
         * view "window". The Data Provider will use callbacks to load only a
         * portion of the data.
         */
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

        addCard("Grid with lazy loading", grid);
    }

    private void createDisabledGrid() {
        Grid<Person> grid = new Grid<>();

        List<Person> people = createItems(500);
        grid.setItems(people);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        grid.addColumn(new NativeButtonRenderer<>("Button"))
                .setHeader("Action");

        grid.setSelectionMode(SelectionMode.SINGLE);

        // The selection and action button won't work, but the scrolling will
        grid.setEnabled(false);

        NativeButton toggleEnable = new NativeButton("Toggle enable",
                evt -> grid.setEnabled(!grid.isEnabled()));
        toggleEnable.setId("disabled-grid-toggle-enable");
        Div div = new Div(toggleEnable);

        grid.setId("disabled-grid");
        addCard("Disabled grid", grid, div);
    }
}
