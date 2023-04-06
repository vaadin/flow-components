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
