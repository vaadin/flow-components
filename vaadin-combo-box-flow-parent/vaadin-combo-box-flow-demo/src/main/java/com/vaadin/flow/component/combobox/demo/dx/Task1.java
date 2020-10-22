package com.vaadin.flow.component.combobox.demo.dx;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.demo.entity.Person;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("dx-test-task1")
public class Task1 extends DemoView {

    private PersonService personService = new PersonService();

    @Override
    protected void initView() {
        ComboBox<Person> comboBox = new ComboBox<>();

        // TODO: show the persons from PersonService in combo box

        addCard("Task1", comboBox);
    }
}
