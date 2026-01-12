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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Demo page for UploadManager with custom external components.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload/manager")
public class UploadManagerPage extends Div {

    public UploadManagerPage() {
        add(new H2("Upload Manager Demo"));
        add(new Paragraph(
                "This demo shows the UploadManager with custom external components."));

        // Create the manager with an upload handler
        var manager = new UploadManager(UI.getCurrent(), UploadHandler.inMemory((metadata, data) -> {
            System.out.println("Uploaded file: " + metadata.fileName()
                    + ", size: " + data.length + " bytes");
        }));

        // Add file removed listener
        manager.addFileRemovedListener(event -> {
            System.out.println("Removed file: " + event.getFileName());
        });

        // Add file rejected listener
        manager.addFileRejectedListener(event -> {
            System.out.println("Rejected file: " + event.getFileName()
                    + ", reason: " + event.getErrorMessage());
        });

        // Create UI components linked to the manager
        var dropZone = new UploadDropZone(manager);
        dropZone.add(new Span("Drag and drop files here or "));

        var addButton = new UploadButton(manager);
        addButton.add(new Span("Select Files"));

        var fileList = new UploadFileList(manager);

        // Add UI components to the layout
        add(dropZone, addButton, fileList);

        // Style the drop zone
        dropZone.getStyle().set("border", "2px dashed #ccc");
        dropZone.getStyle().set("padding", "40px");
        dropZone.getStyle().set("text-align", "center");
    }
}
