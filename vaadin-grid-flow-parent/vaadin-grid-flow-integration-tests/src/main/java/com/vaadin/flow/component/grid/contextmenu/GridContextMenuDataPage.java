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
package com.vaadin.flow.component.grid.contextmenu;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/context-menu-data")
public class GridContextMenuDataPage extends Div {

    private final PeopleGenerator generator = new PeopleGenerator();

    public GridContextMenuDataPage() {
        createContextMenuGrid();
        createContextSubMenuGrid();
    }

    private void createContextMenuGrid() {
        ListDataProvider<Person> dataProvider = DataProvider
                .ofCollection(generator.generatePeople(10));

        Grid<Person> grid = new Grid<>();
        grid.setId("context-menu-grid");
        grid.setItems(dataProvider);
        grid.addColumn(Person::getFirstName).setHeader("Name");

        GridContextMenu<Person> contextMenu = new GridContextMenu<>(grid);
        contextMenu.addItem("Update",
                event -> event.getItem().ifPresent(person -> {
                    person.setFirstName(person.getFirstName() + " Updated");
                    dataProvider.refreshItem(person);
                }));
        contextMenu.addItem("Remove",
                event -> event.getItem().ifPresent(person -> {
                    dataProvider.getItems().remove(person);
                    dataProvider.refreshAll();
                }));

        add(grid);
    }

    private void createContextSubMenuGrid() {
        List<Person> items = generator.generatePeople(10);
        ListDataProvider<Person> dataProvider = DataProvider
                .ofCollection(items);

        Grid<Person> grid = new Grid<>();
        grid.setId("context-submenu-grid");
        grid.setItems(dataProvider);
        grid.addColumn(Person::getFirstName).setHeader("Name");

        GridContextMenu<Person> contextMenu = new GridContextMenu<>(grid);
        GridMenuItem<Person> insert = contextMenu.addItem("Insert");
        insert.getSubMenu().addItem("Insert a row above",
                event -> event.getItem().ifPresent(person -> {
                    items.add(items.indexOf(person),
                            generator.createPerson(items.size() + 1));
                    dataProvider.refreshAll();
                }));
        insert.getSubMenu().addItem("Insert a row below",
                event -> event.getItem().ifPresent(person -> {
                    items.add(items.indexOf(person) + 1,
                            generator.createPerson(items.size() + 1));
                    dataProvider.refreshAll();
                }));

        add(grid);
    }
}
