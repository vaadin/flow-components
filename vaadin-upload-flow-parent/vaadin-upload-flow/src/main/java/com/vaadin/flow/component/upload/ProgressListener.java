/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload;

import java.io.Serializable;

/**
 * ProgressListener receives events to track progress of upload.
 */
@FunctionalInterface
public interface ProgressListener extends Serializable {

    /**
     * Updates progress to listener.
     *
     * @param readBytes
     *            bytes transferred
     * @param contentLength
     *            total size of file currently being uploaded, -1 if unknown
     */
    void updateProgress(long readBytes, long contentLength);
}
