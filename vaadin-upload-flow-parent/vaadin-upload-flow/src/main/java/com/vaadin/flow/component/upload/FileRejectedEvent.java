/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
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

    private String errorMessage;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source
     *            the source component
     * @param errorMessage
     *            the error message
     */
    public FileRejectedEvent(Upload source, String errorMessage) {
        super(source, true);
        this.errorMessage = errorMessage;
    }

    /**
     * Get the error message
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
