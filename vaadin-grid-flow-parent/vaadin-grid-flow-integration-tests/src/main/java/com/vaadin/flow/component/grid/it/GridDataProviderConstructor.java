/*
 * Copyright 2000-2023 Vaadin Ltd.
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Test view for GridDataProviderConstructorIT displaying 5 Grids using each one a different constructor.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/grid-data-provider-constructor")
public class GridDataProviderConstructor extends Div {

    transient List<Person> people = Arrays.asList(
            new Person(1, "Mik", "Gir", "mik@gmail.com",
                    LocalDate.now(ZoneId.systemDefault()).minusDays(10000), "Dev"),
            new Person(2, "Mak", "Gar", "mak@gmail.com",
                    LocalDate.now(ZoneId.systemDefault()).minusDays(20000), "Dev"),
            new Person(3, "Muk", "Gur", "muk@gmail.com",
                    LocalDate.now(ZoneId.systemDefault()).minusDays(30000), "Dev"),
            new Person(4, "Mok", "Gor", "mok@gmail.com",
                    LocalDate.now(ZoneId.systemDefault()).minusDays(40000), "Dev"));

    public GridDataProviderConstructor() {
        Grid<Person> gridBackendDataProvider = createGrid(
                new PersonBackendDataProvider());
        gridBackendDataProvider.setId("gridBackendDataProvider");
        add(gridBackendDataProvider);

        Grid<Person> gridGenericDataProvider = createGrid(
                new PersonGenericDataProvider());
        gridGenericDataProvider.setId("gridGenericDataProvider");
        add(gridGenericDataProvider);

        Grid<Person> gridInMemoryDataProvider = createGrid(
                new PersonInMemoryDataProvider());
        gridInMemoryDataProvider.setId("gridInMemoryDataProvider");
        add(gridInMemoryDataProvider);

        Grid<Person> gridListDataProvider = createGrid(
                new PersonListDataProvider());
        gridListDataProvider.setId("gridListDataProvider");
        add(gridListDataProvider);

        Grid<Person> gridCollection = createGrid(people);
        gridCollection.setId("gridCollection");
        add(gridCollection);
    }

    private final Grid<Person> createGrid(DataProvider<?, ?> dataProvider) {
        Grid<Person> grid = new Grid<>(dataProvider);
        return createColumns(grid);
    }

    private final Grid<Person> createGrid(Collection<Person> items) {
        Grid<Person> grid = new Grid<>(items);
        return createColumns(grid);
    }

    private final Grid<Person> createColumns(Grid<Person> grid){
        grid.addColumn(Person::getFirstName).setHeader("First name")
                .setKey("first");
        grid.addColumn(Person::getLastName).setHeader("Last name")
                .setKey("last");
        grid.addColumn(Person::getEmail).setHeader("Email").setKey("email");
        grid.addColumn(Person::getProfession).setHeader("Profession")
                .setKey("profession");
        grid.addColumn(new LocalDateRenderer<>(Person::getBirthday,
                        () -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                .setHeader("Date Renderer").setKey("date");
        grid.addColumn(
                        new LocalDateRenderer<>(Person::getBirthday, "dd/MM/yyyy"))
                .setHeader("String Renderer").setKey("renderer");
        return grid;
    }

    public final static class Person {

        private String firstName;

        private String lastName;

        private String email;

        private LocalDate birthday;

        private Integer id;

        private String profession;

        public Person(Integer id, String firstName, String lastName,
                String email, LocalDate birthday, String profession) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.birthday = birthday;
            this.id = id;
            this.profession = profession;
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getProfession() {
            return profession;
        }

        public void setProfession(String profession) {
            this.profession = profession;
        }
    }

    private final class PersonBackendDataProvider
            implements BackEndDataProvider<Person, Void> {

        @Override
        public void setSortOrders(List<QuerySortOrder> list) {

        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<Person, Void> query) {
            return 4;
        }

        @Override
        public Stream<Person> fetch(Query<Person, Void> query) {
            query.getPage();
            query.getPageSize();
            return people.stream();
        }

        @Override
        public void refreshItem(Person person) {

        }

        @Override
        public void refreshAll() {

        }

        @Override
        public Registration addDataProviderListener(
                DataProviderListener<Person> dataProviderListener) {
            return null;
        }
    }

    private final class PersonListDataProvider extends ListDataProvider<Person> {

        public PersonListDataProvider() {
            super(people);
        }

    }

    private final class PersonInMemoryDataProvider
            implements InMemoryDataProvider<Person> {

        @Override
        public SerializablePredicate<Person> getFilter() {
            return null;
        }

        @Override
        public void setFilter(
                SerializablePredicate<Person> serializablePredicate) {

        }

        @Override
        public SerializableComparator<Person> getSortComparator() {
            return null;
        }

        @Override
        public void setSortComparator(
                SerializableComparator<Person> serializableComparator) {

        }

        @Override
        public int size(Query<Person, SerializablePredicate<Person>> query) {
            return 4;
        }

        @Override
        public Stream<Person> fetch(
                Query<Person, SerializablePredicate<Person>> query) {
            query.getPage();
            query.getPageSize();
            return people.stream();
        }

        @Override
        public void refreshItem(Person person) {

        }

        @Override
        public void refreshAll() {

        }

        @Override
        public Registration addDataProviderListener(
                DataProviderListener<Person> dataProviderListener) {
            return null;
        }
    }

    private final class PersonGenericDataProvider
            implements DataProvider<Person, Void> {

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<Person, Void> query) {
            return 4;
        }

        @Override
        public Stream<Person> fetch(Query<Person, Void> query) {
            query.getPage();
            query.getPageSize();
            return people.stream();
        }

        @Override
        public void refreshItem(Person person) {

        }

        @Override
        public void refreshAll() {

        }

        @Override
        public Registration addDataProviderListener(
                DataProviderListener<Person> dataProviderListener) {
            return null;
        }
    }
}
