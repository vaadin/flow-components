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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.vaadin.flow.component.upload.Receiver;

/**
 * Basic in memory file receiver implementation.
 */
public class MemoryBuffer implements Receiver {

    private FileData file;

    @Override
    public OutputStream receiveUpload(String fileName, String MIMEType) {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        file = new FileData(fileName, MIMEType, outputBuffer);

        return outputBuffer;
    }

    /**
     * Get the file data object.
     *
     * @return file data for the latest upload or null
     */
    public FileData getFileData() {
        return file;
    }

    /**
     * Get the file name for this buffer.
     *
     * @return file name or empty if no file
     */
    public String getFileName() {
        return file != null ? file.getFileName() : "";
    }

    /**
     * Get the input stream for file with filename.
     *
     * @return input stream for file or empty stream if file not found
     */
    public InputStream getInputStream() {
        if (file != null) {
            return new ByteArrayInputStream(
                    ((ByteArrayOutputStream) file.getOutputBuffer())
                            .toByteArray());
        }
        return new ByteArrayInputStream(new byte[0]);
    }
}
