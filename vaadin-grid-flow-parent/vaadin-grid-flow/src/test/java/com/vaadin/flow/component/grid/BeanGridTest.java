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

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.Renderer;

class BeanGridTest {

    Grid<Person> grid;
    ExtendedGrid<Person> extendedGrid;

    @BeforeEach
    void setup() {
        grid = new Grid<>(Person.class);
        extendedGrid = new ExtendedGrid<>(Person.class);
    }

    @Test
    void beanGrid_columnsForPropertiesAddedWithCorrectKeysInAlphabeticalOrder() {
        String[] expectedKeys = new String[] { "born", "friend", "grades",
                "items", "name" };
        Object[] keys = grid.getColumns().stream().map(Column::getKey)
                .toArray();

        Assertions.assertArrayEquals(expectedKeys, keys,
                "Unexpected columns or column-keys");
    }

    @Test
    void addColumnForSubProperty_columnAddedWithCorrectKey() {
        addColumnAndTestKey("friend.name");
        addColumnAndTestKey("friend.friend.friend");
        addColumnAndTestKey("friend.friend.friend.born");
    }

    private void addColumnAndTestKey(String property) {
        grid.addColumn(property);
        Assertions.assertNotNull(grid.getColumnByKey(property),
                "Column for sub-property not found by key");
    }

    private static class ExtendedColumn<T> extends Column<T> {
        ExtendedColumn(Grid<T> grid, String columnId, Renderer<T> renderer) {
            super(grid, columnId, renderer);
        }
    }

    private static class ExtendedGrid<T> extends Grid<T> {
        public ExtendedGrid() {
            super();
        }

        public ExtendedGrid(Class<T> beanType) {
            super(beanType, false);
        }

        public ExtendedColumn<T> createCustomColumn(Renderer<T> renderer,
                String columnId) {
            return new ExtendedColumn<>(this, columnId, renderer);
        }
    }

    private void assertEqualColumnClasses(Class columnClass, Class compareTo) {
        Assertions.assertNotNull(columnClass);
        Assertions.assertEquals(compareTo, columnClass);
    }

    @Test
    void addRegularColumnAndExtendedColumn() {
        Column<Person> regularColumn = extendedGrid.addColumn("name");
        ExtendedColumn<Person> extendedColumn = extendedGrid.addColumn("born",
                extendedGrid::createCustomColumn);

        assertEqualColumnClasses(regularColumn.getClass(), Column.class);
        assertEqualColumnClasses(extendedColumn.getClass(),
                ExtendedColumn.class);
    }

    @Test
    void duplicateColumnsForSameProperty_throws() {
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.addColumn("name"));
        Assertions.assertTrue(ex.getMessage()
                .contains("Multiple columns for the same property"));
    }

    @Test
    void duplicateColumnsForSameSubProperty_throws() {
        grid.addColumn("friend.name");
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.addColumn("friend.name"));
        Assertions.assertTrue(ex.getMessage()
                .contains("Multiple columns for the same property"));
    }

    @Test
    void addColumnForNonExistingProperty_throws() {
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.addColumn("foobar"));
        Assertions.assertTrue(
                ex.getMessage().contains("Can't resolve property name"));
    }

    @Test
    void addColumnForNonExistingSubProperty_throws() {
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.addColumn("friend.foobar"));
        Assertions.assertTrue(
                ex.getMessage().contains("Can't resolve property name"));
    }

    @Test
    void addPropertyColumnForGridWithoutPropertySet_throws() {
        Grid<Person> nonBeanGrid = new Grid<Person>();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> nonBeanGrid.addColumn("friend.name"));
    }

    @Test
    void setColumns_columnsForPropertiesAddedWithCorrectKeys() {
        String[] properties = new String[] { "grades", "born" };
        grid.setColumns(properties);

        Object[] columnKeys = grid.getColumns().stream().map(Column::getKey)
                .toArray();
        Assertions.assertArrayEquals(properties, columnKeys,
                "Unexpected columns or column-keys");
    }

    @Test
    void setColumnsForGridWithoutPropertySet_throws() {
        Grid<Person> nonBeanGrid = new Grid<Person>();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> nonBeanGrid.setColumns("name"));
    }

    @Test
    void beanGridWithNoAutoColumns_columnsAreNotAdded() {
        Grid<Person> nonAutoGrid = new Grid<>(Person.class, false);
        Assertions.assertEquals(Collections.emptyList(),
                nonAutoGrid.getColumns());
        Assertions.assertNull(nonAutoGrid.getColumnByKey("name"));
    }

    @Test
    void addColumnsForProperties_columnsAddedWithCorrectKeys() {
        grid.setColumns(); // cleans up all columns
        String[] expectedKeys = new String[] { "name", "born" };
        grid.addColumns(expectedKeys);
        Object[] keys = grid.getColumns().stream().map(Column::getKey)
                .toArray();

        Assertions.assertArrayEquals(expectedKeys, keys,
                "Unexpected columns or column-keys");
    }

    @Test
    void addColumnsForSubProperties_columnsAddedWithCorrectKeys() {
        grid.setColumns(); // cleans up all columns
        String[] expectedKeys = new String[] { "friend.name",
                "friend.friend.friend", "friend.friend.friend.born" };
        grid.addColumns(expectedKeys);
        Object[] keys = grid.getColumns().stream().map(Column::getKey)
                .toArray();

        Assertions.assertArrayEquals(expectedKeys, keys,
                "Unexpected columns or column-keys");
    }

    @Test
    void addColumnsForNonExistingProperty_throws() {
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.addColumns("friend.foobar"));
        Assertions.assertTrue(
                ex.getMessage().contains("Can't resolve property name"));
    }

    @Test
    void addColumnsForGridWithoutPropertySet_throws() {
        Grid<Person> nonBeanGrid = new Grid<>();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> nonBeanGrid.addColumns("name"));
    }

    @Test
    void getEditor_editorHasBinder_binderIsAwareOfBeanProperties() {
        Editor<Person> editor = grid.getEditor();
        Binder<Person> binder = editor.getBinder();
        Assertions.assertNotNull(binder);

        // Binder is aware about Person properties: otherwise it will throw
        binder.bind(new TextField(), "name");
        binder.bind(new TextField(), "born");
    }

    @Test
    void removeAllColumns() {
        int initialColumnCount = grid.getColumns().size();
        Assertions.assertTrue(initialColumnCount > 0);
        grid.removeAllColumns();
        int columnCount = grid.getColumns().size();
        Assertions.assertEquals(0, columnCount);
    }

    @Test
    void beanType_isSet() {
        Assertions.assertEquals(Person.class, grid.getBeanType());
    }

    @Test
    void configureBeanTypeAfterInit() {
        extendedGrid = new ExtendedGrid();
        Assertions.assertNull(extendedGrid.getBeanType());
        Assertions.assertEquals(0, extendedGrid.getColumns().size());
        extendedGrid.configureBeanType(Person.class, false);
        Assertions.assertEquals(Person.class, extendedGrid.getBeanType());
        Assertions.assertEquals(0, extendedGrid.getColumns().size());
    }

    @Test
    void configureBeanTypeAndAddColumnsAfterInit() {
        extendedGrid = new ExtendedGrid();
        Assertions.assertNull(extendedGrid.getBeanType());
        Assertions.assertEquals(0, extendedGrid.getColumns().size());
        extendedGrid.configureBeanType(Person.class, true);
        Assertions.assertEquals(Person.class, extendedGrid.getBeanType());
        Assertions.assertEquals(5, extendedGrid.getColumns().size());
    }

    @Test
    void configureBeanTypeFailsWhenTypeIsSet() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> extendedGrid.configureBeanType(Person.class, true));
    }

    @Test
    void configureBeanTypeFailsWhenColumnsExist() {
        extendedGrid = new ExtendedGrid();
        extendedGrid.addColumn((p) -> "hello");
        Assertions.assertThrows(IllegalStateException.class,
                () -> extendedGrid.configureBeanType(Person.class, true));
    }
}
