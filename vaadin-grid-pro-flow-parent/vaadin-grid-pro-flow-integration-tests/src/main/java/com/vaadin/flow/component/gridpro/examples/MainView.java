package com.vaadin.flow.component.gridpro.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid-pro")
public class MainView extends VerticalLayout {

    public MainView() {
        createEditorColumns();
        createBeanGridWithEditColumns();
    }

    protected void createEditorColumns() {
        Div itemDisplayPanel = new Div();
        Div subPropertyDisplayPanel = new Div();
        subPropertyDisplayPanel.setId("prop-panel");

        Div eventsPanel = new Div();
        eventsPanel.setId("events-panel");

        GridPro<Person> grid = new GridPro<>();
        Button disableGrid = new Button("Disable Grid");
        disableGrid.setId("disable-grid-id");

        List<City> cityList = createCityItems();
        List<Person> personList = createItems();
        mapLists(personList, cityList);
        grid.setItems(personList);

        grid.addCellEditStartedListener(
                e -> eventsPanel.add(e.getItem().toString()));

        grid.addColumn(Person::getAge).setHeader("Age");

        grid.addEditColumn(Person::getName, "name").text((item, newValue) -> {
            item.setName(newValue);
            itemDisplayPanel.setText(item.toString());
            subPropertyDisplayPanel.setText(newValue);
        }).setHeader("Name").setWidth("300px");

        ComboBox<Department> cb = new ComboBox<>();
        cb.setItems(Department.values());
        grid.addEditColumn(Person::getDepartment)
                .custom(cb, (item, newValue) -> {
                    item.setDepartment(newValue);
                    itemDisplayPanel.setText(item.toString());
                    subPropertyDisplayPanel.setText(String.valueOf(newValue));
                }).setHeader("Department").setWidth("300px");

        ComponentRenderer<Span, Person> booleanRenderer = new ComponentRenderer<>(
                person -> new Span(person.isSubscriber() ? "Yes" : "No"));
        grid.addEditColumn(Person::isSubscriber, booleanRenderer)
                .checkbox((item, newValue) -> {
                    item.setSubscriber(newValue);
                    itemDisplayPanel.setText(item.toString());
                    subPropertyDisplayPanel.setText(newValue.toString());
                }).setHeader("Subscriber").setWidth("300px");

        ComboBox<City> cityCb = new ComboBox<>();
        cityCb.setItems(cityList);
        cityCb.setItemLabelGenerator(City::getName);

        ComponentRenderer<Span, Person> cityRenderer = new ComponentRenderer<>(
                person -> {
                    if (person.getCity() != null) {
                        return new Span(person.getCity().getName());
                    } else {
                        return new Span("");
                    }
                });

        grid.addEditColumn(Person::getCity, cityRenderer)
                .custom(cityCb, (item, newValue) -> {
                    item.setCity(newValue);
                    newValue.setPerson(item);
                    itemDisplayPanel.setText(item.toString());
                    subPropertyDisplayPanel.setText(newValue.toString());
                }).setHeader("City").setWidth("300px");

        Input customField = new Input();
        grid.addEditColumn(Person::getEmail)
                .custom(customField,
                        (item, newValue) -> item.setEmail(newValue))
                .setHeader("Email").setWidth("300px");

        disableGrid.addClickListener(click -> grid.setEnabled(false));

        add(grid, itemDisplayPanel, subPropertyDisplayPanel, eventsPanel,
                disableGrid);
    }

    protected void createBeanGridWithEditColumns() {
        GridPro<Person> beanGrid = new GridPro<>(Person.class);
        beanGrid.setColumns();
        beanGrid.setItems(createItems());

        beanGrid.addEditColumn("age").text(
                (item, newValue) -> item.setAge(Integer.valueOf(newValue)));

        TextField textField = new TextField();
        beanGrid.addEditColumn("name").custom(textField,
                (item, newValue) -> item.setName(newValue));

        List<String> listOptions = new ArrayList<>();
        listOptions.add("Services");
        listOptions.add("Marketing");
        listOptions.add("Sales");
        beanGrid.addEditColumn("department").select((item, newValue) -> {
            item.setDepartment(fromStringRepresentation((newValue)));
        }, listOptions).setHeader("Department").setWidth("300px");

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

        if (index == 1) {
            person.setDepartment(Department.SALES);
        } else {
            person.setDepartment(Department.getRandomDepartment());
        }

        return person;
    }

    private static List<City> createCityItems() {
        return IntStream.range(1, 500).mapToObj(index -> createCity(index))
                .collect(Collectors.toList());
    }

    private static City createCity(int index) {
        City city = new City();
        city.setId(index);
        city.setName("City " + index);

        return city;
    }

    private static void mapLists(List<Person> personList, List<City> cityList) {
        IntStream.range(0, personList.size()).forEach(index -> {
            Person person = personList.get(index);
            City city = cityList.get(index);
            person.setCity(city);
            city.setPerson(person);
        });
    }

    public static Department fromStringRepresentation(
            String stringRepresentation) {
        for (Department type : Department.values()) {
            if (type.getStringRepresentation()
                    .equals(stringRepresentation.toLowerCase(Locale.ENGLISH))) {
                return type;
            }
        }

        return null;
    }
}
