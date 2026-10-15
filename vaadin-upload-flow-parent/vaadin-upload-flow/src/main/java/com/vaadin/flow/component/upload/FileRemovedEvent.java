/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.upload;

import com.vaadin.flow.component.ComponentEvent;

/**
 * Sent when a file selected for upload is removed.
 *
 * @author Vaadin Ltd.
 */
public class FileRemovedEvent extends ComponentEvent<Upload> {

    private final String fileName;

    /**
     * Creates a new event using the given source and the removed file name.
     *
     * @param source
     *            the source component
     * @param fileName
     *            the removed file name
     */
    public FileRemovedEvent(Upload source, String fileName) {
        super(source, true);
        this.fileName = fileName;
    }

    /**
     * Get the removed file name.
     *
     * @return the removed file name
     */
    public String getFileName() {
        return fileName;
    }
}
