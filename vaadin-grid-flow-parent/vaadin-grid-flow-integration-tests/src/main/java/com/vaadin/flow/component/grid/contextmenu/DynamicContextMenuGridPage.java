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
