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
package com.vaadin.flow.component.popover;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.VaadinSession;

/**
 * Test to verify that popovers work correctly in modal contexts (like dialogs)
 * by using the appropriate attachment strategy.
 */
public class PopoverModalContextTest {

    private UI ui;
    private VaadinSession session;

    @Before
    public void setUp() {
        ui = new UI();
        session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        VaadinSession.setCurrent(session);
        Mockito.when(session.getErrorHandler()).thenReturn(event -> {
            throw new RuntimeException(event.getThrowable());
        });
        UI.setCurrent(ui);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
        VaadinSession.setCurrent(null);
    }

    @Test
    public void popoverInModalDialog_usesLegacyAttachmentBehavior() {
        // Create a dialog component with a target element
        Dialog dialog = new Dialog();
        Div target = new Div();
        target.setText("Target in dialog");
        dialog.add(target);

        // Create popover targeting the div
        Popover popover = new Popover();
        popover.add(new Span("Popover content"));
        popover.setTarget(target);

        // Add dialog to UI to trigger attachment
        ui.add(dialog);

        // Verify popover is attached to the UI tree
        Assert.assertTrue("Popover should be attached to UI tree",
                popover.getElement().getNode().getParent() != null);

        // In modal context, popover should be appended to target's parent for
        // proper DOM structure
        Assert.assertEquals("Popover should be child of dialog",
                dialog.getElement(), popover.getElement().getParent());
    }

    @Test
    public void popoverInNonModalContext_usesModalComponentAttachment() {
        // Create a regular container (non-modal context)
        Div container = new Div();
        Div target = new Div();
        target.setText("Target in regular container");
        container.add(target);

        // Create popover targeting the div
        Popover popover = new Popover();
        popover.add(new Span("Popover content"));
        popover.setTarget(target);

        // Add container to UI to trigger attachment
        ui.add(container);

        // Verify popover is attached to UI tree
        Assert.assertTrue("Popover should be attached to UI tree",
                popover.getElement().getNode().getParent() != null);

        // In non-modal context, popover uses UI modal container instead of
        // target's parent
        Assert.assertNotEquals("Popover should not be child of container",
                container.getElement(), popover.getElement().getParent());
    }

}
