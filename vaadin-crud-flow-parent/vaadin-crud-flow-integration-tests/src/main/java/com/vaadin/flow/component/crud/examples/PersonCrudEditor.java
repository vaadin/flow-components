package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;

public class PersonCrudEditor implements CrudEditor<Person> {

    private final TextField nameField = new TextField("Name");
    private final TextField ageField = new TextField("Age");

    private final VerticalLayout view = new VerticalLayout();

    private Person workingCopy;
    private Binder<Person> binder;

    PersonCrudEditor() {
        ageField.setMaxLength(2);
        ageField.setPattern("\\d+");
        ageField.setPreventInvalidInput(true);

        view.add(new H2("Edit Person"), new Hr(), nameField, ageField);
    }

    @Override
    public Person getWorkingCopy() {
        return workingCopy;
    }

    @Override
    public void createWorkingCopyFrom(Person item) {
        // TODO(oluwasayo): Remove when WC no longer fires edit event on grid active item change
        if (item == null) {
            clear();
            return;
        }

        workingCopy = copyOf(item);

        binder = new Binder<>(Person.class);
        binder.setBean(workingCopy);
        binder.bind(nameField, Person::getName, Person::setName);
        binder.forField(ageField)
                .withConverter(
                        string -> string.isEmpty() ? null : Integer.parseInt(string),
                        number -> number == null ? "" : Integer.toString(number))
                .bind(Person::getId, Person::setId);
    }

    private Person copyOf(Person item) {
        final Person copy = new Person();
        copy.setId(item.getId());
        copy.setName(item.getName());
        return copy;
    }

    @Override
    public void clear() {
        if (binder != null) {
            binder.removeBinding("id");
            binder.removeBinding("name");
            binder.removeBean();
            binder = null;
        }

        workingCopy = null;

        nameField.clear();
        ageField.clear();
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
