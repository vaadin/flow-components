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
package com.vaadin.flow.component.confirmdialog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.flow.internal.nodefeature.PropertyChangeDeniedException;
import com.vaadin.tests.MockUIExtension;

class ConfirmDialogOpenedChangeListenerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private ConfirmDialog dialog;
    private ComponentEventListener<ConfirmDialog.OpenedChangeEvent> mockListener;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        dialog = new ConfirmDialog();

        mockListener = Mockito.mock(ComponentEventListener.class);
        dialog.addOpenedChangeListener(mockListener);
    }

    @Test
    void open() {
        dialog.open();
        assertEventFired(true, false);

        resetMock();
        syncOpenedFromClient(true);
        assertNoEventFired();
    }

    @Test
    void close() {
        dialog.open();
        resetMock();

        dialog.close();
        assertEventFired(false, false);

        resetMock();
        syncOpenedFromClient(false);
        assertNoEventFired();
    }

    @Test
    void closeFromClient() {
        dialog.open();
        resetMock();

        syncOpenedFromClient(false);
        assertEventFired(false, true);

        resetMock();
        dialog.close();
        assertNoEventFired();
    }

    @Test
    void noInitialEvent() {
        syncOpenedFromClient(false);
        assertNoEventFired();
    }

    private void syncOpenedFromClient(boolean opened) {
        try {
            dialog.getElement().getNode().getFeature(ElementPropertyMap.class)
                    .deferredUpdateFromClient("opened", opened).run();
        } catch (PropertyChangeDeniedException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertNoEventFired() {
        Mockito.verify(mockListener, Mockito.never())
                .onComponentEvent(Mockito.any());
    }

    private void assertEventFired(boolean opened, boolean fromClient) {
        Mockito.verify(mockListener, Mockito.times(1)).onComponentEvent(
                Mockito.argThat(event -> event.isOpened() == opened
                        && event.isFromClient() == fromClient));
    }

    @SuppressWarnings("unchecked")
    private void resetMock() {
        Mockito.reset(mockListener);
    }
}
