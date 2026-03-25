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
package com.vaadin.flow.component.grid.editor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.tests.MockUIExtension;

class EditorBeanTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    public static class MutableBean {
        private String name;

        public MutableBean(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static record ImmutableBean(String name) {
    }

    public static class MutableBeanEditor extends EditorImpl<MutableBean> {
        public MutableBeanEditor(Grid<MutableBean> grid) {
            super(grid, null);
        }
    }

    public static class ImmutableBeanEditor extends EditorImpl<ImmutableBean> {
        public ImmutableBeanEditor(Grid<ImmutableBean> grid) {
            super(grid, null);
        }
    }

    private TextField nameField;

    @BeforeEach
    void init() {
        nameField = new TextField();
    }

    @Test
    void createEditorWithMutableBean_editItem_binderBeanSet() {
        Grid<MutableBean> grid = new Grid<>();
        ui.add(grid);

        Binder<MutableBean> binder = new Binder<>(MutableBean.class);
        binder.forField(nameField).bind("name");

        MutableBeanEditor editor = new MutableBeanEditor(grid);
        editor.setBinder(binder);
        editor.editItem(new MutableBean("foo"));
        ui.fakeClientCommunication();

        Assertions.assertNotNull(binder.getBean());
        Assertions.assertEquals("foo", nameField.getValue());
    }

    @Test
    void createEditorWithMutableBean_setBuffered_editItem_binderBeanRead() {
        Grid<MutableBean> grid = new Grid<>();
        ui.add(grid);

        Binder<MutableBean> binder = new Binder<>(MutableBean.class);
        binder.forField(nameField).bind("name");

        MutableBeanEditor editor = new MutableBeanEditor(grid);
        editor.setBinder(binder);
        editor.setBuffered(true);
        editor.editItem(new MutableBean("foo"));
        ui.fakeClientCommunication();

        Assertions.assertNull(binder.getBean());
        Assertions.assertEquals("foo", nameField.getValue());
    }

    @Test
    void createEditorWithImmutableBean_editItem_binderBeanRead() {
        Grid<ImmutableBean> grid = new Grid<>();
        ui.add(grid);

        Binder<ImmutableBean> binder = new Binder<>(ImmutableBean.class);
        binder.forField(nameField).bind("name");

        ImmutableBeanEditor editor = new ImmutableBeanEditor(grid);
        editor.setBinder(binder);
        editor.editItem(new ImmutableBean("foo"));
        ui.fakeClientCommunication();

        Assertions.assertNull(binder.getBean());
        Assertions.assertEquals("foo", nameField.getValue());
    }
}
