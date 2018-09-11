package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;

public class PersonCrudEditor implements CrudEditor<Person> {

    private final TextField firstNameField = new TextField("First name");
    private final TextField lastNameField = new TextField("Last name");

    private final VerticalLayout view = new VerticalLayout();

    private Person editableItem;
    private Binder<Person> binder;

    PersonCrudEditor() {
        view.add(firstNameField, lastNameField);
        view.setPadding(false);
    }

    @Override
    public Person getItem() {
        return editableItem;
    }

    @Override
    public void setItem(Person item) {
        // TODO(oluwasayo): Remove when WC no longer fires edit event on grid active item change
        if (item == null) {
            clear();
            return;
        }

        binder = new Binder<>(Person.class);
        binder.bind(firstNameField, Person::getFirstName, Person::setFirstName);
        binder.bind(lastNameField, Person::getLastName, Person::setLastName);

        editableItem = copyOf(item);
        binder.setBean(editableItem);
    }

    private Person copyOf(Person item) {
        final Person copy = new Person();
        copy.setId(item.getId());
        copy.setFirstName(item.getFirstName());
        copy.setLastName(item.getLastName());
        return copy;
    }

    @Override
    public void clear() {
        if (binder != null) {
            binder.removeBinding("firstName");
            binder.removeBinding("lastName");
            binder.removeBean();
            binder = null;
        }

        editableItem = null;

        firstNameField.clear();
        lastNameField.clear();
    }

    @Override
    public boolean isValid() {
        return binder != null && binder.isValid();
    }

    @Override
    public boolean isDirty() {
        return binder != null && binder.hasChanges();
    }

    @Override
    public Element getView() {
        return view.getElement();
    }
}
