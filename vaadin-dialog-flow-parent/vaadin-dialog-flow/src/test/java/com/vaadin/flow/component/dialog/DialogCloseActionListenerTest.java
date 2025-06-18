/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

public class DialogCloseActionListenerTest {

    private UI ui = new UI();

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void addDialogCloseActionListener_onCloseNotConfigured() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        assertOnCloseConfigured(false);
    }

    @Test
    public void openDialog_onCloseNotConfigured() {
        Dialog dialog = new Dialog();

        dialog.open();

        assertOnCloseConfigured(false);
    }

    @Test
    public void addDialogCloseActionListener_openDialog_onCloseConfigured() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        dialog.open();

        assertOnCloseConfigured(true);
    }

    @Test
    public void openDialog_addDialogCloseActionListener_onCloseConfigured() {
        Dialog dialog = new Dialog();

        dialog.open();

        dialog.addDialogCloseActionListener(event -> {
        });

        assertOnCloseConfigured(true);
    }

    @Test
    public void addDialogCloseActionListener_openDialog_removeListener_onCloseNotConfigured() {
        Dialog dialog = new Dialog();

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        dialog.open();

        registration.remove();

        assertOnCloseConfigured(false);
    }

    @Test
    public void addDialogCloseActionListener_removeListener_openDialog_onCloseNotConfigured() {
        Dialog dialog = new Dialog();

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        registration.remove();

        dialog.open();

        assertOnCloseConfigured(false);
    }

    @Test
    public void addDialogCloseActionListener_openDialog_closeAndReopen_onCloseConfigured() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        dialog.open();

        flushInvocations();

        dialog.close();

        dialog.open();

        assertOnCloseConfigured(true);
    }

    @Test
    public void addTwoDialogCloseActionListeners_openDialog_onCloseConfigured() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        dialog.addDialogCloseActionListener(event -> {
        });

        dialog.open();

        assertOnCloseConfigured(true);
    }

    @Test
    public void addTwoDialogCloseActionListeners_openDialog_removeOneListener_onCloseConfigured() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        dialog.open();

        registration.remove();

        assertOnCloseConfigured(true);
    }

    @Test
    public void addTwoDialogCloseActionListeners_openDialog_closeDialog_removeOneListener_reopenDialog_onCloseConfigured() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        dialog.open();

        flushInvocations();

        dialog.close();

        registration.remove();

        dialog.open();

        assertOnCloseConfigured(true);
    }

    private List<PendingJavaScriptInvocation> flushInvocations() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    private void assertOnCloseConfigured(boolean configured) {
        List<PendingJavaScriptInvocation> invocations = flushInvocations();
        long onCloseConfiguredCount = invocations
                .stream().filter(invocation -> invocation.getInvocation()
                        .getExpression().startsWith("var f = function(e)"))
                .count();
        int expectedCount = configured ? 1 : 0;
        Assert.assertEquals(expectedCount, onCloseConfiguredCount);
    }
}
