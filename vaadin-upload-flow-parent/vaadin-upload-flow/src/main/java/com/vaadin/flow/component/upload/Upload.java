/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.server.NoInputStreamException;
import com.vaadin.flow.server.NoOutputStreamException;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamVariable;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonNull;
import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * Upload is a component for uploading one or more files. It shows the upload
 * progression and status of each file. Files can be uploaded using the Upload
 * button or via drag and drop.
 *
 * @author Vaadin Ltd.
 */
public class Upload extends GeneratedVaadinUpload<Upload> implements HasSize {

    private StreamVariable streamVariable;
    private boolean interrupted = false;

    private int activeUploads = 0;
    private boolean uploading;

    private UploadI18N i18n;

    /**
     * The output of the upload is redirected to this receiver.
     */
    private Receiver receiver;

    /**
     * Create a new instance of Upload.
     * <p>
     * The receiver must be set before performing an upload.
     */
    public Upload() {
        // Get a server round trip for upload error and success.
        addUploadErrorListener(event -> {
        });

        addUploadSuccessListener(event -> {
        });

        addFileRejectListener(event -> fireEvent(
                new FileRejectedEvent(this, event.getDetailError())));

        // If client aborts upload mark upload as interrupted on server also
        addUploadAbortListener(event -> interruptUpload());

        getElement().setAttribute("target", new StreamReceiver(
                getElement().getNode(), "upload", getStreamVariable()));

        final String elementFiles = "element.files";
        DomEventListener allFinishedListener = e -> {
            JsonArray files = e.getEventData().getArray(elementFiles);

            boolean isUploading = IntStream.range(0, files.length())
                    .anyMatch(index -> {
                        final String KEY = "uploading";
                        JsonObject object = files.getObject(index);
                        return object.hasKey(KEY) && object.getBoolean(KEY);
                    });

            if (this.uploading && !isUploading) {
                this.fireAllFinish();
            }
            this.uploading = isUploading;
        };

        addUploadStartListener(e -> this.uploading = true);

        getElement().addEventListener("upload-success", allFinishedListener)
                .addEventData(elementFiles);
        getElement().addEventListener("upload-error", allFinishedListener)
                .addEventData(elementFiles);
        getElement().addEventListener("upload-abort", allFinishedListener)
                .addEventData(elementFiles);
    }

    /**
     * Create a new instance of Upload with the given receiver.
     *
     * @param receiver
     *            receiver that handles the upload
     */
    public Upload(Receiver receiver) {
        this();

        setReceiver(receiver);
    }

    /**
     * Add listener that is informed on all uploads finished.
     *
     * @param listener
     *            all finished listener to add
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addAllFinishedListener(
            ComponentEventListener<AllFinishedEvent> listener) {
        return addListener(AllFinishedEvent.class, listener);
    }

    private StreamVariable getStreamVariable() {
        if (streamVariable == null) {
            streamVariable = new DefaultStreamVariable(this);
        }
        return streamVariable;
    }

    /**
     * Limit of files to upload, by default it is unlimited. If the value is set
     * to one, the native file browser will prevent selecting multiple files.
     *
     * @param maxFiles
     *            the maximum number of files allowed for the user to select
     */
    public void setMaxFiles(int maxFiles) {
        super.setMaxFiles(maxFiles);
    }

    /**
     * Get the maximum number of files allowed for the user to select to upload.
     *
     * @return the maximum number of files
     */
    public int getMaxFiles() {
        return (int) getMaxFilesDouble();
    }

    /**
     * Specify the maximum file size in bytes allowed to upload. Notice that it
     * is a client-side constraint, which will be checked before sending the
     * request.
     *
     * @param maxFileSize
     *            the maximum file size in bytes
     */
    public void setMaxFileSize(int maxFileSize) {
        super.setMaxFileSize(maxFileSize);
    }

    /**
     * Get the maximum allowed file size in the client-side, in bytes.
     *
     * @return the maximum file size in bytes
     */
    public int getMaxFileSize() {
        return (int) getMaxFileSizeDouble();
    }

    /**
     * When <code>false</code>, it prevents uploads from triggering immediately
     * upon adding file(s). The default is <code>true</code>.
     *
     * @param autoUpload
     *            <code>true</code> to allow uploads to start immediately after
     *            selecting files, <code>false</code> otherwise.
     */
    public void setAutoUpload(boolean autoUpload) {
        setNoAuto(!autoUpload);
    }

    /**
     * Get the auto upload status.
     *
     * @return <code>true</code> if the upload of files should start immediately
     *         after they are selected, <code>false</code> otherwise.
     */
    public boolean isAutoUpload() {
        return isNoAutoBoolean();
    }

    /**
     * Define whether the element supports dropping files on it for uploading.
     * By default it's enabled in desktop and disabled in touch devices because
     * mobile devices do not support drag events in general. Setting it
     * <code>true</code> means that drop is enabled even in touch-devices, and
     * <code>false</code> disables drop in all devices.
     *
     * @param dropAllowed
     *            <code>true</code> to allow file dropping, <code>false</code>
     *            otherwise
     */
    public void setDropAllowed(boolean dropAllowed) {
        setNodrop(!dropAllowed);
    }

    /**
     * Get whether file dropping is allowed or not. By default it's enabled in
     * desktop and disabled in touch devices because mobile devices do not
     * support drag events in general.
     *
     * @return <code>true</code> if file dropping is allowed, <code>false</code>
     *         otherwise.
     */
    public boolean isDropAllowed() {
        return !isNodropBoolean();
    }

    /**
     * Specify the types of files that the server accepts. Syntax: a MIME type
     * pattern (wildcards are allowed) or file extensions. Notice that MIME
     * types are widely supported, while file extensions are only implemented in
     * certain browsers, so it should be avoided.
     * <p>
     * Example: <code>"video/*","image/tiff"</code> or
     * <code>".pdf","audio/mp3"</code>
     *
     * @param acceptedFileTypes
     *            the allowed file types to be uploaded, or <code>null</code> to
     *            clear any restrictions
     */
    public void setAcceptedFileTypes(String... acceptedFileTypes) {
        String accepted = "";
        if (acceptedFileTypes != null) {
            accepted = String.join(",", acceptedFileTypes);
        }
        setAccept(accepted);
    }

    /**
     * Get the list of accepted file types for upload.
     *
     * @return a list of allowed file types, never <code>null</code>.
     */
    public List<String> getAcceptedFileTypes() {
        String accepted = getAcceptString();
        if (accepted == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(accepted.split(","));
    }

    /**
     * Set the component as the actionable button inside the upload component,
     * that opens the dialog for choosing the files to be upload.
     *
     * @param uploadButton
     *            the component to be clicked by the user to open the dialog, or
     *            <code>null</code> to reset to the default button
     */
    public void setUploadButton(Component uploadButton) {
        removeElementsAtSlot("add-button");
        if (uploadButton != null) {
            addToAddButton(uploadButton);
        }
    }

    /**
     * Get the component set as the upload button for the upload, if any.
     *
     * @return the actionable button, or <code>null</code> if none was set
     */
    public Component getUploadButton() {
        return getComponentAtSlot("add-button");
    }

    /**
     * Set the component to show as a message to the user to drop files in the
     * upload component. Despite of the name, the label can be any component.
     *
     * @param dropLabel
     *            the label to show for the users when it's possible drop files,
     *            or <code>null</code> to clear it
     */
    public void setDropLabel(Component dropLabel) {
        removeElementsAtSlot("drop-label");
        if (dropLabel != null) {
            addToDropLabel(dropLabel);
        }
    }

    /**
     * Get the component set as the drop label, if any.
     *
     * @return the drop label component, or <code>null</code> if none was set
     */
    public Component getDropLabel() {
        return getComponentAtSlot("drop-label");
    }

    /**
     * Set the component to show as the drop label icon. The icon is visible
     * when the user can drop files to this upload component. Despite of the
     * name, the drop label icon can be any component.
     *
     * @param dropLabelIcon
     *            the label icon to show for the users when it's possible to
     *            drop files, or <code>null</code> to cleat it
     */
    public void setDropLabelIcon(Component dropLabelIcon) {
        removeElementsAtSlot("drop-label-icon");
        if (dropLabelIcon != null) {
            addToDropLabelIcon(dropLabelIcon);
        }
    }

    /**
     * Get the component set as the drop label icon, if any.
     *
     * @return the drop label icon component, or <code>null</code> if none was
     *         set
     */
    public Component getDropLabelIcon() {
        return getComponentAtSlot("drop-label-icon");
    }

    private void removeElementsAtSlot(String slot) {
        getElement().getChildren()
                .filter(child -> slot.equals(child.getAttribute("slot")))
                .collect(Collectors.toList())
                .forEach(Element::removeFromParent);
    }

    private Component getComponentAtSlot(String slot) {
        return getElement().getChildren()
                .filter(child -> slot.equals(child.getAttribute("slot")))
                .filter(child -> child.getComponent().isPresent())
                .map(child -> child.getComponent().get()).findFirst()
                .orElse(null);
    }

    /**
     * Go into upload state. This is to prevent uploading more files than
     * accepted on same component.
     */
    private void startUpload() {
        if (getMaxFiles() != 0 && getMaxFiles() <= activeUploads) {
            throw new IllegalStateException(
                    "Maximum supported amount of uploads already started");
        }
        activeUploads++;
    }

    /**
     * Interrupt the upload currently being received.
     * <p>
     * The interruption will be done by the receiving thread so this method will
     * return immediately and the actual interrupt will happen a bit later.
     * <p>
     * Note! this will interrupt all uploads in multi-upload mode.
     */
    public void interruptUpload() {
        if (isUploading()) {
            interrupted = true;
        }
    }

    private void endUpload() {
        activeUploads--;
        interrupted = false;
    }

    /**
     * Is upload in progress.
     *
     * @return true if receiving upload
     */
    public boolean isUploading() {
        return activeUploads > 0;
    }

    private void fireStarted(String filename, String mimeType,
            long contentLength) {
        fireEvent(new StartedEvent(this, filename, mimeType, contentLength));
    }

    private void fireUploadInterrupted(String filename, String mimeType,
            long length) {
        fireEvent(new FailedEvent(this, filename, mimeType, length));
    }

    private void fireNoInputStream(String filename, String mimeType,
            long length) {
        fireEvent(new NoInputStreamEvent(this, filename, mimeType, length));
    }

    private void fireNoOutputStream(String filename, String mimeType,
            long length) {
        fireEvent(new NoOutputStreamEvent(this, filename, mimeType, length));
    }

    private void fireUploadInterrupted(String filename, String mimeType,
            long length, Exception e) {
        fireEvent(new FailedEvent(this, filename, mimeType, length, e));
    }

    private void fireUploadSuccess(String filename, String mimeType,
            long length) {
        fireEvent(new SucceededEvent(this, filename, mimeType, length));
    }

    private void fireUploadFinish(String filename, String mimeType,
            long length) {
        fireEvent(new FinishedEvent(this, filename, mimeType, length));
    }

    private void fireAllFinish() {
        fireEvent(new AllFinishedEvent(this));
    }

    /**
     * Emit the progress event.
     *
     * @param totalBytes
     *            bytes received so far
     * @param contentLength
     *            actual size of the file being uploaded, if known
     */
    protected void fireUpdateProgress(long totalBytes, long contentLength) {
        fireEvent(new ProgressUpdateEvent(this, totalBytes, contentLength));
    }

    /**
     * Add a progress listener that is informed on upload progress.
     *
     * @param listener
     *            progress listener to add
     * @return registration for removal of listener
     */
    public Registration addProgressListener(
            ComponentEventListener<ProgressUpdateEvent> listener) {
        return addListener(ProgressUpdateEvent.class, listener);
    }

    /**
     * Add a succeeded listener that is informed on upload failure.
     *
     * @param listener
     *            failed listener to add
     * @return registration for removal of listener
     */
    public Registration addFailedListener(
            ComponentEventListener<FailedEvent> listener) {
        return addListener(FailedEvent.class, listener);
    }

    /**
     * Add a succeeded listener that is informed on upload finished.
     *
     * @param listener
     *            finished listener to add
     * @return registration for removal of listener
     */
    public Registration addFinishedListener(
            ComponentEventListener<FinishedEvent> listener) {
        return addListener(FinishedEvent.class, listener);
    }

    /**
     * Add a succeeded listener that is informed on upload start.
     *
     * @param listener
     *            start listener to add
     * @return registration for removal of listener
     */
    public Registration addStartedListener(
            ComponentEventListener<StartedEvent> listener) {
        return addListener(StartedEvent.class, listener);
    }

    /**
     * Add a succeeded listener that is informed on upload succeeded.
     *
     * @param listener
     *            succeeded listener to add
     * @return registration for removal of listener
     */
    public Registration addSucceededListener(
            ComponentEventListener<SucceededEvent> listener) {
        return addListener(SucceededEvent.class, listener);
    }

    /**
     * Adds a listener for {@code file-reject} events fired when a file cannot
     * be added due to some constrains:
     * {@code setMaxFileSize, setMaxFiles, setAcceptedFileTypes}
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addFileRejectedListener(
            ComponentEventListener<FileRejectedEvent> listener) {
        return addListener(FileRejectedEvent.class, listener);
    }

    /**
     * Return the current receiver.
     *
     * @return the StreamVariable.
     */
    public Receiver getReceiver() {
        return receiver;
    }

    /**
     * Set the receiver implementation that should be used for this upload
     * component.
     * <p>
     * Note! If the receiver doesn't implement {@link MultiFileReceiver} then
     * the upload will be automatically set to only accept one file.
     *
     * @param receiver
     *            receiver to use for file reception
     */
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
        if (!(receiver instanceof MultiFileReceiver)) {
            setMaxFiles(1);
        } else {
            getElement().removeAttribute("maxFiles");
        }
    }

    /**
     * Set the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(UploadI18N i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        this.i18n = i18n;

        runBeforeClientResponse(ui -> {
            if (i18n == this.i18n) {
                setI18nWithJS();
            }
        });
    }

    private void setI18nWithJS() {
        JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(this.i18n);

        // Remove null values so that we don't overwrite existing WC
        // translations with empty ones
        deeplyRemoveNullValuesFromJsonObject(i18nJson);

        // Assign new I18N object to WC, by deeply merging the existing
        // WC I18N, and the values from the new UploadI18N instance,
        // into an empty object
        getElement().executeJs(
                "const dropFiles = Object.assign({}, this.i18n.dropFiles, $0.dropFiles);"
                        + "const addFiles = Object.assign({}, this.i18n.addFiles, $0.addFiles);"
                        + "const error = Object.assign({}, this.i18n.error, $0.error);"
                        + "const uploadingStatus = Object.assign({}, this.i18n.uploading.status, $0.uploading && $0.uploading.status);"
                        + "const uploadingRemainingTime = Object.assign({}, this.i18n.uploading.remainingTime, $0.uploading && $0.uploading.remainingTime);"
                        + "const uploadingError = Object.assign({}, this.i18n.uploading.error, $0.uploading && $0.uploading.error);"
                        + "const uploading = {status: uploadingStatus,"
                        + "  remainingTime: uploadingRemainingTime,"
                        + "  error: uploadingError};"
                        + "const units = $0.units || this.i18n.units;"
                        + "this.i18n = Object.assign({}, this.i18n, $0, {"
                        + "  addFiles: addFiles,  dropFiles: dropFiles,"
                        + "  uploading: uploading, units: units});",
                i18nJson);
    }

    private void deeplyRemoveNullValuesFromJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keys()) {
            if (jsonObject.get(key).getType() == JsonType.OBJECT) {
                deeplyRemoveNullValuesFromJsonObject(jsonObject.get(key));
            } else if (jsonObject.get(key).getType() == JsonType.NULL) {
                jsonObject.remove(key);
            }
        }
    }

    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * Get the internationalization object previously set for this component.
     * <p>
     * Note: updating the object content that is gotten from this method will
     * not update the language on the component if not set back using
     * {@link Upload#setI18n(UploadI18N)}
     *
     * @return the object with the i18n properties. If the i18n properties
     *         weren't set, the object will return <code>null</code>.
     */
    public UploadI18N getI18n() {
        return i18n;
    }

    private String getStringObject(String propertyName, String subName) {
        String result = null;
        JsonObject json = (JsonObject) getElement()
                .getPropertyRaw(propertyName);
        if (json != null && json.hasKey(subName)
                && !(json.get(subName) instanceof JsonNull)) {
            result = json.getString(subName);
        }
        return result;
    }

    private String getStringObject(String propertyName, String object,
            String subName) {
        String result = null;
        JsonObject json = (JsonObject) getElement()
                .getPropertyRaw(propertyName);
        if (json != null && json.hasKey(object)
                && !(json.get(object) instanceof JsonNull)) {
            json = json.getObject(object);
            if (json != null && json.hasKey(subName)
                    && !(json.get(subName) instanceof JsonNull)) {
                result = json.getString(subName);
            }
        }
        return result;
    }

    /**
     * Clear the list of files being processed, or already uploaded.
     */
    public void clearFileList() {
        setFiles(Json.createArray());
    }

    private static class DefaultStreamVariable implements StreamVariable {

        private Deque<StreamVariable.StreamingStartEvent> lastStartedEvent = new ArrayDeque<>();

        private final Upload upload;

        public DefaultStreamVariable(Upload upload) {
            this.upload = upload;
        }

        @Override
        public boolean listenProgress() {
            return upload.getEventBus().hasListener(ProgressUpdateEvent.class);
        }

        @Override
        public void onProgress(StreamVariable.StreamingProgressEvent event) {
            upload.fireUpdateProgress(event.getBytesReceived(),
                    event.getContentLength());
        }

        @Override
        public boolean isInterrupted() {
            return upload.interrupted;
        }

        @Override
        public OutputStream getOutputStream() {
            if (upload.getReceiver() == null) {
                throw new IllegalStateException(
                        "Upload cannot be performed without a receiver set. "
                                + "Please firstly set the receiver implementation with upload.setReceiver");
            }
            StreamVariable.StreamingStartEvent event = lastStartedEvent.pop();
            OutputStream receiveUpload = upload.getReceiver()
                    .receiveUpload(event.getFileName(), event.getMimeType());
            return receiveUpload;
        }

        @Override
        public void streamingStarted(StreamVariable.StreamingStartEvent event) {
            upload.startUpload();
            try {
                upload.fireStarted(event.getFileName(), event.getMimeType(),
                        event.getContentLength());
            } finally {
                lastStartedEvent.addLast(event);
            }
        }

        @Override
        public void streamingFinished(StreamVariable.StreamingEndEvent event) {
            try {
                upload.fireUploadSuccess(event.getFileName(),
                        event.getMimeType(), event.getContentLength());
            } finally {
                upload.endUpload();
                upload.fireUploadFinish(event.getFileName(),
                        event.getMimeType(), event.getContentLength());
            }
        }

        @Override
        public void streamingFailed(StreamVariable.StreamingErrorEvent event) {
            try {
                Exception exception = event.getException();
                if (exception instanceof NoInputStreamException) {
                    upload.fireNoInputStream(event.getFileName(),
                            event.getMimeType(), 0);
                } else if (exception instanceof NoOutputStreamException) {
                    upload.fireNoOutputStream(event.getFileName(),
                            event.getMimeType(), 0);
                } else {
                    upload.fireUploadInterrupted(event.getFileName(),
                            event.getMimeType(), event.getBytesReceived(),
                            exception);
                }
            } finally {
                upload.endUpload();
                upload.fireUploadFinish(event.getFileName(),
                        event.getMimeType(), event.getContentLength());
            }
        }
    }
}
