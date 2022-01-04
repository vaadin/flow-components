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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.SerializableFunction;

public class GridColumnEditorTest {

    private Grid<Person> grid;
    private Binder<Person> binder;
    private Column<Person> nameColumn;

    @Before
    public void init() {
        grid = new Grid<>();
        binder = new Binder<>(Person.class);
        grid.getEditor().setBinder(binder);
        nameColumn = grid.addColumn(Person::getName);
    }

    @Test
    public void setEditorComponent_setLambda_getComponentIsNull() {
        Assert.assertNull(nameColumn.getEditorComponent());

        TextField field = new TextField();
        Column<Person> returnedColumn = nameColumn.setEditorComponent(item -> {
            binder.bind(field, "name");
            return field;
        });
        Assert.assertEquals(nameColumn, returnedColumn);

        Assert.assertNull(nameColumn.getEditorComponent());

        binder.bind(field, "name");
        nameColumn.setEditorComponent(field);
        nameColumn.setEditorComponent(item -> field);

        Assert.assertNull(nameColumn.getEditorComponent());
    }

    @Test
    public void setEditorComponent_setComponentl() {
        Assert.assertNull(nameColumn.getEditorComponent());

        TextField field = new TextField();
        Column<Person> returnedColumn = nameColumn.setEditorComponent(field);
        Assert.assertEquals(nameColumn, returnedColumn);

        Assert.assertEquals(field, nameColumn.getEditorComponent());
    }

    @Test
    public void setEditorComponent_setNull_getComponentIsNull() {
        Assert.assertNull(nameColumn.getEditorComponent());
        nameColumn.setEditorComponent((Component) null);
        Assert.assertNull(nameColumn.getEditorComponent());
    }

    @Test
    public void setEditorComponent_setNullLambda_getComponentIsNull() {
        Assert.assertNull(nameColumn.getEditorComponent());
        nameColumn.setEditorComponent(
                (SerializableFunction<Person, Component>) null);
        Assert.assertNull(nameColumn.getEditorComponent());
    }

}
