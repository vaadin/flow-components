/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route(value = "vaadin-grid/grid-editor-filtering")
public class GridEditorFilteringPage extends Div {

    private final Grid<Person> grid;
    private final TextField firstNameEditor;
    private final TextField lastNameEditor;
    private final ConfigurableFilterDataProvider<Person, Void, String> dp;

    private final PersonService samplePersonService = new PersonService();

    public GridEditorFilteringPage() {
        setSizeFull();

        grid = new Grid<>(Person.class, false);
        grid.addSelectionListener(
                e -> e.getFirstSelectedItem().ifPresent(this::editItem));

        CallbackDataProvider<Person, String> dataProvider = DataProvider
                .fromFilteringCallbacks(
                        query -> samplePersonService.fetch(query.getOffset(),
                                query.getLimit(), query.getFilter()).stream(),
                        query -> samplePersonService.count(query.getFilter()));
        dp = dataProvider.withConfigurableFilter();
        grid.setItems(dp);

        firstNameEditor = new TextField();
        firstNameEditor.setId("first-name-editor");
        grid.addColumn("firstName").setHeader("First Name").setAutoWidth(true)
                .setEditorComponent(firstNameEditor);
        lastNameEditor = new TextField();
        lastNameEditor.setId("last-name-editor");
        grid.addColumn("lastName").setHeader("Last Name").setAutoWidth(true)
                .setEditorComponent(lastNameEditor);

        grid.setSizeFull();

        TextField nameFilter = new TextField("Name");
        nameFilter.setPlaceholder("First or last name");
        nameFilter.setId("name-filter");

        Button searchBtn = new Button("Search");
        searchBtn.setId("search-button");
        searchBtn.addClickListener(e -> {
            dp.setFilter(nameFilter.getValue());
            grid.asSingleSelect().getOptionalValue().ifPresent(this::editItem);
        });

        Button addColumn = new Button("Add Column",
                e -> grid.addColumn(person -> "NEW"));
        addColumn.setId("add-column");

        add(addColumn, nameFilter, searchBtn, grid);
    }

    private void editItem(Person person) {
        grid.getEditor().editItem(person);
        firstNameEditor.setValue(person.getFirstName());
        lastNameEditor.setValue(person.getLastName());
    }

    public static class Person implements Cloneable {
        private int id;
        private String firstName;
        private String lastName;

        public Person(int id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Person other)) {
                return false;
            }
            return id == other.id;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", firstName, lastName);
        }

        @Override
        public Person clone() {
            try {
                return (Person) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(
                        "The Person object could not be cloned.", e);
            }
        }
    }

    static class PersonService {
        private final List<Person> people = IntStream.range(0, 100)
                .mapToObj(i -> new Person(i, "Name " + i, "Surname " + i))
                .toList();

        public List<Person> fetch(int offset, int limit,
                Optional<String> filter) {
            int end = offset + limit;
            int size = count(filter);
            if (size <= end) {
                end = size;
            }
            if (filter.isPresent() && !filter.get().isEmpty()) {
                return people.stream()
                        .filter(item -> item.toString().toLowerCase()
                                .contains(filter.get().toLowerCase()))
                        .collect(Collectors.toList()).subList(offset, end);
            } else {
                return people.subList(offset, end);
            }
        }

        public int count(Optional<String> filter) {
            if (filter.isPresent() && !filter.get().isEmpty()) {
                return (int) people.stream().filter(item -> item.toString()
                        .toLowerCase().contains(filter.get().toLowerCase()))
                        .count();
            } else {
                return people.size();
            }
        }
    }
}
