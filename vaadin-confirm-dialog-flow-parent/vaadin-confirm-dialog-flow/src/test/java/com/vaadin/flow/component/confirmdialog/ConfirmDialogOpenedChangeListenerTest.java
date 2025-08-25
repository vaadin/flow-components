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
package com.vaadin.flow.component.confirmdialog;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

public class ConfirmDialogOpenedChangeListenerTest {
    private final UI ui = new UI();
    private ConfirmDialog dialog;
    private AtomicReference<ConfirmDialog.OpenedChangeEvent> event;
    private ComponentEventListener<ConfirmDialog.OpenedChangeEvent> mockListener;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        dialog = new ConfirmDialog();
        event = new AtomicReference<>();
        dialog.addOpenedChangeListener(event::set);

        mockListener = Mockito.mock(ComponentEventListener.class);
        dialog.addOpenedChangeListener(mockListener);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void open() {
        dialog.open();

        Assert.assertFalse(event.get().isFromClient());
        Assert.assertTrue(event.get().isOpened());
        assertListenerCalls(1);

        clearCapturedData();
        dialog.open();
        Assert.assertNull(event.get());
        assertListenerCalls(0);
    }

    @Test
    public void close() {
        dialog.open();
        clearCapturedData();

        dialog.close();
        Assert.assertFalse(event.get().isFromClient());
        Assert.assertFalse(event.get().isOpened());
        assertListenerCalls(1);

        clearCapturedData();
        dialog.close();
        Assert.assertNull(event.get());
        assertListenerCalls(0);
    }

    @Test
    public void openFromClient() {
        dialog.handleClientSideOpenedStateChange(true);
        Assert.assertTrue(event.get().isFromClient());
        Assert.assertTrue(event.get().isOpened());
        assertListenerCalls(1);

        clearCapturedData();
        dialog.handleClientSideOpenedStateChange(true);
        Assert.assertNull(event.get());
        assertListenerCalls(0);
    }

    @Test
    public void closeFromClient() {
        dialog.open();
        clearCapturedData();

        dialog.handleClientSideOpenedStateChange(false);
        Assert.assertTrue(event.get().isFromClient());
        Assert.assertFalse(event.get().isOpened());
        assertListenerCalls(1);

        clearCapturedData();
        dialog.handleClientSideOpenedStateChange(false);
        Assert.assertNull(event.get());
        assertListenerCalls(0);
    }

    private void assertListenerCalls(int expectedCount) {
        Mockito.verify(mockListener, Mockito.times(expectedCount))
                .onComponentEvent(Mockito.any());
    }

    private void clearCapturedData() {
        event.set(null);
        Mockito.reset(mockListener);
    }
}
