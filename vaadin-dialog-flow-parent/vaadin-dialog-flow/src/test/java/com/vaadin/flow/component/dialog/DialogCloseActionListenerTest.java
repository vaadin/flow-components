/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

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
    public void addDialogCloseActionListenerOnClosedDialog_onCloseNotConfigured() {
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
