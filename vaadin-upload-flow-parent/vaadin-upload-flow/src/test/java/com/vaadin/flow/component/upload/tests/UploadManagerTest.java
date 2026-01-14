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
package com.vaadin.flow.component.upload.tests;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.UploadHandler;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadManagerTest {

    private UI ui;
    private Div owner;
    private UploadManager manager;

    @Before
    public void setup() {
        ui = Mockito.spy(new UI());
        UI.setCurrent(ui);

        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        StreamResourceRegistry streamResourceRegistry = new StreamResourceRegistry(
                mockSession);
        Mockito.when(mockSession.getResourceRegistry())
                .thenReturn(streamResourceRegistry);
        Mockito.when(mockSession.access(Mockito.any()))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, Command.class).execute();
                    return new CompletableFuture<>();
                });
        ui.getInternals().setSession(mockSession);

        owner = new Div();
        ui.add(owner);
        manager = new UploadManager(owner);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void constructor_withOwner_createsManager() {
        Assert.assertNotNull(manager);
        Assert.assertSame(owner, manager.getOwner());
    }

    @Test
    public void constructor_withOwnerAndHandler_setsHandler() {
        UploadHandler handler = UploadHandler
                .inMemory((metadata, data) -> {
                });

        UploadManager managerWithHandler = new UploadManager(owner, handler);

        Assert.assertSame(handler, managerWithHandler.getUploadHandler());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNullOwner_throws() {
        new UploadManager(null);
    }

    @Test
    public void getUploadHandler_withoutSettingHandler_returnsNull() {
        Assert.assertNull(manager.getUploadHandler());
    }

    @Test
    public void setUploadHandler_setsHandler() {
        UploadHandler handler = UploadHandler
                .inMemory((metadata, data) -> {
                });

        manager.setUploadHandler(handler);

        Assert.assertSame(handler, manager.getUploadHandler());
    }

    @Test(expected = NullPointerException.class)
    public void setUploadHandler_withNull_throws() {
        manager.setUploadHandler(null);
    }

    @Test(expected = NullPointerException.class)
    public void setUploadHandler_withNullTargetName_throws() {
        UploadHandler handler = UploadHandler
                .inMemory((metadata, data) -> {
                });
        manager.setUploadHandler(handler, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUploadHandler_withBlankTargetName_throws() {
        UploadHandler handler = UploadHandler
                .inMemory((metadata, data) -> {
                });
        manager.setUploadHandler(handler, "   ");
    }

    @Test
    public void setUploadHandler_withCustomTargetName_setsHandler() {
        UploadHandler handler = UploadHandler
                .inMemory((metadata, data) -> {
                });

        manager.setUploadHandler(handler, "custom-upload");

        Assert.assertSame(handler, manager.getUploadHandler());
    }

    @Test
    public void setMaxFiles_setsProperty() {
        manager.setMaxFiles(5);

        Assert.assertEquals(5, manager.getMaxFiles());
    }

    @Test
    public void setMaxFiles_withZero_meansUnlimited() {
        manager.setMaxFiles(0);

        Assert.assertEquals(0, manager.getMaxFiles());
    }

    @Test
    public void getMaxFiles_defaultIsZero() {
        Assert.assertEquals(0, manager.getMaxFiles());
    }

    @Test
    public void setMaxFileSize_setsProperty() {
        manager.setMaxFileSize(1024 * 1024); // 1MB

        Assert.assertEquals(1024 * 1024, manager.getMaxFileSize());
    }

    @Test
    public void setMaxFileSize_withZero_meansUnlimited() {
        manager.setMaxFileSize(0);

        Assert.assertEquals(0, manager.getMaxFileSize());
    }

    @Test
    public void getMaxFileSize_defaultIsZero() {
        Assert.assertEquals(0, manager.getMaxFileSize());
    }

    @Test
    public void setAcceptedFileTypes_setsProperty() {
        manager.setAcceptedFileTypes("image/*", "application/pdf");

        List<String> accepted = manager.getAcceptedFileTypes();
        Assert.assertEquals(2, accepted.size());
        Assert.assertTrue(accepted.contains("image/*"));
        Assert.assertTrue(accepted.contains("application/pdf"));
    }

    @Test
    public void setAcceptedFileTypes_withNull_clearsRestrictions() {
        manager.setAcceptedFileTypes("image/*");
        manager.setAcceptedFileTypes((String[]) null);

        Assert.assertTrue(manager.getAcceptedFileTypes().isEmpty());
    }

    @Test
    public void getAcceptedFileTypes_defaultIsEmpty() {
        Assert.assertTrue(manager.getAcceptedFileTypes().isEmpty());
    }

    @Test
    public void setAutoUpload_setsProperty() {
        manager.setAutoUpload(false);

        Assert.assertFalse(manager.isAutoUpload());
    }

    @Test
    public void isAutoUpload_defaultIsTrue() {
        Assert.assertTrue(manager.isAutoUpload());
    }

    @Test
    public void setEnabled_setsProperty() {
        manager.setEnabled(false);

        Assert.assertFalse(manager.isEnabled());
    }

    @Test
    public void isEnabled_defaultIsTrue() {
        Assert.assertTrue(manager.isEnabled());
    }

    @Test
    public void isUploading_initiallyFalse() {
        Assert.assertFalse(manager.isUploading());
    }

    @Test
    public void isInterrupted_initiallyFalse() {
        Assert.assertFalse(manager.isInterrupted());
    }

    @Test
    public void interruptUpload_whenNotUploading_doesNothing() {
        manager.interruptUpload();

        Assert.assertFalse(manager.isInterrupted());
    }

    @Test
    public void addFileRemovedListener_registersListener() {
        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        var registration = manager
                .addFileRemovedListener(event -> listenerCalled.set(true));

        Assert.assertNotNull(registration);
    }

    @Test
    public void addFileRejectedListener_registersListener() {
        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        var registration = manager
                .addFileRejectedListener(event -> listenerCalled.set(true));

        Assert.assertNotNull(registration);
    }

    @Test
    public void addAllFinishedListener_registersListener() {
        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        var registration = manager
                .addAllFinishedListener(event -> listenerCalled.set(true));

        Assert.assertNotNull(registration);
    }

    @Test
    public void removeListener_unregistersListener() {
        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        var registration = manager
                .addFileRemovedListener(event -> listenerCalled.set(true));
        registration.remove();

        Assert.assertNotNull(registration);
    }

    @Test
    public void fileRemovedEvent_hasFileName() {
        var event = new UploadManager.FileRemovedEvent(new Div(), true,
                "test.txt");

        Assert.assertEquals("test.txt", event.getFileName());
        Assert.assertTrue(event.isFromClient());
    }

    @Test
    public void fileRejectedEvent_hasFileNameAndErrorMessage() {
        var event = new UploadManager.FileRejectedEvent(new Div(), true,
                "File too large", "large-file.zip");

        Assert.assertEquals("large-file.zip", event.getFileName());
        Assert.assertEquals("File too large", event.getErrorMessage());
        Assert.assertTrue(event.isFromClient());
    }

    @Test
    public void allFinishedEvent_createdCorrectly() {
        var event = new UploadManager.AllFinishedEvent(owner);

        Assert.assertSame(owner, event.getSource());
        Assert.assertFalse(event.isFromClient());
    }
}
