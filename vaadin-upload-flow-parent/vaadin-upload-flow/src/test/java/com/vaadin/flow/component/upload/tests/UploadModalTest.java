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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamVariable;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class UploadModalTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    private Upload upload;

    @BeforeEach
    void setup() {
        upload = new Upload();
        ui.add(upload);
        openModal();
    }

    @ParameterizedTest
    @ValueSource(strings = { "upload-success", "upload-error", "upload-abort" })
    void modalOpen_uploadFinished_firesAllFinished(String finishEvent) {
        AtomicBoolean allFinished = new AtomicBoolean();
        upload.addAllFinishedListener(event -> allFinished.set(true));

        fireUploadDomEvent("upload-start", true);
        fireUploadDomEvent(finishEvent, false);

        Assertions.assertTrue(allFinished.get());
    }

    @SuppressWarnings("removal")
    @Test
    void modalOpen_receiverUploadAborted_interruptsUpload()
            throws URISyntaxException {
        upload.setReceiver(new MemoryBuffer());
        ui.fakeClientCommunication();
        StreamVariable streamVariable = getStreamReceiver().getStreamVariable();
        streamVariable.streamingStarted(
                Mockito.mock(StreamVariable.StreamingStartEvent.class));

        fireUploadDomEvent("upload-abort", false);

        Assertions.assertTrue(streamVariable.isInterrupted());
    }

    private void openModal() {
        Div modal = new Div();
        ui.add(modal);
        ui.getUI().setChildComponentModal(modal, true);
        ui.fakeClientCommunication();
        Assertions.assertTrue(upload.getElement().getNode().isInert(),
                "The modal should make the upload inert");
    }

    private void fireUploadDomEvent(String eventType, boolean filesUploading) {
        Element element = upload.getElement();
        DomEvent event = new DomEvent(element, eventType,
                JacksonUtils.createObjectNode().put(
                        "element.files.some(file => file.uploading)",
                        filesUploading));
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(event);
    }

    @SuppressWarnings("removal")
    private StreamReceiver getStreamReceiver() throws URISyntaxException {
        String target = upload.getElement().getAttribute("target");
        Assertions.assertNotNull(target, "Target attribute is not set");

        Optional<AbstractStreamResource> maybeResource = ui.getSession()
                .getResourceRegistry().getResource(new URI(target));
        Assertions.assertTrue(maybeResource.isPresent(),
                "No resource found for target: " + target);
        return (StreamReceiver) maybeResource.get();
    }
}
