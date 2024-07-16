/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/preserve-on-refresh")
@PreserveOnRefresh
public class PreserveOnRefreshPage extends Div {

    int count = 0;

    public PreserveOnRefreshPage() {
        Grid<Person> grid = new Grid<>();
        Person foo = new Person("foo", 20);
        grid.setItems(foo);
        grid.addComponentColumn(person -> new Span(person.getFirstName()))
                .setHeader(new Span("header")).setFooter(new Span("footer"));

        // Add editable column
        Column<Person> firstNameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("First Name");

        // define editor & binder for editor
        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        // define editor components for columns
        TextField firstNameField = new TextField();
        binder.bind(firstNameField, Person::getFirstName, Person::setFirstName);
        firstNameColumn.setEditorComponent(firstNameField);

        Button button = new Button("Edit");
        button.setId("edit-button");
        button.addClickListener(event -> {
            grid.getEditor().editItem(foo);
        });

        // Grid editor will fire close event, this will be tested
        grid.getEditor().addCloseListener(event -> {
            Span close = new Span("Closed");
            close.setId("closed");
            add(close);
        });

        // Grid editor will fire open event, this will be tested after
        // refresh
        grid.getEditor().addOpenListener(event -> {
            count++;
            Span open = new Span("Open: " + count);
            open.setId("open-" + count);
            add(open);
        });
        add(grid, button);
    }

}
