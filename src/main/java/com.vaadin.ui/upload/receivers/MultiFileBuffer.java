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
import java.util.logging.Logger;

import com.vaadin.ui.upload.MultiFileReceiver;

/**
 * Basic multi file in memory file receiver implementation.
 */
public class MultiFileBuffer extends AbstractFileBuffer implements MultiFileReceiver {

    private Map<String, FileData> files = new HashMap<>();

    @Override
    public OutputStream receiveUpload(String fileName, String MIMEType) {
        FileOutputStream outputBuffer = createFileOutputStream(fileName);
        files.put(fileName, new FileData(fileName, MIMEType, outputBuffer));

        return outputBuffer;
    }

    /**
     * Get the files in memory for this buffer.
     * 
     * @return files in memory
     */
    public Set<String> getFiles() {
        return files.keySet();
    }

    /**
     * Get file data for upload with file name
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
                return new FileInputStream(((FileOutputStream) files
                        .get(fileName).getOutputBuffer()).getFD());
            } catch (IOException e) {
                getLogger().log(Level.WARNING,
                        "Failed to create InputStream for: '" + fileName + "'",
                        e);
            }
        }
        return new ByteArrayInputStream(new byte[0]);
    }
}
