/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.upload;

/**
 * FailedEvent event is sent when the upload is received, but the reception is
 * interrupted for some reason.
 *
 * @author Vaadin Ltd.
 */
public class FailedEvent extends FinishedEvent {

    private Exception reason = null;

    /**
     * Create an instance of the event.
     *
     * @param source
     *            the source of the file
     * @param filename
     *            the received file name
     * @param mimeType
     *            the MIME type of the received file
     * @param length
     *            the number of uploaded bytes
     * @param reason
     *            exception that failed the upload
     */
    public FailedEvent(Upload source, String filename, String mimeType,
            long length, Exception reason) {
        this(source, filename, mimeType, length);
        this.reason = reason;
    }

    /**
     * Create an instance of the event.
     *
     * @param source
     *            the source of the file
     * @param filename
     *            the received file name
     * @param MIMEType
     *            the MIME type of the received file
     * @param length
     *            the number of uploaded bytes
     */
    public FailedEvent(Upload source, String filename, String MIMEType,
            long length) {
        super(source, filename, MIMEType, length);
    }

    /**
     * Get the exception that caused the failure.
     *
     * @return the exception that caused the failure, null if n/a
     */
    public Exception getReason() {
        return reason;
    }

    /**
     * Get the number of uploaded bytes.
     *
     * @return the number of uploaded bytes
     */
    @Override
    public long getContentLength() {
        return super.getContentLength();
    }

}
