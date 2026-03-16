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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.StatusChangeEvent;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.tests.MockUIRule;

public class EditorImplTest {
    @Rule
    public final MockUIRule ui = new MockUIRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    public void setup() {
        grid = new Grid<>();
        ui.add(grid);
        editor = new TestEditor(grid);

        editor.setBinder(new Binder<>());
        grid.getDataCommunicator().getKeyMapper().key("bar");
    }

    @Test()
    public void editItem_itemIsNotKnown_noException() {
        try {
            // Edit an item that is not in the grid's active range yet
            editor.editItem("foo");
            ui.fakeClientCommunication();
        } catch (Exception e) {
            Assert.fail("No exception should be thrown");
        }
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_noBinder_throw() {
        editor = new TestEditor(grid);
        editor.editItem("bar");

        ui.fakeClientCommunication();
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_editorIsBufferedAndOpen_throw() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.setBuffered(true);
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.editItem("foo");
        ui.fakeClientCommunication();
    }

    @Test
    public void editItem_itemIsKnown_binderStatusEventAndEditorOpenEvent() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> openEventCapure = new AtomicReference<EditorEvent<String>>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is the bean in the binder
        Assert.assertEquals("bar", editor.getBinder().getBean());
    }

    @Test
    public void editItem_itemIsKnown_binderIsInBufferedMode_binderStatusEventAndEditorOpenEvent() {
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> openEventCapure = new AtomicReference<EditorEvent<String>>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is no bean in the binder
        Assert.assertNull("bar", editor.getBinder().getBean());
    }

    @Test
    public void editItem_switchEditedItem_itemsAreRefreshed() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();
        editor.editItem("foo");
        ui.fakeClientCommunication();

        Assert.assertEquals(2, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));
        Assert.assertEquals("foo", editor.refreshedItems.get(1));
    }

    @Test
    public void cancel_eventIsFiredAndItemIsRefreshed() {
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();

        AtomicReference<EditorEvent<String>> cancelEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();
        editor.addCancelListener(
                event -> cancelEventCapture.compareAndSet(null, event));
        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));
        editor.cancel();

        Assert.assertNotNull(cancelEventCapture.get());
        Assert.assertNotNull(closeEventCapture.get());

        Assert.assertEquals(1, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));
    }

    @Test
    public void save_editorIsNotOpened_noEvents() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        assertNegativeSave(statusEventCapture, saveEventCapture,
                closeEventCapture);
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapture.get());
        Assert.assertNull(closeEventCapture.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInNotBufferedMode_noEvents() {
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        assertNegativeSave(statusEventCapture, saveEventCapture,
                closeEventCapture);
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapture.get());
        Assert.assertNull(closeEventCapture.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInBufferedMode_eventsAreFired() {
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        Assert.assertTrue(doSave(statusEventCapture, saveEventCapture,
                closeEventCapture));
        Assert.assertEquals(1, editor.refreshedItems.size());
        Assert.assertEquals("bar", editor.refreshedItems.get(0));

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNotNull(saveEventCapture.get());
        Assert.assertNotNull(closeEventCapture.get());
    }

    @Test
    public void save_editorIsOpened_editorIsInBufferedMode_beanIsInvalid_editorIsNotClosed() {
        editor.getBinder().forField(new TextField())
                .withValidator(value -> !value.equals("bar"), "")
                .bind(ValueProvider.identity(), (item, value) -> {
                });
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        Assert.assertFalse(doSave(statusEventCapture, saveEventCapture,
                closeEventCapture));
        Assert.assertEquals(0, editor.refreshedItems.size());

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNull(saveEventCapture.get());
        Assert.assertNull(closeEventCapture.get());

        Assert.assertTrue(statusEventCapture.get().hasValidationErrors());
    }

    @Test
    public void editorIsInBufferedMode_closeEditorThrows() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.reportMissingExceptionWithMessage(
                "Buffered editor should be closed using save() or cancel()");

        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));

        editor.closeEditor();

        Assert.assertNull(
                "Received close event even though method should have thrown.",
                closeEventCapture.get());
    }

    @Test
    public void editorInUnBufferedMode_closeEditorSendsCloseEvent() {
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();

        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));

        editor.closeEditor();

        Assert.assertNotNull("No close event was fired.",
                closeEventCapture.get());
    }

    private void assertNegativeSave(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorEvent<String>> saveEventCapture,
            AtomicReference<EditorEvent<String>> closeEventCapture) {
        Assert.assertFalse(doSave(statusEventCapture, saveEventCapture,
                closeEventCapture));
    }

    private boolean doSave(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorEvent<String>> saveEventCapture,
            AtomicReference<EditorEvent<String>> closeEventCapture) {
        editor.getBinder().addStatusChangeListener(
                event -> statusEventCapture.compareAndSet(null, event));
        editor.addSaveListener(
                event -> saveEventCapture.compareAndSet(null, event));
        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));

        return editor.save();
    }

    private void assertOpenEvents(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorEvent<String>> openEventCapure) {
        editor.getBinder().addStatusChangeListener(
                event -> statusEventCapture.compareAndSet(null, event));

        editor.addOpenListener(
                event -> openEventCapure.compareAndSet(null, event));
        editor.editItem("bar");
        ui.fakeClientCommunication();

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNotNull(openEventCapure.get());

        Assert.assertEquals("bar", openEventCapure.get().getItem());
    }
}
