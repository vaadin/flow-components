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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadOrchestrator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Demo page for UploadOrchestrator with custom external components.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload/orchestrator")
public class UploadOrchestratorPage extends Div {

    public UploadOrchestratorPage() {
        add(new H2("Upload Orchestrator Demo"));
        add(new Paragraph(
                "This demo shows the UploadOrchestrator with custom external components."));

        // Create external UI components
        var dropZone = new UploadDropZone();
        dropZone.add(new Span("Drag and drop files here or "));

        var addButton = new NativeButton("Select Files");

        var fileList = new UploadFileList();

        // Create orchestrator and connect components
        var orchestrator = new UploadOrchestrator();
        orchestrator.setDropZone(dropZone);
        orchestrator.setAddButton(addButton);
        orchestrator.setFileList(fileList);

        // Set up upload handling
        orchestrator.setUploadHandler(UploadHandler.inMemory((metadata, data) -> {
            System.out.println("Uploaded file: " + metadata.fileName()
                    + ", size: " + data.length + " bytes");
        }));

        // Add file removed listener
        orchestrator.addFileRemovedListener(event -> {
            System.out.println("Removed file: " + event.getFileName());
        });

        // Add file rejected listener
        orchestrator.addFileRejectedListener(event -> {
            System.out.println("Rejected file: " + event.getFileName()
                    + ", reason: " + event.getErrorMessage());
        });

        // Add UI components to the layout
        add(dropZone, addButton, fileList);

        // Style the drop zone
        dropZone.getStyle().set("border", "2px dashed #ccc");
        dropZone.getStyle().set("padding", "40px");
        dropZone.getStyle().set("text-align", "center");
    }
}
