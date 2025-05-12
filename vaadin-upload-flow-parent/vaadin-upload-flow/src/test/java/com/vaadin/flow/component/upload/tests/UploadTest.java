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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;

public class UploadTest {

    @Test
    public void uploadNewUpload() {
        // Test no NPE due missing UI when setAttribute is called.
        Upload upload = new Upload();
    }

    @Test
    public void implementsHasEnabled() {
        Assert.assertTrue(HasEnabled.class.isAssignableFrom(Upload.class));
    }

    @Test
    public void switchBetweenSingleAndMultiFileReceiver_assertMaxFilesProperty() {
        Upload upload = new Upload();

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));

        upload.setReceiver(new MultiFileMemoryBuffer());
        Assert.assertNull(upload.getElement().getProperty("maxFiles"));

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));
    }

    @Test
    public void uploadWithHandler_doUpload_activeUploadsIsChanged_from0to1_from1to0()
            throws IOException {
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        VaadinSession session = Mockito.mock(VaadinSession.class);

        UI ui = Mockito.mock(UI.class);
        // run the command immediately
        Mockito.doAnswer(invocation -> {
            Command command = invocation.getArgument(0);
            command.execute();
            return null;
        }).when(ui).access(Mockito.any(Command.class));

        Element owner = Mockito.mock(Element.class);
        Component componentOwner = Mockito.mock(Component.class);
        Mockito.when(owner.getComponent())
                .thenReturn(Optional.of(componentOwner));
        Mockito.when(componentOwner.getUI()).thenReturn(Optional.of(ui));

        Upload upload = new Upload();
        Assert.assertFalse(upload.isUploading());

        UploadEvent uploadEvent = Mockito.mock(UploadEvent.class);
        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.when(inputStream.read(Mockito.any(byte[].class),
                Mockito.anyInt(), Mockito.anyInt()))
                .thenAnswer(invocationOnMock -> {
                    Assert.assertTrue(upload.isUploading());
                    return -1;
                });
        Mockito.when(uploadEvent.getInputStream()).thenReturn(inputStream);
        Mockito.when(uploadEvent.getRequest()).thenReturn(request);
        Mockito.when(uploadEvent.getResponse()).thenReturn(response);
        Mockito.when(uploadEvent.getSession()).thenReturn(session);
        Mockito.when(uploadEvent.getOwningElement()).thenReturn(owner);
        Mockito.when(uploadEvent.getOwningComponent())
                .thenReturn(componentOwner);

        InMemoryUploadHandler handler = new InMemoryUploadHandler(
                (metadata, data) -> Assert.assertFalse(upload.isUploading()));
        upload.setUploadHandler(handler);
        handler.handleUploadRequest(uploadEvent);
    }

    @Test
    public void uploadWithoutHandler_throwsWhenUploadStarts() {
        class TestUpload extends Upload {
            private UploadHandler handler;

            @Override
            public void setUploadHandler(UploadHandler handler) {
                super.setUploadHandler(handler);
                this.handler = handler;
            }

            public UploadHandler getHandler() {
                return handler;
            }
        }
        TestUpload testUpload = new TestUpload();
        UploadHandler handler = testUpload.getHandler();

        Assert.assertNotNull(handler);
        Assert.assertThrows(
                "Expected IllegalStateException for Upload with no handler",
                IllegalStateException.class,
                () -> handler.handleUploadRequest(null));
    }
}
