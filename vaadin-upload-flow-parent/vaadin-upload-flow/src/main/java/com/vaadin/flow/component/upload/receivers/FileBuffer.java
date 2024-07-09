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
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import com.vaadin.flow.component.upload.Receiver;

/**
 * Basic in file receiver implementation. File is stored by default to File
 * created using {@link java.io.File#createTempFile(String, String)} with a null
 * suffix.
 * <p>
 * For a custom file the constructor {@link AbstractFileBuffer(FileFactory)}
 * should be used.
 */
public class FileBuffer extends AbstractFileBuffer implements Receiver {

    private FileData file;

    @Override
    public OutputStream receiveUpload(String fileName, String MIMEType) {
        FileOutputStream outputBuffer = createFileOutputStream(fileName);
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
     * Get the output stream for file.
     *
     * @return file output stream or null if not available
     */
    public FileDescriptor getFileDescriptor() {
        if (file != null) {
            try {
                return ((FileOutputStream) file.getOutputBuffer()).getFD();
            } catch (IOException e) {
                getLogger().log(Level.WARNING,
                        "Failed to get file descriptor for: '" + getFileName()
                                + "'",
                        e);
            }
        }
        return null;
    }

    /**
     * Get the input stream for file.
     *
     * @return input stream for file or empty stream if file not found
     */
    public InputStream getInputStream() {
        if (file != null) {
            final File path = file.getFile();
            try {
                return new FileInputStream(path);
            } catch (IOException e) {
                getLogger().log(Level.WARNING,
                        "Failed to create InputStream for: '" + getFileName()
                                + "'",
                        e);
            }
        }
        return new ByteArrayInputStream(new byte[0]);
    }
}
