/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DialogCloseActionListenerTest {

    private final UI ui = new UI();
    private Dialog dialog;
    private ComponentEventListener<Dialog.DialogCloseActionEvent> mockListener;

    @SuppressWarnings({ "unchecked" })
    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        dialog = new Dialog();
        mockListener = Mockito.mock(ComponentEventListener.class);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addListener_open_closeFromClient_staysOpen() {
        dialog.addDialogCloseActionListener(mockListener);
        dialog.open();
        dialog.handleClientClose();

        Assert.assertTrue(dialog.isOpened());
        assertListenerCalls(1);
    }

    @Test
    public void open_addListener_closeFromClient_staysOpen() {
        dialog.open();
        dialog.addDialogCloseActionListener(mockListener);
        dialog.handleClientClose();

        Assert.assertTrue(dialog.isOpened());
        assertListenerCalls(1);
    }

    @Test
    public void addListener_open_removeListener_closeFromClient_closes() {
        Registration registration = dialog
                .addDialogCloseActionListener(mockListener);
        dialog.open();
        registration.remove();
        dialog.handleClientClose();

        Assert.assertFalse(dialog.isOpened());
        assertListenerCalls(0);
    }

    @Test
    public void addListener_open_closeFromServer_closes() {
        dialog.addDialogCloseActionListener(mockListener);
        dialog.open();
        dialog.close();

        Assert.assertFalse(dialog.isOpened());
        assertListenerCalls(0);
    }

    @Test
    public void addMultipleListeners_open_removeOneListener_closeFromClient_staysOpen() {
        Registration registration = dialog
                .addDialogCloseActionListener(mockListener);
        dialog.addDialogCloseActionListener(mockListener);
        dialog.open();
        registration.remove();

        dialog.handleClientClose();

        Assert.assertTrue(dialog.isOpened());
        assertListenerCalls(1);
    }

    @Test
    public void addMultipleListeners_open_removeAllListeners_closeFromClient_closes() {
        Registration registration1 = dialog
                .addDialogCloseActionListener(mockListener);
        Registration registration2 = dialog
                .addDialogCloseActionListener(mockListener);
        dialog.open();
        registration1.remove();
        registration2.remove();
        dialog.handleClientClose();

        Assert.assertFalse(dialog.isOpened());
        assertListenerCalls(0);
    }

    @Test
    public void addListener_openAndCloseMultipleTimes() {
        Registration registration = dialog
                .addDialogCloseActionListener(mockListener);
        dialog.open();
        dialog.handleClientClose();

        // Should not close automatically
        Assert.assertTrue(dialog.isOpened());
        assertListenerCalls(1);

        // Close manually
        dialog.close();

        // Open and close again
        dialog.open();
        dialog.handleClientClose();

        // Should still stay open
        Assert.assertTrue(dialog.isOpened());
        assertListenerCalls(2);

        // Close manually and remove listener
        dialog.close();
        registration.remove();

        // Open and close again
        dialog.open();
        dialog.handleClientClose();

        // Should close
        Assert.assertFalse(dialog.isOpened());
        assertListenerCalls(2);
    }

    private void assertListenerCalls(int expectedCount) {
        Mockito.verify(mockListener, Mockito.times(expectedCount))
                .onComponentEvent(Mockito.any());
    }
}
