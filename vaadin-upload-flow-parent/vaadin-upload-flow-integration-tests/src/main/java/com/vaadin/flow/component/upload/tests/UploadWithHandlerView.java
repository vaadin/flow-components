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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;

// Note: this test needs Push enabled for Upload handler to push UI updates
@Route("vaadin-upload/handler")
public class UploadWithHandlerView extends Div {

    static final String UPLOAD_TEST_CONTENT_SIZE_ID = "upload-with-handler-output-id";
    static final String UPLOAD_ID = "upload-with-handler-id";
    static final String UPLOAD_HANDLER_EVENTS_ID = "upload-with-handler-events-id";
    static final String WAITING_MESSAGE = "Waiting for upload...";

    public UploadWithHandlerView() {
        Div output = new Div(WAITING_MESSAGE);
        Div handlerEventsOutput = new Div();

        InMemoryUploadHandler handler = UploadHandler
                .inMemory((metaData, bytes) -> UI.getCurrent().access(
                        () -> output.setText(String.valueOf(bytes.length))))
                .whenStart(() -> handlerEventsOutput.add("started"))
                .onProgress((transferred, total) -> handlerEventsOutput
                        .add("-progress"))
                .whenComplete(success -> {
                    if (success) {
                        handlerEventsOutput.add("-completed");
                    } else {
                        handlerEventsOutput.add("-error");
                    }
                });

        Upload upload = new Upload(handler);
        upload.setMaxFileSize(1024 * 100);
        upload.setId(UPLOAD_ID);
        output.setId(UPLOAD_TEST_CONTENT_SIZE_ID);
        handlerEventsOutput.setId(UPLOAD_HANDLER_EVENTS_ID);

        addCard("Upload with handler", upload, output, handlerEventsOutput);
    }

    private void addCard(String title, Component... components) {
        Div container = new Div(components);
        container.addComponentAsFirst(new H2(title));
        add(container);
    }
}
