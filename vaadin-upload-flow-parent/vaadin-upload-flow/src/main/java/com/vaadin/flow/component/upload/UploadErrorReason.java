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

/**
 * Reasons why an upload can fail on the client side, as reported by the
 * {@code upload-error} event of the upload web component.
 *
 * @author Vaadin Ltd.
 * @since 25.3
 */
public enum UploadErrorReason {

    /**
     * The server could not be reached, e.g. due to a network error.
     */
    SERVER_UNAVAILABLE("serverUnavailable"),

    /**
     * The server responded with an unexpected error (HTTP status 500 or above).
     */
    UNEXPECTED_SERVER_ERROR("unexpectedServerError"),

    /**
     * The server rejected the upload because the file is too large (HTTP status
     * 413).
     */
    FILE_TOO_LARGE("fileTooLarge"),

    /**
     * The server rejected the upload (HTTP status 400-499, excluding 413). This
     * also covers requests blocked before reaching the application, e.g. by a
     * reverse proxy or a web application firewall.
     */
    FORBIDDEN("forbidden"),

    /**
     * The upload request timed out.
     */
    TIMEOUT("timeout"),

    /**
     * An unrecognized failure reason.
     */
    UNKNOWN(null);

    private final String clientCode;

    UploadErrorReason(String clientCode) {
        this.clientCode = clientCode;
    }

    /**
     * Returns the reason matching the given client-side error code, or
     * {@link #UNKNOWN} if no match is found.
     *
     * @param clientCode
     *            the error code from the client-side upload manager
     * @return the matching reason, or {@link #UNKNOWN}
     */
    public static UploadErrorReason fromClientCode(String clientCode) {
        for (UploadErrorReason reason : values()) {
            if (reason.clientCode != null
                    && reason.clientCode.equals(clientCode)) {
                return reason;
            }
        }
        return UNKNOWN;
    }

    /**
     * Returns the reason for the given HTTP status code of a failed upload
     * request, using the same mapping as the client-side upload component, or
     * {@link #UNKNOWN} if the status code does not indicate a failure.
     *
     * @param statusCode
     *            the HTTP status code of the upload response, or 0 if the
     *            request did not complete
     * @return the matching reason, or {@link #UNKNOWN}
     */
    public static UploadErrorReason fromStatusCode(int statusCode) {
        if (statusCode == 0) {
            return SERVER_UNAVAILABLE;
        } else if (statusCode >= 500) {
            return UNEXPECTED_SERVER_ERROR;
        } else if (statusCode == 413) {
            return FILE_TOO_LARGE;
        } else if (statusCode >= 400) {
            return FORBIDDEN;
        }
        return UNKNOWN;
    }
}
