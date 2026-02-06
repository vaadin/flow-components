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

import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadDropZoneTest {

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
    public void constructor_default_createsDropZone() {
        UploadDropZone dropZone = new UploadDropZone();

        Assert.assertNotNull(dropZone);
        Assert.assertEquals("vaadin-upload-drop-zone",
                dropZone.getElement().getTag());
    }

    @Test
    public void constructor_withManager_linksToManager() {
        UploadDropZone dropZone = new UploadDropZone(manager);

        Assert.assertSame(manager, dropZone.getUploadManager());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNull_throws() {
        new UploadDropZone(null);
    }

    @Test
    public void setUploadManager_linksToManager() {
        UploadDropZone dropZone = new UploadDropZone();

        dropZone.setUploadManager(manager);

        Assert.assertSame(manager, dropZone.getUploadManager());
    }

    @Test
    public void getUploadManager_default_returnsNull() {
        UploadDropZone dropZone = new UploadDropZone();

        Assert.assertNull(dropZone.getUploadManager());
    }

    @Test
    public void implementsHasComponents() {
        Assert.assertTrue(
                HasComponents.class.isAssignableFrom(UploadDropZone.class));
    }

    @Test
    public void implementsHasSize() {
        Assert.assertTrue(HasSize.class.isAssignableFrom(UploadDropZone.class));
    }

    @Test
    public void add_addsChildComponent() {
        UploadDropZone dropZone = new UploadDropZone();
        Span span = new Span("Drop files here");

        dropZone.add(span);

        Assert.assertEquals(1, dropZone.getElement().getChildCount());
    }
}
