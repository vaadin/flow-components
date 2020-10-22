package com.vaadin.flow.component.combobox.demo.dx;

import java.util.Collection;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.combobox.demo.data.PersonData;
import com.vaadin.flow.component.combobox.demo.entity.Person;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("dx-test-task4")
public class Task4 extends DemoView {

    @Override
    protected void initView() {
        ComboBox<Person> comboBox = new ComboBox<>();

        PersonData personData = new PersonData(1000);

        ComboBoxListDataView<Person> dataView = comboBox
                .setItems(personData.getPersons());

        Button exportButton = new Button("Export", click -> {

            // TODO: export the persons from combo box with a certain name.
            //  Use the filtering.

            export(null);
        });

        addCard("Task4", comboBox, exportButton);
    }

    private void export(Collection<Person> personsToExport) {
        Notification.show(
                String.format("Exported %d persons: %s", personsToExport.size(),
                        personsToExport.stream().map(Person::toString)
                                .collect(Collectors.joining(", "))),
                3000, Notification.Position.MIDDLE);
    }
}
