/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
