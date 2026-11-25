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
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Utility methods for Upload and UploadManager components.
 * <p>
 * Intended only for internal use and can be removed or changed in the future.
 * 
 * @since 25.1
 */
public class UploadHelper implements Serializable {

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
     * {@link UploadHandler} methods are delegated to the original handler.
     * <p>
     * The MIME type and extension lists are retrieved at request time via the
     * provided suppliers, so changes made after wrapping are reflected
     * immediately.
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
                return delegate.isAllowInert();
            }

            @Override
            public DisabledUpdateMode getDisabledUpdateMode() {
                return delegate.getDisabledUpdateMode();
            }
        };
    }
}
