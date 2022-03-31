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
package com.vaadin.flow.component.grid;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.ListDataProvider;

public class GridMultiSelectionIdOverrideTest {

    private Person AARON;
    private Person BORIS;

    private Grid<Person> grid;
    private GridMultiSelectionModel<Person> selectionModel;
    private AtomicReference<Set<Person>> currentSelectionCapture;
    private AtomicReference<Set<Person>> oldSelectionCapture;
    private AtomicInteger events;

    @Before
    public void setUp() {
        grid = new Grid<>();
        selectionModel = (GridMultiSelectionModel<Person>) grid
                .setSelectionMode(SelectionMode.MULTI);

        AARON = new Person("Aaron", "Archer");
        BORIS = new Person("Boris", "Birch");
        Set<Person> mockPersons = new HashSet<>();
        mockPersons.add(AARON);
        mockPersons.add(BORIS);

        
        grid.setDataProvider(new ListDataProvider<Person>(mockPersons) {
            @Override
            public Object getId(Person item) {
                return item.getId();
            }
        });

        currentSelectionCapture = new AtomicReference<>();
        oldSelectionCapture = new AtomicReference<>();
        events = new AtomicInteger();

        selectionModel.addMultiSelectionListener(event -> {
            currentSelectionCapture.set(new HashSet<>(event.getValue()));
            oldSelectionCapture.set(new HashSet<>(event.getOldSelection()));
            events.incrementAndGet();
        });
    }

    @Test
    public void selectEditDeselectWorks() {
        grid.select(AARON);
        assertEquals(1, events.get());
        AARON.setFirstName("Aaroni");
        grid.deselect(AARON);
        assertEquals(2, events.get());
    }

    private class Person {
        private String id;
        private String firstName;
        private String lastName;

        public Person() {
            // usually increment from DB, but for simplicity just use UUID
            this.id = UUID.randomUUID().toString();
        }

        public Person(String firstName, String lastName) {
            this();
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return id.equals(person.id) && Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, firstName, lastName);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
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
    }

}
