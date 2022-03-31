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

import com.vaadin.flow.data.selection.SelectionListener;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.data.provider.ListDataProvider;
import org.mockito.Mockito;

public class GridMultiSelectionWithIdProviderTest {

    private Person AARON;

    private Grid<Person> grid;
    private SelectionListener<Grid<Person>, Person> selectionListenerMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        AARON = new Person(1, "Aaron", "Archer");
        Set<Person> mockPersons = new HashSet<>();
        mockPersons.add(AARON);
        mockPersons.add(new Person(2, "Boris", "Birch"));

        grid = new Grid<>();
        grid.setItems(new ListDataProvider<>(mockPersons) {
            @Override
            public Object getId(Person item) {
                return item.getId();
            }
        });

        GridSelectionModel<Person> selectionModel = grid
                .setSelectionMode(Grid.SelectionMode.MULTI);
        selectionListenerMock = Mockito.mock(SelectionListener.class);
        selectionModel.addSelectionListener(selectionListenerMock);
    }

    @Test
    public void select_changeHashCode_deselect_nothingSelected() {
        grid.select(AARON);
        assertEquals(1, grid.getSelectedItems().size());
        Mockito.verify(selectionListenerMock, Mockito.times(1))
                .selectionChange(Mockito.any());

        AARON.setFirstName("Aaroni");
        grid.deselect(AARON);
        assertEquals(0, grid.getSelectedItems().size());
        Mockito.verify(selectionListenerMock, Mockito.times(2))
                .selectionChange(Mockito.any());
    }

    @Test
    public void selectFromClient_changeHashCode_deselectFromClient_nothingSelected() {
        grid.getSelectionModel().selectFromClient(AARON);
        assertEquals(1, grid.getSelectedItems().size());
        Mockito.verify(selectionListenerMock, Mockito.times(1))
                .selectionChange(Mockito.any());

        AARON.setFirstName("Aaroni");
        grid.getSelectionModel().deselectFromClient(AARON);
        assertEquals(0, grid.getSelectedItems().size());
        Mockito.verify(selectionListenerMock, Mockito.times(2))
                .selectionChange(Mockito.any());
    }

    @Test
    public void select_selectDuplicateWithDifferentHashCode_oneItemSelected() {
        Person aaronDuplicate = new Person(AARON.id, "aaron", "archer");

        grid.select(AARON);
        assertEquals(1, grid.getSelectedItems().size());
        Mockito.verify(selectionListenerMock, Mockito.times(1))
                .selectionChange(Mockito.any());

        grid.select(aaronDuplicate);
        assertEquals(1, grid.getSelectedItems().size());
        Mockito.verify(selectionListenerMock, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @Test
    public void selectFromClient_selectDuplicateWithDifferentHashCode_oneItemSelected() {
        Person aaronDuplicate = new Person(AARON.id, "aaron", "archer");

        grid.getSelectionModel().selectFromClient(AARON);
        assertEquals(1, grid.getSelectedItems().size());
        Mockito.verify(selectionListenerMock, Mockito.times(1))
                .selectionChange(Mockito.any());

        grid.getSelectionModel().selectFromClient(aaronDuplicate);
        assertEquals(1, grid.getSelectedItems().size());
        Mockito.verify(selectionListenerMock, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    private static class Person {
        private long id;
        private String firstName;
        private String lastName;

        public Person(long id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        // equals and hashCode are intentionally implemented differently from
        // the identifier getter for the data provider. We want to make sure
        // that the selection model uses the data provider identity, rather than
        // the equals implementation
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Person person = (Person) o;
            return Objects.equals(id, person.id)
                    && Objects.equals(firstName, person.firstName)
                    && Objects.equals(lastName, person.lastName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, firstName, lastName);
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
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
