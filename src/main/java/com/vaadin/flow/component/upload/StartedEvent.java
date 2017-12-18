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
package com.vaadin.flow.component.upload;

import com.vaadin.flow.component.ComponentEvent;

/**
 * Upload.StartedEvent event is sent when the upload is started to received.
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
     *
     * @param source
     *            the source of the file.
     * @param filename
     *            the received file name.
     * @param MIMEType
     *            the MIME type of the received file.
     * @param contentLength
     *            the length of the received file.
     */
    public StartedEvent(Upload source, String filename, String MIMEType,
            long contentLength) {
        super(source, false);
        this.filename = filename;
        type = MIMEType;
        length = contentLength;
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
     * @return the filename.
     */
    public String getFilename() {
        return filename;
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
     * @return the length of the file that is being uploaded
     */
    public long getContentLength() {
        return length;
    }

}
