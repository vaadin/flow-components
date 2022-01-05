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

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.function.SerializableComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertNotNull;

public class GridColumnTest {

    Grid<String> grid;
    Column<String> firstColumn;
    Column<String> secondColumn;
    Column<String> thirdColumn;
    Column<String> fourthColumn;

    IconRenderer<String> renderer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        grid = new Grid<>();
        firstColumn = grid.addColumn(str -> str);
        secondColumn = grid.addColumn(str -> str);
        thirdColumn = grid.addColumn(str -> str);
        renderer = new IconRenderer<String>(generator -> new Label(":D"));
        fourthColumn = grid.addColumn(renderer);
    }

    @Test
    public void templateWarningSuppressed() {
        Assert.assertTrue("Template warning is not suppressed", firstColumn
                .getElement().hasAttribute("suppress-template-warning"));
    }

    @Test
    public void setKey_getByKey() {
        firstColumn.setKey("foo");
        secondColumn.setKey("bar");
        Assert.assertEquals(firstColumn, grid.getColumnByKey("foo"));
        Assert.assertEquals(secondColumn, grid.getColumnByKey("bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void changeKey_throws() {
        firstColumn.setKey("foo");
        firstColumn.setKey("bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateKey_throws() {
        firstColumn.setKey("foo");
        secondColumn.setKey("foo");
    }

    @Test
    public void removeColumnByKey() {
        firstColumn.setKey("first");
        grid.removeColumnByKey("first");
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void removeColumnByNullKey_throws() {
        expectNullPointerException("columnKey should not be null");
        grid.removeColumnByKey(null);
    }

    @Test
    public void removeColumn() {
        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void removeNullColumn_throws() {
        expectNullPointerException("column should not be null");
        grid.removeColumn(null);
    }

    @Test
    public void removeInvalidColumnByKey_throws() {
        expectIllegalArgumentException(
                "The column with key 'wrong' is not part of this Grid");

        grid.removeColumnByKey("wrong");
    }

    @Test
    public void removeColumnByKeyTwice_throws() {
        expectIllegalArgumentException(
                "The column with key 'first' is not part of this Grid");

        firstColumn.setKey("first");
        grid.removeColumnByKey("first");
        grid.removeColumnByKey("first");
    }

    @Test
    public void removeInvalidColumn_throws() {
        expectIllegalArgumentException(
                "The column with key 'wrong' is not owned by this Grid");

        Grid<String> grid2 = new Grid<>();
        Column<String> wrongColumn = grid2.addColumn(str -> str);
        wrongColumn.setKey("wrong");
        grid.removeColumn(wrongColumn);
    }

    @Test
    public void removeColumnTwice_throws() {
        expectIllegalArgumentException(
                "The column with key 'first' is not owned by this Grid");

        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);
        grid.removeColumn(firstColumn);
    }

    @Test
    public void addColumn_defaultComparator() {
        Grid<Person> grid = new Grid<>();

        Column<Person> nameColumn = grid.addColumn(Person::getName);
        SerializableComparator<Person> nameComparator = nameColumn
                .getComparator(SortDirection.ASCENDING);

        Person person1 = new Person("a", 1970);
        Person person2 = new Person("b", 1960);
        int result = nameComparator.compare(person1, person2);

        Assert.assertEquals(
                "The first person name should be less than the name of the second person",
                -1, result);

        Column<Person> ageColumn = grid.addColumn(Person::getBorn);
        SerializableComparator<Person> ageComparator = ageColumn
                .getComparator(SortDirection.ASCENDING);
        result = ageComparator.compare(person1, person2);

        Assert.assertEquals(
                "The first person year of born should be greater than the year of born of the second person",
                1, result);

        // comparator which uses toString
        Column<Person> identityColumn = grid.addColumn(person -> person);
        SerializableComparator<Person> personComparator = identityColumn
                .getComparator(SortDirection.ASCENDING);
        result = personComparator.compare(person1, person2);

        Assert.assertEquals(
                "The first person toString() result greater than the the second person toString() result",
                -1, result);
    }

    @Test
    public void testRenderer() {
        assert renderer != null;
        Assert.assertEquals(renderer, fourthColumn.getRenderer());
    }

    @Test
    public void addColumn_defaultTextAlign() {
        Grid<Person> grid = new Grid<>();

        Column<Person> nameColumn = grid.addColumn(Person::getName);
        Assert.assertEquals(ColumnTextAlign.START, nameColumn.getTextAlign());
    }

    @Test
    public void setTextAlignToNull_defaultTextAlign() {
        Grid<Person> grid = new Grid<>();

        Column<Person> nameColumn = grid.addColumn(Person::getName)
                .setTextAlign(null);
        Assert.assertEquals(ColumnTextAlign.START, nameColumn.getTextAlign());

        nameColumn.setTextAlign(ColumnTextAlign.CENTER);
        Assert.assertEquals(ColumnTextAlign.CENTER, nameColumn.getTextAlign());

        nameColumn.setTextAlign(null);
        Assert.assertEquals(ColumnTextAlign.START, nameColumn.getTextAlign());
    }

    private void expectNullPointerException(String message) {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(message);
    }

    private void expectIllegalArgumentException(String message) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(message);
    }

    @Test
    public void createColumn_returnsNonNullAndBasicType() {
        Column column = new Grid<Person>().createColumn(TemplateRenderer.of(""),
                "");
        assertNotNull(column);
        Assert.assertEquals(Column.class, column.getClass());
    }

    @Test
    public void addColumn_extendedColumnTypeByOverridingCreateMethod() {
        Grid<Person> extendedGrid = new Grid<Person>() {
            @Override
            protected Column<Person> createColumn(Renderer<Person> renderer,
                    String columnId) {
                return new ExtendedColumn<>(this, columnId, renderer);
            }
        };

        Column<Person> column = extendedGrid.addColumn(Person::toString);

        assertNotNull(column);
        Assert.assertEquals(ExtendedColumn.class, column.getClass());
    }

    @Test
    public void addColumn_extendedColumnTypeByOverridingDefaultColumnFactoryGetter() {
        Grid<Person> extendedGrid = new Grid<Person>() {
            public ExtendedColumn<Person> createCustomColumn(
                    Renderer<Person> renderer, String columnId) {
                return new ExtendedColumn<>(this, columnId, renderer);
            }

            @Override
            protected BiFunction<Renderer<Person>, String, Column<Person>> getDefaultColumnFactory() {
                return this::createCustomColumn;
            }
        };

        Column<Person> column = extendedGrid.addColumn(Person::toString);

        assertNotNull(column);
        Assert.assertEquals(ExtendedColumn.class, column.getClass());
    }

    @Test
    public void addColumn_extendedColumnTypeByUsingAddColumnOverload() {
        ExtendedGrid<Person> extendedGrid = new ExtendedGrid<>();

        List<ExtendedColumn<Person>> columnsList = new ArrayList<>();

        columnsList.add(extendedGrid.addColumn(Person::toString,
                extendedGrid::createCustomColumn));
        columnsList.add(extendedGrid.addColumn(TemplateRenderer.of(""),
                extendedGrid::createCustomColumn));
        columnsList.add(extendedGrid.addColumn(TemplateRenderer.of(""),
                extendedGrid::createCustomColumn, ""));

        columnsList.forEach(column -> {
            assertNotNull(column);
            Assert.assertEquals(ExtendedColumn.class, column.getClass());
        });
    }

    @Test
    public void addRegularColumnAndExtendedColumn() {
        ExtendedGrid<Person> extendedGrid = new ExtendedGrid<>();

        Column regularColumn = extendedGrid.addColumn(Person::toString);
        ExtendedColumn extendedColumn = extendedGrid.addColumn(
                TemplateRenderer.of(""), extendedGrid::createCustomColumn);

        assertEqualColumnClasses(regularColumn.getClass(), Column.class);
        assertEqualColumnClasses(extendedColumn.getClass(),
                ExtendedColumn.class);
    }

    private void assertEqualColumnClasses(Class columnClass, Class compareTo) {
        assertNotNull(columnClass);
        Assert.assertEquals(compareTo, columnClass);
    }

    private static class ExtendedColumn<T> extends Column<T> {
        ExtendedColumn(Grid<T> grid, String columnId, Renderer<T> renderer) {
            super(grid, columnId, renderer);
        }
    }

    private static class ExtendedGrid<T> extends Grid<T> {
        public ExtendedColumn<T> createCustomColumn(Renderer<T> renderer,
                String columnId) {
            return new ExtendedColumn<>(this, columnId, renderer);
        }
    }
}
