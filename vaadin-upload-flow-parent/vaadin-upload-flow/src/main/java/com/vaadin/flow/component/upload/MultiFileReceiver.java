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
 * Interface that must be implemented by the upload receivers to provide the
 * Upload component an output stream to write the uploaded data.
 * <p>
 * Informs the upload component that multi file upload is supported.
 *
 * @author Vaadin Ltd.
 */
@FunctionalInterface
public interface MultiFileReceiver extends Receiver {
}
