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

import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Utility methods for Upload and UploadManager components.
 * <p>
 * Intended only for internal use and can be removed or changed in the future.
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
