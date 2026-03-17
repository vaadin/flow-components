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

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.ModularUploadFeatureFlagProvider;
import com.vaadin.flow.component.upload.UploadFormat;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.internal.streams.UploadCompleteEvent;
import com.vaadin.flow.internal.streams.UploadStartEvent;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.ElementRequestHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class UploadManagerTest {
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
    void constructor_withOwnerAndHandler_createsManager() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        UploadManager managerWithHandler = new UploadManager(owner, handler);

        Assertions.assertNotNull(managerWithHandler);
    }

    @Test
    void constructor_withNullOwner_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new UploadManager(null));
    }

    @Test
    void constructor_withNullOwnerAndHandler_throws() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        Assertions.assertThrows(NullPointerException.class,
                () -> new UploadManager(null, handler));
    }

    @Test
    void setUploadHandler_withNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> manager.setUploadHandler(null));
    }

    @Test
    void setUploadHandler_withNullTargetName_throws() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        Assertions.assertThrows(NullPointerException.class,
                () -> manager.setUploadHandler(handler, null));
    }

    @Test
    void setUploadHandler_withBlankTargetName_throws() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.setUploadHandler(handler, "   "));
    }

    @Test
    void setUploadHandler_setsHandler() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });

        // Should not throw
        manager.setUploadHandler(handler);
        Assertions.assertNotNull(manager);
    }

    @Test
    void setUploadHandler_withCustomTargetName_setsHandler() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });

        // Should not throw
        manager.setUploadHandler(handler, "custom-upload");
        Assertions.assertNotNull(manager);
    }

    @Test
    void setMaxFiles_setsProperty() {
        manager.setMaxFiles(5);

        Assertions.assertEquals(5, manager.getMaxFiles());
    }

    @Test
    void setMaxFiles_withZero_meansUnlimited() {
        manager.setMaxFiles(0);

        Assertions.assertEquals(0, manager.getMaxFiles());
    }

    @Test
    void getMaxFiles_defaultIsZero() {
        Assertions.assertEquals(0, manager.getMaxFiles());
    }

    @Test
    void setMaxFileSize_setsProperty() {
        manager.setMaxFileSize(1024 * 1024); // 1MB

        Assertions.assertEquals(1024 * 1024, manager.getMaxFileSize());
    }

    @Test
    void setMaxFileSize_withLargeValue_supportsFilesOver2GB() {
        long fiveGB = 5L * 1024 * 1024 * 1024; // 5GB
        manager.setMaxFileSize(fiveGB);

        Assertions.assertEquals(fiveGB, manager.getMaxFileSize());
    }

    @Test
    void setMaxFileSize_withZero_meansUnlimited() {
        manager.setMaxFileSize(0);

        Assertions.assertEquals(0, manager.getMaxFileSize());
    }

    @Test
    void getMaxFileSize_defaultIsZero() {
        Assertions.assertEquals(0, manager.getMaxFileSize());
    }

    // --- Accepted MIME Types ---

    @Test
    void setAcceptedMimeTypes_setsValues() {
        manager.setAcceptedMimeTypes("image/*", "application/pdf");

        List<String> accepted = manager.getAcceptedMimeTypes();
        Assertions.assertEquals(2, accepted.size());
        Assertions.assertTrue(accepted.contains("image/*"));
        Assertions.assertTrue(accepted.contains("application/pdf"));
    }

    @Test
    void setAcceptedMimeTypes_withNull_clears() {
        manager.setAcceptedMimeTypes("image/*");
        manager.setAcceptedMimeTypes((String[]) null);

        Assertions.assertTrue(manager.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    void setAcceptedMimeTypes_withEmptyArray_clears() {
        manager.setAcceptedMimeTypes("image/*");
        manager.setAcceptedMimeTypes(new String[0]);

        Assertions.assertTrue(manager.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    void setAcceptedMimeTypes_withNullValue_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedMimeTypes("image/*", null));
    }

    @Test
    void setAcceptedMimeTypes_withBlankValue_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedMimeTypes("image/*", "   "));
    }

    @Test
    void setAcceptedMimeTypes_withoutSlash_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedMimeTypes("imagepng"));
    }

    @Test
    void getAcceptedMimeTypes_defaultIsEmpty() {
        Assertions.assertTrue(manager.getAcceptedMimeTypes().isEmpty());
    }

    // --- Accepted File Extensions ---

    @Test
    void setAcceptedFileExtensions_setsValues() {
        manager.setAcceptedFileExtensions(".pdf", ".txt");

        List<String> accepted = manager.getAcceptedFileExtensions();
        Assertions.assertEquals(2, accepted.size());
        Assertions.assertTrue(accepted.contains(".pdf"));
        Assertions.assertTrue(accepted.contains(".txt"));
    }

    @Test
    void setAcceptedFileExtensions_withNull_clears() {
        manager.setAcceptedFileExtensions(".pdf");
        manager.setAcceptedFileExtensions((String[]) null);

        Assertions.assertTrue(manager.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    void setAcceptedFileExtensions_withEmptyArray_clears() {
        manager.setAcceptedFileExtensions(".pdf");
        manager.setAcceptedFileExtensions(new String[0]);

        Assertions.assertTrue(manager.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    void setAcceptedFileExtensions_withNullValue_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedFileExtensions(".pdf", null));
    }

    @Test
    void setAcceptedFileExtensions_withBlankValue_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedFileExtensions(".pdf", "   "));
    }

    @Test
    void setAcceptedFileExtensions_withoutDot_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedFileExtensions("pdf"));
    }

    @Test
    void getAcceptedFileExtensions_defaultIsEmpty() {
        Assertions.assertTrue(manager.getAcceptedFileExtensions().isEmpty());
    }

    // --- Accept property derivation ---

    @Test
    void setAcceptedMimeTypes_setsAcceptProperty() {
        manager.setAcceptedMimeTypes("image/*", "application/pdf");

        String accept = getConnector(manager).getElement()
                .getProperty("accept");
        Assertions.assertEquals("image/*,application/pdf", accept);
    }

    @Test
    void setAcceptedFileExtensions_setsAcceptProperty() {
        manager.setAcceptedFileExtensions(".pdf", ".txt");

        String accept = getConnector(manager).getElement()
                .getProperty("accept");
        Assertions.assertEquals(".pdf,.txt", accept);
    }

    @Test
    void setBoth_combinesInAcceptProperty() {
        manager.setAcceptedMimeTypes("image/*");
        manager.setAcceptedFileExtensions(".pdf");

        String accept = getConnector(manager).getElement()
                .getProperty("accept");
        Assertions.assertEquals("image/*,.pdf", accept);
    }

    @Test
    void setAutoUpload_setsProperty() {
        manager.setAutoUpload(false);

        Assertions.assertFalse(manager.isAutoUpload());
    }

    @Test
    void setAutoUpload_canBeReEnabled() {
        manager.setAutoUpload(false);
        manager.setAutoUpload(true);

        Assertions.assertTrue(manager.isAutoUpload());
    }

    @Test
    void isAutoUpload_defaultIsTrue() {
        Assertions.assertTrue(manager.isAutoUpload());
    }

    @Test
    void setUploadFormat_setsProperty() {
        manager.setUploadFormat(UploadFormat.MULTIPART);

        Assertions.assertEquals(UploadFormat.MULTIPART,
                manager.getUploadFormat());
    }

    @Test
    void getUploadFormat_defaultIsRaw() {
        Assertions.assertEquals(UploadFormat.RAW, manager.getUploadFormat());
    }

    @Test
    void setEnabled_setsProperty() {
        manager.setEnabled(false);

        Assertions.assertFalse(manager.isEnabled());
    }

    @Test
    void setEnabled_canBeReEnabled() {
        manager.setEnabled(false);
        manager.setEnabled(true);

        Assertions.assertTrue(manager.isEnabled());
    }

    @Test
    void isEnabled_defaultIsTrue() {
        Assertions.assertTrue(manager.isEnabled());
    }

    @Test
    void isUploading_initiallyFalse() {
        Assertions.assertFalse(manager.isUploading());
    }

    @Test
    void addFileRemovedListener_registersListener() {
        var registration = manager.addFileRemovedListener(event -> {
        });

        Assertions.assertNotNull(registration);
    }

    @Test
    void addFileRejectedListener_registersListener() {
        var registration = manager.addFileRejectedListener(event -> {
        });

        Assertions.assertNotNull(registration);
    }

    @Test
    void addAllFinishedListener_registersListener() {
        var registration = manager.addAllFinishedListener(event -> {
        });

        Assertions.assertNotNull(registration);
    }

    @Test
    void removeListener_unregistersListener() {
        var registration = manager.addFileRemovedListener(event -> {
        });
        registration.remove();

        Assertions.assertNotNull(registration);
    }

    @Test
    void fileRemovedEvent_hasFileName() {
        var event = new UploadManager.FileRemovedEvent(new Div(), true,
                "test.txt");

        Assertions.assertEquals("test.txt", event.getFileName());
        Assertions.assertTrue(event.isFromClient());
    }

    @Test
    void fileRejectedEvent_hasFileNameAndReason() {
        var event = new UploadManager.FileRejectedEvent(new Div(), true,
                UploadManager.FileRejectionReason.FILE_TOO_LARGE,
                "large-file.zip");

        Assertions.assertEquals("large-file.zip", event.getFileName());
        Assertions.assertEquals(
                UploadManager.FileRejectionReason.FILE_TOO_LARGE,
                event.getReason());
        Assertions.assertTrue(event.isFromClient());
    }

    @Test
    void fileRejectionReason_fromClientCode_knownCodes() {
        Assertions.assertEquals(
                UploadManager.FileRejectionReason.TOO_MANY_FILES,
                UploadManager.FileRejectionReason
                        .fromClientCode("tooManyFiles"));
        Assertions.assertEquals(
                UploadManager.FileRejectionReason.FILE_TOO_LARGE,
                UploadManager.FileRejectionReason
                        .fromClientCode("fileIsTooBig"));
        Assertions.assertEquals(
                UploadManager.FileRejectionReason.INCORRECT_FILE_TYPE,
                UploadManager.FileRejectionReason
                        .fromClientCode("incorrectFileType"));
    }

    @Test
    void fileRejectionReason_fromClientCode_unknownCode() {
        Assertions.assertEquals(UploadManager.FileRejectionReason.UNKNOWN,
                UploadManager.FileRejectionReason
                        .fromClientCode("someNewCode"));
    }

    @Test
    void allFinishedEvent_createdCorrectly() {
        var event = new UploadManager.AllFinishedEvent(owner);

        Assertions.assertSame(owner, event.getSource());
        Assertions.assertFalse(event.isFromClient());
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
    void concurrentUploads_tracksMultipleActiveUploads() {
        // Set up manager with handler to enable upload tracking
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);

        // Start 5 concurrent uploads
        for (int i = 0; i < 5; i++) {
            simulateUploadStart(manager);
        }

        Assertions.assertTrue(manager.isUploading(),
                "Should be uploading with 5 active uploads");

        // Complete 3 uploads
        for (int i = 0; i < 3; i++) {
            simulateUploadComplete(manager);
        }

        Assertions.assertTrue(manager.isUploading(),
                "Should still be uploading with 2 active uploads");

        // Complete remaining uploads
        simulateUploadComplete(manager);
        simulateUploadComplete(manager);

        Assertions.assertFalse(manager.isUploading(),
                "Should not be uploading after all complete");
    }

    @Test
    void setMaxFiles_enforcedDuringActiveUpload() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);
        manager.setMaxFiles(3);

        // Start 3 uploads (at max)
        simulateUploadStart(manager);
        simulateUploadStart(manager);
        simulateUploadStart(manager);

        Assertions.assertTrue(manager.isUploading());

        // Try to start a 4th upload - should throw
        IllegalStateException e = Assertions.assertThrows(
                IllegalStateException.class,
                () -> simulateUploadStart(manager));
        Assertions.assertTrue(
                e.getMessage().contains("Maximum supported amount of uploads"),
                "Should throw IllegalStateException when exceeding maxFiles");
    }

    @Test
    void setMaxFiles_changesDuringActiveUpload_affectsNewUploads() {
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
        IllegalStateException e = Assertions.assertThrows(
                IllegalStateException.class,
                () -> simulateUploadStart(manager));
        Assertions.assertTrue(
                e.getMessage().contains("Maximum supported amount of uploads"),
                "Should throw when at new maxFiles limit");
    }

    @Test
    void allFinishedEvent_firedWhenDomEventReceived() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);

        AtomicInteger finishedCount = new AtomicInteger(0);
        manager.addAllFinishedListener(
                event -> finishedCount.incrementAndGet());

        Assertions.assertEquals(0, finishedCount.get(),
                "AllFinished should not have fired yet");

        // Simulate the client-side "all-finished" DOM event
        simulateAllFinishedDomEvent(manager);

        Assertions.assertEquals(1, finishedCount.get(),
                "AllFinished should fire once");

        // Fire again to ensure multiple events work
        simulateAllFinishedDomEvent(manager);

        Assertions.assertEquals(2, finishedCount.get(),
                "AllFinished should fire twice");
    }

    // --- Wrapper delegation of ElementRequestHandler defaults ---
    //
    // These tests verify that the file-type-validation wrapper created
    // internally by setUploadHandler() delegates ElementRequestHandler
    // default methods to the original handler. Each test sets a custom
    // handler via the public API, then retrieves the actually-registered
    // handler from the StreamResourceRegistry and asserts the value.

    @Test
    void setUploadHandler_delegatesGetUrlPostfix() {
        UploadHandler handler = new UploadHandler() {
            @Override
            public void handleUploadRequest(
                    com.vaadin.flow.server.streams.UploadEvent event) {
            }

            @Override
            public String getUrlPostfix() {
                return "custom-postfix";
            }
        };

        manager.setUploadHandler(handler);

        Assertions.assertEquals("custom-postfix",
                getRegisteredHandler().getUrlPostfix());
    }

    @Test
    void setUploadHandler_delegatesIsAllowInert() {
        UploadHandler handler = new UploadHandler() {
            @Override
            public void handleUploadRequest(
                    com.vaadin.flow.server.streams.UploadEvent event) {
            }

            @Override
            public boolean isAllowInert() {
                return true;
            }
        };

        manager.setUploadHandler(handler);

        Assertions.assertTrue(getRegisteredHandler().isAllowInert());
    }

    @Test
    void setUploadHandler_delegatesGetDisabledUpdateMode() {
        UploadHandler handler = new UploadHandler() {
            @Override
            public void handleUploadRequest(
                    com.vaadin.flow.server.streams.UploadEvent event) {
            }

            @Override
            public DisabledUpdateMode getDisabledUpdateMode() {
                return DisabledUpdateMode.ALWAYS;
            }
        };

        manager.setUploadHandler(handler);

        Assertions.assertEquals(DisabledUpdateMode.ALWAYS,
                getRegisteredHandler().getDisabledUpdateMode());
    }

    /**
     * Retrieves the {@link ElementRequestHandler} that is actually registered
     * on the connector element via the {@link StreamResourceRegistry}. This
     * returns the wrapper handler created by {@code setUploadHandler()}, so
     * calling methods on it verifies that the wrapper correctly delegates.
     */
    private ElementRequestHandler getRegisteredHandler() {
        Element connectorElement = getConnector(manager).getElement();
        String targetUri = connectorElement.getAttribute("target");
        StreamResourceRegistry registry = ui.getSession().getResourceRegistry();
        URI uri = URI.create(targetUri);
        StreamResourceRegistry.ElementStreamResource resource = registry
                .getResource(StreamResourceRegistry.ElementStreamResource.class,
                        uri)
                .orElseThrow(() -> new AssertionError(
                        "No ElementStreamResource found for URI: "
                                + targetUri));
        return resource.getElementRequestHandler();
    }

    /**
     * Simulates the client-side "all-finished" DOM event on the connector.
     */
    private void simulateAllFinishedDomEvent(UploadManager manager) {
        Component connector = getConnector(manager);
        Element element = connector.getElement();
        DomEvent event = new DomEvent(element, "all-finished",
                JacksonUtils.createObjectNode());
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(event);
    }

}
