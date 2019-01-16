package com.vaadin.flow.component.gridpro.examples;

import com.vaadin.flow.component.gridpro.EditColumnConfigurator;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route
public class MainView extends Div {

    public MainView() {
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        grid.addColumn(Person::getName).setHeader("NAME");

        grid.addEditColumn(Person::getAge, EditColumnConfigurator.text((obj, string) -> {
            System.out.println(obj);
            System.out.println(string);
        })).setHeader("Age").setWidth("300px");

        grid.addEditColumn(Person::getEmail, EditColumnConfigurator.checkbox((obj, string) -> {
            System.out.println(obj);
            System.out.println(string);
        })).setHeader("Email").setWidth("300px");

        List<String> listOptions = new ArrayList<String>();
        listOptions.add("Sergey");
        listOptions.add("Tomi");
        listOptions.add("Manolo");
        listOptions.add("Yuriy");
        grid.addEditColumn(Person::getName, EditColumnConfigurator.select((obj, string) -> {
            System.out.println(obj);
            System.out.println(string);
        }, listOptions).setAllowEnterRowChange(true).setPreserveEditMode(true)).setHeader("Name Options").setWidth("300px");

        add(grid);
    }


    private static List<Person> createItems() {
        Random random = new Random(0);
        return IntStream.range(1, 500)
                .mapToObj(index -> createPerson(index, random))
                .collect(Collectors.toList());
    }

    private static Person createPerson(int index, Random random) {
        Person person = new Person();
        person.setId(index);
        person.setEmail("bla-bla@" + index);
        person.setName("Person " + index);
        person.setAge(13 + random.nextInt(50));

        return person;
    }
}
