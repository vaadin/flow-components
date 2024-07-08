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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class for common file receiver buffers.
 */
public abstract class AbstractFileBuffer implements Serializable {

    private FileFactory factory;

    /**
     * Constructor for creating a file buffer with the default temporary file
     * factory.
     */
    public AbstractFileBuffer() {
        factory = new TemporaryFileFactory();
    }

    /**
     * Constructor taking in the file factory used to create upload
     * {@link File}.
     *
     * @param factory
     *            file factory for file buffer
     */
    public AbstractFileBuffer(FileFactory factory) {
        this.factory = factory;
    }

    /**
     * Create a file output stream for the file.
     *
     * @param fileName
     *            the name of the file
     * @return the file output stream
     */
    protected FileOutputStream createFileOutputStream(String fileName) {
        try {
            return new UploadOutputStream(factory.createFile(fileName));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE,
                    "Failed to create temporary file output stream", e);
        }
        return null;
    }

    protected Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }
}
