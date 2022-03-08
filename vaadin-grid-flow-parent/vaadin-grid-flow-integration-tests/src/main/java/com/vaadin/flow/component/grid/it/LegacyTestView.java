package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.bean.PersonWithLevel;

public class LegacyTestView extends Div {

    static final List<Person> items;
    static final List<PersonWithLevel> rootItems;
    static {
        items = createItems();
        rootItems = createRootItems();
    }

    public LegacyTestView() {
        addClassName("demo-view");
    }

    protected void addCard(String title, Component... components) {
        addCard(title, null, components);
    }

    protected void addCard(String title, String description,
            Component... components) {
        if (description != null) {
            title = title + ": " + description;
        }
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }

    protected List<Person> getItems() {
        return items.stream().map(Person::clone).collect(Collectors.toList());
    }

    protected List<PersonWithLevel> getRootItems() {
        return rootItems;
    }

    protected static List<Person> createItems() {
        return createItems(500);
    }

    protected static List<Person> createItems(int number) {
        return new PeopleGenerator().generatePeople(number);
    }

    private static List<PersonWithLevel> createRootItems() {
        return createSubItems(500, 0);
    }

    private static List<PersonWithLevel> createSubItems(int number, int level) {
        return new PeopleGenerator().generatePeopleWithLevels(number, level);
    }
}
