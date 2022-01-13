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
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/hidden-column")
public class HiddenColumnPage extends Div {

    public HiddenColumnPage() {
        Grid<Person> grid = new Grid<>();

        Person person = createPerson("foo", "bar@gmail.com");
        grid.setItems(Collections.singletonList(person));

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        grid.addColumn(Person::getEmail).setHeader("E-mail");

        nameColumn.setEditorComponent(new TextField());

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);

        TextField field = new TextField();
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        grid.addItemDoubleClickListener(
                event -> grid.getEditor().editItem(event.getItem()));

        grid.getElement()
                .addEventListener("keyup", event -> grid.getEditor().cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");

        NativeButton hideUnhide = new NativeButton("Hide/unhide Name column",
                event -> nameColumn.setVisible(!nameColumn.isVisible()));
        hideUnhide.setId("hide-unhide");

        NativeButton update = new NativeButton("Replace the item",
                event -> grid.setItems(createPerson("bar", "baz@example.com")));

        update.setId("update");

        add(grid, hideUnhide, update);
    }

    private Person createPerson(String name, String email) {
        Person person = new Person();
        person.setFirstName(name);
        person.setEmail(email);
        return person;
    }
}
