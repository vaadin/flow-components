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
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Demo page for headless upload mode with custom external components.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload/headless")
public class HeadlessUploadPage extends Div {

    public HeadlessUploadPage() {
        setId("headless-upload-page");

        add(new H2("Headless Upload Demo"));
        add(new Paragraph(
                "This demo shows the Upload component in headless mode with custom external components."));

        // Create the upload component in headless mode with an upload handler
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            System.out.println("Uploaded file: " + metadata.fileName()
                    + ", size: " + data.length + " bytes");
        }));
        upload.addFileRemovedListener(event -> {
            System.out.println("Removed file: " + event.getFileName());
        });
        upload.setHeadless(true);
        upload.setId("upload");

        // Create custom add button
        NativeButton addButton = new NativeButton("Select Files");
        addButton.setId("add-button");

        // Create custom drop zone with content
        UploadDropZone dropZone = new UploadDropZone();
        dropZone.setId("drop-zone");
        dropZone.getStyle().set("border", "2px dashed #ccc");
        dropZone.getStyle().set("padding", "40px");
        dropZone.getStyle().set("text-align", "center");

        Div dropZoneContent = new Div();
        dropZoneContent.add(new Span("Drag and drop files here or "));
        dropZoneContent.add(addButton);
        dropZone.add(dropZoneContent);

        // Create custom file list
        UploadFileList fileList = new UploadFileList();
        fileList.setId("file-list");

        // Connect external components to upload
        upload.setAddButton(addButton);
        upload.setDropZone(dropZone);
        upload.setFileList(fileList);

        // File list section
        Div fileListSection = new Div();
        fileListSection.add(new H3("Uploaded Files"));
        fileListSection.add(fileList);

        add(upload, dropZone, fileListSection);
    }
}
