package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Route(value = "vaadin-crud/crudintemplate")
@Tag("crud-app")
@HtmlImport("frontend://src/crud-in-template.html")
@JsModule("./src/crud-in-template.js")
public class CrudInTemplate extends PolymerTemplate<TemplateModel> {

    @Id
    private Crud<Person> crud;

    @Id
    private TextField firstName;

    @Id
    private TextField lastName;

    @Id
    private VerticalLayout events;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        Binder<Person> binder = new Binder<>(Person.class);
        binder.bind(firstName, Person::getFirstName, Person::setFirstName);
        binder.bind(lastName, Person::getLastName, Person::setLastName);

        crud.setEditor(new BinderCrudEditor<>(binder));
        crud.setBeanType(Person.class);

        PersonCrudDataProvider provider = new PersonCrudDataProvider();
        crud.setDataProvider(provider);

        crud.addNewListener(e -> addEvent("New: " + e.getItem()));
        crud.addEditListener(e -> addEvent("Edit: " + e.getItem()));
        crud.addSaveListener(e -> provider.persist(e.getItem()));
    }

    private void addEvent(String event) {
        events.add(new Span(event));
    }
}
