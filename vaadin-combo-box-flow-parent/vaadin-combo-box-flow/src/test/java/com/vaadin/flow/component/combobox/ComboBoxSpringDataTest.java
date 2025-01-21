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

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class ComboBoxSpringDataTest {
    public static class Person implements Serializable {
        private String name;
        private final int born;

        public Person(String name, int born) {
            this.name = name;
            this.born = born;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBorn() {
            return born;
        }
    }

    private static List<Person> data = List.of(new Person("John", 1293),
            new Person("Jane", 1923), new Person("Homer", 1956));

    @Test
    public void setItemsPageableNoCountNoFilter() {
        AtomicInteger pageSize = new AtomicInteger(-1);
        AtomicInteger pageNumber = new AtomicInteger(-1);
        ComboBox<Person> combobox = new ComboBox<>();
        combobox.setItemsPageable((pageable, filterString) -> {
            if (pageSize.get() != -1) {
                throw new IllegalStateException(
                        "There should be only one call to the data provider");
            }
            pageSize.set(pageable.getPageSize());
            pageNumber.set(pageable.getPageNumber());

            return filteredData(filterString);
        });

        Person item = combobox.getLazyDataView().getItems().toList().get(2);

        Assert.assertEquals(0, pageNumber.get());
        Assert.assertTrue(pageSize.get() > 0);
        Assert.assertEquals("Homer", item.getName());
    }

    @Test
    public void setItemsPageableNoCountFilter() {
        AtomicInteger pageSize = new AtomicInteger(-1);
        AtomicInteger pageNumber = new AtomicInteger(-1);
        ComboBox<Person> comboBox = new ComboBox<>();
        comboBox.setItemsPageable((pageable, filterString) -> {
            if (pageSize.get() != -1) {
                throw new IllegalStateException(
                        "There should be only one call to the data provider");
            }
            pageSize.set(pageable.getPageSize());
            pageNumber.set(pageable.getPageNumber());

            return filteredData(filterString);
        });
        comboBox.getDataController().setRequestedRange(0, 50, "J");

        List<Person> items = comboBox.getLazyDataView().getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals(0, pageNumber.get());
        Assert.assertTrue(pageSize.get() > 0);
        Assert.assertEquals("Jane", items.get(1).getName());
    }

    @Test
    public void setItemsPageableWithCount() {
        AtomicInteger pageSize = new AtomicInteger(-1);
        AtomicInteger pageNumber = new AtomicInteger(-1);
        ComboBox<Person> comboBox = new ComboBox<>();
        comboBox.setItemsPageable((pageable, filterString) -> {
            if (pageSize.get() != -1) {
                throw new IllegalStateException(
                        "There should be only one call to the data provider");
            }
            pageSize.set(pageable.getPageSize());
            pageNumber.set(pageable.getPageNumber());

            return filteredData(filterString);
        }, (pageable, filterString) -> 3L);

        List<Person> items = comboBox.getLazyDataView().getItems().toList();

        Assert.assertEquals(3, items.size());
        Assert.assertEquals(0, pageNumber.get());
        Assert.assertTrue(pageSize.get() > 0);
        Assert.assertEquals("Jane", items.get(1).getName());
    }

    private static List<Person> filteredData(String filterString) {
        return data.stream()
                .filter(person -> person.getName().contains(filterString))
                .toList();
    }

}
