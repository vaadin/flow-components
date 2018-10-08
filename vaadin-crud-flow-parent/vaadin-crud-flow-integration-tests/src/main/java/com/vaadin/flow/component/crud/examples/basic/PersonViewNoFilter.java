package com.vaadin.flow.component.crud.examples.basic;

import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudGrid;
import com.vaadin.flow.component.crud.examples.Person;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import static com.vaadin.flow.component.crud.examples.basic.PersonHelper.createPersonEditor;

@Route(value = "NoFilter")
@Theme(Lumo.class)
@BodySize(height = "100vh", width = "100vw")
public class PersonViewNoFilter extends VerticalLayout {

    public PersonViewNoFilter() {
        final Crud<Person> crud = new Crud<>(Person.class,
                new CrudGrid<>(Person.class, false), createPersonEditor());

        final PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        crud.setDataProvider(dataProvider);

        add(crud);
    }
}
