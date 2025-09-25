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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.InMemoryUploadCallback;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadHandlerTest {
    private UI ui;
    private StreamResourceRegistry streamResourceRegistry;
    private Upload upload;

    @Before
    public void setup() {
        ui = Mockito.spy(new UI());
        UI.setCurrent(ui);

        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        streamResourceRegistry = new StreamResourceRegistry(mockSession);
        Mockito.when(mockSession.getResourceRegistry())
                .thenReturn(streamResourceRegistry);
        Mockito.when(mockSession.access(Mockito.any()))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, Command.class).execute();
                    return new CompletableFuture<>();
                });
        ui.getInternals().setSession(mockSession);

        upload = new Upload();
        ui.add(upload);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void setUploadHandler_setsHandlerInTargetStreamResource()
            throws URISyntaxException {
        InMemoryUploadHandler handler = new InMemoryUploadHandler(
                (metadata, data) -> {
                });
        upload.setUploadHandler(handler);
        fakeClientCommunication();

        UploadHandler targetHandler = getUploadHandler();
        Assert.assertEquals(handler, targetHandler);
    }

    @Test
    public void setUploadHandler_generatedUrlEndsWithUpload() {
        upload.setUploadHandler(event -> {
        });
        fakeClientCommunication();

        String targetUploadUrl = upload.getElement().getAttribute("target");
        Assert.assertTrue("Upload url should end with 'upload'",
                targetUploadUrl.endsWith("upload"));
    }

    @Test
    public void setUploadHandler_withCustomTarget_generatedUrlEndsWithCustomName() {
        String targetName = "custom-target";
        upload.setUploadHandler(event -> {
        }, targetName);
        fakeClientCommunication();

        String targetUploadUrl = upload.getElement().getAttribute("target");
        Assert.assertTrue(
                "Upload url should end with custom target name 'custom-target'",
                targetUploadUrl.endsWith(targetName));
    }

    @Test
    public void setUploadHandler_withNullTarget_throws() {
        Assert.assertThrows(NullPointerException.class,
                () -> upload.setUploadHandler(event -> {
                }, null));
    }

    @Test
    public void setUploadHandler_withBlankTarget_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> upload.setUploadHandler(event -> {
                }, ""));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> upload.setUploadHandler(event -> {
                }, "   "));
    }

    @Test
    public void uploadWithStandardHandler_activeUploadsIsChanged_from0to1_from1to0()
            throws IOException, URISyntaxException {
        // Not uploading initially
        Assert.assertFalse(upload.isUploading());

        // Simulate an upload using a handler, verify that uploading flag is set
        // while upload is in progress
        InMemoryUploadCallback callback = Mockito
                .spy(new InMemoryUploadCallback() {
                    @Override
                    public void complete(UploadMetadata metadata, byte[] data) {
                        Assert.assertTrue(upload.isUploading());
                    }
                });
        InMemoryUploadHandler handler = new InMemoryUploadHandler(callback);
        upload.setUploadHandler(handler);
        fakeClientCommunication();
        simulateUpload();

        // Verify flag is reset after upload completes
        Assert.assertFalse(upload.isUploading());

        // Sanity check that callback containing the assertion was called
        Mockito.verify(callback, Mockito.times(1)).complete(Mockito.any(),
                Mockito.any());
    }

    @Test
    public void uploadWithCustomHandler_activeUploadsIsChanged_from0to1_from1to0()
            throws IOException, URISyntaxException {
        // Not uploading initially
        Assert.assertFalse(upload.isUploading());

        // Simulate an upload using a custom handler, verify that uploading flag
        // is set while upload is in progress
        UploadHandler customHandler = Mockito.spy(new UploadHandler() {
            @Override
            public void handleUploadRequest(UploadEvent event) {
                // when the upload starts,
                // then the upload is in progress
                Assert.assertTrue(upload.isUploading());
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
        fakeClientCommunication();
        simulateUpload();

        // Verify flag is reset after upload completes
        Assert.assertFalse(upload.isUploading());

        // verify that the original methods of custom handler were called
        Mockito.verify(customHandler)
                .handleUploadRequest(Mockito.any(UploadEvent.class));
        Mockito.verify(customHandler).responseHandled(Mockito.anyBoolean(),
                Mockito.any(VaadinResponse.class));
        Assert.assertEquals(111L, customHandler.getRequestSizeMax());
        Assert.assertEquals(222L, customHandler.getFileSizeMax());
        Assert.assertEquals(333L, customHandler.getFileCountMax());
        Assert.assertEquals("custom-postfix", customHandler.getUrlPostfix());
        Assert.assertTrue(customHandler.isAllowInert());
        Assert.assertEquals(DisabledUpdateMode.ALWAYS,
                customHandler.getDisabledUpdateMode());
    }

    @Test
    public void uploadWithoutHandler_throwsWhenUploadStarts()
            throws URISyntaxException {
        fakeClientCommunication();
        UploadHandler uploadHandler = getUploadHandler();

        Assert.assertThrows(
                "Expected IllegalStateException for Upload with no handler",
                IllegalStateException.class,
                () -> uploadHandler.handleUploadRequest(null));
    }

    @Test
    public void setUploadHandler_maxFileIsDefined_maxFileIsPreserved() {
        upload.setMaxFiles(5);
        upload.setUploadHandler(event -> {
        });
        Assert.assertEquals(5, upload.getMaxFiles());
    }

    @Test
    public void setUploadHandler_maxFileIsNotDefined_doesNotSetMaxFiles() {
        upload.setUploadHandler(event -> {
        });
        Assert.assertNull(upload.getElement().getProperty("maxFiles"));
    }

    private void simulateUpload() throws IOException, URISyntaxException {
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        VaadinSession session = Mockito.mock(VaadinSession.class);

        Mockito.when(request.getInputStream())
                .thenReturn(new ByteArrayInputStream("test data".getBytes()));

        UploadHandler uploadHandler = getUploadHandler();
        uploadHandler.handleRequest(request, response, session,
                upload.getElement());
    }

    private UploadHandler getUploadHandler() throws URISyntaxException {
        String target = upload.getElement().getAttribute("target");
        Assert.assertNotNull("Target attribute is not set", target);

        Optional<AbstractStreamResource> maybeResource = streamResourceRegistry
                .getResource(new URI(target));
        Assert.assertTrue("No resource found for target: " + target,
                maybeResource.isPresent());

        StreamResourceRegistry.ElementStreamResource elementStreamResource = (StreamResourceRegistry.ElementStreamResource) maybeResource
                .get();

        return (UploadHandler) elementStreamResource.getElementRequestHandler();
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
