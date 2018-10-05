package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudI18n;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route(value = "")
@Theme(Lumo.class)
@BodySize(height = "100vh", width = "100vw")
public class PersonView extends Div {

    public PersonView() {
        CrudEditor<Person> editor = createPersonEditor();

        Crud<Person> crud = new Crud<>(Person.class, editor);

        PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        dataProvider.setSizeChangeListener(count ->
                crud.setFooter(String.format("Eeyan %d wa", count)));

        crud.setDataProvider(dataProvider);

        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        crud.setI18n(createYorubaI18n());
        crud.getGrid().removeColumnByKey("id");

        crud.setHeight("100%");
        setHeight("100%");
        add(crud);
    }

    private CrudEditor<Person> createPersonEditor() {
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

    private CrudI18n createYorubaI18n() {
        CrudI18n yorubaI18n = CrudI18n.createDefault();

        yorubaI18n.setNewItem("Eeyan titun");
        yorubaI18n.setEditItem("S'atunko eeyan");
        yorubaI18n.setSave("Fi pamo");
        yorubaI18n.setCancel("Fa'gi lee");
        yorubaI18n.setDelete("Paare");

        yorubaI18n.getConfirm().getCancel().setHeader("Akosile");
        yorubaI18n.getConfirm().getCancel().setMessage("Akosile ti a o tii fi pamo nbe");
        yorubaI18n.getConfirm().getCancel().getButton().setCancel("Se atunko sii");
        yorubaI18n.getConfirm().getCancel().getButton().setOk("Fa'gi lee");

        yorubaI18n.getConfirm().getDelete().setHeader("Amudaju ipare");
        yorubaI18n.getConfirm().getDelete().setMessage("Se o da o l'oju pe o fe pa eeyan yi re? Igbese yi o l'ayipada o.");
        yorubaI18n.getConfirm().getDelete().getButton().setCancel("Da'wo duro");
        yorubaI18n.getConfirm().getDelete().getButton().setOk("Paare");

        return yorubaI18n;
    }
}
