/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.Grid.Column;

public class BeanGridTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Grid<Person> grid;

    @Before
    public void init() {
        grid = new Grid<>(Person.class);
    }

    @Test
    public void beanGrid_columnsForPropertiesAddedWithCorrectKeys() {
        String[] expectedKeys = new String[] { "born", "name", "friend",
                "grades", "items" };
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

}
