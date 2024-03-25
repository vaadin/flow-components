/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
     * Event constructor method to construct a new progress event.
     *
     * @param source
     *            the source of the file
     * @param readBytes
     *            bytes transferred
     * @param contentLength
     *            total size of file currently being uploaded, -1 if unknown
     */
    public ProgressUpdateEvent(Upload source, long readBytes,
            long contentLength) {
        super(source, false);
        this.readBytes = readBytes;
        this.contentLength = contentLength;
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
}
