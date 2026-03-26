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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.tests.MockUIExtension;

class GridColumnTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    Grid<String> grid;
    Column<String> firstColumn;
    Column<String> secondColumn;
    Column<String> thirdColumn;
    Column<String> fourthColumn;

    IconRenderer<String> renderer;

    @BeforeEach
    void setup() {
        grid = new Grid<>();
        firstColumn = grid.addColumn(str -> str);
        secondColumn = grid.addColumn(str -> str);
        thirdColumn = grid.addColumn(str -> str);
        renderer = new IconRenderer<String>(generator -> new Span(":D"));
        fourthColumn = grid.addColumn(renderer);
    }

    @Test
    void setKey_getByKey() {
        firstColumn.setKey("foo");
        secondColumn.setKey("bar");
        Assertions.assertEquals(firstColumn, grid.getColumnByKey("foo"));
        Assertions.assertEquals(secondColumn, grid.getColumnByKey("bar"));
    }

    @Test
    void changeKey_throws() {
        firstColumn.setKey("foo");
        Assertions.assertThrows(IllegalStateException.class,
                () -> firstColumn.setKey("bar"));
    }

    @Test
    void duplicateKey_throws() {
        firstColumn.setKey("foo");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> secondColumn.setKey("foo"));
    }

    @Test
    void removeColumnByKey() {
        firstColumn.setKey("first");
        grid.removeColumnByKey("first");
        Assertions.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    void removeColumnByNullKey_throws() {
        var ex = Assertions.assertThrows(NullPointerException.class,
                () -> grid.removeColumnByKey(null));
        Assertions.assertTrue(
                ex.getMessage().contains("columnKey should not be null"));
    }

    @Test
    void removeColumn() {
        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);
        Assertions.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    void removeNullColumn_throws() {
        var ex = Assertions.assertThrows(NullPointerException.class,
                () -> grid.removeColumn(null));
        Assertions.assertTrue(
                ex.getMessage().contains("column should not be null"));
    }

    @Test
    void removeInvalidColumnByKey_throws() {
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.removeColumnByKey("wrong"));
        Assertions.assertTrue(ex.getMessage().contains(
                "The column with key 'wrong' is not part of this Grid"));
    }

    @Test
    void removeColumnByKeyTwice_throws() {
        firstColumn.setKey("first");
        grid.removeColumnByKey("first");
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.removeColumnByKey("first"));
        Assertions.assertTrue(ex.getMessage().contains(
                "The column with key 'first' is not part of this Grid"));
    }

    @Test
    void removeInvalidColumn_throws() {
        Grid<String> grid2 = new Grid<>();
        Column<String> wrongColumn = grid2.addColumn(str -> str);
        wrongColumn.setKey("wrong");
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.removeColumn(wrongColumn));
        Assertions.assertTrue(ex.getMessage().contains(
                "The column with key 'wrong' is not owned by this Grid"));
    }

    @Test
    void removeColumnTwice_throws() {
        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);
        var ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> grid.removeColumn(firstColumn));
        Assertions.assertTrue(ex.getMessage().contains(
                "The column with key 'first' is not owned by this Grid"));
    }

    @Test
    void addColumn_defaultComparator() {
        Grid<Person> grid = new Grid<>();

        Column<Person> nameColumn = grid.addColumn(Person::getName);
        SerializableComparator<Person> nameComparator = nameColumn
                .getComparator(SortDirection.ASCENDING);

        Person person1 = new Person("a", 1970);
        Person person2 = new Person("b", 1960);
        int result = nameComparator.compare(person1, person2);

        Assertions.assertEquals(-1, result,
                "The first person name should be less than the name of the second person");

        Column<Person> ageColumn = grid.addColumn(Person::getBorn);
        SerializableComparator<Person> ageComparator = ageColumn
                .getComparator(SortDirection.ASCENDING);
        result = ageComparator.compare(person1, person2);

        Assertions.assertEquals(1, result,
                "The first person year of born should be greater than the year of born of the second person");

        // comparator which uses toString
        Column<Person> identityColumn = grid.addColumn(person -> person);
        SerializableComparator<Person> personComparator = identityColumn
                .getComparator(SortDirection.ASCENDING);
        result = personComparator.compare(person1, person2);

        Assertions.assertEquals(-1, result,
                "The first person toString() result greater than the the second person toString() result");
    }

    @Test
    void testRenderer() {
        assert renderer != null;
        Assertions.assertEquals(renderer, fourthColumn.getRenderer());
    }

    @Test
    void setRenderer() {
        Renderer<String> newRenderer = LitRenderer
                .<String> of("<span>${text}</span>")
                .withProperty("text", ValueProvider.identity());
        fourthColumn.setRenderer(newRenderer);
        Assertions.assertEquals(newRenderer, fourthColumn.getRenderer());
    }

    @Test
    void setRendererReturnsColumn() {
        Renderer<String> newRenderer = LitRenderer
                .<String> of("<span>${text}</span>")
                .withProperty("text", ValueProvider.identity());
        Grid.Column<String> result = fourthColumn.setRenderer(newRenderer);
        Assertions.assertEquals(fourthColumn, result);
    }

    @Test
    void addColumn_defaultTextAlign() {
        Grid<Person> grid = new Grid<>();

        Column<Person> nameColumn = grid.addColumn(Person::getName);
        Assertions.assertEquals(ColumnTextAlign.START,
                nameColumn.getTextAlign());
    }

    @Test
    void setTextAlignToNull_defaultTextAlign() {
        Grid<Person> grid = new Grid<>();

        Column<Person> nameColumn = grid.addColumn(Person::getName)
                .setTextAlign(null);
        Assertions.assertEquals(ColumnTextAlign.START,
                nameColumn.getTextAlign());

        nameColumn.setTextAlign(ColumnTextAlign.CENTER);
        Assertions.assertEquals(ColumnTextAlign.CENTER,
                nameColumn.getTextAlign());

        nameColumn.setTextAlign(null);
        Assertions.assertEquals(ColumnTextAlign.START,
                nameColumn.getTextAlign());
    }

    @Test
    void createColumn_returnsNonNullAndBasicType() {
        Column column = new Grid<Person>().createColumn(LitRenderer.of(""), "");
        assertNotNull(column);
        Assertions.assertEquals(Column.class, column.getClass());
    }

    @Test
    void addColumn_extendedColumnTypeByOverridingCreateMethod() {
        Grid<Person> extendedGrid = new Grid<Person>() {
            @Override
            protected Column<Person> createColumn(Renderer<Person> renderer,
                    String columnId) {
                return new ExtendedColumn<>(this, columnId, renderer);
            }
        };

        Column<Person> column = extendedGrid.addColumn(Person::toString);

        assertNotNull(column);
        Assertions.assertEquals(ExtendedColumn.class, column.getClass());
    }

    @Test
    void addColumn_extendedColumnTypeByOverridingDefaultColumnFactoryGetter() {
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
        Assertions.assertEquals(ExtendedColumn.class, column.getClass());
    }

    @Test
    void addColumn_extendedColumnTypeByUsingAddColumnOverload() {
        ExtendedGrid<Person> extendedGrid = new ExtendedGrid<>();

        List<ExtendedColumn<Person>> columnsList = new ArrayList<>();

        columnsList.add(extendedGrid.addColumn(Person::toString,
                extendedGrid::createCustomColumn));
        columnsList.add(extendedGrid.addColumn(LitRenderer.of(""),
                extendedGrid::createCustomColumn));

        columnsList.forEach(column -> {
            assertNotNull(column);
            Assertions.assertEquals(ExtendedColumn.class, column.getClass());
        });
    }

    @Test
    void addRegularColumnAndExtendedColumn() {
        ExtendedGrid<Person> extendedGrid = new ExtendedGrid<>();

        Column regularColumn = extendedGrid.addColumn(Person::toString);
        ExtendedColumn extendedColumn = extendedGrid.addColumn(
                LitRenderer.of(""), extendedGrid::createCustomColumn);

        assertEqualColumnClasses(regularColumn.getClass(), Column.class);
        assertEqualColumnClasses(extendedColumn.getClass(),
                ExtendedColumn.class);
    }

    @Test
    void setColumnRowHeader_updatedPropertyValue() {
        Grid<Person> grid = new Grid<>();

        Column<Person> rowHeaderColumn = grid.addColumn(Person::getName);
        rowHeaderColumn.setRowHeader(true);
        Assertions.assertTrue(
                rowHeaderColumn.getElement().getProperty("rowHeader", false));
        Assertions.assertTrue(rowHeaderColumn.isRowHeader());
    }

    private void assertEqualColumnClasses(Class columnClass, Class compareTo) {
        assertNotNull(columnClass);
        Assertions.assertEquals(compareTo, columnClass);
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
