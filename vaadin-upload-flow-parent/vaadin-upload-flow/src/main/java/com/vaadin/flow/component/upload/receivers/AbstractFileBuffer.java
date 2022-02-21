/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.upload.receivers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
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
