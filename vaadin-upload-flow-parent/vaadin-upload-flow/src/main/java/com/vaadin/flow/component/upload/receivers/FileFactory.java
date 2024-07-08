/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.receivers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * File factory interface for generating file to store the uploaded data into.
 */
public interface FileFactory extends Serializable {

    /**
     * Create a new file for given file name.
     *
     * @param fileName
     *            file name to create file for
     * @return {@link File} that should be used
     */
    File createFile(String fileName) throws IOException;
}
