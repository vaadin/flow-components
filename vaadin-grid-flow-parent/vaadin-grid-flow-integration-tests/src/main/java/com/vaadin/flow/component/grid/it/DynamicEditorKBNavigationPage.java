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
