/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/using-components")
public class GridViewUsingComponentsPage extends LegacyTestView {

    public GridViewUsingComponentsPage() {
        createColumnComponentRenderer();
    }

    private void createColumnComponentRenderer() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(createItems());

        // Use the component constructor that accepts an item ->
        // new PersonComponent(Person person)
        grid.addComponentColumn(PersonComponent::new).setHeader("Person");

        // Or you can use an ordinary function to setup the component
        grid.addComponentColumn(item -> new NativeButton("Remove", evt -> {
            ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                    .getDataProvider();
            dataProvider.getItems().remove(item);
            dataProvider.refreshAll();
        })).setHeader("Actions");

        // Item details can also use components
        grid.setItemDetailsRenderer(new ComponentRenderer<>(PersonCard::new));

        // When items are updated, new components are generated
        TextField idField = new TextField("", "Person id");
        TextField nameField = new TextField("", "New name");

        NativeButton updateButton = new NativeButton("Update person", event -> {
            String id = idField.getValue();
            String name = nameField.getValue();
            ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                    .getDataProvider();

            dataProvider.getItems().stream()
                    .filter(person -> String.valueOf(person.getId()).equals(id))
                    .findFirst().ifPresent(person -> {
                        person.setFirstName(name);
                        dataProvider.refreshItem(person);
                    });

        });

        grid.setSelectionMode(SelectionMode.NONE);

        grid.setId("component-renderer");
        idField.setId("component-renderer-id-field");
        nameField.setId("component-renderer-name-field");
        updateButton.setId("component-renderer-update-button");
        addCard("Using components",
                "Grid with columns using component renderer", grid, idField,
                nameField, updateButton);
    }

    /**
     * Component used for the cell rendering.
     */
    public static class PersonComponent extends Div {

        private String text;
        private int timesClicked;

        /**
         * Creates a new component with the given item.
         *
         * @param person
         *            the person to set
         */
        public PersonComponent(Person person) {
            this.addClickListener(event -> {
                timesClicked++;
                setText(text + "\nClicked " + timesClicked);
            });
            setPerson(person);
        }

        /**
         * Sets the person for the component.
         *
         * @param person
         *            the person to be inside inside the cell
         */
        public void setPerson(Person person) {
            text = "Hi, I'm " + person.getFirstName() + "!";
            setText(text);
        }

        @Override
        public int hashCode() {
            return text == null ? 0 : text.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PersonComponent)) {
                return false;
            }
            PersonComponent other = (PersonComponent) obj;
            if (text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!text.equals(other.text)) {
                return false;
            }
            return true;
        }
    }

    /**
     * Component used for the details row.
     */
    public static class PersonCard extends Div {

        /**
         * Constructor that takes a Person as parameter.
         *
         * @param person
         *            the person to be used inside the card
         */
        public PersonCard(Person person) {
            addClassName("custom-details");
            setId("person-card-" + person.getId());

            VerticalLayout layout1 = new VerticalLayout();
            layout1.add(new Span("Name: " + person.getFirstName()));
            layout1.add(new Span("Id: " + person.getId()));
            layout1.add(new Span("Age: " + person.getAge()));

            VerticalLayout layout2 = new VerticalLayout();
            layout2.add(new Span("Street: " + person.getAddress().getStreet()));
            layout2.add(new Span(
                    "Address number: " + person.getAddress().getNumber()));
            layout2.add(new Span(
                    "Postal Code: " + person.getAddress().getPostalCode()));

            HorizontalLayout hlayout = new HorizontalLayout(layout1, layout2);
            hlayout.getStyle().set("border", "1px solid gray")
                    .set("padding", "10px").set("boxSizing", "border-box")
                    .set("width", "100%");

            add(hlayout);
        }
    }
}
