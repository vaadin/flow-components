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

import com.vaadin.flow.component.ComponentEvent;

/**
 * StartedEvent event is sent when the upload is started to received.
 *
 * @author Vaadin Ltd.
 */
public class StartedEvent extends ComponentEvent<Upload> {

    private final String filename;
    private final String type;
    /**
     * Length of the received file.
     */
    private final long length;

    /**
     * Create an instance of the event.
     *
     * @param source
     *            the source of the file
     * @param fileName
     *            the received file name
     * @param mimeType
     *            the MIME type of the received file
     * @param contentLength
     *            the length of the received file
     */
    public StartedEvent(Upload source, String fileName, String mimeType,
            long contentLength) {
        super(source, false);
        this.filename = fileName;
        type = mimeType;
        length = contentLength;
    }

    /**
     * Upload where the event occurred.
     *
     * @return the source of the event
     */
    public Upload getUpload() {
        return getSource();
    }

    /**
     * Get the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return filename;
    }

    /**
     * Get the MIME type of the file.
     *
     * @return the MIME type
     */
    public String getMIMEType() {
        return type;
    }

    /**
     * Get the length of the file.
     *
     * @return the length of the file that is being uploaded
     */
    public long getContentLength() {
        return length;
    }

}
