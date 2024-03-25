/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.Collections;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/dynamic-editor-keyboard")
public class DynamicEditorKBNavigationPage extends Div {

    public DynamicEditorKBNavigationPage() {
        Grid<Person> grid = new Grid<>();

        Person person = new Person();
        person.setFirstName("foo");
        person.setSubscriber(true);
        person.setEmail("bar@gmail.com");
        grid.setItems(Collections.singletonList(person));

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> subscriberColumn = grid.addColumn(Person::isSubscriber)
                .setHeader("Subscriber");
        Column<Person> emailColumn = grid.addColumn(Person::getEmail)
                .setHeader("E-mail");

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);

        TextField field = new TextField();
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        Div validationStatus = new Div();
        validationStatus.getStyle().set("color", "red");
        validationStatus.setId("email-validation");

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        TextField emailField = new TextField();
        TextField readOnlyEmail = new TextField() {
            @Override
            public void setValue(String value) {
                super.setValue("Not a subscriber");
            }

            @Override
            public String getValue() {
                return "";
            }
        };
        readOnlyEmail.setValue("");
        readOnlyEmail.setReadOnly(true);

        emailColumn.setEditorComponent(item -> {
            if (item.isSubscriber()) {
                binder.bind(emailField, "email");
                return emailField;
            } else {
                binder.bind(readOnlyEmail, "email");
                return readOnlyEmail;
            }
        });

        grid.addItemDoubleClickListener(
                event -> grid.getEditor().editItem(event.getItem()));

        binder.addValueChangeListener(event -> {
            grid.getEditor().refresh();
        });

        Div message = new Div();
        message.setId("updated-person");

        grid.addItemClickListener(event -> {
            if (binder.getBean() != null) {
                message.setText(binder.getBean().getFirstName() + ", "
                        + binder.getBean().isSubscriber() + ", "
                        + binder.getBean().getEmail());
            }
        });

        add(grid, message);
    }
}
