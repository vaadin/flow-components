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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;

/**
 * FileOutputStream with a reference to the output file.
 */
public class UploadOutputStream extends FileOutputStream
        implements Serializable {
    private final File file;

    /**
     * @see FileOutputStream#FileOutputStream(File)
     * @param file
     *            File to write.
     * @throws FileNotFoundException
     *             see {@link FileOutputStream#FileOutputStream(File)}
     */
    public UploadOutputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    /**
     * @return File written by this output stream.
     */
    public File getFile() {
        return file;
    }
}
