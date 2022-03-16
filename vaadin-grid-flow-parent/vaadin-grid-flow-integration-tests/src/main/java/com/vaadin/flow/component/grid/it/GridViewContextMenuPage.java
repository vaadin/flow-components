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
import java.util.Optional;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/context-menu")
public class GridViewContextMenuPage extends LegacyTestView {

    public GridViewContextMenuPage() {
        createContextMenu();
        createContextSubMenu();
    }

    private void createContextMenu() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        GridContextMenu<Person> contextMenu = new GridContextMenu<>(grid);
        contextMenu.addItem("Update", event -> {
            event.getItem().ifPresent(person -> {
                person.setFirstName(person.getFirstName() + " Updated");
                ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) event
                        .getGrid().getDataProvider();
                dataProvider.refreshItem(person);
            });
        });
        contextMenu.addItem("Remove", event -> {
            event.getItem().ifPresent(person -> {
                ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                        .getDataProvider();
                dataProvider.getItems().remove(person);
                dataProvider.refreshAll();
            });
        });
        grid.setId("context-menu-grid");
        addCard("Context Menu", "Using ContextMenu With Grid", grid,
                contextMenu);
    }

    private void createContextSubMenu() {
        Grid<Person> grid = new Grid<>();

        ListDataProvider<Person> dataProvider = DataProvider
                .ofCollection(getItems());

        grid.setDataProvider(dataProvider);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        GridContextMenu<Person> contextMenu = new GridContextMenu<>(grid);
        GridMenuItem<Person> insert = contextMenu.addItem("Insert");

        insert.getSubMenu().addItem("Insert a row above", event -> {
            Optional<Person> item = event.getItem();
            if (!item.isPresent()) {
                // no selected row
                return;
            }
            List<Person> items = (List) dataProvider.getItems();
            items.add(items.indexOf(item.get()),
                    new PeopleGenerator().createPerson(items.size() + 1));
            dataProvider.refreshAll();
        });
        insert.getSubMenu().add(new Hr());
        insert.getSubMenu().addItem("Insert a row below", event -> {
            Optional<Person> item = event.getItem();
            if (!item.isPresent()) {
                // no selected row
                return;
            }
            List<Person> items = (List) dataProvider.getItems();
            items.add(items.indexOf(item.get()) + 1,
                    new PeopleGenerator().createPerson(items.size() + 1));
            dataProvider.refreshAll();
        });
        grid.setId("context-submenu-grid");
        addCard("Context Menu", "Using Context Sub Menu With Grid", grid,
                contextMenu);
    }
}
