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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListI18N;
import com.vaadin.flow.component.upload.UploadManager;

import java.util.Arrays;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Demo page for UploadManager with custom external components.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload/manager")
public class UploadManagerPage extends Div {

    private final Div logArea;

    public UploadManagerPage() {
        add(new H2("Upload Manager Demo"));
        add(new Paragraph(
                "This demo shows the UploadManager with custom external components."));

        // Log area for events
        logArea = new Div();
        logArea.setId("log-area");
        logArea.getStyle().set("background", "#f5f5f5");
        logArea.getStyle().set("padding", "10px");
        logArea.getStyle().set("margin", "10px 0");
        logArea.getStyle().set("max-height", "150px");
        logArea.getStyle().set("overflow-y", "auto");
        logArea.getStyle().set("font-family", "monospace");
        logArea.getStyle().set("font-size", "12px");

        var owner = new Div();
        owner.setId("owner");
        add(owner);

        // Create the manager with an upload handler
        var manager = new UploadManager(owner,
                UploadHandler.inMemory((metadata, data) -> {
                    // Use UI.access() since upload handler runs on background thread
                    UI.getCurrent().access(() -> log(
                            "Uploaded: " + metadata.fileName() + " (" + data.length
                                    + " bytes)"));
                }));

        // Add event listeners
        manager.addFileRemovedListener(
                event -> log("Removed: " + event.getFileName()));
        manager.addFileRejectedListener(event -> log("Rejected: "
                + event.getFileName() + " - " + event.getErrorMessage()));
        manager.addAllFinishedListener(event -> log("All uploads finished"));

        // Create UI components linked to the manager
        var dropZone = new UploadDropZone(manager);
        dropZone.add(new Span("Drag and drop files here or "));

        var addButton = new UploadButton(manager);
        addButton.setText("Select Files");

        var fileList = new UploadFileList(manager);

        // Style the drop zone
        dropZone.getStyle().set("border", "2px dashed #ccc");
        dropZone.getStyle().set("padding", "40px");
        dropZone.getStyle().set("text-align", "center");

        // Add upload area
        add(dropZone, addButton, fileList);

        add(new Hr());
        add(new H3("Controls"));

        // Controls section
        var controlsDiv = new Div();
        controlsDiv.getStyle().set("display", "flex");
        controlsDiv.getStyle().set("flex-wrap", "wrap");
        controlsDiv.getStyle().set("gap", "20px");
        controlsDiv.getStyle().set("margin-bottom", "20px");

        // Enabled toggle
        var enabledBtn = new Button("Toggle Enabled", event -> {
            manager.setEnabled(!manager.isEnabled());
            log("setEnabled(" + manager.isEnabled() + ")");
        });
        controlsDiv.add(enabledBtn);

        // Auto upload toggle
        var autoUploadBtn = new Button("Toggle Auto Upload", event -> {
            manager.setAutoUpload(!manager.isAutoUpload());
            log("setAutoUpload(" + manager.isAutoUpload() + ")");
        });
        controlsDiv.add(autoUploadBtn);

        add(controlsDiv);

        // Max files control
        var maxFilesDiv = new Div();
        maxFilesDiv.getStyle().set("margin-bottom", "10px");
        var maxFilesInput = new Input();
        maxFilesInput.setType("number");
        maxFilesInput.setValue("0");
        maxFilesInput.getElement().setAttribute("min", "0");
        maxFilesInput.getStyle().set("width", "80px");
        var maxFilesBtn = new Button("Set Max Files", event -> {
            int value = Integer.parseInt(maxFilesInput.getValue());
            manager.setMaxFiles(value);
            log("setMaxFiles(" + value + ")");
        });
        maxFilesDiv.add(new NativeLabel("Max Files: "), maxFilesInput,
                maxFilesBtn);
        add(maxFilesDiv);

        // Max file size control
        var maxSizeDiv = new Div();
        maxSizeDiv.getStyle().set("margin-bottom", "10px");
        var maxSizeInput = new Input();
        maxSizeInput.setType("number");
        maxSizeInput.setValue("0");
        maxSizeInput.getElement().setAttribute("min", "0");
        maxSizeInput.getStyle().set("width", "80px");
        var maxSizeBtn = new Button("Set Max Size (KB)", event -> {
            int valueKb = Integer.parseInt(maxSizeInput.getValue());
            int valueBytes = valueKb * 1024;
            manager.setMaxFileSize(valueBytes);
            log("setMaxFileSize(" + valueBytes + " bytes)");
        });
        maxSizeDiv.add(new NativeLabel("Max Size (KB): "), maxSizeInput,
                maxSizeBtn);
        add(maxSizeDiv);

        // Accepted file types control
        var typesDiv = new Div();
        typesDiv.getStyle().set("margin-bottom", "10px");
        var typesInput = new Input();
        typesInput.setType("text");
        typesInput.setPlaceholder("e.g., image/*,.pdf");
        typesInput.getStyle().set("width", "200px");
        var typesBtn = new Button("Set Accepted Types", event -> {
            String value = typesInput.getValue();
            if (value == null || value.isBlank()) {
                manager.setAcceptedFileTypes((String[]) null);
                log("setAcceptedFileTypes(null)");
            } else {
                String[] types = value.split(",");
                manager.setAcceptedFileTypes(types);
                log("setAcceptedFileTypes(" + value + ")");
            }
        });
        typesDiv.add(new NativeLabel("Accepted Types: "), typesInput, typesBtn);
        add(typesDiv);

        add(new Hr());
        add(new H3("Actions"));

        // Action buttons
        var actionsDiv = new Div();
        actionsDiv.getStyle().set("display", "flex");
        actionsDiv.getStyle().set("gap", "10px");
        actionsDiv.getStyle().set("flex-wrap", "wrap");

        var clearFileListButton = new Button("Clear File List", event -> {
            manager.clearFileList();
            log("clearFileList()");
        });
        actionsDiv.add(clearFileListButton);

        var interruptButton = new Button("Interrupt Upload", event -> {
            manager.interruptUpload();
            log("interruptUpload() - isInterrupted: " + manager.isInterrupted());
        });
        actionsDiv.add(interruptButton);

        var statusButton = new Button("Log Status", event -> log(
                "Status: enabled=" + manager.isEnabled() + ", uploading="
                        + manager.isUploading() + ", interrupted="
                        + manager.isInterrupted() + ", maxFiles="
                        + manager.getMaxFiles() + ", maxFileSize="
                        + manager.getMaxFileSize() + ", autoUpload="
                        + manager.isAutoUpload() + ", acceptedTypes="
                        + manager.getAcceptedFileTypes()));
        actionsDiv.add(statusButton);

        add(actionsDiv);

        add(new Hr());
        add(new H3("Owner Component Actions"));

        var ownerActionsDiv = new Div();
        ownerActionsDiv.getStyle().set("display", "flex");
        ownerActionsDiv.getStyle().set("gap", "10px");

        var detachOwnerButton = new Button("Detach Owner", event -> {
            owner.removeFromParent();
            log("Owner detached");
        });
        ownerActionsDiv.add(detachOwnerButton);

        var reattachOwnerButton = new Button("Re-attach Owner", event -> {
            if (owner.getParent().isEmpty()) {
                getElement().insertChild(2, owner.getElement());
                log("Owner re-attached");
            } else {
                log("Owner already attached");
            }
        });
        ownerActionsDiv.add(reattachOwnerButton);

        add(ownerActionsDiv);

        add(new Hr());
        add(new H3("Internationalization"));

        var i18nDiv = new Div();
        i18nDiv.getStyle().set("display", "flex");
        i18nDiv.getStyle().set("gap", "10px");

        // Store references to components that need text updates
        var dropZoneText = new Span("Drag and drop files here or ");
        dropZone.removeAll();
        dropZone.add(dropZoneText);

        var finnishBtn = new Button("Finnish (Suomi)", event -> {
            // Update UploadFileList i18n
            var i18n = new UploadFileListI18N();
            i18n.setFile(new UploadFileListI18N.File()
                    .setRetry("Yritä uudelleen")
                    .setStart("Aloita")
                    .setRemove("Poista"));
            i18n.setError(new UploadFileListI18N.Error()
                    .setTooManyFiles("Liian monta tiedostoa")
                    .setFileIsTooBig("Tiedosto on liian suuri")
                    .setIncorrectFileType("Väärä tiedostotyyppi"));
            i18n.setUploading(new UploadFileListI18N.Uploading()
                    .setStatus(new UploadFileListI18N.Uploading.Status()
                            .setConnecting("Yhdistetään...")
                            .setStalled("Pysähtynyt")
                            .setProcessing("Käsitellään...")
                            .setHeld("Odottaa"))
                    .setRemainingTime(
                            new UploadFileListI18N.Uploading.RemainingTime()
                                    .setPrefix("Aikaa jäljellä: ")
                                    .setUnknown("tuntematon aika jäljellä"))
                    .setError(new UploadFileListI18N.Uploading.UploadError()
                            .setServerUnavailable("Palvelin ei ole käytettävissä")
                            .setUnexpectedServerError("Odottamaton palvelinvirhe")
                            .setForbidden("Kielletty")));
            i18n.setUnits(Arrays.asList("t", "kt", "Mt", "Gt", "Tt", "Pt", "Et",
                    "Zt", "Yt"));
            fileList.setI18n(i18n);

            // Update button and drop zone text
            addButton.setText("Valitse tiedostot");
            dropZoneText.setText("Vedä ja pudota tiedostot tähän tai ");

            log("Applied Finnish i18n");
        });
        i18nDiv.add(finnishBtn);

        var englishBtn = new Button("English", event -> {
            // Update UploadFileList i18n
            var i18n = new UploadFileListI18N();
            i18n.setFile(new UploadFileListI18N.File()
                    .setRetry("Retry")
                    .setStart("Start")
                    .setRemove("Remove"));
            i18n.setError(new UploadFileListI18N.Error()
                    .setTooManyFiles("Too many files")
                    .setFileIsTooBig("File is too big")
                    .setIncorrectFileType("Incorrect file type"));
            i18n.setUploading(new UploadFileListI18N.Uploading()
                    .setStatus(new UploadFileListI18N.Uploading.Status()
                            .setConnecting("Connecting...")
                            .setStalled("Stalled")
                            .setProcessing("Processing...")
                            .setHeld("Held"))
                    .setRemainingTime(
                            new UploadFileListI18N.Uploading.RemainingTime()
                                    .setPrefix("remaining time: ")
                                    .setUnknown("unknown remaining time"))
                    .setError(new UploadFileListI18N.Uploading.UploadError()
                            .setServerUnavailable("Server unavailable")
                            .setUnexpectedServerError("Unexpected server error")
                            .setForbidden("Forbidden")));
            i18n.setUnits(Arrays.asList("B", "kB", "MB", "GB", "TB", "PB", "EB",
                    "ZB", "YB"));
            fileList.setI18n(i18n);

            // Update button and drop zone text
            addButton.setText("Select Files");
            dropZoneText.setText("Drag and drop files here or ");

            log("Applied English i18n");
        });
        i18nDiv.add(englishBtn);

        add(i18nDiv);

        add(new Hr());
        add(new H3("Event Log"));
        var clearLogButton = new Button("Clear Log",
                event -> logArea.removeAll());
        add(clearLogButton);
        add(logArea);
    }

    private void log(String message) {
        var entry = new Div();
        entry.setText("[" + java.time.LocalTime.now().toString().substring(0, 8)
                + "] " + message);
        logArea.addComponentAsFirst(entry);
    }
}
