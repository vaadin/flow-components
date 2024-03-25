/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
