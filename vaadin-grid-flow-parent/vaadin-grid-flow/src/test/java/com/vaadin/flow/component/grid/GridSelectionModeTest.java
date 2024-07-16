/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridNoneSelectionModel;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.Grid.SelectionMode;

public class GridSelectionModeTest {

    private Grid<String> grid;

    @Before
    public void setup() {
        grid = new Grid<>();
        grid.setItems("foo", "bar", "baz");
    }

    @Test
    public void testSelectionModes() {
        assertInstanceOf(GridSingleSelectionModel.class,
                grid.getSelectionModel().getClass());

        assertInstanceOf(GridMultiSelectionModel.class,
                grid.setSelectionMode(SelectionMode.MULTI).getClass());
        assertInstanceOf(GridMultiSelectionModel.class,
                grid.getSelectionModel().getClass());

        assertInstanceOf(GridNoneSelectionModel.class,
                grid.setSelectionMode(SelectionMode.NONE).getClass());
        assertInstanceOf(GridNoneSelectionModel.class,
                grid.getSelectionModel().getClass());

        assertInstanceOf(GridSingleSelectionModel.class,
                grid.setSelectionMode(SelectionMode.SINGLE).getClass());
        assertInstanceOf(GridSingleSelectionModel.class,
                grid.getSelectionModel().getClass());
    }

    private void assertInstanceOf(Class<?> expected, Class<?> actual) {
        Assert.assertTrue(
                actual.getName() + " should be instance of "
                        + expected.getName(),
                expected.isAssignableFrom(actual));
    }

    @Test(expected = NullPointerException.class)
    public void testNullSelectionMode() {
        grid.setSelectionMode(null);
    }

}
