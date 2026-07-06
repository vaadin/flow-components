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
import org.junit.jupiter.api.Test;

class GridI18nTest {

    @Test
    void defaultValues_areNull() {
        GridI18n i18n = new GridI18n();
        Assertions.assertNull(i18n.getSelectAll());
        Assertions.assertNull(i18n.getSelectRow());
        Assertions.assertNull(i18n.getSorter());
    }

    @Test
    void setters_returnSameInstance() {
        GridI18n i18n = new GridI18n();
        Assertions.assertSame(i18n, i18n.setSelectAll("Select all"));
        Assertions.assertSame(i18n,
                i18n.setSelectRow("Select row {rowHeader}"));
        Assertions.assertSame(i18n, i18n.setSorter("Sort by {column}"));
    }

    @Test
    void setters_updateValues() {
        GridI18n i18n = new GridI18n().setSelectAll("Select all")
                .setSelectRow("Select row {rowHeader}")
                .setSorter("Sort by {column}");
        Assertions.assertEquals("Select all", i18n.getSelectAll());
        Assertions.assertEquals("Select row {rowHeader}", i18n.getSelectRow());
        Assertions.assertEquals("Sort by {column}", i18n.getSorter());
    }

    @Test
    void grid_getI18n_returnsNullByDefault() {
        Assertions.assertNull(new Grid<String>().getI18n());
    }

    @Test
    void grid_setI18n_getI18n_returnsSameInstance() {
        Grid<String> grid = new Grid<>();
        GridI18n i18n = new GridI18n();
        grid.setI18n(i18n);
        Assertions.assertSame(i18n, grid.getI18n());
    }

    @Test
    void grid_setI18n_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new Grid<String>().setI18n(null));
    }

    @Test
    void grid_setI18n_setsElementProperty() {
        Grid<String> grid = new Grid<>();
        grid.setI18n(new GridI18n().setSelectAll("Select all")
                .setSelectRow("Select row {rowHeader}")
                .setSorter("Sort by {column}"));

        String json = grid.getElement().getPropertyRaw("i18n").toString();
        Assertions.assertTrue(json.contains("\"selectAll\":\"Select all\""));
        Assertions.assertTrue(
                json.contains("\"selectRow\":\"Select row {rowHeader}\""));
        Assertions.assertTrue(json.contains("\"sorter\":\"Sort by {column}\""));
    }

    @Test
    void grid_setI18n_omitsUnsetValues() {
        Grid<String> grid = new Grid<>();
        grid.setI18n(new GridI18n().setSorter("Sort by {column}"));

        String json = grid.getElement().getPropertyRaw("i18n").toString();
        Assertions.assertFalse(json.contains("selectAll"));
        Assertions.assertFalse(json.contains("selectRow"));
        Assertions.assertTrue(json.contains("\"sorter\":\"Sort by {column}\""));
    }
}
