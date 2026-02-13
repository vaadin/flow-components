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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.internal.streams.UploadCompleteEvent;
import com.vaadin.flow.internal.streams.UploadStartEvent;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.ElementRequestHandler;
import com.vaadin.flow.server.streams.UploadHandler;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadManagerTest {

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
    public void constructor_withOwnerAndHandler_createsManager() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        UploadManager managerWithHandler = new UploadManager(owner, handler);

        Assert.assertNotNull(managerWithHandler);
    }

    @Test
    public void constructor_withNullOwner_throws() {
        Assert.assertThrows(NullPointerException.class,
                () -> new UploadManager(null));
    }

    @Test
    public void constructor_withNullOwnerAndHandler_throws() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        Assert.assertThrows(NullPointerException.class,
                () -> new UploadManager(null, handler));
    }

    @Test
    public void setUploadHandler_withNull_throws() {
        Assert.assertThrows(NullPointerException.class,
                () -> manager.setUploadHandler(null));
    }

    @Test
    public void setUploadHandler_withNullTargetName_throws() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        Assert.assertThrows(NullPointerException.class,
                () -> manager.setUploadHandler(handler, null));
    }

    @Test
    public void setUploadHandler_withBlankTargetName_throws() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        Assert.assertThrows(IllegalArgumentException.class,
                () -> manager.setUploadHandler(handler, "   "));
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

    // --- Accepted MIME Types ---

    @Test
    public void setAcceptedMimeTypes_setsValues() {
        manager.setAcceptedMimeTypes("image/*", "application/pdf");

        List<String> accepted = manager.getAcceptedMimeTypes();
        Assert.assertEquals(2, accepted.size());
        Assert.assertTrue(accepted.contains("image/*"));
        Assert.assertTrue(accepted.contains("application/pdf"));
    }

    @Test
    public void setAcceptedMimeTypes_withNull_clears() {
        manager.setAcceptedMimeTypes("image/*");
        manager.setAcceptedMimeTypes((String[]) null);

        Assert.assertTrue(manager.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    public void setAcceptedMimeTypes_withEmptyArray_clears() {
        manager.setAcceptedMimeTypes("image/*");
        manager.setAcceptedMimeTypes(new String[0]);

        Assert.assertTrue(manager.getAcceptedMimeTypes().isEmpty());
    }

    @Test
    public void setAcceptedMimeTypes_withNullValue_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedMimeTypes("image/*", null));
    }

    @Test
    public void setAcceptedMimeTypes_withBlankValue_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedMimeTypes("image/*", "   "));
    }

    @Test
    public void setAcceptedMimeTypes_withoutSlash_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedMimeTypes("imagepng"));
    }

    @Test
    public void getAcceptedMimeTypes_defaultIsEmpty() {
        Assert.assertTrue(manager.getAcceptedMimeTypes().isEmpty());
    }

    // --- Accepted File Extensions ---

    @Test
    public void setAcceptedFileExtensions_setsValues() {
        manager.setAcceptedFileExtensions(".pdf", ".txt");

        List<String> accepted = manager.getAcceptedFileExtensions();
        Assert.assertEquals(2, accepted.size());
        Assert.assertTrue(accepted.contains(".pdf"));
        Assert.assertTrue(accepted.contains(".txt"));
    }

    @Test
    public void setAcceptedFileExtensions_withNull_clears() {
        manager.setAcceptedFileExtensions(".pdf");
        manager.setAcceptedFileExtensions((String[]) null);

        Assert.assertTrue(manager.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    public void setAcceptedFileExtensions_withEmptyArray_clears() {
        manager.setAcceptedFileExtensions(".pdf");
        manager.setAcceptedFileExtensions(new String[0]);

        Assert.assertTrue(manager.getAcceptedFileExtensions().isEmpty());
    }

    @Test
    public void setAcceptedFileExtensions_withNullValue_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedFileExtensions(".pdf", null));
    }

    @Test
    public void setAcceptedFileExtensions_withBlankValue_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedFileExtensions(".pdf", "   "));
    }

    @Test
    public void setAcceptedFileExtensions_withoutDot_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> manager.setAcceptedFileExtensions("pdf"));
    }

    @Test
    public void getAcceptedFileExtensions_defaultIsEmpty() {
        Assert.assertTrue(manager.getAcceptedFileExtensions().isEmpty());
    }

    // --- Accept property derivation ---

    @Test
    public void setAcceptedMimeTypes_setsAcceptProperty() {
        manager.setAcceptedMimeTypes("image/*", "application/pdf");

        String accept = getConnector(manager).getElement()
                .getProperty("accept");
        Assert.assertEquals("image/*,application/pdf", accept);
    }

    @Test
    public void setAcceptedFileExtensions_setsAcceptProperty() {
        manager.setAcceptedFileExtensions(".pdf", ".txt");

        String accept = getConnector(manager).getElement()
                .getProperty("accept");
        Assert.assertEquals(".pdf,.txt", accept);
    }

    @Test
    public void setBoth_combinesInAcceptProperty() {
        manager.setAcceptedMimeTypes("image/*");
        manager.setAcceptedFileExtensions(".pdf");

        String accept = getConnector(manager).getElement()
                .getProperty("accept");
        Assert.assertEquals("image/*,.pdf", accept);
    }

    // --- File type validation logic (isFileTypeAccepted) ---

    @Test
    public void isFileTypeAccepted_mimeOnly_matchingType_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.txt", "text/plain",
                List.of("text/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeOnly_nonMatchingType_rejected() {
        Assert.assertFalse(isFileTypeAccepted("file.txt", "text/plain",
                List.of("image/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeOnly_exactMatch_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.pdf", "application/pdf",
                List.of("application/pdf"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeOnly_caseInsensitive_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.pdf", "Application/PDF",
                List.of("application/pdf"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_extensionOnly_matchingExt_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.txt", "text/plain",
                List.of(), List.of(".txt")));
    }

    @Test
    public void isFileTypeAccepted_extensionOnly_nonMatchingExt_rejected() {
        Assert.assertFalse(isFileTypeAccepted("file.txt", "text/plain",
                List.of(), List.of(".pdf")));
    }

    @Test
    public void isFileTypeAccepted_extensionOnly_caseInsensitive_accepted() {
        Assert.assertTrue(isFileTypeAccepted("FILE.TXT", "text/plain",
                List.of(), List.of(".txt")));
    }

    @Test
    public void isFileTypeAccepted_bothConfigured_bothMatch_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.pdf", "application/pdf",
                List.of("application/pdf"), List.of(".pdf")));
    }

    @Test
    public void isFileTypeAccepted_bothConfigured_mimeMatchesExtDoesNot_rejected() {
        Assert.assertFalse(isFileTypeAccepted("file.html", "text/html",
                List.of("text/*"), List.of(".pdf")));
    }

    @Test
    public void isFileTypeAccepted_bothConfigured_extMatchesMimeDoesNot_rejected() {
        Assert.assertFalse(isFileTypeAccepted("file.pdf", "text/plain",
                List.of("image/*"), List.of(".pdf")));
    }

    @Test
    public void isFileTypeAccepted_nullContentType_mimeConfigured_rejected() {
        Assert.assertFalse(isFileTypeAccepted("file.txt", null,
                List.of("text/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_nullFileName_extensionConfigured_rejected() {
        Assert.assertFalse(isFileTypeAccepted(null, "text/plain", List.of(),
                List.of(".txt")));
    }

    @Test
    public void isFileTypeAccepted_neitherConfigured_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.txt", "text/plain",
                List.of(), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeWithParameters_exactMatch_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("text/html"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeWithParameters_wildcardMatch_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("text/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_mimeWithParameters_nonMatch_rejected() {
        Assert.assertFalse(isFileTypeAccepted("file.html",
                "text/html; charset=utf-8", List.of("image/*"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_multipleMimeTypes_oneMatches_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.pdf", "application/pdf",
                List.of("image/*", "application/pdf"), List.of()));
    }

    @Test
    public void isFileTypeAccepted_multipleExtensions_oneMatches_accepted() {
        Assert.assertTrue(isFileTypeAccepted("file.txt", "text/plain",
                List.of(), List.of(".pdf", ".txt")));
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
    public void addFileRemovedListener_registersListener() {
        var registration = manager.addFileRemovedListener(event -> {
        });

        Assert.assertNotNull(registration);
    }

    @Test
    public void addFileRejectedListener_registersListener() {
        var registration = manager.addFileRejectedListener(event -> {
        });

        Assert.assertNotNull(registration);
    }

    @Test
    public void addAllFinishedListener_registersListener() {
        var registration = manager.addAllFinishedListener(event -> {
        });

        Assert.assertNotNull(registration);
    }

    @Test
    public void removeListener_unregistersListener() {
        var registration = manager.addFileRemovedListener(event -> {
        });
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
     * Helper to invoke the private isFileTypeAccepted method via reflection.
     */
    private static boolean isFileTypeAccepted(String fileName,
            String contentType, List<String> mimeTypes,
            List<String> extensions) {
        try {
            Method method = UploadManager.class.getDeclaredMethod(
                    "isFileTypeAccepted", String.class, String.class,
                    List.class, List.class);
            method.setAccessible(true);
            return (boolean) method.invoke(null, fileName, contentType,
                    mimeTypes, extensions);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(
                    "Failed to access isFileTypeAccepted via reflection", e);
        }
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
    public void allFinishedEvent_firedWhenDomEventReceived() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);

        AtomicInteger finishedCount = new AtomicInteger(0);
        manager.addAllFinishedListener(
                event -> finishedCount.incrementAndGet());

        Assert.assertEquals("AllFinished should not have fired yet", 0,
                finishedCount.get());

        // Simulate the client-side "all-finished" DOM event
        simulateAllFinishedDomEvent(manager);

        Assert.assertEquals("AllFinished should fire once", 1,
                finishedCount.get());

        // Fire again to ensure multiple events work
        simulateAllFinishedDomEvent(manager);

        Assert.assertEquals("AllFinished should fire twice", 2,
                finishedCount.get());
    }

    // --- Wrapper delegation of ElementRequestHandler defaults ---
    //
    // These tests verify that the file-type-validation wrapper created
    // internally by setUploadHandler() delegates ElementRequestHandler
    // default methods to the original handler. Each test sets a custom
    // handler via the public API, then retrieves the actually-registered
    // handler from the StreamResourceRegistry and asserts the value.

    @Test
    public void setUploadHandler_delegatesGetUrlPostfix() {
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

        Assert.assertEquals("custom-postfix",
                getRegisteredHandler().getUrlPostfix());
    }

    @Test
    public void setUploadHandler_delegatesIsAllowInert() {
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

        Assert.assertTrue(getRegisteredHandler().isAllowInert());
    }

    @Test
    public void setUploadHandler_delegatesGetDisabledUpdateMode() {
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

        Assert.assertEquals(DisabledUpdateMode.ALWAYS,
                getRegisteredHandler().getDisabledUpdateMode());
    }

    @Test
    public void setUploadHandler_wrapperOverridesAllInterfaceMethods() {
        UploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
        });
        manager.setUploadHandler(handler);
        ElementRequestHandler wrapper = getRegisteredHandler();

        // Collect all non-static methods from UploadHandler and its
        // super-interfaces. These are the methods the wrapper must override
        // to ensure proper delegation.
        // handleRequest is excluded because UploadHandler provides a default
        // that routes to handleUploadRequest, which the wrapper overrides.
        Set<String> interfaceMethods = new LinkedHashSet<>();
        collectInterfaceMethods(UploadHandler.class, interfaceMethods);
        interfaceMethods.removeIf(sig -> sig.startsWith("handleRequest("));

        Set<String> wrapperMethods = new LinkedHashSet<>();
        for (Method m : wrapper.getClass().getDeclaredMethods()) {
            if (!m.isSynthetic()) {
                wrapperMethods.add(methodSignature(m));
            }
        }

        for (String sig : interfaceMethods) {
            Assert.assertTrue(
                    "Validation wrapper must delegate: " + sig
                            + ". See NOTE in wrapWithFileTypeValidation.",
                    wrapperMethods.contains(sig));
        }
    }

    /**
     * Collects all non-static method signatures from the given interface and
     * all its super-interfaces (at any depth).
     */
    private static void collectInterfaceMethods(Class<?> iface,
            Set<String> signatures) {
        for (Method m : iface.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) {
                signatures.add(methodSignature(m));
            }
        }
    }

    private static String methodSignature(Method m) {
        StringBuilder sb = new StringBuilder(m.getName()).append('(');
        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(params[i].getName());
        }
        return sb.append(')').toString();
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
