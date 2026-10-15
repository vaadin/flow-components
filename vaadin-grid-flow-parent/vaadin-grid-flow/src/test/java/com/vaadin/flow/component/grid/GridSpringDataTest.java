/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class GridSpringDataTest {

    @Test
    public void setItemsPageableNoCount() {
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

        Assert.assertEquals(0, pageNumber.get());
        Assert.assertTrue(pageSize.get() > 0);
        Assert.assertEquals("John", item.getName());
    }

    @Test
    public void setItemsPageableWithCount() {
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

        Assert.assertEquals(0, pageNumber.get());
        Assert.assertEquals(3, pageSize.get());
        Assert.assertEquals("Jane", item.getName());
    }
}
