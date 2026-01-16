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

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.internal.streams.UploadCompleteEvent;
import com.vaadin.flow.internal.streams.UploadStartEvent;
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
    public void constructor_withOwnerAndHandler_createsManager() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        UploadManager managerWithHandler = new UploadManager(owner, handler);

        Assert.assertNotNull(managerWithHandler);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNullOwner_throws() {
        new UploadManager(null);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNullOwnerAndHandler_throws() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        new UploadManager(null, handler);
    }

    @Test(expected = NullPointerException.class)
    public void setUploadHandler_withNull_throws() {
        manager.setUploadHandler(null);
    }

    @Test(expected = NullPointerException.class)
    public void setUploadHandler_withNullTargetName_throws() { // NOSONAR
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUploadHandler_withBlankTargetName_throws() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler, "   ");
    }

    @Test
    public void setUploadHandler_setsHandler() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });

        // Should not throw
        manager.setUploadHandler(handler);
        Assert.assertNotNull(manager);
    }

    @Test
    public void setUploadHandler_withCustomTargetName_setsHandler() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });

        // Should not throw
        manager.setUploadHandler(handler, "custom-upload");
        Assert.assertNotNull(manager);
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
    public void setMaxFileSize_withLargeValue_supportsFilesOver2GB() {
        long fiveGB = 5L * 1024 * 1024 * 1024; // 5GB
        manager.setMaxFileSize(fiveGB);

        Assert.assertEquals(fiveGB, manager.getMaxFileSize());
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
    public void setAcceptedFileTypes_withEmptyArray_clearsRestrictions() {
        manager.setAcceptedFileTypes("image/*");
        manager.setAcceptedFileTypes(new String[0]);

        Assert.assertTrue(manager.getAcceptedFileTypes().isEmpty());
    }

    @Test
    public void setAcceptedFileTypes_withSingleType_setsProperty() {
        manager.setAcceptedFileTypes("image/*");

        List<String> accepted = manager.getAcceptedFileTypes();
        Assert.assertEquals(1, accepted.size());
        Assert.assertEquals("image/*", accepted.get(0));
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
    public void setAutoUpload_canBeReEnabled() {
        manager.setAutoUpload(false);
        manager.setAutoUpload(true);

        Assert.assertTrue(manager.isAutoUpload());
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
    public void setEnabled_canBeReEnabled() {
        manager.setEnabled(false);
        manager.setEnabled(true);

        Assert.assertTrue(manager.isEnabled());
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

    /**
     * Helper to get the internal connector component via reflection.
     */
    private Component getConnector(UploadManager manager) {
        try {
            Field connectorField = UploadManager.class
                    .getDeclaredField("connector");
            connectorField.setAccessible(true);
            return (Component) connectorField.get(manager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(
                    "Failed to access connector field via reflection", e);
        }
    }

    /**
     * Simulates an upload start event on the connector.
     */
    private void simulateUploadStart(UploadManager manager) {
        Component connector = getConnector(manager);
        ComponentUtil.fireEvent(connector, new UploadStartEvent(connector));
    }

    /**
     * Simulates an upload complete event on the connector.
     */
    private void simulateUploadComplete(UploadManager manager) {
        Component connector = getConnector(manager);
        ComponentUtil.fireEvent(connector, new UploadCompleteEvent(connector));
    }

    @Test
    public void concurrentUploads_tracksMultipleActiveUploads() {
        // Set up manager with handler to enable upload tracking
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);

        // Start 5 concurrent uploads
        for (int i = 0; i < 5; i++) {
            simulateUploadStart(manager);
        }

        Assert.assertTrue("Should be uploading with 5 active uploads",
                manager.isUploading());

        // Complete 3 uploads
        for (int i = 0; i < 3; i++) {
            simulateUploadComplete(manager);
        }

        Assert.assertTrue("Should still be uploading with 2 active uploads",
                manager.isUploading());

        // Complete remaining uploads
        simulateUploadComplete(manager);
        simulateUploadComplete(manager);

        Assert.assertFalse("Should not be uploading after all complete",
                manager.isUploading());
    }

    @Test
    public void interruptUpload_duringActiveUpload_setsInterruptedFlag() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);

        // Start an upload
        simulateUploadStart(manager);
        Assert.assertTrue(manager.isUploading());
        Assert.assertFalse(manager.isInterrupted());

        // Interrupt the upload
        manager.interruptUpload();

        Assert.assertTrue("Should be interrupted during active upload",
                manager.isInterrupted());
        Assert.assertTrue("Should still be marked as uploading",
                manager.isUploading());
    }

    @Test
    public void interruptUpload_flagClearedAfterUploadComplete() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);

        // Start an upload and interrupt it
        simulateUploadStart(manager);
        manager.interruptUpload();
        Assert.assertTrue(manager.isInterrupted());

        // Complete the upload
        simulateUploadComplete(manager);

        Assert.assertFalse(
                "Interrupted flag should be cleared after upload completes",
                manager.isInterrupted());
        Assert.assertFalse(manager.isUploading());
    }

    @Test
    public void setMaxFiles_enforcedDuringActiveUpload() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);
        manager.setMaxFiles(3);

        // Start 3 uploads (at max)
        simulateUploadStart(manager);
        simulateUploadStart(manager);
        simulateUploadStart(manager);

        Assert.assertTrue(manager.isUploading());

        // Try to start a 4th upload - should throw
        try {
            simulateUploadStart(manager);
            Assert.fail(
                    "Should throw IllegalStateException when exceeding maxFiles");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage()
                    .contains("Maximum supported amount of uploads"));
        }
    }

    @Test
    public void setMaxFiles_changesDuringActiveUpload_affectsNewUploads() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);
        manager.setMaxFiles(5);

        // Start 2 uploads
        simulateUploadStart(manager);
        simulateUploadStart(manager);

        // Reduce maxFiles to 2 during upload
        manager.setMaxFiles(2);

        // Try to start another upload - should throw because we're at max
        try {
            simulateUploadStart(manager);
            Assert.fail("Should throw when at new maxFiles limit");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage()
                    .contains("Maximum supported amount of uploads"));
        }
    }

    @Test
    public void allFinishedEvent_firedOnlyWhenAllUploadsComplete() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);

        AtomicInteger finishedCount = new AtomicInteger(0);
        manager.addAllFinishedListener(
                event -> finishedCount.incrementAndGet());

        // Start 3 uploads
        simulateUploadStart(manager);
        simulateUploadStart(manager);
        simulateUploadStart(manager);

        // Complete first two - should not fire event yet
        simulateUploadComplete(manager);
        Assert.assertEquals("AllFinished should not fire yet", 0,
                finishedCount.get());

        simulateUploadComplete(manager);
        Assert.assertEquals("AllFinished should not fire yet", 0,
                finishedCount.get());

        // Complete last upload - should fire event
        simulateUploadComplete(manager);
        Assert.assertEquals("AllFinished should fire once", 1,
                finishedCount.get());
    }

}
