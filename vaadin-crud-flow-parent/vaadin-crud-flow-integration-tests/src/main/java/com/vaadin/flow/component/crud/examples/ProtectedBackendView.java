/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.ArrayList;
import java.util.List;

import static com.vaadin.flow.component.crud.examples.Helper.createPersonEditor;

@Route(value = "vaadin-crud/protectedbackend")
@Theme(Lumo.class)
@BodySize(height = "100vh", width = "100vw")
public class ProtectedBackendView extends VerticalLayout {

    public ProtectedBackendView() {
        final Crud<Person> crud = new Crud<>(Person.class,
                createPersonEditor());

        List<Person> data = new ArrayList<>();
        data.add(new Person(1, "Unmodifiable", "User"));
        data.add(new Person(2, "Another", "User"));

        final PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        dataProvider.setDatabase(data);

        crud.setDataProvider(dataProvider);

        crud.addDeleteListener(e -> {
            throw new RuntimeException("Forbidden to delete any user");
        });
        crud.addSaveListener(e -> {
            if (e.getItem().getLastName().length() <= 3) {
                throw new RuntimeException("User has to have longer name");
            }
            if (e.getItem().getId() != null && e.getItem().getId() == 1) {
                throw new RuntimeException(
                        "Forbidden to modify Unmodifiable user");
            }
            dataProvider.persist(e.getItem());
        });

        crud.addCancelListener(e -> {
            throw new RuntimeException("Exception happened during cancel");
        });

        setHeight("100%");
        add(crud);
    }

}
