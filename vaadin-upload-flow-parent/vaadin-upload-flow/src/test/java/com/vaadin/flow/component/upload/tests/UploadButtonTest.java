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
package com.vaadin.flow.component.upload.tests;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.upload.ModularUploadFeatureFlagProvider;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class UploadButtonTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            ModularUploadFeatureFlagProvider.MODULAR_UPLOAD);

    private Div owner;
    private UploadManager manager;

    @BeforeEach
    void setup() {
        owner = new Div();
        ui.add(owner);
        manager = new UploadManager(owner);
    }

    @Test
    void constructor_default_createsButton() {
        UploadButton button = new UploadButton();

        Assertions.assertNotNull(button);
        Assertions.assertEquals("vaadin-upload-button",
                button.getElement().getTag());
    }

    @Test
    void constructor_withManager_linksToManager() {
        UploadButton button = new UploadButton(manager);

        Assertions.assertSame(manager, button.getUploadManager());
    }

    @Test
    void constructor_withTextAndManager_setsTextAndLinksToManager() {
        UploadButton button = new UploadButton("Upload", manager);

        Assertions.assertEquals("Upload", button.getText());
        Assertions.assertSame(manager, button.getUploadManager());
    }

    @Test
    void constructor_withNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new UploadButton(null));
    }

    @Test
    void constructor_withTextAndNullManager_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new UploadButton("Upload", null));
    }

    @Test
    void setUploadManager_linksToManager() {
        UploadButton button = new UploadButton();

        button.setUploadManager(manager);

        Assertions.assertSame(manager, button.getUploadManager());
    }

    @Test
    void getUploadManager_default_returnsNull() {
        UploadButton button = new UploadButton();

        Assertions.assertNull(button.getUploadManager());
    }

    @Test
    void extendsButton() {
        Assertions
                .assertTrue(Button.class.isAssignableFrom(UploadButton.class));
    }

    @Test
    void setUploadManager_changeManager_detachAndReattach_onlyOneManagerLinkJsExecuted() {
        // Create two managers
        Div owner2 = new Div();
        ui.add(owner2);
        UploadManager manager2 = new UploadManager(owner2);

        // Create button with first manager
        UploadButton button = new UploadButton(manager);
        ui.add(button);
        ui.fakeClientCommunication();

        // Change to second manager
        button.setUploadManager(manager2);
        ui.fakeClientCommunication();

        // Drain pending JS invocations
        ui.dumpPendingJavaScriptInvocations();

        // Detach and reattach the button
        ui.remove(button);
        ui.add(button);

        // Get pending JS invocations after reattach
        List<PendingJavaScriptInvocation> pendingInvocations = ui
                .dumpPendingJavaScriptInvocations();

        // Count how many "this.manager = " JS invocations are pending
        long managerLinkCount = pendingInvocations.stream().filter(inv -> inv
                .getInvocation().getExpression().contains("this.manager"))
                .count();

        Assertions.assertEquals(1, managerLinkCount,
                "Only one 'this.manager' JS invocation should be pending after reattach. "
                        + "Old attach listeners should be removed when setUploadManager is called.");

        // Verify that the button is linked to manager2
        Assertions.assertSame(manager2, button.getUploadManager());
    }
}
