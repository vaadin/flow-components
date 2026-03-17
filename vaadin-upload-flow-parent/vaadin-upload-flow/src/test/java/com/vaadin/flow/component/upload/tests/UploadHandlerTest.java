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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.InMemoryUploadCallback;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
import com.vaadin.flow.server.streams.UploadResult;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class UploadHandlerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    private Upload upload;

    @BeforeEach
    void setup() {
        upload = new Upload();
        ui.add(upload);
    }

    @Test
    void setUploadHandler_setsHandlerInTargetStreamResource()
            throws URISyntaxException {
        InMemoryUploadHandler handler = new InMemoryUploadHandler(
                (metadata, data) -> {
                });
        upload.setUploadHandler(handler);
        ui.fakeClientCommunication();

        UploadHandler targetHandler = getUploadHandler();
        Assertions.assertNotNull(targetHandler);
    }

    @Test
    void setUploadHandler_generatedUrlEndsWithUpload() {
        upload.setUploadHandler(event -> {
        });
        ui.fakeClientCommunication();

        String targetUploadUrl = upload.getElement().getAttribute("target");
        Assertions.assertTrue(targetUploadUrl.endsWith("upload"),
                "Upload url should end with 'upload'");
    }

    @Test
    void setUploadHandler_withCustomTarget_generatedUrlEndsWithCustomName() {
        String targetName = "custom-target";
        upload.setUploadHandler(event -> {
        }, targetName);
        ui.fakeClientCommunication();

        String targetUploadUrl = upload.getElement().getAttribute("target");
        Assertions.assertTrue(targetUploadUrl.endsWith(targetName),
                "Upload url should end with custom target name 'custom-target'");
    }

    @Test
    void setUploadHandler_withNullTarget_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> upload.setUploadHandler(event -> {
                }, null));
    }

    @Test
    void setUploadHandler_withBlankTarget_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> upload.setUploadHandler(event -> {
                }, ""));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> upload.setUploadHandler(event -> {
                }, "   "));
    }

    @Test
    void uploadWithStandardHandler_activeUploadsIsChanged_from0to1_from1to0()
            throws IOException, URISyntaxException {
        // Not uploading initially
        Assertions.assertFalse(upload.isUploading());

        // Simulate an upload using a handler, verify that uploading flag is set
        // while upload is in progress
        InMemoryUploadCallback callback = Mockito
                .spy(new InMemoryUploadCallback() {
                    @Override
                    public void complete(UploadMetadata metadata, byte[] data) {
                        Assertions.assertTrue(upload.isUploading());
                    }
                });
        InMemoryUploadHandler handler = new InMemoryUploadHandler(callback);
        upload.setUploadHandler(handler);
        ui.fakeClientCommunication();
        simulateUpload();

        // Verify flag is reset after upload completes
        Assertions.assertFalse(upload.isUploading());

        // Sanity check that callback containing the assertion was called
        Mockito.verify(callback, Mockito.times(1)).complete(Mockito.any(),
                Mockito.any());
    }

    @Test
    void uploadWithCustomHandler_activeUploadsIsChanged_from0to1_from1to0()
            throws IOException, URISyntaxException {
        // Not uploading initially
        Assertions.assertFalse(upload.isUploading());

        // Simulate an upload using a custom handler, verify that uploading flag
        // is set while upload is in progress
        UploadHandler customHandler = Mockito.spy(new UploadHandler() {
            @Override
            public void handleUploadRequest(UploadEvent event) {
                // when the upload starts,
                // then the upload is in progress
                Assertions.assertTrue(upload.isUploading());
            }

            @Override
            public long getRequestSizeMax() {
                return 111L;
            }

            @Override
            public long getFileSizeMax() {
                return 222L;
            }

            @Override
            public long getFileCountMax() {
                return 333L;
            }

            @Override
            public String getUrlPostfix() {
                return "custom-postfix";
            }

            @Override
            public boolean isAllowInert() {
                return true;
            }

            @Override
            public DisabledUpdateMode getDisabledUpdateMode() {
                return DisabledUpdateMode.ALWAYS;
            }
        });
        upload.setUploadHandler(customHandler);
        ui.fakeClientCommunication();
        simulateUpload();

        // Verify flag is reset after upload completes
        Assertions.assertFalse(upload.isUploading());

        // verify that the original methods of custom handler were called
        Mockito.verify(customHandler)
                .handleUploadRequest(Mockito.any(UploadEvent.class));
        Mockito.verify(customHandler)
                .responseHandled(Mockito.any(UploadResult.class));
        Assertions.assertEquals(111L, customHandler.getRequestSizeMax());
        Assertions.assertEquals(222L, customHandler.getFileSizeMax());
        Assertions.assertEquals(333L, customHandler.getFileCountMax());
        Assertions.assertEquals("custom-postfix",
                customHandler.getUrlPostfix());
        Assertions.assertTrue(customHandler.isAllowInert());
        Assertions.assertEquals(DisabledUpdateMode.ALWAYS,
                customHandler.getDisabledUpdateMode());
    }

    @Test
    void uploadWithoutHandler_throwsWhenUploadStarts()
            throws URISyntaxException {
        ui.fakeClientCommunication();
        UploadHandler uploadHandler = getUploadHandler();

        Assertions.assertThrows(IllegalStateException.class,
                () -> uploadHandler.handleUploadRequest(null),
                "Expected IllegalStateException for Upload with no handler");
    }

    @Test
    void setUploadHandler_maxFileIsDefined_maxFileIsPreserved() {
        upload.setMaxFiles(5);
        upload.setUploadHandler(event -> {
        });
        Assertions.assertEquals(5, upload.getMaxFiles());
    }

    @Test
    void setUploadHandler_maxFileIsNotDefined_doesNotSetMaxFiles() {
        upload.setUploadHandler(event -> {
        });
        Assertions.assertNull(upload.getElement().getProperty("maxFiles"));
    }

    private void simulateUpload() throws IOException, URISyntaxException {
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        VaadinSession session = Mockito.mock(VaadinSession.class);

        Mockito.when(request.getInputStream())
                .thenReturn(new ByteArrayInputStream("test data".getBytes()));

        UploadHandler uploadHandler = getUploadHandler();
        uploadHandler.handleRequest(request, response, ui.getSession(),
                upload.getElement());
    }

    private UploadHandler getUploadHandler() throws URISyntaxException {
        String target = upload.getElement().getAttribute("target");
        Assertions.assertNotNull(target, "Target attribute is not set");

        Optional<AbstractStreamResource> maybeResource = ui.getSession()
                .getResourceRegistry().getResource(new URI(target));
        Assertions.assertTrue(maybeResource.isPresent(),
                "No resource found for target: " + target);

        StreamResourceRegistry.ElementStreamResource elementStreamResource = (StreamResourceRegistry.ElementStreamResource) maybeResource
                .get();

        return (UploadHandler) elementStreamResource.getElementRequestHandler();
    }
}
