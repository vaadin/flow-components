/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.upload;

import java.io.OutputStream;
import java.io.Serializable;

/**
 * Interface that must be implemented by the upload receivers to provide the
 * Upload component an output stream to write the uploaded data.
 *
 * @author Vaadin Ltd.
 */
@FunctionalInterface
public interface Receiver extends Serializable {

    /**
     * Invoked when a new upload arrives.
     *
     * @param fileName
     *            the desired filename of the upload, usually as specified by
     *            the client
     * @param mimeType
     *            the MIME type of the uploaded file
     * @return stream to which the uploaded file should be written
     */
    OutputStream receiveUpload(String fileName, String mimeType);
}
