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

import com.vaadin.ui.event.ComponentEvent;
import com.vaadin.ui.upload.Upload;

/**
 * Upload.FinishedEvent is sent when the upload receives a file, regardless of
 * whether the reception was successful or failed. If you wish to distinguish
 * between the two cases, use either SucceededEvent or FailedEvent, which are
 * both subclasses of the FinishedEvent.
 *
 * @author Vaadin Ltd.
 */
public class FinishedEvent extends ComponentEvent<Upload> {

    /**
     * Length of the received file.
     */
    private final long length;

    /**
     * MIME type of the received file.
     */
    private final String type;

    /**
     * Received file name.
     */
    private final String fileName;

    /**
     * @param source
     *         the source of the file.
     * @param fileName
     *         the received file name.
     * @param MIMEType
     *         the MIME type of the received file.
     * @param length
     *         the length of the received file.
     */
    public FinishedEvent(Upload source, String fileName, String MIMEType,
            long length) {
        super(source, false);
        type = MIMEType;
        this.fileName = fileName;
        this.length = length;
    }

    /**
     * Uploads where the event occurred.
     *
     * @return the Source of the event.
     */
    public Upload getUpload() {
        return getSource();
    }

    /**
     * Gets the file name.
     *
     * @return the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the MIME Type of the file.
     *
     * @return the MIME type.
     */
    public String getMIMEType() {
        return type;
    }

    /**
     * Gets the length of the file.
     *
     * @return the length.
     */
    public long getLength() {
        return length;
    }

}
