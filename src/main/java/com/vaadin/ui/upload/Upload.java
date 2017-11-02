/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.ui.upload;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import com.vaadin.flow.dom.Element;
import com.vaadin.server.NoInputStreamException;
import com.vaadin.server.NoOutputStreamException;
import com.vaadin.server.StreamReceiver;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.Registration;
import com.vaadin.ui.common.HasSize;
import com.vaadin.ui.event.ComponentEventListener;
import com.vaadin.ui.upload.events.FailedEvent;
import com.vaadin.ui.upload.events.FinishedEvent;
import com.vaadin.ui.upload.events.NoInputStreamEvent;
import com.vaadin.ui.upload.events.NoOutputStreamEvent;
import com.vaadin.ui.upload.events.ProgressUpdateEvent;
import com.vaadin.ui.upload.events.StartedEvent;
import com.vaadin.ui.upload.events.SucceededEvent;
import com.vaadin.util.JsonSerializer;

import elemental.json.JsonArray;
import elemental.json.JsonNull;
import elemental.json.JsonObject;

/**
 * Server-side component for the {@code vaadin-upload} element.
 *
 * @author Vaadin Ltd
 */
public class Upload extends GeneratedVaadinUpload<Upload> implements HasSize {

    private StreamVariable streamVariable;
    private boolean interrupted = false;

    private int activeUploads = 0;

    private static final String I18N_PROPERTY = "i18n";

    /**
     * The output of the upload is redirected to this receiver.
     */
    private Receiver receiver;

    /**
     * Creates a new instance of Upload.
     * <p>
     * The receiver must be set before performing an upload.
     */
    public Upload() {
        // Get a server round trip for upload error and success.
        addUploadErrorListener(event -> {
        });
        addUploadSuccessListener(event -> {
        });

        // If client aborts upload mark upload as interrupted on server also
        addUploadAbortListener(event -> interruptUpload());

        getElement().setAttribute("target",
                new StreamReceiver(getElement().getNode(), "upload",
                        getStreamVariable()));
    }

    /**
     * Create a new instance of Upload with the given receiver.
     *
     * @param receiver
     *         receiver that handles the upload
     */
    public Upload(Receiver receiver) {
        this();

        setReceiver(receiver);
    }

    private StreamVariable getStreamVariable() {
        if (streamVariable == null) {
            streamVariable = new DefaultStreamVariable(this);
        }
        return streamVariable;
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
     * Interrupts the upload currently being received. The interruption will be
     * done by the receiving thread so this method will return immediately and
     * the actual interrupt will happen a bit later.
     * <p>
     * Note! this will interrupt all uploads in multi upload mode.
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

    private void fireStarted(String filename, String MIMEType,
            long contentLength) {
        fireEvent(new StartedEvent(this, filename, MIMEType, contentLength));
    }

    private void fireUploadInterrupted(String filename, String MIMEType,
            long length) {
        fireEvent(new FailedEvent(this, filename, MIMEType, length));
    }

    private void fireNoInputStream(String filename, String MIMEType,
            long length) {
        fireEvent(new NoInputStreamEvent(this, filename, MIMEType, length));
    }

    private void fireNoOutputStream(String filename, String MIMEType,
            long length) {
        fireEvent(new NoOutputStreamEvent(this, filename, MIMEType, length));
    }

    private void fireUploadInterrupted(String filename, String MIMEType,
            long length, Exception e) {
        fireEvent(new FailedEvent(this, filename, MIMEType, length, e));
    }

    private void fireUploadSuccess(String filename, String MIMEType,
            long length) {
        fireEvent(new SucceededEvent(this, filename, MIMEType, length));
    }

    /**
     * Emits the progress event.
     *
     * @param totalBytes
     *         bytes received so far
     * @param contentLength
     *         actual size of the file being uploaded, if known
     */
    protected void fireUpdateProgress(long totalBytes, long contentLength) {
        fireEvent(new ProgressUpdateEvent(this, totalBytes, contentLength));
    }

    /**
     * Add a progress listener that is informed on upload progress.
     *
     * @param listener
     *         progress listener to add
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
     *         failed listener to add
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
     *         finished listener to add
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
     *         start listener to add
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
     *         succeeded listener to add
     * @return registration for removal of listener
     */
    public Registration addSucceededListener(
            ComponentEventListener<SucceededEvent> listener) {
        return addListener(SucceededEvent.class, listener);
    }

    /**
     * Returns the current receiver.
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
     *         receiver to use for file reception
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
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *         the internationalized properties, not <code>null</code>
     * @return this instance for method chaining
     */
    public Upload setI18n(UploadI18N i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        JsonObject json = (JsonObject) JsonSerializer.toJson(i18n);
        Element element = getElement();
        for (String key : json.keys()) {
            element.setPropertyJson(I18N_PROPERTY + "." + key, json.get(key));
        }
        return get();
    }

    /**
     * Gets the internationalization properties previously set for this
     * component.
     *
     * @return the object with the i18n properties, never <code>null</code>. If
     * the i18n properties weren't set, the internal properties of the
     * object will be <code>null</code>, but not the object itself.
     */
    public UploadI18N getI18n() {
        UploadI18N i18n = new UploadI18N();
        Element element = getElement();

        UploadI18N.DropFiles dropFiles = new UploadI18N.DropFiles();
        dropFiles.setOne(getStringObject(I18N_PROPERTY + ".dropFiles", "one"));
        dropFiles
                .setMany(getStringObject(I18N_PROPERTY + ".dropFiles", "many"));
        i18n.setDropFiles(dropFiles);

        UploadI18N.AddFiles addFiles = new UploadI18N.AddFiles();
        addFiles.setOne(getStringObject(I18N_PROPERTY + ".addFiles", "one"));
        addFiles.setMany(getStringObject(I18N_PROPERTY + ".addFiles", "many"));
        i18n.setAddFiles(addFiles);

        i18n.setCancel(element.getProperty(I18N_PROPERTY + ".cancel"));

        UploadI18N.Error error = new UploadI18N.Error();
        error.setTooManyFiles(
                getStringObject(I18N_PROPERTY + ".error", "tooManyFiles"));
        error.setFileIsTooBig(
                getStringObject(I18N_PROPERTY + ".error", "fileIsTooBig"));
        error.setIncorrectFileType(
                getStringObject(I18N_PROPERTY + ".error", "incorrectFileType"));
        i18n.setError(error);

        UploadI18N.Uploading uploading = new UploadI18N.Uploading();
        String uploadingBase = I18N_PROPERTY + ".uploading";
        UploadI18N.Uploading.Status status = new UploadI18N.Uploading.Status();
        status.setConnecting(
                getStringObject(uploadingBase, "status", "connecting"));
        status.setStalled(getStringObject(uploadingBase, "status", "stalled"));
        status.setProcessing(
                getStringObject(uploadingBase, "status", "processing"));
        status.setHeld(getStringObject(uploadingBase, "status", "held"));

        UploadI18N.Uploading.RemainingTime remainingTime = new UploadI18N.Uploading.RemainingTime();
        remainingTime.setPrefix(
                getStringObject(uploadingBase, "remainingTime", "prefix"));
        remainingTime.setUnknown(
                getStringObject(uploadingBase, "remainingTime", "unknown"));
        UploadI18N.Uploading.Error uploadingError = new UploadI18N.Uploading.Error();
        uploadingError.setServerUnavailable(
                getStringObject(uploadingBase, "error", "serverUnavailable"));
        uploadingError.setUnexpectedServerError(
                getStringObject(uploadingBase, "error",
                        "unexpectedServerError"));
        uploadingError.setForbidden(
                getStringObject(uploadingBase, "error", "forbidden"));

        uploading.setStatus(status).setRemainingTime(remainingTime)
                .setError(uploadingError);
        i18n.setUploading(uploading);

        i18n.setUnits(JsonSerializer.toObjects(String.class,
                (JsonArray) element.getPropertyRaw(I18N_PROPERTY + ".units")));

        return i18n;
    }

    private String getStringObject(String propertyName, String subName) {
        String result = null;
        JsonObject json = (JsonObject) getElement()
                .getPropertyRaw(propertyName);
        if (json != null && json.hasKey(subName) && !(json
                .get(subName) instanceof JsonNull)) {
            result = json.getString(subName);
        }
        return result;
    }

    private String getStringObject(String propertyName, String object,
            String subName) {
        String result = null;
        JsonObject json = (JsonObject) getElement()
                .getPropertyRaw(propertyName);
        if (json != null && json.hasKey(object) && !(json
                .get(object) instanceof JsonNull)) {
            json = json.getObject(object);
            if (json != null && json.hasKey(subName) && !(json
                    .get(subName) instanceof JsonNull)) {
                result = json.getString(subName);
            }
        }
        return result;
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
                        "Upload cannot be performed without a receiver set");
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
            }
        }
    }
}
