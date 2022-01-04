/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *
 * use this file except in compliance with the License. You may obtain a copy of
 *
 * the License at
 *
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 *
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *
 * License for the specific language governing permissions and limitations under
 *
 * the License.
 *
 */

package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/editor-focus")
public class GridEditorFocusPage extends Div {

    public GridEditorFocusPage() {
        Grid<Person> grid = new Grid<>();

        Person person = new Person();
        person.setFirstName("foo");

        List<Person> items = new ArrayList<>();
        items.add(person);

        ListDataProvider<Person> dataProvider = new ListDataProvider<>(items);
        grid.setDataProvider(dataProvider);

        Grid.Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);

        TextField field = new TextField();
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);
        editor.addOpenListener(event -> field.focus());
        grid.addItemDoubleClickListener(
                event -> grid.getEditor().editItem(event.getItem()));

        NativeButton addItem = new NativeButton("Add item", event -> {
            Person newPerson = new Person();
            items.add(newPerson);
            dataProvider.refreshAll();
            editor.editItem(newPerson);
        });

        addItem.setId("add-item");

        NativeButton editFirstRow = new NativeButton("Edit first item",
                event -> editor.editItem(items.get(0)));

        editFirstRow.setId("edit-first-item");

        add(grid, addItem, editFirstRow);
    }
}
