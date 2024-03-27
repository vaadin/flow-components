/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.upload.receivers;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Class containing file information for upload.
 */
public class FileData implements Serializable {
    private final String fileName, mimeType;
    private final OutputStream outputBuffer;

    /**
     * Create a FileData instance for a file.
     *
     * @param fileName
     *            the file name
     * @param mimeType
     *            the file MIME type
     * @param outputBuffer
     *            the output buffer where to write the file
     */
    public FileData(String fileName, String mimeType,
            OutputStream outputBuffer) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.outputBuffer = outputBuffer;
    }

    /**
     * Return the mimeType of this file.
     *
     * @return mime types of the files
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Return the name of this file.
     *
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Return the output buffer for this file data.
     *
     * @return output buffer
     */
    public OutputStream getOutputBuffer() {
        return outputBuffer;
    }

    /**
     *
     * @return Temporary file containing the uploaded data.
     * @throws NullPointerException
     *             if outputBuffer is null
     * @throws UnsupportedOperationException
     *             if outputBuffer is not an {@link UploadOutputStream}
     */
    public File getFile() {
        if (outputBuffer == null) {
            throw new NullPointerException("OutputBuffer is null");
        }
        if (outputBuffer instanceof UploadOutputStream) {
            return ((UploadOutputStream) outputBuffer).getFile();
        }
        final String MESSAGE = String.format(
                "%s not supported. Use a UploadOutputStream",
                outputBuffer.getClass());
        throw new UnsupportedOperationException(MESSAGE);
    }
}
