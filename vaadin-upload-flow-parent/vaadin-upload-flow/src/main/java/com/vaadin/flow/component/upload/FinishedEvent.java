/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.upload;

import com.vaadin.flow.component.ComponentEvent;

/**
 * FinishedEvent is sent when the upload receives a file, regardless of whether
 * the reception was successful or failed. If you wish to distinguish between
 * the two cases, use either SucceededEvent or FailedEvent, which are both
 * subclasses of the FinishedEvent.
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
     * Create an instance of the event.
     *
     * @param source
     *            the source of the file
     * @param fileName
     *            the received file name
     * @param mimeType
     *            the MIME type of the received file
     * @param length
     *            the length of the received file
     */
    public FinishedEvent(Upload source, String fileName, String mimeType,
            long length) {
        super(source, false);
        type = mimeType;
        this.fileName = fileName;
        this.length = length;
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
     * Get the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the MIME Type of the file.
     *
     * @return the MIME type
     */
    public String getMIMEType() {
        return type;
    }

    /**
     * Get the length of the file.
     *
     * @return the length
     */
    public long getContentLength() {
        return length;
    }

}
