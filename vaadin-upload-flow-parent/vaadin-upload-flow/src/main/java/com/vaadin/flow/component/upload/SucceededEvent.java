/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.upload;

/**
 * SucceededEvent event is sent when the upload is received successfully.
 *
 * @author Vaadin Ltd.
 */
public class SucceededEvent extends FinishedEvent {

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
    public SucceededEvent(Upload source, String fileName, String mimeType,
            long length) {
        super(source, fileName, mimeType, length);
    }

}
