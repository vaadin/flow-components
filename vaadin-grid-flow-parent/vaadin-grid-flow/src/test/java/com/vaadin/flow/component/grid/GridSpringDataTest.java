/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GridSpringDataTest {

    @Test
    void setItemsPageableNoCount() {
        AtomicInteger pageSize = new AtomicInteger(-1);
        AtomicInteger pageNumber = new AtomicInteger(-1);
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setItemsPageable(pageable -> {
            if (pageSize.get() != -1) {
                throw new IllegalStateException(
                        "There should be only one call to the data provider");
            }
            pageSize.set(pageable.getPageSize());
            pageNumber.set(pageable.getPageNumber());

            return List.of(new Person("John", 1293));
        });

        Person item = grid.getLazyDataView().getItem(0);

        Assertions.assertEquals(0, pageNumber.get());
        Assertions.assertTrue(pageSize.get() > 0);
        Assertions.assertEquals("John", item.getName());
    }

    @Test
    void setItemsPageableWithCount() {
        AtomicInteger pageSize = new AtomicInteger(-1);
        AtomicInteger pageNumber = new AtomicInteger(-1);
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setItemsPageable(pageable -> {
            if (pageSize.get() != -1) {
                throw new IllegalStateException(
                        "There should be only one call to the data provider");
            }
            pageSize.set(pageable.getPageSize());
            pageNumber.set(pageable.getPageNumber());

            return List.of(new Person("John", 1293), new Person("Jane", 1923),
                    new Person("Homer", 1956));
        }, pageable -> 3L);

        Person item = grid.getLazyDataView().getItems().toList().get(1);

        Assertions.assertEquals(0, pageNumber.get());
        Assertions.assertEquals(3, pageSize.get());
        Assertions.assertEquals("Jane", item.getName());
    }
}
