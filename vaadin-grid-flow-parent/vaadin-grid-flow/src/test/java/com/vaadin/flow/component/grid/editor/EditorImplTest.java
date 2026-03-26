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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.StatusChangeEvent;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.tests.MockUIExtension;

class EditorImplTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

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

    @BeforeEach
    void setup() {
        grid = new Grid<>();
        ui.add(grid);
        editor = new TestEditor(grid);

        editor.setBinder(new Binder<>());
        grid.getDataCommunicator().getKeyMapper().key("bar");
    }

    @Test()
    void editItem_itemIsNotKnown_noException() {
        try {
            // Edit an item that is not in the grid's active range yet
            editor.editItem("foo");
            ui.fakeClientCommunication();
        } catch (Exception e) {
            Assertions.fail("No exception should be thrown");
        }
    }

    @Test
    void editItem_noBinder_throw() {
        editor = new TestEditor(grid);
        editor.editItem("bar");
        Assertions.assertThrows(IllegalStateException.class, () -> {
            ui.fakeClientCommunication();
        });
    }

    @Test
    void editItem_editorIsBufferedAndOpen_throw() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.setBuffered(true);
        editor.editItem("bar");
        ui.fakeClientCommunication();
        editor.editItem("foo");

        Assertions.assertThrows(IllegalStateException.class, () -> {
            ui.fakeClientCommunication();
        });
    }

    @Test
    void editItem_itemIsKnown_binderStatusEventAndEditorOpenEvent() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> openEventCapure = new AtomicReference<EditorEvent<String>>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is the bean in the binder
        Assertions.assertEquals("bar", editor.getBinder().getBean());
    }

    @Test
    void editItem_itemIsKnown_binderIsInBufferedMode_binderStatusEventAndEditorOpenEvent() {
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> openEventCapure = new AtomicReference<EditorEvent<String>>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is no bean in the binder
        Assertions.assertNull(editor.getBinder().getBean());
    }

    @Test
    void editItem_switchEditedItem_itemsAreRefreshed() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();
        editor.editItem("foo");
        ui.fakeClientCommunication();

        Assertions.assertEquals(2, editor.refreshedItems.size());
        Assertions.assertEquals("bar", editor.refreshedItems.get(0));
        Assertions.assertEquals("foo", editor.refreshedItems.get(1));
    }

    @Test
    void cancel_eventIsFiredAndItemIsRefreshed() {
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

        Assertions.assertNotNull(cancelEventCapture.get());
        Assertions.assertNotNull(closeEventCapture.get());

        Assertions.assertEquals(1, editor.refreshedItems.size());
        Assertions.assertEquals("bar", editor.refreshedItems.get(0));
    }

    @Test
    void save_editorIsNotOpened_noEvents() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        assertNegativeSave(statusEventCapture, saveEventCapture,
                closeEventCapture);
        Assertions.assertEquals(0, editor.refreshedItems.size());

        Assertions.assertNull(statusEventCapture.get());
        Assertions.assertNull(saveEventCapture.get());
        Assertions.assertNull(closeEventCapture.get());
    }

    @Test
    void save_editorIsOpened_editorIsInNotBufferedMode_noEvents() {
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        assertNegativeSave(statusEventCapture, saveEventCapture,
                closeEventCapture);
        Assertions.assertEquals(0, editor.refreshedItems.size());

        Assertions.assertNull(statusEventCapture.get());
        Assertions.assertNull(saveEventCapture.get());
        Assertions.assertNull(closeEventCapture.get());
    }

    @Test
    void save_editorIsOpened_editorIsInBufferedMode_eventsAreFired() {
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> saveEventCapture = new AtomicReference<>();
        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        Assertions.assertTrue(doSave(statusEventCapture, saveEventCapture,
                closeEventCapture));
        Assertions.assertEquals(1, editor.refreshedItems.size());
        Assertions.assertEquals("bar", editor.refreshedItems.get(0));

        Assertions.assertNotNull(statusEventCapture.get());
        Assertions.assertNotNull(saveEventCapture.get());
        Assertions.assertNotNull(closeEventCapture.get());
    }

    @Test
    void save_editorIsOpened_editorIsInBufferedMode_beanIsInvalid_editorIsNotClosed() {
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

        Assertions.assertFalse(doSave(statusEventCapture, saveEventCapture,
                closeEventCapture));
        Assertions.assertEquals(0, editor.refreshedItems.size());

        Assertions.assertNotNull(statusEventCapture.get());
        Assertions.assertNull(saveEventCapture.get());
        Assertions.assertNull(closeEventCapture.get());

        Assertions.assertTrue(statusEventCapture.get().hasValidationErrors());
    }

    @Test
    void editorIsInBufferedMode_closeEditorThrows() {
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();
        editor.setBuffered(true);

        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> editor.closeEditor());

        Assertions.assertNull(closeEventCapture.get(),
                "Received close event even though method should have thrown.");
    }

    @Test
    void editorInUnBufferedMode_closeEditorSendsCloseEvent() {
        editor.editItem("bar");
        ui.fakeClientCommunication();

        editor.refreshedItems.clear();

        AtomicReference<EditorEvent<String>> closeEventCapture = new AtomicReference<>();

        editor.addCloseListener(
                event -> closeEventCapture.compareAndSet(null, event));

        editor.closeEditor();

        Assertions.assertNotNull(closeEventCapture.get(),
                "No close event was fired.");
    }

    private void assertNegativeSave(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorEvent<String>> saveEventCapture,
            AtomicReference<EditorEvent<String>> closeEventCapture) {
        Assertions.assertFalse(doSave(statusEventCapture, saveEventCapture,
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

        Assertions.assertNotNull(statusEventCapture.get());
        Assertions.assertNotNull(openEventCapure.get());

        Assertions.assertEquals("bar", openEventCapure.get().getItem());
    }
}
