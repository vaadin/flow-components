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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/component-column-height")
public class ComponentColumnWithHeightPage extends Div {
    private List<Person> persons = new ArrayList<>();

    public ComponentColumnWithHeightPage() {
        Grid<Person> grid = new Grid<>();
        grid.addComponentColumn(person -> {
            Div div = new Div();
            div.setText(person.getFirstName());
            div.setHeight("50px");
            return div;
        });

        for (int i = 0; i < 50; i++) {
            persons.add(new Person("Name", -1));
        }
        grid.setItems(persons);

        NativeButton add = new NativeButton("Add person");
        add.addClickListener(e -> {
            persons.add(new Person("Name", -1));
            grid.setItems(persons);
        });

        add(grid, add);
    }
}
