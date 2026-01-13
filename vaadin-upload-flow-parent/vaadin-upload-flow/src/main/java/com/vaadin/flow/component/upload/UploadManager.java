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
package com.vaadin.flow.component.upload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.shared.Registration;

/**
 * A non-visual manager that handles file upload state and XHR requests. It
 * creates a JavaScript {@code UploadManager} instance that UI components can
 * connect to.
 * <p>
 * Unlike the {@link Upload} component which provides a complete upload UI, this
 * manager is invisible and UI components connect to it by setting this manager
 * as their target. This allows complete freedom in designing the upload
 * experience.
 * <p>
 * Components connect to the manager by passing it in their constructor or via
 * setter methods. The manager does not hold references to the components -
 * instead, the components hold a reference to the manager.
 * <p>
 * Example usage:
 *
 * <pre>
 * // Create the manager with an upload handler
 * var manager = new UploadManager(this,
 *         UploadHandler.inMemory((metadata, data) -&gt; {
 *             // Process uploaded file
 *         }));
 *
 * // Create UI components linked to the manager
 * var dropZone = new UploadDropZone(manager);
 * var addButton = new UploadButton(manager);
 * var fileList = new UploadFileList(manager);
 *
 * // Add UI components to the layout
 * add(dropZone, addButton, fileList);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see Upload
 * @see UploadDropZone
 * @see UploadButton
 * @see UploadFileList
 */
public class UploadManager implements Serializable {

    private final String id = UUID.randomUUID().toString();
    private final Component owner;

    private UploadHandler uploadHandler;

    // Configuration properties
    private int maxFiles = 0;
    private int maxFileSize = 0;
    private String accept;
    private boolean autoUpload = true;

    // Listeners
    private final List<SerializableConsumer<FileRemovedEventData>> fileRemovedListeners = new ArrayList<>();
    private final List<SerializableConsumer<FileRejectedEventData>> fileRejectedListeners = new ArrayList<>();

    // Track if initialized
    private boolean initialized;

    /**
     * Creates a new upload manager without an upload handler. The handler must
     * be set using {@link #setUploadHandler(UploadHandler)} before uploads can
     * work.
     *
     * @param owner
     *            the owner component this manager is attached to
     */
    public UploadManager(Component owner) {
        // TODO: How to make sure vaadin-upload-manager-connector.js is loaded?
        this(owner, null);
    }

    /**
     * Creates a new upload manager with the given upload handler.
     *
     * @param owner
     *            the owner component this manager is attached to
     * @param handler
     *            the upload handler to use
     */
    public UploadManager(Component owner, UploadHandler handler) {
        this.owner = Objects.requireNonNull(owner, "Owner component cannot be null");
        if (handler != null) {
            setUploadHandler(handler);
        }
    }

    /**
     * Gets the unique identifier for this manager.
     *
     * @return the manager ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the upload handler that processes uploaded files.
     *
     * @param handler
     *            the upload handler, not {@code null}
     */
    public void setUploadHandler(UploadHandler handler) {
        setUploadHandler(handler, "upload");
    }

    /**
     * Sets the upload handler that processes uploaded files.
     *
     * @param handler
     *            the upload handler, not {@code null}
     * @param targetName
     *            the endpoint name (single path segment), used as the last path
     *            segment of the upload URL, not {@code null} or blank
     */
    public void setUploadHandler(UploadHandler handler, String targetName) {
        Objects.requireNonNull(handler, "UploadHandler cannot be null");
        Objects.requireNonNull(targetName, "The target name cannot be null");
        if (targetName.isBlank()) {
            throw new IllegalArgumentException(
                    "The target name cannot be blank");
        }
        this.uploadHandler = handler;
        initializeManager();
    }

    /**
     * Limit of files to upload, by default it is unlimited. If the value is set
     * to one, the native file browser will prevent selecting multiple files.
     *
     * @param maxFiles
     *            the maximum number of files allowed for the user to select, or
     *            0 for unlimited
     */
    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
        updateProperty("maxFiles", maxFiles == 0 ? "Infinity" : maxFiles);
    }

    /**
     * Get the maximum number of files allowed for the user to select to upload.
     *
     * @return the maximum number of files, or 0 if unlimited
     */
    public int getMaxFiles() {
        return maxFiles;
    }

    /**
     * Specify the maximum file size in bytes allowed to upload. Notice that it
     * is a client-side constraint, which will be checked before sending the
     * request.
     *
     * @param maxFileSize
     *            the maximum file size in bytes, or 0 for unlimited
     */
    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
        updateProperty("maxFileSize",
                maxFileSize == 0 ? "Infinity" : maxFileSize);
    }

    /**
     * Get the maximum allowed file size in the client-side, in bytes.
     *
     * @return the maximum file size in bytes, or 0 if unlimited
     */
    public int getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Specify the types of files that the upload accepts. Syntax: a MIME type
     * pattern (wildcards are allowed) or file extensions. Notice that MIME
     * types are widely supported, while file extensions are only implemented in
     * certain browsers, so it should be avoided.
     * <p>
     * Example: <code>"video/*","image/tiff"</code> or
     * <code>".pdf","audio/mp3"</code>
     *
     * @param acceptedFileTypes
     *            the allowed file types to be uploaded, or {@code null} to
     *            clear any restrictions
     */
    public void setAcceptedFileTypes(String... acceptedFileTypes) {
        if (acceptedFileTypes != null) {
            this.accept = String.join(",", acceptedFileTypes);
        } else {
            this.accept = null;
        }
        updateProperty("accept", accept != null ? accept : "");
    }

    /**
     * Get the list of accepted file types for upload.
     *
     * @return a list of allowed file types, never {@code null}
     */
    public List<String> getAcceptedFileTypes() {
        if (accept == null || accept.isEmpty()) {
            return Collections.emptyList();
        }
        return List.of(accept.split(","));
    }

    /**
     * When {@code false}, it prevents uploads from triggering immediately upon
     * adding file(s). The default is {@code true}.
     *
     * @param autoUpload
     *            {@code true} to allow uploads to start immediately after
     *            selecting files, {@code false} otherwise
     */
    public void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
        updateProperty("noAuto", !autoUpload);
    }

    /**
     * Get the auto upload status.
     *
     * @return {@code true} if the upload of files should start immediately
     *         after they are selected, {@code false} otherwise
     */
    public boolean isAutoUpload() {
        return autoUpload;
    }

    /**
     * Adds a listener for events fired when a file is removed.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addFileRemovedListener(
            SerializableConsumer<FileRemovedEventData> listener) {
        fileRemovedListeners.add(listener);
        return () -> fileRemovedListeners.remove(listener);
    }

    /**
     * Adds a listener for {@code file-reject} events fired when a file cannot
     * be added due to some constraints:
     * {@code setMaxFileSize, setMaxFiles, setAcceptedFileTypes}
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addFileRejectedListener(
            SerializableConsumer<FileRejectedEventData> listener) {
        // TODO: Let's just use regular component events now that we have the owner component
        fileRejectedListeners.add(listener);
        return () -> fileRejectedListeners.remove(listener);
    }

    private void initializeManager() {
        if (uploadHandler == null) {
            return;
        }

        Element ownerElement = owner.getElement();

        // Set up event listeners on owner element for events from JS manager
        setupEventListeners(ownerElement);

        // Store the target URL as an attribute on the owner element (setAttribute
        // auto-registers the resource and converts to URL)
        final String attrName = "data-upload-manager-target-" + id;
        ownerElement.setAttribute(attrName, uploadHandler);

        // Capture current config values for the lambda
        final int currentMaxFiles = maxFiles;
        final int currentMaxFileSize = maxFileSize;
        final String currentAccept = accept;
        final boolean currentAutoUpload = autoUpload;

        ownerElement.getNode().runWhenAttached(ui -> {
            ui.beforeClientResponse(owner, context -> {
                // Build configuration options - read target from attribute and
                // remove it
                StringBuilder options = new StringBuilder();
                options.append("{ target: (()=>{ const a='").append(attrName)
                        .append("'; const t=$1.getAttribute(a); return t; })()");
                if (currentMaxFiles > 0) {
                    options.append(", maxFiles: ").append(currentMaxFiles);
                }
                if (currentMaxFileSize > 0) {
                    options.append(", maxFileSize: ").append(currentMaxFileSize);
                }
                if (currentAccept != null && !currentAccept.isEmpty()) {
                    options.append(", accept: '").append(escapeJs(currentAccept))
                            .append("'");
                }
                if (!currentAutoUpload) {
                    options.append(", noAuto: true");
                }
                options.append(" }");

                // Create the JS manager using the connector, passing the owner element
                // for event forwarding
                ui.getPage()
                        .executeJs("window.Vaadin.Upload.UploadManager.createUploadManager($0, "
                                + options + ", $1);", id, ownerElement);

                initialized = true;
            });
        });
    }

    private void setupEventListeners(Element ownerElement) {
        final String detailFileName = "event.detail.fileName";
        final String detailErrorMessage = "event.detail.errorMessage";

        // Listener for file-remove events
        ownerElement.addEventListener("upload-manager-file-remove",
                event -> {
                    String fileName = event.getEventData()
                            .get(detailFileName).asString();
                    FileRemovedEventData eventData = new FileRemovedEventData(
                            fileName);
                    fileRemovedListeners
                            .forEach(listener -> listener.accept(eventData));
                }).addEventData(detailFileName);

        // Listener for file-reject events
        ownerElement.addEventListener("upload-manager-file-reject",
                event -> {
                    String fileName = event.getEventData()
                            .get(detailFileName).asString();
                    String errorMessage = event.getEventData()
                            .get(detailErrorMessage).asString();
                    FileRejectedEventData eventData = new FileRejectedEventData(
                            fileName, errorMessage);
                    fileRejectedListeners
                            .forEach(listener -> listener.accept(eventData));
                }).addEventData(detailFileName)
                .addEventData(detailErrorMessage);
    }

    private void updateProperty(String property, Object value) {
        if (initialized) {
            String jsValue;
            if (value instanceof String) {
                jsValue = "'" + escapeJs((String) value) + "'";
            } else if (value instanceof Boolean) {
                jsValue = value.toString();
            } else {
                jsValue = String.valueOf(value);
            }
            owner.getElement().getNode().runWhenAttached(ui -> {
                ui.getPage().executeJs(
                        "const manager = window.Vaadin.Upload.UploadManager.getUploadManager($0);"
                                + "if (manager) { manager." + property + " = "
                                + jsValue + "; }",
                        id);
            });
        }
    }

    private String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    /**
     * Event data for file removed events.
     */
    public static class FileRemovedEventData implements Serializable {
        private final String fileName;

        /**
         * Creates file removed event data.
         *
         * @param fileName
         *            the name of the removed file
         */
        public FileRemovedEventData(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Gets the name of the removed file.
         *
         * @return the file name
         */
        public String getFileName() {
            return fileName;
        }
    }

    /**
     * Event data for file rejected events.
     */
    public static class FileRejectedEventData implements Serializable {
        private final String fileName;
        private final String errorMessage;

        /**
         * Creates file rejected event data.
         *
         * @param fileName
         *            the name of the rejected file
         * @param errorMessage
         *            the error message
         */
        public FileRejectedEventData(String fileName, String errorMessage) {
            this.fileName = fileName;
            this.errorMessage = errorMessage;
        }

        /**
         * Gets the name of the rejected file.
         *
         * @return the file name
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Gets the error message.
         *
         * @return the error message
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
