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

        // TODO: create the items fetch callback and set it to combo box

        // TODO: Use PersonService::getPersons to fetch the persons

        // TODO: Note that the PersonService uses Person entity as a filter type

        addCard("Task1", comboBox);
    }
}
