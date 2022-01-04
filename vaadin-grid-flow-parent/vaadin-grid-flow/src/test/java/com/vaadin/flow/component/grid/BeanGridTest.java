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

import java.util.Collections;

import com.vaadin.flow.data.renderer.Renderer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

public class BeanGridTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Grid<Person> grid;
    ExtendedGrid<Person> extendedGrid;

    @Before
    public void init() {
        grid = new Grid<>(Person.class);
        extendedGrid = new ExtendedGrid<>(Person.class);
    }

    @Test
    public void beanGrid_columnsForPropertiesAddedWithCorrectKeysInAlphabeticalOrder() {
        String[] expectedKeys = new String[] { "born", "friend", "grades",
                "items", "name" };
        Object[] keys = grid.getColumns().stream().map(Column::getKey)
                .toArray();

        Assert.assertArrayEquals("Unexpected columns or column-keys",
                expectedKeys, keys);
    }

    @Test
    public void addColumnForSubProperty_columnAddedWithCorrectKey() {
        addColumnAndTestKey("friend.name");
        addColumnAndTestKey("friend.friend.friend");
        addColumnAndTestKey("friend.friend.friend.born");
    }

    private void addColumnAndTestKey(String property) {
        grid.addColumn(property);
        Assert.assertNotNull("Column for sub-property not found by key",
                grid.getColumnByKey(property));
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
        Assert.assertNotNull(columnClass);
        Assert.assertEquals(compareTo, columnClass);
    }

    @Test
    public void addRegularColumnAndExtendedColumn() {
        Column<Person> regularColumn = extendedGrid.addColumn("name");
        ExtendedColumn<Person> extendedColumn = extendedGrid.addColumn("born",
                extendedGrid::createCustomColumn);

        assertEqualColumnClasses(regularColumn.getClass(), Column.class);
        assertEqualColumnClasses(extendedColumn.getClass(),
                ExtendedColumn.class);
    }

    @Test
    public void duplicateColumnsForSameProperty_throws() {
        expectIllegalArgumentException(
                "Multiple columns for the same property");
        grid.addColumn("name");
    }

    @Test
    public void duplicateColumnsForSameSubProperty_throws() {
        expectIllegalArgumentException(
                "Multiple columns for the same property");
        grid.addColumn("friend.name");
        grid.addColumn("friend.name");
    }

    @Test
    public void addColumnForNonExistingProperty_throws() {
        expectIllegalArgumentException("Can't resolve property name");
        grid.addColumn("foobar");
    }

    @Test
    public void addColumnForNonExistingSubProperty_throws() {
        expectIllegalArgumentException("Can't resolve property name");
        grid.addColumn("friend.foobar");
    }

    private void expectIllegalArgumentException(String expectedMessage) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(expectedMessage);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addPropertyColumnForGridWithoutPropertySet_throws() {
        Grid<Person> nonBeanGrid = new Grid<Person>();
        nonBeanGrid.addColumn("friend.name");
    }

    @Test
    public void setColumns_columnsForPropertiesAddedWithCorrectKeys() {
        String[] properties = new String[] { "grades", "born" };
        grid.setColumns(properties);

        Object[] columnKeys = grid.getColumns().stream().map(Column::getKey)
                .toArray();
        Assert.assertArrayEquals("Unexpected columns or column-keys",
                properties, columnKeys);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setColumnsForGridWithoutPropertySet_throws() {
        Grid<Person> nonBeanGrid = new Grid<Person>();
        nonBeanGrid.setColumns("name");
    }

    @Test
    public void beanGridWithNoAutoColumns_columnsAreNotAdded() {
        Grid<Person> nonAutoGrid = new Grid<>(Person.class, false);
        Assert.assertEquals(Collections.emptyList(), nonAutoGrid.getColumns());
        Assert.assertNull(nonAutoGrid.getColumnByKey("name"));
    }

    @Test
    public void addColumnsForProperties_columnsAddedWithCorrectKeys() {
        grid.setColumns(); // cleans up all columns
        String[] expectedKeys = new String[] { "name", "born" };
        grid.addColumns(expectedKeys);
        Object[] keys = grid.getColumns().stream().map(Column::getKey)
                .toArray();

        Assert.assertArrayEquals("Unexpected columns or column-keys",
                expectedKeys, keys);
    }

    @Test
    public void addColumnsForSubProperties_columnsAddedWithCorrectKeys() {
        grid.setColumns(); // cleans up all columns
        String[] expectedKeys = new String[] { "friend.name",
                "friend.friend.friend", "friend.friend.friend.born" };
        grid.addColumns(expectedKeys);
        Object[] keys = grid.getColumns().stream().map(Column::getKey)
                .toArray();

        Assert.assertArrayEquals("Unexpected columns or column-keys",
                expectedKeys, keys);
    }

    @Test
    public void addColumnsForNonExistingProperty_throws() {
        expectIllegalArgumentException("Can't resolve property name");
        grid.addColumns("friend.foobar");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addColumnsForGridWithoutPropertySet_throws() {
        Grid<Person> nonBeanGrid = new Grid<>();
        nonBeanGrid.addColumns("name");
    }

    @Test
    public void getEditor_editorHasBinder_binderIsAwareOfBeanProperties() {
        Editor<Person> editor = grid.getEditor();
        Binder<Person> binder = editor.getBinder();
        Assert.assertNotNull(binder);

        // Binder is aware about Person properties: otherwise it will throw
        binder.bind(new TextField(), "name");
        binder.bind(new TextField(), "born");
    }

    @Test
    public void removeAllColumns() {
        int initialColumnCount = grid.getColumns().size();
        Assert.assertTrue(initialColumnCount > 0);
        grid.removeAllColumns();
        int columnCount = grid.getColumns().size();
        Assert.assertEquals(0, columnCount);
    }

    @Test
    public void beanType_isSet() {
        Assert.assertEquals(Person.class, grid.getBeanType());
    }

    @Test
    public void configureBeanTypeAfterInit() {
        extendedGrid = new ExtendedGrid();
        Assert.assertNull(extendedGrid.getBeanType());
        Assert.assertEquals(0, extendedGrid.getColumns().size());
        extendedGrid.configureBeanType(Person.class, false);
        Assert.assertEquals(Person.class, extendedGrid.getBeanType());
        Assert.assertEquals(0, extendedGrid.getColumns().size());
    }

    @Test
    public void configureBeanTypeAndAddColumnsAfterInit() {
        extendedGrid = new ExtendedGrid();
        Assert.assertNull(extendedGrid.getBeanType());
        Assert.assertEquals(0, extendedGrid.getColumns().size());
        extendedGrid.configureBeanType(Person.class, true);
        Assert.assertEquals(Person.class, extendedGrid.getBeanType());
        Assert.assertEquals(5, extendedGrid.getColumns().size());
    }

    @Test(expected = IllegalStateException.class)
    public void configureBeanTypeFailsWhenTypeIsSet() {
        extendedGrid.configureBeanType(Person.class, true);
    }

    @Test(expected = IllegalStateException.class)
    public void configureBeanTypeFailsWhenColumnsExist() {
        extendedGrid = new ExtendedGrid();
        extendedGrid.addColumn((p) -> "hello");
        extendedGrid.configureBeanType(Person.class, true);
    }
}
