package com.vaadin.flow.component.crud.tests;

import static com.vaadin.flow.component.crud.tests.Helper.createPersonEditor;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-crud/new-button")
public class NewButtonView extends VerticalLayout {

    public NewButtonView() {
        createCrudWithNewButtonNull();
        createCrudWithNewButtonHidden();
    }

    private void createCrudWithNewButtonNull() {
        Crud<Person> crud = new Crud<>(Person.class, createPersonEditor());
        crud.setNewButton(null);
        crud.setId("crud-new-button-null");
        add(crud);
    }

    private void createCrudWithNewButtonHidden() {
        Crud<Person> crud = new Crud<>(Person.class, createPersonEditor());
        crud.getNewButton().setVisible(false);
        crud.setId("crud-new-button-hidden");
        add(crud);
    }
}
