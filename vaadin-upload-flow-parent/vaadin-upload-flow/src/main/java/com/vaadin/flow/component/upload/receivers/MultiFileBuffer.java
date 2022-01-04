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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.vaadin.flow.component.upload.MultiFileReceiver;

/**
 * Basic receiver implementation for receiving multiple file upload and storing
 * them as files. Files are stored by default to Files created using
 * {@link java.io.File#createTempFile(String, String)} with a null suffix.
 * <p>
 * For a custom file the constructor {@link AbstractFileBuffer(FileFactory)}
 * should be used.
 */
public class MultiFileBuffer extends AbstractFileBuffer
        implements MultiFileReceiver {

    private Map<String, FileData> files = new HashMap<>();

    /**
     * Creates a file buffer with a default file factory.
     * <p>
     * Files will be created using {@link File#createTempFile(String, String)}
     * and have that build 'upload_tmpfile_{FILENAME}_{currentTimeMillis}'
     */
    public MultiFileBuffer() {
        super();
    }

    /**
     * Creates a file buffer that uses a file factory to create custom upload
     * {@link File}s.
     *
     * @param factory
     *            file factory for file buffer
     */
    public MultiFileBuffer(FileFactory factory) {
        super(factory);
    }

    @Override
    public OutputStream receiveUpload(String fileName, String mimeType) {
        FileOutputStream outputBuffer = createFileOutputStream(fileName);
        files.put(fileName, new FileData(fileName, mimeType, outputBuffer));

        return outputBuffer;
    }

    /**
     * Get the files stored for this buffer.
     *
     * @return files stored
     */
    public Set<String> getFiles() {
        return files.keySet();
    }

    /**
     * Get file data for upload with file name.
     *
     * @param fileName
     *            file name to get upload data for
     * @return file data for filename or null if not found
     */
    public FileData getFileData(String fileName) {
        return files.get(fileName);
    }

    /**
     * Get the output stream for file.
     *
     * @param fileName
     *            name of file to get stream for
     * @return file output stream or null if not available
     */
    public FileDescriptor getFileDescriptor(String fileName) {
        if (files.containsKey(fileName)) {
            try {
                return ((FileOutputStream) files.get(fileName)
                        .getOutputBuffer()).getFD();
            } catch (IOException e) {
                getLogger().log(Level.WARNING,
                        "Failed to get file descriptor for: '" + fileName + "'",
                        e);
            }
        }
        return null;
    }

    /**
     * Get the input stream for file with fileName.
     *
     * @param fileName
     *            name of file to get input stream for
     * @return input stream for file or empty stream if file not found
     */
    public InputStream getInputStream(String fileName) {
        if (files.containsKey(fileName)) {
            try {
                return new FileInputStream(files.get(fileName).getFile());
            } catch (IOException e) {
                getLogger().log(Level.WARNING,
                        "Failed to create InputStream for: '" + fileName + "'",
                        e);
            }
        }
        return new ByteArrayInputStream(new byte[0]);
    }
}
