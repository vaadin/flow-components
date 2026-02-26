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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.upload.ModularUploadFeatureFlagProvider;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.tests.EnableFeatureFlagRule;
import com.vaadin.tests.MockUIRule;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadButtonTest {
    @Rule
    public MockUIRule ui = new MockUIRule();
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            ModularUploadFeatureFlagProvider.MODULAR_UPLOAD);

    private Div owner;
    private UploadManager manager;

    @Before
    public void setup() {
        owner = new Div();
        ui.add(owner);
        manager = new UploadManager(owner);
    }

    @Test
    public void constructor_default_createsButton() {
        UploadButton button = new UploadButton();

        Assert.assertNotNull(button);
        Assert.assertEquals("vaadin-upload-button",
                button.getElement().getTag());
    }

    @Test
    public void constructor_withManager_linksToManager() {
        UploadButton button = new UploadButton(manager);

        Assert.assertSame(manager, button.getUploadManager());
    }

    @Test
    public void constructor_withTextAndManager_setsTextAndLinksToManager() {
        UploadButton button = new UploadButton("Upload", manager);

        Assert.assertEquals("Upload", button.getText());
        Assert.assertSame(manager, button.getUploadManager());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNull_throws() {
        new UploadButton(null);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withTextAndNullManager_throws() {
        new UploadButton("Upload", null);
    }

    @Test
    public void setUploadManager_linksToManager() {
        UploadButton button = new UploadButton();

        button.setUploadManager(manager);

        Assert.assertSame(manager, button.getUploadManager());
    }

    @Test
    public void getUploadManager_default_returnsNull() {
        UploadButton button = new UploadButton();

        Assert.assertNull(button.getUploadManager());
    }

    @Test
    public void extendsButton() {
        Assert.assertTrue(Button.class.isAssignableFrom(UploadButton.class));
    }

    @Test
    public void setUploadManager_changeManager_detachAndReattach_onlyOneManagerLinkJsExecuted() {
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

        Assert.assertEquals(
                "Only one 'this.manager' JS invocation should be pending after reattach. "
                        + "Old attach listeners should be removed when setUploadManager is called.",
                1, managerLinkCount);

        // Verify that the button is linked to manager2
        Assert.assertSame(manager2, button.getUploadManager());
    }
}
