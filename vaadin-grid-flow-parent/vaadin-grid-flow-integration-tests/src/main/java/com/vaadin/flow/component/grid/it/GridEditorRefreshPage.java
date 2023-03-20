/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
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
