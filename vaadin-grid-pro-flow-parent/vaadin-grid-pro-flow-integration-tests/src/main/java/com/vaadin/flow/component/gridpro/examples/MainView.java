package com.vaadin.flow.component.gridpro.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {

    public MainView() {
        createEditorColumns();
        createBeanGridWithEditColumns();
    }

    protected void createEditorColumns() {
        Div itemDisplayPanel = new Div();
        Div subPropertyDisplayPanel = new Div();

        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        grid.addColumn(Person::getAge).setHeader("Age");

        grid.addEditColumn(Person::getName, "name").text((item, newValue) -> {
            item.setName(newValue);
            itemDisplayPanel.setText(item.toString());
            subPropertyDisplayPanel.setText(newValue);
        }).setHeader("Name").setWidth("300px");

        ComponentRenderer<Span, Person> booleanRenderer = new ComponentRenderer<>(person ->
            new Span(person.isSubscriber() ? "Yes" : "No")
        );
        grid.addEditColumn(Person::isSubscriber, booleanRenderer).checkbox((item, newValue) -> {
            item.setSubscriber(newValue);
            itemDisplayPanel.setText(item.toString());
            subPropertyDisplayPanel.setText(newValue.toString());
        }).setHeader("Subscriber").setWidth("300px");

        List<String> listOptions = new ArrayList<>();
        listOptions.add("Services");
        listOptions.add("Marketing");
        listOptions.add("Sales");
        grid.addEditColumn(Person::getDepartment).select((item, newValue) -> {
            item.setDepartment(fromStringRepresentation((newValue)));
            itemDisplayPanel.setText(item.toString());
            subPropertyDisplayPanel.setText(newValue);
        }, listOptions).setHeader("Department").setWidth("300px");

        add(grid, itemDisplayPanel, subPropertyDisplayPanel);
    }

    protected void createBeanGridWithEditColumns() {
        GridPro<Person> beanGrid = new GridPro<>(Person.class);
        beanGrid.setColumns();
        beanGrid.setItems(createItems());

        beanGrid.addEditColumn("name").text((item, newValue) -> item.setName(newValue));

        add(beanGrid);
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
        person.setEmail("person" + index + "@vaadin.com");
        person.setName("Person " + index);
        person.setAge(13 + random.nextInt(50));
        person.setDepartment(Department.getRandomDepartment());

        return person;
    }

    public static Department fromStringRepresentation(String stringRepresentation)
    {
        for(Department type : Department.values()) {
            if(type.getStringRepresentation().equals(stringRepresentation.toLowerCase())) {
               return type;
            }
        }

        return null;
    }
}
