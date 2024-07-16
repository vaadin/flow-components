/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
