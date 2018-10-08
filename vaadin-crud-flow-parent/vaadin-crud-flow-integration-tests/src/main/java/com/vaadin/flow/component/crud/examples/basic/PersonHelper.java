package com.vaadin.flow.component.crud.examples.basic;

import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.examples.Person;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

class PersonHelper {

    static CrudEditor<Person> createPersonEditor() {
        TextField firstName = new TextField("First name");
        TextField lastName = new TextField("Last name");
        FormLayout form = new FormLayout(firstName, lastName);

        Binder<Person> binder = new Binder<>(Person.class);
        binder.bind(firstName, Person::getFirstName, Person::setFirstName);
        binder
                .forField(lastName)
                .withValidator(
                        value -> value != null && value.startsWith("O"),
                        "Only last names starting with 'O' allowed")
                .bind(Person::getLastName, Person::setLastName);

        return new BinderCrudEditor<>(binder, form);
    }
}
