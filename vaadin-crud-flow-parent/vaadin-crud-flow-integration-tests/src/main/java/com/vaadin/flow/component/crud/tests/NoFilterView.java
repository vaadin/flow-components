/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import static com.vaadin.flow.component.crud.tests.Helper.createPersonEditor;

import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudGrid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-crud/nofilter")
public class NoFilterView extends VerticalLayout {

    public NoFilterView() {
        final Crud<Person> crud = new Crud<>(Person.class,
                new CrudGrid<>(Person.class, false), createPersonEditor());

        final PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        crud.setDataProvider(dataProvider);

        setHeight("100%");
        add(crud);
    }
}
