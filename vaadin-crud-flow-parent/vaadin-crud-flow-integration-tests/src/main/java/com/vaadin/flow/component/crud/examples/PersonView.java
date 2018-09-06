package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route(value = "")
@Theme(Lumo.class)
@BodySize(height = "100vh", width = "100vw")
public class PersonView extends VerticalLayout {

    public PersonView() {
        final Crud<Person> crud = new Crud<>(Person.class, new PersonCrudEditor());

        final PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        crud.setDataProvider(dataProvider);

        crud.addSaveListener(e -> dataProvider.persist(crud.getEditor().getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(crud.getEditor().getItem()));

        add(crud);
    }
}
