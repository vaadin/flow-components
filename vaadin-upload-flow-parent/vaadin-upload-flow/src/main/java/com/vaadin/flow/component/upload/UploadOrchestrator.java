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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.shared.Registration;

import tools.jackson.databind.node.ObjectNode;

/**
 * A non-visual orchestrator that manages file uploads using external UI
 * components. It programmatically creates a JavaScript {@code UploadOrchestrator}
 * instance and connects it to the provided drop zone, add button, and file list
 * components.
 * <p>
 * Unlike the {@link Upload} component which provides a complete upload UI, this
 * orchestrator is invisible and delegates all UI rendering to external
 * components that you provide. This allows complete freedom in designing the
 * upload experience.
 * <p>
 * The orchestrator automatically initializes when the components are attached
 * to the UI and cleans up when they are detached.
 * <p>
 * Note: The JavaScript module for the orchestrator is loaded via
 * {@link UploadDropZone}. Ensure at least an {@link UploadDropZone} is used
 * to guarantee the module is bundled.
 * <p>
 * Example usage:
 *
 * <pre>
 * // Create external UI components
 * var dropZone = new UploadDropZone();
 * var addButton = new NativeButton("Select Files");
 * var fileList = new UploadFileList();
 *
 * // Create orchestrator and connect components
 * var orchestrator = new UploadOrchestrator();
 * orchestrator.setDropZone(dropZone);
 * orchestrator.setAddButton(addButton);
 * orchestrator.setFileList(fileList);
 * orchestrator.setUploadHandler(UploadHandler.inMemory((metadata, data) -&gt; {
 *     // Process uploaded file
 * }));
 *
 * // Add UI components to the layout
 * add(dropZone, addButton, fileList);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see Upload
 * @see UploadDropZone
 * @see UploadFileList
 */
public class UploadOrchestrator implements Serializable {

    private Component addButton;
    private UploadDropZone dropZone;
    private HasUploadFileList fileList;
    private UploadHandler uploadHandler;
    private String targetName = "upload";

    // Configuration properties
    private int maxFiles = 0;
    private int maxFileSize = 0;
    private String accept;
    private boolean autoUpload = true;
    private boolean dropAllowed = true;
    private UploadI18N i18n;

    // Listeners
    private final List<SerializableConsumer<FileRemovedEventData>> fileRemovedListeners = new ArrayList<>();
    private final List<SerializableConsumer<FileRejectedEventData>> fileRejectedListeners = new ArrayList<>();

    // DOM listener registrations
    private DomListenerRegistration fileRemoveRegistration;
    private DomListenerRegistration fileRejectRegistration;

    // Track if initialized
    private boolean initialized;

    /**
     * Creates a new upload orchestrator.
     */
    public UploadOrchestrator() {
    }

    /**
     * Creates a new upload orchestrator with the given upload handler.
     *
     * @param handler
     *            the upload handler to use
     */
    public UploadOrchestrator(UploadHandler handler) {
        this();
        setUploadHandler(handler);
    }

    /**
     * Sets a custom component to be used as the add button. When clicked, this
     * component will open the file selection dialog.
     *
     * @param addButton
     *            the component to use as the add button, or {@code null} to
     *            remove
     */
    public void setAddButton(Component addButton) {
        this.addButton = addButton;
        initializeIfReady();
    }

    /**
     * Gets the custom component used as the add button.
     *
     * @return the add button component, or {@code null} if not set
     */
    public Component getAddButton() {
        return addButton;
    }

    /**
     * Sets the drop zone component. Files can be dragged and dropped onto this
     * component to upload them.
     *
     * @param dropZone
     *            the drop zone component, or {@code null} to remove
     */
    public void setDropZone(UploadDropZone dropZone) {
        this.dropZone = dropZone;
        initializeIfReady();
    }

    /**
     * Gets the drop zone component.
     *
     * @return the drop zone component, or {@code null} if not set
     */
    public UploadDropZone getDropZone() {
        return dropZone;
    }

    /**
     * Sets a custom component to display the list of files being uploaded.
     *
     * @param <T>
     *            the type of the file list component
     * @param fileList
     *            the file list component, or {@code null} to remove
     */
    public <T extends Component & HasUploadFileList> void setFileList(
            T fileList) {
        this.fileList = fileList;
        initializeIfReady();
    }

    /**
     * Gets the file list component.
     *
     * @return the file list component, or {@code null} if not set
     */
    public HasUploadFileList getFileList() {
        return fileList;
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
        this.targetName = targetName;
        initializeIfReady();
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
        updateProperty("maxFileSize", maxFileSize == 0 ? "Infinity" : maxFileSize);
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
     * Define whether the element supports dropping files on it for uploading.
     * By default it's enabled in desktop and disabled in touch devices because
     * mobile devices do not support drag events in general. Setting it
     * {@code true} means that drop is enabled even in touch-devices, and
     * {@code false} disables drop in all devices.
     *
     * @param dropAllowed
     *            {@code true} to allow file dropping, {@code false} otherwise
     */
    public void setDropAllowed(boolean dropAllowed) {
        this.dropAllowed = dropAllowed;
        updateProperty("nodrop", !dropAllowed);
    }

    /**
     * Get whether file dropping is allowed or not. By default it's enabled in
     * desktop and disabled in touch devices because mobile devices do not
     * support drag events in general.
     *
     * @return {@code true} if file dropping is allowed, {@code false} otherwise
     */
    public boolean isDropAllowed() {
        return dropAllowed;
    }

    /**
     * Set the internationalization properties for this orchestrator.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(UploadI18N i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        updateI18n();
    }

    /**
     * Get the internationalization object previously set for this orchestrator.
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public UploadI18N getI18n() {
        return i18n;
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
        fileRejectedListeners.add(listener);
        return () -> fileRejectedListeners.remove(listener);
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

    private void initializeIfReady() {
        // We need at least one component and a handler to initialize
        Element anchorElement = getAnchorElement();
        if (anchorElement == null || uploadHandler == null) {
            return;
        }

        // Register the stream resource with the anchor element
        StreamResourceRegistry.ElementStreamResource elementStreamResource = new StreamResourceRegistry.ElementStreamResource(
                uploadHandler, anchorElement) {
            @Override
            public String getName() {
                return targetName;
            }
        };

        // Set the target attribute and create the JS orchestrator
        anchorElement.getNode().runWhenAttached(ui -> ui.beforeClientResponse(
                anchorElement.getComponent().orElse(null), context -> {
                    anchorElement.setAttribute("data-upload-target",
                            elementStreamResource);

                    // Build configuration options
                    StringBuilder options = new StringBuilder();
                    options.append("{ target: target");
                    if (maxFiles > 0) {
                        options.append(", maxFiles: ").append(maxFiles);
                    }
                    if (maxFileSize > 0) {
                        options.append(", maxFileSize: ").append(maxFileSize);
                    }
                    if (accept != null && !accept.isEmpty()) {
                        options.append(", accept: '").append(escapeJs(accept)).append("'");
                    }
                    if (!autoUpload) {
                        options.append(", noAuto: true");
                    }
                    if (!dropAllowed) {
                        options.append(", nodrop: true");
                    }
                    options.append(" }");

                    // Create the JS orchestrator using element.executeJs
                    // This automatically handles lifecycle (cleanup on detach)
                    anchorElement.executeJs(
                            "const target = this.getAttribute('data-upload-target');" +
                            "const orchestrator = new window.Vaadin.UploadOrchestrator(" + options + ");" +
                            "if ($0) orchestrator.dropZone = $0;" +
                            "if ($1) orchestrator.addButton = $1;" +
                            "if ($2) orchestrator.fileList = $2;" +
                            "this.__orchestrator = orchestrator;",
                            dropZone != null ? dropZone.getElement() : null,
                            addButton != null ? addButton.getElement() : null,
                            fileList != null ? ((Component) fileList).getElement() : null);

                    // Set up event listeners on the anchor element
                    setupEventListeners(anchorElement);

                    // Apply i18n if set
                    if (i18n != null) {
                        updateI18n();
                    }

                    initialized = true;
                }));
    }

    private void setupEventListeners(Element anchorElement) {
        // Clean up old registrations
        if (fileRemoveRegistration != null) {
            fileRemoveRegistration.remove();
        }
        if (fileRejectRegistration != null) {
            fileRejectRegistration.remove();
        }

        // Set up file-remove listener
        fileRemoveRegistration = anchorElement.addEventListener("file-remove", event -> {
            String fileName = event.getEventData().get("event.detail.file.name").asString();
            FileRemovedEventData eventData = new FileRemovedEventData(fileName);
            fileRemovedListeners.forEach(listener -> listener.accept(eventData));
        }).addEventData("event.detail.file.name");

        // Set up file-reject listener
        fileRejectRegistration = anchorElement.addEventListener("file-reject", event -> {
            String fileName = event.getEventData().get("event.detail.file.name").asString();
            String error = event.getEventData().get("event.detail.error").asString();
            FileRejectedEventData eventData = new FileRejectedEventData(fileName, error);
            fileRejectedListeners.forEach(listener -> listener.accept(eventData));
        }).addEventData("event.detail.file.name").addEventData("event.detail.error");

        // Forward events from the orchestrator to the anchor element
        anchorElement.executeJs(
                "const orchestrator = this.__orchestrator;" +
                "if (orchestrator) {" +
                "  orchestrator.addEventListener('file-remove', (e) => {" +
                "    this.dispatchEvent(new CustomEvent('file-remove', { detail: e.detail }));" +
                "  });" +
                "  orchestrator.addEventListener('file-reject', (e) => {" +
                "    this.dispatchEvent(new CustomEvent('file-reject', { detail: e.detail }));" +
                "  });" +
                "}");
    }

    private void updateProperty(String property, Object value) {
        Element anchorElement = getAnchorElement();
        if (anchorElement != null && initialized) {
            String jsValue;
            if (value instanceof String) {
                jsValue = "'" + escapeJs((String) value) + "'";
            } else if (value instanceof Boolean) {
                jsValue = value.toString();
            } else {
                jsValue = String.valueOf(value);
            }
            anchorElement.executeJs(
                    "if (this.__orchestrator) { this.__orchestrator." + property + " = " + jsValue + "; }");
        }
    }

    private void updateI18n() {
        Element anchorElement = getAnchorElement();
        if (anchorElement != null && initialized && i18n != null) {
            ObjectNode i18nJson = JacksonUtils.beanToJson(i18n);
            anchorElement.executeJs(
                    "if (this.__orchestrator) { " +
                    "  const currentI18n = this.__orchestrator.i18n || {};" +
                    "  this.__orchestrator.i18n = Object.assign({}, currentI18n, $0);" +
                    "}",
                    i18nJson);
        }
    }

    private String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private Element getAnchorElement() {
        if (dropZone != null) {
            return dropZone.getElement();
        }
        if (addButton != null) {
            return addButton.getElement();
        }
        if (fileList != null) {
            return ((Component) fileList).getElement();
        }
        return null;
    }
}
