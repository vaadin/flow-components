package com.vaadin.flow.component.gridpro.vaadincom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.gridpro.GridPro;
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
        enterNextRow();
        keepEditorOpen();
    }

    private void basicGridPro() {
        // begin-source-example
        // source-example-heading: Basic Grid Pro
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * Grid Pro is an extension of the Grid and provides all
         * the same functionality on top of basic one.
         * It is possible to use Grid's API in Grid Pro.
         */
        grid.addColumn(Person::getName).setHeader("NAME");

        /*
         * Lambda provided as a parameter for .text() method is a callback function that will
         * be called when item is changed.
         */
        grid.addEditColumn(Person::getEmail)
                .text((item, newValue) -> {})
                .setHeader("Email (editable)");
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
                .checkbox((item, newValue) -> {})
                .setHeader("Subscriber (editable)");
        // end-source-example

        addCard("Sorting", grid);
    }

    private void editorTypes() {
        // begin-source-example
        // source-example-heading: Editor Types
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * Using EditColumnConfigurator it is possible to define the type of the editor:
         * "text", "checkbox" or "select" and provide needed parameters.
         */
        grid.addEditColumn(Person::getName)
                .text((item, newValue) -> {})
                .setHeader("Name (editable)");

        grid.addEditColumn(Person::isSubscriber)
                .checkbox((item, newValue) -> {})
                .setHeader("Subscriber (editable)");

        List<String> optionsList = new ArrayList<>();
        optionsList.add("bla-bla@vaadin.com");
        optionsList.add("bla-bla@gmail.com");
        optionsList.add("super-mail@gmail.com");
        grid.addEditColumn(Person::getEmail)
                .select((item, newValue) -> {}, optionsList)
                .setHeader("Email (editable)");
        // end-source-example

        addCard("Editor Types", grid);
    }

    private void customRepresentation() {
        // begin-source-example
        // source-example-heading: Custom Representation
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        grid.addColumn(Person::getName).setHeader("NAME");

        /*
         * Using ComponentRenderer to create a custom representation of the boolean value.
         */
        ComponentRenderer<Span, Person> booleanRenderer = new ComponentRenderer<>(person ->
            new Span(person.isSubscriber() ? "Yes" : "No")
        );
        grid.addComponentEditColumn(Person::isSubscriber, booleanRenderer)
                .checkbox((item, newValue) -> {})
                .setHeader("Subscriber (editable)");
        // end-source-example

        addCard("Custom Representation", grid);
    }

    private void enterNextRow() {
        // begin-source-example
        // source-example-heading: Enter Next Row
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * It is possible to allow enter pressing change the row by using grid pro method setEnterNextRow.
         */
        grid.setEnterNextRow(true);
        grid.addEditColumn(Person::getName)
                .text((item, newValue) -> {})
                .setHeader("Name (editable)");
        // end-source-example

        addCard("Enter Next Row", grid);
    }

    private void keepEditorOpen() {
        // begin-source-example
        // source-example-heading: Keep Editor Open
        GridPro<Person> grid = new GridPro<>();
        grid.setItems(createItems());

        /*
         * It is possible to preserve edit mode when moving to the next cell by using grid pro method setKeepEditorOpen.
         */
        grid.setKeepEditorOpen(true);
        grid.addEditColumn(Person::getEmail)
                .text((item, newValue) -> {})
                .setHeader("Email (editable)");
        // end-source-example

        addCard("Keep Editor Open", grid);
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
