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

import com.vaadin.flow.component.ComponentEvent;

/**
 * Sent when an upload fails on the client side, e.g. because the server
 * responded with an error status or could not be reached. This also covers
 * requests that never reach the application, e.g. uploads blocked by a reverse
 * proxy or a web application firewall.
 * <p>
 * This event is based on information reported by the browser and should be used
 * for informational purposes only, such as showing a notification.
 *
 * @author Vaadin Ltd.
 * @since 25.3
 */
public class UploadErrorEvent extends ComponentEvent<Upload> {

    private final String fileName;

    private final UploadErrorReason reason;

    private final int statusCode;

    /**
     * Creates a new event.
     *
     * @param source
     *            the source component
     * @param reason
     *            the reason why the upload failed
     * @param fileName
     *            the name of the file that failed to upload
     * @param statusCode
     *            the HTTP status code of the upload response, or 0 if the
     *            request did not complete
     */
    public UploadErrorEvent(Upload source, UploadErrorReason reason,
            String fileName, int statusCode) {
        super(source, true);
        this.reason = reason;
        this.fileName = fileName;
        this.statusCode = statusCode;
    }

    /**
     * Gets the name of the file that failed to upload.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the reason why the upload failed.
     *
     * @return the failure reason
     */
    public UploadErrorReason getReason() {
        return reason;
    }

    /**
     * Gets the HTTP status code of the upload response.
     *
     * @return the HTTP status code, or 0 if the request did not complete (e.g.
     *         network error or timeout)
     */
    public int getStatusCode() {
        return statusCode;
    }
}
