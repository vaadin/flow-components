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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.shared.Registration;

/**
 * Utility methods for Upload and UploadManager components.
 * <p>
 * Intended only for internal use and can be removed or changed in the future.
 * 
 * @since 25.1
 */
public class UploadHelper implements Serializable {

    private static final String UPLOAD_FILES_UPLOADING = "element.files.some(file => file.uploading)";
    private static final String MANAGER_FILES_UPLOADING = "element.manager.files.some(file => file.uploading)";

    /**
     * Checks whether the given {@link UploadManager} has an explicitly
     * configured {@link UploadHandler UploadHandler}.
     *
     * @param uploadManager
     *            the upload manager to check, not {@code null}
     * @return {@code true} if the upload manager has an explicitly configured
     *         upload handler, {@code false} otherwise
     */
    public static boolean hasUploadHandler(UploadManager uploadManager) {
        return uploadManager.isHandlerExplicitlyConfigured();
    }

    /**
     * Checks whether the given {@link Upload} has an explicitly configured
     * {@link UploadHandler UploadHandler}.
     *
     * @param upload
     *            the upload component to check, not {@code null}
     * @return {@code true} if the upload has an explicitly configured upload
     *         handler, {@code false} otherwise
     */
    public static boolean hasUploadHandler(Upload upload) {
        return upload.isHandlerExplicitlyConfigured();
    }

    /**
     * Adds a listener that is notified when the browser starts or stops
     * uploading files to the given {@link UploadManager}. The listener receives
     * {@code true} when a file starts uploading, and {@code false} when all
     * uploads have finished, when a removed or canceled file was the last one
     * uploading, or when the owner is detached. Unlike
     * {@link UploadManager#isUploading()}, the notifications arrive through
     * regular client-server round-trips, so that UI changes made by the
     * listener are sent to the browser. The listener is notified also while a
     * modal component makes the owner inert, and while the upload manager is
     * disabled, as uploads that have started go on.
     *
     * @param uploadManager
     *            the upload manager to listen to, not {@code null}
     * @param listener
     *            the listener to notify, not {@code null}
     * @return a registration for removing the listener
     */
    public static Registration addUploadingChangeListener(
            UploadManager uploadManager,
            SerializableConsumer<Boolean> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        return Registration.combine(
                addUploadStartListener(
                        uploadManager.getConnector().getElement(), listener),
                addTrackingListener(uploadManager.getConnector().getElement(),
                        "all-finished", event -> listener.accept(false)),
                addUploadEndListener(uploadManager.getConnector().getElement(),
                        "file-remove", MANAGER_FILES_UPLOADING, listener),
                uploadManager.getConnector().getElement()
                        .addDetachListener(event -> listener.accept(false)));
    }

    /**
     * Adds a listener that is notified when the browser starts or stops
     * uploading files to the given {@link Upload}. The listener receives
     * {@code true} when a file starts uploading, and {@code false} when all
     * uploads have finished, when a removed or canceled file was the last one
     * uploading, or when the upload is detached. Unlike
     * {@link Upload#isUploading()}, the notifications arrive through regular
     * client-server round-trips, so that UI changes made by the listener are
     * sent to the browser. The listener is notified also while a modal
     * component makes the upload inert, and while the upload is disabled, as
     * uploads that have started go on.
     *
     * @param upload
     *            the upload component to listen to, not {@code null}
     * @param listener
     *            the listener to notify, not {@code null}
     * @return a registration for removing the listener
     */
    public static Registration addUploadingChangeListener(Upload upload,
            SerializableConsumer<Boolean> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        return Registration.combine(
                addUploadStartListener(upload.getElement(), listener),
                addUploadEndListener(upload.getElement(), "upload-success",
                        UPLOAD_FILES_UPLOADING, listener),
                addUploadEndListener(upload.getElement(), "upload-error",
                        UPLOAD_FILES_UPLOADING, listener),
                addUploadEndListener(upload.getElement(), "upload-abort",
                        UPLOAD_FILES_UPLOADING, listener),
                addUploadEndListener(upload.getElement(), "file-remove",
                        UPLOAD_FILES_UPLOADING, listener),
                upload.addDetachListener(event -> listener.accept(false)));
    }

    private static Registration addUploadStartListener(Element element,
            SerializableConsumer<Boolean> listener) {
        return addTrackingListener(element, "upload-start",
                event -> listener.accept(true));
    }

    /**
     * Notifies that no file is uploading when the given event is dispatched
     * while no file is uploading. Canceling an upload does not produce an
     * all-finished event, as the upload-abort event is dispatched while the
     * file is still uploading. The file-remove event that follows is dispatched
     * after the file has been removed from the files.
     */
    private static Registration addUploadEndListener(Element element,
            String eventType, String filesUploading,
            SerializableConsumer<Boolean> listener) {
        return addTrackingListener(element, eventType, event -> {
            if (!event.getEventData().get(filesUploading).booleanValue()) {
                listener.accept(false);
            }
        }).addEventData(filesUploading);
    }

    /**
     * Adds a listener that follows the uploads of the browser. Uploads go on
     * while a modal component makes the element inert, and also when the
     * element is disabled during an upload, so the events are received in both
     * cases. Otherwise the end of an upload would never be noticed.
     */
    private static DomListenerRegistration addTrackingListener(Element element,
            String eventType, DomEventListener listener) {
        return element.addEventListener(eventType, listener).allowInert()
                .setDisabledUpdateMode(DisabledUpdateMode.ALWAYS);
    }

    /**
     * Validates the given MIME types and returns them as an immutable list.
     * Each value must be non-null, non-blank and contain a {@code /} character.
     *
     * @param mimeTypes
     *            the MIME types to validate, may be {@code null} or empty
     * @return an immutable list of the given MIME types, or an empty list when
     *         the input is {@code null} or empty
     * @throws IllegalArgumentException
     *             if any value is null, blank, or does not contain a {@code /}
     *             character
     */
    static List<String> validateMimeTypes(String... mimeTypes) {
        if (mimeTypes == null || mimeTypes.length == 0) {
            return List.of();
        }
        for (var mimeType : mimeTypes) {
            if (mimeType == null || mimeType.isBlank()) {
                throw new IllegalArgumentException(
                        "MIME types cannot contain null or blank values");
            }
            if (!mimeType.contains("/")) {
                throw new IllegalArgumentException(
                        "MIME type must contain a '/' character: " + mimeType);
            }
        }
        return List.of(mimeTypes);
    }

    /**
     * Validates the given file extensions and returns them as an immutable
     * list. Each value must be non-null, non-blank and start with a dot.
     *
     * @param extensions
     *            the file extensions to validate, may be {@code null} or empty
     * @return an immutable list of the given file extensions, or an empty list
     *         when the input is {@code null} or empty
     * @throws IllegalArgumentException
     *             if any value is null, blank, or does not start with a dot
     */
    static List<String> validateFileExtensions(String... extensions) {
        if (extensions == null || extensions.length == 0) {
            return List.of();
        }
        for (var ext : extensions) {
            if (ext == null || ext.isBlank()) {
                throw new IllegalArgumentException(
                        "File extensions cannot contain null or blank values");
            }
            if (!ext.startsWith(".")) {
                throw new IllegalArgumentException(
                        "File extension must start with '.': " + ext);
            }
        }
        return List.of(extensions);
    }

    /**
     * Formats the client-side {@code accept} property value from the given MIME
     * types and file extensions.
     *
     * @param mimeTypes
     *            the accepted MIME types, not {@code null}
     * @param extensions
     *            the accepted file extensions, not {@code null}
     * @return a comma-separated string of all values, empty when both lists are
     *         empty
     */
    static String formatAcceptValue(List<String> mimeTypes,
            List<String> extensions) {
        return Stream.concat(mimeTypes.stream(), extensions.stream())
                .collect(Collectors.joining(","));
    }

    /**
     * Creates the stream resource to set as the upload {@code target}
     * attribute. Validates the handler and target name, wraps the handler with
     * file type validation (see
     * {@link #wrapHandlerWithFileTypeValidation(UploadHandler, SerializableSupplier, SerializableSupplier)})
     * and uses the given target name as the last path segment of the generated
     * upload URL.
     *
     * @param handler
     *            the upload handler, not {@code null}
     * @param ownerElement
     *            the element owning the stream resource, not {@code null}
     * @param targetName
     *            the endpoint name (single path segment); must not be blank
     * @param mimeTypesSupplier
     *            supplier for the current list of accepted MIME type patterns,
     *            not {@code null}
     * @param extensionsSupplier
     *            supplier for the current list of accepted file extensions, not
     *            {@code null}
     * @return the stream resource to set as the {@code target} attribute
     */
    static StreamResourceRegistry.ElementStreamResource createTargetResource(
            UploadHandler handler, Element ownerElement, String targetName,
            SerializableSupplier<List<String>> mimeTypesSupplier,
            SerializableSupplier<List<String>> extensionsSupplier) {
        Objects.requireNonNull(handler, "UploadHandler cannot be null");
        Objects.requireNonNull(targetName, "The target name cannot be null");
        if (targetName.isBlank()) {
            throw new IllegalArgumentException(
                    "The target name cannot be blank");
        }
        var validatingHandler = wrapHandlerWithFileTypeValidation(handler,
                mimeTypesSupplier, extensionsSupplier);
        return new StreamResourceRegistry.ElementStreamResource(
                validatingHandler, ownerElement) {
            @Override
            public String getName() {
                return targetName;
            }
        };
    }

    /**
     * An internal implementation of the UploadHandler interface that reminds
     * the developer that an upload handler must be set. Upload event listeners
     * are not registered for this handler.
     */
    static final class FailFastUploadHandler implements UploadHandler {
        @Override
        public void handleUploadRequest(UploadEvent event) {
            throw new IllegalStateException(
                    "Upload cannot be performed without an upload handler set. "
                            + "Please first set the upload handler with setUploadHandler()");
        }
    }

    /**
     * Checks whether a file is accepted based on the configured MIME types and
     * file extensions. Each configured source acts as an independent gate: if
     * MIME types are configured, the file's content type must match at least
     * one; if extensions are configured, the file name must match at least one.
     * When both are configured, both checks must pass (AND logic).
     *
     * @param fileName
     *            the name of the file to check, may be {@code null}
     * @param contentType
     *            the MIME content type of the file, may be {@code null}
     * @param mimeTypes
     *            the list of accepted MIME type patterns (e.g.
     *            {@code "image/*"}, {@code "text/plain"}), not {@code null}
     * @param extensions
     *            the list of accepted file extensions including the leading dot
     *            (e.g. {@code ".txt"}, {@code ".pdf"}), not {@code null}
     * @return {@code true} if the file passes all configured checks,
     *         {@code false} otherwise
     */
    static boolean isFileTypeAccepted(String fileName, String contentType,
            List<String> mimeTypes, List<String> extensions) {
        if (!mimeTypes.isEmpty()
                && !matchesAnyMimeType(contentType, mimeTypes)) {
            return false;
        }
        return extensions.isEmpty()
                || matchesAnyExtension(fileName, extensions);
    }

    private static boolean matchesAnyMimeType(String contentType,
            List<String> mimeTypes) {
        return mimeTypes.stream()
                .anyMatch(pattern -> matchesMimeType(contentType, pattern));
    }

    private static boolean matchesAnyExtension(String fileName,
            List<String> extensions) {
        if (fileName == null) {
            return false;
        }
        var locale = Locale.ENGLISH;
        var lowerFileName = fileName.toLowerCase(locale);
        return extensions.stream().map(ext -> ext.toLowerCase(locale))
                .anyMatch(lowerFileName::endsWith);
    }

    /**
     * Checks whether an actual MIME type matches a pattern. Supports exact
     * match and wildcard patterns like {@code "image/*"}. Parameters in the
     * actual MIME type (e.g. {@code "text/html; charset=utf-8"}) are stripped
     * before comparison.
     */
    private static boolean matchesMimeType(String actual, String pattern) {
        if (actual == null || pattern == null) {
            return false;
        }
        // Strip MIME type parameters (e.g. "; charset=utf-8")
        var semicolonIndex = actual.indexOf(';');
        if (semicolonIndex >= 0) {
            actual = actual.substring(0, semicolonIndex).trim();
        }
        if (actual.equalsIgnoreCase(pattern)) {
            return true;
        }
        if (pattern.endsWith("/*")) {
            var prefix = pattern.substring(0, pattern.length() - 1);
            return actual.toLowerCase(Locale.ENGLISH)
                    .startsWith(prefix.toLowerCase(Locale.ENGLISH));
        }
        return false;
    }

    /**
     * Wraps the given upload handler with file type validation. The returned
     * handler checks each upload against the current MIME types and file
     * extensions (using AND logic when both are set) and rejects non-matching
     * files via {@link UploadEvent#reject(String)}. All other
     * {@link UploadHandler} methods except {@link UploadHandler#isAllowInert()}
     * are delegated to the original handler.
     * <p>
     * The MIME type and extension lists are retrieved at request time via the
     * provided suppliers, so changes made after wrapping are reflected
     * immediately.
     * <p>
     * Upload requests are received also when the owner is inert, so that a
     * modal component opened before or during an upload does not reject files
     * the user selected. To block uploads, disable the owner component instead.
     * <p>
     * NOTE: If new methods are added to {@link UploadHandler} or
     * {@link com.vaadin.flow.server.streams.ElementRequestHandler}, they must
     * be explicitly delegated here.
     *
     * @param delegate
     *            the original upload handler to delegate to, not {@code null}
     * @param mimeTypesSupplier
     *            supplier for the current list of accepted MIME type patterns,
     *            not {@code null}
     * @param extensionsSupplier
     *            supplier for the current list of accepted file extensions
     *            (including the leading dot), not {@code null}
     * @return a new {@link UploadHandler} that validates file types before
     *         delegating to the original handler
     */
    static UploadHandler wrapHandlerWithFileTypeValidation(
            UploadHandler delegate,
            SerializableSupplier<List<String>> mimeTypesSupplier,
            SerializableSupplier<List<String>> extensionsSupplier) {
        return new UploadHandler() {
            @Override
            public void handleUploadRequest(UploadEvent event)
                    throws IOException {
                var mimeTypes = mimeTypesSupplier.get();
                var extensions = extensionsSupplier.get();
                if ((!mimeTypes.isEmpty() || !extensions.isEmpty())
                        && !UploadHelper.isFileTypeAccepted(event.getFileName(),
                                event.getContentType(), mimeTypes,
                                extensions)) {
                    event.reject(
                            "File type not allowed: " + event.getFileName());
                    return;
                }
                delegate.handleUploadRequest(event);
            }

            @Override
            public void responseHandled(
                    com.vaadin.flow.server.streams.UploadResult result) {
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

            @Override
            public String getUrlPostfix() {
                return delegate.getUrlPostfix();
            }

            @Override
            public boolean isAllowInert() {
                return true;
            }

            @Override
            public DisabledUpdateMode getDisabledUpdateMode() {
                return delegate.getDisabledUpdateMode();
            }
        };
    }
}
