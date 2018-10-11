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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.ValueProvider;

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
                "The column with key 'wrong' is not part of this Grid");

        Grid<String> grid2 = new Grid<>();
        Column<String> wrongColumn = grid2.addColumn(str -> str);
        wrongColumn.setKey("wrong");
        grid.removeColumn(wrongColumn);
    }

    @Test
    public void removeColumnTwice_throws() {
        expectIllegalArgumentException(
                "The column with key 'first' is not part of this Grid");

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

    @Test(expected = IllegalStateException.class)
    public void setBinding_coumnAlreadyHasBinding_throw() {
        Binder<String> binder = new Binder<>();
        firstColumn.setEditorBinding(binder.bind(new TextField(),
                ValueProvider.identity(), (item, value) -> {
                }));
        firstColumn.setEditorBinding(binder.bind(new TextArea(),
                ValueProvider.identity(), (item, value) -> {
                }));
    }

    @Test(expected = IllegalStateException.class)
    public void setBinding_coumnAlreadyHasEditorComponent_throw() {
        Binder<String> binder = new Binder<>();
        firstColumn.setEditorBinding(binder.bind(new TextField(),
                ValueProvider.identity(), (item, value) -> {
                }));
        firstColumn.setEditorComponent(new TextArea());
    }

    @Test(expected = IllegalStateException.class)
    public void setEditorComponent_coumnAlreadyHasBinding_throw() {
        firstColumn.setEditorComponent(new TextArea());

        Binder<String> binder = new Binder<>();
        firstColumn.setEditorBinding(binder.bind(new TextField(),
                ValueProvider.identity(), (item, value) -> {
                }));
    }

    @Test(expected = IllegalStateException.class)
    public void setEditorComponent_coumnAlreadyHasComponent_throw() {
        firstColumn.setEditorComponent(new TextArea());

        firstColumn.setEditorComponent(new TextField());
    }

    private void expectNullPointerException(String message) {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(message);
    }

    private void expectIllegalArgumentException(String message) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(message);
    }
}
