/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.ui.upload.receivers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class for common file receiver buffers.
 */
public abstract class AbstractFileBuffer {

    private FileFactory factory;

    /**
     * Constructor for creating a file buffer with the default file factory.
     * <p>
     * Files will be created using {@link File#createTempFile(String, String)}
     * and have that build 'upload_tmpfile_{FILENAME}_{currentTimeMillis}'
     */
    public AbstractFileBuffer() {
        factory = fileName -> {
            final String tempFileName = "upload_tmpfile_" + fileName + "_"
                    + System.currentTimeMillis();
            return File.createTempFile(tempFileName, null);
        };
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
     * Create a file output stream for filename
     * 
     * @param fileName
     * @return
     */
    protected FileOutputStream createFileOutputStream(String fileName) {
        try {
            return new FileOutputStream(factory.createFile(fileName));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE,
                    "Failed to create file output stream for: '" + fileName
                            + "'",
                    e);
        }
        return null;
    }

    protected Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }
}
