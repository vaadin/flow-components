/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.StatusChangeEvent;
import com.vaadin.flow.function.ValueProvider;

public class EditorImplTest {

    private Grid<String> grid;
    private TestEditor editor;

    private static class TestEditor extends EditorImpl<String> {

        private List<String> refreshedItems = new ArrayList<>();

        public TestEditor(Grid<String> grid) {
            super(grid, null);
        }

        @Override
        protected void refresh(String item) {
            refreshedItems.add(item);
        }
    }

    @Before
    public void setUp() {
        grid = new Grid<>();
        editor = new TestEditor(grid);

        editor.setBinder(new Binder<>());
        grid.getDataCommunicator().getKeyMapper().key("bar");
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_itemIsNotKnown_throw() {
        editor.editItem("foo");
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_noBinder_throw() {
        editor = new TestEditor(grid);
        editor.editItem("bar");
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_editorIsBufferedAndOpen_throw() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.setBuffered(true);
        editor.editItem("bar");

        editor.editItem("foo");
    }

    @Test
    public void editItem_itemIsKnown_binderStatusEventAndEditorOpenEvent() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorOpenEvent<String>> openEventCapure = new AtomicReference<EditorOpenEvent<String>>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is the bean in the binder
        Assert.assertEquals("bar", editor.getBinder().getBean());
    }

    @Test
    public void editItem_itemIsKnown_binderIsInBufferedMode_binderStatusEventAndEditorOpenEvent() {
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorOpenEvent<String>> openEventCapure = new AtomicReference<EditorOpenEvent<String>>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is no bean in the binder
        Assert.assertNull("bar", editor.getBinder().getBean());
    }

    @Test
    public void editItem_switchEditedItem_itemsAreRefreshed() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.editItem("bar");
        editor.refreshedItems.clear();
        editor.editItem("foo");

        Assert.assertEquals(2, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));
        Assert.assertEquals("foo", editor.refreshedItems.get(1));
    }

    @Test
    public void cancel_eventIsFiredAndItemIsRefreshed() {
        editor.editItem("bar");
        editor.refreshedItems.clear();

        AtomicReference<EditorCancelEvent<String>> cancelEventCapure = new AtomicReference<EditorCancelEvent<String>>();
        editor.addCancelListener(
                event -> cancelEventCapure.compareAndSet(null, event));
        editor.cancel();

        Assert.assertNotNull(cancelEventCapure.get());

        Assert.assertEquals(1, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));
    }

    @Test
    public void save_editorIsNotOpened_noEvents() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorSaveEvent<String>> saveEventCapure = new AtomicReference<EditorSaveEvent<String>>();

        assertNegativeSave(statusEventCapture, saveEventCapure);
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapure.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInNotBufferedMode_noEvents() {
        editor.editItem("bar");
        editor.refreshedItems.clear();

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorSaveEvent<String>> saveEventCapure = new AtomicReference<EditorSaveEvent<String>>();

        assertNegativeSave(statusEventCapture, saveEventCapure);
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapure.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInBufferedMode_eventsAreFired() {
        editor.editItem("bar");
        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorSaveEvent<String>> saveEventCapure = new AtomicReference<EditorSaveEvent<String>>();

        Assert.assertTrue(doSave(statusEventCapture, saveEventCapure));
        Assert.assertEquals(1, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNotNull(saveEventCapure.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInBufferedMode_beanIsInvalid_editorIsNotClosed() {
        editor.getBinder().forField(new TextField())
                .withValidator(value -> !value.equals("bar"), "")
                .bind(ValueProvider.identity(), (item, value) -> {
                });
        editor.editItem("bar");
        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorSaveEvent<String>> saveEventCapure = new AtomicReference<EditorSaveEvent<String>>();

        Assert.assertFalse(doSave(statusEventCapture, saveEventCapure));
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapure.get());

        Assert.assertTrue(statusEventCapture.get().hasValidationErrors());
    }

    private void assertNegativeSave(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorSaveEvent<String>> saveEventCapure) {
        Assert.assertFalse(doSave(statusEventCapture, saveEventCapure));
    }

    private boolean doSave(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorSaveEvent<String>> saveEventCapure) {
        editor.getBinder().addStatusChangeListener(
                event -> statusEventCapture.compareAndSet(null, event));
        editor.addSaveListener(
                event -> saveEventCapure.compareAndSet(null, event));

        return editor.save();
    }

    private void assertOpenEvents(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorOpenEvent<String>> openEventCapure) {
        editor.getBinder().addStatusChangeListener(
                event -> statusEventCapture.compareAndSet(null, event));

        editor.addOpenListener(
                event -> openEventCapure.compareAndSet(null, event));
        editor.editItem("bar");

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNotNull(openEventCapure.get());

        Assert.assertEquals("bar", openEventCapure.get().getBean());
    }
}
