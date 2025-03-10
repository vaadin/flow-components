/*
 * Copyright 2000-2025 Vaadin Ltd.
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
 * ProgressUpdateEvent is sent to track progress of upload.
 *
 * @author Vaadin Ltd.
 */
public class ProgressUpdateEvent extends ComponentEvent<Upload> {

    /**
     * Bytes transferred.
     */
    private final long readBytes;

    /**
     * Total size of file currently being uploaded, -1 if unknown
     */
    private final long contentLength;

    /**
     * Name of file currently being uploaded
     */
    private final String fileName;

    /**
     * Event constructor method to construct a new progress event.
     *
     * @deprecated since 24.4. Use
     *             {@link #ProgressUpdateEvent(Upload, long, long, String)}
     *
     * @param source
     *            the source of the file
     * @param readBytes
     *            bytes transferred
     * @param contentLength
     *            total size of file currently being uploaded, -1 if unknown
     */
    @Deprecated(since = "24.4")
    public ProgressUpdateEvent(Upload source, long readBytes,
            long contentLength) {
        this(source, readBytes, contentLength, null);
    }

    /**
     * Event constructor method to construct a new progress event.
     *
     * @param source
     *            the source of the file
     * @param readBytes
     *            bytes transferred
     * @param contentLength
     *            total size of file currently being uploaded, -1 if unknown
     * @param fileName
     *            name of file currently being uploaded
     */
    public ProgressUpdateEvent(Upload source, long readBytes,
            long contentLength, String fileName) {
        super(source, false);
        this.readBytes = readBytes;
        this.contentLength = contentLength;
        this.fileName = fileName;
    }

    /**
     * Upload where the event occurred.
     *
     * @return the Source of the event
     */
    public Upload getUpload() {
        return getSource();
    }

    /**
     * Get bytes transferred for this update.
     *
     * @return bytes transferred
     */
    public long getReadBytes() {
        return readBytes;
    }

    /**
     * Get total file size.
     *
     * @return total file size or -1 if unknown
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Get the file name.
     *
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }
}
