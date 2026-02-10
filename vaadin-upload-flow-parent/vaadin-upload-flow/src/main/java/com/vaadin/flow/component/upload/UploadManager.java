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
package com.vaadin.flow.component.upload;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.internal.streams.UploadCompleteEvent;
import com.vaadin.flow.internal.streams.UploadStartEvent;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadResult;
import com.vaadin.flow.shared.Registration;

/**
 * A non-visual manager that handles file upload state and XHR requests. It
 * creates a JavaScript {@code UploadManager} instance that UI components can
 * connect to.
 * <p>
 * Unlike the {@link Upload} component which provides a complete upload UI, this
 * manager is invisible and UI components connect to it by setting this manager
 * as their target. This allows more freedom in designing the upload experience.
 * <p>
 * For upload progress monitoring (start, progress, success, failure events),
 * use an {@link com.vaadin.flow.server.streams.UploadHandler} that implements
 * {@link com.vaadin.flow.server.streams.TransferProgressAwareHandler}.
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
 */
public class UploadManager implements Serializable {

    private final Connector connector = new Connector();
    private final Component owner;

    // Upload state tracking
    private final AtomicInteger activeUploads = new AtomicInteger(0);

    // Accepted file type restrictions (used for both client hints and
    // server-side validation)
    private List<String> acceptedMimeTypes = List.of();
    private List<String> acceptedFileExtensions = List.of();

    /**
     * Creates a new upload manager without an upload handler. The handler must
     * be set using {@link #setUploadHandler(UploadHandler)} before uploads can
     * work.
     *
     * @param owner
     *            the component that owns this manager. The manager's lifecycle
     *            is tied to the owner's lifecycle - when the owner is detached
     *            from the UI, uploads will stop working. The owner is typically
     *            the view or layout containing the upload UI components.
     */
    public UploadManager(Component owner) {
        this(owner, null);
    }

    /**
     * Creates a new upload manager with the given upload handler.
     *
     * @param owner
     *            the component that owns this manager. The manager's lifecycle
     *            is tied to the owner's lifecycle - when the owner is detached
     *            from the UI, uploads will stop working. The owner is typically
     *            the view or layout containing the upload UI components.
     * @param handler
     *            the upload handler to use
     */
    public UploadManager(Component owner, UploadHandler handler) {
        Objects.requireNonNull(owner, "Owner component cannot be null");
        this.owner = owner;

        // Add connector as virtual child of owner (doesn't appear in DOM)
        owner.getElement().appendVirtualChild(connector.getElement());

        // Set up default fail-fast handler
        setUploadHandler(
                handler != null ? handler : new FailFastUploadHandler());

        // Listen for file-remove and file-reject events from client.
        // We manually listen to DOM events and fire ComponentEvents with the
        // owner as source, so that getSource() returns the meaningful owner
        // component instead of the internal connector.
        final String eventDetailFileName = "event.detail.fileName";
        final String eventDetailErrorMessage = "event.detail.errorMessage";

        connector.getElement().addEventListener("file-remove", event -> {
            String fileName = event.getEventData().get(eventDetailFileName)
                    .asString();
            ComponentUtil.fireEvent(owner,
                    new FileRemovedEvent(owner, true, fileName));
        }).addEventData(eventDetailFileName);

        connector.getElement().addEventListener("file-reject", event -> {
            String fileName = event.getEventData().get(eventDetailFileName)
                    .asString();
            String errorMessage = event.getEventData()
                    .get(eventDetailErrorMessage).asString();
            ComponentUtil.fireEvent(owner,
                    new FileRejectedEvent(owner, true, errorMessage, fileName));
        }).addEventData(eventDetailFileName)
                .addEventData(eventDetailErrorMessage);

        // Listen for all-finished event from client (triggered when all
        // uploads are complete, including success, error, or abort)
        connector.getElement().addEventListener("all-finished",
                event -> ComponentUtil.fireEvent(owner,
                        new AllFinishedEvent(owner)));

        // Register internal listeners for upload state tracking
        ComponentUtil.addListener(connector, UploadStartEvent.class,
                event -> startUpload());
        ComponentUtil.addListener(connector, UploadCompleteEvent.class,
                event -> endUpload());
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
        // Wrap handler with file type validation
        UploadHandler validatingHandler = wrapWithFileTypeValidation(handler);
        // Wrap handler with ElementStreamResource to use custom target name
        StreamResourceRegistry.ElementStreamResource elementStreamResource = new StreamResourceRegistry.ElementStreamResource(
                validatingHandler, connector.getElement()) {
            @Override
            public String getName() {
                return targetName;
            }
        };
        connector.getElement().setAttribute("target", elementStreamResource);
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
        connector.getElement().setProperty("maxFiles",
                maxFiles == 0 ? Double.POSITIVE_INFINITY : (double) maxFiles);
    }

    /**
     * Get the maximum number of files allowed for the user to select to upload.
     *
     * @return the maximum number of files, or 0 if unlimited
     */
    public int getMaxFiles() {
        double value = connector.getElement().getProperty("maxFiles", 0.0);
        return Double.isInfinite(value) ? 0 : (int) value;
    }

    /**
     * Specify the maximum file size in bytes allowed to upload. Notice that it
     * is a client-side constraint, which will be checked before sending the
     * request.
     *
     * @param maxFileSize
     *            the maximum file size in bytes, or 0 for unlimited
     */
    public void setMaxFileSize(long maxFileSize) {
        connector.getElement().setProperty("maxFileSize",
                maxFileSize == 0 ? Double.POSITIVE_INFINITY
                        : (double) maxFileSize);
    }

    /**
     * Get the maximum allowed file size in the client-side, in bytes.
     *
     * @return the maximum file size in bytes, or 0 if unlimited
     */
    public long getMaxFileSize() {
        double value = connector.getElement().getProperty("maxFileSize", 0.0);
        return Double.isInfinite(value) ? 0 : (long) value;
    }

    /**
     * Sets the accepted MIME types for uploads. Only files matching these MIME
     * types will be accepted. Wildcard patterns like {@code "image/*"} are
     * supported.
     * <p>
     * MIME types are used both as a client-side hint (to filter the file
     * picker) and for server-side validation (to reject uploads that don't
     * match).
     * <p>
     * If both MIME types and file extensions are configured, a file must match
     * at least one of each (AND logic).
     *
     * @param mimeTypes
     *            the accepted MIME types, e.g. {@code "image/*"},
     *            {@code "application/pdf"}; or {@code null} to clear
     * @throws IllegalArgumentException
     *             if any value is null, blank, or does not contain a {@code /}
     *             character
     */
    public void setAcceptedMimeTypes(String... mimeTypes) {
        if (mimeTypes == null || mimeTypes.length == 0) {
            acceptedMimeTypes = List.of();
        } else {
            for (String mimeType : mimeTypes) {
                if (mimeType == null || mimeType.isBlank()) {
                    throw new IllegalArgumentException(
                            "MIME types cannot contain null or blank values");
                }
                if (!mimeType.contains("/")) {
                    throw new IllegalArgumentException(
                            "MIME type must contain a '/' character: "
                                    + mimeType);
                }
            }
            acceptedMimeTypes = List.of(mimeTypes);
        }
        updateAcceptProperty();
    }

    /**
     * Gets the list of accepted MIME types for upload.
     *
     * @return a list of accepted MIME types, never {@code null}
     */
    public List<String> getAcceptedMimeTypes() {
        return acceptedMimeTypes;
    }

    /**
     * Sets the accepted file extensions for uploads. Only files with matching
     * extensions will be accepted. Extensions must start with a dot, e.g.
     * {@code ".pdf"}, {@code ".txt"}.
     * <p>
     * File extensions are used both as a client-side hint and for server-side
     * validation.
     * <p>
     * If both MIME types and file extensions are configured, a file must match
     * at least one of each (AND logic).
     *
     * @param extensions
     *            the accepted file extensions, each starting with a dot; or
     *            {@code null} to clear
     * @throws IllegalArgumentException
     *             if any value is null, blank, or does not start with a dot
     */
    public void setAcceptedFileExtensions(String... extensions) {
        if (extensions == null || extensions.length == 0) {
            acceptedFileExtensions = List.of();
        } else {
            for (String ext : extensions) {
                if (ext == null || ext.isBlank()) {
                    throw new IllegalArgumentException(
                            "File extensions cannot contain null or blank values");
                }
                if (!ext.startsWith(".")) {
                    throw new IllegalArgumentException(
                            "File extension must start with '.': " + ext);
                }
            }
            acceptedFileExtensions = List.of(extensions);
        }
        updateAcceptProperty();
    }

    /**
     * Gets the list of accepted file extensions for upload.
     *
     * @return a list of accepted file extensions, never {@code null}
     */
    public List<String> getAcceptedFileExtensions() {
        return acceptedFileExtensions;
    }

    /**
     * Wraps the given upload handler with file type validation. The wrapper
     * reads the current {@link #acceptedMimeTypes} and
     * {@link #acceptedFileExtensions} at the time of each upload request, so
     * changes made after {@link #setUploadHandler} are picked up.
     */
    private UploadHandler wrapWithFileTypeValidation(UploadHandler delegate) {
        return new UploadHandler() {
            @Override
            public void handleUploadRequest(UploadEvent event)
                    throws IOException {
                List<String> mimeTypes = acceptedMimeTypes;
                List<String> extensions = acceptedFileExtensions;
                if ((!mimeTypes.isEmpty() || !extensions.isEmpty())
                        && !isFileTypeAccepted(event.getFileName(),
                                event.getContentType(), mimeTypes,
                                extensions)) {
                    event.reject(
                            "File type not allowed: " + event.getFileName());
                    return;
                }
                delegate.handleUploadRequest(event);
            }

            @Override
            public void responseHandled(UploadResult result) {
                delegate.responseHandled(result);
            }

            @Override
            public long getRequestSizeMax() {
                return delegate.getRequestSizeMax();
            }

            @Override
            public long getFileSizeMax() {
                return delegate.getFileSizeMax();
            }

            @Override
            public long getFileCountMax() {
                return delegate.getFileCountMax();
            }
        };
    }

    /**
     * Checks whether a file is accepted based on the configured MIME types and
     * file extensions. Each configured source acts as an independent gate: if
     * MIME types are configured, the file's content type must match at least
     * one; if extensions are configured, the file name must match at least one.
     * When both are configured, both checks must pass (AND logic).
     */
    private static boolean isFileTypeAccepted(String fileName,
            String contentType, List<String> mimeTypes,
            List<String> extensions) {
        if (!mimeTypes.isEmpty()
                && !matchesAnyMimeType(contentType, mimeTypes)) {
            return false;
        }
        return extensions.isEmpty()
                || matchesAnyExtension(fileName, extensions);
    }

    private static boolean matchesAnyMimeType(String contentType,
            List<String> mimeTypes) {
        for (String pattern : mimeTypes) {
            if (matchesMimeType(contentType, pattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAnyExtension(String fileName,
            List<String> extensions) {
        if (fileName == null) {
            return false;
        }
        String lowerFileName = fileName.toLowerCase(Locale.ENGLISH);
        for (String ext : extensions) {
            if (lowerFileName.endsWith(ext.toLowerCase(Locale.ENGLISH))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether an actual MIME type matches a pattern. Supports exact
     * match and wildcard patterns like {@code "image/*"}.
     */
    private static boolean matchesMimeType(String actual, String pattern) {
        if (actual == null || pattern == null) {
            return false;
        }
        if (actual.equalsIgnoreCase(pattern)) {
            return true;
        }
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return actual.toLowerCase(Locale.ENGLISH)
                    .startsWith(prefix.toLowerCase(Locale.ENGLISH));
        }
        return false;
    }

    /**
     * Derives and sets the client-side {@code accept} property from the
     * configured MIME types and file extensions.
     */
    private void updateAcceptProperty() {
        List<String> combined = new ArrayList<>();
        combined.addAll(acceptedMimeTypes);
        combined.addAll(acceptedFileExtensions);
        connector.getElement().setProperty("accept",
                String.join(",", combined));
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
        connector.getElement().setProperty("noAuto", !autoUpload);
    }

    /**
     * Get the auto upload status.
     *
     * @return {@code true} if the upload of files should start immediately
     *         after they are selected, {@code false} otherwise
     */
    public boolean isAutoUpload() {
        return !connector.getElement().getProperty("noAuto", false);
    }

    /**
     * Sets whether the upload manager is enabled. When disabled, uploads cannot
     * be started from any linked UI components (buttons, drop zones).
     * <p>
     * This is the authoritative server-side control for preventing uploads.
     * Disabling individual UI components only affects the UI but does not
     * prevent a malicious client from initiating uploads. Use this method to
     * securely prevent uploads.
     *
     * @param enabled
     *            {@code true} to enable uploads, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
        connector.getElement().setEnabled(enabled);
    }

    /**
     * Gets whether the upload manager is enabled.
     *
     * @return {@code true} if uploads are enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return connector.getElement().isEnabled();
    }

    /**
     * Checks if an upload is currently in progress.
     *
     * @return {@code true} if receiving upload, {@code false} otherwise
     */
    public boolean isUploading() {
        return activeUploads.get() > 0;
    }

    /**
     * Clear the list of files being processed, or already uploaded.
     */
    public void clearFileList() {
        connector.getElement().callJsFunction("clearFileList");
    }

    /**
     * Go into upload state. This is to prevent uploading more files than
     * accepted on same component.
     *
     * @throws IllegalStateException
     *             if maximum supported amount of uploads already started
     */
    private void startUpload() {
        int maxFiles = getMaxFiles();
        int current = activeUploads.get();
        if (maxFiles != 0 && maxFiles <= current) {
            throw new IllegalStateException(
                    "Maximum supported amount of uploads already started");
        }
        activeUploads.incrementAndGet();
    }

    /**
     * End upload state.
     */
    private void endUpload() {
        activeUploads.decrementAndGet();
    }

    /**
     * Adds a listener for events fired when a file is removed.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addFileRemovedListener(
            ComponentEventListener<FileRemovedEvent> listener) {
        return ComponentUtil.addListener(owner, FileRemovedEvent.class,
                listener);
    }

    /**
     * Adds a listener for {@code file-reject} events fired when a file cannot
     * be added due to some constraints:
     * {@code setMaxFileSize, setMaxFiles, setAcceptedMimeTypes, setAcceptedFileExtensions}
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addFileRejectedListener(
            ComponentEventListener<FileRejectedEvent> listener) {
        return ComponentUtil.addListener(owner, FileRejectedEvent.class,
                listener);
    }

    /**
     * Add a listener that is informed when all uploads have finished.
     *
     * @param listener
     *            all finished listener to add
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addAllFinishedListener(
            ComponentEventListener<AllFinishedEvent> listener) {
        return ComponentUtil.addListener(owner, AllFinishedEvent.class,
                listener);
    }

    /**
     * The feature flag ID for modular upload components (UploadManager and
     * related components).
     */
    public static final String FEATURE_FLAG_ID = ModularUploadFeatureFlagProvider.FEATURE_FLAG_ID;

    /**
     * Internal connector component that loads the JS module and handles
     * client-server communication. Added as a virtual child of the owner
     * component so it doesn't appear in the DOM.
     */
    @Tag("vaadin-upload-manager-connector")
    @JsModule("./vaadin-upload-manager-connector.ts")
    @NpmPackage(value = "@vaadin/upload", version = "25.1.0-alpha6")
    static class Connector extends Component {
        @Override
        protected void onAttach(AttachEvent attachEvent) {
            super.onAttach(attachEvent);
            checkFeatureFlag(attachEvent.getUI());
        }

        private void checkFeatureFlag(com.vaadin.flow.component.UI ui) {
            FeatureFlags featureFlags = FeatureFlags
                    .get(ui.getSession().getService().getContext());

            // Check if either the specific modularUpload flag or the umbrella
            // aiComponents flag is enabled
            boolean enabled = featureFlags.isEnabled(FEATURE_FLAG_ID)
                    || featureFlags.isEnabled("aiComponents");

            if (!enabled) {
                throw new ModularUploadExperimentalFeatureException();
            }
        }
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
     * Event fired when a file is removed from the upload manager. The event
     * source is the owner component passed to the {@link UploadManager}
     * constructor.
     */
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
                String fileName) {
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
     * constraints like max file size, max files, or accepted file types. The
     * event source is the owner component passed to the {@link UploadManager}
     * constructor.
     */
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
                String errorMessage, String fileName) {
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
     * or aborted). The event source is the owner component passed to the
     * {@link UploadManager} constructor.
     */
    public static class AllFinishedEvent extends ComponentEvent<Component> {

        /**
         * Creates a new event.
         *
         * @param source
         *            the owner component of the upload manager
         */
        public AllFinishedEvent(Component source) {
            super(source, false);
        }
    }
}
