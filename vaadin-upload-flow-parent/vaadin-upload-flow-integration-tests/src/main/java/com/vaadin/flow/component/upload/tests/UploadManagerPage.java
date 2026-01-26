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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Test page for UploadManager.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-upload/manager")
public class UploadManagerPage extends UploadDropZone {

    private final Div logArea;
    private final Div owner;
    private UploadManager manager;
    private UploadButton uploadButton;
    private UploadFileList fileList;

    public UploadManagerPage() {
        // Log area for events
        logArea = new Div();
        logArea.setId("log-area");

        // We're using a separate Div as the owner component for testing
        // purposes. In real usage, the owner component would typically be the
        // UploadButton, the current view, or some other component in the UI
        // depending on the case.
        owner = new Div();
        owner.setId("owner");
        add(owner);

        // Create the manager with an upload handler
        manager = new UploadManager(owner,
                UploadHandler.inMemory((metadata, data) -> {
                    // Use UI.access() since upload handler runs on background
                    // thread
                    UI.getCurrent()
                            .access(() -> log("Uploaded: " + metadata.fileName()
                                    + " (" + data.length + " bytes)"));
                }));

        // Link the drop zone to the manager
        setManager(manager);

        // Add event listeners
        manager.addFileRemovedListener(
                event -> log("Removed: " + event.getFileName()));
        manager.addFileRejectedListener(event -> log("Rejected: "
                + event.getFileName() + " - " + event.getErrorMessage()));
        manager.addAllFinishedListener(event -> log("All uploads finished"));

        // Create upload button linked to the manager
        uploadButton = new UploadButton(manager);
        uploadButton.setId("upload-button");
        uploadButton.setText("Select Files");
        add(uploadButton);

        // Create file list linked to the manager
        fileList = new UploadFileList(manager);
        fileList.setId("file-list");
        add(fileList);

        // Add status button
        var statusButton = new NativeButton("Log Status",
                event -> log("Status: enabled=" + manager.isEnabled()
                        + ", uploading=" + manager.isUploading() + ", maxFiles="
                        + manager.getMaxFiles() + ", maxFileSize="
                        + manager.getMaxFileSize() + ", autoUpload="
                        + manager.isAutoUpload() + ", acceptedTypes="
                        + manager.getAcceptedFileTypes()));
        statusButton.setId("status-button");
        add(statusButton);

        // Add control buttons for testing various API methods
        addControlButtons();

        add(logArea);
    }

    private void addControlButtons() {
        // --- Max Files ---
        var maxFilesGroup = createButtonGroup("Max Files:");
        var setMaxFiles1 = new NativeButton("1",
                event -> manager.setMaxFiles(1));
        setMaxFiles1.setId("set-max-files-1");
        var setMaxFiles3 = new NativeButton("3",
                event -> manager.setMaxFiles(3));
        setMaxFiles3.setId("set-max-files-3");
        var setMaxFilesUnlimited = new NativeButton("Unlimited",
                event -> manager.setMaxFiles(0));
        setMaxFilesUnlimited.setId("set-max-files-unlimited");
        maxFilesGroup.add(setMaxFiles1, setMaxFiles3, setMaxFilesUnlimited);
        add(maxFilesGroup);

        // --- Max File Size ---
        var maxFileSizeGroup = createButtonGroup("Max File Size:");
        var setMaxFileSize100 = new NativeButton("100 bytes",
                event -> manager.setMaxFileSize(100));
        setMaxFileSize100.setId("set-max-file-size-100");
        var setMaxFileSizeUnlimited = new NativeButton("Unlimited",
                event -> manager.setMaxFileSize(0));
        setMaxFileSizeUnlimited.setId("set-max-file-size-unlimited");
        maxFileSizeGroup.add(setMaxFileSize100, setMaxFileSizeUnlimited);
        add(maxFileSizeGroup);

        // --- Accepted File Types ---
        var acceptGroup = createButtonGroup("Accept:");
        var setAcceptText = new NativeButton("text/*",
                event -> manager.setAcceptedFileTypes("text/*"));
        setAcceptText.setId("set-accept-text");
        var setAcceptImage = new NativeButton("image/*",
                event -> manager.setAcceptedFileTypes("image/*"));
        setAcceptImage.setId("set-accept-image");
        var setAcceptMultiple = new NativeButton("text/*,application/pdf",
                event -> manager.setAcceptedFileTypes("text/*",
                        "application/pdf"));
        setAcceptMultiple.setId("set-accept-multiple");
        var clearAccept = new NativeButton("Clear",
                event -> manager.setAcceptedFileTypes((String[]) null));
        clearAccept.setId("clear-accept");
        acceptGroup.add(setAcceptText, setAcceptImage, setAcceptMultiple,
                clearAccept);
        add(acceptGroup);

        // --- Auto Upload ---
        var autoUploadGroup = createButtonGroup("Auto Upload:");
        var disableAutoUpload = new NativeButton("Disable",
                event -> manager.setAutoUpload(false));
        disableAutoUpload.setId("disable-auto-upload");
        var enableAutoUpload = new NativeButton("Enable",
                event -> manager.setAutoUpload(true));
        enableAutoUpload.setId("enable-auto-upload");
        autoUploadGroup.add(disableAutoUpload, enableAutoUpload);
        add(autoUploadGroup);

        // --- Manager Enabled ---
        var enabledGroup = createButtonGroup("Manager:");
        var disableManager = new NativeButton("Disable",
                event -> manager.setEnabled(false));
        disableManager.setId("disable-manager");
        var enableManager = new NativeButton("Enable",
                event -> manager.setEnabled(true));
        enableManager.setId("enable-manager");
        enabledGroup.add(disableManager, enableManager);
        add(enabledGroup);

        // --- Owner Lifecycle ---
        var ownerGroup = createButtonGroup("Owner:");
        var detachOwner = new NativeButton("Detach", event -> {
            if (owner.getParent().isPresent()) {
                remove(owner);
                log("Owner detached");
            }
        });
        detachOwner.setId("detach-owner");
        var reattachOwner = new NativeButton("Reattach", event -> {
            if (owner.getParent().isEmpty()) {
                addComponentAsFirst(owner);
                log("Owner reattached");
            }
        });
        reattachOwner.setId("reattach-owner");
        ownerGroup.add(detachOwner, reattachOwner);
        add(ownerGroup);

        // --- File Operations ---
        var fileOpsGroup = createButtonGroup("Files:");
        var clearFileListBtn = new NativeButton("Clear List",
                event -> manager.clearFileList());
        clearFileListBtn.setId("clear-file-list");
        fileOpsGroup.add(clearFileListBtn);
        add(fileOpsGroup);

        // --- Log ---
        var logGroup = createButtonGroup("Log:");
        var clearLog = new NativeButton("Clear", event -> logArea.removeAll());
        clearLog.setId("clear-log");
        logGroup.add(clearLog);
        add(logGroup);
    }

    private Div createButtonGroup(String label) {
        var group = new Div();
        group.getStyle().set("display", "flex");
        group.getStyle().set("gap", "5px");
        group.getStyle().set("align-items", "center");
        group.getStyle().set("margin", "5px 0");

        var labelSpan = new Div();
        labelSpan.setText(label);
        labelSpan.getStyle().set("font-weight", "bold");
        labelSpan.getStyle().set("min-width", "120px");
        group.add(labelSpan);

        return group;
    }

    private void log(String message) {
        var entry = new Div();
        entry.setText(message);
        logArea.addComponentAsFirst(entry);
    }
}
