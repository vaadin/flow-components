/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.vaadincom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-pro")
public class GridProView extends DemoView {

    @Override
    protected void initView() {
        basicGridPro();
        sortingEditColumn();
        editorTypes();
        customRepresentation();
        customEditorType();
        enterNextRow();
        singleCellEdit();
        editOnClick();
    }

    private void basicGridPro() {
        // begin-source-example
        // source-example-heading: Basic Grid Pro
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * Grid Pro is an extension of the Grid and provides all the same
         * functionality on top of basic one. It is possible to use Grid's API
         * in Grid Pro.
         */
        grid.addColumn(Person::getName).setHeader("Name");

        /*
         * Lambda provided as a parameter for .text() method is a callback
         * function that will be called when item is changed.
         */
        grid.addEditColumn(Person::getEmail)
                .text((item, newValue) -> item.setEmail(newValue))
                .setHeader("Email (editable)");
        add(grid);
        // end-source-example

        addCard("Basic Grid Pro", grid);
    }

    private void sortingEditColumn() {
        // begin-source-example
        // source-example-heading: Sorting
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        grid.addEditColumn(Person::getName, "name")
                .text((item, newValue) -> item.setName(newValue))
                .setHeader("Name (editable)");

        grid.addEditColumn(Person::isSubscriber)
                .checkbox((item, newValue) -> item.setSubscriber(newValue))
                .setHeader("Subscriber (editable)");
        add(grid);
        // end-source-example

        addCard("Sorting", grid);
    }

    private void editorTypes() {
        // begin-source-example
        // source-example-heading: Editor Types
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * Using EditColumnConfigurator it is possible to define the type of the
         * editor: "text", "checkbox" or "select" and provide needed parameters.
         */
        grid.addEditColumn(Person::getName)
                .text((item, newValue) -> item.setName(newValue))
                .setHeader("Name (editable)");

        grid.addEditColumn(Person::isSubscriber)
                .checkbox((item, newValue) -> item.setSubscriber(newValue))
                .setHeader("Subscriber (editable)");

        List<String> optionsList = new ArrayList<>();
        optionsList.add("bla-bla@vaadin.com");
        optionsList.add("bla-bla@gmail.com");
        optionsList.add("super-mail@gmail.com");
        grid.addEditColumn(Person::getEmail)
                .select((item, newValue) -> item.setEmail(newValue),
                        optionsList)
                .setHeader("Email (editable)");
        add(grid);
        // end-source-example

        addCard("Editor Types", grid);
    }

    private void customRepresentation() {
        // begin-source-example
        // source-example-heading: Custom Representation
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        grid.addColumn(Person::getName).setHeader("Name");

        /*
         * Using ComponentRenderer to create a custom representation of the
         * boolean value.
         */
        ComponentRenderer<Span, Person> booleanRenderer = new ComponentRenderer<>(
                person -> new Span(person.isSubscriber() ? "Yes" : "No"));
        grid.addEditColumn(Person::isSubscriber, booleanRenderer)
                .checkbox((item, newValue) -> item.setSubscriber(newValue))
                .setHeader("Subscriber (editable)");
        add(grid);
        // end-source-example

        addCard("Custom Representation", grid);
    }

    private void customEditorType() {
        // begin-source-example
        // source-example-heading: Custom Editor Type
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        grid.addColumn(Person::getName).setHeader("Name");

        Input customInput = new Input();
        grid.addEditColumn(Person::getEmail)
                .custom(customInput,
                        (item, newValue) -> item.setEmail(newValue))
                .setHeader("Email").setWidth("300px");
        add(grid);
        // end-source-example

        addCard("Custom Editor Type", grid);
    }

    private void enterNextRow() {
        // begin-source-example
        // source-example-heading: Enter Next Row
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * It is possible to allow enter pressing change the row by using grid
         * pro method setEnterNextRow.
         */
        grid.setEnterNextRow(true);

        grid.addEditColumn(Person::getName)
                .text((item, newValue) -> item.setName(newValue))
                .setHeader("Name (editable)");

        grid.addEditColumn(Person::getEmail)
                .text((item, newValue) -> item.setEmail(newValue))
                .setHeader("Email (editable)");

        grid.addEditColumn(Person::isSubscriber)
                .checkbox((item, newValue) -> item.setSubscriber(newValue))
                .setHeader("Subscriber (editable)");
        add(grid);
        // end-source-example

        addCard("Enter Next Row", grid);
    }

    private void singleCellEdit() {
        // begin-source-example
        // source-example-heading: Single Cell Edit
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * It is possible to discard edit mode when moving to the next cell by
         * using grid pro method setSingleCellEdit.
         */
        grid.setSingleCellEdit(true);

        grid.addEditColumn(Person::getName)
                .text((item, newValue) -> item.setName(newValue))
                .setHeader("Name (editable)");

        grid.addEditColumn(Person::getEmail)
                .text((item, newValue) -> item.setEmail(newValue))
                .setHeader("Email (editable)");

        grid.addEditColumn(Person::isSubscriber)
                .checkbox((item, newValue) -> item.setSubscriber(newValue))
                .setHeader("Subscriber (editable)");
        add(grid);
        // end-source-example

        addCard("Single Cell Edit", grid);
    }

    private void editOnClick() {
        // begin-source-example
        // source-example-heading: Single Cell Edit
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * It is possible to discard edit mode when moving to the next cell by
         * using grid pro method setSingleCellEdit.
         */
        grid.setEditOnClick(true);

        grid.addEditColumn(Person::getName)
                .text((item, newValue) -> item.setName(newValue))
                .setHeader("Name (editable)");

        grid.addEditColumn(Person::getEmail)
                .text((item, newValue) -> item.setEmail(newValue))
                .setHeader("Email (editable)");

        grid.addEditColumn(Person::isSubscriber)
                .checkbox((item, newValue) -> item.setSubscriber(newValue))
                .setHeader("Subscriber (editable)");
        // end-source-example

        addCard("Edit on Click", grid);
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
}
