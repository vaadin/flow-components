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
package com.vaadin.ui.upload.events;

import com.vaadin.ui.upload.Upload;

/**
 * Upload.FailedEvent event is sent when the upload is received, but the
 * reception is interrupted for some reason.
 *
 * @author Vaadin Ltd.
 */
public class FailedEvent extends FinishedEvent {

    private Exception reason = null;

    /**
     *
     * @param source
     *            the source of the file.
     * @param filename
     *            the received file name.
     * @param MIMEType
     *            the MIME type of the received file.
     * @param length
     *            the length of the received file.
     * @param reason
     *            exception that failed the upload
     */
    public FailedEvent(Upload source, String filename, String MIMEType,
            long length, Exception reason) {
        this(source, filename, MIMEType, length);
        this.reason = reason;
    }

    /**
     *
     * @param source
     *            the source of the file.
     * @param filename
     *            the received file name.
     * @param MIMEType
     *            the MIME type of the received file.
     * @param length
     *            the length of the received file.
     */
    public FailedEvent(Upload source, String filename, String MIMEType,
            long length) {
        super(source, filename, MIMEType, length);
    }

    /**
     * Gets the exception that caused the failure.
     *
     * @return the exception that caused the failure, null if n/a
     */
    public Exception getReason() {
        return reason;
    }

}
