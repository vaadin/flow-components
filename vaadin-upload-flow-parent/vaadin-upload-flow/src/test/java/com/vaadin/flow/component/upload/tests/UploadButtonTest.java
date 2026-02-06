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
import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadButtonTest {

    private UI ui;
    private Div owner;
    private UploadManager manager;
    private MockedStatic<FeatureFlags> mockFeatureFlagsStatic;

    @Before
    public void setup() {
        ui = Mockito.spy(new UI());
        UI.setCurrent(ui);

        // Mock feature flags to enable the upload manager component
        FeatureFlags mockFeatureFlags = Mockito.mock(FeatureFlags.class);
        mockFeatureFlagsStatic = Mockito.mockStatic(FeatureFlags.class);
        Mockito.when(mockFeatureFlags.isEnabled(UploadManager.FEATURE_FLAG_ID))
                .thenReturn(true);

        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        VaadinService mockService = Mockito.mock(VaadinService.class);
        VaadinContext mockContext = Mockito.mock(VaadinContext.class);
        StreamResourceRegistry streamResourceRegistry = new StreamResourceRegistry(
                mockSession);
        Mockito.when(mockSession.getResourceRegistry())
                .thenReturn(streamResourceRegistry);
        Mockito.when(mockSession.access(Mockito.any()))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, Command.class).execute();
                    return new CompletableFuture<>();
                });
        Mockito.when(mockSession.getService()).thenReturn(mockService);
        Mockito.when(mockService.getContext()).thenReturn(mockContext);
        mockFeatureFlagsStatic.when(() -> FeatureFlags.get(mockContext))
                .thenReturn(mockFeatureFlags);
        ui.getInternals().setSession(mockSession);

        owner = new Div();
        ui.add(owner);
        manager = new UploadManager(owner);
    }

    @After
    public void tearDown() {
        mockFeatureFlagsStatic.close();
        UI.setCurrent(null);
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

    @Test(expected = NullPointerException.class)
    public void constructor_withNull_throws() {
        new UploadButton(null);
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
    public void setCapture_user_propertyIsSet() {
        UploadButton button = new UploadButton();

        button.setCapture("user");

        Assert.assertEquals("user", button.getCapture());
    }

    @Test
    public void setCapture_environment_propertyIsSet() {
        UploadButton button = new UploadButton();

        button.setCapture("environment");

        Assert.assertEquals("environment", button.getCapture());
    }

    @Test
    public void getCapture_default_returnsNull() {
        UploadButton button = new UploadButton();

        Assert.assertNull(button.getCapture());
    }

    @Test
    public void setCapture_null_removesProperty() {
        UploadButton button = new UploadButton();
        button.setCapture("user");

        button.setCapture(null);

        Assert.assertNull(button.getCapture());
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
        fakeClientResponse();

        // Change to second manager
        button.setUploadManager(manager2);
        fakeClientResponse();

        // Drain pending JS invocations
        ui.getInternals().dumpPendingJavaScriptInvocations();

        // Detach and reattach the button
        ui.remove(button);
        ui.add(button);

        // Get pending JS invocations after reattach
        List<PendingJavaScriptInvocation> pendingInvocations = getPendingJavaScriptInvocations();

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

    private List<PendingJavaScriptInvocation> getPendingJavaScriptInvocations() {
        fakeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
