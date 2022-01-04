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
