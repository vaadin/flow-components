package com.vaadin.flow.component.crud.tests;

import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import static com.vaadin.flow.component.crud.tests.Helper.createPersonEditor;

import java.util.ArrayList;
import java.util.List;

@Route(value = "vaadin-crud/protectedbackend")
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
