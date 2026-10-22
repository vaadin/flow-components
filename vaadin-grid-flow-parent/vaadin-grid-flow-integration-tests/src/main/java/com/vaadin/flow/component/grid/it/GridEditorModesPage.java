/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/editor-modes")
public class GridEditorModesPage extends Div {

    public GridEditorModesPage() {
        createBufferedEditor();
        createBufferedDynamicEditor();
        createNotBufferedDynamicEditor();
    }

    private void createBufferedEditor() {
        Div message = new Div();
        message.setId("buffered-editor-msg");

        Grid<Person> grid = new Grid<>();
        grid.setId("buffered-editor");
        grid.setItems(new PeopleGenerator().generatePeople(10));
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> subscriberColumn = grid.addColumn(Person::isSubscriber)
                .setHeader("Subscriber");

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        Div validationStatus = new Div();
        validationStatus.setId("validation");

        TextField field = new TextField();
        binder.forField(field)
                .withValidator(name -> name.startsWith("Person"),
                        "Name should start with Person")
                .withStatusLabel(validationStatus).bind("firstName");
        nameColumn.setEditorComponent(field);

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        addEditorColumn(grid, editor, field);

        editor.addSaveListener(
                event -> message.setText(event.getItem().getFirstName() + ", "
                        + event.getItem().isSubscriber()));

        add(message, validationStatus, grid);
    }

    private void createBufferedDynamicEditor() {
        Div message = new Div();
        message.setId("buffered-dynamic-editor-msg");

        Grid<Person> grid = new Grid<>();
        grid.setId("buffered-dynamic-editor");
        grid.setItems(new PeopleGenerator().generatePeople(10));
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> subscriberColumn = grid.addColumn(Person::isSubscriber)
                .setHeader("Subscriber");
        Column<Person> emailColumn = grid.addColumn(Person::getEmail)
                .setHeader("E-mail");

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        TextField field = new TextField();
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        Div validationStatus = new Div();
        validationStatus.setId("email-validation");

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        TextField emailField = new TextField();

        // A non-subscriber gets a read-only field that ignores its value
        TextField readOnlyEmail = new TextField();
        readOnlyEmail.setValue("Not a subscriber");
        readOnlyEmail.setReadOnly(true);

        Runnable bindEmail = () -> binder.forField(emailField)
                .withValidator(new EmailValidator("Invalid email"))
                .withStatusLabel(validationStatus).bind("email");

        Runnable setEmail = () -> emailColumn.setEditorComponent(item -> {
            if (item.isSubscriber()) {
                bindEmail.run();
                return emailField;
            }
            return readOnlyEmail;
        });
        setEmail.run();

        // In buffered mode the bean is only updated on save, so the email
        // editor follows the checkbox value instead of the bean
        checkbox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                emailColumn.setEditorComponent(item -> {
                    if (checkbox.getValue()) {
                        bindEmail.run();
                        return emailField;
                    }
                    return readOnlyEmail;
                });
                editor.refresh();
            }
        });
        editor.addCloseListener(event -> setEmail.run());

        addEditorColumn(grid, editor, field);

        editor.addSaveListener(
                event -> message.setText(event.getItem().getFirstName() + ", "
                        + event.getItem().isSubscriber() + ", "
                        + event.getItem().getEmail()));

        add(message, validationStatus, grid);
    }

    private void createNotBufferedDynamicEditor() {
        Div message = new Div();
        message.setId("not-buffered-dynamic-editor-msg");

        Grid<Person> grid = new Grid<>();
        grid.setId("not-buffered-dynamic-editor");
        grid.setItems(new PeopleGenerator().generatePeople(10));
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

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        TextField emailField = new TextField();
        emailColumn.setEditorComponent(item -> {
            if (item.isSubscriber()) {
                binder.bind(emailField, "email");
                return emailField;
            }
            return null;
        });

        // Refresh the editor so that the email editor follows the checkbox
        binder.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                editor.refresh();
            }
        });

        grid.addItemDoubleClickListener(event -> {
            editor.editItem(event.getItem());
            field.focus();
        });

        grid.addItemClickListener(event -> {
            if (binder.getBean() != null) {
                message.setText(binder.getBean().getFirstName() + ", "
                        + binder.getBean().isSubscriber() + ", "
                        + binder.getBean().getEmail());
            }
        });

        add(message, grid);
    }

    private void addEditorColumn(Grid<Person> grid, Editor<Person> editor,
            TextField focusField) {
        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        Column<Person> editorColumn = grid.addComponentColumn(person -> {
            Button edit = new Button("Edit", event -> {
                editor.editItem(person);
                focusField.focus();
            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(event -> editButtons
                .forEach(button -> button.setEnabled(false)));
        editor.addCloseListener(event -> editButtons
                .forEach(button -> button.setEnabled(true)));

        Button save = new Button("Save", event -> editor.save());
        Button cancel = new Button("Cancel", event -> editor.cancel());
        editorColumn.setEditorComponent(new Div(save, cancel));
    }
}
