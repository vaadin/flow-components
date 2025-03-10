/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-crud/crudintemplate")
@Tag("crud-app")
@JsModule("./src/crud-in-template.js")
public class CrudInTemplate extends LitTemplate {

    @Id
    private Crud<Person> crud;

    @Id
    private TextField firstName;

    @Id
    private TextField lastName;

    @Id
    private VerticalLayout events;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        Binder<Person> binder = new Binder<>(Person.class);
        binder.bind(firstName, Person::getFirstName, Person::setFirstName);
        binder.bind(lastName, Person::getLastName, Person::setLastName);

        crud.setEditor(new BinderCrudEditor<>(binder));
        crud.setBeanType(Person.class);

        PersonCrudDataProvider provider = new PersonCrudDataProvider();
        crud.setDataProvider(provider);

        crud.addNewListener(e -> addEvent("New: " + e.getItem()));
        crud.addEditListener(e -> addEvent("Edit: " + e.getItem()));
        crud.addSaveListener(e -> provider.persist(e.getItem()));
    }

    private void addEvent(String event) {
        events.add(new Span(event));
    }
}
