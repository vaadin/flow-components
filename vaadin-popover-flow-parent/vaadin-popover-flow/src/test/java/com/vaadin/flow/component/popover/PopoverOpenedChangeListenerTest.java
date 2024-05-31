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
package com.vaadin.flow.component.popover;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

public class PopoverOpenedChangeListenerTest {
    private final UI ui = new UI();
    private Popover popover;
    private AtomicReference<Popover.OpenedChangeEvent> event;
    private ComponentEventListener<Popover.OpenedChangeEvent> mockListener;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        popover = new Popover();
        ui.add(popover);

        event = new AtomicReference<>();
        popover.addOpenedChangeListener(event::set);

        mockListener = Mockito.mock(ComponentEventListener.class);
        popover.addOpenedChangeListener(mockListener);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void open() {
        popover.open();

        Assert.assertFalse(event.get().isFromClient());
        Assert.assertTrue(event.get().isOpened());
        assertListenerCalls(1);

        clearCapturedData();
        popover.open();
        Assert.assertNull(event.get());
        assertListenerCalls(0);
    }

    @Test
    public void close() {
        popover.open();
        clearCapturedData();

        popover.close();
        Assert.assertFalse(event.get().isFromClient());
        Assert.assertFalse(event.get().isOpened());
        assertListenerCalls(1);

        clearCapturedData();
        popover.close();
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
