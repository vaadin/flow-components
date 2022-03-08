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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/grid-editor")
public class GridViewEditorPage extends LegacyTestView {

    public GridViewEditorPage() {
        createBufferedEditor();
        createNotBufferedEditor();
        createBufferedDynamicEditor();
        createNotBufferedDynamicEditor();
    }

    private void createBufferedEditor() {
        Div message = new Div();
        message.setId("buffered-editor-msg");

        Grid<Person> grid = new Grid<>();
        List<Person> persons = getItems();
        grid.setItems(persons);
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

        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        Column<Person> editorColumn = grid.addComponentColumn(person -> {
            Button edit = new Button("Edit");
            edit.addClassName("edit");
            edit.addClickListener(e -> {
                editor.editItem(person);
                field.focus();
            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Button save = new Button("Save", e -> editor.save());
        save.addClassName("save");

        Button cancel = new Button("Cancel", e -> editor.cancel());
        cancel.addClassName("cancel");

        // Add a keypress listener that listens for an escape key up event.
        // Note! some browsers return key as Escape and some as Esc
        grid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);

        editor.addSaveListener(
                event -> message.setText(event.getItem().getFirstName() + ", "
                        + event.getItem().isSubscriber()));

        grid.setId("buffered-editor");
        addCard("Grid Editor", "Editor in Buffered Mode", message,
                validationStatus, grid);
    }

    private void createNotBufferedEditor() {
        Div message = new Div();
        message.setId("not-buffered-editor-msg");

        Grid<Person> grid = new Grid<>();
        List<Person> persons = getItems();
        grid.setItems(persons);
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> subscriberColumn = grid.addColumn(Person::isSubscriber)
                .setHeader("Subscriber");

        Binder<Person> binder = new Binder<>(Person.class);
        grid.getEditor().setBinder(binder);

        TextField field = new TextField();
        // Close the editor in case of backward between components
        field.getElement()
                .addEventListener("keydown",
                        event -> grid.getEditor().closeEditor())
                .setFilter("event.key === 'Tab' && event.shiftKey");

        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        // Close the editor in case of forward navigation between
        checkbox.getElement()
                .addEventListener("keydown",
                        event -> grid.getEditor().closeEditor())
                .setFilter("event.key === 'Tab' && !event.shiftKey");

        grid.addItemDoubleClickListener(event -> {
            grid.getEditor().editItem(event.getItem());
            field.focus();
        });

        grid.addItemClickListener(event -> {
            if (binder.getBean() != null) {
                message.setText(binder.getBean().getFirstName() + ", "
                        + binder.getBean().isSubscriber());
            }
        });

        grid.setId("not-buffered-editor");
        addCard("Grid Editor", "Editor in Not Buffered Mode", message, grid);
    }

    private void createBufferedDynamicEditor() {
        Div message = new Div();
        message.setId("buffered-dynamic-editor-msg");

        Grid<Person> grid = new Grid<>();
        List<Person> persons = new ArrayList<>();
        persons.addAll(createItems());
        grid.setItems(persons);

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
        validationStatus.getStyle().set("color", "red");
        validationStatus.setId("email-validation");

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        TextField emailField = new TextField();

        // When not a subscriber, we want to show a read-only text-field that
        // ignores whatever is set to it
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
            } else {
                return readOnlyEmail;
            }
        });

        // Sets the binding based on the Person bean state
        setEmail.run();

        // Refresh subscriber editor component when checkbox value is changed
        checkbox.addValueChangeListener(event -> {
            // Only updates from the client-side should be taken into account
            if (event.isFromClient()) {

                // When using buffered mode, the partial updates shouldn't be
                // propagated to the bean before the Save button is clicked, so
                // here we need to override the binding function to take the
                // checkbox state into consideration instead
                emailColumn.setEditorComponent(item -> {
                    if (checkbox.getValue()) {
                        bindEmail.run();
                        return emailField;
                    } else {
                        return readOnlyEmail;
                    }
                });
                grid.getEditor().refresh();
            }
        });

        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        // Resets the binding function to use the bean state whenever the editor
        // is closed
        editor.addCloseListener(event -> {
            setEmail.run();
            editButtons.stream().forEach(button -> button.setEnabled(true));
        });

        Column<Person> editorColumn = grid.addComponentColumn(person -> {
            Button edit = new Button("Edit");
            edit.addClassName("edit");
            edit.addClickListener(e -> {
                editor.editItem(person);
                field.focus();
            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Button save = new Button("Save", e -> editor.save());
        save.addClassName("save");

        Button cancel = new Button("Cancel", e -> editor.cancel());
        cancel.addClassName("cancel");

        // Add a keypress listener that listens for an escape key up event.
        // Note! some browsers return key as Escape and some as Esc
        grid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);

        editor.addSaveListener(
                event -> message.setText(event.getItem().getFirstName() + ", "
                        + event.getItem().isSubscriber() + ", "
                        + event.getItem().getEmail()));

        grid.setId("buffered-dynamic-editor");
        addCard("Grid Editor", "Dynamic Editor in Buffered Mode", message,
                validationStatus, grid);
    }

    private void createNotBufferedDynamicEditor() {
        Div message = new Div();
        message.setId("not-buffered-dynamic-editor-msg");

        Grid<Person> grid = new Grid<>();
        List<Person> persons = new ArrayList<>();
        persons.addAll(createItems());
        grid.setItems(persons);

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
        // Close the editor in case of backward navigation between components
        field.getElement()
                .addEventListener("keydown",
                        event -> grid.getEditor().closeEditor())
                .setFilter("event.key === 'Tab' && event.shiftKey");
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);
        // Close the editor in case of forward navigation between components
        checkbox.getElement().addEventListener("keydown", event -> {
            if (!checkbox.getValue()) {
                grid.getEditor().closeEditor();
            }
        }).setFilter("event.key === 'Tab' && !event.shiftKey");

        TextField emailField = new TextField();
        emailColumn.setEditorComponent(item -> {
            if (item.isSubscriber()) {
                binder.bind(emailField, "email");
                return emailField;
            } else {
                return null;
            }
        });
        // Close the editor in case of forward navigation between components
        emailField.getElement()
                .addEventListener("keydown",
                        event -> grid.getEditor().closeEditor())
                .setFilter("event.key === 'Tab' && !event.shiftKey");

        grid.addItemDoubleClickListener(event -> {
            grid.getEditor().editItem(event.getItem());
            field.focus();
        });

        // Re-validates the editors every time something changes on the Binder.
        // This is needed for the email column to turn into nothing when the
        // checkbox is deselected, for example.
        binder.addValueChangeListener(event -> {
            // Only updates from the client-side should be taken into account
            if (event.isFromClient()) {
                grid.getEditor().refresh();
            }
        });

        grid.addItemClickListener(event -> {
            if (binder.getBean() != null) {
                message.setText(binder.getBean().getFirstName() + ", "
                        + binder.getBean().isSubscriber() + ", "
                        + binder.getBean().getEmail());
            }
        });

        grid.setId("not-buffered-dynamic-editor");
        addCard("Grid Editor", "Dynamic Editor in Not Buffered Mode", message,
                grid);
    }
}
