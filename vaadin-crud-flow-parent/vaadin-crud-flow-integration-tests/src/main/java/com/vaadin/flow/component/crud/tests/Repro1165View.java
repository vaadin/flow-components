/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1165 — a
 * non-empty validator with a withValidationStatusHandler reportedly fires an
 * error every time CRUD clears the editor after save/cancel/delete, even for a
 * valid operation. Signal: the error counter increases after cancelling a
 * valid record.
 */
@Route("repro-1165")
public class Repro1165View extends Div {

    private int errorCount = 0;

    public Repro1165View() {
        Div status = new Div();
        status.setId("status");
        status.setText("errors: 0");

        Div lastEvent = new Div();
        lastEvent.setId("last-event");

        TextField firstName = new TextField("First name");
        TextField lastName = new TextField("Last name");
        FormLayout form = new FormLayout(firstName, lastName);

        Binder<Person> binder = new Binder<>(Person.class);
        binder.forField(firstName)
                .withValidator(name -> name != null && !name.isEmpty(),
                        "name cannot be empty")
                .withValidationStatusHandler(result -> {
                    if (result.isError()) {
                        errorCount++;
                        status.setText("errors: " + errorCount);
                    }
                }).bind(Person::getFirstName, Person::setFirstName);
        binder.forField(lastName).bind(Person::getLastName,
                Person::setLastName);

        CrudEditor<Person> editor = new BinderCrudEditor<>(binder, form);
        Crud<Person> crud = new Crud<>(Person.class, editor);
        crud.setId("crud");

        PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        crud.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        crud.addCancelListener(e -> lastEvent.setText("cancel"));
        crud.addSaveListener(e -> lastEvent.setText("save"));
        crud.addDeleteListener(e -> lastEvent.setText("delete"));

        // Open a VALID existing record (non-empty first name) server-side.
        Button editValid = new Button("Edit valid record",
                e -> crud.edit(new Person(1, "Sayo", "Oladeji"),
                        Crud.EditMode.EXISTING_ITEM));
        editValid.setId("edit-valid");

        // Reset the counter between trials without a server restart.
        Button reset = new Button("Reset counter", e -> {
            errorCount = 0;
            status.setText("errors: 0");
            lastEvent.setText("");
        });
        reset.setId("reset");

        add(crud, new Div(editValid, reset), status, lastEvent);
    }
}
