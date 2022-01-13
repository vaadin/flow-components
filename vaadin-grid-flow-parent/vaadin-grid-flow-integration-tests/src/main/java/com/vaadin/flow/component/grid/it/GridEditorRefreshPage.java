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

import java.util.Collections;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/editor-refresh")
public class GridEditorRefreshPage extends Div {

    public GridEditorRefreshPage() {
        Grid<Person> grid = new Grid<>();

        Person person = new Person();
        person.setFirstName("foo");
        person.setEmail("bar@gmail.com");

        ListDataProvider<Person> dataProvider = new ListDataProvider<>(
                Collections.singletonList(person));
        grid.setDataProvider(dataProvider);

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        grid.addColumn(Person::getEmail).setHeader("E-mail");

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);

        TextField field = new TextField();
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        grid.addItemDoubleClickListener(
                event -> grid.getEditor().editItem(event.getItem()));

        NativeButton replaceData = new NativeButton("Replace items",
                event -> grid.setItems(
                        Collections.singletonList(createAnotherPerson())));

        replaceData.setId("replace-items");

        NativeButton updateData = new NativeButton("Update item", event -> {
            person.setFirstName("bar");
            person.setEmail("baz@gmail.com");
            dataProvider.refreshItem(person);
        });

        updateData.setId("update-item");

        NativeButton updateAllData = new NativeButton("Update item", event -> {
            person.setFirstName("bar");
            person.setEmail("baz@gmail.com");
            dataProvider.refreshAll();
        });

        updateAllData.setId("update-all");

        add(grid, replaceData, updateData, updateAllData);
    }

    private Person createAnotherPerson() {
        Person person = new Person();
        person.setFirstName("bar");
        person.setEmail("baz@gmail.com");
        return person;
    }
}
