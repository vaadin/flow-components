/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.VaadinSession;

public class EditorBeanTest {

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

    public static class MockUI extends UI {

        public MockUI() {
            getInternals().setSession(mock(VaadinSession.class));
        }
    }

    private MockUI ui;
    private TextField nameField;

    @Before
    public void init() {
        ui = new MockUI();
        nameField = new TextField();
    }

    @Test
    public void createEditorWithMutableBean_editItem_binderBeanSet() {
        Grid<MutableBean> grid = new Grid<>();
        ui.getElement().appendChild(grid.getElement());

        Binder<MutableBean> binder = new Binder<>(MutableBean.class);
        binder.forField(nameField).bind("name");

        MutableBeanEditor editor = new MutableBeanEditor(grid);
        editor.setBinder(binder);
        editor.editItem(new MutableBean("foo"));
        fakeClientResponse();

        Assert.assertNotNull(binder.getBean());
        Assert.assertEquals(nameField.getValue(), "foo");
    }

    @Test
    public void createEditorWithMutableBean_setBuffered_editItem_binderBeanRead() {
        Grid<MutableBean> grid = new Grid<>();
        ui.getElement().appendChild(grid.getElement());

        Binder<MutableBean> binder = new Binder<>(MutableBean.class);
        binder.forField(nameField).bind("name");

        MutableBeanEditor editor = new MutableBeanEditor(grid);
        editor.setBinder(binder);
        editor.setBuffered(true);
        editor.editItem(new MutableBean("foo"));
        fakeClientResponse();

        Assert.assertNull(binder.getBean());
        Assert.assertEquals(nameField.getValue(), "foo");
    }

    @Test
    public void createEditorWithImmutableBean_editItem_binderBeanRead() {
        Grid<ImmutableBean> grid = new Grid<>();
        ui.getElement().appendChild(grid.getElement());

        Binder<ImmutableBean> binder = new Binder<>(ImmutableBean.class);
        binder.forField(nameField).bind("name");

        ImmutableBeanEditor editor = new ImmutableBeanEditor(grid);
        editor.setBinder(binder);
        editor.editItem(new ImmutableBean("foo"));
        fakeClientResponse();

        Assert.assertNull(binder.getBean());
        Assert.assertEquals(nameField.getValue(), "foo");
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
