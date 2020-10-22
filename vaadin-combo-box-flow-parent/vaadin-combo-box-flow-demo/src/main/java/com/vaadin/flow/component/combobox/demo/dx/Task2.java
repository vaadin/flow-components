package com.vaadin.flow.component.combobox.demo.dx;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.demo.entity.Person;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("dx-test-task2")
public class Task2 extends DemoView {

    @Override
    protected void initView() {
        ComboBox<Person> comboBox = new ComboBox<>();

        PersonDataProvider dataProvider = new PersonDataProvider();

        // TODO: use the existing PersonDataProvider to populate combo box
        //  with the items

        addCard("Task2", comboBox);
    }

}
