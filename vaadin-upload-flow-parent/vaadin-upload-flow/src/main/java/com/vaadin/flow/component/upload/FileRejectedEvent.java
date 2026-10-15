/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.upload;

import com.vaadin.flow.component.ComponentEvent;

/**
 * Sent when the file selected for upload doesn't meet the constraints specified
 * on {@link Upload}
 *
 * @author Vaadin Ltd.
 */
public class FileRejectedEvent extends ComponentEvent<Upload> {

    private final String fileName;

    private final String errorMessage;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @deprecated since 24.4. Use
     *             {@link #FileRejectedEvent(Upload, String, String)}
     *
     * @param source
     *            the source component
     * @param errorMessage
     *            the error message
     */
    @Deprecated(since = "24.4")
    public FileRejectedEvent(Upload source, String errorMessage) {
        this(source, errorMessage, null);
    }

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source
     *            the source component
     * @param errorMessage
     *            the error message
     * @param fileName
     *            the rejected file name
     */
    public FileRejectedEvent(Upload source, String errorMessage,
            String fileName) {
        super(source, true);
        this.errorMessage = errorMessage;
        this.fileName = fileName;
    }

    /**
     * Get the error message
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
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
