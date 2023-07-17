package com.vaadin.flow.component.dialog;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

public class DialogOpenedChangeListenerTest {
    private final UI ui = new UI();
    private Dialog dialog;
    private AtomicReference<Dialog.OpenedChangeEvent> event;
    private ComponentEventListener<Dialog.OpenedChangeEvent> mockListener;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        dialog = new Dialog();
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
    public void closeFromClient() {
        dialog.open();
        clearCapturedData();

        dialog.handleClientClose();
        Assert.assertTrue(event.get().isFromClient());
        Assert.assertFalse(event.get().isOpened());
        assertListenerCalls(1);

        clearCapturedData();
        dialog.handleClientClose();
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
