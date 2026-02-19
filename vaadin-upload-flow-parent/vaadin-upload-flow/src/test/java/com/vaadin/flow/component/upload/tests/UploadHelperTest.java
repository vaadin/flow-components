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
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.ModularUploadFeatureFlagProvider;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadHelper;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.tests.EnableFeatureFlagRule;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadHelperTest {
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            ModularUploadFeatureFlagProvider.MODULAR_UPLOAD);

    private Div owner;

    @Before
    public void setup() {
        var ui = Mockito.spy(new UI());
        UI.setCurrent(ui);
        var mockSession = Mockito.mock(VaadinSession.class);
        var mockService = Mockito.mock(VaadinService.class);
        var streamResourceRegistry = new StreamResourceRegistry(mockSession);
        Mockito.when(mockSession.getResourceRegistry())
                .thenReturn(streamResourceRegistry);
        Mockito.when(mockSession.access(Mockito.any()))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, Command.class).execute();
                    return new CompletableFuture<>();
                });
        Mockito.when(mockSession.getService()).thenReturn(mockService);
        ui.getInternals().setSession(mockSession);

        owner = new Div();
        ui.add(owner);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void hasUploadHandler_withDefaultConstructor_returnsFalse() {
        var manager = new UploadManager(owner);
        Assert.assertFalse(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    public void hasUploadHandler_withHandlerInConstructor_returnsTrue() {
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        var manager = new UploadManager(owner, handler);
        Assert.assertTrue(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    public void hasUploadHandler_withDefaultConstructor_setHandler_returnsTrue() {
        var manager = new UploadManager(owner);
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);
        Assert.assertTrue(UploadHelper.hasUploadHandler(manager));
    }

    @Test
    public void hasUploadHandler_upload_withDefaultConstructor_returnsFalse() {
        var upload = new Upload();
        Assert.assertFalse(UploadHelper.hasUploadHandler(upload));
    }

    @Test
    public void hasUploadHandler_upload_withHandlerInConstructor_returnsTrue() {
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        var upload = new Upload(handler);
        Assert.assertTrue(UploadHelper.hasUploadHandler(upload));
    }

    @Test
    public void hasUploadHandler_upload_withDefaultConstructor_setHandler_returnsTrue() {
        var upload = new Upload();
        var handler = UploadHandler.inMemory((metadata, data) -> {
        });
        upload.setUploadHandler(handler);
        Assert.assertTrue(UploadHelper.hasUploadHandler(upload));
    }
}
