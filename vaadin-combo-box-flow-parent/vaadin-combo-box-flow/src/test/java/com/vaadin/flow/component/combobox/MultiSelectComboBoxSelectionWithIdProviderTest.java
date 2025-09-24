/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelectionListener;

public class MultiSelectComboBoxSelectionWithIdProviderTest {

    MultiSelectComboBox<Person> comboBox;
    private MultiSelectionListener<MultiSelectComboBox<Person>, Person> selectionListenerSpy;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        comboBox = new MultiSelectComboBox<>();
        List<Person> items = List.of(new Person(1, "Magnus"),
                new Person(2, "Amelia"), new Person(3, "Adelaide"));
        comboBox.setItems(new TestDataProvider(items));

        selectionListenerSpy = Mockito.mock(MultiSelectionListener.class);
        comboBox.addSelectionListener(selectionListenerSpy);
    }

    @Test
    public void isSelected_usesDataProviderIdentity() {
        // Select "Amelia"
        comboBox.select(new Person(2, "Amelia"));

        // Check for "amelia", but with same ID
        Assert.assertTrue(comboBox.isSelected(new Person(2, "amelia")));
    }

    @Test
    public void select_selectDuplicate_noChanges() {
        // Select "Amelia"
        comboBox.select(new Person(2, "Amelia"));
        // Select "amelia", but with same ID
        comboBox.select(new Person(2, "amelia"));

        // One item selected
        Assert.assertEquals(1, comboBox.getSelectedItems().size());
        // Change listener only triggered once
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @Test
    public void selectMultiple_ignoresDuplicates() {
        // Select 3 duplicates of the same person
        comboBox.select(new Person(2, "Amelia"), new Person(2, "amelia"),
                new Person(2, "amelai"));

        // One item selected
        Assert.assertEquals(1, comboBox.getSelectedItems().size());
    }

    @Test
    public void select_changeHashCode_deselect_nothingSelected() {
        // Select "Amelia"
        Person bean = new Person(2, "Amelia");
        comboBox.select(bean);
        // Modify hashcode
        bean.name = "amelia";
        // Deselect item with modified hashcode
        comboBox.deselect(bean);

        // Should be deselected
        Assert.assertEquals(0, comboBox.getSelectedItems().size());
    }

    @Test
    public void setValue_setValueWithDuplicates_noChanges() {
        // Set value
        comboBox.setValue(
                Set.of(new Person(1, "Magnus"), new Person(2, "Amelia")));
        // Set same value with duplicates that have different hashcode
        comboBox.setValue(
                Set.of(new Person(1, "magnus"), new Person(2, "amelia")));

        // Change listener only triggered once
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @Test
    public void selectAllWithDuplicates_deselectAll_nothingSelected() {
        // Select duplicates with different hashcode than the data provider
        // items
        comboBox.select(new Person(1, "magnus"));
        comboBox.select(new Person(2, "amelia"));
        comboBox.select(new Person(3, "adelaide"));
        // Deselect all
        comboBox.deselectAll();

        // Should have empty selection
        Assert.assertEquals(0, comboBox.getSelectedItems().size());
    }

    @Test
    public void selectItem_changeIdentityProvider_itemStillSelected() {
        // Select Magnus, identified by ID 1
        comboBox.select(new Person(1, "Magnus"));
        // Change identifier provider to identify person by name
        comboBox.getGenericDataView().setIdentifierProvider(Person::getName);
        // Check if person is now identified by name, while passing a different
        // ID
        Assert.assertTrue(comboBox.isSelected(new Person(2, "Magnus")));
    }

    private static class TestDataProvider extends ListDataProvider<Person> {
        public TestDataProvider(Collection<Person> items) {
            super(items);
        }

        // Override implementation to only use the id property for identity,
        // rather than equals/hashCode
        @Override
        public Object getId(Person item) {
            return item.getId();
        }
    }

    private static class Person {
        long id;
        String name;

        public Person(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
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
                    && Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }
}
