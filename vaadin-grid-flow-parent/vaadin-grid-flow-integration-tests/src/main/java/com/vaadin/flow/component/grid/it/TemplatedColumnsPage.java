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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.provider.BeanDataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Route("vaadin-grid/templated-columns")
@Tag("templated-columns")
// Order matters see https://github.com/vaadin/flow/issues/5591
@JsModule("src/templated-columns.js")
@JsModule("@vaadin/grid/src/vaadin-grid-column-group.js")
public class TemplatedColumnsPage extends PolymerTemplate<TemplateModel> {

    @Id
    private Grid<Person> grid;

    public TemplatedColumnsPage() {
        grid.setItems(DataProvider.fromCallbacks(this::fetchPeople,
                this::countPeople));
        grid.addDataGenerator(new BeanDataGenerator<>());
    }

    private Stream<Person> fetchPeople(Query<?, ?> query) {
        List<Person> people = new ArrayList<>(query.getLimit());
        for (int i = 0; i < query.getLimit(); i++) {
            people.add(fetchPerson(i + query.getOffset()));
        }
        return people.stream();
    }

    private Person fetchPerson(int index) {
        Person person = new Person();
        person.setName(new Name("Person" + (index + 1), index + "son"));
        person.setLocation(new Location("Street " + index, "State " + index,
                "Country " + index));
        return person;
    }

    private int countPeople(Query<?, ?> query) {
        return 500;
    }

    public static class Person {
        private Name name;
        private Location location;

        public Name getName() {
            return name;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    public static class Name {
        private String first;
        private String last;

        public Name() {
        }

        public Name(String first, String last) {
            this.first = first;
            this.last = last;
        }

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

    }

    public static class Location {
        private String street;
        private String city;
        private String state;

        public Location() {
        }

        public Location(String street, String city, String state) {
            this.street = street;
            this.city = city;
            this.state = state;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }

}
