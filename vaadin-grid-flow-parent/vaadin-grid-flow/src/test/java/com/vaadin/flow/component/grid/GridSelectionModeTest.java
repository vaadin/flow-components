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
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Test
    public void testGetSelectionMode() {
        Assert.assertEquals(SelectionMode.SINGLE, grid.getSelectionMode());

        grid.setSelectionMode(SelectionMode.MULTI);
        Assert.assertEquals(SelectionMode.MULTI, grid.getSelectionMode());

        grid.setSelectionMode(SelectionMode.NONE);
        Assert.assertEquals(SelectionMode.NONE, grid.getSelectionMode());
    }
}
