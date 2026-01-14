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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.internal.streams.UploadCompleteEvent;
import com.vaadin.flow.internal.streams.UploadStartEvent;
import com.vaadin.flow.server.streams.UploadEvent;
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

    private final Component owner;
    private final Connector connector;

    private UploadHandler uploadHandler;

    // Configuration properties
    private int maxFiles = 0;
    private int maxFileSize = 0;
    private String accept;
    private boolean autoUpload = true;

    // Upload state tracking
    private int activeUploads = 0;
    private volatile boolean interrupted = false;
    private boolean uploading = false;
    private boolean uploadListenersRegistered = false;

    /**
     * Creates a new upload manager without an upload handler. The handler must
     * be set using {@link #setUploadHandler(UploadHandler)} before uploads can
     * work.
     *
     * @param owner
     *            the owner component this manager is attached to
     */
    public UploadManager(Component owner) {
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
        this.connector = new Connector();

        // Add connector as virtual child of owner (doesn't appear in DOM)
        owner.getElement().appendVirtualChild(connector.getElement());

        // Set up default fail-fast handler
        setUploadHandler(new FailFastUploadHandler());

        // Listen for upload-abort from client
        connector.getElement().addEventListener("upload-manager-abort",
                event -> interruptUpload());

        if (handler != null) {
            setUploadHandler(handler);
        }
    }

    /**
     * Gets the owner component this manager is attached to.
     *
     * @return the owner component
     */
    public Component getOwner() {
        return owner;
    }

    /**
     * Gets the internal connector component.
     *
     * @return the connector component
     */
    Connector getConnector() {
        return connector;
    }

    /**
     * Sets the upload handler that processes uploaded files.
     * <p>
     * This overload uses the default upload target name {@code "upload"}, which
     * becomes the last segment of the dynamically generated upload URL.
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
     *            segment of the dynamically generated upload URL; must not be
     *            blank
     */
    public void setUploadHandler(UploadHandler handler, String targetName) {
        Objects.requireNonNull(handler, "UploadHandler cannot be null");
        Objects.requireNonNull(targetName, "The target name cannot be null");
        if (targetName.isBlank()) {
            throw new IllegalArgumentException(
                    "The target name cannot be blank");
        }
        this.uploadHandler = handler;
        connector.getElement().setAttribute("target", uploadHandler);

        // Register internal listeners for upload state tracking
        if (!(handler instanceof FailFastUploadHandler)
                && !uploadListenersRegistered) {
            uploadListenersRegistered = true;
            ComponentUtil.addListener(connector, UploadStartEvent.class,
                    event -> startUpload());
            ComponentUtil.addListener(connector, UploadCompleteEvent.class,
                    event -> endUpload());
        }
    }

    /**
     * Gets the upload handler that processes uploaded files.
     *
     * @return the upload handler, or {@code null} if not set
     */
    public UploadHandler getUploadHandler() {
        if (uploadHandler instanceof FailFastUploadHandler) {
            return null;
        }
        return uploadHandler;
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
        connector.getElement().setProperty("maxFiles",
                maxFiles == 0 ? Double.POSITIVE_INFINITY : (double) maxFiles);
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
        connector.getElement().setProperty("maxFileSize",
                maxFileSize == 0 ? Double.POSITIVE_INFINITY : (double) maxFileSize);
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
        connector.getElement().setProperty("accept", accept != null ? accept : "");
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
        connector.getElement().setProperty("autoUpload", autoUpload);
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
     * Checks if an upload is currently in progress.
     *
     * @return {@code true} if receiving upload, {@code false} otherwise
     */
    public boolean isUploading() {
        return activeUploads > 0;
    }

    /**
     * Checks if the upload has been interrupted.
     *
     * @return {@code true} if the upload was interrupted, {@code false}
     *         otherwise
     */
    public boolean isInterrupted() {
        return interrupted;
    }

    /**
     * Interrupt the upload currently being received.
     * <p>
     * The interruption will be done by the receiving thread so this method will
     * return immediately and the actual interrupt will happen a bit later.
     * <p>
     * Note! This will interrupt all ongoing uploads.
     */
    public void interruptUpload() {
        if (isUploading()) {
            interrupted = true;
        }
    }

    /**
     * Clear the list of files being processed, or already uploaded.
     */
    public void clearFileList() {
        connector.getElement().executeJs(
                "if (this.manager) { this.manager.files = []; }");
    }

    /**
     * Go into upload state. This is to prevent uploading more files than
     * accepted on same component.
     *
     * @throws IllegalStateException
     *             if maximum supported amount of uploads already started
     */
    private void startUpload() {
        if (maxFiles != 0 && maxFiles <= activeUploads) {
            throw new IllegalStateException(
                    "Maximum supported amount of uploads already started");
        }
        activeUploads++;
        uploading = true;
    }

    /**
     * End upload state and check if all uploads are finished.
     */
    private void endUpload() {
        activeUploads--;
        if (activeUploads == 0) {
            interrupted = false;
            if (uploading) {
                uploading = false;
                fireAllFinished();
            }
        }
    }

    private void fireAllFinished() {
        ComponentUtil.fireEvent(connector, new AllFinishedEvent(connector));
    }

    /**
     * Adds a listener for events fired when a file is removed.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Registration addFileRemovedListener(
            ComponentEventListener<FileRemovedEvent> listener) {
        return ComponentUtil.addListener(connector, FileRemovedEvent.class,
                (ComponentEventListener) listener);
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Registration addFileRejectedListener(
            ComponentEventListener<FileRejectedEvent> listener) {
        return ComponentUtil.addListener(connector, FileRejectedEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Add a listener that is informed when all uploads have finished.
     *
     * @param listener
     *            all finished listener to add
     * @return a {@link Registration} for removing the event listener
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Registration addAllFinishedListener(
            ComponentEventListener<AllFinishedEvent> listener) {
        return ComponentUtil.addListener(connector, AllFinishedEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Internal connector component that loads the JS module and handles
     * client-server communication. Added as a virtual child of the owner
     * component so it doesn't appear in the DOM.
     */
    @Tag("vaadin-upload-manager-connector")
    @JsModule("./vaadin-upload-manager-connector.ts")
    static class Connector extends Component {
    }

    /**
     * An internal implementation of the UploadHandler interface that reminds
     * the developer that UploadHandler must be set. Upload event listeners are
     * not registered for this handler.
     */
    private static final class FailFastUploadHandler implements UploadHandler {
        @Override
        public void handleUploadRequest(UploadEvent event) {
            throw new IllegalStateException(
                    "Upload cannot be performed without an upload handler set. "
                            + "Please first set the upload handler with setUploadHandler()");
        }
    }

    /**
     * Event fired when a file is removed from the upload manager.
     */
    @DomEvent("upload-manager-file-remove")
    public static class FileRemovedEvent extends ComponentEvent<Component> {
        private final String fileName;

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            whether the event originated from the client
         * @param fileName
         *            the name of the removed file
         */
        public FileRemovedEvent(Component source, boolean fromClient,
                @EventData("event.detail.fileName") String fileName) {
            super(source, fromClient);
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
     * Event fired when a file is rejected by the upload manager due to
     * constraints like max file size, max files, or accepted file types.
     */
    @DomEvent("upload-manager-file-reject")
    public static class FileRejectedEvent extends ComponentEvent<Component> {
        private final String fileName;
        private final String errorMessage;

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         * @param fromClient
         *            whether the event originated from the client
         * @param errorMessage
         *            the error message
         * @param fileName
         *            the name of the rejected file
         */
        public FileRejectedEvent(Component source, boolean fromClient,
                @EventData("event.detail.errorMessage") String errorMessage,
                @EventData("event.detail.fileName") String fileName) {
            super(source, fromClient);
            this.errorMessage = errorMessage;
            this.fileName = fileName;
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
         * Gets the error message describing why the file was rejected.
         *
         * @return the error message
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Event fired when all uploads have finished (either successfully, failed,
     * or aborted).
     */
    public static class AllFinishedEvent extends ComponentEvent<Component> {

        /**
         * Creates a new event.
         *
         * @param source
         *            the source component
         */
        public AllFinishedEvent(Component source) {
            super(source, false);
        }
    }
}
