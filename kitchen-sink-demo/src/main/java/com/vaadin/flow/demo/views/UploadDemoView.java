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
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Upload component.
 */
@Route(value = "upload", layout = MainLayout.class)
@PageTitle("Upload | Vaadin Kitchen Sink")
public class UploadDemoView extends VerticalLayout {

    public UploadDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Upload Component"));
        add(new Paragraph("The Upload component allows users to upload files."));

        // Basic upload
        MemoryBuffer buffer = new MemoryBuffer();
        Upload basic = new Upload(buffer);
        basic.addSucceededListener(event -> {
            Notification.show("File uploaded: " + event.getFileName() +
                " (" + event.getContentLength() + " bytes)")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        addSection("Basic Upload", basic);

        // Multi-file upload
        MultiFileMemoryBuffer multiBuffer = new MultiFileMemoryBuffer();
        Upload multiFile = new Upload(multiBuffer);
        multiFile.addSucceededListener(event -> {
            Notification.show("File uploaded: " + event.getFileName());
        });
        multiFile.addAllFinishedListener(event -> {
            Notification.show("All files uploaded!")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        addSection("Multi-file Upload", multiFile);

        // With file type filter
        MemoryBuffer imageBuffer = new MemoryBuffer();
        Upload imageUpload = new Upload(imageBuffer);
        imageUpload.setAcceptedFileTypes("image/*", ".png", ".jpg", ".jpeg", ".gif");
        imageUpload.addSucceededListener(event ->
            Notification.show("Image uploaded: " + event.getFileName()));
        addSection("Image Upload Only", imageUpload);

        // With max file size
        MemoryBuffer limitedBuffer = new MemoryBuffer();
        Upload limited = new Upload(limitedBuffer);
        limited.setMaxFileSize(1024 * 1024); // 1 MB
        limited.addFileRejectedListener(event ->
            Notification.show("File too large: " + event.getErrorMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR));
        limited.addSucceededListener(event ->
            Notification.show("File uploaded: " + event.getFileName()));
        addSection("Max File Size (1 MB)", limited);

        // With max files limit
        MultiFileMemoryBuffer limitedMultiBuffer = new MultiFileMemoryBuffer();
        Upload limitedMulti = new Upload(limitedMultiBuffer);
        limitedMulti.setMaxFiles(3);
        limitedMulti.addSucceededListener(event ->
            Notification.show("File uploaded: " + event.getFileName()));
        addSection("Max 3 Files", limitedMulti);

        // Drop label customization
        MemoryBuffer customBuffer = new MemoryBuffer();
        Upload customLabel = new Upload(customBuffer);
        customLabel.setDropLabel(new Paragraph("Drop your documents here"));
        customLabel.setUploadButton(new com.vaadin.flow.component.button.Button("Choose files..."));
        customLabel.addSucceededListener(event ->
            Notification.show("Uploaded: " + event.getFileName()));
        addSection("Custom Labels", customLabel);

        // With event listeners
        MemoryBuffer eventBuffer = new MemoryBuffer();
        Upload withEvents = new Upload(eventBuffer);
        withEvents.addStartedListener(event ->
            Notification.show("Upload started: " + event.getFileName()));
        withEvents.addProgressListener(event -> {
            // Progress updates
        });
        withEvents.addSucceededListener(event ->
            Notification.show("Upload succeeded: " + event.getFileName())
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS));
        withEvents.addFailedListener(event ->
            Notification.show("Upload failed: " + event.getFileName())
                .addThemeVariants(NotificationVariant.LUMO_ERROR));
        addSection("With Event Listeners", withEvents);

        // Auto upload disabled
        MemoryBuffer manualBuffer = new MemoryBuffer();
        Upload manual = new Upload(manualBuffer);
        manual.setAutoUpload(false);
        manual.addSucceededListener(event ->
            Notification.show("File uploaded: " + event.getFileName()));
        addSection("Manual Upload (Auto-upload disabled)", manual);

        // Disabled upload
        MemoryBuffer disabledBuffer = new MemoryBuffer();
        Upload disabled = new Upload(disabledBuffer);
        disabled.setEnabled(false);
        addSection("Disabled Upload", disabled);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
