package com.vaadin.flow.component.crud.examples.basic;

import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.examples.Person;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import static com.vaadin.flow.component.crud.examples.basic.PersonHelper.createPersonEditor;

@Route(value = "")
@Theme(Lumo.class)
@BodySize(height = "100vh", width = "100vw")
public class PersonViewWithEventHandling extends Div {

    public PersonViewWithEventHandling() {
        CrudEditor<Person> editor = createPersonEditor();

        Crud<Person> crud = new Crud<>(Person.class, editor);

        PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        dataProvider.setSizeChangeListener(count ->
                crud.setFooter(String.format("%d items available", count)));

        crud.setDataProvider(dataProvider);

        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        crud.setHeight("100%");
        setHeight("100%");
        add(crud);
    }


}
