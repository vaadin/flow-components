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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.grid.Grid.SelectionMode;

class GridSelectionModeTest {

    private Grid<String> grid;

    @BeforeEach
    void setup() {
        grid = new Grid<>();
        grid.setItems("foo", "bar", "baz");
    }

    @Test
    void testSelectionModes() {
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
        Assertions.assertTrue(expected.isAssignableFrom(actual),
                actual.getName() + " should be instance of "
                        + expected.getName());
    }

    @Test
    void testNullSelectionMode() {
        Assertions.assertThrows(NullPointerException.class,
                () -> grid.setSelectionMode(null));
    }

    @Test
    void testGetSelectionMode() {
        Assertions.assertEquals(SelectionMode.SINGLE, grid.getSelectionMode());

        grid.setSelectionMode(SelectionMode.MULTI);
        Assertions.assertEquals(SelectionMode.MULTI, grid.getSelectionMode());

        grid.setSelectionMode(SelectionMode.NONE);
        Assertions.assertEquals(SelectionMode.NONE, grid.getSelectionMode());
    }
}
